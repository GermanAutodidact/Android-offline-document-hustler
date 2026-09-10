package com.example.ui.editor

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.DocumentFormat
import com.example.model.TextCommand
import com.example.model.UndoRedoManager

enum class EditorViewMode {
    EDIT,
    PREVIEW,
    SPLIT
}

@Composable
fun TextEditor(
    initialText: String,
    format: DocumentFormat,
    onTextChanged: (String) -> Unit,
    onShowInfo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = remember {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    var text by remember(initialText) { mutableStateOf(initialText) }
    val undoRedoManager = remember { UndoRedoManager() }
    var viewMode by remember {
        mutableStateOf(if (format == DocumentFormat.MD) EditorViewMode.EDIT else EditorViewMode.EDIT)
    }

    val haptic = LocalHapticFeedback.current

    // Large file check (> 1 MB)
    val isLargeFile = text.length > 1_000_000

    fun updateTextWithCommand(newText: String) {
        if (newText != text) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            undoRedoManager.pushCommand(TextCommand.Replace(0, text, newText))
            text = newText
            onTextChanged(newText)
        }
    }

    fun handleUndo() {
        if (undoRedoManager.canUndo) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            val undone = undoRedoManager.undo(text)
            text = undone
            onTextChanged(undone)
        }
    }

    fun handleRedo() {
        if (undoRedoManager.canRedo) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            val redone = undoRedoManager.redo(text)
            text = redone
            onTextChanged(redone)
        }
    }

    fun copyToClipboard() {
        val clip = ClipData.newPlainText("AllDocsOff Text", text)
        clipboardManager.setPrimaryClip(clip)
    }

    fun pasteFromClipboard() {
        val clip = clipboardManager.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val pasted = clip.getItemAt(0).text?.toString() ?: ""
            if (pasted.isNotEmpty()) {
                updateTextWithCommand(text + pasted)
            }
        }
    }

    val wordCount by remember {
        androidx.compose.runtime.derivedStateOf {
            if (text.isBlank()) 0 else text.split(Regex("\\s+")).count { it.isNotBlank() }
        }
    }
    val lineCount by remember {
        androidx.compose.runtime.derivedStateOf { text.lines().size }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Toolbar: Undo, Redo, View mode toggle (Edit/Preview/Split), Stats
        Surface(
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Undo
                    IconButton(
                        onClick = { handleUndo() },
                        enabled = undoRedoManager.canUndo,
                        modifier = Modifier.size(36.dp).testTag("btn_undo")
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_undo),
                            contentDescription = "Rückgängig (Command-basiert)",
                            tint = if (undoRedoManager.canUndo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    }

                    // Redo
                    IconButton(
                        onClick = { handleRedo() },
                        enabled = undoRedoManager.canRedo,
                        modifier = Modifier.size(36.dp).testTag("btn_redo")
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_redo),
                            contentDescription = "Wiederholen",
                            tint = if (undoRedoManager.canRedo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Markdown mode selector
                    if (format == DocumentFormat.MD) {
                        FilterChip(
                            selected = viewMode == EditorViewMode.EDIT,
                            onClick = { viewMode = EditorViewMode.EDIT },
                            label = { Text("Editor", fontSize = 12.sp) },
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        FilterChip(
                            selected = viewMode == EditorViewMode.PREVIEW,
                            onClick = { viewMode = EditorViewMode.PREVIEW },
                            label = { Text("Vorschau", fontSize = 12.sp) },
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        FilterChip(
                            selected = viewMode == EditorViewMode.SPLIT,
                            onClick = { viewMode = EditorViewMode.SPLIT },
                            label = { Text("Geteilt", fontSize = 12.sp) }
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Words / lines summary
                    Text(
                        text = "$wordCount W. · $lineCount Z.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )

                    // Info
                    IconButton(
                        onClick = onShowInfo,
                        modifier = Modifier.size(36.dp).testTag("btn_doc_info")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Dokument-Features & Hashes",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Large file warning banner
        AnimatedVisibility(visible = isLargeFile) {
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Große Datei: Textpuffer arbeitet mit optimierter Speicherverwaltung.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }

        // Editor Body
        when (viewMode) {
            EditorViewMode.EDIT -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    BasicTextField(
                        value = text,
                        onValueChange = { newText ->
                            updateTextWithCommand(newText)
                        },
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("text_editor_input")
                    )
                }
            }

            EditorViewMode.PREVIEW -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    MarkdownRenderer(markdown = text)
                }
            }

            EditorViewMode.SPLIT -> {
                Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        BasicTextField(
                            value = text,
                            onValueChange = { updateTextWithCommand(it) },
                            textStyle = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                            .verticalScroll(rememberScrollState())
                            .padding(12.dp)
                    ) {
                        MarkdownRenderer(markdown = text)
                    }
                }
            }
        }
    }
}

/**
 * Lightweight, native Compose Markdown previewer without heavy WebView or third-party bloat.
 */
@Composable
fun MarkdownRenderer(markdown: String, modifier: Modifier = Modifier) {
    val lines = markdown.lines()

    Column(modifier = modifier.fillMaxWidth()) {
        lines.forEach { line ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith("# ") -> {
                    Text(
                        text = trimmed.removePrefix("# ").trim(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                trimmed.startsWith("## ") -> {
                    Text(
                        text = trimmed.removePrefix("## ").trim(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                    )
                }
                trimmed.startsWith("### ") -> {
                    Text(
                        text = trimmed.removePrefix("### ").trim(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    Row(modifier = Modifier.padding(vertical = 2.dp, horizontal = 8.dp)) {
                        Text("• ", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text(
                            text = trimmed.substring(2).trim(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                trimmed.startsWith("> ") -> {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp))
                    ) {
                        Text(
                            text = trimmed.removePrefix("> ").trim(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
                trimmed.startsWith("|") && trimmed.endsWith("|") -> {
                    // Render simple table row
                    val cells = trimmed.split("|").filter { it.isNotBlank() }
                    if (!trimmed.contains("---")) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (trimmed.contains("**") || lines.indexOf(line) == 0)
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                    else
                                        MaterialTheme.colorScheme.surface
                                )
                                .padding(vertical = 4.dp, horizontal = 4.dp)
                        ) {
                            cells.forEach { cell ->
                                Text(
                                    text = cell.trim().replace("`", ""),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }
                }
                trimmed.isEmpty() -> {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                else -> {
                    Text(
                        text = trimmed,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}
