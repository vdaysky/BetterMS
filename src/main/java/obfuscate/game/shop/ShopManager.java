package obfuscate.game.shop;

import obfuscate.event.custom.item.DropReason;
import obfuscate.event.custom.shop.PlayerShopEvent;
import obfuscate.game.config.GameConfiguration;
import obfuscate.game.dataregistry.DataKey;
import obfuscate.game.dataregistry.DataScope;
import obfuscate.game.player.StrikePlayer;
import obfuscate.mechanic.item.guns.Shotgun;
import obfuscate.team.StrikeTeam;
import obfuscate.game.config.ConfigField;
import obfuscate.game.core.ShoppableGame;
import obfuscate.game.state.StateTag;
import obfuscate.mechanic.item.utility.ConsumableItem;
import obfuscate.mechanic.item.StrikeItem;
import obfuscate.mechanic.item.armor.Helmet;
import obfuscate.mechanic.item.armor.Kevlar;
import obfuscate.mechanic.item.guns.Gun;
import obfuscate.mechanic.item.guns.GunStats;
import obfuscate.mechanic.item.objective.DefusalKit;
import obfuscate.mechanic.item.utility.grenade.*;
import obfuscate.message.MsgSender;
import obfuscate.util.chat.C;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class ShopManager
{
    ShoppableGame _game;
    private final HashMap<StrikePlayer, Integer> money = new HashMap<>();

    private static final StrikeItem[] T_Shop = new StrikeItem[36];
    private static final StrikeItem[] CT_Shop = new StrikeItem[36];

    static {
        //  9 10 11 12 13 14 15 16 17
        // 18 19 20 21 22 23 24 25 26
        // 27 28 29 30 31 32 33 34 35
        //  0  1  2  3  4  5  6  7  8
        T_Shop[9] = new Gun(GunStats.GLOCK_18);
        T_Shop[10] = new Gun(GunStats.P250);
        T_Shop[11] = new Gun(GunStats.CZ75);
        T_Shop[12] = new Gun(GunStats.DEAGLE);

        T_Shop[18] = new Shotgun(GunStats.NOVA);
        T_Shop[19] = new Shotgun(GunStats.XM1014);
        T_Shop[20] = new Gun(GunStats.PPBIZON);
        T_Shop[21] = new Gun(GunStats.P90);

        T_Shop[27] = new Gun(GunStats.GALIL);
        T_Shop[28] = new Gun(GunStats.AK47);
        T_Shop[29] = new Gun(GunStats.SG553);
        T_Shop[30] = new Gun(GunStats.SSG08);
        T_Shop[31] = new Gun(GunStats.AWP);

        T_Shop[14] = new FlashBang();
        T_Shop[15] = new HighExplosive();
        T_Shop[16] = new Smoke();
        T_Shop[17] = new Molotov();

        T_Shop[34] = new Helmet();
        T_Shop[35] = new Kevlar();
    }

    static {
        CT_Shop[9] = new Gun(GunStats.P2000);
        CT_Shop[10] = new Gun(GunStats.P250);
        CT_Shop[11] = new Gun(GunStats.CZ75);
        CT_Shop[12] = new Gun(GunStats.DEAGLE);

        CT_Shop[18] = new Shotgun(GunStats.NOVA);
        CT_Shop[19] = new Shotgun(GunStats.XM1014);
        CT_Shop[20] = new Gun(GunStats.PPBIZON);
        CT_Shop[21] = new Gun(GunStats.P90);

        CT_Shop[27] = new Gun(GunStats.FAMAS);
        CT_Shop[28] = new Gun(GunStats.M4A4);
        CT_Shop[29] = new Gun(GunStats.AUG);
        CT_Shop[30] = new Gun(GunStats.SSG08);
        CT_Shop[31] = new Gun(GunStats.AWP);

        CT_Shop[14] = new FlashBang();
        CT_Shop[15] = new HighExplosive();
        CT_Shop[16] = new Smoke();
        CT_Shop[17] = new Incendiary();

        CT_Shop[26] = new DefusalKit();
        CT_Shop[34] = new Helmet();
        CT_Shop[35] = new Kevlar();
    }

    public StrikeItem[] getShopSnapshot(StrikeTeam team)
    {
        return (team == StrikeTeam.T) ? T_Shop : CT_Shop;
    }

    public ShopManager(ShoppableGame game)
    {
        _game = game;
    }

    public Integer getStartMoney()
    {
        return _game.getConfig().getValue(ConfigField.START_MONEY).val();
    }
    public Integer getMaxMoney()
    {
        return _game.getConfig().getValue(ConfigField.MAX_MONEY).val();
    }

    public boolean areGunsFree()
    {
        return _game.getConfig().getValue(ConfigField.FREE_GUNS).bool();
    }


    public Integer getBalance(StrikePlayer player)
    {
        money.putIfAbsent(player, getStartMoney());
        return money.get(player);
    }

    public void addToBalance(StrikePlayer player, Integer amount)
    {
        if (player.isOnline()) {

            // don't send message if player not in this game
            if (!player.isParticipating(_game))
                return;

            // todo: game.sendMessage would be nice
            if (_game.getGameSession(player).isInGame()) {
                if (amount > 0)
                    player.sendMessage(MsgSender.GAME, C.cGray + "You received " + C.cDGreen + amount + "$" + C.cGray + ".");
            }

        }

        money.put(player, Math.min(getMaxMoney(), Math.max(0, getBalance(player) + amount)));
    }

    public void updateInventoryShop(StrikePlayer player, boolean shoppable)
    {
        if (!shoppable)
        {
            for (int i = 9; i < 36; i++)
            {
                player.getPlayer().getInventory().setItem(i, null);
            }
            return;
        }

        StrikeItem[] inventory = getShopSnapshot(_game.getPlayerRoster(player).getTeam());

        for (int slot = 9; slot < 36; slot++)
        {
            StrikeItem item = inventory[slot];
            if (item == null)
                continue;

            boolean alreadyHas = _game.getGameSession(player).getInventory().alreadyHasMaxCapacity(item);
            boolean canPickUp = item.canPickup(_game, player);

            ItemStack shopItem = item.getShopItem(
                    getBalance(player),
                    alreadyHas,
                    canPickUp,
                    _game.getConfig().getValue(ConfigField.FREE_GUNS).bool()
            );

            player.getPlayer().getInventory().setItem(slot, shopItem);
        }
    }

    public boolean hasMoney(StrikePlayer player, Integer money)
    {
        return getBalance(player) >= money;
    }

    public boolean canAfford(StrikePlayer buyer, int cost)
    {
        return areGunsFree() || hasMoney(buyer, cost);
    }

    private boolean canBuy(StrikePlayer player, StrikeItem item) {

        int grenadesBought = _game.getDataRegistry().getForPlayer(
                player,
                DataScope.ROUND_LIFE,
                DataKey.GRENADES_BOUGHT,
                0
        );

        int grenadeLimit = _game.getConfig().getValue(ConfigField.MAX_GRENADES).val();

        if (item instanceof Grenade && grenadeLimit > 0 && grenadesBought >= grenadeLimit) {
            player.sendMessage(MsgSender.GAME, C.cRed + "You can't buy more grenades this round.");
            _game.getSoundManager().bass().forPlayer(player).play();
            return false;
        }

        return true;
    }

    public void buy(StrikePlayer buyer, StrikeItem item, boolean drop)
    {
        StrikeItem boughtItem = item.copy();

        if (!canBuy(buyer, item)) {
            return;
        }

        if (item.canPickup(_game, buyer)) {
            getItemFromShop(boughtItem, buyer, true);
        }
        else if (item.droppable())
        {
            if (boughtItem.getSlot() != null) {
                if (drop)
                {
                    // save existing gun
                    StrikeItem itemToSave = buyer.getInventory(_game).getItem(boughtItem.getSlot());
                    // give bought one
                    getItemFromShop(boughtItem, buyer, true);
                    // drop bought gun
                    _game.dropItem(boughtItem,null, buyer, false, DropReason.DROP);
                    // give gun back
                    itemToSave.giveToPlayer(_game, buyer, false);
                }
                else
                {
                    var stack = buyer.getInventory(_game).getStack(boughtItem.getSlot());

                    // drop existing gun
                    _game.dropItem(stack.top(), stack, buyer, false, DropReason.DROP); // bug here, stack is empty

                    // give new one
                    getItemFromShop(boughtItem, buyer, true);
                }
            }
        }
        else
        {
            if (item instanceof ConsumableItem) {
                buyer.sendMessage(MsgSender.GAME, C.cRed + "You can't carry any more");
                _game.getSoundManager().bass().forPlayer(buyer).play();
                return;
            }

            buyer.sendMessage(MsgSender.GAME, C.cRed +  "You already have " + C.cGreen + item.getName() + C.cGray +  "!");
            _game.getSoundManager().bass().forPlayer(buyer).play();
            return;
        }

        // Take money
        if (!areGunsFree()) {
            addToBalance(buyer, -boughtItem.getCost());
        }
    }

    private void getItemFromShop(StrikeItem item, StrikePlayer buyer, boolean setOwnerName) {
        item.giveToPlayer(_game, buyer, setOwnerName);
        new PlayerShopEvent(_game, item, buyer, areGunsFree()).trigger();
    }

    public boolean canShop(StrikePlayer player)
    {
        if (!_game.getGameSession(player).isAlive())
            return false;

        GameConfiguration config = _game.getConfig();

        boolean timeShoppable = _game.getRoundTime() < config.getValue(ConfigField.BUY_TIME).val() ||
                config.getValue(ConfigField.BUY_TIME).infinite() ||
                _game.getGameState().is(StateTag.OVERRIDE_BUYTIME);

        boolean locationShoppable = _game.isLocationShoppable(player.getLocation(), _game.getPlayerRoster(player).getTeam()) ||
                config.getValue(ConfigField.BUY_ANYWHERE).bool();

        return timeShoppable && locationShoppable;
    }

    public void setBalance(StrikePlayer player, Integer amount)
    {
        money.put(player, amount);
    }

    public StrikeItem getIcon(StrikeTeam team, int slot) {
        if (slot < 36) {
            return getShopSnapshot(team)[slot];
        }
        return null;
    }
}
