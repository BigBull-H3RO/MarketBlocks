package de.bigbull.marketblocks.client.mixin;

import de.bigbull.marketblocks.Constants;
import de.bigbull.marketblocks.core.init.RegistriesInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import de.bigbull.marketblocks.feature.singleoffer.block.BaseShopBlock;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin for MultiPlayerGameMode to handle multi-block breaking logic on the client.
 * Normalizes hit coordinates so that attacking the top half of a Trade Stand
 * correctly targets the base block instead, preventing desync issues.
 * Also performs client-side permission checks to prevent ghost-block pop-ups
 * and synchronizes Trade Stand multiblock destruction.
 */
@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
    @Shadow @Final private Minecraft minecraft;

    @Unique
    private long marketblocks$lastDesyncLogTick = Long.MIN_VALUE;

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void marketblocks$interceptDestroyBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        BlockState state = minecraft.level.getBlockState(pos);
        boolean isBase = state.is(RegistriesInit.TRADE_STAND_BLOCK.get()) || state.is(RegistriesInit.MARKETCRATE_BLOCK.get());
        boolean isTop = state.is(RegistriesInit.TRADE_STAND_BLOCK_TOP.get());

        if (!isBase && !isTop) {
            return;
        }

        // Check client-side permission before predictively destroying locally
        if (!BaseShopBlock.canPlayerDestroy(minecraft.level, pos, minecraft.player, false)) {
            cir.setReturnValue(false);
            return;
        }

        // Synchronous multiblock destruction on client:
        if (isBase && state.is(RegistriesInit.TRADE_STAND_BLOCK.get())) {
            BlockPos topPos = pos.above();
            if (minecraft.level.getBlockState(topPos).is(RegistriesInit.TRADE_STAND_BLOCK_TOP.get())) {
                minecraft.level.setBlock(topPos, Blocks.AIR.defaultBlockState(), 11);
            }
        } else if (isTop) {
            BlockPos basePos = pos.below();
            if (minecraft.level.getBlockState(basePos).is(RegistriesInit.TRADE_STAND_BLOCK.get())) {
                minecraft.level.setBlock(basePos, Blocks.AIR.defaultBlockState(), 11);
            }
        }
    }

    @ModifyVariable(method = "startDestroyBlock", at = @At("HEAD"), argsOnly = true)
    private BlockPos marketblocks$normalizeStartTarget(BlockPos pos) {
        return marketblocks$toCanonicalDestroyPos(pos);
    }

    @ModifyVariable(method = "continueDestroyBlock", at = @At("HEAD"), argsOnly = true)
    private BlockPos marketblocks$normalizeContinueTarget(BlockPos pos) {
        return marketblocks$toCanonicalDestroyPos(pos);
    }

    @Unique
    private BlockPos marketblocks$toCanonicalDestroyPos(BlockPos pos) {
        if (minecraft.level == null || pos == null) {
            return pos;
        }

        BlockState state = minecraft.level.getBlockState(pos);
        if (!state.is(RegistriesInit.TRADE_STAND_BLOCK_TOP.get())) {
            return pos;
        }

        BlockPos basePos = pos.below();
        if (minecraft.level.getBlockState(basePos).is(RegistriesInit.TRADE_STAND_BLOCK.get())) {
            return basePos;
        }

        long gameTime = minecraft.level.getGameTime();
        if (gameTime - marketblocks$lastDesyncLogTick >= 20L) {
            marketblocks$lastDesyncLogTick = gameTime;
            Constants.LOG.debug("[MarketBlocks] Mining target fallback: top block at {} has no valid base block at {}", pos, basePos);
        }

        return pos;
    }
}
