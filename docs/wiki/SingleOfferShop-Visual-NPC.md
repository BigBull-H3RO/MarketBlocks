# SingleOfferShop: Visual NPC

The **Visual NPC** feature lets shop owners position an animated clerk above their shop block. The NPC can appear as a **Villager** with configurable professions or as a **Player Model** adopting any Minecraft player's skin.

---

## Enabling the NPC

1. Open your shop and navigate to the **Settings** tab.
2. Click the **Villager** (🧑‍🌾) sub-tab.
3. Toggle the NPC switch on. The clerk will appear positioned directly above the counter.

> ⚠️ **Clearance Required:** The block space directly above the shop must be free of solid blocks. If the space is obstructed, the NPC will not appear until space is cleared.

---

## NPC Appearance Modes

### 1. Villager Mode (Default)
Displays a classic Villager model. You can choose from **15 villager professions**:

| Professions | Professions | Professions |
|---|---|---|
| None (Unemployed) | Armorer | Butcher |
| Cartographer | Cleric | Farmer |
| Fisherman | Fletcher | Leatherworker |
| Librarian | Mason | Nitwit |
| Shepherd | Toolsmith | Weaponsmith |

*Cycle through professions using the arrow/cycle button in the Villager tab.*

### 2. Player Skin Mode
- Toggle **Use Player Skin** to replace the villager model with a player character model.
- Enter the **Player Name** in the text box.
- MarketBlocks will fetch and render the official skin corresponding to that account name.

---

## Custom Name Tag

- Enter a custom name (up to 32 characters) to display a floating name tag above the clerk's head.
- Supports letters, numbers, spaces, hyphens, and underscores.
- Leave empty to hide the name tag.

---

## Trade Celebrations & Sound Effects

The Villager tab includes feedback settings to make purchases feel rewarding:

| Setting | Default | Effect |
|---|:---:|---|
| **Purchase Particles** | Enabled | Emits emerald-green happy villager particles upon each trade. |
| **Purchase Sounds** | Enabled | Plays villager affirmation voice lines when goods are bought. |
| **Payment Slot Sounds** | Enabled | Plays interactive feedback chimes when currency is placed in payment slots. |

---

## XP Pickup Audio Feedback

Located in **Settings -> General**:
- **Purchase XP Sound**: Plays a satisfying experience orb ding on purchase. The pitch scales smoothly with transaction volume (higher pitch for bulk purchases). Includes a built-in cooldown to prevent ear fatigue during rapid trades.

---

## Configuration

- **Client Optimization (`config/marketblocks/client.toml`)**:
  - `enableShopItemRendering = true`: Master toggle for low-end graphics cards.
- **Server Management (`config/marketblocks/singleoffer/general.toml`)**:
  - `[Tabs] villager = true`: Set to `false` if you wish to hide the NPC tab for all players on the server.
- **Per-Block Defaults**:
  - Configure default NPC state, profession, sounds, and particles in `config/marketblocks/singleoffer/tradestand.toml` and `marketcrate.toml`.
