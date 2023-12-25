package obfuscate.util;

import obfuscate.util.chat.Message;

public class ValidationResponse {

    private final boolean valid;
    private final Message message;

    public ValidationResponse(boolean valid, Message message) {
        this.valid = valid;
        this.message = message;
    }

    public static ValidationResponse invalid(Message s) {
        return new ValidationResponse(false, s);
    }

    public static ValidationResponse valid() {
        return new ValidationResponse(true, null);
    }

    public boolean isValid() {
        return valid;
    }

    public Message getMessage() {
        return message;
    }

}
