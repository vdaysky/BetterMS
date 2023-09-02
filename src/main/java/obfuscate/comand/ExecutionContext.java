package obfuscate.comand;

import obfuscate.game.player.StrikePlayer;

public class ExecutionContext {

    /** what command are we executing */
    private CommandLevel level;

    /** who executes */
    private WrappedSender sender;

    /** what label was used */
    private String label;

    public ExecutionContext(CommandLevel level, WrappedSender sender, String label) {
        this.level = level;
        this.sender = sender;
        this.label = label;
    }

    public CommandLevel getLevel() {
        return level;
    }

    public WrappedSender getSender() {
        return sender;
    }

    public String getLabel() {
        return label;
    }

    public <T> T getRequired(String name) {
        return (T) getLevel().getParsedRequired().getParsed(name);
    }
    public <T> T getOptional(String name) {
        return (T) getLevel().getParsedOptional().getParsed(name);
    }

    public StrikePlayer getPlayer() {
        return getSender().getPlayer();
    }

}
