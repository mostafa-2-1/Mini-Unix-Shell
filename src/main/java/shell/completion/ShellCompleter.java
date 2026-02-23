package shell.completion;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.reader.Candidate;
import shell.builtin.BuiltinRegistry;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShellCompleter implements Completer {

    private final BuiltinRegistry registry;

    private volatile Trie trie = new Trie();
    private String lastTabInput = null;

    private String cachedPathEnv = null;

    public ShellCompleter(BuiltinRegistry registry) {
        this.registry = registry;
        rebuildTrieIfNeeded(); // initial build
    }

    private void rebuildTrieIfNeeded() {
        String pathEnv = System.getenv("PATH");
        if (pathEnv == null) pathEnv = "";

        // rebuild only if PATH changed
        if (pathEnv.equals(cachedPathEnv) && trie != null) {
            return;
        }
        cachedPathEnv = pathEnv;

        Trie newTrie = new Trie();

        // builtins from registry
        for (String name : registry.names()) {
            newTrie.insert(name);
        }

        // executables from PATH snapshot
        if (!pathEnv.isEmpty()) {
            for (String dir : pathEnv.split(File.pathSeparator)) {
                if (dir == null || dir.isEmpty()) continue;

                File folder = new File(dir);
                if (!folder.isDirectory()) continue;

                File[] files = folder.listFiles();
                if (files == null) continue;

                for (File f : files) {
                    if (f.isFile() && f.canExecute()) {
                        newTrie.insert(f.getName());
                    }
                }
            }
        }

        trie = newTrie;
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        rebuildTrieIfNeeded();

        String input = line.word();
        List<String> matches = trie.getWordsWithPrefix(input);

        if (matches.isEmpty()) {
            bell(reader);
            lastTabInput = null;
            return;
        }

        if (matches.size() == 1) {
            String full = matches.get(0) + " ";
            int cursor = line.wordCursor();
            reader.getBuffer().write(full.substring(cursor));
            reader.callWidget(LineReader.REDISPLAY);
            lastTabInput = null;
            return;
        }

        ArrayList<String> sorted = new ArrayList<>(matches);
        Collections.sort(sorted);
        String lcp = longestCommonPrefix(sorted);

        if (lcp.length() > input.length()) {
            reader.getBuffer().write(lcp.substring(input.length()));
            reader.callWidget(LineReader.REDISPLAY);
            lastTabInput = null;
        } else if (input.equals(lastTabInput)) {
            reader.getTerminal().writer().println();
            reader.getTerminal().writer().println(String.join("  ", matches));
            reader.getTerminal().writer().flush();
            reader.callWidget(LineReader.REDRAW_LINE);
            reader.callWidget(LineReader.REDISPLAY);
            lastTabInput = null;
        } else {
            bell(reader);
            reader.callWidget(LineReader.REDISPLAY);
            lastTabInput = input;
        }
    }

    private void bell(LineReader reader) {
        reader.getTerminal().writer().write("\u0007");
        reader.getTerminal().writer().flush();
    }

    private String longestCommonPrefix(ArrayList<String> strs) {
        String s1 = strs.get(0);
        String s2 = strs.get(strs.size() - 1);
        int idx = 0;
        while (idx < s1.length() && idx < s2.length() && s1.charAt(idx) == s2.charAt(idx)) {
            idx++;
        }
        return s1.substring(0, idx);
    }
}
