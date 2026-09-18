package de.bigbull.marketblocks.event;

import de.bigbull.marketblocks.Constants;

import de.bigbull.marketblocks.core.data.MarketplaceLinkSavedData;
import de.bigbull.marketblocks.core.init.RegistriesInit;
import de.bigbull.marketblocks.feature.singleoffer.block.BaseShopBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import de.bigbull.marketblocks.feature.marketplace.data.MarketplaceManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * Handles block interactions for linking physical blocks to the virtual
 * Marketplace.
 */
@EventBusSubscriber(modid = Constants.MOD_ID)
public final class MarketBlocksInteractionEvents {

    private MarketBlocksInteractionEvents() {
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide())
            return;
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        BlockPos pos = event.getPos();
        GlobalPos globalPos = GlobalPos.of(player.serverLevel().dimension(), pos);

        boolean isLinked = MarketplaceLinkSavedData.get(player.serverLevel()).isLinked(globalPos);

        if (isLinked) {
            MarketplaceManager.get().openShop(player);
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide())
            return;
        if (!(event.getPlayer() instanceof ServerPlayer player))
            return;

        BlockPos pos = event.getPos();
        BlockState state = event.getState();
        if (event.getLevel() instanceof Level level) {
            if (state.getBlock() instanceof BaseShopBlock || state.is(RegistriesInit.TRADE_STAND_BLOCK_TOP.get())) {
                if (!BaseShopBlock.canPlayerDestroy(level, pos, player, true)) {
                    event.setCanceled(true);
                    return;
                }
            }
        }
        GlobalPos globalPos = GlobalPos.of(player.serverLevel().dimension(), pos);

        boolean isLinked = MarketplaceLinkSavedData.get(player.serverLevel()).isLinked(globalPos);

        if (isLinked) {
            if (!player.hasPermissions(2)) {
                player.sendSystemMessage(Component.translatable("command.marketblocks.break.denied"));
                event.setCanceled(true);
            } else {
                MarketplaceLinkSavedData.get(player.serverLevel()).removeLink(globalPos);
                MarketplaceLinkSavedData.get(player.serverLevel()).syncToAll(player.getServer());
                player.sendSystemMessage(Component.translatable("command.marketblocks.break.unlinked"));
            }
        }
    }
}
