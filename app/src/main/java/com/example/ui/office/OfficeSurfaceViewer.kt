package com.example.ui.office

import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.engine.lokit.LibreOfficeKitBridge
import com.example.model.DocumentFeatures
import com.example.model.DocumentMetadata
import com.example.ui.docx.DocxInspector

enum class OfficeViewMode {
    STRUCTURE,
    SURFACE_VIEW
}

@Composable
fun OfficeSurfaceViewer(
    metadata: DocumentMetadata,
    rawBytes: ByteArray,
    features: DocumentFeatures,
    onShowInfo: () -> Unit,
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableStateOf(OfficeViewMode.STRUCTURE) }
    var showOptimizationDetails by remember { mutableStateOf(false) }
    val profile = remember { LibreOfficeKitBridge.getOptimizationProfile() }

    Column(modifier = modifier.fillMaxSize()) {
        // Mode Selector Bar: Structure vs Direct ANativeWindow SurfaceView
        Surface(
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(
                        selected = viewMode == OfficeViewMode.STRUCTURE,
                        onClick = { viewMode = OfficeViewMode.STRUCTURE },
                        label = { Text("OOXML Struktur", fontSize = 12.sp) },
                        modifier = Modifier.padding(end = 6.dp).testTag("tab_office_structure")
                    )

                    FilterChip(
                        selected = viewMode == OfficeViewMode.SURFACE_VIEW,
                        onClick = { viewMode = OfficeViewMode.SURFACE_VIEW },
                        label = { Text("ANativeWindow (Surface)", fontSize = 12.sp) },
                        modifier = Modifier.testTag("tab_office_surface")
                    )
                }

                IconButton(
                    onClick = { showOptimizationDetails = !showOptimizationDetails },
                    modifier = Modifier.size(36.dp).testTag("btn_toggle_lokit_profile")
                ) {
                    Icon(
                        imageVector = if (showOptimizationDetails) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "LOKit Build-Parameter"
                    )
                }
            }
        }

        // Optimization banner (Flags, ANativeWindow, System Fonts)
        AnimatedVisibility(visible = showOptimizationDetails) {
            Card(
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "LOKit Build- & Render-Konfiguration (Samsung Galaxy A25)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    ParamRow("Ziel-Architektur:", profile.targetAbi)
                    ParamRow("Render-Pipeline:", profile.renderMode)
                    ParamRow("Compiler-Flags:", "-Os, -ffunction-sections, -fdata-sections, -flto")
                    ParamRow("Linker-Flags:", "-Wl,--gc-sections, -s, -flto")
                    ParamRow("System-Fonts:", "${profile.systemFontsPath} (APK ohne Font-Ballast)")
                }
            }
        }

        // Body Content
        when (viewMode) {
            OfficeViewMode.STRUCTURE -> {
                DocxInspector(
                    metadata = metadata,
                    rawBytes = rawBytes,
                    features = features,
                    onShowInfo = onShowInfo,
                    modifier = Modifier.weight(1f)
                )
            }

            OfficeViewMode.SURFACE_VIEW -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(16.dp)
                ) {
                    // Surface header note
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Zero-Copy Direct Rendering: Zeichnet direkt in die ANativeWindow Hardware-Surface (kein Java Bitmap Buffer).",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Direct ANativeWindow SurfaceView embedded into Jetpack Compose
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .background(androidx.compose.ui.graphics.Color(0xFFE2E8F0))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                LOKitSurfaceView(ctx).apply {
                                    setDocument(1L)
                                }
                            },
                            modifier = Modifier.fillMaxSize().testTag("lokit_surface_view")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Gesten: 2-Finger-Zoom & Schwenken aktiv",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "1440 Twips = 1 Inch",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ParamRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
