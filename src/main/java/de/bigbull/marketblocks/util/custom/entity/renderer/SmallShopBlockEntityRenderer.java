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
import net.minecraft.world.level.Level;

public class SmallShopBlockEntityRenderer implements BlockEntityRenderer<SmallShopBlockEntity> {
    public SmallShopBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(SmallShopBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource,
                       int packedLight, int packedOverlay) {
        if (!blockEntity.hasOffer() || blockEntity.getLevel() == null) {
            return;
        }

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        Font font = Minecraft.getInstance().font;

        // Kauf-Item schwebend über dem Block rendern
        ItemStack result = blockEntity.getOfferResult();
        if (!result.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 1.1D, 0.5D);
            float time = (blockEntity.getLevel().getGameTime() + partialTick) % 360;
            poseStack.mulPose(Axis.YP.rotationDegrees(time));
            poseStack.scale(0.8F, 0.8F, 0.8F);
            itemRenderer.renderStatic(result, ItemDisplayContext.GROUND, packedLight, packedOverlay, poseStack, bufferSource,
                    blockEntity.getLevel(), 0);
            poseStack.popPose();
        }

        // Bezahl-Items vor dem Block rendern
        Direction dir = blockEntity.getBlockState().getValue(SmallShopBlock.FACING);
        renderPaymentItem(itemRenderer, font, poseStack, bufferSource, packedLight, packedOverlay,
                blockEntity.getOfferPayment1(), dir, 0, blockEntity.getLevel());
        if (!blockEntity.getOfferPayment2().isEmpty()) {
            renderPaymentItem(itemRenderer, font, poseStack, bufferSource, packedLight, packedOverlay,
                    blockEntity.getOfferPayment2(), dir, 1, blockEntity.getLevel());
        }
    }

    private void renderPaymentItem(ItemRenderer itemRenderer, Font font, PoseStack poseStack, MultiBufferSource bufferSource,
                                   int packedLight, int packedOverlay, ItemStack stack, Direction dir, int index,
                                   Level level) {
        if (stack.isEmpty()) {
            return;
        }

        // Position vor dem Block berechnen
        float y = 0.7F - index * 0.5F; // Items untereinander
        double xOff = 0.5D + dir.getStepX() * 0.6D;
        double zOff = 0.5D + dir.getStepZ() * 0.6D;

        poseStack.pushPose();
        poseStack.translate(xOff, y, zOff);
        poseStack.mulPose(Axis.YP.rotationDegrees(-dir.toYRot()));
        poseStack.scale(0.5F, 0.5F, 0.5F);
        itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, packedLight, packedOverlay, poseStack, bufferSource,
                level, 0);

        // Menge anzeigen
        poseStack.pushPose();
        poseStack.translate(xOff, y, zOff + 0.01D); // etwas nach vorne, um Z-Fighting zu vermeiden
        poseStack.mulPose(Axis.YP.rotationDegrees(-dir.toYRot()));
        poseStack.scale(0.01F, 0.01F, 0.01F);
        String count = Integer.toString(stack.getCount());
        font.drawInBatch(count, -font.width(count) / 2f, 0, 0xFFFFFF, false,
                poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, packedLight);
        poseStack.popPose();
    }
}
