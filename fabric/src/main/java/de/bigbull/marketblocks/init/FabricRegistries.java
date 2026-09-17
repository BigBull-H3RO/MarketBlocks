package de.bigbull.marketblocks.init;

import de.bigbull.marketblocks.Constants;
import de.bigbull.marketblocks.core.init.RegistriesInit;
import de.bigbull.marketblocks.feature.marketplace.advancement.MarketplaceBuyTrigger;
import de.bigbull.marketblocks.feature.marketplace.advancement.MarketplaceOpenTrigger;
import de.bigbull.marketblocks.feature.marketplace.menu.MarketplaceMenu;
import de.bigbull.marketblocks.feature.singleoffer.advancement.*;
import de.bigbull.marketblocks.feature.singleoffer.block.MarketCrateBlock;
import de.bigbull.marketblocks.feature.singleoffer.block.TradeStandBlock;
import de.bigbull.marketblocks.feature.singleoffer.block.TradeStandTopBlock;
import de.bigbull.marketblocks.feature.singleoffer.entity.SingleOfferShopBlockEntity;
import de.bigbull.marketblocks.feature.singleoffer.menu.SingleOfferShopMenu;
import de.bigbull.marketblocks.feature.trader.entity.ShopBuyerEntity;
import de.bigbull.marketblocks.feature.trader.item.TradeBookItem;
import de.bigbull.marketblocks.platform.FabricPlatformHelper;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public final class FabricRegistries {

    private static BlockBehaviour.Properties tradeStandProperties() {
        return BlockBehaviour.Properties.of()
                .noOcclusion()
                .mapColor(MapColor.PODZOL)
                .instrument(NoteBlockInstrument.BASS)
                .strength(2.5F, 3600000.0F)
                .sound(SoundType.WOOD)
                .pushReaction(PushReaction.BLOCK);
    }

    public static void init() {
        // Blocks
        Block tradeStand = Registry.register(BuiltInRegistries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "trade_stand"),
                new TradeStandBlock(tradeStandProperties()));
        RegistriesInit.TRADE_STAND_BLOCK = () -> tradeStand;

        Block tradeStandTop = Registry.register(BuiltInRegistries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "trade_stand_top"),
                new TradeStandTopBlock(tradeStandProperties().sound(SoundType.GLASS)));
        RegistriesInit.TRADE_STAND_BLOCK_TOP = () -> tradeStandTop;

        Block marketCrate = Registry.register(BuiltInRegistries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "marketcrate"),
                new MarketCrateBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
                        .noOcclusion()
                        .mapColor(MapColor.WOOD)
                        .instrument(NoteBlockInstrument.BASS)
                        .strength(2.5F, 3600000.0F)
                        .sound(SoundType.WOOD)
                        .pushReaction(PushReaction.BLOCK)));
        RegistriesInit.MARKETCRATE_BLOCK = () -> marketCrate;

        // Block Items
        Item tradeStandItem = Registry.register(BuiltInRegistries.ITEM,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "trade_stand"),
                new BlockItem(tradeStand, new Item.Properties()));
        RegistriesInit.TRADE_STAND_ITEM = () -> tradeStandItem;

        Item marketCrateItem = Registry.register(BuiltInRegistries.ITEM,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "marketcrate"),
                new BlockItem(marketCrate, new Item.Properties()));
        RegistriesInit.MARKETCRATE_ITEM = () -> marketCrateItem;

        // Items
        Item tradeBook = Registry.register(BuiltInRegistries.ITEM,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "trade_book"),
                new TradeBookItem(new Item.Properties().stacksTo(1)));
        RegistriesInit.TRADE_BOOK = () -> tradeBook;

        // Entity Types
        EntityType<ShopBuyerEntity> shopBuyer = Registry.register(BuiltInRegistries.ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "shop_buyer"),
                EntityType.Builder.of(ShopBuyerEntity::new, MobCategory.CREATURE)
                        .sized(0.6F, 1.95F)
                        .clientTrackingRange(10)
                        .build("shop_buyer"));
        RegistriesInit.SHOP_BUYER = () -> shopBuyer;

        // Spawn Egg
        Item spawnEgg = Registry.register(BuiltInRegistries.ITEM,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "shop_buyer_spawn_egg"),
                new SpawnEggItem(shopBuyer, 0x0000AA, 0xFFFF00, new Item.Properties()));
        RegistriesInit.SHOP_BUYER_SPAWN_EGG = () -> spawnEgg;

        // Block Entities
        BlockEntityType<SingleOfferShopBlockEntity> shopBe = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "single_offer_shop"),
                BlockEntityType.Builder.of(SingleOfferShopBlockEntity::new, tradeStand, marketCrate).build(null));
        RegistriesInit.SINGLE_OFFER_SHOP_BLOCK_ENTITY = () -> shopBe;

        // Menus
        MenuType<SingleOfferShopMenu> singleOfferMenu = Registry.register(BuiltInRegistries.MENU,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "single_offer_shop_menu"),
                new ExtendedScreenHandlerType<>((syncId, inv, data) -> {
                    RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(data.bytes()), inv.player.registryAccess());
                    return new SingleOfferShopMenu(syncId, inv, buf);
                }, FabricPlatformHelper.OpenMenuData.STREAM_CODEC));
        RegistriesInit.SINGLE_OFFER_SHOP_MENU = () -> singleOfferMenu;

        MenuType<MarketplaceMenu> mktMenu = Registry.register(BuiltInRegistries.MENU,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "marketplace_menu"),
                new ExtendedScreenHandlerType<>((syncId, inv, data) -> {
                    RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(data.bytes()), inv.player.registryAccess());
                    return new MarketplaceMenu(syncId, inv, buf);
                }, FabricPlatformHelper.OpenMenuData.STREAM_CODEC));
        RegistriesInit.MARKETPLACE_MENU = () -> mktMenu;

        // Sounds
        SoundEvent fallSound = Registry.register(BuiltInRegistries.SOUND_EVENT,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "visual_npc_fall"),
                SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "visual_npc_fall")));
        RegistriesInit.VISUAL_NPC_FALL_SOUND = () -> fallSound;

        // Triggers
        ShopSellTrigger t1 = Registry.register(BuiltInRegistries.TRIGGER_TYPES, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "shop_sell"), new ShopSellTrigger());
        RegistriesInit.SHOP_SELL_TRIGGER = () -> t1;
        ShopNpcTrigger t2 = Registry.register(BuiltInRegistries.TRIGGER_TYPES, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "shop_npc"), new ShopNpcTrigger());
        RegistriesInit.SHOP_NPC_TRIGGER = () -> t2;
        ShopCoOwnerTrigger t3 = Registry.register(BuiltInRegistries.TRIGGER_TYPES, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "shop_co_owner"), new ShopCoOwnerTrigger());
        RegistriesInit.SHOP_CO_OWNER_TRIGGER = () -> t3;
        ShopOutOfStockTrigger t4 = Registry.register(BuiltInRegistries.TRIGGER_TYPES, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "shop_out_of_stock"), new ShopOutOfStockTrigger());
        RegistriesInit.SHOP_OUT_OF_STOCK_TRIGGER = () -> t4;
        ShopWholesalerTrigger t5 = Registry.register(BuiltInRegistries.TRIGGER_TYPES, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "shop_wholesaler"), new ShopWholesalerTrigger());
        RegistriesInit.SHOP_WHOLESALER_TRIGGER = () -> t5;
        MarketplaceOpenTrigger t6 = Registry.register(BuiltInRegistries.TRIGGER_TYPES, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "marketplace_open"), new MarketplaceOpenTrigger());
        RegistriesInit.MARKETPLACE_OPEN_TRIGGER = () -> t6;
        ShopNpcCustomizeTrigger t7 = Registry.register(BuiltInRegistries.TRIGGER_TYPES, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "shop_npc_customize"), new ShopNpcCustomizeTrigger());
        RegistriesInit.SHOP_NPC_CUSTOMIZE_TRIGGER = () -> t7;
        ShopRedstoneTrigger t8 = Registry.register(BuiltInRegistries.TRIGGER_TYPES, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "shop_redstone"), new ShopRedstoneTrigger());
        RegistriesInit.SHOP_REDSTONE_TRIGGER = () -> t8;
        ShopAutoIoTrigger t9 = Registry.register(BuiltInRegistries.TRIGGER_TYPES, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "shop_auto_io"), new ShopAutoIoTrigger());
        RegistriesInit.SHOP_AUTO_IO_TRIGGER = () -> t9;
        ShopAdminModeTrigger t10 = Registry.register(BuiltInRegistries.TRIGGER_TYPES, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "shop_admin_mode"), new ShopAdminModeTrigger());
        RegistriesInit.SHOP_ADMIN_MODE_TRIGGER = () -> t10;
        MarketplaceBuyTrigger t11 = Registry.register(BuiltInRegistries.TRIGGER_TYPES, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "marketplace_buy"), new MarketplaceBuyTrigger());
        RegistriesInit.MARKETPLACE_BUY_TRIGGER = () -> t11;

        // Creative Tab
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "marketblocks_tab"),
                FabricItemGroup.builder()
                        .title(Component.translatable("itemGroup.marketblocks"))
                        .icon(() -> new ItemStack(tradeStand))
                        .displayItems((parameters, output) -> {
                            output.accept(tradeStand);

                            ItemStack showcaseStack = new ItemStack(tradeStand);
                            showcaseStack.set(DataComponents.BLOCK_STATE,
                                    BlockItemStateProperties.EMPTY.with(TradeStandBlock.HAS_SHOWCASE, true));
                            showcaseStack.set(DataComponents.CUSTOM_NAME,
                                    Component.translatable("item.marketblocks.trade_stand.with_showcase"));
                            output.accept(showcaseStack);

                            output.accept(marketCrate);
                            output.accept(tradeBook);
                            output.accept(spawnEgg);
                        })
                        .build());
    }
}
