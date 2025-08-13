/*
 * Copyright (C) 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rkr.simplekeyboard.inputmethod.latin.learning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Trie data structure for efficient word storage and prefix-based search.
 * Supports word frequency tracking and suggestion generation.
 */
public class WordTrie {
    private TrieNode root;
    private static final int MAX_SUGGESTIONS = 5;

    public WordTrie() {
        this.root = new TrieNode();
    }

    /**
     * Inserts a word into the trie with frequency tracking.
     */
    public void insert(String word) {
        if (word == null || word.trim().isEmpty()) {
            return;
        }

        word = word.toLowerCase().trim();
        TrieNode current = root;

        for (char c : word.toCharArray()) {
            if (!current.hasChild(c)) {
                current.putChild(c, new TrieNode());
            }
            current = current.getChild(c);
        }

        current.setEndOfWord(true);
        current.incrementFrequency();
    }

    /**
     * Searches for words with the given prefix and returns suggestions.
     */
    public List<String> getSuggestions(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return new ArrayList<>();
        }

        prefix = prefix.toLowerCase().trim();
        TrieNode current = root;

        // Navigate to the prefix node
        for (char c : prefix.toCharArray()) {
            if (!current.hasChild(c)) {
                return new ArrayList<>();
            }
            current = current.getChild(c);
        }

        // Collect all words with this prefix
        List<WordSuggestion> suggestions = new ArrayList<>();
        collectSuggestions(current, prefix, suggestions);

        // Sort by frequency and recency
        Collections.sort(suggestions, new Comparator<WordSuggestion>() {
            @Override
            public int compare(WordSuggestion a, WordSuggestion b) {
                // Primary sort: frequency (descending)
                int freqCompare = Integer.compare(b.frequency, a.frequency);
                if (freqCompare != 0) {
                    return freqCompare;
                }
                // Secondary sort: recency (descending)
                return Long.compare(b.lastUsed, a.lastUsed);
            }
        });

        // Convert to string list and limit results
        List<String> result = new ArrayList<>();
        int limit = Math.min(suggestions.size(), MAX_SUGGESTIONS);
        for (int i = 0; i < limit; i++) {
            result.add(suggestions.get(i).word);
        }

        return result;
    }

    /**
     * Recursively collects all words from the given node.
     */
    private void collectSuggestions(TrieNode node, String prefix, List<WordSuggestion> suggestions) {
        if (node.isEndOfWord()) {
            suggestions.add(new WordSuggestion(prefix, node.getFrequency(), node.getLastUsed()));
        }

        for (char c : node.getChildren().keySet()) {
            collectSuggestions(node.getChild(c), prefix + c, suggestions);
        }
    }

    /**
     * Checks if a word exists in the trie.
     */
    public boolean contains(String word) {
        if (word == null || word.trim().isEmpty()) {
            return false;
        }

        word = word.toLowerCase().trim();
        TrieNode current = root;

        for (char c : word.toCharArray()) {
            if (!current.hasChild(c)) {
                return false;
            }
            current = current.getChild(c);
        }

        return current.isEndOfWord();
    }

    /**
     * Gets the frequency of a word.
     */
    public int getWordFrequency(String word) {
        if (word == null || word.trim().isEmpty()) {
            return 0;
        }

        word = word.toLowerCase().trim();
        TrieNode current = root;

        for (char c : word.toCharArray()) {
            if (!current.hasChild(c)) {
                return 0;
            }
            current = current.getChild(c);
        }

        return current.isEndOfWord() ? current.getFrequency() : 0;
    }

    /**
     * Internal class for word suggestions with metadata.
     */
    private static class WordSuggestion {
        final String word;
        final int frequency;
        final long lastUsed;

        WordSuggestion(String word, int frequency, long lastUsed) {
            this.word = word;
            this.frequency = frequency;
            this.lastUsed = lastUsed;
        }
    }
}