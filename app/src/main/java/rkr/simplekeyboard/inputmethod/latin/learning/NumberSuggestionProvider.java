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

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides contextual number suggestions based on patterns and context.
 */
public class NumberSuggestionProvider {
    
    private static final Pattern DATE_PATTERN = Pattern.compile("\\b(january|february|march|april|may|june|july|august|september|october|november|december|jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)\\s+(\\d{1,2})\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern ARABIC_DATE_PATTERN = Pattern.compile("\\b(يناير|فبراير|مارس|أبريل|مايو|يونيو|يوليو|أغسطس|سبتمبر|أكتوبر|نوفمبر|ديسمبر)\\s+(\\d{1,2})\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\b\\d+\\b");
    private static final Pattern CURRENCY_CONTEXT_PATTERN = Pattern.compile("\\b(price|cost|buy|sell|pay|dollar|euro|pound|سعر|شراء|بيع|دفع|دولار|يورو)\\s+(\\d+)\\b", Pattern.CASE_INSENSITIVE);
    
    /**
     * Gets contextual number suggestions based on the input context.
     * @param context The full text context
     * @param currentWord The current word being typed (may be null)
     * @return List of number-based suggestions
     */
    public static List<String> getNumberSuggestions(String context, String currentWord) {
        List<String> suggestions = new ArrayList<>();
        
        if (TextUtils.isEmpty(context)) {
            return suggestions;
        }
        
        String lowerContext = context.toLowerCase().trim();
        
        // Date completion suggestions
        suggestions.addAll(getDateSuggestions(lowerContext));
        
        // Currency and price suggestions
        suggestions.addAll(getCurrencySuggestions(lowerContext, currentWord));
        
        // Time-based suggestions
        suggestions.addAll(getTimeSuggestions(lowerContext));
        
        // Number format suggestions
        suggestions.addAll(getNumberFormatSuggestions(lowerContext, currentWord));
        
        // Limit to 3 suggestions to avoid overwhelming
        return suggestions.size() > 3 ? suggestions.subList(0, 3) : suggestions;
    }
    
    /**
     * Suggests year completion for date patterns like "August 14" -> suggest "2025"
     */
    private static List<String> getDateSuggestions(String context) {
        List<String> suggestions = new ArrayList<>();
        
        // Check for English date patterns
        Matcher englishMatcher = DATE_PATTERN.matcher(context);
        if (englishMatcher.find()) {
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            suggestions.add(String.valueOf(currentYear));
            if (currentYear < 2030) {
                suggestions.add(String.valueOf(currentYear + 1));
            }
            return suggestions;
        }
        
        // Check for Arabic date patterns
        Matcher arabicMatcher = ARABIC_DATE_PATTERN.matcher(context);
        if (arabicMatcher.find()) {
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            suggestions.add(String.valueOf(currentYear));
            return suggestions;
        }
        
        return suggestions;
    }
    
    /**
     * Suggests currency symbols and formats based on context.
     */
    private static List<String> getCurrencySuggestions(String context, String currentWord) {
        List<String> suggestions = new ArrayList<>();
        
        // Check if context mentions money/price and a number
        Matcher currencyMatcher = CURRENCY_CONTEXT_PATTERN.matcher(context);
        if (currencyMatcher.find()) {
            String number = currencyMatcher.group(2);
            // Suggest common currency formats
            suggestions.add("$" + number);
            suggestions.add(number + " USD");
            suggestions.add("€" + number);
            return suggestions;
        }
        
        // If current word is a number, suggest currency symbols
        if (currentWord != null && currentWord.matches("\\d+")) {
            suggestions.add("$" + currentWord);
            suggestions.add(currentWord + "%");
            suggestions.add("#" + currentWord);
        }
        
        return suggestions;
    }
    
    /**
     * Suggests time-related numbers based on context.
     */
    private static List<String> getTimeSuggestions(String context) {
        List<String> suggestions = new ArrayList<>();
        
        // Time patterns
        if (context.contains("at ") || context.contains("في ") || 
            context.contains("time") || context.contains("وقت")) {
            
            int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            
            // Suggest current time
            if (currentHour < 12) {
                suggestions.add(currentHour + ":00 AM");
            } else {
                suggestions.add((currentHour - 12) + ":00 PM");
            }
            
            // Suggest common meeting times
            suggestions.add("2:00 PM");
            suggestions.add("10:00 AM");
        }
        
        return suggestions;
    }
    
    /**
     * Suggests number formatting based on context and current number.
     */
    private static List<String> getNumberFormatSuggestions(String context, String currentWord) {
        List<String> suggestions = new ArrayList<>();
        
        if (currentWord == null || !currentWord.matches("\\d+")) {
            return suggestions;
        }
        
        int number = Integer.parseInt(currentWord);
        
        // Phone number formatting
        if (context.contains("phone") || context.contains("call") || 
            context.contains("هاتف") || context.contains("اتصال")) {
            if (currentWord.length() >= 3) {
                suggestions.add("(" + currentWord.substring(0, 3) + ") " + 
                               currentWord.substring(3));
            }
        }
        
        // Percentage suggestions
        if (context.contains("percent") || context.contains("rate") || 
            context.contains("نسبة") || context.contains("معدل")) {
            suggestions.add(number + "%");
        }
        
        // Age-related suggestions
        if (context.contains("age") || context.contains("years old") ||
            context.contains("عمر") || context.contains("سنة")) {
            suggestions.add(number + " years old");
            suggestions.add(number + " سنة"); // Arabic: years
        }
        
        // Quantity suggestions
        if (context.contains("buy") || context.contains("need") ||
            context.contains("want") || context.contains("شراء") || context.contains("أريد")) {
            suggestions.add(number + " items");
            suggestions.add(number + " pieces");
        }
        
        return suggestions;
    }
    
    /**
     * Checks if the context suggests number-related input.
     */
    public static boolean isNumberContext(String context) {
        if (TextUtils.isEmpty(context)) {
            return false;
        }
        
        String lowerContext = context.toLowerCase();
        
        // Check for number-related keywords
        return lowerContext.contains("number") || lowerContext.contains("count") ||
               lowerContext.contains("amount") || lowerContext.contains("price") ||
               lowerContext.contains("cost") || lowerContext.contains("age") ||
               lowerContext.contains("time") || lowerContext.contains("date") ||
               lowerContext.contains("year") || lowerContext.contains("phone") ||
               lowerContext.contains("رقم") || lowerContext.contains("عدد") ||
               lowerContext.contains("سعر") || lowerContext.contains("عمر") ||
               lowerContext.contains("وقت") || lowerContext.contains("تاريخ") ||
               lowerContext.contains("سنة") || lowerContext.contains("هاتف");
    }
}