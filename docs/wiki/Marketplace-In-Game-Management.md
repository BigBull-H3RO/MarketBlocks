# Marketplace: In-Game Management

MarketBlocks provides real-time in-game management tools that allow server administrators to design, customize, and regulate the server marketplace directly within Minecraft without editing text files or restarting the server.

---

## Accessing the Marketplace

Players and administrators can open the Marketplace through three methods:

- **Keybind**: Press **O** (default, customizable in standard Minecraft Key Binds).
- **Command**: Run `/marketblocks marketplace open` (or `/mb marketplace open`).
- **Linked Block**: Right-click any block in the world linked to the Marketplace.

---

## Enabling In-Game Edit Mode

To modify pages, items, pricing, or limits in-game, you must:
1. Have Operator rights (permission level 2).
2. Enable global edit mode via chat command:  
   `/marketblocks admin editmode true` (or `/mb admin editmode true`).

When edit mode is active, the Marketplace GUI reveals intuitive administrative toolbars:

### Editor Capabilities
- **Page Operations**: Create new category tabs, rename existing pages, change icons, or delete categories.
- **Offer Operations**: Click `+` to add new offers, reorder items left/right via arrow buttons, or delete outdated offers.
- **Limits Configuration**: Set daily limits, finite stock amounts, and restock intervals (in seconds) directly via UI text inputs.
- **Demand Pricing**: Configure starting price multipliers, demand step increments per purchase, and minimum/maximum multiplier bounds.
- **Live Sync**: All changes take effect immediately on the server and are synchronized in real-time to all players currently browsing the Marketplace.

---

## Temporary Sales & Timed Discounts

Administrators can launch timed sales on any marketplace offer without permanently altering base prices:

- **Start a Sale**:
  ```
  /marketblocks admin sale marketplace set <offer> <percent> <duration_minutes>
  ```
  *Example:* `/mb admin sale marketplace set "Diamonds" 20 60` (applies a 20% discount for 60 minutes).  
  *Tab completion automatically suggests available offers along with their current page, count, and price!*

- **End a Sale Early**:
  ```
  /marketblocks admin sale marketplace remove <offer>
  ```

---

## Linking World Blocks to the Marketplace

Administrators can designate physical blocks (counters, signs, NPC stalls, custom models) as access points for the Marketplace:

- **Link the Looked-At Block**:
  ```
  /marketblocks admin marketplace link [name] [tp_pos]
  ```
  - `[name]` *(optional)*: Sets a custom display title for this market hub.
  - `[tp_pos]` *(optional)*: Specifies the exact landing coordinates when players teleport to this marketplace hub via `/mb search`.

- **Unlink a Block**:
  ```
  /marketblocks admin marketplace unlink [name]
  ```
  - Without arguments: Unlinks the block currently in your crosshair.
  - With `[name]`: Unlinks a specific registered block link by name (with tab auto-completion).

---

## Administrator Command Reference

| Action | Command (also supports `/mb`) | Notes |
|---|---|---|
| Toggle Edit Mode | `/marketblocks admin editmode [true\|false]` | Toggles or sets visual editor mode |
| Reload JSON | `/marketblocks admin reload` | Reloads `marketplace.json` from disk without restart |
| Reset Player Limits | `/marketblocks admin resetlimits <player>` | Clears daily buy limits for a player |
| Link Block | `/marketblocks admin marketplace link [name] [tp_pos]` | Links targeted block to marketplace |
| Unlink Block | `/marketblocks admin marketplace unlink [name]` | Removes link from targeted block or by name |
| Set Sale | `/marketblocks admin sale marketplace set <offer> <percent> <min>` | Starts timed discount |
| Clear Sale | `/marketblocks admin sale marketplace remove <offer>` | Cancels timed discount |
| Top 10 Sales | `/marketblocks stats marketplace` | Shows leaderboard of top bought offers |

---

## Safety & Best Practices

- **Instant Live Sync**: Players currently inside the Marketplace GUI will immediately see added, modified, or discounted items update without closing their screen.
- **External JSON Edits**: If you make changes directly to `<world>/marketblocks/marketplace.json`, execute `/mb admin reload` to load them into the running server immediately.
- **Automated Backups**: Every write to `marketplace.json` is performed atomically and preserves a `<world>/marketblocks/marketplace.json.bak` backup file to protect against corruption.
