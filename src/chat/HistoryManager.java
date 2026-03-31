package chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryManager {
    private final List<String> logs = Collections.synchronizedList(new ArrayList<>());

    public void save(String message) {
        logs.add(message);
    }

    public String exportAll() {
        return String.join("\n", logs);
    }

    public void importHistory(String data) {
        if (data == null || data.isEmpty()) return;
        String[] lines = data.split("\n");
        for (String s : lines) {
            String line = s.trim();
            if (line.isEmpty()) continue;

            synchronized (logs) {
                boolean alreadyHasJoin = line.contains(">>>") && logs.stream().anyMatch(l -> l.contains(extractName(line)));

                if (!logs.contains(line) && !alreadyHasJoin) {
                    logs.add(line);
                }
            }
        }
    }

    private String extractName(String line) {
        try {
            if (line.contains(">>>")) return line.split(">>> ПОДКЛЮЧИЛСЯ: ")[1].split(" ")[0];
            if (line.contains("<<<")) return line.split("<<< ОТКЛЮЧИЛСЯ: ")[1].split(" ")[0];
        } catch (Exception e) {}
        return "UNKNOWN_RANDOM_STRING_999";
    }
}