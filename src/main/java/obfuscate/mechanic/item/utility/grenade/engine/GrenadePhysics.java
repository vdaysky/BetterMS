package obfuscate.mechanic.item.utility.grenade.engine;

import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public interface GrenadePhysics {

    Location getCurrentLocation();


    void launch(ItemStack stack, StrikePlayer player, boolean wasLeftClick, Game game);

    void removeEntity();

    boolean update(Game game);

    boolean touchedGround();

    void multiplyVelocity(float f);

    void moveRebounding(Game game);


    int getGroundTicks();
}
