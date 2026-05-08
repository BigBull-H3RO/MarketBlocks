# MarketBlocks – Modbeschreibung

## Überblick

**MarketBlocks** ist ein Minecraft-Mod für **NeoForge** (Minecraft **1.21.1**) mit integrierten, serverautoritativen Handelssystemen.

Ziele:
- Handel direkt im Spiel ohne externe Plugin-Logik
- klare, sichere Kaufabläufe
- keine Item-Duplikation / kein stiller Itemverlust
- konsistente Client-Server-Synchronisation

---

## Begriffe (aktualisiert)

Die bisherigen Namen wurden umgestellt:

- **SmallShop** heißt jetzt **SingleOfferShop**
- **ServerShop** heißt jetzt **Marketplace**

Hinweis: Die ältere Schreibweise **MarketPlace** bezeichnet dasselbe System.

Dieses Dokument startet mit dem vollständig überarbeiteten **SingleOfferShop**-Teil.

---

## Shop-Typen (aktueller Stand)

- **SingleOfferShop** (blockbasiert, ein Angebot pro Shop)
- **Marketplace** (GUI-basiert, zentraler Server-Shop)

---

## SingleOfferShop (blockbasiert)

Der SingleOfferShop ist ein platzierbarer Shop-Block mit genau einem aktiven Angebot gleichzeitig.

### Block-Varianten

Aktuell registrierte Varianten für SingleOfferShop:
- **Trade Stand** (`trade_stand`)
  - optional mit zuschaltbarer Vitrine (`has_showcase`) inkl. Top-Block
- **Market Crate** (`marketcrate`)
- (zusätzlich intern/testnah: `shop_block_test`)

Alle Varianten nutzen dieselbe SingleOfferShop-BlockEntity-Logik, aber unterschiedliche Shape-/Render-Konfigurationen.

---

### Ownership- und Zugriffsmodell

- Beim Platzieren wird der Spieler zum **Primary Owner**.
- Zusätzliche Owner können gespeichert werden.
- Nur Owner dürfen:
  - Angebot erstellen/löschen
  - Shop-Einstellungen ändern
  - Input/Output-Inventare verwalten
  - Shop abbauen
- **Primary Owner** hat exklusive Rechte für Owner-Liste-Verwaltung und Log-Clear.
- Falls Owner-Daten fehlen, ist Recovery nur mit Admin-Rechten (OP / Singleplayer-Owner) erlaubt.

Öffnen ohne Angebot:
- Nicht-Owner können den Shop ohne aktives Angebot nicht öffnen und erhalten eine Meldung.

---

### Tabs und UI-Struktur

Der SingleOfferShop nutzt ein einheitliches Menü mit Tabs:

1. **Offers**
2. **Inventory**
3. **Settings**
4. **Log**

Tab-Berechtigungen:
- **Offers**: alle mit Zugriff
- **Inventory**: nur Owner, und nur wenn Admin-Shop-Modus aus ist
- **Settings**: Owner; zusätzlich OP bei global aktivem Admin-Modus
- **Log**: nur Owner

Tab-Wechsel werden serverseitig validiert und synchronisiert.

---

### Angebots-Lifecycle

#### Erstellung
- Owner legt Items in:
  - 2 Payment-Slots
  - 1 Result-/Offer-Slot
- Client zeigt Vorschau; finale Validierung läuft serverseitig im `OfferManager`.
- Regeln:
  - Result darf nicht leer sein
  - mindestens ein Payment-Item erforderlich
  - Slots müssen serverseitig exakt/ordnungstolerant konsistent sein

#### Normalisierung
- Wenn **Payment-Slot 1 leer** und **Payment-Slot 2 befüllt** ist, wird das Item aus Slot 2 als Payment 1 übernommen.
- Ist Payment-Slot 1 bereits befüllt, erfolgt keine automatische Umsortierung.

#### Löschen
- Owner kann Angebot löschen.
- Angebotsstatus wird an betroffene Clients synchronisiert.

---

### Kauflogik (Single- und Shift-Kauf)

#### Grundlogik
Ein Kauf ist nur möglich, wenn gleichzeitig erfüllt:
- aktives Angebot vorhanden
- ausreichende Payment-Items in den Payment-Slots
- Ergebnis-Item verfügbar (außer Admin-Shop-Modus)
- genügend Output-Platz für Payment-Ablage (außer Admin-Shop-Modus)

#### Payment-Regeln
- ein oder zwei Payment-Items möglich
- bei identischen Payment-Itemtypen wird die Gesamtmenge korrekt zusammengefasst

#### Offer-Slot-Sicherheit
- bei aktivem Angebot ist Entnahme **all-or-nothing** (keine Teilentnahme)

#### Shift-Kauf / Bulk-Kauf
- serverseitig atomisch berechnet anhand von:
  1) Bezahlbarkeit
  2) Bestand (Input)
  3) Output-Kapazität
  4) Spielerinventar-Kapazität
- Ergebnis: tatsächliche Kaufanzahl wird exakt ausgeführt und geloggt

#### Auto-Fill
- Klick auf Offer-Preview kann Payment-Slots automatisch aus Spielerinventar befüllen.

---

### Inventare und Slot-Regeln

Interne Shop-Inventare:
- **Input**: 12 Slots (4×3)
- **Output**: 12 Slots (4×3)
- **Payment**: 2 Slots
- **Offer**: 1 Slot

Regeln:
- Output-Slots sind nicht manuell befüllbar (nur Entnahme durch Owner)
- Offer-Slot ist bei aktivem Angebot nicht manuell überschreibbar
- Settings/Log blenden Spielerinventar-Slots im Menü aus

Beim Abbau werden Shop-Inhalte gedroppt.
Begriff **Template-Fall**: Zustand ohne aktives Angebot (`hasOffer = false`), in dem der Offer-Slot nur als Bearbeitungs-/Vorschau-Slot für die Angebotserstellung dient. In diesem Zustand wird dessen Inhalt beim Abbau separat berücksichtigt.

---

### Admin-Shop-Modus

Zusätzlicher Modus pro Shop (`adminShopEnabled`):
- aktivierbar nur durch OP
- zusätzlich muss globaler Admin-Modus in Config aktiv sein (`marketblocksAdminModeEnabled`)

Effekte bei aktivem Admin-Shop:
- keine Input-Bestandsprüfung
- keine Output-Platzprüfung
- Inventory-Tab wird deaktiviert
- Kauf funktioniert als „unendlicher“ Shop für Result-Items

---

### Redstone- und Comparator-Verhalten

- Optionaler Redstone-Puls pro erfolgreichem Kauf (`emitRedstone`)
- Pulsdauer: 20 Ticks (POWERED true -> false)
- Comparator-Signal basiert seitenspezifisch auf konfiguriertem Input-/Output-Füllstand

---

### Side-Modes & Chest-I/O-Erweiterung (experimentell)

Pro relevanter Seite kann konfiguriert werden:
- `DISABLED`
- `INPUT`
- `OUTPUT`

Ausrichtung in Settings:
- links, rechts, unten, hinten (relativ zur Shop-Facing-Richtung)

Experimentelle Chest-I/O (`enableChestIoExtensionExperimental`):
- Pull aus INPUT-Nachbarinventaren in Shop-Input
- Push aus Shop-Output in OUTPUT-Nachbarinventare
- periodisch über Config-Intervalle (`offerUpdateInterval`, `chestIoInterval`)

Chest-Security:
- angrenzende I/O-Truhen sind für Nicht-Owner blockiert
- Double-Chest-Verhalten ist konfigurierbar (`enableDoubleChestSupport`)

---

### Status-Feedback in Offers-Ansicht

UI zeigt Kaufblocker klar an:
- **Out of Stock** (kein passender Bestand)
- **Output Full / Almost Full**

`Output almost full` wird über konfigurierbaren Schwellwert gesteuert (`enableOutputWarning`, `outputWarningPercent`).

---

### Visual-System (Shop-NPC + Effekte)

SingleOfferShop unterstützt visuelle Shop-Darstellung:
- optionaler Visual-NPC
- Name + Profession
- Kauf-Partikel
- Kauf-Sounds
- Payment-Slot-Sounds (Match/Fail-Feedback)
- XP-Sound-Feedback bei Kauf (auch für Bulk-Käufe, mit Pitch-Skalierung)

Spawn wird vor Aktivierung validiert (Platzprüfung); bei Blockade wird NPC-Aktivierung zurückgenommen.

Außerdem rendert der Shop:
- Angebots-Item (je nach Variante schwebend/gestapelt)
- Payment-Items vorne am Block
- Mengen-Text
- bei Market Crate zusätzlich Front-Offer + Trade-Arrow

---

### Transaktionslog

Jeder SingleOfferShop führt ein eigenes Kauf-Log:
- Käufer (UUID/Name)
- bezahlte Items
- erhaltene Items
- Kaufart (Single / Shift)
- Zeitstempel

Eigenschaften:
- persistiert als SavedData
- pro Shop begrenzte Historie (aktuell max. 100 Einträge)
- Sync in die UI bei Log-Tab-Wechsel per dediziertem Packet
- Clear nur für Primary Owner

---

### Netzwerk- und Synchronisationsprinzipien

SingleOfferShop nutzt dedizierte Packets u. a. für:
- Offer erstellen/löschen
- Auto-Fill
- Tab-Wechsel
- Settings-Update (inkl. Visuals/I/O/Name/Redstone/XP-Sound)
- Owner-Update
- Admin-Shop-Toggle
- Log-Sync und Log-Clear

Sicherheitsprinzipien:
- Server validiert Berechtigungen und Zustände
- Client bekommt nur notwendige, UI-relevante Shopdaten (kein vollständiges Inventar-NBT)

---

## Marketplace

Der Marketplace-Teil wurde bereits technisch auf den neuen Namen umgestellt, wird aber in diesem Dokument als nächster Schritt separat detailliert überarbeitet.

---

## Technische Basis

- **Modloader:** NeoForge
- **Minecraft-Version:** 1.21.1
- **Schwerpunkte:** GUI, Inventar-/Slotlogik, Client-Server-Sync, sichere Handelslogik

---

## Hinweis

Dieses Dokument ist eine lebende Spezifikation und wird fortlaufend an den tatsächlichen Codezustand angepasst.
