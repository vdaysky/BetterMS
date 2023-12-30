package obfuscate.game.core;

import obfuscate.MsdmPlugin;
import obfuscate.event.LocalEvent;
import obfuscate.event.LocalPriority;
import obfuscate.event.custom.gamestate.RoundTimerEndEvent;
import obfuscate.event.custom.gamestate.RoundResetEvent;
import obfuscate.event.custom.intent.PlayerTeamChangeIntentEvent;
import obfuscate.event.custom.damage.PlayerDeathEvent;
import obfuscate.event.custom.game.GameStartedEvent;
import obfuscate.event.custom.game.PreGameEndEvent;
import obfuscate.event.custom.objective.BombExplodeEvent;
import obfuscate.event.custom.player.PlayerLeaveGameEvent;
import obfuscate.event.custom.player.PlayerRosterChangeEvent;
import obfuscate.event.custom.round.OvertimeStartEvent;
import obfuscate.event.custom.round.RoundWinEvent;
import obfuscate.event.custom.round.RoundStartEvent;
import obfuscate.event.custom.team.PostSideSwapEvent;
import obfuscate.event.custom.team.RosterPostTeamChangeEvent;
import obfuscate.game.player.StrikePlayer;
import obfuscate.game.state.GeneralGameStage;
import obfuscate.logging.Logger;
import obfuscate.logging.Tag;
import obfuscate.team.InGameTeamData;
import obfuscate.team.StrikeTeam;
import obfuscate.game.config.ConfigField;
import obfuscate.message.MsgSender;
import obfuscate.util.Position;
import obfuscate.util.Promise;
import obfuscate.util.chat.C;
import obfuscate.util.java.DefaultMap;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import javax.annotation.Nullable;
import java.util.*;

/** team games have teams. :+1:
 * TeamGame class adds rounds as well. */
public abstract class TeamGame extends PausableGame
{
    protected final HashMap<InGameTeamData, Integer> scoreboard = new DefaultMap<>(x -> 0);

    // Round Winners
    protected InGameTeamData winner = null;
    protected InGameTeamData looser = null;

    private boolean isOvertime = false;
    private int overtimeIndex = -1;

    private int roundInProgress = 1;

    private final HashMap<InGameTeamData, Integer> looseStreak = new HashMap<>();

    /**
     * Reset all data related to previous round. After this method is called previous round is forgotten.
     * @see RoundResetEvent
     * */
    public void resetRoundInfo()
    {
        // RoundResetEvent was last event to access winner and looser of round ended.
        // everything from now relates to new round.
        winner = looser = null;

        // this will do basic data reset
        resetGameInfo();
    }

    /** Prepares new round by reloading guns and respawning players.
     * Called after round ends or after game starts.
     * @see RoundStartEvent
     * */
    public void startNewRound() {
        new RoundStartEvent(this).trigger();

        for (StrikePlayer player : this.getOnlineDeadOrAliveParticipants()) {
            getGameSession(player).setScoped(false);
            player.reloadAll(this);

            // stop spectating
            setSpectatorTarget(player, null);

            respawn(player,false);
        }
    }

    @Override
    public void initGame() {
        super.initGame();
    }

    public @Nullable InGameTeamData getCurrentRoundWinner() {
        return winner;
    }

    public void resetRoundCount()
    {
        roundInProgress = 1;
        looseStreak.put(getTeamA(), 1);
        looseStreak.put(getTeamB(), 1);
        scoreboard.put(getTeamA(), 0);
        scoreboard.put(getTeamB(), 0);
    }

    public InGameTeamData getRoster(StrikeTeam team)
    {
        if (getTeamA().getTeam() == team)
            return getTeamA();
        return getTeamB();
    }

    /** Make team win a round: trigger event, add win */
    public void winRound(StrikeTeam winningTeam, RoundWinEvent.Reason reason) {

        // avoid double win caused by live state end (skip) after team elimination
        if (winner != null) {
            return;
        }

        winner = this.getRoster(winningTeam);
        looser = this.getRoster(winningTeam.getOpposite());

        this.addWonRound(winner);

        String winnerName = winner.getNiceName();
        int scoreA = getTotalScore(getTeamA());
        int scoreB = getTotalScore(getTeamB());

        StrikePlayer mvp;
        mvp = winner.getOnlineParticipants(this).stream()
                .max(Comparator.comparingInt((player) -> roundKillStreak.getOrDefault(player, 0)))
                .orElse(null);

//        String mvpReason;
//
//        if (reason == RoundWinEvent.Reason.OBJECTIVE) {
//            if (winner.getTeam() == StrikeTeam.T) {
//                mvp = getBomb().getPlanter();
//                mvpReason = "planting the bomb";
//            } else {
//                mvp = getBomb().getDefuser();
//                mvpReason = "defusing the bomb";
//            }
//        } else {
//            // get player with most kills in winning roster
//            mvp = winner.getOnlineParticipants(this).stream()
//                    .max(Comparator.comparingInt((player) -> roundKillStreak.getOrDefault(player, 0)))
//                    .orElse(null);
//
//            mvpReason = "most eliminations";
//        }

        String readableWinReason = "By glitching the game";

        switch (reason) {
            case OBJECTIVE -> {
                if (winner.getTeam() == StrikeTeam.T) {
                    readableWinReason = getBomb().getPlanter().getActualName() + " destroyed the bomb site!";
                } else {
                    readableWinReason =  getBomb().getDefuser().getActualName() + " defused the bomb!";
                }
            }
            case ELIMINATION -> readableWinReason = looser.getNiceName() + " were eliminated!";
            case TIME_RUN_OUT -> readableWinReason = "Bomb sites were successfully defended!";
        }

        String winnerColor = winner.getTeam().getColor();

        broadcastSubtitle(
            winnerColor + ChatColor.BOLD + winnerName + " has won the round!"
        );

        broadcastChat(MsgSender.NONE, "");
        broadcastChat(MsgSender.NONE, winnerColor + "===================================");
        broadcastChat(MsgSender.NONE, "");
        broadcastChat(MsgSender.NONE, winnerColor + ChatColor.BOLD + winnerName + " has won the round!");
        broadcastChat(MsgSender.NONE, C.cWhite + readableWinReason);

        if (mvp != null) {
            var mvpKills = roundKillStreak.getOrDefault(mvp, 0);
            broadcastChat(MsgSender.NONE, C.cWhite + "MVP ✮ " + winnerColor + mvp.getName() + C.cWhite + ": " + mvpKills + " kills");
        }

        broadcastChat(MsgSender.NONE, "");
        broadcastChat(MsgSender.NONE, winnerColor + "===================================");

        Logger.info("Team " + winnerName + " won round " +  + roundInProgress + " by" +
                reason + ". Score: " + scoreA + ":" + scoreB, this, Tag.GAME_LIFECYCLE);

        new RoundWinEvent(this, winner, getRoundNumber(), reason).trigger();
    }

    /** handlers specific to round-based games
     * Game class will handle basic events (like game ending) for us
     * This handler will process live stage that was ended naturally.
     * Also, it will reset round when we run out of states in stack
     * */
    @LocalEvent
    private void winOnRoundEndAndResetRoundOnStackEnd(RoundTimerEndEvent e)
    {
        if (!isInProgress() || isEnded())
            return;

        winRound(StrikeTeam.CT, RoundWinEvent.Reason.TIME_RUN_OUT); // time ran out
//
//        // round ended (RoundWinEvent) (earlier)
//        // make sure live wasn't replaced by bomb planted or something
//        if (e.getEndedState().isLive() && e.getNextState().isRoundEnd()) {
//
//        }
//
//        // round reset (RoundResetEvent) (later)
//        // all states in state stack ended, it's time to reset round and loop back to the beginning
//        if (!getStateManager().hasNext()) {
//            if (isEnded()) {
//                return;
//            }
//
//
//            resetRoundInfo();
//            startNewRound();
//        }
    }

    @LocalEvent(priority = LocalPriority.POST)
    private void resetRoundnStackEnd(RoundResetEvent e) {
        Logger.info("RoundResetEvent", this, Tag.GAME_LIFECYCLE);
        if (isEnded()) {
            return;
        }

        resetRoundInfo();
        startNewRound();
    }

    @LocalEvent
    private void terroristsWinOnBombExplode(BombExplodeEvent e) {

        if (!isInProgress() || isEnded())
            return;

        // bomb exploded after round was won by someone
        if (winner != null) {
            return;
        }
        winRound(StrikeTeam.T, RoundWinEvent.Reason.OBJECTIVE);
        setRoundEndState();
    }

    @LocalEvent(priority = LocalPriority.POST)
    private void teamWinOnElimination(PlayerDeathEvent e) {
        InGameTeamData deadTeam = getPlayerRoster(e.getDamagee());
        checkTeamElimination(deadTeam);
    }

    @LocalEvent(priority = LocalPriority.PRE)
    private void teamWinOnElimination(PlayerLeaveGameEvent e) {
        InGameTeamData deadTeam = getPlayerRoster(e.getPlayer());
        checkTeamElimination(deadTeam);
    }

    private void checkTeamElimination(InGameTeamData deadTeam) {

        // rounds are not winnable by elimination
        if (!getConfig().getValue(ConfigField.WINS_BY_ELIMINATION).bool()) {
            return;
        }

        if (!isInProgress() || isEnded())
            return;

        // if CTs killed all Ts they still have to defuse, so the only way for them to win is to defuse
        if (deadTeam.getTeam() == StrikeTeam.T && getBomb().isActive()) {
            return;
        }

        if (!deadTeam.getAlivePlayers().isEmpty()) {
            return;
        }

        // Any team eliminated during live state
        if (getGameState().getGeneralStage() == GeneralGameStage.LIVE) {
            winRound(deadTeam.getTeam().getOpposite(), RoundWinEvent.Reason.ELIMINATION);
            // skip state after triggering round win, because ended live state triggers win itself
            setRoundEndState();
        }

        // CTs eliminated during bomb plant
        if (getGameState().getGeneralStage() == GeneralGameStage.BOMB_PLANT && deadTeam.getTeam() == StrikeTeam.CT) {
            winRound(StrikeTeam.T, RoundWinEvent.Reason.ELIMINATION);
            setRoundEndState();
        }
    }

    public InGameTeamData getRosterByShortName(String team)
    {
        for (InGameTeamData r : getRosters())
        {
            if (r.getName().equalsIgnoreCase(team))
                return r;
        }
        return null;
    }

    public void restart()
    {
        resetRoundCount();
        super.restart();
    }

    public void setRound(int round){
        this.roundInProgress = round;
    }

    public int getRoundNumber()
    {
        return roundInProgress;
    }

    public int getRelativeRoundNumber()
    {
        return getRelativeScore(getTeamA()) + getRelativeScore(getTeamB());
    }

    public void addWonRound(InGameTeamData winner)
    {
        if (winner == null)
            return;

        scoreboard.put(winner, getTotalScore(winner)+1);
        InGameTeamData looser = getRoster(winner.getTeam().getOpposite());

        looseStreak.put(looser, Math.min(4, looseStreak.get(looser)+1));
        looseStreak.put(winner, Math.max(0, looseStreak.get(looser)-1));
    }

    public int getRelativeScore(InGameTeamData roster)
    {
        int score = scoreboard.get(roster);
        int enough_to_tie = getConfig().getValue(ConfigField.MAX_ROUNDS).val() / 2;
        int ot_rounds = getConfig().getValue(ConfigField.MAX_OVERTIME_ROUNDS).val()/2;

        if (!isOvertime())
        {
            return score;
        }
        else {
            score -= overtimeIndex * ot_rounds;
        }

        score -= enough_to_tie;
        return score; // +1 bec we count from one and another +1 cuz we want to see max value
    }

    public int getRelativeScore(StrikeTeam team)
    {
        return getRelativeScore(getRoster(team));
    }

    public int getTotalScore(InGameTeamData roster)
    {
        return scoreboard.get(roster); // defaults to 0
    }

    public int getTotalScore(StrikeTeam team)
    {
        return scoreboard.get(getRoster(team)); // defaults to 0
    }

    public String formatScore(boolean abs)
    {
        int team_a_score;
        int team_b_score;
        if (abs) {
            team_a_score =  getTotalScore(getTeamA().getTeam());
            team_b_score =  getTotalScore(getTeamB().getTeam());
        } else {
            team_a_score =  getRelativeScore(getTeamA().getTeam());
            team_b_score =  getRelativeScore(getTeamB().getTeam());
        }
        return getTeamA().getTeam().getColor() + ChatColor.BOLD + team_a_score +
                    ChatColor.WHITE + ":" +
                    getTeamB().getTeam().getColor() + ChatColor.BOLD + team_b_score + ChatColor.RESET;
    }

    public List<StrikePlayer> getPlayers(StrikeTeam team) {
        return getRoster(team).getPlayers();
    }

    public List<StrikePlayer> getPlayers(StrikeTeam team, boolean online) {
        List<StrikePlayer> players = new ArrayList<>();
        for (StrikePlayer p : getPlayers(team)) {
            if (online == p.isOnline()){
                players.add(p);
            }
        }
        return players;
    }

    public boolean isOvertime()
    {
        return isOvertime;
    }

    public boolean isTeamAlive(StrikeTeam team)
    {
        return isTeamAlive(getRoster(team));
    }

    public boolean isTeamAlive(InGameTeamData roster)
    {
        for (StrikePlayer player : getOnline(roster))
        {
            // Check if player is alive and not a coach
            if (player.isAlive(this) && player.isParticipating(this))
            {
                return true;
            }
        }
        return false;
    }

    public boolean areInSameTeam(StrikePlayer p1, StrikePlayer p2)
    {
        if (p1 == null || p2 == null)
            return false;

        return getPlayerTeam(p1) == getPlayerTeam(p2);
    }

    public Location getRespawnPosition(StrikePlayer player)
    {
        return getTeamRespawnPoint(getPlayerTeam(player));
    }

    public Location getRandomRespawn(StrikePlayer player) {
        List<Position> respawns = getGameMap().getDMRespawns();
        Collections.shuffle(respawns);

        // try to get location with no line of sight to other player and with sufficient distance
        for (var pos : respawns) {
            var loc = pos.toLoc(getTempMap().getWorld());
            boolean isLocationBad = false;
            for (var other : getAlivePlayers()) {
                if (other.getLocation().distance(loc) < 5) {
                    isLocationBad = true;
                    break;
                }
                if (other.hasLineOfSight(player)) {
                    isLocationBad = true;
                    break;
                }
            }
            if (isLocationBad) continue;
            return loc;
        }

        return respawns.get(0).toLoc(getTempMap().getWorld());
    }

    /** Get any point that is not taken by teammate */
    public Location getTeamRespawnPoint(StrikeTeam team)
    {
        ArrayList<Position> spawnPos = getGameMap().getTeamRespawns(team);
        Collections.shuffle(spawnPos);

        World world = getTempMap().getWorld();

        for (var pos : spawnPos) {
            Collection<?> players = world.getNearbyEntities(pos.toLoc(world), 1, 1, 1, (e) -> e.getType() == EntityType.PLAYER);
            if (players.isEmpty()) {
                return pos.toLoc(world);
            }
        }
        return spawnPos.get(0).toLoc(world);
    }

    @Nullable
    public StrikePlayer getRandomInGameMember(StrikeTeam team)
    {
        List<StrikePlayer> members =getRoster(team).getOnlineParticipants(this);
        if (members.isEmpty())
            return null;

        return members.get((int)(members.size() * Math.random()));
    }

    @Override
    public int getMaxRounds()
    {
        if (!isOvertime())
            return super.getMaxRounds();

        return getConfig().getValue(ConfigField.MAX_OVERTIME_ROUNDS).val();
    }

    public int getOvertimeIndex()
    {
        return overtimeIndex;
    }

    @LocalEvent
    private void handleOvertimeStart(OvertimeStartEvent e)
    {
        swapSides();
    }

    public boolean shouldSwapSides() {
        int enoughToTie = (getMaxRounds()/2);
        return getRelativeRoundNumber() == enoughToTie;
    }

    // do preparations before regular handler respawns player.
    @LocalEvent(priority = LocalPriority.PRE)
    public void checkOvertimeAndSideSwap(RoundResetEvent e)
    {
        roundInProgress++;

        int enoughToTie = (getMaxRounds()/2);
        int enoughToWin = enoughToTie+1;

        // side swap check
        if (shouldSwapSides()) {
            swapSides();
        }

        // overtime check
        if (winner == null || looser == null) {
            return;
        }

        if (getRelativeScore(winner) == enoughToTie && getRelativeScore(looser) == enoughToTie)
        {
            isOvertime = true;
            overtimeIndex++;

            // will swap sides on new overtime too, other one wont
            new OvertimeStartEvent(getOvertimeIndex(), this).trigger();
        }

        // Game end check
        if (getRelativeScore(winner) == enoughToWin)
        {
            endGame(winner, looser);
        }
    }
    public int getLossCount(InGameTeamData team)
    {
        return looseStreak.get(team);
    }

    public void swapSides()
    {
        // change team will trigger team change event in strike player
        getTeamA().changeTeam();
        getTeamB().changeTeam();

        // update equipment colors, scoreboard, etc.
        for (StrikePlayer player : getOnlineDeadOrAliveParticipants()) {
            player.updateTeam(this, getPlayerTeam(player));
        }

        new PostSideSwapEvent(this).trigger();

        looseStreak.put(getTeamA(), 1);
        looseStreak.put(getTeamB(), 1);

        broadcastSubtitle("Swapping Sides", 20, 60, 20);
    }

    @LocalEvent
    private void broadcastWinMessage(PreGameEndEvent e)
    {
        if (e.getWinner() == null) {
            return;
        }

        var winnerColor = e.getWinner().getTeam().getColor();

        broadcastChat(MsgSender.GAME, "==================================");
        broadcastChat(MsgSender.GAME, "");
        broadcastChat(MsgSender.GAME, winnerColor + C.Bold + e.getWinner().getNiceName() + " Won the game");
        broadcastChat(MsgSender.GAME, "");
        broadcastChat(MsgSender.GAME, "==================================");

        // also can do like stats or something here
        broadcastSubtitle(winnerColor + e.getWinner().getNiceName() + " Won the Game", 20, 60, 20);
    }

    @LocalEvent
    private void rosterTeamChanged(RosterPostTeamChangeEvent e)
    {
        for (StrikePlayer member : e.getRoster())
        {
            StrikeTeam team = e.getTeam();
            changePlayerTeam(member, team);
        }
    }

    /** Request team change from server and trigger event when granted. Should be used in async context such as commands,
     * because it waits for server response which may result in bad bugs in sync context */
    public Promise<?> changePlayerTeam(StrikePlayer player, StrikeTeam newTeam) {
        return new PlayerTeamChangeIntentEvent(this, player, newTeam).trigger()
        .thenAsync(
            x -> new PlayerRosterChangeEvent(
                    this,
                    player,
                    newTeam
            ).trigger()
        );
    }

    public StrikeTeam getWinningTeam()
    {
        InGameTeamData tRoster = getRoster(StrikeTeam.T);
        InGameTeamData ctRoster = getRoster(StrikeTeam.CT);

        if (getRelativeScore(tRoster) > getRelativeScore(ctRoster))
            return StrikeTeam.T;

        if (getRelativeScore(tRoster) < getRelativeScore(ctRoster))
            return StrikeTeam.CT;

        return null;
    }

    @LocalEvent
    private void startRoundOnGameStart(GameStartedEvent e) {
        resetRoundCount();
        startNewRound();
    }
}
