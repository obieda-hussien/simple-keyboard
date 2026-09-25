package rkr.simplekeyboard.inputmethod.latin.learning;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.junit.Test;

public class MixedScriptRankingTest {
    @Test
    public void typingArabicPrefixPrefersArabicCompletion() {
        List<String> ranked = SuggestionRanker.rankSuggestions(
                Arrays.asList("hello", "مرحبا"), "مرح", "hello",
                new HashMap<>(), new HashMap<>());
        assertEquals("مرحبا", ranked.get(0));
    }

    @Test
    public void typingLatinPrefixPrefersLatinCompletion() {
        List<String> ranked = SuggestionRanker.rankSuggestions(
                Arrays.asList("مرحبا", "hello"), "hel", "مرحبا",
                new HashMap<>(), new HashMap<>());
        assertEquals("hello", ranked.get(0));
    }

    @Test
    public void noTypedWordStillAllowsCrossLanguagePrediction() {
        List<String> ranked = SuggestionRanker.rankSuggestions(
                Collections.singletonList("hello"), "", "مرحبا",
                new HashMap<>(), new HashMap<>());
        assertTrue(ranked.contains("hello"));
    }
}
