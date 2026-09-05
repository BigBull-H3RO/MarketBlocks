# Marketplace: Troubleshooting

This guide addresses common issues encountered when managing or trading in the Marketplace.

---

## ❓ "Offer cannot be purchased"

If clicking an offer in the Marketplace fails or displays a warning:
1. **Missing Payment Items**: Verify that you have the exact payment items, quantities, and matching NBT/data components in your inventory.
2. **Daily Limit Reached**: Check if the item displays a daily purchase limit indicator. If the server uses `sharedDailyLimits = true`, another player may have exhausted the server pool.
3. **Out of Stock**: If the item has a stock limit, all units may have been purchased. It will become available again once the restock timer elapses.
4. **Demand Price Escalation**: Check whether dynamic demand pricing has raised the required payment amount beyond what you have in your inventory.

---

## ❓ "Marketplace does not open"

1. **Keybind Conflict**: Ensure the **O** key isn't conflicting with another mod in **Options -> Controls -> Key Binds**.
2. **Command Check**: Test whether opening via command works: `/marketblocks marketplace open` (or `/mb marketplace open`).
3. **Server Loaded State**: Ensure the server completed world loading without JSON syntax errors in `marketplace.json`.

---

## ❓ "In-Game Edits or Sales do not show up"

1. **Edit Mode Active?**: Verify that you enabled edit mode: `/mb admin editmode true`.
2. **Operator Level**: The in-game editor and administrative commands require **OP level 2** (`hasPermission(2)`).
3. **Manual JSON Reload**: If you modified `marketplace.json` in an external text editor while the server was running, execute `/mb admin reload` to load the changes into memory.

---

## ❓ "marketplace.json appears corrupted"

MarketBlocks uses atomic writes and automated backups:
1. Check the directory `<world>/marketblocks/`.
2. Locate `marketplace.json.bak`, which contains the last known good configuration before the most recent save.
3. If necessary, restore the `.bak` file by copying it over `marketplace.json` and running `/mb admin reload`.
