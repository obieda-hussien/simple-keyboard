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

/**
 * Simple test class to validate the learning system functionality.
 * Note: This is a basic test for demonstration - in a real project you'd use proper testing frameworks.
 */
public class LearningSystemTest {
    
    /**
     * Tests basic word trie functionality.
     */
    public static boolean testWordTrie() {
        WordTrie trie = new WordTrie();
        
        // Test insertion
        trie.insert("hello");
        trie.insert("help");
        trie.insert("hero");
        
        // Test contains
        if (!trie.contains("hello") || !trie.contains("help") || !trie.contains("hero")) {
            return false;
        }
        
        // Test non-existent word
        if (trie.contains("helicopter")) {
            return false;
        }
        
        // Test suggestions
        java.util.List<String> suggestions = trie.getSuggestions("he");
        if (suggestions.isEmpty() || !suggestions.contains("hello")) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Tests N-gram model functionality.
     */
    public static boolean testNGramModel() {
        NGramModel model = new NGramModel();
        
        // Learn some patterns
        model.learnFromSentence("how are you today");
        model.learnFromSentence("how are you doing");
        model.learnFromSentence("what are you doing");
        
        // Test predictions
        java.util.List<String> predictions = model.predictNextWords("how are");
        if (predictions.isEmpty() || !predictions.contains("you")) {
            return false;
        }
        
        // Test punctuation suggestions
        java.util.List<String> punctuation = model.suggestPunctuation("how are you");
        if (punctuation.isEmpty() || !punctuation.contains("?")) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Tests bootstrap vocabulary.
     */
    public static boolean testBootstrapVocabulary() {
        WordTrie trie = new WordTrie();
        BootstrapVocabulary.initializeVocabulary(trie);
        
        // Test that common words are available
        if (!trie.contains("the") || !trie.contains("and") || !trie.contains("for")) {
            return false;
        }
        
        // Test prefix suggestions
        java.util.List<String> suggestions = BootstrapVocabulary.getCommonWordsForPrefix("th");
        if (suggestions.isEmpty() || !suggestions.contains("the")) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Tests null context handling in LocalLearningEngine.
     */
    public static boolean testNullContextHandling() {
        // Test that LocalLearningEngine handles null context gracefully
        try {
            LocalLearningEngine.getInstance(null);
            return false; // Should not reach here
        } catch (IllegalArgumentException e) {
            // Expected behavior - null context should be rejected
            return true;
        } catch (Exception e) {
            // Unexpected exception
            return false;
        }
    }

    /**
     * Runs all tests and returns overall success.
     */
    public static boolean runAllTests() {
        System.out.println("Running Learning System Tests...");
        
        boolean trieTest = testWordTrie();
        System.out.println("Word Trie Test: " + (trieTest ? "PASS" : "FAIL"));
        
        boolean ngramTest = testNGramModel();
        System.out.println("N-Gram Model Test: " + (ngramTest ? "PASS" : "FAIL"));
        
        boolean bootstrapTest = testBootstrapVocabulary();
        System.out.println("Bootstrap Vocabulary Test: " + (bootstrapTest ? "PASS" : "FAIL"));
        
        boolean nullContextTest = testNullContextHandling();
        System.out.println("Null Context Handling Test: " + (nullContextTest ? "PASS" : "FAIL"));
        
        boolean allPassed = trieTest && ngramTest && bootstrapTest && nullContextTest;
        System.out.println("Overall Result: " + (allPassed ? "ALL TESTS PASSED" : "SOME TESTS FAILED"));
        
        return allPassed;
    }
}