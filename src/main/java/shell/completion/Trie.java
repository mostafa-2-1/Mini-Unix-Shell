package shell.completion;

import java.util.ArrayList;
import java.util.List;

public class Trie {

    static class TrieNode {
        TrieNode[] children = new TrieNode[128]; // ASCII
        boolean isEndOfWord;
    }

    private final TrieNode root = new TrieNode();

    public void insert(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            if (c >= 128) {
                return;  
            }
            if (node.children[c] == null) {
                node.children[c] = new TrieNode();
            }
            node = node.children[c];
        }
        node.isEndOfWord = true;
    }

    public List<String> getWordsWithPrefix(String prefix) {
        List<String> result = new ArrayList<>();
        TrieNode node = root;

        for (char c : prefix.toCharArray()) {
            if (c >= 128) {
                return result; // no matches for non-ASCII prefix in this trie
            }
            if (node.children[c] == null) {
                return result;
            }
            node = node.children[c];
        }

        collect(node, new StringBuilder(prefix), result);
        return result;
    }

    private void collect(TrieNode node, StringBuilder current, List<String> result) {
        if (node.isEndOfWord) {
            result.add(current.toString());
        }
        for (int i = 0; i < 128; i++) {
            if (node.children[i] != null) {
                current.append((char) i);
                collect(node.children[i], current, result);
                current.deleteCharAt(current.length() - 1);
            }
        }
    }
}