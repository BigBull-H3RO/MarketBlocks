# Marketplace: In-Game Management

## Opening

The Marketplace can be opened via:

- **Keybind**: **O** (default, configurable in Minecraft controls)
- **Command**: `/marketblocks marketplace` (requires operator level 2)
- **Marketplace Block**: Right-click a placed Marketplace Block in the world

## Editing

Editing the Marketplace requires **both**:
1. Operator rights (permission level 2)
2. Global admin mode enabled (`/marketblocks adminmode true`)

Typical editor actions:

- Create, rename, or delete **pages**
- Add, move, or delete **offers**
- Adjust **limits** (daily, stock, restock)
- Adjust **pricing** (demand-based multipliers and bounds)

All mutations are validated server-side and synchronized to all open viewers on success.

## Permissions

| Action | Requirement |
| --- | --- |
| Open Marketplace (keybind) | Any player |
| Open Marketplace (command) | Operator level 2 |
| Buy from offers | Any player (subject to limits) |
| Edit pages/offers | Operator level 2 + global admin mode enabled |
| Reload from disk | Operator level 2 (`/marketblocks marketplace reload`) |
| Reset player limits | Operator level 2 (`/marketblocks marketplace resetlimits <player>`) |

## Operational Notes

- Communicate changes to the team immediately (prices/limits).
- For bigger changes, work in smaller incremental steps.
- After major edits, run functional checks with test purchases.
- Use the reload command after manual JSON edits to apply changes without a server restart.
