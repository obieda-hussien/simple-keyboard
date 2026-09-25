package rkr.simplekeyboard.inputmethod.latin.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Text explicitly pasted or opened by the user in this process.
 *
 * Nothing is persisted to disk. Pinned entries are process-local and are cleared when the IME
 * service is destroyed or when private input disables clipboard history.
 */
public final class ClipboardHistory {
    private static final int MAX_ENTRIES = 8;
    private static final int MAX_PINNED = 4;
    private static final int MAX_ENTRY_LENGTH = 1000;

    private final List<String> recent = new ArrayList<>();
    private final LinkedHashSet<String> pinned = new LinkedHashSet<>();

    public synchronized void recordPaste(String text) {
        final String normalized = normalize(text);
        if (normalized == null) return;
        recent.remove(normalized);
        recent.add(0, normalized);
        trimRecent();
    }

    public synchronized boolean togglePinned(String text) {
        final String normalized = normalize(text);
        if (normalized == null) return false;
        if (pinned.remove(normalized)) return false;
        if (pinned.size() >= MAX_PINNED) {
            String first = pinned.iterator().next();
            pinned.remove(first);
        }
        pinned.add(normalized);
        recent.remove(normalized);
        recent.add(0, normalized);
        trimRecent();
        return true;
    }

    public synchronized boolean isPinned(String text) {
        return text != null && pinned.contains(text);
    }

    public synchronized Set<String> pinnedEntries() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(pinned));
    }

    /**
     * Returns pinned entries first, followed by recent non-pinned entries.
     */
    public synchronized List<String> entries() {
        final ArrayList<String> result = new ArrayList<>(MAX_ENTRIES + MAX_PINNED);
        result.addAll(pinned);
        for (String value : recent) {
            if (!pinned.contains(value)) result.add(value);
        }
        return Collections.unmodifiableList(result);
    }

    public synchronized void clearRecent() {
        for (int i = recent.size() - 1; i >= 0; i--) {
            if (!pinned.contains(recent.get(i))) recent.remove(i);
        }
    }

    public synchronized void clear() {
        recent.clear();
        pinned.clear();
    }

    private void trimRecent() {
        while (recent.size() > MAX_ENTRIES) recent.remove(recent.size() - 1);
    }

    private static String normalize(String text) {
        if (text == null) return null;
        String value = text.trim();
        if (value.isEmpty() || value.length() > MAX_ENTRY_LENGTH) return null;
        return value;
    }
}
