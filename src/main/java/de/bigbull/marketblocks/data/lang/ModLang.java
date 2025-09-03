package de.bigbull.marketblocks.data.lang;

/**
 * A utility class that holds all translation keys for the mod.
 * Using constants for keys helps prevent typos and makes managing translations easier.
 */
public final class ModLang {

    private ModLang() {
        // Private constructor to prevent instantiation
    }

    // Creative Tab
    public static final String CREATIVE_TAB = "itemGroup.marketblocks";

    // Container Titles
    public static final String CONTAINER_SMALL_SHOP = "container.marketblocks.small_shop";
    public static final String CONTAINER_SMALL_SHOP_OFFERS = "container.marketblocks.small_shop_offers";
    public static final String CONTAINER_SMALL_SHOP_INVENTORY = "container.marketblocks.small_shop_inventory";

    // GUI Titles
    public static final String GUI_SHOP_TITLE = "gui.marketblocks.shop_title";
    public static final String GUI_INVENTORY_TITLE = "gui.marketblocks.inventory_title";
    public static final String GUI_SETTINGS_TITLE = "gui.marketblocks.settings_title";

    // GUI Status Messages
    public static final String GUI_TRADE_AVAILABLE = "gui.marketblocks.trade_available";
    public static final String GUI_TRADE_UNAVAILABLE = "gui.marketblocks.trade_unavailable";
    public static final String GUI_AVAILABLE = "gui.marketblocks.available";
    public static final String GUI_NO_OFFERS = "gui.marketblocks.no_offers";
    public static final String GUI_NO_PLAYERS_AVAILABLE = "gui.marketblocks.no_players_available";
    public static final String GUI_OUT_OF_STOCK = "gui.marketblocks.out_of_stock";
    public static final String GUI_OWNER = "gui.marketblocks.owner";
    public static final String GUI_CREATING_OFFER = "gui.marketblocks.creating_offer";
    public static final String GUI_OFFER_READY = "gui.marketblocks.offer_ready";
    public static final String GUI_INSUFFICIENT_STOCK = "gui.marketblocks.insufficient_stock";

    // GUI Offer Creation
    public static final String GUI_CREATE_HINT = "gui.marketblocks.create_hint";
    public static final String GUI_CONFIRM_OFFER = "gui.marketblocks.confirm_offer";
    public static final String GUI_CANCEL_OFFER = "gui.marketblocks.cancel_offer";
    public static final String GUI_CREATE_OFFER = "gui.marketblocks.create_offer";
    public static final String GUI_DELETE_OFFER = "gui.marketblocks.delete_offer";

    // GUI Navigation
    public static final String GUI_OFFERS = "gui.marketblocks.offers";
    public static final String GUI_OFFERS_TAB = "gui.marketblocks.offers_tab";
    public static final String GUI_INVENTORY_TAB = "gui.marketblocks.inventory_tab";
    public static final String GUI_SETTINGS_TAB = "gui.marketblocks.settings_tab";

    // GUI Inventory Labels
    public static final String GUI_INPUT_INVENTORY = "gui.marketblocks.input_inventory";
    public static final String GUI_OUTPUT_INVENTORY = "gui.marketblocks.output_inventory";
    public static final String GUI_INVENTORY_OWNER_ONLY = "gui.marketblocks.inventory_owner_only";
    public static final String GUI_INVENTORY_FLOW_HINT = "gui.marketblocks.inventory_flow_hint";

    // GUI Settings
    public static final String GUI_SAVE = "gui.marketblocks.save";
    public static final String GUI_SHOP_NAME = "gui.marketblocks.shop_name";
    public static final String GUI_EMIT_REDSTONE = "gui.marketblocks.emit_redstone";
    public static final String GUI_EMIT_REDSTONE_TOOLTIP = "gui.marketblocks.emit_redstone.tooltip";
    public static final String GUI_SETTINGS_OWNER_ONLY = "gui.marketblocks.settings_owner_only";

    // GUI Side Configuration
    public static final String GUI_SIDE_LEFT = "gui.marketblocks.side.left";
    public static final String GUI_SIDE_RIGHT = "gui.marketblocks.side.right";
    public static final String GUI_SIDE_BOTTOM = "gui.marketblocks.side.bottom";
    public static final String GUI_SIDE_BACK = "gui.marketblocks.side.back";
    public static final String GUI_INPUT = "gui.marketblocks.input";
    public static final String GUI_OUTPUT = "gui.marketblocks.output";
    public static final String GUI_DISABLED = "gui.marketblocks.disabled";

    // GUI Error Messages
    public static final String GUI_ERROR_NO_RESULT_ITEM = "gui.marketblocks.error.no_result_item";
    public static final String GUI_ERROR_NO_PAYMENT_ITEMS = "gui.marketblocks.error.no_payment_items";
    public static final String GUI_ERROR_INVALID_OFFER = "gui.marketblocks.error.invalid_offer";

    // GUI Success Messages
    public static final String GUI_SUCCESS_OFFER_CREATED = "gui.marketblocks.success.offer_created";
    public static final String GUI_SUCCESS_OFFER_DELETED = "gui.marketblocks.success.offer_deleted";
}