package rkr.simplekeyboard.inputmethod.latin.learning;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class AdaptiveSuggestionRankerTest {
    @Test
    public void damerauDistanceTreatsAdjacentSwapAsOneEdit() {
        assertEquals(1, AdaptiveSuggestionRanker.boundedDamerauLevenshtein("teh", "the", 2));
        assertEquals(1, AdaptiveSuggestionRanker.boundedDamerauLevenshtein("hlelo", "hello", 2));
        assertEquals(3, AdaptiveSuggestionRanker.boundedDamerauLevenshtein(
                "keyboard", "house", 2));
    }

    @Test
    public void typedPrefixAndTypoEvidenceBeatUnrelatedCandidates() {
        List<String> ranked = AdaptiveSuggestionRanker.rerank(
                Arrays.asList("house", "hello", "help"),
                "hlelo",
                "",
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptyMap(),
                3);
        assertEquals("hello", ranked.get(0));
    }

    @Test
    public void personalFrequencyAndRecencyCanRefineEqualCandidates() {
        Map<String, Integer> frequency = new HashMap<>();
        frequency.put("تمام", 30);
        Map<String, Long> recent = new HashMap<>();
        recent.put("تمام", System.currentTimeMillis());

        List<String> ranked = AdaptiveSuggestionRanker.rerank(
                Arrays.asList("ماشي", "تمام"),
                "",
                "انا",
                frequency,
                recent,
                Collections.emptyMap(),
                2);
        assertEquals("تمام", ranked.get(0));
    }

    @Test
    public void trigramContextUsesBigramBackoffAndPrefersObservedFollower() {
        NGramModel model = new NGramModel();
        model.deserializeBigramData("عايز|||اعمل|||4;;;عايز|||انام|||1;;;");
        model.deserializeTrigramData("انا عايز|||اعمل|||8;;;انا عايز|||انام|||1;;;");

        List<String> candidates = Arrays.asList("انام", "اعمل");
        Map<String, Double> scores = model.getContextScores("انا عايز", candidates);

        assertTrue(scores.get("اعمل") > scores.get("انام"));
        List<String> ranked = AdaptiveSuggestionRanker.rerank(
                candidates,
                "",
                "انا عايز",
                Collections.emptyMap(),
                Collections.emptyMap(),
                scores,
                2);
        assertEquals("اعمل", ranked.get(0));
    }

    @Test
    public void ArabicAndLatinScriptsDoNotCrowdTypedPrefix() {
        List<String> ranked = AdaptiveSuggestionRanker.rerank(
                Arrays.asList("hello", "مرحبا", "مراحل"),
                "مرح",
                "",
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptyMap(),
                3);
        assertTrue(ranked.indexOf("مرحبا") < ranked.indexOf("hello"));
    }
}
