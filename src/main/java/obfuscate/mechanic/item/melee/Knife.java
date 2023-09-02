package obfuscate.mechanic.item.melee;

import obfuscate.mechanic.item.StrikeItem;

public class Knife extends StrikeItem {

    public Knife() {
        super(MeleeTypes.KNIFE);
    }

    @Override
    public StrikeItem copy() {
        return new Knife();
    }
}
