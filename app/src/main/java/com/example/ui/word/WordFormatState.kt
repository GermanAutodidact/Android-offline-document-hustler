package com.example.ui.word

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import com.example.ui.theme.WordBlue
import com.example.ui.theme.WordHighlightCyan
import com.example.ui.theme.WordHighlightGreen
import com.example.ui.theme.WordHighlightPink
import com.example.ui.theme.WordHighlightYellow

enum class WordRibbonTab(val title: String) {
    START("Start"),
    EINFUEGEN("Einfügen"),
    LAYOUT("Layout"),
    ANSICHT("Ansicht")
}

data class WordFormatState(
    val fontFamilyName: String = "Aptos",
    val fontSizePt: Int = 11,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val isStrikethrough: Boolean = false,
    val isSubscript: Boolean = false,
    val isSuperscript: Boolean = false,
    val fontColor: Color = Color(0xFF1F2937),
    val highlightColor: Color = Color.Transparent,
    val textAlign: TextAlign = TextAlign.Start,
    val lineSpacingMultiplier: Float = 1.15f,
    val isBulletList: Boolean = false,
    val isNumberedList: Boolean = false,
    val currentStyle: String = "Standard",
    val isPrintLayoutView: Boolean = true,
    val isReadingMode: Boolean = false,
    val isRibbonExpanded: Boolean = true,
    val activeRibbonTab: WordRibbonTab = WordRibbonTab.START,
    val pageOrientation: String = "Hochformat",
    val pageMargins: String = "Normal (2,5 cm)",
    val pageSize: String = "A4 (210 × 297 mm)"
) {
    val composeFontFamily: FontFamily
        get() = when (fontFamilyName) {
            "Calibri" -> FontFamily.SansSerif
            "Arial" -> FontFamily.SansSerif
            "Times New Roman" -> FontFamily.Serif
            "Georgia" -> FontFamily.Serif
            "Courier New" -> FontFamily.Monospace
            else -> FontFamily.Default // Aptos / System default clean
        }

    companion object {
        val AVAILABLE_FONT_SIZES = listOf(8, 9, 10, 11, 12, 14, 16, 18, 20, 24, 28, 36, 48, 72)
        val AVAILABLE_FONT_FAMILIES = listOf("Aptos", "Calibri", "Arial", "Times New Roman", "Segoe UI", "Georgia", "Courier New")
        val AVAILABLE_STYLES = listOf("Standard", "Überschrift 1", "Überschrift 2", "Titel", "Untertitel", "Zitat")

        val WORD_TEXT_COLORS = listOf(
            Color(0xFF000000) to "Automatisch",
            WordBlue to "Word-Blau",
            Color(0xFFC00000) to "Dunkelrot",
            Color(0xFF385723) to "Tannengrün",
            Color(0xFF7030A0) to "Königspurpur",
            Color(0xFFED7D31) to "Orange",
            Color(0xFF595959) to "Dunkelgrau"
        )

        val WORD_HIGHLIGHT_COLORS = listOf(
            Color.Transparent to "Keine",
            WordHighlightYellow to "Gelb",
            WordHighlightGreen to "Hellgrün",
            WordHighlightCyan to "Türkis",
            WordHighlightPink to "Magenta",
            Color(0xFFFFC000) to "Bernstein"
        )
    }
}
