package de.bigbull.marketblocks.util;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.block.SmallShopBlock;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CreativeTabInit {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MarketBlocks.MODID);

    private CreativeTabInit() {
    }

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MARKETBLOCKS_TAB =
            CREATIVE_MODE_TABS.register("marketblocks_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.marketblocks"))
                    .icon(() -> new ItemStack(RegistriesInit.SMALL_SHOP_BLOCK.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(RegistriesInit.SMALL_SHOP_BLOCK.get());

                        ItemStack showcaseStack = new ItemStack(RegistriesInit.SMALL_SHOP_BLOCK.get());
                        showcaseStack.set(DataComponents.BLOCK_STATE,
                                BlockItemStateProperties.EMPTY.with(SmallShopBlock.HAS_SHOWCASE, true));
                        showcaseStack.set(DataComponents.CUSTOM_NAME,
                                Component.translatable("item.marketblocks.small_shop.with_showcase"));
                        output.accept(showcaseStack);
                    })
                    .build());
}
