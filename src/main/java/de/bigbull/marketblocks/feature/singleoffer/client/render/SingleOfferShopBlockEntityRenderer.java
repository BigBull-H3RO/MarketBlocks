package de.bigbull.marketblocks.feature.singleoffer.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.bigbull.marketblocks.feature.singleoffer.block.BaseShopBlock;
import de.bigbull.marketblocks.feature.singleoffer.block.CrateLayoutMode;
import de.bigbull.marketblocks.feature.singleoffer.block.MarketCrateBlock;
import net.minecraft.world.item.BlockItem;
import de.bigbull.marketblocks.feature.singleoffer.entity.SingleOfferShopBlockEntity;
import de.bigbull.marketblocks.feature.singleoffer.block.ShopRenderConfig;
import de.bigbull.marketblocks.feature.visual.render.VisualShopNpcRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import de.bigbull.marketblocks.feature.visual.npc.ShopVisualSettings;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import de.bigbull.marketblocks.MarketBlocks;

public class SingleOfferShopBlockEntityRenderer implements BlockEntityRenderer<SingleOfferShopBlockEntity> {
    private static final int MAX_DYNAMIC_OFFER_ITEM_DISPLAY_COUNT = 10;

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
        ShopVisualSettings visualSettings = blockEntity.getVisualSettings();
        boolean renderOfferItem = blockEntity.isOfferItemRenderingGloballyEnabled() && visualSettings.offerItemVisible();

        int actualPackedLightFront = visualSettings.offerItemFullbright() ? LightTexture.FULL_BRIGHT : packedLight;

        if (!result.isEmpty() && renderOfferItem) {
            ShopRenderConfig.SlotRenderConfig offerItem = config.getOfferItem();
            BakedModel offerModel = itemRenderer.getModel(result, blockEntity.getLevel(), null, 0);
            float finalOfferScale = getFinalOfferScale(offerModel, offerItem, result) * visualSettings.offerItemScale();

            if (config.isOfferItemFloating()) {
                poseStack.pushPose();

                float heightOffset = visualSettings.offerItemHeightOffset();
                float bobbingOffset = 0.0f;
                if (visualSettings.offerItemBobbing()) {
                    float bobTime = (blockEntity.getLevel().getGameTime() + partialTick) * 0.05f;
                    bobbingOffset = net.minecraft.util.Mth.sin(bobTime) * 0.1f;
                }

                poseStack.translate(offerItem.x(), offerItem.y() + heightOffset + bobbingOffset, offerItem.z());

                float speed = visualSettings.offerItemSpeed();
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
                int displayCount = visualSettings.dynamicFillLevel()
                        ? calculateDynamicOfferItemDisplayCount(blockEntity, result)
                        : (visualSettings.offerItemCount() > 0 ? visualSettings.offerItemCount() : config.getOfferItemDisplayCount());
                long seed = blockEntity.getBlockPos().asLong();
                java.util.Random rand = new java.util.Random(seed);
                float heightOffset = visualSettings.offerItemHeightOffset();
                float spacing = visualSettings.offerItemSpacing();
                float chaosRotation = visualSettings.offerItemChaosRotation();
                float baseRotation = visualSettings.offerItemRotation();
                CrateLayoutMode layoutMode = visualSettings.offerItemLayoutMode();

                boolean isMarketCrate = blockEntity.getBlockState().getBlock() instanceof MarketCrateBlock;

                if (isMarketCrate) {
                    // Exakte Korb-Ausrichtung aus dem Blockbench-Modell (Reihenfolge ist wichtig).
                    poseStack.pushPose();
                    poseStack.translate(0.5f, 0.9375f, 0.96875f);
                    poseStack.mulPose(Axis.XP.rotationDegrees(-22.5f));
                    poseStack.translate(0.0f, -0.125f, -0.5f);

                    // Innerer Korbbereich laut Modell: X/Z ~ [2.25..13.75] px, zusätzlicher Sicherheitsrand gegen Clipping.
                    final float texturePadding = 0.15f;
                    final float innerHalfSpan = (13.75f - 2.25f) / 32.0f; // 5.75 px -> 0.359375 blocks
                    final float safeHalfSpan = Math.max(0.05f, innerHalfSpan - texturePadding);
                    final float maxOffsetX = safeHalfSpan;
                    final float maxOffsetZ = safeHalfSpan;

                    // Höhere Scale macht die Items dicker -> Layerhöhe wächst mit.
                    boolean isBlock = result.getItem() instanceof BlockItem;
                    double baseLayerHeight = isBlock ? 0.25 : 0.08;
                    double layerHeight = baseLayerHeight * Math.max(0.35f, finalOfferScale);

                    // Spacing im sicheren Korbraum halten.
                    double minSpacing = 0.08;
                    double maxSpacing = Math.min(0.20, maxOffsetX * 1.6);
                    double spacingBlocks = minSpacing + (maxSpacing - minSpacing) * spacing;

                    int cols = Math.max(1, (int) Math.floor((maxOffsetX * 2.0) / Math.max(0.01, spacingBlocks)) + 1);
                    int rows = Math.max(1, (int) Math.floor((maxOffsetZ * 2.0) / Math.max(0.01, spacingBlocks)) + 1);
                    int itemsPerLayer = Math.max(1, cols * rows);

                    for (int i = 0; i < displayCount; i++) {
                        poseStack.pushPose();

                        int layer = i / itemsPerLayer;
                        int indexInLayer = i % itemsPerLayer;

                        double offsetX;
                        double offsetZ;

                        if (layoutMode == CrateLayoutMode.GESTAPELT) {
                            int xIdx = indexInLayer % cols;
                            int zIdx = indexInLayer / cols;
                            double centerOffsetX = (xIdx - (cols - 1) / 2.0) * spacingBlocks;
                            double centerOffsetZ = (zIdx - (rows - 1) / 2.0) * spacingBlocks;
                            offsetX = Math.clamp(centerOffsetX, -maxOffsetX, maxOffsetX);
                            offsetZ = Math.clamp(centerOffsetZ, -maxOffsetZ, maxOffsetZ);
                        } else {
                            offsetX = rand.nextDouble() * (maxOffsetX * 2.0) - maxOffsetX;
                            offsetZ = rand.nextDouble() * (maxOffsetZ * 2.0) - maxOffsetZ;
                        }

                        double offsetY = layer * layerHeight;
                        poseStack.translate(offsetX, offsetY, offsetZ);

                        double itemRotationOffset = 0.0;
                        if (layoutMode == CrateLayoutMode.LOSE) {
                            itemRotationOffset = (rand.nextDouble() * 2.0 - 1.0) * 30.0 * chaosRotation;
                        }
                        poseStack.mulPose(Axis.YP.rotationDegrees(baseRotation + (float) itemRotationOffset));

                        // Fix: Items should lie flat in the basket, not stand upright.
                        // This rotates them by 90 degrees around the X-axis to achieve a "lying down" effect.
                        poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));

                        // Scale nach Korb-Transform, direkt vor dem eigentlichen Item-Render.
                        poseStack.scale(finalOfferScale, finalOfferScale, finalOfferScale);
                        itemRenderer.renderStatic(result, ItemDisplayContext.FIXED, actualPackedLightFront, packedOverlay,
                                poseStack, defaultBufferSource, blockEntity.getLevel(), 0);
                        poseStack.popPose();
                    }
                    poseStack.popPose();
                } else {
                    // Fallback: preserve previous non-market-crate behaviour using the (now reduced) enum
                    for (int i = 0; i < displayCount; i++) {
                        poseStack.pushPose();

                        // Translate to base position
                        poseStack.translate(offerItem.x(), offerItem.y() + heightOffset, offerItem.z());

                        // Apply base rotation to the entire item group
                        poseStack.mulPose(Axis.YP.rotationDegrees(baseRotation));

                        // Calculate position offsets based on layout mode
                        double offsetX = 0;
                        double offsetY = 0;
                        double offsetZ = 0;

                        // The old STACK/GRID/CHAOS modes no longer exist; use simple stacking/grid/chaos
                        if (layoutMode == CrateLayoutMode.GESTAPELT) {
                            int gridSize = (int) Math.ceil(Math.sqrt(displayCount));
                            int x = i % gridSize;
                            int z = i / gridSize;
                            offsetX = (x - (gridSize - 1) / 2.0) * spacing;
                            offsetZ = (z - (gridSize - 1) / 2.0) * spacing;
                        } else if (layoutMode == CrateLayoutMode.LOSE) {
                            offsetX = (rand.nextDouble() - 0.5) * spacing * 2;
                            offsetZ = (rand.nextDouble() - 0.5) * spacing * 2;
                        }

                        // Apply calculated offsets
                        poseStack.translate(offsetX, offsetY, offsetZ);

                        applySlotRotation(poseStack, offerItem);
                        poseStack.scale(finalOfferScale, finalOfferScale, finalOfferScale);
                        itemRenderer.renderStatic(result, ItemDisplayContext.FIXED, actualPackedLightFront, packedOverlay,
                                poseStack, defaultBufferSource, blockEntity.getLevel(), 0);
                        poseStack.popPose();
                    }
                }
            }

            renderCountText(font, poseStack, defaultBufferSource, actualPackedLightFront,
                    result.getCount(), config.getOfferCountText(), dir);
        }

        // --- Payment items on the front side ---
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

    private static int calculateDynamicOfferItemDisplayCount(SingleOfferShopBlockEntity blockEntity, ItemStack result) {
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
        return Math.max(1, (int) Math.ceil(fillRatio * MAX_DYNAMIC_OFFER_ITEM_DISPLAY_COUNT));
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