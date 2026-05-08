<p align="center">
  <img src=".idea/icon.png" alt="MarketBlocks Logo" width="180">
</p>

<p align="center">
  <a href="https://github.com/BigBull-H3RO/MarketBlocks/releases"><img src="https://img.shields.io/github/v/release/BigBull-H3RO/MarketBlocks?style=flat&color=0280ff" alt="Latest Release"></a>
  <a href="https://github.com/BigBull-H3RO/MarketBlocks/blob/main/LICENSE.txt"><img src="https://img.shields.io/github/license/BigBull-H3RO/MarketBlocks?style=flat&color=0280ff" alt="License"></a>
  <a href="https://github.com/BigBull-H3RO/MarketBlocks/stargazers"><img src="https://img.shields.io/github/stars/BigBull-H3RO/MarketBlocks?style=flat&color=1c1c1c" alt="GitHub Stars"></a>
  <a href="https://github.com/BigBull-H3RO/MarketBlocks/releases"><img src="https://img.shields.io/github/downloads/BigBull-H3RO/MarketBlocks/total?style=flat&color=5ca424" alt="Downloads"></a>
</p>

# MarketBlocks

**MarketBlocks** ist ein NeoForge-Mod für **Minecraft 1.21.1**, der ein serverautoritäres Handelssystem mit zwei Shop-Typen bereitstellt:

- **SingleOfferShop** (blockbasiert, ein aktives Angebot pro Shop)
- **Marketplace** (blockloses, seitenbasiertes Angebotssystem)

Der Fokus liegt auf **sicherer Kaufabwicklung**, **klaren Berechtigungen** und **zuverlässiger Persistenz** – geeignet für Survival-Server, SMPs und modded Multiplayer-Welten.

## **✨ Features**
✅ **SingleOfferShop (ehem. SmallShop)**
- Blockbasierter Shop mit einem aktiven Angebot pro Shop.
- Unterstützt bis zu **2 Payment-Stacks** und **1 Result-Stack**.
- Zusätzliche Co-Owner möglich, inklusive klarer Rollen im UI.
- Optionaler **Admin-Shop-Modus** (kein Bestand nötig, serverseitig abgesichert).

✅ **Marketplace (ehem. ServerShop / MarketPlace)**
- Blockloses, zentrales Marktsystem mit Seiten und mehreren Angeboten.
- Öffnen per Keybind (**O**) oder Command.
- Serverseitige Synchronisation mit Snapshot + Runtime-ViewStates.
- Persistenz über JSON mit Backup/Restore-Strategie.

✅ **Sichere, serverseitige Transaktionen**
- Validierung von Item, Count und Components auf dem Server.
- Deterministische Kauflogik (auch bei Shift-/Bulk-Käufen).
- Schutz vor Client-Manipulation durch serverautoritative Prüfungen.

✅ **Limits, Restock & Demand Pricing (Marketplace)**
- Daily-Limits (global oder pro Spieler), Stock-Limits und Restock-Intervalle.
- Dynamische Preisberechnung über Multiplikator-System.
- Automatische Runtime-Upkeep-Logik über Serverticks.

✅ **Chest-I/O Erweiterung (experimentell)**
- Optionales automatisches Ziehen/Schieben von Items über angrenzende Inventare.
- Separat über Config aktivierbar.

✅ **Transaktionslog & QoL**
- Persistentes Shop-Transaktionslog für SingleOfferShop.
- Redstone-Puls bei erfolgreichem Kauf (optional).
- Konfigurierbare visuelle/soundbasierte Shop-Feedbacks.

---

> Die komplette Logik für Kaufabwicklung und Rechteprüfung läuft serverseitig.

---

## **⚙️ Konfiguration**
Die wichtigsten Schalter liegen in der Common-Config, u. a.:

- `enableDoubleChestSupport`
- `enableChestIoExtensionExperimental`
- `offerUpdateInterval`
- `chestIoInterval`
- `marketplaceGlobalDailyLimit`
- `marketblocksAdminModeEnabled`
- `visualNpcRenderViewDistance`

Zusätzlich gibt es weitere Feinabstimmungen für SingleOfferShop, Marketplace und Visual/NPC-Verhalten.

---

## **📝 Commands**
Die zentralen Commands sind unter **`/marketblocks`** gebündelt:

| Command                                           | Permission | Beschreibung |
|---------------------------------------------------|------------|--------------|
| **`/marketblocks adminmode [true|false]`**       | `admin`    | Aktiviert/deaktiviert den globalen Admin-/Edit-Modus. |
| **`/marketblocks marketplace`**                   | `admin`    | Öffnet den Marketplace. |
| **`/marketblocks marketplace reload`**            | `admin`    | Lädt die Marketplace-Konfiguration neu von der Festplatte. |
| **`/marketblocks marketplace resetlimits <player>`** | `admin` | Setzt Daily-Limits für den angegebenen Spieler zurück. |

---

## **🛠 Kurz-Workflow**
1. **SingleOfferShop platzieren** und als Owner verwalten.
2. **Angebot setzen** (Result + bis zu 2 Payments).
3. Optional **Input/Output + Chest-I/O** konfigurieren.
4. Für zentrale Handelsseiten den **Marketplace** öffnen und Angebote verwalten.

So lassen sich sowohl lokale Spieler-Shops als auch ein globales Marktsystem parallel betreiben.
