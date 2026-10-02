package de.bigbull.marketblocks.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.bigbull.marketblocks.core.init.RegistriesInit;
import de.bigbull.marketblocks.feature.singleoffer.block.TradeStandBlock;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
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
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

/**
 * Custom hover outline for the tall shop (Trade Stand) and market crate on
 * Fabric.
 * Matches 1:1 with NeoForge's BlockOutlineHandler.
 * Base shape is always shown. Showcase shape is shown only if the top block
 * exists.
 */
public class FabricBlockOutlineHandler {
    private static final VoxelShape BASE_OUTLINE = Block.box(0, 0, 0, 16, 11, 16);
    private static final VoxelShape SHOWCASE_OUTLINE = Block.box(1, 11, 1, 15, 25, 15);
    private static final VoxelShape FULL_SHOWCASE_OUTLINE = Shapes.or(BASE_OUTLINE, SHOWCASE_OUTLINE);

    private static final VoxelShape CRATE_BASE_OUTLINE = Block.box(0, 0, 0, 16, 8, 16);
    private static final VoxelShape CRATE_LID_OUTLINE = Block.box(0.5, 15, -0.5, 15.5, 17, 15.5);
    private static final VoxelShape CRATE_LID_INNER_OUTLINE = Block.box(2.5, 15, 1.5, 13.5, 17, 13.5);

    public static void init() {
        LevelRenderEvents.BEFORE_BLOCK_OUTLINE.register((context, outlineRenderState) -> {
            Level level = Minecraft.getInstance().level;
            if (level == null) {
                return true;
            }

            BlockPos pos = outlineRenderState.pos();
            BlockState state = level.getBlockState(pos);

            if (state.is(RegistriesInit.MARKETCRATE_BLOCK.get())) {
                renderMarketCrateOutline(context, outlineRenderState, pos, state);
                return false;
            }

            if (state.is(RegistriesInit.TRADE_STAND_BLOCK.get())
                    || state.is(RegistriesInit.TRADE_STAND_BLOCK_TOP.get())) {
                renderTradeStandOutline(context, outlineRenderState, level, pos, state);
                return false;
            }

            return true;
        });
    }

    private static void renderTradeStandOutline(LevelRenderContext context, BlockOutlineRenderState outlineRenderState,
            Level level, BlockPos pos, BlockState state) {
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
        PoseStack poseStack = context.poseStack();
        SubmitNodeCollector collector = context.submitNodeCollector();
        if (collector == null)
            return;

        BlockState baseState = level.getBlockState(outlineOrigin);
        boolean hasShowcase = TradeStandBlock.hasShowcase(baseState)
                || level.getBlockState(outlineOrigin.above()).is(RegistriesInit.TRADE_STAND_BLOCK_TOP.get());
        VoxelShape outline = hasShowcase ? FULL_SHOWCASE_OUTLINE : BASE_OUTLINE;

        poseStack.pushPose();
        poseStack.translate(
                outlineOrigin.getX() - camPos.x,
                outlineOrigin.getY() - camPos.y,
                outlineOrigin.getZ() - camPos.z);

        boolean highContrast = outlineRenderState.highContrast();
        if (highContrast) {
            collector.submitShapeOutline(
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

        collector.submitShapeOutline(
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

    private static void renderMarketCrateOutline(LevelRenderContext context, BlockOutlineRenderState outlineRenderState,
            BlockPos pos, BlockState state) {
        Camera camera = Minecraft.getInstance().gameRenderer.mainCamera();
        Vec3 camPos = camera.position();
        PoseStack poseStack = context.poseStack();
        SubmitNodeCollector collector = context.submitNodeCollector();
        if (collector == null)
            return;

        Direction facing = state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? state.getValue(BlockStateProperties.HORIZONTAL_FACING)
                : Direction.NORTH;

        float yRot = switch (facing) {
            case EAST -> -90.0f;
            case SOUTH -> 180.0f;
            case WEST -> 90.0f;
            default -> 0.0f;
        };

        boolean highContrast = outlineRenderState.highContrast();
        if (highContrast) {
            renderMarketCrateShape(collector, poseStack, pos, camPos, yRot, -16777216, 7.0F, RenderTypes.secondaryBlockOutline());
        }

        int color = highContrast ? -11010079 : ARGB.black(102);
        RenderType renderType = getOutlineRenderType(highContrast);
        float lineWidth = Minecraft.getInstance().getWindow().getAppropriateLineWidth();
        renderMarketCrateShape(collector, poseStack, pos, camPos, yRot, color, lineWidth, renderType);
    }

    private static void renderMarketCrateShape(SubmitNodeCollector collector, PoseStack poseStack, BlockPos pos, Vec3 camPos,
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

        collector.submitShapeOutline(
                poseStack,
                CRATE_BASE_OUTLINE,
                renderType,
                color,
                lineWidth,
                false);

        collector.submitCustomGeometry(poseStack, renderType,
                (pose, consumer) -> renderSlantedBasket(pose, consumer, color, lineWidth));

        poseStack.pushPose();

        poseStack.translate(8.0 / 16.0, 15.0 / 16.0, 15.5 / 16.0);
        poseStack.rotate(new Quaternionf(new AxisAngle4f((float) Math.toRadians(-22.5), 1.0f, 0.0f, 0.0f)));
        poseStack.translate(-8.0 / 16.0, -15.0 / 16.0, -15.5 / 16.0);

        collector.submitShapeOutline(
                poseStack,
                CRATE_LID_OUTLINE,
                renderType,
                color,
                lineWidth,
                false);
        collector.submitShapeOutline(
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
