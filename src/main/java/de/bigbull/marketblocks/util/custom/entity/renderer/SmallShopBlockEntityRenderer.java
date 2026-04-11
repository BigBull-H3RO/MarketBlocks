package de.bigbull.marketblocks.util.custom.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.bigbull.marketblocks.util.block.BaseShopBlock;
import de.bigbull.marketblocks.util.block.ShopBlockConfig;
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

        // Get config from block
        ShopBlockConfig config = ShopBlockConfig.SMALL_SHOP_DEFAULT;
        if (blockEntity.getBlockState().getBlock() instanceof BaseShopBlock shopBlock) {
            config = shopBlock.getShopConfig();
        }

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        Font font = Minecraft.getInstance().font;

        // Kauf-Item schwebend über dem Block rendern (config-basierte Position)
        ItemStack result = blockEntity.getOfferResult();
        if (!result.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(config.getOfferItemX(), config.getOfferItemY(), config.getOfferItemZ());
            float time = (blockEntity.getLevel().getGameTime() + partialTick) * 2.0f;
            poseStack.mulPose(Axis.YP.rotationDegrees(time % 360));
            poseStack.scale(config.getOfferItemScale(), config.getOfferItemScale(), config.getOfferItemScale());
            itemRenderer.renderStatic(result, ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, bufferSource,
                    blockEntity.getLevel(), 0);
            poseStack.popPose();
        }

        // Bezahl-Items vor dem Block rendern
        Direction dir = blockEntity.getBlockState().getValue(BaseShopBlock.FACING);

        // Sammle alle Bezahl-Items die nicht leer sind
        ItemStack payment1 = blockEntity.getOfferPayment1();
        ItemStack payment2 = blockEntity.getOfferPayment2();

        int itemIndex = 0;
        if (!payment1.isEmpty()) {
            renderPaymentItem(itemRenderer, font, poseStack, bufferSource, packedLight, packedOverlay,
                    payment1, dir, itemIndex, config);
            itemIndex++;
        }
        if (!payment2.isEmpty()) {
            renderPaymentItem(itemRenderer, font, poseStack, bufferSource, packedLight, packedOverlay,
                    payment2, dir, itemIndex, config);
        }
    }

    private void renderPaymentItem(ItemRenderer itemRenderer, Font font, PoseStack poseStack, MultiBufferSource bufferSource,
                                   int packedLight, int packedOverlay, ItemStack stack, Direction dir, int index, ShopBlockConfig config) {
        if (stack.isEmpty()) {
            return;
        }

        // Position vor dem Block berechnen - Items untereinander (config-basiert)
        // index 0 = erstes Item, index 1 = zweites Item
        float y = (float) (config.getPaymentItemY() - index * config.getPaymentItemSpacing());
        
        // Lokales Koordinatensystem: Z=vor dem Block, X=seitlicher Offset.
        Direction right = dir.getClockWise();
        double sideOffset = config.getPaymentItemX() - 0.5D;
        double xOff = 0.5D + dir.getStepX() * config.getPaymentItemZ() + right.getStepX() * sideOffset;
        double zOff = 0.5D + dir.getStepZ() * config.getPaymentItemZ() + right.getStepZ() * sideOffset;

        // Item rendern
        poseStack.pushPose();
        poseStack.translate(xOff, y, zOff);
        poseStack.mulPose(Axis.YP.rotationDegrees(-dir.toYRot()));
        poseStack.scale(config.getPaymentItemScale(), config.getPaymentItemScale(), config.getPaymentItemScale());
        itemRenderer.renderStatic(stack, ItemDisplayContext.GUI, packedLight, packedOverlay, poseStack, bufferSource, null, 0);
        poseStack.popPose();

        // Menge als Text rendern (config-basiert)
        if (stack.getCount() > 1) {
            poseStack.pushPose();
            poseStack.translate(
                    xOff + right.getStepX() * config.getCountTextOffsetX(),
                    y - config.getCountTextOffsetY(),
                    zOff + right.getStepZ() * config.getCountTextOffsetX()
            );
            poseStack.mulPose(Axis.YP.rotationDegrees(-dir.toYRot()));
            poseStack.scale(config.getCountTextScale(), -config.getCountTextScale(), config.getCountTextScale());

            String count = Integer.toString(stack.getCount());
            int width = font.width(count);

            font.drawInBatch(count, -width / 2f, 0, 0xFFFFFF, true,
                    poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0x80000000, packedLight);
            poseStack.popPose();
        }
    }
}
