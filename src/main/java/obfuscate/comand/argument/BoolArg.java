package obfuscate.comand.argument;

import obfuscate.comand.ExecutionContext;
import obfuscate.comand.exception.CommandArgParseException;
import obfuscate.util.UtilMath;

import java.util.Arrays;
import java.util.List;

public class BoolArg extends AbstractArg<Boolean> {

    public BoolArg(String name, String description) {
        super(name, description);
    }

    @Override
    public Boolean parse(String argument) throws CommandArgParseException {

        if (argument == null) {
            return null;
        }

        if (UtilMath.isNumeric(argument)) {
            return Integer.parseInt(argument) != 0;
        }

        if (argument.equals("true")) return true;
        if (argument.equals("false")) return false;

        throw new CommandArgParseException();
    }

    @Override
    public List<String> getOptions(ExecutionContext context) {
        return Arrays.asList("true", "false");
    }

    @Override
    public String toString() {
        return "<BoolArg " + getName() + ">";
    }

    @Override
    public String getTypeName() {
        return "Bool";
    }

    @Override
    public boolean isGreedy() {
        return false;
    }
}
