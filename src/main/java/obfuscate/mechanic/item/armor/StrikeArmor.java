package obfuscate.mechanic.item.armor;

import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;
import obfuscate.mechanic.item.StrikeItem;

public abstract class StrikeArmor extends StrikeItem
{
    private final ArmorType type;

    public StrikeArmor(ArmorType type) {
        super(type);
        this.type = type;
    }

    @Override
    public Integer getSlot() {
        return type.getSlot();
    }

    @Override
    public void giveToPlayer(Game game, StrikePlayer player, boolean setOwnerName) {
        game.getGameSession(player).getInventory().setArmor(this);
        player.equip(game);
    }

}
