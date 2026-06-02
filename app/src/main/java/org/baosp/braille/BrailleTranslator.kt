package org.baosp.braille

/**
 * BrailleTranslator — converts plain text to Braille dot patterns.
 *
 * Grade 1: one-to-one letter mapping.
 * Grade 2: contracted Braille — common words and patterns compressed.
 *          Currently a stub that falls back to Grade 1; full LibLouis
 *          integration is tracked in ROADMAP.md.
 *
 * Output format: each byte encodes one Braille cell using the standard
 * 8-dot layout (dots 1–8 → bits 0–7). Cells are sent in row order,
 * left to right across the display.
 */
object BrailleTranslator {

    // ──────────────────────────────────────────────────────────────────────
    // Grade 1 table — English Braille (EBAE), 6-dot, lower case
    // Bit layout: bit 0 = dot 1, bit 1 = dot 2, …, bit 5 = dot 6
    // ──────────────────────────────────────────────────────────────────────
    private val GRADE1_TABLE: Map<Char, Byte> = mapOf(
        'a' to 0b000001.toByte(),  // dot 1
        'b' to 0b000011.toByte(),  // dots 1 2
        'c' to 0b001001.toByte(),  // dots 1 4
        'd' to 0b011001.toByte(),  // dots 1 4 5
        'e' to 0b010001.toByte(),  // dots 1 5
        'f' to 0b001011.toByte(),  // dots 1 2 4
        'g' to 0b011011.toByte(),  // dots 1 2 4 5
        'h' to 0b010011.toByte(),  // dots 1 2 5
        'i' to 0b001010.toByte(),  // dots 2 4
        'j' to 0b011010.toByte(),  // dots 2 4 5
        'k' to 0b000101.toByte(),  // dots 1 3
        'l' to 0b000111.toByte(),  // dots 1 2 3
        'm' to 0b001101.toByte(),  // dots 1 3 4
        'n' to 0b011101.toByte(),  // dots 1 3 4 5
        'o' to 0b010101.toByte(),  // dots 1 3 5
        'p' to 0b001111.toByte(),  // dots 1 2 3 4
        'q' to 0b011111.toByte(),  // dots 1 2 3 4 5
        'r' to 0b010111.toByte(),  // dots 1 2 3 5
        's' to 0b001110.toByte(),  // dots 2 3 4
        't' to 0b011110.toByte(),  // dots 2 3 4 5
        'u' to 0b100101.toByte(),  // dots 1 3 6
        'v' to 0b100111.toByte(),  // dots 1 2 3 6
        'w' to 0b111010.toByte(),  // dots 2 4 5 6
        'x' to 0b101101.toByte(),  // dots 1 3 4 6
        'y' to 0b111101.toByte(),  // dots 1 3 4 5 6
        'z' to 0b110101.toByte(),  // dots 1 3 5 6
        ' ' to 0b000000.toByte(),  // blank cell
        '\n' to 0b000000.toByte()  // newline → blank
    )

    // Number indicator cell (dots 3 4 5 6) followed by digit cells
    private const val NUMBER_INDICATOR: Byte = 0b111100.toByte()

    private val DIGIT_TABLE: Map<Char, Byte> = mapOf(
        '1' to GRADE1_TABLE['a']!!,
        '2' to GRADE1_TABLE['b']!!,
        '3' to GRADE1_TABLE['c']!!,
        '4' to GRADE1_TABLE['d']!!,
        '5' to GRADE1_TABLE['e']!!,
        '6' to GRADE1_TABLE['f']!!,
        '7' to GRADE1_TABLE['g']!!,
        '8' to GRADE1_TABLE['h']!!,
        '9' to GRADE1_TABLE['i']!!,
        '0' to GRADE1_TABLE['j']!!
    )

    // Capital indicator cell (dot 6)
    private const val CAPITAL_INDICATOR: Byte = 0b100000.toByte()

    // ──────────────────────────────────────────────────────────────────────
    // Grade 2 common-word contractions (subset — expand over time)
    // ──────────────────────────────────────────────────────────────────────
    private val GRADE2_WORDS: Map<String, ByteArray> = mapOf(
        "the"  to byteArrayOf(0b011110.toByte()),           // dot 2345 (⠞)
        "and"  to byteArrayOf(0b011111.toByte()),           // dots 12345 (⠯)
        "for"  to byteArrayOf(0b111111.toByte()),           // dots 123456 (⠿)
        "of"   to byteArrayOf(0b101111.toByte()),           // dots 12346 (⠯ stub)
        "with" to byteArrayOf(0b110111.toByte()),           // dots 12356 (stub)
        "in"   to byteArrayOf(0b001010.toByte()),           // dots 24 (⠊)
        "a"    to byteArrayOf(0b000001.toByte()),           // dot 1
        "i"    to byteArrayOf(0b001010.toByte())            // dots 24
    )

    // ──────────────────────────────────────────────────────────────────────
    // Public API
    // ──────────────────────────────────────────────────────────────────────

    fun toGrade1Bytes(text: String): ByteArray {
        val cells = mutableListOf<Byte>()
        var i = 0
        while (i < text.length) {
            val ch = text[i]
            when {
                ch.isDigit() -> {
                    cells.add(NUMBER_INDICATOR)
                    cells.add(DIGIT_TABLE[ch] ?: 0b000000.toByte())
                }
                ch.isUpperCase() -> {
                    cells.add(CAPITAL_INDICATOR)
                    cells.add(GRADE1_TABLE[ch.lowercaseChar()] ?: 0b000000.toByte())
                }
                else -> cells.add(GRADE1_TABLE[ch] ?: 0b000000.toByte())
            }
            i++
        }
        // Append carriage return so the display advances
        cells.add(0b000000.toByte())
        return cells.toByteArray()
    }

    fun toGrade2Bytes(text: String): ByteArray {
        // Split on word boundaries and check each word against contraction table
        val cells = mutableListOf<Byte>()
        val words = text.lowercase().split(Regex("(?<=\\s)|(?=\\s)"))
        for (word in words) {
            val trimmed = word.trim()
            val contraction = GRADE2_WORDS[trimmed]
            if (contraction != null) {
                cells.addAll(contraction.toList())
            } else {
                // Fall back to Grade 1 for uncontracted words
                cells.addAll(toGrade1Bytes(word).toList())
            }
        }
        return cells.toByteArray()
    }

    /**
     * Returns the Grade 1 Unicode Braille character for display in the UI.
     * Used for status/preview text in BrailleActivity.
     */
    fun toUnicodeBraille(text: String): String {
        val sb = StringBuilder()
        for (ch in text.lowercase()) {
            val byte = GRADE1_TABLE[ch] ?: 0b000000.toByte()
            // Unicode Braille block starts at U+2800
            val codePoint = 0x2800 or byte.toInt()
            sb.appendCodePoint(codePoint)
        }
        return sb.toString()
    }
}
