package de.bigbull.marketblocks.data.lang;

import de.bigbull.marketblocks.MarketBlocks;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModEnLangProvider extends LanguageProvider {
    public ModEnLangProvider(PackOutput output) {
        super(output, MarketBlocks.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.marketblocks", "Small Shop");
        add("gui.marketblocks.shop_title", "Shop");
        add("gui.marketblocks.inventory_title", "Shop Inventory");
        add("gui.marketblocks.trade_available", "Trade Available");
        add("gui.marketblocks.trade_unavailable", "Trade Unavailable");
        add("gui.marketblocks.available", "Available");
        add("gui.marketblocks.no_offers", "No Offers Available");
        add("gui.marketblocks.create_hint", "Place items in slots below, then click create");
        add("gui.marketblocks.confirm_offer", "Confirm Offer");
        add("gui.marketblocks.cancel_offer", "Cancel");
        add("gui.marketblocks.offers", "Offers");
        add("gui.marketblocks.offers_tab", "Show Offers");
        add("gui.marketblocks.inventory_tab", "Show Inventory");
        add("gui.marketblocks.input_inventory", "Input");
        add("gui.marketblocks.output_inventory", "Output");
        add("gui.marketblocks.create_offer", "Create Offer");
        add("gui.marketblocks.delete_offer", "Delete Offer");
        add("gui.marketblocks.owner", "Owner: %s");
        add("gui.marketblocks.out_of_stock", "Out of Stock");
    }
}