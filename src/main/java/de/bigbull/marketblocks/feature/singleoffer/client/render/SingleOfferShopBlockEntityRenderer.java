package de.bigbull.marketblocks.feature.singleoffer.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.feature.singleoffer.block.BaseShopBlock;
import de.bigbull.marketblocks.feature.singleoffer.block.CrateLayoutMode;
import de.bigbull.marketblocks.feature.singleoffer.block.ShopRenderConfig;
import de.bigbull.marketblocks.feature.singleoffer.block.ShopVisualType;
import de.bigbull.marketblocks.feature.singleoffer.entity.SingleOfferShopBlockEntity;
import de.bigbull.marketblocks.feature.singleoffer.settings.OfferItemSettings;
import de.bigbull.marketblocks.feature.visual.render.VisualShopNpcRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

import org.joml.Matrix4f;

/**
 * Custom BlockEntityRenderer for the {@link SingleOfferShopBlockEntity}.
 * Renders the floating or crate-bound items of the shop offer, the payment
 * items on the front,
 * the transaction arrows, and the item count texts.
 */
public class SingleOfferShopBlockEntityRenderer implements BlockEntityRenderer<SingleOfferShopBlockEntity> {
    private static final ResourceLocation TRADE_ARROW = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID,
            "textures/gui/icon/trade_arrow.png");

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

        ItemStack result = blockEntity.getOfferResult();
        OfferItemSettings offerSettings = blockEntity.getOfferItemSettings();
        boolean renderOfferItem = blockEntity.isOfferItemRenderingGloballyEnabled() && offerSettings.visible();

        int actualPackedLightFront = offerSettings.fullbright() ? LightTexture.FULL_BRIGHT : packedLight;

        if (!result.isEmpty()) {
            ShopRenderConfig.SlotRenderConfig offerItem = config.getOfferItem();
            BakedModel offerModel = itemRenderer.getModel(result, blockEntity.getLevel(), null, 0);
            float finalOfferScale = getFinalOfferScale(offerModel, offerItem, result) * offerSettings.scale();

            if (renderOfferItem) {

                if (config.isOfferItemFloating()) {
                    poseStack.pushPose();

                    float heightOffset = offerSettings.heightOffset();
                    float bobbingOffset = 0.0f;
                    if (offerSettings.bobbing()) {
                        float bobTime = (blockEntity.getLevel().getGameTime() + partialTick) * 0.05f;
                        bobbingOffset = Mth.sin(bobTime) * 0.1f;
                    }

                    poseStack.translate(offerItem.x(), offerItem.y() + heightOffset + bobbingOffset, offerItem.z());

                    float speed = offerSettings.speed();
                    if (speed > 0) {
                        float time = (blockEntity.getLevel().getGameTime() + partialTick) * speed;
                        poseStack.mulPose(Axis.YP.rotationDegrees(time % 360));
                    }

                    applySlotRotation(poseStack, offerItem);
                    poseStack.scale(finalOfferScale, finalOfferScale, finalOfferScale);
                    itemRenderer.renderStatic(result, ItemDisplayContext.FIXED, actualPackedLightFront, packedOverlay,
                            poseStack, defaultBufferSource, blockEntity.getLevel(), 0);
                    poseStack.popPose();
                } else {
                    int displayCount = offerSettings.dynamicFillLevel()
                            ? calculateDynamicOfferItemDisplayCount(blockEntity, result, offerSettings)
                            : (offerSettings.count() > 0 ? offerSettings.count() : config.getOfferItemDisplayCount());

                    long seed = blockEntity.getBlockPos().asLong();
                    Random rand = new Random(seed);

                    float heightOffset = offerSettings.heightOffset();
                    float spacingXZ = offerSettings.spacingXZ();
                    float spacingY = offerSettings.spacingY();
                    float chaosRotation = offerSettings.chaosRotation();
                    float baseRotation = offerSettings.rotation();

                    CrateLayoutMode layoutMode = offerSettings.layoutMode();
                    if (layoutMode == null)
                        layoutMode = CrateLayoutMode.SCATTERED;

                    ShopVisualType visualType = ShopVisualType.from(blockEntity.getBlockState().getBlock());

                    if (visualType.isMarketCrate()) {
                        poseStack.pushPose();

                        poseStack.translate(0.0f, heightOffset, 0.0f);

                        poseStack.translate(0.5f, 0.0f, 0.5f);
                        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - dir.toYRot()));
                        poseStack.translate(-0.5f, 0.0f, -0.5f);

                        poseStack.translate(0.5f, 15.0f / 16.0f, 15.5f / 16.0f);
                        poseStack.mulPose(Axis.XP.rotationDegrees(-22.5f));
                        poseStack.translate(0.0f, -0.125f, -0.515f);

                        boolean isBlock = result.getItem() instanceof BlockItem;
                        boolean isTool = !isBlock && isToolOrWeapon(result);

                        float baseScaleBlock = 0.85f;
                        float baseScaleTool = 0.75f;
                        float baseScaleFlat = 0.75f;

                        float boostBlock = 1.0f + Math.max(0.0f, finalOfferScale - 0.5f) * 1.6f;
                        float boostTool = 1.0f + Math.max(0.0f, finalOfferScale - 0.5f) * 1.2f;
                        float boostFlat = 1.0f + Math.max(0.0f, finalOfferScale - 0.5f) * 4.6f;

                        float itemScale = isBlock
                                ? (finalOfferScale * boostBlock * baseScaleBlock)
                                : (isTool ? (finalOfferScale * boostTool * baseScaleTool)
                                        : (finalOfferScale * boostFlat * baseScaleFlat));

                        float layerHeight = isBlock ? (itemScale * 0.55f) : (itemScale * 0.08f);

                        float radius = isBlock ? (itemScale * 0.20f) : (isTool ? itemScale * 0.25f : itemScale * 0.25f);
                        float maxOffsetX = Math.max(0.01f, 0.418f - radius);
                        float maxOffsetZ = Math.max(0.01f, 0.4285f - radius);

                        float baselineY = 0.01f;
                        float maxHeightLimit = 0.45f;

                        float effectiveSpacingXZ = isBlock ? (spacingXZ - 0.32f) : spacingXZ;

                        for (int i = 0; i < displayCount; i++) {
                            poseStack.pushPose();

                            float currentHeightOffset = 0f;
                            if (layoutMode == CrateLayoutMode.SCATTERED) {
                                currentHeightOffset = renderCrateItemLoose(poseStack, rand, i, layerHeight, maxOffsetX,
                                        maxOffsetZ, itemScale, baseRotation, chaosRotation, baselineY, isBlock,
                                        spacingY);
                            } else if (layoutMode == CrateLayoutMode.STACKED) {
                                currentHeightOffset = renderCrateItemStacked(poseStack, i, displayCount, layerHeight,
                                        maxOffsetX, maxOffsetZ, itemScale, effectiveSpacingXZ, spacingY, baseRotation,
                                        baselineY,
                                        isBlock);
                            }

                            if (currentHeightOffset > maxHeightLimit) {
                                poseStack.popPose();
                                break;
                            }

                            poseStack.scale(itemScale, itemScale, itemScale);

                            itemRenderer.renderStatic(result, ItemDisplayContext.FIXED, actualPackedLightFront,
                                    packedOverlay,
                                    poseStack, defaultBufferSource, blockEntity.getLevel(), 0);

                            poseStack.popPose();
                        }
                        poseStack.popPose();

                    } else {
                        for (int i = 0; i < displayCount; i++) {
                            poseStack.pushPose();

                            poseStack.translate(offerItem.x(), offerItem.y() + heightOffset, offerItem.z());
                            poseStack.mulPose(Axis.YP.rotationDegrees(baseRotation));

                            double offsetX = 0;
                            double offsetY = 0;
                            double offsetZ = 0;

                            if (layoutMode == CrateLayoutMode.STACKED) {
                                int gridSize = (int) Math.ceil(Math.sqrt(displayCount));
                                int x = i % gridSize;
                                int z = i / gridSize;
                                offsetX = (x - (gridSize - 1) / 2.0) * spacingXZ;
                                offsetZ = (z - (gridSize - 1) / 2.0) * spacingXZ;
                            } else if (layoutMode == CrateLayoutMode.SCATTERED) {
                                offsetX = (rand.nextDouble() - 0.5) * spacingXZ * 2;
                                offsetZ = (rand.nextDouble() - 0.5) * spacingXZ * 2;
                            }

                            poseStack.translate(offsetX, offsetY, offsetZ);

                            applySlotRotation(poseStack, offerItem);
                            poseStack.scale(finalOfferScale, finalOfferScale, finalOfferScale);
                            itemRenderer.renderStatic(result, ItemDisplayContext.FIXED, actualPackedLightFront,
                                    packedOverlay,
                                    poseStack, defaultBufferSource, blockEntity.getLevel(), 0);
                            poseStack.popPose();
                        }
                    }
                }
            }

            renderCountText(font, poseStack, defaultBufferSource, actualPackedLightFront,
                    result.getCount(), config.getOfferCountText(), dir);
        }

        ItemStack payment1 = blockEntity.getOfferPayment1();
        ItemStack payment2 = blockEntity.getOfferPayment2();

        if (config.isShowFrontOffer() && !result.isEmpty()) {
            renderPaymentItem(itemRenderer, font, poseStack, defaultBufferSource, actualPackedLightFront, packedOverlay,
                    result, dir, config.getFrontOfferItem(), null);
        }

        if (config.isShowTradeArrow()) {
            renderTradeArrow(poseStack, defaultBufferSource, actualPackedLightFront, dir, config.getTradeArrow());
        }

        if (!payment1.isEmpty()) {
            renderPaymentItem(itemRenderer, font, poseStack, defaultBufferSource, actualPackedLightFront, packedOverlay,
                    payment1, dir, config.getPayment1Item(), config.getPayment1CountText());
        }
        if (!payment2.isEmpty()) {
            renderPaymentItem(itemRenderer, font, poseStack, defaultBufferSource, actualPackedLightFront, packedOverlay,
                    payment2, dir, config.getPayment2Item(), config.getPayment2CountText());
        }
    }

    private float renderCrateItemLoose(PoseStack poseStack, Random rand, int index, float layerHeight, float maxOffsetX,
            float maxOffsetZ, float itemScale, float baseRotation, float chaosRotation, float baselineY,
            boolean isBlock, float spacingY) {
        float rx = (rand.nextFloat() * 2.0f - 1.0f) * maxOffsetX;
        float rz = (rand.nextFloat() * 2.0f - 1.0f) * maxOffsetZ;

        int looseItemsPerLayer = Math.max(2,
                (int) Math.floor((maxOffsetX * maxOffsetZ * 4.0f) / (itemScale * itemScale * 0.8f)));
        int looseLayer = index / looseItemsPerLayer;

        float hOffset = (looseLayer * layerHeight * 0.8f) + (rand.nextFloat() * layerHeight * 0.4f);
        float yRest = hOffset + (isBlock ? itemScale * 0.2f : 0) + baselineY + (spacingY * 0.5f);

        poseStack.translate(rx, yRest, rz);
        poseStack.mulPose(Axis.YP.rotationDegrees(baseRotation));

        if (chaosRotation > 0) {
            poseStack.mulPose(Axis.YP.rotationDegrees(rand.nextFloat() * 360f * chaosRotation));
            poseStack.mulPose(Axis.XP.rotationDegrees((rand.nextFloat() - 0.5f) * 45f * chaosRotation));
            poseStack.mulPose(Axis.ZP.rotationDegrees((rand.nextFloat() - 0.5f) * 45f * chaosRotation));
        }

        if (!isBlock) {
            poseStack.mulPose(Axis.XP.rotationDegrees(90f));
        }
        return yRest;
    }

    private float renderCrateItemStacked(PoseStack poseStack, int index, int displayCount, float layerHeight,
            float maxOffsetX, float maxOffsetZ, float itemScale, float spacingXZ, float spacingY, float baseRotation,
            float baselineY, boolean isBlock) {
        float stepX = itemScale * (1.0f + spacingXZ);
        float stepZ = itemScale * (1.0f + spacingXZ);
        float verticalSpacing = layerHeight * (1.0f + spacingY);

        int cols = Math.max(1, (int) Math.floor((maxOffsetX * 2.0f) / Math.max(0.05f, stepX)));
        int rows = Math.max(1, (int) Math.floor((maxOffsetZ * 2.0f) / Math.max(0.05f, stepZ)));
        int itemsPerLayer = cols * rows;

        int layer = index / itemsPerLayer;
        int indexInLayer = index % itemsPerLayer;
        int row = indexInLayer / cols;
        int col = indexInLayer % cols;

        int itemsInThisLayer = Math.min(itemsPerLayer, displayCount - layer * itemsPerLayer);
        int usedRows = (int) Math.ceil((double) itemsInThisLayer / cols);
        int colsInThisRow = Math.min(cols, itemsInThisLayer - row * cols);

        float rowStartX = -((colsInThisRow - 1) * stepX) / 2.0f;
        float startZ = -((usedRows - 1) * stepZ) / 2.0f;

        float posX = rowStartX + col * stepX;
        float posZ = startZ + row * stepZ;

        float hOffset = layer * verticalSpacing;
        float yRest = hOffset + (isBlock ? itemScale * 0.2f : 0) + baselineY;

        poseStack.translate(posX, yRest, posZ);
        poseStack.mulPose(Axis.YP.rotationDegrees(baseRotation));

        if (!isBlock) {
            poseStack.mulPose(Axis.XP.rotationDegrees(90f));
        }
        return yRest;
    }

    private static float getFinalOfferScale(BakedModel offerModel, ShopRenderConfig.SlotRenderConfig offerItem,
            ItemStack result) {
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

    private static int calculateDynamicOfferItemDisplayCount(SingleOfferShopBlockEntity blockEntity, ItemStack result,
            OfferItemSettings offerSettings) {
        if (result.isEmpty()) {
            return 0;
        }

        int storedItems = 0;
        int inventoryCapacity = 0;

        for (int slot = 0; slot < blockEntity.getInputHandler().getSlots(); slot++) {
            ItemStack stack = blockEntity.getInputHandler().getStackInSlot(slot);
            int slotCapacity = Math.min(blockEntity.getInputHandler().getSlotLimit(slot), result.getMaxStackSize());

            if (stack.isEmpty()) {
                inventoryCapacity += slotCapacity;
            } else if (ItemStack.isSameItemSameComponents(stack, result)) {
                storedItems += stack.getCount();
                inventoryCapacity += slotCapacity;
            }
        }

        if (storedItems <= 0 || inventoryCapacity <= 0) {
            return 0;
        }

        float fillRatio = Math.min(1.0f, (float) storedItems / (float) inventoryCapacity);
        return Math.max(1, (int) Math.ceil(fillRatio * offerSettings.count()));
    }

    private void renderPaymentItem(ItemRenderer itemRenderer, Font font, PoseStack poseStack,
            MultiBufferSource bufferSource, int packedLight, int packedOverlay,
            ItemStack stack, Direction dir,
            ShopRenderConfig.SlotRenderConfig itemConfig,
            ShopRenderConfig.SlotRenderConfig countConfig) {
        if (stack.isEmpty())
            return;

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

        if (is3D)
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

        float finalPaymentScale = itemConfig.scale();
        if (is3D)
            finalPaymentScale *= 1.3f;

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

        vertexConsumer.addVertex(matrix4f, -halfW, -halfH, 0).setColor(255, 255, 255, 255).setUv(0.0F, 0.0F)
                .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(packedLight)
                .setNormal(poseStack.last(), 0, 0, 1);
        vertexConsumer.addVertex(matrix4f, -halfW, halfH, 0).setColor(255, 255, 255, 255).setUv(0.0F, 1.0F)
                .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(packedLight)
                .setNormal(poseStack.last(), 0, 0, 1);
        vertexConsumer.addVertex(matrix4f, halfW, halfH, 0).setColor(255, 255, 255, 255).setUv(1.0F, 1.0F)
                .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(packedLight)
                .setNormal(poseStack.last(), 0, 0, 1);
        vertexConsumer.addVertex(matrix4f, halfW, -halfH, 0).setColor(255, 255, 255, 255).setUv(1.0F, 0.0F)
                .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(packedLight)
                .setNormal(poseStack.last(), 0, 0, 1);

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
        if (slot.yaw() != 0.0F)
            poseStack.mulPose(Axis.YP.rotationDegrees(slot.yaw()));
        if (slot.pitch() != 0.0F)
            poseStack.mulPose(Axis.XP.rotationDegrees(slot.pitch()));
        if (slot.roll() != 0.0F)
            poseStack.mulPose(Axis.ZP.rotationDegrees(slot.roll()));
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
                pos.getZ() + 3);
    }
}
