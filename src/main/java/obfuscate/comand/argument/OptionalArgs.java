package obfuscate.comand.argument;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class OptionalArgs implements Iterable<CommandArgument<?>> {
    public ArrayList<CommandArgument<?>> args;

    public OptionalArgs(CommandArgument<?>... args) {
        this.args = new ArrayList<>(Arrays.asList(args));
    }

    @Override
    public String toString() {
        return args.toString();
    }

    @Override
    public Iterator<CommandArgument<?>> iterator() {
        return args.iterator();
    }

    public CommandArgument<?> get(int i) {
        return args.get(i);
    }
}
