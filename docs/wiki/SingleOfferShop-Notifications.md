# SingleOfferShop: Notifications

The **Notifications** system keeps shop owners informed about important events in their shops, even when they are offline or away from the shop.

## Notification Types

| Event | Description |
| --- | --- |
| **Purchase** | A customer bought something from your shop. |
| **Out of Stock** | Your shop's input inventory has run out of the offered item. No more sales are possible until restocked. |
| **Output Full** | Your shop's output inventory is full. No more sales are possible until items are removed. |

## How Notifications Work

### Real-Time Notifications

When a notification event occurs, the server checks if the shop owner (and optionally co-owners) is online:

- **Online owners** receive a chat message immediately.
- **Offline owners** have the notification stored and delivered when they next log in.

### Offline Notification Delivery

When a player logs in, the system checks for any pending notifications:

- **Out of Stock**: Shows the number of affected shops and lists each shop's coordinates.
- **Output Full**: Shows the number of affected shops and lists each shop's coordinates.

These notifications are delivered once and then cleared. The data is persisted server-side, so it survives server restarts.

### Notification Cooldown

To prevent notification spam (e.g., during rapid repeated purchases), there is a configurable cooldown:

- **Default**: 1200 ticks (60 seconds)
- During the cooldown, the same type of notification for the same shop will not be sent again.

## Configuration

### Per-Shop Settings (Notifications Tab)

Each setting can be toggled individually in the **Notifications** category of the Settings tab:

| Setting | Default | Description |
| --- | --- | --- |
| **Notify on Purchase** | ❌ Off | Send a message when someone buys from this shop |
| **Notify on Out of Stock** | ❌ Off | Send a message when this shop runs out of stock |
| **Notify on Output Full** | ❌ Off | Send a message when the output inventory is full |
| **Notify Co-Owners** | ❌ Off | Also send notifications to all co-owners, not just the primary owner |

> **Note:** All notification toggles default to **off** for new shops. Enable the ones you need.

### Server-Side Config

| Config Key | Default | Description |
| --- | --- | --- |
| `notificationCooldownTicks` | `1200` | Minimum ticks between repeated notifications of the same type (0 = no cooldown). Default is 60 seconds. |
| `shopTabNotificationsEnabled` | `true` | Whether the Notifications settings tab is visible to players |
| `enableOutputWarning` | `true` | Show a visual warning icon in the GUI when the output inventory is nearly full |
| `outputWarningPercent` | `90` | Percentage threshold (1–100) for the "nearly full" output warning |

### Default Values for New Shops

| Config Key | Default |
| --- | --- |
| `shopDefaultNotifyPurchase` | `false` |
| `shopDefaultNotifyOutOfStock` | `false` |
| `shopDefaultNotifyOutputFull` | `false` |
| `shopDefaultNotifyCoOwners` | `false` |
