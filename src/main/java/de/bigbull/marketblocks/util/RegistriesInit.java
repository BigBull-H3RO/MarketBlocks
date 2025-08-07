package de.bigbull.marketblocks.util;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.block.SmallShopBlock;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import de.bigbull.marketblocks.util.custom.menu.SmallShopMenu;
import de.bigbull.marketblocks.util.custom.screen.gui.SmallShopBuyerScreen;
import de.bigbull.marketblocks.util.custom.screen.gui.SmallShopOwnerScreen;
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

    /**
     * Clientseitige Registrierung der Bildschirmklassen.
     */
    @EventBusSubscriber(modid = MarketBlocks.MODID, value = Dist.CLIENT)
    public static class ClientRegistry {
        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(SMALL_SHOP_MENU.get(), (menu, inventory, title) ->
                    menu.isOwnerView()
                            ? new SmallShopOwnerScreen(menu, inventory, title)
                            : new SmallShopBuyerScreen(menu, inventory, title));
        }
    }

    public static final DeferredHolder<MenuType<?>, MenuType<SmallShopMenu>> SMALL_SHOP_MENU =
            MENU_TYPES.register("small_shop_menu", () -> new MenuType<>(SmallShopMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final DeferredHolder<Block, SmallShopBlock> SMALL_SHOP_BLOCK =
            BLOCKS.register("small_shop_block", () -> new SmallShopBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)));

    public static final DeferredHolder<Item, BlockItem> SMALL_SHOP_BLOCK_ITEM =
            ITEMS.register("small_shop_block", () -> new BlockItem(SMALL_SHOP_BLOCK.get(), new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SmallShopBlockEntity>> SMALL_SHOP_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("small_shop_block", () -> BlockEntityType.Builder.of(SmallShopBlockEntity::new, SMALL_SHOP_BLOCK.get()).build(null));

}
