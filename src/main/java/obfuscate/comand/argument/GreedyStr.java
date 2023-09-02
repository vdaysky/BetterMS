package obfuscate.comand.argument;

import obfuscate.comand.exception.CommandArgParseException;

public class GreedyStr extends AbstractArg<String> {

    public GreedyStr(String name, String description) {
        super(name, description);
    }

    public static CommandArgument<?> of(String name, String descr) {
        return new GreedyStr(name, descr);
    }

    @Override
    public String parse(String argument) throws CommandArgParseException {
        return argument;
    }

    @Override
    public String getTypeName() {
        return "GreedyStr";
    }

    @Override
    public boolean isGreedy() {
        return true;
    }
}
