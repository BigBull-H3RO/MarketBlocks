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
        add("container.small_shop", "Small Shop");
        add("screen.marketblocks.small_shop.save", "Save");
        add("screen.marketblocks.small_shop.remove", "Remove");
        add("screen.marketblocks.small_shop.tab.offer", "Offer");
        add("screen.marketblocks.small_shop.tab.storage", "Storage");
        add("screen.marketblocks.small_shop.buy", "Buy");
        add("message.marketblocks.small_shop.no_stock", "No stock available");
        add("message.marketblocks.small_shop.payment_mismatch", "Payment mismatch");
        add("message.marketblocks.small_shop.buy_failed", "Purchase failed");
    }
}