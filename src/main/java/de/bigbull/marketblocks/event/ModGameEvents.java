package de.bigbull.marketblocks.event;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.util.custom.block.SideMode;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * Handles server-side game events, primarily for capability registration.
 */
@EventBusSubscriber(modid = MarketBlocks.MODID)
public class ModGameEvents {

    private ModGameEvents() {
        // Private constructor to prevent instantiation
    }

    /**
     * Registers capabilities for the mod's block entities.
     * This method attaches the IItemHandler capability to the SmallShopBlockEntity,
     * allowing for item interaction with hoppers and pipes based on side configuration.
     *
     * @param event The capability registration event.
     */
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                RegistriesInit.SMALL_SHOP_BLOCK_ENTITY.get(),
                (blockEntity, side) -> {
                    if (side == null) {
                        return null; // Should not be null, but good practice
                    }
                    SideMode mode = blockEntity.getModeForSide(side);
                    return switch (mode) {
                        case INPUT -> blockEntity.getInputOnly();
                        case OUTPUT -> blockEntity.getOutputOnly();
                        default -> null; // DISABLED or other modes expose no items
                    };
                }
        );
    }
}
