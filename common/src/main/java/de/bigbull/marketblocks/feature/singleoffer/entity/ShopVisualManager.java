package de.bigbull.marketblocks.feature.singleoffer.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import de.bigbull.marketblocks.platform.inventory.CommonItemStackHandler;
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

    private final ItemStack[] paymentFeedbackSnapshot = new ItemStack[]{ItemStack.EMPTY, ItemStack.EMPTY};
    private long lastPurchaseXpSoundTick = -1L;

    public ShopVisualManager(SingleOfferShopBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    public void load(CompoundTag tag) {
        this.visualAnimationNonce = tag.getIntOr(NBT_VISUAL_ANIMATION_NONCE, 0);
        this.visualAnimationEvent = tag.getByteOr(NBT_VISUAL_ANIMATION_EVENT, (byte) 0);
        this.visualPurchaseCounter = tag.getIntOr(NBT_VISUAL_PURCHASE_COUNTER, 0);
        this.visualPaymentSuccessCounter = tag.getIntOr(NBT_VISUAL_PAYMENT_SUCCESS_COUNTER, 0);
        this.visualPaymentFailCounter = tag.getIntOr(NBT_VISUAL_PAYMENT_FAIL_COUNTER, 0);
    }

    public void load(net.minecraft.world.level.storage.ValueInput input) {
        this.visualAnimationNonce = input.getIntOr(NBT_VISUAL_ANIMATION_NONCE, 0);
        this.visualAnimationEvent = input.getByteOr(NBT_VISUAL_ANIMATION_EVENT, (byte) 0);
        this.visualPurchaseCounter = input.getIntOr(NBT_VISUAL_PURCHASE_COUNTER, 0);
        this.visualPaymentSuccessCounter = input.getIntOr(NBT_VISUAL_PAYMENT_SUCCESS_COUNTER, 0);
        this.visualPaymentFailCounter = input.getIntOr(NBT_VISUAL_PAYMENT_FAIL_COUNTER, 0);
    }

    public void save(CompoundTag tag) {
        tag.putInt(NBT_VISUAL_ANIMATION_NONCE, visualAnimationNonce);
        tag.putByte(NBT_VISUAL_ANIMATION_EVENT, visualAnimationEvent);
        tag.putInt(NBT_VISUAL_PURCHASE_COUNTER, visualPurchaseCounter);
        tag.putInt(NBT_VISUAL_PAYMENT_SUCCESS_COUNTER, visualPaymentSuccessCounter);
        tag.putInt(NBT_VISUAL_PAYMENT_FAIL_COUNTER, visualPaymentFailCounter);
    }

    public void save(net.minecraft.world.level.storage.ValueOutput output) {
        output.putInt(NBT_VISUAL_ANIMATION_NONCE, visualAnimationNonce);
        output.putByte(NBT_VISUAL_ANIMATION_EVENT, visualAnimationEvent);
        output.putInt(NBT_VISUAL_PURCHASE_COUNTER, visualPurchaseCounter);
        output.putInt(NBT_VISUAL_PAYMENT_SUCCESS_COUNTER, visualPaymentSuccessCounter);
        output.putInt(NBT_VISUAL_PAYMENT_FAIL_COUNTER, visualPaymentFailCounter);
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

    public void refreshPaymentFeedbackSnapshot(CommonItemStackHandler paymentHandler) {
        for (int i = 0; i < paymentFeedbackSnapshot.length; i++) {
            paymentFeedbackSnapshot[i] = paymentHandler.getStackInSlot(i).copy();
        }
    }

    public void handlePaymentFeedbackChange(int slot, CommonItemStackHandler paymentHandler) {
        if (slot < 0 || slot >= paymentFeedbackSnapshot.length) {
            return;
        }

        ItemStack previous = paymentFeedbackSnapshot[slot];
        ItemStack current = paymentHandler.getStackInSlot(slot).copy();
        paymentFeedbackSnapshot[slot] = current.copy();

        Level level = blockEntity.getLevel();
        if (level == null || level.isClientSide() || !blockEntity.hasOffer() || !blockEntity.getVillagerSettings().paymentSlotSoundsEnabled()) {
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
