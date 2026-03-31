package chat;
import java.util.*;

public class HistoryManager {
    private final List<String> logs = Collections.synchronizedList(new ArrayList<>());

    public void save(String message) {
        synchronized (logs) {
            if (message.contains(">>> ПОДКЛЮЧИЛСЯ:")) {
                String name = extractName(message);
                if (hasEventFor(name, ">>> ПОДКЛЮЧИЛСЯ:")) return;
            }
            if (!logs.contains(message)) {
                logs.add(message);
            }
        }
    }

    public String exportAll() {
        synchronized (logs) {
            return String.join("\n", logs);
        }
    }

    public void importHistory(String data) {
        if (data == null || data.isEmpty()) return;
        String[] lines = data.split("\n");
        for (String line : lines) {
            String s = line.trim();
            if (s.isEmpty()) continue;

            synchronized (logs) {
                if (s.contains(">>> ПОДКЛЮЧИЛСЯ:")) {
                    String name = extractName(s);
                    if (hasEventFor(name, ">>> ПОДКЛЮЧИЛСЯ:")) continue;
                }
                if (!logs.contains(s)) {
                    logs.add(s);
                }
            }
        }
        synchronized (logs) {
            logs.sort(Comparator.comparing(this::extractTime));
        }
    }

    private boolean hasEventFor(String name, String type) {
        return logs.stream().anyMatch(log -> log.contains(type) && log.contains(name));
    }

    private String extractName(String line) {
        try {
            if (line.contains(">>>")) return line.split(">>> ПОДКЛЮЧИЛСЯ: ")[1].split(" ")[0];
            if (line.contains("<<<")) return line.split("<<< ОТКЛЮЧИЛСЯ: ")[1].split(" ")[0];
        } catch (Exception e) {}
        return "UNKNOWN";
    }

    private String extractTime(String line) {
        try { return line.substring(1, 9); } catch (Exception e) { return "00:00:00"; }
    }
}