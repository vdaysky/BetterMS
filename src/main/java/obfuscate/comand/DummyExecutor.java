package obfuscate.comand;

import obfuscate.comand.builder.CommandExecutor;

public class DummyExecutor implements CommandExecutor {

    @Override
    public boolean execute(ExecutionContext ctx) {
        return true;
    }
}
