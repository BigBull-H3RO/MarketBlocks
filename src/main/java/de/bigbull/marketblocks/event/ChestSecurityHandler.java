package de.bigbull.marketblocks.event;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.config.Config;
import de.bigbull.marketblocks.util.custom.block.SideMode;
import de.bigbull.marketblocks.util.custom.block.SmallShopBlock;
import de.bigbull.marketblocks.util.custom.entity.LockedChestWrapper;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Handles security and interaction logic for chests connected to a Small Shop.
 * This class prevents unauthorized access to shop inventories and manages capability registration
 * to ensure that item transfers (e.g., from pipes) respect shop ownership.
 */
@EventBusSubscriber(modid = MarketBlocks.MODID)
public class ChestSecurityHandler {

    /**
     * Registers a custom item handler capability for chests connected to a shop.
     * This handler wraps the original chest inventory in a {@link LockedChestWrapper} to enforce ownership rules.
     * It runs at HIGH priority to ensure it wraps the chest before other mods can access it.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                BlockEntityType.CHEST,
                (chest, side) -> {
                    Level level = chest.getLevel();
                    if (level == null) {
                        return null; // Should not happen, but good practice
                    }

                    SmallShopBlockEntity shop = findShop(level, chest.getBlockPos());
                    if (shop == null) {
                        return null; // Not a shop-connected chest, let default handlers take over
                    }

                    // This chest is part of a shop, so wrap its inventory with our security handler
                    IItemHandler handler = createItemHandler(chest);

                    return new LockedChestWrapper(handler, shop.getOwnerId());
                    }
        );
    }

    /**
     * Prevents players from creating a double chest next to a shop if double chest support is disabled.
     * If enabled, it invalidates capabilities to force a refresh of the item handler.
     */
    @SubscribeEvent
    public static void onChestPlaced(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof Level level) || level.isClientSide()) {
            return;
        }

        BlockState state = event.getPlacedBlock();
        // We only care about chests that are forming a double chest
        if (!(state.getBlock() instanceof ChestBlock) || state.getValue(ChestBlock.TYPE) == ChestType.SINGLE) {
            return;
        }

        BlockPos placedPos = event.getPos();
        Direction connectedDir = ChestBlock.getConnectedDirection(state);
        if (connectedDir == null) return;
        BlockPos otherPos = placedPos.relative(connectedDir);

        // If neither part of the double chest is adjacent to a shop, we don't care
        if (!isAdjacentToShop(level, placedPos) && !isAdjacentToShop(level, otherPos)) {
            return;
        }

        if (Config.ENABLE_DOUBLE_CHEST_SUPPORT.get()) {
            // Invalidate capabilities to force a refresh with the new CombinedInvWrapper
            level.invalidateCapabilities(placedPos);
            level.invalidateCapabilities(otherPos);
        } else {
            // Prevent forming a double chest if the feature is disabled
            event.setCanceled(true);
        }
    }

    /**
     * Prevents non-owners from accessing a shop-connected chest by right-clicking it.
     */
    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        if (level.getBlockEntity(pos) instanceof ChestBlockEntity) {
            SmallShopBlockEntity shop = findShop(level, pos);
            // If a shop is found and the player is not the owner, cancel the interaction
            if (shop != null && !shop.isOwner(event.getEntity())) {
                event.setCanceled(true);
            }
        }
    }

    /**
     * Creates the appropriate IItemHandler for a chest, handling single and double chests.
     */
    private static IItemHandler createItemHandler(@NotNull ChestBlockEntity chest) {
        Level level = chest.getLevel();
        BlockState state = chest.getBlockState();

        // Check for double chest support and if the chest is part of a double chest
        if (Config.ENABLE_DOUBLE_CHEST_SUPPORT.get() &&
                state.getBlock() instanceof ChestBlock &&
                state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {

            Direction connectedDir = ChestBlock.getConnectedDirection(state);
            if (level != null && connectedDir != null) {
                BlockPos otherPos = chest.getBlockPos().relative(connectedDir);
                if (level.getBlockEntity(otherPos) instanceof ChestBlockEntity otherChest) {
                    // Return a combined wrapper for both halves of the double chest
                    return new CombinedInvWrapper(new InvWrapper(chest), new InvWrapper(otherChest));
                }
            }
        }
        // Default to a single inventory wrapper
        return new InvWrapper(chest);
    }

    /**
     * Checks if a block at a given position is adjacent to any SmallShopBlock.
     */
    private static boolean isAdjacentToShop(@NotNull Level level, @NotNull BlockPos pos) {
        for (Direction dir : Direction.values()) {
            if (level.getBlockState(pos.relative(dir)).getBlock() instanceof SmallShopBlock) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds a SmallShopBlockEntity connected to a chest at a given position.
     * This method correctly handles both single and double chests.
     *
     * @return The found {@link SmallShopBlockEntity}, or null if none is connected.
     */
    @Nullable
    private static SmallShopBlockEntity findShop(@NotNull Level level, @NotNull BlockPos chestPos) {
        // First, check the sides of the chest at the given position
        SmallShopBlockEntity shop = findShopSingle(level, chestPos);
        if (shop != null) {
            return shop;
        }

        // If double chests are enabled, check if it's part of a double chest and check the other half
        if (Config.ENABLE_DOUBLE_CHEST_SUPPORT.get()) {
            BlockState state = level.getBlockState(chestPos);
            if (state.getBlock() instanceof ChestBlock && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                Direction direction = ChestBlock.getConnectedDirection(state);
                if (direction != null) {
                    BlockPos otherPos = chestPos.relative(direction);
                    return findShopSingle(level, otherPos);
                }
            }
        }

        return null;
    }

    /**
     * Checks all 6 sides of a single block position to find a configured SmallShopBlockEntity.
     *
     * @return The found {@link SmallShopBlockEntity}, or null if none is configured for I/O on that side.
     */
    @Nullable
    private static SmallShopBlockEntity findShopSingle(@NotNull Level level, @NotNull BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            if (level.getBlockEntity(neighborPos) instanceof SmallShopBlockEntity shop) {
                // Check if the shop's side facing the chest is configured for input or output
                SideMode mode = shop.getModeForSide(dir.getOpposite());
                if (mode == SideMode.INPUT || mode == SideMode.OUTPUT) {
                    return shop;
                }
            }
        }
        return null;
    }
}