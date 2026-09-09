package com.example

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DocumentFormat
import com.example.model.DocumentMetadata
import com.example.ui.DocumentUiState
import com.example.ui.DocumentViewModel
import com.example.ui.dialogs.ByteIntegrityDialog
import com.example.ui.dialogs.FormatPickerDialog
import com.example.ui.dialogs.PreFlightDialog
import com.example.ui.docx.DocxInspector
import com.example.ui.editor.TextEditor
import com.example.ui.home.DocumentHomeScreen
import com.example.ui.pdf.PdfViewer
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: DocumentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize sample documents
        viewModel.loadSamples(this)

        setContent {
            MyApplicationTheme {
                DocPreserveApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocPreserveApp(viewModel: DocumentViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showMoreMenu by remember { mutableStateOf(false) }
    var pendingTargetFormat by remember { mutableStateOf<DocumentFormat?>(null) }

    // SAF Open Document launcher
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.openDocument(context, uri)
        }
    }

    // SAF Save / Export Document launcher
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { uri: Uri? ->
        val targetFormat = pendingTargetFormat
        if (uri != null && targetFormat != null) {
            viewModel.executeSave(context, uri, targetFormat)
        }
        pendingTargetFormat = null
    }

    // Show status messages in snackbar
    LaunchedEffect(uiState.statusMessage) {
        val msg = uiState.statusMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            DocPreserveTopAppBar(
                metadata = uiState.metadata,
                onBack = { viewModel.closeDocument() },
                onSave = {
                    val meta = uiState.metadata
                    if (meta != null) {
                        if (meta.uri.scheme == "memory") {
                            // Needs target URI from user
                            pendingTargetFormat = meta.format
                            createDocumentLauncher.launch(meta.name)
                        } else {
                            // Direct save to current file (No-Op byte copy if !isDirty)
                            viewModel.executeSave(context, meta.uri, meta.format)
                        }
                    }
                },
                onSaveAs = {
                    viewModel.setFormatPickerVisible(true)
                },
                onShowIntegrity = {
                    viewModel.setIntegrityDialogVisible(true)
                },
                onMoreMenuToggle = { showMoreMenu = !showMoreMenu },
                showMoreMenu = showMoreMenu,
                onDismissMoreMenu = { showMoreMenu = false }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val meta = uiState.metadata
            val bytes = uiState.rawBytes

            if (meta == null || bytes == null) {
                // Home Screen with test documents and quick actions
                DocumentHomeScreen(
                    sampleDocs = uiState.sampleDocs,
                    onOpenSaf = {
                        openDocumentLauncher.launch(
                            arrayOf(
                                "application/pdf",
                                "text/plain",
                                "text/markdown",
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                "*/*"
                            )
                        )
                    },
                    onCreateNew = { format ->
                        viewModel.createNewDocument(format)
                    },
                    onOpenSample = { sample ->
                        viewModel.openDocument(context, sample.uri, sample.name)
                    }
                )
            } else {
                // Active Document Workspace
                when (meta.format) {
                    DocumentFormat.PDF -> {
                        PdfViewer(
                            fileBytes = bytes,
                            onPageRotationChanged = { viewModel.onPageRotated() },
                            onShowInfo = { viewModel.setIntegrityDialogVisible(true) }
                        )
                    }

                    DocumentFormat.TXT, DocumentFormat.MD -> {
                        TextEditor(
                            initialText = uiState.textContent,
                            format = meta.format,
                            onTextChanged = { viewModel.onTextChanged(it) },
                            onShowInfo = { viewModel.setIntegrityDialogVisible(true) }
                        )
                    }

                    DocumentFormat.DOCX, DocumentFormat.ODT -> {
                        DocxInspector(
                            metadata = meta,
                            rawBytes = bytes,
                            features = uiState.features,
                            onShowInfo = { viewModel.setIntegrityDialogVisible(true) }
                        )
                    }

                    DocumentFormat.UNKNOWN -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Binärformat nicht direkt editierbar.\nByte-Preserving No-Op-Save bleibt aktiv.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Saving progress overlay
            AnimatedVisibility(
                visible = uiState.isSaving,
                modifier = Modifier.align(Alignment.Center)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Validiere & Sichere...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }

    // Pre-Flight Lossy Warning Modal
    val report = uiState.preFlightReport
    if (report != null) {
        PreFlightDialog(
            report = report,
            features = uiState.features,
            onConfirm = {
                val target = report.toFormat
                pendingTargetFormat = target
                val currentName = uiState.metadata?.name ?: "dokument"
                val baseName = currentName.substringBeforeLast(".")
                createDocumentLauncher.launch("$baseName.${target.extension}")
            },
            onDismiss = {
                viewModel.dismissPreFlight()
            }
        )
    }

    // Format Selector Modal ("Speichern unter...")
    if (uiState.showFormatPicker) {
        val currentFormat = uiState.metadata?.format ?: DocumentFormat.TXT
        FormatPickerDialog(
            currentFormat = currentFormat,
            onFormatSelected = { selectedFormat ->
                viewModel.setFormatPickerVisible(false)
                viewModel.requestSaveOrPreFlight(selectedFormat)
            },
            onDismiss = {
                viewModel.setFormatPickerVisible(false)
            }
        )
    }

    // Byte-Integrity Inspector Modal
    if (uiState.showIntegrityDialog && uiState.metadata != null) {
        ByteIntegrityDialog(
            metadata = uiState.metadata!!,
            features = uiState.features,
            currentSha256 = uiState.metadata!!.currentSha256,
            onDismiss = {
                viewModel.setIntegrityDialogVisible(false)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocPreserveTopAppBar(
    metadata: DocumentMetadata?,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onSaveAs: () -> Unit,
    onShowIntegrity: () -> Unit,
    onMoreMenuToggle: () -> Unit,
    showMoreMenu: Boolean,
    onDismissMoreMenu: () -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        title = {
            if (metadata != null) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = metadata.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        // Dirty indicator or clean check
                        if (metadata.isDirty) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(MaterialTheme.colorScheme.tertiary, CircleShape)
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_shield_check),
                                contentDescription = "Byte-Identisch",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        text = if (!metadata.isDirty)
                            "${metadata.format.displayName} · No-Op-Save bereit"
                        else
                            "${metadata.format.displayName} · Geändert (isDirty = true)",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = if (!metadata.isDirty) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                    )
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_shield_check),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DocPreserve",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        navigationIcon = {
            if (metadata != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("btn_close_doc")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Dokument schließen"
                    )
                }
            }
        },
        actions = {
            if (metadata != null) {
                // Direct Save (No-op byte copy if not dirty)
                IconButton(
                    onClick = onSave,
                    modifier = Modifier.testTag("btn_save_document")
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_save),
                        contentDescription = "Speichern (Byte-Preserving No-Op)",
                        tint = if (!metadata.isDirty) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                    )
                }

                // Integrity & feature analysis
                IconButton(
                    onClick = onShowIntegrity,
                    modifier = Modifier.testTag("btn_show_integrity")
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_shield_check),
                        contentDescription = "Byte-Integritätsanalyse",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Overflow menu
                Box {
                    IconButton(
                        onClick = onMoreMenuToggle,
                        modifier = Modifier.testTag("btn_more_options")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Weitere Optionen"
                        )
                    }

                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = onDismissMoreMenu
                    ) {
                        DropdownMenuItem(
                            text = { Text("Speichern unter... (Pre-Flight)") },
                            onClick = {
                                onDismissMoreMenu()
                                onSaveAs()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Byte-Integrität & Hashes") },
                            onClick = {
                                onDismissMoreMenu()
                                onShowIntegrity()
                            }
                        )
                    }
                }
            }
        }
    )
}
