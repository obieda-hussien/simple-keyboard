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

/**
 * Test class for calculator and emoji functionality.
 */
public class KeyboardFeaturesTest {
    
    /**
     * Tests calculator functionality.
     */
    public static boolean testCalculator() {
        // Test basic operations (existing tests)
        String result1 = CalculatorUtils.evaluateMathExpression("125 * 4");
        if (!"500".equals(result1)) {
            System.out.println("Calculator test failed: 125 * 4 = " + result1 + " (expected 500)");
            return false;
        }
        
        String result2 = CalculatorUtils.evaluateMathExpression("10 + 5 =");
        if (!"15".equals(result2)) {
            System.out.println("Calculator test failed: 10 + 5 = " + result2 + " (expected 15)");
            return false;
        }
        
        String result3 = CalculatorUtils.evaluateMathExpression("20 / 4");
        if (!"5".equals(result3)) {
            System.out.println("Calculator test failed: 20 / 4 = " + result3 + " (expected 5)");
            return false;
        }
        
        String result4 = CalculatorUtils.evaluateMathExpression("7.5 + 2.5");
        if (!"10".equals(result4)) {  // Since 7.5 + 2.5 = 10 (whole number)
            System.out.println("Calculator test failed: 7.5 + 2.5 = " + result4 + " (expected 10)");
            return false;
        }
        
        // Test new advanced functionality
        
        // Test PEMDAS/BODMAS order of operations
        String result5 = CalculatorUtils.evaluateMathExpression("5 + 7 * 2");
        if (!"19".equals(result5)) {
            System.out.println("Calculator test failed: 5 + 7 * 2 = " + result5 + " (expected 19)");
            return false;
        }
        
        // Test multi-step calculations
        String result6 = CalculatorUtils.evaluateMathExpression("100 - 25 + 50");
        if (!"125".equals(result6)) {
            System.out.println("Calculator test failed: 100 - 25 + 50 = " + result6 + " (expected 125)");
            return false;
        }
        
        // Test parentheses
        String result7 = CalculatorUtils.evaluateMathExpression("150 + (5 * 22.5)");
        if (!"262.5".equals(result7)) {  // 150 + 112.5 = 262.5 (not 262.50)
            System.out.println("Calculator test failed: 150 + (5 * 22.5) = " + result7 + " (expected 262.5)");
            return false;
        }
        
        // Test complex expression with parentheses
        String result8 = CalculatorUtils.evaluateMathExpression("(10 + 5) * 2 - 3");
        if (!"27".equals(result8)) {
            System.out.println("Calculator test failed: (10 + 5) * 2 - 3 = " + result8 + " (expected 27)");
            return false;
        }
        
        // Test invalid expressions
        if (CalculatorUtils.isMathExpression("hello world")) {
            System.out.println("Calculator test failed: 'hello world' should not be a math expression");
            return false;
        }
        
        if (CalculatorUtils.evaluateMathExpression("5 / 0") != null) {
            System.out.println("Calculator test failed: division by zero should return null");
            return false;
        }
        
        return true;
    }
    
    /**
     * Tests emoji search functionality.
     */
    public static boolean testEmojiSearch() {
        // Test searching for car emoji
        java.util.List<rkr.simplekeyboard.inputmethod.emoji.EmojiItem> carEmojis = 
            rkr.simplekeyboard.inputmethod.emoji.EmojiData.searchEmojis("car");
        
        boolean foundCarEmoji = false;
        for (rkr.simplekeyboard.inputmethod.emoji.EmojiItem emoji : carEmojis) {
            if ("🚗".equals(emoji.getEmoji())) {
                foundCarEmoji = true;
                break;
            }
        }
        
        if (!foundCarEmoji) {
            System.out.println("Emoji test failed: car search should include 🚗");
            return false;
        }
        
        // Test category retrieval
        java.util.List<rkr.simplekeyboard.inputmethod.emoji.EmojiItem> smileysEmojis = 
            rkr.simplekeyboard.inputmethod.emoji.EmojiData.getEmojisByCategory(
                rkr.simplekeyboard.inputmethod.emoji.EmojiCategory.SMILEYS);
        
        if (smileysEmojis.isEmpty()) {
            System.out.println("Emoji test failed: smileys category should not be empty");
            return false;
        }
        
        return true;
    }
    
    /**
     * Runs all feature tests.
     */
    public static boolean runAllTests() {
        System.out.println("Running Keyboard Features Tests...");
        
        boolean calculatorTest = testCalculator();
        System.out.println("Calculator Test: " + (calculatorTest ? "PASS" : "FAIL"));
        
        boolean emojiTest = testEmojiSearch();
        System.out.println("Emoji Search Test: " + (emojiTest ? "PASS" : "FAIL"));
        
        boolean allPassed = calculatorTest && emojiTest;
        System.out.println("Overall Result: " + (allPassed ? "ALL TESTS PASSED" : "SOME TESTS FAILED"));
        
        return allPassed;
    }
    
    /**
     * Main method to run tests.
     */
    public static void main(String[] args) {
        runAllTests();
    }
}