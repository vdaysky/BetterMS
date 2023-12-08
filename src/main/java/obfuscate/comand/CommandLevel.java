package obfuscate.comand;

import obfuscate.MsdmPlugin;
import obfuscate.comand.argument.ArgSet;
import obfuscate.comand.argument.CommandArgument;
import obfuscate.comand.argument.ParsedArgument;
import obfuscate.comand.builder.CommandHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CommandLevel {

    private final CommandHandler handler;

    private final CommandLevel child;

    private final ArgSet required;

    private final ArgSet optional;

    private final String label;

    /** List of arguments that were not yet parsed into command.
     * Some of them may be taken by current level, some may be passed to the next one
     * */
    private final List<String> leftArgs;


    public CommandLevel(CommandHandler handler, ArgSet required, ArgSet optional, CommandLevel child, String label, List<String> leftArgs) {
        this.handler = handler;
        this.required = required;
        this.optional = optional;
        this.child = child;
        this.leftArgs = leftArgs;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public CommandHandler getHandler() {
        return handler;
    }

    public ArgSet getParsedRequired() {
        return required;
    }

    public ArgSet getParsedOptional() {
        return optional;
    }

    public @Nullable CommandLevel getChild() {
        return child;
    }

    public List<CommandArgument<?>> missingRequired() {
        List<CommandArgument<?>> args = new ArrayList<>();
        for (CommandArgument<?> req : getHandler().getDeclaredPositionalArgs().args) {
            if (!required.containsNamedArg(req.getName())) {
                args.add(req);
            }
        }
        return args;
    }

    public List<String> getArgsLeft() {
        return Objects.requireNonNullElseGet(
                leftArgs,
                ArrayList::new // there are no args left
        );
    }

    /** Get optional argument declarations that were not passed to the command or not completed
     * (value not provided after prefix)
     *
     * @return list of optional argument declarations
     * */
    public List<CommandArgument<?>> missingOptionals(boolean includeNull) {
        List<CommandArgument<?>> args = new ArrayList<>();
        for (CommandArgument<?> opt : getHandler().getDeclaredOptionalArgs().args) {
            // either value is not present or it is null, meaning that prefix was specified, but was not followed by value
            if (!optional.containsNamedArg(opt.getName()) || (includeNull && optional.isNull(opt.getName()))) {
                args.add(opt);
            }
        }

        return args;
    }

    public CommandLevel reachLeaf() {
        CommandLevel level = this;
        while (level.getChild() != null) {
            level = level.getChild();
        }
        return level;
    }

    /** Find first invalid argument in the command.
     * Note that last argument is never considered invalid, because it is in progress. */
    public ParsedArgument<?> getFirstInvalidArgument() {

//        MsdmPlugin.highlight("Attempt to check if there is an invalid arg");
        int requiredSize = required.size();

        // if there are no optionals, then last required argument is in progress
        if (optional.size() == 0) {
            requiredSize -= 1;
        }

        for (int i = 0; i < requiredSize; i++) {
//            MsdmPlugin.highlight("Check required at " + i + " : " + required.getAt(i).getDeclaration().getName() + " invalid: " + required.getAt(i).isInvalid());
            if (required.getAt(i).isInvalid()) {
                return required.getAt(i);
            }
        }
        for (int i = 0; i < optional.size() - 1; i++) {
//            MsdmPlugin.highlight("Check opt at " + i + " : " + optional.getAt(i).getDeclaration().getName() + " invalid: " + optional.getAt(i).isInvalid());
            if (optional.getAt(i).isInvalid()) {
                return optional.getAt(i);
            }
        }
        return null;
    }
}