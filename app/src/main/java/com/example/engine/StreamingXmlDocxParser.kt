package com.example.engine

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream

/**
 * High-performance streaming OOXML / XML parser using Android's native Expat-backed XmlPullParser.
 *
 * Avoids loading the full XML document into memory:
 * - Constant low memory footprint (< 1 MB RAM) even on huge documents.
 * - Extracts paragraphs, run texts, feature flags and word count in a single streaming pass.
 */
object StreamingXmlDocxParser {

    data class ParseResult(
        val textSnippet: String,
        val wordCount: Int,
        val hasTables: Boolean,
        val hasImages: Boolean,
        val hasHyperlinks: Boolean,
        val hasFootnotes: Boolean,
        val hasEndnotes: Boolean,
        val hasColumns: Boolean,
        val hasShapes: Boolean,
        val hasComments: Boolean,
        val hasTrackingChanges: Boolean
    )

    /**
     * Parses an OOXML document stream (such as word/document.xml or content.xml)
     * using the Android system Expat pull parser.
     */
    fun parseStream(
        inputStream: InputStream,
        maxPreviewChars: Int = 30000
    ): ParseResult {
        val parser: XmlPullParser = Xml.newPullParser()
        parser.setInput(inputStream, "UTF-8")

        val textBuilder = StringBuilder()
        var wordCount = 0
        var hasTables = false
        var hasImages = false
        var hasHyperlinks = false
        var hasFootnotes = false
        var hasEndnotes = false
        var hasColumns = false
        var hasShapes = false
        var hasComments = false
        var hasTrackingChanges = false

        var inTextRun = false
        var eventType = parser.eventType

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    val tagName = parser.name.lowercase()
                    when {
                        tagName == "tbl" || tagName.endsWith(":tbl") || tagName == "table" -> hasTables = true
                        tagName == "drawing" || tagName.endsWith(":drawing") ||
                                tagName == "pict" || tagName.endsWith(":pict") ||
                                tagName == "blip" || tagName.endsWith(":blip") ||
                                tagName == "image" || tagName.endsWith(":image") -> hasImages = true
                        tagName == "hyperlink" || tagName.endsWith(":hyperlink") -> hasHyperlinks = true
                        tagName == "footnotereference" || tagName.endsWith(":footnotereference") ||
                                tagName == "footnote" || tagName.endsWith(":footnote") -> hasFootnotes = true
                        tagName == "endnotereference" || tagName.endsWith(":endnotereference") ||
                                tagName == "endnote" || tagName.endsWith(":endnote") -> hasEndnotes = true
                        tagName == "cols" || tagName.endsWith(":cols") -> hasColumns = true
                        tagName == "shape" || tagName.endsWith(":shape") -> hasShapes = true
                        tagName == "comment" || tagName.endsWith(":comment") -> hasComments = true
                        tagName == "ins" || tagName.endsWith(":ins") ||
                                tagName == "del" || tagName.endsWith(":del") -> hasTrackingChanges = true
                        tagName == "t" || tagName.endsWith(":t") ||
                                tagName == "p" || tagName.endsWith(":p") -> {
                            inTextRun = true
                        }
                    }
                }

                XmlPullParser.TEXT -> {
                    if (inTextRun) {
                        val text = parser.text
                        if (!text.isNullOrEmpty()) {
                            // Word count on-the-fly
                            var inWord = false
                            for (i in 0 until text.length) {
                                val c = text[i]
                                if (Character.isWhitespace(c)) {
                                    if (inWord) inWord = false
                                } else {
                                    if (!inWord) {
                                        wordCount++
                                        inWord = true
                                    }
                                }
                            }

                            // Append to preview if within limits
                            if (textBuilder.length < maxPreviewChars) {
                                textBuilder.append(text)
                            }
                        }
                    }
                }

                XmlPullParser.END_TAG -> {
                    val tagName = parser.name.lowercase()
                    if (tagName == "t" || tagName.endsWith(":t")) {
                        inTextRun = false
                    } else if (tagName == "p" || tagName.endsWith(":p")) {
                        inTextRun = false
                        if (textBuilder.length < maxPreviewChars && textBuilder.isNotEmpty() && !textBuilder.endsWith("\n")) {
                            textBuilder.append("\n")
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return ParseResult(
            textSnippet = textBuilder.toString().trim(),
            wordCount = wordCount,
            hasTables = hasTables,
            hasImages = hasImages,
            hasHyperlinks = hasHyperlinks,
            hasFootnotes = hasFootnotes,
            hasEndnotes = hasEndnotes,
            hasColumns = hasColumns,
            hasShapes = hasShapes,
            hasComments = hasComments,
            hasTrackingChanges = hasTrackingChanges
        )
    }
}
