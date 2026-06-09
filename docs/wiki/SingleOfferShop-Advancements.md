# SingleOfferShop: Advancements

MarketBlocks features a comprehensive advancement system that guides players through the mechanics of both the **SingleOfferShop** and the **Marketplace**, rewarding them for exploring features and building their trading empire.

## Advancement Tree Overview

The advancement tree starts with a **root** advancement and branches into **4 paths**, each focusing on a different aspect of the mod.

```text
Root (Obtain a Trade Stand or Market Crate)
└── First Shop (Place your first shop)
    ├── Branch 1: Trade & Sales
    │   └── Sold Item → Out of Stock
    │                  → Wholesaler
    │                  → Tycoon (hidden)
    ├── Branch 2: Shop Design
    │   └── Showcase → Hiring → Custom NPC
    ├── Branch 3: Network & Cooperation
    │   └── Joint Venture → Wall Street → Marketplace Buy
    └── Branch 4: Technology & Automation
        └── Redstone → Auto I/O
                     → Admin Shop (hidden)
```

## All Advancements

### Root

| Advancement | Icon | Trigger | Type |
| --- | --- | --- | --- |
| **MarketBlocks** | Market Crate | Obtain a Trade Stand **or** Market Crate in your inventory | Task |
| **Your First Shop** | Trade Stand | Place a Trade Stand **or** Market Crate in the world | Task |

---

### Branch 1: Trade & Sales

| Advancement | Icon | Trigger | Type |
| --- | --- | --- | --- |
| **Sold Item** | Emerald | Make your first successful sale to another player | Task |
| **Out of Stock** | Barrier | Your shop runs out of stock (input inventory empty) | Goal |
| **Wholesaler** | Chest Minecart | Buy 64 or more items in one transaction | Challenge |
| **Tycoon** | Golden Apple | Sell 100 items total across all your shops (hidden — only appears after unlocking) | Challenge |

> The sell count is tracked globally per player using persistent saved data. It accumulates across all shops and play sessions.

---

### Branch 2: Shop Design

| Advancement | Icon | Trigger | Type |
| --- | --- | --- | --- |
| **Showcase** | Glass | Use Glass on a Trade Stand (activating the showcase feature) | Task |
| **Hiring** | Villager Spawn Egg | Enable a Visual NPC on your shop | Task |
| **Custom NPC** | Name Tag | Customize your NPC with a name or player skin | Task |

---

### Branch 3: Network & Cooperation

| Advancement | Icon | Trigger | Type |
| --- | --- | --- | --- |
| **Joint Venture** | Writable Book | Add a co-owner to your shop | Task |
| **Wall Street** | Filled Map | Open the Marketplace for the first time | Task |
| **Marketplace Buy** | Bundle | Make your first purchase from the Marketplace | Task |

---

### Branch 4: Technology & Automation

| Advancement | Icon | Trigger | Type |
| --- | --- | --- | --- |
| **Redstone** | Redstone Dust | Enable redstone emission or redstone-controlled I/O on a shop | Task |
| **Auto I/O** | Hopper | Enable the Auto I/O feature for a shop | Task |
| **Admin Shop** | Command Block | Enable Admin Shop Mode on a shop (hidden — requires global admin mode to be active) | Challenge |

## Notes

- All advancements are under the `marketblocks:marketblocks/` namespace.
- Hidden advancements (**Tycoon** and **Admin Shop**) only become visible in the advancement screen after they are unlocked.
- The advancement tree uses a stone background texture.
- Advancements serve as both a **tutorial** to introduce players to the Settings tab and a **progression system** to track their trading journey.
