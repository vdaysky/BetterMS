package obfuscate.event;

import obfuscate.MsdmPlugin;
import obfuscate.event.bus.EventBus;
import obfuscate.event.custom.game.PlayerChatEvent;
import obfuscate.event.custom.game.PlayerClickEvent;
import obfuscate.event.custom.item.*;
import obfuscate.event.custom.item.objective.PlayerAttemptPlantEvent;
import obfuscate.event.custom.shop.PlayerPreShopEvent;
import obfuscate.event.custom.time.TimeEvent;
import obfuscate.game.core.*;
import obfuscate.game.damage.DamageSourceWrapper;
import obfuscate.game.damage.NamedDamageSource;
import obfuscate.game.debug.ViewRecorder;
import obfuscate.game.player.StrikePlayer;
import obfuscate.game.state.StateTag;
import obfuscate.logging.Logger;
import obfuscate.logging.Tag;
import obfuscate.mechanic.item.StrikeItem;
import obfuscate.mechanic.item.StrikeStack;
import obfuscate.mechanic.item.objective.Bomb;
import obfuscate.mechanic.item.utility.grenade.Grenade;
import obfuscate.mechanic.item.guns.*;
import obfuscate.ui.ServerList;
import obfuscate.util.EventCollector;
import obfuscate.util.chat.MarkdownParser;
import obfuscate.util.chat.Message;
import obfuscate.util.recahrge.Recharge;
import obfuscate.util.telegram.Telegram;
import obfuscate.util.time.Task;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class EventManager implements Listener
{
    private static EventManager instance = null;

    /** this is static class instance that registers all essential event handlers
     * that pass event data to game instances */
    private EventManager()
    {
        Bukkit.getPluginManager().registerEvents(this, MsdmPlugin.getInstance());

        new Task(
                ()->timingEvent(TimeEvent.UpdateReason.TICK),
                1, 1).run();

        new Task(
              ()->timingEvent(TimeEvent.UpdateReason.SECOND),
              1, 20).run();
    }

    /* this static instance insures that there will be only one event listener instance */
    public static EventManager construct()
    {
        if (instance == null) {
            instance = new EventManager();
        }

        return instance;
    }

    /* this event is always passed to game even if its stopped */
    private void timingEvent(TimeEvent.UpdateReason r)
    {
        // dispatch event like this to prevent game class
        // staying loaded because of active event listener
        // this class is static and does not care
        MsdmPlugin.getGameServer().triggerTimeEvent(r);
    }

//    @EventHandler
//    private void removeSpawnPosIndicator(EntityDamageByEntityEvent event) {
//        var indicators = WorldTools.positionIndicator.get(event.getEntity().getWorld().getName());
//        if (indicators == null) {
//            return;
//        }
//        if (indicators.containsKey(event.getEntity())) {
//            indicators.get(event.getEntity()).remove();
//            indicators.remove(event.getEntity());
//        }
//    }

    @EventHandler
    private void meleeDamageEvent(EntityDamageByEntityEvent event)
    {
        Entity damaged = event.getEntity();
        Entity damager = event.getDamager();
        event.setCancelled(true);

        if (damager instanceof Player && damaged instanceof Player)
        {
            StrikePlayer damagedPlayer = StrikePlayer.getOrCreate((Player) damaged);
            StrikePlayer damagerPlayer = StrikePlayer.getOrCreate((Player) damager);

            Game contextGame = damagedPlayer.getGame();

            // not in game
            if (contextGame == null) {
                return;
            }

            StrikeItem knife = damagerPlayer.getHeldItem(contextGame);

            if (knife == null || knife.getType() != StrikeItemType.MELEE) {
                return;
            }

            if (!Recharge.done(damagerPlayer, "Knife", 500))
                return;

            damagerPlayer.animateHand();
            contextGame.getDamageManager().handleMeleeAttack(contextGame, damagedPlayer, damagerPlayer, knife);
        }
    }

    @EventHandler
    private void scroll(PlayerItemHeldEvent e)
    {
        StrikePlayer player = StrikePlayer.getOrCreate(e.getPlayer());
        Game game = player.getGame();
        if (game == null)
        {
            return;
        }

        if ( game.getGameSession(player) != null)
            game.getGameSession(player).setScoped(false);

        StrikeItem inactiveItem = player.getHeldItem(game);

        if (inactiveItem != null)
            new ItemLostFocusEvent(inactiveItem, game, player, ItemLostFocusEvent.FocusLostReason.SWITCH).trigger();


        Bukkit.getScheduler().scheduleSyncDelayedTask(MsdmPlugin.getInstance(), ()->
        {
            StrikeItem activeItem = player.getHeldItem(game);
            if (activeItem == null)
                return;
            new ItemFocusEvent(activeItem, game, player).trigger();
        }, 1);
    }

    @EventHandler
    public void triggerGrenade(PlayerInteractEvent event)
    {
        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }

        StrikePlayer player = StrikePlayer.getOrCreate(event.getPlayer());
        Game game = player.getGame();

        if (game == null)
            return;

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            new PlayerClickEvent(game, player, true).trigger();
        }

    }
    @EventHandler
    public void triggerBomb(PlayerInteractEvent e)
    {
        if (e.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }

        StrikePlayer player = StrikePlayer.getOrCreate(e.getPlayer());
        Game game = player.getGame();

        if (game == null)
            return;

        StrikeItem item = player.getHeldItem(game);

        if (item instanceof Bomb) {
            // trigger event instead of calling method because bomb logic is in plugin,
            // therefore it can't be called directly.
            new PlayerAttemptPlantEvent(player, game, (Bomb) item).trigger();
        }
    }

    @EventHandler
    public void shoot(PlayerInteractEvent event)
    {
        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }

        StrikePlayer player = StrikePlayer.getOrCreate(event.getPlayer());
        Game game = player.getGame();
        if (game == null)
            return;

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (!game.getGameState().is(StateTag.CAN_INTERACT)) {
            return;
        }

        var rec = ViewRecorder.getInstance();
        if (rec.getHistory(player) != null) {
            rec.onShoot(player);
        }

        player.shoot(game);
    }

    @EventHandler
    private void standingInFire(EntityDamageEvent e)
    {
        if (e.getCause() != EntityDamageEvent.DamageCause.FIRE)
            return;

        Entity en = e.getEntity();
        if (!(en instanceof Player))
            return;

        StrikePlayer player = StrikePlayer.getOrCreate(((Player) en));
        Game game = player.getGame();
        Block fire = en.getLocation().getBlock();
        Grenade grenade = game.getFireStarterAt(fire);
        StrikePlayer damager = null;

        NamedDamageSource damageSource;

        if (grenade == null) {
            // player died in fire somewhere in the map. what a legend
            damageSource = DamageSourceWrapper.INTENDED_DESIGN;
        }
        else {
            damageSource = grenade;
            damager = grenade.getThrower();
        }

        game.getDamageManager().handleBurnDamage(game, player, damager, damageSource);
    }

    @EventHandler
    public void triggerLeftClick(PlayerAnimationEvent event) {
        if (event.getAnimationType() != PlayerAnimationType.ARM_SWING)
            return;

        Player player = event.getPlayer();
        StrikePlayer stPlayer = StrikePlayer.getOrCreate(player);
        Game game = stPlayer.getGame();

        if (game == null)
            return;

        // item drops cause arm swing
        if (EventCollector.count(stPlayer, "dropItem", 19) > 0){
            return;
        }
        new PlayerClickEvent(game, stPlayer, false).trigger();
    }

    @EventHandler
    private void fallDamage(EntityDamageEvent e)
    {
        e.setCancelled(true);

        if ( (e instanceof EntityDamageByEntityEvent) )
            return;

        if ( !(e.getEntity() instanceof Player) )
            return;

        StrikePlayer player = StrikePlayer.getOrCreate((Player) e.getEntity());
        Game game = player.getGame();

        if (game == null)
            return;

        if (e.getCause() == EntityDamageEvent.DamageCause.FALL)
        {
            game.getDamageManager().handleFallDamage(game, player, e.getDamage());
        }
    }

    @EventHandler
    private void triggerDrop(PlayerDropItemEvent event)
    {
        StrikePlayer player = StrikePlayer.getOrCreate(event.getPlayer());
        Game game = player.getGame();

        if (game == null) {
            event.setCancelled(true);
            return;
        }

        //Without this, the event is cancelled
        //Which results in the item staying in your hand, even if i set to null here.
        event.setCancelled(false);

        //player.getActiveGame().dropItem(player);
        StrikeStack stack = player.getHeldSlot(game);

        if (stack.isEmpty())
            return;

        if (!stack.top().droppable()) {
            event.setCancelled(true);
            return;
        }

        event.getItemDrop().remove();
        StrikeItem toDrop = stack.top();

        game.dropItem(toDrop, stack, player, false, DropReason.DROP);
    }

    @EventHandler
    private void moveEvent(PlayerMoveEvent event)
    {
        StrikePlayer player = StrikePlayer.getOrCreate(event.getPlayer());
        Game game = player.getGame();

        if (game == null)return;

        if (!game.canMove(player))
            cancelMovement(event);
    }

    public static void cancelMovement(PlayerMoveEvent event)
    {
//        StrikePlayer player = StrikePlayer.getOrCreate(event.getPlayer());
        Location loc = event.getTo();

        boolean moveAlt = false;
        // only cancel x/z movement
        if (Math.abs(event.getFrom().getX() - event.getTo().getX()) > 0.01) {
            loc.setX(event.getFrom().getX());
            moveAlt = true;
        }
        if (Math.abs(event.getFrom().getZ() - event.getTo().getZ()) > 0.01) {
            loc.setZ(event.getFrom().getZ());
            moveAlt = true;
        }

        if (moveAlt) {
            event.setTo(loc);
        }
//        player.getPlayer().teleport(loc);
    }

    @EventHandler
    public void scopeUpdate(PlayerToggleSneakEvent event)
    {
        StrikePlayer player = StrikePlayer.getOrCreate(event.getPlayer());
        Game game = player.getGame();

        if(game == null)
            return;

        if (game.getGameSession(player) == null)
            return;

        Gun gun = player.getGunInHand(game);
        if (gun == null)
            return;

        if (!gun.hasScope())
            return;

        //Enable
        if (!event.getPlayer().isSneaking()) {

            game.getGameSession(player).setScoped(true);
//            if (gun.getGunType() == GunType.SNIPER)
//            {
//                event.getPlayer().getWorld().playSound(event.getPlayer().getEyeLocation(), Sound.GHAST_DEATH, 0.8f, 1f);
//                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 10, 1, false, false));
//            }
        }
        else {
            game.getGameSession(player).setScoped(false);
        }
    }
    @EventHandler
    public void onItem(PlayerPickupItemEvent e)
    {
        e.setCancelled(true);

        StrikePlayer player = StrikePlayer.getOrCreate(e.getPlayer());
        Game game = player.getGame();
        Item item = e.getItem();

        if (game == null)
            return;

        StrikeItem gameItem = game.getItem(item);

        if (gameItem == null)
        {
            return;
        }

        if (gameItem.canPickup(game, player))
        {
            boolean cancelled = new ItemPickUpEvent(item, gameItem, game, player).triggerSync();

            if (cancelled)
                return;

            game.pickUpFromGround(player, gameItem, item);
        }
    }

    @EventHandler
    private void clickInServerMenu(InventoryClickEvent e) {
        EventBus.pushEvent(e);

        ServerList.onInventoryClick(
            e.getClickedInventory(),
            e.getSlot(),
            e.isRightClick()
        );
    }

    HashMap<StrikePlayer, StrikeStack> lastSlot = new HashMap<>();


    @EventHandler
    private void onInventoryClosed(InventoryCloseEvent e) {
        EventBus.pushEvent(e);
        ServerList.onInventoryClose(e.getInventory());
        StrikePlayer player = StrikePlayer.getOrCreate((Player) e.getPlayer());
        if (lastSlot.containsKey(player)) {
            var game = player.getGame();
            player.getInventory(game).refreshHotBarDisplay();
            lastSlot.remove(player);
        }
    }

    @EventHandler
    private void onLeave(PlayerQuitEvent e) {
        EventBus.pushEvent(e);
        StrikePlayer player = StrikePlayer.getOrCreate(e.getPlayer());
        lastSlot.remove(player);
    }

    @EventHandler
    private void inventoryInteract(InventoryDragEvent e) {
        e.setCancelled(true);
//        MsdmPlugin.highlight("------------ InventoryDragEvent ------------");
//        MsdmPlugin.highlight("InventoryDragEvent: " + e.getInventorySlots());
//
//        Integer slot = e.getInventorySlots().stream().filter(s -> s >= 3 && s <= 6).findFirst().orElse(null);
//
//        // out of bounds for nade slots
//        if (slot == null) {
//            e.setCancelled(true);
//            return;
//        }
//
//        e.setCancelled(true);
//
//        StrikePlayer player = StrikePlayer.getOrCreate((Player) e.getWhoClicked());
//        Game game = player.getGame();
//
//        if (game == null) {
//            return;
//        }
//
//        var fromStack = lastSlot.get(player);
//
//        // save last slot as slot that is about to be replaced
//        if (player.getInventory(game).hasItem(slot)) {
//            MsdmPlugin.highlight("Set last slot to " + slot);
//            lastSlot.put(player, player.getInventory(game).getStack(slot));
//
//            // set cursor to item in slot that will be replaced soon
//            player.getPlayer().setItemOnCursor(player.getInventory(game).getStack(slot).top().getStack());
//        } else {
//            MsdmPlugin.highlight("Remove last slot and cursor");
//            lastSlot.remove(player);
//            e.setCursor(null);
//        }
//
//        if (fromStack != null) {
//            if (slot != fromStack.getRealSlot()) {
//                var stackMoved = player.getInventory(game).getStack(fromStack.getRealSlot());
//                // don't update inventory display during the move,
//                // because client should be synced anyway,
//                // and updating inventory causes items to disappear
//                player.getInventory(game).setStack(slot, fromStack, true);
//                stackMoved.clear(true);
//            }
//        }
    }

    @EventHandler
    private void inventoryClick(InventoryClickEvent event)
    {

        // don't cancel creative inventory
        if (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.CREATIVE) {
            return;
        }

        if (event.getWhoClicked().getOpenInventory().getType() == InventoryType.CREATIVE) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player bPlayer)) {
            return;
        }

        StrikePlayer player = StrikePlayer.getOrCreate((Player) event.getWhoClicked());

        Game game = player.getGame();

        if (game != null) {
            // allow grenade swapping
            if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
                int slot = event.getSlot();
                var fromStack = lastSlot.get(player);

                // 0 1 2 3 4 5 6 7 8
                if (slot >= 3 && slot <= 6) {
                    if (fromStack != null) {

                        if (fromStack.getRealSlot() == slot) {
                            return;
                        }

                        event.setCancelled(false);

                        final ItemStack itemBeingReplaced;

                        // save last slot as slot that is about to be replaced
                        if (player.getInventory(game).hasItem(slot)) {
                            lastSlot.put(player, player.getInventory(game).getStack(slot).copy());
                            itemBeingReplaced = player.getInventory(game).getItem(slot).getStack();
                            // place item from slot in cursor, even if it was a right click
                            event.setCursor(null);
                        } else {
                            itemBeingReplaced = null;
                            lastSlot.remove(player);
                            event.setCursor(null);
                        }

                        var stackMoved = player.getInventory(game).getStack(fromStack.getRealSlot());
                        // don't update inventory display during the move,
                        // because client should be synced anyway,
                        // and updating inventory causes items to disappear
                        player.getInventory(game).setStack(slot, fromStack, false);

                        // only clear moved stack in case it didn't change since the move.
                        // if two items are interchanged, stack will already have new item in it
                        if (stackMoved.top() == fromStack.top()) {
                            stackMoved.clear(false);
                        }

                        // since I am messing with cursor on events we can't rely on client being synced
                        // not sure why, but it doesn't work without a delay. Let's hope it doesn't break
                        new Task(()->
                        {
                            player.getInventory(game).refreshHotBarDisplay();
                        }, 0).run();

                        // one tick later set player cursor to item that was in slot before the move
                        // to indicate that it is being moved
                        // make sure it happens after inventory update. For some reason cursor disappears
                        // during the update
                        new Task(()-> {
                            player.getPlayer().setItemOnCursor(itemBeingReplaced);
                        }, 2).run();

                    } else {
                        event.setCancelled(true);
                    }
                    return;
                }
            } else {
                // pick up an item on cursor (doesn't change underlying inventory state)
                int slot = event.getSlot();
                if (slot >= 3 && slot <= 6) {
                    event.setCancelled(false);
                    if (player.getInventory(game).hasItem(slot)) {
                        lastSlot.put(player, player.getInventory(game).getStack(slot).copy());
                    } else {
                        lastSlot.remove(player);
                    }
                    return;
                }
            }
        }

        // check for outside click
        if (event.getRawSlot() == -999) {
            return;
        }


        if (event.getClickedInventory() != bPlayer.getInventory()) {
            return;
        }

        event.setCancelled(true);

        if (game == null)
            return;

        if (!game.getShopManager().canShop(player))
            return;

        StrikeItem clickedIcon = game.getShopManager().getIcon(game.getPlayerRoster(player).getTeam(), event.getSlot());

        if (clickedIcon == null)
            return;

        // pass shop event
        new PlayerPreShopEvent(game, player, event.getClick(), clickedIcon).trigger();
    }

    @EventHandler
    public void updatePlayerMoveInfo(PlayerMoveEvent e)
    {
        if (e.isCancelled())
            return;

        StrikePlayer player = StrikePlayer.getOrCreate(e.getPlayer());
        Game Game = player.getGame();

        if (Game == null)
            return;

        player.setMoveInfo(e.getFrom(), e.getTo());

        if (player.sinceMove() < 5)
        {
            player.addToLastMoveTime(Game, player.sinceMove());
        }
    }

    @EventHandler
    private void targetsTest (final ProjectileHitEvent event) {
        if (event.getEntity().getLocation().getBlock().getType() == Material.REDSTONE_BLOCK) {
            final Block block = event.getEntity().getLocation().getBlock();
            block.setType(Material.AIR);
            new Task(()->block.setType(Material.REDSTONE_BLOCK), (int) (20 * 10 * Math.random())).run();
        }
    }

    @EventHandler
    private void nextSpecTarget(PlayerInteractEvent e)
    {
        EventBus.pushEvent(e);

        if (e.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }

        StrikePlayer player = StrikePlayer.getOrCreate(e.getPlayer());
        Game game = player.getGame();

        if (game == null)
            return;

        if (game.getSpectatedPlayer(player) == null)
            return;

        // dead players can switch perspectives
        // TODO: implementation is real shit here
        if (!game.getGameSession(player).isAlive()) {
            game.spectateNext(player);
        }
    }

    @EventHandler
    private void useHubCompass(PlayerInteractEvent e) {

        var p = e.getPlayer();

        if (e.getAction() == Action.PHYSICAL) {
            return;
        }

        StrikePlayer player = StrikePlayer.getOrCreate(p);
        if (player.getGame() != null) {
            return;
        }
        var hand = p.getInventory().getItemInMainHand();

        if (hand.getType() == Material.COMPASS) {
            // show menu
            new ServerList(MsdmPlugin.getGameServer().getGames(), player).show();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void spec(PlayerTeleportEvent e)
    {

        if (e.getCause() != PlayerTeleportEvent.TeleportCause.SPECTATE)
            return;

        e.setCancelled(true);

        // for now ignore spectating
        //        StrikePlayer player = StrikePlayer.getOrCreate(e.getPlayer());
        //        Game game = player.getGame();
        //        game.updateSpectator(player);
    }

    @EventHandler
    private void specTest(PlayerToggleSneakEvent e)
    {
        StrikePlayer player = StrikePlayer.getOrCreate(e.getPlayer());

        if (player.getPlayer().getGameMode() != GameMode.SPECTATOR)
            return;

        Game game = player.getGame();
        game.updateSpectator(player);
    }

    @EventHandler
    private void voidDamage(PlayerMoveEvent e) {
        if (e.getTo().getY() < 0) {
            var player = StrikePlayer.getOrCreate(e.getPlayer());
            Game game = player.getGame();
            if (game == null) {
                var spawnLoc = player.getPlayer().getWorld().getSpawnLocation();
                player.getPlayer().teleport(spawnLoc);
                return;
            }
            game.getDamageManager().handleFallDamage(
                game,
                player,
                1000
            );
        }
    }

    @EventHandler
    private void chat(AsyncPlayerChatEvent e)
    {
        e.setCancelled(true);
        StrikePlayer player = StrikePlayer.getOrCreate(e.getPlayer());
        Game game = player.getGame();

        Telegram.sendMessage(player.getActualName() + (game == null ? " in hub": " in game #" + game.getId().getObjId()) + ": " + e.getMessage());

        String message = MarkdownParser.parse(e.getMessage());

        if (game == null) {
            for (StrikePlayer hubPlayer : MsdmPlugin.getGameServer().getFallbackServer().getPlayers()) {
                if (!hubPlayer.isOnline()) {
                    continue;
                }
                hubPlayer.sendMessage(Message.of("").green("[").gray("Hub").green("] ").gray(e.getPlayer().getName() + " >> ").white(message));
            }
            Logger.info("[Hub] " + e.getPlayer().getName() + " > " + message, player, Tag.CHAT);
            return;
        }
        //String message = player.getFullChatName(game) + ChatColor.GRAY + " >> " + player.getRole().getChatMessageColor() + e.getMessage();
        new PlayerChatEvent(game, player, message).trigger();
        Logger.info("[Game#" + game.getId() + "] " + e.getPlayer().getName() + " > " + message, player, Tag.CHAT, game);
    }
}
