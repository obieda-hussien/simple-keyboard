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

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Handles local storage and persistence of learning data.
 * Stores word frequencies, n-gram models, and user preferences.
 */
public class LocalStorage {
    private static final String PREF_NAME = "simple_keyboard_learning";
    private static final String KEY_WORD_FREQUENCIES = "word_frequencies";
    private static final String KEY_BIGRAM_DATA = "bigram_data";
    private static final String KEY_TRIGRAM_DATA = "trigram_data";
    private static final String KEY_USER_WORDS = "user_words";
    private static final String SEPARATOR = "|||";
    private static final String PAIR_SEPARATOR = ":::";

    private final SharedPreferences preferences;

    public LocalStorage(Context context) {
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Saves word frequencies to local storage.
     */
    public void saveWordFrequencies(WordTrie wordTrie) {
        // This is a simplified implementation
        // In a real implementation, you'd serialize the trie structure
        // For now, we'll save frequently used words
        Set<String> userWords = new HashSet<>();
        // This would need to be implemented to extract words from trie
        // userWords = extractWordsFromTrie(wordTrie);
        
        preferences.edit()
                .putStringSet(KEY_USER_WORDS, userWords)
                .apply();
    }

    /**
     * Loads word frequencies from local storage.
     */
    public void loadWordFrequencies(WordTrie wordTrie) {
        Set<String> userWords = preferences.getStringSet(KEY_USER_WORDS, new HashSet<String>());
        
        for (String word : userWords) {
            if (!TextUtils.isEmpty(word)) {
                wordTrie.insert(word);
            }
        }
    }

    /**
     * Saves N-gram model data to local storage.
     */
    public void saveNGramData(NGramModel ngramModel) {
        // This is a simplified implementation
        // In a real implementation, you'd serialize the n-gram maps
        // For now, we'll use a basic string-based storage approach
        
        preferences.edit()
                .putString(KEY_BIGRAM_DATA, "")  // Placeholder
                .putString(KEY_TRIGRAM_DATA, "") // Placeholder
                .apply();
    }

    /**
     * Loads N-gram model data from local storage.
     */
    public void loadNGramData(NGramModel ngramModel) {
        String bigramData = preferences.getString(KEY_BIGRAM_DATA, "");
        String trigramData = preferences.getString(KEY_TRIGRAM_DATA, "");
        
        // Deserialize and load data into the model
        // This would need proper implementation for production use
    }

    /**
     * Adds a new word to user vocabulary.
     */
    public void addUserWord(String word) {
        if (TextUtils.isEmpty(word)) return;
        
        Set<String> userWords = new HashSet<>(preferences.getStringSet(KEY_USER_WORDS, new HashSet<String>()));
        userWords.add(word.toLowerCase().trim());
        
        preferences.edit()
                .putStringSet(KEY_USER_WORDS, userWords)
                .apply();
    }

    /**
     * Removes a word from user vocabulary.
     */
    public void removeUserWord(String word) {
        if (TextUtils.isEmpty(word)) return;
        
        Set<String> userWords = new HashSet<>(preferences.getStringSet(KEY_USER_WORDS, new HashSet<String>()));
        userWords.remove(word.toLowerCase().trim());
        
        preferences.edit()
                .putStringSet(KEY_USER_WORDS, userWords)
                .apply();
    }

    /**
     * Clears all learning data.
     */
    public void clearAllData() {
        preferences.edit()
                .remove(KEY_WORD_FREQUENCIES)
                .remove(KEY_BIGRAM_DATA)
                .remove(KEY_TRIGRAM_DATA)
                .remove(KEY_USER_WORDS)
                .apply();
    }

    /**
     * Gets the number of stored user words.
     */
    public int getUserWordCount() {
        Set<String> userWords = preferences.getStringSet(KEY_USER_WORDS, new HashSet<String>());
        return userWords.size();
    }
}