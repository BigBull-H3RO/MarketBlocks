package de.bigbull.marketblocks.data.lang;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.RegistriesInit;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModDeLangProvider extends LanguageProvider {
    public ModDeLangProvider(PackOutput output) {
        super(output, MarketBlocks.MODID, "de_de");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.marketblocks", "Market Blocks");

        // Container-Titel
        add("container.marketblocks.small_shop", "Laden");
        add("container.marketblocks.small_shop_offers", "Laden - Angebote");
        add("container.marketblocks.small_shop_inventory", "Laden - Inventar");

        add("gui.marketblocks.shop_title", "Laden");
        add("gui.marketblocks.inventory_title", "Laden Inventar");
        add("gui.marketblocks.trade_available", "Handel verfügbar");
        add("gui.marketblocks.trade_unavailable", "Handel nicht verfügbar");
        add("gui.marketblocks.available", "Verfügbar");
        add("gui.marketblocks.no_offers", "Keine Angebote verfügbar");
        add("gui.marketblocks.no_players_available", "Es sind aktuell keine Spieler verfügbar");
        add("gui.marketblocks.create_hint", "Platziere Items in den unteren Slots");
        add("gui.marketblocks.confirm_offer", "Angebot bestätigen");
        add("gui.marketblocks.cancel_offer", "Abbrechen");
        add("gui.marketblocks.create_offer", "Angebot erstellen");
        add("gui.marketblocks.delete_offer", "Angebot löschen");
        add("gui.marketblocks.offers", "Angebote");
        add("gui.marketblocks.offers_tab", "Angebote anzeigen");
        add("gui.marketblocks.inventory_tab", "Inventar anzeigen");
        add("gui.marketblocks.settings_tab", "Einstellungen anzeigen");
        add("gui.marketblocks.input_inventory", "Eingabe");
        add("gui.marketblocks.output_inventory", "Ausgabe");
        add("gui.marketblocks.inventory_owner_only", "Nur der Besitzer kann das Inventar verwalten");
        add("gui.marketblocks.inventory_flow_hint", "Items wandern vom Eingabe- ins Ausgabeinventar");
        add("gui.marketblocks.owner", "Besitzer: %s");
        add("gui.marketblocks.out_of_stock", "Nicht auf Lager");

        // Einstellungen
        add("gui.marketblocks.settings_title", "Shop-Einstellungen");
        add("gui.marketblocks.save", "Speichern");
        add("gui.marketblocks.shop_name", "Shopname");
        add("gui.marketblocks.emit_redstone", "Redstone-Signal ausgeben");
        add("gui.marketblocks.emit_redstone.tooltip", "Gibt nach einem Kauf kurz ein Redstone-Signal aus");
        add("gui.marketblocks.settings_owner_only", "Nur der Besitzer kann Einstellungen ändern");

        add("gui.marketblocks.side.left", "Links");
        add("gui.marketblocks.side.right", "Rechts");
        add("gui.marketblocks.side.bottom", "Unten");
        add("gui.marketblocks.side.back", "Hinten");
        add("gui.marketblocks.input", "Eingang");
        add("gui.marketblocks.output", "Ausgang");
        add("gui.marketblocks.disabled", "Deaktiviert");

        // Fehler- und Erfolgsmeldungen
        add("gui.marketblocks.error.no_result_item", "Bitte platziere ein Item im Ergebnis-Slot");
        add("gui.marketblocks.error.no_payment_items", "Bitte platziere mindestens ein Zahlungsitem");
        add("gui.marketblocks.error.invalid_offer", "Ungültige Angebotskonfiguration");

        add("gui.marketblocks.success.offer_created", "Angebot erfolgreich erstellt");
        add("gui.marketblocks.success.offer_deleted", "Angebot erfolgreich gelöscht");

        // Zusätzliche Statusmeldungen
        add("gui.marketblocks.creating_offer", "Angebot wird erstellt...");
        add("gui.marketblocks.offer_ready", "Angebot bereit");
        add("gui.marketblocks.insufficient_stock", "Nicht genügend Bestand");

        // Blocks
        addBlock(RegistriesInit.SMALL_SHOP_BLOCK, "Kleiner Laden");
    }
}