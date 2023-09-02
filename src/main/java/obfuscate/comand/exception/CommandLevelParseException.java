package obfuscate.comand.exception;

import obfuscate.comand.CommandLevel;

public class CommandLevelParseException extends Exception {

    private final CommandLevel level;

    public CommandLevelParseException(CommandLevel level) {
        this.level = level;
    }

    /** Get command level at which parsing failed */
    public CommandLevel getLevel() {
        return level;
    }
}
