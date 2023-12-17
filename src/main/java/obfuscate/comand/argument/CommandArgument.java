package obfuscate.comand.argument;

import obfuscate.comand.ExecutionContext;
import obfuscate.comand.exception.CommandArgParseException;

import java.util.List;
import java.util.function.Function;

public interface CommandArgument<T> {

    String getName();
    String getDescription();
    T parse(String argument) throws CommandArgParseException;

    String validate(ExecutionContext ctx, Object value);

    List<String> getOptions(ExecutionContext context);

    CommandArgument<T> validator(Function<AbstractArg<T>.ValidationContext, String> validator);

    CommandArgument<T> options(Function<ExecutionContext, List<String>> optionsGenerator);

    CommandArgument<T> arbitrary(boolean allow);

    CommandArgument<T> options(List<String> staticOptions);

    CommandArgument<T> options(String ... staticOptions);

    String getTypeName();

    List<String> getStaticOptions();

    default boolean isGreedy() {
        return false;
    };
}
