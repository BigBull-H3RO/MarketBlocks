package de.bigbull.marketblocks.mixin.client;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.config.Config;
import de.bigbull.marketblocks.init.RegistriesInit;
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

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
    @Shadow @Final private Minecraft minecraft;

    @Unique
    private long marketblocks$lastDesyncLogTick = Long.MIN_VALUE;

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
        if (Config.ENABLE_MIXIN_DESYNC_LOGGING.get() && gameTime - marketblocks$lastDesyncLogTick >= 20L) {
            marketblocks$lastDesyncLogTick = gameTime;
            MarketBlocks.LOGGER.debug("[MarketBlocks] Mining target fallback: top block at {} has no valid base block at {}", pos, basePos);
        }

        return pos;
    }
}

