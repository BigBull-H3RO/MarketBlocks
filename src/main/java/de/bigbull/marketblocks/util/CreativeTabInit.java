package de.bigbull.marketblocks.util;

import de.bigbull.marketblocks.MarketBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CreativeTabInit {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MarketBlocks.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MARKETBLOCKS_TAB =
            CREATIVE_MODE_TABS.register("marketblocks_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.marketblocks"))
                    .icon(() -> new ItemStack(RegistriesInit.SMALL_SHOP_BLOCK_ITEM.get()))
                    .displayItems((parameters, output) -> {
                        // Füge alle Items des Mods hinzu
                        output.accept(RegistriesInit.SMALL_SHOP_BLOCK_ITEM.get());
                    })
                    .build());
}