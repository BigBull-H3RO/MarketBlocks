package de.bigbull.marketblocks.data.lang;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.core.init.RegistriesInit;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * Data provider for generating the French (fr_fr) language file.
 * Contains all translated strings for UI elements, messages, and blocks.
 */
public class ModFrLangProvider extends LanguageProvider {
        public ModFrLangProvider(PackOutput output) {
                super(output, MarketBlocks.MODID, "fr_fr");
        }

        @Override
        protected void addTranslations() {
                // === Creative Tab ===
                add("itemGroup.marketblocks", "MarketBlocks");

                // === Blocks & Items ===
                add("item.marketblocks.trade_stand.with_showcase", "Stand de commerce (avec vitrine)");

                // === Entities ===
                add("entity.marketblocks.shop_buyer", "Acheteur ambulant");
                add("item.marketblocks.shop_buyer_spawn_egg", "Œuf d'apparition d'acheteur ambulant");

                // === Containers & Menus ===
                add("container.marketblocks.trade_stand", "Stand de commerce");
                add("menu.marketblocks.marketplace", "Place du marché");

                // === Keybinds ===
                add("key.categories.marketblocks", "MarketBlocks");
                add("key.marketblocks.open_marketplace", "Ouvrir la place du marché");

                // === Commands ===
                add("command.marketblocks.break.denied", "§cVous ne pouvez pas détruire ce bloc tant qu'il est lié à une place du marché !");
                add("command.marketblocks.break.unlinked", "§eLe bloc a été délié de la place du marché.");
                add("command.marketblocks.link.already_linked", "§cCe bloc est déjà lié à une place du marché.");
                add("command.marketblocks.link.not_looking_at_block", "§cVous devez regarder un bloc pour le lier.");
                add("command.marketblocks.link.success", "§aBloc lié à la place du marché avec succès !");
                add("command.marketblocks.list.click_to_delete", "Cliquez pour supprimer");
                add("command.marketblocks.list.click_to_teleport", "Cliquez pour vous téléporter");
                add("command.marketblocks.list.click_to_waypoint", "Cliquez pour obtenir les liens Waypoint dans le chat");
                add("command.marketblocks.list.delete", "[Supprimer]");
                add("command.marketblocks.list.tp", "[Téléportation]");
                add("command.marketblocks.list.waypoint", "[Waypoint]");
                add("command.marketblocks.list.page_header", "§8======== §6§lBoutiques (Page %s/%s) §8========");
                add("command.marketblocks.list.prev", "[< Préc]");
                add("command.marketblocks.list.next", "[Suiv >]");
                add("command.marketblocks.marketplacelist.entry", "§8▪ §6Place du marché §e%s");
                add("command.marketblocks.marketplacelist.page_header", "§8======== §6§lPlaces du marché (Page %s/%s) §8========");
                add("command.marketblocks.marketplacelist.no_links", "§cAucune place du marché trouvée.");
                add("command.marketblocks.player_not_found", "§cJoueur introuvable");
                add("command.marketblocks.reload.success", "§aConfiguration de MarketBlocks rechargée avec succès !");
                add("command.marketblocks.resetlimits.no_changes", "§eAucune limite journalière n'a été réinitialisée.");
                add("command.marketblocks.resetlimits.success", "§aLimites journalières réinitialisées avec succès.");
                add("command.marketblocks.search.header", "§8======== §6§lBoutiques vendant %s (Page %s/%s) §8========");
                add("command.marketblocks.search.no_shops", "§cAucune boutique ni place du marché trouvée vendant %s.");
                add("command.marketblocks.shoplist.closed", "FERMÉ");
                add("command.marketblocks.shoplist.entry", "§8▪ §7[%s§7] §e%s §8(par §7%s§8)");
                add("command.marketblocks.shoplist.header", "§8======== §6§lBoutiques MarketBlocks §8========");
                add("command.marketblocks.shoplist.no_shops", "§cAucune boutique disponible.");
                add("command.marketblocks.shoplist.open", "OUVERT");
                add("command.marketblocks.shoplist.hover.shop", "Boutique : %s");
                add("command.marketblocks.shoplist.hover.owner", "Propriétaire : %s");
                add("command.marketblocks.shoplist.hover.status", "Statut : %s");
                add("command.marketblocks.shoplist.hover.offer", "Offre :");
                add("command.marketblocks.shoplist.hover.arrow", "➔");
                add("command.marketblocks.unlink.not_found", "§cImpossible de trouver le lien de la place du marché.");
                add("command.marketblocks.unlink.not_linked", "§cCe bloc n'est pas lié.");
                add("command.marketblocks.unlink.not_looking_at_block", "§cVous devez regarder un bloc pour le délier.");
                add("command.marketblocks.unlink.success", "§aBloc délié avec succès !");
                add("command.marketblocks.unlink.success_name", "§aDélié de la place du marché : §e%s");
                add("command.marketblocks.waypoint.created", "§aLiens de partage Waypoint créés :");

                // === GUI - Settings ===
                add("gui.marketblocks.access.edit_access_list", "Liste d'accès");
                add("gui.marketblocks.access.edit_owners", "Propriétaires");
                add("gui.marketblocks.access.mode.blacklist", "Mode : Liste noire");
                add("gui.marketblocks.access.mode.everyone", "Mode : Tout le monde");
                add("gui.marketblocks.access.mode.whitelist", "Mode : Liste blanche");
                add("gui.marketblocks.io.allow_io", "Autoriser I/O");
                add("gui.marketblocks.io.allow_io.tooltip", "Autorise les entonnoirs et tuyaux à interagir avec ce côté.");
                add("gui.marketblocks.io.auto_io", "Auto Push/Pull");
                add("gui.marketblocks.io.auto_io.tooltip", "Pousse et tire automatiquement les objets des inventaires adjacents.");
                add("gui.marketblocks.io.redstone_control.ignored", "Ignoré");
                add("gui.marketblocks.io.redstone_control.require_no_signal", "Bas (Aucun signal)");
                add("gui.marketblocks.io.redstone_control.require_signal", "Haut (Nécessite un signal)");
                add("gui.marketblocks.io.redstone_control.tooltip", "Mode de contrôle de redstone pour ce côté.");
                add("gui.marketblocks.settings_owner_only", "Seul le propriétaire peut modifier les paramètres");
                add("gui.marketblocks.settings_tab", "Afficher les paramètres");
                add("gui.marketblocks.settings_title", "Paramètres de la boutique");
                add("gui.marketblocks.toggle.off", "OFF");
                add("gui.marketblocks.toggle.on", "ON");
                add("gui.marketblocks.settings.category.access", "Accès");
                add("gui.marketblocks.settings.category.general", "Général");
                add("gui.marketblocks.settings.category.io", "I/O");
                add("gui.marketblocks.settings.category.notifications", "Notifications");
                add("gui.marketblocks.settings.category.villager", "PNJ");
                add("gui.marketblocks.settings.category.visual", "Visuel");
                add("gui.marketblocks.visuals.bobbing", "Flottement");
                add("gui.marketblocks.visuals.chaos_rotation", "Rotation chaotique");
                add("gui.marketblocks.visuals.count", "Nombre d'objets");
                add("gui.marketblocks.visuals.dynamic_fill_level", "Remplissage dynamique");
                add("gui.marketblocks.visuals.error.no_surface", "Aucune surface de stand derrière la boutique !");
                add("gui.marketblocks.visuals.error.space_blocked", "Espace bloqué !");
                add("gui.marketblocks.visuals.height", "Hauteur");
                add("gui.marketblocks.visuals.layout_mode", "Mode d'agencement");
                add("gui.marketblocks.visuals.layout_mode.gestapelt", "Empilé");
                add("gui.marketblocks.visuals.layout_mode.lose", "En vrac");
                add("gui.marketblocks.visuals.npc_enabled", "Villageois décoratif");
                add("gui.marketblocks.visuals.npc_name", "Nom du PNJ");
                add("gui.marketblocks.visuals.offer_item_disabled_global", "Désactivé par l'administrateur du serveur.");
                add("gui.marketblocks.visuals.offer_item_fullbright", "Luminescence");
                add("gui.marketblocks.visuals.offer_item_fullbright.tooltip", "Fait briller l'objet dans l'obscurité (pas d'ombres).");
                add("gui.marketblocks.visuals.offer_item_visible", "Objet d'offre visible");
                add("gui.marketblocks.visuals.offer_item_visible.tooltip", "Affiche ou masque l'objet d'offre flottant/exposé.");
                add("gui.marketblocks.visuals.payment_sounds", "Sons de paiement");
                add("gui.marketblocks.visuals.player_skin_name", "Nom du joueur");
                add("gui.marketblocks.visuals.profession", "Profession");
                add("gui.marketblocks.visuals.profession.armorer", "Armurier");
                add("gui.marketblocks.visuals.profession.butcher", "Boucher");
                add("gui.marketblocks.visuals.profession.cartographer", "Cartographe");
                add("gui.marketblocks.visuals.profession.cleric", "Prêtre");
                add("gui.marketblocks.visuals.profession.farmer", "Fermier");
                add("gui.marketblocks.visuals.profession.fisherman", "Pêcheur");
                add("gui.marketblocks.visuals.profession.fletcher", "Fléchier");
                add("gui.marketblocks.visuals.profession.leatherworker", "Tanneur");
                add("gui.marketblocks.visuals.profession.librarian", "Bibliothécaire");
                add("gui.marketblocks.visuals.profession.mason", "Maçon");
                add("gui.marketblocks.visuals.profession.nitwit", "Idiot");
                add("gui.marketblocks.visuals.profession.none", "Sans emploi");
                add("gui.marketblocks.visuals.profession.shepherd", "Berger");
                add("gui.marketblocks.visuals.profession.toolsmith", "Forgeron d'outils");
                add("gui.marketblocks.visuals.profession.weaponsmith", "Forgeron d'armes");
                add("gui.marketblocks.visuals.purchase_particles", "Particules d'achat");
                add("gui.marketblocks.visuals.purchase_sounds", "Sons d'achat");
                add("gui.marketblocks.visuals.rotation", "Rotation");
                add("gui.marketblocks.visuals.rotation_x", "Rotation X");
                add("gui.marketblocks.visuals.rotation_y", "Rotation Y");
                add("gui.marketblocks.visuals.rotation_z", "Rotation Z");
                add("gui.marketblocks.visuals.scale", "Échelle");
                add("gui.marketblocks.visuals.spacing_xz", "Espacement X/Z");
                add("gui.marketblocks.visuals.spacing_y", "Espacement Y");
                add("gui.marketblocks.visuals.speed", "Vitesse");
                add("gui.marketblocks.visuals.use_player_skin", "Apparence de joueur");
                add("gui.marketblocks.visuals.use_player_skin.tooltip", "Affiche un joueur au lieu d'un villageois.");

                // === GUI - Notifications ===
                add("gui.marketblocks.notifications.co_owners", "Informer les copropriétaires");
                add("gui.marketblocks.notifications.co_owners.tooltip", "Envoie également les notifications aux propriétaires supplémentaires.");
                add("gui.marketblocks.notifications.out_of_stock", "Alerte de rupture de stock");
                add("gui.marketblocks.notifications.out_of_stock.tooltip", "Être averti lorsque la boutique n'a plus d'articles.");
                add("gui.marketblocks.notifications.output_full", "Alerte d'inventaire plein");
                add("gui.marketblocks.notifications.output_full.tooltip", "Être averti lorsque l'inventaire de sortie de la boutique est plein.");
                add("gui.marketblocks.notifications.purchase", "Notifications d'achat");
                add("gui.marketblocks.notifications.purchase.tooltip", "Recevoir une notification dans le chat lorsque quelqu'un achète dans votre boutique.");
                add("message.marketblocks.notifications.out_of_stock", "§cVotre boutique est en rupture de stock !§r");
                add("message.marketblocks.notifications.output_full", "§cL'inventaire de sortie de votre boutique est plein !§r");
                add("message.marketblocks.notifications.purchase", "§a%s a acheté %sx %s dans votre boutique.§r");

                // === GUI - Marketplace Editors ===
                add("gui.marketblocks.marketplace.editor.limits.daily", "Limite journalière");
                add("gui.marketblocks.marketplace.editor.limits.restock", "Réapprovisionnement (s)");
                add("gui.marketblocks.marketplace.editor.limits.stock", "Limite de stock");
                add("gui.marketblocks.marketplace.editor.limits.title", "Limites");
                add("gui.marketblocks.marketplace.editor.limits.daily.tooltip", "Nombre maximum d'achats qu'un joueur peut effectuer par jour.");
                add("gui.marketblocks.marketplace.editor.limits.stock.tooltip", "Stock total disponible pour cette offre.");
                add("gui.marketblocks.marketplace.editor.limits.restock.tooltip", "Temps en secondes avant que le stock ne se reconstitue.");
                add("gui.marketblocks.marketplace.editor.pricing.disabled", "Tarification OFF");
                add("gui.marketblocks.marketplace.editor.pricing.enabled", "Tarification ON");
                add("gui.marketblocks.marketplace.editor.pricing.label", "Activer la tarification");
                add("gui.marketblocks.marketplace.editor.pricing.label.tooltip", "Si activée, le prix s'ajuste dynamiquement en fonction de la température du marché.");
                add("gui.marketblocks.marketplace.editor.pricing.max", "Prix max (%)");
                add("gui.marketblocks.marketplace.editor.pricing.max.tooltip", "Pourcentage maximum que le prix peut atteindre (ex. 200 = prix doublé).");
                add("gui.marketblocks.marketplace.editor.pricing.min", "Prix min (%)");
                add("gui.marketblocks.marketplace.editor.pricing.min.tooltip", "Pourcentage minimum auquel le prix peut baisser (ex. 50 = moitié prix).");
                add("gui.marketblocks.marketplace.editor.pricing.volatility", "Volatilité");
                add("gui.marketblocks.marketplace.editor.pricing.volatility.tooltip", "Vitesse à laquelle le prix réagit aux achats et au temps.");
                add("gui.marketblocks.marketplace.editor.pricing.volatility.slow", "Lente");
                add("gui.marketblocks.marketplace.editor.pricing.volatility.normal", "Normale");
                add("gui.marketblocks.marketplace.editor.pricing.volatility.fast", "Rapide");
                add("gui.marketblocks.marketplace.editor.pricing.title", "Tarification");

                // === GUI - Marketplace Elements ===
                add("gui.marketblocks.marketplace.add_offer", "Ajouter une offre");
                add("gui.marketblocks.marketplace.add_page", "Ajouter une page");
                add("gui.marketblocks.marketplace.delete_offer", "Supprimer l'offre");
                add("gui.marketblocks.marketplace.delete_page", "Supprimer la page");
                add("gui.marketblocks.marketplace.inline.limits", "Modifier les limites");
                add("gui.marketblocks.marketplace.inline.pricing", "Modifier la tarification");
                add("gui.marketblocks.marketplace.inline.pricing.disabled_global", "Désactivé : La tarification globale est active dans la configuration du serveur");
                add("gui.marketblocks.marketplace.mode.edit", "Passer en mode édition");
                add("gui.marketblocks.marketplace.mode.view", "Passer en mode consultation");
                add("gui.marketblocks.marketplace.move_offer", "Déplacer l'offre");
                add("gui.marketblocks.marketplace.move_offer_down", "Déplacer l'offre vers le bas");
                add("gui.marketblocks.marketplace.move_offer_up", "Déplacer l'offre vers le haut");
                add("gui.marketblocks.marketplace.no_offers", "Aucune offre");
                add("gui.marketblocks.marketplace.no_pages", "Aucune page\ndisponible");
                add("gui.marketblocks.marketplace.rename_page", "Renommer la page");
                add("gui.marketblocks.marketplace.status.daily_short", "J : %s");
                add("gui.marketblocks.marketplace.status.price_short", "x%s");
                add("gui.marketblocks.marketplace.status.restock_short", "R : %s");
                add("gui.marketblocks.marketplace.status.stock_short", "S : %s");
                add("gui.marketblocks.marketplace.tooltip.price_multiplier", "Facteur de prix.");
                add("gui.marketblocks.marketplace.tooltip.remaining_daily", "Vos achats restants aujourd'hui.");
                add("gui.marketblocks.marketplace.tooltip.remaining_daily_empty", "Limite journalière atteinte.");
                add("gui.marketblocks.marketplace.tooltip.remaining_stock", "Achats en stock restants.");
                add("gui.marketblocks.marketplace.tooltip.remaining_stock_empty", "En rupture de stock.");
                add("gui.marketblocks.marketplace.tooltip.restock_in", "Temps avant réapprovisionnement.");
                add("gui.marketblocks.marketplace.tooltip.restock_ready", "Réapprovisionnement en cours.");
                add("gui.marketblocks.marketplace.tooltip.unavailable_daily", "Indisponible : limite journalière atteinte.");
                add("gui.marketblocks.marketplace.tooltip.unavailable_generic", "Indisponible.");
                add("gui.marketblocks.marketplace.tooltip.unavailable_restock", "Indisponible : réapprovisionnement en cours.");
                add("gui.marketblocks.marketplace.tooltip.unavailable_stock", "Indisponible : en rupture de stock.");
                add("gui.marketblocks.marketplace.unnamed_page", "Page %s");

                // === GUI - General ===
                add("gui.marketblocks.admin_shop.disabled", "Admin-Shop : OFF");
                add("gui.marketblocks.admin_shop.enabled", "Admin-Shop : ON");
                add("gui.marketblocks.category", "Catégorie");
                add("gui.marketblocks.category.none", "Aucune");
                add("gui.marketblocks.category.weapons_armor", "Armes et Armures");
                add("gui.marketblocks.category.tools", "Outils");
                add("gui.marketblocks.category.blocks", "Blocs");
                add("gui.marketblocks.category.food_potions", "Nourriture et Potions");
                add("gui.marketblocks.category.valuables", "Objets de valeur");
                add("gui.marketblocks.category.misc", "Divers");
                add("gui.marketblocks.category.tooltip", "Catégorie sous laquelle répertorier la boutique dans l'annuaire global");
                add("gui.marketblocks.create_offer", "Créer une offre");
                add("gui.marketblocks.delete_offer", "Supprimer l'offre");
                add("gui.marketblocks.disabled", "Désactivé");
                add("gui.marketblocks.emit_redstone", "Émettre de la redstone");
                add("gui.marketblocks.emit_redstone.tooltip", "Émet une courte impulsion de redstone après un achat");
                add("gui.marketblocks.error.invalid_offer", "Configuration d'offre invalide");
                add("gui.marketblocks.error.no_payment_items", "Veuillez placer au moins un objet de paiement");
                add("gui.marketblocks.error.no_result_item", "Veuillez placer un objet dans l'emplacement de résultat");
                add("gui.marketblocks.input", "Entrée");
                add("gui.marketblocks.inventory_admin_disabled", "Inventaire désactivé en mode admin");
                add("gui.marketblocks.inventory_owner_only", "Seul le propriétaire peut gérer l'inventaire");
                add("gui.marketblocks.inventory_tab", "Afficher l'inventaire");
                add("gui.marketblocks.inventory_title", "Inventaire du stand");
                add("gui.marketblocks.log_tab", "Afficher le journal");
                add("gui.marketblocks.log_title", "Journal des transactions");
                add("gui.marketblocks.log.clear", "Effacer le journal");
                add("gui.marketblocks.log.count", "Entrées : %s");
                add("gui.marketblocks.log.empty", "Aucune transaction pour le moment");
                add("gui.marketblocks.log.none", "Aucun");
                add("gui.marketblocks.log.time.days", "il y a %s j");
                add("gui.marketblocks.log.time.hours", "il y a %s h");
                add("gui.marketblocks.log.time.just_now", "À l'instant");
                add("gui.marketblocks.log.time.minutes", "il y a %s min");
                add("gui.marketblocks.log.time.seconds", "il y a %s s");
                add("gui.marketblocks.mode.edit_active", "MODE ÉDITION");
                add("gui.marketblocks.no_players_available", "Aucun joueur disponible");
                add("gui.marketblocks.offers", "Offres");
                add("gui.marketblocks.offers_tab", "Afficher les offres");
                add("gui.marketblocks.out_of_stock", "Rupture de stock");
                add("gui.marketblocks.output", "Sortie");
                add("gui.marketblocks.output_full", "Sortie pleine");
                add("gui.marketblocks.owner", "Propriétaire : %s");
                add("gui.marketblocks.purchase_xp_sound", "Son d'XP à l'achat");
                add("gui.marketblocks.purchase_xp_sound.tooltip", "Joue un son d'orbe d'XP lorsqu'un joueur achète quelque chose");
                add("gui.marketblocks.save", "Enregistrer");
                add("gui.marketblocks.shop_closed", "Boutique en pause");
                add("gui.marketblocks.shop_closed.tooltip", "Si actif, seuls les propriétaires peuvent acheter des objets.");
                add("gui.marketblocks.shop_name", "Nom de la boutique");
                add("gui.marketblocks.shop_title", "Stand de commerce");
                add("gui.marketblocks.side.back", "Arrière");
                add("gui.marketblocks.side.bottom", "Bas");
                add("gui.marketblocks.side.left", "Gauche");
                add("gui.marketblocks.side.right", "Droite");

                // === Messages & Chat ===
                add("message.marketblocks.marketplace.daily_limit_reached", "La limite journalière pour cette offre a été atteinte.");
                add("message.marketblocks.marketplace.edit_mode_disabled", "Mode édition de la place du marché désactivé.");
                add("message.marketblocks.marketplace.edit_mode_enabled", "Mode édition de la place du marché activé.");
                add("message.marketblocks.marketplace.limits.invalid_data", "Impossible d'enregistrer les limites : données invalides.");
                add("message.marketblocks.marketplace.limits.invalid_positive_int", "Veuillez entrer uniquement des nombres entiers positifs pour les limites.");
                add("message.marketblocks.marketplace.limits.no_connection", "Impossible d'enregistrer les limites : aucune connexion au serveur.");
                add("message.marketblocks.marketplace.page_name_blank", "Le nom de la page ne doit pas être vide.");
                add("message.marketblocks.marketplace.page_name_duplicate", "Une page nommée '%s' existe déjà.");
                add("message.marketblocks.marketplace.page_name_too_long", "Le nom de la page doit contenir au maximum %s caractères.");
                add("message.marketblocks.marketplace.page_not_found", "La page de boutique sélectionnée est introuvable.");
                add("message.marketblocks.marketplace.pricing.invalid_data", "Impossible d'enregistrer la tarification : données invalides.");
                add("message.marketblocks.marketplace.pricing.invalid_finite", "Veuillez entrer des nombres finis valides pour la tarification.");
                add("message.marketblocks.marketplace.pricing.invalid_number_format", "Veuillez utiliser uniquement des chiffres (point ou virgule autorisés).");
                add("message.marketblocks.marketplace.pricing.no_connection", "Impossible d'enregistrer la tarification : aucune connexion au serveur.");
                add("message.marketblocks.trade_stand.no_offer", "Ce stand de commerce n'a actuellement aucune offre active.");
                add("message.marketblocks.trade_stand.not_owner", "Seul le propriétaire peut détruire ce stand de commerce.");
                add("message.marketblocks.trade_stand.break_not_empty", "Vous devez d'abord vider tous les objets et les paiements !");
                add("message.marketblocks.shop.limit_reached", "Vous pouvez placer au maximum %s boutiques !");
                
                add("message.marketblocks.shop_buyer.interact.1", "§eJe cherche de bons articles à acheter !§r");
                add("message.marketblocks.shop_buyer.interact.2", "§eAvez-vous quelque chose d'intéressant à vendre ?§r");
                add("message.marketblocks.shop_buyer.interact.3", "§eJe voyage pour faire des affaires. Vous avez peut-être ce qu'il me faut !§r");
                add("message.marketblocks.shop_buyer.interact.4", "§eJe viens de trouver une super affaire ! J'adore faire des achats ici !§r");
                add("message.marketblocks.shop_buyer.interact.5", "§eEncore un bon achat ! Mon sac commence à peser lourd.§r");
                add("message.marketblocks.shop_buyer.interact.6", "§eJ'ai trouvé tout ce dont j'avais besoin grâce à ces boutiques !§r");
                add("message.marketblocks.shop_buyer.interact.7", "§eHmm, je cherche quelque chose de spécifique...§r");
                add("message.marketblocks.shop_buyer.interact.8", "§eJe me demande quelles autres boutiques se trouvent dans les environs...§r");
                add("message.marketblocks.shop_buyer.interact.9", "§eJe ne fais que regarder pour l'instant. Rien n'a encore retenu mon attention.§r");
                add("message.marketblocks.shop_buyer.interact.10", "§eJolie boutique que vous avez là ! Je la garderai en tête.§r");

                // === Jade / Waila Support ===
                add("config.jade.plugin_marketblocks.shop_info", "Infos de la boutique");
                add("marketblocks.jade.for", "Pour :");
                add("marketblocks.jade.out_of_stock", "Rupture de stock !");
                add("marketblocks.jade.output_full", "Inventaire plein !");
                add("marketblocks.jade.owner", "Propriétaire : %s");
                add("marketblocks.jade.shop", "Boutique : %s");
                add("marketblocks.jade.selling", "En vente :");
                add("marketblocks.jade.status.admin_shop", "Admin Shop");
                add("marketblocks.jade.status.closed", "Boutique fermée");
                add("marketblocks.jade.trader.budget", "Budget : %s");

                // === Advancements ===
                add("advancements.marketblocks.admin_shop.description", "Activer le mode admin shop");
                add("advancements.marketblocks.admin_shop.title", "Biens infinis");
                add("advancements.marketblocks.auto_io.description", "Activer l'entrée/sortie automatique pour votre boutique");
                add("advancements.marketblocks.auto_io.title", "Logistique");
                add("advancements.marketblocks.custom_npc.description", "Personnaliser le PNJ de votre boutique avec un nom ou une apparence de joueur");
                add("advancements.marketblocks.custom_npc.title", "Personnel sur mesure");
                add("advancements.marketblocks.first_shop.description", "Placer votre premier bloc de boutique MarketBlocks");
                add("advancements.marketblocks.first_shop.title", "Ouvert aux affaires");
                add("advancements.marketblocks.hiring.description", "Activer un PNJ pour votre boutique");
                add("advancements.marketblocks.hiring.title", "Recrutement en cours");
                add("advancements.marketblocks.joint_venture.description", "Ajouter un copropriétaire à votre boutique");
                add("advancements.marketblocks.joint_venture.title", "Coentreprise");
                add("advancements.marketblocks.marketplace_buy.description", "Acheter un objet via la place du marché");
                add("advancements.marketblocks.marketplace_buy.title", "Client du centre commercial");
                add("advancements.marketblocks.out_of_stock.description", "Avoir une boutique (non-admin) en rupture de stock");
                add("advancements.marketblocks.out_of_stock.title", "Rupture de stock");
                add("advancements.marketblocks.redstone.description", "Activer la sortie redstone ou les I/O contrôlés par redstone");
                add("advancements.marketblocks.redstone.title", "Logique Redstone");
                add("advancements.marketblocks.root.description", "Obtenir un bloc de boutique MarketBlocks");
                add("advancements.marketblocks.root.title", "MarketBlocks");
                add("advancements.marketblocks.showcase.description", "Ajouter une vitrine en verre à un stand de commerce");
                add("advancements.marketblocks.showcase.title", "Exposition");
                add("advancements.marketblocks.sold_item.description", "Vendre votre premier objet à un autre joueur");
                add("advancements.marketblocks.sold_item.title", "Première vente !");
                add("advancements.marketblocks.tycoon.description", "Vendre 100 objets via vos boutiques");
                add("advancements.marketblocks.tycoon.title", "Magnat");
                add("advancements.marketblocks.wall_street.description", "Ouvrir la place du marché");
                add("advancements.marketblocks.wall_street.title", "Wall Street");
                add("advancements.marketblocks.wholesaler.description", "Acheter 64 objets ou plus en une seule transaction");
                add("advancements.marketblocks.wholesaler.title", "Grossiste");

                // === Subtitles ===
                add("subtitles.marketblocks.visual_npc_fall", "Le villageois atterrit");

                // === Login Notifications ===
                add("gui.marketblocks.notifications.login.out_of_stock", "§c[MarketBlocks] %s de vos boutiques sont en rupture de stock !§r");
                add("gui.marketblocks.notifications.login.output_full", "§c[MarketBlocks] %s de vos boutiques ont un espace de stockage de sortie plein !§r");
                add("gui.marketblocks.notifications.login.coordinate", "§7 - Localisation : X : %s, Y : %s, Z : %s§r");

                // === Purchase Confirmations ===
                add("message.marketblocks.purchase_success", "Vous avez acheté avec succès %s x %s.");
                add("message.marketblocks.purchase_success.global", "%s a acheté %s x %s.");

                // === Admin Commands ===
                add("command.marketblocks.trader.value.set", "Valeur de %s définie sur %s.");
                add("command.marketblocks.trader.value.remove", "Valeur supprimée pour %s.");
                add("command.marketblocks.trader.blacklist.add", "Ajout de %s à la liste noire.");
                add("command.marketblocks.trader.blacklist.remove", "Suppression de %s de la liste noire.");
                add("command.marketblocks.sale.set.success", "Soldes activés pour [%s] : Variation de prix %s (Durée : %s min)");
                add("command.marketblocks.sale.remove.success", "Soldes terminés pour [%s].");
                add("command.marketblocks.sale.not_found", "Offre / Boutique introuvable : %s");
                add("command.marketblocks.sale.failed", "Échec de la modification des soldes.");
                add("command.marketblocks.stats.shop.header", "--- Top 10 SingleOfferShops ---");
                add("command.marketblocks.stats.shop.empty", "Aucune boutique disponible.");
                add("command.marketblocks.stats.shop.unnamed", "Sans nom");
                add("command.marketblocks.stats.shop.entry", "%s. %s - %s Ventes");
                add("command.marketblocks.stats.marketplace.header", "--- Top 10 Offres Marketplace ---");
                add("command.marketblocks.stats.marketplace.empty", "Aucune offre disponible.");
                add("command.marketblocks.stats.marketplace.entry", "%s. %s - %s Ventes");

                // === Block Registrations ===
                addBlock(RegistriesInit.MARKETCRATE_BLOCK, "Caisse du marché");
                addBlock(RegistriesInit.TRADE_STAND_BLOCK, "Stand de commerce");

        }
}
