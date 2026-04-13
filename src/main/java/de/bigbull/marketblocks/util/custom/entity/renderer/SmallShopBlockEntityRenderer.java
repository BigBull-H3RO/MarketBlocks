package de.bigbull.marketblocks.util.custom.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.bigbull.marketblocks.util.block.BaseShopBlock;
import de.bigbull.marketblocks.util.block.ShopRenderConfig;
import de.bigbull.marketblocks.util.block.entity.SmallShopBlockEntity;
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

        // Render-Offsets state-aware vom Block holen.
        ShopRenderConfig config = ShopRenderConfig.SMALL_SHOP_DEFAULT;
        if (blockEntity.getBlockState().getBlock() instanceof BaseShopBlock shopBlock) {
            config = shopBlock.getRenderConfig(blockEntity.getBlockState());
        }

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        Font font = Minecraft.getInstance().font;

        // Kauf-Item schwebend über dem Block rendern (config-basierte Position)
        ItemStack result = blockEntity.getOfferResult();
        if (!result.isEmpty()) {
            poseStack.pushPose();
            ShopRenderConfig.SlotRenderConfig offerItem = config.getOfferItem();
            poseStack.translate(offerItem.x(), offerItem.y(), offerItem.z());
            float time = (blockEntity.getLevel().getGameTime() + partialTick) * 2.0f;
            poseStack.mulPose(Axis.YP.rotationDegrees(time % 360));
            applySlotRotation(poseStack, offerItem);
            poseStack.scale(offerItem.scale(), offerItem.scale(), offerItem.scale());
            itemRenderer.renderStatic(result, ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, bufferSource,
                    blockEntity.getLevel(), 0);
            poseStack.popPose();

            if (result.getCount() > 1) {
                ShopRenderConfig.SlotRenderConfig countText = config.getOfferCountText();
                poseStack.pushPose();
                poseStack.translate(countText.x(), countText.y(), countText.z());
                applySlotRotation(poseStack, countText);
                poseStack.scale(countText.scale(), -countText.scale(), countText.scale());

                String countStr = Integer.toString(result.getCount());
                int textWidth = font.width(countStr);
                for (int i = 0; i < 4; i++) {
                    font.drawInBatch(countStr, -textWidth / 2f, 0, 0xFFFFFF, true,
                            poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0x80000000, packedLight);
                    poseStack.mulPose(Axis.YP.rotationDegrees(90));
                }
                poseStack.popPose();
            }
        }

        // Bezahl-Items vor dem Block rendern
        Direction dir = blockEntity.getBlockState().getValue(BaseShopBlock.FACING);

        // Sammle alle Bezahl-Items die nicht leer sind
        ItemStack payment1 = blockEntity.getOfferPayment1();
        ItemStack payment2 = blockEntity.getOfferPayment2();

        if (!payment1.isEmpty()) {
            renderPaymentItem(itemRenderer, font, poseStack, bufferSource, packedLight, packedOverlay,
                    payment1, dir, config.getPayment1Item(), config.getPayment1CountText());
        }
        if (!payment2.isEmpty()) {
            renderPaymentItem(itemRenderer, font, poseStack, bufferSource, packedLight, packedOverlay,
                    payment2, dir, config.getPayment2Item(), config.getPayment2CountText());
        }
    }

    private void renderPaymentItem(ItemRenderer itemRenderer, Font font, PoseStack poseStack, MultiBufferSource bufferSource,
                                   int packedLight, int packedOverlay, ItemStack stack, Direction dir,
                                   ShopRenderConfig.SlotRenderConfig itemConfig,
                                   ShopRenderConfig.SlotRenderConfig countConfig) {
        if (stack.isEmpty()) {
            return;
        }

        // Lokales Koordinatensystem: Z=vor dem Block, X=seitlicher Offset.
        Direction right = dir.getClockWise();
        double sideOffset = itemConfig.x() - 0.5D;
        double xOff = 0.5D + dir.getStepX() * itemConfig.z() + right.getStepX() * sideOffset;
        double zOff = 0.5D + dir.getStepZ() * itemConfig.z() + right.getStepZ() * sideOffset;

        // Item rendern
        poseStack.pushPose();
        poseStack.translate(xOff, itemConfig.y(), zOff);
        poseStack.mulPose(Axis.YP.rotationDegrees(-dir.toYRot()));
        applySlotRotation(poseStack, itemConfig);
        poseStack.scale(itemConfig.scale(), itemConfig.scale(), itemConfig.scale());
        itemRenderer.renderStatic(stack, ItemDisplayContext.GUI, packedLight, packedOverlay, poseStack, bufferSource, null, 0);
        poseStack.popPose();

        renderCountText(font, poseStack, bufferSource, packedLight, stack.getCount(), countConfig, dir, true);
    }

    private void renderCountText(Font font, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                                 int count, ShopRenderConfig.SlotRenderConfig countConfig, Direction dir, boolean localFacingCoords) {
        if (count <= 1) {
            return;
        }

        double x = countConfig.x();
        double y = countConfig.y();
        double z = countConfig.z();

        if (localFacingCoords) {
            Direction right = dir.getClockWise();
            double sideOffset = x - 0.5D;
            x = 0.5D + dir.getStepX() * z + right.getStepX() * sideOffset;
            z = 0.5D + dir.getStepZ() * z + right.getStepZ() * sideOffset;
        }

        poseStack.pushPose();
        poseStack.translate(x, y, z);
        // FIX: Rotation nur anwenden wenn Koordinaten im lokalen Block-Facing-System sind.
        // Für das Offer-Count-Text (localFacingCoords=false) sind x/z absolute Block-Koordinaten
        // (0.5, 0.5 = Blockmitte). Eine dir-abhängige Rotation würde den Text bei FACING=NORTH
        // auf den Kopf drehen und bei EAST/WEST seitlich stellen.
        if (localFacingCoords) {
            poseStack.mulPose(Axis.YP.rotationDegrees(-dir.toYRot()));
        }
        // (kein mulPose bei localFacingCoords=false – absolute Koordinaten brauchen keine Richtungsrotation)
        applySlotRotation(poseStack, countConfig);
        poseStack.scale(countConfig.scale(), -countConfig.scale(), countConfig.scale());

        String text = Integer.toString(count);
        int width = font.width(text);
        font.drawInBatch(text, -width / 2f, 0, 0xFFFFFF, true,
                poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0x80000000, packedLight);
        poseStack.popPose();
    }

    private static void applySlotRotation(PoseStack poseStack, ShopRenderConfig.SlotRenderConfig slot) {
        if (slot.yaw() != 0.0F) {
            poseStack.mulPose(Axis.YP.rotationDegrees(slot.yaw()));
        }
        if (slot.pitch() != 0.0F) {
            poseStack.mulPose(Axis.XP.rotationDegrees(slot.pitch()));
        }
        if (slot.roll() != 0.0F) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(slot.roll()));
        }
    }
}