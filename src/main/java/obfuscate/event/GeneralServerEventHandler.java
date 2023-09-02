package obfuscate.event;

import obfuscate.MsdmPlugin;
import obfuscate.game.player.StrikePlayer;
import obfuscate.util.InvulnerableItem;
import obfuscate.util.block.UtilBlock;
import obfuscate.util.chat.C;
import obfuscate.util.time.Scheduler;
import obfuscate.util.time.Task;
import obfuscate.world.WorldTools;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.material.Openable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.material.TrapDoor;
import org.bukkit.util.BoundingBox;

public class GeneralServerEventHandler implements Listener
{
    @EventHandler
    private void creatureSpawn(CreatureSpawnEvent event)
    {
        if (event.getEntity().getType() != EntityType.DROPPED_ITEM && event.getEntity().getType() != EntityType.ARMOR_STAND)
        {
            event.setCancelled(true);
        }
    }

    private void hideAdvancementsFor(World world) {
        world.setGameRuleValue("announceAdvancements", "false");
        MsdmPlugin.info("Achievements are now hidden for world '" + world.getName() + "'.");
    }

    /* Hide advancements for new worlds */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldLoad(WorldLoadEvent event) {
        hideAdvancementsFor(event.getWorld());
    }

    @EventHandler
    private void weatherChange(WeatherChangeEvent e) {
        Scheduler.runNextTick(()->e.getWorld().setStorm(false));
    }

    @EventHandler
    private void worldSetup(WorldLoadEvent e) {
        e.getWorld().setStorm(false);
        for (Entity en : e.getWorld().getEntities()) {
            if (en instanceof Bat) {
                en.remove();
            }
        }
    }

    @EventHandler
    private void hunger(FoodLevelChangeEvent e)
    {
        e.setFoodLevel(20);
    }

    @EventHandler
    private void regen(EntityRegainHealthEvent e)
    {
        if (e.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED || e.getRegainReason() == EntityRegainHealthEvent.RegainReason.REGEN) {
            e.setCancelled(true);
        }
    }

    //@EventHandler
    public void onItemBurn(EntityCombustEvent e) {

        if (e.getEntity().getType() == EntityType.DROPPED_ITEM)
        {

            Item item = e.getEntity().getWorld().spawn(e.getEntity().getLocation(), Item.class);
            item.setItemStack(((Item) e.getEntity()).getItemStack());
            item.setFireTicks(0);
            item.setVelocity(e.getEntity().getVelocity());
//            for (Player p : e.getEntity().getWorld().getPlayers())
//                UtilItem.spawnClientSide((Item) e.getEntity(), p);

        }
    }


    @EventHandler
    public void onEntityDamage(EntityDamageEvent e)
    {
        if (e.getCause() == EntityDamageEvent.DamageCause.FIRE)
        {
            Scheduler.runNextTick(()->e.getEntity().setFireTicks(0));

            if (e.getEntity() instanceof Item)
            {
                InvulnerableItem.itemBurn((Item) e.getEntity());
            }
        }
    }

    @EventHandler
    private void preventHangingBreak(HangingBreakByEntityEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    private void block(BlockBreakEvent e)
    {
        if(e.getBlock().getType() == Material.NETHER_PORTAL) {
            e.setCancelled(true);
        }

        // don't allow to break blocks
        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {

        // fix doors
        if (event.getBlock().getType().toString().toLowerCase().contains("door")) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    private void onBlockSpread(BlockSpreadEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onTp(EntityPortalEvent e)
    {
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void cancelGUIs(PlayerInteractEvent e) {
        // because of this I think it would be impossible to plant
        // while looking at an intractable block.
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Material interacted = e.getClickedBlock().getType();
            if (UtilBlock.hasGui(interacted)) {
                e.setCancelled(true);
                return;
            }

            BlockState state = e.getClickedBlock().getState();

            if (state.getData() instanceof Openable openable) {
                e.setCancelled(false);
                if (openable instanceof TrapDoor data) {
                    boolean isOpen = data.isOpen();
                    new Task(()-> {
                        openable.setOpen(isOpen);
                        state.setData((MaterialData) openable);
                        state.update();
                    }, 5).run();
                }
            } else {
                e.setCancelled(true); // for flower pots and stuff like that
            }
        }
    }

    @EventHandler
    private void selectionUpdate(PlayerInteractEvent e) {
        ItemStack item = e.getPlayer().getInventory().getItemInMainHand();

        if (item.getType() != Material.NETHERITE_AXE) {
            return;
        }

        if (e.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }

        StrikePlayer player = StrikePlayer.getOrCreate(e.getPlayer());
        if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
            WorldTools.selectFirstPoint(player, e.getClickedBlock().getLocation());
            player.sendMessage(C.cGreen + "First point selected");
        } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (WorldTools.hasFirstPoint(player)) {
                WorldTools.selectSecondPoint(StrikePlayer.getOrCreate(e.getPlayer()), e.getClickedBlock().getLocation());
                BoundingBox box = WorldTools.getSelection(player);
                int volume = (int) (box.getWidthX() * box.getHeight() * box.getWidthZ());
                player.sendMessage(C.cGreen + "Second point selected, total volume of " + C.cYellow + volume);
            }
        }
    }

    @EventHandler
    private void rightClick(PlayerInteractEvent e)
    {
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)
        {
            StrikePlayer player = StrikePlayer.getOrCreate(e.getPlayer());
            player.setLastRightClick(System.currentTimeMillis());
        }
    }

    @EventHandler
    private void onPlayerPortalTeleport(PlayerTeleportEvent e) {
        if(e.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    private void onHandSwap(PlayerSwapHandItemsEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    private void burn(BlockBurnEvent e)
    {
        e.setCancelled(true);
    }

//    @EventHandler
//    public void onBlockSpread(BlockSpreadEvent e) {
//        e.setCancelled(true);
//    }
    @EventHandler
    private void onPlayerLeave(PlayerQuitEvent e) {
        MsdmPlugin.getGameServer().getOnlinePlayerDataRegistry().resetPlayerData(e.getPlayer().getUniqueId());
    }
}
