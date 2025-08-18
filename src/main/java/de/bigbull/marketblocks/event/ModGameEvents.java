package de.bigbull.marketblocks.event;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.RegistriesInit;
import net.minecraft.core.Direction;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = MarketBlocks.MODID)
public class ModGameEvents {
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                RegistriesInit.SMALL_SHOP_BLOCK_ENTITY.get(),
                (be, side) -> {
                    if (side == null) return null;
                    if (side == Direction.DOWN)  return be.getOutputOnly();
                    if (side == Direction.UP)    return be.getInputOnly();
                    return null;
                }
        );
    }
}
