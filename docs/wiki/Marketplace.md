# Marketplace

The **Marketplace** is the centrally managed, page-based trading hub of MarketBlocks. Unlike individual single-offer shop blocks owned by players, the Marketplace acts as the server-wide economy backbone. It can be accessed globally via keybind/command, or physically tethered to decorative market stalls in world hubs.

---

## Contents

- [In-Game Management](Marketplace-In-Game-Management): How to create and edit pages and offers live in-game.
- [JSON Configuration Guide](Marketplace-JSON-Configuration-Guide): Technical file layout and backup system.
- [Dynamic Pricing & Limits](Marketplace-Dynamic-Pricing-and-Limits): Daily limits, finite stock, restock timers, and demand curves.
- [Troubleshooting](Marketplace-Troubleshooting): Common questions and resolution steps.

---

## Quick Overview

- **Multiple Ways to Access**: Open immediately via keybind (**O** by default), chat command (`/marketblocks marketplace open` or `/mb marketplace open`), or by right-clicking linked blocks in the world.
- **Categorized Pages**: Clean tabbed layout separating goods into custom categories (e.g., Minerals, Farm Produce, Enchants, Exotics).
- **Live In-Game Editor**: Operators can create pages, add offers, reorder slots, and configure pricing without ever touching JSON files (`/mb admin editmode true`).
- **Dynamic Pricing & Stock Limits**: Realistic economic simulations including demand-based price scaling, restock intervals, and daily player limits.
- **Temporary Sales**: Launch timed discounts on any marketplace item using `/mb admin sale marketplace set`.
- **Top 10 Leaderboards**: View the most frequently purchased items using `/marketblocks stats marketplace` (or `/mb stats marketplace`).
- **Resilient Storage**: Server-authoritative JSON storage at `<world>/marketblocks/marketplace.json` with atomic writes and automatic `.bak` backups.

---

## Three Ways to Access the Marketplace

Depending on your server style (SMP, RPG, skyblock, or minigames), you can customize how players access the Marketplace:

1. **Keybind (`O`)**: Best for fast-paced survival servers where players can buy or sell resources on the go without returning to spawn.
2. **Command (`/mb marketplace open`)**: Ideal for servers utilizing NPC dialog plugins, custom menu GUIs, or command signs.
3. **Linked World Blocks**: Perfect for immersive RPG or spawn hub servers. Administrators can link any decorative block or NPC stall (`/mb admin marketplace link [name] [tp_pos]`) so players must physically visit marketplace stalls to shop.
