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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import rkr.simplekeyboard.inputmethod.latin.utils.EmojiUtils;

/**
 * N-gram model for context-based word prediction.
 * Supports bigram and trigram predictions based on previous words.
 */
public class NGramModel {
    private final Map<String, Map<String, Integer>> bigramModel;
    private final Map<String, Map<String, Integer>> trigramModel;
    private static final int MAX_PREDICTIONS = 3;

    public NGramModel() {
        this.bigramModel = new HashMap<>();
        this.trigramModel = new HashMap<>();
    }

    /**
     * Learns from a sequence of words by updating bigram and trigram frequencies.
     */
    public void learnFromSequence(String[] words) {
        if (words == null || words.length < 2) {
            return;
        }

        // Normalize words
        for (int i = 0; i < words.length; i++) {
            words[i] = normalizeWord(words[i]);
        }

        // Learn bigrams
        for (int i = 0; i < words.length - 1; i++) {
            if (isValidToken(words[i]) && isValidToken(words[i + 1])) {
                addBigram(words[i], words[i + 1]);
            }
        }

        // Learn trigrams
        for (int i = 0; i < words.length - 2; i++) {
            if (isValidToken(words[i]) && isValidToken(words[i + 1]) && isValidToken(words[i + 2])) {
                addTrigram(words[i] + " " + words[i + 1], words[i + 2]);
            }
        }
    }

    /**
     * Learns from a sentence by splitting it into words and punctuation tokens.
     * Enhanced to handle punctuation as separate tokens and preserve single-letter words.
     */
    public void learnFromSentence(String sentence) {
        if (sentence == null || sentence.trim().isEmpty()) {
            return;
        }

        // Enhanced tokenization that preserves punctuation and single-letter words
        String[] tokens = tokenizeWithPunctuation(sentence);
        learnFromSequence(tokens);
    }
    
    /**
     * Enhanced tokenization that treats punctuation and emojis as separate tokens
     * and preserves single-letter words.
     */
    private String[] tokenizeWithPunctuation(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new String[0];
        }
        
        // Step 1: Extract emojis first and replace them with placeholders
        String[] emojis = EmojiUtils.extractEmojis(text);
        String processed = text;
        
        // Replace emojis with placeholders to preserve them during tokenization
        for (int i = 0; i < emojis.length; i++) {
            processed = processed.replace(emojis[i], " __EMOJI_" + i + "__ ");
        }
        
        // Step 2: Insert spaces around punctuation marks to separate them
        processed = processed
            .replaceAll("([.!?;:,])", " $1 ")  // Add spaces around punctuation
            .replaceAll("\\s+", " ")           // Normalize whitespace
            .trim();
        
        // Step 3: Split by whitespace to get tokens (words + punctuation + emoji placeholders)
        String[] tokens = processed.split("\\s+");
        
        // Step 4: Filter and restore emojis from placeholders
        List<String> validTokens = new ArrayList<>();
        for (String token : tokens) {
            token = token.trim();
            if (!token.isEmpty()) {
                // Check if it's an emoji placeholder
                if (token.startsWith("__EMOJI_") && token.endsWith("__")) {
                    try {
                        int emojiIndex = Integer.parseInt(token.substring(8, token.length() - 2));
                        if (emojiIndex >= 0 && emojiIndex < emojis.length) {
                            validTokens.add(emojis[emojiIndex]);
                        }
                    } catch (NumberFormatException e) {
                        // Invalid placeholder, skip
                    }
                } else {
                    // Keep words (including single letters) and punctuation
                    if (isValidToken(token)) {
                        validTokens.add(token);
                    }
                }
            }
        }
        
        return validTokens.toArray(new String[0]);
    }
    
    /**
     * Checks if a token is valid (word, punctuation, or emoji).
     */
    private boolean isValidToken(String token) {
        return isValidWord(token) || isPunctuation(token) || EmojiUtils.isEmoji(token);
    }
    
    /**
     * Checks if a token is punctuation.
     */
    private boolean isPunctuation(String token) {
        return token.length() == 1 && 
               (token.equals(".") || token.equals("!") || token.equals("?") || 
                token.equals(",") || token.equals(";") || token.equals(":"));
    }

    /**
     * Predicts next words based on the given context.
     * Enhanced to provide both single words and multi-word phrases.
     */
    public List<String> predictNextWords(String context) {
        if (context == null || context.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String[] contextWords = context.trim().split("\\s+");
        List<String> predictions = new ArrayList<>();

        // Try trigram prediction first (if we have at least 2 context words)
        if (contextWords.length >= 2) {
            String trigramKey = normalizeWord(contextWords[contextWords.length - 2]) + 
                               " " + normalizeWord(contextWords[contextWords.length - 1]);
            List<String> trigramPredictions = getPredictions(trigramModel, trigramKey);
            predictions.addAll(trigramPredictions);
            
            // Also try multi-word predictions from trigram model
            List<String> phrasePredictions = getPhrasePredictions(trigramKey);
            for (String phrase : phrasePredictions) {
                if (!predictions.contains(phrase) && predictions.size() < MAX_PREDICTIONS) {
                    predictions.add(phrase);
                }
            }
        }

        // Add bigram predictions
        if (contextWords.length >= 1) {
            String bigramKey = normalizeWord(contextWords[contextWords.length - 1]);
            List<String> bigramPredictions = getPredictions(bigramModel, bigramKey);
            
            // Add bigram predictions that aren't already in trigram results
            for (String prediction : bigramPredictions) {
                if (!predictions.contains(prediction)) {
                    predictions.add(prediction);
                }
            }
        }

        // Limit results
        return predictions.size() > MAX_PREDICTIONS ? 
               predictions.subList(0, MAX_PREDICTIONS) : predictions;
    }

    /**
     * Gets phrase predictions (multi-word suggestions) from trigram model.
     */
    private List<String> getPhrasePredictions(String context) {
        List<String> phrases = new ArrayList<>();
        
        // Look for common phrase patterns in the trigram model
        Map<String, Integer> nextWords = trigramModel.get(context);
        if (nextWords != null) {
            // For each predicted next word, try to find what commonly follows it
            for (String nextWord : nextWords.keySet()) {
                String extendedContext = context.split(" ")[1] + " " + nextWord;
                Map<String, Integer> followingWords = trigramModel.get(extendedContext);
                
                if (followingWords != null && !followingWords.isEmpty()) {
                    // Find the most common word that follows
                    String mostCommon = followingWords.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse(null);
                    
                    if (mostCommon != null && followingWords.get(mostCommon) > 2) {
                        phrases.add(nextWord + " " + mostCommon);
                    }
                }
            }
        }
        
        return phrases;
    }

    /**
     * Suggests appropriate punctuation based on context.
     */
    public List<String> suggestPunctuation(String context) {
        if (context == null || context.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<String> punctuationSuggestions = new ArrayList<>();
        String trimmedContext = context.trim();
        String lowerContext = trimmedContext.toLowerCase();

        // Count words to determine sentence length
        int wordCount = trimmedContext.split("\\s+").length;

        // Suggest period for longer sentences
        if (wordCount >= 5 && !trimmedContext.endsWith(".") && 
            !trimmedContext.endsWith("!") && !trimmedContext.endsWith("?")) {
            punctuationSuggestions.add(".");
        }

        // Suggest question mark for question patterns
        if (lowerContext.startsWith("what") || lowerContext.startsWith("how") ||
            lowerContext.startsWith("when") || lowerContext.startsWith("where") ||
            lowerContext.startsWith("why") || lowerContext.startsWith("who") ||
            lowerContext.startsWith("which") || lowerContext.startsWith("can") ||
            lowerContext.startsWith("could") || lowerContext.startsWith("would") ||
            lowerContext.startsWith("should") || lowerContext.startsWith("do") ||
            lowerContext.startsWith("does") || lowerContext.startsWith("did") ||
            lowerContext.startsWith("is") || lowerContext.startsWith("are") ||
            lowerContext.startsWith("was") || lowerContext.startsWith("were")) {
            if (!punctuationSuggestions.contains("?")) {
                punctuationSuggestions.add("?");
            }
        }

        // Suggest comma for longer phrases
        if (wordCount >= 3 && wordCount < 8 && !trimmedContext.endsWith(",") && 
            !trimmedContext.endsWith(".") && !trimmedContext.endsWith("!") && 
            !trimmedContext.endsWith("?")) {
            // Add comma suggestion if sentence contains connecting words
            if (lowerContext.contains(" and ") || lowerContext.contains(" but ") ||
                lowerContext.contains(" or ") || lowerContext.contains(" so ") ||
                lowerContext.contains(" because ") || lowerContext.contains(" since ")) {
                punctuationSuggestions.add(",");
            }
        }

        // Suggest exclamation for exclamatory words
        if (lowerContext.contains("wow") || lowerContext.contains("amazing") ||
            lowerContext.contains("great") || lowerContext.contains("awesome") ||
            lowerContext.contains("fantastic") || lowerContext.contains("excellent")) {
            if (!punctuationSuggestions.contains("!")) {
                punctuationSuggestions.add("!");
            }
        }

        return punctuationSuggestions;
    }

    private void addBigram(String word1, String word2) {
        bigramModel.computeIfAbsent(word1, k -> new HashMap<>())
                   .merge(word2, 1, Integer::sum);
    }

    private void addTrigram(String context, String word) {
        trigramModel.computeIfAbsent(context, k -> new HashMap<>())
                    .merge(word, 1, Integer::sum);
    }

    private List<String> getPredictions(Map<String, Map<String, Integer>> model, String key) {
        Map<String, Integer> candidates = model.get(key);
        if (candidates == null || candidates.isEmpty()) {
            return new ArrayList<>();
        }

        List<Map.Entry<String, Integer>> sortedCandidates = new ArrayList<>(candidates.entrySet());
        Collections.sort(sortedCandidates, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
                return b.getValue().compareTo(a.getValue()); // Descending order
            }
        });

        List<String> result = new ArrayList<>();
        int limit = Math.min(sortedCandidates.size(), MAX_PREDICTIONS);
        for (int i = 0; i < limit; i++) {
            result.add(sortedCandidates.get(i).getKey());
        }

        return result;
    }

    private String normalizeWord(String word) {
        if (word == null) return "";
        
        // Don't normalize emojis - keep them as-is
        if (EmojiUtils.isEmoji(word)) {
            return word;
        }
        
        return word.toLowerCase().trim()
                  .replaceAll("[^a-zA-Z0-9\\u0600-\\u06FF]", ""); // Support Arabic characters
    }

    private boolean isValidWord(String word) {
        // Updated to accept single-letter words like "a" in English or "و" in Arabic, and emojis
        if (word == null || word.trim().isEmpty()) {
            return false;
        }
        
        // Accept emojis as valid tokens
        if (EmojiUtils.isEmoji(word)) {
            return true;
        }
        
        // Accept words (including single letters)
        return word.length() >= 1;
    }

    /**
     * Serializes bigram data for persistence.
     */
    public String serializeBigramData() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Map<String, Integer>> entry : bigramModel.entrySet()) {
            String key = entry.getKey();
            for (Map.Entry<String, Integer> subEntry : entry.getValue().entrySet()) {
                sb.append(key).append("|||").append(subEntry.getKey()).append("|||").append(subEntry.getValue()).append(";;;");
            }
        }
        return sb.toString();
    }

    /**
     * Serializes trigram data for persistence.
     */
    public String serializeTrigramData() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Map<String, Integer>> entry : trigramModel.entrySet()) {
            String key = entry.getKey();
            for (Map.Entry<String, Integer> subEntry : entry.getValue().entrySet()) {
                sb.append(key).append("|||").append(subEntry.getKey()).append("|||").append(subEntry.getValue()).append(";;;");
            }
        }
        return sb.toString();
    }

    /**
     * Deserializes bigram data from persistence.
     */
    public void deserializeBigramData(String data) {
        if (data == null || data.isEmpty()) return;
        
        bigramModel.clear();
        String[] entries = data.split(";;;");
        for (String entry : entries) {
            if (entry.trim().isEmpty()) continue;
            String[] parts = entry.split("\\|\\|\\|");
            if (parts.length == 3) {
                String key = parts[0];
                String value = parts[1];
                try {
                    int frequency = Integer.parseInt(parts[2]);
                    bigramModel.computeIfAbsent(key, k -> new HashMap<>()).put(value, frequency);
                } catch (NumberFormatException e) {
                    // Skip invalid entries
                }
            }
        }
    }

    /**
     * Deserializes trigram data from persistence.
     */
    public void deserializeTrigramData(String data) {
        if (data == null || data.isEmpty()) return;
        
        trigramModel.clear();
        String[] entries = data.split(";;;");
        for (String entry : entries) {
            if (entry.trim().isEmpty()) continue;
            String[] parts = entry.split("\\|\\|\\|");
            if (parts.length == 3) {
                String key = parts[0];
                String value = parts[1];
                try {
                    int frequency = Integer.parseInt(parts[2]);
                    trigramModel.computeIfAbsent(key, k -> new HashMap<>()).put(value, frequency);
                } catch (NumberFormatException e) {
                    // Skip invalid entries
                }
            }
        }
    }
}