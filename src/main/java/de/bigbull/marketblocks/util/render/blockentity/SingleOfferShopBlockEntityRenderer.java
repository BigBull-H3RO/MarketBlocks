package de.bigbull.marketblocks.util.render.blockentity;

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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import de.bigbull.marketblocks.MarketBlocks;

public class SingleOfferShopBlockEntityRenderer implements BlockEntityRenderer<SingleOfferShopBlockEntity> {

    public SingleOfferShopBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(SingleOfferShopBlockEntity blockEntity) {
        return true;
    }

    @Override
    public boolean shouldRender(SingleOfferShopBlockEntity blockEntity, Vec3 cameraPos) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public void render(SingleOfferShopBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (blockEntity.getLevel() == null) {
            return;
        }

        MultiBufferSource defaultBufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        // NPC rendering must stay independent from the offer state.
        VisualShopNpcRenderer.render(blockEntity, partialTick, poseStack, defaultBufferSource, packedLight);

        if (!blockEntity.hasOffer()) {
            return;
        }

        ShopRenderConfig config = ShopRenderConfig.TRADE_STAND_DEFAULT;
        if (blockEntity.getBlockState().getBlock() instanceof BaseShopBlock shopBlock) {
            config = shopBlock.getRenderConfig(blockEntity.getBlockState());
        }

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        Font font = Minecraft.getInstance().font;
        Direction dir = blockEntity.getBlockState().getValue(BaseShopBlock.FACING);

        // --- 1. Offer-Item schwebend ueber dem Block ---
        ItemStack result = blockEntity.getOfferResult();
        if (!result.isEmpty()) {
            ShopRenderConfig.SlotRenderConfig offerItem = config.getOfferItem();
            BakedModel offerModel = itemRenderer.getModel(result, blockEntity.getLevel(), null, 0);
            float finalOfferScale = getFinalOfferScale(offerModel, offerItem, result);

            if (config.isOfferItemFloating()) {
                poseStack.pushPose();
                poseStack.translate(offerItem.x(), offerItem.y(), offerItem.z());
                float time = (blockEntity.getLevel().getGameTime() + partialTick) * 2.0f;
                poseStack.mulPose(Axis.YP.rotationDegrees(time % 360));
                applySlotRotation(poseStack, offerItem);
                poseStack.scale(finalOfferScale, finalOfferScale, finalOfferScale);
                itemRenderer.renderStatic(result, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                        poseStack, defaultBufferSource, blockEntity.getLevel(), 0);
                poseStack.popPose();
            } else {
                int displayCount = config.getOfferItemDisplayCount();
                long seed = blockEntity.getBlockPos().asLong();
                java.util.Random rand = new java.util.Random(seed);

                for (int i = 0; i < displayCount; i++) {
                    poseStack.pushPose();
                    double offsetX = (rand.nextDouble() - 0.5) * 0.4;
                    double offsetZ = (rand.nextDouble() - 0.5) * 0.4;
                    double offsetY = i * 0.05; // Stack them slightly

                    poseStack.translate(offerItem.x() + offsetX, offerItem.y() + offsetY, offerItem.z() + offsetZ);

                    float randomYaw = rand.nextFloat() * 360.0f;
                    poseStack.mulPose(Axis.YP.rotationDegrees(randomYaw));

                    applySlotRotation(poseStack, offerItem);
                    poseStack.scale(finalOfferScale, finalOfferScale, finalOfferScale);
                    itemRenderer.renderStatic(result, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                            poseStack, defaultBufferSource, blockEntity.getLevel(), 0);
                    poseStack.popPose();
                }
            }

            renderCountText(font, poseStack, defaultBufferSource, packedLight,
                    result.getCount(), config.getOfferCountText(), dir);
        }

        // --- Payment-Items auf der Frontseite ---
        ItemStack payment1 = blockEntity.getOfferPayment1();
        ItemStack payment2 = blockEntity.getOfferPayment2();

        if (config.isShowFrontOffer() && !result.isEmpty()) {
            renderPaymentItem(itemRenderer, font, poseStack, defaultBufferSource, packedLight, packedOverlay,
                    result, dir, config.getFrontOfferItem(), null);
        }

        if (config.isShowTradeArrow()) {
            renderTradeArrow(poseStack, defaultBufferSource, packedLight, dir, config.getTradeArrow());
        }

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

        if (countConfig != null) {
            renderCountText(font, poseStack, bufferSource, packedLight, stack.getCount(), countConfig, dir);
        }
    }

    private void renderTradeArrow(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                                  Direction dir, ShopRenderConfig.SlotRenderConfig arrowConfig) {
        ResourceLocation TRADE_ARROW = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/trade_arrow.png");

        Direction right = dir.getClockWise();
        double sideOffset = arrowConfig.x() - 0.5D;
        double x = 0.5D + dir.getStepX() * arrowConfig.z() + right.getStepX() * sideOffset;
        double z = 0.5D + dir.getStepZ() * arrowConfig.z() + right.getStepZ() * sideOffset;

        poseStack.pushPose();
        poseStack.translate(x, arrowConfig.y(), z);
        poseStack.mulPose(Axis.YP.rotationDegrees(-dir.toYRot()));
        applySlotRotation(poseStack, arrowConfig);

        float scale = arrowConfig.scale();
        poseStack.scale(scale, -scale, scale);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutout(TRADE_ARROW));
        Matrix4f matrix4f = poseStack.last().pose();

        float halfW = 0.5f;
        float halfH = 0.5f;

        vertexConsumer.addVertex(matrix4f, -halfW, -halfH, 0).setColor(255, 255, 255, 255).setUv(0.0F, 0.0F).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(poseStack.last(), 0, 0, 1);
        vertexConsumer.addVertex(matrix4f, -halfW,  halfH, 0).setColor(255, 255, 255, 255).setUv(0.0F, 1.0F).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(poseStack.last(), 0, 0, 1);
        vertexConsumer.addVertex(matrix4f,  halfW,  halfH, 0).setColor(255, 255, 255, 255).setUv(1.0F, 1.0F).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(poseStack.last(), 0, 0, 1);
        vertexConsumer.addVertex(matrix4f,  halfW, -halfH, 0).setColor(255, 255, 255, 255).setUv(1.0F, 0.0F).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(poseStack.last(), 0, 0, 1);

        poseStack.popPose();
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

    @Override
    public AABB getRenderBoundingBox(SingleOfferShopBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return new AABB(
                pos.getX() - 2, pos.getY(),
                pos.getZ() - 2,
                pos.getX() + 3, pos.getY() + 4,
                pos.getZ() + 3
        );
    }
}
