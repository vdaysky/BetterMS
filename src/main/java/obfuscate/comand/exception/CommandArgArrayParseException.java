package obfuscate.comand.exception;

public class CommandArgArrayParseException extends Exception {

    private int argIndex;

    public CommandArgArrayParseException(int argIndex) {
        this.argIndex = argIndex;
    }

    public int getArgIndex() {
        return argIndex;
    }
}
