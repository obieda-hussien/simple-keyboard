package rkr.simplekeyboard.inputmethod.latin.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class ClipboardHistoryTest {
    @Test
    public void recentHistoryIsBoundedUniqueAndMostRecentFirst() {
        ClipboardHistory history = new ClipboardHistory();
        for (int i = 1; i <= 9; i++) {
            history.recordPaste("item-" + i);
        }
        history.recordPaste("item-5");

        assertEquals(8, history.entries().size());
        assertEquals("item-5", history.entries().get(0));
        assertFalse(history.entries().contains("item-1"));
    }

    @Test
    public void pinnedEntriesSurviveClearRecentAndComeFirst() {
        ClipboardHistory history = new ClipboardHistory();
        history.recordPaste("first");
        history.recordPaste("important");
        history.recordPaste("last");

        assertTrue(history.togglePinned("important"));
        assertTrue(history.isPinned("important"));
        assertEquals("important", history.entries().get(0));

        history.clearRecent();
        assertEquals(Collections.singletonList("important"), history.entries());

        assertFalse(history.togglePinned("important"));
        assertTrue(history.entries().contains("important"));
    }

    @Test
    public void pinSetIsBounded() {
        ClipboardHistory history = new ClipboardHistory();
        for (int i = 0; i < 6; i++) {
            history.recordPaste("pin-" + i);
            history.togglePinned("pin-" + i);
        }
        assertEquals(4, history.pinnedEntries().size());
        assertFalse(history.pinnedEntries().contains("pin-0"));
        assertFalse(history.pinnedEntries().contains("pin-1"));
    }

    @Test
    public void skipsOversizedTextAndClearRemovesEverything() {
        ClipboardHistory history = new ClipboardHistory();
        history.recordPaste("safe");
        history.recordPaste(new String(new char[1001]).replace('\0', 'x'));
        assertEquals(Arrays.asList("safe"), history.entries());

        history.togglePinned("safe");
        history.clear();
        assertTrue(history.entries().isEmpty());
        assertTrue(history.pinnedEntries().isEmpty());
    }
}
