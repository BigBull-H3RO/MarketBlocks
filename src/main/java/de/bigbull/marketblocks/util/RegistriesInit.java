package de.bigbull.marketblocks.util;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.block.SmallShopBlock;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import de.bigbull.marketblocks.util.custom.menu.SmallShopInventoryMenu;
import de.bigbull.marketblocks.util.custom.menu.SmallShopOffersMenu;
import de.bigbull.marketblocks.util.custom.screen.SmallShopInventoryScreen;
import de.bigbull.marketblocks.util.custom.screen.SmallShopOffersScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.awt.*;

public class RegistriesInit {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, MarketBlocks.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MarketBlocks.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MarketBlocks.MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, MarketBlocks.MODID);

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        MENU_TYPES.register(bus);
    }

    // Block Registrierung
    public static final DeferredHolder<Block, SmallShopBlock> SMALL_SHOP_BLOCK =
            BLOCKS.register("small_shop", () -> new SmallShopBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
                    .strength(2.0f, 3.0f)
                    .noOcclusion()));

    // Item Registrierung
    public static final DeferredHolder<Item, BlockItem> SMALL_SHOP_BLOCK_ITEM =
            ITEMS.register("small_shop", () -> new BlockItem(SMALL_SHOP_BLOCK.get(), new Item.Properties()));

    // BlockEntity Registrierung
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SmallShopBlockEntity>> SMALL_SHOP_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("small_shop", () -> BlockEntityType.Builder.of(
                    SmallShopBlockEntity::new, SMALL_SHOP_BLOCK.get()).build(null));

    // Menu Registrierungen - Separate Menüs für Offers und Inventory
    public static final DeferredHolder<MenuType<?>, MenuType<SmallShopOffersMenu>> SMALL_SHOP_OFFERS_MENU =
            MENU_TYPES.register("small_shop_offers_menu", () -> new MenuType<>(SmallShopOffersMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final DeferredHolder<MenuType<?>, MenuType<SmallShopInventoryMenu>> SMALL_SHOP_INVENTORY_MENU =
            MENU_TYPES.register("small_shop_inventory_menu", () -> new MenuType<>(SmallShopInventoryMenu::new, FeatureFlags.DEFAULT_FLAGS));

    /**
     * Clientseitige Registrierung der Bildschirmklassen.
     */
    @EventBusSubscriber(modid = MarketBlocks.MODID, value = Dist.CLIENT)
    public static class ClientRegistry {

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(SMALL_SHOP_OFFERS_MENU.get(), SmallShopOffersScreen::new);
            event.register(SMALL_SHOP_INVENTORY_MENU.get(), SmallShopInventoryScreen::new);
        }
    }
}