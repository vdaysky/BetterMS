package obfuscate.mechanic.item.armor;

import obfuscate.mechanic.item.StrikeItem;

public class Helmet extends StrikeArmor {

    public Helmet() {
        super(ArmorType.HELMET);
    }

    @Override
    public StrikeItem copy() {
        return new Helmet();
    }
}
