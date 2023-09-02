package obfuscate.team;

import obfuscate.event.custom.network.ModelEvent;
import obfuscate.game.core.Game;
import obfuscate.game.core.PlayerState;
import obfuscate.game.player.*;
import obfuscate.util.serialize.load.SyncableObject;
import obfuscate.util.serialize.load.Loadable;
import obfuscate.util.serialize.load.Model;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Stores players that are teammates inside a game. Has variable team that can be changed after halftime */
@Model(name = "inGameTeam")
public class InGameTeamData extends SyncableObject implements PlayerGroup<StrikePlayer> {
    // VERY IMPORTANT TODO: MAKE ROSTER BE ITERABLE OF OFFLINE PLAYER
    @Loadable(field = "name")
    private String name = null;

    @Loadable(field = "starts_as_ct")
    private Boolean startsAsCT;

    @Loadable(field = "players")
    private List<StrikePlayer> players;

    @Nullable
    public String getRosterName() {
        return name;
    }

    /** If this in-game team is based off a competitive team, 
     * override names T/CT with their team name */
    public String getName() {
        if (name != null) {
            return name;
        }
        return getTeam().name();
    }

    private StrikeTeam team;

    public StrikeTeam getTeam() {
        // Definitely a great place for bugs
        // current team should be stored in the database?
        if (this.team == null) {
            if (this.startsAsCT) {
                this.team = StrikeTeam.CT;
            } else {
                this.team = StrikeTeam.T;
            }
        }
        return team;
    }

    public void setTeam(StrikeTeam team) {
        this.team = team;
    }

    @Override
    public Iterator<StrikePlayer> iterator() {
        return players.iterator();
    }

    public ArrayList<StrikePlayer> getOnlinePlayers() {
        ArrayList<StrikePlayer> view = new ArrayList<>();

        for (StrikePlayer player : this) {
            if (player.isOnline()) {
                view.add(player);
            }
        }
        return view;
    }

    public ArrayList<StrikePlayer> getOnlineParticipants(Game game) {
        ArrayList<StrikePlayer> view = new ArrayList<>();

        for (StrikePlayer player : this) {
            if (player.isOnline() && game.getGameSession(player).getState() == PlayerState.IN_GAME) {
                view.add(player);
            }
        }
        return view;
    }

    public List<StrikePlayer> getPlayers() {
        return players;
    }


    @Override
    public int size() {
        return players.size();
    }

    @Override
    public boolean isEmpty() {
        return players.isEmpty();
    }

    @Override
    public boolean has(StrikePlayer player) {
        return players.contains(player);
    }

    public void changeTeam() {
        this.team = StrikeTeam.getOpposite(team);
    }


    @Override
    public Class<? extends ModelEvent<? extends InGameTeamData>> getFulfilledEvent() {
//        MsdmPlugin.important("InGameTeamData.getFulfilledEvent() called. Players: " + players);
        return null;
    }

    public List<StrikePlayer> getAlivePlayers() {
        List<StrikePlayer> alive = new ArrayList<>();

        for (var player : getPlayers()) {
            if (player.isOnline() && player.getGame() != null && player.getGame().getGameSession(player).isAlive()) {
                alive.add(player);
            }
        }
        return alive;
    }

    public String getNiceName() {
        if (name != null) {
            return name;
        }
        return getTeam().getNiceName();
    }
}
