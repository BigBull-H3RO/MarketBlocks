# Phase 6: Performance & Optimization

**Review-Datum:** 2026-04-02  
**Reviewer:** AI Senior Mod Developer  
**Betroffene Bereiche:**
- Server-Shop Runtime/Sync-Performance
- SmallShop Kauf-/Inventar-Hotpaths
- Menu-Hotpaths (Shift-Click/Bulk-Kauf)

---

## Ziel

Phase 6 reduziert Tick- und Sync-Kosten unter Last (viele Spieler, viele Offers, häufige Käufe), ohne Feature-Verhalten zu ändern.

---

## Implementierte Optimierungen

### 1) Runtime-Upkeep im ServerShopManager gedrosselt und dedupliziert

**Änderung:**
- Runtime-Upkeep nicht mehr jeden Tick, sondern in `RUNTIME_UPKEEP_INTERVAL_TICKS`
- zusätzliche Deduplizierung über `lastRuntimeUpkeepGameTime` + `lastRuntimeUpkeepDay`
- Marker-Reset bei `initialize()`, `shutdown()`, `loadFromDisk()`

**Nutzen:**
- deutlich weniger Voll-Scans über alle Offers pro Tick
- vermeidet redundante Upkeep-Läufe im selben Zeitpunkt

---

### 2) Snapshot-/ViewState-Encoding wiederverwendet

**Änderung:**
- Snapshot (`ServerShopData`) wird pro Sync-Zyklus einmal encoded und wiederverwendet
- bei globalem Daily-Limit werden Offer-ViewStates einmal berechnet/encoded und für alle Viewer genutzt

**Nutzen:**
- weniger NBT-Serialisierung
- geringere CPU-Last bei vielen offenen Shop-Menüs

---

### 3) Lock-Contention im ServerShopManager reduziert

**Änderung:**
- neue Batch-Strategie:
  - unter `synchronized(lock)` nur Daten/Snapshots/Targets sammeln
  - Menü-Updates + `sendToPlayer(...)` außerhalb des Locks
- umgesetzt in:
  - `tick()`
  - `syncOpenViewers(...)`
  - `reload()`
  - `setGlobalEditModeEnabled(...)`
- `processPurchaseTransactionSlotBased(...)` triggert Sync jetzt nach Verlassen des Locks

**Nutzen:**
- kürzere Lock-Haltezeit
- weniger Blockierung konkurrierender Shop-Operationen

---

### 4) SmallShopBlockEntity Hotpath-Optimierungen

**Änderung:**
- `Direction.values()`-Allokationen durch statisches Array ersetzt
- `isOfferAvailable()` nutzt Offer-Slot-Cache statt teurer Vollprüfung
- Tick-Refresh nutzt `updateOfferSlot(true)` für gezielten Neighbor-Check
- Bulk-Purchase: batched Removal (`executeTrades(...)`) statt wiederholter Vollscans pro Trade

**Nutzen:**
- weniger Garbage + weniger wiederholte Inventory-Scans
- bessere Skalierung bei Multi-Käufen

---

### 5) SmallShopMenu Hotpath-Optimierungen

**Änderung:**
- Bulk-Kauf-Kapazität wird in einem Pass berechnet (`calculatePlayerTransferCapacity(...)`)
- redundante Mehrfach-Scans für Transfer-Prechecks entfernt
- Mengenrechnung auf `long` gehärtet (Overflow-Schutz bei großen Mengen)
- `Direction.values()`-Allokationen gecached

**Nutzen:**
- weniger CPU-Kosten bei Shift-Click/Bulk-Käufen
- robustere Mengenbehandlung

---

### 6) Kleine Zusatzoptimierungen

**Änderung:**
- redundantes `shopEntity.sync()` in `OfferManager.applyOffer()` entfernt (bereits durch `createOffer()` abgedeckt)
- `ShopInventoryManager` nutzt statisches `Direction[]` in Iterationen

---

## Verifikation

- `./gradlew compileJava` ✅
- `./gradlew build` ✅

---

## Empfohlene Lasttests (manuell)

- 10+ Spieler mit geöffnetem Server-Shop gleichzeitig
- mehrere Seiten mit vielen Offers, Limits und Restock aktiv
- wiederholte Bulk-Käufe per Shift-Click
- parallel: Angebotsänderungen + Käuferzugriffe

