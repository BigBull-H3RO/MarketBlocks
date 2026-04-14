package de.bigbull.marketblocks.event;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.util.custom.block.SideMode;
import de.bigbull.marketblocks.util.custom.block.SmallShopBlockNeu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = MarketBlocks.MODID)
public class ModGameEvents {
    @SubscribeEvent
    public static void onSmallShopShowcaseUse(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getEntity().isShiftKeyDown()) {
            return;
        }

        Level level = event.getLevel();
        BlockPos clickedPos = event.getPos();
        BlockState clickedState = level.getBlockState(clickedPos);

        BlockPos basePos = clickedPos;
        BlockState baseState = clickedState;
        if (clickedState.is(RegistriesInit.SMALL_SHOP_BLOCK_TOP.get())) {
            basePos = clickedPos.below();
            baseState = level.getBlockState(basePos);
        }

        ItemStack stack = event.getItemStack();
        InteractionResult result = InteractionResult.PASS;

        if (stack.getItem() instanceof AxeItem) {
            result = SmallShopBlockNeu.tryDisableShowcase(level, basePos, baseState, event.getEntity());
            if (result == InteractionResult.FAIL && !level.isClientSide && event.getEntity() instanceof ServerPlayer player) {
                player.displayClientMessage(Component.translatable("message.marketblocks.small_shop.not_owner"), true);
            }
        } else if (stack.is(Items.GLASS)) {
            result = SmallShopBlockNeu.tryEnableShowcase(level, basePos, baseState, event.getEntity(), stack);
            if (result == InteractionResult.FAIL && !level.isClientSide && event.getEntity() instanceof ServerPlayer player) {
                player.displayClientMessage(Component.translatable("message.marketblocks.small_shop.not_owner"), true);
            }
        }

        if (result != InteractionResult.PASS) {
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
    }

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