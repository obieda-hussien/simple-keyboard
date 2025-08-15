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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

/**
 * Utility class for detecting and evaluating mathematical expressions.
 * Supports both Latin and Arabic-Indic numerals.
 */
public class CalculatorUtils {
    
    // Pattern to find mathematical expressions in text (supports Arabic-Indic numerals)
    private static final Pattern MATH_EXPRESSION_PATTERN = Pattern.compile(
        "([\\d٠-٩+\\-*/()\\s.]+[+\\-*/][\\d٠-٩+\\-*/()\\s.]*)"
    );
    
    // Pattern to validate that expression contains math operations
    private static final Pattern CONTAINS_MATH_OPS = Pattern.compile(
        ".*[+\\-*/].*"
    );
    
    // Pattern to ensure expression has valid mathematical characters (including Arabic-Indic)
    private static final Pattern VALID_MATH_EXPRESSION = Pattern.compile(
        "^[\\d٠-٩+\\-*/()\\s.]+$"
    );
    
    /**
     * Converts Arabic-Indic numerals to Latin numerals.
     */
    private static String convertArabicIndicToLatin(String text) {
        if (text == null) return null;
        
        return text.replace('٠', '0')
                   .replace('١', '1')
                   .replace('٢', '2')
                   .replace('٣', '3')
                   .replace('٤', '4')
                   .replace('٥', '5')
                   .replace('٦', '6')
                   .replace('٧', '7')
                   .replace('٨', '8')
                   .replace('٩', '9');
    }
    
    /**
     * Extracts the last valid mathematical expression from text.
     * This handles cases where expressions are embedded in sentences.
     */
    private static String extractLastMathExpression(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        
        // Find all potential mathematical expressions
        Matcher matcher = MATH_EXPRESSION_PATTERN.matcher(text);
        String lastExpression = null;
        
        while (matcher.find()) {
            String candidate = matcher.group(1).trim();
            
            // Remove trailing equals sign if present
            if (candidate.endsWith("=")) {
                candidate = candidate.substring(0, candidate.length() - 1).trim();
            }
            
            // Validate the candidate
            if (isValidMathExpression(candidate)) {
                lastExpression = candidate;
            }
        }
        
        return lastExpression;
    }
    
    /**
     * Validates if a string is a valid mathematical expression.
     */
    private static boolean isValidMathExpression(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = expression.trim();
        
        // Must contain mathematical operations
        if (!CONTAINS_MATH_OPS.matcher(trimmed).matches()) {
            return false;
        }
        
        // Must only contain valid mathematical characters (including Arabic-Indic)
        if (!VALID_MATH_EXPRESSION.matcher(trimmed).matches()) {
            return false;
        }
        
        // Must have at least one digit (Latin or Arabic-Indic)
        if (!trimmed.matches(".*[\\d٠-٩].*")) {
            return false;
        }
        
        // Convert Arabic-Indic numerals and try to parse with exp4j
        String latinExpression = convertArabicIndicToLatin(trimmed);
        try {
            Expression exp = new ExpressionBuilder(latinExpression).build();
            exp.evaluate(); // This will throw exception if invalid
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Checks if a string contains a mathematical expression that can be evaluated.
     * Now supports Arabic-Indic numerals and in-sentence expression detection.
     */
    public static boolean isMathExpression(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        
        // Try to extract the last valid mathematical expression from the text
        String expression = extractLastMathExpression(text);
        return expression != null;
    }
    
    /**
     * Evaluates a mathematical expression and returns the result.
     * Returns null if the expression cannot be evaluated.
     * Now supports Arabic-Indic numerals and in-sentence expression detection.
     */
    public static String evaluateMathExpression(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        
        // Extract the last valid mathematical expression from the text
        String expression = extractLastMathExpression(text);
        if (expression == null) {
            return null;
        }
        
        // Convert Arabic-Indic numerals to Latin numerals
        String latinExpression = convertArabicIndicToLatin(expression);
        
        try {
            Expression exp = new ExpressionBuilder(latinExpression).build();
            double result = exp.evaluate();
            
            // Check for invalid results
            if (Double.isNaN(result) || Double.isInfinite(result)) {
                return null;
            }
            
            // Format result: show integer if it's a whole number
            if (result == Math.floor(result) && !Double.isInfinite(result)) {
                return String.valueOf((long) result);
            } else {
                // Format with 2 decimal places, but remove trailing zeros unless it's a .00 case
                String formatted = String.format("%.2f", result);
                if (formatted.endsWith("0") && !formatted.endsWith("00")) {
                    formatted = formatted.substring(0, formatted.length() - 1);
                }
                return formatted;
            }
            
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Creates a formatted calculation suggestion.
     */
    public static String createCalculationSuggestion(String expression, String result) {
        if (result == null) {
            return null;
        }
        return "= " + result;
    }
}