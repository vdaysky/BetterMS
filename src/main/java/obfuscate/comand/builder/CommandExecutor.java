package obfuscate.comand.builder;

import obfuscate.comand.ExecutionContext;

public interface CommandExecutor {

    boolean execute(ExecutionContext context);

    default String wouldFail(ExecutionContext context) {
        return null;
    }
}
