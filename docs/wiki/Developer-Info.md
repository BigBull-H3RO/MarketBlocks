# Developer Info

This page is for developers building addons, integrations, and server extensions.

## Project Structure

The codebase is organized by feature slices under a `feature/` package:

```
de.bigbull.marketblocks
├── MarketBlocks                     (Mod entry point)
├── MarketBlocksClient               (Client-side entry point)
├── client/
│   ├── event/                       (Client event handlers)
│   ├── gui/                         (Shared UI components: sliders, buttons)
│   └── mixin/                       (Client-side mixins)
├── core/
│   ├── command/                     (MarketBlocksCommand - /marketblocks list)
│   ├── config/                      (Config - all config options)
│   ├── data/                        (ShopDirectorySavedData)
│   ├── event/                       (MarketBlocksEvents - server lifecycle, commands)
│   └── init/                        (RegistriesInit, CreativeTabInit)
├── data/                            (Data generators)
│   ├── advancement/                 (ModAdvancementProvider)
│   ├── blockstate/lang/loot/recipe/tag/
├── feature/
│   ├── log/                         (TransactionLogEntry, ShopTransactionLogSavedData)
│   ├── marketplace/
│   │   ├── advancement/             (MarketplaceOpenTrigger, MarketplaceBuyTrigger)
│   │   ├── block/                   (MarketplaceBlock)
│   │   ├── client/screen/           (MarketplaceScreen, overlays, editors)
│   │   ├── data/                    (MarketplaceManager, MarketplaceData, offers, pricing)
│   │   ├── entity/                  (MarketplaceBlockEntity)
│   │   ├── menu/                    (MarketplaceMenu, MarketplaceMenuProvider)
│   │   └── network/                 (All marketplace packets)
│   ├── notification/                (PendingNotificationsSavedData)
│   ├── singleoffer/
│   │   ├── advancement/             (All shop advancement triggers)
│   │   ├── block/                   (BaseShopBlock, TradeStandBlock, MarketCrateBlock, ...)
│   │   ├── client/render/           (SingleOfferShopBlockEntityRenderer)
│   │   ├── client/screen/           (Shop screens, SettingsCategory, settings sections)
│   │   ├── entity/                  (SingleOfferShopBlockEntity, OfferManager, ShopInventoryManager, ShopSettingsManager)
│   │   ├── menu/                    (SingleOfferShopMenu, ShopTab)
│   │   ├── network/                 (All shop packets)
│   │   └── settings/                (GeneralSettings, IoSettings, VillagerSettings, OfferItemSettings, NotificationSettings, AccessSettings, AccessMode, IoRedstoneControl)
│   └── visual/
│       ├── npc/                     (IVisualShopNPC, VillagerVisualProfession, placement, animation)
│       └── render/                  (VisualShopNpcRenderer)
└── network/                         (NetworkHandler - central packet registration)
```

## Key Entry Points

### SingleOfferShop

| Class | Purpose |
| --- | --- |
| `feature/singleoffer/entity/SingleOfferShopBlockEntity` | Core block entity — inventory, offer system, ownership, settings |
| `feature/singleoffer/entity/OfferManager` | Offer creation, validation, purchase logic |
| `feature/singleoffer/entity/ShopInventoryManager` | I/O, neighbor cache, chest extension |
| `feature/singleoffer/entity/ShopSettingsManager` | Manages all settings records (General, IO, Villager, Visuals, Notifications, Access) |
| `feature/singleoffer/menu/SingleOfferShopMenu` | Menu with 4 tabs (Offers, Inventory, Settings, Log) |
| `feature/singleoffer/settings/*` | Immutable settings records with serialization, network codecs, and mutable Draft classes |

### Marketplace

| Class | Purpose |
| --- | --- |
| `feature/marketplace/data/MarketplaceManager` | Singleton — lifecycle, tick, persistence, purchase processing |
| `feature/marketplace/data/MarketplaceData` | Root data container (pages + offers) |
| `feature/marketplace/data/MarketplaceOffer` | Single offer with limits, pricing, and runtime state |
| `feature/marketplace/data/DemandPricing` | Demand-based dynamic pricing |
| `feature/marketplace/menu/MarketplaceMenu` | Menu for the Marketplace GUI |

### Cross-Cutting

| Class | Purpose |
| --- | --- |
| `network/NetworkHandler` | Central registration for all network payloads |
| `core/event/MarketBlocksEvents` | Server lifecycle, command registration, login notifications |
| `core/config/Config` | All config options (50+) |
| `core/data/ShopDirectorySavedData` | Global shop registry for `/marketblocks list` |
| `feature/log/ShopTransactionLogSavedData` | Persistent transaction log per shop |
| `feature/notification/PendingNotificationsSavedData` | Offline notification storage |

## Settings Architecture

The SingleOfferShop settings use an **immutable record + mutable draft** pattern:

1. **Immutable Record** (e.g., `GeneralSettings`): The canonical settings state. Contains `save()` / `load()` for NBT, a `STREAM_CODEC` for network sync, and `with*()` factory methods.
2. **Mutable Draft** (e.g., `GeneralSettings.Draft`): Used by the GUI for building up changes. The draft is converted to a settings record via `toSettings()` and sent to the server via `UpdateSettingsPacket`.
3. **ShopSettingsManager**: Holds all current settings for a block entity and handles sync/persistence.

## Events / Hooks

The mod is server-authoritative with clear network and lifecycle separation.

Especially relevant:
- Server start/tick/stop flow for Marketplace runtime (`MarketBlocksEvents`)
- Packet-based mutations with server-side validation
- Viewer synchronization after relevant state changes
- Login event for offline notification delivery

## Advancement Triggers

All triggers are registered in `RegistriesInit` and live under `feature/singleoffer/advancement/` and `feature/marketplace/advancement/`:

| Trigger | Description |
| --- | --- |
| `ShopSellTrigger` | Fires with cumulative sell count |
| `ShopNpcTrigger` | NPC enabled |
| `ShopNpcCustomizeTrigger` | NPC name or profession changed |
| `ShopCoOwnerTrigger` | Co-owner added |
| `ShopOutOfStockTrigger` | Shop went out of stock |
| `ShopWholesalerTrigger` | Bulk purchase occurred |
| `ShopRedstoneTrigger` | Redstone emission enabled |
| `ShopAutoIoTrigger` | Auto I/O enabled |
| `ShopAdminModeTrigger` | Admin shop mode enabled |
| `MarketplaceOpenTrigger` | Marketplace opened |
| `MarketplaceBuyTrigger` | Purchase from Marketplace |

## Data Formats / Integration

- Marketplace persistence via JSON: `<world>/marketblocks/marketplace.json`
- Backup/restore strategy with `.bak`
- Shop data stored via BlockEntity NBT
- Transaction log and notification data stored as `SavedData`
- Runtime views are kept separate from persistent configuration

## Integration Principles

- No client-side assumptions for critical transactions
- Apply changes only through validated server paths
- For external tools, plan defensive parsing and fallbacks
- Use the `feature/*` → `core/*` dependency direction; avoid direct feature-to-feature dependencies
