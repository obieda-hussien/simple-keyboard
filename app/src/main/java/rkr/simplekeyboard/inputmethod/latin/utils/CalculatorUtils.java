/*
 * Copyright (C) 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package rkr.simplekeyboard.inputmethod.latin.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

/**
 * Small, bounded on-device calculator used by the suggestion engine.
 *
 * Supports Latin, Arabic-Indic and Eastern Arabic numerals plus common mobile math glyphs.
 * Input is deliberately limited before it reaches the expression parser.
 */
public final class CalculatorUtils {
    private static final int MAX_EXPRESSION_LENGTH = 160;

    private static final String NUMBER_CHARS = "\\d٠-٩۰-۹";
    private static final String OP_CHARS = "+\\-−–*/×÷^";
    private static final String SAFE_CHARS = NUMBER_CHARS + OP_CHARS + "()\\s.٫٬";

    private static final Pattern MATH_EXPRESSION_PATTERN = Pattern.compile(
            "([" + SAFE_CHARS + "]+[" + OP_CHARS + "][" + SAFE_CHARS + "]*)");

    private static final Pattern CONTAINS_MATH_OPS = Pattern.compile(
            ".*[" + OP_CHARS + "].*");

    private static final Pattern VALID_MATH_EXPRESSION = Pattern.compile(
            "^[" + SAFE_CHARS + "]+$");

    private CalculatorUtils() {}

    private static String normalizeMathCharacters(String text) {
        if (text == null) return null;
        StringBuilder out = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            switch (ch) {
            case '٠': case '۰': out.append('0'); break;
            case '١': case '۱': out.append('1'); break;
            case '٢': case '۲': out.append('2'); break;
            case '٣': case '۳': out.append('3'); break;
            case '٤': case '۴': out.append('4'); break;
            case '٥': case '۵': out.append('5'); break;
            case '٦': case '۶': out.append('6'); break;
            case '٧': case '۷': out.append('7'); break;
            case '٨': case '۸': out.append('8'); break;
            case '٩': case '۹': out.append('9'); break;
            case '×': out.append('*'); break;
            case '÷': out.append('/'); break;
            case '−': case '–': out.append('-'); break;
            case '٫': out.append('.'); break;
            case '٬': break; // Arabic thousands separator.
            default: out.append(ch); break;
            }
        }
        return out.toString();
    }

    private static Double evaluateCandidate(String expression) {
        if (expression == null) return null;
        String trimmed = expression.trim();
        if (trimmed.isEmpty() || trimmed.length() > MAX_EXPRESSION_LENGTH) return null;
        if (!CONTAINS_MATH_OPS.matcher(trimmed).matches()) return null;
        if (!VALID_MATH_EXPRESSION.matcher(trimmed).matches()) return null;

        String normalized = normalizeMathCharacters(trimmed);
        if (normalized == null || !normalized.matches(".*\\d.*")) return null;
        try {
            Expression exp = new ExpressionBuilder(normalized).build();
            double result = exp.evaluate();
            return Double.isFinite(result) ? result : null;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    /**
     * Finds the last valid expression and evaluates it in a single scan. This avoids parsing the
     * same expression twice on every suggestion update.
     */
    public static String evaluateMathExpression(String text) {
        if (text == null || text.trim().isEmpty()) return null;

        Matcher matcher = MATH_EXPRESSION_PATTERN.matcher(text);
        Double lastValue = null;
        while (matcher.find()) {
            String candidate = matcher.group(1).trim();
            if (candidate.endsWith("=")) {
                candidate = candidate.substring(0, candidate.length() - 1).trim();
            }
            Double value = evaluateCandidate(candidate);
            if (value != null) lastValue = value;
        }
        return lastValue == null ? null : formatResult(lastValue);
    }

    public static boolean isMathExpression(String text) {
        return evaluateMathExpression(text) != null;
    }

    private static String formatResult(double result) {
        if (result == 0d) return "0";
        BigDecimal decimal = BigDecimal.valueOf(result)
                .setScale(8, RoundingMode.HALF_UP)
                .stripTrailingZeros();
        String plain = decimal.toPlainString();
        // Keep suggestion chips compact for extremely large/small results.
        if (plain.length() > 28) {
            return BigDecimal.valueOf(result)
                    .round(new java.math.MathContext(10))
                    .stripTrailingZeros()
                    .toEngineeringString();
        }
        return plain;
    }

    public static String createCalculationSuggestion(String expression, String result) {
        return result == null ? null : "= " + result;
    }
}
