package seabattlerest;

import java.time.LocalDateTime;

public class MessageLogic {

    private String message;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    MessageLogic() {
        this.message = "Hello! This is the default message!";
    }

    public String getInfoMessage() {
        LocalDateTime now = LocalDateTime.now();
        return now.toString() + " " + message;
    }

}
