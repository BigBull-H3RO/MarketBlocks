package de.bigbull.marketblocks.feature.singleoffer.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemStackHandler;
import de.bigbull.marketblocks.feature.visual.npc.ShopNpcAnimationState;
import de.bigbull.marketblocks.feature.visual.npc.VisualNpcAnimationEvent;

public class ShopVisualManager {

    private static final String NBT_VISUAL_ANIMATION_NONCE = "VisualAnimationNonce";
    private static final String NBT_VISUAL_ANIMATION_EVENT = "VisualAnimationEvent";
    private static final String NBT_VISUAL_PURCHASE_COUNTER = "VisualPurchaseCounter";
    private static final String NBT_VISUAL_PAYMENT_SUCCESS_COUNTER = "VisualPaymentSuccessCounter";
    private static final String NBT_VISUAL_PAYMENT_FAIL_COUNTER = "VisualPaymentFailCounter";

    private final SingleOfferShopBlockEntity blockEntity;

    private int visualAnimationNonce = 0;
    private byte visualAnimationEvent = VisualNpcAnimationEvent.NONE;
    private int visualPurchaseCounter = 0;
    private int visualPaymentSuccessCounter = 0;
    private int visualPaymentFailCounter = 0;
    private final ShopNpcAnimationState visualAnimationState = new ShopNpcAnimationState();

    private final ItemStack[] paymentFeedbackSnapshot = new ItemStack[]{ItemStack.EMPTY, ItemStack.EMPTY};
    private long lastPurchaseXpSoundTick = -1L;

    public ShopVisualManager(SingleOfferShopBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    public void load(CompoundTag tag) {
        this.visualAnimationNonce = tag.getInt(NBT_VISUAL_ANIMATION_NONCE);
        this.visualAnimationEvent = tag.getByte(NBT_VISUAL_ANIMATION_EVENT);
        this.visualPurchaseCounter = tag.getInt(NBT_VISUAL_PURCHASE_COUNTER);
        this.visualPaymentSuccessCounter = tag.getInt(NBT_VISUAL_PAYMENT_SUCCESS_COUNTER);
        this.visualPaymentFailCounter = tag.getInt(NBT_VISUAL_PAYMENT_FAIL_COUNTER);
    }

    public void save(CompoundTag tag) {
        tag.putInt(NBT_VISUAL_ANIMATION_NONCE, visualAnimationNonce);
        tag.putByte(NBT_VISUAL_ANIMATION_EVENT, visualAnimationEvent);
        tag.putInt(NBT_VISUAL_PURCHASE_COUNTER, visualPurchaseCounter);
        tag.putInt(NBT_VISUAL_PAYMENT_SUCCESS_COUNTER, visualPaymentSuccessCounter);
        tag.putInt(NBT_VISUAL_PAYMENT_FAIL_COUNTER, visualPaymentFailCounter);
    }

    public int getVisualAnimationNonce() {
        return visualAnimationNonce;
    }

    public byte getVisualAnimationEvent() {
        return visualAnimationEvent;
    }

    public int getVisualPurchaseCounter() {
        return visualPurchaseCounter;
    }

    public int getVisualPaymentSuccessCounter() {
        return visualPaymentSuccessCounter;
    }

    public int getVisualPaymentFailCounter() {
        return visualPaymentFailCounter;
    }

    public ShopNpcAnimationState getVisualAnimationState() {
        return visualAnimationState;
    }

    public void incrementVisualPurchaseCounter(int amount) {
        visualPurchaseCounter += amount;
    }

    public void triggerNpcAnimationEvent(byte event) {
        this.visualAnimationEvent = event;
        this.visualAnimationNonce++;
    }

    public void playPurchaseXpSound(int actualAmount) {
        Level level = blockEntity.getLevel();
        if (blockEntity.isPurchaseXpFeedbackSound() && level != null) {
            long now = level.getGameTime();
            if (now - lastPurchaseXpSoundTick > 4L) {
                float pitch = Math.min(0.7F + actualAmount * 0.06F, 1.6F);
                level.playSound(null, blockEntity.getBlockPos(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                        SoundSource.BLOCKS, 0.4F, pitch);
                lastPurchaseXpSoundTick = now;
            }
        }
    }

    public void refreshPaymentFeedbackSnapshot(ItemStackHandler paymentHandler) {
        for (int i = 0; i < paymentFeedbackSnapshot.length; i++) {
            paymentFeedbackSnapshot[i] = paymentHandler.getStackInSlot(i).copy();
        }
    }

    public void handlePaymentFeedbackChange(int slot, ItemStackHandler paymentHandler) {
        if (slot < 0 || slot >= paymentFeedbackSnapshot.length) {
            return;
        }

        ItemStack previous = paymentFeedbackSnapshot[slot];
        ItemStack current = paymentHandler.getStackInSlot(slot).copy();
        paymentFeedbackSnapshot[slot] = current.copy();

        Level level = blockEntity.getLevel();
        if (level == null || level.isClientSide || !blockEntity.hasOffer() || !blockEntity.getVillagerSettings().paymentSlotSoundsEnabled()) {
            return;
        }

        if (!isPaymentInsertion(previous, current)) {
            return;
        }

        if (matchesOfferPayment(current)) {
            visualPaymentSuccessCounter++;
        } else {
            visualPaymentFailCounter++;
        }
    }

    private static boolean isPaymentInsertion(ItemStack previous, ItemStack current) {
        if (current.isEmpty()) {
            return false;
        }
        if (previous == null || previous.isEmpty()) {
            return true;
        }
        if (ItemStack.isSameItemSameComponents(previous, current)) {
            return current.getCount() > previous.getCount();
        }
        return true;
    }

    private boolean matchesOfferPayment(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        ItemStack offerPayment1 = blockEntity.getOfferPayment1();
        ItemStack offerPayment2 = blockEntity.getOfferPayment2();
        return (!offerPayment1.isEmpty() && ItemStack.isSameItemSameComponents(offerPayment1, stack))
                || (!offerPayment2.isEmpty() && ItemStack.isSameItemSameComponents(offerPayment2, stack));
    }
}
