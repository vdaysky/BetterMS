package obfuscate.comand.argument;

public class ParsedArgument<T> {

    private final String value;
    private final CommandArgument<?> declaration;

    private final boolean isPositional;

    private final T parsedValue;

    private boolean isInvalid;

    public ParsedArgument(
            String value,
            CommandArgument<?> declaration,
            boolean isPositional,
            boolean isInvalid,
            T parsedValue
    ) {
        this.value = value;
        this.declaration = declaration;
        this.isPositional = isPositional;
        this.parsedValue = parsedValue;
        this.isInvalid = isInvalid;
    }

    public String getRawValue() {
        return value;
    }

    public CommandArgument<?> getDeclaration() {
        return declaration;
    }

    public boolean isInvalid() {
        return isInvalid;
    }

    public boolean isPositional() {
        return isPositional;
    }

    public T getParsedValue() {
        return parsedValue;
    }
}
