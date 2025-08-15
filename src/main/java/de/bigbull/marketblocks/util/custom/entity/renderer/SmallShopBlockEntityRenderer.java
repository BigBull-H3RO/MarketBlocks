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
            poseStack.translate(0.5D, 1.3D, 0.5D); // Höher positioniert
            float time = (blockEntity.getLevel().getGameTime() + partialTick) * 2.0f; // Langsamere Rotation
            poseStack.mulPose(Axis.YP.rotationDegrees(time % 360));
            poseStack.scale(0.8F, 0.8F, 0.8F);
            itemRenderer.renderStatic(result, ItemDisplayContext.GROUND, packedLight, packedOverlay, poseStack, bufferSource,
                    blockEntity.getLevel(), 0);
            poseStack.popPose();
        }

        // Bezahl-Items vor dem Block rendern
        Direction dir = blockEntity.getBlockState().getValue(SmallShopBlock.FACING);

        // Sammle alle Bezahl-Items die nicht leer sind
        ItemStack payment1 = blockEntity.getOfferPayment1();
        ItemStack payment2 = blockEntity.getOfferPayment2();

        int itemIndex = 0;
        if (!payment1.isEmpty()) {
            renderPaymentItem(itemRenderer, font, poseStack, bufferSource, packedLight, packedOverlay,
                    payment1, dir, itemIndex);
            itemIndex++;
        }
        if (!payment2.isEmpty()) {
            renderPaymentItem(itemRenderer, font, poseStack, bufferSource, packedLight, packedOverlay,
                    payment2, dir, itemIndex);
        }
    }

    private void renderPaymentItem(ItemRenderer itemRenderer, Font font, PoseStack poseStack, MultiBufferSource bufferSource,
                                   int packedLight, int packedOverlay, ItemStack stack, Direction dir, int index) {
        if (stack.isEmpty()) {
            return;
        }

        // Position vor dem Block berechnen - Items untereinander
        float y = 0.8F - index * 0.4F; // Items untereinander mit mehr Abstand
        double xOff = 0.5D + dir.getStepX() * 0.7D; // Etwas weiter weg vom Block
        double zOff = 0.5D + dir.getStepZ() * 0.7D;

        // Item rendern
        poseStack.pushPose();
        poseStack.translate(xOff, y, zOff);
        poseStack.mulPose(Axis.YP.rotationDegrees(-dir.toYRot()));
        poseStack.scale(0.6F, 0.6F, 0.6F); // Etwas größer
        itemRenderer.renderStatic(stack, ItemDisplayContext.GUI, packedLight, packedOverlay, poseStack, bufferSource, null, 0);
        poseStack.popPose();

        // Menge als Text rendern - größer und besser sichtbar
        if (stack.getCount() > 1) {
            poseStack.pushPose();
            poseStack.translate(xOff + dir.getStepX() * 0.15D, y - 0.15D, zOff + dir.getStepZ() * 0.15D);
            poseStack.mulPose(Axis.YP.rotationDegrees(-dir.toYRot()));
            poseStack.scale(0.015F, -0.015F, 0.015F); // Größere Schrift

            String count = Integer.toString(stack.getCount());
            int width = font.width(count);

            // Schwarzer Hintergrund für bessere Lesbarkeit
            font.drawInBatch(count, -width / 2f, 0, 0xFFFFFF, true,
                    poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0x80000000, packedLight);
            poseStack.popPose();
        }
    }
}
