package obfuscate.game.state;

public enum StatePlace
{
    PREPEND(-1), // add state before current state
    SET(0),      // add state at current state
    APPEND(1),   // add state after current state
    PRE_BEGIN(-999), // add state before first state of the list
    ;

    final int offset;

    StatePlace(int offset) {
        this.offset = offset;
    }

    public int apply(int currentStateIndex) {
        return Math.max(-1, currentStateIndex + offset);
    }
}
