# SingleOfferShop: Settings Overview

The **Settings** tab in the SingleOfferShop is organized into **6 categories**, each with its own sub-tab. Shop owners (and operators when global admin mode is enabled) can access the settings to customize every aspect of their shop.

## Settings Categories

| Category | Icon | Description |
| --- | --- | --- |
| **General** | ⚙️ | Shop name, closed status, redstone emission, XP feedback sound |
| **I/O** | 🔄 | Side-based hopper/chest input and output, redstone control, auto I/O |
| **Villager** | 🧑‍🌾 | Visual NPC toggle, name, profession, player skin, particle and sound effects |
| **Visuals** | 🎨 | Offer item rendering: visibility, scale, speed, rotation, count, layout mode |
| **Notifications** | 🔔 | Toggle notifications for purchases, out of stock, output full, and co-owners |
| **Access** | 🔒 | Admin shop toggle, access mode (everyone/whitelist/blacklist), access list |

Each category is documented in detail on its own page:

- [Visual NPC](SingleOfferShop-Visual-NPC)
- [Offer Item Visuals](SingleOfferShop-Offer-Item-Visuals)
- [Notifications](SingleOfferShop-Notifications)
- [Access Control](SingleOfferShop-Access-Control)

## Server-Side Tab Visibility

Server administrators can **enable or disable individual settings tabs** via the config file (`marketblocks-common.toml`). When a tab is disabled, players cannot see or interact with it. Instead, the **default values** from the config are applied as fixed settings for all shops.

| Config Key | Default | Controls |
| --- | --- | --- |
| `shopTabGeneralEnabled` | `true` | General settings tab |
| `shopTabIoEnabled` | `true` | I/O settings tab |
| `shopTabVillagerEnabled` | `true` | Villager/NPC settings tab |
| `shopTabVisualsEnabled` | `true` | Visuals (offer item) settings tab |
| `shopTabNotificationsEnabled` | `true` | Notifications settings tab |
| `shopTabAccessEnabled` | `true` | Access control settings tab |

> **Use case:** If a server wants to enforce a consistent look (e.g., all shops must have NPC enabled with a specific profession), disable the Villager tab and set the desired defaults in the config.

## Default Values

When a new shop is placed, it uses the **default values** defined in the config. These also serve as **forced values** when the corresponding tab is disabled.

### General Defaults

| Config Key | Default | Description |
| --- | --- | --- |
| `shopDefaultEmitRedstone` | `false` | Whether new shops emit a redstone pulse on purchase |
| `shopDefaultPurchaseXpSound` | `true` | XP orb sound on purchase |
| `shopDefaultIsClosed` | `false` | Whether new shops start closed |

### Villager Defaults

| Config Key | Default | Description |
| --- | --- | --- |
| `shopDefaultVillagerNpcEnabled` | `true` | Whether the Visual NPC is enabled by default |
| `shopDefaultVillagerProfession` | `NONE` | Default NPC profession |
| `shopDefaultPurchaseParticles` | `true` | Purchase particle effects |
| `shopDefaultPurchaseSounds` | `true` | Purchase sound effects |
| `shopDefaultPaymentSlotSounds` | `true` | Payment slot feedback sounds |
| `shopDefaultUsePlayerSkin` | `false` | Use player skin instead of villager model |

### Visuals Defaults

| Config Key | Default | Description |
| --- | --- | --- |
| `shopDefaultItemVisible` | `true` | Show the offer item above the shop |
| `shopDefaultItemFullbright` | `false` | Render item with full brightness |
| `shopDefaultItemScale` | `0.75` | Item display scale (0.1–4.0) |
| `shopDefaultItemSpeed` | `2.0` | Rotation speed (0.0–20.0) |
| `shopDefaultItemHeightOffset` | `0.0` | Vertical offset (-2.0 to 4.0) |
| `shopDefaultItemBobbing` | `true` | Bobbing animation |
| `shopDefaultItemCount` | `1` | Number of rendered items (1–96) |
| `shopDefaultItemLayoutMode` | `GESTAPELT` | Layout mode for Market Crate (GESTAPELT or LOSE) |
| `shopDefaultItemDynamicFill` | `false` | Adjust item count based on stock level |

### Notification Defaults

| Config Key | Default | Description |
| --- | --- | --- |
| `shopDefaultNotifyPurchase` | `false` | Notify owner on purchase |
| `shopDefaultNotifyOutOfStock` | `false` | Notify owner when out of stock |
| `shopDefaultNotifyOutputFull` | `false` | Notify owner when output is full |
| `shopDefaultNotifyCoOwners` | `false` | Also notify co-owners |

## Other Global Config Options

| Config Key | Default | Description |
| --- | --- | --- |
| `shopBlastResistance` | `3600000.0` | Explosion resistance for all shop blocks. Default is bedrock-level. Set to `6.0` for obsidian-level or `3.0` for wood-like resistance. |
| `enableDoubleChestSupport` | `false` | Allow double chests next to Trade Stand |
| `enableChestIoExtensionExperimental` | `false` | Enable experimental chest I/O extension |
| `offerUpdateInterval` | `5` | Ticks between offer slot updates |
| `chestIoInterval` | `20` | Ticks between chest I/O transfers |
| `enableOutputWarning` | `true` | Show warning icon when output is nearly full |
| `outputWarningPercent` | `90` | Percentage threshold for "nearly full" warning |
| `notificationCooldownTicks` | `1200` | Minimum ticks (60 seconds) between repeated notifications |
| `maxCoOwnersPerShop` | `10` | Maximum number of co-owners per shop (0–100) |
| `enableGlobalOfferItemRendering` | `true` | Global master switch for offer item rendering |
| `visualNpcForceOffscreenRendering` | `true` | Render NPCs even when near screen borders |
| `visualNpcRenderViewDistance` | `128` | Maximum render distance for visual NPCs in blocks (16–512) |
