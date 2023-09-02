package obfuscate.comand.argument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PositionalArgs {

    public ArrayList<CommandArgument<?>> args;

    public PositionalArgs(CommandArgument<?>... args) {
        this.args = new ArrayList<>(Arrays.asList(args));
    }

    public List<CommandArgument<?>> slice(int from) {
        return this.args.subList(from, this.args.size());
    }

    public int count() {
        return args.size();
    }

    @Override
    public String toString() {
        return args.toString();
    }
}
