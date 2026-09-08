package de.bigbull.marketblocks.data.lang;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.core.init.RegistriesInit;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * Data provider for generating the Spanish (es_es) language file.
 * Contains all translated strings for UI elements, messages, and blocks.
 */
public class ModEsLangProvider extends LanguageProvider {
        public ModEsLangProvider(PackOutput output) {
                super(output, MarketBlocks.MODID, "es_es");
        }

        @Override
        protected void addTranslations() {
                // === Creative Tab ===
                add("itemGroup.marketblocks", "MarketBlocks");

                // === Blocks & Items ===
                add("item.marketblocks.trade_stand.with_showcase", "Puesto de comercio (con vitrina)");

                // === Entities ===
                add("entity.marketblocks.shop_buyer", "Comprador ambulante");
                add("item.marketblocks.shop_buyer_spawn_egg", "Huevo de generador de comprador ambulante");
                addItem(RegistriesInit.TRADE_BOOK, "Libro de comercio");

                // === Containers & Menus ===
                add("container.marketblocks.trade_stand", "Puesto de comercio");
                add("menu.marketblocks.marketplace", "Mercado");

                // === Keybinds ===
                add("key.categories.marketblocks", "MarketBlocks");
                add("key.marketblocks.open_marketplace", "Abrir el mercado");

                // === Commands ===
                add("command.marketblocks.break.denied", "\u00a7c\u00a1No puedes romper este bloque mientras est\u00e9 vinculado a un mercado!");
                add("command.marketblocks.break.unlinked", "\u00a7eEl bloque ha sido desvinculado del mercado.");
                add("command.marketblocks.link.already_linked", "\u00a7cEste bloque ya est\u00e1 vinculado a un mercado.");
                add("command.marketblocks.link.not_looking_at_block", "\u00a7cDebes mirar un bloque para vincularlo.");
                add("command.marketblocks.link.success", "\u00a7a\u00a1Bloque vinculado al mercado exitosamente!");
                add("command.marketblocks.list.click_to_delete", "Haz clic para eliminar");
                add("command.marketblocks.list.click_to_teleport", "Haz clic para teletransportarte");
                add("command.marketblocks.list.click_to_waypoint", "Haz clic para obtener enlaces de Waypoint en el chat");
                add("command.marketblocks.list.delete", "[Eliminar]");
                add("command.marketblocks.list.tp", "[Teletransporte]");
                add("command.marketblocks.list.waypoint", "[Waypoint]");
                add("command.marketblocks.list.page_header", "\u00a78======== \u00a76\u00a7lTiendas (P\u00e1gina %s/%s) \u00a78========");
                add("command.marketblocks.list.prev", "[< Ant]");
                add("command.marketblocks.list.next", "[Sig >]");
                add("command.marketblocks.marketplacelist.entry", "\u00a78\u25aa \u00a76Mercado \u00a7e%s");
                add("command.marketblocks.marketplacelist.page_header", "\u00a78======== \u00a76\u00a7lMercados (P\u00e1gina %s/%s) \u00a78========");
                add("command.marketblocks.marketplacelist.no_links", "\u00a7cNo se encontraron mercados.");
                add("command.marketblocks.player_not_found", "\u00a7cJugador no encontrado");
                add("command.marketblocks.reload.success", "\u00a7a\u00a1Configuraci\u00f3n de MarketBlocks recargada exitosamente!");
                add("command.marketblocks.resetlimits.no_changes", "\u00a7eNo se restablecieron l\u00edmites diarios.");
                add("command.marketblocks.resetlimits.success", "\u00a7aL\u00edmites diarios restablecidos exitosamente.");
                add("command.marketblocks.search.header", "\u00a78======== \u00a76\u00a7lTiendas vendiendo %s (P\u00e1gina %s/%s) \u00a78========");
                add("command.marketblocks.search.no_shops", "\u00a7cNo se encontraron tiendas ni mercados vendiendo %s.");
                add("command.marketblocks.shoplist.closed", "CERRADO");
                add("command.marketblocks.shoplist.entry", "\u00a78\u25aa \u00a77[%s\u00a77] \u00a7e%s \u00a78(por \u00a77%s\u00a78)");
                add("command.marketblocks.shoplist.header", "\u00a78======== \u00a76\u00a7lTiendas de MarketBlocks \u00a78========");
                add("command.marketblocks.shoplist.no_shops", "\u00a7cNo hay tiendas disponibles.");
                add("command.marketblocks.shoplist.open", "ABIERTO");
                add("command.marketblocks.shoplist.hover.shop", "Tienda: %s");
                add("command.marketblocks.shoplist.hover.owner", "Propietario: %s");
                add("command.marketblocks.shoplist.hover.status", "Estado: %s");
                add("command.marketblocks.shoplist.hover.offer", "Oferta:");
                add("command.marketblocks.shoplist.hover.arrow", "\u2794");
                add("command.marketblocks.unlink.not_found", "\u00a7cNo se pudo encontrar el enlace del mercado.");
                add("command.marketblocks.unlink.not_linked", "\u00a7cEste bloque no est\u00e1 vinculado.");
                add("command.marketblocks.unlink.not_looking_at_block", "\u00a7cDebes mirar un bloque para desvincularlo.");
                add("command.marketblocks.unlink.success", "\u00a7a\u00a1Bloque desvinculado exitosamente!");
                add("command.marketblocks.unlink.success_name", "\u00a7aDesvinculado del mercado: \u00a7e%s");
                add("command.marketblocks.waypoint.created", "\u00a7aEnlaces de Waypoint creados:");
                add("command.marketblocks.internal.waypoint.coords", "Coordenadas del punto de ruta para %s: X: %d, Y: %d, Z: %d (%s)");
                add("command.marketblocks.internal.waypoint.journeymap", "Punto de ruta de JourneyMap creado / enlace generado.");
                add("command.marketblocks.internal.waypoint.xaero", "Enlace de punto de ruta de Xaero's Minimap:");
                add("command.marketblocks.internal.tp.no_permission", "No tienes permiso para teletransportarte a las tiendas.");
                add("command.marketblocks.internal.tp.invalid_dimension", "No se pudo encontrar la dimensi\u00f3n de destino.");
                add("command.marketblocks.internal.tp.success", "\u00a1Teletransportado a la tienda!");

                // === GUI - Settings ===
                add("gui.marketblocks.access.edit_access_list", "Lista de acceso");
                add("gui.marketblocks.access.edit_owners", "Propietarios");
                add("gui.marketblocks.access.mode.blacklist", "Modo: Lista negra");
                add("gui.marketblocks.access.mode.everyone", "Modo: Todos");
                add("gui.marketblocks.access.mode.whitelist", "Modo: Lista blanca");
                add("gui.marketblocks.io.allow_io", "Permitir I/O");
                add("gui.marketblocks.io.allow_io.tooltip", "Permite a las tolvas y tuber\u00edas interactuar con este lado.");
                add("gui.marketblocks.io.auto_io", "Auto Push/Pull");
                add("gui.marketblocks.io.auto_io.tooltip", "Extrae e inserta objetos autom\u00e1ticamente de inventarios adyacentes.");
                add("gui.marketblocks.io.redstone_control.ignored", "Ignorado");
                add("gui.marketblocks.io.redstone_control.require_no_signal", "Bajo (Sin se\u00f1al)");
                add("gui.marketblocks.io.redstone_control.require_signal", "Alto (Requiere se\u00f1al)");
                add("gui.marketblocks.io.redstone_control.tooltip", "Modo de control de redstone para este lado.");
                add("gui.marketblocks.settings_owner_only", "Solo el propietario puede cambiar la configuraci\u00f3n");
                add("gui.marketblocks.settings_tab", "Mostrar configuraci\u00f3n");
                add("gui.marketblocks.settings_title", "Configuraci\u00f3n de la tienda");
                add("gui.marketblocks.toggle.off", "OFF");
                add("gui.marketblocks.toggle.on", "ON");
                add("gui.marketblocks.settings.category.access", "Acceso");
                add("gui.marketblocks.settings.category.access.title", "Ajustes de acceso");
                add("gui.marketblocks.settings.category.general", "General");
                add("gui.marketblocks.settings.category.general.title", "Ajustes generales");
                add("gui.marketblocks.settings.category.io", "I/O");
                add("gui.marketblocks.settings.category.io.title", "Ajustes de E/S");
                add("gui.marketblocks.settings.category.notifications", "Notificaciones");
                add("gui.marketblocks.settings.category.notifications.title", "Ajustes de notificaciones");
                add("gui.marketblocks.settings.category.villager", "NPC");
                add("gui.marketblocks.settings.category.villager.title", "Ajustes de NPC");
                add("gui.marketblocks.settings.category.visual", "Visual");
                add("gui.marketblocks.settings.category.visual.title", "Ajustes visuales");
                add("gui.marketblocks.settings.reset", "Restablecer valores predeterminados");
                add("gui.marketblocks.visuals.bobbing", "Flotaci\u00f3n");
                add("gui.marketblocks.visuals.chaos_rotation", "Rotaci\u00f3n ca\u00f3tica");
                add("gui.marketblocks.visuals.count", "Cantidad de objetos");
                add("gui.marketblocks.visuals.count_short", "Cant.:");
                add("gui.marketblocks.visuals.display", "VisualizaciÃ³n:");
                add("gui.marketblocks.visuals.group.item_arrangement", "DISPOSICIÃ“N DE OBJETOS");
                add("gui.marketblocks.visuals.group.visuals_transformations", "EFECTOS Y TRANSFORMACIONES");
                add("gui.marketblocks.visuals.dynamic_fill_level", "Llenado din\u00e1mico");
                add("gui.marketblocks.visuals.error.no_surface", "\u00a1No hay superficie de puesto detr\u00e1s de la tienda!");
                add("gui.marketblocks.visuals.error.space_blocked", "\u00a1Espacio bloqueado!");
                add("gui.marketblocks.visuals.height", "Altura");
                add("gui.marketblocks.visuals.layout_mode", "Modo de dise\u00f1o");
                add("gui.marketblocks.visuals.layout_mode.gestapelt", "Apilado");
                add("gui.marketblocks.visuals.layout_mode.lose", "Suelto");
                add("gui.marketblocks.visuals.npc_enabled", "Aldeano decorativo");
                add("gui.marketblocks.visuals.npc_name", "Nombre del NPC");
                add("gui.marketblocks.visuals.offer_item_disabled_global", "Desactivado por el administrador del servidor.");
                add("gui.marketblocks.visuals.offer_item_fullbright", "Brillo");
                add("gui.marketblocks.visuals.offer_item_fullbright.tooltip", "Hace que el objeto brille en la oscuridad (sin sombras).");
                add("gui.marketblocks.visuals.offer_item_visible", "Objeto de oferta visible");
                add("gui.marketblocks.visuals.offer_item_visible.tooltip", "Muestra u oculta el objeto de oferta flotante/expuesto.");
                add("gui.marketblocks.visuals.payment_sounds", "Sonidos de pago");
                add("gui.marketblocks.visuals.npc_sounds", "Sonidos del PNJ");
                                add("gui.marketblocks.visuals.npc_sounds.tooltip", "Controla cuándo el PNJ reproduce sonidos (compra, pago o desactivado).");
                                add("gui.marketblocks.visuals.npc_sounds.all", "Todos");
                                add("gui.marketblocks.visuals.npc_sounds.purchase", "Solo compra");
                                add("gui.marketblocks.visuals.npc_sounds.payment", "Solo pago");
                                add("gui.marketblocks.visuals.npc_sounds.off", "Desactivado");
                add("gui.marketblocks.visuals.player_skin_name", "Nombre del jugador");
                add("gui.marketblocks.visuals.profession", "Profesi\u00f3n");
                add("gui.marketblocks.visuals.profession.armorer", "Herrero de armaduras");
                add("gui.marketblocks.visuals.profession.butcher", "Carnicero");
                add("gui.marketblocks.visuals.profession.cartographer", "Cart\u00f3grafo");
                add("gui.marketblocks.visuals.profession.cleric", "Cl\u00e9rigo");
                add("gui.marketblocks.visuals.profession.farmer", "Granjero");
                add("gui.marketblocks.visuals.profession.fisherman", "Pescador");
                add("gui.marketblocks.visuals.profession.fletcher", "Flechero");
                add("gui.marketblocks.visuals.profession.leatherworker", "Peletero");
                add("gui.marketblocks.visuals.profession.librarian", "Bibliotecario");
                add("gui.marketblocks.visuals.profession.mason", "Alba\u00f1il");
                add("gui.marketblocks.visuals.profession.nitwit", "Bobo");
                add("gui.marketblocks.visuals.profession.none", "Desempleado");
                add("gui.marketblocks.visuals.profession.shepherd", "Pastor");
                add("gui.marketblocks.visuals.profession.toolsmith", "Herrero de herramientas");
                add("gui.marketblocks.visuals.profession.weaponsmith", "Herrero de armas");
                add("gui.marketblocks.visuals.purchase_particles", "Part\u00edculas de compra");
                add("gui.marketblocks.visuals.purchase_sounds", "Sonidos de compra");
                add("gui.marketblocks.visuals.rotation", "Rotaci\u00f3n");
                add("gui.marketblocks.visuals.rotation_x", "Rotaci\u00f3n X");
                add("gui.marketblocks.visuals.rotation_y", "Rotaci\u00f3n Y");
                add("gui.marketblocks.visuals.rotation_z", "Rotaci\u00f3n Z");
                add("gui.marketblocks.visuals.scale", "Escala");
                add("gui.marketblocks.visuals.spacing_xz", "Espaciado X/Z");
                add("gui.marketblocks.visuals.spacing_y", "Espaciado Y");
                add("gui.marketblocks.visuals.speed", "Velocidad");
                add("gui.marketblocks.visuals.use_player_skin", "Apariencia de jugador");
                add("gui.marketblocks.visuals.use_player_skin.tooltip", "Muestra un jugador en lugar de un aldeano.");

                // === GUI - Notifications ===
                add("gui.marketblocks.notifications.co_owners", "Notificar a los copropietarios");
                add("gui.marketblocks.notifications.co_owners.tooltip", "Tambi\u00e9n env\u00eda notificaciones a los propietarios adicionales de la tienda.");
                add("gui.marketblocks.notifications.out_of_stock", "Alerta de agotamiento");
                add("gui.marketblocks.notifications.out_of_stock.tooltip", "Recibe una advertencia cuando la tienda se quede sin existencias.");
                add("gui.marketblocks.notifications.output_full", "Alerta de almacenamiento lleno");
                add("gui.marketblocks.notifications.output_full.tooltip", "Recibe una advertencia cuando el inventario de salida de la tienda est\u00e9 lleno.");
                add("gui.marketblocks.notifications.purchase", "Notificaciones de compra");
                add("gui.marketblocks.notifications.purchase.tooltip", "Recibe una notificaci\u00f3n en el chat cuando alguien compre en tu tienda.");
                add("message.marketblocks.notifications.out_of_stock", "\u00a7c\u00a1Tu tienda est\u00e1 agotada!\u00a7r");
                add("message.marketblocks.notifications.output_full", "\u00a7c\u00a1El inventario de salida de tu tienda est\u00e1 lleno!\u00a7r");
                add("message.marketblocks.notifications.purchase", "\u00a7a%s compr\u00f3 %sx %s en tu tienda.\u00a7r");

                // === GUI - Marketplace Editors ===
                add("gui.marketblocks.marketplace.editor.limits.daily", "L\u00edmite diario");
                add("gui.marketblocks.marketplace.editor.limits.restock", "Reabastecimiento (s)");
                add("gui.marketblocks.marketplace.editor.limits.stock", "L\u00edmite de stock");
                add("gui.marketblocks.marketplace.editor.limits.title", "L\u00edmites");
                add("gui.marketblocks.marketplace.editor.limits.daily.tooltip", "Compras m\u00e1ximas que un jugador puede hacer por d\u00eda.");
                add("gui.marketblocks.marketplace.editor.limits.stock.tooltip", "Stock total disponible para esta oferta.");
                add("gui.marketblocks.marketplace.editor.limits.restock.tooltip", "Tiempo en segundos hasta que se reponga el stock.");
                add("gui.marketblocks.marketplace.editor.pricing.disabled", "Fijaci\u00f3n de precios OFF");
                add("gui.marketblocks.marketplace.editor.pricing.enabled", "Fijaci\u00f3n de precios ON");
                add("gui.marketblocks.marketplace.editor.pricing.label", "Activar fijaci\u00f3n de precios");
                add("gui.marketblocks.marketplace.editor.pricing.label.tooltip", "Si se activa, el precio se ajusta din\u00e1micamente seg\u00fan la temperatura del mercado.");
                add("gui.marketblocks.marketplace.editor.pricing.base", "Precio base (%)");
                add("gui.marketblocks.marketplace.editor.pricing.base.tooltip", "Factor de escala del precio base en porcentaje (ej. 100 = normal, 150 = 50% de recargo). Se aplica antes de los ajustes de demanda.");
                add("gui.marketblocks.marketplace.editor.pricing.max", "Precio m\u00e1x (%)");
                add("gui.marketblocks.marketplace.editor.pricing.max.tooltip", "Porcentaje m\u00e1ximo que puede alcanzar el precio (ej. 200 = precio duplicado).");
                add("gui.marketblocks.marketplace.editor.pricing.min", "Precio m\u00edn (%)");
                add("gui.marketblocks.marketplace.editor.pricing.min.tooltip", "Porcentaje m\u00ednimo al que puede caer el precio (ej. 50 = mitad de precio).");
                add("gui.marketblocks.marketplace.editor.pricing.volatility", "Volatilidad");
                add("gui.marketblocks.marketplace.editor.pricing.volatility.tooltip", "Qu\u00e9 tan r\u00e1pido reacciona el precio a las compras y al tiempo.");
                add("gui.marketblocks.marketplace.editor.pricing.volatility.slow", "Lenta");
                add("gui.marketblocks.marketplace.editor.pricing.volatility.normal", "Normal");
                add("gui.marketblocks.marketplace.editor.pricing.volatility.fast", "R\u00e1pida");
                add("gui.marketblocks.marketplace.editor.pricing.title", "Fijaci\u00f3n de precios");

                // === GUI - Marketplace Elements ===
                add("gui.marketblocks.marketplace.add_offer", "A\u00f1adir oferta");
                add("gui.marketblocks.marketplace.add_page", "A\u00f1adir p\u00e1gina");
                add("gui.marketblocks.marketplace.delete_offer", "Eliminar oferta");
                add("gui.marketblocks.marketplace.delete_page", "Eliminar p\u00e1gina");
                add("gui.marketblocks.marketplace.inline.limits", "Editar l\u00edmites");
                add("gui.marketblocks.marketplace.inline.limits.disabled_global", "Desactivado: Los l\u00edmites globales est\u00e1n activos en la configuraci\u00f3n del servidor");
                add("gui.marketblocks.marketplace.inline.pricing", "Editar fijaci\u00f3n de precios");
                add("gui.marketblocks.marketplace.inline.pricing.disabled_global", "Desactivado: La fijaci\u00f3n de precios global est\u00e1 activa en la configuraci\u00f3n del servidor");
                add("gui.marketblocks.marketplace.mode.edit", "Cambiar al modo edici\u00f3n");
                add("gui.marketblocks.marketplace.mode.view", "Cambiar al modo vista");
                add("gui.marketblocks.marketplace.move_offer", "Mover oferta");
                add("gui.marketblocks.marketplace.move_offer_down", "Mover oferta abajo");
                add("gui.marketblocks.marketplace.move_offer_up", "Mover oferta arriba");
                add("gui.marketblocks.marketplace.no_offers", "Sin ofertas");
                add("gui.marketblocks.marketplace.no_pages", "Sin p\u00e1ginas\ndisponibles");
                add("gui.marketblocks.marketplace.rename_page", "Renombrar p\u00e1gina");
                add("gui.marketblocks.marketplace.sidebar.next", "Siguiente p\u00e1gina de categor\u00edas");
                add("gui.marketblocks.marketplace.sidebar.prev", "P\u00e1gina de categor\u00edas anterior");
                add("gui.marketblocks.marketplace.status.daily_short", "D:%s");
                add("gui.marketblocks.marketplace.status.price_short", "x%s");
                add("gui.marketblocks.marketplace.status.restock_short", "R:%s");
                add("gui.marketblocks.marketplace.status.stock_short", "S:%s");
                add("gui.marketblocks.marketplace.tooltip.price_multiplier", "Factor de precio.");
                add("gui.marketblocks.marketplace.tooltip.remaining_daily", "Tus compras restantes hoy.");
                add("gui.marketblocks.marketplace.tooltip.remaining_daily_empty", "L\u00edmite diario alcanzado.");
                add("gui.marketblocks.marketplace.tooltip.remaining_stock", "Compras de stock restantes.");
                add("gui.marketblocks.marketplace.tooltip.remaining_stock_empty", "Agotado.");
                add("gui.marketblocks.marketplace.tooltip.restock_in", "Tiempo hasta el reabastecimiento.");
                add("gui.marketblocks.marketplace.tooltip.restock_ready", "Reabastecimiento listo.");
                add("gui.marketblocks.marketplace.tooltip.unavailable_daily", "No disponible: l\u00edmite diario alcanzado.");
                add("gui.marketblocks.marketplace.tooltip.unavailable_generic", "No disponible.");
                add("gui.marketblocks.marketplace.tooltip.unavailable_restock", "No disponible: reabasteciendo.");
                add("gui.marketblocks.marketplace.tooltip.unavailable_stock", "No disponible: agotado.");
                add("gui.marketblocks.marketplace.unnamed_page", "P\u00e1gina %s");

                // === GUI - General ===
                add("gui.marketblocks.admin_shop.disabled", "Admin-Shop: OFF");
                add("gui.marketblocks.admin_shop.enabled", "Admin-Shop: ON");
                add("gui.marketblocks.category", "Categor\u00eda");
                add("gui.marketblocks.category.none", "Ninguna");
                add("gui.marketblocks.category.weapons_armor", "Armas y Armaduras");
                add("gui.marketblocks.category.tools", "Herramientas");
                add("gui.marketblocks.category.blocks", "Bloques");
                add("gui.marketblocks.category.food_potions", "Comida y Pociones");
                add("gui.marketblocks.category.valuables", "Objetos de valor");
                add("gui.marketblocks.category.misc", "Varios");
                add("gui.marketblocks.category.tooltip", "Categor\u00eda bajo la cual listar la tienda en el directorio global");
                add("gui.marketblocks.create_offer", "Crear oferta");
                add("gui.marketblocks.delete_offer", "Eliminar oferta");
                add("gui.marketblocks.disabled", "Desactivado");
                add("gui.marketblocks.emit_redstone", "Emitir Redstone");
                add("gui.marketblocks.emit_redstone.tooltip", "Emite un pulso corto de redstone despu\u00e9s de una compra");
                add("gui.marketblocks.error.invalid_offer", "Configuraci\u00f3n de oferta no v\u00e1lida");
                add("gui.marketblocks.error.no_payment_items", "Por favor, coloca al menos un objeto de pago");
                add("gui.marketblocks.error.no_result_item", "Por favor, coloca un objeto en la ranura de resultado");
                add("gui.marketblocks.input", "Entrada");
                add("gui.marketblocks.inventory_admin_disabled", "Inventario desactivado en modo admin");
                add("gui.marketblocks.inventory_owner_only", "Solo el propietario puede gestionar el inventario");
                add("gui.marketblocks.inventory_tab", "Inventario");
                add("gui.marketblocks.inventory_title", "Inventario del puesto");
                add("gui.marketblocks.log_tab", "Registro");
                add("gui.marketblocks.log_title", "Registro de transacciones");
                add("gui.marketblocks.log.clear", "Limpiar registro");
                add("gui.marketblocks.log.count", "Entradas: %s");
                add("gui.marketblocks.log.empty", "Sin transacciones a\u00fan");
                add("gui.marketblocks.log.none", "Ninguno");
                add("gui.marketblocks.log.time.days", "hace %s d");
                add("gui.marketblocks.log.time.hours", "hace %s h");
                add("gui.marketblocks.log.time.just_now", "Ahora mismo");
                add("gui.marketblocks.log.time.minutes", "hace %s min");
                add("gui.marketblocks.log.time.seconds", "hace %s s");
                add("gui.marketblocks.mode.edit_active", "MODO EDICI\u00d3N");
                add("gui.marketblocks.no_players_available", "Sin jugadores disponibles");
                add("gui.marketblocks.offers", "Ofertas");
                add("gui.marketblocks.offers_tab", "Ofertas");
                add("gui.marketblocks.out_of_stock", "Agotado");
                add("gui.marketblocks.output", "Salida");
                add("gui.marketblocks.output_full", "Salida llena");
                add("gui.marketblocks.owner", "Propietario: %s");
                add("gui.marketblocks.purchase_sound", "Sonido al comprar");
                add("gui.marketblocks.purchase_sound.tooltip", "Reproduce un sonido cuando un jugador compra algo");
                add("gui.marketblocks.purchase_xp_sound", "Sonido al comprar");
                add("gui.marketblocks.purchase_xp_sound.tooltip", "Reproduce un sonido cuando un jugador compra algo");
                add("gui.marketblocks.save", "Guardar");
                add("gui.marketblocks.shop_closed", "Tienda en pausa");
                add("gui.marketblocks.shop_closed.tooltip", "Si est\u00e1 activa, la tienda est\u00e1 pausada y nadie puede comprar objetos.");
                add("gui.marketblocks.shop_name", "Nombre de la tienda");
                add("gui.marketblocks.shop_title", "Puesto de comercio");
                add("gui.marketblocks.side.back", "Atr\u00e1s");
                add("gui.marketblocks.side.bottom", "Abajo");
                add("gui.marketblocks.side.left", "Izquierda");
                add("gui.marketblocks.side.right", "Derecha");

                // === Messages & Chat ===
                add("message.marketblocks.marketplace.daily_limit_reached", "Se ha alcanzado el l\u00edmite diario para esta oferta.");
                add("message.marketblocks.marketplace.edit_mode_disabled", "Modo edici\u00f3n del mercado desactivado.");
                add("message.marketblocks.marketplace.edit_mode_enabled", "Modo edici\u00f3n del mercado activado.");
                add("message.marketblocks.marketplace.limits.invalid_data", "No se pudieron guardar los l\u00edmites: datos no v\u00e1lidos.");
                add("message.marketblocks.marketplace.limits.invalid_positive_int", "Por favor, introduce solo n\u00fameros enteros positivos para los l\u00edmites.");
                add("message.marketblocks.marketplace.limits.no_connection", "No se pudieron guardar los l\u00edmites: sin conexi\u00f3n con el servidor.");
                add("message.marketblocks.marketplace.page_limit_reached",
                                "Se ha alcanzado el n\u00famero m\u00e1ximo de %s p\u00e1ginas.");
                add("message.marketblocks.marketplace.page_name_blank", "El nombre de la p\u00e1gina no debe estar vac\u00edo.");
                add("message.marketblocks.marketplace.page_name_duplicate", "Ya existe una p\u00e1gina con el nombre '%s'.");
                add("message.marketblocks.marketplace.page_name_too_long", "El nombre de la p\u00e1gina debe tener como m\u00e1ximo %s caracteres.");
                add("message.marketblocks.marketplace.page_not_found", "No se pudo encontrar la p\u00e1gina de tienda seleccionada.");
                add("message.marketblocks.marketplace.pricing.invalid_data", "No se pudo guardar la fijaci\u00f3n de precios: datos no v\u00e1lidos.");
                add("message.marketblocks.marketplace.pricing.invalid_finite", "Por favor, introduce n\u00fameros finitos v\u00e1lidos para la fijaci\u00f3n de precios.");
                add("message.marketblocks.marketplace.pricing.invalid_number_format", "Por favor, utiliza solo n\u00fameros (se permite punto o coma).");
                add("message.marketblocks.marketplace.pricing.no_connection", "No se pudo guardar la fijaci\u00f3n de precios: sin conexi\u00f3n con el servidor.");
                add("message.marketblocks.trade_stand.no_offer", "Este puesto de comercio actualmente no tiene ninguna oferta activa.");
                add("message.marketblocks.trade_stand.not_owner", "Solo el propietario puede romper este puesto de comercio.");
                add("message.marketblocks.trade_stand.break_not_empty", "\u00a1Debes vaciar primero todos los objetos y pagos!");
                add("message.marketblocks.shop.limit_reached", "\u00a1Puedes colocar un m\u00e1ximo de %s tiendas!");
                
                add("message.marketblocks.shop_buyer.interact.1", "\u00a7e\u00a1Estoy buscando buenos art\u00edculos para comprar!\u00a7r");
                add("message.marketblocks.shop_buyer.interact.2", "\u00a7e\u00bfTienes algo interesante a la venta?\u00a7r");
                add("message.marketblocks.shop_buyer.interact.3", "\u00a7eViajo para hacer negocios. \u00a1Quiz\u00e1s tengas lo que necesito!\u00a7r");
                add("message.marketblocks.shop_buyer.interact.4", "\u00a7e\u00a1Acabo de encontrar una gran oferta! \u00a1Me encanta comprar aqu\u00ed!\u00a7r");
                add("message.marketblocks.shop_buyer.interact.5", "\u00a7e\u00a1Otra buena compra! Mi bolso empieza a pesar.\u00a7r");
                add("message.marketblocks.shop_buyer.interact.6", "\u00a7e\u00a1Encontr\u00e9 todo lo que necesitaba gracias a estas tiendas!\u00a7r");
                add("message.marketblocks.shop_buyer.interact.7", "\u00a7eHmm, estoy buscando algo en espec\u00edfico...\u00a7r");
                add("message.marketblocks.shop_buyer.interact.8", "\u00a7eMe pregunto qu\u00e9 otras tiendas habr\u00e1 por aqu\u00ed...\u00a7r");
                add("message.marketblocks.shop_buyer.interact.9", "\u00a7eSolo estoy mirando por ahora. Nada me ha llamado la atenci\u00f3n a\u00fan.\u00a7r");
                add("message.marketblocks.shop_buyer.interact.10", "\u00a7e\u00a1Bonita tienda tienes aqu\u00ed! La tendr\u00e9 en cuenta.\u00a7r");

                // NPC Rank & Category display (G2)
                add("message.marketblocks.shop_buyer.info", "\u00a77[%s - %s]");
                add("entity.marketblocks.shop_buyer.rank.citizen", "Ciudadano");
                add("entity.marketblocks.shop_buyer.rank.wealthy", "Comerciante adinerado");
                add("entity.marketblocks.shop_buyer.rank.noble", "Noble mercader");
                add("entity.marketblocks.shop_buyer.category.general", "Comerciante general");
                add("entity.marketblocks.shop_buyer.category.farmer", "Granjero");
                add("entity.marketblocks.shop_buyer.category.alchemist", "Alquimista");
                add("entity.marketblocks.shop_buyer.category.blacksmith", "Herrero");
                add("entity.marketblocks.shop_buyer.category.valuables", "Coleccionista");

                // === Jade / Waila Support ===
                add("config.jade.plugin_marketblocks.shop_info", "Informaci\u00f3n de la tienda");
                add("config.jade.plugin_marketblocks.shop_buyer_info", "Informaci\u00f3n del comerciante");
                add("marketblocks.jade.for", "Por:");
                add("marketblocks.jade.out_of_stock", "\u00a1Agotado!");
                add("marketblocks.jade.output_full", "\u00a1Inventario lleno!");
                add("marketblocks.jade.owner", "Propietario: %s");
                add("marketblocks.jade.shop", "Tienda: %s");
                add("marketblocks.jade.selling", "Vendiendo:");
                add("marketblocks.jade.status.admin_shop", "Admin Shop");
                add("marketblocks.jade.status.closed", "Tienda cerrada");
                add("marketblocks.jade.trader.budget", "Presupuesto: %s");

                // === Advancements ===
                add("advancements.marketblocks.admin_shop.description", "Activer el modo admin shop");
                add("advancements.marketblocks.admin_shop.title", "Bienes Infinitos");
                add("advancements.marketblocks.auto_io.description", "Activar la entrada/salida autom\u00e1tica para tu tienda");
                add("advancements.marketblocks.auto_io.title", "Log\u00edstica");
                add("advancements.marketblocks.custom_npc.description", "Personalizar el NPC de tu tienda con un nombre o apariencia de jugador");
                add("advancements.marketblocks.custom_npc.title", "Personal a medida");
                add("advancements.marketblocks.first_shop.description", "Colocar tu primer bloque de tienda MarketBlocks");
                add("advancements.marketblocks.first_shop.title", "Abierto para los negocios");
                add("advancements.marketblocks.hiring.description", "Activar un NPC para tu tienda");
                add("advancements.marketblocks.hiring.title", "Contrataci\u00f3n en curso");
                add("advancements.marketblocks.joint_venture.description", "A\u00f1adir un copropietario a tu tienda");
                add("advancements.marketblocks.joint_venture.title", "Empresa conjunta");
                add("advancements.marketblocks.marketplace_buy.description", "Comprar un objeto a trav\u00e9s del mercado");
                add("advancements.marketblocks.marketplace_buy.title", "Cliente del centro comercial");
                add("advancements.marketblocks.out_of_stock.description", "Hacer que una tienda (no-admin) se quede sin existencias");
                add("advancements.marketblocks.out_of_stock.title", "Agotado");
                add("advancements.marketblocks.redstone.description", "Activar la salida de redstone o I/O controlados por redstone");
                add("advancements.marketblocks.redstone.title", "L\u00f3gica de Redstone");
                add("advancements.marketblocks.root.description", "Obtener un bloque de tienda MarketBlocks");
                add("advancements.marketblocks.root.title", "MarketBlocks");
                add("advancements.marketblocks.showcase.description", "A\u00f1adir una vitrina de cristal a un puesto de comercio");
                add("advancements.marketblocks.showcase.title", "Exhibici\u00f3n");
                add("advancements.marketblocks.sold_item.description", "Vender tu primer objeto a otro jugador");
                add("advancements.marketblocks.sold_item.title", "\u00a1Primera venta!");
                add("advancements.marketblocks.tycoon.description", "Vender 100 objetos a trav\u00e9s de tus tiendas");
                add("advancements.marketblocks.tycoon.title", "Magnate");
                add("advancements.marketblocks.wall_street.description", "Abrir el mercado");
                add("advancements.marketblocks.wall_street.title", "Wall Street");
                add("advancements.marketblocks.wholesaler.description", "Comprar 64 objetos o m\u00e1s en una sola transacci\u00f3n");
                add("advancements.marketblocks.wholesaler.title", "Mayorista");

                // === Subtitles ===
                add("subtitles.marketblocks.visual_npc_fall", "Aldeano aterriza");

                // === Login Notifications ===
                add("gui.marketblocks.notifications.login.out_of_stock", "\u00a7c[MarketBlocks] \u00a1%s de tus tiendas est\u00e1n agotadas!\u00a7r");
                add("gui.marketblocks.notifications.login.output_full", "\u00a7c[MarketBlocks] \u00a1%s de tus tiendas tienen el almacenamiento de salida lleno!\u00a7r");
                add("gui.marketblocks.notifications.login.coordinate", "\u00a77 - Localizaci\u00f3n: X: %s, Y: %s, Z: %s\u00a7r");

                // === Purchase Confirmations ===
                add("message.marketblocks.purchase_success", "Has comprado con \u00e9xito %s x %s.");
                add("message.marketblocks.purchase_success.global", "%s ha comprado %s x %s.");

                // === Admin Commands ===
                add("command.marketblocks.trader.value.set", "Valor de %s establecido en %s.");
                add("command.marketblocks.trader.value.remove", "Valor eliminado para %s.");
                add("command.marketblocks.trader.blacklist.add", "A\u00f1adido %s a la lista negra.");
                add("command.marketblocks.trader.blacklist.remove", "Eliminado %s de la lista negra.");
                add("command.marketblocks.sale.set.success", "Oferta activada para [%s]: Cambio de precio %s (Duraci\u00f3n: %s min)");
                add("command.marketblocks.sale.remove.success", "Oferta finalizada para [%s].");
                add("command.marketblocks.sale.not_found", "Oferta / Tienda no encontrada: %s");
                add("command.marketblocks.sale.failed", "Error al modificar la oferta.");
                add("command.marketblocks.stats.shop.header", "--- Top 10 SingleOfferShops ---");
                add("command.marketblocks.stats.shop", "Estad\u00edsticas de la tienda: %s");
                add("command.marketblocks.stats.shop.empty", "No hay tiendas disponibles.");
                add("command.marketblocks.stats.shop.entry", "%s. %s - %s Ventas");
                add("command.marketblocks.stats.shop.total_sales", "Ventas totales: %d");
                add("command.marketblocks.stats.marketplace.header", "--- Top 10 Ofertas de Mercado ---");
                add("command.marketblocks.stats.marketplace.empty", "No hay ofertas disponibles.");
                add("command.marketblocks.stats.marketplace.entry", "%s. %s - %s Ventas");

                // === Trade Book GUI Translations ===
                add("gui.marketblocks.trade_book.title", "Libro de comercio");
                add("gui.marketblocks.trade_book.toc.header", "=== LIBRO DE COMERCIO ===\n\n");
                add("gui.marketblocks.trade_book.toc.subheader", "Informe econ\u00f3mico del mercado.\n\n");
                add("gui.marketblocks.trade_book.toc.my_shops", "\ud83c\udfe0 Mis tiendas");
                add("gui.marketblocks.trade_book.toc.my_shops.tooltip", "Tu resumen personal de tiendas");
                add("gui.marketblocks.trade_book.toc.trends", "\ud83d\udcca Tendencias NPC");
                add("gui.marketblocks.trade_book.toc.trends.tooltip", "Oferta y demanda de NPC");
                add("gui.marketblocks.trade_book.toc.shop_stats", "\ud83c\udfc6 Tabla de clasificaci\u00f3n");
                add("gui.marketblocks.trade_book.toc.shop_stats.tooltip", "Clasificaci\u00f3n de tiendas");
                add("gui.marketblocks.trade_book.toc.market_stats", "\ud83c\udfdb M\u00e1s vendidos de mercado");
                add("gui.marketblocks.trade_book.toc.market_stats.tooltip", "Estad\u00edsticas de mercado");
                add("gui.marketblocks.trade_book.toc.active_shops", "\ud83d\udccd Puestos de venta");
                add("gui.marketblocks.trade_book.toc.active_shops.tooltip", "Puestos de venta activos");
                add("gui.marketblocks.trade_book.my_shops.title", "=== Mis Tiendas ===\n\n");
                add("gui.marketblocks.trade_book.my_shops.empty", "A\u00fan no tienes tiendas.\n\u00a1Coloca un Puesto o una Caja para empezar!");
                add("gui.marketblocks.trade_book.my_shops.summary", "Tiendas: %s (Abiertas: %s | Cerradas: %s)\nVentas totales: %s\n\n");
                add("gui.marketblocks.trade_book.my_shops.sales_count", "  Ventas: %s\n");
                add("gui.marketblocks.trade_book.my_shops.sells", "  Vende: %s\n");
                add("gui.marketblocks.trade_book.trends.title", "=== Tendencias NPC ===\n\n");
                add("gui.marketblocks.trade_book.trends.hover", "Valor base: %s Esmeraldas\nCompra de NPC: %s Esmeraldas");
                add("gui.marketblocks.trade_book.trends.stable", "\nNo hay tendencias de mercado activas.");
                add("gui.marketblocks.trade_book.shops.title", "=== M\u00e1s Vendidos ===\n\n");
                add("gui.marketblocks.trade_book.shops.empty", "No hay tiendas activas en el servidor.");
                add("gui.marketblocks.trade_book.shops.entry", "%s. %s:\n");
                add("gui.marketblocks.trade_book.shops.sales", "   Ventas: %s\n");
                add("gui.marketblocks.trade_book.shops.owner_sales", "  %s | Ventas: %s\n");
                add("gui.marketblocks.trade_book.shops.sales_only", "  Ventas: %s\n");
                add("gui.marketblocks.trade_book.shops.player_stats", "Tiendas: %s | Ventas: %s\n");
                add("gui.marketblocks.trade_book.marketplace.title", "=== Top de Mercado ===\n\n");
                add("gui.marketblocks.trade_book.marketplace.empty", "No hay ventas en el mercado.");
                add("gui.marketblocks.trade_book.marketplace.entry", "%s. %s:\n");
                add("gui.marketblocks.trade_book.marketplace.sales", "   Ventas: %s");
                add("gui.marketblocks.trade_book.marketplace.sale_active", "   \u2605 OFERTA: %s\n");
                add("gui.marketblocks.trade_book.active.title", "=== Puestos de Venta ===\n\n");
                add("gui.marketblocks.trade_book.active.empty", "No hay puestos activos en el servidor.");
                add("gui.marketblocks.trade_book.active.owner", "  Propietario: %s\n");
                add("gui.marketblocks.trade_book.active.sells", "  Vende: %s\n");
                add("gui.marketblocks.trade_book.active.hover_tp", "Haz clic para teletransportarte");
                add("gui.marketblocks.trade_book.active.unknown_owner", "Desconocido");
                add("gui.marketblocks.shop.default_name", "Tienda #%s");
                add("gui.marketblocks.shop.named_format", "%s (#%s)");
                add("gui.marketblocks.trade_book.active.closed", "Cerrado");
                add("gui.marketblocks.trade_book.active.open", "Abierto");
                add("gui.marketblocks.trade_book.active.no_offer", "Sin oferta definida");

                add("gui.marketblocks.trade_book.status.out_of_stock", " \u00a7c\u26a0 Agotado");
                add("gui.marketblocks.trade_book.status.output_full", " \u00a76\u26a0 Salida llena");

                // === Trade Book Guide ===
                add("gui.marketblocks.trade_book.toc.guide.intro", "Introducci\u00f3n");
                add("gui.marketblocks.trade_book.toc.guide.intro.tooltip", "Lee sobre los conceptos b\u00e1sicos de MarketBlocks");
                add("gui.marketblocks.trade_book.toc.guide.visuals", "Personalizaci\u00f3n visual");
                add("gui.marketblocks.trade_book.toc.guide.visuals.tooltip", "Aprende a personalizar tus tiendas visualmente");
                add("gui.marketblocks.trade_book.toc.guide.setup", "Configuraci\u00f3n y mec\u00e1nicas");
                add("gui.marketblocks.trade_book.toc.guide.setup.tooltip", "Aprende sobre la interfaz, Redstone y Tolvas");
                add("gui.marketblocks.trade_book.toc.guide.advanced", "Funciones avanzadas");
                add("gui.marketblocks.trade_book.toc.guide.advanced.tooltip", "Aprende sobre copropietarios, econom\u00eda y funciones de administrador");

                add("gui.marketblocks.trade_book.guide.intro.title", "=== Introducci\u00f3n ===\n\n");
                add("gui.marketblocks.trade_book.guide.intro.text", "\u00a1Bienvenido a MarketBlocks!\n\nEste mod te permite construir una econom\u00eda pr\u00f3spera. Puedes crear varias tiendas para intercambiar objetos con otros jugadores o compradores NPC. Veamos los bloques de tiendas disponibles.");

                add("gui.marketblocks.trade_book.guide.tradestand.title", "=== Puesto de comercio ===\n\n");
                add("gui.marketblocks.trade_book.guide.tradestand.text", "Una tienda abierta que muestra el objeto vendido flotando encima. \u00a1A los NPC les encantan estos puestos!");

                add("gui.marketblocks.trade_book.guide.marketcrate.title", "=== Caja de mercado ===\n\n");
                add("gui.marketblocks.trade_book.guide.marketcrate.text", "Una variante de tienda compacta sin visualizaci\u00f3n de objeto flotante. Ideal para espacios reducidos.");

                add("gui.marketblocks.trade_book.guide.visuals.title", "=== Visuales ===\n\n");
                add("gui.marketblocks.trade_book.guide.visuals.text", "\u00a1Las tiendas se pueden personalizar visualmente!\n\nUsa una llave inglesa para rotar el bloque. Si activas el 'Aldeano decorativo' en la configuraci\u00f3n, \u00a1un NPC amigable estar\u00e1 detr\u00e1s del bloque de la tienda!");

                add("gui.marketblocks.trade_book.guide.setup.title", "=== Configuraci\u00f3n e interfaz ===\n\n");
                add("gui.marketblocks.trade_book.guide.setup.text", "Haz clic derecho en tu tienda para abrir la configuraci\u00f3n.\n\nPuedes establecer el precio, el objeto que vendes y llenar el inventario. Tambi\u00e9n puedes configurar l\u00edmites para los compradores.");

                add("gui.marketblocks.trade_book.guide.mechanics.title", "=== Mec\u00e1nicas ===\n\n");
                add("gui.marketblocks.trade_book.guide.mechanics.redstone", "Las tiendas emiten una se\u00f1al de Redstone seg\u00fan lo llenas que est\u00e9n o si est\u00e1n agotadas. Puedes configurar esto en la pesta\u00f1a Redstone.");
                add("gui.marketblocks.trade_book.guide.mechanics.hopper", "\u00a1Puedes usar tolvas para automatizar el reabastecimiento y extraer las ganancias! Configura los ajustes de entrada/salida en la pesta\u00f1a Auto-IO.");

                add("gui.marketblocks.trade_book.guide.advanced.title", "=== Funciones avanzadas ===\n\n");
                add("gui.marketblocks.trade_book.guide.advanced.text", "Puedes agregar copropietarios para administrar tu tienda.\n\n\u00a1Los compradores NPC tienen su propia econom\u00eda con tendencias! Si todos venden madera, el precio baja. Mantente atento a la p\u00e1gina de Tendencias.");

                // NPC Economy Guide Page (G4)
                add("gui.marketblocks.trade_book.guide.economy.title", "=== Econom\u00eda NPC ===\n\n");
                add("gui.marketblocks.trade_book.guide.economy.text", "\u00a1Los compradores NPC visitan tus tiendas y compran art\u00edculos si la oferta es buena!\n\n\u00a76Precios:\u00a7r Los objetos tienen valores base. Los objetos crafteados valen m\u00e1s (+10% por paso de crafteo).\n\n\u00a76Oferta y demanda:\u00a7r Si un objeto se vende a menudo, su valor NPC baja. Con el tiempo, los precios se recuperan.\n\n\u00a76Rangos NPC:\u00a7r\n\u2022 \u00a77Ciudadano\u00a7r \u2013 Presupuesto bajo\n\u2022 \u00a7eAdinerado\u00a7r \u2013 Presupuesto medio\n\u2022 \u00a76Noble\u00a7r \u2013 Gran presupuesto, compra objetos raros\n\n\u00a76Intereses:\u00a7r Cada NPC tiene una especializaci\u00f3n (Granjero, Herrero, Alquimista, Coleccionista). \u00a1Gastan m\u00e1s en su categor\u00eda preferida!");

                // === Block Registrations ===
                addBlock(RegistriesInit.MARKETCRATE_BLOCK, "Caja de mercado");
                addBlock(RegistriesInit.TRADE_STAND_BLOCK, "Puesto de comercio");

        }
}
