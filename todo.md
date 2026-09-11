# MarketBlocks - TODO

Stand: Juni 2026

---

## 🚀 Offene Feature-Ideen & Geplante Aufgaben

- [ ] **Marketplace: Buttons "Edit Limits" und "Edit Pricing" testen**
  - UI-Ablauf im Marketplace prüfen und ggf. Layout/Validierung/Feedback anpassen.

- [ ] **Mod-API und Entwickler-Schnittstelle vorbereiten**
  - `api`-Package, NeoForge Custom Events/Capabilities und stabile Interaktionsebenen planen.
  - Addons für Custom Economy, Custom NPC-Visuals und externe Shop-Steuerung ermöglichen.

- [ ] **MarketCrate mit mehreren Angeboten evaluieren**
  - Variante mit 3-4 gleichzeitigen Offers prüfen.
  - Offene Frage: Visuelle Darstellung, Slot-/Teilbereich-Layout und Item-Preview.

- [x] **Clear log Button Funktion gefixt**
  - Maus-Klick Interzeption in `SingleOfferShopScreen` behoben.

---

## ✨ Neue Feature-Ideen (Gameplay & UI) (P3)

- [/] **Dynamische NPC-Handelswerte & Budget-Überarbeitung**
  - [x] *Basis-Sättigung & Decay:* Erste Implementierung eines World Saved Data Pools mit zeitbasiertem Decay zur NPC-Sättigung abgeschlossen.
  - [x] *Rezept-Scanner-Erweiterung:* Ofen- (`Smelting`), Schmelz- (`Blasting`), Koch- (`Smoking`) und Steinsäge-Rezepte (`Stonecutter`) einbinden. Verarbeitungsbonus von $+10\%$ für Crafting-Schritte einbauen, um Veredelung zu belohnen.
  - [x] *Händler-Klassen:* Spawnen von NPC-Händlern mit Rängen:
    - *Bürger (70%):* Budget 32-128 Emeralds (kauft Holz, Essen, etc.)
    - *Wohlhabender Händler (25%):* Budget 256-1024 Emeralds (kauft Erze, Tränke)
    - *Adliger / Großhändler (5%):* Budget 1024-8192 Emeralds (kauft Netherite, seltene Items)
  - [x] *NPC-Spezialisierung / Interessensgruppen:* Händler erhalten beim Spawnen Kategorien (z.B. Schmied, Bauer) und kaufen bevorzugt passende Items (mit erweitertem Budget für diese Items).
  - [x] *Visuelles Markt-Feedback:* Ein Marktbericht-Buch oder ein GUI-Element einführen, um aktuelle Preis-Trends (z. B. Sättigung von Items) für Spieler sichtbar zu machen.

- [ ] ~~**Shopping-List-Overlay für Wandering Trader NPCs**~~
  - ~~Dem Spieler ermöglichen, per Rechtsklick auf den NPC zu sehen, nach welchen Items er sucht und wie hoch sein aktuelles Budget ist.~~

- [ ] ~~**Such- und Filterfunktion im Marketplace**~~
  - ~~Bei vielen Angeboten wird der Marketplace unübersichtlich. Kategorien (z.B. Blöcke, Werkzeuge, Dekoration) oder ein einfaches Suchfeld im GUI hinzufügen.~~

- [ ] ~~**Transaktionsprotokoll-Export & Suche**~~
  - ~~Ermöglichen, das Transaktionsprotokoll im GUI zu durchsuchen oder für Server-Admins als CSV/JSON zu exportieren.~~

### 🎮 Code-Review P3: Spielerperspektive-Findings (Aug 2026)

- [ ] **Onboarding-Hilfe für neue Spieler**
  - Tooltip-Hints bei leeren Slots (z.B. „Lege das Item hier ab, das du verkaufen möchtest").
  - Erstes-Mal-Overlay beim ersten Shop-Öffnen mit Pfeilen und kurzen Erklärungen.
  - Trade Book als interaktives Tutorial mit Schritt-für-Schritt-Anleitungen verbessern.

- [ ] **Kauf-Bestätigung für Käufer (Buyer-Seite)**
  - Toast-Notification nach erfolgreichem Kauf: „✅ Du hast 16x Diamond für 64x Emerald gekauft".
  - Optionaler Sound-Feedback (charakteristischer „Kaching"-Sound).
  - Kurze Animation / Partikel-Effekt am Offer-Slot.

- [ ] **Preisvergleich / Marketplace-Suche**
  - Globaler Suchfilter im Marketplace-GUI nach Item-Typ.
  - „Alle Shops, die [Diamond] verkaufen, sortiert nach Preis"-Ansicht.
  - *Hinweis: War als Idee verworfen, ist aber aus Spielersicht das #1 QoL-Feature.*

- [ ] **Owner-Dashboard für Shop-Status**
  - Tab im Trade Book oder `/marketblocks mystatus` Befehl.
  - Zeigt alle eigenen Shops mit Status: 🟢 Aktiv | 🔴 Out of Stock | 🟡 Output voll | ⏸️ Geschlossen.
  - Optional: Klickbare Koordinaten-Links zu jedem Shop.

- [ ] **NPC-Trader-Feedback verbessern**
  - Spezifischer NPC-Name statt „Shop Buyer" in Kauf-Benachrichtigungen.
  - Nametag-Hinweis oder Partikel über NPC, damit Spieler wissen, dass Rechtsklick-Info möglich ist.

- [ ] **Sale-Statistiken anzeigen**
  - Im Settings-Tab: „Seit Sale-Start: X Verkäufe" anzeigen.
  - Hilft Admins zu bewerten, wie erfolgreich ein Sale ist.

- [ ] **Ghost-Items bei Out-of-Stock**
  - Offer-Preview (Payment + Result Items) auch bei „Out of Stock" als Geister-Items anzeigen.
  - Tooltip: „Derzeit nicht auf Lager" – damit Käufer wissen, was der Shop normalerweise verkauft.

- [ ] **MarketCrate vs. TradeStand Unterschied klarer kommunizieren**
  - Klare In-Game-Beschreibung im Item-Tooltip.
  - Im Trade Book eine Vergleichstabelle beider Block-Typen.

- [ ] **Konfigurations-Reset per GUI**
  - „Reset to Defaults"-Button im Settings-Tab.
  - Setzt alle Einstellungen eines Shops auf die Config-Standardwerte zurück.

- [ ] **Shop-Favoriten / „Besuchte Shops"-Liste**
  - Shift+Rechtsklick auf einen Shop → „Shop merken".
  - Persönliche Favoriten-Liste im Trade Book.
  - *Hinweis: War als Idee verworfen – für Multiplayer-Server mit vielen Shops aber wertvoll.*

---

## 📦 Abgeschlossene Aufgaben (Archiv)

### 🔒 Sicherheit und Exploit-Schutz (P0)
- [x] **`UpdateSettingsPacket` serverseitig härter validieren** (Server-Side Source of Truth für Owner/Access).
- [x] **SingleOffer-Mutationspakete an geöffnetes Menü und Distanz binden** (`SingleOfferShopMenu` Validierung).
- [x] **Netzwerk-Codecs gegen DoS/ungültige Werte absichern** (`AccessSettings`, `IoSettings`, Map-Größenbegrenzung).
- [x] **Rate-Limiter pro Packet-Typ statt global pro Spieler** (Schutz vor GUI-Spam ohne UI-Lags).

### 🔍 Code-Review: Fehler & Robustheit (P1)
- [x] **Double-Chest capability invalidation in `ShopRedstoneManager`** (Invalidierung beider Double-Chest-Hälften gesichert).
- [x] **Name-Sanitization Hex-Color-Code-Bypass** (Erweiterung auf Hex-Farbcode-Patterns in `NameValidator`).
- [x] **Sicherheitslücke bei ChestSecurityHandler & versetztem Double-Chest-Platzieren** (Zusätzlicher Check in `ShopSettingsManager`).
- [x] **Server-Side Translation Leak bei NPC-Transaktionslogs** (Fester String `"Shop Buyer"` in `OfferManager`).

### 🛠️ Gameplay- und Datenkonsistenz (P1)
- [x] **Marketplace-Autofill darf nur Angebote der aktuellen Page aktivieren** (Schutz vor seitenübergreifenden Exploits).
- [x] **Shop-Namen, NPC-Namen und Marketplace-Page-Namen serverseitig einheitlich validieren** (`NameValidator`).
- [x] **Auto-IO Output-Space bei externen Output-Chests klären** (`ShopInventoryManager`).
- [x] **`marketblocks:shop_blocks` Tag prüfen** (`ModBlockTagProvider` um `trade_stand` erweitert).

### 🎨 Rendering, Assets und Compat (P1)
- [x] **JourneyMap Marketplace-Icon auf lowercase ResourceLocation umstellen** (`marketplace.png`).
- [x] **Optionale Compat-Abhängigkeiten in `neoforge.mods.toml` deklarieren** (`jade` und `journeymap`).

### ⚡ Performance & Optimierungen (P2)
- [x] **Neighbor-Cache Polling-Overhead reduzieren** (Polling im Tick entfernt, Aktualisierung bedarfsgesteuert über `neighborChanged`).
- [x] **Optimierung des Entity-Scans beim Spawnen von NPCs** (Zentraler Zähler via Map in `ShopBuyerSpawner` und `ShopBuyerEntity`).

### ⚙️ Build-Qualität und Warnungen (P2)
- [x] **Gradle-Deprecation in `build.gradle:102` fixen** (`uri(...)` statt `file(...)`).
- [x] **Minecraft-Version-Range bewusst eingrenzen** (`[1.21,1.22)`).
- [x] **`org.gradle.jvmargs=-Xmx2G` prüfen und erhöhen** (Stabilerer Datagen).
- [x] **Compiler-Warnungen und unbenutzte Imports/Konstanten bereinigen** (Pristine Build-Status).

### 📚 Dokumentation und Release-Polish (P2)
- [x] **Doku auf blockless Marketplace / verlinkte Blöcke aktualisieren** (`README.md`, Wiki-Dokumente).
- [x] **Mod-Compatibility-Wiki korrigieren** (Blocknamen und Badges korrigiert).
- [x] **Mojibake/Encoding-Reste in Markdown und Kommentaren bereinigen** (Sauberes UTF-8).

---

## 🗑️ Verworfene / Abgebrochene Ideen
- ~~**GameTests oder Unit-Tests für Transaktionslogik einführen** (Vom User vorerst verworfen)~~
- ~~**Virtuellen Offer-Slot nach Reload/Chunk-Load testen** (Vom User vorerst verworfen)~~
- ~~**Shop-Favoriten erneut bewerten**~~
- ~~**Marketplace-Offer Sortierung erneut bewerten**~~
- ~~**Shop-Import/Export erneut bewerten**~~
