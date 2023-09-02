package obfuscate.comand.builder;

import obfuscate.comand.ExecutionContext;
import obfuscate.comand.argument.CommandArgument;
import obfuscate.comand.argument.OptionalArgs;
import obfuscate.comand.argument.PositionalArgs;
import obfuscate.comand.autocomplete.CommandAutocomplete;
import obfuscate.permission.Permission;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.Function;

public class CommandBuilder {

    PositionalArgs req_args = new PositionalArgs();
    OptionalArgs opt_args = new OptionalArgs();
    Permission perm = Permission.ANY;
    CommandExecutor exec = null;
    String description = "";
    boolean playerRequired = false;
    private CommandAutocomplete completer = null;
    HashMap<String, CommandHandler> childCommands = new HashMap<>();
    private CommandHandler builtHandler = null;

    private boolean implemented = true;

    private Function<ExecutionContext, String> condition = x -> null;

    public CommandBuilder permission(Permission perm) {
        this.perm = perm;
        return this;
    }

    public CommandBuilder executor(CommandExecutor executor) {
        exec = executor;
        return this;
    }

    public CommandBuilder notImplemented() {
        implemented = false;
        return this;
    }

    public CommandBuilder required(CommandArgument<?> ... args) {
        req_args = new PositionalArgs(args);
        return this;
    }

    public CommandBuilder optional(CommandArgument<?> ... args) {
        opt_args = new OptionalArgs(args);
        return this;
    }

    public CommandBuilder description(String description)
    {
        this.description = description;
        return this;
    }

    public CommandBuilder condition(Function<ExecutionContext, String> conditionFunction) {
        condition = conditionFunction;
        return this;
    }

    public CommandBuilder child(String name, CommandBuilder command)
    {
        var handler = command.build();
        handler.setParent(this);
        childCommands.put(name, handler);

        return this;
    }

    public CommandBuilder requirePlayer(boolean b) {
        playerRequired = b;
        return this;
    }

    public CommandBuilder autocomplete(CommandAutocomplete completer) {
        this.completer = completer;
        return this;
    }

    public CommandHandler build(CommandBuilder parent) {

        if (exec == null && childCommands.isEmpty()) {
            throw new InvalidParameterException("Executor must be defined for leaf command");
        }

        CommandHandler handler;

        handler = new CommandHandler(
                exec,
                perm,
                description,
                req_args,
                opt_args,
                playerRequired,
                childCommands,
                completer,
                implemented,
                Objects.requireNonNullElseGet(condition, () -> exec::wouldFail)
        );

        handler.setParent(parent);

        builtHandler = handler;
        return handler;
    }

    public CommandHandler build() {
        return build(null);
    }

    public Permission getRequiredPermission() {
        if (perm != null) {
            return perm;
        }

        if (builtHandler != null) {
            return builtHandler.getParent().getRequiredPermission();
        }

        return null;
    }

}
