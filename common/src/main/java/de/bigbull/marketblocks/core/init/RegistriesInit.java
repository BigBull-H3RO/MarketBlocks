package de.bigbull.marketblocks.core.init;

import de.bigbull.marketblocks.feature.marketplace.advancement.MarketplaceBuyTrigger;
import de.bigbull.marketblocks.feature.marketplace.advancement.MarketplaceOpenTrigger;
import de.bigbull.marketblocks.feature.marketplace.menu.MarketplaceMenu;
import de.bigbull.marketblocks.feature.singleoffer.advancement.*;
import de.bigbull.marketblocks.feature.singleoffer.entity.SingleOfferShopBlockEntity;
import de.bigbull.marketblocks.feature.singleoffer.menu.SingleOfferShopMenu;
import de.bigbull.marketblocks.feature.trader.entity.ShopBuyerEntity;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public final class RegistriesInit {
    private RegistriesInit() {}

    // Blocks
    public static Supplier<Block> TRADE_STAND_BLOCK;
    public static Supplier<Block> TRADE_STAND_BLOCK_TOP;
    public static Supplier<Block> MARKETCRATE_BLOCK;

    // Items
    public static Supplier<Item> TRADE_STAND_ITEM;
    public static Supplier<Item> MARKETCRATE_ITEM;
    public static Supplier<Item> TRADE_BOOK;
    public static Supplier<Item> SHOP_BUYER_SPAWN_EGG;

    // Block Entities
    public static Supplier<BlockEntityType<SingleOfferShopBlockEntity>> SINGLE_OFFER_SHOP_BLOCK_ENTITY;

    // Menus
    public static Supplier<MenuType<SingleOfferShopMenu>> SINGLE_OFFER_SHOP_MENU;
    public static Supplier<MenuType<MarketplaceMenu>> MARKETPLACE_MENU;

    // Entities
    public static Supplier<EntityType<ShopBuyerEntity>> SHOP_BUYER;

    // Sounds
    public static Supplier<SoundEvent> VISUAL_NPC_FALL_SOUND;

    // Triggers
    public static Supplier<ShopSellTrigger> SHOP_SELL_TRIGGER;
    public static Supplier<ShopNpcTrigger> SHOP_NPC_TRIGGER;
    public static Supplier<ShopCoOwnerTrigger> SHOP_CO_OWNER_TRIGGER;
    public static Supplier<ShopOutOfStockTrigger> SHOP_OUT_OF_STOCK_TRIGGER;
    public static Supplier<ShopWholesalerTrigger> SHOP_WHOLESALER_TRIGGER;
    public static Supplier<MarketplaceOpenTrigger> MARKETPLACE_OPEN_TRIGGER;
    public static Supplier<ShopNpcCustomizeTrigger> SHOP_NPC_CUSTOMIZE_TRIGGER;
    public static Supplier<ShopRedstoneTrigger> SHOP_REDSTONE_TRIGGER;
    public static Supplier<ShopAutoIoTrigger> SHOP_AUTO_IO_TRIGGER;
    public static Supplier<ShopAdminModeTrigger> SHOP_ADMIN_MODE_TRIGGER;
    public static Supplier<MarketplaceBuyTrigger> MARKETPLACE_BUY_TRIGGER;
}