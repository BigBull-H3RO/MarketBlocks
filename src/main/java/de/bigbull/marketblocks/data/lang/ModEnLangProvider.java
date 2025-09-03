package de.bigbull.marketblocks.data.lang;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.RegistriesInit;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * Generates US English (en_us) translations for the mod.
 */
public class ModEnLangProvider extends LanguageProvider {
    public ModEnLangProvider(PackOutput output) {
        super(output, MarketBlocks.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        // Creative Tab
        add(ModLang.CREATIVE_TAB, "Market Blocks");

        // Blocks
        addBlock(RegistriesInit.SMALL_SHOP_BLOCK, "Small Shop");

        // Container Titles
        add(ModLang.CONTAINER_SMALL_SHOP, "Shop");
        add(ModLang.CONTAINER_SMALL_SHOP_OFFERS, "Shop - Offers");
        add(ModLang.CONTAINER_SMALL_SHOP_INVENTORY, "Shop - Inventory");

        // GUI Titles
        add(ModLang.GUI_SHOP_TITLE, "Shop");
        add(ModLang.GUI_INVENTORY_TITLE, "Shop Inventory");
        add(ModLang.GUI_SETTINGS_TITLE, "Shop Settings");

        // GUI Status Messages
        add(ModLang.GUI_TRADE_AVAILABLE, "Trade Available");
        add(ModLang.GUI_TRADE_UNAVAILABLE, "Trade Unavailable");
        add(ModLang.GUI_AVAILABLE, "Available");
        add(ModLang.GUI_NO_OFFERS, "No Offers Available");
        add(ModLang.GUI_NO_PLAYERS_AVAILABLE, "No players available");
        add(ModLang.GUI_OUT_OF_STOCK, "Out of Stock");
        add(ModLang.GUI_OWNER, "Owner: %s");
        add(ModLang.GUI_CREATING_OFFER, "Creating Offer...");
        add(ModLang.GUI_OFFER_READY, "Offer Ready");
        add(ModLang.GUI_INSUFFICIENT_STOCK, "Insufficient Stock");

        // GUI Offer Creation
        add(ModLang.GUI_CREATE_HINT, "Place items in slots below, then click create");
        add(ModLang.GUI_CONFIRM_OFFER, "Confirm Offer");
        add(ModLang.GUI_CANCEL_OFFER, "Cancel");
        add(ModLang.GUI_CREATE_OFFER, "Create Offer");
        add(ModLang.GUI_DELETE_OFFER, "Delete Offer");

        // GUI Navigation
        add(ModLang.GUI_OFFERS, "Offers");
        add(ModLang.GUI_OFFERS_TAB, "Show Offers");
        add(ModLang.GUI_INVENTORY_TAB, "Show Inventory");
        add(ModLang.GUI_SETTINGS_TAB, "Show Settings");

        // GUI Inventory Labels
        add(ModLang.GUI_INPUT_INVENTORY, "Input");
        add(ModLang.GUI_OUTPUT_INVENTORY, "Output");
        add(ModLang.GUI_INVENTORY_OWNER_ONLY, "Only the owner can manage inventory");
        add(ModLang.GUI_INVENTORY_FLOW_HINT, "Items flow from Input to Output inventory");

        // GUI Settings
        add(ModLang.GUI_SAVE, "Save");
        add(ModLang.GUI_SHOP_NAME, "Shop Name");
        add(ModLang.GUI_EMIT_REDSTONE, "Emit Redstone");
        add(ModLang.GUI_EMIT_REDSTONE_TOOLTIP, "Emit a short redstone pulse after a purchase");
        add(ModLang.GUI_SETTINGS_OWNER_ONLY, "Only the owner can change settings");

        // GUI Side Configuration
        add(ModLang.GUI_SIDE_LEFT, "Left");
        add(ModLang.GUI_SIDE_RIGHT, "Right");
        add(ModLang.GUI_SIDE_BOTTOM, "Bottom");
        add(ModLang.GUI_SIDE_BACK, "Back");
        add(ModLang.GUI_INPUT, "Input");
        add(ModLang.GUI_OUTPUT, "Output");
        add(ModLang.GUI_DISABLED, "Disabled");

        // GUI Error Messages
        add(ModLang.GUI_ERROR_NO_RESULT_ITEM, "Please place an item in the result slot");
        add(ModLang.GUI_ERROR_NO_PAYMENT_ITEMS, "Please place at least one payment item");
        add(ModLang.GUI_ERROR_INVALID_OFFER, "Invalid offer configuration");

        // GUI Success Messages
        add(ModLang.GUI_SUCCESS_OFFER_CREATED, "Offer successfully created");
        add(ModLang.GUI_SUCCESS_OFFER_DELETED, "Offer successfully deleted");
    }
}