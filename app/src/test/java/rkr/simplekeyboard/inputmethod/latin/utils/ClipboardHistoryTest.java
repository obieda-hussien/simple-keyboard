package rkr.simplekeyboard.inputmethod.latin.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;

public class ClipboardHistoryTest {
    @Test
    public void retainsOnlyThreeUniqueRecentPastes() {
        ClipboardHistory history = new ClipboardHistory();
        history.recordPaste("one");
        history.recordPaste("two");
        history.recordPaste("three");
        history.recordPaste("one");
        history.recordPaste("four");
        assertEquals(Arrays.asList("four", "one", "three"), history.entries());
    }

    @Test
    public void skipsOversizedTextAndCanBeCleared() {
        ClipboardHistory history = new ClipboardHistory();
        history.recordPaste("safe");
        history.recordPaste(new String(new char[501]).replace('\0', 'x'));
        assertEquals(Arrays.asList("safe"), history.entries());
        history.clear();
        assertTrue(history.entries().isEmpty());
    }
}
