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

                // === Containers & Menus ===
                add("container.marketblocks.trade_stand", "Puesto de comercio");
                add("menu.marketblocks.marketplace", "Mercado");

                // === Keybinds ===
                add("key.categories.marketblocks", "MarketBlocks");
                add("key.marketblocks.open_marketplace", "Abrir el mercado");

                // === Commands ===
                add("command.marketblocks.break.denied", "§c¡No puedes romper este bloque mientras esté vinculado a un mercado!");
                add("command.marketblocks.break.unlinked", "§eEl bloque ha sido desvinculado del mercado.");
                add("command.marketblocks.link.already_linked", "§cEste bloque ya está vinculado a un mercado.");
                add("command.marketblocks.link.not_looking_at_block", "§cDebes mirar un bloque para vincularlo.");
                add("command.marketblocks.link.success", "§a¡Bloque vinculado al mercado exitosamente!");
                add("command.marketblocks.list.click_to_delete", "Haz clic para eliminar");
                add("command.marketblocks.list.click_to_teleport", "Haz clic para teletransportarte");
                add("command.marketblocks.list.click_to_waypoint", "Haz clic para obtener enlaces de Waypoint en el chat");
                add("command.marketblocks.list.delete", "[Eliminar]");
                add("command.marketblocks.list.tp", "[Teletransporte]");
                add("command.marketblocks.list.waypoint", "[Waypoint]");
                add("command.marketblocks.list.page_header", "§8======== §6§lTiendas (Página %s/%s) §8========");
                add("command.marketblocks.list.prev", "[< Ant]");
                add("command.marketblocks.list.next", "[Sig >]");
                add("command.marketblocks.marketplacelist.entry", "§8▪ §6Mercado §e%s");
                add("command.marketblocks.marketplacelist.page_header", "§8======== §6§lMercados (Página %s/%s) §8========");
                add("command.marketblocks.marketplacelist.no_links", "§cNo se encontraron mercados.");
                add("command.marketblocks.player_not_found", "§cJugador no encontrado");
                add("command.marketblocks.reload.success", "§a¡Configuración de MarketBlocks recargada exitosamente!");
                add("command.marketblocks.resetlimits.no_changes", "§eNo se restablecieron límites diarios.");
                add("command.marketblocks.resetlimits.success", "§aLímites diarios restablecidos exitosamente.");
                add("command.marketblocks.search.header", "§8======== §6§lTiendas vendiendo %s (Página %s/%s) §8========");
                add("command.marketblocks.search.no_shops", "§cNo se encontraron tiendas ni mercados vendiendo %s.");
                add("command.marketblocks.shoplist.closed", "CERRADO");
                add("command.marketblocks.shoplist.entry", "§8▪ §7[%s§7] §e%s §8(por §7%s§8)");
                add("command.marketblocks.shoplist.header", "§8======== §6§lTiendas de MarketBlocks §8========");
                add("command.marketblocks.shoplist.no_shops", "§cNo hay tiendas disponibles.");
                add("command.marketblocks.shoplist.open", "ABIERTO");
                add("command.marketblocks.shoplist.hover.shop", "Tienda: %s");
                add("command.marketblocks.shoplist.hover.owner", "Propietario: %s");
                add("command.marketblocks.shoplist.hover.status", "Estado: %s");
                add("command.marketblocks.shoplist.hover.offer", "Oferta:");
                add("command.marketblocks.shoplist.hover.arrow", "➔");
                add("command.marketblocks.unlink.not_found", "§cNo se pudo encontrar el enlace del mercado.");
                add("command.marketblocks.unlink.not_linked", "§cEste bloque no está vinculado.");
                add("command.marketblocks.unlink.not_looking_at_block", "§cDebes mirar un bloque para desvincularlo.");
                add("command.marketblocks.unlink.success", "§a¡Bloque desvinculado exitosamente!");
                add("command.marketblocks.unlink.success_name", "§aDesvinculado del mercado: §e%s");
                add("command.marketblocks.waypoint.created", "§aEnlaces de Waypoint creados:");

                // === GUI - Settings ===
                add("gui.marketblocks.access.edit_access_list", "Lista de acceso");
                add("gui.marketblocks.access.edit_owners", "Propietarios");
                add("gui.marketblocks.access.mode.blacklist", "Modo: Lista negra");
                add("gui.marketblocks.access.mode.everyone", "Modo: Todos");
                add("gui.marketblocks.access.mode.whitelist", "Modo: Lista blanca");
                add("gui.marketblocks.io.allow_io", "Permitir I/O");
                add("gui.marketblocks.io.allow_io.tooltip", "Permite a las tolvas y tuberías interactuar con este lado.");
                add("gui.marketblocks.io.auto_io", "Auto Push/Pull");
                add("gui.marketblocks.io.auto_io.tooltip", "Extrae e inserta objetos automáticamente de inventarios adyacentes.");
                add("gui.marketblocks.io.redstone_control.ignored", "Ignorado");
                add("gui.marketblocks.io.redstone_control.require_no_signal", "Bajo (Sin señal)");
                add("gui.marketblocks.io.redstone_control.require_signal", "Alto (Requiere señal)");
                add("gui.marketblocks.io.redstone_control.tooltip", "Modo de control de redstone para este lado.");
                add("gui.marketblocks.settings_owner_only", "Solo el propietario puede cambiar la configuración");
                add("gui.marketblocks.settings_tab", "Mostrar configuración");
                add("gui.marketblocks.settings_title", "Configuración de la tienda");
                add("gui.marketblocks.toggle.off", "OFF");
                add("gui.marketblocks.toggle.on", "ON");
                add("gui.marketblocks.settings.category.access", "Acceso");
                add("gui.marketblocks.settings.category.general", "General");
                add("gui.marketblocks.settings.category.io", "I/O");
                add("gui.marketblocks.settings.category.notifications", "Notificaciones");
                add("gui.marketblocks.settings.category.villager", "NPC");
                add("gui.marketblocks.settings.category.visual", "Visual");
                add("gui.marketblocks.visuals.bobbing", "Flotación");
                add("gui.marketblocks.visuals.chaos_rotation", "Rotación caótica");
                add("gui.marketblocks.visuals.count", "Cantidad de objetos");
                add("gui.marketblocks.visuals.dynamic_fill_level", "Llenado dinámico");
                add("gui.marketblocks.visuals.error.no_surface", "¡No hay superficie de puesto detrás de la tienda!");
                add("gui.marketblocks.visuals.error.space_blocked", "¡Espacio bloqueado!");
                add("gui.marketblocks.visuals.height", "Altura");
                add("gui.marketblocks.visuals.layout_mode", "Modo de diseño");
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
                add("gui.marketblocks.visuals.player_skin_name", "Nombre del jugador");
                add("gui.marketblocks.visuals.profession", "Profesión");
                add("gui.marketblocks.visuals.profession.armorer", "Herrero de armaduras");
                add("gui.marketblocks.visuals.profession.butcher", "Carnicero");
                add("gui.marketblocks.visuals.profession.cartographer", "Cartógrafo");
                add("gui.marketblocks.visuals.profession.cleric", "Clérigo");
                add("gui.marketblocks.visuals.profession.farmer", "Granjero");
                add("gui.marketblocks.visuals.profession.fisherman", "Pescador");
                add("gui.marketblocks.visuals.profession.fletcher", "Flechero");
                add("gui.marketblocks.visuals.profession.leatherworker", "Peletero");
                add("gui.marketblocks.visuals.profession.librarian", "Bibliotecario");
                add("gui.marketblocks.visuals.profession.mason", "Albañil");
                add("gui.marketblocks.visuals.profession.nitwit", "Bobo");
                add("gui.marketblocks.visuals.profession.none", "Desempleado");
                add("gui.marketblocks.visuals.profession.shepherd", "Pastor");
                add("gui.marketblocks.visuals.profession.toolsmith", "Herrero de herramientas");
                add("gui.marketblocks.visuals.profession.weaponsmith", "Herrero de armas");
                add("gui.marketblocks.visuals.purchase_particles", "Partículas de compra");
                add("gui.marketblocks.visuals.purchase_sounds", "Sonidos de compra");
                add("gui.marketblocks.visuals.rotation", "Rotación");
                add("gui.marketblocks.visuals.rotation_x", "Rotación X");
                add("gui.marketblocks.visuals.rotation_y", "Rotación Y");
                add("gui.marketblocks.visuals.rotation_z", "Rotación Z");
                add("gui.marketblocks.visuals.scale", "Escala");
                add("gui.marketblocks.visuals.spacing_xz", "Espaciado X/Z");
                add("gui.marketblocks.visuals.spacing_y", "Espaciado Y");
                add("gui.marketblocks.visuals.speed", "Velocidad");
                add("gui.marketblocks.visuals.use_player_skin", "Apariencia de jugador");
                add("gui.marketblocks.visuals.use_player_skin.tooltip", "Muestra un jugador en lugar de un aldeano.");

                // === GUI - Notifications ===
                add("gui.marketblocks.notifications.co_owners", "Notificar a los copropietarios");
                add("gui.marketblocks.notifications.co_owners.tooltip", "También envía notificaciones a los propietarios adicionales de la tienda.");
                add("gui.marketblocks.notifications.out_of_stock", "Alerta de agotamiento");
                add("gui.marketblocks.notifications.out_of_stock.tooltip", "Recibe una advertencia cuando la tienda se quede sin existencias.");
                add("gui.marketblocks.notifications.output_full", "Alerta de almacenamiento lleno");
                add("gui.marketblocks.notifications.output_full.tooltip", "Recibe una advertencia cuando el inventario de salida de la tienda esté lleno.");
                add("gui.marketblocks.notifications.purchase", "Notificaciones de compra");
                add("gui.marketblocks.notifications.purchase.tooltip", "Recibe una notificación en el chat cuando alguien compre en tu tienda.");
                add("message.marketblocks.notifications.out_of_stock", "§c¡Tu tienda está agotada!§r");
                add("message.marketblocks.notifications.output_full", "§c¡El inventario de salida de tu tienda está lleno!§r");
                add("message.marketblocks.notifications.purchase", "§a%s compró %sx %s en tu tienda.§r");

                // === GUI - Marketplace Editors ===
                add("gui.marketblocks.marketplace.editor.limits.daily", "Límite diario");
                add("gui.marketblocks.marketplace.editor.limits.restock", "Reabastecimiento (s)");
                add("gui.marketblocks.marketplace.editor.limits.stock", "Límite de stock");
                add("gui.marketblocks.marketplace.editor.limits.title", "Límites");
                add("gui.marketblocks.marketplace.editor.limits.daily.tooltip", "Compras máximas que un jugador puede hacer por día.");
                add("gui.marketblocks.marketplace.editor.limits.stock.tooltip", "Stock total disponible para esta oferta.");
                add("gui.marketblocks.marketplace.editor.limits.restock.tooltip", "Tiempo en segundos hasta que se reponga el stock.");
                add("gui.marketblocks.marketplace.editor.pricing.disabled", "Fijación de precios OFF");
                add("gui.marketblocks.marketplace.editor.pricing.enabled", "Fijación de precios ON");
                add("gui.marketblocks.marketplace.editor.pricing.label", "Activar fijación de precios");
                add("gui.marketblocks.marketplace.editor.pricing.label.tooltip", "Si se activa, el precio se ajusta dinámicamente según la temperatura del mercado.");
                add("gui.marketblocks.marketplace.editor.pricing.max", "Precio máx (%)");
                add("gui.marketblocks.marketplace.editor.pricing.max.tooltip", "Porcentaje máximo que puede alcanzar el precio (ej. 200 = precio duplicado).");
                add("gui.marketblocks.marketplace.editor.pricing.min", "Precio mín (%)");
                add("gui.marketblocks.marketplace.editor.pricing.min.tooltip", "Porcentaje mínimo al que puede caer el precio (ej. 50 = mitad de precio).");
                add("gui.marketblocks.marketplace.editor.pricing.volatility", "Volatilidad");
                add("gui.marketblocks.marketplace.editor.pricing.volatility.tooltip", "Qué tan rápido reacciona el precio a las compras y al tiempo.");
                add("gui.marketblocks.marketplace.editor.pricing.volatility.slow", "Lenta");
                add("gui.marketblocks.marketplace.editor.pricing.volatility.normal", "Normal");
                add("gui.marketblocks.marketplace.editor.pricing.volatility.fast", "Rápida");
                add("gui.marketblocks.marketplace.editor.pricing.title", "Fijación de precios");

                // === GUI - Marketplace Elements ===
                add("gui.marketblocks.marketplace.add_offer", "Añadir oferta");
                add("gui.marketblocks.marketplace.add_page", "Añadir página");
                add("gui.marketblocks.marketplace.delete_offer", "Eliminar oferta");
                add("gui.marketblocks.marketplace.delete_page", "Eliminar página");
                add("gui.marketblocks.marketplace.inline.limits", "Editar límites");
                add("gui.marketblocks.marketplace.inline.pricing", "Editar fijación de precios");
                add("gui.marketblocks.marketplace.inline.pricing.disabled_global", "Desactivado: La fijación de precios global está activa en la configuración del servidor");
                add("gui.marketblocks.marketplace.mode.edit", "Cambiar al modo edición");
                add("gui.marketblocks.marketplace.mode.view", "Cambiar al modo vista");
                add("gui.marketblocks.marketplace.move_offer", "Mover oferta");
                add("gui.marketblocks.marketplace.move_offer_down", "Mover oferta abajo");
                add("gui.marketblocks.marketplace.move_offer_up", "Mover oferta arriba");
                add("gui.marketblocks.marketplace.no_offers", "Sin ofertas");
                add("gui.marketblocks.marketplace.no_pages", "Sin páginas\ndisponibles");
                add("gui.marketblocks.marketplace.rename_page", "Renombrar página");
                add("gui.marketblocks.marketplace.status.daily_short", "D:%s");
                add("gui.marketblocks.marketplace.status.price_short", "x%s");
                add("gui.marketblocks.marketplace.status.restock_short", "R:%s");
                add("gui.marketblocks.marketplace.status.stock_short", "S:%s");
                add("gui.marketblocks.marketplace.tooltip.price_multiplier", "Factor de precio.");
                add("gui.marketblocks.marketplace.tooltip.remaining_daily", "Tus compras restantes hoy.");
                add("gui.marketblocks.marketplace.tooltip.remaining_daily_empty", "Límite diario alcanzado.");
                add("gui.marketblocks.marketplace.tooltip.remaining_stock", "Compras de stock restantes.");
                add("gui.marketblocks.marketplace.tooltip.remaining_stock_empty", "Agotado.");
                add("gui.marketblocks.marketplace.tooltip.restock_in", "Tiempo hasta el reabastecimiento.");
                add("gui.marketblocks.marketplace.tooltip.restock_ready", "Reabastecimiento listo.");
                add("gui.marketblocks.marketplace.tooltip.unavailable_daily", "No disponible: límite diario alcanzado.");
                add("gui.marketblocks.marketplace.tooltip.unavailable_generic", "No disponible.");
                add("gui.marketblocks.marketplace.tooltip.unavailable_restock", "No disponible: reabasteciendo.");
                add("gui.marketblocks.marketplace.tooltip.unavailable_stock", "No disponible: agotado.");
                add("gui.marketblocks.marketplace.unnamed_page", "Página %s");

                // === GUI - General ===
                add("gui.marketblocks.admin_shop.disabled", "Admin-Shop: OFF");
                add("gui.marketblocks.admin_shop.enabled", "Admin-Shop: ON");
                add("gui.marketblocks.category", "Categoría");
                add("gui.marketblocks.category.none", "Ninguna");
                add("gui.marketblocks.category.weapons_armor", "Armas y Armaduras");
                add("gui.marketblocks.category.tools", "Herramientas");
                add("gui.marketblocks.category.blocks", "Bloques");
                add("gui.marketblocks.category.food_potions", "Comida y Pociones");
                add("gui.marketblocks.category.valuables", "Objetos de valor");
                add("gui.marketblocks.category.misc", "Varios");
                add("gui.marketblocks.category.tooltip", "Categoría bajo la cual listar la tienda en el directorio global");
                add("gui.marketblocks.create_offer", "Crear oferta");
                add("gui.marketblocks.delete_offer", "Eliminar oferta");
                add("gui.marketblocks.disabled", "Desactivado");
                add("gui.marketblocks.emit_redstone", "Emitir Redstone");
                add("gui.marketblocks.emit_redstone.tooltip", "Emite un pulso corto de redstone después de una compra");
                add("gui.marketblocks.error.invalid_offer", "Configuración de oferta no válida");
                add("gui.marketblocks.error.no_payment_items", "Por favor, coloca al menos un objeto de pago");
                add("gui.marketblocks.error.no_result_item", "Por favor, coloca un objeto en la ranura de resultado");
                add("gui.marketblocks.input", "Entrada");
                add("gui.marketblocks.inventory_admin_disabled", "Inventario desactivado en modo admin");
                add("gui.marketblocks.inventory_owner_only", "Solo el propietario puede gestionar el inventario");
                add("gui.marketblocks.inventory_tab", "Mostrar inventario");
                add("gui.marketblocks.inventory_title", "Inventario del puesto");
                add("gui.marketblocks.log_tab", "Mostrar registro");
                add("gui.marketblocks.log_title", "Registro de transacciones");
                add("gui.marketblocks.log.clear", "Limpiar registro");
                add("gui.marketblocks.log.count", "Entradas: %s");
                add("gui.marketblocks.log.empty", "Sin transacciones aún");
                add("gui.marketblocks.log.none", "Ninguno");
                add("gui.marketblocks.log.time.days", "hace %s d");
                add("gui.marketblocks.log.time.hours", "hace %s h");
                add("gui.marketblocks.log.time.just_now", "Ahora mismo");
                add("gui.marketblocks.log.time.minutes", "hace %s min");
                add("gui.marketblocks.log.time.seconds", "hace %s s");
                add("gui.marketblocks.mode.edit_active", "MODO EDICIÓN");
                add("gui.marketblocks.no_players_available", "Sin jugadores disponibles");
                add("gui.marketblocks.offers", "Ofertas");
                add("gui.marketblocks.offers_tab", "Mostrar ofertas");
                add("gui.marketblocks.out_of_stock", "Agotado");
                add("gui.marketblocks.output", "Salida");
                add("gui.marketblocks.output_full", "Salida llena");
                add("gui.marketblocks.owner", "Propietario: %s");
                add("gui.marketblocks.purchase_xp_sound", "Sonido de XP al comprar");
                add("gui.marketblocks.purchase_xp_sound.tooltip", "Reproduce un sonido de orbe de XP cuando un jugador compra algo");
                add("gui.marketblocks.save", "Guardar");
                add("gui.marketblocks.shop_closed", "Tienda en pausa");
                add("gui.marketblocks.shop_closed.tooltip", "Si está activa, solo los propietarios pueden comprar objetos.");
                add("gui.marketblocks.shop_name", "Nombre de la tienda");
                add("gui.marketblocks.shop_title", "Puesto de comercio");
                add("gui.marketblocks.side.back", "Atrás");
                add("gui.marketblocks.side.bottom", "Abajo");
                add("gui.marketblocks.side.left", "Izquierda");
                add("gui.marketblocks.side.right", "Derecha");

                // === Messages & Chat ===
                add("message.marketblocks.marketplace.daily_limit_reached", "Se ha alcanzado el límite diario para esta oferta.");
                add("message.marketblocks.marketplace.edit_mode_disabled", "Modo edición del mercado desactivado.");
                add("message.marketblocks.marketplace.edit_mode_enabled", "Modo edición del mercado activado.");
                add("message.marketblocks.marketplace.limits.invalid_data", "No se pudieron guardar los límites: datos no válidos.");
                add("message.marketblocks.marketplace.limits.invalid_positive_int", "Por favor, introduce solo números enteros positivos para los límites.");
                add("message.marketblocks.marketplace.limits.no_connection", "No se pudieron guardar los límites: sin conexión con el servidor.");
                add("message.marketblocks.marketplace.page_name_blank", "El nombre de la página no debe estar vacío.");
                add("message.marketblocks.marketplace.page_name_duplicate", "Ya existe una página con el nombre '%s'.");
                add("message.marketblocks.marketplace.page_name_too_long", "El nombre de la página debe tener como máximo %s caracteres.");
                add("message.marketblocks.marketplace.page_not_found", "No se pudo encontrar la página de tienda seleccionada.");
                add("message.marketblocks.marketplace.pricing.invalid_data", "No se pudo guardar la fijación de precios: datos no válidos.");
                add("message.marketblocks.marketplace.pricing.invalid_finite", "Por favor, introduce números finitos válidos para la fijación de precios.");
                add("message.marketblocks.marketplace.pricing.invalid_number_format", "Por favor, utiliza solo números (se permite punto o coma).");
                add("message.marketblocks.marketplace.pricing.no_connection", "No se pudo guardar la fijación de precios: sin conexión con el servidor.");
                add("message.marketblocks.trade_stand.no_offer", "Este puesto de comercio actualmente no tiene ninguna oferta activa.");
                add("message.marketblocks.trade_stand.not_owner", "Solo el propietario puede romper este puesto de comercio.");
                add("message.marketblocks.trade_stand.break_not_empty", "¡Debes vaciar primero todos los objetos y pagos!");
                add("message.marketblocks.shop.limit_reached", "¡Puedes colocar un máximo de %s tiendas!");
                
                add("message.marketblocks.shop_buyer.interact.1", "§e¡Estoy buscando buenos artículos para comprar!§r");
                add("message.marketblocks.shop_buyer.interact.2", "§e¿Tienes algo interesante a la venta?§r");
                add("message.marketblocks.shop_buyer.interact.3", "§eViajo para hacer negocios. ¡Quizás tengas lo que necesito!§r");
                add("message.marketblocks.shop_buyer.interact.4", "§e¡Acabo de encontrar una gran oferta! ¡Me encanta comprar aquí!§r");
                add("message.marketblocks.shop_buyer.interact.5", "§e¡Otra buena compra! Mi bolso empieza a pesar.§r");
                add("message.marketblocks.shop_buyer.interact.6", "§e¡Encontré todo lo que necesitaba gracias a estas tiendas!§r");
                add("message.marketblocks.shop_buyer.interact.7", "§eHmm, estoy buscando algo en específico...§r");
                add("message.marketblocks.shop_buyer.interact.8", "§eMe pregunto qué otras tiendas habrá por aquí...§r");
                add("message.marketblocks.shop_buyer.interact.9", "§eSolo estoy mirando por ahora. Nada me ha llamado la atención aún.§r");
                add("message.marketblocks.shop_buyer.interact.10", "§e¡Bonita tienda tienes aquí! La tendré en cuenta.§r");

                // === Jade / Waila Support ===
                add("config.jade.plugin_marketblocks.shop_info", "Información de la tienda");
                add("marketblocks.jade.for", "Por:");
                add("marketblocks.jade.out_of_stock", "¡Agotado!");
                add("marketblocks.jade.output_full", "¡Inventario lleno!");
                add("marketblocks.jade.owner", "Propietario: %s");
                add("marketblocks.jade.shop", "Tienda: %s");
                add("marketblocks.jade.selling", "Vendiendo:");
                add("marketblocks.jade.status.admin_shop", "Admin Shop");
                add("marketblocks.jade.status.closed", "Tienda cerrada");
                add("marketblocks.jade.trader.budget", "Presupuesto: %s");

                // === Advancements ===
                add("advancements.marketblocks.admin_shop.description", "Activer el modo admin shop");
                add("advancements.marketblocks.admin_shop.title", "Bienes Infinitos");
                add("advancements.marketblocks.auto_io.description", "Activar la entrada/salida automática para tu tienda");
                add("advancements.marketblocks.auto_io.title", "Logística");
                add("advancements.marketblocks.custom_npc.description", "Personalizar el NPC de tu tienda con un nombre o apariencia de jugador");
                add("advancements.marketblocks.custom_npc.title", "Personal a medida");
                add("advancements.marketblocks.first_shop.description", "Colocar tu primer bloque de tienda MarketBlocks");
                add("advancements.marketblocks.first_shop.title", "Abierto para los negocios");
                add("advancements.marketblocks.hiring.description", "Activar un NPC para tu tienda");
                add("advancements.marketblocks.hiring.title", "Contratación en curso");
                add("advancements.marketblocks.joint_venture.description", "Añadir un copropietario a tu tienda");
                add("advancements.marketblocks.joint_venture.title", "Empresa conjunta");
                add("advancements.marketblocks.marketplace_buy.description", "Comprar un objeto a través del mercado");
                add("advancements.marketblocks.marketplace_buy.title", "Cliente del centro comercial");
                add("advancements.marketblocks.out_of_stock.description", "Hacer que una tienda (no-admin) se quede sin existencias");
                add("advancements.marketblocks.out_of_stock.title", "Agotado");
                add("advancements.marketblocks.redstone.description", "Activar la salida de redstone o I/O controlados por redstone");
                add("advancements.marketblocks.redstone.title", "Lógica de Redstone");
                add("advancements.marketblocks.root.description", "Obtener un bloque de tienda MarketBlocks");
                add("advancements.marketblocks.root.title", "MarketBlocks");
                add("advancements.marketblocks.showcase.description", "Añadir una vitrina de cristal a un puesto de comercio");
                add("advancements.marketblocks.showcase.title", "Exhibición");
                add("advancements.marketblocks.sold_item.description", "Vender tu primer objeto a otro jugador");
                add("advancements.marketblocks.sold_item.title", "¡Primera venta!");
                add("advancements.marketblocks.tycoon.description", "Vender 100 objetos a través de tus tiendas");
                add("advancements.marketblocks.tycoon.title", "Magnate");
                add("advancements.marketblocks.wall_street.description", "Abrir el mercado");
                add("advancements.marketblocks.wall_street.title", "Wall Street");
                add("advancements.marketblocks.wholesaler.description", "Comprar 64 objetos o más en una sola transacción");
                add("advancements.marketblocks.wholesaler.title", "Mayorista");

                // === Subtitles ===
                add("subtitles.marketblocks.visual_npc_fall", "Aldeano aterriza");

                // === Login Notifications ===
                add("gui.marketblocks.notifications.login.out_of_stock", "§c[MarketBlocks] ¡%s de tus tiendas están agotadas!§r");
                add("gui.marketblocks.notifications.login.output_full", "§c[MarketBlocks] ¡%s de tus tiendas tienen el almacenamiento de salida lleno!§r");
                add("gui.marketblocks.notifications.login.coordinate", "§7 - Localización: X: %s, Y: %s, Z: %s§r");

                // === Purchase Confirmations ===
                add("message.marketblocks.purchase_success", "Has comprado con éxito %s x %s.");
                add("message.marketblocks.purchase_success.global", "%s ha comprado %s x %s.");

                // === Admin Commands ===
                add("command.marketblocks.trader.value.set", "Valor de %s establecido en %s.");
                add("command.marketblocks.trader.value.remove", "Valor eliminado para %s.");
                add("command.marketblocks.trader.blacklist.add", "Añadido %s a la lista negra.");
                add("command.marketblocks.trader.blacklist.remove", "Eliminado %s de la lista negra.");
                add("command.marketblocks.sale.set.success", "Oferta activada para [%s]: Cambio de precio %s (Duración: %s min)");
                add("command.marketblocks.sale.remove.success", "Oferta finalizada para [%s].");
                add("command.marketblocks.sale.not_found", "Oferta / Tienda no encontrada: %s");
                add("command.marketblocks.sale.failed", "Error al modificar la oferta.");
                add("command.marketblocks.stats.shop.header", "--- Top 10 SingleOfferShops ---");
                add("command.marketblocks.stats.shop.empty", "No hay tiendas disponibles.");
                add("command.marketblocks.stats.shop.unnamed", "Sin nombre");
                add("command.marketblocks.stats.shop.entry", "%s. %s - %s Ventas");
                add("command.marketblocks.stats.marketplace.header", "--- Top 10 Ofertas de Mercado ---");
                add("command.marketblocks.stats.marketplace.empty", "No hay ofertas disponibles.");
                add("command.marketblocks.stats.marketplace.entry", "%s. %s - %s Ventas");

                // === Block Registrations ===
                addBlock(RegistriesInit.MARKETCRATE_BLOCK, "Caja de mercado");
                addBlock(RegistriesInit.TRADE_STAND_BLOCK, "Puesto de comercio");

        }
}
