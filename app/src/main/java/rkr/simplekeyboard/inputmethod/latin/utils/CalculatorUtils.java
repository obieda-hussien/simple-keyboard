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

/**
 * Utility class for detecting and evaluating mathematical expressions.
 */
public class CalculatorUtils {
    
    // Pattern to match simple math expressions
    private static final Pattern MATH_PATTERN = Pattern.compile(
        "\\s*(\\d+(?:\\.\\d+)?)\\s*([+\\-*/])\\s*(\\d+(?:\\.\\d+)?)\\s*=?\\s*"
    );
    
    /**
     * Checks if a string contains a mathematical expression that can be evaluated.
     */
    public static boolean isMathExpression(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        
        Matcher matcher = MATH_PATTERN.matcher(text.trim());
        return matcher.matches();
    }
    
    /**
     * Evaluates a mathematical expression and returns the result.
     * Returns null if the expression cannot be evaluated.
     */
    public static String evaluateMathExpression(String text) {
        if (!isMathExpression(text)) {
            return null;
        }
        
        Matcher matcher = MATH_PATTERN.matcher(text.trim());
        if (!matcher.matches()) {
            return null;
        }
        
        try {
            double num1 = Double.parseDouble(matcher.group(1));
            String operator = matcher.group(2);
            double num2 = Double.parseDouble(matcher.group(3));
            
            double result;
            switch (operator) {
                case "+":
                    result = num1 + num2;
                    break;
                case "-":
                    result = num1 - num2;
                    break;
                case "*":
                    result = num1 * num2;
                    break;
                case "/":
                    if (num2 == 0) {
                        return null; // Division by zero
                    }
                    result = num1 / num2;
                    break;
                default:
                    return null;
            }
            
            // Format result: show integer if it's a whole number
            if (result == Math.floor(result)) {
                return String.valueOf((long) result);
            } else {
                return String.format("%.2f", result);
            }
            
        } catch (NumberFormatException e) {
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