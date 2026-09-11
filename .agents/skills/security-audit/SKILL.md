---
name: "Security-Audit"
description: "Prüft Netzwerk-Packets, Menü-Interaktionen und Server-Side-Validierung auf Sicherheitslücken und Exploits. Fokus auf Client-zu-Server Vertrauensgrenzen, Duplizierung, Injection und Bypass-Angriffe."
---

# Security-Audit Skill

Führe ein fokussiertes Security-Audit für die angegebene(n) Klasse(n) oder das Feature durch.

## Angriffsszenarien prüfen

Simuliere die Perspektive eines böswilligen Clients/Modded-Clients und prüfe systematisch:

### 1. Netzwerk-Packet-Validierung
- Werden **alle Felder** des Packets serverseitig validiert (nicht nur clientseitig)?
- Gibt es **Bereichsprüfungen** für numerische Werte (Negative Zahlen, Integer-Overflow, 0)?
- Werden **ItemStack-Eingaben** validiert (Stack-Größe, Item-Typ, NBT)?
- Gibt es **String-Längen-Limits** und Sanitization (Namensfelder, Suchfelder)?
- Wird geprüft, ob der **sendende Spieler berechtigt** ist (Owner, Permissions)?

### 2. Menü/Container-Sicherheit
- Wird `stillValid()` korrekt implementiert (Distanz-Check, Block-Existenz)?
- Können Spieler auf Menüs zugreifen, die ihnen nicht gehören?
- Sind **Slot-Klicks validiert** (Quick-Move, Shift-Click, Number-Keys)?
- Gibt es **Duplication-Glitch-Potenzial** bei gleichzeitigem Zugriff?

### 3. BlockEntity / SavedData Integrität
- Kann ein Spieler **Daten eines fremden Blocks** manipulieren (fehlender Owner-Check)?
- Wird die **Distanz zum Block** geprüft, bevor Aktionen ausgeführt werden?
- Können **Chunk-Unloading/Reloading** Dateninkonsistenzen verursachen?
- Ist die **dirty()-Markierung** bei SavedData korrekt gesetzt?

### 4. Timing & Race Conditions
- Können **Rate-Limits** umgangen werden?
- Gibt es **TOCTOU-Schwachstellen** (Time-of-check-to-time-of-use)?
- Können Packets **in falscher Reihenfolge** gesendet werden, um Zustände zu korrumpieren?
- Was passiert bei **gleichzeitigem Zugriff** mehrerer Spieler?

### 5. Economy / Handels-Exploits
- Können **negative Preise** oder Mengen gesetzt werden?
- Gibt es **Integer-Overflow** bei Preisberechnungen?
- Können Items **dupliziert** werden (Transaktions-Atomarität)?
- Funktioniert die **Bestandsprüfung** korrekt (Race zwischen Prüfung und Entnahme)?

## Referenz: Bekannte Fixes in diesem Projekt

Diese Schwachstellen wurden bereits in MarketBlocks gefunden und gefixt – prüfe ob ähnliche Patterns in neuem Code vorkommen:

- **UpdateSettingsPacket** ohne Server-Side Owner-Validierung
- **SingleOffer-Mutationspakete** ohne Menü-Bindung und Distanz-Check
- **Codecs ohne Größenbegrenzung** (DoS via riesige Maps/Listen)
- **Rate-Limiter global statt pro Packet-Typ** (UI-Lags bei legitimem Nutzen)
- **Name-Sanitization** Bypass via Hex-Color-Codes
- **Double-Chest** Platzierungs-Exploit bei Sicherheitschecks
- **Autofill seitenübergreifend** aktiviert statt nur aktuelle Page

## Ablauf (2 Stufen)

### Stufe 1: Audit-Report

Erstelle zuerst ein Artifact (`security_audit_report.md`) mit dem Audit-Ergebnis:

```markdown
# Security-Audit: [Feature/Klasse]

## Risiko-Einschätzung
[HOCH / MITTEL / NIEDRIG] – Zusammenfassung

## Schwachstellen

### 🔴 Kritisch (Exploit möglich)
- **[Titel]** ([Datei:Zeile](link))
  - Angriff: [Wie kann dies ausgenutzt werden?]
  - Impact: [Was ist der Schaden?]
  - Fix: [Konkreter Lösungsvorschlag]

### 🟡 Potenziell (unter bestimmten Bedingungen)
...

### 🟢 Empfehlung (Defense in Depth)
...

## Geprüfte Dateien
[Liste aller geprüften Dateien mit Links]
```

**Wichtig:** In Stufe 1 werden **keine Code-Änderungen** durchgeführt – nur analysiert.

### Stufe 2: Implementation Plan

Wenn Schwachstellen mit 🔴 Kritisch oder 🟡 Potenziell gefunden wurden, erstelle anschließend einen **Implementation Plan** (`implementation_plan.md`) mit:

- Konkreten Security-Fixes pro Datei (mit Datei-Links und Zeilennummern)
- Gruppiert nach Angriffsvektor/Komponente
- Klarer Beschreibung des Fixes und welchen Angriff er verhindert
- Setze `RequestFeedback: true`, damit der Benutzer einen **"Proceed"-Button** bekommt

Der Benutzer kann dann:
- ✅ **"Proceed"** klicken → Agent setzt die Security-Fixes um
- ✏️ **Feedback geben** → Agent passt den Plan an
- ❌ **Ablehnen** → Nichts wird geändert

Wenn nur 🟢 Empfehlungen gefunden wurden, frage den Benutzer ob ein Plan gewünscht ist.

## Regeln

- **Stufe 1 ändert keinen Code** – nur analysieren und berichten.
- **Stufe 2 ändert keinen Code** – nur den Plan erstellen und auf Genehmigung warten.
- Sei **konservativ** – wenn etwas potenziell unsicher ist, melde es.
- Teste Annahmen: Lies den tatsächlichen Code, rate nicht.
- Prüfe die gesamte Kette: Packet → Handler → BlockEntity/SavedData.
- Verweise auf die bekannten Fixes oben, wenn ähnliche Patterns gefunden werden.
