package de.bigbull.marketblocks.feature.visual.npc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface IVisualShopNPC {
    BlockPos getVisualShopPos();

    @Nullable
    Level getVisualLevel();

    Direction getVisualFacing();

    ShopVisualSettings getVisualSettings();

    int getVisualAnimationNonce();

    byte getVisualAnimationEvent();

    int getVisualPurchaseCounter();

    int getVisualPaymentSuccessCounter();

    int getVisualPaymentFailCounter();

    boolean isVisualXpPurchaseFeedbackEnabled();

    ShopNpcAnimationState getVisualAnimationState();
}

