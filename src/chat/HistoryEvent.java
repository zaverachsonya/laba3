package chat;

import java.io.Serializable;

public record HistoryEvent(String content) implements Serializable {}