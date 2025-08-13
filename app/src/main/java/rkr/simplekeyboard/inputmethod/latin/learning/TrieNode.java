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

import java.util.HashMap;
import java.util.Map;

/**
 * Node class for Trie data structure used in word storage and suggestion.
 * Each node represents a character in the trie.
 */
public class TrieNode {
    private Map<Character, TrieNode> children;
    private boolean isEndOfWord;
    private int frequency;
    private long lastUsed;

    public TrieNode() {
        this.children = new HashMap<>();
        this.isEndOfWord = false;
        this.frequency = 0;
        this.lastUsed = 0;
    }

    public Map<Character, TrieNode> getChildren() {
        return children;
    }

    public boolean isEndOfWord() {
        return isEndOfWord;
    }

    public void setEndOfWord(boolean endOfWord) {
        isEndOfWord = endOfWord;
    }

    public int getFrequency() {
        return frequency;
    }

    public void incrementFrequency() {
        this.frequency++;
        this.lastUsed = System.currentTimeMillis();
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public long getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(long lastUsed) {
        this.lastUsed = lastUsed;
    }

    public TrieNode getChild(char c) {
        return children.get(c);
    }

    public void putChild(char c, TrieNode node) {
        children.put(c, node);
    }

    public boolean hasChild(char c) {
        return children.containsKey(c);
    }
}