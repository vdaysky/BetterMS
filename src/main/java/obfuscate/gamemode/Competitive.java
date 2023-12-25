package obfuscate.gamemode;

import obfuscate.event.LocalEvent;
import obfuscate.event.LocalPriority;
import obfuscate.event.custom.game.*;
import obfuscate.event.custom.gamestate.RoundResetEvent;
import obfuscate.event.custom.item.ItemFocusEvent;
import obfuscate.event.custom.item.ItemLostFocusEvent;
import obfuscate.event.custom.round.RoundStartEvent;
import obfuscate.event.custom.time.TimeEvent;
import obfuscate.event.custom.damage.PlayerDeathEvent;
import obfuscate.event.custom.player.*;
import obfuscate.event.custom.round.OvertimeStartEvent;
import obfuscate.event.custom.team.PostSideSwapEvent;
import obfuscate.game.config.ConfigField;
import obfuscate.game.core.DefusalGame;
import obfuscate.game.player.StrikePlayer;
import obfuscate.game.shop.ShopManager;
import obfuscate.mechanic.item.StrikeItem;
import obfuscate.mechanic.item.guns.Gun;
import obfuscate.mechanic.item.guns.GunStats;
import obfuscate.mechanic.item.melee.Knife;
import obfuscate.mechanic.item.objective.Compass;
import obfuscate.message.MsgSender;
import obfuscate.team.StrikeTeam;

import obfuscate.util.chat.C;
import obfuscate.util.chat.Message;
import org.bukkit.Location;

public class Competitive extends DefusalGame
{
    private final ShopManager _ShopManager = new ShopManager(this);

    @LocalEvent(priority = LocalPriority.PRE) // we want to drop inventory before other handlers reset player's inventory
    public void onPlayerLeaveTeamGame(PlayerPostLeaveGameEvent e)
    {
        // drop items
        if (isInProgress()) {
            dropInventory(e.getPlayer());
        }
    }


    @LocalEvent
    public void giveMoneyIfNotInProgress(PlayerPreRespawnEvent e) {

        if (isInProgress()) {
            return;
        }
        getShopManager().addToBalance(
            e.getPlayer(),
            getConfig().getValue(ConfigField.MAX_MONEY).val()
        );
    }

    @LocalEvent
    private void giveMoneyOnKill(PlayerDeathEvent e)
    {
        if (e.getDamager() == e.getDamagee()) {
            getShopManager().addToBalance(e.getDamager(), -300);
            return;
        }

        if (e.getDamager() == null) {
            return;
        }

        // Increase round kill counter for player
        roundKillStreak.put(
            e.getDamager(),
            roundKillStreak.getOrDefault(e.getDamager(), 0) + 1
        );

        if (is(ConfigField.GIVE_KILL_MONEY)) {
            StrikeItem item = e.getDamager().getHeldItem(this);
            int amount;

            if (item instanceof Gun gun) {
                amount = gun.getKillReward();
            }
            else if (item instanceof Knife) {
                amount = 1500;
            } else {
                amount = 300;
            }

            getShopManager().addToBalance(e.getDamager(), amount);
        }
    }

    @LocalEvent(priority = LocalPriority.PRE_HIGH)
    private void giveMoneyOnRoundEnd(RoundResetEvent e)
    {
        // do not give money before side swap
        if (shouldSwapSides()) {
            return;
        }

        if (!is(ConfigField.USE_ECONOMY)) {
            return;
        }

        if (isOvertime() && getRelativeRoundNumber() == 0)
        {
            int ot_money = getConfig().getValue(ConfigField.OVERTIME_MONEY).val();

            // money are given to offline players too. Am I going to regret it?
            for (StrikePlayer player : this.getEverParticipated())
            {
                getShopManager().setBalance(player, ot_money);
            }
            return;
        }

        if (getBomb() == null) {
            return;
        }

        if (winner == null) {
            return;
        }

        for (StrikePlayer player : winner)
        {

            if (getBomb().isPlanted() && winner.getTeam() == StrikeTeam.T)
                getShopManager().addToBalance(player, 3250 + 300);
            else if (getBomb().isExploded() && winner.getTeam() == StrikeTeam.T)
            {
                getShopManager().addToBalance(player, 3500);
            }
            else
                getShopManager().addToBalance(player, 3250);
        }

        for (StrikePlayer player : looser)
        {
            if (looser.getTeam() == StrikeTeam.T && getBomb().isPlanted()){
                getShopManager().addToBalance(player, 800 + 1400 + 500 * getLossCount(looser));
            }
            else
                getShopManager().addToBalance(player, 1400 + 500 * getLossCount(looser));
        }

        for (StrikePlayer player : getOnlineDeadOrAliveParticipants()) {
            var money = C.cDGreen + "$" + getShopManager().getBalance(player);
            player.sendMessage(MsgSender.GAME, C.cGray + "You have " + money + C.cGray + ". Open your inventory to spend it.");
            player.sendTitle(money, "Open your inventory to spend it", 10, 60, 10);
        }
    }

    @LocalEvent
    protected void onOvertimeStart(OvertimeStartEvent e)
    {
        broadcastSubtitle(
            Message.n().green("Overtime #").yellow("" + e.getOvertimeIndex() + 1).green(" started")
        );
    }

    @LocalEvent
    public void onSideSwap(PostSideSwapEvent e) {
        // reset inventory and money for online participants
        for (StrikePlayer player : this.getOnlineDeadOrAliveParticipants())
        {
            getGameSession(player).getInventory().clear();
            giveDefaultGuns(player);
            // equipment is reset in player join roster event as well,
            // but it was called before inventory is reset.
            player.equip(this);

            if (isOvertime()) {
                getShopManager().setBalance(player, get(ConfigField.OVERTIME_MONEY));
            } else {
                getShopManager().setBalance(player, get(ConfigField.START_MONEY));
            }
        }

        pdcRefreshTeams();
    }

    @LocalEvent
    public void onGameReady(GameStartedEvent e)
    {
        // player will be respawned by game

        // setup inventory
        for (StrikePlayer participant : getOnlineDeadOrAliveParticipants())
        {
            giveDefaultGuns(participant);

            // give money
            getShopManager().setBalance(participant, getShopManager().getStartMoney());
        }
    }

    private void giveDefaultGuns(StrikePlayer participant)
    {
        boolean replaceIfExists = !is(ConfigField.KEEP_INVENTORY);

        Gun defaultGun = createDefaultGun(participant);
        Knife knife = new Knife();

        // if KEEP_INVENTORY is true, do not replace items
        if (!participant.getInventory(this).hasItem(defaultGun.getSlot()) || replaceIfExists) {
            defaultGun.giveToPlayer(this, participant, true);
        }

        if (!participant.getInventory(this).hasItem(knife.getSlot()) || replaceIfExists) {
            knife.giveToPlayer(this, participant, true);
        }

        if (getPlayerRoster(participant) != null && getPlayerRoster(participant).getTeam() == StrikeTeam.T) {
            // do not replace bomb
            if (!getGameSession(participant).getInventory().hasItem(8)) {
                new Compass().giveToPlayer(this, participant, false);
            }
        }
    }

    @LocalEvent
    private void onCompassFocus(ItemFocusEvent e) {
        if (e.getItem() instanceof Compass) {
            var hotBar = e.getGame().getGameSession(e.getHolder()).getHotbarMessenger();
            var player = e.getHolder();

            hotBar.setMessage(unused -> {
                Location loc;
                boolean isPlanted = getBomb().isPlanted() && getBomb().getBlock() != null;
                boolean isCarried = getBombCarry() != null;
                boolean isDropped = findDroppedItem(getBomb()) != null;

                if (isPlanted) {
                    loc = getBomb().getBlock().getLocation();
                } else if (isCarried) {
                    loc = getBombCarry().getLocation();
                } else if (isDropped) {
                    loc = findDroppedItem(getBomb()).getLocation();
                } else if (player.isOnline()) {
                    // this can happen in games with no bomb
                    loc = player.getLocation(); // just to avoid null pointer exception
                } else {
                    // should never happen
                    return C.cRed + C.Bold + "Compass is broken";
                }

                player.setCompassTarget(loc);

                double distance = player.getLocation().distance(loc);
                String roundedDist = String.format("%.2f", distance);

                if (isPlanted) {
                    String siteName = getGameMap().getSiteName(loc);
                    return C.cRed + C.Bold + "Bomb Planted at " + C.cYellow + C.Bold + siteName + C.cRed + C.Bold + " site: " + C.cYellow + roundedDist  + "m";
                } else if (isCarried) {
                    return C.cGreen + C.Bold + "Bomb Carry: " +  getBombCarry().getShortChatName(this) +  C.Bold + C.cGreen + ": "  + C.cYellow + roundedDist + "m";
                } else if (isDropped) {
                    return C.cRed + C.Bold  + "Bomb Dropped: " + C.cYellow + roundedDist + "m";
                } else {
                    return C.cRed + C.Bold + "Bomb not found";
                }
            });
        }
    }

    @LocalEvent(priority = LocalPriority.POST)
    private void giveCompasses(RoundStartEvent e) {
        // compasses are already given to respawning players with giveDefaultGuns.
        // but when player who had bomb loses it after round reset, slot becomes empty.
        // to fix this, we add this excessive loop.
        for (StrikePlayer player : getOnline(getRoster(StrikeTeam.T))) {
            if (!getGameSession(player).getInventory().hasItem(8)) {
                new Compass().giveToPlayer(e.getGame(), player, false);
            }
        }
    }

    @LocalEvent
    private void onCompassFocusLost(ItemLostFocusEvent e) {

        if (e.getItem() instanceof Compass) {
            var hotBar = e.getGame().getGameSession(e.getHolder()).getHotbarMessenger();
            hotBar.setMessage("");
            hotBar.clearMessage();
        }
    }

    @LocalEvent
    private void updateCompasses(TimeEvent e) {
        if (e.getReason() != TimeEvent.UpdateReason.SECOND) {
            return;
        }

        for (StrikePlayer player : getOnline(getRoster(StrikeTeam.T))) {
            var inv = getGameSession(player).getInventory();

            StrikeItem item = inv.getItem(8);

            if (item == null) {
                return;
            }

            if (item instanceof Compass) {
                Location loc;
                boolean isPlanted = getBomb().isPlanted();
                boolean isCarried = getBombCarry() != null;
                boolean isDropped = findDroppedItem(getBomb()) != null;

                if (isPlanted) {
                    loc = getBomb().getBlock().getLocation();
                } else if (isCarried) {
                    loc = getBombCarry().getLocation();
                } else if (isDropped) {
                    loc = findDroppedItem(getBomb()).getLocation();
                } else {
                    // this can happen in games with no bomb
                    loc = player.getLocation(); // just to avoid null pointer exception
                }
                player.setCompassTarget(loc);
            }
        }
    }

    private Gun createDefaultGun(StrikePlayer player)
    {
        if (getPlayerRoster(player).getTeam() == StrikeTeam.T)
            return new Gun(GunStats.GLOCK_18);
        return new Gun(GunStats.P2000);
    }

    @LocalEvent
    public void updateOnScreenGraphics(TimeEvent e)
    {
        if (e.getReason() != TimeEvent.UpdateReason.SECOND)
            return;

        for (StrikePlayer p : getOnlinePlayers()) {
            getSharedContext().getSidebarUpdater().update(this, getGameSession(p).getSidebar(), p);
        }
    }

    @Override
    public int getMaxPlayers() {
        return get(ConfigField.MAX_PLAYERS);
    }

    @Override
    public int getMinPlayers() {
        return get(ConfigField.MIN_PLAYERS);
    }

    @Override
    public ShopManager getShopManager() {
        return _ShopManager;
    }

    @Override
    public boolean canMove(StrikePlayer player)
    {
        return super.canMove(player) && !getGameSession(player).isPlanting();
    }

    @LocalEvent(priority = LocalPriority.POST)
    public void handleRespawn(PlayerPreRespawnEvent e)
    {
        if (getGameSession(e.getPlayer()).isAlive())
            return;

        giveDefaultGuns(e.getPlayer());
    }
}
