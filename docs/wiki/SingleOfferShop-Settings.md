# SingleOfferShop: Settings Guide

The **Settings** tab in the SingleOfferShop GUI allows shop owners and server operators to configure automation, NPC clerks, visual showcases, notifications, and customer access permissions.

The menu is organized into **6 sub-tabs**:

---

## 1. ⚙️ General Tab

Controls basic shop metadata, categorization, signals, and administrative mode:

- **Status (Active / Paused)**: Toggle button in the top-right header (🟢 Active / 🟠 Paused). When paused, customers cannot purchase items while you restock or adjust prices.
- **Shop Name**: Custom title (up to 32 characters) displayed at the top of the shop GUI and in `/mb search` results.
- **Shop ID Badge**: Displays the generated shop ID (e.g., `#A1B2`). Clicking the badge copies the ID to your clipboard.
- **Shop Category**: Assign a trading category (`General`, `Blocks`, `Food & Potions`, `Weapons & Armor`, `Tools`, `Valuables`, `Misc`). Visiting Trader NPCs evaluate this category to determine if your shop matches their shopping interests!
- **Emit Redstone**: Emits a redstone pulse upon every completed sale. A comparator can also be attached to read stock fullness.
- **Purchase XP Sound**: Plays a satisfying experience ding sound on successful purchases.
- **Admin Shop Mode Toggle**: *(Visible only to server operators with active `/mb admin editmode true`)*. Converts the shop into an infinite-supply server shop. See [Admin Shop Mode](SingleOfferShop-Admin-Shop-Mode).

---

## 2. 🔄 I/O (Automation) Tab

Enables automation with hoppers, item pipes, and adjacent chests:

- **Master Toggle (Allow I/O)**: Master switch in the top-right corner to enable or disable all automation for this shop.
- **Sided Directional Routing**: Configure each block face (**Back**, **Left**, **Right**, **Bottom**) independently:
  - `INPUT` (Green): Accepts stock items to replenish the shop's sell offer.
  - `OUTPUT` (Red): Extracts collected payment currency.
  - `DISABLED` (Gray): Blocks hoppers and pipes from interacting with this side.
- **Auto-I/O (Chest Extension)**: When enabled on the server (`enableChestExtension = true`), the shop automatically pulls stock from adjacent input chests and pushes earnings into adjacent profit chests without hoppers.
- **Redstone Control Mode**:
  - `Ignored`: Automation is always active.
  - `Active on Low`: Automation halts when powered by redstone.
  - `Active on High`: Automation only runs while receiving a redstone signal.

---

## 3. 🧑‍🌾 Villager / Clerk Tab

Places an animated clerk behind the shop counter:

- **Enable Clerk**: Master switch in the top-right. Displays a warning (`!`) if there is insufficient space behind the counter to place an NPC.
- **Clerk Name**: Sets a floating name tag above the clerk's head (up to 32 characters).
- **Villager Profession**: Cycle through **15 villager professions** (Farmer, Librarian, Armorer, Cleric, Weaponsmith, etc.).
- **Player Skin Mode**:
  - Check the **SKIN:** box and enter any Minecraft username to render that player's official skin (supports slim/classic models and skin layers).
  - Activating player skin mode disables the villager profession selector.
- **Feedback & Effects**:
  - **Purchase Particles**: Emits green happy-villager particles on trade.
  - **Purchase Sounds**: Plays villager affirmation voice lines when a sale occurs.
  - **Payment Slot Sounds**: Plays an interaction sound when customers insert payment.

---

## 4. 🎨 Visuals Tab

Controls how your goods are rendered in the world. The available options adapt depending on whether you are configuring a **Trade Stand** or a **Market Crate**:

### Global Control
- **Visible**: Master toggle in the top-right to show or hide all 3D item rendering.

### A. Trade Stand (Showcase Counter)
- **Fullbright**: Makes the floating item render at maximum brightness, ignoring dim cave or market lighting.
- **Bobbing Motion**: Toggles a gentle floating up-and-down animation.
- **Scale (0.5x – 1.5x)**: Adjusts item size.
- **Rotation (0° – 360°)**: Sets the orientation angle of the displayed item.
- **Height Offset (-0.25 to +0.25)**: Adjusts the item's floating height above the counter surface.

### B. Market Crate (Bulk Display)
- **Item Arrangement**:
  - **Layout Mode**: Choose between **Stacked** (neatly aligned rows) or **Scattered / Loose** (rustic, realistic market produce).
  - **Count (1 – 64)**: Number of visual item models rendered inside the crate.
  - **Dynamic Fill Level**: Visually depletes crate contents as customers buy items, providing an intuitive at-a-glance stock meter.
- **Transformations**:
  - **Fullbright**: Maximizes render brightness.
  - **Scale (0.5x – 1.5x)**: Overall item size.
  - **Rotation (0° – 360°)**: Base orientation angle.
  - **Spacing Y (0.0 – 2.0)**: Vertical separation between items.
  - *In Stacked Mode:* **Spacing XZ (-0.25 to +0.25)** controls horizontal grid spacing.
  - *In Scattered Mode:* **Chaos Rotation (0% – 100%)** randomizes individual item angles.

---

## 5. 🔔 Notifications Tab

Keeps shop managers informed about trading activity and stock levels:

- **Trade Activity**:
  - **Notify on Purchase**: Sends a chat confirmation whenever a customer buys goods.
  - **Notify Co-Owners**: Broadcasts purchase and warning notifications to all registered co-owners.
- **Status Warnings**:
  - **Notify on Out of Stock**: Alerts you when input storage runs empty.
  - **Notify on Output Full**: Warns you when profit storage is full and cannot accept further payments.
- **Offline Notifications**: Alerts triggered while offline are queued and delivered with coordinates upon your next server login.

---

## 6. 🔒 Access Tab

Exclusively accessible to the **Primary Owner** to manage store permissions:

- **Owners List**:
  - Register up to **10 trusted players** as Co-Owners.
  - Displays an active owner counter badge (e.g. `2 / 10`).
  - Co-owners have full access to restock, collect profits, modify prices, and adjust shop settings.
- **Access List**:
  - Toggle between **Whitelist** and **Blacklist** filter modes:
    - **Whitelist**: Only explicitly listed players are permitted to buy.
    - **Blacklist**: All players can buy *except* those on the blacklist.
  - Add or remove customer usernames from the active list.
