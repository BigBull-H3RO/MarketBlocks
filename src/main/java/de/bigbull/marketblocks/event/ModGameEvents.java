package de.bigbull.marketblocks.event;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.util.custom.block.SideMode;
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
                    SideMode mode = be.getModeForSide(side);
                    if (mode == SideMode.INPUT)  return be.getInputOnly();
                    if (mode == SideMode.OUTPUT) return be.getOutputOnly();
                    return null;
                }
        );
    }
}
