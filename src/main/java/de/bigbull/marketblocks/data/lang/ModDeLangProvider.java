package de.bigbull.marketblocks.data.lang;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.RegistriesInit;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * Generates German (de_de) translations for the mod.
 */
public class ModDeLangProvider extends LanguageProvider {
    public ModDeLangProvider(PackOutput output) {
        super(output, MarketBlocks.MODID, "de_de");
    }

    @Override
    protected void addTranslations() {
        // Creative Tab
        add(ModLang.CREATIVE_TAB, "Market Blocks");

        // Blocks
        addBlock(RegistriesInit.SMALL_SHOP_BLOCK, "Kleiner Laden");

        // Container Titles
        add(ModLang.CONTAINER_SMALL_SHOP, "Laden");
        add(ModLang.CONTAINER_SMALL_SHOP_OFFERS, "Laden - Angebote");
        add(ModLang.CONTAINER_SMALL_SHOP_INVENTORY, "Laden - Inventar");

        // GUI Titles
        add(ModLang.GUI_SHOP_TITLE, "Laden");
        add(ModLang.GUI_INVENTORY_TITLE, "Laden Inventar");
        add(ModLang.GUI_SETTINGS_TITLE, "Shop-Einstellungen");

        // GUI Status Messages
        add(ModLang.GUI_TRADE_AVAILABLE, "Handel verfügbar");
        add(ModLang.GUI_TRADE_UNAVAILABLE, "Handel nicht verfügbar");
        add(ModLang.GUI_AVAILABLE, "Verfügbar");
        add(ModLang.GUI_NO_OFFERS, "Keine Angebote verfügbar");
        add(ModLang.GUI_NO_PLAYERS_AVAILABLE, "Es sind aktuell keine Spieler verfügbar");
        add(ModLang.GUI_OUT_OF_STOCK, "Nicht auf Lager");
        add(ModLang.GUI_OWNER, "Besitzer: %s");
        add(ModLang.GUI_CREATING_OFFER, "Angebot wird erstellt...");
        add(ModLang.GUI_OFFER_READY, "Angebot bereit");
        add(ModLang.GUI_INSUFFICIENT_STOCK, "Nicht genügend Bestand");

        // GUI Offer Creation
        add(ModLang.GUI_CREATE_HINT, "Platziere Items in den unteren Slots");
        add(ModLang.GUI_CONFIRM_OFFER, "Angebot bestätigen");
        add(ModLang.GUI_CANCEL_OFFER, "Abbrechen");
        add(ModLang.GUI_CREATE_OFFER, "Angebot erstellen");
        add(ModLang.GUI_DELETE_OFFER, "Angebot löschen");

        // GUI Navigation
        add(ModLang.GUI_OFFERS, "Angebote");
        add(ModLang.GUI_OFFERS_TAB, "Angebote anzeigen");
        add(ModLang.GUI_INVENTORY_TAB, "Inventar anzeigen");
        add(ModLang.GUI_SETTINGS_TAB, "Einstellungen anzeigen");

        // GUI Inventory Labels
        add(ModLang.GUI_INPUT_INVENTORY, "Eingabe");
        add(ModLang.GUI_OUTPUT_INVENTORY, "Ausgabe");
        add(ModLang.GUI_INVENTORY_OWNER_ONLY, "Nur der Besitzer kann das Inventar verwalten");
        add(ModLang.GUI_INVENTORY_FLOW_HINT, "Items wandern vom Eingabe- ins Ausgabeinventar");

        // GUI Settings
        add(ModLang.GUI_SAVE, "Speichern");
        add(ModLang.GUI_SHOP_NAME, "Shopname");
        add(ModLang.GUI_EMIT_REDSTONE, "Redstone-Signal ausgeben");
        add(ModLang.GUI_EMIT_REDSTONE_TOOLTIP, "Gibt nach einem Kauf kurz ein Redstone-Signal aus");
        add(ModLang.GUI_SETTINGS_OWNER_ONLY, "Nur der Besitzer kann Einstellungen ändern");

        // GUI Side Configuration
        add(ModLang.GUI_SIDE_LEFT, "Links");
        add(ModLang.GUI_SIDE_RIGHT, "Rechts");
        add(ModLang.GUI_SIDE_BOTTOM, "Unten");
        add(ModLang.GUI_SIDE_BACK, "Hinten");
        add(ModLang.GUI_INPUT, "Eingang");
        add(ModLang.GUI_OUTPUT, "Ausgang");
        add(ModLang.GUI_DISABLED, "Deaktiviert");

        // GUI Error Messages
        add(ModLang.GUI_ERROR_NO_RESULT_ITEM, "Bitte platziere ein Item im Ergebnis-Slot");
        add(ModLang.GUI_ERROR_NO_PAYMENT_ITEMS, "Bitte platziere mindestens ein Zahlungsitem");
        add(ModLang.GUI_ERROR_INVALID_OFFER, "Ungültige Angebotskonfiguration");

        // GUI Success Messages
        add(ModLang.GUI_SUCCESS_OFFER_CREATED, "Angebot erfolgreich erstellt");
        add(ModLang.GUI_SUCCESS_OFFER_DELETED, "Angebot erfolgreich gelöscht");
    }
}