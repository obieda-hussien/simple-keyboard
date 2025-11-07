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

/**
 * Advanced suggestion ranker that scores and ranks suggestions based on multiple factors.
 * Implements Gboard-like intelligent ranking with:
 * - Frequency-based scoring
 * - Context awareness
 * - Typo tolerance (edit distance)
 * - Prefix matching priority
 * - Recent usage boosting
 */
public class SuggestionRanker {
    
    // Scoring weights (tuned for optimal user experience)
    private static final double WEIGHT_EXACT_MATCH = 100.0;
    private static final double WEIGHT_PREFIX_MATCH = 50.0;
    private static final double WEIGHT_TYPO_TOLERANCE = 30.0;
    private static final double WEIGHT_FREQUENCY = 40.0;
    private static final double WEIGHT_CONTEXT = 35.0;
    private static final double WEIGHT_RECENT_USAGE = 25.0;
    private static final double WEIGHT_LENGTH_PREFERENCE = 10.0;
    
    // Typo tolerance threshold (Levenshtein distance)
    private static final int MAX_EDIT_DISTANCE = 2;
    
    /**
     * Scored suggestion with ranking metadata.
     */
    public static class ScoredSuggestion {
        public final String text;
        public double score;
        public final String source; // "exact", "prefix", "typo", "context", "prediction"
        
        public ScoredSuggestion(String text, double score, String source) {
            this.text = text;
            this.score = score;
            this.source = source;
        }
    }
    
    /**
     * Ranks suggestions based on multiple scoring factors.
     */
    public static List<String> rankSuggestions(
            List<String> candidates,
            String currentWord,
            String previousContext,
            Map<String, Integer> wordFrequency,
            Map<String, Long> recentUsage) {
        
        if (candidates == null || candidates.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<ScoredSuggestion> scoredSuggestions = new ArrayList<>();
        
        for (String candidate : candidates) {
            double score = calculateScore(
                candidate,
                currentWord,
                previousContext,
                wordFrequency,
                recentUsage
            );
            
            String source = determineSource(candidate, currentWord);
            scoredSuggestions.add(new ScoredSuggestion(candidate, score, source));
        }
        
        // Sort by score (highest first)
        Collections.sort(scoredSuggestions, new Comparator<ScoredSuggestion>() {
            @Override
            public int compare(ScoredSuggestion a, ScoredSuggestion b) {
                return Double.compare(b.score, a.score);
            }
        });
        
        // Extract sorted suggestions
        List<String> rankedSuggestions = new ArrayList<>();
        for (ScoredSuggestion scored : scoredSuggestions) {
            rankedSuggestions.add(scored.text);
        }
        
        return rankedSuggestions;
    }
    
    /**
     * Calculates comprehensive score for a suggestion.
     */
    private static double calculateScore(
            String suggestion,
            String currentWord,
            String previousContext,
            Map<String, Integer> wordFrequency,
            Map<String, Long> recentUsage) {
        
        double score = 0.0;
        
        if (currentWord != null && !currentWord.isEmpty()) {
            String lowerSuggestion = suggestion.toLowerCase();
            String lowerCurrent = currentWord.toLowerCase();
            
            // Exact match bonus
            if (lowerSuggestion.equals(lowerCurrent)) {
                score += WEIGHT_EXACT_MATCH;
            }
            
            // Prefix match bonus
            else if (lowerSuggestion.startsWith(lowerCurrent)) {
                score += WEIGHT_PREFIX_MATCH;
                // Bonus for closer match (shorter completion needed)
                double completionRatio = (double) lowerCurrent.length() / lowerSuggestion.length();
                score += WEIGHT_PREFIX_MATCH * completionRatio * 0.5;
            }
            
            // Typo tolerance (edit distance)
            else {
                int editDistance = calculateLevenshteinDistance(lowerSuggestion, lowerCurrent);
                if (editDistance <= MAX_EDIT_DISTANCE) {
                    double typoScore = WEIGHT_TYPO_TOLERANCE * (1.0 - (double) editDistance / MAX_EDIT_DISTANCE);
                    score += typoScore;
                }
            }
        }
        
        // Frequency-based scoring
        if (wordFrequency != null && wordFrequency.containsKey(suggestion)) {
            int frequency = wordFrequency.get(suggestion);
            // Logarithmic scaling to prevent over-dominance
            score += WEIGHT_FREQUENCY * Math.log(frequency + 1);
        }
        
        // Context-aware scoring
        if (previousContext != null && !previousContext.isEmpty()) {
            // Bonus for suggestions that commonly follow the previous word
            score += WEIGHT_CONTEXT * calculateContextScore(suggestion, previousContext);
        }
        
        // Recent usage boost
        if (recentUsage != null && recentUsage.containsKey(suggestion)) {
            long lastUsedTime = recentUsage.get(suggestion);
            long currentTime = System.currentTimeMillis();
            long timeDiff = currentTime - lastUsedTime;
            
            // Decay over time (30 days = full decay)
            double recencyFactor = Math.exp(-timeDiff / (30.0 * 24 * 60 * 60 * 1000));
            score += WEIGHT_RECENT_USAGE * recencyFactor;
        }
        
        // Length preference (slight bonus for common word lengths 3-7 characters)
        int length = suggestion.length();
        if (length >= 3 && length <= 7) {
            score += WEIGHT_LENGTH_PREFERENCE;
        }
        
        return score;
    }
    
    /**
     * Calculates context score based on how well the suggestion fits with previous word.
     */
    private static double calculateContextScore(String suggestion, String previousContext) {
        // Simple heuristic: check for common word pairs
        String[] commonFollowers = getCommonFollowers(previousContext);
        for (int i = 0; i < commonFollowers.length; i++) {
            if (suggestion.equalsIgnoreCase(commonFollowers[i])) {
                // Higher score for earlier matches
                return 1.0 - (i * 0.1);
            }
        }
        return 0.0;
    }
    
    /**
     * Gets common words that typically follow the given word.
     */
    private static String[] getCommonFollowers(String word) {
        String lowerWord = word.toLowerCase().trim();
        
        // Common English patterns
        if (lowerWord.equals("i")) return new String[]{"am", "have", "will", "can", "don't"};
        if (lowerWord.equals("you")) return new String[]{"are", "have", "can", "will", "don't"};
        if (lowerWord.equals("the")) return new String[]{"best", "first", "last", "most", "only"};
        if (lowerWord.equals("to")) return new String[]{"be", "do", "go", "get", "see"};
        if (lowerWord.equals("have")) return new String[]{"a", "to", "been", "had", "not"};
        if (lowerWord.equals("will")) return new String[]{"be", "have", "not", "go", "do"};
        if (lowerWord.equals("can")) return new String[]{"be", "you", "I", "we", "not"};
        
        // Common Arabic patterns
        if (lowerWord.equals("أنا")) return new String[]{"أريد", "أحب", "لا", "سوف", "كنت"};
        if (lowerWord.equals("هذا")) return new String[]{"هو", "ما", "كان", "يعني", "جيد"};
        if (lowerWord.equals("في")) return new String[]{"البيت", "المدرسة", "الصباح", "المساء", "الوقت"};
        if (lowerWord.equals("من")) return new String[]{"فضلك", "هنا", "هناك", "الآن", "البداية"};
        
        return new String[]{};
    }
    
    /**
     * Determines the source/type of suggestion.
     */
    private static String determineSource(String suggestion, String currentWord) {
        if (currentWord == null || currentWord.isEmpty()) {
            return "prediction";
        }
        
        String lowerSuggestion = suggestion.toLowerCase();
        String lowerCurrent = currentWord.toLowerCase();
        
        if (lowerSuggestion.equals(lowerCurrent)) {
            return "exact";
        } else if (lowerSuggestion.startsWith(lowerCurrent)) {
            return "prefix";
        } else {
            int distance = calculateLevenshteinDistance(lowerSuggestion, lowerCurrent);
            if (distance <= MAX_EDIT_DISTANCE) {
                return "typo";
            }
        }
        
        return "context";
    }
    
    /**
     * Calculates Levenshtein distance (edit distance) between two strings.
     * Used for typo tolerance.
     */
    public static int calculateLevenshteinDistance(String s1, String s2) {
        if (s1 == null || s2 == null) return Integer.MAX_VALUE;
        if (s1.isEmpty()) return s2.length();
        if (s2.isEmpty()) return s1.length();
        
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                    Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                );
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
    
    /**
     * Generates typo-tolerant suggestions by finding words within edit distance.
     * Enhanced with specialized typo detection for common mistakes.
     */
    public static List<String> generateTypoSuggestions(
            String currentWord,
            List<String> dictionary,
            int maxDistance) {
        
        List<String> typoSuggestions = new ArrayList<>();
        
        if (currentWord == null || currentWord.isEmpty() || dictionary == null) {
            return typoSuggestions;
        }
        
        // First, try quick specialized typo corrections
        typoSuggestions.addAll(generateTranspositionSuggestions(currentWord, dictionary));
        typoSuggestions.addAll(generateMissingCharSuggestions(currentWord, dictionary));
        typoSuggestions.addAll(generateExtraCharSuggestions(currentWord, dictionary));
        typoSuggestions.addAll(generateKeyboardProximitySuggestions(currentWord, dictionary));
        
        // Then add general edit distance matches that aren't already included
        for (String word : dictionary) {
            if (!typoSuggestions.contains(word)) {
                int distance = calculateLevenshteinDistance(
                    currentWord.toLowerCase(),
                    word.toLowerCase()
                );
                
                if (distance > 0 && distance <= maxDistance) {
                    typoSuggestions.add(word);
                }
            }
        }
        
        return typoSuggestions;
    }
    
    /**
     * Detects transposition errors (adjacent characters swapped).
     * Example: "teh" → "the"
     */
    private static List<String> generateTranspositionSuggestions(String word, List<String> dictionary) {
        List<String> suggestions = new ArrayList<>();
        if (word == null || word.length() < 2) return suggestions;
        
        String lowerWord = word.toLowerCase();
        
        // Try swapping each pair of adjacent characters
        for (int i = 0; i < lowerWord.length() - 1; i++) {
            String transposed = lowerWord.substring(0, i) +
                               lowerWord.charAt(i + 1) +
                               lowerWord.charAt(i) +
                               lowerWord.substring(i + 2);
            
            // Check if transposed version matches dictionary
            for (String dictWord : dictionary) {
                if (dictWord.toLowerCase().equals(transposed)) {
                    suggestions.add(dictWord);
                }
            }
        }
        
        return suggestions;
    }
    
    /**
     * Detects missing character errors.
     * Example: "wrld" → "world"
     */
    private static List<String> generateMissingCharSuggestions(String word, List<String> dictionary) {
        List<String> suggestions = new ArrayList<>();
        if (word == null || word.isEmpty()) return suggestions;
        
        String lowerWord = word.toLowerCase();
        
        // For each dictionary word, check if removing one char makes it match input
        for (String dictWord : dictionary) {
            String lowerDict = dictWord.toLowerCase();
            if (lowerDict.length() == lowerWord.length() + 1) {
                // Try removing each character from dictionary word
                for (int i = 0; i < lowerDict.length(); i++) {
                    String withoutChar = lowerDict.substring(0, i) + lowerDict.substring(i + 1);
                    if (withoutChar.equals(lowerWord)) {
                        suggestions.add(dictWord);
                        break;
                    }
                }
            }
        }
        
        return suggestions;
    }
    
    /**
     * Detects extra character errors.
     * Example: "helllo" → "hello"
     */
    private static List<String> generateExtraCharSuggestions(String word, List<String> dictionary) {
        List<String> suggestions = new ArrayList<>();
        if (word == null || word.length() < 2) return suggestions;
        
        String lowerWord = word.toLowerCase();
        
        // Try removing each character from input
        for (int i = 0; i < lowerWord.length(); i++) {
            String withoutChar = lowerWord.substring(0, i) + lowerWord.substring(i + 1);
            
            for (String dictWord : dictionary) {
                if (dictWord.toLowerCase().equals(withoutChar)) {
                    suggestions.add(dictWord);
                }
            }
        }
        
        return suggestions;
    }
    
    /**
     * Detects keyboard proximity errors (hitting adjacent key).
     * Example: "n" instead of "m" (adjacent on QWERTY keyboard)
     */
    private static List<String> generateKeyboardProximitySuggestions(String word, List<String> dictionary) {
        List<String> suggestions = new ArrayList<>();
        if (word == null || word.isEmpty()) return suggestions;
        
        String lowerWord = word.toLowerCase();
        
        // QWERTY keyboard layout proximity map
        Map<Character, String> proximityMap = getKeyboardProximityMap();
        
        // Try replacing each character with nearby keys
        for (int i = 0; i < lowerWord.length(); i++) {
            char c = lowerWord.charAt(i);
            String nearby = proximityMap.get(c);
            
            if (nearby != null) {
                for (int j = 0; j < nearby.length(); j++) {
                    String variant = lowerWord.substring(0, i) +
                                   nearby.charAt(j) +
                                   lowerWord.substring(i + 1);
                    
                    for (String dictWord : dictionary) {
                        if (dictWord.toLowerCase().equals(variant)) {
                            suggestions.add(dictWord);
                        }
                    }
                }
            }
        }
        
        return suggestions;
    }
    
    /**
     * Returns keyboard proximity map for QWERTY layout.
     * Each key maps to its adjacent keys.
     */
    private static Map<Character, String> getKeyboardProximityMap() {
        Map<Character, String> map = new HashMap<>();
        
        // QWERTY keyboard layout (English)
        map.put('q', "wa");
        map.put('w', "qeas");
        map.put('e', "wrsd");
        map.put('r', "etdf");
        map.put('t', "ryfg");
        map.put('y', "tugh");
        map.put('u', "yihj");
        map.put('i', "uojk");
        map.put('o', "ipkl");
        map.put('p', "ol");
        
        map.put('a', "qwsz");
        map.put('s', "awedxz");
        map.put('d', "serfcx");
        map.put('f', "drtgvc");
        map.put('g', "ftyhbv");
        map.put('h', "gyujnb");
        map.put('j', "huikmn");
        map.put('k', "jiolm");
        map.put('l', "kop");
        
        map.put('z', "asx");
        map.put('x', "zsdc");
        map.put('c', "xdfv");
        map.put('v', "cfgb");
        map.put('b', "vghn");
        map.put('n', "bhjm");
        map.put('m', "njk");
        
        // Arabic keyboard layout (common positions)
        map.put('ا', "سش");
        map.put('ب', "لان");
        map.put('ت', "نفب");
        map.put('ث', "قفت");
        map.put('ج', "حخ");
        map.put('ح', "جخع");
        map.put('خ', "حجع");
        map.put('د', "ذجس");
        map.put('ذ', "دشس");
        map.put('ر', "زو");
        map.put('ز', "رة");
        map.put('س', "شاد");
        map.put('ش', "سذا");
        map.put('ص', "ضق");
        map.put('ض', "صف");
        map.put('ط', "ظك");
        map.put('ظ', "طم");
        map.put('ع', "حخغ");
        map.put('غ', "عف");
        map.put('ف', "قثت");
        map.put('ق', "فصث");
        map.put('ك', "مطل");
        map.put('ل', "كاي");
        map.put('م', "نظك");
        map.put('ن', "تبم");
        map.put('ه', "ةو");
        map.put('و', "هرز");
        map.put('ي', "لءئ");
        
        return map;
    }
}
