package de.bigbull.marketblocks.event;

import de.bigbull.marketblocks.core.init.RegistriesInit;
import de.bigbull.marketblocks.feature.singleoffer.SideMode;
import de.bigbull.marketblocks.platform.CommonToNeoForgeItemHandlerWrapper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class ModCapabilityEvents {
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                RegistriesInit.SINGLE_OFFER_SHOP_BLOCK_ENTITY.get(),
                (be, side) -> {
                    if (side == null)
                        return null;
                    SideMode mode = be.getMode(side);
                    if (mode == SideMode.INPUT)
                        return new CommonToNeoForgeItemHandlerWrapper(be.getInputOnly());
                    if (mode == SideMode.OUTPUT)
                        return new CommonToNeoForgeItemHandlerWrapper(be.getOutputOnly());
                    return null;
                });
    }
}
