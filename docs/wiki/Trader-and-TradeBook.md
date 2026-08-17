# Trader & Trade Book

MarketBlocks includes an optional trader subsystem alongside SingleOfferShop and Marketplace.

## What it includes

- **Trade Book item** (`marketblocks:trade_book`) with interactive market pages
- **Shop Buyer entity** (`marketblocks:shop_buyer`) and spawn egg (`marketblocks:shop_buyer_spawn_egg`)
- **Trader economy manager** for custom value mappings and blacklists

## Admin Commands

All commands below require OP level 2 via `/marketblocks admin ...`:

- `/marketblocks admin trader value set <item> <value>`
- `/marketblocks admin trader value remove <item>`
- `/marketblocks admin trader blacklist add <item>`
- `/marketblocks admin trader blacklist remove <item>`

## Config Relevance

- `giveTradeBookOnFirstJoin`: gives players the Trade Book on first join
- Trader values and blacklist are persisted server-side

## Notes

- This subsystem is server-authoritative like the rest of MarketBlocks.
- Use trader values/blacklists carefully, as they directly affect automated trader behavior and balancing.
