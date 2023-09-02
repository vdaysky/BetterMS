package obfuscate.comand.argument;

import obfuscate.comand.CommandLevel;
import obfuscate.comand.ExecutionContext;
import obfuscate.comand.WrappedSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public abstract class AbstractArg<T> implements CommandArgument<T> {

    public class ValidationContext {

        private final WrappedSender sender;
        private final T value;

        private final CommandLevel level;

        public ValidationContext(WrappedSender sender, CommandLevel level, T value) {
            this.sender = sender;
            this.value = value;
            this.level = level;
        }

        public WrappedSender getSender() {
            return sender;
        }

        public T getValue() {
            return value;
        }

        public CommandLevel getCommand() {
            return level;
        }
    }

    private String name;

    private String description;

    private List<String> staticOptions = new ArrayList<>();

    private boolean optionsProvided = false;

    private Function<ValidationContext, String> validator = x -> null;

    private Function<ExecutionContext, List<String>> optionsGenerator = ctx -> new ArrayList<>();

    private boolean allowArbitraryOptions = false;

    public AbstractArg(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public List<String> getOptions(ExecutionContext context) {
        List<String> opts = new ArrayList<>();
        opts.addAll(staticOptions);
        opts.addAll(optionsGenerator.apply(context));

        return opts;
    }


    @Override
    public CommandArgument<T> validator(Function<ValidationContext, String> validator) {
        this.validator = validator;
        return this;
    }

    @Override
    public CommandArgument<T> options(Function<ExecutionContext, List<String>> optionsGenerator) {
        this.optionsGenerator = optionsGenerator;
        this.optionsProvided = true;
        return this;
    }

    @Override
    public CommandArgument<T> arbitrary(boolean allow) {
        this.allowArbitraryOptions = allow;
        return this;
    }

    @Override
    public CommandArgument<T> options(List<String> staticOptions) {
        this.staticOptions.addAll(staticOptions);
        this.optionsProvided = true;
        return this;
    }

    @Override
    public CommandArgument<T> options(String ... options) {
        Collections.addAll(staticOptions, options);
        this.optionsProvided = true;
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String validate(ExecutionContext ctx, Object value) {
        if (!allowArbitraryOptions) {
            if (optionsProvided && !getOptions(ctx).contains(value.toString())) {
                return "Invalid value '" + value + "' for argument " + getName();
            }
        }
        return validator.apply(new ValidationContext(ctx.getSender(), ctx.getLevel(), (T) value));
    }

    @Override
    public List<String> getStaticOptions() {
        return staticOptions;
    }

    @Override
    public String getDescription() {
        return description;
    }


}
