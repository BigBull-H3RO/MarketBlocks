package de.bigbull.marketblocks.client.event;

import de.bigbull.marketblocks.Constants;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.bigbull.marketblocks.core.init.RegistriesInit;
import de.bigbull.marketblocks.feature.singleoffer.block.TradeStandBlock;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import org.joml.Quaternionf;
import org.joml.AxisAngle4f;

/**
 * Custom hover outline for the tall shop and market crate.
 * Base shape is always shown. Showcase shape is shown only if the top block
 * exists.
 */
@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class BlockOutlineHandler {
    private static final VoxelShape BASE_OUTLINE = Block.box(0, 0, 0, 16, 11, 16);
    private static final VoxelShape SHOWCASE_OUTLINE = Block.box(1, 11, 1, 15, 25, 15);
    private static final VoxelShape FULL_SHOWCASE_OUTLINE = Shapes.or(BASE_OUTLINE, SHOWCASE_OUTLINE);

    private static final VoxelShape CRATE_BASE_OUTLINE = Block.box(0, 0, 0, 16, 8, 16);
    private static final VoxelShape CRATE_LID_OUTLINE = Block.box(0.5, 15, -0.5, 15.5, 17, 15.5);
    private static final VoxelShape CRATE_LID_INNER_OUTLINE = Block.box(2.5, 15, 1.5, 13.5, 17, 13.5);

    @SubscribeEvent
    public static void onExtractBlockOutline(ExtractBlockOutlineRenderStateEvent event) {
        if (event.isInTranslucentPass()) {
            return;
        }

        Level level = event.getLevel();
        if (level == null) {
            return;
        }

        BlockPos pos = event.getBlockPos();
        BlockState state = event.getBlockState();

        if (state.is(RegistriesInit.MARKETCRATE_BLOCK.get())) {
            event.addCustomRenderer((renderState, submitNodeCollector, poseStack, levelRenderState) -> {
                renderMarketCrateOutline(submitNodeCollector, poseStack, renderState, pos, state, levelRenderState);
                return true;
            });
            return;
        }

        if (state.is(RegistriesInit.TRADE_STAND_BLOCK.get()) || state.is(RegistriesInit.TRADE_STAND_BLOCK_TOP.get())) {
            event.addCustomRenderer((renderState, submitNodeCollector, poseStack, levelRenderState) -> {
                renderTradeStandOutline(submitNodeCollector, poseStack, renderState, level, pos, state, levelRenderState);
                return true;
            });
        }
    }

    private static void renderTradeStandOutline(SubmitNodeCollector submitNodeCollector, PoseStack poseStack,
            BlockOutlineRenderState renderState, Level level, BlockPos pos, BlockState state, LevelRenderState levelRenderState) {
        BlockPos outlineOrigin;
        if (state.is(RegistriesInit.TRADE_STAND_BLOCK.get())) {
            outlineOrigin = pos;
        } else if (state.is(RegistriesInit.TRADE_STAND_BLOCK_TOP.get())) {
            outlineOrigin = pos.below();
            if (!level.getBlockState(outlineOrigin).is(RegistriesInit.TRADE_STAND_BLOCK.get())) {
                return;
            }
        } else {
            return;
        }

        Camera camera = Minecraft.getInstance().gameRenderer.mainCamera();
        Vec3 camPos = camera.position();

        BlockState baseState = level.getBlockState(outlineOrigin);
        boolean hasShowcase = TradeStandBlock.hasShowcase(baseState)
                || level.getBlockState(outlineOrigin.above()).is(RegistriesInit.TRADE_STAND_BLOCK_TOP.get());
        VoxelShape outline = hasShowcase ? FULL_SHOWCASE_OUTLINE : BASE_OUTLINE;

        poseStack.pushPose();
        poseStack.translate(
                outlineOrigin.getX() - camPos.x,
                outlineOrigin.getY() - camPos.y,
                outlineOrigin.getZ() - camPos.z);

        boolean highContrast = renderState.highContrast();
        if (highContrast) {
            submitNodeCollector.submitShapeOutline(
                    poseStack,
                    outline,
                    RenderTypes.secondaryBlockOutline(),
                    -16777216,
                    7.0F,
                    false);
        }

        int color = highContrast ? -11010079 : ARGB.black(102);
        RenderType renderType = getOutlineRenderType(highContrast);
        float lineWidth = Minecraft.getInstance().getWindow().getAppropriateLineWidth();

        submitNodeCollector.submitShapeOutline(
                poseStack,
                outline,
                renderType,
                color,
                lineWidth,
                false);

        poseStack.popPose();
    }

    private static RenderType getOutlineRenderType(boolean highContrast) {
        if (highContrast) {
            return RenderTypes.linesDepthBias();
        }
        return Minecraft.getInstance().gameRenderer.useImprovedTransparency()
                ? RenderTypes.linesTranslucentNoDepthWrite()
                : RenderTypes.linesTranslucent();
    }

    private static void renderMarketCrateOutline(SubmitNodeCollector submitNodeCollector, PoseStack poseStack,
            BlockOutlineRenderState renderState, BlockPos pos, BlockState state, LevelRenderState levelRenderState) {
        Camera camera = Minecraft.getInstance().gameRenderer.mainCamera();
        Vec3 camPos = camera.position();

        Direction facing = state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? state.getValue(BlockStateProperties.HORIZONTAL_FACING)
                : Direction.NORTH;

        float yRot = switch (facing) {
            case EAST -> -90.0f;
            case SOUTH -> 180.0f;
            case WEST -> 90.0f;
            default -> 0.0f;
        };

        boolean highContrast = renderState.highContrast();
        if (highContrast) {
            renderMarketCrateShape(submitNodeCollector, poseStack, pos, camPos, yRot, -16777216, 7.0F, RenderTypes.secondaryBlockOutline());
        }

        int color = highContrast ? -11010079 : ARGB.black(102);
        RenderType renderType = getOutlineRenderType(highContrast);
        float lineWidth = Minecraft.getInstance().getWindow().getAppropriateLineWidth();
        renderMarketCrateShape(submitNodeCollector, poseStack, pos, camPos, yRot, color, lineWidth, renderType);
    }

    private static void renderMarketCrateShape(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, BlockPos pos, Vec3 camPos,
            float yRot, int color, float lineWidth, RenderType renderType) {
        poseStack.pushPose();
        poseStack.translate(
                pos.getX() - camPos.x,
                pos.getY() - camPos.y,
                pos.getZ() - camPos.z);

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        if (yRot != 0.0f) {
            poseStack.rotate(new Quaternionf(new AxisAngle4f((float) Math.toRadians(yRot), 0.0f, 1.0f, 0.0f)));
        }

        poseStack.translate(-0.5, -0.5, -0.5);

        submitNodeCollector.submitShapeOutline(
                poseStack,
                CRATE_BASE_OUTLINE,
                renderType,
                color,
                lineWidth,
                false);

        submitNodeCollector.submitCustomGeometry(poseStack, renderType,
                (pose, consumer) -> renderSlantedBasket(pose, consumer, color, lineWidth));

        poseStack.pushPose();

        poseStack.translate(8.0 / 16.0, 15.0 / 16.0, 15.5 / 16.0);
        poseStack.rotate(new Quaternionf(new AxisAngle4f((float) Math.toRadians(-22.5), 1.0f, 0.0f, 0.0f)));
        poseStack.translate(-8.0 / 16.0, -15.0 / 16.0, -15.5 / 16.0);

        submitNodeCollector.submitShapeOutline(
                poseStack,
                CRATE_LID_OUTLINE,
                renderType,
                color,
                lineWidth,
                false);
        submitNodeCollector.submitShapeOutline(
                poseStack,
                CRATE_LID_INNER_OUTLINE,
                renderType,
                color,
                lineWidth,
                false);

        poseStack.popPose();
        poseStack.popPose();
        poseStack.popPose();
    }

    private static void renderSlantedBasket(PoseStack.Pose pose, VertexConsumer consumer, int color, float lineWidth) {
        float minX = 1 / 16f, maxX = 15 / 16f;
        float minZ = 1 / 16f, maxZ = 15 / 16f;
        float yBottom = 8 / 16f;
        float yFrontTop = 10 / 16f;
        float yBackTop = 15 / 16f;

        drawLine(pose, consumer, minX, yBottom, minZ, maxX, yBottom, minZ, color, lineWidth);
        drawLine(pose, consumer, maxX, yBottom, minZ, maxX, yBottom, maxZ, color, lineWidth);
        drawLine(pose, consumer, maxX, yBottom, maxZ, minX, yBottom, maxZ, color, lineWidth);
        drawLine(pose, consumer, minX, yBottom, maxZ, minX, yBottom, minZ, color, lineWidth);

        drawLine(pose, consumer, minX, yBottom, minZ, minX, yFrontTop, minZ, color, lineWidth);
        drawLine(pose, consumer, maxX, yBottom, minZ, maxX, yFrontTop, minZ, color, lineWidth);
        drawLine(pose, consumer, maxX, yBottom, maxZ, maxX, yBackTop, maxZ, color, lineWidth);
        drawLine(pose, consumer, minX, yBottom, maxZ, minX, yBackTop, maxZ, color, lineWidth);

        drawLine(pose, consumer, minX, yFrontTop, minZ, maxX, yFrontTop, minZ, color, lineWidth);
        drawLine(pose, consumer, maxX, yBackTop, maxZ, minX, yBackTop, maxZ, color, lineWidth);
        drawLine(pose, consumer, minX, yFrontTop, minZ, minX, yBackTop, maxZ, color, lineWidth);
        drawLine(pose, consumer, maxX, yFrontTop, minZ, maxX, yBackTop, maxZ, color, lineWidth);
    }

    private static void drawLine(PoseStack.Pose pose, VertexConsumer consumer, float x1, float y1, float z1, float x2,
            float y2, float z2, int color, float lineWidth) {
        org.joml.Matrix4f matrix4f = pose.pose();

        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len > 0) {
            dx /= len;
            dy /= len;
            dz /= len;
        }

        consumer.addVertex(matrix4f, x1, y1, z1).setColor(color).setNormal(pose, dx, dy, dz).setLineWidth(lineWidth);
        consumer.addVertex(matrix4f, x2, y2, z2).setColor(color).setNormal(pose, dx, dy, dz).setLineWidth(lineWidth);
    }
}
