package com.market.analysis.domain.service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

/**
 * Structural parsing of AI valuations into the four contract sections.
 *
 * <p>Single source of truth for "what a section boundary is", shared by
 * validation ({@link PromptResponseValidator}) and view rendering. A header
 * counts only when it <strong>opens a line</strong> (after optional markdown
 * markers, numbering or whitespace), case- and accent-insensitive; a bare
 * {@code contains} anywhere in the text is not enough, because models
 * sometimes echo the prompt instructions or cram every section under the
 * last header.
 *
 * <p>Each section body must also reach {@value #MIN_SECTION_BODY_CHARS}
 * characters: echoed one-liners ("brief analysis of price and trend.") are
 * shorter and get rejected instead of being displayed as real analysis.
 */
public final class ValorationSections {

    public static final List<String> SECTION_LABELS = List.of(
            "Resumen técnico:",
            "Fortalezas:",
            "Riesgos:",
            "Conclusión interpretativa:");

    static final int MIN_SECTION_BODY_CHARS = 40;

    private ValorationSections() {
        // utility class – no instantiation
    }

    /**
     * Tells whether the response carries the four sections in order, each
     * opening its own line and with a non-trivial body.
     */
    public static boolean isStructured(String response) {
        List<String> bodies = split(response);
        return bodies.stream().allMatch(body -> body.length() >= MIN_SECTION_BODY_CHARS);
    }

    /**
     * Splits the response into the four section bodies in contract order.
     * Always returns four strings (possibly blank) so callers never deal
     * with nulls; check {@link #isStructured(String)} first when it matters.
     */
    public static List<String> split(String response) {
        List<StringBuilder> accumulators = List.of(
                new StringBuilder(), new StringBuilder(), new StringBuilder(), new StringBuilder());
        if (response != null && !response.isBlank()) {
            int current = -1;
            for (String line : response.split("\\R")) {
                int header = headerIndexOf(line, current + 1);
                if (header >= 0) {
                    current = header;
                    accumulators.get(current).append(bodyAfterHeader(line, current)).append('\n');
                } else if (current >= 0) {
                    accumulators.get(current).append(line).append('\n');
                }
            }
        }
        return accumulators.stream().map(x -> x.toString().trim()).toList();
    }

    /**
     * Returns the section index whose header opens the line, or -1.
     * Only headers at or after {@code minIndex} count, enforcing order.
     */
    private static int headerIndexOf(String line, int minIndex) {
        String trimmed = line.strip();
        String stripped = trimmed.replaceFirst("^[\\s#*>`\\-\\d.\\)\\(]+", "");
        String normalized = normalize(stripped);
        for (int i = Math.max(0, minIndex); i < SECTION_LABELS.size(); i++) {
            if (normalized.startsWith(normalize(SECTION_LABELS.get(i)))) {
                return i;
            }
        }
        return -1;
    }

    private static String bodyAfterHeader(String line, int headerIndex) {
        String trimmed = line.strip();
        String stripped = trimmed.replaceFirst("^[\\s#*>`\\-\\d.\\)\\(]+", "");
        String label = SECTION_LABELS.get(headerIndex);
        int prefix = Math.min(stripped.length(), label.length());
        return stripped.substring(prefix).replaceFirst("^[\\s*#>`_~]+", "");
    }

    static String normalize(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        String noDiacritics = Normalizer.normalize(lower, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return noDiacritics.replaceAll("\\s+", " ").trim();
    }
}
