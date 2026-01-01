# MarketBlocks – Modbeschreibung

## Überblick

**MarketBlocks** ist ein Minecraft-Mod für **NeoForge** (Minecraft **1.21.1**), dessen Ziel es ist, **Shop-Systeme direkt in Minecraft zu integrieren**. Der Mod verzichtet bewusst auf externe Plugins oder reine Command-Lösungen und setzt stattdessen auf **GUI-basierte, spielernahe Shops**, die sich möglichst natürlich in das Vanilla-Gameplay einfügen.

Der Fokus liegt auf **stabilen, sicheren und klar definierten Handelsmechaniken**, bei denen der Server immer autoritativ ist (kein Itemverlust, keine Duplikation, keine Client-Manipulation).

---

## Ziel des Mods

Das Hauptziel von MarketBlocks ist es:

- Handel zwischen Spielern zu ermöglichen
- strukturierte Shop-Systeme bereitzustellen
- klassische Chest-Shops oder Plugin-Lösungen zu ersetzen
- sichere Kaufprozesse zu garantieren
  - kein Itemverlust
  - keine Item-Duplikation
  - saubere Client-Server-Synchronisation

---

## Aktuelle Shop-Typen

MarketBlocks besteht aktuell aus zwei zentralen Shop-Konzepten.

### SmallShop (Block-basiert)

Der **SmallShop** ist ein platzierbarer Block, der einen **einzelnen Handel** zwischen Spielern ermöglicht.

#### Owner-Konzept

- Wird ein SmallShop von einem Spieler platziert, wird dieser Spieler automatisch zum **Owner** des Shops.
- Der Owner sieht in der SmallShop-GUI **zusätzliche Inhalte und Buttons**, die für normale Spieler nicht sichtbar sind.

---

#### Angebots-Erstellung

- Existiert **noch kein Angebot**, kann der Owner:
  - Items in die **zwei Bezahl-Slots** und den **Kauf-Slot** legen
- **Sobald der Owner Items in diese Slots legt**, werden diese **live in der Angebots-Vorschau angezeigt**:
  - diese Live-Vorschau zeigt **nur die Items und Mengen**
  - **ohne Button-Funktion**
  - sie dient ausschließlich zur visuellen Kontrolle des Angebots

- Klickt der Owner anschließend neben der Angebots-Vorschau auf den **„Create Offer“-Button**:
  - wird aus der Live-Vorschau ein **echtes Angebot mit Button-Funktion**
  - das Angebot ist nun für andere Spieler vollständig nutzbar

- Nach dem Erstellen:
  - wird das Angebot in der **Angebots-Vorschau** angezeigt
  - der Button **„Create Offer“** wird durch **„Delete Offer“** ersetzt

- Wird das Angebot gelöscht:
  - verschwindet das Angebot vollständig
  - es kann erneut ein neues Angebot erstellt werden
  - die Live-Vorschau steht wieder zur Verfügung

---

#### GUI-Seiten (nur für den Owner sichtbar)

Rechts neben der GUI stehen dem Owner **drei Seiten-Buttons** zur Verfügung:

##### 1. Angebotsseite (Standardseite)

- Wird immer zuerst angezeigt
- Hier können:
  - Angebote erstellt oder gelöscht werden
  - Spieler das Angebot einsehen und Käufe durchführen
  - Spieler ihre Items in die **Bezahl-Slots** legen

##### 2. Inventarseite

- Besteht aus zwei separaten Inventarbereichen:
  - **Input-Inventar (links)** – enthält die verfügbaren Kauf-Items
  - **Output-Inventar (rechts)** – sammelt die Bezahl-Items der Käufer
- Beide Inventare haben eine feste Größe von **4×3 Slots (Breite × Höhe)**
- Regeln:
  - Der Owner muss Kauf-Items in das **Input-Inventar** legen
  - Bei einem erfolgreichen Kauf:
    - werden die Bezahl-Items automatisch in das **Output-Inventar** gelegt
  - Der Owner kann:
    - Items **nur aus dem Output-Inventar entnehmen**
    - **keine Items manuell in das Output-Inventar legen**

##### 3. Settings-Seite

Auf der Settings-Seite kann der Owner folgende Optionen konfigurieren:

- Setzen des **Shop-Namens**
- **Redstone-Signal** aktivieren oder deaktivieren
  - bei jedem Kauf wird (falls aktiviert) ein Redstone-Signal ausgelöst
- **Weitere Spieler als Owner hinzufügen**
- Vier kleine Konfigurations-Buttons:
  - Jeder Button hat drei Zustände:
    - *Nichts*
    - *Input*
    - *Output*
  - Diese Buttons definieren, ob und wie **erweiterbare Truhen** als Input- oder Output-Erweiterung genutzt werden

---

#### Zentrale Regeln für Slots

- Existiert ein Angebot:
  - **Spieler und Owner können keine Items in den Kauf-Slot legen**
- Auf der Inventarseite gilt:
  - Der Owner darf **keine Items in das Output-Inventar legen**, sondern nur entnehmen

---

#### Angebots-Vorschau und automatische Bezahl-Logik

Die **Angebots-Vorschau** existiert in zwei Zuständen:

1. **Live-Vorschau (beim Erstellen eines Angebots)**
   - zeigt Bezahl- und Kauf-Items mit korrekter Menge
   - besitzt **keine Interaktion**
   - dient ausschließlich zur Kontrolle durch den Owner

2. **Aktives Angebot (nach Erstellung)**
   - ist ein **Button**
   - erlaubt Interaktion durch Spieler

---

**Automatisches Einziehen der Bezahl-Items (SmallShop & ServerShop identisch):**

- Existiert ein aktives Angebot, kann ein Spieler auf die Angebots-Vorschau klicken
- Beim Klick werden:
  - die passenden Bezahl-Items automatisch aus dem Spielerinventar entnommen
  - und in die beiden Bezahl-Slots gelegt
- Dabei gilt:
  - es wird **immer die maximal mögliche Anzahl** aus dem Spielerinventar verwendet
  - nicht nur die exakt für einen Kauf benötigte Menge

Die **Angebots-Vorschau** stellt das aktuell existierende Angebot visuell dar und ist ein zentrales Interaktionselement des SmallShops.

- Die Angebots-Vorschau ist ein **Button**, auf dem:
  - die beiden **Bezahl-Items**
  - sowie das **Kauf-Item**
  - jeweils mit der **korrekten Item-Anzahl** angezeigt werden
- Dadurch ist für jeden Spieler jederzeit klar ersichtlich:
  - welche Items bezahlt werden müssen
  - und welches Item man dafür erhält

**Automatisches Einziehen der Bezahl-Items:**

- Existiert ein Angebot, kann ein Spieler auf die Angebots-Vorschau klicken
- Beim Klick werden:
  - die passenden Bezahl-Items automatisch aus dem Spielerinventar entnommen
  - und in die beiden Bezahl-Slots gelegt
- Dabei gilt:
  - es wird **immer die maximal mögliche Anzahl** aus dem Spielerinventar verwendet
  - nicht nur die exakt für einen Kauf benötigte Menge

---

#### Kauf-Interaktion und Anzeige der Kaufmenge

**Anzeige der Kaufmenge (SmallShop & ServerShop):**

- Im Kauf-Slot wird **immer exakt die im Angebot definierte Kaufmenge angezeigt**
  - **nicht** die maximal kaufbare Menge
  - **nicht** die Anzahl möglicher Käufe
- Möchte ein Spieler mehrere Kauf-Items erwerben:
  - klickt er **mehrfach manuell** auf das Kauf-Item
  - jedes Herausnehmen entspricht **einem einzelnen Kauf**
- Die Menge des Kauf-Items auf der Maus erhöht sich entsprechend

---

#### Kauf-Interaktion und Shift-Klick-Logik

Spieler haben zwei Möglichkeiten, Kauf-Items aus dem Kauf-Slot zu entnehmen:

1. **Manueller Kauf**
   - Der Spieler nimmt das Kauf-Item normal mit der Maus aus dem Kauf-Slot

2. **Schnellkauf per Shift-Mausklick**
   - Der Spieler kann per **Shift-Klick** mehrere Kauf-Items automatisch erhalten

Für den Shift-Klick-Kauf gelten folgende zwingende Regeln:

1. Es muss berechnet werden:
   - wie viele Bezahl-Items in den Bezahl-Slots liegen
   - wie viele Kauf-Items damit maximal gekauft werden könnten
2. Zusätzlich muss geprüft werden:
   - wie viele Kauf-Items im **Input-Inventar** verfügbar sind
   - ob im **Output-Inventar** noch genügend Platz vorhanden ist
3. Es dürfen **nur so viele Kauf-Items gekauft werden**, wie:
   - in das Spielerinventar passen
4. Ist das Spielerinventar **voll**:
   - ist der Shift-Klick-Kauf **nicht erlaubt**
   - der Spieler kann nur noch **manuell einzelne Käufe** durchführen

---

#### Kauf- und Angebotsregeln

Die folgenden Regeln gelten, **sobald ein Angebot existiert**:

1. Liegen die **richtigen Bezahl-Items** in korrekter Menge oder mehr in den Bezahl-Slots:
   - wird das **Kauf-Item im Kauf-Slot angezeigt**
2. Liegen **nicht genügend Bezahl-Items** in den Bezahl-Slots:
   - wird **kein Kauf-Item** im Kauf-Slot angezeigt
3. Sind genügend Bezahl-Items vorhanden, aber:
   - das **Input-Inventar enthält keine Kauf-Items mehr** (nur SmallShop),
   - wird dies durch ein **eindeutiges Icon und einen erklärenden Text** in der GUI angezeigt
4. Ist das **Output-Inventar voll** (nur SmallShop):
   - wird dies ebenfalls durch ein **Icon und einen erklärenden Text** signalisiert
   - der Spieler kann **kein Kauf-Item mehr kaufen**
   - das Kauf-Item darf **nicht im Kauf-Slot erscheinen**

---

#### Einheitliches visuelles Feedback (SmallShop & ServerShop)

Für alle relevanten Kaufzustände soll die GUI **klares, einheitliches Feedback** liefern:

- **Falsche oder zu wenige Bezahl-Items**
- **Inventar voll** (Spielerinventar)
- **Kein Kauf möglich** (z. B. Output voll oder keine Kauf-Items)

Das Feedback erfolgt immer durch:
- ein eindeutiges **Icon** (z. B. Barrier, Redstone, Warnsymbol)
- einen **klaren, kurzen Text**, der dem Spieler erklärt, warum der Kauf aktuell nicht möglich ist

Ziel:
- kein stilles Scheitern
- keine verwirrenden Zustände
- für Spieler jederzeit nachvollziehbar, **warum** etwas nicht funktioniert

Die folgenden Regeln gelten, **sobald ein Angebot existiert**:

1. Liegen die **richtigen Bezahl-Items** in korrekter Menge oder mehr in den Bezahl-Slots:
   - wird das **Kauf-Item im Kauf-Slot angezeigt**
2. Liegen **nicht genügend Bezahl-Items** in den Bezahl-Slots:
   - wird **kein Kauf-Item** im Kauf-Slot angezeigt
3. Sind genügend Bezahl-Items vorhanden, aber:
   - das **Input-Inventar enthält keine Kauf-Items mehr**,
   - wird dies durch ein **Icon und einen Text** in der GUI angezeigt
4. Ist das **Output-Inventar voll**:
   - wird dies ebenfalls durch ein **Icon und einen Text** signalisiert
   - der Spieler kann **kein Kauf-Item mehr kaufen**
   - das Kauf-Item darf **nicht im Kauf-Slot erscheinen**

---

#### Block- und Zugriffsregeln

- **Nur der Owner** (oder weitere eingetragene Owner) kann den SmallShop abbauen
  - Spieler ohne Owner-Rechte können den Block **nicht zerstören**
- Der SmallShop ist:
  - **explosionsgeschützt** (z. B. TNT, Creeper)
  - **nicht durch Pistons verschiebbar**

- Existiert **kein Angebot**:
  - können Spieler, die **nicht Owner** sind, die GUI **nicht per Rechtsklick öffnen**
  - stattdessen erhalten sie eine **Benachrichtigung**, dass noch kein Angebot existiert

- Baut der Owner den SmallShop ab:
  - werden **alle Items**, die sich
    - in den drei Angebots-Slots
    - im Input-Inventar
    - im Output-Inventar
    befinden,
  - vollständig in der Welt **gedroppt**

- Der Owner darf den SmallShop **wie ein normaler Spieler benutzen**:
  - der Owner kann selbst Käufe tätigen
  - dies dient u. a. zum **Testen des Angebots und der Einstellungen**

---

### ServerShop (GUI-basiert)

Der **ServerShop** ist ein rein GUI-basierter Shop ohne Block. Er dient als zentraler, serverweiter Shop mit mehreren Angebotsseiten und Angeboten.

- Öffnung über eine **Taste**
- Unterstützt mehrere Angebote gleichzeitig
- Funktional grob vergleichbar mit einem **Villager-Trade**, jedoch erweitert um Seiten, Listen und Verwaltungsfunktionen

---

#### View-Mode und Edit-Mode

Der ServerShop besitzt zwei klar getrennte Modi:

##### View-Mode (Standard)

- Der **View-Mode** ist die Standardsicht für **alle Spieler**
- In diesem Modus sichtbar:
  - Angebotsliste
  - Angebots-Vorschau
  - zwei Bezahl-Slots und ein Kauf-Slot
  - Seiten-Buttons auf der linken Seite

**Seiten-Buttons (links):**
- Dienen zum Wechseln zwischen Angebotsseiten
- Beispiel:
  - Seite 1: Werkzeuge
  - Seite 2: Nahrung
  - Seite 3: Rohstoffe

Spieler können im View-Mode:
- Angebote auswählen
- Bezahl-Items einlegen
- Kauf-Items erwerben

---

##### Edit-Mode (nur für Server-Owner)

- Der **Edit-Mode** kann **nur vom Server-Owner** aktiviert werden
- Umschaltung erfolgt über einen **Button oben rechts** in der GUI
- Nur der Server-Owner kann:
  - den Button sehen
  - den Modus wechseln

Im Edit-Mode stehen zusätzliche Verwaltungsfunktionen zur Verfügung.

---

#### Angebotsseiten-Verwaltung (Edit-Mode)

- Oben links über der GUI befindet sich der Button **„Add Page“**
- Beim Klick:
  - wird ein Name für die neue Angebotsseite eingegeben
- Nach Erstellung einer Seite erscheinen zusätzlich:
  - **„Rename Page“**
  - **„Delete Page“**

Diese Buttons gelten immer für die aktuell ausgewählte Angebotsseite.

---

#### Angebots-Erstellung im ServerShop

- Angebote werden **nur im Edit-Mode** erstellt
- Die Erstellung erfolgt grundsätzlich **wie beim SmallShop**:
  - Items werden in die zwei Bezahl-Slots und den Kauf-Slot gelegt

- **Während der Item-Platzierung**:
  - werden die Items **live in der Angebots-Vorschau angezeigt**
  - diese Ansicht dient nur zur Kontrolle
  - sie besitzt **keine Button-Funktion**

- Klickt der Server-Owner auf **„Add Offer“**:
  - wird die Live-Vorschau **vollständig entfernt**
  - das Angebot wird in die **Angebotsliste** der aktuell ausgewählten Angebotsseite eingefügt

- Klickt man ein Angebot in der Angebotsliste an:
  - wird es in der **Angebots-Vorschau** angezeigt
  - und folgende zusätzliche Buttons erscheinen:
    - **„Delete Offer“**
    - **„Move Up“**
    - **„Move Down“**

- Mit diesen Buttons kann der Server-Owner:
  - Angebote löschen
  - die Reihenfolge der Angebote innerhalb der Liste verändern

---

#### Kauf-Logik im ServerShop

Die **Kauf-Logik des ServerShops** orientiert sich grundsätzlich an der des **SmallShops**, mit folgenden **zentralen Unterschieden**:

- Es existieren **kein Input- und kein Output-Inventar**
- Kauf-Items sind vorerst **unendlich verfügbar**
- Bezahl-Items werden beim Kauf **einfach entfernt** (verbraucht)

##### Gemeinsame Grundlagen (wie SmallShop)

- Der Spieler legt Bezahl-Items in die **zwei Bezahl-Slots**
- Das aktuell ausgewählte Angebot bestimmt:
  - welche Items erlaubt sind
  - welche Menge benötigt wird
- Liegen genügend passende Bezahl-Items in den Bezahl-Slots:
  - wird das Kauf-Item im Kauf-Slot angezeigt
- Liegen nicht genügend oder falsche Items vor:
  - bleibt der Kauf-Slot leer

##### Unterschiede zur SmallShop-Logik

- Beim Kauf im ServerShop wird **nicht geprüft**:
  - ob Kauf-Items im Input-Inventar vorhanden sind
  - ob Platz im Output-Inventar existiert
- Stattdessen gilt:
  - Bezahl-Items werden beim Kauf **serverseitig entfernt**
  - Kauf-Items werden direkt dem Spieler gegeben

##### Shift-Kauf im ServerShop

Die **Shift-Klick-Logik** ist identisch zur SmallShop-Logik, mit angepassten Regeln:

- Es wird berechnet:
  - wie viele Käufe anhand der Bezahl-Items möglich sind
  - wie viele Kauf-Items ins Spielerinventar passen
- Da Kauf-Items unendlich sind:
  - entfällt jede Prüfung auf verfügbare Kauf-Items
- Es dürfen nur so viele Kauf-Items vergeben werden, wie:
  - das Spielerinventar aufnehmen kann
- Ist das Spielerinventar voll:
  - ist der Shift-Kauf **nicht erlaubt**
  - nur manuelle Einzelkäufe sind möglich

##### Wichtige Einschränkung im View-Mode

- Im **View-Mode** gilt zwingend:
  - **Niemand** (weder Spieler noch Server-Owner)
  - kann Items manuell in den **Kauf-Slot** legen

Der ServerShop befindet sich aktuell:
- stark in Entwicklung
- mit mehreren bekannten Problemen
- noch nicht in einem finalen Zustand

---

## Technische Basis

- **Modloader:** NeoForge
- **Minecraft-Version:** 1.21.1
- **Schwerpunkte:**
  - GUI- und ScreenHandler-Logik
  - Inventar- und Slot-Management
  - Client-Server-Synchronisation

---

## Hinweis zum Dokument

Dieses Dokument ist **kein finaler Text**, sondern eine **lebende Mod-Beschreibung**. Es dient als:

- Kontext für KI-Tools (z. B. Jules, Gemini)
- Grundlage für Dokumentation / README
- Referenz für Bugfixes und Refactorings

Der Text ist bewusst modular aufgebaut und kann jederzeit erweitert, angepasst oder umstrukturiert werden.

