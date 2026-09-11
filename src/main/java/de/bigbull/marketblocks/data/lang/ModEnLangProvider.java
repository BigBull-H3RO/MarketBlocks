package de.bigbull.marketblocks.data.lang;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.core.init.RegistriesInit;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * Data provider for generating the English (en_us) language file.
 * Contains all translated strings for UI elements, messages, and blocks.
 */
public class ModEnLangProvider extends LanguageProvider {
        public ModEnLangProvider(PackOutput output) {
                super(output, MarketBlocks.MODID, "en_us");
        }

        @Override
        protected void addTranslations() {
                // === Creative Tab ===
                add("itemGroup.marketblocks", "MarketBlocks");

                // === Blocks & Items ===
                add("item.marketblocks.trade_stand.with_showcase", "Trade Stand (With Showcase)");

                // === Entities ===
                add("entity.marketblocks.shop_buyer", "Wandering Shop Buyer");
                add("item.marketblocks.shop_buyer_spawn_egg", "Wandering Shop Buyer Spawn Egg");
                addItem(RegistriesInit.TRADE_BOOK, "Trade Book");

                // === Containers & Menus ===
                add("container.marketblocks.trade_stand", "Trade Stand");
                add("menu.marketblocks.marketplace", "Marketplace");

                // === Keybinds ===
                add("key.categories.marketblocks", "MarketBlocks");
                add("key.marketblocks.open_marketplace", "Open Marketplace");

                // === Commands ===
                add("command.marketblocks.break.denied",
                                "\u00a7cYou cannot break this block while it is linked to a Marketplace!");
                add("command.marketblocks.break.unlinked", "\u00a7eThe block was unlinked from the Marketplace.");
                add("command.marketblocks.link.already_linked", "\u00a7cThis block is already linked to a Marketplace.");
                add("command.marketblocks.link.not_looking_at_block", "\u00a7cYou must look at a block to link it.");
                add("command.marketblocks.link.success", "\u00a7aSuccessfully linked block to Marketplace!");
                add("command.marketblocks.list.click_to_delete", "Click to delete");
                add("command.marketblocks.list.click_to_teleport", "Click to teleport");
                add("command.marketblocks.list.click_to_waypoint", "Click to get Waypoint links in chat");
                add("command.marketblocks.list.delete", "[Delete]");
                add("command.marketblocks.list.tp", "[Teleport]");
                add("command.marketblocks.list.waypoint", "[Waypoint]");
                add("command.marketblocks.list.page_header", "\u00a78======== \u00a76\u00a7lShops (Page %s/%s) \u00a78========");
                add("command.marketblocks.list.prev", "[< Prev]");
                add("command.marketblocks.list.next", "[Next >]");
                add("command.marketblocks.marketplacelist.entry", "\u00a78\u25aa \u00a76Marketplace \u00a7e%s");
                add("command.marketblocks.marketplacelist.page_header", "\u00a78======== \u00a76\u00a7lMarketplaces (Page %s/%s) \u00a78========");
                add("command.marketblocks.marketplacelist.no_links", "\u00a7cNo marketplaces found.");
                add("command.marketblocks.player_not_found", "\u00a7cPlayer not found");
                add("command.marketblocks.reload.success", "\u00a7aMarketBlocks configuration reloaded successfully!");
                add("command.marketblocks.resetlimits.no_changes", "\u00a7eNo daily limits were reset.");
                add("command.marketblocks.resetlimits.success", "\u00a7aDaily limits reset successfully.");
                add("command.marketblocks.search.header", "\u00a78======== \u00a76\u00a7lShops selling %s (Page %s/%s) \u00a78========");
                add("command.marketblocks.search.no_shops", "\u00a7cNo shops or marketplaces found selling %s.");
                add("command.marketblocks.shoplist.closed", "CLOSED");
                add("command.marketblocks.shoplist.entry", "\u00a78\u25aa \u00a77[%s\u00a77] \u00a7e%s \u00a78(by \u00a77%s\u00a78)");
                add("command.marketblocks.shoplist.header", "\u00a78======== \u00a76\u00a7lMarketBlocks Shops \u00a78========");
                add("command.marketblocks.shoplist.no_shops", "\u00a7cNo shops available.");
                add("command.marketblocks.shoplist.open", "OPEN");
                add("command.marketblocks.shoplist.hover.shop", "Shop: %s");
                add("command.marketblocks.shoplist.hover.owner", "Owner: %s");
                add("command.marketblocks.shoplist.hover.status", "Status: %s");
                add("command.marketblocks.shoplist.hover.offer", "Offer:");
                add("command.marketblocks.shoplist.hover.arrow", "\u2794");
                add("command.marketblocks.unlink.not_found", "\u00a7cCould not find the Marketplace link.");
                add("command.marketblocks.unlink.not_linked", "\u00a7cThis block is not linked.");
                add("command.marketblocks.unlink.not_looking_at_block", "\u00a7cYou must look at a block to unlink it.");
                add("command.marketblocks.unlink.success", "\u00a7aSuccessfully unlinked block!");
                add("command.marketblocks.unlink.success_name", "\u00a7aUnlinked from Marketplace: \u00a7e%s");
                add("command.marketblocks.waypoint.created", "\u00a7aWaypoint sharing links created:");
                add("command.marketblocks.internal.waypoint.coords", "Waypoint coordinates for %s: X: %d, Y: %d, Z: %d (%s)");
                add("command.marketblocks.internal.waypoint.journeymap", "JourneyMap waypoint created / link generated.");
                add("command.marketblocks.internal.waypoint.xaero", "Xaero's Minimap waypoint link:");
                add("command.marketblocks.internal.tp.no_permission", "You do not have permission to teleport to shops.");
                add("command.marketblocks.internal.tp.invalid_dimension", "The target dimension could not be found.");
                add("command.marketblocks.internal.tp.success", "Teleported to shop!");

                // === GUI - Settings ===
                add("gui.marketblocks.access.edit_access_list", "ACCESS LIST");
                add("gui.marketblocks.access.edit_access_list.tooltip", "Manage customer access list");
                add("gui.marketblocks.access.edit_owners", "CO-OWNERS");
                add("gui.marketblocks.access.edit_owners.tooltip", "Manage shop co-owners");
                add("gui.marketblocks.access.group.management", "MANAGEMENT");
                add("gui.marketblocks.access.group.players", "PLAYER LIST");
                add("gui.marketblocks.access.group.whitelist", "WHITELIST PLAYERS");
                add("gui.marketblocks.access.group.blacklist", "BLACKLIST PLAYERS");
                add("gui.marketblocks.access.counter", "%d / %d");
                add("gui.marketblocks.access.filter_whitelist", "MODE: WHITELIST");
                add("gui.marketblocks.access.filter_blacklist", "MODE: BLACKLIST");
                add("gui.marketblocks.access.filter_whitelist.tooltip", "Whitelist Mode: Only listed players may purchase");
                add("gui.marketblocks.access.filter_blacklist.tooltip", "Blacklist Mode: Listed players cannot purchase");
                add("gui.marketblocks.access.primary_owner_only", "Available to primary owner only");
                add("gui.marketblocks.access.mode.blacklist", "Mode: Blacklist");
                add("gui.marketblocks.access.mode.everyone", "Mode: Everyone");
                add("gui.marketblocks.access.mode.whitelist", "Mode: Whitelist");
                add("gui.marketblocks.io.status_label", "I/O:");
                add("gui.marketblocks.io.master_toggle.tooltip", "Enable / disable I/O system & hoppers");
                add("gui.marketblocks.io.group.sides", "BLOCK SIDES");
                add("gui.marketblocks.io.group.automation", "AUTOMATION & REDSTONE");
                add("gui.marketblocks.io.allow_io", "Allow I/O");
                add("gui.marketblocks.io.allow_io.tooltip", "Interaction with hoppers and pipes");
                add("gui.marketblocks.io.auto_io", "Chest Transfer (Auto-I/O):");
                add("gui.marketblocks.io.auto_io.tooltip", "Automatic item transfer with adjacent chests");
                add("gui.marketblocks.io.redstone_control.ignored", "Redstone: Ignored");
                add("gui.marketblocks.io.redstone_control.require_no_signal", "Redstone: Low (No Signal)");
                add("gui.marketblocks.io.redstone_control.require_signal", "Redstone: High (Signal Required)");
                add("gui.marketblocks.io.redstone_control.tooltip", "Redstone condition for I/O");
                add("gui.marketblocks.legend.input", "INPUT");
                add("gui.marketblocks.legend.output", "OUTPUT");
                add("gui.marketblocks.legend.disabled", "DISABLED");
                add("gui.marketblocks.settings_owner_only", "Only the owner can change settings");
                add("gui.marketblocks.settings_tab", "Settings");
                add("gui.marketblocks.settings_title", "Shop Settings");
                add("gui.marketblocks.toggle.off", "OFF");
                add("gui.marketblocks.toggle.on", "ON");
                add("gui.marketblocks.settings.category.access", "Access");
                add("gui.marketblocks.settings.category.access.title", "Access Settings");
                add("gui.marketblocks.settings.category.general", "General");
                add("gui.marketblocks.settings.category.general.title", "General Settings");
                add("gui.marketblocks.settings.category.io", "I/O");
                add("gui.marketblocks.settings.category.io.title", "I/O Settings");
                add("gui.marketblocks.settings.category.notifications", "Notifications");
                add("gui.marketblocks.settings.category.notifications.title", "Notification Settings");
                add("gui.marketblocks.settings.category.villager", "NPC");
                add("gui.marketblocks.settings.category.villager.title", "NPC Settings");
                add("gui.marketblocks.settings.category.visual", "Visual");
                add("gui.marketblocks.settings.category.visual.title", "Visual Settings");
                add("gui.marketblocks.settings.reset", "Reset to Defaults");
                add("gui.marketblocks.visuals.bobbing", "BOBBING:");
                add("gui.marketblocks.visuals.bobbing.tooltip", "Gentle bobbing motion");
                add("gui.marketblocks.visuals.chaos_rotation", "CHAOS ROTATION:");
                add("gui.marketblocks.visuals.chaos_rotation.tooltip", "Random item rotation variation");
                add("gui.marketblocks.visuals.count", "Item Count");
                add("gui.marketblocks.visuals.count.tooltip", "Number of visible items (1–64)");
                add("gui.marketblocks.visuals.count_short", "ITEM COUNT:");
                add("gui.marketblocks.visuals.display", "DISPLAY:");
                add("gui.marketblocks.visuals.group.item_arrangement", "ITEM ARRANGEMENT");
                add("gui.marketblocks.visuals.group.visuals_transformations", "VISUALS & TRANSFORMATIONS");
                add("gui.marketblocks.visuals.dynamic_fill_level", "DYN. FILL:");
                add("gui.marketblocks.visuals.dynamic_fill_level.tooltip", "Adjust display to stock level");
                add("gui.marketblocks.visuals.error.no_surface", "No stand surface behind the shop!");
                add("gui.marketblocks.visuals.error.space_blocked", "Space blocked!");
                add("gui.marketblocks.visuals.height", "HEIGHT:");
                add("gui.marketblocks.visuals.height.tooltip", "Floating height above block");
                add("gui.marketblocks.visuals.layout_mode", "Layout Mode");
                add("gui.marketblocks.visuals.layout_mode.tooltip", "Arrangement: Stacked or Loose");
                add("gui.marketblocks.visuals.layout_mode.gestapelt", "Stacked");
                add("gui.marketblocks.visuals.layout_mode.lose", "Loose");
                add("gui.marketblocks.visuals.npc_enabled", "Decorative Villager");
                add("gui.marketblocks.visuals.npc_enabled.tooltip", "Show/hide decorative NPC");
                add("gui.marketblocks.visuals.npc_name", "NPC Name");
                add("gui.marketblocks.visuals.npc_name.tooltip", "Name displayed above NPC");
                add("gui.marketblocks.visuals.offer_item_disabled_global", "Disabled by Server Admin.");
                add("gui.marketblocks.visuals.offer_item_fullbright", "GLOW:");
                add("gui.marketblocks.visuals.offer_item_fullbright.tooltip", "Makes the item glow in the dark");
                add("gui.marketblocks.visuals.offer_item_visible", "Offer Item Visible");
                add("gui.marketblocks.visuals.offer_item_visible.tooltip", "Show/hide item display");
                add("gui.marketblocks.visuals.payment_sounds", "PAYMENT SOUNDS:");
                add("gui.marketblocks.visuals.payment_sounds.tooltip", "Coin sound on payment");
                add("gui.marketblocks.visuals.purchase_particles", "PURCHASE PARTICLES:");
                add("gui.marketblocks.visuals.purchase_particles.tooltip", "Emerald particles on purchase");
                add("gui.marketblocks.visuals.purchase_sounds", "PURCHASE SOUNDS:");
                add("gui.marketblocks.visuals.purchase_sounds.tooltip", "Villager purchase sound");
                add("gui.marketblocks.visuals.npc_short", "NPC:");
                add("gui.marketblocks.visuals.npc_name_label", "NAME:");
                add("gui.marketblocks.visuals.rotation", "ROTATION:");
                add("gui.marketblocks.visuals.rotation.tooltip", "Base rotation of items");
                add("gui.marketblocks.visuals.rotation_x", "ROTATION X:");
                add("gui.marketblocks.visuals.rotation_y", "ROTATION Y:");
                add("gui.marketblocks.visuals.rotation_z", "ROTATION Z:");
                add("gui.marketblocks.visuals.scale", "SCALE:");
                add("gui.marketblocks.visuals.scale.tooltip", "Size of items");
                add("gui.marketblocks.visuals.spacing_xz", "SPACING X/Z:");
                add("gui.marketblocks.visuals.spacing_xz.tooltip", "Horizontal offset");
                add("gui.marketblocks.visuals.spacing_y", "SPACING Y:");
                add("gui.marketblocks.visuals.spacing_y.tooltip", "Vertical item spacing");
                add("gui.marketblocks.visuals.speed", "SPEED:");
                add("gui.marketblocks.visuals.speed.tooltip", "Rotation speed");
                add("gui.marketblocks.visuals.use_player_skin_short", "SKIN:");
                add("gui.marketblocks.visuals.use_player_skin", "Player Skin");
                add("gui.marketblocks.visuals.use_player_skin.tooltip", "Player model instead of villager");
                add("gui.marketblocks.visuals.group.npc_appearance", "APPEARANCE");
                add("gui.marketblocks.visuals.group.npc_feedback", "FEEDBACK & EFFECTS");
                add("gui.marketblocks.visuals.npc_sounds", "NPC Sounds");
                add("gui.marketblocks.visuals.npc_sounds.tooltip", "Controls when the NPC plays audio feedback (purchase, payment slots, or none).");
                add("gui.marketblocks.visuals.npc_sounds.all", "All");
                add("gui.marketblocks.visuals.npc_sounds.purchase", "Purchase only");
                add("gui.marketblocks.visuals.npc_sounds.payment", "Payment only");
                add("gui.marketblocks.visuals.npc_sounds.off", "Off");
                add("gui.marketblocks.visuals.player_skin_name", "Player Name");
                add("gui.marketblocks.visuals.player_skin_name.tooltip", "Skin of the specified player");
                add("gui.marketblocks.visuals.profession", "Profession");
                add("gui.marketblocks.visuals.profession.tooltip", "Outfit & profession of NPC");
                add("gui.marketblocks.visuals.profession.armorer", "Armorer");
                add("gui.marketblocks.visuals.profession.butcher", "Butcher");
                add("gui.marketblocks.visuals.profession.cartographer", "Cartographer");
                add("gui.marketblocks.visuals.profession.cleric", "Cleric");
                add("gui.marketblocks.visuals.profession.farmer", "Farmer");
                add("gui.marketblocks.visuals.profession.fisherman", "Fisherman");
                add("gui.marketblocks.visuals.profession.fletcher", "Fletcher");
                add("gui.marketblocks.visuals.profession.leatherworker", "Leatherworker");
                add("gui.marketblocks.visuals.profession.librarian", "Librarian");
                add("gui.marketblocks.visuals.profession.mason", "Mason");
                add("gui.marketblocks.visuals.profession.nitwit", "Nitwit");
                add("gui.marketblocks.visuals.profession.none", "Unemployed");
                add("gui.marketblocks.visuals.profession.shepherd", "Shepherd");
                add("gui.marketblocks.visuals.profession.toolsmith", "Toolsmith");
                add("gui.marketblocks.visuals.profession.weaponsmith", "Weaponsmith");

                // === GUI - Notifications ===
                add("gui.marketblocks.notifications.group.trade_activity", "TRADE ACTIVITY");
                add("gui.marketblocks.notifications.group.status_warnings", "STATUS WARNINGS");
                add("gui.marketblocks.notifications.co_owners", "NOTIFY CO-OWNERS:");
                add("gui.marketblocks.notifications.co_owners.tooltip", "Send notifications to co-owners");
                add("gui.marketblocks.notifications.out_of_stock", "OUT OF STOCK WARNING:");
                add("gui.marketblocks.notifications.out_of_stock.tooltip", "Warning when shop is empty");
                add("gui.marketblocks.notifications.output_full", "OUTPUT FULL WARNING:");
                add("gui.marketblocks.notifications.output_full.tooltip", "Warning when output is full");
                add("gui.marketblocks.notifications.purchase", "PURCHASE NOTIFICATIONS:");
                add("gui.marketblocks.notifications.purchase.tooltip", "Chat notification on purchase");
                add("message.marketblocks.notifications.out_of_stock", "\u00a7cYour shop is out of stock!\u00a7r");
                add("message.marketblocks.notifications.output_full", "\u00a7cYour shop's output inventory is full!\u00a7r");
                add("message.marketblocks.notifications.purchase", "\u00a7a%s bought %sx %s from your shop.\u00a7r");

                // === GUI - Marketplace Editors ===
                add("gui.marketblocks.marketplace.editor.limits.daily", "Daily Limit");
                add("gui.marketblocks.marketplace.editor.limits.restock", "Restock (s)");
                add("gui.marketblocks.marketplace.editor.limits.stock", "Stock Limit");
                add("gui.marketblocks.marketplace.editor.limits.title", "Limits");
                add("gui.marketblocks.marketplace.editor.limits.daily.tooltip", "Maximum purchases a player can make per day.");
                add("gui.marketblocks.marketplace.editor.limits.stock.tooltip", "Total available stock for this offer.");
                add("gui.marketblocks.marketplace.editor.limits.restock.tooltip", "Time in seconds until the stock replenishes.");
                add("gui.marketblocks.marketplace.editor.pricing.disabled", "Pricing OFF");
                add("gui.marketblocks.marketplace.editor.pricing.enabled", "Pricing ON");
                add("gui.marketblocks.marketplace.editor.pricing.label", "Enable Pricing");
                add("gui.marketblocks.marketplace.editor.pricing.label.tooltip", "If enabled, the price scales dynamically based on market temperature.");
                add("gui.marketblocks.marketplace.editor.pricing.base", "Base Price (%)");
                add("gui.marketblocks.marketplace.editor.pricing.base.tooltip", "Base price scaling factor in percent (e.g. 100 = normal, 150 = 50% markup). Applied before demand adjustments.");
                add("gui.marketblocks.marketplace.editor.pricing.max", "Max Price (%)");
                add("gui.marketblocks.marketplace.editor.pricing.max.tooltip", "Maximum percentage the price can reach (e.g. 200 = double price).");
                add("gui.marketblocks.marketplace.editor.pricing.min", "Min Price (%)");
                add("gui.marketblocks.marketplace.editor.pricing.min.tooltip", "Minimum percentage the price can drop to (e.g. 50 = half price).");
                add("gui.marketblocks.marketplace.editor.pricing.volatility", "Volatility");
                add("gui.marketblocks.marketplace.editor.pricing.volatility.tooltip", "How fast the price reacts to purchases and time decay.");
                add("gui.marketblocks.marketplace.editor.pricing.volatility.slow", "Slow");
                add("gui.marketblocks.marketplace.editor.pricing.volatility.normal", "Normal");
                add("gui.marketblocks.marketplace.editor.pricing.volatility.fast", "Fast");
                add("gui.marketblocks.marketplace.editor.pricing.title", "Pricing");

                // === GUI - Marketplace Elements ===
                add("gui.marketblocks.marketplace.add_offer", "Add Offer");
                add("gui.marketblocks.marketplace.add_page", "Add Page");
                add("gui.marketblocks.marketplace.delete_offer", "Delete Offer");
                add("gui.marketblocks.marketplace.delete_page", "Delete Page");
                add("gui.marketblocks.marketplace.inline.limits", "Edit limits");
                add("gui.marketblocks.marketplace.inline.limits.disabled_global", "Disabled: Global Limits are active in server config");
                add("gui.marketblocks.marketplace.inline.pricing", "Edit pricing");
                add("gui.marketblocks.marketplace.inline.pricing.disabled_global", "Disabled: Global Pricing is active in server config");
                add("gui.marketblocks.marketplace.mode.edit", "Switch to edit mode");
                add("gui.marketblocks.marketplace.mode.view", "Switch to view mode");
                add("gui.marketblocks.marketplace.move_offer", "Move Offer");
                add("gui.marketblocks.marketplace.move_offer_down", "Move offer down");
                add("gui.marketblocks.marketplace.move_offer_up", "Move offer up");
                add("gui.marketblocks.marketplace.no_offers", "No offers");
                add("gui.marketblocks.marketplace.no_pages", "No pages\navailable");
                add("gui.marketblocks.marketplace.rename_page", "Rename Page");
                add("gui.marketblocks.marketplace.sidebar.next", "Next Category Page");
                add("gui.marketblocks.marketplace.sidebar.prev", "Previous Category Page");
                add("gui.marketblocks.marketplace.status.daily_short", "D:%s");
                add("gui.marketblocks.marketplace.status.price_short", "x%s");
                add("gui.marketblocks.marketplace.status.restock_short", "R:%s");
                add("gui.marketblocks.marketplace.status.stock_short", "S:%s");
                add("gui.marketblocks.marketplace.tooltip.price_multiplier", "Price factor.");
                add("gui.marketblocks.marketplace.tooltip.remaining_daily", "Your remaining purchases today.");
                add("gui.marketblocks.marketplace.tooltip.remaining_daily_empty", "Daily limit reached.");
                add("gui.marketblocks.marketplace.tooltip.remaining_stock", "Remaining stock purchases.");
                add("gui.marketblocks.marketplace.tooltip.remaining_stock_empty", "Out of stock.");
                add("gui.marketblocks.marketplace.tooltip.restock_in", "Time until restock.");
                add("gui.marketblocks.marketplace.tooltip.restock_ready", "Restock due now.");
                add("gui.marketblocks.marketplace.tooltip.unavailable_daily", "Unavailable: daily limit reached.");
                add("gui.marketblocks.marketplace.tooltip.unavailable_generic", "Unavailable.");
                add("gui.marketblocks.marketplace.tooltip.unavailable_restock", "Unavailable: restocking.");
                add("gui.marketblocks.marketplace.tooltip.unavailable_stock", "Unavailable: out of stock.");
                add("gui.marketblocks.marketplace.unnamed_page", "Page %s");

                // === GUI - General ===
                add("gui.marketblocks.admin_shop.disabled", "Admin-Shop: OFF");
                add("gui.marketblocks.admin_shop.enabled", "Admin-Shop: ON");
                add("gui.marketblocks.admin_shop.button.active", "ADMIN MODE: ACTIVE");
                add("gui.marketblocks.admin_shop.button.inactive", "ADMIN MODE: DISABLED");
                add("gui.marketblocks.admin_shop.button.tooltip", "Toggles Admin Shop mode. In Admin mode the shop has infinite stock and requires no chest inventory.");
                add("gui.marketblocks.admin_shop.badge", "★ ADMIN SHOP");
                add("gui.marketblocks.admin_shop.badge.tooltip", "Admin Shop active: Infinite stock. Items are not required or consumed from the chest inventory.");
                add("gui.marketblocks.category", "Category");
                add("gui.marketblocks.category.none", "None");
                add("gui.marketblocks.category.weapons_armor", "Weapons & Armor");
                add("gui.marketblocks.category.tools", "Tools");
                add("gui.marketblocks.category.blocks", "Blocks");
                add("gui.marketblocks.category.food_potions", "Food & Potions");
                add("gui.marketblocks.category.valuables", "Valuables");
                add("gui.marketblocks.category.misc", "Miscellaneous");
                add("gui.marketblocks.category.tooltip", "Category in market directory");
                add("gui.marketblocks.create_offer", "Create Offer");
                add("gui.marketblocks.delete_offer", "Delete Offer");
                add("gui.marketblocks.disabled", "Disabled");
                add("gui.marketblocks.emit_redstone", "REDSTONE SIGNAL:");
                add("gui.marketblocks.emit_redstone.tooltip", "Short redstone pulse on purchase");
                add("gui.marketblocks.general.status_label", "STATUS:");
                add("gui.marketblocks.general.status.active", "ACTIVE");
                add("gui.marketblocks.general.status.paused", "PAUSED");
                add("gui.marketblocks.general.status.active.tooltip", "Shop open (Click to pause)");
                add("gui.marketblocks.general.status.paused.tooltip", "Shop paused (Click to open)");
                add("gui.marketblocks.general.group.shop_profile", "SHOP PROFILE");
                add("gui.marketblocks.general.group.features", "FEATURES & SIGNALS");
                add("gui.marketblocks.general.shop_name_label", "NAME:");
                add("gui.marketblocks.error.invalid_offer", "Invalid offer configuration");
                add("gui.marketblocks.error.no_payment_items", "Please place at least one payment item");
                add("gui.marketblocks.error.no_result_item", "Please place an item in the result slot");
                add("gui.marketblocks.input", "Input");
                add("gui.marketblocks.inventory_admin_disabled", "Inventory disabled in admin mode");
                add("gui.marketblocks.inventory_owner_only", "Only the owner can manage inventory");
                add("gui.marketblocks.inventory_tab", "Inventory");
                add("gui.marketblocks.inventory_title", "Trade Stand Inventory");
                add("gui.marketblocks.log_tab", "Log");
                add("gui.marketblocks.log_title", "Transaction Log");
                add("gui.marketblocks.log.clear", "Clear log");
                add("gui.marketblocks.log.count", "Entries: %s");
                add("gui.marketblocks.log.empty", "No transactions yet");
                add("gui.marketblocks.log.none", "None");
                add("gui.marketblocks.log.time.days", "%s d ago");
                add("gui.marketblocks.log.time.hours", "%s h ago");
                add("gui.marketblocks.log.time.just_now", "Just now");
                add("gui.marketblocks.log.time.minutes", "%s min ago");
                add("gui.marketblocks.log.time.seconds", "%s sec ago");
                add("gui.marketblocks.mode.edit_active", "EDIT MODE");
                add("gui.marketblocks.no_players_available", "No players available");
                add("gui.marketblocks.offers", "Offers");
                add("gui.marketblocks.offers_tab", "Offers");
                add("gui.marketblocks.out_of_stock", "Out of Stock");
                add("gui.marketblocks.output", "Output");
                add("gui.marketblocks.output_full", "Output Full");
                add("gui.marketblocks.owner", "Owner: %s");
                add("gui.marketblocks.purchase_sound", "XP PURCHASE SOUND:");
                add("gui.marketblocks.purchase_sound.tooltip", "Sound effect on purchase");
                add("gui.marketblocks.purchase_xp_sound", "XP PURCHASE SOUND:");
                add("gui.marketblocks.purchase_xp_sound.tooltip", "Sound effect on purchase");
                add("gui.marketblocks.save", "Save");
                add("gui.marketblocks.save.active.tooltip", "Save changes");
                add("gui.marketblocks.save.inactive.tooltip", "No unsaved changes");
                add("gui.marketblocks.shop_closed", "Shop Paused");
                add("gui.marketblocks.shop_closed.tooltip", "If active, the shop is paused and nobody can buy items.");
                add("gui.marketblocks.shop_name", "Shop Name");
                add("gui.marketblocks.shop_name.tooltip", "Display name of the shop");
                add("gui.marketblocks.shop_title", "Trade Stand");
                add("gui.marketblocks.side.back", "Back");
                add("gui.marketblocks.side.back.letter", "B");
                add("gui.marketblocks.side.bottom", "Bottom");
                add("gui.marketblocks.side.bottom.letter", "D");
                add("gui.marketblocks.side.left", "Left");
                add("gui.marketblocks.side.left.letter", "L");
                add("gui.marketblocks.side.right", "Right");
                add("gui.marketblocks.side.right.letter", "R");

                // === Messages & Chat ===
                add("message.marketblocks.marketplace.daily_limit_reached",
                                "The daily limit for this offer has been reached.");
                add("message.marketblocks.marketplace.edit_mode_disabled", "Marketplace edit mode disabled.");
                add("message.marketblocks.marketplace.edit_mode_enabled", "Marketplace edit mode enabled.");
                add("message.marketblocks.marketplace.limits.invalid_data", "Could not save limits: invalid data.");
                add("message.marketblocks.marketplace.limits.invalid_positive_int",
                                "Please enter only positive whole numbers for limits.");
                add("message.marketblocks.marketplace.limits.no_connection",
                                "Could not save limits: no server connection.");
                add("message.marketblocks.marketplace.page_limit_reached",
                                "The maximum number of %s pages has been reached.");
                add("message.marketblocks.marketplace.page_name_blank", "The page name must not be empty.");
                add("message.marketblocks.marketplace.page_name_duplicate", "A page named '%s' already exists.");
                add("message.marketblocks.marketplace.page_name_too_long",
                                "The page name must be at most %s characters long.");
                add("message.marketblocks.marketplace.page_not_found", "The selected shop page could not be found.");
                add("message.marketblocks.marketplace.pricing.invalid_data", "Could not save pricing: invalid data.");
                add("message.marketblocks.marketplace.pricing.invalid_finite",
                                "Please enter valid finite numbers for pricing.");
                add("message.marketblocks.marketplace.pricing.invalid_number_format",
                                "Please use numbers only (dot or comma allowed).");
                add("message.marketblocks.marketplace.pricing.no_connection",
                                "Could not save pricing: no server connection.");
                add("message.marketblocks.trade_stand.no_offer", "This trade stand currently has no active offer.");
                add("message.marketblocks.trade_stand.not_owner", "Only the owner can break this trade stand.");
                add("message.marketblocks.trade_stand.break_not_empty", "You must first empty all items and payouts!");
                add("message.marketblocks.shop.limit_reached", "You can place a maximum of %s shops!");
                
                add("message.marketblocks.shop_buyer.interact.1", "\u00a7eI'm looking for some good items to buy!\u00a7r");
                add("message.marketblocks.shop_buyer.interact.2", "\u00a7eDo you have anything interesting for sale?\u00a7r");
                add("message.marketblocks.shop_buyer.interact.3", "\u00a7eI travel around to buy things. Maybe you have what I need!\u00a7r");
                add("message.marketblocks.shop_buyer.interact.4", "\u00a7eI just found a great deal! I love shopping here!\u00a7r");
                add("message.marketblocks.shop_buyer.interact.5", "\u00a7eAnother good purchase! My bag is getting heavy.\u00a7r");
                add("message.marketblocks.shop_buyer.interact.6", "\u00a7eI got what I needed, thanks to these shops!\u00a7r");
                add("message.marketblocks.shop_buyer.interact.7", "\u00a7eHmm, I'm looking for something specific...\u00a7r");
                add("message.marketblocks.shop_buyer.interact.8", "\u00a7eI wonder what other shops are around here...\u00a7r");
                add("message.marketblocks.shop_buyer.interact.9", "\u00a7eJust browsing for now. Nothing has caught my eye yet.\u00a7r");
                add("message.marketblocks.shop_buyer.interact.10", "\u00a7eNice shop you have here! I'll keep it in mind.\u00a7r");

                // NPC Rank & Category display (G2)
                add("message.marketblocks.shop_buyer.info", "\u00a77[%s - %s]");
                add("entity.marketblocks.shop_buyer.rank.citizen", "Citizen");
                add("entity.marketblocks.shop_buyer.rank.wealthy", "Wealthy Trader");
                add("entity.marketblocks.shop_buyer.rank.noble", "Noble Merchant");
                add("entity.marketblocks.shop_buyer.category.general", "General Trader");
                add("entity.marketblocks.shop_buyer.category.farmer", "Farmer");
                add("entity.marketblocks.shop_buyer.category.alchemist", "Alchemist");
                add("entity.marketblocks.shop_buyer.category.blacksmith", "Blacksmith");
                add("entity.marketblocks.shop_buyer.category.valuables", "Collector");

                // Rage & Revenge Easter Egg messages
                add("message.marketblocks.shop_buyer.rage.1", "\u00a7c%s: Stop bothering me! You asked for this!\u00a7r");
                add("message.marketblocks.shop_buyer.rage.2", "\u00a7c%s: That's it! I've had enough of you!\u00a7r");
                add("message.marketblocks.shop_buyer.rage.3", "\u00a7c%s: You want trouble? You got it!\u00a7r");
                add("message.marketblocks.shop_buyer.revenge", "\u00a7c%s: You again?! I haven't forgotten what you did!\u00a7r");

                // === Jade / Waila Support ===
                add("config.jade.plugin_marketblocks.shop_info", "Shop Info");
                add("config.jade.plugin_marketblocks.shop_buyer_info", "Shop Buyer Info");
                add("marketblocks.jade.for", "For:");
                add("marketblocks.jade.out_of_stock", "Out of Stock!");
                add("marketblocks.jade.output_full", "Inventory Full!");
                add("marketblocks.jade.owner", "Owner: %s");
                add("marketblocks.jade.shop", "Shop: %s");
                add("marketblocks.jade.selling", "Selling:");
                add("marketblocks.jade.status.admin_shop", "Admin Shop");
                add("marketblocks.jade.status.closed", "Shop Closed");
                add("marketblocks.jade.trader.budget", "Budget: %s");

                // === Advancements ===
                add("advancements.marketblocks.admin_shop.description", "Enable admin shop mode");
                add("advancements.marketblocks.admin_shop.title", "Infinite Goods");
                add("advancements.marketblocks.auto_io.description", "Enable automatic input/output for your shop");
                add("advancements.marketblocks.auto_io.title", "Logistics");
                add("advancements.marketblocks.custom_npc.description",
                                "Customize your shop NPC with a name or player skin");
                add("advancements.marketblocks.custom_npc.title", "Custom Staff");
                add("advancements.marketblocks.first_shop.description", "Place your first MarketBlocks shop block");
                add("advancements.marketblocks.first_shop.title", "Open for Business");
                add("advancements.marketblocks.hiring.description", "Enable a shop NPC for your shop");
                add("advancements.marketblocks.hiring.title", "Now Hiring");
                add("advancements.marketblocks.joint_venture.description", "Add a co-owner to your shop");
                add("advancements.marketblocks.joint_venture.title", "Joint Venture");
                add("advancements.marketblocks.marketplace_buy.description", "Buy an item through the Marketplace");
                add("advancements.marketblocks.marketplace_buy.title", "Mall Shopper");
                add("advancements.marketblocks.out_of_stock.description", "Have a non-admin shop run out of stock");
                add("advancements.marketblocks.out_of_stock.title", "Out of Stock");
                add("advancements.marketblocks.redstone.description",
                                "Enable redstone output or redstone-controlled I/O");
                add("advancements.marketblocks.redstone.title", "Redstone Logic");
                add("advancements.marketblocks.root.description", "Obtain a MarketBlocks shop block");
                add("advancements.marketblocks.root.title", "MarketBlocks");
                add("advancements.marketblocks.showcase.description", "Add a glass showcase to a Trade Stand");
                add("advancements.marketblocks.showcase.title", "Show off");
                add("advancements.marketblocks.sold_item.description", "Sell your first item to another player");
                add("advancements.marketblocks.sold_item.title", "First Sale!");
                add("advancements.marketblocks.tycoon.description", "Sell 100 items through your shops");
                add("advancements.marketblocks.tycoon.title", "Tycoon");
                add("advancements.marketblocks.wall_street.description", "Open the Marketplace");
                add("advancements.marketblocks.wall_street.title", "Wall Street");
                add("advancements.marketblocks.wholesaler.description", "Buy 64 or more items in one transaction");
                add("advancements.marketblocks.wholesaler.title", "Wholesaler");

                // === Subtitles ===
                add("subtitles.marketblocks.visual_npc_fall", "Villager lands");

                // === Login Notifications ===
                add("gui.marketblocks.notifications.login.out_of_stock", "\u00a7c[MarketBlocks] %s of your shops are out of stock!\u00a7r");
                add("gui.marketblocks.notifications.login.output_full", "\u00a7c[MarketBlocks] %s of your shops have full output storage!\u00a7r");
                add("gui.marketblocks.notifications.login.coordinate", "\u00a77 - Location: X: %s, Y: %s, Z: %s\u00a7r");

                // === Purchase Confirmations ===
                add("message.marketblocks.purchase_success", "You have successfully purchased %s x %s.");
                add("message.marketblocks.purchase_success.global", "%s has purchased %s x %s.");

                // === Admin Commands ===
                add("command.marketblocks.trader.value.set", "Set value of %s to %s.");
                add("command.marketblocks.trader.value.remove", "Removed value for %s.");
                add("command.marketblocks.trader.blacklist.add", "Added %s to blacklist.");
                add("command.marketblocks.trader.blacklist.remove", "Removed %s from blacklist.");
                add("command.marketblocks.sale.set.success", "Sale activated for [%s]: Price change %s (Duration: %s min)");
                add("command.marketblocks.sale.remove.success", "Sale ended for [%s].");
                add("command.marketblocks.sale.not_found", "Offer / Shop not found: %s");
                add("command.marketblocks.sale.failed", "Failed to modify sale.");
                add("command.marketblocks.stats.shop.header", "--- Top 10 SingleOfferShops ---");
                add("command.marketblocks.stats.shop", "Shop Stats for: %s");
                add("command.marketblocks.stats.shop.empty", "No shops available.");
                add("command.marketblocks.stats.shop.entry", "%s. %s - %s Sales");
                add("command.marketblocks.stats.shop.total_sales", "Total Sales: %d");
                add("command.marketblocks.stats.marketplace.header", "--- Top 10 Marketplace Offers ---");
                add("command.marketblocks.stats.marketplace.empty", "No offers available.");
                add("command.marketblocks.stats.marketplace.entry", "%s. %s - %s Sales");

                // === Trade Book GUI Translations ===
                add("gui.marketblocks.trade_book.title", "Trade Book");
                add("gui.marketblocks.trade_book.toc.header", "=== TRADE BOOK ===\n\n");
                add("gui.marketblocks.trade_book.toc.subheader", "Market Economic Report.\n\n");
                add("gui.marketblocks.trade_book.toc.my_shops", "My Shops");
                add("gui.marketblocks.trade_book.toc.my_shops.tooltip", "Your personal shop overview");
                add("gui.marketblocks.trade_book.toc.trends", "NPC Trends");
                add("gui.marketblocks.trade_book.toc.trends.tooltip", "NPC Supply & Demand");
                add("gui.marketblocks.trade_book.toc.shop_stats", "Shop Leaderboard");
                add("gui.marketblocks.trade_book.toc.shop_stats.tooltip", "Top Shops Leaderboard");
                add("gui.marketblocks.trade_book.toc.market_stats", "Marketplace Top");
                add("gui.marketblocks.trade_book.toc.market_stats.tooltip", "Marketplace Statistics");
                add("gui.marketblocks.trade_book.toc.active_shops", "Player Shops");
                add("gui.marketblocks.trade_book.toc.active_shops.tooltip", "Active player shops on the server");
                add("gui.marketblocks.trade_book.my_shops.title", "=== My Shops ===\n\n");
                add("gui.marketblocks.trade_book.my_shops.empty", "You don't have any shops yet.\nPlace a Trade Stand or Market Crate to get started!");
                add("gui.marketblocks.trade_book.my_shops.summary", "Shops: %s (Open: %s | Closed: %s)\nTotal Sales: %s\n\n");
                add("gui.marketblocks.trade_book.my_shops.sales_count", "  Sales: %s\n");
                add("gui.marketblocks.trade_book.my_shops.sells", "  Sells: %s\n");
                add("gui.marketblocks.trade_book.trends.title", "=== NPC Trends ===\n\n");
                add("gui.marketblocks.trade_book.trends.hover", "Base Value: %s Emeralds\nNPC Purchase: %s Emeralds");
                add("gui.marketblocks.trade_book.trends.stable", "\nNo active market trends.");
                add("gui.marketblocks.trade_book.shops.title", "=== Top Sellers ===\n\n");
                add("gui.marketblocks.trade_book.shops.empty", "No active shops on the server.");
                add("gui.marketblocks.trade_book.shops.entry", "%s. %s:\n");
                add("gui.marketblocks.trade_book.shops.sales", "   Sales: %s\n");
                add("gui.marketblocks.trade_book.shops.owner_sales", "  %s | Sales: %s\n");
                add("gui.marketblocks.trade_book.shops.sales_only", "  Sales: %s\n");
                add("gui.marketblocks.trade_book.shops.player_stats", "Shops: %s | Sales: %s");
                add("gui.marketblocks.trade_book.marketplace.title", "=== Marketplace Top ===\n\n");
                add("gui.marketblocks.trade_book.marketplace.empty", "No sales on the marketplace.");
                add("gui.marketblocks.trade_book.marketplace.entry", "%s. %s:\n");
                add("gui.marketblocks.trade_book.marketplace.sales", "   Sales: %s");
                add("gui.marketblocks.trade_book.marketplace.sale_active", "   \u2605 SALE: %s");
                add("gui.marketblocks.trade_book.active.title", "=== Player Shops ===\n\n");
                add("gui.marketblocks.trade_book.active.empty", "No active shops on the server.");
                add("gui.marketblocks.trade_book.active.owner", "  Owner: %s\n");
                add("gui.marketblocks.trade_book.active.sells", "  Sells: %s\n");
                add("gui.marketblocks.trade_book.active.hover_tp", "Click to teleport");
                add("gui.marketblocks.trade_book.active.unknown_owner", "Unknown");
                add("gui.marketblocks.shop.default_name", "Shop #%s");
                add("gui.marketblocks.shop.named_format", "%s (#%s)");
                add("gui.marketblocks.trade_book.active.closed", "Closed");
                add("gui.marketblocks.trade_book.active.open", "Open");
                add("gui.marketblocks.trade_book.active.no_offer", "No offer set");

                add("gui.marketblocks.trade_book.status.out_of_stock", " \u00a7c\u26a0 Out of Stock");
                add("gui.marketblocks.trade_book.status.output_full", " \u00a76\u26a0 Output Full");

                // === Block Registrations ===
                addBlock(RegistriesInit.MARKETCRATE_BLOCK, "Market Crate");
                addBlock(RegistriesInit.TRADE_STAND_BLOCK, "Trade Stand");

                // === Trade Book Guide ===
                add("gui.marketblocks.trade_book.toc.guide.intro", "Introduction");
                add("gui.marketblocks.trade_book.toc.guide.intro.tooltip", "Read about the basics of MarketBlocks");
                add("gui.marketblocks.trade_book.toc.guide.visuals", "Visual Customization");
                add("gui.marketblocks.trade_book.toc.guide.visuals.tooltip", "Learn how to customize your shops visually");
                add("gui.marketblocks.trade_book.toc.guide.setup", "Setup & Mechanics");
                add("gui.marketblocks.trade_book.toc.guide.setup.tooltip", "Learn about UI, Redstone and Hoppers");
                add("gui.marketblocks.trade_book.toc.guide.advanced", "Advanced Features");
                add("gui.marketblocks.trade_book.toc.guide.advanced.tooltip", "Learn about Co-owners, Economy and Admin features");

                add("gui.marketblocks.trade_book.guide.intro.title", "=== Introduction ===\n\n");
                add("gui.marketblocks.trade_book.guide.intro.text", "Welcome to MarketBlocks!\n\nThis mod allows you to build a thriving economy. You can create various shops to trade items with other players or NPC buyers. Let's look at the available shop blocks.");

                add("gui.marketblocks.trade_book.guide.tradestand.title", "=== Trade Stand ===\n\n");
                add("gui.marketblocks.trade_book.guide.tradestand.text", "An open shop that displays the sold item floating above it. NPCs love these stands!");

                add("gui.marketblocks.trade_book.guide.marketcrate.title", "=== Market Crate ===\n\n");
                add("gui.marketblocks.trade_book.guide.marketcrate.text", "A compact shop variant without a floating item display. Ideal for tight spaces.");

                add("gui.marketblocks.trade_book.guide.visuals.title", "=== Visuals ===\n\n");
                add("gui.marketblocks.trade_book.guide.visuals.text", "Shops can be customized visually!\n\nUse a wrench to rotate the block. If you enable the 'NPC Showcase' in the settings, a friendly NPC will stand behind the shop block!");

                add("gui.marketblocks.trade_book.guide.setup.title", "=== Setup & UI ===\n\n");
                add("gui.marketblocks.trade_book.guide.setup.text", "Right-click your shop to open the settings.\n\nYou can set the price, the item you sell, and stock the inventory. You can also configure limits for buyers.");

                add("gui.marketblocks.trade_book.guide.mechanics.title", "=== Mechanics ===\n\n");
                add("gui.marketblocks.trade_book.guide.mechanics.redstone", "Shops emit a Redstone signal based on how full they are or if they are out of stock. You can configure this in the Redstone tab.");
                add("gui.marketblocks.trade_book.guide.mechanics.hopper", "You can use Hoppers to automate stocking and extracting profits! Configure the Input/Output settings in the Auto-IO tab.");

                add("gui.marketblocks.trade_book.guide.advanced.title", "=== Advanced Features ===\n\n");
                add("gui.marketblocks.trade_book.guide.advanced.text", "You can add co-owners to manage your shop.\n\nNPC buyers have their own economy with trends! If everyone sells wood, the price drops. Keep an eye on the Trends page.");

                // NPC Economy Guide Page (G4)
                add("gui.marketblocks.trade_book.guide.economy.title", "=== NPC Economy ===\n\n");
                add("gui.marketblocks.trade_book.guide.economy.text", "NPC Buyers visit your shops and buy items if the deal is good!\n\n\u00a76Pricing:\u00a7r Items have base values. Crafted items are worth more (+10% per crafting step).\n\n\u00a76Supply & Demand:\u00a7r If an item is sold often, its NPC value drops. Over time, prices recover.\n\n\u00a76NPC Ranks:\u00a7r\n\u2022 \u00a77Citizen\u00a7r \u2013 Small budget, buys cheap items\n\u2022 \u00a7eWealthy\u00a7r \u2013 Medium budget\n\u2022 \u00a76Noble\u00a7r \u2013 Large budget, buys rare items\n\n\u00a76Interests:\u00a7r Each NPC has a specialization (Farmer, Blacksmith, Alchemist, Collector). They prefer items from their category and spend more on them!");

        }
}
