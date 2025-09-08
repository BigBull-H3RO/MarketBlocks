package de.bigbull.marketblocks.util;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.block.SmallShopBlock;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import de.bigbull.marketblocks.util.custom.menu.SmallShopInventoryMenu;
import de.bigbull.marketblocks.util.custom.menu.SmallShopMenu;
import de.bigbull.marketblocks.util.custom.menu.SmallShopOffersMenu;
import de.bigbull.marketblocks.util.custom.menu.SmallShopSettingsMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class RegistriesInit {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MarketBlocks.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MarketBlocks.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MarketBlocks.MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, MarketBlocks.MODID);

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        MENU_TYPES.register(bus);
    }

    // Block Registrierung
    public static final DeferredBlock<Block> SMALL_SHOP_BLOCK = registerBlock("small_shop",
            () -> new SmallShopBlock(BlockBehaviour.Properties.of().noOcclusion()));

    // BlockEntity Registrierung
    public static final Supplier<BlockEntityType<SmallShopBlockEntity>> SMALL_SHOP_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("small_shop", () -> BlockEntityType.Builder.of(
                    SmallShopBlockEntity::new, SMALL_SHOP_BLOCK.get()).build(null));

    // Menu Registrierungen
    // Deprecated registrations - replaced by SMALL_SHOP_MENU
    public static final Supplier<MenuType<SmallShopMenu>> SMALL_SHOP_MENU =
            MENU_TYPES.register("small_shop_menu", () -> IMenuTypeExtension.create(SmallShopMenu::new));

    public static final Supplier<MenuType<SmallShopOffersMenu>> SMALL_SHOP_OFFERS_MENU =
            MENU_TYPES.register("small_shop_offers_menu", () -> IMenuTypeExtension.create(SmallShopOffersMenu::new));

    public static final Supplier<MenuType<SmallShopInventoryMenu>> SMALL_SHOP_INVENTORY_MENU =
            MENU_TYPES.register("small_shop_inventory_menu", () -> IMenuTypeExtension.create(SmallShopInventoryMenu::new));

    public static final Supplier<MenuType<SmallShopSettingsMenu>> SMALL_SHOP_CONFIG_MENU =
            MENU_TYPES.register("small_shop_config_menu", () -> IMenuTypeExtension.create(SmallShopSettingsMenu::new));

    public static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    public static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}