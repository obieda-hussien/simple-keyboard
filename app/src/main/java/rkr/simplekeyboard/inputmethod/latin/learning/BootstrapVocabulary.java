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

import java.util.Arrays;
import java.util.List;

/**
 * Bootstrap vocabulary with common words to jump-start the suggestion system.
 * Provides initial word suggestions before user learning data is built up.
 */
public class BootstrapVocabulary {
    
    // Common English words for initial suggestions
    private static final String[] COMMON_ENGLISH_WORDS = {
        "the", "and", "for", "are", "but", "not", "you", "all", "can", "had", "her", "was", "one",
        "our", "out", "day", "get", "has", "him", "his", "how", "its", "may", "new", "now", "old",
        "see", "two", "way", "who", "boy", "did", "man", "car", "dog", "cat", "run", "big", "red",
        "home", "work", "time", "year", "back", "call", "came", "each", "good", "here", "know",
        "last", "life", "live", "look", "make", "most", "move", "must", "name", "need", "next",
        "only", "open", "over", "play", "said", "same", "seem", "show", "side", "tell", "turn",
        "used", "want", "ways", "well", "went", "were", "what", "when", "with", "word", "your",
        "about", "after", "again", "before", "being", "below", "could", "every", "first", "found",
        "great", "group", "house", "large", "place", "right", "small", "sound", "still", "such",
        "those", "three", "under", "water", "where", "which", "world", "would", "write", "young"
    };
    
    // Common Arabic words for initial suggestions (supporting Arabic users)
    private static final String[] COMMON_ARABIC_WORDS = {
        "في", "من", "على", "إلى", "هذا", "هذه", "التي", "الذي", "كان", "كانت", "يكون", "تكون",
        "لكن", "أيضا", "أيضاً", "بعد", "قبل", "عند", "حول", "حتى", "أثناء", "خلال", "كل",
        "بعض", "جميع", "معظم", "أكثر", "أقل", "أول", "آخر", "جديد", "قديم", "كبير", "صغير",
        "طويل", "قصير", "جميل", "سهل", "صعب", "مهم", "ضروري", "ممكن", "مستحيل", "صحيح",
        "خطأ", "جيد", "سيء", "أبيض", "أسود", "أحمر", "أزرق", "أخضر", "أصفر", "بيت", "منزل",
        "مدرسة", "عمل", "مكتب", "طريق", "شارع", "مدينة", "قرية", "دولة", "عالم", "شخص",
        "رجل", "امرأة", "طفل", "أسرة", "صديق", "يوم", "ليلة", "صباح", "مساء", "وقت", "ساعة"
    };
    
    // Common punctuation and special suggestions
    private static final String[] COMMON_PUNCTUATION = {
        ".", "?", "!", ",", ";", ":", "'", "\"", "(", ")", "-"
    };
    
    /**
     * Initializes the word trie with common vocabulary.
     */
    public static void initializeVocabulary(WordTrie wordTrie) {
        // Add common English words
        for (String word : COMMON_ENGLISH_WORDS) {
            wordTrie.insert(word);
        }
        
        // Add common Arabic words
        for (String word : COMMON_ARABIC_WORDS) {
            wordTrie.insert(word);
        }
    }
    
    /**
     * Initializes the N-gram model with common word patterns.
     */
    public static void initializeNGramModel(NGramModel ngramModel) {
        // Add some common English patterns
        ngramModel.learnFromSentence("I am going to");
        ngramModel.learnFromSentence("How are you");
        ngramModel.learnFromSentence("What is your");
        ngramModel.learnFromSentence("Thank you very much");
        ngramModel.learnFromSentence("Have a good day");
        ngramModel.learnFromSentence("See you later");
        ngramModel.learnFromSentence("Nice to meet you");
        ngramModel.learnFromSentence("How do you do");
        ngramModel.learnFromSentence("What time is it");
        ngramModel.learnFromSentence("Where are you from");
        
        // Add some common Arabic patterns
        ngramModel.learnFromSentence("كيف حالك اليوم");
        ngramModel.learnFromSentence("أهلا وسهلا بك");
        ngramModel.learnFromSentence("شكرا لك جزيلا");
        ngramModel.learnFromSentence("مع السلامة");
        ngramModel.learnFromSentence("يوم سعيد");
        ngramModel.learnFromSentence("إن شاء الله");
        ngramModel.learnFromSentence("الحمد لله");
        ngramModel.learnFromSentence("ما شاء الله");
    }
    
    /**
     * Gets common words for the given prefix.
     */
    public static List<String> getCommonWordsForPrefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return Arrays.asList("the", "and", "for", "you", "are");
        }
        
        prefix = prefix.toLowerCase();
        java.util.List<String> matches = new java.util.ArrayList<>();
        
        // Find all words that start with the prefix
        for (String word : COMMON_ENGLISH_WORDS) {
            if (word.toLowerCase().startsWith(prefix)) {
                matches.add(word);
                if (matches.size() >= 5) break; // Limit to 5 suggestions
            }
        }
        
        // If no matches found, provide some common suggestions based on first letter
        if (matches.isEmpty()) {
            char firstChar = prefix.charAt(0);
            switch (firstChar) {
                case 't':
                    return Arrays.asList("the", "this", "that", "they", "there");
                case 'a':
                    return Arrays.asList("and", "are", "all", "also", "about");
                case 'w':
                    return Arrays.asList("what", "when", "where", "with", "would");
                case 'h':
                    return Arrays.asList("how", "have", "has", "here", "home");
                case 'i':
                    return Arrays.asList("is", "in", "it", "if", "I");
                case 's':
                    return Arrays.asList("see", "said", "show", "such", "still");
                case 'c':
                    return Arrays.asList("can", "came", "call", "could", "car");
                case 'b':
                    return Arrays.asList("but", "boy", "back", "big", "being");
                case 'm':
                    return Arrays.asList("may", "man", "make", "most", "move");
                case 'n':
                    return Arrays.asList("not", "new", "now", "need", "next");
                default:
                    return Arrays.asList("the", "and", "for", "you", "are");
            }
        }
        
        return matches;
    }
    
    /**
     * Gets common punctuation suggestions.
     */
    public static List<String> getCommonPunctuation() {
        return Arrays.asList(COMMON_PUNCTUATION);
    }
}