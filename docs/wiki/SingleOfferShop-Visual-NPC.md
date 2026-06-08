# SingleOfferShop: Visual NPC

The **Visual NPC** system allows shop owners to display an interactive NPC figure above their shop. The NPC can appear as a **Villager** with a configurable profession or as a **Player Skin** of any Minecraft player.

## Enabling the NPC

1. Open your shop and go to the **Settings** tab.
2. Select the **Villager** category.
3. Toggle the NPC on. A villager will appear above the shop block.

> **Note:** The NPC can only be placed if there is enough space above the shop. The placement is validated automatically. If the NPC cannot be placed (e.g., blocked by another block), it will not appear.

## NPC Modes

### Villager Mode (Default)

- Displays a Villager entity model above the shop
- Choose from **15 professions** to change the Villager's appearance:

| Profession | Profession | Profession |
| --- | --- | --- |
| None (default) | Armorer | Butcher |
| Cartographer | Cleric | Farmer |
| Fisherman | Fletcher | Leatherworker |
| Librarian | Mason | Nitwit |
| Shepherd | Toolsmith | Weaponsmith |

- Cycle through professions using the profession button in the Villager settings tab.

### Player Skin Mode

- Toggle **Use Player Skin** to display a player model instead of a villager.
- Enter the **player name** (up to 36 characters) to load their skin.
- The NPC will render with the specified player's skin.

## NPC Name

- Set a custom NPC name (up to 32 characters) that appears above the NPC's head.
- Allowed characters: letters, numbers, spaces, underscores, and hyphens.
- Leave empty for no name tag.

## Purchase Feedback Effects

The Villager settings tab also controls the visual and audio feedback when a purchase occurs:

| Setting | Default | Description |
| --- | --- | --- |
| **Purchase Particles** | ✅ On | Spawns particle effects on the NPC when a purchase is made |
| **Purchase Sounds** | ✅ On | Plays a sound effect on purchase |
| **Payment Slot Sounds** | ✅ On | Plays feedback sounds when items are placed in or removed from payment slots (success/fail sounds based on whether the payment matches the offer) |

## NPC Animations

The Visual NPC has a built-in animation system that responds to shop events:

- **Purchase animation**: The NPC reacts when a customer buys something.
- **Payment feedback**: Visual feedback based on payment slot interactions (success or failure).
- Animations are synced between server and client using animation nonce and event counters.

## XP Feedback Sound

In the **General** settings tab, there is an additional audio feature:

- **Purchase XP Sound**: When enabled, plays an experience orb sound on each purchase. The pitch scales with the purchase amount (louder for bulk purchases). This has a short cooldown to prevent audio spam.

## Configuration

### Server-Side Config

| Config Key | Default | Description |
| --- | --- | --- |
| `visualNpcForceOffscreenRendering` | `true` | Render NPCs even when near screen borders. Disable for stricter culling and better performance. |
| `visualNpcRenderViewDistance` | `128` | Maximum distance in blocks for rendering Visual NPCs (16–512). |
| `shopTabVillagerEnabled` | `true` | Whether the Villager settings tab is visible to players. |

### Default Values for New Shops

| Config Key | Default |
| --- | --- |
| `shopDefaultVillagerNpcEnabled` | `true` |
| `shopDefaultVillagerProfession` | `NONE` |
| `shopDefaultPurchaseParticles` | `true` |
| `shopDefaultPurchaseSounds` | `true` |
| `shopDefaultPaymentSlotSounds` | `true` |
| `shopDefaultUsePlayerSkin` | `false` |
