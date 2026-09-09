package com.example.ui.word

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.dialogs.SettingsDialog
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.DocumentFormat
import com.example.model.DocumentMetadata
import com.example.model.TextCommand
import com.example.model.UndoRedoManager
import com.example.ui.theme.WordBlue
import com.example.ui.theme.WordBorderGray
import com.example.ui.theme.WordDeskBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordDocumentEditor(
    metadata: DocumentMetadata,
    initialContent: String,
    onContentChanged: (String) -> Unit,
    onSave: () -> Unit,
    onSaveAs: () -> Unit,
    onClose: () -> Unit,
    onPrint: () -> Unit,
    onShowIntegrity: () -> Unit,
    onShowKnoxVault: () -> Unit,
    onToggleAmoled: () -> Unit,
    isAmoledMode: Boolean,
    modifier: Modifier = Modifier
) {
    var content by remember(initialContent) { mutableStateOf(initialContent) }
    var formatState by remember { mutableStateOf(WordFormatState()) }
    val undoRedoManager = remember { UndoRedoManager() }

    var showSearchDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showMoreMenu by remember { mutableStateOf(false) }
    var isRenamingDoc by remember { mutableStateOf(false) }
    var documentTitle by remember(metadata.name) { mutableStateOf(metadata.name) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val wordCount by remember {
        derivedStateOf {
            if (content.isBlank()) 0 else content.split(Regex("\\s+")).count { it.isNotBlank() }
        }
    }
    val charCount by remember {
        derivedStateOf { content.length }
    }

    val haptic = LocalHapticFeedback.current

    fun updateText(newText: String) {
        if (newText != content) {
            // Tactile feedback on character modification / typing
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            undoRedoManager.pushCommand(TextCommand.Replace(0, content, newText))
            content = newText
            onContentChanged(newText)
        }
    }

    fun handleUndo() {
        if (undoRedoManager.canUndo) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            val prev = undoRedoManager.undo(content)
            content = prev
            onContentChanged(prev)
        }
    }

    fun handleRedo() {
        if (undoRedoManager.canRedo) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            val next = undoRedoManager.redo(content)
            content = next
            onContentChanged(next)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (isAmoledMode) Color.Black else WordDeskBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // 1. MICROSOFT WORD MOBILE SIGNATURE BLUE TOP BAR
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = if (isAmoledMode) Color.Black else WordBlue,
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White,
                actionIconContentColor = Color.White
            ),
            navigationIcon = {
                // Checkmark / Back button (Word Mobile Save & Exit)
                IconButton(
                    onClick = {
                        onSave()
                        onClose()
                    },
                    modifier = Modifier.testTag("btn_word_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Speichern und Schließen",
                        tint = Color.White
                    )
                }
            },
            title = {
                Column(
                    modifier = Modifier
                        .clickable { isRenamingDoc = true }
                        .padding(vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = documentTitle,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        // Word .docx badge
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "DOCX",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = if (metadata.isDirty) "Geändert · Zum Speichern tippen" else "Gespeichert",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            },
            actions = {
                // Search Lupe
                IconButton(
                    onClick = { showSearchDialog = !showSearchDialog },
                    modifier = Modifier.size(38.dp).testTag("btn_word_search")
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Suchen", tint = Color.White)
                }

                // Edit / Reading Mode Toggle (Stift-Symbol)
                IconButton(
                    onClick = { formatState = formatState.copy(isReadingMode = !formatState.isReadingMode) },
                    modifier = Modifier.size(38.dp).testTag("btn_word_edit_mode")
                ) {
                    Icon(
                        imageVector = if (formatState.isReadingMode) Icons.Default.Edit else Icons.Default.Check,
                        contentDescription = if (formatState.isReadingMode) "Bearbeiten" else "Lesemodus",
                        tint = Color.White
                    )
                }

                // Word Mobile 'A' Formatierung & Leiste rechts umschalten
                IconButton(
                    onClick = { formatState = formatState.copy(isRibbonExpanded = !formatState.isRibbonExpanded) },
                    modifier = Modifier.size(38.dp).testTag("btn_word_format_toggle")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "A",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (formatState.isRibbonExpanded) Color.Yellow else Color.White
                        )
                        Icon(
                            imageVector = if (formatState.isRibbonExpanded) Icons.AutoMirrored.Filled.ArrowForward else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Rechte Einstellungsleiste umschalten",
                            tint = if (formatState.isRibbonExpanded) Color.Yellow else Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Mobile View vs Print Layout View Toggle (A4-Symbol)
                IconButton(
                    onClick = { formatState = formatState.copy(isPrintLayoutView = !formatState.isPrintLayoutView) },
                    modifier = Modifier.size(38.dp).testTag("btn_word_view_toggle")
                ) {
                    Icon(
                        painter = painterResource(id = if (formatState.isPrintLayoutView) R.drawable.ic_doc else R.drawable.ic_preview),
                        contentDescription = if (formatState.isPrintLayoutView) "Zu Mobilansicht" else "Zu Drucklayout",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Undo
                IconButton(
                    onClick = { handleUndo() },
                    enabled = undoRedoManager.canUndo,
                    modifier = Modifier.size(36.dp).testTag("btn_word_undo")
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_undo),
                        contentDescription = "Rückgängig",
                        tint = if (undoRedoManager.canUndo) Color.White else Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Redo
                IconButton(
                    onClick = { handleRedo() },
                    enabled = undoRedoManager.canRedo,
                    modifier = Modifier.size(36.dp).testTag("btn_word_redo")
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_redo),
                        contentDescription = "Wiederholen",
                        tint = if (undoRedoManager.canRedo) Color.White else Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Settings Gear (Zahnrad für Einstellungen)
                IconButton(
                    onClick = { showSettingsDialog = true },
                    modifier = Modifier.size(38.dp).testTag("btn_word_settings")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Einstellungen",
                        tint = Color.White
                    )
                }

                // Overflow Menu
                Box {
                    IconButton(
                        onClick = { showMoreMenu = true },
                        modifier = Modifier.size(38.dp).testTag("btn_word_overflow")
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Optionen", tint = Color.White)
                    }

                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Einstellungen ⚙️") },
                            onClick = {
                                showSettingsDialog = true
                                showMoreMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Speichern (Word)") },
                            onClick = {
                                onSave()
                                showMoreMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Speichern unter...") },
                            onClick = {
                                onSaveAs()
                                showMoreMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Drucken / Als PDF exportieren") },
                            onClick = {
                                onPrint()
                                showMoreMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Samsung Knox Hardware-Tresor") },
                            onClick = {
                                onShowKnoxVault()
                                showMoreMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("SHA-256 Integritätsprüfung") },
                            onClick = {
                                onShowIntegrity()
                                showMoreMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (isAmoledMode) "Super-AMOLED: Aus" else "Super-AMOLED: Ein") },
                            onClick = {
                                onToggleAmoled()
                                showMoreMenu = false
                            }
                        )
                    }
                }
            }
        )

        // Search Bar in-page overlay
        AnimatedVisibility(visible = showSearchDialog) {
            Surface(
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = WordBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = TextStyle(fontSize = 14.sp, color = Color.Black),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text("Im Dokument suchen...", fontSize = 14.sp, color = Color.Gray)
                            }
                            innerTextField()
                        }
                    )
                    IconButton(onClick = { showSearchDialog = false }) {
                        Icon(Icons.Default.Check, contentDescription = "Fertig", tint = WordBlue)
                    }
                }
            }
        }

        // 2. MAIN WORKSPACE (Document Canvas on the Left, Editing & Formatting Toolbar on the RIGHT edge)
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // DOCUMENT CANVAS (Left)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(if (isAmoledMode) Color.Black else WordDeskBackground),
                contentAlignment = Alignment.TopCenter
            ) {
                val scrollState = rememberScrollState()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(
                            if (formatState.isPrintLayoutView) 12.dp else 4.dp
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // THE WORD A4 SHEET
                    Surface(
                        shape = if (formatState.isPrintLayoutView) RoundedCornerShape(2.dp) else RoundedCornerShape(0.dp),
                        color = if (isAmoledMode) Color(0xFF111111) else Color.White,
                        shadowElevation = if (formatState.isPrintLayoutView) 4.dp else 0.dp,
                        border = if (formatState.isPrintLayoutView) androidx.compose.foundation.BorderStroke(0.5.dp, WordBorderGray) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 680.dp)
                            .padding(bottom = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = if (formatState.isPrintLayoutView) 18.dp else 12.dp,
                                    vertical = if (formatState.isPrintLayoutView) 20.dp else 12.dp
                                )
                        ) {
                            // THE EDITABLE RICH TEXT AREA (Pure document text surface, zero header / footer overlays)
                            val targetTextStyle = remember(formatState, isAmoledMode) {
                                TextStyle(
                                    fontFamily = formatState.composeFontFamily,
                                    fontSize = formatState.fontSizePt.sp,
                                    fontWeight = if (formatState.isBold) FontWeight.Bold else FontWeight.Normal,
                                    fontStyle = if (formatState.isItalic) FontStyle.Italic else FontStyle.Normal,
                                    textDecoration = when {
                                        formatState.isUnderline && formatState.isStrikethrough -> TextDecoration.combine(listOf(TextDecoration.Underline, TextDecoration.LineThrough))
                                        formatState.isUnderline -> TextDecoration.Underline
                                        formatState.isStrikethrough -> TextDecoration.LineThrough
                                        else -> TextDecoration.None
                                    },
                                    color = if (isAmoledMode && formatState.fontColor == Color(0xFF000000)) Color.White else formatState.fontColor,
                                    background = formatState.highlightColor,
                                    textAlign = formatState.textAlign,
                                    lineHeight = (formatState.fontSizePt * formatState.lineSpacingMultiplier * 1.3f).sp
                                )
                            }

                            if (!formatState.isReadingMode) {
                                BasicTextField(
                                    value = content,
                                    onValueChange = { updateText(it) },
                                    textStyle = targetTextStyle,
                                    cursorBrush = SolidColor(WordBlue),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("word_document_text_editor")
                                )
                            } else {
                                // Reading mode (Formatted text display)
                                Text(
                                    text = if (content.isNotBlank()) content else "Leeres Dokument",
                                    style = targetTextStyle,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            // BEARBEITUNGS- UND EINSTELLUNGSLEISTE RECHTS AM RAND!
            WordRightSideBar(
                state = formatState,
                onStateChange = { formatState = it },
                onInsertText = { inserted ->
                    updateText(content + inserted)
                },
                wordCount = wordCount,
                charCount = charCount,
                isAmoledMode = isAmoledMode
            )
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(
            isAmoledMode = isAmoledMode,
            onToggleAmoled = onToggleAmoled,
            formatState = formatState,
            onFormatStateChange = { formatState = it },
            onDismiss = { showSettingsDialog = false }
        )
    }
}
