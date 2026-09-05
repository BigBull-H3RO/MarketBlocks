# SingleOfferShop: Settings Overview

The **Settings** tab in the SingleOfferShop GUI is organized into **6 categories**, each accessible via its own dedicated sub-tab. Shop owners (and operators when global edit mode is enabled) can configure every detail of their shop's behavior, visuals, automation, and security.

---

## Settings Categories

| Category | Icon | Description |
|---|:---:|---|
| **General** | ⚙️ | Custom shop name (max 32 chars), shop open/closed toggle, redstone pulse on sale, XP sound effect on purchase. |
| **I/O** | 🔄 | Side-based hopper/chest input and output routing, redstone control modes, automatic timed I/O transfers. |
| **Villager** | 🧑‍🌾 | Visual NPC toggle, custom NPC name, profession selection, player skin mode, celebratory particles, and trading sounds. |
| **Visuals** | 🎨 | Floating offer item rendering: visibility, fullbright, scale, rotation speed, height offset, and bobbing animation. |
| **Notifications** | 🔔 | Configurable chat and toast alerts for purchases, out of stock warnings, output full warnings, and co-owner broadcasts. |
| **Access** | 🔒 | Access mode (Everyone / Whitelist / Blacklist), customer access list, co-owner management, and Admin Shop toggle (operators only). |

---

## Configuration Architecture

MarketBlocks organizes SingleOfferShop configuration cleanly across three dedicated files inside `config/marketblocks/singleoffer/`:

1. **`general.toml`**: Global mechanics, blast resistance, survival placement limits, chest extension toggles, and tab visibility.
2. **`tradestand.toml`**: Default settings and forced fallbacks specifically for newly placed **Trade Stand** blocks.
3. **`marketcrate.toml`**: Default settings and forced fallbacks specifically for newly placed **Market Crate** blocks.

---

## Server Mechanics & Limits (`general.toml`)

Server administrators can fine-tune shop behavior and protections in `config/marketblocks/singleoffer/general.toml`:

```toml
[ShopMechanics]
# Explosion resistance for shop blocks (Trade Stand, Market Crate, etc.).
# Default: 3600000.0 (bedrock level, prevents explosion griefing).
shopBlastResistance = 3600000.0

# Maximum number of shops a player can place in Survival mode (-1 for unlimited)
maxShopsPerPlayerSurvival = 10

# Maximum number of co-owners allowed per SingleOffer shop
maxCoOwnersPerShop = 10

# Enable automatic pulling from and pushing to adjacent chests (chest extension).
# Default: false (Hides the I/O tab when disabled).
enableChestExtension = false

# Ticks between adjacent chest input/output transfers (Default: 20 ticks = 1 second)
chestIoInterval = 20

# Show warning icon when output inventory is (almost) full
enableOutputWarning = true
outputWarningPercent = 90

# Ticks to wait before sending another 'Out of Stock' or 'Output Full' notification (Default: 1200 = 1 minute)
notificationCooldownTicks = 1200

[Notifications]
buyerChatMessage = true
broadcastPurchaseToAll = false

[Tabs]
# Server administrators can disable specific tabs for players across all shops:
villager = true
visuals = true
notifications = true
```

---

## Default Values per Block Type (`tradestand.toml` & `marketcrate.toml`)

When a player places a new shop block, its initial settings are inherited from the corresponding config file. If a settings tab is disabled via `general.toml`, the default value defined here serves as the permanently enforced rule for all shops:

### General Section
- `emitRedstone` (Default: `false`): Emits a redstone pulse upon each successful trade.
- `purchaseXpSound` (Default: `false`): Plays an XP orb ding when a purchase is completed.
- `isClosed` (Default: `false`): Whether newly placed shops start in a closed state.

### Villager / NPC Section
- `enabled` (Default: `false`): Whether the visual NPC is rendered above the shop.
- `profession` (Default: `NONE`): Default villager costume / profession.
- `usePlayerSkin` (Default: `false`): Displays the shop owner's skin instead of a villager model.
- `purchaseParticles` (Default: `false`): Emits happy villager green particles upon trade.
- `purchaseSounds` (Default: `false`): Plays villager trade voice lines on purchase.
- `paymentSlotSounds` (Default: `false`): Plays villager ambient sounds when payment items are placed.

### Visuals (Floating Item) Section
- `visible` (Default: `true`): Displays the sold item floating in the showcase.
- `fullbright` (Default: `false`): Renders the item with maximum brightness ignoring ambient block light.
- `scale` (Default: `1.0`, range `0.5` to `1.5`): Size scale of the displayed item.
- `speed` (Default: `0.75`, range `0.0` to `1.5`): Rotation speed of the floating item.
- `heightOffset` (Default: `0.0`, range `-0.25` to `0.25`): Vertical adjustment.
- `bobbing` (Default: `false`): Enables smooth floating bobbing animation.

### I/O Automation Section
- `allowIo` (Default: `false`): Enables hopper / pipe insertion and extraction.
- `autoIo` (Default: `false`): Automatically pushes and pulls to adjacent storage chests.
- `redstoneControl` (Default: `IGNORED`): Redstone behavior (`IGNORED`, `LOW`, `HIGH`).

### Notifications Section
- `notifyPurchase` (Default: `false`): Sends owner a chat notification upon purchase.
- `notifyOutOfStock` (Default: `false`): Alerts owner when stock is depleted.
- `notifyOutputFull` (Default: `false`): Alerts owner when payment storage cannot fit more items.
- `notifyCoOwners` (Default: `false`): Broadcasts alerts to all registered co-owners.
