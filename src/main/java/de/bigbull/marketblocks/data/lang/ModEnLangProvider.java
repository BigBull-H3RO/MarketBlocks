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
        add("itemGroup.marketblocks", "Small Shop");

        // Container Titles
        add("container.marketblocks.small_shop", "Shop");
        add("container.marketblocks.small_shop_offers", "Shop - Offers");
        add("container.marketblocks.small_shop_inventory", "Shop - Inventory");

        // GUI Titles
        add("gui.marketblocks.shop_title", "Shop");
        add("gui.marketblocks.inventory_title", "Shop Inventory");

        // Status Messages
        add("gui.marketblocks.trade_available", "Trade Available");
        add("gui.marketblocks.trade_unavailable", "Trade Unavailable");
        add("gui.marketblocks.available", "Available");
        add("gui.marketblocks.no_offers", "No Offers Available");
        add("gui.marketblocks.out_of_stock", "Out of Stock");
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

        // Inventory Labels
        add("gui.marketblocks.input_inventory", "Input");
        add("gui.marketblocks.output_inventory", "Output");

        // Inventory Info
        add("gui.marketblocks.inventory_owner_only", "Only the owner can manage inventory");
        add("gui.marketblocks.inventory_flow_hint", "Items flow from Input to Output inventory");

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

        // Blocks
        addBlock(RegistriesInit.SMALL_SHOP_BLOCK, "Small Shop");
    }
}