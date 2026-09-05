# SingleOfferShop: Notifications

The **Notifications** system keeps shop owners informed about sales and inventory bottlenecks, ensuring you never miss a trade or lose business due to an unemptied cashbox.

---

## Notification Types

| Event | Icon | Description |
|---|:---:|---|
| **Purchase** | 💰 | A customer successfully purchased goods from your shop. Includes buyer name and quantity. |
| **Out of Stock** | ⚠️ | Your input inventory has run dry. No further sales can occur until restocked. |
| **Output Full** | 🛑 | Your payment storage has no free space to accept incoming payment items. |

---

## Delivery Modes

### Real-Time Alerts
If the primary owner (or co-owners) is online when a purchase or warning happens, a private chat notification is delivered immediately.

### Offline Notifications on Login
If you are offline when your shop runs out of stock or fills its output inventory:
- The alert is safely stored server-side.
- The moment you log back onto the server, a summary message lists which shops require attention along with their coordinates and dimensions.

### Cooldown Protection
To eliminate chat spam when customers purchase stacks of items in rapid succession, notifications are throttled by a configurable cooldown (default: 1200 ticks = 60 seconds).

---

## Per-Shop Configuration (Notifications Tab)

In each shop's **Settings -> Notifications** tab, owners can selectively enable or disable:

- **Notify on Purchase**: Send a chat message upon every transaction.
- **Notify on Out of Stock**: Alert when input stock reaches zero.
- **Notify on Output Full**: Alert when payments cannot be accepted.
- **Notify Co-Owners**: Forward all active notifications to registered co-owners.

---

## Server Configuration

Server administrators can fine-tune notification rules in `config/marketblocks/singleoffer/`:

- **In `general.toml`**:
  - `notificationCooldownTicks = 1200`: Minimum ticks between repeated notifications for the same shop.
  - `enableOutputWarning = true`: Displays a yellow/red warning badge directly on the GUI when output storage exceeds capacity threshold.
  - `outputWarningPercent = 90`: Capacity percentage considered "nearly full".
  - `buyerChatMessage = true`: Sends the buyer a receipt message.
  - `broadcastPurchaseToAll = false`: Server-wide broadcast on purchase.

- **In `tradestand.toml` and `marketcrate.toml`**:
  - Default settings for newly placed shop blocks (`notifyPurchase`, `notifyOutOfStock`, `notifyOutputFull`, `notifyCoOwners`).
