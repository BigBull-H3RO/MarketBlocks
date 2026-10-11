package de.bigbull.marketblocks.event;

import de.bigbull.marketblocks.Constants;
import de.bigbull.marketblocks.core.config.Config;
import de.bigbull.marketblocks.core.config.toml.TomlConfigManager;
import de.bigbull.marketblocks.core.init.RegistriesInit;
import de.bigbull.marketblocks.feature.singleoffer.block.TradeStandBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import de.bigbull.marketblocks.feature.trader.entity.ai.AttackShopBuyerGoal;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Handles gameplay-related events on the NeoForge GAME event bus.
 * Gives the Trade Book to players on first join, handles Trade Stand showcase
 * toggling (Axe while crouching).
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
        boolean isTopBlock = clickedState.is(RegistriesInit.TRADE_STAND_BLOCK_TOP.get());
        if (isTopBlock) {
            basePos = clickedPos.below();
            baseState = level.getBlockState(basePos);
        }

        ItemStack stack = event.getItemStack();
        InteractionResult result = InteractionResult.PASS;

        if (stack.is(ItemTags.AXES)) {
            boolean isShowcaseHit = isTopBlock || (TradeStandBlock.hasShowcase(baseState)
                    && TradeStandBlock.isHitOnShowcase(basePos, event.getHitVec()));
            if (isShowcaseHit && TradeStandBlock.canPlayerModifyShowcase(level, basePos, event.getEntity())) {
                result = TradeStandBlock.tryDisableShowcase(level, basePos, baseState, event.getEntity());
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
            if (!persistentData.getBooleanOr("MB_ReceivedTradeBook", false)
                    && !player.entityTags().contains("marketblocks.received_trade_book")) {
                persistentData.putBoolean("MB_ReceivedTradeBook", true);
                player.addTag("marketblocks.received_trade_book");
                ItemStack book = new ItemStack(RegistriesInit.TRADE_BOOK.get());
                if (!player.getInventory().add(book)) {
                    player.drop(book, false, Prediction.SERVER_ONLY);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof Mob mob) {
            AttackShopBuyerGoal.addHostileTargetGoal(mob);
        }
    }
}
