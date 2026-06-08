# SingleOfferShop: Offer Item Visuals

The **Visuals** settings tab lets shop owners customize how the offered item is displayed above their shop block. Items can be shown floating, spinning, and bobbing — with full control over size, speed, count, and layout.

## Enabling Item Display

By default, the offer item is rendered above the shop when an offer is active. You can toggle this in the **Visuals** category of the Settings tab.

> **Global Master Switch:** Server admins can disable all offer item rendering across the entire server using `enableGlobalOfferItemRendering = false` in the config. This overrides individual shop settings.

## Display Options

### Basic Settings

| Setting | Range | Default | Description |
| --- | --- | --- | --- |
| **Visible** | on/off | ✅ On | Whether to show the item at all |
| **Fullbright** | on/off | ❌ Off | Render the item at full brightness regardless of lighting |
| **Scale** | 0.1 – 4.0 | 0.75 | Size of the displayed item |
| **Speed** | 0.0 – 20.0 | 2.0 | Rotation speed of the item |
| **Height Offset** | -2.0 – 4.0 | 0.0 | Vertical offset from the default position |
| **Bobbing** | on/off | ✅ On | Gentle up-and-down floating animation |
| **Rotation** | 0° – 360° | 0° | Fixed rotation offset for the item |

### Multi-Item Display

| Setting | Range | Default | Description |
| --- | --- | --- | --- |
| **Count** | 1 – 96 | 1 | Number of items to render simultaneously |
| **Spacing XZ** | -0.5 – 2.0 | 0.0 | Horizontal spacing between items |
| **Spacing Y** | -0.5 – 2.0 | 0.0 | Vertical spacing between items |
| **Chaos Rotation** | 0.0 – 1.0 | 0.1 | Randomness of item rotation (0 = uniform, 1 = chaotic) |

### Layout Modes (Market Crate)

The Market Crate block supports two layout modes for displaying multiple items:

| Mode | Description |
| --- | --- |
| **GESTAPELT** (Stacked) | Items are arranged in a neat, grid-like pattern. Default mode. |
| **LOSE** (Loose/Scattered) | Items are placed in a more natural, scattered arrangement. |

### Dynamic Fill Level

When **Dynamic Fill Level** is enabled, the number of rendered items adjusts automatically based on the shop's current stock level. A fully stocked shop shows the configured number of items, while a nearly empty shop shows fewer.

This provides a visual indicator to customers about how much stock is left.

## Trade Stand vs. Market Crate

- **Trade Stand**: Items float above the top of the two-block-tall stand. Best for single or small-count items.
- **Market Crate**: Items render inside/above the crate. The layout mode (GESTAPELT/LOSE) only applies to Market Crates with count > 1.

## Configuration

### Server-Side Config

| Config Key | Default | Description |
| --- | --- | --- |
| `enableGlobalOfferItemRendering` | `true` | Global master switch — disable to save performance on busy servers |
| `shopTabVisualsEnabled` | `true` | Whether the Visuals settings tab is visible to players |

### Default Values for New Shops

| Config Key | Default |
| --- | --- |
| `shopDefaultItemVisible` | `true` |
| `shopDefaultItemFullbright` | `false` |
| `shopDefaultItemScale` | `0.75` |
| `shopDefaultItemSpeed` | `2.0` |
| `shopDefaultItemHeightOffset` | `0.0` |
| `shopDefaultItemBobbing` | `true` |
| `shopDefaultItemCount` | `1` |
| `shopDefaultItemLayoutMode` | `GESTAPELT` |
| `shopDefaultItemDynamicFill` | `false` |
