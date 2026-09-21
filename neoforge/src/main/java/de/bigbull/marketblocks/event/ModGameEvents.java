package de.bigbull.marketblocks.event;

import de.bigbull.marketblocks.Constants;
import de.bigbull.marketblocks.core.config.Config;
import de.bigbull.marketblocks.core.config.toml.TomlConfigManager;
import de.bigbull.marketblocks.core.init.RegistriesInit;
import de.bigbull.marketblocks.feature.singleoffer.block.TradeStandBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Handles gameplay-related events on the NeoForge GAME event bus.
 * Gives the Trade Book to players on first join, handles Trade Stand showcase
 * toggling (Axe/Glass while crouching).
 */
@EventBusSubscriber(modid = Constants.MOD_ID)
public class ModGameEvents {

    @SubscribeEvent
    public static void onTradeStandShowcaseUse(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getEntity().isShiftKeyDown()) {
            return;
        }

        Level level = event.getLevel();
        BlockPos clickedPos = event.getPos();
        BlockState clickedState = level.getBlockState(clickedPos);

        BlockPos basePos = clickedPos;
        BlockState baseState = clickedState;
        if (clickedState.is(RegistriesInit.TRADE_STAND_BLOCK_TOP.get())) {
            basePos = clickedPos.below();
            baseState = level.getBlockState(basePos);
        }

        ItemStack stack = event.getItemStack();
        InteractionResult result = InteractionResult.PASS;

        if (stack.getItem() instanceof AxeItem) {
            result = TradeStandBlock.tryDisableShowcase(level, basePos, baseState, event.getEntity());
            if (result == InteractionResult.FAIL && !level.isClientSide
                    && event.getEntity() instanceof ServerPlayer player) {
                player.displayClientMessage(Component.translatable("message.marketblocks.trade_stand.not_owner"), true);
            }
        } else if (stack.is(Items.GLASS)) {
            result = TradeStandBlock.tryEnableShowcase(level, basePos, baseState, event.getEntity(), stack);
            if (result == InteractionResult.FAIL && !level.isClientSide
                    && event.getEntity() instanceof ServerPlayer player) {
                player.displayClientMessage(Component.translatable("message.marketblocks.trade_stand.not_owner"), true);
            }
        }

        if (result != InteractionResult.PASS) {
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        // Synchronize server configurations to the connected player
        TomlConfigManager.syncToPlayer(player);

        if (Config.GIVE_TRADE_BOOK_ON_FIRST_JOIN.get()) {
            CompoundTag persistentData = player.getPersistentData();
            if (!persistentData.getBoolean("MB_ReceivedTradeBook")
                    && !player.getTags().contains("marketblocks.received_trade_book")) {
                persistentData.putBoolean("MB_ReceivedTradeBook", true);
                player.addTag("marketblocks.received_trade_book");
                ItemStack book = new ItemStack(RegistriesInit.TRADE_BOOK.get());
                if (!player.getInventory().add(book)) {
                    player.drop(book, false);
                }
            }
        }
    }
}
