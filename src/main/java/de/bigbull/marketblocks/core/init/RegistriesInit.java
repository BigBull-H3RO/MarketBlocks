package de.bigbull.marketblocks.core.init;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.feature.marketplace.menu.MarketplaceMenu;
import de.bigbull.marketblocks.feature.marketplace.advancement.MarketplaceBuyTrigger;
import de.bigbull.marketblocks.feature.marketplace.advancement.MarketplaceOpenTrigger;
import de.bigbull.marketblocks.feature.marketplace.block.MarketplaceBlock;
import de.bigbull.marketblocks.feature.marketplace.entity.MarketplaceBlockEntity;
import de.bigbull.marketblocks.feature.singleoffer.advancement.ShopAdminModeTrigger;
import de.bigbull.marketblocks.feature.singleoffer.advancement.ShopAutoIoTrigger;
import de.bigbull.marketblocks.feature.singleoffer.advancement.ShopCoOwnerTrigger;
import de.bigbull.marketblocks.feature.singleoffer.advancement.ShopNpcCustomizeTrigger;
import de.bigbull.marketblocks.feature.singleoffer.advancement.ShopNpcTrigger;
import de.bigbull.marketblocks.feature.singleoffer.advancement.ShopOutOfStockTrigger;
import de.bigbull.marketblocks.feature.singleoffer.advancement.ShopRedstoneTrigger;
import de.bigbull.marketblocks.feature.singleoffer.advancement.ShopSellTrigger;
import de.bigbull.marketblocks.feature.singleoffer.advancement.ShopWholesalerTrigger;
import de.bigbull.marketblocks.feature.singleoffer.block.MarketCrateBlock;
import de.bigbull.marketblocks.feature.singleoffer.block.TradeStandBlock;
import de.bigbull.marketblocks.feature.singleoffer.block.TradeStandTopBlock;
import de.bigbull.marketblocks.feature.singleoffer.entity.SingleOfferShopBlockEntity;
import de.bigbull.marketblocks.feature.singleoffer.menu.SingleOfferShopMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.advancements.CriterionTrigger;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Central registry initialization class for the MarketBlocks mod.
 * Uses NeoForge's DeferredRegisters to safely register all blocks, items, block entities,
 * menus, sounds, and advancement triggers during the mod loading phase.
 */
public final class RegistriesInit {
        public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MarketBlocks.MODID);
        public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MarketBlocks.MODID);
        public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister
                        .create(Registries.BLOCK_ENTITY_TYPE, MarketBlocks.MODID);
        public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU,
                        MarketBlocks.MODID);
        public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT,
                        MarketBlocks.MODID);
        public static final DeferredRegister<CriterionTrigger<?>> TRIGGER_TYPES = DeferredRegister
                        .create(Registries.TRIGGER_TYPE, MarketBlocks.MODID);

        private RegistriesInit() {
        }

        public static void register(IEventBus bus) {
                BLOCKS.register(bus);
                ITEMS.register(bus);
                BLOCK_ENTITIES.register(bus);
                MENU_TYPES.register(bus);
                SOUND_EVENTS.register(bus);
                TRIGGER_TYPES.register(bus);
        }

        public static BlockBehaviour.Properties tradeStandProperties() {
                return BlockBehaviour.Properties.of()
                                .noOcclusion()
                                .mapColor(MapColor.PODZOL)
                                .instrument(NoteBlockInstrument.BASS)
                                .strength(2.5F, 3600000.0F)
                                .sound(SoundType.WOOD);
        }

        public static final DeferredBlock<Block> TRADE_STAND_BLOCK = registerBlock("trade_stand",
                        () -> new TradeStandBlock(tradeStandProperties()));

        public static final DeferredBlock<Block> TRADE_STAND_BLOCK_TOP = registerInternalBlock("trade_stand_top",
                        () -> new TradeStandTopBlock(tradeStandProperties()));

        public static final DeferredBlock<Block> MARKETCRATE_BLOCK = registerBlock("marketcrate",
                        () -> new MarketCrateBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
                                        .noOcclusion()
                                        .mapColor(MapColor.WOOD)
                                        .instrument(NoteBlockInstrument.BASS)
                                        .strength(2.0f, 3.0f)
                                        .sound(SoundType.WOOD)));

        public static final DeferredBlock<Block> MARKETPLACE_BLOCK = registerBlock("marketplace",
                        () -> new MarketplaceBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
                                        .noOcclusion()
                                        .mapColor(MapColor.WOOD)
                                        .instrument(NoteBlockInstrument.BASS)
                                        .strength(2.0f, 3.0f)
                                        .sound(SoundType.WOOD)));

        public static final Supplier<BlockEntityType<SingleOfferShopBlockEntity>> SINGLE_OFFER_SHOP_BLOCK_ENTITY = BLOCK_ENTITIES
                        .register("single_offer_shop", () -> BlockEntityType.Builder.of(
                                        SingleOfferShopBlockEntity::new, TRADE_STAND_BLOCK.get(),
                                        MARKETCRATE_BLOCK.get()).build(null));

        public static final Supplier<BlockEntityType<MarketplaceBlockEntity>> MARKETPLACE_BLOCK_ENTITY = BLOCK_ENTITIES
                        .register("marketplace", () -> BlockEntityType.Builder.of(
                                        MarketplaceBlockEntity::new, MARKETPLACE_BLOCK.get()).build(null));

        public static final Supplier<MenuType<SingleOfferShopMenu>> SINGLE_OFFER_SHOP_MENU = MENU_TYPES
                        .register("single_offer_shop_menu", () -> IMenuTypeExtension.create(SingleOfferShopMenu::new));

        public static final Supplier<MenuType<MarketplaceMenu>> MARKETPLACE_MENU = MENU_TYPES
                        .register("marketplace_menu", () -> IMenuTypeExtension.create(MarketplaceMenu::new));

        public static final Supplier<SoundEvent> VISUAL_NPC_FALL_SOUND = SOUND_EVENTS.register("visual_npc_fall",
                        () -> SoundEvent.createVariableRangeEvent(
                                        ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "visual_npc_fall")));

        public static final Supplier<ShopSellTrigger> SHOP_SELL_TRIGGER = TRIGGER_TYPES.register("shop_sell",
                        ShopSellTrigger::new);
        public static final Supplier<ShopNpcTrigger> SHOP_NPC_TRIGGER = TRIGGER_TYPES.register("shop_npc",
                        ShopNpcTrigger::new);
        public static final Supplier<ShopCoOwnerTrigger> SHOP_CO_OWNER_TRIGGER = TRIGGER_TYPES.register("shop_co_owner",
                        ShopCoOwnerTrigger::new);
        public static final Supplier<ShopOutOfStockTrigger> SHOP_OUT_OF_STOCK_TRIGGER = TRIGGER_TYPES
                        .register("shop_out_of_stock", ShopOutOfStockTrigger::new);
        public static final Supplier<ShopWholesalerTrigger> SHOP_WHOLESALER_TRIGGER = TRIGGER_TYPES
                        .register("shop_wholesaler", ShopWholesalerTrigger::new);
        public static final Supplier<MarketplaceOpenTrigger> MARKETPLACE_OPEN_TRIGGER = TRIGGER_TYPES
                        .register("marketplace_open", MarketplaceOpenTrigger::new);
        public static final Supplier<ShopNpcCustomizeTrigger> SHOP_NPC_CUSTOMIZE_TRIGGER = TRIGGER_TYPES
                        .register("shop_npc_customize", ShopNpcCustomizeTrigger::new);
        public static final Supplier<ShopRedstoneTrigger> SHOP_REDSTONE_TRIGGER = TRIGGER_TYPES
                        .register("shop_redstone", ShopRedstoneTrigger::new);
        public static final Supplier<ShopAutoIoTrigger> SHOP_AUTO_IO_TRIGGER = TRIGGER_TYPES.register("shop_auto_io",
                        ShopAutoIoTrigger::new);
        public static final Supplier<ShopAdminModeTrigger> SHOP_ADMIN_MODE_TRIGGER = TRIGGER_TYPES
                        .register("shop_admin_mode", ShopAdminModeTrigger::new);
        public static final Supplier<MarketplaceBuyTrigger> MARKETPLACE_BUY_TRIGGER = TRIGGER_TYPES
                        .register("marketplace_buy", MarketplaceBuyTrigger::new);

        public static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
                DeferredBlock<T> toReturn = BLOCKS.register(name, block);
                registerBlockItem(name, toReturn);
                return toReturn;
        }

        public static <T extends Block> DeferredBlock<T> registerInternalBlock(String name, Supplier<T> block) {
                return BLOCKS.register(name, block);
        }

        public static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
                ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        }
}
