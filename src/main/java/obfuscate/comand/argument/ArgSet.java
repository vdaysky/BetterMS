package obfuscate.comand.argument;

import obfuscate.MsdmPlugin;
import obfuscate.comand.exception.CommandArgParseException;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ArgSet implements Iterable<ParsedArgument<?>> {
    HashMap<String, ParsedArgument<?>> arguments = new HashMap<>();
    ArrayList<String> orderOfInsertion = new ArrayList<>();

    ArrayList<ArgSet> savepoints = new ArrayList<>();

    /** Add value to argument set. Parses string into argument.
     *
     * @param argument Argument declaration
     * @param value String value to parse
     * @param isPositional Whether this argument is positional or named
     * @param isInvalid Whether this argument is invalid. If true, parsed value will be null
     * */
    public <T> void add(CommandArgument<T> argument, String value, boolean isPositional, boolean isInvalid) throws CommandArgParseException {
        T parsedValue = null;

        if (!isInvalid) {
            parsedValue = argument.parse(value);
        }

        var arg = new ParsedArgument<>(value, argument, isPositional, isInvalid, parsedValue);
        arguments.put(argument.getName(), arg);
        orderOfInsertion.add(argument.getName());
    }

    public <T> void add(CommandArgument<T> argument, String value, boolean isPositional) throws CommandArgParseException {
        add(argument, value, isPositional, false);
    }

    public boolean containsNamedArg(String name) {
        return arguments.containsKey(name);
    }

    public boolean containsNamedArg(CommandArgument<?> arg) {
        return containsNamedArg(arg.getName());
    }
    
    @Nullable
    public Object getParsed(String name) {
        ParsedArgument<?> value_with_meta = arguments.get(name);

        if (value_with_meta == null)
            return null;

        return value_with_meta.getParsedValue();
    }

    public String getRaw(String name) {
        ParsedArgument<?> value_with_meta = arguments.get(name);

        if (value_with_meta == null)
            return null;

        return value_with_meta.getRawValue();
    }

    /** Get raw string value of the argument */
    public String getRaw(CommandArgument<?> arg) {
        return getRaw(arg.getName());
    }

    public String getStr(String name) {
        return getParsed(name).toString();
    }

    public boolean has(CommandArgument<?> opt_arg) {
        return arguments.containsKey(opt_arg.getName());
    }

    public int size() {
        return arguments.size();
    }

    public ParsedArgument<?> getLast() {
        if (arguments.isEmpty()) return null;
        return arguments.get(orderOfInsertion.get(orderOfInsertion.size() - 1));
    }

    public ParsedArgument<?> getAt(int i) {
        if (arguments.isEmpty()) return null;
        return arguments.get(orderOfInsertion.get(i));
    }

    @Override
    public String toString() {
        StringBuilder repr = new StringBuilder();

        for (String argName : arguments.keySet()) {
            repr.append(argName).append("=").append(arguments.get(argName).getParsedValue()).append(", ");
        }

        return "{" + repr + "}";
    }

    /** Check if variable was passed as key but value was not yet supplied */
    public boolean isNull(String name) {
      return containsNamedArg(name) && arguments.get(name).getRawValue() == null;
    }

    public boolean isEmpty() {
        return arguments.isEmpty();
    }

    public Iterator<ParsedArgument<?>> values() {
        return arguments.values().iterator();
    }

    @Override
    public Iterator<ParsedArgument<?>> iterator() {
        return values();
    }

    public ArgSet copy() {
        var copy = new ArgSet();
        copy.arguments = new HashMap<>();
        copy.orderOfInsertion = new ArrayList<>();

        for (String key : arguments.keySet()) {
            var arg = arguments.get(key);
            copy.arguments.put(key, arg.copy());
        }
        copy.orderOfInsertion.addAll(orderOfInsertion);
        return copy;
    }

    public void savepoint() {
        savepoints.add(copy());
    }

    public void rollback() {
        if (savepoints.isEmpty()) {
            throw new IllegalStateException("No savepoints to rollback to");
        }
        var argset = savepoints.remove(savepoints.size() - 1);
        this.arguments = argset.arguments;
        this.orderOfInsertion = argset.orderOfInsertion;
    }
}
