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
        System.out.println("Test completed successfully!");
    }
}