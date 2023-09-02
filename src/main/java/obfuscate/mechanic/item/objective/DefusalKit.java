package obfuscate.mechanic.item.objective;

import obfuscate.mechanic.item.StrikeItem;

public class DefusalKit extends StrikeItem
{
    public DefusalKit()
    {
        super(MiscItemType.DEFUSER);
    }

    @Override
    public StrikeItem copy() {
        return new DefusalKit();
    }

}
