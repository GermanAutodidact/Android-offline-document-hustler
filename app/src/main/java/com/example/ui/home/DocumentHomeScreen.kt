package com.example.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.engine.SampleDocumentProvider
import com.example.model.DocumentFormat
import com.example.ui.theme.WordBlue
import com.example.ui.theme.WordBorderGray
import com.example.ui.theme.WordDeskBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentHomeScreen(
    sampleDocs: List<SampleDocumentProvider.SampleDoc>,
    onOpenSaf: () -> Unit,
    onCreateNew: (DocumentFormat) -> Unit,
    onOpenSample: (SampleDocumentProvider.SampleDoc) -> Unit,
    onOpenSettings: () -> Unit,
    isAmoledMode: Boolean = false,
    onToggleAmoled: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (isAmoledMode) Color.Black else WordDeskBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // TOP APP BAR MIT WORD-BRANDING & ZAHNRAD EINSTELLUNGEN
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = if (isAmoledMode) Color.Black else WordBlue,
                titleContentColor = Color.White,
                actionIconContentColor = Color.White
            ),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.25f),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "W",
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp,
                                color = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "DocPreserve",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Word Edition · Samsung A25",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            },
            actions = {
                // AMOLED Quick Switch
                IconButton(
                    onClick = onToggleAmoled,
                    modifier = Modifier.testTag("btn_home_amoled")
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isAmoledMode) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = if (isAmoledMode) "AMOLED ✓" else "AMOLED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                // ZAHNRAD FÜR EINSTELLUNGEN
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.testTag("btn_home_settings")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Einstellungen",
                        tint = Color.White
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isAmoledMode) Color.Black else WordDeskBackground)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))

            // MICROSOFT WORD MOBILE BRANDING & TEMPLATE HEADER
            Text(
                text = "Neues Dokument erstellen",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF201F1E)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Template Carousel (Leeres Dokument, Notizen, Vorlage)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Leeres Dokument (DOCX)
                WordTemplateCard(
                    title = "Leeres Dokument",
                    subtitle = "Word (.docx)",
                    isPrimary = true,
                    testTag = "btn_new_docx",
                    onClick = { onCreateNew(DocumentFormat.DOCX) }
                )

                // 2. Notizen (MD)
                WordTemplateCard(
                    title = "Notizen & Markdown",
                    subtitle = "Markdown (.md)",
                    isPrimary = false,
                    testTag = "btn_new_md",
                    onClick = { onCreateNew(DocumentFormat.MD) }
                )

                // 3. Reines Textdokument (TXT)
                WordTemplateCard(
                    title = "Textdokument",
                    subtitle = "Reiner Text (.txt)",
                    isPrimary = false,
                    testTag = "btn_new_txt",
                    onClick = { onCreateNew(DocumentFormat.TXT) }
                )
            }
        }

        // QUICK OPEN / DEVICE STORAGE BUTTON
        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
                shadowElevation = 1.dp,
                border = androidx.compose.foundation.BorderStroke(0.5.dp, WordBorderGray),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenSaf() }
                    .testTag("btn_open_saf")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = WordBlue.copy(alpha = 0.12f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_doc),
                                contentDescription = null,
                                tint = WordBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Dokument vom Gerät öffnen",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "Dateimanager (SAF) · DOCX, PDF, MD, TXT, ODT",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Öffnen",
                        tint = WordBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // SECTION: ZULETZT VERWENDET (Recent Documents with Word Blue badges)
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Zuletzt verwendet",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF201F1E)
                )
                Text(
                    text = "${sampleDocs.size} Dokumente",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        items(sampleDocs) { sample ->
            ElevatedCard(
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenSample(sample) }
                    .testTag("sample_doc_${sample.name}"),
                colors = androidx.compose.material3.CardDefaults.elevatedCardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // WORD ICON BADGE (The signature Word 'W' Blue Tile)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (sample.formatTag) {
                            "DOCX" -> WordBlue
                            "PDF" -> Color(0xFFC00000) // Red for PDF
                            "MD" -> Color(0xFF0D9488) // Teal for Markdown
                            else -> Color(0xFF595959) // Gray for Text
                        },
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = when (sample.formatTag) {
                                    "DOCX" -> "W"
                                    "PDF" -> "PDF"
                                    "MD" -> "MD"
                                    else -> "TXT"
                                },
                                fontWeight = FontWeight.Black,
                                fontSize = if (sample.formatTag == "DOCX") 20.sp else 12.sp,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = sample.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B),
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Auf diesem Gerät · ${sample.description}",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            maxLines = 1
                        )
                    }

                    IconButton(
                        onClick = { onOpenSample(sample) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Optionen",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
    }
}

@Composable
private fun WordTemplateCard(
    title: String,
    subtitle: String,
    isPrimary: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isPrimary) 1.5.dp else 1.dp,
            color = if (isPrimary) WordBlue else WordBorderGray
        ),
        color = Color.White,
        shadowElevation = if (isPrimary) 2.dp else 1.dp,
        modifier = Modifier
            .width(130.dp)
            .height(150.dp)
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Miniature Page Sheet Preview
            Surface(
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, WordBorderGray),
                color = if (isPrimary) WordBlue.copy(alpha = 0.05f) else Color(0xFFFAFAFA),
                modifier = Modifier
                    .width(60.dp)
                    .height(78.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isPrimary) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = WordBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Column(
                            modifier = Modifier.padding(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            repeat(4) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(if (it == 3) 0.6f else 1f)
                                        .height(3.dp)
                                        .background(Color.LightGray, RoundedCornerShape(1.dp))
                                )
                            }
                        }
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPrimary) WordBlue else Color(0xFF1E293B),
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    fontSize = 9.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
            }
        }
    }
}
