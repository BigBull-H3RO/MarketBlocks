# Marketplace: JSON Configuration Guide

This guide details the internal JSON schema and data structures used to persist Marketplace pages and offers. Server administrators and modpack creators can pre-configure market offerings directly via JSON.

---

## 📂 Storage Location & File Safety

Marketplace data is stored server-side per world/save at:

```
<world>/marketblocks/marketplace.json
```

- **Atomic Writes**: Whenever offers are updated, MarketBlocks writes to a temporary file (`.tmp`) before renaming it, preventing file corruption during server crashes.
- **Automatic Backups**: A safety copy `<world>/marketblocks/marketplace.json.bak` is maintained on every disk write.
- **In-Game Reload**: You can modify `marketplace.json` while the server is running and reload it instantly via:
  ```
  /mb admin reload
  ```

---

## 🧱 Data Architecture

```
MarketplaceData
└── pages [Array]
    └── MarketplacePage
        ├── name (String)
        └── offers [Array]
            └── MarketplaceOffer
                ├── id (UUID String or Int-Array, optional)
                ├── result (ItemStack)
                ├── payments [Array of ItemStacks, max 2]
                ├── limits (OfferLimit object)
                ├── pricing (DemandPricing object)
                └── runtime_state (MarketplaceOfferRuntimeState, optional)
```

---

## 📝 Field Specifications

### 1. Page Definition
| Field | Type | Required | Description |
|---|:---:|:---:|---|
| **`name`** | String | Yes | Category display name (e.g. `"Minerals"`, max 64 characters). |
| **`offers`** | Array | Yes | List of trade offers associated with this page. |

---

### 2. Offer Definition (`MarketplaceOffer`)
| Field | Type | Required | Description |
|---|:---:|:---:|---|
| **`id`** | String / Int[4] | No | Unique UUID. If omitted when creating new offers, a random UUID is automatically generated. |
| **`result`** | Object | Yes | The item sold to the customer. Follows standard Minecraft 1.21.1 `ItemStack` format. |
| **`payments`** | Array | Yes | 1 or 2 items required as payment from the customer. |
| **`limits`** | Object | No | Scarcity configuration. If omitted, defaults to unlimited. |
| **`pricing`** | Object | No | Dynamic pricing configuration. If omitted, defaults to disabled. |
| **`runtime_state`** | Object | No | Internal tracking data (stock, sales, temperature). Safe to omit in manual templates. |

---

### 3. Limits Schema (`limits`)
| Field | Type | Default | Description |
|---|:---:|:---:|---|
| **`unlimited`** | Boolean | `true` | When `true`, limits are bypassed entirely. Set to `false` to enforce limits. |
| **`daily_limit`** | Integer | *null* | Maximum purchases allowed per real-world day (resets at midnight). |
| **`stock_limit`** | Integer | *null* | Maximum pool inventory available before going out of stock. |
| **`restock_seconds`** | Integer | *null* | Interval in seconds after which depleted stock is replenished back to `stock_limit`. |

---

### 4. Dynamic Pricing Schema (`pricing`)
| Field | Type | Default | Description |
|---|:---:|:---:|---|
| **`enabled`** | Boolean | `false` | Enables or disables dynamic demand pricing for this offer. |
| **`base_multiplier`** | Double | `1.0` | Baseline price factor. |
| **`volatility`** | String | `"normal"` | Market sensitivity to purchases. Valid values: `"slow"`, `"normal"`, `"fast"`. |
| **`min_multiplier`** | Double | `0.25` | Price floor (e.g. `0.25` = 25% minimum base price). |
| **`max_multiplier`** | Double | `4.0` | Price ceiling (e.g. `4.0` = 400% maximum base price). |

---

## 📋 Complete Working Example

Below is a complete, valid `marketplace.json` configuration showcasing both standard trades and dynamic offers:

```json
{
  "pages": [
    {
      "name": "Building Supplies",
      "offers": [
        {
          "result": {
            "id": "minecraft:oak_log",
            "count": 64
          },
          "payments": [
            {
              "id": "minecraft:iron_ingot",
              "count": 8
            }
          ],
          "limits": {
            "unlimited": true
          },
          "pricing": {
            "enabled": false
          }
        }
      ]
    },
    {
      "name": "Exotics",
      "offers": [
        {
          "result": {
            "id": "minecraft:netherite_ingot",
            "count": 1
          },
          "payments": [
            {
              "id": "minecraft:diamond",
              "count": 8
            },
            {
              "id": "minecraft:emerald",
              "count": 32
            }
          ],
          "limits": {
            "unlimited": false,
            "stock_limit": 5,
            "restock_seconds": 3600,
            "daily_limit": 2
          },
          "pricing": {
            "enabled": true,
            "volatility": "fast",
            "base_multiplier": 1.0,
            "min_multiplier": 0.5,
            "max_multiplier": 3.0
          }
        }
      ]
    }
  ]
}
```

> [!NOTE]
> When defining custom items with NBT/Components (such as enchanted books, named tools, or potions), include the standard 1.21.1 `components` object inside `result` or `payments`.

