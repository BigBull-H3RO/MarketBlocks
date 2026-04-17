package de.bigbull.marketblocks.mixin.client;

import de.bigbull.marketblocks.util.RegistriesInit;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
    @Unique
    private static final int MARKETBLOCKS_MIRROR_ID_MASK = 0x40000000;

    @Unique
    private final Map<Integer, BlockPos> marketblocks$mirrorTargets = new HashMap<>();

    @Unique
    private boolean marketblocks$isMirroring;

    @Inject(method = "destroyBlockProgress", at = @At("TAIL"))
    private void marketblocks$mirrorDestroyProgress(int breakerId, BlockPos pos, int progress, CallbackInfo ci) {
        if (marketblocks$isMirroring || marketblocks$isMirrorBreakerId(breakerId)) {
            return;
        }

        ClientLevel level = (ClientLevel) (Object) this;

        if (progress < 0) {
            BlockPos previousTarget = marketblocks$mirrorTargets.remove(breakerId);
            if (previousTarget != null) {
                marketblocks$dispatchMirror(level, breakerId, previousTarget, -1);
            }
            return;
        }

        BlockPos mirrorTarget = marketblocks$resolveMirrorTarget(level, pos);
        if (mirrorTarget == null) {
            BlockPos previousTarget = marketblocks$mirrorTargets.remove(breakerId);
            if (previousTarget != null) {
                marketblocks$dispatchMirror(level, breakerId, previousTarget, -1);
            }
            return;
        }

        BlockPos previousTarget = marketblocks$mirrorTargets.put(breakerId, mirrorTarget.immutable());
        if (previousTarget != null && !previousTarget.equals(mirrorTarget)) {
            marketblocks$dispatchMirror(level, breakerId, previousTarget, -1);
        }

        marketblocks$dispatchMirror(level, breakerId, mirrorTarget, progress);
    }

    @Unique
    private static BlockPos marketblocks$resolveMirrorTarget(ClientLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);

        if (state.is(RegistriesInit.TRADE_STAND_BLOCK.get())) {
            BlockPos topPos = pos.above();
            return level.getBlockState(topPos).is(RegistriesInit.TRADE_STAND_BLOCK_TOP.get()) ? topPos : null;
        }

        if (state.is(RegistriesInit.TRADE_STAND_BLOCK_TOP.get())) {
            BlockPos basePos = pos.below();
            return level.getBlockState(basePos).is(RegistriesInit.TRADE_STAND_BLOCK.get()) ? basePos : null;
        }

        return null;
    }

    @Unique
    private void marketblocks$dispatchMirror(ClientLevel level, int breakerId, BlockPos targetPos, int progress) {
        marketblocks$isMirroring = true;
        try {
            level.destroyBlockProgress(marketblocks$toMirrorBreakerId(breakerId), targetPos, progress);
        } finally {
            marketblocks$isMirroring = false;
        }
    }

    @Unique
    private static int marketblocks$toMirrorBreakerId(int breakerId) {
        return breakerId | MARKETBLOCKS_MIRROR_ID_MASK;
    }

    @Unique
    private static boolean marketblocks$isMirrorBreakerId(int breakerId) {
        return (breakerId & MARKETBLOCKS_MIRROR_ID_MASK) != 0;
    }
}

