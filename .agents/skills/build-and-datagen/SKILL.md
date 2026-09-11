---
name: "Build und Datagen"
description: "Baut das MarketBlocks-Projekt mit Gradle und führt optional Datagen aus. Analysiert Fehler automatisch, schlägt Fixes vor und gibt einen strukturierten Status-Report."
---

# Build und Datagen Skill

Führe den Build- und/oder Datagen-Prozess für das MarketBlocks-Projekt durch.

## Ablauf

### Schritt 1: Build ausführen

Führe den Gradle-Build aus:

```
gradlew build
```

- Arbeitsverzeichnis: Projekt-Root
- Erwarte eine Laufzeit von 30-120 Sekunden

### Schritt 2: Build-Ergebnis analysieren

**Bei Erfolg (BUILD SUCCESSFUL):**
- Melde Erfolg mit Zusammenfassung
- Prüfe ob Warnungen vorhanden sind und liste sie auf

**Bei Fehler (BUILD FAILED):**
- Identifiziere den **Fehlertyp**:
  - Kompilierfehler → Zeige die betroffene Datei, Zeile und Fehlermeldung
  - Ressourcen-Fehler → Prüfe fehlende Assets oder JSON-Syntax
  - Dependency-Fehler → Prüfe `build.gradle` und `gradle.properties`
- Analysiere die **Ursache** und schlage einen konkreten Fix vor
- **Führe den Fix NICHT automatisch aus** – beschreibe ihn nur

### Schritt 3: Datagen ausführen (wenn angefordert)

Falls der Benutzer Datagen angefordert hat oder falls es sinnvoll ist:

```
gradlew runData
```

- Prüfe ob alle Data Providers erfolgreich durchlaufen
- Bei Fehlern: Identifiziere den fehlgeschlagenen Provider und die Ursache

## Ausgabeformat

Berichte das Ergebnis direkt in der Chat-Antwort:

```
## Build-Report

**Status:** ✅ Erfolgreich / ❌ Fehlgeschlagen
**Dauer:** X Sekunden
**Warnungen:** X

### Fehler (falls vorhanden)
- Datei: [Link zur Datei]
- Zeile: X
- Fehler: [Fehlermeldung]
- Ursache: [Analyse]
- Vorgeschlagener Fix: [Beschreibung]

### Warnungen (falls vorhanden)
- [Liste der Warnungen]

### Datagen (falls ausgeführt)
**Status:** ✅ / ❌
- [Details]
```

## Regeln

- Führe **nur Build und Datagen** aus – keine anderen Gradle-Tasks.
- Bei Build-Fehlern: **Analysiere und berichte**, aber fixe nicht automatisch, es sei denn der Benutzer fordert es explizit.
- Wenn der Build erfolgreich ist, aber Warnungen auftreten, liste die Warnungen auf.
- Führe Datagen **nur aus**, wenn der Benutzer es anfordert oder explizit nach „Build und Datagen" fragt.
