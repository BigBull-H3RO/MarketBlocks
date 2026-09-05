# Developer Info

This page provides an architectural overview for mod developers building addons, integrations, and server extensions for MarketBlocks.

---

## Project Structure

The codebase is organized into domain-driven packages and feature slices:

```
de.bigbull.marketblocks
├── MarketBlocks                     (Mod entry point & config registration)
├── MarketBlocksClient               (Client-side entry point & keybinds)
├── client/
│   ├── event/                       (ClientGameEvents, BlockOutlineHandler)
│   ├── gui/                         (Shared UI components: sliders, custom edit boxes)
│   └── mixin/                       (ClientLevelMixin for block breaking progress)
├── compat/
│   ├── jade/                        (Jade HUD providers for shops & wandering buyers)
│   ├── jei/                         (JEI plugin for GUI bounds and extra areas)
│   └── journeymap/                  (Live shop & marketplace map markers)
├── core/
│   ├── command/                     (MarketplaceAdminCommand, ShopSearchCommand, ShopStatsCommand)
│   ├── config/                      (Config, ClientConfig, MarketplaceConfig, SingleOfferConfig, etc.)
│   ├── data/                        (ShopDirectorySavedData, MarketplaceLinkSavedData)
│   ├── event/                       (MarketBlocksCommandEvents, MarketBlocksLifecycleEvents, etc.)
│   └── init/                        (RegistriesInit, CreativeTabInit)
├── data/                            (NeoForge data generators for advancements, lang, models, recipes)
├── feature/
│   ├── log/                         (TransactionLogEntry, ShopTransactionLogSavedData)
│   ├── marketplace/
│   │   ├── advancement/             (MarketplaceOpenTrigger, MarketplaceBuyTrigger)
│   │   ├── client/screen/           (MarketplaceScreen, overlays, in-game editor dialogs)
│   │   ├── data/                    (MarketplaceManager, MarketplaceData, MarketplaceOffer, DemandPricing)
│   │   └── menu/                    (MarketplaceMenu)
│   ├── notification/                (PendingNotificationsSavedData)
│   ├── singleoffer/
│   │   ├── advancement/             (ShopSellTrigger, ShopWholesalerTrigger, etc.)
│   │   ├── block/                   (TradeStandBlock, TradeStandTopBlock, MarketCrateBlock, shapes)
│   │   ├── client/render/           (SingleOfferShopBlockEntityRenderer)
│   │   ├── client/screen/           (SingleOfferShopScreen, SettingsCategory sections)
│   │   ├── entity/                  (SingleOfferShopBlockEntity, OfferManager, ShopInventoryManager, ShopSettingsManager)
│   │   ├── menu/                    (SingleOfferShopMenu, ShopTab)
│   │   └── settings/                (GeneralSettings, IoSettings, VillagerSettings, OfferItemSettings, etc.)
│   ├── trader/                      (ShopBuyerEntity, TraderEconomyManager)
│   └── visual/                      (VisualShopNPC, VillagerVisualProfession, VisualShopNpcRenderer)
└── network/                         (NetworkHandler - central packet payload registration)
```

---

## Core Systems & Key Entry Points

### 1. SingleOfferShop
- **`SingleOfferShopBlockEntity`**: Core block entity handling inventories, offer evaluation, ownership, sided capability exposures, and settings management.
- **`OfferManager`**: Server-authoritative purchase execution, payment validation, and test-purchase simulations.
- **`ShopSettingsManager`**: Manages immutable settings records with serialization, network sync codecs, and mutable draft objects for GUI editing.
- **`ShopInventoryManager`**: Handles input stock, output earnings, and sided hopper/chest I/O.

### 2. Marketplace
- **`MarketplaceManager`**: Central singleton service controlling marketplace lifecycle, background tick updates, atomic disk persistence, and real-time client synchronization.
- **`MarketplaceData` & `MarketplaceOffer`**: Server-authoritative data models defining category pages, offers, daily limits, stock counters, and dynamic pricing curves.
- **`MarketplaceRuntimeMath`**: Mathematical utilities for demand step increments, cooling decay curves, and percent discount sales.

### 3. SavedData Persistence
- **`ShopDirectorySavedData`**: Global registry tracking all placed player and admin shops across dimensions (queried by `/marketblocks search` and `/marketblocks stats shops`).
- **`MarketplaceLinkSavedData`**: Global registry of world blocks linked to the Marketplace via `/marketblocks admin marketplace link`.
- **`ShopTransactionLogSavedData`**: Per-shop transactional ledger recording the last 100 customer trades.
- **`PendingNotificationsSavedData`**: Persistent queue for offline stock and full-output alerts delivered upon player login.

---

## Settings Architecture (Record + Mutable Draft)

Settings in MarketBlocks utilize an **immutable record + mutable draft** design pattern:
1. **Immutable Records** (e.g. `GeneralSettings`, `VillagerSettings`): The canonical, thread-safe state stored on the server BlockEntity. Contains `STREAM_CODEC` for network packets and `save()` / `load()` methods for NBT tags.
2. **Mutable Drafts** (e.g. `GeneralSettings.Draft`): Client-side staging objects bound to GUI widgets (text boxes, sliders, toggles). When the player clicks save, the draft compiles into an immutable record (`toSettings()`) and dispatches via `UpdateSettingsPacket`.

---

## Server-Authoritative Architecture

MarketBlocks follows strict server-authoritative design principles:
- **No Client Purchase Authority**: Clients send intents; the server simulates, validates stock and payment slots, moves items, and broadcasts updates.
- **Permission Validation**: All admin commands and in-game editor actions strictly require `hasPermission(2)` and active global edit mode.
- **Safe Disk I/O**: The marketplace file (`<world>/marketblocks/marketplace.json`) writes to a temporary file before atomic renaming, preserving a `.bak` copy on every save.
