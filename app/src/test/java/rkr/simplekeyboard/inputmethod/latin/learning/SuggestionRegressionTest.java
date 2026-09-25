package rkr.simplekeyboard.inputmethod.latin.learning;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.junit.Test;
import rkr.simplekeyboard.inputmethod.latin.utils.EmailSuggestionProvider;

public class SuggestionRegressionTest {
    @Test
    public void typoSuggestionsAreBoundedAndDoNotIncludeExactMatch() {
        List<String> candidates = Arrays.asList("the", "then", "they", "there", "teh");
        List<String> result = SuggestionRanker.generateTypoSuggestions("teh", candidates, 2);
        assertTrue(result.size() <= 2);
        assertTrue(result.contains("the"));
        assertFalse(result.contains("teh"));
    }

    @Test
    public void arabicWordsCanBeRetrievedByPrefix() {
        WordTrie dictionary = new WordTrie();
        dictionary.insert("دلوقتي");
        dictionary.insert("دلوقتي");
        dictionary.insert("دليل");
        List<String> result = dictionary.getSuggestions("دلو");
        assertEquals(Arrays.asList("دلوقتي"), result);
    }

    @Test
    public void bundledDictionaryIncludesEgyptianAndModernEnglishWords() {
        List<String> words = BootstrapVocabulary.getAllWords();
        assertTrue(words.contains("دلوقتي"));
        assertTrue(words.contains("عايزين"));
        assertTrue(words.contains("autocorrect"));
        assertTrue(words.contains("clipboard"));
    }

    @Test
    public void arabicOrthographicVariantsRankAsSameWord() {
        List<String> ranked = SuggestionRanker.rankSuggestions(
                Arrays.asList("how", "إزاي", "ازايك"), "ازاي", "",
                new java.util.HashMap<>(), new java.util.HashMap<>());
        assertEquals("إزاي", ranked.get(0));
        assertEquals("ازاي", SuggestionRanker.normalizeForComparison("إزاي"));
    }

    @Test
    public void boundedDistanceStopsDistantCandidates() {
        assertEquals(1, SuggestionRanker.calculateBoundedDistance("teh", "the", 2));
        assertEquals(3, SuggestionRanker.calculateBoundedDistance("keyboard", "house", 2));
    }

    @Test
    public void restoredLanguageModelRemainsBounded() {
        NGramModel model = new NGramModel();
        StringBuilder contexts = new StringBuilder();
        for (int i = 0; i < 4100; i++) {
            contexts.append("context").append(i).append("|||next|||1;;;");
        }
        model.deserializeBigramData(contexts.toString());
        assertTrue(model.serializeBigramData().split(";;;").length <= 4000);
        assertFalse(model.serializeBigramData().contains("context0|||"));

        StringBuilder followers = new StringBuilder();
        for (int i = 0; i < 30; i++) {
            followers.append("same|||word").append(i).append("|||1;;;");
        }
        model.deserializeTrigramData(followers.toString());
        assertTrue(model.serializeTrigramData().split(";;;").length <= 16);
    }

    @Test
    public void personalVocabularyRejectsAddressesAndLongTokens() {
        assertTrue(LocalLearningEngine.isValidWord("دلوقتي"));
        assertTrue(LocalLearningEngine.isValidWord("can't"));
        assertFalse(LocalLearningEngine.isValidWord("person@example.com"));
        assertFalse(LocalLearningEngine.isValidWord("https://example.com"));
        char[] longToken = new char[49];
        Arrays.fill(longToken, 'x');
        assertFalse(LocalLearningEngine.isValidWord(new String(longToken)));
    }

    @Test
    public void domainCompletionOnlyOffersMatchingUnfinishedAddresses() {
        EmailSuggestionProvider provider = new EmailSuggestionProvider(null);
        List<String> suggestions = provider.getDomainCompletions("oba", "gm");
        assertEquals(Arrays.asList("oba@gmail.com"), suggestions);
        assertTrue(provider.getDomainCompletions("oba", "gmail.com").isEmpty());
    }

    @Test
    public void autocorrectionOnlyUsesUnambiguousEditsAndRespectsRejection() {
        HashSet<String> rejected = new HashSet<>();
        assertEquals("the", AutoCorrectionPolicy.choose("teh",
                Arrays.asList("teh", "the"), false, rejected));
        assertEquals("Hello", AutoCorrectionPolicy.choose("Hella",
                Collections.singletonList("hello"), false, rejected));
        assertTrue(AutoCorrectionPolicy.choose("teh", Arrays.asList("the", "eth"),
                false, rejected) == null);
        assertTrue(AutoCorrectionPolicy.choose("teh", Collections.singletonList("the"),
                true, rejected) == null);
        rejected.add(AutoCorrectionPolicy.rejectionKey("teh", "the"));
        assertTrue(AutoCorrectionPolicy.choose("teh", Collections.singletonList("the"),
                false, rejected) == null);
        assertTrue(AutoCorrectionPolicy.choose("مريم", Collections.singletonList("مرام"),
                false, Collections.emptySet()) == null);
    }
}
