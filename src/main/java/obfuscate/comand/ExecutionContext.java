package obfuscate.comand;

import obfuscate.game.player.StrikePlayer;

import java.util.ArrayList;

public class ExecutionContext {

    /** what command are we executing */
    private CommandLevel level;

    /** who executes */
    private WrappedSender sender;

    /** what label was used */
    private String label;

    private ArrayList<Object> scopes = new ArrayList<>();

    public ExecutionContext(CommandLevel level, WrappedSender sender, String label) {
        this.level = level;
        this.sender = sender;
        this.label = label;
    }

    public void addScope(Object scope) {
        this.scopes.add(scope);
    }

    public ArrayList<Object> getScopes() {
        return scopes;
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
        return (T) getRequiredRecursive(name);
    }

    public Object getRequiredRecursive(String name) {
        CommandLevel level = getLevel();
        while (level != null) {
            Object value = level.getParsedRequired().getParsed(name);
            if (value != null) {
                return value;
            }
            level = level.getParent();
        }
        return null;
    }

    public Object getOptionalRecursive(String name) {
        CommandLevel level = getLevel();
        while (level != null) {
            Object value = level.getParsedOptional().getParsed(name);
            if (value != null) {
                return value;
            }
            level = level.getParent();
        }
        return null;
    }

    public String getRawRequired(String name) {
        return getLevel().getParsedRequired().getRaw(name);
    }

    public String getRawOptional(String name) {
        return getLevel().getParsedOptional().getRaw(name);
    }

    public <T> T getOptional(String name) {
        return (T) getOptionalRecursive(name);
    }

    public StrikePlayer getPlayer() {
        return getSender().getPlayer();
    }

}
