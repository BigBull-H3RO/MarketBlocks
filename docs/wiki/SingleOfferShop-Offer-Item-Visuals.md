# SingleOfferShop: Offer Item Visuals

The **Visuals** settings tab lets shop owners customize how sold items are showcased above their shop blocks. Goods can be displayed floating, rotating, and gently bobbing — with precise control over size, speed, elevation, and layout.

---

## Showcase Customization Options

| Setting | Range | Default | Description |
|---|:---:|:---:|---|
| **Visible** | Toggle | On | Toggles whether the floating offer item is rendered at all. |
| **Fullbright** | Toggle | Off | Renders the item with maximum brightness, ignoring ambient shadow or dark caves. |
| **Scale** | 0.5 – 1.5 | 1.0 | Adjusts the scale of the displayed item. |
| **Speed** | 0.0 – 1.5 | 0.75 | Adjusts the rotation speed of the item. Set to 0 for a static display. |
| **Height Offset** | -0.25 – 0.25 | 0.0 | Moves the item up or down relative to the counter surface. |
| **Bobbing** | Toggle | Off | Enables a gentle floating up-and-down oscillation motion. |

---

## Multi-Item Display & Crate Layouts

When using the **Market Crate** block variant:
- Items can be rendered in bulk directly inside the crate.
- Supports **Stacked** (neat grid) or **Loose** (natural scattered) arrangements.
- **Dynamic Stock Level Indicator**: When enabled, the visual quantity of items resting in the crate visibly depletes as customers buy stock, offering an intuitive at-a-glance stock meter for shoppers passing by.

---

## Performance & Server Configuration

MarketBlocks is designed to maintain high framerates even in crowded player malls with dozens of shops:

### Client Performance (`config/marketblocks/client.toml`)
```toml
[Rendering]
# Set to false on low-end PCs to disable all floating items,
# crate contents, and front recipe displays for a maximum FPS boost:
enableShopItemRendering = true
```

### Server Enforcement (`config/marketblocks/singleoffer/`)
- **Disable Tab**: In `general.toml`, setting `[Tabs] visuals = false` hides the tab from players and locks all shops to server defaults.
- **Default Visuals**: In `tradestand.toml` and `marketcrate.toml`, define standard fallback values for scale, speed, fullbright, and bobbing for newly placed shops.
