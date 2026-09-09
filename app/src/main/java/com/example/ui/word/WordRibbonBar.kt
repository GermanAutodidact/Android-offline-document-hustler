package com.example.ui.word

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WordBlue
import com.example.ui.theme.WordBorderGray
import com.example.ui.theme.WordRibbonGray

@Composable
fun WordRibbonBar(
    state: WordFormatState,
    onStateChange: (WordFormatState) -> Unit,
    onInsertText: (String) -> Unit,
    wordCount: Int,
    charCount: Int,
    modifier: Modifier = Modifier
) {
    var showFontFamilyMenu by remember { mutableStateOf(false) }
    var showFontSizeMenu by remember { mutableStateOf(false) }
    var showFontColorMenu by remember { mutableStateOf(false) }
    var showHighlightMenu by remember { mutableStateOf(false) }
    var showMarginsMenu by remember { mutableStateOf(false) }
    var showPaperSizeMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(WordRibbonGray)
            .border(width = 0.5.dp, color = WordBorderGray)
    ) {
        // PERMANENT TWO-TIER WORD MOBILE FORMATTING BAR (Always visible!)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(WordRibbonGray)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // ROW 1: SCHRIFTART, SCHRIFTGRÖSSE, +/- STEPPER, UND AUSRICHTUNG (Links, Mitte, Rechts, Blocksatz)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 1. Schriftart Dropdown (Aptos, Calibri, Arial, ...)
                Box {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WordBorderGray),
                        color = Color.White,
                        modifier = Modifier
                            .clickable { showFontFamilyMenu = true }
                            .testTag("picker_font_family_quick")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = state.fontFamilyName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
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

                // 2. Schriftgröße Dropdown (8 pt, 11 pt, 12 pt, 14 pt, ...)
                Box {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WordBorderGray),
                        color = Color.White,
                        modifier = Modifier
                            .clickable { showFontSizeMenu = true }
                            .testTag("picker_font_size_quick")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${state.fontSizePt} pt",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = WordBlue
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
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

                // 3. Schnell-Schritt-Tasten: A– (kleiner) und A+ (größer)
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
                                .padding(horizontal = 8.dp, vertical = 4.dp),
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
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("A+", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WordBlue)
                        }
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))
                Box(modifier = Modifier.width(1.dp).height(20.dp).background(WordBorderGray))
                Spacer(modifier = Modifier.width(4.dp))

                // 4. AUSRICHTUNG (Linksbündig, Zentriert / Mittelbündig, Rechtsbündig, Blocksatz)
                AlignmentOptionButton(
                    label = "Links",
                    textAlign = TextAlign.Start,
                    isSelected = state.textAlign == TextAlign.Start,
                    testTag = "btn_align_start",
                    onClick = { onStateChange(state.copy(textAlign = TextAlign.Start)) }
                )

                AlignmentOptionButton(
                    label = "Mitte",
                    textAlign = TextAlign.Center,
                    isSelected = state.textAlign == TextAlign.Center,
                    testTag = "btn_align_center",
                    onClick = { onStateChange(state.copy(textAlign = TextAlign.Center)) }
                )

                AlignmentOptionButton(
                    label = "Rechts",
                    textAlign = TextAlign.End,
                    isSelected = state.textAlign == TextAlign.End,
                    testTag = "btn_align_end",
                    onClick = { onStateChange(state.copy(textAlign = TextAlign.End)) }
                )

                AlignmentOptionButton(
                    label = "Block",
                    textAlign = TextAlign.Justify,
                    isSelected = state.textAlign == TextAlign.Justify,
                    testTag = "btn_align_justify",
                    onClick = { onStateChange(state.copy(textAlign = TextAlign.Justify)) }
                )
            }

            // ROW 2: FORMATIERUNG (Fett B, Kursiv I, Unterstrichen U, Durchgestrichen abc, Farbe, Marker, Listen, Menüband-Button)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Fett (B)
                FormatToggleButton(
                    label = "B",
                    isSelected = state.isBold,
                    fontWeight = FontWeight.Bold,
                    testTag = "btn_quick_bold",
                    onClick = { onStateChange(state.copy(isBold = !state.isBold)) }
                )

                // Kursiv (I)
                FormatToggleButton(
                    label = "I",
                    isSelected = state.isItalic,
                    fontStyle = FontStyle.Italic,
                    testTag = "btn_quick_italic",
                    onClick = { onStateChange(state.copy(isItalic = !state.isItalic)) }
                )

                // Unterstrichen (U)
                FormatToggleButton(
                    label = "U",
                    isSelected = state.isUnderline,
                    textDecoration = TextDecoration.Underline,
                    testTag = "btn_quick_underline",
                    onClick = { onStateChange(state.copy(isUnderline = !state.isUnderline)) }
                )

                // Durchgestrichen (abc)
                FormatToggleButton(
                    label = "abc",
                    isSelected = state.isStrikethrough,
                    textDecoration = TextDecoration.LineThrough,
                    testTag = "btn_quick_strikethrough",
                    onClick = { onStateChange(state.copy(isStrikethrough = !state.isStrikethrough)) }
                )

                // Schriftfarbe mit Dropdown
                Box {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, WordBorderGray),
                        modifier = Modifier
                            .height(30.dp)
                            .clickable { showFontColorMenu = true }
                            .testTag("btn_quick_color")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("A", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = state.fontColor)
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

                // Textmarker mit Dropdown
                Box {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, WordBorderGray),
                        modifier = Modifier
                            .height(30.dp)
                            .clickable { showHighlightMenu = true }
                            .testTag("btn_quick_highlight")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
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

                // Aufzählungsliste (•)
                FormatToggleButton(
                    label = "• Liste",
                    isSelected = state.isBulletList,
                    testTag = "btn_quick_bullet",
                    onClick = {
                        onStateChange(
                            state.copy(
                                isBulletList = !state.isBulletList,
                                isNumberedList = false
                            )
                        )
                    }
                )

                // Nummerierte Liste (1.)
                FormatToggleButton(
                    label = "1. Liste",
                    isSelected = state.isNumberedList,
                    testTag = "btn_quick_number",
                    onClick = {
                        onStateChange(
                            state.copy(
                                isNumberedList = !state.isNumberedList,
                                isBulletList = false
                            )
                        )
                    }
                )

                Spacer(modifier = Modifier.width(4.dp))

                // MENÜBAND ERWEITERN / EINKLAPPEN (Iconic Word Mobile Ribbon Button)
                Surface(
                    color = if (state.isRibbonExpanded) WordBlue else Color.White,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (state.isRibbonExpanded) WordBlue else WordBorderGray),
                    modifier = Modifier
                        .height(30.dp)
                        .clickable { onStateChange(state.copy(isRibbonExpanded = !state.isRibbonExpanded)) }
                        .testTag("btn_toggle_ribbon")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (state.isRibbonExpanded) "Menüband ▲" else "Menüband ▼",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (state.isRibbonExpanded) Color.White else WordBlue
                        )
                    }
                }
            }
        }

        // EXPANDED WORD MOBILE RIBBON DRAWER
        AnimatedVisibility(
            visible = state.isRibbonExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .height(280.dp)
            ) {
                // RIBBON TAB BAR: [Start] | [Einfügen] | [Layout] | [Ansicht]
                Surface(
                    color = WordRibbonGray,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            WordRibbonTab.values().forEach { tab ->
                                val isSelected = state.activeRibbonTab == tab
                                Box(
                                    modifier = Modifier
                                        .clickable { onStateChange(state.copy(activeRibbonTab = tab)) }
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                        .testTag("tab_${tab.name.lowercase()}")
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = tab.title,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) WordBlue else Color(0xFF555555)
                                        )
                                        if (isSelected) {
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Box(
                                                modifier = Modifier
                                                    .width(28.dp)
                                                    .height(2.5.dp)
                                                    .background(WordBlue, RoundedCornerShape(1.dp))
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Close ribbon button
                        IconButton(
                            onClick = { onStateChange(state.copy(isRibbonExpanded = false)) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Schließen", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                HorizontalDivider(thickness = 0.5.dp, color = WordBorderGray)

                // TAB CONTENT CONTAINER
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    when (state.activeRibbonTab) {
                        WordRibbonTab.START -> {
                            // 1. SCHRIFTART & SCHRIFTGRÖSSE DROPDOWNS & STEPPER
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Schriftart Selector (Calibri, Aptos, Arial...)
                                Box(modifier = Modifier.weight(1.4f)) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, WordBorderGray),
                                        color = Color.White,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { showFontFamilyMenu = true }
                                            .testTag("picker_font_family")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = state.fontFamilyName,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF1E293B)
                                            )
                                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
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

                                // Schriftgröße Stepper [-] [pt] [+] & Dropdown
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    // Stepper minus
                                    Surface(
                                        shape = RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, WordBorderGray),
                                        color = WordRibbonGray,
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                val newSize = (state.fontSizePt - 1).coerceAtLeast(6)
                                                onStateChange(state.copy(fontSizePt = newSize))
                                            },
                                            modifier = Modifier.testTag("btn_size_minus")
                                        ) {
                                            Text("-", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // Size Display & Dropdown
                                    Box(modifier = Modifier.weight(1f)) {
                                        Surface(
                                            border = androidx.compose.foundation.BorderStroke(1.dp, WordBorderGray),
                                            color = Color.White,
                                            modifier = Modifier
                                                .height(34.dp)
                                                .fillMaxWidth()
                                                .clickable { showFontSizeMenu = true }
                                                .testTag("picker_font_size")
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "${state.fontSizePt}",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = WordBlue
                                                )
                                            }
                                        }

                                        DropdownMenu(
                                            expanded = showFontSizeMenu,
                                            onDismissRequest = { showFontSizeMenu = false }
                                        ) {
                                            WordFormatState.AVAILABLE_FONT_SIZES.forEach { size ->
                                                DropdownMenuItem(
                                                    text = { Text("$size pt", fontWeight = if (size == state.fontSizePt) FontWeight.Bold else FontWeight.Normal) },
                                                    onClick = {
                                                        onStateChange(state.copy(fontSizePt = size))
                                                        showFontSizeMenu = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    // Stepper plus
                                    Surface(
                                        shape = RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, WordBorderGray),
                                        color = WordRibbonGray,
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                val newSize = (state.fontSizePt + 1).coerceAtMost(72)
                                                onStateChange(state.copy(fontSizePt = newSize))
                                            },
                                            modifier = Modifier.testTag("btn_size_plus")
                                        ) {
                                            Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 2. SCHRIFTFARBEN-PALETTE (Word-Palette)
                            Text("Schriftfarbe", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF666666))
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                WordFormatState.WORD_TEXT_COLORS.forEach { (color, name) ->
                                    val isSelected = state.fontColor == color
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .border(
                                                width = if (isSelected) 2.5.dp else 1.dp,
                                                color = if (isSelected) WordBlue else WordBorderGray,
                                                shape = CircleShape
                                            )
                                            .clickable { onStateChange(state.copy(fontColor = color)) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, contentDescription = name, tint = if (color == Color(0xFF000000)) Color.White else Color.White, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 3. TEXTMARKER-PALETTE (Hervorhebung)
                            Text("Texthervorhebung (Marker)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF666666))
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                WordFormatState.WORD_HIGHLIGHT_COLORS.forEach { (color, name) ->
                                    val isSelected = state.highlightColor == color
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(if (color == Color.Transparent) Color.White else color)
                                            .border(
                                                width = if (isSelected) 2.5.dp else 1.dp,
                                                color = if (isSelected) WordBlue else WordBorderGray,
                                                shape = CircleShape
                                            )
                                            .clickable { onStateChange(state.copy(highlightColor = color)) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (color == Color.Transparent) {
                                            Icon(Icons.Default.Close, contentDescription = "Keine", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                        } else if (isSelected) {
                                            Icon(Icons.Default.Check, contentDescription = name, tint = Color.Black, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 4. FORMATVORLAGEN (Styles)
                            Text("Formatvorlagen", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF666666))
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                WordFormatState.AVAILABLE_STYLES.forEach { style ->
                                    val isSelected = state.currentStyle == style
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) WordBlue else WordBorderGray
                                        ),
                                        color = if (isSelected) WordRibbonGray else Color.White,
                                        modifier = Modifier
                                            .clickable {
                                                val (size, bold, color) = when (style) {
                                                    "Titel" -> Triple(24, true, WordBlue)
                                                    "Überschrift 1" -> Triple(18, true, WordBlue)
                                                    "Überschrift 2" -> Triple(14, true, Color(0xFF2E74B5))
                                                    "Untertitel" -> Triple(12, false, Color(0xFF595959))
                                                    "Zitat" -> Triple(11, false, Color(0xFF595959))
                                                    else -> Triple(11, false, Color(0xFF000000))
                                                }
                                                onStateChange(
                                                    state.copy(
                                                        currentStyle = style,
                                                        fontSizePt = size,
                                                        isBold = bold,
                                                        fontColor = color
                                                    )
                                                )
                                            }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = "AaBbCc",
                                                fontSize = when (style) {
                                                    "Titel" -> 16.sp
                                                    "Überschrift 1" -> 14.sp
                                                    else -> 12.sp
                                                },
                                                fontWeight = if (style.startsWith("Über") || style == "Titel") FontWeight.Bold else FontWeight.Normal,
                                                color = if (style.startsWith("Über") || style == "Titel") WordBlue else Color.Black
                                            )
                                            Text(text = style, fontSize = 10.sp, color = Color.Gray)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 5. ABSATZAUSRICHTUNG (Ausrichtung & Einzug)
                            Text("Absatzausrichtung", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF666666))
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AlignmentOptionButton(
                                    label = "Linksbündig",
                                    textAlign = TextAlign.Start,
                                    isSelected = state.textAlign == TextAlign.Start,
                                    modifier = Modifier.weight(1f),
                                    testTag = "ribbon_align_start",
                                    onClick = { onStateChange(state.copy(textAlign = TextAlign.Start)) }
                                )
                                AlignmentOptionButton(
                                    label = "Zentriert",
                                    textAlign = TextAlign.Center,
                                    isSelected = state.textAlign == TextAlign.Center,
                                    modifier = Modifier.weight(1f),
                                    testTag = "ribbon_align_center",
                                    onClick = { onStateChange(state.copy(textAlign = TextAlign.Center)) }
                                )
                                AlignmentOptionButton(
                                    label = "Rechtsbündig",
                                    textAlign = TextAlign.End,
                                    isSelected = state.textAlign == TextAlign.End,
                                    modifier = Modifier.weight(1f),
                                    testTag = "ribbon_align_end",
                                    onClick = { onStateChange(state.copy(textAlign = TextAlign.End)) }
                                )
                                AlignmentOptionButton(
                                    label = "Blocksatz",
                                    textAlign = TextAlign.Justify,
                                    isSelected = state.textAlign == TextAlign.Justify,
                                    modifier = Modifier.weight(1f),
                                    testTag = "ribbon_align_justify",
                                    onClick = { onStateChange(state.copy(textAlign = TextAlign.Justify)) }
                                )
                            }
                        }

                        WordRibbonTab.EINFUEGEN -> {
                            Text("Einfügen in das Dokument", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF555555))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                InsertTile(
                                    title = "Tabelle",
                                    subtitle = "3 × 3 Raster",
                                    icon = Icons.Default.Add,
                                    onClick = {
                                        onInsertText("\n| Spalte 1 | Spalte 2 | Spalte 3 |\n| :--- | :--- | :--- |\n| Zelle 1 | Zelle 2 | Zelle 3 |\n| Zelle 4 | Zelle 5 | Zelle 6 |\n\n")
                                    }
                                )

                                InsertTile(
                                    title = "Trennlinie",
                                    subtitle = "Horizontale Linie",
                                    icon = Icons.Default.DateRange,
                                    onClick = {
                                        onInsertText("\n---\n\n")
                                    }
                                )

                                InsertTile(
                                    title = "Datum & Zeit",
                                    subtitle = "Aktueller Zeitstempel",
                                    icon = Icons.Default.DateRange,
                                    onClick = {
                                        val now = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.GERMAN).format(java.util.Date())
                                        onInsertText(" $now ")
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                InsertTile(
                                    title = "Seitenumbruch",
                                    subtitle = "Neue Druckseite",
                                    icon = Icons.Default.KeyboardArrowDown,
                                    onClick = {
                                        onInsertText("\n\n=== SEITENUMBRUCH ===\n\n")
                                    }
                                )

                                InsertTile(
                                    title = "Aufzählung",
                                    subtitle = "Spiegelstriche",
                                    icon = Icons.Default.Add,
                                    onClick = {
                                        onInsertText("\n• Punkt 1\n• Punkt 2\n• Punkt 3\n\n")
                                    }
                                )
                            }
                        }

                        WordRibbonTab.LAYOUT -> {
                            Text("Seiten- & Drucklayout", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF555555))
                            Spacer(modifier = Modifier.height(8.dp))

                            // Ausrichtung Hoch / Quer
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Ausrichtung:", fontSize = 13.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    FilterChip(
                                        selected = state.pageOrientation == "Hochformat",
                                        onClick = { onStateChange(state.copy(pageOrientation = "Hochformat")) },
                                        label = { Text("Hochformat", fontSize = 11.sp) }
                                    )
                                    FilterChip(
                                        selected = state.pageOrientation == "Querformat",
                                        onClick = { onStateChange(state.copy(pageOrientation = "Querformat")) },
                                        label = { Text("Querformat", fontSize = 11.sp) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Seitenränder
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Seitenränder:", fontSize = 13.sp)
                                Box {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, WordBorderGray),
                                        modifier = Modifier.clickable { showMarginsMenu = true }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(state.pageMargins, fontSize = 11.sp)
                                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    DropdownMenu(
                                        expanded = showMarginsMenu,
                                        onDismissRequest = { showMarginsMenu = false }
                                    ) {
                                        listOf("Normal (2,5 cm)", "Schmal (1,27 cm)", "Breit (3,18 cm)").forEach { m ->
                                            DropdownMenuItem(
                                                text = { Text(m) },
                                                onClick = {
                                                    onStateChange(state.copy(pageMargins = m))
                                                    showMarginsMenu = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Papiergröße
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Papiergröße:", fontSize = 13.sp)
                                Box {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, WordBorderGray),
                                        modifier = Modifier.clickable { showPaperSizeMenu = true }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(state.pageSize, fontSize = 11.sp)
                                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    DropdownMenu(
                                        expanded = showPaperSizeMenu,
                                        onDismissRequest = { showPaperSizeMenu = false }
                                    ) {
                                        listOf("A4 (210 × 297 mm)", "Letter (216 × 279 mm)", "A5 (148 × 210 mm)").forEach { p ->
                                            DropdownMenuItem(
                                                text = { Text(p) },
                                                onClick = {
                                                    onStateChange(state.copy(pageSize = p))
                                                    showPaperSizeMenu = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        WordRibbonTab.ANSICHT -> {
                            Text("Ansichtsmodi & Dokumentstatistik", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF555555))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = state.isPrintLayoutView,
                                    onClick = { onStateChange(state.copy(isPrintLayoutView = true)) },
                                    label = { Text("Drucklayout (A4)", fontSize = 12.sp) }
                                )
                                FilterChip(
                                    selected = !state.isPrintLayoutView,
                                    onClick = { onStateChange(state.copy(isPrintLayoutView = false)) },
                                    label = { Text("Mobilansicht", fontSize = 12.sp) }
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = WordRibbonGray,
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, WordBorderGray),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Dokument-Statistik:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WordBlue)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Wörter: $wordCount", fontSize = 12.sp)
                                    Text("Zeichen (mit Leerzeichen): $charCount", fontSize = 12.sp)
                                    Text("Seiten: 1 von 1 (DIN A4)", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormatToggleButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    fontWeight: FontWeight = FontWeight.Normal,
    fontStyle: FontStyle = FontStyle.Normal,
    textDecoration: TextDecoration = TextDecoration.None,
    testTag: String = ""
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isSelected) WordBlue else Color.Transparent,
        modifier = Modifier
            .size(34.dp)
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = fontWeight,
                fontStyle = fontStyle,
                textDecoration = textDecoration,
                color = if (isSelected) Color.White else Color(0xFF222222)
            )
        }
    }
}

@Composable
private fun InsertTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, WordBorderGray),
        color = Color.White,
        modifier = Modifier
            .clickable { onClick() }
            .padding(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = WordBlue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            }
            Text(text = subtitle, fontSize = 10.sp, color = Color.Gray)
        }
    }
}

@Composable
fun AlignmentOptionButton(
    label: String,
    textAlign: TextAlign,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
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
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            AlignmentLinesIcon(textAlign = textAlign, isSelected = isSelected)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) WordBlue else Color(0xFF333333)
            )
        }
    }
}

@Composable
fun AlignmentLinesIcon(textAlign: TextAlign, isSelected: Boolean) {
    val barColor = if (isSelected) WordBlue else Color(0xFF555555)
    Column(
        modifier = Modifier.size(14.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = when (textAlign) {
            TextAlign.Start -> Alignment.Start
            TextAlign.Center -> Alignment.CenterHorizontally
            TextAlign.End -> Alignment.End
            else -> Alignment.Start
        }
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(1.8.dp).background(barColor, RoundedCornerShape(1.dp)))
        Box(modifier = Modifier.fillMaxWidth(if (textAlign == TextAlign.Justify) 1f else 0.65f).height(1.8.dp).background(barColor, RoundedCornerShape(1.dp)))
        Box(modifier = Modifier.fillMaxWidth().height(1.8.dp).background(barColor, RoundedCornerShape(1.dp)))
        Box(modifier = Modifier.fillMaxWidth(if (textAlign == TextAlign.Justify) 1f else 0.45f).height(1.8.dp).background(barColor, RoundedCornerShape(1.dp)))
    }
}

