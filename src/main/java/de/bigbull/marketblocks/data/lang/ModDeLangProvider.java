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

        // Menüs
        add("menu.marketblocks.server_shop", "Server-Shop");

        // Tastenkürzel
        add("key.categories.marketblocks", "Market Blocks");
        add("key.marketblocks.open_server_shop", "Server-Shop öffnen");

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
        add("gui.marketblocks.output_almost_full", "Ausgabe fast voll");
        add("gui.marketblocks.output_full", "Ausgabeinventar voll");

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

        // Modi
        add("gui.marketblocks.mode.edit_active", "EDITOR AKTIV");

        // Server-Shop
        add("gui.marketblocks.server_shop.unnamed_page", "Seite %s");
        add("gui.marketblocks.server_shop.add_page", "Seite hinzufügen");
        add("gui.marketblocks.server_shop.rename_page", "Seite umbenennen");
        add("gui.marketblocks.server_shop.delete_page", "Seite löschen");
        add("gui.marketblocks.server_shop.add_offer", "Angebot hinzufügen");
        add("gui.marketblocks.server_shop.delete_offer", "Angebot löschen");
        add("gui.marketblocks.server_shop.clear_selection", "Auswahl aufheben");
        add("gui.marketblocks.server_shop.mode.view", "In Ansichtsmodus wechseln");
        add("gui.marketblocks.server_shop.mode.edit", "In Bearbeitungsmodus wechseln");
        add("gui.marketblocks.server_shop.inline.limits", "Limits bearbeiten");
        add("gui.marketblocks.server_shop.inline.pricing", "Preisdynamik bearbeiten");
        add("gui.marketblocks.server_shop.editor.limits.title", "Limits");
        add("gui.marketblocks.server_shop.editor.limits.daily", "Tageslimit");
        add("gui.marketblocks.server_shop.editor.limits.stock", "Lagerlimit");
        add("gui.marketblocks.server_shop.editor.limits.restock", "Restock (s)");
        add("gui.marketblocks.server_shop.editor.pricing.title", "Preisdynamik");
        add("gui.marketblocks.server_shop.editor.pricing.enabled", "Preisdynamik AN");
        add("gui.marketblocks.server_shop.editor.pricing.disabled", "Preisdynamik AUS");
        add("gui.marketblocks.server_shop.editor.pricing.label", "Preisdynamik");
        add("gui.marketblocks.server_shop.editor.pricing.step", "Nachfrage-Schritt");
        add("gui.marketblocks.server_shop.editor.pricing.min", "Minimaler Multiplikator");
        add("gui.marketblocks.server_shop.editor.pricing.max", "Maximaler Multiplikator");
        add("gui.marketblocks.server_shop.move_offer", "Angebot verschieben");
        add("gui.marketblocks.server_shop.move_offer_up", "Angebot nach oben verschieben");
        add("gui.marketblocks.server_shop.move_offer_down", "Angebot nach unten verschieben");
        add("gui.marketblocks.server_shop.no_pages", "Keine Seiten vorhanden");
        add("gui.marketblocks.server_shop.collapsed", "Eingeklappt");
        add("gui.marketblocks.server_shop.no_offers", "Keine Angebote");
        add("gui.marketblocks.server_shop.select_offer_hint", "Wähle links ein Angebot aus");
        add("gui.marketblocks.server_shop.badge.available", "VERFÜGBAR");
        add("gui.marketblocks.server_shop.badge.out_of_stock", "LEER");
        add("gui.marketblocks.server_shop.badge.daily_limit", "TAGESLIMIT");
        add("gui.marketblocks.server_shop.badge.restocking", "RESTOCK LÄUFT");
        add("gui.marketblocks.server_shop.badge.restock_ready", "RESTOCK BEREIT");
        add("gui.marketblocks.server_shop.price_multiplier", "Preisfaktor: x%s");
        add("gui.marketblocks.server_shop.remaining_stock", "Bestand: %s");
        add("gui.marketblocks.server_shop.remaining_daily", "Heute übrig: %s");
        add("gui.marketblocks.server_shop.restock_in", "Nächster Restock in: %s");
        add("gui.marketblocks.server_shop.restock_ready", "Restock bereit");
        add("gui.marketblocks.server_shop.status.price_short", "x%s");
        add("gui.marketblocks.server_shop.status.daily_short", "D:%s");
        add("gui.marketblocks.server_shop.status.stock_short", "S:%s");
        add("gui.marketblocks.server_shop.status.restock_short", "R:%s");
        add("gui.marketblocks.server_shop.tooltip.price_multiplier", "Preisfaktor.");
        add("gui.marketblocks.server_shop.tooltip.remaining_stock", "Verbleibende Käufe aus Bestand.");
        add("gui.marketblocks.server_shop.tooltip.remaining_stock_empty", "Nicht auf Lager.");
        add("gui.marketblocks.server_shop.tooltip.remaining_daily", "Deine restlichen Käufe heute.");
        add("gui.marketblocks.server_shop.tooltip.remaining_daily_empty", "Tageslimit erreicht.");
        add("gui.marketblocks.server_shop.tooltip.restock_in", "Zeit bis Restock.");
        add("gui.marketblocks.server_shop.tooltip.restock_ready", "Restock jetzt fällig.");
        add("gui.marketblocks.server_shop.tooltip.unavailable_daily", "Nicht kaufbar: Tageslimit erreicht.");
        add("gui.marketblocks.server_shop.tooltip.unavailable_stock", "Nicht kaufbar: kein Bestand.");
        add("gui.marketblocks.server_shop.tooltip.unavailable_restock", "Nicht kaufbar: Restock läuft.");
        add("gui.marketblocks.server_shop.tooltip.unavailable_generic", "Nicht kaufbar.");

        add("message.marketblocks.small_shop.not_owner", "Nur der Besitzer kann diesen Shop abbauen.");
        add("message.marketblocks.small_shop.no_offer", "Dieser Shop hat aktuell kein aktives Angebot.");
        add("message.marketblocks.server_shop.page_name_blank", "Der Seitenname darf nicht leer sein.");
        add("message.marketblocks.server_shop.page_name_too_long", "Der Seitenname darf maximal %s Zeichen lang sein.");
        add("message.marketblocks.server_shop.page_name_duplicate", "Eine Seite mit dem Namen '%s' existiert bereits.");
        add("message.marketblocks.server_shop.page_not_found", "Die gewählte Shop-Seite wurde nicht gefunden.");
        add("message.marketblocks.server_shop.daily_limit_reached", "Das Tageslimit für dieses Angebot wurde erreicht.");
        add("message.marketblocks.server_shop.edit_mode_enabled", "ServerShop-Bearbeitungsmodus aktiviert.");
        add("message.marketblocks.server_shop.edit_mode_disabled", "ServerShop-Bearbeitungsmodus deaktiviert.");
        add("message.marketblocks.server_shop.limits.no_connection", "Limits konnten nicht gespeichert werden: keine Serververbindung.");
        add("message.marketblocks.server_shop.limits.invalid_data", "Limits konnten nicht gespeichert werden: ungültige Daten.");
        add("message.marketblocks.server_shop.limits.invalid_positive_int", "Bitte nur positive ganze Zahlen für Limits eingeben.");
        add("message.marketblocks.server_shop.pricing.invalid_finite", "Bitte gültige endliche Zahlen für die Preisdynamik eingeben.");
        add("message.marketblocks.server_shop.pricing.no_connection", "Preisdynamik konnte nicht gespeichert werden: keine Serververbindung.");
        add("message.marketblocks.server_shop.pricing.invalid_data", "Preisdynamik konnte nicht gespeichert werden: ungültige Daten.");
        add("message.marketblocks.server_shop.pricing.invalid_number_format", "Bitte nur Zahlen eingeben (Punkt oder Komma erlaubt).");

        add("item.marketblocks.small_shop.with_showcase", "Kleiner Laden (mit Vitrine)");

        // Blocks
        addBlock(RegistriesInit.SMALL_SHOP_BLOCK, "Kleiner Laden");
        addBlock(RegistriesInit.SMALL_SHOP_BLOCK_TEST, "Kleiner Laden (Test)");
    }
}
