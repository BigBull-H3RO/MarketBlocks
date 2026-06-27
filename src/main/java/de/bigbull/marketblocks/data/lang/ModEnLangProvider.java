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

                // === Containers & Menus ===
                add("container.marketblocks.trade_stand", "Trade Stand");
                add("menu.marketblocks.marketplace", "Marketplace");

                // === Keybinds ===
                add("key.categories.marketblocks", "MarketBlocks");
                add("key.marketblocks.open_marketplace", "Open Marketplace");

                // === Commands ===
                add("command.marketblocks.break.denied",
                                "§cYou cannot break this block while it is linked to a Marketplace!");
                add("command.marketblocks.break.unlinked", "§eThe block was unlinked from the Marketplace.");
                add("command.marketblocks.link.already_linked", "§cThis block is already linked to a Marketplace.");
                add("command.marketblocks.link.not_looking_at_block", "§cYou must look at a block to link it.");
                add("command.marketblocks.link.success", "§aSuccessfully linked block to Marketplace!");
                add("command.marketblocks.list.click_to_delete", "Click to delete");
                add("command.marketblocks.list.click_to_teleport", "Click to teleport");
                add("command.marketblocks.list.click_to_waypoint", "Click to get Waypoint links in chat");
                add("command.marketblocks.list.delete", "[Delete]");
                add("command.marketblocks.list.tp", "[Teleport]");
                add("command.marketblocks.list.waypoint", "[Waypoint]");
                add("command.marketblocks.list.page_header", "§8======== §6§lShops (Page %s/%s) §8========");
                add("command.marketblocks.list.prev", "[< Prev]");
                add("command.marketblocks.list.next", "[Next >]");
                add("command.marketblocks.marketplacelist.entry", "§8▪ §6Marketplace §e%s");
                add("command.marketblocks.marketplacelist.page_header", "§8======== §6§lMarketplaces (Page %s/%s) §8========");
                add("command.marketblocks.marketplacelist.no_links", "§cNo marketplaces found.");
                add("command.marketblocks.player_not_found", "§cPlayer not found");
                add("command.marketblocks.reload.success", "§aMarketBlocks configuration reloaded successfully!");
                add("command.marketblocks.resetlimits.no_changes", "§eNo daily limits were reset.");
                add("command.marketblocks.resetlimits.success", "§aDaily limits reset successfully.");
                add("command.marketblocks.search.header", "§8======== §6§lShops selling %s (Page %s/%s) §8========");
                add("command.marketblocks.search.no_shops", "§cNo shops or marketplaces found selling %s.");
                add("command.marketblocks.shoplist.closed", "CLOSED");
                add("command.marketblocks.shoplist.entry", "§8▪ §7[%s§7] §e%s §8(by §7%s§8)");
                add("command.marketblocks.shoplist.header", "§8======== §6§lMarketBlocks Shops §8========");
                add("command.marketblocks.shoplist.no_shops", "§cNo shops available.");
                add("command.marketblocks.shoplist.open", "OPEN");
                add("command.marketblocks.shoplist.hover.shop", "Shop: %s");
                add("command.marketblocks.shoplist.hover.owner", "Owner: %s");
                add("command.marketblocks.shoplist.hover.status", "Status: %s");
                add("command.marketblocks.shoplist.hover.offer", "Offer:");
                add("command.marketblocks.shoplist.hover.arrow", "➔");
                add("command.marketblocks.unlink.not_found", "§cCould not find the Marketplace link.");
                add("command.marketblocks.unlink.not_linked", "§cThis block is not linked.");
                add("command.marketblocks.unlink.not_looking_at_block", "§cYou must look at a block to unlink it.");
                add("command.marketblocks.unlink.success", "§aSuccessfully unlinked block!");
                add("command.marketblocks.unlink.success_name", "§aUnlinked from Marketplace: §e%s");
                add("command.marketblocks.waypoint.created", "§aWaypoint sharing links created:");

                // === GUI - Settings ===
                add("gui.marketblocks.access.edit_access_list", "Access List");
                add("gui.marketblocks.access.edit_owners", "Owners");
                add("gui.marketblocks.access.mode.blacklist", "Mode: Blacklist");
                add("gui.marketblocks.access.mode.everyone", "Mode: Everyone");
                add("gui.marketblocks.access.mode.whitelist", "Mode: Whitelist");
                add("gui.marketblocks.io.allow_io", "Allow I/O");
                add("gui.marketblocks.io.allow_io.tooltip", "Allow hoppers and pipes to interact with this side.");
                add("gui.marketblocks.io.auto_io", "Auto Push/Pull");
                add("gui.marketblocks.io.auto_io.tooltip",
                                "Automatically push and pull items to/from adjacent inventories.");
                add("gui.marketblocks.io.redstone_control.ignored", "Ignored");
                add("gui.marketblocks.io.redstone_control.require_no_signal", "Low (No Signal)");
                add("gui.marketblocks.io.redstone_control.require_signal", "High (Requires Signal)");
                add("gui.marketblocks.io.redstone_control.tooltip", "Redstone control mode for this side.");
                add("gui.marketblocks.settings_owner_only", "Only the owner can change settings");
                add("gui.marketblocks.settings_tab", "Show Settings");
                add("gui.marketblocks.settings_title", "Shop Settings");
                add("gui.marketblocks.toggle.off", "OFF");
                add("gui.marketblocks.toggle.on", "ON");
                add("gui.marketblocks.settings.category.access", "Access");
                add("gui.marketblocks.settings.category.general", "General");
                add("gui.marketblocks.settings.category.io", "I/O");
                add("gui.marketblocks.settings.category.notifications", "Notifications");
                add("gui.marketblocks.settings.category.villager", "NPC");
                add("gui.marketblocks.settings.category.visual", "Visual");
                add("gui.marketblocks.visuals.bobbing", "Bobbing");
                add("gui.marketblocks.visuals.chaos_rotation", "Chaos Rotation");
                add("gui.marketblocks.visuals.count", "Item Count");
                add("gui.marketblocks.visuals.dynamic_fill_level", "Dynamic Fill");
                add("gui.marketblocks.visuals.error.no_surface", "No stand surface behind the shop!");
                add("gui.marketblocks.visuals.error.space_blocked", "Space blocked!");
                add("gui.marketblocks.visuals.height", "Height");
                add("gui.marketblocks.visuals.layout_mode", "Layout Mode");
                add("gui.marketblocks.visuals.layout_mode.gestapelt", "Stacked");
                add("gui.marketblocks.visuals.layout_mode.lose", "Loose");
                add("gui.marketblocks.visuals.npc_enabled", "Decorative Villager");
                add("gui.marketblocks.visuals.npc_name", "NPC Name");
                add("gui.marketblocks.visuals.offer_item_disabled_global", "Disabled by Server Admin.");
                add("gui.marketblocks.visuals.offer_item_fullbright", "Glow");
                add("gui.marketblocks.visuals.offer_item_fullbright.tooltip",
                                "Makes the item glow in the dark (no shadows).");
                add("gui.marketblocks.visuals.offer_item_visible", "Offer Item Visible");
                add("gui.marketblocks.visuals.offer_item_visible.tooltip",
                                "Show or hide the floating/displayed offer item.");
                add("gui.marketblocks.visuals.payment_sounds", "Payment Sounds");
                add("gui.marketblocks.visuals.player_skin_name", "Player Name");
                add("gui.marketblocks.visuals.profession", "Profession");
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
                add("gui.marketblocks.visuals.purchase_particles", "Purchase Particles");
                add("gui.marketblocks.visuals.purchase_sounds", "Purchase Sounds");
                add("gui.marketblocks.visuals.rotation", "Rotation");
                add("gui.marketblocks.visuals.rotation_x", "Rotation X");
                add("gui.marketblocks.visuals.rotation_y", "Rotation Y");
                add("gui.marketblocks.visuals.rotation_z", "Rotation Z");
                add("gui.marketblocks.visuals.scale", "Scale");
                add("gui.marketblocks.visuals.spacing_xz", "Spacing X/Z");
                add("gui.marketblocks.visuals.spacing_y", "Spacing Y");
                add("gui.marketblocks.visuals.speed", "Speed");
                add("gui.marketblocks.visuals.use_player_skin", "Player Skin");
                add("gui.marketblocks.visuals.use_player_skin.tooltip", "Render a player instead of a villager.");

                // === GUI - Notifications ===
                add("gui.marketblocks.notifications.co_owners", "Notify Co-Owners");
                add("gui.marketblocks.notifications.co_owners.tooltip",
                                "Also send notifications to additional shop owners.");
                add("gui.marketblocks.notifications.out_of_stock", "Out of Stock Warning");
                add("gui.marketblocks.notifications.out_of_stock.tooltip",
                                "Get warned when the shop runs out of items.");
                add("gui.marketblocks.notifications.output_full", "Output Full Warning");
                add("gui.marketblocks.notifications.output_full.tooltip",
                                "Get warned when the shop's output inventory is full.");
                add("gui.marketblocks.notifications.purchase", "Purchase Notifications");
                add("gui.marketblocks.notifications.purchase.tooltip",
                                "Get notified in chat when someone buys from your shop.");
                add("message.marketblocks.notifications.out_of_stock", "§cYour shop is out of stock!§r");
                add("message.marketblocks.notifications.output_full", "§cYour shop's output inventory is full!§r");
                add("message.marketblocks.notifications.purchase", "§a%s bought %sx %s from your shop.§r");

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
                add("gui.marketblocks.category", "Category");
                add("gui.marketblocks.category.none", "None");
                add("gui.marketblocks.category.weapons_armor", "Weapons & Armor");
                add("gui.marketblocks.category.tools", "Tools");
                add("gui.marketblocks.category.blocks", "Blocks");
                add("gui.marketblocks.category.food_potions", "Food & Potions");
                add("gui.marketblocks.category.valuables", "Valuables");
                add("gui.marketblocks.category.misc", "Miscellaneous");
                add("gui.marketblocks.category.tooltip", "Category to list the shop under in the global directory");
                add("gui.marketblocks.create_offer", "Create Offer");
                add("gui.marketblocks.delete_offer", "Delete Offer");
                add("gui.marketblocks.disabled", "Disabled");
                add("gui.marketblocks.emit_redstone", "Emit Redstone");
                add("gui.marketblocks.emit_redstone.tooltip", "Emit a short redstone pulse after a purchase");
                add("gui.marketblocks.error.invalid_offer", "Invalid offer configuration");
                add("gui.marketblocks.error.no_payment_items", "Please place at least one payment item");
                add("gui.marketblocks.error.no_result_item", "Please place an item in the result slot");
                add("gui.marketblocks.input", "Input");
                add("gui.marketblocks.inventory_admin_disabled", "Inventory disabled in admin mode");
                add("gui.marketblocks.inventory_owner_only", "Only the owner can manage inventory");
                add("gui.marketblocks.inventory_tab", "Show Inventory");
                add("gui.marketblocks.inventory_title", "Trade Stand Inventory");
                add("gui.marketblocks.log_tab", "Show Log");
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
                add("gui.marketblocks.offers_tab", "Show Offers");
                add("gui.marketblocks.out_of_stock", "Out of Stock");
                add("gui.marketblocks.output", "Output");
                add("gui.marketblocks.output_full", "Output Full");
                add("gui.marketblocks.owner", "Owner: %s");
                add("gui.marketblocks.purchase_xp_sound", "Purchase XP Sound");
                add("gui.marketblocks.purchase_xp_sound.tooltip",
                                "Plays an XP orb sound when a player purchases something");
                add("gui.marketblocks.save", "Save");
                add("gui.marketblocks.shop_closed", "Shop Paused");
                add("gui.marketblocks.shop_closed.tooltip", "If active, only owners can buy items.");
                add("gui.marketblocks.shop_name", "Shop Name");
                add("gui.marketblocks.shop_title", "Trade Stand");
                add("gui.marketblocks.side.back", "Back");
                add("gui.marketblocks.side.bottom", "Bottom");
                add("gui.marketblocks.side.left", "Left");
                add("gui.marketblocks.side.right", "Right");

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
                
                add("message.marketblocks.shop_buyer.interact.1", "§eI'm looking for some good items to buy!§r");
                add("message.marketblocks.shop_buyer.interact.2", "§eDo you have anything interesting for sale?§r");
                add("message.marketblocks.shop_buyer.interact.3", "§eI travel around to buy things. Maybe you have what I need!§r");
                add("message.marketblocks.shop_buyer.interact.4", "§eI just found a great deal! I love shopping here!§r");
                add("message.marketblocks.shop_buyer.interact.5", "§eAnother good purchase! My bag is getting heavy.§r");
                add("message.marketblocks.shop_buyer.interact.6", "§eI got what I needed, thanks to these shops!§r");
                add("message.marketblocks.shop_buyer.interact.7", "§eHmm, I'm looking for something specific...§r");
                add("message.marketblocks.shop_buyer.interact.8", "§eI wonder what other shops are around here...§r");
                add("message.marketblocks.shop_buyer.interact.9", "§eJust browsing for now. Nothing has caught my eye yet.§r");
                add("message.marketblocks.shop_buyer.interact.10", "§eNice shop you have here! I'll keep it in mind.§r");

                // === Jade / Waila Support ===
                add("config.jade.plugin_marketblocks.shop_info", "Shop Info");
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
                add("gui.marketblocks.notifications.login.out_of_stock", "§c[MarketBlocks] %s of your shops are out of stock!§r");
                add("gui.marketblocks.notifications.login.output_full", "§c[MarketBlocks] %s of your shops have full output storage!§r");
                add("gui.marketblocks.notifications.login.coordinate", "§7 - Location: X: %s, Y: %s, Z: %s§r");

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
                add("command.marketblocks.stats.shop.empty", "No shops available.");
                add("command.marketblocks.stats.shop.unnamed", "Unnamed");
                add("command.marketblocks.stats.shop.entry", "%s. %s - %s Sales");
                add("command.marketblocks.stats.marketplace.header", "--- Top 10 Marketplace Offers ---");
                add("command.marketblocks.stats.marketplace.empty", "No offers available.");
                add("command.marketblocks.stats.marketplace.entry", "%s. %s - %s Sales");

                // === Block Registrations ===
                addBlock(RegistriesInit.MARKETCRATE_BLOCK, "Market Crate");
                addBlock(RegistriesInit.TRADE_STAND_BLOCK, "Trade Stand");

        }
}
