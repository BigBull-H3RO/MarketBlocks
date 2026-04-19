package de.bigbull.marketblocks.init;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.shop.singleoffer.block.TradeStandBlock;
import de.bigbull.marketblocks.shop.singleoffer.block.TradeStandClassicBlock;
import de.bigbull.marketblocks.shop.singleoffer.block.TradeStandTopBlock;
import de.bigbull.marketblocks.shop.singleoffer.block.entity.SingleOfferShopBlockEntity;
import de.bigbull.marketblocks.shop.marketplace.menu.MarketplaceMenu;
import de.bigbull.marketblocks.shop.singleoffer.menu.SingleOfferShopMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class RegistriesInit {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MarketBlocks.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MarketBlocks.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MarketBlocks.MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, MarketBlocks.MODID);

    private RegistriesInit() {
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        MENU_TYPES.register(bus);
    }

    // Block Registrierung
    public static final DeferredBlock<Block> SHOP_BLOCK_TEST = registerBlock("shop_block_test",
            () -> new TradeStandClassicBlock(BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .mapColor(MapColor.PODZOL)
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(2.5F, 3600000.0F)
                    .sound(SoundType.WOOD)));

    // TradeStandBlock Neu (Tall Showcase Design)
    public static final DeferredBlock<Block> TRADE_STAND_BLOCK = registerBlock("trade_stand",
            () -> new TradeStandBlock(BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .mapColor(MapColor.PODZOL)
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(2.5F, 3600000.0F)
                    .sound(SoundType.WOOD)));

    public static final DeferredBlock<Block> TRADE_STAND_BLOCK_TOP = registerInternalBlock("trade_stand_top",
            () -> new TradeStandTopBlock(BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .mapColor(MapColor.PODZOL)
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(2.5F, 3600000.0F)
                    .sound(SoundType.WOOD)));

    // BlockEntity Registrierung
    public static final Supplier<BlockEntityType<SingleOfferShopBlockEntity>> SINGLE_OFFER_SHOP_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("single_offer_shop", () -> BlockEntityType.Builder.of(
                    SingleOfferShopBlockEntity::new, SHOP_BLOCK_TEST.get(), TRADE_STAND_BLOCK.get()).build(null));

    // Menu Registrierungen
    public static final Supplier<MenuType<SingleOfferShopMenu>> SINGLE_OFFER_SHOP_MENU =
            MENU_TYPES.register("single_offer_shop_menu", () -> IMenuTypeExtension.create(SingleOfferShopMenu::new));

    public static final Supplier<MenuType<MarketplaceMenu>> MARKETPLACE_MENU =
            MENU_TYPES.register("marketplace_menu", () -> IMenuTypeExtension.create(MarketplaceMenu::new));


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

