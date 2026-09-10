# AllDocsOff 📄🔒
### Word Offline Clone für Android – DOCX, PDF, MD, TXT, ODT mit 1:1 Byte-Preserving Storage Engine

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84.svg?style=flat&logo=android)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin%202.0-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20%2F%20MVVM-blue.svg)](https://developer.android.com/topic/architecture)
[![Security](https://img.shields.io/badge/Security-Knox%20Hardware%20Vault%20AES--256--GCM-red.svg)](https://developer.samsung.com/knox)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

**AllDocsOff** ist ein nativer, hochgradig optimierter Offline Word-Clone für Android (speziell abgestimmt auf Smartphones wie das **Samsung Galaxy A25** und One UI). Die App verbindet die vertraute mobile Microsoft-Word-Bedienung mit einer kompromisslosen **1:1 Byte-Preserving Storage Engine**, die Originaldateien bei unveränderten Inhalten auf Bitebene exakt bewahrt (Bit-für-Bit-Integrität via SHA-256).

---

## 🏷️ GitHub Repository Setup (Für beste Auffindbarkeit & SEO)

Falls dein GitHub-Repository noch die alten Standard-Texte anzeigt, kannst du diese auf **GitHub.com** mit zwei Klicks aktualisieren (da GitHub Web-Metadaten nicht über Git-Commits geändert werden können):

1. **Repository Name** (unter *Settings* ➔ *General* ➔ *Repository name*):
   - `AllDocsOff`
2. **About / Beschreibung** (auf der GitHub-Hauptseite rechts bei ⚙️ *About*):
   ```text
   Word offline Clone für Android inklusive DOCX, PDF, MD, TXT & ODT Editor mit 1:1 Byte-Preserving Engine, rechter Bearbeitungsleiste & Knox-Sicherheit.
   ```
3. **Topics / Tags** (im selben ⚙️ *About*-Fenster eingeben):
   ```text
   alldocsoff, android, word-clone, docx, odt, markdown, pdf-editor, text-editor, byte-preserving, offline-first, kotlin, jetpack-compose, material3, samsung-galaxy, knox-vault
   ```

---

## 🌟 Kernfunktionen & Highlights

### 1. 🔏 1:1 Byte-Preserving Storage Engine (No-Op-Save)
- **Linux Kernel Zero-Copy:** Nutzt den `sendfile`-Systemaufruf des Linux-Kernels für direkte Deskriptor-Übertragungen ohne Zwischenpufferung im Userspace.
- **Bitgenaue Erhaltung:** Wenn ein Dokument ohne Format- oder Textänderung gespeichert wird, garantiert AllDocsOff einen identischen SHA-256-Hash zum Original (`No-Op-Save`).
- **Atomare Dateispeicherungen:** Änderungen werden in isolierten temporären Dateien vorbereitet, auf Integrität validiert und erst dann atomar am Zielspeicherort ersetzt.
- **Pre-Flight Conversion Scanner:** Warnt vor potenziellem Formatverlust (z. B. Makros, Vektorgrafiken, komplexe Tabellen), bevor eine Konvertierung ausgeführt wird.

### 2. 📝 Microsoft Word Mobile Editor mit rechter Bearbeitungsleiste
- **Rechte Werkzeug- & Typografieleiste:** Ausklappbare Seitenleiste am rechten Rand für einhändige Bedienung auf Smartphones.
- **Typografie-Steuerung:** Direkte Anpassung von Schriftart (Aptos, Calibri, Arial, Times New Roman, Georgia, Courier New), Schriftgrad (8–72 pt), Fett (**B**), Kursiv (*I*), Unterstrichen (<u>U</u>) und Durchgestrichen (~abc~).
- **Absatzausrichtung:** Linksbündig, Zentriert, Rechtsbündig und Blocksatz.
- **Farben & Textmarker:** Große Auswahl an Schriftfarben und Word-Highlighter-Farben.
- **Echtzeit-Wort- & Zeichenzähler:** Ständige Anzeige der Textstatistiken direkt in der Leiste.
- **Schnellbausteine & Symbole:** Datum/Uhrzeit-Stempel, Bullet-Listen, Trennlinien und Pfeile mit nur einem Fingertipp.

### 3. 🖤 Samsung Galaxy A25 & AMOLED True-Black Optimierung
- **AMOLED Dark Mode:** Echtes Tiefschwarz (`#000000` / `#121212`) spart Energie auf dem Super-AMOLED-Display des Galaxy A25 und schont die Augen.
- **Intelligente Kontrastumschaltung:** Textfarben passen sich im Dark-Mode automatisch an, sodass dunkle Schrift auf dunklem Grund stets in lesbarem Weiß dargestellt wird.
- **Haptisches Feedback:** Dezent abgestimmte Vibrationen über die Android `HapticFeedback`-API für Tastendrücke und Toolbar-Aktionen.

### 4. 🛡️ Knox Hardware-Vault Verschlüsselung
- Hardwaregestützter **Android KeyStore** mit AES-256-GCM.
- Auf Samsung-Geräten hardware-isoliert durch den **Samsung Knox Vault**.
- Zero-Cloud-Politik: Keine externen Server, keine Telemetrie, 100% Offline-Betrieb.

---

## 📂 Unterstützte Dateiformate

| Format | Erweiterung | Leseunterstützung | Byte-Preserving | Bearbeitung |
|---|---|:---:|:---:|:---:|
| **Microsoft Word** | `.docx` | ✅ Vollständig | ✅ 100% Bit-Identisch | ✅ Text & Struktur |
| **OpenDocument Text** | `.odt` | ✅ Vollständig | ✅ 100% Bit-Identisch | ✅ Text & Struktur |
| **PDF Dokument** | `.pdf` | ✅ Native PDF-Renderer | ✅ 100% Bit-Identisch | 🔍 Lese- & Prüfmodus |
| **Markdown** | `.md` | ✅ Vollständig | ✅ 100% Bit-Identisch | ✅ Vollständig |
| **Reiner Text** | `.txt` | ✅ UTF-8 / ASCII | ✅ 100% Bit-Identisch | ✅ Vollständig |

---

## 🏛️ Architektur & Technische Details

AllDocsOff folgt strikt den Empfehlungen der **Android Clean Architecture** und **MVVM**:

```
com.example/
├── MainActivity.kt                      # Navigation & Lifecycle Handling
├── engine/                              # Core Processing & Storage Layer
│   ├── BytePreservingStorageEngine.kt   # Kernel Zero-Copy, Hash-Validierung, Atomic Save
│   ├── KernelZeroCopyTransfer.kt        # Linux sendfile / FileDescriptor Bridge
│   ├── PreFlightAnalyzer.kt             # Konvertierungs-Verlustanalyse
│   ├── ZipPackageEngine.kt              # Low-Level DOCX / ODT XML-Verarbeitung
│   ├── KnoxVaultSecurityManager.kt      # Hardware-gestützte AES-256-GCM Verschlüsselung
│   ├── DraftManager.kt                  # Automatische Wiederherstellung bei App-Schließen
│   └── SampleDocumentProvider.kt        # Bereitstellung von Test- und Beispieldokumenten
├── model/                               # Data Models, Enums & Undo/Redo Manager
│   ├── DocumentFormat.kt                # Format-Definitionen & MIME-Types
│   ├── DocumentMetadata.kt              # Metadaten, Hashes, Knox-Status
│   └── UndoRedoManager.kt               # Undo/Redo-Stack für Textbearbeitung
└── ui/                                  # Jetpack Compose UI
    ├── DocumentViewModel.kt             # Zentraler State-Manager (DocumentUiState)
    ├── HomeScreen.kt                    # Startbildschirm, Dateiauswahl & Beispieldokumente
    ├── dialogs/                         # Dialoge (Integrität, Einstellungen, Format-Picker)
    ├── theme/                           # AMOLED Dark & Word-Blue Themes
    └── word/                            # Word Mobile UI-Komponenten
        ├── WordDocumentEditor.kt        # Zentraler Editor-Canvas mit dynamischer Skalierung
        ├── WordRightSideBar.kt          # Rechte ausklappbare Bearbeitungsleiste
        ├── WordFormatState.kt           # Formatierungs-Zustand (Font, Spacing, Margin)
        └── WordRibbonBar.kt             # Alternative Ribbon-Toolbar-Komponenten
```

---

## 🚀 Voraussetzungen & Kompilierung

### Voraussetzungen
- **Android Studio:** Ladybug (2024.2.1) oder neuer
- **Android SDK:** Min SDK 26 (Android 8.0), Target SDK 35 (Android 15)
- **JDK:** Java 17 oder Java 21
- **Gradle:** 8.7+ mit Kotlin DSL (`.gradle.kts`)

### Build-Befehle
```bash
# Debug APK bauen
./gradlew assembleDebug

# Tests ausführen
./gradlew testDebugUnitTest
```

---

## 🔒 Datenschutz & Google Play Richtlinien

- **Keine unnötigen Berechtigungen:** AllDocsOff nutzt das Android Storage Access Framework (SAF) und den modernen Photo/File Picker. Es werden **keine** weitreichenden Dateiberechtigungen (`MANAGE_EXTERNAL_STORAGE`) angefordert.
- **Keine Internetverbindung nötig:** Das Manifest deklariert keine `android.permission.INTERNET`-Berechtigung. Dokumente verlassen zu keinem Zeitpunkt das Gerät.
- **Keine Tracking- oder Werbe-SDKs:** 100% quelloffen, transparent und sicher.

---

## 📄 Lizenz

Dieses Projekt steht unter der [MIT-Lizenz](LICENSE).

---

*Keywords: alldocsoff, android, kotlin, jetpack-compose, material3, word-editor, docx, odt, pdf, markdown, byte-preserving, storage-access-framework, samsung-galaxy, amoled, knox-vault, zero-copy, offline-first*
