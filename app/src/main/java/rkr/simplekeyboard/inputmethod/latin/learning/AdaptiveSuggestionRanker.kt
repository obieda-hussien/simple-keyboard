package rkr.simplekeyboard.inputmethod.latin.learning

import java.util.Locale
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max

/**
 * Second-stage ranker for the local keyboard model.
 *
 * It deliberately operates after candidate generation so it can evolve independently from the
 * trie and n-gram storage. All inputs are local signals; no network or cloud model is involved.
 */
object AdaptiveSuggestionRanker {
    private const val DAY_MS = 86_400_000.0
    private const val RECENCY_HALF_LIFE_DAYS = 12.0

    @JvmStatic
    fun rerank(
        candidates: List<String>?,
        currentWord: String?,
        previousContext: String?,
        wordFrequency: Map<String, Int>?,
        recentUsage: Map<String, Long>?,
        contextScores: Map<String, Double>?,
        maxResults: Int
    ): List<String> {
        if (candidates.isNullOrEmpty() || maxResults <= 0) return emptyList()

        val typed = normalize(currentWord.orEmpty())
        val typedScript = scriptOf(typed)
        val previousWord = normalize(previousContext.orEmpty().trim().substringAfterLast(' ', ""))
        val now = System.currentTimeMillis()

        return candidates.asSequence()
            .filter { it.isNotBlank() }
            .distinctBy { normalize(it) }
            .mapIndexed { index, candidate ->
                val normalized = normalize(candidate)
                var score = 18.0 / (1.0 + index * 0.55)

                // Functional chips such as the on-device calculator should not be buried under
                // ordinary lexical completions merely because their text shape differs.
                if (candidate.startsWith("= ")) score += 180.0

                if (typed.isNotEmpty()) {
                    val candidateScript = scriptOf(normalized)
                    if (typedScript != 0 && candidateScript != 0 && typedScript != candidateScript) {
                        score -= 80.0
                    }

                    when {
                        normalized == typed -> score += 120.0
                        normalized.startsWith(typed) -> {
                            val completion = (normalized.length - typed.length).coerceAtLeast(0)
                            score += 72.0 - completion.coerceAtMost(12) * 2.1
                        }
                        else -> {
                            val threshold = if (typed.length >= 7) 3 else 2
                            val distance = boundedDamerauLevenshtein(typed, normalized, threshold)
                            if (distance <= threshold) {
                                score += (threshold - distance + 1) * 18.0
                                score += keyboardProximityBonus(typed, normalized)
                            }
                        }
                    }
                }

                val frequency = lookupFrequency(wordFrequency, candidate, normalized)
                if (frequency > 0) {
                    score += 12.0 * ln(1.0 + frequency)
                }

                val lastUsed = lookupRecent(recentUsage, candidate, normalized)
                if (lastUsed > 0L && lastUsed <= now) {
                    val ageDays = (now - lastUsed) / DAY_MS
                    val decay = exp(-0.69314718056 * ageDays / RECENCY_HALF_LIFE_DAYS)
                    score += 28.0 * decay
                }

                val contextual = lookupContext(contextScores, candidate, normalized)
                if (contextual > 0.0) {
                    score += 95.0 * contextual.coerceIn(0.0, 1.0)
                    if (candidate.indexOf(' ') >= 0 && contextual >= 0.18) score += 10.0
                }

                if (previousWord.isNotEmpty() && normalized == previousWord && typed.isEmpty()) {
                    score -= 14.0
                }

                // Small preference for compact suggestions, without penalizing Arabic morphology.
                if (normalized.length in 2..12) score += 4.0

                Ranked(candidate, score)
            }
            .sortedWith(compareByDescending<Ranked> { it.score }.thenBy { it.text.length })
            .take(maxResults)
            .map { it.text }
            .toList()
    }

    @JvmStatic
    fun boundedDamerauLevenshtein(first: String?, second: String?, maxDistance: Int): Int {
        if (first == null || second == null) return maxDistance + 1
        if (first == second) return 0
        if (abs(first.length - second.length) > maxDistance) return maxDistance + 1
        if (first.isEmpty()) return if (second.length <= maxDistance) second.length else maxDistance + 1
        if (second.isEmpty()) return if (first.length <= maxDistance) first.length else maxDistance + 1

        var previousPrevious = IntArray(second.length + 1)
        var previous = IntArray(second.length + 1) { it }
        var current = IntArray(second.length + 1)

        for (i in 1..first.length) {
            current[0] = i
            var rowMin = current[0]
            val from = max(1, i - maxDistance - 1)
            val to = minOf(second.length, i + maxDistance + 1)

            for (j in 1 until from) current[j] = maxDistance + 1

            for (j in from..to) {
                val substitution = previous[j - 1] + if (first[i - 1] == second[j - 1]) 0 else 1
                val insertion = current[j - 1] + 1
                val deletion = previous[j] + 1
                var value = minOf(substitution, insertion, deletion)

                if (i > 1 && j > 1 &&
                    first[i - 1] == second[j - 2] &&
                    first[i - 2] == second[j - 1]
                ) {
                    value = minOf(value, previousPrevious[j - 2] + 1)
                }
                current[j] = value
                rowMin = minOf(rowMin, value)
            }

            for (j in to + 1..second.length) current[j] = maxDistance + 1
            if (rowMin > maxDistance) return maxDistance + 1

            val swap = previousPrevious
            previousPrevious = previous
            previous = current
            current = swap
        }

        return previous[second.length].coerceAtMost(maxDistance + 1)
    }

    private fun lookupFrequency(map: Map<String, Int>?, raw: String, normalized: String): Int {
        if (map == null) return 0
        return map[raw] ?: map[normalized] ?: map[raw.lowercase(Locale.ROOT)] ?: 0
    }

    private fun lookupRecent(map: Map<String, Long>?, raw: String, normalized: String): Long {
        if (map == null) return 0L
        return map[raw] ?: map[normalized] ?: map[raw.lowercase(Locale.ROOT)] ?: 0L
    }

    private fun lookupContext(map: Map<String, Double>?, raw: String, normalized: String): Double {
        if (map == null) return 0.0
        return map[raw] ?: map[normalized] ?: 0.0
    }

    private fun normalize(value: String): String {
        if (value.isBlank()) return ""
        val lower = value.lowercase(Locale.ROOT)
        val out = StringBuilder(lower.length)
        var i = 0
        while (i < lower.length) {
            val cp = lower.codePointAt(i)
            i += Character.charCount(cp)
            when {
                cp == 0x0640 -> Unit
                cp in 0x064B..0x065F || cp == 0x0670 || cp in 0x06D6..0x06ED -> Unit
                cp == 0x0622 || cp == 0x0623 || cp == 0x0625 || cp == 0x0671 -> out.append('ا')
                cp == 0x0649 -> out.append('ي')
                else -> out.appendCodePoint(cp)
            }
        }
        return out.toString()
    }

    private fun scriptOf(word: String): Int {
        var script = 0
        for (ch in word) {
            val next = when {
                ch in 'a'..'z' -> 1
                ch.code in 0x0600..0x06FF || ch.code in 0x0750..0x077F ||
                    ch.code in 0x08A0..0x08FF -> 2
                ch.isLetter() -> 3
                else -> continue
            }
            if (script != 0 && script != next) return 0
            script = next
        }
        return script
    }

    private fun keyboardProximityBonus(a: String, b: String): Double {
        if (a.length != b.length || a.isEmpty()) return 0.0
        var mismatches = 0
        var adjacent = 0
        for (i in a.indices) {
            if (a[i] == b[i]) continue
            mismatches++
            if (areAdjacent(a[i], b[i])) adjacent++
            if (mismatches > 2) return 0.0
        }
        return when {
            mismatches == 1 && adjacent == 1 -> 12.0
            mismatches == 2 && adjacent == 2 -> 6.0
            else -> 0.0
        }
    }

    private fun areAdjacent(a: Char, b: Char): Boolean {
        if (a == b) return true
        val latinRows = arrayOf("qwertyuiop", "asdfghjkl", "zxcvbnm")
        val arabicRows = arrayOf("ضصثقفغعهخحج", "شسيبلاتنمكة", "ئءؤرلاىةوزظ")
        return adjacentInRows(a, b, latinRows) || adjacentInRows(a, b, arabicRows)
    }

    private fun adjacentInRows(a: Char, b: Char, rows: Array<String>): Boolean {
        for (rowIndex in rows.indices) {
            val ia = rows[rowIndex].indexOf(a)
            if (ia < 0) continue
            val ib = rows[rowIndex].indexOf(b)
            if (ib >= 0 && abs(ia - ib) <= 1) return true
            for (other in max(0, rowIndex - 1)..minOf(rows.lastIndex, rowIndex + 1)) {
                if (other == rowIndex) continue
                val ob = rows[other].indexOf(b)
                if (ob >= 0 && abs(ia - ob) <= 1) return true
            }
        }
        return false
    }

    private data class Ranked(val text: String, val score: Double)
}
