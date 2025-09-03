package de.bigbull.marketblocks.util.custom.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.bigbull.marketblocks.util.custom.block.SmallShopBlock;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders the items associated with a {@link SmallShopBlockEntity}.
 * This includes the result item spinning above the block and the payment items in front.
 */
public class SmallShopBlockEntityRenderer implements BlockEntityRenderer<SmallShopBlockEntity> {

    private static final float RESULT_ITEM_Y_OFFSET = 1.3F;
    private static final float RESULT_ITEM_SCALE = 0.8F;
    private static final float RESULT_ITEM_ROTATION_SPEED = 2.0F;

    private static final float PAYMENT_ITEM_SCALE = 0.4F;
    private static final float PAYMENT_ITEM_Y_OFFSET = 0.8F;
    private static final float PAYMENT_ITEM_Y_SPACING = 0.4F;
    private static final double PAYMENT_ITEM_DISTANCE_MOD = 0.7D;

    private static final float COUNT_TEXT_SCALE = 0.015F;
    private static final double COUNT_TEXT_DISTANCE_MOD = 0.15D;
    private static final float COUNT_TEXT_Y_OFFSET = -0.15F;
    private static final int COUNT_TEXT_COLOR = 0xFFFFFF;
    private static final int COUNT_TEXT_BG_COLOR = 0x80000000;

    private final ItemRenderer itemRenderer;
    private final Font font;

    public SmallShopBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
        this.font = Minecraft.getInstance().font;
    }

    @Override
    public void render(@NotNull SmallShopBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!blockEntity.hasOffer() || blockEntity.getLevel() == null) {
            return;
        }

        renderResultItem(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        renderPaymentItems(blockEntity, poseStack, bufferSource, packedLight, packedOverlay);
    }

    /**
     * Renders the spinning result item above the shop block.
     */
    private void renderResultItem(@NotNull SmallShopBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack resultStack = blockEntity.getOfferResult();
        if (resultStack.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5D, RESULT_ITEM_Y_OFFSET, 0.5D);
        float time = (blockEntity.getLevel().getGameTime() + partialTick) * RESULT_ITEM_ROTATION_SPEED;
        poseStack.mulPose(Axis.YP.rotationDegrees(time % 360));
        poseStack.scale(RESULT_ITEM_SCALE, RESULT_ITEM_SCALE, RESULT_ITEM_SCALE);
        itemRenderer.renderStatic(resultStack, ItemDisplayContext.GROUND, packedLight, packedOverlay, poseStack, bufferSource, blockEntity.getLevel(), 0);
        poseStack.popPose();
    }

    /**
     * Renders the required payment items in front of the shop block.
     */
    private void renderPaymentItems(@NotNull SmallShopBlockEntity blockEntity, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Direction facing = blockEntity.getBlockState().getValue(SmallShopBlock.FACING);

        List<ItemStack> payments = new ArrayList<>();
        if (!blockEntity.getOfferPayment1().isEmpty()) payments.add(blockEntity.getOfferPayment1());
        if (!blockEntity.getOfferPayment2().isEmpty()) payments.add(blockEntity.getOfferPayment2());

        for (int i = 0; i < payments.size(); i++) {
            renderSinglePaymentItem(payments.get(i), facing, i, poseStack, bufferSource, packedLight, packedOverlay);
        }
    }

    /**
     * Renders a single payment item and its count label.
     */
    private void renderSinglePaymentItem(@NotNull ItemStack stack, @NotNull Direction dir, int index, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        float y = PAYMENT_ITEM_Y_OFFSET - (index * PAYMENT_ITEM_Y_SPACING);
        double xOff = 0.5D + dir.getStepX() * PAYMENT_ITEM_DISTANCE_MOD;
        double zOff = 0.5D + dir.getStepZ() * PAYMENT_ITEM_DISTANCE_MOD;

        // Render the item
        poseStack.pushPose();
        poseStack.translate(xOff, y, zOff);
        poseStack.mulPose(Axis.YP.rotationDegrees(-dir.toYRot()));
        poseStack.scale(PAYMENT_ITEM_SCALE, PAYMENT_ITEM_SCALE, PAYMENT_ITEM_SCALE);
        itemRenderer.renderStatic(stack, ItemDisplayContext.GUI, packedLight, packedOverlay, poseStack, bufferSource, null, 0);
        poseStack.popPose();

        // Render the count label if greater than 1
        if (stack.getCount() > 1) {
            poseStack.pushPose();
            poseStack.translate(xOff + dir.getStepX() * COUNT_TEXT_DISTANCE_MOD, y + COUNT_TEXT_Y_OFFSET, zOff + dir.getStepZ() * COUNT_TEXT_DISTANCE_MOD);
            poseStack.mulPose(Axis.YP.rotationDegrees(-dir.toYRot()));
            poseStack.scale(COUNT_TEXT_SCALE, -COUNT_TEXT_SCALE, COUNT_TEXT_SCALE);

            String count = Integer.toString(stack.getCount());
            int width = font.width(count);

            // Render with a dark background for readability
            font.drawInBatch(count, -width / 2f, 0, COUNT_TEXT_COLOR, true,
                    poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, COUNT_TEXT_BG_COLOR, packedLight);
            poseStack.popPose();
        }
    }
}
