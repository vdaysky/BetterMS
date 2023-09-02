package obfuscate.comand.builder;

import obfuscate.MsdmPlugin;
import obfuscate.comand.DummyExecutor;
import obfuscate.comand.ExecutionContext;
import obfuscate.comand.argument.OptionalArgs;
import obfuscate.comand.argument.PositionalArgs;
import obfuscate.comand.autocomplete.CommandAutocomplete;
import obfuscate.comand.autocomplete.DefaultAutocomplete;
import obfuscate.permission.Permission;

import java.util.HashMap;
import java.util.function.Function;

public class CommandHandler {
    private final PositionalArgs req_args;
    private final OptionalArgs opt_args;
    private final Permission perm;
    private final Function<ExecutionContext, Boolean> exec;
    private final String description;
    private final boolean playerRequired;
    private final HashMap<String, CommandHandler> children;
    private CommandBuilder parent = null;
    private final CommandAutocomplete completer;

    private final Function<ExecutionContext, String> condition;

    private final boolean implemented;
    private final boolean hasExecutor;

    public CommandHandler(
            CommandExecutor exec,
            Permission perm,
            String description,
            PositionalArgs req_args,
            OptionalArgs opt_args,
            boolean playerRequired,
            HashMap<String, CommandHandler> children,
            CommandAutocomplete completer,
            boolean implemented,
            Function<ExecutionContext, String> condition
    ) {
        this.req_args = req_args;
        this.opt_args = opt_args;
        this.perm = perm;

        hasExecutor = exec != null;

        // always allow to propagate
        if (exec == null) {
            exec = new DummyExecutor();
        }

        this.exec = exec::execute;

        this.description = description;
        this.playerRequired = playerRequired;
        this.children = children;
        this.completer = completer;
        this.condition = condition;
        this.implemented = implemented;
    }

    public boolean isImplemented() {
        return implemented;
    }

    public boolean hasExecutor() {
        return hasExecutor;
    }

    public void setParent(CommandBuilder parent) {
        this.parent = parent;
    }

    public CommandAutocomplete getCompleter() {
        if (completer != null)
            return completer;

        return new DefaultAutocomplete();
    }

    public CommandHandler getChildCommand(String arg) {
        return children.get(arg);
    }

    public HashMap<String, CommandHandler> getChildren() {
        return children;
    }

    public boolean isPlayerRequired() {
        return playerRequired;
    }

    public Function<ExecutionContext, Boolean> getExecutor() {
        return exec;
    }

    public Permission getRequiredPermission() {
        if (perm != null) {
            return perm;
        }
        if (getParent() != null) {
            return getParent().getRequiredPermission();
        }
        return null;
    }

    public String getDescription() {
        return description;
    }

    public OptionalArgs getDeclaredOptionalArgs() {
        return opt_args;
    }

    public PositionalArgs getDeclaredPositionalArgs() {
        return req_args;
    }

    public CommandBuilder getParent() {
        return parent;
    }

    public Function<ExecutionContext, String> getCondition() {
        return condition;
    }

    public String findNameOf(CommandHandler handler) {
        if (parent == null) {
            return null;
        }

        for (String key : parent.childCommands.keySet()) {
            MsdmPlugin.highlight("childCommands>key: " + key);
            if (parent.childCommands.get(key) == handler) {
                return key;
            }
        }
        return null;
    }
}
