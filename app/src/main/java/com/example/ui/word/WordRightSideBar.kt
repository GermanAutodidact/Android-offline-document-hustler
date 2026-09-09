package com.example.ui.word

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WordBlue
import com.example.ui.theme.WordBorderGray
import com.example.ui.theme.WordRibbonGray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WordRightSideBar(
    state: WordFormatState,
    onStateChange: (WordFormatState) -> Unit,
    onInsertText: (String) -> Unit,
    wordCount: Int,
    charCount: Int,
    isAmoledMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showFontFamilyMenu by remember { mutableStateOf(false) }
    var showFontSizeMenu by remember { mutableStateOf(false) }
    var showFontColorMenu by remember { mutableStateOf(false) }
    var showHighlightMenu by remember { mutableStateOf(false) }
    var showMarginsMenu by remember { mutableStateOf(false) }
    var showOrientationMenu by remember { mutableStateOf(false) }

    val isExpanded = state.isRibbonExpanded

    Surface(
        color = if (isAmoledMode) Color(0xFF141414) else WordRibbonGray,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, WordBorderGray),
        shadowElevation = 3.dp,
        modifier = modifier
            .fillMaxHeight()
            .width(if (isExpanded) 172.dp else 44.dp)
            .testTag("word_right_side_bar")
    ) {
        if (!isExpanded) {
            // COMPACT VERTICAL RIGHT-EDGE TOOLBAR (When collapsed)
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Expand Button (Pointing Left into the screen to open)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = WordBlue,
                    modifier = Modifier
                        .size(34.dp)
                        .clickable { onStateChange(state.copy(isRibbonExpanded = true)) }
                        .testTag("btn_expand_right_sidebar")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Leiste aufklappen",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Quick Bold
                CompactRightToolButton(
                    label = "B",
                    isSelected = state.isBold,
                    fontWeight = FontWeight.Bold,
                    testTag = "btn_compact_bold",
                    onClick = { onStateChange(state.copy(isBold = !state.isBold)) }
                )

                // Quick Italic
                CompactRightToolButton(
                    label = "I",
                    isSelected = state.isItalic,
                    fontStyle = FontStyle.Italic,
                    testTag = "btn_compact_italic",
                    onClick = { onStateChange(state.copy(isItalic = !state.isItalic)) }
                )

                // Quick Underline
                CompactRightToolButton(
                    label = "U",
                    isSelected = state.isUnderline,
                    isUnderlined = true,
                    testTag = "btn_compact_underline",
                    onClick = { onStateChange(state.copy(isUnderline = !state.isUnderline)) }
                )

                // Quick Size Badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WordBorderGray),
                    color = Color.White,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onStateChange(state.copy(isRibbonExpanded = true)) }
                        .testTag("btn_compact_size")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${state.fontSizePt}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = WordBlue
                        )
                    }
                }

                // Quick Align Cycle Button
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WordBorderGray),
                    color = Color.White,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable {
                            val nextAlign = when (state.textAlign) {
                                TextAlign.Start -> TextAlign.Center
                                TextAlign.Center -> TextAlign.End
                                TextAlign.End -> TextAlign.Justify
                                else -> TextAlign.Start
                            }
                            onStateChange(state.copy(textAlign = nextAlign))
                        }
                        .testTag("btn_compact_align")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        AlignmentLinesIcon(textAlign = state.textAlign, isSelected = true)
                    }
                }

                // Quick Color Dot
                Surface(
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(1.dp, WordBorderGray),
                    color = state.fontColor,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onStateChange(state.copy(isRibbonExpanded = true)) }
                        .testTag("btn_compact_color")
                ) {}

                Spacer(modifier = Modifier.weight(1f))

                // Bottom 'A' icon to trigger full panel
                IconButton(
                    onClick = { onStateChange(state.copy(isRibbonExpanded = true)) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Text("A▾", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WordBlue)
                }
            }
        } else {
            // FULL EXPANDED RIGHT-SIDE EDITING & FORMATTING PANEL
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                // PANEL HEADER (Rechts am Rand)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = WordBlue,
                            modifier = Modifier.size(18.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("W", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Formatieren",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAmoledMode) Color.White else Color(0xFF1E293B)
                        )
                    }

                    // Collapse Arrow button (Pushes back to right edge)
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, WordBorderGray),
                        modifier = Modifier
                            .size(26.dp)
                            .clickable { onStateChange(state.copy(isRibbonExpanded = false)) }
                            .testTag("btn_collapse_right_sidebar")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Leiste minimieren",
                                tint = WordBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(WordBorderGray)
                )

                // SCROLLABLE CONTROLS SECTION
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. SCHRIFTART (Font Family)
                    SidebarSectionTitle("Schriftart")
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, WordBorderGray),
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showFontFamilyMenu = true }
                                .testTag("picker_font_family_right")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = state.fontFamilyName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1E293B),
                                    maxLines = 1
                                )
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }

                        DropdownMenu(
                            expanded = showFontFamilyMenu,
                            onDismissRequest = { showFontFamilyMenu = false }
                        ) {
                            WordFormatState.AVAILABLE_FONT_FAMILIES.forEach { family ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = family,
                                            fontWeight = if (family == state.fontFamilyName) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    trailingIcon = if (family == state.fontFamilyName) {
                                        { Icon(Icons.Default.Check, contentDescription = null, tint = WordBlue) }
                                    } else null,
                                    onClick = {
                                        onStateChange(state.copy(fontFamilyName = family))
                                        showFontFamilyMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // 2. SCHRIFTGRÖSSE (Font Size & Stepper)
                    SidebarSectionTitle("Schriftgröße")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Dropdown button for pt
                        Box(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, WordBorderGray),
                                color = Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showFontSizeMenu = true }
                                    .testTag("picker_font_size_right")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${state.fontSizePt} pt",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WordBlue
                                    )
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                }
                            }

                            DropdownMenu(
                                expanded = showFontSizeMenu,
                                onDismissRequest = { showFontSizeMenu = false }
                            ) {
                                WordFormatState.AVAILABLE_FONT_SIZES.forEach { size ->
                                    DropdownMenuItem(
                                        text = { Text("$size pt", fontWeight = if (size == state.fontSizePt) FontWeight.Bold else FontWeight.Normal) },
                                        trailingIcon = if (size == state.fontSizePt) {
                                            { Icon(Icons.Default.Check, contentDescription = null, tint = WordBlue) }
                                        } else null,
                                        onClick = {
                                            onStateChange(state.copy(fontSizePt = size))
                                            showFontSizeMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // A– and A+ steppers
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, WordBorderGray),
                            color = Color.White,
                            modifier = Modifier.height(30.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clickable {
                                            val newSize = (state.fontSizePt - 1).coerceAtLeast(6)
                                            onStateChange(state.copy(fontSizePt = newSize))
                                        }
                                        .padding(horizontal = 6.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("A–", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                                }
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(16.dp)
                                        .background(WordBorderGray)
                                )
                                Box(
                                    modifier = Modifier
                                        .clickable {
                                            val newSize = (state.fontSizePt + 1).coerceAtMost(72)
                                            onStateChange(state.copy(fontSizePt = newSize))
                                        }
                                        .padding(horizontal = 6.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("A+", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WordBlue)
                                }
                            }
                        }
                    }

                    // 3. AUSRICHTUNG (Linksbündig, Mittelbündig, Rechtsbündig, Blocksatz)
                    SidebarSectionTitle("Ausrichtung")
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            SidebarAlignButton(
                                label = "Links",
                                textAlign = TextAlign.Start,
                                isSelected = state.textAlign == TextAlign.Start,
                                modifier = Modifier.weight(1f),
                                testTag = "side_align_start",
                                onClick = { onStateChange(state.copy(textAlign = TextAlign.Start)) }
                            )
                            SidebarAlignButton(
                                label = "Mitte",
                                textAlign = TextAlign.Center,
                                isSelected = state.textAlign == TextAlign.Center,
                                modifier = Modifier.weight(1f),
                                testTag = "side_align_center",
                                onClick = { onStateChange(state.copy(textAlign = TextAlign.Center)) }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            SidebarAlignButton(
                                label = "Rechts",
                                textAlign = TextAlign.End,
                                isSelected = state.textAlign == TextAlign.End,
                                modifier = Modifier.weight(1f),
                                testTag = "side_align_end",
                                onClick = { onStateChange(state.copy(textAlign = TextAlign.End)) }
                            )
                            SidebarAlignButton(
                                label = "Block",
                                textAlign = TextAlign.Justify,
                                isSelected = state.textAlign == TextAlign.Justify,
                                modifier = Modifier.weight(1f),
                                testTag = "side_align_justify",
                                onClick = { onStateChange(state.copy(textAlign = TextAlign.Justify)) }
                            )
                        }
                    }

                    // 4. FORMATIERUNG (Fett B, Kursiv I, Unterstrichen U, Durchgestrichen abc)
                    SidebarSectionTitle("Formatierung")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SidebarFormatToggle(
                            label = "B",
                            isSelected = state.isBold,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            testTag = "side_format_bold",
                            onClick = { onStateChange(state.copy(isBold = !state.isBold)) }
                        )
                        SidebarFormatToggle(
                            label = "I",
                            isSelected = state.isItalic,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.weight(1f),
                            testTag = "side_format_italic",
                            onClick = { onStateChange(state.copy(isItalic = !state.isItalic)) }
                        )
                        SidebarFormatToggle(
                            label = "U",
                            isSelected = state.isUnderline,
                            isUnderlined = true,
                            modifier = Modifier.weight(1f),
                            testTag = "side_format_underline",
                            onClick = { onStateChange(state.copy(isUnderline = !state.isUnderline)) }
                        )
                        SidebarFormatToggle(
                            label = "abc",
                            isSelected = state.isStrikethrough,
                            isStrikethrough = true,
                            modifier = Modifier.weight(1f),
                            testTag = "side_format_strikethrough",
                            onClick = { onStateChange(state.copy(isStrikethrough = !state.isStrikethrough)) }
                        )
                    }

                    // 5. FARBEN & TEXTMARKER
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Schriftfarbe
                        Box(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, WordBorderGray),
                                color = Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(32.dp)
                                    .clickable { showFontColorMenu = true }
                                    .testTag("side_font_color")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("A", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = state.fontColor)
                                        Box(
                                            modifier = Modifier
                                                .width(14.dp)
                                                .height(2.5.dp)
                                                .background(state.fontColor, RoundedCornerShape(1.dp))
                                        )
                                    }
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                }
                            }

                            DropdownMenu(
                                expanded = showFontColorMenu,
                                onDismissRequest = { showFontColorMenu = false }
                            ) {
                                WordFormatState.WORD_TEXT_COLORS.forEach { (col, name) ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(16.dp)
                                                        .clip(CircleShape)
                                                        .background(col)
                                                        .border(0.5.dp, Color.Gray, CircleShape)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(name, fontWeight = if (col == state.fontColor) FontWeight.Bold else FontWeight.Normal)
                                            }
                                        },
                                        trailingIcon = if (col == state.fontColor) {
                                            { Icon(Icons.Default.Check, contentDescription = null, tint = WordBlue) }
                                        } else null,
                                        onClick = {
                                            onStateChange(state.copy(fontColor = col))
                                            showFontColorMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Textmarker
                        Box(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, WordBorderGray),
                                color = Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(32.dp)
                                    .clickable { showHighlightMenu = true }
                                    .testTag("side_highlight")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Marker", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                        Box(
                                            modifier = Modifier
                                                .width(16.dp)
                                                .height(2.5.dp)
                                                .background(
                                                    if (state.highlightColor != Color.Transparent) state.highlightColor else Color.Yellow,
                                                    RoundedCornerShape(1.dp)
                                                )
                                        )
                                    }
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                }
                            }

                            DropdownMenu(
                                expanded = showHighlightMenu,
                                onDismissRequest = { showHighlightMenu = false }
                            ) {
                                WordFormatState.WORD_HIGHLIGHT_COLORS.forEach { (col, name) ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(16.dp)
                                                        .clip(CircleShape)
                                                        .background(if (col == Color.Transparent) Color.White else col)
                                                        .border(0.5.dp, Color.Gray, CircleShape)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(name, fontWeight = if (col == state.highlightColor) FontWeight.Bold else FontWeight.Normal)
                                            }
                                        },
                                        trailingIcon = if (col == state.highlightColor) {
                                            { Icon(Icons.Default.Check, contentDescription = null, tint = WordBlue) }
                                        } else null,
                                        onClick = {
                                            onStateChange(state.copy(highlightColor = col))
                                            showHighlightMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 6. LISTEN
                    SidebarSectionTitle("Listen")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SidebarFormatToggle(
                            label = "• Liste",
                            isSelected = state.isBulletList,
                            modifier = Modifier.weight(1f),
                            testTag = "side_list_bullets",
                            onClick = {
                                val next = !state.isBulletList
                                onStateChange(state.copy(isBulletList = next, isNumberedList = if (next) false else state.isNumberedList))
                                if (next) onInsertText("\n• ")
                            }
                        )

                        SidebarFormatToggle(
                            label = "1. Liste",
                            isSelected = state.isNumberedList,
                            modifier = Modifier.weight(1f),
                            testTag = "side_list_numbers",
                            onClick = {
                                val next = !state.isNumberedList
                                onStateChange(state.copy(isNumberedList = next, isBulletList = if (next) false else state.isBulletList))
                                if (next) onInsertText("\n1. ")
                            }
                        )
                    }

                    // 7. WEITERE OPTIONEN (Einfügen & Layout)
                    SidebarSectionTitle("Einfügen & Extras")
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WordBorderGray),
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onInsertText("\n| Spalte 1 | Spalte 2 |\n|---|---|\n| Inhalt | Inhalt |\n")
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("+ Tabelle", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = WordBlue)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WordBorderGray),
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onInsertText("\n────────────────────────\n")
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("+ Trennlinie", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = WordBlue)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WordBorderGray),
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val dateStr = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY).format(Date())
                                onInsertText(" [$dateStr] ")
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("+ Datum / Uhrzeit", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = WordBlue)
                        }
                    }

                    // STATUS: WÖRTER & ZEICHEN
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(6.dp))
                            .border(0.5.dp, WordBorderGray, RoundedCornerShape(6.dp))
                            .padding(6.dp)
                    ) {
                        Column {
                            Text("Statistik", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text("$wordCount Wörter", fontSize = 10.sp, color = Color(0xFF333333))
                            Text("$charCount Zeichen", fontSize = 10.sp, color = Color(0xFF333333))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun SidebarSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF555555),
        modifier = Modifier.padding(top = 2.dp)
    )
}

@Composable
private fun SidebarAlignButton(
    label: String,
    textAlign: TextAlign,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    testTag: String = "",
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) WordBlue else WordBorderGray
        ),
        color = if (isSelected) WordBlue.copy(alpha = 0.12f) else Color.White,
        modifier = modifier
            .height(30.dp)
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            AlignmentLinesIcon(textAlign = textAlign, isSelected = isSelected)
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) WordBlue else Color(0xFF333333)
            )
        }
    }
}

@Composable
private fun SidebarFormatToggle(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight = FontWeight.Normal,
    fontStyle: FontStyle = FontStyle.Normal,
    isUnderlined: Boolean = false,
    isStrikethrough: Boolean = false,
    testTag: String = "",
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) WordBlue else WordBorderGray
        ),
        color = if (isSelected) WordBlue.copy(alpha = 0.12f) else Color.White,
        modifier = modifier
            .height(30.dp)
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else fontWeight,
                fontStyle = fontStyle,
                color = if (isSelected) WordBlue else Color(0xFF333333),
                textDecoration = when {
                    isUnderlined -> androidx.compose.ui.text.style.TextDecoration.Underline
                    isStrikethrough -> androidx.compose.ui.text.style.TextDecoration.LineThrough
                    else -> androidx.compose.ui.text.style.TextDecoration.None
                }
            )
        }
    }
}

@Composable
private fun CompactRightToolButton(
    label: String,
    isSelected: Boolean,
    fontWeight: FontWeight = FontWeight.Normal,
    fontStyle: FontStyle = FontStyle.Normal,
    isUnderlined: Boolean = false,
    testTag: String = "",
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) WordBlue else WordBorderGray
        ),
        color = if (isSelected) WordBlue.copy(alpha = 0.12f) else Color.White,
        modifier = Modifier
            .size(32.dp)
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else fontWeight,
                fontStyle = fontStyle,
                color = if (isSelected) WordBlue else Color(0xFF333333),
                textDecoration = if (isUnderlined) androidx.compose.ui.text.style.TextDecoration.Underline else androidx.compose.ui.text.style.TextDecoration.None
            )
        }
    }
}
