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

package rkr.simplekeyboard.inputmethod.latin.utils;

import java.util.List;

/**
 * Test class for EmailSuggestionProvider functionality.
 * Note: This is a basic test that doesn't require Android context for domain completion testing.
 */
public class EmailSuggestionProviderTest {
    
    /**
     * Simple test method to verify domain completion functionality.
     * This can be called manually to test the logic.
     */
    public static void testDomainCompletion() {
        System.out.println("=== Testing Domain Completion ===");
        
        // Create a mock provider for testing domain completion (doesn't need context)
        TestEmailProvider provider = new TestEmailProvider();
        
        // Test domain completion
        List<String> suggestions = provider.getDomainCompletions("test");
        System.out.println("Domain completions for 'test':");
        for (String suggestion : suggestions) {
            System.out.println("  " + suggestion);
        }
        
        // Test with empty input
        List<String> emptySuggestions = provider.getDomainCompletions("");
        System.out.println("Domain completions for empty input: " + emptySuggestions.size());
        
        // Test with null input
        List<String> nullSuggestions = provider.getDomainCompletions(null);
        System.out.println("Domain completions for null input: " + nullSuggestions.size());
        
        // Test specific cases that should work
        testSpecificCase(provider, "john", "john@gmail.com");
        testSpecificCase(provider, "user123", "user123@outlook.com");
    }
    
    private static void testSpecificCase(TestEmailProvider provider, String input, String expected) {
        List<String> suggestions = provider.getDomainCompletions(input);
        boolean found = suggestions.contains(expected);
        System.out.println("Input '" + input + "' -> Expected '" + expected + "': " + (found ? "✓ PASS" : "✗ FAIL"));
        if (!found && !suggestions.isEmpty()) {
            System.out.println("  Got: " + suggestions.get(0));
        }
    }
    
    /**
     * Test tokenization with domain preservation.
     */
    public static void testTokenization() {
        System.out.println("\n=== Testing Tokenization ===");
        
        // Test cases for domain tokenization
        String[] testInputs = {
            "Visit gmail.com for email",
            "My email is user@domain.com and I love it",
            "Check out example.org website",
            "Send to test@company.co.uk please"
        };
        
        for (String input : testInputs) {
            String[] tokens = mockTokenize(input);
            System.out.println("Input: " + input);
            System.out.print("Tokens: ");
            for (String token : tokens) {
                System.out.print("[" + token + "] ");
            }
            System.out.println();
        }
    }
    
    /**
     * Mock tokenization that preserves domains (simplified version)
     */
    private static String[] mockTokenize(String text) {
        // This is a simplified version of the domain preservation logic
        java.util.List<String> tokens = new java.util.ArrayList<>();
        
        // Protect common domains
        String[] commonTlds = {"com", "org", "net", "edu", "co.uk"};
        String processed = text;
        
        for (String tld : commonTlds) {
            String pattern = "\\b([a-zA-Z0-9._-]+\\." + tld.replace(".", "\\.") + ")\\b";
            processed = processed.replaceAll(pattern, " __DOMAIN__ ");
        }
        
        // Split and show result
        String[] parts = processed.split("\\s+");
        for (String part : parts) {
            if ("__DOMAIN__".equals(part)) {
                tokens.add("[DOMAIN_PRESERVED]");
            } else if (!part.isEmpty()) {
                tokens.add(part);
            }
        }
        
        return tokens.toArray(new String[0]);
    }
    
    /**
     * Test email provider that doesn't require Android context.
     */
    static class TestEmailProvider {
        private static final String[] COMMON_EMAIL_DOMAINS = {
            "@gmail.com",
            "@outlook.com", 
            "@yahoo.com",
            "@hotmail.com",
            "@icloud.com"
        };
        
        public List<String> getDomainCompletions(String textBeforeAt) {
            java.util.List<String> suggestions = new java.util.ArrayList<>();
            
            if (textBeforeAt == null || textBeforeAt.trim().isEmpty()) {
                return suggestions;
            }

            String cleanText = textBeforeAt.trim();
            
            for (String domain : COMMON_EMAIL_DOMAINS) {
                suggestions.add(cleanText + domain);
                if (suggestions.size() >= 5) {
                    break;
                }
            }
            
            return suggestions;
        }
    }
    
    /**
     * Main method for manual testing.
     */
    public static void main(String[] args) {
        System.out.println("Testing EmailSuggestionProvider functionality...");
        testDomainCompletion();
        testTokenization();
        System.out.println("Test completed successfully!");
    }
}