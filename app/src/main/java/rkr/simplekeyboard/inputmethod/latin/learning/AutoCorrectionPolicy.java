package rkr.simplekeyboard.inputmethod.latin.learning;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/** A deliberately conservative policy for automatic replacements on space. */
public final class AutoCorrectionPolicy {
    private AutoCorrectionPolicy() {}

    public static String rejectionKey(String original, String replacement) {
        return original.toLowerCase(Locale.ROOT) + "|||"
                + replacement.toLowerCase(Locale.ROOT);
    }

    public static String choose(String original, List<String> suggestions,
            boolean originalIsKnown, Set<String> rejected) {
        if (originalIsKnown || original == null || suggestions == null
                || original.length() < 3 || original.length() > 32
                || !original.matches("[\\p{L}]+")
                || original.codePointCount(0, original.length()) != original.length()) return null;
        if (scriptGroup(original.codePointAt(0)) == 1
                && original.equals(original.toUpperCase(Locale.ROOT))) return null;

        String choice = null;
        String typed = original.toLowerCase(Locale.ROOT);
        for (String suggestion : suggestions) {
            if (suggestion == null || !suggestion.matches("[\\p{L}]+")
                    || suggestion.codePointCount(0, suggestion.length()) != suggestion.length()) {
                continue;
            }
            String candidate = suggestion.toLowerCase(Locale.ROOT);
            if (candidate.equals(typed) || !sameScript(typed, candidate)
                    || (!oneSubstitution(typed, candidate)
                    && !adjacentSwap(typed, candidate))
                    || rejected.contains(rejectionKey(original, suggestion))) continue;
            if (choice != null && !choice.equalsIgnoreCase(suggestion)) return null;
            choice = suggestion;
        }
        if (choice == null) return null;
        if (Character.isUpperCase(original.codePointAt(0))) {
            return choice.substring(0, 1).toUpperCase(Locale.ROOT) + choice.substring(1);
        }
        return choice;
    }

    private static boolean sameScript(String first, String second) {
        int script = scriptGroup(first.codePointAt(0));
        if (script == 0) return false;
        for (int i = 0; i < first.length(); i += Character.charCount(first.codePointAt(i))) {
            if (scriptGroup(first.codePointAt(i)) != script) return false;
        }
        for (int i = 0; i < second.length(); i += Character.charCount(second.codePointAt(i))) {
            if (scriptGroup(second.codePointAt(i)) != script) return false;
        }
        return true;
    }

    private static int scriptGroup(int codePoint) {
        if (codePoint >= 'a' && codePoint <= 'z'
                || codePoint >= 'A' && codePoint <= 'Z') return 1;
        if (codePoint >= 0x0600 && codePoint <= 0x06FF
                || codePoint >= 0x0750 && codePoint <= 0x077F
                || codePoint >= 0x08A0 && codePoint <= 0x08FF) return 2;
        return 0;
    }

    private static boolean oneSubstitution(String first, String second) {
        if (first.length() != second.length() || first.length() < 4) return false;
        // Arabic short words often differ by one meaningful letter; require an explicit tap.
        if (scriptGroup(first.codePointAt(0)) == 2) {
            return false;
        }
        int changed = 0;
        for (int i = 0; i < first.length(); i++) {
            if (first.charAt(i) != second.charAt(i)) changed++;
        }
        return changed == 1;
    }

    private static boolean adjacentSwap(String first, String second) {
        if (first.length() != second.length()) return false;
        for (int i = 0; i < first.length() - 1; i++) {
            if (first.charAt(i) == second.charAt(i + 1)
                    && first.charAt(i + 1) == second.charAt(i)
                    && first.substring(0, i).equals(second.substring(0, i))
                    && first.substring(i + 2).equals(second.substring(i + 2))) return true;
        }
        return false;
    }
}
