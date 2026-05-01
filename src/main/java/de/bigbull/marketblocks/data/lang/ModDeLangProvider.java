package de.bigbull.marketblocks.data.lang;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.init.RegistriesInit;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModDeLangProvider extends LanguageProvider {
    public ModDeLangProvider(PackOutput output) {
        super(output, MarketBlocks.MODID, "de_de");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.marketblocks", "MarketBlocks");

        // Container-Titel
        add("container.marketblocks.trade_stand", "Handelsstand");
        add("container.marketblocks.trade_stand_offers", "Handelsstand - Angebote");
        add("container.marketblocks.trade_stand_inventory", "Handelsstand - Inventar");

        // Menüs
        add("menu.marketblocks.marketplace", "Marktplatz");

        // Tastenkürzel
        add("key.categories.marketblocks", "MarketBlocks");
        add("key.marketblocks.open_marketplace", "Marktplatz öffnen");

        add("gui.marketblocks.shop_title", "Handelsstand");
        add("gui.marketblocks.inventory_title", "Handelsstand Inventar");
        add("gui.marketblocks.trade_available", "Handel verfügbar");
        add("gui.marketblocks.trade_unavailable", "Handel nicht verfügbar");
        add("gui.marketblocks.available", "Verfügbar");
        add("gui.marketblocks.no_offers", "Keine Angebote verfügbar");
        add("gui.marketblocks.no_players_available", "Keine Spieler verf\u00fcgbar");
        add("gui.marketblocks.out_of_stock", "Nicht auf Lager");
        add("gui.marketblocks.output_full", "Ausgabe voll");
        add("gui.marketblocks.owner", "Besitzer: %s");
        add("gui.marketblocks.inventory_owner_only", "Nur der Besitzer kann das Inventar verwalten");
        add("gui.marketblocks.inventory_flow_hint", "Items wandern vom Eingabe- ins Ausgabeinventar");
        add("gui.marketblocks.inventory_tab", "Inventar anzeigen");
        add("gui.marketblocks.settings_tab", "Einstellungen anzeigen");
        add("gui.marketblocks.log_tab", "Verlauf anzeigen");
        add("gui.marketblocks.input_inventory", "Eingabe");
        add("gui.marketblocks.output_inventory", "Ausgabe");
        add("gui.marketblocks.delete_offer", "Angebot löschen");
        add("gui.marketblocks.offers", "Angebote");
        add("gui.marketblocks.offers_tab", "Angebote anzeigen");
        add("gui.marketblocks.create_hint", "Platziere Items in den unteren Slots");
        add("gui.marketblocks.confirm_offer", "Angebot bestätigen");
        add("gui.marketblocks.cancel_offer", "Abbrechen");
        add("gui.marketblocks.create_offer", "Angebot erstellen");

        // Einstellungen
        add("gui.marketblocks.settings_title", "Shop-Einstellungen");
        add("gui.marketblocks.settings.category.general", "Allgemein");
        add("gui.marketblocks.settings.category.io", "I/O");
        add("gui.marketblocks.settings.category.visuals", "Visuals");
        add("gui.marketblocks.settings.category.access", "Zugriff");
        add("gui.marketblocks.log_title", "Transaktions-Log");
        add("gui.marketblocks.log.empty", "Keine Transaktionen vorhanden");
        add("gui.marketblocks.log.clear", "Log leeren");
        add("gui.marketblocks.log.count", "Eintr\u00e4ge: %s");
        add("gui.marketblocks.log.buyer", "K\u00e4ufer: %s");
        add("gui.marketblocks.log.none", "Nichts");
        add("gui.marketblocks.log.time.just_now", "Gerade eben");
        add("gui.marketblocks.log.time.seconds", "Vor %s Sek.");
        add("gui.marketblocks.log.time.minutes", "Vor %s Min.");
        add("gui.marketblocks.log.time.hours", "Vor %s Std.");
        add("gui.marketblocks.log.time.days", "Vor %s T.");
        add("gui.marketblocks.save", "Speichern");
        add("gui.marketblocks.shop_name", "Shopname");
        add("gui.marketblocks.emit_redstone", "Redstone-Signal ausgeben");
        add("gui.marketblocks.emit_redstone.tooltip", "Gibt nach einem Kauf kurz ein Redstone-Signal aus");
        add("gui.marketblocks.purchase_xp_sound", "XP-Sound beim Kauf");
        add("gui.marketblocks.purchase_xp_sound.tooltip", "Spielt einen XP-Sound ab, wenn ein Spieler etwas kauft");
        add("gui.marketblocks.settings_owner_only", "Nur der Besitzer kann Einstellungen ändern");
        add("gui.marketblocks.admin_shop.enabled", "Admin-Shop: AN");
        add("gui.marketblocks.admin_shop.disabled", "Admin-Shop: AUS");
        add("gui.marketblocks.inventory_admin_disabled", "Inventar im Admin-Modus deaktiviert");
        add("gui.marketblocks.visuals.npc_enabled", "Deko-Villager");
        add("gui.marketblocks.visuals.npc_name", "NPC-Name");
        add("gui.marketblocks.visuals.profession", "Beruf");
        add("gui.marketblocks.visuals.purchase_particles", "Kauf-Partikel");
        add("gui.marketblocks.visuals.purchase_sounds", "Kauf-Sounds");
        add("gui.marketblocks.visuals.payment_sounds", "Bezahl-Sounds");
        add("gui.marketblocks.visuals.error.no_surface", "Kein Standblock hinter dem Shop!");
        add("gui.marketblocks.visuals.error.space_blocked", "Platz blockiert!");
        add("gui.marketblocks.visuals.profession.none", "Arbeitslos");
        add("gui.marketblocks.visuals.profession.armorer", "Rüstungsschmied");
        add("gui.marketblocks.visuals.profession.butcher", "Metzger");
        add("gui.marketblocks.visuals.profession.cartographer", "Kartograf");
        add("gui.marketblocks.visuals.profession.cleric", "Kleriker");
        add("gui.marketblocks.visuals.profession.farmer", "Bauer");
        add("gui.marketblocks.visuals.profession.fisherman", "Fischer");
        add("gui.marketblocks.visuals.profession.fletcher", "Pfeilmacher");
        add("gui.marketblocks.visuals.profession.leatherworker", "Lederarbeiter");
        add("gui.marketblocks.visuals.profession.librarian", "Bibliothekar");
        add("gui.marketblocks.visuals.profession.mason", "Steinmetz");
        add("gui.marketblocks.visuals.profession.nitwit", "Trottel");
        add("gui.marketblocks.visuals.profession.shepherd", "Schäfer");
        add("gui.marketblocks.visuals.profession.toolsmith", "Werkzeugschmied");
        add("gui.marketblocks.visuals.profession.weaponsmith", "Waffenschmied");

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

        // Marktplatz
        add("gui.marketblocks.marketplace.unnamed_page", "Seite %s");
        add("gui.marketblocks.marketplace.add_page", "Seite hinzufügen");
        add("gui.marketblocks.marketplace.rename_page", "Seite umbenennen");
        add("gui.marketblocks.marketplace.delete_page", "Seite löschen");
        add("gui.marketblocks.marketplace.add_offer", "Angebot hinzufügen");
        add("gui.marketblocks.marketplace.delete_offer", "Angebot löschen");
        add("gui.marketblocks.marketplace.clear_selection", "Auswahl aufheben");
        add("gui.marketblocks.marketplace.mode.view", "In Ansichtsmodus wechseln");
        add("gui.marketblocks.marketplace.mode.edit", "In Bearbeitungsmodus wechseln");
        add("gui.marketblocks.marketplace.inline.limits", "Limits bearbeiten");
        add("gui.marketblocks.marketplace.inline.pricing", "Preisdynamik bearbeiten");
        add("gui.marketblocks.marketplace.editor.limits.title", "Limits");
        add("gui.marketblocks.marketplace.editor.limits.daily", "Tageslimit");
        add("gui.marketblocks.marketplace.editor.limits.stock", "Lagerlimit");
        add("gui.marketblocks.marketplace.editor.limits.restock", "Restock (s)");
        add("gui.marketblocks.marketplace.editor.pricing.title", "Preisdynamik");
        add("gui.marketblocks.marketplace.editor.pricing.enabled", "Preisdynamik AN");
        add("gui.marketblocks.marketplace.editor.pricing.disabled", "Preisdynamik AUS");
        add("gui.marketblocks.marketplace.editor.pricing.label", "Preisdynamik");
        add("gui.marketblocks.marketplace.editor.pricing.step", "Nachfrage-Schritt");
        add("gui.marketblocks.marketplace.editor.pricing.min", "Minimaler Multiplikator");
        add("gui.marketblocks.marketplace.editor.pricing.max", "Maximaler Multiplikator");
        add("gui.marketblocks.marketplace.move_offer", "Angebot verschieben");
        add("gui.marketblocks.marketplace.move_offer_up", "Angebot nach oben verschieben");
        add("gui.marketblocks.marketplace.move_offer_down", "Angebot nach unten verschieben");
        add("gui.marketblocks.marketplace.no_pages", "Keine Seiten vorhanden");
        add("gui.marketblocks.marketplace.collapsed", "Eingeklappt");
        add("gui.marketblocks.marketplace.no_offers", "Keine Angebote");
        add("gui.marketblocks.marketplace.select_offer_hint", "Wähle links ein Angebot aus");
        add("gui.marketblocks.marketplace.badge.available", "VERFÜGBAR");
        add("gui.marketblocks.marketplace.badge.out_of_stock", "LEER");
        add("gui.marketblocks.marketplace.badge.daily_limit", "TAGESLIMIT");
        add("gui.marketblocks.marketplace.badge.restocking", "RESTOCK LÄUFT");
        add("gui.marketblocks.marketplace.badge.restock_ready", "RESTOCK BEREIT");
        add("gui.marketblocks.marketplace.price_multiplier", "Preisfaktor: x%s");
        add("gui.marketblocks.marketplace.remaining_stock", "Bestand: %s");
        add("gui.marketblocks.marketplace.remaining_daily", "Heute übrig: %s");
        add("gui.marketblocks.marketplace.restock_in", "Nächster Restock in: %s");
        add("gui.marketblocks.marketplace.restock_ready", "Restock bereit");
        add("gui.marketblocks.marketplace.status.price_short", "x%s");
        add("gui.marketblocks.marketplace.status.daily_short", "D:%s");
        add("gui.marketblocks.marketplace.status.stock_short", "S:%s");
        add("gui.marketblocks.marketplace.status.restock_short", "R:%s");
        add("gui.marketblocks.marketplace.tooltip.price_multiplier", "Preisfaktor.");
        add("gui.marketblocks.marketplace.tooltip.remaining_stock", "Verbleibende Käufe aus Bestand.");
        add("gui.marketblocks.marketplace.tooltip.remaining_stock_empty", "Nicht auf Lager.");
        add("gui.marketblocks.marketplace.tooltip.remaining_daily", "Deine restlichen Käufe heute.");
        add("gui.marketblocks.marketplace.tooltip.remaining_daily_empty", "Tageslimit erreicht.");
        add("gui.marketblocks.marketplace.tooltip.restock_in", "Zeit bis Restock.");
        add("gui.marketblocks.marketplace.tooltip.restock_ready", "Restock jetzt fällig.");
        add("gui.marketblocks.marketplace.tooltip.unavailable_daily", "Nicht kaufbar: Tageslimit erreicht.");
        add("gui.marketblocks.marketplace.tooltip.unavailable_stock", "Nicht kaufbar: kein Bestand.");
        add("gui.marketblocks.marketplace.tooltip.unavailable_restock", "Nicht kaufbar: Restock läuft.");
        add("gui.marketblocks.marketplace.tooltip.unavailable_generic", "Nicht kaufbar.");

        add("message.marketblocks.trade_stand.not_owner", "Nur der Besitzer kann diesen Handelsstand abbauen.");
        add("message.marketblocks.trade_stand.no_offer", "Dieser Handelsstand hat aktuell kein aktives Angebot.");
        add("message.marketblocks.marketplace.page_name_blank", "Der Seitenname darf nicht leer sein.");
        add("message.marketblocks.marketplace.page_name_too_long", "Der Seitenname darf maximal %s Zeichen lang sein.");
        add("message.marketblocks.marketplace.page_name_duplicate", "Eine Seite mit dem Namen '%s' existiert bereits.");
        add("message.marketblocks.marketplace.page_not_found", "Die gewählte Shop-Seite wurde nicht gefunden.");
        add("message.marketblocks.marketplace.daily_limit_reached", "Das Tageslimit für dieses Angebot wurde erreicht.");
        add("message.marketblocks.marketplace.edit_mode_enabled", "Marktplatz-Bearbeitungsmodus aktiviert.");
        add("message.marketblocks.marketplace.edit_mode_disabled", "Marktplatz-Bearbeitungsmodus deaktiviert.");
        add("message.marketblocks.marketplace.limits.no_connection", "Limits konnten nicht gespeichert werden: keine Serververbindung.");
        add("message.marketblocks.marketplace.limits.invalid_data", "Limits konnten nicht gespeichert werden: ungültige Daten.");
        add("message.marketblocks.marketplace.limits.invalid_positive_int", "Bitte nur positive ganze Zahlen für Limits eingeben.");
        add("message.marketblocks.marketplace.pricing.invalid_finite", "Bitte gültige endliche Zahlen für die Preisdynamik eingeben.");
        add("message.marketblocks.marketplace.pricing.no_connection", "Preisdynamik konnte nicht gespeichert werden: keine Serververbindung.");
        add("message.marketblocks.marketplace.pricing.invalid_data", "Preisdynamik konnte nicht gespeichert werden: ungültige Daten.");
        add("message.marketblocks.marketplace.pricing.invalid_number_format", "Bitte verwenden Sie nur Zahlen (Punkt oder Komma erlaubt).");
        add("message.marketblocks.visual_npc.space_blocked", "Der dekorative Villager kann nicht spawnen: Es m\u00fcssen zwei freie Bl\u00f6cke dar\u00fcber sein.");
        add("subtitles.marketblocks.visual_npc_fall", "Villager landet");

        add("item.marketblocks.trade_stand.with_showcase", "Handelsstand (Mit Vitrine)");

        // Blocks
        addBlock(RegistriesInit.TRADE_STAND_BLOCK, "Handelsstand");
        addBlock(RegistriesInit.MARKETCRATE_BLOCK, "Marktkiste");
        addBlock(RegistriesInit.SHOP_BLOCK_TEST, "Shop Block Test");
    }
}
