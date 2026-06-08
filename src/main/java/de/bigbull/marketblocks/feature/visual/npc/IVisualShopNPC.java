package de.bigbull.marketblocks.feature.visual.npc;

import de.bigbull.marketblocks.feature.singleoffer.settings.VillagerSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for shop entities that support visual NPC rendering.
 * Provides the required rendering parameters and state.
 */
public interface IVisualShopNPC {
    BlockPos getVisualShopPos();

    @Nullable
    Level getVisualLevel();

    Direction getVisualFacing();

    VillagerSettings getVillagerSettings();

    int getVisualAnimationNonce();

    byte getVisualAnimationEvent();

    int getVisualPurchaseCounter();

    int getVisualPaymentSuccessCounter();

    int getVisualPaymentFailCounter();

    boolean isVisualXpPurchaseFeedbackEnabled();

    ShopNpcAnimationState getVisualAnimationState();
}
