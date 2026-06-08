# MarketBlocks – Technische Spezifikation

## 1. Ziel und Scope

**MarketBlocks** ist ein NeoForge-Mod (Minecraft **1.21.1**) für serverautoritativen Ingame-Handel.

Der Mod enthält zwei unabhängige, aber konzeptionell verwandte Handelssysteme:

1. **SingleOfferShop** (blockbasiert, ein aktives Angebot pro Shop)
2. **Marketplace** (blockloses, seitenbasiertes Angebotssystem)

Kernziele:
- valide und deterministische Kaufabwicklung auf dem Server
- klare Rechte-/Rollenmodelle
- robuste Persistenz und Wiederherstellbarkeit
- minimierte Client-Vertrauensannahmen

---

## 2. Terminologie und Legacy-Namen

Aktuelle Namen:
- **SingleOfferShop** (früher: **SmallShop**)
- **Marketplace** (früher: **ServerShop**)

Legacy-Schreibweise:
- **MarketPlace** bezeichnet dasselbe System wie Marketplace.

---

## 3. Mod-Architektur (High Level)

### 3.1 Einstieg und Registrierung
- Mod-Einstieg: `MarketBlocks`
- Registries: `RegistriesInit`
  - Blöcke: `trade_stand`, `marketcrate`, intern `shop_block_test`, Top-Block `trade_stand_top`
  - BlockEntity: `single_offer_shop`
  - Menüs: `single_offer_shop_menu`, `marketplace_menu`
- Creative Tab: `CreativeTabInit`

### 3.2 Event-Lifecycle
- Serverstart: `MarketplaceManager.initialize(...)`
- Servertick: `MarketplaceManager.tick()`
- Serverstop: `MarketplaceManager.shutdown()`

### 3.3 Netzwerk
- Zentrale Registrierung aller Payloads: `NetworkHandler`
- Strikte Trennung zwischen SingleOfferShop- und Marketplace-Paketgruppen
- Paketstruktur: `network.singleoffer` und `network.marketplace` (keine CamelCase-Paketnamen)
- Server synchronisiert Marketplace per vollständigem Snapshot + offer-spezifischen Runtime-ViewStates

### 3.4 Paket- und Abhängigkeitsregeln
- Feature-Slices liegen unter `feature/*` (z. B. `feature/singleoffer`, `feature/marketplace`, `feature/visual`, `feature/log`)
- UI-Bausteine liegen unter `client/gui`, Feature-spezifische Screens/Renderer unter `feature/*/client/*`
- `util` ist für echte, generische Hilfsklassen reserviert (keine Screen-/Render-/Block-Config-Logik)
- Erlaubte Richtung: `feature/*` -> `core/*` (bzw. bestehende zentrale Initialisierungs-/Event-Pakete)
- Zu vermeiden: direkte Feature-zu-Feature-Abhängigkeiten

---

## 4. Shop-Typen

### 4.1 SingleOfferShop (blockbasiert)

### 4.1.1 Blockvarianten
- `trade_stand` (optional mit Showcase/Top-Block)
- `marketcrate`
- `shop_block_test` (intern/testnah)

Alle Varianten laufen über dieselbe BlockEntity-Logik: `SingleOfferShopBlockEntity`.

### 4.1.2 Inventar- und Slotmodell
Interne Handler:
- Input: **12 Slots**
- Output: **12 Slots**
- Payment: **2 Slots**
- Offer: **1 Slot**

Wichtige Regeln:
- Output-Slots: keine manuelle Befüllung
- Offer-Slot bei aktivem Angebot: all-or-nothing Entnahme
- bei aktivem Angebot ist Offer-Slot ein servervalidierter Kaufslot
- ohne aktives Angebot ist Offer-Slot Template-/Bearbeitungsslot (Owner-only)

### 4.1.3 Angebotsmodell
Ein Angebot besteht aus:
- `offerPayment1`
- `offerPayment2`
- `offerResult`
- `hasOffer`

Erstellung erfolgt servervalidiert über `OfferManager`:
- Result darf nicht leer sein
- mindestens ein Payment-Stack nötig
- Client-Vorschlag muss mit Server-Slots konsistent sein (inkl. Count/Components)
- Payment-Slots werden semantisch geprüft (ordnungstolerant)

Normalisierung:
- wenn Payment1 leer und Payment2 gesetzt, wird intern zu Payment1 verschoben.

### 4.1.4 Kaufpfad
Kauf ist nur möglich, wenn:
- Angebot aktiv
- Payment erfüllt
- Result verfügbar (außer Admin-Shop)
- Output-Aufnahme möglich (außer Admin-Shop)

Bulk-/Shift-Kauf:
- `processBulkPurchase(...)` berechnet transaktionssicher:
  1) Affordability (Payment)
  2) Bestand (Input)
  3) Output-Kapazität (inkl. Simulation)
  4) Zielinventar-Kapazität (Menü-seitig)
- effektive Kaufanzahl wird atomar durchgeführt
- bei Erfolg: Sync, optional Redstone-Puls, Log-Eintrag, visuelle Counter

Sonderfall gleiche Payment-Items:
- Wenn Payment1 und Payment2 derselbe Itemtyp sind, wird gegen die Summe beider Required Counts geprüft.

### 4.1.5 Ownership und Berechtigungen
- Primary Owner wird beim ersten legitimen Ownership-Set gesetzt
- zusätzliche Owner möglich
- `ShopOwnerManager` verwaltet Owner + zusätzliche Owner

UI-Rechte (`SingleOfferShopMenu`):
- **Offers**: grundsätzlich zugänglich (mit Shop-Zustandsregeln)
- **Inventory**: nur Owner und nur wenn Admin-Shop aus
- **Settings**: Owner oder OP bei global aktiviertem Admin-Modus
- **Log**: nur Owner

Primary-Owner-spezifische Rechte (z. B. Log-Clear/Owner-Verwaltung) werden paketseitig separat geprüft.

### 4.1.6 Admin-Shop-Modus
`adminShopEnabled` (pro Shop):
- Toggle nur durch OP
- zusätzlich nur bei globalem `marketblocksAdminModeEnabled = true`

Effekt:
- kein Input-Bestand nötig
- keine Output-Kapazitätsprüfung
- Inventory-Tab wird aus Sicherheits-/UX-Gründen unzugänglich

### 4.1.7 Side-Modes und Chest-I/O (experimentell)
`SideMode`: `DISABLED`, `INPUT`, `OUTPUT`

Aktiv über:
- `enableChestIoExtensionExperimental`

Pro Tick-Intervall:
- Pull aus INPUT-Nachbarn in Shop-Input
- Push aus Shop-Output in OUTPUT-Nachbarn

Nachbarhandler werden gecacht (`ShopInventoryManager`) und bei Config aus deaktiviert.

### 4.1.8 Comparator/Redstone
- Comparator liest seitenabhängig Input/Output-Füllstand
- optionaler Redstone-Puls je erfolgreichem Kauf (`emitRedstone`), Dauer 20 Ticks

### 4.1.9 Visual-System
`IVisualShopNPC` + `ShopVisualSettings`:
- NPC an/aus
- NPC-Name + Beruf
- Purchase-Partikel
- Purchase-Sounds
- Payment-Slot-Sounds (Success/Fail-Counter)
- XP-Feedback-Sound (inkl. Pitch-Skalierung für Bulk-Käufe)

NPC-Aktivierung wird vor Anwendung auf Platzierbarkeit geprüft.

### 4.1.10 Transaktionslog
Persistenz: `ShopTransactionLogSavedData` (persistiert als SavedData, nicht als Chunk-NBT)

Eintrag: `TransactionLogEntry`
- Käufer UUID/Name
- bezahlte Stacks
- gekaufte Stacks
- Kaufart: SINGLE/SHIFT
- Zeitstempel
- Aggregationszähler

- zeitnah identische Käufe desselben Käufers werden zusammengeführt.

### 4.1.11 Benachrichtigungen (Offline Notifications)
`PendingNotificationsSavedData` (persistiert als SavedData):
- Speichert Events für Shop-Owner, falls diese offline sind oder sich nicht in der Nähe befinden.
- **Out of Stock**: Benachrichtigung, wenn der Input-Vorrat eines Shops aufgebraucht ist.
- **Output Full**: Benachrichtigung, wenn das Output-Inventar keine weiteren Items mehr aufnehmen kann.
- Beim Login oder bei Rückkehr wird der Spieler über diese Zustände gesammelt informiert.

### 4.1.12 Advancements (Errungenschaften)
Umfangreiches Set an Triggern für den SingleOfferShop:
- `ShopNpcTrigger` / `ShopNpcCustomizeTrigger`: Platzierung und Anpassung des Visual NPCs.
- `ShopSellTrigger` / `ShopWholesalerTrigger`: Erster Verkauf und Massenverkäufe (getrackt via `ShopSellCountSavedData`).
- `ShopRedstoneTrigger` / `ShopAutoIoTrigger`: Nutzung der Automatisierungs-Features.
- `ShopAdminModeTrigger` / `ShopCoOwnerTrigger` / `ShopOutOfStockTrigger`: Management-Events.

---

### 4.2 Marketplace (ehem. ServerShop / MarketPlace)

Der Marketplace ist ein **blockloses**, zentral verwaltetes Handelssystem mit persistentem JSON-Backstore.

### 4.2.1 Öffnen und Zugriff
Öffnen möglich via:
- Keybind (Standard: **O**) -> `MarketplaceOpenRequestPacket`
- Command `/marketblocks marketplace`

Editorrechte:
- grundsätzlich OP-Level (`hasPermissions(2)`)
- tatsächliche Edit-Nutzung zusätzlich an globales Admin-Flag gekoppelt (`marketblocksAdminModeEnabled`)

### 4.2.2 Datenmodell
Root:
- `MarketplaceData` -> Liste von `MarketplacePage`

Page:
- `name`
- optional `icon`
- Liste `MarketplaceOffer`

Offer:
- `id` (UUID)
- `result`
- `payments` (max. 2; intern auf zwei Slots normalisiert)
- `limits` (`OfferLimit`)
- `pricing` (`DemandPricing`)
- `runtimeState` (`MarketplaceOfferRuntimeState`)

### 4.2.3 Offer-Limits
`OfferLimit` unterstützt:
- unlimited
- optional daily limit
- optional stock limit
- optional restock seconds

Werte <= 0 werden als „nicht gesetzt“ behandelt.

### 4.2.4 Demand Pricing
`DemandPricing`:
- enabled
- base multiplier
- demand step
- min/max multiplier

Effektive Payment-Kosten:
- `effectivePayments()` skaliert Count je Payment-Stack via Multiplikator
- Rundung nach oben (mindestens 1)

### 4.2.5 Runtime-State je Offer
`MarketplaceOfferRuntimeState` enthält:
- stockRemaining
- purchasedTodayGlobal
- purchasedTodayByPlayer
- lastDailyResetDay
- lastRestockGameTime
- demandPurchases
- lastDemandDecayDay

### 4.2.6 Runtime-Upkeep
`MarketplaceManager.tick()` führt periodisch aus:
- Daily-Reset bei aktivem Daily-Limit
- Restock für stock-limitierte Offers
- Demand-Decay (pro Tag)
- Viewer-Resync bei Runtime-Änderungen

### 4.2.7 Kaufabwicklung
Kaufpfad: `processPurchaseTransactionSlotBased(...)`

Ablauf:
1. Offer finden + Runtime-Upkeep anwenden
2. Maximum aus Limits für aktuellen Spieler bestimmen
3. bei Überschreitung: translatierte Fehlermeldung (Daily-Limit/Out-of-Stock)
4. Runtime-Counter fortschreiben (Stock, Daily, Demand)
5. Zustand markieren + offene Viewer synchronisieren

Wichtig:
- MarketplaceMenu verwaltet Payment-/Result-Template-Slots
- Zahlungsabzug/Slot-Interaktion erfolgt im Menu; Limit-/Runtime-Commit im Manager

### 4.2.8 Global vs. per-player Daily-Limit
Config:
- `marketplaceGlobalDailyLimit`

Wenn aktiv:
- alle Spieler teilen einen globalen Daily-Counter pro Offer
Wenn inaktiv:
- Daily-Counter wird pro Spieler getrennt geführt

### 4.2.9 Editor-Flow (Marketplace)
Editor-Funktionen über Pakete:
- Seite erstellen/umbenennen/löschen
- Offer hinzufügen/verschieben/löschen
- Limits aktualisieren
- Pricing aktualisieren

Alle Mutationen:
- serverseitig validiert
- bei Erfolg: Sync an offene Viewer
- bei Fehler: direkte User-Message

### 4.2.10 View-/Sync-Modell
Server -> Client:
- `MarketplaceSyncPacket` mit
  - vollständigem Daten-Snapshot
  - offer-spezifischen `MarketplaceOfferViewState`
  - Rechtebits (`canEdit`, `globalEditModeEnabled`)

`MarketplaceOfferViewState` enthält:
- maxPurchasable
- remainingDailyPurchases (optional)
- remainingStock (optional)
- restockSecondsRemaining (optional)
- priceMultiplier

Clientcache:
- `MarketplaceClientState`

### 4.2.11 Persistenz und Dateisicherheit
Datei:
- `<world>/marketblocks/marketplace.json`

Speicherstrategie:
- async I/O Single-Thread-Executor
- temporäre Datei + Move/Replace
- Backup-Datei (`.bak`)
- Restore von Backup bei defekter Primärdatei

### 4.2.12 Command-Integration
Unter `/marketblocks marketplace`:
- öffnen
- `reload` (JSON neu laden)
- `resetlimits <player>` (Daily-Limit-Zustand für Spieler zurücksetzen)

Globaler Admin-Modus:
- `/marketblocks adminmode [true|false]`
- beeinflusst Marketplace-Edit und OP-Settingsrechte im SingleOfferShop

---

## 5. Gemeinsame Sicherheits- und Konsistenzprinzipien

1. **Serverautorität**
   - kritische Aktionen werden serverseitig validiert
2. **Zustandskapselung**
   - Client erhält nur UI-relevante Daten
   - sensible Inventarzustände werden nicht vollständig via UpdateTag repliziert
3. **Deterministische Slotlogik**
   - Payment/Result-Checks nutzen Item + Components + Count
4. **Fehlerrobustheit**
   - defensive Parsing-/Fallback-Strategien bei NBT/JSON
5. **Synchronisation offener Menüs**
   - relevante Zustandsänderungen triggern gezielte Viewer-Syncs

---

## 6. Relevante Config-Schalter (Common)

### SingleOfferShop
- `enableDoubleChestSupport`
- `enableChestIoExtensionExperimental`
- `offerUpdateInterval`
- `chestIoInterval`
- `enableOutputWarning`
- `outputWarningPercent`

### Marketplace / global
- `marketplaceGlobalDailyLimit`
- `marketblocksAdminModeEnabled`

### Visual NPC
- `visualNpcForceOffscreenRendering`
- `visualNpcRenderViewDistance`

### Debug
- `enableMixinDesyncLogging`

---

## 7. UI-Struktur (Kurzreferenz)

### SingleOfferShop UI
Tabs:
- Offers
- Inventory
- Settings
- Log

### Marketplace UI
- Seiten-Sidebar
- Offer-Liste + Scroller
- Preview-Template
- optionaler Edit-Mode mit Create/Delete/Move/Rename + Limits/Pricing-Dialogen

---

## 8. Wichtige Klassen (Code-Navigation)

### SingleOfferShop
- `shop/singleoffer/block/entity/SingleOfferShopBlockEntity`
- `shop/singleoffer/block/entity/OfferManager`
- `shop/singleoffer/block/entity/ShopInventoryManager`
- `shop/singleoffer/block/entity/ShopOwnerManager`
- `shop/singleoffer/menu/SingleOfferShopMenu`

### Marketplace
- `shop/marketplace/MarketplaceManager`
- `shop/marketplace/MarketplaceData`
- `shop/marketplace/MarketplacePage`
- `shop/marketplace/MarketplaceOffer`
- `shop/marketplace/MarketplaceOfferRuntimeState`
- `shop/marketplace/MarketplaceRuntimeMath`
- `shop/marketplace/menu/MarketplaceMenu`
- `util/screen/marketplace/MarketplaceScreen`

### Cross-cutting
- `network/NetworkHandler`
- `event/MarketBlocksEvents`
- `config/Config`
- `shop/log/ShopTransactionLogSavedData`
- `feature/notification/PendingNotificationsSavedData`
- `feature/singleoffer/advancement/*Trigger`

---

## 9. Status

Diese Spezifikation beschreibt den aktuellen Codezustand und ersetzt die vorherige, unvollständige Marketplace-Dokumentation.
