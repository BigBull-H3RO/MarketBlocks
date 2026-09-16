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
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NeoForgeRegistries {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Constants.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Constants.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Constants.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, Constants.MOD_ID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, Constants.MOD_ID);
    public static final DeferredRegister<net.minecraft.advancements.CriterionTrigger<?>> TRIGGER_TYPES = DeferredRegister.create(Registries.TRIGGER_TYPE, Constants.MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Constants.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Constants.MOD_ID);

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
        DeferredBlock<Block> tradeStand = BLOCKS.register("trade_stand", () -> new TradeStandBlock(tradeStandProperties()));
        RegistriesInit.TRADE_STAND_BLOCK = tradeStand;

        DeferredBlock<Block> tradeStandTop = BLOCKS.register("trade_stand_top", () -> new TradeStandTopBlock(tradeStandProperties().sound(SoundType.GLASS)));
        RegistriesInit.TRADE_STAND_BLOCK_TOP = tradeStandTop;

        DeferredBlock<Block> marketCrate = BLOCKS.register("marketcrate", () -> new MarketCrateBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
                        .noOcclusion()
                        .mapColor(MapColor.WOOD)
                        .instrument(NoteBlockInstrument.BASS)
                        .strength(2.5F, 3600000.0F)
                        .sound(SoundType.WOOD)
                        .pushReaction(PushReaction.BLOCK)));
        RegistriesInit.MARKETCRATE_BLOCK = marketCrate;

        // Block Items
        DeferredItem<BlockItem> tradeStandItem = ITEMS.register("trade_stand", () -> new BlockItem(tradeStand.get(), new Item.Properties()));
        RegistriesInit.TRADE_STAND_ITEM = () -> tradeStandItem.get();

        DeferredItem<BlockItem> marketCrateItem = ITEMS.register("marketcrate", () -> new BlockItem(marketCrate.get(), new Item.Properties()));
        RegistriesInit.MARKETCRATE_ITEM = () -> marketCrateItem.get();

        // Items
        DeferredItem<Item> tradeBook = ITEMS.register("trade_book", () -> new TradeBookItem(new Item.Properties().stacksTo(1)));
        RegistriesInit.TRADE_BOOK = tradeBook;

        // Entity Types
        DeferredHolder<EntityType<?>, EntityType<ShopBuyerEntity>> shopBuyer = ENTITY_TYPES.register("shop_buyer",
                () -> EntityType.Builder.of(ShopBuyerEntity::new, MobCategory.CREATURE)
                        .sized(0.6F, 1.95F)
                        .clientTrackingRange(10)
                        .build("shop_buyer"));
        RegistriesInit.SHOP_BUYER = shopBuyer;

        // Spawn Egg
        DeferredItem<Item> spawnEgg = ITEMS.register("shop_buyer_spawn_egg",
                () -> new DeferredSpawnEggItem(shopBuyer, 0x0000AA, 0xFFFF00, new Item.Properties()));
        RegistriesInit.SHOP_BUYER_SPAWN_EGG = spawnEgg;

        // Block Entities
        DeferredHolder<BlockEntityType<?>, BlockEntityType<SingleOfferShopBlockEntity>> shopBe = BLOCK_ENTITIES.register("single_offer_shop",
                () -> BlockEntityType.Builder.of(SingleOfferShopBlockEntity::new, tradeStand.get(), marketCrate.get()).build(null));
        RegistriesInit.SINGLE_OFFER_SHOP_BLOCK_ENTITY = shopBe;

        // Menus
        DeferredHolder<MenuType<?>, MenuType<SingleOfferShopMenu>> shopMenu = MENU_TYPES.register("single_offer_shop_menu",
                () -> IMenuTypeExtension.create(SingleOfferShopMenu::new));
        RegistriesInit.SINGLE_OFFER_SHOP_MENU = shopMenu;

        DeferredHolder<MenuType<?>, MenuType<MarketplaceMenu>> mktMenu = MENU_TYPES.register("marketplace_menu",
                () -> IMenuTypeExtension.create(MarketplaceMenu::new));
        RegistriesInit.MARKETPLACE_MENU = mktMenu;

        // Sounds
        DeferredHolder<SoundEvent, SoundEvent> fallSound = SOUND_EVENTS.register("visual_npc_fall",
                () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "visual_npc_fall")));
        RegistriesInit.VISUAL_NPC_FALL_SOUND = fallSound;

        // Triggers
        RegistriesInit.SHOP_SELL_TRIGGER = TRIGGER_TYPES.register("shop_sell", ShopSellTrigger::new);
        RegistriesInit.SHOP_NPC_TRIGGER = TRIGGER_TYPES.register("shop_npc", ShopNpcTrigger::new);
        RegistriesInit.SHOP_CO_OWNER_TRIGGER = TRIGGER_TYPES.register("shop_co_owner", ShopCoOwnerTrigger::new);
        RegistriesInit.SHOP_OUT_OF_STOCK_TRIGGER = TRIGGER_TYPES.register("shop_out_of_stock", ShopOutOfStockTrigger::new);
        RegistriesInit.SHOP_WHOLESALER_TRIGGER = TRIGGER_TYPES.register("shop_wholesaler", ShopWholesalerTrigger::new);
        RegistriesInit.MARKETPLACE_OPEN_TRIGGER = TRIGGER_TYPES.register("marketplace_open", MarketplaceOpenTrigger::new);
        RegistriesInit.SHOP_NPC_CUSTOMIZE_TRIGGER = TRIGGER_TYPES.register("shop_npc_customize", ShopNpcCustomizeTrigger::new);
        RegistriesInit.SHOP_REDSTONE_TRIGGER = TRIGGER_TYPES.register("shop_redstone", ShopRedstoneTrigger::new);
        RegistriesInit.SHOP_AUTO_IO_TRIGGER = TRIGGER_TYPES.register("shop_auto_io", ShopAutoIoTrigger::new);
        RegistriesInit.SHOP_ADMIN_MODE_TRIGGER = TRIGGER_TYPES.register("shop_admin_mode", ShopAdminModeTrigger::new);
        RegistriesInit.MARKETPLACE_BUY_TRIGGER = TRIGGER_TYPES.register("marketplace_buy", MarketplaceBuyTrigger::new);

        // Creative Tab
        CREATIVE_MODE_TABS.register("marketblocks_tab", () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.marketblocks"))
                .icon(() -> new ItemStack(tradeStand.get()))
                .displayItems((parameters, output) -> {
                    output.accept(tradeStand.get());

                    ItemStack showcaseStack = new ItemStack(tradeStand.get());
                    showcaseStack.set(DataComponents.BLOCK_STATE,
                            BlockItemStateProperties.EMPTY.with(TradeStandBlock.HAS_SHOWCASE, true));
                    showcaseStack.set(DataComponents.CUSTOM_NAME,
                            Component.translatable("item.marketblocks.trade_stand.with_showcase"));
                    output.accept(showcaseStack);

                    output.accept(marketCrate.get());
                    output.accept(tradeBook.get());
                    output.accept(spawnEgg.get());
                })
                .build());
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        MENU_TYPES.register(bus);
        SOUND_EVENTS.register(bus);
        TRIGGER_TYPES.register(bus);
        ENTITY_TYPES.register(bus);
        CREATIVE_MODE_TABS.register(bus);
    }
}
