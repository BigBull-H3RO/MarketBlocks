# Market Blocks – To-Do Liste

## NPC / KI & Trader-System
- [x] **Trader NPC Verhalten & Spawning komplett überarbeiten**
  - [x] Spawn-Rate & Multiplayer-Fairness: Spieler-bezogener Cooldown (24.000 Ticks / 1 Tag), 60s Taktung, Spawn nur bei aktiven Shops in 64 Blöcken Umkreis, solider Boden-Check, kein Verfallen des Cooldowns bei Höhlen/Wildnis.
  - [x] KI-Pathfinding & Theken-Toleranz: Entspanntes Gehtempo (`0.65D`), Theken-Toleranz ("Smart Reach") aus 1.5–2.5 Blöcken Distanz über Ladentheken, Tische und Zäune hinweg.
  - [x] Kaufverhalten & Shopping-Erlebnis: Realistische Bedenkzeit (4–8s) mit Kopfbewegung zur Auslage, Mengen-Kauf für `WEALTHY` (1–3) und `NOBLE` (1–5), Item nach Kauf sichtbar für ~2.5s in den Armen halten.
  - [x] Namens- & Transaktions-Integration: Händlername samt Rang (z. B. "Jonathan (Noble)") wird direkt im Transaktions-Log des Shops eingetragen.
  - [x] Textur-Fix („Nackter Villager“ behoben): Grundtextur für alle Ränge ist die vollständige Wandering-Trader-Robe; Accessoires (Monokel für `WEALTHY`, Brille/Hut für `NOBLE`) als sauberer Layer darüber.
  - [x] Audio-Entschlackung: Nerviges Villager-Dauergeplapper („hm, ha, hi, ha“) eliminiert – umgestellt auf authentische Wandering-Trader-Sounds mit 15s Pause und Stille beim Auslagen-Begutachten.
  - [ ] *(Optionaler Asset-Task)*: Bei Bedarf eigene handgezeichnete Custom-Roben (z. B. Samtumhang für Nobles) zeichnen.

## Portierung & Multi-Loader (Fabric / Versionen)
- [ ] **Multi-Loader Architektur evaluieren (NeoForge + Fabric)**
  - [x] Projektstruktur auf Multi-Loader vorbereiten (Template von Jaredlll08 auf Branch `feature/multiloader` eingerichtet und verifiziert)
  - [x] Git-Worktree (`MarketBlocks-Multiloader`) eingerichtet & mit aktuellem `main`-Stand synchronisiert (inkl. `legacy-src/`)
  - [ ] Loader-spezifische APIs (Networking, Capabilities/Item-Storage, Registries) sauber abstrahieren
- [ ] **Versions-Fahrplan & Port-Strategie festlegen**
  - [ ] Basisversion: 1.21.1 stabil fertigstellen und fehlerfrei testen
  - [ ] Zielversionen analysieren: Festlegen, welche neueren Zwischenversionen übersprungen werden können, um Testaufwand zu minimieren
  - [ ] Test-Checkliste für Ports erstellen (GUI, Networking, Rendering, Shop-Transaktionen nach jedem Port verifizieren)

## Dokumentation, Release & Branding
- [x] **GitHub Wiki überarbeiten**
  - [x] Bestehende Erklärungen sprachlich und inhaltlich verbessern
  - [x] Übersichtliche Struktur für Features, Blöcke, Items und Konfigurationsmöglichkeiten schaffen (14 strukturierte Seiten, 3 Säulen)
- [x] **GitHub README überarbeiten**
  - [x] Schnellstart-Guide, Crafting-Rezepte, Feature-Highlights, Trader-NPCs, Badges und direkte Wiki-Verlinkung sauber einbinden
- [ ] **Mod-Beschreibungen erstellen**
  - [ ] CurseForge-Beschreibung (Formatierung für CurseForge-Editor)
  - [ ] Modrinth-Beschreibung (Markdown-optimiert)
- [ ] **Logo & Icon-Design erstellen**
  - [x] Mod-Icon / Logo (handgezeichnet, 1064x1064 px in `.idea/icon.png` für README, CurseForge & Modrinth) fertiggestellt
  - [ ] Eventuell Banner / Header-Grafik für Projektseiten vorbereiten
