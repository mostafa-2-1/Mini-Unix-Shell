package shell.history;

import org.jline.reader.History;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class HistoryManager {

    private final List<String> commandHistory = new ArrayList<>();
    private int lastHistoryWriteIndex = 0;

    public HistoryManager() {}

    /* === STARTUP PRELOAD (HISTFILE) === */

    public void preload(Path histFile, History jlineHistory) throws IOException {
        if (histFile == null || !Files.exists(histFile)) return;

        try (var lines = Files.lines(histFile)) {
            lines.map(String::trim)
                 .filter(s -> !s.isEmpty())
                 .forEach(s -> {
                     commandHistory.add(s);
                     jlineHistory.add(Instant.now(), s);
                 });
        }

        lastHistoryWriteIndex = commandHistory.size();
    }

    /* === PER-READLINE UPDATE === */

    public synchronized void record(String commandLine, History jlineHistory) {
        if (commandLine == null || commandLine.trim().isEmpty()) return;

        commandHistory.add(commandLine);
        jlineHistory.add(Instant.now(), commandLine);
    }

    public synchronized void markWritten() {
        lastHistoryWriteIndex = commandHistory.size();
    }

    /* === history (print / print n) === */

    public void print(PrintWriter out, Integer n) {
        int historySize = commandHistory.size();

        int indexStart = 1;
        if (n != null && n > 0 && n < historySize) {
            indexStart = historySize - n + 1;
        }

        for (int i = indexStart - 1; i < historySize; i++) {
            out.printf("    %d  %s%n", i + 1, commandHistory.get(i));
        }
    }

    /* === history -r <file> === */

    public void read(Path file, History jlineHistory) throws IOException {
        if (file == null || !Files.exists(file)) {
            throw new IOException("history file does not exist");
        }

        try (var lines = Files.lines(file)) {
            lines.map(String::trim)
                 .filter(s -> !s.isEmpty())
                 .forEach(s -> {
                     commandHistory.add(s);
                     jlineHistory.add(Instant.now(), s);
                 });
        }
    }

    /* === history -w <file> === */

    public void write(Path file) throws IOException {
        if (file.getParent() != null) Files.createDirectories(file.getParent());

        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(file))) {
            for (String cmd : commandHistory) out.println(cmd);
        }
    }

    /* === history -a <file> === */

    public synchronized void append(Path file) throws IOException {
        if (file.getParent() != null) Files.createDirectories(file.getParent());

        try (PrintWriter out = new PrintWriter(
                Files.newBufferedWriter(file,
                        java.nio.file.StandardOpenOption.CREATE,
                        java.nio.file.StandardOpenOption.APPEND))) {

            for (int i = lastHistoryWriteIndex; i < commandHistory.size(); i++) {
                out.println(commandHistory.get(i));
            }
        }

        lastHistoryWriteIndex = commandHistory.size();
    }

    public void onExit(Path histFile) throws IOException {
        if (histFile != null) append(histFile);
    }
}