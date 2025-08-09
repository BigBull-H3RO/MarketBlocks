package de.bigbull.marketblocks.data.lang;

import de.bigbull.marketblocks.MarketBlocks;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModDeLangProvider extends LanguageProvider {
    public ModDeLangProvider(PackOutput output) {
        super(output, MarketBlocks.MODID, "de_de");
    }

    @Override
    protected void addTranslations() {
        add("container.small_shop", "Kleiner Laden");
        add("screen.marketblocks.small_shop.save", "Speichern");
        add("screen.marketblocks.small_shop.remove", "Entfernen");
        add("screen.marketblocks.small_shop.tab.offer", "Angebot");
        add("screen.marketblocks.small_shop.tab.storage", "Lager");
        add("screen.marketblocks.small_shop.buy", "Kaufen");
        add("message.marketblocks.small_shop.no_stock", "Kein Lagerbestand");
        add("message.marketblocks.small_shop.payment_mismatch", "Falsche Bezahlung");
        add("message.marketblocks.small_shop.buy_failed", "Kauf fehlgeschlagen");
        add("message.marketblocks.small_shop.offer_exists", "Angebot existiert bereits");
        add("message.marketblocks.small_shop.offer_incomplete", "Angebot unvollständig");
    }
}