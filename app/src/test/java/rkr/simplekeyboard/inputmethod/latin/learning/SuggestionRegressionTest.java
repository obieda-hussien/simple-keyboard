package rkr.simplekeyboard.inputmethod.latin.learning;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

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
}
