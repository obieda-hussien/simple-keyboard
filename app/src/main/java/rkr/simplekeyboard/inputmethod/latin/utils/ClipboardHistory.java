package rkr.simplekeyboard.inputmethod.latin.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Text explicitly pasted by the user in this process. Never persisted or polled. */
public final class ClipboardHistory {
    private static final int MAX_ENTRIES = 3;
    private static final int MAX_ENTRY_LENGTH = 500;
    private final List<String> entries = new ArrayList<>();

    public void recordPaste(String text) {
        if (text == null || text.isEmpty() || text.length() > MAX_ENTRY_LENGTH) return;
        entries.remove(text);
        entries.add(0, text);
        if (entries.size() > MAX_ENTRIES) entries.remove(entries.size() - 1);
    }

    public List<String> entries() {
        return Collections.unmodifiableList(new ArrayList<>(entries));
    }

    public void clear() {
        entries.clear();
    }
}
