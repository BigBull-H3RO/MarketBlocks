package de.bigbull.marketblocks.data.lang;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.core.init.RegistriesInit;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * Data provider for generating the German (de_de) language file.
 * Contains all translated strings for UI elements, messages, and blocks.
 */
public class ModDeLangProvider extends LanguageProvider {
    public ModDeLangProvider(PackOutput output) {
        super(output, MarketBlocks.MODID, "de_de");
    }

    @Override
    protected void addTranslations() {
        // === Creative Tab ===
        add("itemGroup.marketblocks", "MarketBlocks");

        // === Blocks & Items ===
        add("item.marketblocks.trade_stand.with_showcase", "Handelsstand (Mit Vitrine)");

        // === Containers & Menus ===
        add("container.marketblocks.trade_stand", "Handelsstand");
        add("menu.marketblocks.marketplace", "Marktplatz");

        // === Keybinds ===
        add("key.categories.marketblocks", "MarketBlocks");
        add("key.marketblocks.open_marketplace", "Marktplatz öffnen");

        // === Commands ===
        add("command.marketblocks.break.denied", "Du kannst diesen Block nicht abbauen, solange er mit einem Marktplatz verlinkt ist!");
        add("command.marketblocks.break.unlinked", "Der Block wurde vom Marktplatz entlinkt.");
        add("command.marketblocks.link.already_linked", "Dieser Verkaufsstand ist bereits mit dem Marktplatz verlinkt.");
        add("command.marketblocks.link.not_looking_at_block", "Du musst einen Verkaufsstand ansehen, um ihn zu verlinken.");
        add("command.marketblocks.link.success", "Verkaufsstand erfolgreich mit Marktplatz verlinkt!");
        add("command.marketblocks.list.click_to_delete", "Klicken zum Löschen");
        add("command.marketblocks.list.click_to_teleport", "Klicken zum Teleportieren");
        add("command.marketblocks.list.click_to_waypoint", "Klicken für Wegpunkt-Links im Chat");
        add("command.marketblocks.list.delete", "[Löschen]");
        add("command.marketblocks.list.tp", "[Teleport]");
        add("command.marketblocks.list.waypoint", "[Wegpunkt]");
        add("command.marketblocks.marketplacelist.entry", "- Marktplatz %s");
        add("command.marketblocks.marketplacelist.header", "--- Marktplatz-Liste ---");
        add("command.marketblocks.marketplacelist.no_links", "Keine Marktplätze gefunden.");
        add("command.marketblocks.player_not_found", "Spieler nicht gefunden");
        add("command.marketblocks.reload.success", "MarketBlocks-Konfiguration erfolgreich neu geladen!");
        add("command.marketblocks.resetlimits.no_changes", "Es wurden keine Tageslimits zurückgesetzt.");
        add("command.marketblocks.resetlimits.success", "Tageslimits erfolgreich zurückgesetzt.");
        add("command.marketblocks.shoplist.closed", "GESCHLOSSEN");
        add("command.marketblocks.shoplist.entry", "- [%s] %s (von %s)");
        add("command.marketblocks.shoplist.header", "--- Shop-Liste ---");
        add("command.marketblocks.shoplist.no_shops", "Keine Shops auf diesem Marktplatz verfügbar.");
        add("command.marketblocks.shoplist.open", "OFFEN");
        add("command.marketblocks.unlink.not_found", "Konnte die Marktplatz-Verlinkung nicht finden.");
        add("command.marketblocks.unlink.not_linked", "Dieser Verkaufsstand ist nicht verlinkt.");
        add("command.marketblocks.unlink.not_looking_at_block", "Du musst einen Verkaufsstand ansehen, um ihn zu entlinken.");
        add("command.marketblocks.unlink.success", "Verkaufsstand erfolgreich entlinkt!");
        add("command.marketblocks.unlink.success_name", "Vom Marktplatz entlinkt: %s");
        add("command.marketblocks.waypoint.created", "Wegpunkt-Links erfolgreich erstellt:");

        // === GUI - Settings ===
        add("gui.marketblocks.access.edit_access_list", "Zugriffsliste");
        add("gui.marketblocks.access.edit_owners", "Besitzer");
        add("gui.marketblocks.access.mode.blacklist", "Modus: Blacklist");
        add("gui.marketblocks.access.mode.everyone", "Modus: Alle");
        add("gui.marketblocks.access.mode.whitelist", "Modus: Whitelist");
        add("gui.marketblocks.io.allow_io", "I/O erlauben");
        add("gui.marketblocks.io.allow_io.tooltip", "Erlaube Trichtern und Kabeln, mit dieser Seite zu interagieren.");
        add("gui.marketblocks.io.auto_io", "Autom. Ein-/Ausgabe");
        add("gui.marketblocks.io.auto_io.tooltip", "Items automatisch in angrenzende Inventare verschieben.");
        add("gui.marketblocks.io.redstone_control.ignored", "Ignoriert");
        add("gui.marketblocks.io.redstone_control.require_no_signal", "Niedrig (kein Signal)");
        add("gui.marketblocks.io.redstone_control.require_signal", "Hoch (Signal benötigt)");
        add("gui.marketblocks.io.redstone_control.tooltip", "Redstone-Steuerungsmodus für diese Seite.");
        add("gui.marketblocks.settings_owner_only", "Nur der Besitzer kann Einstellungen ändern");
        add("gui.marketblocks.settings_tab", "Einstellungen anzeigen");
        add("gui.marketblocks.settings_title", "Shop-Einstellungen");
        add("gui.marketblocks.toggle.off", "AUS");
        add("gui.marketblocks.toggle.on", "AN");
        add("gui.marketblocks.settings.category.access", "Zugriff");
        add("gui.marketblocks.settings.category.general", "Allgemein");
        add("gui.marketblocks.settings.category.io", "I/O");
        add("gui.marketblocks.settings.category.notifications", "Benachrichtigungen");
        add("gui.marketblocks.settings.category.villager", "NPC");
        add("gui.marketblocks.settings.category.visual", "Visual");
        add("gui.marketblocks.visuals.bobbing", "Wippen (Bobbing)");
        add("gui.marketblocks.visuals.chaos_rotation", "Chaos-Rotation");
        add("gui.marketblocks.visuals.count", "Anzahl");
        add("gui.marketblocks.visuals.dynamic_fill_level", "Dyn. Füllstand");
        add("gui.marketblocks.visuals.error.no_surface", "Kein Standblock hinter dem Shop!");
        add("gui.marketblocks.visuals.error.space_blocked", "Platz blockiert!");
        add("gui.marketblocks.visuals.height", "Höhe");
        add("gui.marketblocks.visuals.layout_mode", "Layout-Modus");
        add("gui.marketblocks.visuals.layout_mode.gestapelt", "Gestapelt");
        add("gui.marketblocks.visuals.layout_mode.lose", "Lose");
        add("gui.marketblocks.visuals.npc_enabled", "Deko-Dorfbewohner");
        add("gui.marketblocks.visuals.npc_name", "NPC-Name");
        add("gui.marketblocks.visuals.offer_item_disabled_global", "Vom Server-Admin deaktiviert.");
        add("gui.marketblocks.visuals.offer_item_fullbright", "Leuchten");
        add("gui.marketblocks.visuals.offer_item_fullbright.tooltip", "Lässt das Item im Dunkeln leuchten.");
        add("gui.marketblocks.visuals.offer_item_visible", "Angebot sichtbar");
        add("gui.marketblocks.visuals.offer_item_visible.tooltip", "Zeigt oder versteckt das schwebende/dargestellte Angebot.");
        add("gui.marketblocks.visuals.payment_sounds", "Bezahl-Sounds");
        add("gui.marketblocks.visuals.player_skin_name", "Spielername");
        add("gui.marketblocks.visuals.profession", "Beruf");
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
        add("gui.marketblocks.visuals.profession.none", "Arbeitslos");
        add("gui.marketblocks.visuals.profession.shepherd", "Schäfer");
        add("gui.marketblocks.visuals.profession.toolsmith", "Werkzeugschmied");
        add("gui.marketblocks.visuals.profession.weaponsmith", "Waffenschmied");
        add("gui.marketblocks.visuals.purchase_particles", "Kauf-Partikel");
        add("gui.marketblocks.visuals.purchase_sounds", "Kauf-Sounds");
        add("gui.marketblocks.visuals.rotation", "Rotation");
        add("gui.marketblocks.visuals.rotation_x", "Rotation X");
        add("gui.marketblocks.visuals.rotation_y", "Rotation Y");
        add("gui.marketblocks.visuals.rotation_z", "Rotation Z");
        add("gui.marketblocks.visuals.scale", "Größe");
        add("gui.marketblocks.visuals.spacing", "Item-Abstand");
        add("gui.marketblocks.visuals.spacing_xz", "Abstand X/Z");
        add("gui.marketblocks.visuals.spacing_y", "Abstand Y");
        add("gui.marketblocks.visuals.speed", "Rotationsgeschw.");
        add("gui.marketblocks.visuals.use_player_skin", "Spieler-Skin");
        add("gui.marketblocks.visuals.use_player_skin.tooltip", "Einen Spieler anstelle eines Dorfbewohners anzeigen.");

        // === GUI - Notifications ===
        add("gui.marketblocks.notifications.co_owners", "Mitbesitzer benachrichtigen");
        add("gui.marketblocks.notifications.co_owners.tooltip", "Sende Benachrichtigungen auch an zusätzliche Shop-Besitzer.");
        add("gui.marketblocks.notifications.login.out_of_stock", "§cWarnung: %s deiner Shops sind leer!§r");
        add("gui.marketblocks.notifications.login.output_full", "§cWarnung: Bei %s deiner Shops ist das Ausgabe-Inventar voll!§r");
        add("gui.marketblocks.notifications.out_of_stock", "Leer-Warnung");
        add("gui.marketblocks.notifications.out_of_stock.tooltip", "Werde gewarnt, wenn der Shop leer ist.");
        add("gui.marketblocks.notifications.output_full", "Ausgabe Voll-Warnung");
        add("gui.marketblocks.notifications.output_full.tooltip", "Werde gewarnt, wenn das Ausgabe-Inventar des Shops voll ist.");
        add("gui.marketblocks.notifications.purchase", "Kaufbenachrichtigung");
        add("gui.marketblocks.notifications.purchase.tooltip", "Werde im Chat benachrichtigt, wenn jemand aus deinem Shop kauft.");
        add("message.marketblocks.notifications.out_of_stock", "§cDein Shop ist leer!§r");
        add("message.marketblocks.notifications.output_full", "§cDas Ausgabe-Inventar deines Shops ist voll!§r");
        add("message.marketblocks.notifications.purchase", "§a%s kaufte %sx %s aus deinem Shop.§r");

        // === GUI - Marketplace Editors ===
        add("gui.marketblocks.marketplace.editor.limits.daily", "Tageslimit");
        add("gui.marketblocks.marketplace.editor.limits.restock", "Auffüllzeit (s)");
        add("gui.marketblocks.marketplace.editor.limits.stock", "Lagerlimit");
        add("gui.marketblocks.marketplace.editor.limits.title", "Limits");
        add("gui.marketblocks.marketplace.editor.pricing.disabled", "Preisdynamik AUS");
        add("gui.marketblocks.marketplace.editor.pricing.enabled", "Preisdynamik AN");
        add("gui.marketblocks.marketplace.editor.pricing.label", "Preisdynamik");
        add("gui.marketblocks.marketplace.editor.pricing.max", "Maximaler Multiplikator");
        add("gui.marketblocks.marketplace.editor.pricing.min", "Minimaler Multiplikator");
        add("gui.marketblocks.marketplace.editor.pricing.step", "Nachfrage-Schritt");
        add("gui.marketblocks.marketplace.editor.pricing.title", "Preisdynamik");

        // === GUI - Marketplace Elements ===
        add("gui.marketblocks.marketplace.add_offer", "Angebot hinzufügen");
        add("gui.marketblocks.marketplace.add_page", "Seite hinzufügen");
        add("gui.marketblocks.marketplace.delete_offer", "Angebot löschen");
        add("gui.marketblocks.marketplace.delete_page", "Seite löschen");
        add("gui.marketblocks.marketplace.inline.limits", "Limits bearbeiten");
        add("gui.marketblocks.marketplace.inline.pricing", "Preisdynamik bearbeiten");
        add("gui.marketblocks.marketplace.mode.edit", "In Bearbeitungsmodus wechseln");
        add("gui.marketblocks.marketplace.mode.view", "In Ansichtsmodus wechseln");
        add("gui.marketblocks.marketplace.move_offer", "Angebot verschieben");
        add("gui.marketblocks.marketplace.move_offer_down", "Angebot nach unten verschieben");
        add("gui.marketblocks.marketplace.move_offer_up", "Angebot nach oben verschieben");
        add("gui.marketblocks.marketplace.no_offers", "Keine Angebote");
        add("gui.marketblocks.marketplace.no_pages", "Keine Seiten\nvorhanden");
        add("gui.marketblocks.marketplace.rename_page", "Seite umbenennen");
        add("gui.marketblocks.marketplace.status.daily_short", "D:%s");
        add("gui.marketblocks.marketplace.status.price_short", "x%s");
        add("gui.marketblocks.marketplace.status.restock_short", "R:%s");
        add("gui.marketblocks.marketplace.status.stock_short", "S:%s");
        add("gui.marketblocks.marketplace.tooltip.price_multiplier", "Preisfaktor.");
        add("gui.marketblocks.marketplace.tooltip.remaining_daily", "Deine restlichen Käufe heute.");
        add("gui.marketblocks.marketplace.tooltip.remaining_daily_empty", "Tageslimit erreicht.");
        add("gui.marketblocks.marketplace.tooltip.remaining_stock", "Verbleibende Käufe aus Bestand.");
        add("gui.marketblocks.marketplace.tooltip.remaining_stock_empty", "Nicht auf Lager.");
        add("gui.marketblocks.marketplace.tooltip.restock_in", "Zeit bis zum Auffüllen.");
        add("gui.marketblocks.marketplace.tooltip.restock_ready", "Auffüllen ist jetzt fällig.");
        add("gui.marketblocks.marketplace.tooltip.unavailable_daily", "Nicht kaufbar: Tageslimit erreicht.");
        add("gui.marketblocks.marketplace.tooltip.unavailable_generic", "Nicht kaufbar.");
        add("gui.marketblocks.marketplace.tooltip.unavailable_restock", "Nicht kaufbar: Auffüllen läuft.");
        add("gui.marketblocks.marketplace.tooltip.unavailable_stock", "Nicht kaufbar: kein Bestand.");
        add("gui.marketblocks.marketplace.unnamed_page", "Seite %s");

        // === GUI - General ===
        add("gui.marketblocks.admin_shop.disabled", "Admin-Shop: AUS");
        add("gui.marketblocks.admin_shop.enabled", "Admin-Shop: AN");
        add("gui.marketblocks.create_offer", "Angebot erstellen");
        add("gui.marketblocks.delete_offer", "Angebot löschen");
        add("gui.marketblocks.disabled", "Deaktiviert");
        add("gui.marketblocks.emit_redstone", "Redstone-Signal ausgeben");
        add("gui.marketblocks.emit_redstone.tooltip", "Gibt nach einem Kauf kurz ein Redstone-Signal aus");
        add("gui.marketblocks.error.invalid_offer", "Ungültige Angebotskonfiguration");
        add("gui.marketblocks.error.no_payment_items", "Bitte platziere mindestens ein Zahlungsitem");
        add("gui.marketblocks.error.no_result_item", "Bitte platziere ein Item im Ergebnis-Slot");
        add("gui.marketblocks.input", "Eingang");
        add("gui.marketblocks.inventory_admin_disabled", "Inventar im Admin-Modus deaktiviert");
        add("gui.marketblocks.inventory_owner_only", "Nur der Besitzer kann das Inventar verwalten");
        add("gui.marketblocks.inventory_tab", "Inventar anzeigen");
        add("gui.marketblocks.inventory_title", "Handelsstand-Inventar");
        add("gui.marketblocks.log_tab", "Verlauf anzeigen");
        add("gui.marketblocks.log_title", "Transaktions-Log");
        add("gui.marketblocks.log.clear", "Log leeren");
        add("gui.marketblocks.log.count", "Einträge: %s");
        add("gui.marketblocks.log.empty", "Keine Transaktionen vorhanden");
        add("gui.marketblocks.log.none", "Nichts");
        add("gui.marketblocks.log.time.days", "Vor %s T.");
        add("gui.marketblocks.log.time.hours", "Vor %s Std.");
        add("gui.marketblocks.log.time.just_now", "Gerade eben");
        add("gui.marketblocks.log.time.minutes", "Vor %s Min.");
        add("gui.marketblocks.log.time.seconds", "Vor %s Sek.");
        add("gui.marketblocks.mode.edit_active", "EDITOR AKTIV");
        add("gui.marketblocks.no_players_available", "Keine Spieler verfügbar");
        add("gui.marketblocks.offers", "Angebote");
        add("gui.marketblocks.offers_tab", "Angebote anzeigen");
        add("gui.marketblocks.out_of_stock", "Nicht auf Lager");
        add("gui.marketblocks.output", "Ausgang");
        add("gui.marketblocks.output_full", "Ausgabe voll");
        add("gui.marketblocks.owner", "Besitzer: %s");
        add("gui.marketblocks.purchase_xp_sound", "XP-Sound beim Kauf");
        add("gui.marketblocks.purchase_xp_sound.tooltip", "Spielt einen XP-Sound ab, wenn ein Spieler etwas kauft");
        add("gui.marketblocks.save", "Speichern");
        add("gui.marketblocks.shop_closed", "Shop geschlossen");
        add("gui.marketblocks.shop_closed.tooltip", "Ist dies aktiv, können nur Besitzer einkaufen.");
        add("gui.marketblocks.shop_name", "Shopname");
        add("gui.marketblocks.shop_title", "Handelsstand");
        add("gui.marketblocks.side.back", "Hinten");
        add("gui.marketblocks.side.bottom", "Unten");
        add("gui.marketblocks.side.left", "Links");
        add("gui.marketblocks.side.right", "Rechts");

        // === Messages & Chat ===
        add("message.marketblocks.marketplace.daily_limit_reached", "Das Tageslimit für dieses Angebot wurde erreicht.");
        add("message.marketblocks.marketplace.edit_mode_disabled", "Marktplatz-Bearbeitungsmodus deaktiviert.");
        add("message.marketblocks.marketplace.edit_mode_enabled", "Marktplatz-Bearbeitungsmodus aktiviert.");
        add("message.marketblocks.marketplace.limits.invalid_data", "Limits konnten nicht gespeichert werden: ungültige Daten.");
        add("message.marketblocks.marketplace.limits.invalid_positive_int", "Bitte nur positive ganze Zahlen für Limits eingeben.");
        add("message.marketblocks.marketplace.limits.no_connection", "Limits konnten nicht gespeichert werden: keine Serververbindung.");
        add("message.marketblocks.marketplace.page_name_blank", "Der Seitenname darf nicht leer sein.");
        add("message.marketblocks.marketplace.page_name_duplicate", "Eine Seite mit dem Namen '%s' existiert bereits.");
        add("message.marketblocks.marketplace.page_name_too_long", "Der Seitenname darf maximal %s Zeichen lang sein.");
        add("message.marketblocks.marketplace.page_not_found", "Die gewählte Shop-Seite wurde nicht gefunden.");
        add("message.marketblocks.marketplace.pricing.invalid_data", "Preisdynamik konnte nicht gespeichert werden: ungültige Daten.");
        add("message.marketblocks.marketplace.pricing.invalid_finite", "Bitte gültige endliche Zahlen für die Preisdynamik eingeben.");
        add("message.marketblocks.marketplace.pricing.invalid_number_format", "Bitte verwende nur Zahlen (Punkt oder Komma erlaubt).");
        add("message.marketblocks.marketplace.pricing.no_connection", "Preisdynamik konnte nicht gespeichert werden: keine Serververbindung.");
        add("message.marketblocks.trade_stand.no_offer", "Dieser Handelsstand hat aktuell kein aktives Angebot.");
        add("message.marketblocks.trade_stand.not_owner", "Nur der Besitzer kann diesen Handelsstand abbauen.");

        // === Jade / Waila Support ===
        add("config.jade.plugin_marketblocks.shop_info", "Shop-Info");
        add("marketblocks.jade.for", "Für:");
        add("marketblocks.jade.out_of_stock", "Ausverkauft!");
        add("marketblocks.jade.output_full", "Lager voll!");
        add("marketblocks.jade.owner", "Besitzer: %s");
        add("marketblocks.jade.selling", "Verkauft:");
        add("marketblocks.jade.status.admin_shop", "Admin-Shop");
        add("marketblocks.jade.status.closed", "Shop geschlossen");

        // === Advancements ===
        add("advancements.marketblocks.admin_shop.description", "Aktiviere den Admin-Shop-Modus");
        add("advancements.marketblocks.admin_shop.title", "Unendliche Waren");
        add("advancements.marketblocks.auto_io.description", "Aktiviere automatische Ein-/Ausgabe für deinen Shop");
        add("advancements.marketblocks.auto_io.title", "Logistik");
        add("advancements.marketblocks.custom_npc.description", "Passe deinen Shop-NPC mit Namen oder Spieler-Skin an");
        add("advancements.marketblocks.custom_npc.title", "Eigenes Personal");
        add("advancements.marketblocks.first_shop.description", "Platziere deinen ersten MarketBlocks-Shopblock");
        add("advancements.marketblocks.first_shop.title", "Eröffnet!");
        add("advancements.marketblocks.hiring.description", "Aktiviere einen Shop-NPC für deinen Shop");
        add("advancements.marketblocks.hiring.title", "Wir stellen ein");
        add("advancements.marketblocks.joint_venture.description", "Füge einen Mitbesitzer hinzu");
        add("advancements.marketblocks.joint_venture.title", "Joint Venture");
        add("advancements.marketblocks.marketplace_buy.description", "Kaufe ein Item über den Marktplatz");
        add("advancements.marketblocks.marketplace_buy.title", "Einkaufsbummel");
        add("advancements.marketblocks.out_of_stock.description", "Ein Nicht-Admin-Shop hat keine Waren mehr");
        add("advancements.marketblocks.out_of_stock.title", "Ausverkauft");
        add("advancements.marketblocks.redstone.description", "Aktiviere Redstone-Ausgabe oder redstonegesteuerte I/O");
        add("advancements.marketblocks.redstone.title", "Redstone-Logik");
        add("advancements.marketblocks.root.description", "Erhalte einen MarketBlocks-Shopblock");
        add("advancements.marketblocks.root.title", "MarketBlocks");
        add("advancements.marketblocks.showcase.description", "Füge einem Verkaufsstand eine Glasvitrine hinzu");
        add("advancements.marketblocks.showcase.title", "Schaufenster");
        add("advancements.marketblocks.sold_item.description", "Verkaufe dein erstes Item an einen anderen Spieler");
        add("advancements.marketblocks.sold_item.title", "Erster Verkauf!");
        add("advancements.marketblocks.tycoon.description", "Verkaufe 100 Items über deine Shops");
        add("advancements.marketblocks.tycoon.title", "Tycoon");
        add("advancements.marketblocks.wall_street.description", "Öffne den Marktplatz");
        add("advancements.marketblocks.wall_street.title", "Wall Street");
        add("advancements.marketblocks.wholesaler.description", "Kaufe 64 oder mehr Items in einer Transaktion");
        add("advancements.marketblocks.wholesaler.title", "Großhändler");

        // === Subtitles ===
        add("subtitles.marketblocks.visual_npc_fall", "Dorfbewohner landet");

        // === Block Registrations ===
        addBlock(RegistriesInit.MARKETCRATE_BLOCK, "Marktkiste");
        addBlock(RegistriesInit.MARKETPLACE_BLOCK, "Marktplatz");
        addBlock(RegistriesInit.TRADE_STAND_BLOCK, "Verkaufsstand");

    }
}
