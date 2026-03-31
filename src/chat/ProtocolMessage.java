package chat;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ProtocolMessage implements Serializable {
    private final MessageType type;
    private final String text;
    private final String sender;
    private final String ip;
    private final int port;
    private final String time;

    public ProtocolMessage(MessageType type, String text, String sender, String ip, int port) {
        this.type = type;
        this.text = text;
        this.sender = sender;
        this.ip = ip;
        this.port = port;
        this.time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    public String getDisplayFormat() {
        return switch (type) {
            case TEXT -> String.format("[%s] %s (%s): %s", time, sender, ip, text);
            case SYSTEM_JOIN -> String.format("[%s] >>> ПОДКЛЮЧИЛСЯ: %s [%s:%d]", time, sender, ip, port);
            case SYSTEM_LEAVE -> String.format("[%s] <<< ОТКЛЮЧИЛСЯ: %s [%s:%d]", time, sender, ip, port);
            default -> text;
        };
    }

    public MessageType getType() { return type; }
    public String getText() { return text; }
    public String getSender() { return sender; }
    public String getIp() { return ip; }
    public int getPort() { return port; }
}