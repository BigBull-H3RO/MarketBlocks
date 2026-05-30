package de.bigbull.marketblocks.core.event;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.core.init.RegistriesInit;
import de.bigbull.marketblocks.feature.singleoffer.SideMode;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * Registers block entity capabilities on the MOD event bus.
 * Separated from ModGameEvents (GAME bus) because RegisterCapabilitiesEvent
 * is a mod lifecycle event.
 */
@EventBusSubscriber(modid = MarketBlocks.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModCapabilityEvents {
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                RegistriesInit.SINGLE_OFFER_SHOP_BLOCK_ENTITY.get(),
                (be, side) -> {
                    if (side == null) return null;
                    SideMode mode = be.getMode(side);
                    if (mode == SideMode.INPUT)  return be.getInputOnly();
                    if (mode == SideMode.OUTPUT) return be.getOutputOnly();
                    return null;
                }
        );
    }
}
