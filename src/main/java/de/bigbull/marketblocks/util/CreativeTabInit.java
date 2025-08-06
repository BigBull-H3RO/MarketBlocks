package de.bigbull.marketblocks.util;

import de.bigbull.marketblocks.MarketBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CreativeTabInit {
    public static DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MarketBlocks.MODID);

    public static String MAIN_TAB_ONE_TITLE = "main.tab.one";

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB_ONE = CREATIVE_MODE_TABS.register("main_tab_one", () -> {
        CreativeModeTab.Builder builder = CreativeModeTab.builder();

        builder.displayItems((itemDisplay, output) -> {

        });
        //builder.icon(() -> new ItemStack(ItemInit.VIBRANIUM_MACE.get()));
        builder.title(Component.translatable(MAIN_TAB_ONE_TITLE));

        return builder.build();
    });
}