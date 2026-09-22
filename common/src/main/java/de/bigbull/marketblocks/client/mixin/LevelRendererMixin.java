package de.bigbull.marketblocks.client.mixin;

import de.bigbull.marketblocks.feature.singleoffer.block.TradeStandBlock;
import de.bigbull.marketblocks.feature.singleoffer.block.TradeStandTopBlock;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Shadow
    private ClientLevel level;

    @Shadow
    public abstract void destroyBlockProgress(int breakerId, BlockPos pos, int progress);

    @Unique
    private static final ThreadLocal<Boolean> marketblocks$IS_MIRRORING = ThreadLocal.withInitial(() -> false);

    @Inject(method = "destroyBlockProgress", at = @At("HEAD"))
    private void marketblocks$mirrorDestroyProgress(int breakerId, BlockPos pos, int progress, CallbackInfo ci) {
        if (marketblocks$IS_MIRRORING.get() || this.level == null) {
            return;
        }

        try {
            marketblocks$IS_MIRRORING.set(true);

            BlockState state = this.level.getBlockState(pos);
            BlockPos targetPos = null;

            if (state.getBlock() instanceof TradeStandBlock) {
                targetPos = pos.above();
            } else if (state.getBlock() instanceof TradeStandTopBlock) {
                targetPos = pos.below();
            }

            if (targetPos != null) {
                int mirrorBreakerId = breakerId + 500000;
                this.destroyBlockProgress(mirrorBreakerId, targetPos, progress);
            }
        } finally {
            marketblocks$IS_MIRRORING.set(false);
        }
    }
}