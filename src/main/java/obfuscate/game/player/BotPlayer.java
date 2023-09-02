package obfuscate.game.player;

import obfuscate.MsdmPlugin;
import obfuscate.game.core.Game;
import obfuscate.game.core.IGame;
import obfuscate.mechanic.item.guns.Gun;
import obfuscate.message.MsgSender;
import obfuscate.network.models.schemas.RoleData;
import obfuscate.util.time.Task;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

// this is fucking stupid that I have to extend human player to make bot player, but it is what it is
public class BotPlayer extends StrikePlayer
{
    private NPC npc;

    public BotPlayer(NPC npc) {
        super();
        this.npc = npc;
    }

    public static BotPlayer getOrCreate(String name, Location loc) {
        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, name);
        npc.spawn(loc);
        return getOrCreate(npc);
    }

    public NPC getNPC()
    {
        return npc;
    }

    public @NotNull Player getPlayer()
    {
        return (Player) npc.getEntity();
    }

    @Override
    public String getName() {
        return getNPC().getName();
    }

    @Override
    public boolean isNPC() {
        return true;
    }

    @Override
    public Gun getGunInHand(Game game) {
        return null;
    }

    @Override
    public boolean isBot() {
        return true;
    }

    @Override
    public void sendMessage(String message) {

    }

    @Override
    public void sendMessage(MsgSender sender, String message) {

    }

    @Override
    public void reloadAll(IGame game) {

    }

    @Override
    public void sendTitle(String title, String subtitle, Integer fadeIn, Integer stay, Integer fadeOut) {

    }

    @Override
    public void sendTitle(String title, Integer fadeIn, Integer stay, Integer fadeOut) {

    }

    @Override
    public void setBossbar(String title, Integer percent) {

    }

    @Override
    public boolean isDebugging() {
        return false;
    }

    @Override
    public void sendHotBar(String message) {

    }

    @Override
    public boolean isDisguised() {
        return false;
    }

    @Override
    public void stopDisguise() {

    }

    @Override
    public void toggleDisguise() {

    }

    @Override
    public void setDisguise() {

    }

    @Override
    public void loadResources() {

    }

    @Override
    public void throwGrenade(Game game, boolean isLeftCLick) {

    }

    @Override
    public void setTabHeaderFooter(String header, String footer) {

    }


    @Override
    public void equip(Game game) {

    }

    @Override
    public RoleData getRole() {
        return RoleData.BOT;
    }

    @Override
    public void cancelStun() {
        float normalWalkSpeed = 0.65f;

        if (getPlayer() == null) {
            return;
        }

        MsdmPlugin.info("Cancel stun for bot");
        getPlayer().removePotionEffect(PotionEffectType.JUMP);
        getPlayer().setWalkSpeed(normalWalkSpeed);

        for (Task task : stunTasks) {
            if (task.isRunning()) {
                task.cancel();
            }
        }
        stunTasks.clear();
    }

    @Override
    public void stun(int stunTicks) {

        if (getPlayer() == null) {
            return;
        }

        // if player was already stunned,
        // cancel previous stun
        if (!stunTasks.isEmpty()) {
            cancelStun();
        }

        // reduce jump height
        getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 1000000, 253));

        // cancel upwards momentum
        double y = getPlayer().getVelocity().getY();
        getPlayer().setVelocity(new Vector(0, y > 0 ? 0 : y, 0));

        float walkSpeed = 0.2f;
        float normalWalkSpeed = 0.65f;
        float walkSpeedStep = (normalWalkSpeed - walkSpeed) / stunTicks;

        for (int tick = 0; tick < stunTicks; tick++) {

            final int finalTick = tick;

            var task = new Task(() -> {
                if (getPlayer() == null) {
                    return;
                }

                if (finalTick == stunTicks - 1) {
                    cancelStun();
                } else {
                    getPlayer().setWalkSpeed(walkSpeed + walkSpeedStep * finalTick);
                }
            }, tick).run();

            stunTasks.add(task);
        }
    }
}

