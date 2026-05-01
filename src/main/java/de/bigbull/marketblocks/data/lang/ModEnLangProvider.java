package de.bigbull.marketblocks.data.lang;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.init.RegistriesInit;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModEnLangProvider extends LanguageProvider {
    public ModEnLangProvider(PackOutput output) {
        super(output, MarketBlocks.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        // Creative Tab
        add("itemGroup.marketblocks", "MarketBlocks");

        // Container Titles
        add("container.marketblocks.trade_stand", "Trade Stand");
        add("container.marketblocks.trade_stand_offers", "Trade Stand - Offers");
        add("container.marketblocks.trade_stand_inventory", "Trade Stand - Inventory");

        // Menus
        add("menu.marketblocks.marketplace", "Marketplace");

        // Keybindings
        add("key.categories.marketblocks", "MarketBlocks");
        add("key.marketblocks.open_marketplace", "Open Marketplace");

        // GUI Titles
        add("gui.marketblocks.shop_title", "Trade Stand");
        add("gui.marketblocks.inventory_title", "Trade Stand Inventory");

        // Status Messages
        add("gui.marketblocks.trade_available", "Trade Available");
        add("gui.marketblocks.trade_unavailable", "Trade Unavailable");
        add("gui.marketblocks.available", "Available");
        add("gui.marketblocks.no_offers", "No Offers Available");
        add("gui.marketblocks.no_players_available", "No players available");
        add("gui.marketblocks.out_of_stock", "Out of Stock");
        add("gui.marketblocks.output_full", "Output Full");
        add("gui.marketblocks.owner", "Owner: %s");

        // Offer Creation
        add("gui.marketblocks.create_hint", "Place items in slots below, then click create");
        add("gui.marketblocks.confirm_offer", "Confirm Offer");
        add("gui.marketblocks.cancel_offer", "Cancel");
        add("gui.marketblocks.create_offer", "Create Offer");
        add("gui.marketblocks.delete_offer", "Delete Offer");

        // Navigation
        add("gui.marketblocks.offers", "Offers");
        add("gui.marketblocks.offers_tab", "Show Offers");
        add("gui.marketblocks.inventory_tab", "Show Inventory");
        add("gui.marketblocks.settings_tab", "Show Settings");
        add("gui.marketblocks.log_tab", "Show Log");

        // Inventory Labels
        add("gui.marketblocks.input_inventory", "Input");
        add("gui.marketblocks.output_inventory", "Output");

        // Inventory Info
        add("gui.marketblocks.inventory_owner_only", "Only the owner can manage inventory");
        add("gui.marketblocks.inventory_flow_hint", "Items flow from Input to Output inventory");

        // Settings
        add("gui.marketblocks.settings_title", "Shop Settings");
        add("gui.marketblocks.settings.category.general", "General");
        add("gui.marketblocks.settings.category.io", "I/O");
        add("gui.marketblocks.settings.category.visuals", "Visuals");
        add("gui.marketblocks.settings.category.access", "Access");
        add("gui.marketblocks.log_title", "Transaction Log");
        add("gui.marketblocks.log.empty", "No transactions yet");
        add("gui.marketblocks.log.clear", "Clear log");
        add("gui.marketblocks.log.count", "Entries: %s");
        add("gui.marketblocks.log.buyer", "Buyer: %s");
        add("gui.marketblocks.log.none", "None");
        add("gui.marketblocks.log.time.just_now", "Just now");
        add("gui.marketblocks.log.time.seconds", "%s sec ago");
        add("gui.marketblocks.log.time.minutes", "%s min ago");
        add("gui.marketblocks.log.time.hours", "%s h ago");
        add("gui.marketblocks.log.time.days", "%s d ago");
        add("gui.marketblocks.save", "Save");
        add("gui.marketblocks.shop_name", "Shop Name");
        add("gui.marketblocks.emit_redstone", "Emit Redstone");
        add("gui.marketblocks.emit_redstone.tooltip", "Emit a short redstone pulse after a purchase");
        add("gui.marketblocks.purchase_xp_sound", "Purchase XP Sound");
        add("gui.marketblocks.purchase_xp_sound.tooltip", "Plays an XP orb sound when a player purchases something");
        add("gui.marketblocks.admin_shop.enabled", "Admin-Shop: ON");
        add("gui.marketblocks.admin_shop.disabled", "Admin-Shop: OFF");
        add("gui.marketblocks.settings_owner_only", "Only the owner can change settings");
        add("gui.marketblocks.inventory_admin_disabled", "Inventory disabled in admin mode");
        add("gui.marketblocks.visuals.npc_enabled", "Decorative Villager");
        add("gui.marketblocks.visuals.npc_name", "NPC Name");
        add("gui.marketblocks.visuals.profession", "Profession");
        add("gui.marketblocks.visuals.purchase_particles", "Purchase Particles");
        add("gui.marketblocks.visuals.purchase_sounds", "Purchase Sounds");
        add("gui.marketblocks.visuals.payment_sounds", "Payment Sounds");
        add("gui.marketblocks.visuals.error.no_surface", "No stand surface behind the shop!");
        add("gui.marketblocks.visuals.error.space_blocked", "Space blocked!");
        add("gui.marketblocks.visuals.profession.none", "Unemployed");
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
        add("gui.marketblocks.visuals.profession.shepherd", "Shepherd");
        add("gui.marketblocks.visuals.profession.toolsmith", "Toolsmith");
        add("gui.marketblocks.visuals.profession.weaponsmith", "Weaponsmith");

        add("gui.marketblocks.side.left", "Left");
        add("gui.marketblocks.side.right", "Right");
        add("gui.marketblocks.side.bottom", "Bottom");
        add("gui.marketblocks.side.back", "Back");
        add("gui.marketblocks.input", "Input");
        add("gui.marketblocks.output", "Output");
        add("gui.marketblocks.disabled", "Disabled");

        // Error Messages
        add("gui.marketblocks.error.no_result_item", "Please place an item in the result slot");
        add("gui.marketblocks.error.no_payment_items", "Please place at least one payment item");
        add("gui.marketblocks.error.invalid_offer", "Invalid offer configuration");

        // Success Messages
        add("gui.marketblocks.success.offer_created", "Offer successfully created");
        add("gui.marketblocks.success.offer_deleted", "Offer successfully deleted");

        // Additional Status Messages
        add("gui.marketblocks.creating_offer", "Creating Offer...");
        add("gui.marketblocks.offer_ready", "Offer Ready");
        add("gui.marketblocks.insufficient_stock", "Insufficient Stock");

        // Modes
        add("gui.marketblocks.mode.edit_active", "EDIT MODE");

        // Marketplace
        add("gui.marketblocks.marketplace.unnamed_page", "Page %s");
        add("gui.marketblocks.marketplace.add_page", "Add Page");
        add("gui.marketblocks.marketplace.rename_page", "Rename Page");
        add("gui.marketblocks.marketplace.delete_page", "Delete Page");
        add("gui.marketblocks.marketplace.add_offer", "Add Offer");
        add("gui.marketblocks.marketplace.delete_offer", "Delete Offer");
        add("gui.marketblocks.marketplace.clear_selection", "Clear Selection");
        add("gui.marketblocks.marketplace.mode.view", "Switch to view mode");
        add("gui.marketblocks.marketplace.mode.edit", "Switch to edit mode");
        add("gui.marketblocks.marketplace.inline.limits", "Edit limits");
        add("gui.marketblocks.marketplace.inline.pricing", "Edit pricing");
        add("gui.marketblocks.marketplace.editor.limits.title", "Limits");
        add("gui.marketblocks.marketplace.editor.limits.daily", "Daily Limit");
        add("gui.marketblocks.marketplace.editor.limits.stock", "Stock Limit");
        add("gui.marketblocks.marketplace.editor.limits.restock", "Restock (s)");
        add("gui.marketblocks.marketplace.editor.pricing.title", "Pricing");
        add("gui.marketblocks.marketplace.editor.pricing.enabled", "Pricing ON");
        add("gui.marketblocks.marketplace.editor.pricing.disabled", "Pricing OFF");
        add("gui.marketblocks.marketplace.editor.pricing.label", "Enable Pricing");
        add("gui.marketblocks.marketplace.editor.pricing.step", "Demand Step");
        add("gui.marketblocks.marketplace.editor.pricing.min", "Min Multiplier");
        add("gui.marketblocks.marketplace.editor.pricing.max", "Max Multiplier");
        add("gui.marketblocks.marketplace.move_offer", "Move Offer");
        add("gui.marketblocks.marketplace.move_offer_up", "Move offer up");
        add("gui.marketblocks.marketplace.move_offer_down", "Move offer down");
        add("gui.marketblocks.marketplace.no_pages", "No pages available");
        add("gui.marketblocks.marketplace.collapsed", "Collapsed");
        add("gui.marketblocks.marketplace.no_offers", "No offers");
        add("gui.marketblocks.marketplace.select_offer_hint", "Select an offer from the list");
        add("gui.marketblocks.marketplace.badge.available", "AVAILABLE");
        add("gui.marketblocks.marketplace.badge.out_of_stock", "OUT OF STOCK");
        add("gui.marketblocks.marketplace.badge.daily_limit", "DAILY LIMIT");
        add("gui.marketblocks.marketplace.badge.restocking", "RESTOCKING");
        add("gui.marketblocks.marketplace.badge.restock_ready", "RESTOCK READY");
        add("gui.marketblocks.marketplace.price_multiplier", "Price Multiplier: x%s");
        add("gui.marketblocks.marketplace.remaining_stock", "Stock: %s");
        add("gui.marketblocks.marketplace.remaining_daily", "Remaining Today: %s");
        add("gui.marketblocks.marketplace.restock_in", "Next Restock In: %s");
        add("gui.marketblocks.marketplace.restock_ready", "Restock Ready");
        add("gui.marketblocks.marketplace.status.price_short", "x%s");
        add("gui.marketblocks.marketplace.status.daily_short", "D:%s");
        add("gui.marketblocks.marketplace.status.stock_short", "S:%s");
        add("gui.marketblocks.marketplace.status.restock_short", "R:%s");
        add("gui.marketblocks.marketplace.tooltip.price_multiplier", "Price factor.");
        add("gui.marketblocks.marketplace.tooltip.remaining_stock", "Remaining stock purchases.");
        add("gui.marketblocks.marketplace.tooltip.remaining_stock_empty", "Out of stock.");
        add("gui.marketblocks.marketplace.tooltip.remaining_daily", "Your remaining purchases today.");
        add("gui.marketblocks.marketplace.tooltip.remaining_daily_empty", "Daily limit reached.");
        add("gui.marketblocks.marketplace.tooltip.restock_in", "Time until restock.");
        add("gui.marketblocks.marketplace.tooltip.restock_ready", "Restock due now.");
        add("gui.marketblocks.marketplace.tooltip.unavailable_daily", "Unavailable: daily limit reached.");
        add("gui.marketblocks.marketplace.tooltip.unavailable_stock", "Unavailable: out of stock.");
        add("gui.marketblocks.marketplace.tooltip.unavailable_restock", "Unavailable: restocking.");
        add("gui.marketblocks.marketplace.tooltip.unavailable_generic", "Unavailable.");

        add("message.marketblocks.trade_stand.not_owner", "Only the owner can break this trade stand.");
        add("message.marketblocks.trade_stand.no_offer", "This trade stand currently has no active offer.");
        add("message.marketblocks.marketplace.page_name_blank", "The page name must not be empty.");
        add("message.marketblocks.marketplace.page_name_too_long", "The page name must be at most %s characters long.");
        add("message.marketblocks.marketplace.page_name_duplicate", "A page named '%s' already exists.");
        add("message.marketblocks.marketplace.page_not_found", "The selected shop page could not be found.");
        add("message.marketblocks.marketplace.daily_limit_reached", "The daily limit for this offer has been reached.");
        add("message.marketblocks.marketplace.edit_mode_enabled", "Marketplace edit mode enabled.");
        add("message.marketblocks.marketplace.edit_mode_disabled", "Marketplace edit mode disabled.");
        add("message.marketblocks.marketplace.limits.no_connection", "Could not save limits: no server connection.");
        add("message.marketblocks.marketplace.limits.invalid_data", "Could not save limits: invalid data.");
        add("message.marketblocks.marketplace.limits.invalid_positive_int", "Please enter only positive whole numbers for limits.");
        add("message.marketblocks.marketplace.pricing.invalid_finite", "Please enter valid finite numbers for pricing.");
        add("message.marketblocks.marketplace.pricing.no_connection", "Could not save pricing: no server connection.");
        add("message.marketblocks.marketplace.pricing.invalid_data", "Could not save pricing: invalid data.");
        add("message.marketblocks.marketplace.pricing.invalid_number_format", "Please use numbers only (dot or comma allowed).");
        add("message.marketblocks.visual_npc.space_blocked", "The decorative villager cannot spawn: two free blocks above are required.");
        add("subtitles.marketblocks.visual_npc_fall", "Villager lands");

        add("item.marketblocks.trade_stand.with_showcase", "Trade Stand (With Showcase)");

        // Blocks
        addBlock(RegistriesInit.TRADE_STAND_BLOCK, "Trade Stand");
        addBlock(RegistriesInit.MARKETCRATE_BLOCK, "Market Crate");
        addBlock(RegistriesInit.SHOP_BLOCK_TEST, "Shop Block Test");
    }
}
