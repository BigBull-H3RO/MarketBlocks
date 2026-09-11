---
name: "Code-Review"
description: "Prüft eine Datei, Klasse oder ein ganzes Feature-Modul auf Bugs, Sicherheitslücken, Performance-Probleme und NeoForge Best Practices. Gibt strukturiertes Feedback mit Prioritäten."
---

# Code-Review Skill

Führe ein gründliches Code-Review der angegebenen Datei(en) oder des Features durch.

## Prüfbereiche

Analysiere den Code systematisch in diesen Kategorien:

### 1. Korrektheit & Bugs (P0)
- Logikfehler, Null-Pointer-Risiken, Off-by-One-Fehler
- Fehlende Null-Checks bei `BlockEntity`, `Level`, `Player` oder `ItemStack`
- Falsche Seitenlogik (Client vs. Server) – z.B. `level.isClientSide()` Checks
- Race Conditions bei Multi-Thread-Zugriff (z.B. Server-Tick vs. Netzwerk-Thread)

### 2. Sicherheit (P0)
- Fehlende Server-Side-Validierung bei Netzwerk-Packets
- Unvalidierte Spieler-Eingaben (Namen, Zahlen, ItemStacks)
- Distanz-Checks bei Container-Interaktionen (`stillValid()`)
- Exploit-Potenzial (Duplication Glitches, Negative Stacks, Overflow)

### 3. NeoForge Best Practices (P1)
- Korrekte Verwendung von Capabilities und Registries
- Richtiger Einsatz von `@SubscribeEvent` vs. manueller Event-Registrierung
- Korrekte Codec/StreamCodec-Nutzung für Netzwerk-Packets
- Korrekte `SavedData`-Implementierung (dirty-Markierung, Serialisierung)
- Verwendung von `MarketBlocks.MODID` statt Hardcoded-Strings

### 4. Performance (P2)
- Unnötige Berechnungen in `tick()`-Methoden
- Fehlende Caching-Strategien bei wiederholten Lookups
- Übermäßige NBT-Serialisierung oder Entity-Scans
- Unnötige Client-Server-Synchronisation

### 5. Code-Qualität (P3)
- Lesbarkeit, Benennung, Struktur
- Fehlende oder irreführende Kommentare
- Code-Duplizierung

## Ablauf (2 Stufen)

### Stufe 1: Analyse-Report

Erstelle zuerst ein Artifact (`code_review_report.md`) mit dem Review-Ergebnis:

```markdown
# Code-Review: [Datei/Feature-Name]

## Zusammenfassung
[Kurze Einschätzung des Gesamtzustands]

## Findings

### 🔴 P0 – Kritisch
- **[Titel]** ([Datei:Zeile](link))
  - Problem: ...
  - Vorschlag: ...

### 🟡 P1 – Wichtig
...

### 🟢 P2 – Verbesserung
...

### 💡 P3 – Hinweise
...
```

**Wichtig:** In Stufe 1 werden **keine Code-Änderungen** durchgeführt – nur analysiert.

### Stufe 2: Implementation Plan

Wenn Findings mit P0 oder P1 gefunden wurden, erstelle anschließend einen **Implementation Plan** (`implementation_plan.md`) mit:

- Konkreten Code-Änderungen pro Datei (mit Datei-Links und Zeilennummern)
- Gruppiert nach Komponente/Feature
- Klarer Beschreibung, was geändert wird und warum
- Setze `RequestFeedback: true`, damit der Benutzer einen **"Proceed"-Button** bekommt

Der Benutzer kann dann:
- ✅ **"Proceed"** klicken → Agent setzt die Fixes um
- ✏️ **Feedback geben** → Agent passt den Plan an
- ❌ **Ablehnen** → Nichts wird geändert

Wenn nur P2/P3-Findings gefunden wurden, frage den Benutzer ob ein Plan gewünscht ist.

## Regeln

- **Stufe 1 ändert keinen Code** – nur analysieren und berichten.
- **Stufe 2 ändert keinen Code** – nur den Plan erstellen und auf Genehmigung warten.
- Vermeide falsche Positives – sei präzise und begründe jedes Finding.
- Zeige konkrete Codezeilen mit Links.
- Priorisiere Findings nach Schweregrad (P0 > P1 > P2 > P3).
- Berücksichtige den NeoForge 1.21.x Kontext und Minecraft-spezifische Patterns.
