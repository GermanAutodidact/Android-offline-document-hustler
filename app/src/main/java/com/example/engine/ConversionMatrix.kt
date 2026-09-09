package com.example.engine

import com.example.model.ConversionReport
import com.example.model.ConversionWarning
import com.example.model.DocumentFeatures
import com.example.model.DocumentFormat
import com.example.model.WarningSeverity

object ConversionMatrix {

    fun checkConversion(
        from: DocumentFormat,
        to: DocumentFormat,
        features: DocumentFeatures
    ): ConversionReport {
        if (from == to) {
            return ConversionReport(
                fromFormat = from,
                toFormat = to,
                isLossless = true,
                warnings = emptyList(),
                summary = "Kein Formatwechsel: Byte-Preserving Modus garantiert 100% identische Bytes bei unveränderten Inhalten.",
                canConvertDirectly = true
            )
        }

        val warnings = mutableListOf<ConversionWarning>()
        var isLossless = false
        var summary = ""

        when {
            // DOCX -> TXT
            from == DocumentFormat.DOCX && to == DocumentFormat.TXT -> {
                warnings.add(
                    ConversionWarning(
                        title = "Schrift- & Layoutverlust",
                        description = "Sämtliche Formatierungen (Schriftarten, Farben, Ausrichtungen) werden irreversibel entfernt.",
                        severity = WarningSeverity.CRITICAL_DATA_LOSS
                    )
                )
                if (features.hasTables) {
                    warnings.add(
                        ConversionWarning(
                            title = "Tabellen werden vereinfacht",
                            description = "Tabellenstrukturen werden in simplen Fließtext oder Tab-Separatoren aufgelöst.",
                            severity = WarningSeverity.WARNING
                        )
                    )
                }
                if (features.hasImages) {
                    warnings.add(
                        ConversionWarning(
                            title = "Bilder & Grafiken entfallen",
                            description = "Eingebettete Bilder und Zeichnungen können im Plaintext-Format nicht gespeichert werden.",
                            severity = WarningSeverity.CRITICAL_DATA_LOSS
                        )
                    )
                }
                if (features.hasHyperlinks) {
                    warnings.add(
                        ConversionWarning(
                            title = "Hyperlinks entfernt",
                            description = "Verlinkungen verlieren ihr Ziel und werden als nackter Text dargestellt.",
                            severity = WarningSeverity.WARNING
                        )
                    )
                }
                summary = "Konvertierung nach Plaintext entfernt alle visuellen Elemente und Metadaten."
            }

            // DOCX -> MD
            from == DocumentFormat.DOCX && to == DocumentFormat.MD -> {
                if (features.hasImages) {
                    warnings.add(
                        ConversionWarning(
                            title = "Bilder entfallen im Standalone-MD",
                            description = "Eingebettete Bilder können ohne separates Asset-Verzeichnis nicht mitgespeichert werden.",
                            severity = WarningSeverity.WARNING
                        )
                    )
                }
                if (features.hasTables) {
                    warnings.add(
                        ConversionWarning(
                            title = "Tabellen vereinfacht",
                            description = "Komplexe Tabellen mit verbundenen Zellen werden in Standard-Markdown-Tabellen überführt.",
                            severity = WarningSeverity.INFO
                        )
                    )
                }
                if (features.hasShapes || features.hasWatermark) {
                    warnings.add(
                        ConversionWarning(
                            title = "Spezialobjekte entfallen",
                            description = "Formen, Vektoren und Wasserzeichen werden in Markdown nicht unterstützt.",
                            severity = WarningSeverity.WARNING
                        )
                    )
                }
                summary = "Texte, Überschriften und einfache Tabellen bleiben als Markdown erhalten."
            }

            // PDF -> TXT / MD
            from == DocumentFormat.PDF && (to == DocumentFormat.TXT || to == DocumentFormat.MD) -> {
                warnings.add(
                    ConversionWarning(
                        title = "Layout-Rekonstruktion nötig",
                        description = "PDF speichert feste Koordinaten statt Fließtext. Absätze und Spalten können abweichen.",
                        severity = WarningSeverity.CRITICAL_DATA_LOSS
                    )
                )
                warnings.add(
                    ConversionWarning(
                        title = "Keine OCR für Scans",
                        description = "Reine Bild-Scans ohne OCR-Textlayer liefern keinen extrahierbaren Text.",
                        severity = WarningSeverity.WARNING
                    )
                )
                summary = "Layout und Formatierung gehen verloren; nur lesbare Textströme werden exportiert."
            }

            // MD -> TXT
            from == DocumentFormat.MD && to == DocumentFormat.TXT -> {
                isLossless = true
                warnings.add(
                    ConversionWarning(
                        title = "Markdown-Tags als Plaintext",
                        description = "Sämtliche Markdown-Auszeichnungen (#, *, |) bleiben als lesbarer Text erhalten.",
                        severity = WarningSeverity.INFO
                    )
                )
                summary = "Verlustfreier Export: Inhalt bleibt 1:1 als lesbarer Text erhalten."
            }

            // TXT -> MD
            from == DocumentFormat.TXT && to == DocumentFormat.MD -> {
                isLossless = true
                summary = "Verlustfreie Konvertierung: Plaintext wird nahtlos als Markdown interpretiert."
            }

            // DOCX -> PDF or MD -> PDF
            to == DocumentFormat.PDF -> {
                warnings.add(
                    ConversionWarning(
                        title = "Endgültiges Ausgabeformat",
                        description = "Nach dem PDF-Export ist eine verlustfreie Rückkonvertierung in bearbeitbares Word nicht mehr möglich.",
                        severity = WarningSeverity.INFO
                    )
                )
                summary = "Layout wird für Druck und Anzeige fixiert."
            }

            else -> {
                warnings.add(
                    ConversionWarning(
                        title = "Formatwechsel $from → $to",
                        description = "Bei diesem Formatwechsel können herstellerspezifische Tags und Formatierungen abweichen.",
                        severity = WarningSeverity.WARNING
                    )
                )
                summary = "Formatwechsel von ${from.displayName} zu ${to.displayName}."
            }
        }

        return ConversionReport(
            fromFormat = from,
            toFormat = to,
            isLossless = isLossless,
            warnings = warnings,
            summary = summary,
            canConvertDirectly = true
        )
    }
}
