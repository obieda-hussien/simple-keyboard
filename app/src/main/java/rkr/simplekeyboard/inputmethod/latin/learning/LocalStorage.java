/*
 * Copyright (C) 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package rkr.simplekeyboard.inputmethod.latin.learning;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Handles private on-device persistence for personalization signals.
 */
public class LocalStorage {
    private static final String PREF_NAME = "simple_keyboard_learning";
    private static final String KEY_WORD_FREQUENCIES = "word_frequencies";
    private static final String KEY_RECENT_USAGE = "recent_usage";
    private static final String KEY_BIGRAM_DATA = "bigram_data";
    private static final String KEY_TRIGRAM_DATA = "trigram_data";
    private static final String KEY_USER_WORDS = "user_words";
    private static final String KEY_REJECTED_CORRECTIONS = "rejected_corrections";
    private static final int MAX_RANKING_SIGNAL_WORDS = 4096;

    private final SharedPreferences preferences;

    public LocalStorage(Context context) {
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Legacy compatibility hook. Individual user words are persisted through addUserWord().
     */
    public void saveWordFrequencies(WordTrie wordTrie) {
        // Intentionally empty.
    }

    public void loadWordFrequencies(WordTrie wordTrie) {
        Set<String> userWords = preferences.getStringSet(KEY_USER_WORDS, new HashSet<String>());
        for (String word : userWords) {
            if (!TextUtils.isEmpty(word)) wordTrie.insert(word);
        }
    }

    public void saveRankingSignals(Map<String, Integer> frequencies, Map<String, Long> recentUsage) {
        StringBuilder frequencyData = new StringBuilder();
        StringBuilder recentData = new StringBuilder();

        List<Map.Entry<String, Integer>> ordered = new ArrayList<>(frequencies.entrySet());
        ordered.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        int written = 0;
        for (Map.Entry<String, Integer> entry : ordered) {
            if (written >= MAX_RANKING_SIGNAL_WORDS) break;
            String word = sanitizeSignalWord(entry.getKey());
            if (word == null) continue;
            int value = Math.max(1, Math.min(1_000_000, entry.getValue()));
            frequencyData.append(word).append('\t').append(value).append('\n');
            Long timestamp = recentUsage.get(entry.getKey());
            if (timestamp == null) timestamp = recentUsage.get(word);
            if (timestamp != null && timestamp > 0) {
                recentData.append(word).append('\t').append(timestamp).append('\n');
            }
            written++;
        }

        preferences.edit()
                .putString(KEY_WORD_FREQUENCIES, frequencyData.toString())
                .putString(KEY_RECENT_USAGE, recentData.toString())
                .apply();
    }

    public void loadRankingSignals(Map<String, Integer> frequencies, Map<String, Long> recentUsage) {
        frequencies.clear();
        recentUsage.clear();
        parseIntSignals(preferences.getString(KEY_WORD_FREQUENCIES, ""), frequencies);
        parseLongSignals(preferences.getString(KEY_RECENT_USAGE, ""), recentUsage);
    }

    private void parseIntSignals(String data, Map<String, Integer> out) {
        if (TextUtils.isEmpty(data)) return;
        String[] lines = data.split("\n");
        for (String line : lines) {
            if (out.size() >= MAX_RANKING_SIGNAL_WORDS) break;
            int split = line.lastIndexOf('\t');
            if (split <= 0 || split >= line.length() - 1) continue;
            String word = sanitizeSignalWord(line.substring(0, split));
            if (word == null) continue;
            try {
                int value = Integer.parseInt(line.substring(split + 1));
                if (value > 0) out.put(word, Math.min(1_000_000, value));
            } catch (NumberFormatException ignored) {
                // Skip corrupt legacy entries.
            }
        }
    }

    private void parseLongSignals(String data, Map<String, Long> out) {
        if (TextUtils.isEmpty(data)) return;
        String[] lines = data.split("\n");
        long now = System.currentTimeMillis();
        for (String line : lines) {
            if (out.size() >= MAX_RANKING_SIGNAL_WORDS) break;
            int split = line.lastIndexOf('\t');
            if (split <= 0 || split >= line.length() - 1) continue;
            String word = sanitizeSignalWord(line.substring(0, split));
            if (word == null) continue;
            try {
                long value = Long.parseLong(line.substring(split + 1));
                if (value > 0 && value <= now + 86_400_000L) out.put(word, value);
            } catch (NumberFormatException ignored) {
                // Skip corrupt legacy entries.
            }
        }
    }

    private String sanitizeSignalWord(String word) {
        if (TextUtils.isEmpty(word)) return null;
        String normalized = word.trim().toLowerCase(java.util.Locale.ROOT);
        if (normalized.length() > 48 || normalized.indexOf('\t') >= 0 || normalized.indexOf('\n') >= 0) {
            return null;
        }
        return normalized;
    }

    public void saveNGramData(NGramModel ngramModel) {
        preferences.edit()
                .putString(KEY_BIGRAM_DATA, ngramModel.serializeBigramData())
                .putString(KEY_TRIGRAM_DATA, ngramModel.serializeTrigramData())
                .apply();
    }

    public void loadNGramData(NGramModel ngramModel) {
        String bigramData = preferences.getString(KEY_BIGRAM_DATA, "");
        String trigramData = preferences.getString(KEY_TRIGRAM_DATA, "");
        if (!TextUtils.isEmpty(bigramData)) ngramModel.deserializeBigramData(bigramData);
        if (!TextUtils.isEmpty(trigramData)) ngramModel.deserializeTrigramData(trigramData);
    }

    public void addUserWord(String word) {
        if (TextUtils.isEmpty(word)) return;
        Set<String> userWords = new HashSet<>(
                preferences.getStringSet(KEY_USER_WORDS, new HashSet<String>()));
        userWords.add(word.toLowerCase(java.util.Locale.ROOT).trim());
        preferences.edit().putStringSet(KEY_USER_WORDS, userWords).apply();
    }

    public void removeUserWord(String word) {
        if (TextUtils.isEmpty(word)) return;
        Set<String> userWords = new HashSet<>(
                preferences.getStringSet(KEY_USER_WORDS, new HashSet<String>()));
        userWords.remove(word.toLowerCase(java.util.Locale.ROOT).trim());
        preferences.edit().putStringSet(KEY_USER_WORDS, userWords).apply();
    }

    public void clearAllData() {
        preferences.edit()
                .remove(KEY_WORD_FREQUENCIES)
                .remove(KEY_RECENT_USAGE)
                .remove(KEY_BIGRAM_DATA)
                .remove(KEY_TRIGRAM_DATA)
                .remove(KEY_USER_WORDS)
                .remove(KEY_REJECTED_CORRECTIONS)
                .apply();
    }

    public Set<String> getRejectedCorrections() {
        return new HashSet<>(preferences.getStringSet(
                KEY_REJECTED_CORRECTIONS, new HashSet<String>()));
    }

    public void saveRejectedCorrections(Set<String> rejected) {
        preferences.edit().putStringSet(
                KEY_REJECTED_CORRECTIONS, new HashSet<>(rejected)).apply();
    }

    public int getUserWordCount() {
        return preferences.getStringSet(KEY_USER_WORDS, new HashSet<String>()).size();
    }

    public Set<String> getUserWords() {
        return new HashSet<>(
                preferences.getStringSet(KEY_USER_WORDS, new HashSet<String>()));
    }
}
