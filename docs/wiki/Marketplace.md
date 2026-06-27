# Marketplace

The **Marketplace** is the centrally managed, page-based trading hub of MarketBlocks. Unlike single-offer shop blocks owned by individual players, the Marketplace serves as the server-wide economy backbone, accessible from anywhere or tethered to dedicated hub locations.

## Contents

- [JSON Configuration Guide](Marketplace-JSON-Configuration-Guide)
- [In-Game Management](Marketplace-In-Game-Management)
- [Dynamic Pricing & Limits](Marketplace-Dynamic-Pricing-and-Limits)
- [Troubleshooting](Marketplace-Troubleshooting)

## Quick Overview

- **Multiple Opening Methods**: Open instantly via keybind (**O** by default), command (`/marketblocks marketplace open`), or by right-clicking a linked **Marketplace Block**.
- **Page-Based Structure**: Clean UI organization with tabs for different categories (e.g., Minerals, Food, Enchantments, Rares).
- **Live In-Game Editor**: Fully customize pages, offers, limits, and pricing directly in-game (requires OP + `/marketblocks admin editmode true`).
- **Temporary Sales**: Set up timed discounts on offers to spark player activity.
- **Top 10 Statistics**: Keep track of the most popular items using `/marketblocks marketplace stats`.
- **Advanced Economics**: Supports stock limits, daily limits (global or per-player), timed restocks, and demand-based pricing.
- **Solid Persistence**: JSON persistence with automated backup (`.bak`) and restore strategies.

## Three Ways to Interact

Depending on your server type (anarchy, SMP, RPG, or minigame), you can choose how players access the Marketplace:

1. **Keybind (`O`)**: Best for fast-paced survival servers where players can trade on the go without returning to spawn.
2. **Command (`/marketblocks marketplace open`)**: Excellent for servers using custom menu plugins or NPC command-dispatchers.
3. **Linked Marketplace Blocks**: Ideal for immersive RPG or hub servers. Admins can place Marketplace Blocks at spawn and link them (`/marketblocks admin marketplace link <page_name>`) so players must physically visit the market stalls to trade.
