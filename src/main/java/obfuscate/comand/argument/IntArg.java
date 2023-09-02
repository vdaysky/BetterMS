package obfuscate.comand.argument;

import obfuscate.comand.exception.CommandArgParseException;
import obfuscate.util.UtilMath;

public class IntArg extends AbstractArg<Integer> {

    public IntArg(String name, String description) {
        super(name, description);
    }

    public Integer parse(String object) throws CommandArgParseException {

        if (object == null) {
            return null;
        }

        if (UtilMath.isNumeric(object)) {
            return Integer.valueOf(object);
        }
        throw new CommandArgParseException();
    }

    @Override
    public String toString() {
        return "<IntArg " + getName() + ">";
    }

    @Override
    public String getTypeName() {
        return "Int";
    }

    @Override
    public boolean isGreedy() {
        return false;
    }
}
