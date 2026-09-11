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
                addItem(RegistriesInit.TRADE_BOOK, "Livre de commerce");

                // === Containers & Menus ===
                add("container.marketblocks.trade_stand", "Stand de commerce");
                add("menu.marketblocks.marketplace", "Place du marché");

                // === Keybinds ===
                add("key.categories.marketblocks", "MarketBlocks");
                add("key.marketblocks.open_marketplace", "Ouvrir la place du marché");

                // === Commands ===
                add("command.marketblocks.break.denied", "\u00a7cVous ne pouvez pas détruire ce bloc tant qu'il est lié à une place du marché !");
                add("command.marketblocks.break.unlinked", "\u00a7eLe bloc a été délié de la place du marché.");
                add("command.marketblocks.link.already_linked", "\u00a7cCe bloc est déjà lié à une place du marché.");
                add("command.marketblocks.link.not_looking_at_block", "\u00a7cVous devez regarder un bloc pour le lier.");
                add("command.marketblocks.link.success", "\u00a7aBloc lié à la place du marché avec succès !");
                add("command.marketblocks.list.click_to_delete", "Cliquez pour supprimer");
                add("command.marketblocks.list.click_to_teleport", "Cliquez pour vous téléporter");
                add("command.marketblocks.list.click_to_waypoint", "Cliquez pour obtenir les liens Waypoint dans le chat");
                add("command.marketblocks.list.delete", "[Supprimer]");
                add("command.marketblocks.list.tp", "[Téléportation]");
                add("command.marketblocks.list.waypoint", "[Waypoint]");
                add("command.marketblocks.list.page_header", "\u00a78======== \u00a76\u00a7lBoutiques (Page %s/%s) \u00a78========");
                add("command.marketblocks.list.prev", "[< Préc]");
                add("command.marketblocks.list.next", "[Suiv >]");
                add("command.marketblocks.marketplacelist.entry", "\u00a78\u25aa \u00a76Place du marché \u00a7e%s");
                add("command.marketblocks.marketplacelist.page_header", "\u00a78======== \u00a76\u00a7lPlaces du marché (Page %s/%s) \u00a78========");
                add("command.marketblocks.marketplacelist.no_links", "\u00a7cAucune place du marché trouvée.");
                add("command.marketblocks.player_not_found", "\u00a7cJoueur introuvable");
                add("command.marketblocks.reload.success", "\u00a7aConfiguration de MarketBlocks rechargée avec succès !");
                add("command.marketblocks.resetlimits.no_changes", "\u00a7eAucune limite journalière n'a été réinitialisée.");
                add("command.marketblocks.resetlimits.success", "\u00a7aLimites journalières réinitialisées avec succès.");
                add("command.marketblocks.search.header", "\u00a78======== \u00a76\u00a7lBoutiques vendant %s (Page %s/%s) \u00a78========");
                add("command.marketblocks.search.no_shops", "\u00a7cAucune boutique ni place du marché trouvée vendant %s.");
                add("command.marketblocks.shoplist.closed", "FERMÉ");
                add("command.marketblocks.shoplist.entry", "\u00a78\u25aa \u00a77[%s\u00a77] \u00a7e%s \u00a78(par \u00a77%s\u00a78)");
                add("command.marketblocks.shoplist.header", "\u00a78======== \u00a76\u00a7lBoutiques MarketBlocks \u00a78========");
                add("command.marketblocks.shoplist.no_shops", "\u00a7cAucune boutique disponible.");
                add("command.marketblocks.shoplist.open", "OUVERT");
                add("command.marketblocks.shoplist.hover.shop", "Boutique : %s");
                add("command.marketblocks.shoplist.hover.owner", "Propriétaire : %s");
                add("command.marketblocks.shoplist.hover.status", "Statut : %s");
                add("command.marketblocks.shoplist.hover.offer", "Offre :");
                add("command.marketblocks.shoplist.hover.arrow", "\u2794");
                add("command.marketblocks.unlink.not_found", "\u00a7cImpossible de trouver le lien de la place du marché.");
                add("command.marketblocks.unlink.not_linked", "\u00a7cCe bloc n'est pas lié.");
                add("command.marketblocks.unlink.not_looking_at_block", "\u00a7cVous devez regarder un bloc pour le délier.");
                add("command.marketblocks.unlink.success", "\u00a7aBloc délié avec succès !");
                add("command.marketblocks.unlink.success_name", "\u00a7aDélié de la place du marché : \u00a7e%s");
                add("command.marketblocks.waypoint.created", "\u00a7aLiens de partage Waypoint créés :");
                add("command.marketblocks.internal.waypoint.coords", "Coordonnées du waypoint pour %s : X: %d, Y: %d, Z: %d (%s)");
                add("command.marketblocks.internal.waypoint.journeymap", "Waypoint JourneyMap créé / lien généré.");
                add("command.marketblocks.internal.waypoint.xaero", "Lien de waypoint Xaero's Minimap :");
                add("command.marketblocks.internal.tp.no_permission", "Vous n'avez pas la permission de vous téléporter aux boutiques.");
                add("command.marketblocks.internal.tp.invalid_dimension", "La dimension cible est introuvable.");
                add("command.marketblocks.internal.tp.success", "Téléporté à la boutique !");

                // === GUI - Settings ===
                add("gui.marketblocks.access.edit_access_list", "LISTE ACCÈS");
                add("gui.marketblocks.access.edit_access_list.tooltip", "Gérer la liste d'accès des clients");
                add("gui.marketblocks.access.edit_owners", "CO-PROPRIÉT.");
                add("gui.marketblocks.access.edit_owners.tooltip", "Gérer les copropriétaires de la boutique");
                add("gui.marketblocks.access.group.management", "GESTION");
                add("gui.marketblocks.access.group.players", "LISTE DES JOUEURS");
                add("gui.marketblocks.access.group.whitelist", "JOUEURS WHITELIST");
                add("gui.marketblocks.access.group.blacklist", "JOUEURS BLACKLIST");
                add("gui.marketblocks.access.counter", "%d / %d");
                add("gui.marketblocks.access.filter_whitelist", "MODE : WHITELIST");
                add("gui.marketblocks.access.filter_blacklist", "MODE : BLACKLIST");
                add("gui.marketblocks.access.filter_whitelist.tooltip", "Mode Whitelist : Seuls les joueurs listés peuvent acheter");
                add("gui.marketblocks.access.filter_blacklist.tooltip", "Mode Blacklist : Les joueurs listés ne peuvent pas acheter");
                add("gui.marketblocks.access.primary_owner_only", "Disponible uniquement pour le propriétaire principal");
                add("gui.marketblocks.access.mode.blacklist", "Mode : Liste noire");
                add("gui.marketblocks.access.mode.everyone", "Mode : Tout le monde");
                add("gui.marketblocks.access.mode.whitelist", "Mode : Liste blanche");
                add("gui.marketblocks.io.status_label", "E/S :");
                add("gui.marketblocks.io.master_toggle.tooltip", "Activer / désactiver les E/S et entonnoirs");
                add("gui.marketblocks.io.group.sides", "FACES DU BLOC");
                add("gui.marketblocks.io.group.automation", "AUTOMATISATION & REDSTONE");
                add("gui.marketblocks.io.allow_io", "Autoriser I/O");
                add("gui.marketblocks.io.allow_io.tooltip", "Interaction avec entonnoirs et tuyaux");
                add("gui.marketblocks.io.auto_io", "Transfert de coffre (Auto-E/S) :");
                add("gui.marketblocks.io.auto_io.tooltip", "Transfert automatique avec les coffres adjacents");
                add("gui.marketblocks.io.redstone_control.ignored", "Redstone : Ignoré");
                add("gui.marketblocks.io.redstone_control.require_no_signal", "Redstone : Sans signal");
                add("gui.marketblocks.io.redstone_control.require_signal", "Redstone : Signal requis");
                add("gui.marketblocks.io.redstone_control.tooltip", "Condition redstone pour l'E/S");
                add("gui.marketblocks.legend.input", "ENTRÉE");
                add("gui.marketblocks.legend.output", "SORTIE");
                add("gui.marketblocks.legend.disabled", "DÉSACTIVÉ");
                add("gui.marketblocks.settings_owner_only", "Seul le propriétaire peut modifier les paramètres");
                add("gui.marketblocks.settings_tab", "Paramètres");
                add("gui.marketblocks.settings_title", "Paramètres de la boutique");
                add("gui.marketblocks.toggle.off", "OFF");
                add("gui.marketblocks.toggle.on", "ON");
                add("gui.marketblocks.settings.category.access", "Accès");
                add("gui.marketblocks.settings.category.access.title", "Paramètres d'accès");
                add("gui.marketblocks.settings.category.general", "Général");
                add("gui.marketblocks.settings.category.general.title", "Paramètres généraux");
                add("gui.marketblocks.settings.category.io", "I/O");
                add("gui.marketblocks.settings.category.io.title", "Paramètres I/O");
                add("gui.marketblocks.settings.category.notifications", "Notifications");
                add("gui.marketblocks.settings.category.notifications.title", "Paramètres de notification");
                add("gui.marketblocks.settings.category.villager", "PNJ");
                add("gui.marketblocks.settings.category.villager.title", "Paramètres PNJ");
                add("gui.marketblocks.settings.category.visual", "Visuel");
                add("gui.marketblocks.settings.category.visual.title", "Paramètres visuels");
                add("gui.marketblocks.settings.reset", "Restaurer les paramètres par défaut");
                add("gui.marketblocks.visuals.bobbing", "FLOTTEMENT :");
                add("gui.marketblocks.visuals.bobbing.tooltip", "Léger mouvement de flottement");
                add("gui.marketblocks.visuals.chaos_rotation", "ROTATION CHAOS :");
                add("gui.marketblocks.visuals.chaos_rotation.tooltip", "Variation aléatoire de rotation");
                add("gui.marketblocks.visuals.count", "Nombre d'objets");
                add("gui.marketblocks.visuals.count.tooltip", "Nombre d'objets visibles (1–64)");
                add("gui.marketblocks.visuals.count_short", "QTÉ :");
                add("gui.marketblocks.visuals.display", "AFFICHAGE :");
                add("gui.marketblocks.visuals.group.item_arrangement", "DISPOSITION DES OBJETS");
                add("gui.marketblocks.visuals.group.visuals_transformations", "VISUELS ET TRANSFORMATIONS");
                add("gui.marketblocks.visuals.dynamic_fill_level", "REMPL. DYN. :");
                add("gui.marketblocks.visuals.dynamic_fill_level.tooltip", "Adapter l'affichage au stock");
                add("gui.marketblocks.visuals.error.no_surface", "Aucune surface de stand derrière la boutique !");
                add("gui.marketblocks.visuals.error.space_blocked", "Espace bloqué !");
                add("gui.marketblocks.visuals.height", "HAUTEUR :");
                add("gui.marketblocks.visuals.height.tooltip", "Hauteur de flottement au-dessus du bloc");
                add("gui.marketblocks.visuals.layout_mode", "Mode d'agencement");
                add("gui.marketblocks.visuals.layout_mode.tooltip", "Disposition : Empilé ou En vrac");
                add("gui.marketblocks.visuals.layout_mode.gestapelt", "Empilé");
                add("gui.marketblocks.visuals.layout_mode.lose", "En vrac");
                add("gui.marketblocks.visuals.npc_enabled", "Villageois décoratif");
                add("gui.marketblocks.visuals.npc_enabled.tooltip", "Afficher/masquer le PNJ décoratif");
                add("gui.marketblocks.visuals.npc_name", "Nom du PNJ");
                add("gui.marketblocks.visuals.npc_name.tooltip", "Nom affiché au-dessus du PNJ");
                add("gui.marketblocks.visuals.offer_item_disabled_global", "Désactivé par l'administrateur du serveur.");
                add("gui.marketblocks.visuals.offer_item_fullbright", "LUMINESCENCE :");
                add("gui.marketblocks.visuals.offer_item_fullbright.tooltip", "Fait briller l'objet dans le noir");
                add("gui.marketblocks.visuals.offer_item_visible", "Objet d'offre visible");
                add("gui.marketblocks.visuals.offer_item_visible.tooltip", "Afficher/masquer l'affichage d'objets");
                add("gui.marketblocks.visuals.payment_sounds", "SONS DE PAIEMENT :");
                add("gui.marketblocks.visuals.payment_sounds.tooltip", "Son de pièce lors du paiement");
                add("gui.marketblocks.visuals.purchase_particles", "PARTICULES D'ACHAT :");
                add("gui.marketblocks.visuals.purchase_particles.tooltip", "Particules d'émeraude lors de l'achat");
                add("gui.marketblocks.visuals.purchase_sounds", "SONS D'ACHAT :");
                add("gui.marketblocks.visuals.purchase_sounds.tooltip", "Son d'achat du villageois");
                add("gui.marketblocks.visuals.npc_short", "PNJ :");
                add("gui.marketblocks.visuals.npc_name_label", "NOM :");
                add("gui.marketblocks.visuals.rotation", "ROTATION :");
                add("gui.marketblocks.visuals.rotation.tooltip", "Rotation de base des objets");
                add("gui.marketblocks.visuals.rotation_x", "ROTATION X :");
                add("gui.marketblocks.visuals.rotation_y", "ROTATION Y :");
                add("gui.marketblocks.visuals.rotation_z", "ROTATION Z :");
                add("gui.marketblocks.visuals.scale", "ÉCHELLE :");
                add("gui.marketblocks.visuals.scale.tooltip", "Taille des objets");
                add("gui.marketblocks.visuals.spacing_xz", "ESPACEMENT X/Z :");
                add("gui.marketblocks.visuals.spacing_xz.tooltip", "Décalage horizontal");
                add("gui.marketblocks.visuals.spacing_y", "ESPACEMENT Y :");
                add("gui.marketblocks.visuals.spacing_y.tooltip", "Espacement vertical des objets");
                add("gui.marketblocks.visuals.speed", "VITESSE :");
                add("gui.marketblocks.visuals.speed.tooltip", "Vitesse de rotation");
                add("gui.marketblocks.visuals.use_player_skin_short", "SKIN :");
                add("gui.marketblocks.visuals.use_player_skin", "Apparence de joueur");
                add("gui.marketblocks.visuals.use_player_skin.tooltip", "Modèle de joueur au lieu du villageois");
                add("gui.marketblocks.visuals.group.npc_appearance", "APPARENCE");
                add("gui.marketblocks.visuals.group.npc_feedback", "EFFETS ET RETOURS");
                add("gui.marketblocks.visuals.npc_sounds", "Sons du PNJ");
                add("gui.marketblocks.visuals.npc_sounds.tooltip", "Détermine quand le PNJ émet des sons (achat, paiement ou désactivé).");
                add("gui.marketblocks.visuals.npc_sounds.all", "Tous");
                add("gui.marketblocks.visuals.npc_sounds.purchase", "Achat uniquement");
                add("gui.marketblocks.visuals.npc_sounds.payment", "Paiement uniquement");
                add("gui.marketblocks.visuals.npc_sounds.off", "Désactivé");
                add("gui.marketblocks.visuals.player_skin_name", "Nom du joueur");
                add("gui.marketblocks.visuals.player_skin_name.tooltip", "Skin du joueur spécifié");
                add("gui.marketblocks.visuals.profession", "Profession");
                add("gui.marketblocks.visuals.profession.tooltip", "Tenue et profession du PNJ");
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

                // === GUI - Notifications ===
                add("gui.marketblocks.notifications.group.trade_activity", "ACTIVITÉ COMMERCIALE");
                add("gui.marketblocks.notifications.group.status_warnings", "ALERTES D'ÉTAT");
                add("gui.marketblocks.notifications.co_owners", "INFORMER COPROPRIÉTAIRES :");
                add("gui.marketblocks.notifications.co_owners.tooltip", "Envoyer les notifications aux copropriétaires");
                add("gui.marketblocks.notifications.out_of_stock", "ALERTE DE RUPTURE :");
                add("gui.marketblocks.notifications.out_of_stock.tooltip", "Alerte lorsque la boutique est vide");
                add("gui.marketblocks.notifications.output_full", "ALERTE DE SORTIE PLEINE :");
                add("gui.marketblocks.notifications.output_full.tooltip", "Alerte lorsque la sortie est pleine");
                add("gui.marketblocks.notifications.purchase", "NOTIFICATIONS D'ACHAT :");
                add("gui.marketblocks.notifications.purchase.tooltip", "Notification de chat lors des achats");
                add("message.marketblocks.notifications.out_of_stock", "\u00a7cVotre boutique est en rupture de stock !\u00a7r");
                add("message.marketblocks.notifications.output_full", "\u00a7cL'inventaire de sortie de votre boutique est plein !\u00a7r");
                add("message.marketblocks.notifications.purchase", "\u00a7a%s a acheté %sx %s dans votre boutique.\u00a7r");

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
                add("gui.marketblocks.marketplace.editor.pricing.base", "Prix de base (%)");
                add("gui.marketblocks.marketplace.editor.pricing.base.tooltip", "Facteur de prix de base en pourcentage (ex. 100 = normal, 150 = +50%). Appliqué avant les ajustements de demande.");
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
                add("gui.marketblocks.marketplace.inline.limits.disabled_global", "Désactivé : Les limites globales sont actives dans la configuration du serveur");
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
                add("gui.marketblocks.marketplace.sidebar.next", "Page de catégories suivante");
                add("gui.marketblocks.marketplace.sidebar.prev", "Page de catégories précédente");
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
                add("gui.marketblocks.admin_shop.button.active", "MODE ADMIN : ACTIF");
                add("gui.marketblocks.admin_shop.button.inactive", "MODE ADMIN : DÉSACTIVÉ");
                add("gui.marketblocks.admin_shop.button.tooltip", "Bascule le mode Admin-Shop. En mode Admin, le magasin a un stock infini et ne nécessite pas de coffre.");
                add("gui.marketblocks.admin_shop.badge", "★ BOUTIQUE ADMIN");
                add("gui.marketblocks.admin_shop.badge.tooltip", "Boutique Admin active : Stock infini. Aucun objet n'est requis ou consommé dans l'inventaire.");
                add("gui.marketblocks.category", "Catégorie");
                add("gui.marketblocks.category.none", "Aucune");
                add("gui.marketblocks.category.weapons_armor", "Armes et Armures");
                add("gui.marketblocks.category.tools", "Outils");
                add("gui.marketblocks.category.blocks", "Blocs");
                add("gui.marketblocks.category.food_potions", "Nourriture et Potions");
                add("gui.marketblocks.category.valuables", "Objets de valeur");
                add("gui.marketblocks.category.misc", "Divers");
                add("gui.marketblocks.category.tooltip", "Catégorie dans le répertoire");
                add("gui.marketblocks.create_offer", "Créer une offre");
                add("gui.marketblocks.delete_offer", "Supprimer l'offre");
                add("gui.marketblocks.disabled", "Désactivé");
                add("gui.marketblocks.emit_redstone", "SIGNAL REDSTONE :");
                add("gui.marketblocks.emit_redstone.tooltip", "Brève impulsion redstone lors de l'achat");
                add("gui.marketblocks.general.status_label", "STATUT :");
                add("gui.marketblocks.general.status.active", "ACTIF");
                add("gui.marketblocks.general.status.paused", "PAUSE");
                add("gui.marketblocks.general.status.active.tooltip", "Boutique ouverte (Cliquer pour mettre en pause)");
                add("gui.marketblocks.general.status.paused.tooltip", "Boutique en pause (Cliquer pour ouvrir)");
                add("gui.marketblocks.general.group.shop_profile", "PROFIL DE BOUTIQUE");
                add("gui.marketblocks.general.group.features", "FONCTIONS & SIGNAUX");
                add("gui.marketblocks.general.shop_name_label", "NOM :");
                add("gui.marketblocks.error.invalid_offer", "Configuration d'offre invalide");
                add("gui.marketblocks.error.no_payment_items", "Veuillez placer au moins un objet de paiement");
                add("gui.marketblocks.error.no_result_item", "Veuillez placer un objet dans l'emplacement de résultat");
                add("gui.marketblocks.input", "Entrée");
                add("gui.marketblocks.inventory_admin_disabled", "Inventaire désactivé en mode admin");
                add("gui.marketblocks.inventory_owner_only", "Seul le propriétaire peut gérer l'inventaire");
                add("gui.marketblocks.inventory_tab", "Inventaire");
                add("gui.marketblocks.inventory_title", "Inventaire du stand");
                add("gui.marketblocks.log_tab", "Journal");
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
                add("gui.marketblocks.offers_tab", "Offres");
                add("gui.marketblocks.out_of_stock", "Rupture de stock");
                add("gui.marketblocks.output", "Sortie");
                add("gui.marketblocks.output_full", "Sortie pleine");
                add("gui.marketblocks.owner", "Propriétaire : %s");
                add("gui.marketblocks.purchase_sound", "SON D'ACHAT XP :");
                add("gui.marketblocks.purchase_sound.tooltip", "Effet sonore lors de l'achat");
                add("gui.marketblocks.purchase_xp_sound", "SON D'ACHAT XP :");
                add("gui.marketblocks.purchase_xp_sound.tooltip", "Effet sonore lors de l'achat");
                add("gui.marketblocks.save", "Enregistrer");
                add("gui.marketblocks.save.active.tooltip", "Enregistrer les modifications");
                add("gui.marketblocks.save.inactive.tooltip", "Aucune modification non enregistrée");
                add("gui.marketblocks.shop_closed", "Boutique en pause");
                add("gui.marketblocks.shop_closed.tooltip", "Si actif, la boutique est en pause et personne ne peut acheter d'objets.");
                add("gui.marketblocks.shop_name", "Nom de la boutique");
                add("gui.marketblocks.shop_name.tooltip", "Nom affiché de la boutique");
                add("gui.marketblocks.shop_title", "Stand de commerce");
                add("gui.marketblocks.side.back", "Arrière");
                add("gui.marketblocks.side.back.letter", "A");
                add("gui.marketblocks.side.bottom", "Bas");
                add("gui.marketblocks.side.bottom.letter", "B");
                add("gui.marketblocks.side.left", "Gauche");
                add("gui.marketblocks.side.left.letter", "G");
                add("gui.marketblocks.side.right", "Droite");
                add("gui.marketblocks.side.right.letter", "D");

                // === Messages & Chat ===
                add("message.marketblocks.marketplace.daily_limit_reached", "La limite journalière pour cette offre a été atteinte.");
                add("message.marketblocks.marketplace.edit_mode_disabled", "Mode édition de la place du marché désactivé.");
                add("message.marketblocks.marketplace.edit_mode_enabled", "Mode édition de la place du marché activé.");
                add("message.marketblocks.marketplace.limits.invalid_data", "Impossible d'enregistrer les limites : données invalides.");
                add("message.marketblocks.marketplace.limits.invalid_positive_int", "Veuillez entrer uniquement des nombres entiers positifs pour les limites.");
                add("message.marketblocks.marketplace.limits.no_connection", "Impossible d'enregistrer les limites : aucune connexion au serveur.");
                add("message.marketblocks.marketplace.page_limit_reached",
                                "Le nombre maximum de %s pages a été atteint.");
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
                
                add("message.marketblocks.shop_buyer.interact.1", "\u00a7eJe cherche de bons articles à acheter !\u00a7r");
                add("message.marketblocks.shop_buyer.interact.2", "\u00a7eAvez-vous quelque chose d'intéressant à vendre ?\u00a7r");
                add("message.marketblocks.shop_buyer.interact.3", "\u00a7eJe voyage pour faire des affaires. Vous avez peut-être ce qu'il me faut !\u00a7r");
                add("message.marketblocks.shop_buyer.interact.4", "\u00a7eJe viens de trouver une super affaire ! J'adore faire des achats ici !\u00a7r");
                add("message.marketblocks.shop_buyer.interact.5", "\u00a7eEncore un bon achat ! Mon sac commence à peser lourd.\u00a7r");
                add("message.marketblocks.shop_buyer.interact.6", "\u00a7eJ'ai trouvé tout ce dont j'avais besoin grâce à ces boutiques !\u00a7r");
                add("message.marketblocks.shop_buyer.interact.7", "\u00a7eHmm, je cherche quelque chose de spécifique...\u00a7r");
                add("message.marketblocks.shop_buyer.interact.8", "\u00a7eJe me demande quelles autres boutiques se trouvent dans les environs...\u00a7r");
                add("message.marketblocks.shop_buyer.interact.9", "\u00a7eJe ne fais que regarder pour l'instant. Rien n'a encore retenu mon attention.\u00a7r");
                add("message.marketblocks.shop_buyer.interact.10", "\u00a7eJolie boutique que vous avez là ! Je la garderai en tête.\u00a7r");

                // NPC Rank & Category display (G2)
                add("message.marketblocks.shop_buyer.info", "\u00a77[%s - %s]");
                add("entity.marketblocks.shop_buyer.rank.citizen", "Citoyen");
                add("entity.marketblocks.shop_buyer.rank.wealthy", "Marchand aisé");
                add("entity.marketblocks.shop_buyer.rank.noble", "Noble marchand");
                add("entity.marketblocks.shop_buyer.category.general", "Commerçant général");
                add("entity.marketblocks.shop_buyer.category.farmer", "Fermier");
                add("entity.marketblocks.shop_buyer.category.alchemist", "Alchimiste");
                add("entity.marketblocks.shop_buyer.category.blacksmith", "Forgeron");
                add("entity.marketblocks.shop_buyer.category.valuables", "Collectionneur");

                // === Jade / Waila Support ===
                add("config.jade.plugin_marketblocks.shop_info", "Infos de la boutique");
                add("config.jade.plugin_marketblocks.shop_buyer_info", "Infos du marchand");
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
                add("gui.marketblocks.notifications.login.out_of_stock", "\u00a7c[MarketBlocks] %s de vos boutiques sont en rupture de stock !\u00a7r");
                add("gui.marketblocks.notifications.login.output_full", "\u00a7c[MarketBlocks] %s de vos boutiques ont un espace de stockage de sortie plein !\u00a7r");
                add("gui.marketblocks.notifications.login.coordinate", "\u00a77 - Localisation : X : %s, Y : %s, Z : %s\u00a7r");

                // === Purchase Confirmations ===
                add("message.marketblocks.purchase_success", "Vous avez acheté avec succès %s x %s.");
                add("message.marketblocks.purchase_success.global", "%s a acheté %s x %s.");

                // === Admin Commands ===
                add("command.marketblocks.trader.value.set", "Valeur de %s définie sur %s.");
                add("command.marketblocks.trader.value.remove", "Valeur supprimée pour %s.");
                add("command.marketblocks.trader.blacklist.add", "Ajout de %s à la liste noire.");
                add("command.marketblocks.trader.blacklist.remove", "Suppression de %s de la liste noire.");
                add("command.marketblocks.sale.set.success", "Offre activée pour [%s] : Changement de prix %s (Durée : %s min)");
                add("command.marketblocks.sale.remove.success", "Offre terminée pour [%s].");
                add("command.marketblocks.sale.not_found", "Offre / Boutique introuvable : %s");
                add("command.marketblocks.sale.failed", "Échec de modification de l'offre.");
                add("command.marketblocks.stats.shop.header", "--- Top 10 SingleOfferShops ---");
                add("command.marketblocks.stats.shop", "Statistiques de la boutique : %s");
                add("command.marketblocks.stats.shop.empty", "Aucune boutique disponible.");
                add("command.marketblocks.stats.shop.entry", "%s. %s - %s Ventes");
                add("command.marketblocks.stats.shop.total_sales", "Ventes totales : %d");
                add("command.marketblocks.stats.marketplace.header", "--- Top 10 Offres du Marché ---");
                add("command.marketblocks.stats.marketplace.empty", "Aucune offre disponible.");
                add("command.marketblocks.stats.marketplace.entry", "%s. %s - %s Ventes");

                // === Trade Book GUI Translations ===
                add("gui.marketblocks.trade_book.title", "Livre de commerce");
                add("gui.marketblocks.trade_book.toc.header", "=== LIVRE DE COMMERCE ===\n\n");
                add("gui.marketblocks.trade_book.toc.subheader", "Rapport économique du marché.\n\n");
                add("gui.marketblocks.trade_book.toc.my_shops", "\ud83c\udfe0 Mes boutiques");
                add("gui.marketblocks.trade_book.toc.my_shops.tooltip", "Votre aperçu personnel des boutiques");
                add("gui.marketblocks.trade_book.toc.trends", "\ud83d\udcca Tendances PNJ");
                add("gui.marketblocks.trade_book.toc.trends.tooltip", "Offre et demande des PNJ");
                add("gui.marketblocks.trade_book.toc.shop_stats", "\ud83c\udfc6 Classement");
                add("gui.marketblocks.trade_book.toc.shop_stats.tooltip", "Classement des boutiques");
                add("gui.marketblocks.trade_book.toc.market_stats", "\ud83c\udfdb Meilleures ventes");
                add("gui.marketblocks.trade_book.toc.market_stats.tooltip", "Statistiques du marché");
                add("gui.marketblocks.trade_book.toc.active_shops", "\ud83d\udccd Stands de vente");
                add("gui.marketblocks.trade_book.toc.active_shops.tooltip", "Stands de vente actifs");
                add("gui.marketblocks.trade_book.my_shops.title", "=== Mes Boutiques ===\n\n");
                add("gui.marketblocks.trade_book.my_shops.empty", "Vous n'avez pas encore de boutique.\nPlacez un Stand ou une Caisse pour commencer !");
                add("gui.marketblocks.trade_book.my_shops.summary", "Boutiques : %s (Ouvertes : %s | Fermées : %s)\nVentes totales : %s\n\n");
                add("gui.marketblocks.trade_book.my_shops.sales_count", "  Ventes : %s\n");
                add("gui.marketblocks.trade_book.my_shops.sells", "  Vend : %s\n");
                add("gui.marketblocks.trade_book.trends.title", "=== Tendances PNJ ===\n\n");
                add("gui.marketblocks.trade_book.trends.hover", "Valeur de base : %s Émeraudes\nAchat PNJ : %s Émeraudes");
                add("gui.marketblocks.trade_book.trends.stable", "\nAucune tendance de marché active.");
                add("gui.marketblocks.trade_book.shops.title", "=== Meilleures Ventes ===\n\n");
                add("gui.marketblocks.trade_book.shops.empty", "Aucune boutique active sur le serveur.");
                add("gui.marketblocks.trade_book.shops.entry", "%s. %s :\n");
                add("gui.marketblocks.trade_book.shops.sales", "   Ventes : %s\n");
                add("gui.marketblocks.trade_book.shops.owner_sales", "  %s | Ventes : %s\n");
                add("gui.marketblocks.trade_book.shops.sales_only", "  Ventes : %s\n");
                add("gui.marketblocks.trade_book.shops.player_stats", "Boutiques : %s | Ventes : %s\n");
                add("gui.marketblocks.trade_book.marketplace.title", "=== Top du Marché ===\n\n");
                add("gui.marketblocks.trade_book.marketplace.empty", "Aucune vente sur le marché.");
                add("gui.marketblocks.trade_book.marketplace.entry", "%s. %s :\n");
                add("gui.marketblocks.trade_book.marketplace.sales", "   Ventes : %s");
                add("gui.marketblocks.trade_book.marketplace.sale_active", "   \u2605 OFFRE : %s\n");
                add("gui.marketblocks.trade_book.active.title", "=== Stands de Vente ===\n\n");
                add("gui.marketblocks.trade_book.active.empty", "Aucun stand actif sur le serveur.");
                add("gui.marketblocks.trade_book.active.owner", "  Propriétaire : %s\n");
                add("gui.marketblocks.trade_book.active.sells", "  Vend : %s\n");
                add("gui.marketblocks.trade_book.active.hover_tp", "Cliquez pour vous téléporter");
                add("gui.marketblocks.trade_book.active.unknown_owner", "Inconnu");
                add("gui.marketblocks.shop.default_name", "Boutique n°%s");
                add("gui.marketblocks.shop.named_format", "%s (n°%s)");
                add("gui.marketblocks.trade_book.active.closed", "Fermé");
                add("gui.marketblocks.trade_book.active.open", "Ouvert");
                add("gui.marketblocks.trade_book.active.no_offer", "Aucune offre définie");

                add("gui.marketblocks.trade_book.status.out_of_stock", " \u00a7c\u26a0 Rupture de stock");
                add("gui.marketblocks.trade_book.status.output_full", " \u00a76\u26a0 Sortie pleine");

                // === Trade Book Guide ===
                add("gui.marketblocks.trade_book.toc.guide.intro", "Introduction");
                add("gui.marketblocks.trade_book.toc.guide.intro.tooltip", "En savoir plus sur les bases de MarketBlocks");
                add("gui.marketblocks.trade_book.toc.guide.visuals", "Personnalisation visuelle");
                add("gui.marketblocks.trade_book.toc.guide.visuals.tooltip", "Apprenez à personnaliser visuellement vos boutiques");
                add("gui.marketblocks.trade_book.toc.guide.setup", "Configuration et mécaniques");
                add("gui.marketblocks.trade_book.toc.guide.setup.tooltip", "Découvrez l'interface, la Redstone et les Entonnoirs");
                add("gui.marketblocks.trade_book.toc.guide.advanced", "Fonctions avancées");
                add("gui.marketblocks.trade_book.toc.guide.advanced.tooltip", "En savoir plus sur les copropriétaires, l'économie et les outils admin");

                add("gui.marketblocks.trade_book.guide.intro.title", "=== Introduction ===\n\n");
                add("gui.marketblocks.trade_book.guide.intro.text", "Bienvenue sur MarketBlocks !\n\nCe mod vous permet de bâtir une économie florissante. Vous pouvez créer plusieurs boutiques pour échanger des objets avec d'autres joueurs ou acheteurs PNJ. Découvrons les blocs de boutique disponibles.");

                add("gui.marketblocks.trade_book.guide.tradestand.title", "=== Stand de commerce ===\n\n");
                add("gui.marketblocks.trade_book.guide.tradestand.text", "Une boutique ouverte qui affiche l'article vendu en lévitation au-dessus. Les PNJ adorent ces stands !");

                add("gui.marketblocks.trade_book.guide.marketcrate.title", "=== Caisse de marché ===\n\n");
                add("gui.marketblocks.trade_book.guide.marketcrate.text", "Une variante compacte sans objet flottant exposé. Idéal pour les espaces réduits.");

                add("gui.marketblocks.trade_book.guide.visuals.title", "=== Visuels ===\n\n");
                add("gui.marketblocks.trade_book.guide.visuals.text", "Les boutiques peuvent être personnalisées visuellement !\n\nUtilisez une clé pour faire pivoter le bloc. Si vous activez le 'Villageois décoratif' dans les paramètres, un PNJ sympathique se tiendra derrière le stand !");

                add("gui.marketblocks.trade_book.guide.setup.title", "=== Configuration et interface ===\n\n");
                add("gui.marketblocks.trade_book.guide.setup.text", "Faites un clic droit sur votre boutique pour ouvrir la configuration.\n\nVous pouvez définir le prix, l'article vendu et approvisionner l'inventaire. Vous pouvez également fixer des limites d'achat.");

                add("gui.marketblocks.trade_book.guide.mechanics.title", "=== Mécaniques ===\n\n");
                add("gui.marketblocks.trade_book.guide.mechanics.redstone", "Les boutiques émettent un signal de redstone en fonction de leur remplissage ou rupture de stock. Vous pouvez configurer cela dans l'onglet Redstone.");
                add("gui.marketblocks.trade_book.guide.mechanics.hopper", "Vous pouvez utiliser des entonnoirs pour automatiser le réassort et collecter les gains ! Configurez les options d'entrée/sortie dans l'onglet Auto-IO.");

                add("gui.marketblocks.trade_book.guide.advanced.title", "=== Fonctions avancées ===\n\n");
                add("gui.marketblocks.trade_book.guide.advanced.text", "Vous pouvez ajouter des copropriétaires pour gérer votre boutique.\n\nLes acheteurs PNJ ont leur propre économie soumise aux tendances ! Si tout le monde vend du bois, son prix baisse. Surveillez l'onglet Tendances.");

                // NPC Economy Guide Page (G4)
                add("gui.marketblocks.trade_book.guide.economy.title", "=== Économie PNJ ===\n\n");
                add("gui.marketblocks.trade_book.guide.economy.text", "Des acheteurs PNJ visitent vos boutiques et achètent des articles si l'offre est intéressante !\n\n\u00a76Prix :\u00a7r Les objets possèdent des valeurs de base. Les objets fabriqués valent plus (+10% par étape d'artisanat).\n\n\u00a76Offre et demande :\u00a7r Si un article est souvent vendu, sa valeur PNJ diminue. Avec le temps, les prix remontent.\n\n\u00a76Rangs PNJ :\u00a7r\n\u2022 \u00a77Citoyen\u00a7r – Petit budget\n\u2022 \u00a7eAisé\u00a7r – Budget moyen\n\u2022 \u00a76Noble\u00a7r – Gros budget, achète des objets rares\n\n\u00a76Centres d'intérêt :\u00a7r Chaque PNJ a une spécialisation (Fermier, Forgeron, Alchimiste, Collectionneur). Ils dépensent davantage pour leur catégorie de prédilection !");

                // === Block Registrations ===
                addBlock(RegistriesInit.MARKETCRATE_BLOCK, "Caisse de marché");
                addBlock(RegistriesInit.TRADE_STAND_BLOCK, "Stand de commerce");

        }
}
