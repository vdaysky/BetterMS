package obfuscate.comand.argument;

import obfuscate.comand.exception.CommandArgParseException;

public class StrArg extends AbstractArg<String> {

    public static CommandArgument<?> of(String name, String description) {
        return new StrArg(name, description);
    }

    public StrArg(String name, String description) {
        super(name, description);
    }

    @Override
    public String parse(String argument) throws CommandArgParseException {

        if (argument == null) {
            return null;
        }
            // why was it here?
//        if (argument.length() == 0) {
//            throw new CommandArgParseException();
//        }
        return argument;
    }

    @Override
    public String toString() {
        return "<StrArg " + getName() + ">";
    }

    @Override
    public String getTypeName() {
        return "Str";
    }

    @Override
    public boolean isGreedy() {
        return false;
    }
}
