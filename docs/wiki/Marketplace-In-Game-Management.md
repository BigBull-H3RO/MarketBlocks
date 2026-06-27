# Marketplace: In-Game Management

MarketBlocks provides powerful, real-time in-game management tools for server administrators to design, customize, and regulate the server marketplace directly from within Minecraft.

## Opening the Marketplace

The Marketplace GUI can be accessed through three convenient methods:

- **Keybind**: Press **O** (default, fully configurable in Minecraft controls).
- **Command**: Run `/marketblocks marketplace open` (available to all players).
- **Marketplace Block**: Right-click a placed Marketplace Block in the world (which can be globally linked or targeted to specific pages).

## Enabling Edit Mode

To modify the Marketplace layout or offers in-game, you must fulfill two strict criteria:
1. Have Operator rights (permission level 2).
2. Enable global edit mode via command: `/marketblocks admin editmode true`.

When edit mode is active, the Marketplace GUI dynamically unlocks full administrative toolbars and dialogs.

### Available Editor Actions:

- **Page Management**: Create new tabs, rename existing pages, or delete outdated categories.
- **Offer Management**: Add new trading slots, rearrange items via directional moving, or delete offers.
- **Limit Configuration**: Define daily restrictions, inventory stock limits, and restock timers directly via UI input boxes.
- **Dynamic Pricing**: Set base multipliers, demand scaling steps, and min/max multiplier bounds.

All mutations are strictly validated server-side and instantly broadcast to all players currently viewing the Marketplace.

## Temporary Sales & Discounts

Admins can configure time-limited sales and discounts on Marketplace offers without permanently altering their base pricing structure. This is ideal for weekend events or holiday specials:

- **Set Sale**: `/marketblocks admin sale marketplace set <offer_id> <discount_percentage> <duration_minutes>`
- **End Sale**: `/marketblocks admin sale marketplace remove <offer_id>`

## Linking Marketplace Blocks

Server builders can create physical hub marketplaces by placing Marketplace Blocks and linking them to open either the entire Marketplace or specific sub-pages:

- **Link Block**: Look at a Marketplace block and run `/marketblocks admin marketplace link <page_name>`
- **Unlink Block**: Look at a linked block and run `/marketblocks admin marketplace unlink`

## Permission Overview

| Action | Requirement | Command / Method |
| --- | --- | --- |
| Open Marketplace | Any player | Keybind `O`, `/marketblocks marketplace open`, or Block click |
| Enable Edit Mode | Operator level 2 | `/marketblocks admin editmode true\|false` |
| Edit Pages & Offers | Operator level 2 + Edit Mode | In-Game GUI |
| Manage Sales | Operator level 2 | `/marketblocks admin sale marketplace set\|remove` |
| Link/Unlink Blocks | Operator level 2 | `/marketblocks admin marketplace link\|unlink` |
| Reload JSON from Disk | Operator level 2 | `/marketblocks admin reload` |
| Reset Player Limits | Operator level 2 | `/marketblocks admin resetlimits <player>` |

## Operational Notes

- **Live Synchronization**: Because changes sync instantly, players browsing the marketplace will see price and item updates in real-time.
- **External JSON Edits**: If you prefer editing `marketplace.json` directly via a text editor, always use `/marketblocks admin reload` afterward to apply your changes cleanly without a server restart.
- **Backup & Restore**: The mod automatically maintains a `.bak` copy of your marketplace configuration to prevent data loss in case of unexpected server crashes or malformed JSON edits.
