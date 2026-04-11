# Phase 5: Registration & Lifecycle Fixes

**Review-Datum:** 2026-04-02  
**Reviewer:** AI Senior Mod Developer  
**Betroffene Bereiche:**
- Registrierung & Event-Lifecycle
- Netzwerk-Lifecycle & Packet-Hardening
- Capability-Lifecycle
- Datagen-Workflow

---

## Ziel

Phase 5 stabilisiert den Mod für einen Release-Betrieb mit Fokus auf:
- klarere Lifecycle-Grenzen (Client/Server, Mod/Game)
- robustere Packet-Verarbeitung gegen fehlerhafte Inputs
- reproduzierbare Datagen-Läufe
- sauberes Capability-Verhalten bei Remove/Chunk-Unload

---

## Implementierte Fixes

### 1) Client-Events in Mod-/Game-Lifecycle aufgeteilt

**Warum:** `ClientEvents` enthielt Mod-Registrierungsevents und Tick-Game-Events zusammen.  

**Änderung:**
- `ClientEvents` behält Screen/Renderer/KeyMapping-Registrierung
- neuer `ClientGameEvents` übernimmt nur `ClientTickEvent.Post`

**Dateien:**
- `event/ClientEvents.java`
- `event/ClientGameEvents.java`

**Hinweis:** `EventBusSubscriber.bus` ist in der verwendeten NeoForge-Version deprecated; Bus-Routing erfolgt automatisch anhand der Event-Typen.

---

### 2) Chest-Security Capability-Registration vom Gameplay entkoppelt

**Warum:** Capability-Registrierung und Gameplay-Interaktionslogik waren in einer Klasse gemischt.  

**Änderung:**
- neue Klasse `ChestSecurityCapabilities` nur für Capability-Registration
- `ChestSecurityHandler` enthält nur Gameplay-Events (Place/Interact)

**Dateien:**
- `event/ChestSecurityCapabilities.java`
- `event/ChestSecurityHandler.java`

---

### 3) Network-Handshake für Release gehärtet

**Warum:** `PayloadRegistrar.optional()` erlaubt Verbindungen ohne vollständige Kanal-/Version-Kompatibilität.

**Änderung:**
- `.optional()` entfernt
- Protokollversion als Konstante zentralisiert (`PROTOCOL_VERSION`)

**Datei:**
- `network/NetworkHandler.java`

---

### 4) C2S Packet-Handler gegen invaliden Kontext abgesichert

**Warum:** Mehrere Handler haben `context.player()` direkt auf `ServerPlayer` gecastet.

**Änderung:**
- Guard-Pattern `if (!(context.player() instanceof ServerPlayer player)) return;` ergänzt

**Dateien:**
- `network/packets/smallShop/AutoFillPaymentPacket.java`
- `network/packets/smallShop/DeleteOfferPacket.java`
- `network/packets/smallShop/SwitchTabPacket.java`
- `network/packets/smallShop/UpdateOwnersPacket.java`
- `network/packets/smallShop/UpdateRedstoneSettingPacket.java`
- `network/packets/smallShop/UpdateSettingsPacket.java`

---

### 5) Packet-Input-Limits & Decoding-Hardening

**Warum:** Unbegrenzte Owner-Listen und unvalidierte Enum-Decodes sind unnötige Angriffs-/Fehlerflächen.

**Änderung:**
- Owner-Updates auf `MAX_OWNERS_PER_UPDATE = 64` begrenzt (encode/decode/handle)
- invalide `SideMode`-Strings im Settings-Packet werden auf `DISABLED` fallbacken + Warnlog

**Dateien:**
- `network/packets/smallShop/UpdateOwnersPacket.java`
- `network/packets/smallShop/UpdateSettingsPacket.java`

---

### 6) UI-Sync ohne direkte Client-Klassen in Common-Packetcode

**Warum:** UI-Rebuild hing von packetseitigem Zugriff auf konkrete Screen-Klassen ab.

**Änderung:**
- `OfferStatusPacket` aktualisiert nur Shop-State/Menu-State
- `SmallShopScreen` rebuildet Offers-UI bei `hasOffer`-Statewechsel selbst in `containerTick()`

**Dateien:**
- `network/packets/smallShop/OfferStatusPacket.java`
- `util/custom/screen/SmallShopScreen.java`

---

### 7) S2C Sync-Handler von `Minecraft.getInstance()` entkoppelt

**Warum:** Server-Shop Sync nutzte direkte Client-Singletons.

**Änderung:**
- `ServerShopSyncPacket` arbeitet mit `context.player()` und dessen `registryAccess()`

**Datei:**
- `network/packets/serverShop/ServerShopSyncPacket.java`

---

### 8) Capability-Lifecycle bei BlockEntity-Unload/Remove ergänzt

**Warum:** fehlender Cleanup bei Chunk-Unload/Remove kann stale Capability-Zustände begünstigen.

**Änderung:**
- `SmallShopBlockEntity` überschreibt jetzt:
  - `onChunkUnloaded()`
  - `setRemoved()`
- in beiden Fällen: Neighbor-Unlock + Capability-Invalidierung

**Datei:**
- `block/entity/SmallShopBlockEntity.java`

---

### 9) Datagen-Workflow robuster gemacht

**Warum:** globales `try/catch` maskierte Fehlerquellen; Provider liefen immer.

**Änderung:**
- Provider über `event.includeClient()` / `event.includeServer()` gegated
- unnötiges Catch-All entfernt

**Datei:**
- `data/DataGenerators.java`

---

### 10) Kleine Cleanup-Verbesserungen

**Änderung:**
- Utility-Klassen als `final` + private Konstruktor

**Dateien:**
- `util/RegistriesInit.java`
- `util/CreativeTabInit.java`

---

## Verifikation

- `./gradlew compileJava` ✅
- `./gradlew build` ✅

---

## Manuelle Release-Checks (empfohlen)

- Dedicated Server Start/Stop inkl. Packet-Handshake
- Client Join mit passender Mod-Version
- SmallShop öffnen, Offer erstellen/löschen, Tab-Wechsel
- Owner-Liste speichern (inkl. >64 Einträge Test auf Trunkierung)
- Chunk unload/load neben verbundenen Chests
- `runData` ausführen und generierte Assets prüfen
