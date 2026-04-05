package de.bigbull.marketblocks.data.lang;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.RegistriesInit;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModEnLangProvider extends LanguageProvider {
    public ModEnLangProvider(PackOutput output) {
        super(output, MarketBlocks.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        // Creative Tab
        add("itemGroup.marketblocks", "Market Blocks");

        // Container Titles
        add("container.marketblocks.small_shop", "Shop");
        add("container.marketblocks.small_shop_offers", "Shop - Offers");
        add("container.marketblocks.small_shop_inventory", "Shop - Inventory");

        // Menus
        add("menu.marketblocks.server_shop", "Server Shop");

        // Keybindings
        add("key.categories.marketblocks", "Market Blocks");
        add("key.marketblocks.open_server_shop", "Open Server Shop");

        // GUI Titles
        add("gui.marketblocks.shop_title", "Shop");
        add("gui.marketblocks.inventory_title", "Shop Inventory");

        // Status Messages
        add("gui.marketblocks.trade_available", "Trade Available");
        add("gui.marketblocks.trade_unavailable", "Trade Unavailable");
        add("gui.marketblocks.available", "Available");
        add("gui.marketblocks.no_offers", "No Offers Available");
        add("gui.marketblocks.no_players_available", "No players available");
        add("gui.marketblocks.out_of_stock", "Out of Stock");
        add("gui.marketblocks.output_almost_full", "Output Nearly Full");
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

        // Inventory Labels
        add("gui.marketblocks.input_inventory", "Input");
        add("gui.marketblocks.output_inventory", "Output");

        // Inventory Info
        add("gui.marketblocks.inventory_owner_only", "Only the owner can manage inventory");
        add("gui.marketblocks.inventory_flow_hint", "Items flow from Input to Output inventory");

        // Settings
        add("gui.marketblocks.settings_title", "Shop Settings");
        add("gui.marketblocks.save", "Save");
        add("gui.marketblocks.shop_name", "Shop Name");
        add("gui.marketblocks.emit_redstone", "Emit Redstone");
        add("gui.marketblocks.emit_redstone.tooltip", "Emit a short redstone pulse after a purchase");
        add("gui.marketblocks.settings_owner_only", "Only the owner can change settings");

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

        // Server Shop
        add("gui.marketblocks.server_shop.unnamed_page", "Page %s");
        add("gui.marketblocks.server_shop.add_page", "Add Page");
        add("gui.marketblocks.server_shop.rename_page", "Rename Page");
        add("gui.marketblocks.server_shop.delete_page", "Delete Page");
        add("gui.marketblocks.server_shop.add_offer", "Add Offer");
        add("gui.marketblocks.server_shop.delete_offer", "Delete Offer");
        add("gui.marketblocks.server_shop.clear_selection", "Clear Selection");
        add("gui.marketblocks.server_shop.mode.view", "Switch to view mode");
        add("gui.marketblocks.server_shop.mode.edit", "Switch to edit mode");
        add("gui.marketblocks.server_shop.inline.limits", "Edit limits");
        add("gui.marketblocks.server_shop.inline.pricing", "Edit pricing");
        add("gui.marketblocks.server_shop.editor.limits.title", "Limits");
        add("gui.marketblocks.server_shop.editor.limits.daily", "Daily Limit");
        add("gui.marketblocks.server_shop.editor.limits.stock", "Stock Limit");
        add("gui.marketblocks.server_shop.editor.limits.restock", "Restock (s)");
        add("gui.marketblocks.server_shop.editor.pricing.title", "Pricing");
        add("gui.marketblocks.server_shop.editor.pricing.enabled", "Pricing ON");
        add("gui.marketblocks.server_shop.editor.pricing.disabled", "Pricing OFF");
        add("gui.marketblocks.server_shop.editor.pricing.label", "Enable Pricing");
        add("gui.marketblocks.server_shop.editor.pricing.step", "Demand Step");
        add("gui.marketblocks.server_shop.editor.pricing.min", "Min Multiplier");
        add("gui.marketblocks.server_shop.editor.pricing.max", "Max Multiplier");
        add("gui.marketblocks.server_shop.move_offer", "Move Offer");
        add("gui.marketblocks.server_shop.move_offer_up", "Move offer up");
        add("gui.marketblocks.server_shop.move_offer_down", "Move offer down");
        add("gui.marketblocks.server_shop.no_pages", "No pages available");
        add("gui.marketblocks.server_shop.collapsed", "Collapsed");
        add("gui.marketblocks.server_shop.no_offers", "No offers");
        add("gui.marketblocks.server_shop.select_offer_hint", "Select an offer from the list");
        add("gui.marketblocks.server_shop.badge.available", "AVAILABLE");
        add("gui.marketblocks.server_shop.badge.out_of_stock", "OUT OF STOCK");
        add("gui.marketblocks.server_shop.badge.daily_limit", "DAILY LIMIT");
        add("gui.marketblocks.server_shop.badge.restocking", "RESTOCKING");
        add("gui.marketblocks.server_shop.badge.restock_ready", "RESTOCK READY");
        add("gui.marketblocks.server_shop.price_multiplier", "Price Multiplier: x%s");
        add("gui.marketblocks.server_shop.remaining_stock", "Stock: %s");
        add("gui.marketblocks.server_shop.remaining_daily", "Remaining Today: %s");
        add("gui.marketblocks.server_shop.restock_in", "Next Restock In: %s");
        add("gui.marketblocks.server_shop.restock_ready", "Restock Ready");
        add("gui.marketblocks.server_shop.status.price_short", "x%s");
        add("gui.marketblocks.server_shop.status.daily_short", "D:%s");
        add("gui.marketblocks.server_shop.status.stock_short", "S:%s");
        add("gui.marketblocks.server_shop.status.restock_short", "R:%s");
        add("gui.marketblocks.server_shop.tooltip.price_multiplier", "Price factor.");
        add("gui.marketblocks.server_shop.tooltip.remaining_stock", "Remaining stock purchases.");
        add("gui.marketblocks.server_shop.tooltip.remaining_stock_empty", "Out of stock.");
        add("gui.marketblocks.server_shop.tooltip.remaining_daily", "Your remaining purchases today.");
        add("gui.marketblocks.server_shop.tooltip.remaining_daily_empty", "Daily limit reached.");
        add("gui.marketblocks.server_shop.tooltip.restock_in", "Time until restock.");
        add("gui.marketblocks.server_shop.tooltip.restock_ready", "Restock due now.");
        add("gui.marketblocks.server_shop.tooltip.unavailable_daily", "Unavailable: daily limit reached.");
        add("gui.marketblocks.server_shop.tooltip.unavailable_stock", "Unavailable: out of stock.");
        add("gui.marketblocks.server_shop.tooltip.unavailable_restock", "Unavailable: restocking.");
        add("gui.marketblocks.server_shop.tooltip.unavailable_generic", "Unavailable.");

        add("message.marketblocks.small_shop.not_owner", "Only the owner can break this shop.");
        add("message.marketblocks.small_shop.no_offer", "This shop currently has no active offer.");
        add("message.marketblocks.server_shop.page_name_blank", "The page name must not be empty.");
        add("message.marketblocks.server_shop.page_name_too_long", "The page name must be at most %s characters long.");
        add("message.marketblocks.server_shop.page_name_duplicate", "A page named '%s' already exists.");
        add("message.marketblocks.server_shop.page_not_found", "The selected shop page could not be found.");
        add("message.marketblocks.server_shop.daily_limit_reached", "The daily limit for this offer has been reached.");
        add("message.marketblocks.server_shop.edit_mode_enabled", "ServerShop edit mode enabled.");
        add("message.marketblocks.server_shop.edit_mode_disabled", "ServerShop edit mode disabled.");
        add("message.marketblocks.server_shop.limits.no_connection", "Could not save limits: no server connection.");
        add("message.marketblocks.server_shop.limits.invalid_data", "Could not save limits: invalid data.");
        add("message.marketblocks.server_shop.limits.invalid_positive_int", "Please enter only positive whole numbers for limits.");
        add("message.marketblocks.server_shop.pricing.invalid_finite", "Please enter valid finite numbers for pricing.");
        add("message.marketblocks.server_shop.pricing.no_connection", "Could not save pricing: no server connection.");
        add("message.marketblocks.server_shop.pricing.invalid_data", "Could not save pricing: invalid data.");
        add("message.marketblocks.server_shop.pricing.invalid_number_format", "Please use numbers only (dot or comma allowed).");

        // Blocks
        addBlock(RegistriesInit.SMALL_SHOP_BLOCK, "Small Shop");
    }
}
