# Trader NPCs: Autonomous Customer Guide

**Trader NPCs** are autonomous Wandering Traders that roam the Overworld looking for player-owned shops to browse and purchase items from. They create a living, breathing economy where your shops make sales even without other human players actively visiting!

---

## 🌍 Spawning Mechanics & Fairness

MarketBlocks uses a **multiplayer-fair** spawning system that ensures every player with a shop gets visits, without spamming the world:

- **Shop-Proximity Requirement**: A Trader NPC will **only** spawn if an active player is within **64 blocks of at least one open shop with an active offer**. If you are mining deep underground in a cave or exploring the ocean, the trader will not spawn and your cooldown is preserved!
- **Per-Player Daily Cooldown**: Each player has an independent **24,000-tick cooldown (1 full Minecraft day)**. You will never have another player on the server "steal" your visitor.
- **Fair Spawn Ticks**: Every 60 seconds (1200 ticks), the server checks for eligible players who have completed their daily cooldown.
- **Daytime Only**: In line with vanilla Wandering Traders, they only arrive during daylight hours.
- **Safe Surface Spawning**: Traders only spawn on solid surface ground with open headroom — never trapped in tree leaves, submerged in rivers, or suffocating in walls.
- **Dimension Cap**: A maximum of 4 Trader NPCs can exist simultaneously per dimension by default (configurable).

---

## 👑 Social Ranks & Budgets

When a Trader NPC spawns, they roll a social rank that determines their shopping budget, item preferences, and purchasing power:

| Rank | Spawn Chance | Budget (Coins) | Preferred Goods | Purchase Volume |
|---|:---:|:---:|---|:---:|
| **Citizen** | 70% | 32 – 128 | Everyday staples: bread, crops, building blocks, simple tools, torches. | **1 unit** |
| **Wealthy** | 25% | 256 – 1024 | Mid-to-high tier goods: iron, gold, redstone, potions, quality weapons & armor. | **1 – 3 units** |
| **Noble** | 5% | 1024 – 8192 | Luxury & rare treasures: diamonds, netherite, enchanted gear, rare potions. | **1 – 5 units (Bulk)** |

### Purchasing Preferences & Standards:
- **Citizens** accept fair prices (up to a 15% markup over baseline market value).
- **Wealthy** merchants have higher budgets and can purchase 2 to 3 units if the price is right.
- **Nobles** are aristocratic high-rollers: they **ignore cheap junk items** (items with value under 5 coins), but when they find genuine treasures or bulk diamonds, they will readily buy up to 5 units in a single visit!

---

## 🛍️ In-Game Shopping Behavior

Trader NPCs behave like real, thoughtful customers:

1. **Natural Pacing**: They walk towards your shop counter at a calm, natural walking speed (`0.65`).
2. **Theken-Toleranz ("Smart Reach")**: You can design authentic shop counters, tables, or fence barriers! If the block directly in front of the shop is occupied by a counter table, the trader comfortably stands 1.5 to 2.5 blocks away and trades across the counter (up to 3.5 blocks reach).
3. **Window-Shopping & Inspection**: Upon arriving, the trader stops and carefully examines the showcased goods for **4 to 8 seconds**, tilting their head down to inspect the items.
4. **Celebration & Purchase Feedback**:
   - Plays a calm, authentic Wandering Trader affirmation sound (`WANDERING_TRADER_YES`) — no obnoxious high-pitched villager chatter!
   - Emits emerald-green happy particles.
   - **Holds the Item in Hand**: The trader proudly holds the purchased item visibly in their crossed arms for ~2.5 seconds before walking away.
5. **Logged in History**: The trader's name and rank appear directly in your shop's **Transaction Log** (e.g. *"Jonathan (Noble) bought 2x Diamond"*).

---

## ⚔️ Easter Egg: Rage Mode

Trader NPCs are peaceful merchants, but they do not tolerate being harassed:
- If a player **spam-clicks** the NPC repeatedly within a few seconds, the trader will become **enraged**!
- An angry battle cry sounds, and the trader draws an enchanted sword:
  - **Citizen**: Iron Sword
  - **Wealthy**: Diamond Sword
  - **Noble**: Netherite Sword (often with high Sharpness!)
- The trader will pursue and attack the offending player until calmed down or the player flees.
- *Server administrators can toggle this feature in `config/marketblocks/trader/trader.toml` via `[RageMode] enabled = true`.*

---

## 🛠️ Server Customization & Economy Tuning

All Trader NPC mechanics can be customized inside `config/marketblocks/trader/`:

1. **`trader.toml`**: Toggle spawning, adjust the 24,000-tick player cooldown, check radius, and rank probabilities.
2. **`trader_item_values.json`**: Override item values to teach traders how much diamonds, netherite, or custom modded items are worth on your server.
3. **`trader_blacklist.json`**: Prevent unwanted items (dirt, seeds, joke items) from being bought by NPCs.
4. **`trader_names.json`**: Add your own custom list of names (e.g. community members, lore figures) that appear on traders and in transaction logs.

> 🔄 **Hot-Reload**: You can edit any of these three JSON files and run **`/mb admin reload`** to apply changes immediately without a server restart! See the [Configuration Guide](file:///e:/Projekte/Minecraft/Modding/Modding-Data/MarketBlocks/Antigravity/MarketBlocks/docs/wiki/Configuration-Guide.md) for full syntax and examples.

