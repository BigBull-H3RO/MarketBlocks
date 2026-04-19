package de.bigbull.marketblocks.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.bigbull.marketblocks.shop.singleoffer.block.BaseShopBlock;
import de.bigbull.marketblocks.shop.singleoffer.block.entity.SingleOfferShopBlockEntity;
import de.bigbull.marketblocks.util.block.ShopRenderConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.*;

public class SingleOfferShopBlockEntityRenderer implements BlockEntityRenderer<SingleOfferShopBlockEntity> {

    public SingleOfferShopBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(SingleOfferShopBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!blockEntity.hasOffer() || blockEntity.getLevel() == null) {
            return;
        }

        ShopRenderConfig config = ShopRenderConfig.TRADE_STAND_DEFAULT;
        if (blockEntity.getBlockState().getBlock() instanceof BaseShopBlock shopBlock) {
            config = shopBlock.getRenderConfig(blockEntity.getBlockState());
        }

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        Font font = Minecraft.getInstance().font;
        Direction dir = blockEntity.getBlockState().getValue(BaseShopBlock.FACING);

        // Wir nutzen den Standard-Buffer von Minecraft, damit der Block-Abbau-Overlay (Risse) ignoriert wird!
        MultiBufferSource defaultBufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        // --- 1. Offer-Item schwebend über dem Block ---
        ItemStack result = blockEntity.getOfferResult();
        if (!result.isEmpty()) {
            poseStack.pushPose();
            ShopRenderConfig.SlotRenderConfig offerItem = config.getOfferItem();
            poseStack.translate(offerItem.x(), offerItem.y(), offerItem.z());
            float time = (blockEntity.getLevel().getGameTime() + partialTick) * 2.0f;
            poseStack.mulPose(Axis.YP.rotationDegrees(time % 360));
            applySlotRotation(poseStack, offerItem);

            BakedModel offerModel = itemRenderer.getModel(result, blockEntity.getLevel(), null, 0);
            float finalOfferScale = getFinalOfferScale(offerModel, offerItem, result);

            poseStack.scale(finalOfferScale, finalOfferScale, finalOfferScale);

            itemRenderer.renderStatic(result, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                    poseStack, defaultBufferSource, blockEntity.getLevel(), 0);
            poseStack.popPose();

            renderCountText(font, poseStack, defaultBufferSource, packedLight,
                    result.getCount(), config.getOfferCountText(), dir);
        }

        // --- Payment-Items auf der Frontseite ---
        ItemStack payment1 = blockEntity.getOfferPayment1();
        ItemStack payment2 = blockEntity.getOfferPayment2();

        if (!payment1.isEmpty()) {
            renderPaymentItem(itemRenderer, font, poseStack, defaultBufferSource, packedLight, packedOverlay,
                    payment1, dir, config.getPayment1Item(), config.getPayment1CountText());
        }
        if (!payment2.isEmpty()) {
            renderPaymentItem(itemRenderer, font, poseStack, defaultBufferSource, packedLight, packedOverlay,
                    payment2, dir, config.getPayment2Item(), config.getPayment2CountText());
        }
    }

    private static float getFinalOfferScale(BakedModel offerModel, ShopRenderConfig.SlotRenderConfig offerItem, ItemStack result) {
        boolean isOffer3D = offerModel != null && offerModel.isGui3d();
        float finalOfferScale = offerItem.scale();

        if (!isOffer3D) {
            if (isToolOrWeapon(result)) {
                finalOfferScale *= 0.8f;
            } else {
                finalOfferScale *= 0.7f;
            }
        }
        return finalOfferScale;
    }

    private void renderPaymentItem(ItemRenderer itemRenderer, Font font, PoseStack poseStack,
                                   MultiBufferSource bufferSource, int packedLight, int packedOverlay,
                                   ItemStack stack, Direction dir,
                                   ShopRenderConfig.SlotRenderConfig itemConfig,
                                   ShopRenderConfig.SlotRenderConfig countConfig) {
        if (stack.isEmpty()) return;

        Direction right = dir.getClockWise();
        double sideOffset = itemConfig.x() - 0.5D;
        double xOff = 0.5D + dir.getStepX() * itemConfig.z() + right.getStepX() * sideOffset;
        double zOff = 0.5D + dir.getStepZ() * itemConfig.z() + right.getStepZ() * sideOffset;

        poseStack.pushPose();
        poseStack.translate(xOff, itemConfig.y(), zOff);
        poseStack.mulPose(Axis.YP.rotationDegrees(-dir.toYRot()));
        applySlotRotation(poseStack, itemConfig);

        BakedModel bakedModel = Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(stack);
        boolean is3D = bakedModel != null && bakedModel.isGui3d();
        ItemDisplayContext displayContext = is3D ? ItemDisplayContext.FIXED : ItemDisplayContext.GUI;

        if (is3D) poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

        float finalPaymentScale = itemConfig.scale();
        if (is3D) finalPaymentScale *= 1.3f;

        poseStack.scale(finalPaymentScale, finalPaymentScale, finalPaymentScale);

        itemRenderer.renderStatic(stack, displayContext, packedLight, packedOverlay,
                poseStack, bufferSource, null, 0);
        poseStack.popPose();

        renderCountText(font, poseStack, bufferSource, packedLight, stack.getCount(), countConfig, dir);
    }

    private void renderCountText(Font font, PoseStack poseStack, MultiBufferSource bufferSource,
                                 int packedLight, int count,
                                 ShopRenderConfig.SlotRenderConfig countConfig, Direction dir) {
        Direction right = dir.getClockWise();
        double sideOffset = countConfig.x() - 0.5D;
        double x = 0.5D + dir.getStepX() * countConfig.z() + right.getStepX() * sideOffset;
        double z = 0.5D + dir.getStepZ() * countConfig.z() + right.getStepZ() * sideOffset;

        poseStack.pushPose();
        poseStack.translate(x, countConfig.y(), z);
        poseStack.mulPose(Axis.YP.rotationDegrees(-dir.toYRot()));
        applySlotRotation(poseStack, countConfig);
        poseStack.scale(countConfig.scale(), -countConfig.scale(), countConfig.scale());

        String text = "x" + count;
        int width = font.width(text);

        font.drawInBatch(text, -width / 2f, 0, 0xFFFFFF, false,
                poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, packedLight);

        poseStack.popPose();
    }

    private static void applySlotRotation(PoseStack poseStack, ShopRenderConfig.SlotRenderConfig slot) {
        if (slot.yaw() != 0.0F) poseStack.mulPose(Axis.YP.rotationDegrees(slot.yaw()));
        if (slot.pitch() != 0.0F) poseStack.mulPose(Axis.XP.rotationDegrees(slot.pitch()));
        if (slot.roll() != 0.0F) poseStack.mulPose(Axis.ZP.rotationDegrees(slot.roll()));
    }

    private static boolean isToolOrWeapon(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof TieredItem || item instanceof SwordItem || item instanceof TridentItem ||
                item instanceof ProjectileWeaponItem || item instanceof ShieldItem || item instanceof MaceItem;
    }
}
