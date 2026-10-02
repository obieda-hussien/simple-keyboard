package rkr.simplekeyboard.inputmethod.latin.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class CalculatorRegressionTest {
    @Test
    public void supportsArabicDecimalAndMobileOperatorGlyphs() {
        assertEquals("25", CalculatorUtils.evaluateMathExpression("١٢٫٥ × ٢"));
        assertEquals("17", CalculatorUtils.evaluateMathExpression("20 − 3"));
        assertEquals("4", CalculatorUtils.evaluateMathExpression("20 ÷ 5"));
    }

    @Test
    public void supportsEasternArabicDigitsAndPower() {
        assertEquals("20", CalculatorUtils.evaluateMathExpression("۱۲ + ۸"));
        assertEquals("256", CalculatorUtils.evaluateMathExpression("2^8"));
    }

    @Test
    public void keepsUsefulPrecisionWithoutLongFloatingNoise() {
        assertEquals("0.33333333", CalculatorUtils.evaluateMathExpression("1/3"));
        assertEquals("262.5", CalculatorUtils.evaluateMathExpression("150 + (5 * 22.5)"));
    }

    @Test
    public void extractsLastValidExpressionFromSentence() {
        assertEquals("120", CalculatorUtils.evaluateMathExpression(
                "Total 25*4, then final 100+20"));
    }

    @Test
    public void rejectsNonMathAndInvalidResults() {
        assertFalse(CalculatorUtils.isMathExpression("hello world"));
        assertNull(CalculatorUtils.evaluateMathExpression("5 / 0"));
    }
}
