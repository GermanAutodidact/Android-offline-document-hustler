package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.model.DocumentFormat
import com.example.ui.theme.WordBlue
import com.example.ui.theme.WordBorderGray
import com.example.ui.word.WordFormatState

@Composable
fun SettingsDialog(
    isAmoledMode: Boolean,
    onToggleAmoled: () -> Unit,
    formatState: WordFormatState? = null,
    onFormatStateChange: ((WordFormatState) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var autoSaveEnabled by remember { mutableStateOf(true) }
    var bytePreservationEnabled by remember { mutableStateOf(true) }
    var preFlightWarnEnabled by remember { mutableStateOf(true) }
    var navBarProtectionEnabled by remember { mutableStateOf(true) }
    var defaultFormat by remember { mutableStateOf(DocumentFormat.DOCX) }
    var showFormatDropdown by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 680.dp)
                .testTag("dialog_settings"),
            shape = RoundedCornerShape(20.dp),
            color = if (isAmoledMode) Color(0xFF111111) else Color.White,
            tonalElevation = 6.dp,
            shadowElevation = 16.dp,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isAmoledMode) Color(0xFF333333) else WordBorderGray
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // HEADER MIT ZAHNRAD-SYMBOL
                Surface(
                    color = if (isAmoledMode) Color.Black else WordBlue,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Einstellungen",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Einstellungen",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "DocPreserve · Samsung Galaxy A25 Edition",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("btn_settings_close")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Schließen",
                                tint = Color.White
                            )
                        }
                    }
                }

                // SCROLLABLE SETTINGS CONTENT
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. ERSCHEINUNGSBILD & DISPLAY
                    SettingsSectionTitle(title = "Erscheinungsbild & Display", iconRes = R.drawable.ic_preview)

                    // Super-AMOLED True-Black Switch
                    SettingsSwitchCard(
                        title = "Super-AMOLED True-Black (#000000)",
                        subtitle = "Schaltet alle schwarzen Pixel komplett ab (0 mA Stromverbrauch). Spart maximale Akkulaufzeit auf Ihrem Samsung Galaxy Super-AMOLED-Display.",
                        isChecked = isAmoledMode,
                        onCheckedChange = { onToggleAmoled() },
                        isAmoledMode = isAmoledMode,
                        testTag = "switch_amoled"
                    )

                    // Samsung 3-Tasten-Navigationsleisten-Schutz
                    SettingsSwitchCard(
                        title = "Navigationsleisten-Schutz (Samsung 3-Tasten)",
                        subtitle = "Verhindert Überlappungen mit der Samsung-Systemleiste (Zurück, Home, Letzte Apps). Hält alle Buttons frei erreichbar.",
                        isChecked = navBarProtectionEnabled,
                        onCheckedChange = { navBarProtectionEnabled = it },
                        isAmoledMode = isAmoledMode,
                        testTag = "switch_nav_bar"
                    )

                    HorizontalDivider(color = if (isAmoledMode) Color(0xFF222222) else WordBorderGray)

                    // 2. DOKUMENT & SCHRIFTART-VOREINSTELLUNGEN
                    SettingsSectionTitle(title = "Standard-Formatierung für neue Dokumente", iconRes = R.drawable.ic_doc)

                    // Standard-Schriftart
                    if (formatState != null && onFormatStateChange != null) {
                        var fontMenuOpen by remember { mutableStateOf(false) }
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isAmoledMode) Color(0xFF1E1E1E) else Color(0xFFF9FAFB)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { fontMenuOpen = true }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Standard-Schriftart",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = if (isAmoledMode) Color.White else Color(0xFF201F1E)
                                    )
                                    Text(
                                        text = "Aktuell: ${formatState.fontFamilyName}",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                                Surface(
                                    color = WordBlue.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, WordBlue)
                                ) {
                                    Text(
                                        text = formatState.fontFamilyName,
                                        color = WordBlue,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = fontMenuOpen,
                                    onDismissRequest = { fontMenuOpen = false }
                                ) {
                                    WordFormatState.AVAILABLE_FONT_FAMILIES.forEach { font ->
                                        DropdownMenuItem(
                                            text = { Text(font) },
                                            onClick = {
                                                onFormatStateChange(formatState.copy(fontFamilyName = font))
                                                fontMenuOpen = false
                                            },
                                            trailingIcon = if (formatState.fontFamilyName == font) {
                                                { Icon(Icons.Default.Check, contentDescription = null, tint = WordBlue) }
                                            } else null
                                        )
                                    }
                                }
                            }
                        }

                        // Standard-Schriftgröße
                        var sizeMenuOpen by remember { mutableStateOf(false) }
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isAmoledMode) Color(0xFF1E1E1E) else Color(0xFFF9FAFB)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { sizeMenuOpen = true }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Standard-Schriftgröße",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = if (isAmoledMode) Color.White else Color(0xFF201F1E)
                                    )
                                    Text(
                                        text = "Aktuell: ${formatState.fontSizePt} pt",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                                Surface(
                                    color = WordBlue.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, WordBlue)
                                ) {
                                    Text(
                                        text = "${formatState.fontSizePt} pt",
                                        color = WordBlue,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = sizeMenuOpen,
                                    onDismissRequest = { sizeMenuOpen = false }
                                ) {
                                    listOf(9, 10, 11, 12, 14, 16, 18, 24).forEach { size ->
                                        DropdownMenuItem(
                                            text = { Text("$size pt") },
                                            onClick = {
                                                onFormatStateChange(formatState.copy(fontSizePt = size))
                                                sizeMenuOpen = false
                                            },
                                            trailingIcon = if (formatState.fontSizePt == size) {
                                                { Icon(Icons.Default.Check, contentDescription = null, tint = WordBlue) }
                                            } else null
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Standard-Dateiformat
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isAmoledMode) Color(0xFF1E1E1E) else Color(0xFFF9FAFB)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showFormatDropdown = true }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Standard-Format für neue Dateien",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = if (isAmoledMode) Color.White else Color(0xFF201F1E)
                                )
                                Text(
                                    text = "${defaultFormat.displayName} (.${defaultFormat.extension})",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                            Surface(
                                color = WordBlue.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, WordBlue)
                            ) {
                                Text(
                                    text = defaultFormat.extension.uppercase(),
                                    color = WordBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showFormatDropdown,
                                onDismissRequest = { showFormatDropdown = false }
                            ) {
                                listOf(DocumentFormat.DOCX, DocumentFormat.MD, DocumentFormat.TXT).forEach { fmt ->
                                    DropdownMenuItem(
                                        text = { Text("${fmt.displayName} (.${fmt.extension})") },
                                        onClick = {
                                            defaultFormat = fmt
                                            showFormatDropdown = false
                                        },
                                        trailingIcon = if (defaultFormat == fmt) {
                                            { Icon(Icons.Default.Check, contentDescription = null, tint = WordBlue) }
                                        } else null
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = if (isAmoledMode) Color(0xFF222222) else WordBorderGray)

                    // 3. BYTE-PRESERVATION & SPEICHER-ENGINE
                    SettingsSectionTitle(title = "DocPreserve Speicher-Engine", iconRes = R.drawable.ic_shield_check)

                    SettingsSwitchCard(
                        title = "1:1 Byte-Preserving No-Op Speichern",
                        subtitle = "Wenn ein Dokument nicht editiert wurde, wird die Originaldatei exakt bitweise unverändert zurückgeschrieben (SHA-256 Identitätsgarantie).",
                        isChecked = bytePreservationEnabled,
                        onCheckedChange = { bytePreservationEnabled = it },
                        isAmoledMode = isAmoledMode,
                        testTag = "switch_byte_preservation"
                    )

                    SettingsSwitchCard(
                        title = "Automatisches Speichern (Auto-Save)",
                        subtitle = "Sichert Änderungen im Arbeitsspeicher kontinuierlich ab, um Datenverlust beim App-Wechsel zu verhindern.",
                        isChecked = autoSaveEnabled,
                        onCheckedChange = { autoSaveEnabled = it },
                        isAmoledMode = isAmoledMode,
                        testTag = "switch_auto_save"
                    )

                    SettingsSwitchCard(
                        title = "Pre-Flight Konvertierungs-Warnungen",
                        subtitle = "Prüft vor dem Konvertieren (z.B. von .docx nach .txt), ob Tabellen, Formatierungen oder Bilder verloren gehen würden.",
                        isChecked = preFlightWarnEnabled,
                        onCheckedChange = { preFlightWarnEnabled = it },
                        isAmoledMode = isAmoledMode,
                        testTag = "switch_preflight"
                    )

                    HorizontalDivider(color = if (isAmoledMode) Color(0xFF222222) else WordBorderGray)

                    // 4. SAMSUNG KNOX SICHERHEIT
                    SettingsSectionTitle(title = "Samsung Galaxy Knox Hardware-Sicherheit", iconRes = R.drawable.ic_shield_check)

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isAmoledMode) Color(0xFF1E1E1E) else Color(0xFFF3F4F6)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF0F6CBD).copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Knox",
                                        tint = Color(0xFF0F6CBD),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Samsung Knox Hardware-Tresor",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isAmoledMode) Color.White else Color(0xFF201F1E)
                                )
                                Text(
                                    text = "AES-256-GCM Verschlüsselung über den integrierten Android Keystore. Schützt sensible Dokumente vor unbefugtem Zugriff.",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    // 5. APP-INFO
                    Surface(
                        color = if (isAmoledMode) Color(0xFF191919) else Color(0xFFF3F2F1),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = WordBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DocPreserve Word Edition · v2.2.0 (Samsung Galaxy A25)",
                                fontSize = 11.sp,
                                color = if (isAmoledMode) Color.LightGray else Color(0xFF605E5C)
                            )
                        }
                    }
                }

                // FOOTER MIT SCHLIESSEN BUTTON
                Surface(
                    color = if (isAmoledMode) Color(0xFF161616) else Color(0xFFF9FAFB),
                    modifier = Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, if (isAmoledMode) Color(0xFF2E2E2E) else WordBorderGray)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = WordBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("btn_settings_apply")
                        ) {
                            Text(text = "Fertig", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String, iconRes: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = WordBlue,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = WordBlue
        )
    }
}

@Composable
private fun SettingsSwitchCard(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isAmoledMode: Boolean,
    testTag: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isAmoledMode) Color(0xFF1E1E1E) else Color(0xFFF9FAFB)
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = if (isAmoledMode) Color.White else Color(0xFF201F1E)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = WordBlue
                ),
                modifier = Modifier.testTag(testTag)
            )
        }
    }
}
