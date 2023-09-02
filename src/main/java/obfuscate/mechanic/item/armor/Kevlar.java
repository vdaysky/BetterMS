package obfuscate.mechanic.item.armor;

import obfuscate.mechanic.item.StrikeItem;

public class Kevlar extends StrikeArmor {

    public Kevlar() {
        super(ArmorType.KEVLAR);
    }

    @Override
    public StrikeItem copy() {
        return new Kevlar();
    }
}
