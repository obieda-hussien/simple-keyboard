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
 */
public class CalculatorUtils {
    
    // Enhanced pattern to match complex math expressions with parentheses
    private static final Pattern MATH_PATTERN = Pattern.compile(
        "\\s*([\\d+\\-*/()\\s.]+)\\s*=?\\s*"
    );
    
    // More specific pattern to validate that expression contains math operations
    private static final Pattern CONTAINS_MATH_OPS = Pattern.compile(
        ".*[+\\-*/].*"
    );
    
    // Pattern to ensure expression has numbers and operations
    private static final Pattern VALID_MATH_EXPRESSION = Pattern.compile(
        "^[\\d+\\-*/()\\s.]+$"
    );
    
    /**
     * Checks if a string contains a mathematical expression that can be evaluated.
     */
    public static boolean isMathExpression(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = text.trim();
        
        // Remove trailing equals sign if present
        if (trimmed.endsWith("=")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1).trim();
        }
        
        // Must contain mathematical operations
        if (!CONTAINS_MATH_OPS.matcher(trimmed).matches()) {
            return false;
        }
        
        // Must only contain valid mathematical characters
        if (!VALID_MATH_EXPRESSION.matcher(trimmed).matches()) {
            return false;
        }
        
        // Must have at least one digit
        if (!trimmed.matches(".*\\d.*")) {
            return false;
        }
        
        // Try to parse with exp4j to validate
        try {
            Expression expression = new ExpressionBuilder(trimmed).build();
            expression.evaluate(); // This will throw exception if invalid
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Evaluates a mathematical expression and returns the result.
     * Returns null if the expression cannot be evaluated.
     */
    public static String evaluateMathExpression(String text) {
        if (!isMathExpression(text)) {
            return null;
        }
        
        String trimmed = text.trim();
        
        // Remove trailing equals sign if present
        if (trimmed.endsWith("=")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1).trim();
        }
        
        try {
            Expression expression = new ExpressionBuilder(trimmed).build();
            double result = expression.evaluate();
            
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