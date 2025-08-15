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
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import rkr.simplekeyboard.inputmethod.latin.utils.EmojiUtils;

import rkr.simplekeyboard.inputmethod.latin.utils.CalculatorUtils;
import rkr.simplekeyboard.inputmethod.latin.utils.ClipboardUtils;

/**
 * Main suggestion engine that coordinates all learning components.
 * Provides intelligent word, sentence, and punctuation suggestions.
 */
public class LocalLearningEngine {
    private static LocalLearningEngine instance;
    
    private final WordTrie wordTrie;
    private final NGramModel ngramModel;
    private final LocalStorage localStorage;
    private final Context context;
    
    private static final int MAX_SUGGESTIONS = 5;
    
    // Counters for optimized saving
    private int saveLearningCounter = 0;
    private int wordLearningCounter = 0;
    
    private LocalLearningEngine(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        this.context = context.getApplicationContext();
        this.wordTrie = new WordTrie();
        this.ngramModel = new NGramModel();
        this.localStorage = new LocalStorage(this.context);
        
        // Initialize with bootstrap vocabulary
        initializeBootstrapData();
        
        // Load existing user data
        loadLearningData();
    }
    
    /**
     * Gets the singleton instance of the learning engine.
     */
    public static synchronized LocalLearningEngine getInstance(Context context) {
        if (instance == null) {
            if (context == null) {
                throw new IllegalArgumentException("Context cannot be null for first-time initialization");
            }
            instance = new LocalLearningEngine(context);
        }
        return instance;
    }

    /**
     * Gets suggestions for the current input context.
     * Enhanced with calculator, clipboard, emoji, and number functionality.
     */
    public List<String> getSuggestions(String currentWord, String previousContext) {
        List<String> suggestions = new ArrayList<>();
        String fullText = (previousContext != null ? previousContext + " " : "") + (currentWord != null ? currentWord : "");
        
        // Check for calculator expressions first (highest priority)
        if (CalculatorUtils.isMathExpression(fullText.trim())) {
            String result = CalculatorUtils.evaluateMathExpression(fullText.trim());
            if (result != null) {
                String calcSuggestion = CalculatorUtils.createCalculationSuggestion(fullText.trim(), result);
                if (calcSuggestion != null) {
                    suggestions.add(calcSuggestion);
                }
            }
        }
        
        // Add clipboard suggestion if available and no current word
        if (TextUtils.isEmpty(currentWord) && ClipboardUtils.hasClipboardText(context)) {
            String clipboardText = ClipboardUtils.getClipboardText(context);
            String clipboardSuggestion = ClipboardUtils.createClipboardSuggestion(clipboardText);
            if (clipboardSuggestion != null && !suggestions.contains(clipboardSuggestion)) {
                suggestions.add(clipboardSuggestion);
            }
        }
        
        // Add emoji suggestions based on keywords
        if (!TextUtils.isEmpty(currentWord)) {
            List<String> emojiSuggestions = EmojiSuggestionProvider.getEmojiSuggestions(currentWord);
            for (String emoji : emojiSuggestions) {
                if (!suggestions.contains(emoji) && suggestions.size() < MAX_SUGGESTIONS) {
                    suggestions.add(emoji);
                }
            }
        }
        
        // Add contextual number suggestions
        List<String> numberSuggestions = NumberSuggestionProvider.getNumberSuggestions(fullText, currentWord);
        for (String numberSuggestion : numberSuggestions) {
            if (!suggestions.contains(numberSuggestion) && suggestions.size() < MAX_SUGGESTIONS) {
                suggestions.add(numberSuggestion);
            }
        }
        
        if (!TextUtils.isEmpty(currentWord)) {
            // Get word completions from trie (user-learned words have higher priority)
            List<String> wordSuggestions = wordTrie.getSuggestions(currentWord);
            
            // Prioritize user dictionary words
            List<String> userWordSuggestions = getUserWordSuggestions(currentWord);
            for (String suggestion : userWordSuggestions) {
                if (!suggestions.contains(suggestion) && suggestions.size() < MAX_SUGGESTIONS) {
                    suggestions.add(suggestion);
                }
            }
            
            // Add other word suggestions that aren't already in user suggestions
            for (String suggestion : wordSuggestions) {
                if (!suggestions.contains(suggestion) && suggestions.size() < MAX_SUGGESTIONS) {
                    suggestions.add(suggestion);
                }
            }
            
            // If still not enough suggestions, fall back to bootstrap vocabulary
            if (suggestions.size() < 3) {
                List<String> bootstrapSuggestions = BootstrapVocabulary.getCommonWordsForPrefix(currentWord);
                for (String suggestion : bootstrapSuggestions) {
                    if (!suggestions.contains(suggestion) && suggestions.size() < MAX_SUGGESTIONS) {
                        suggestions.add(suggestion);
                    }
                }
            }
        }
        
        if (!TextUtils.isEmpty(previousContext)) {
            // Get next word predictions from n-gram model
            List<String> contextSuggestions = ngramModel.predictNextWords(previousContext);
            
            // Add context suggestions that aren't duplicates
            for (String suggestion : contextSuggestions) {
                if (!suggestions.contains(suggestion) && suggestions.size() < MAX_SUGGESTIONS) {
                    suggestions.add(suggestion);
                }
            }
            
            // Add punctuation suggestions (enhanced with single-letter word context)
            List<String> punctuationSuggestions = ngramModel.suggestPunctuation(previousContext);
            for (String punctuation : punctuationSuggestions) {
                if (!suggestions.contains(punctuation) && suggestions.size() < MAX_SUGGESTIONS) {
                    suggestions.add(punctuation);
                }
            }
        }
        
        // If still no suggestions and no current word, provide common starters
        if (suggestions.isEmpty() && TextUtils.isEmpty(currentWord)) {
            List<String> commonStarters = BootstrapVocabulary.getCommonWordsForPrefix("");
            for (String suggestion : commonStarters) {
                if (suggestions.size() < MAX_SUGGESTIONS) {
                    suggestions.add(suggestion);
                }
            }
        }
        
        return suggestions.size() > MAX_SUGGESTIONS ? 
               suggestions.subList(0, MAX_SUGGESTIONS) : suggestions;
    }

    /**
     * Learns from user input to improve future suggestions.
     */
    public void learnFromInput(String text) {
        if (TextUtils.isEmpty(text)) return;
        
        // Learn individual words
        String[] words = extractWords(text);
        for (String word : words) {
            if (isValidWord(word)) {
                wordTrie.insert(word);
                localStorage.addUserWord(word);
            }
        }
        
        // Learn from sentence context - this is critical for n-gram learning
        ngramModel.learnFromSentence(text);
        
        // Force save more frequently for learning data (every 5th call instead of 10%)
        saveLearningCounter++;
        if (saveLearningCounter >= 5) {
            saveLearningData();
            saveLearningCounter = 0;
        }
    }

    /**
     * Learns from a completed word.
     */
    public void learnWord(String word) {
        if (isValidWord(word)) {
            wordTrie.insert(word);
            localStorage.addUserWord(word);
            
            // Force save every 10 words learned
            wordLearningCounter++;
            if (wordLearningCounter >= 10) {
                saveLearningData();
                wordLearningCounter = 0;
            }
        }
    }

    /**
     * Learns from a completed sentence.
     */
    public void learnSentence(String sentence) {
        if (!TextUtils.isEmpty(sentence)) {
            ngramModel.learnFromSentence(sentence);
            
            // Also learn individual words
            String[] words = extractWords(sentence);
            for (String word : words) {
                learnWord(word);
            }
            
            // Force save for sentence learning as it's valuable data
            saveLearningData();
        }
    }

    /**
     * Adds a word to the user dictionary for high-priority suggestions.
     */
    public void addToUserDictionary(String word) {
        if (isValidWord(word)) {
            localStorage.addUserWord(word);
            wordTrie.insert(word);
            // Give user words extra frequency boost
            for (int i = 0; i < 5; i++) {
                wordTrie.insert(word);
            }
            // Save immediately for user dictionary changes
            saveLearningData();
        }
    }

    /**
     * Checks if a word is in the user dictionary.
     */
    public boolean isInUserDictionary(String word) {
        if (TextUtils.isEmpty(word)) return false;
        return localStorage.getUserWords().contains(word.toLowerCase().trim());
    }

    /**
     * Gets suggestions from user dictionary words that match the prefix.
     */
    private List<String> getUserWordSuggestions(String prefix) {
        List<String> userSuggestions = new ArrayList<>();
        Set<String> userWords = localStorage.getUserWords();
        
        String lowerPrefix = prefix.toLowerCase();
        for (String userWord : userWords) {
            if (userWord.toLowerCase().startsWith(lowerPrefix)) {
                userSuggestions.add(userWord);
                if (userSuggestions.size() >= 3) break; // Limit user suggestions
            }
        }
        
        return userSuggestions;
    }

    /**
     * Removes a word from suggestions.
     */
    public void removeWord(String word) {
        localStorage.removeUserWord(word);
        // Note: For simplicity, we don't remove from trie as it would require
        // rebuilding the entire structure
    }

    /**
     * Gets statistics about the learning system.
     */
    public LearningStats getStats() {
        return new LearningStats(localStorage.getUserWordCount());
    }

    /**
     * Clears all learning data.
     */
    public void clearAllData() {
        localStorage.clearAllData();
        // Reinitialize components
        // wordTrie and ngramModel would need to be reset
    }

    private void loadLearningData() {
        localStorage.loadWordFrequencies(wordTrie);
        localStorage.loadNGramData(ngramModel);
    }

    private void saveLearningData() {
        localStorage.saveWordFrequencies(wordTrie);
        localStorage.saveNGramData(ngramModel);
    }
    
    private void initializeBootstrapData() {
        BootstrapVocabulary.initializeVocabulary(wordTrie);
        BootstrapVocabulary.initializeNGramModel(ngramModel);
    }

    private String[] extractWords(String text) {
        if (TextUtils.isEmpty(text)) return new String[0];
        
        // Enhanced tokenization that preserves single-letter words, handles punctuation and emojis
        String[] emojis = EmojiUtils.extractEmojis(text);
        String processed = text.trim();
        
        // Replace emojis with placeholders to preserve them during tokenization
        for (int i = 0; i < emojis.length; i++) {
            processed = processed.replace(emojis[i], " __EMOJI_" + i + "__ ");
        }
        
        processed = processed
                  .replaceAll("([.!?;:,])", " $1 ")  // Add spaces around punctuation
                  .replaceAll("\\s+", " ")           // Normalize whitespace
                  .trim();
        
        String[] tokens = processed.split("\\s+");
        
        // Filter to return valid words and emojis (not punctuation)
        List<String> words = new ArrayList<>();
        for (String token : tokens) {
            token = token.trim();
            
            // Check if it's an emoji placeholder
            if (token.startsWith("__EMOJI_") && token.endsWith("__")) {
                try {
                    int emojiIndex = Integer.parseInt(token.substring(8, token.length() - 2));
                    if (emojiIndex >= 0 && emojiIndex < emojis.length) {
                        words.add(emojis[emojiIndex]);
                    }
                } catch (NumberFormatException e) {
                    // Invalid placeholder, skip
                }
            } else if (isValidWord(token)) {
                words.add(token);
            }
        }
        
        return words.toArray(new String[0]);
    }

    private boolean isValidWord(String word) {
        if (TextUtils.isEmpty(word)) return false;
        
        word = word.trim();
        
        // Accept emojis as valid tokens
        if (EmojiUtils.isEmoji(word)) {
            return true;
        }
        
        // Updated to accept single-letter words like "a" in English or "و" in Arabic
        return word.length() >= 1 && 
               word.matches(".*[a-zA-Z\\u0600-\\u06FF].*"); // Contains at least one letter (Latin or Arabic)
    }

    /**
     * Gets spelling corrections and word completions for a given word.
     * This is used for cursor-aware suggestions when the user places their cursor on an existing word.
     * @param word The word to provide corrections and completions for
     * @return List of suggested corrections and completions
     */
    public List<String> getCorrectionsAndCompletions(String word) {
        List<String> suggestions = new ArrayList<>();
        
        if (TextUtils.isEmpty(word)) {
            return suggestions;
        }
        
        String cleanWord = word.trim().toLowerCase();
        
        // 1. First, add completions (words that start with the given word)
        List<String> completions = getCompletions(cleanWord);
        for (String completion : completions) {
            if (!suggestions.contains(completion) && suggestions.size() < MAX_SUGGESTIONS) {
                suggestions.add(completion);
            }
        }
        
        // 2. Add spelling corrections (similar words)
        List<String> corrections = getSpellingCorrections(cleanWord);
        for (String correction : corrections) {
            if (!suggestions.contains(correction) && suggestions.size() < MAX_SUGGESTIONS) {
                suggestions.add(correction);
            }
        }
        
        return suggestions;
    }
    
    /**
     * Gets word completions for a partial word.
     */
    private List<String> getCompletions(String partialWord) {
        List<String> completions = new ArrayList<>();
        
        if (TextUtils.isEmpty(partialWord)) {
            return completions;
        }
        
        // Search in user dictionary first (higher priority)
        Set<String> userWords = localStorage.getUserWords();
        for (String userWord : userWords) {
            if (userWord.toLowerCase().startsWith(partialWord) && 
                !userWord.toLowerCase().equals(partialWord)) {
                completions.add(userWord);
                if (completions.size() >= 3) break; // Limit user completions
            }
        }
        
        // Search in bootstrap vocabulary for additional completions
        List<String> bootstrapCompletions = BootstrapVocabulary.getCommonWordsForPrefix(partialWord);
        for (String completion : bootstrapCompletions) {
            if (!completions.contains(completion) && completions.size() < MAX_SUGGESTIONS) {
                completions.add(completion);
            }
        }
        
        return completions;
    }
    
    /**
     * Gets spelling corrections for a potentially misspelled word.
     */
    private List<String> getSpellingCorrections(String word) {
        List<String> corrections = new ArrayList<>();
        
        if (TextUtils.isEmpty(word) || word.length() < 2) {
            return corrections;
        }
        
        // Check user dictionary first
        Set<String> userWords = localStorage.getUserWords();
        for (String userWord : userWords) {
            if (isSpellingCandidate(word, userWord)) {
                corrections.add(userWord);
                if (corrections.size() >= 2) break; // Limit user corrections
            }
        }
        
        // Check bootstrap vocabulary for additional corrections
        List<String> commonWords = BootstrapVocabulary.getCommonWordsForPrefix(word.substring(0, Math.min(2, word.length())));
        for (String commonWord : commonWords) {
            if (isSpellingCandidate(word, commonWord) && !corrections.contains(commonWord)) {
                corrections.add(commonWord);
                if (corrections.size() >= MAX_SUGGESTIONS) break;
            }
        }
        
        return corrections;
    }
    
    /**
     * Determines if a word is a good spelling correction candidate.
     * Uses simple heuristics like edit distance and common prefixes.
     */
    private boolean isSpellingCandidate(String original, String candidate) {
        if (TextUtils.isEmpty(original) || TextUtils.isEmpty(candidate)) {
            return false;
        }
        
        String origLower = original.toLowerCase();
        String candLower = candidate.toLowerCase();
        
        // Skip if identical
        if (origLower.equals(candLower)) {
            return false;
        }
        
        // Skip if length difference is too large
        int lengthDiff = Math.abs(origLower.length() - candLower.length());
        if (lengthDiff > 2) {
            return false;
        }
        
        // Simple edit distance check (Levenshtein distance approximation)
        int editDistance = calculateSimpleEditDistance(origLower, candLower);
        return editDistance <= 2; // Allow up to 2 character changes
    }
    
    /**
     * Simple edit distance calculation (optimized for short words)
     */
    private int calculateSimpleEditDistance(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();
        
        // Use a simplified approach for performance
        if (len1 == 0) return len2;
        if (len2 == 0) return len1;
        
        int[][] dp = new int[2][len2 + 1];
        
        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= len1; i++) {
            dp[i % 2][0] = i;
            for (int j = 1; j <= len2; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i % 2][j] = Math.min(Math.min(
                    dp[(i - 1) % 2][j] + 1,      // deletion
                    dp[i % 2][j - 1] + 1),       // insertion
                    dp[(i - 1) % 2][j - 1] + cost); // substitution
            }
        }
        
        return dp[len1 % 2][len2];
    }

    /**
     * Statistics about the learning system.
     */
    public static class LearningStats {
        public final int totalWords;
        
        public LearningStats(int totalWords) {
            this.totalWords = totalWords;
        }
    }
}