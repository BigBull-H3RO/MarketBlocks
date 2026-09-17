package de.bigbull.marketblocks.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.bigbull.marketblocks.core.init.RegistriesInit;
import de.bigbull.marketblocks.feature.singleoffer.block.TradeStandBlock;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
 * Custom hover outline for the tall shop (Trade Stand) and market crate on Fabric.
 * Matches 1:1 with NeoForge's BlockOutlineHandler.
 * Base shape is always shown. Showcase shape is shown only if the top block exists.
 */
public class FabricBlockOutlineHandler {
    private static final VoxelShape BASE_OUTLINE = Block.box(0, 0, 0, 16, 11, 16);
    private static final VoxelShape SHOWCASE_OUTLINE = Block.box(1, 11, 1, 15, 25, 15);
    private static final VoxelShape FULL_SHOWCASE_OUTLINE = Shapes.or(BASE_OUTLINE, SHOWCASE_OUTLINE);

    private static final VoxelShape CRATE_BASE_OUTLINE = Block.box(0, 0, 0, 16, 8, 16);
    private static final VoxelShape CRATE_LID_OUTLINE = Block.box(0.5, 15, -0.5, 15.5, 17, 15.5);
    private static final VoxelShape CRATE_LID_INNER_OUTLINE = Block.box(2.5, 15, 1.5, 13.5, 17, 13.5);

    public static void init() {
        WorldRenderEvents.BLOCK_OUTLINE.register((worldRenderContext, outlineContext) -> {
            Level level = worldRenderContext.world();
            if (level == null) {
                return true;
            }

            BlockPos pos = outlineContext.blockPos();
            BlockState state = outlineContext.blockState();

            if (state.is(RegistriesInit.MARKETCRATE_BLOCK.get())) {
                renderMarketCrateOutline(worldRenderContext, pos, state);
                return false;
            }

            if (state.is(RegistriesInit.TRADE_STAND_BLOCK.get()) || state.is(RegistriesInit.TRADE_STAND_BLOCK_TOP.get())) {
                renderTradeStandOutline(worldRenderContext, level, pos, state);
                return false;
            }

            return true;
        });
    }

    private static void renderTradeStandOutline(WorldRenderContext context, Level level, BlockPos pos, BlockState state) {
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

        Camera camera = context.camera();
        Vec3 camPos = camera.getPosition();
        PoseStack poseStack = context.matrixStack();
        MultiBufferSource consumers = context.consumers();
        if (consumers == null) return;
        VertexConsumer consumer = consumers.getBuffer(RenderType.lines());

        BlockState baseState = level.getBlockState(outlineOrigin);
        boolean hasShowcase = TradeStandBlock.hasShowcase(baseState)
                || level.getBlockState(outlineOrigin.above()).is(RegistriesInit.TRADE_STAND_BLOCK_TOP.get());
        VoxelShape outline = hasShowcase ? FULL_SHOWCASE_OUTLINE : BASE_OUTLINE;

        poseStack.pushPose();
        poseStack.translate(
                outlineOrigin.getX() - camPos.x,
                outlineOrigin.getY() - camPos.y,
                outlineOrigin.getZ() - camPos.z
        );

        LevelRenderer.renderVoxelShape(
                poseStack,
                consumer,
                outline,
                0.0,
                0.0,
                0.0,
                0.0f,
                0.0f,
                0.0f,
                0.4f,
                true
        );

        poseStack.popPose();
    }

    private static void renderMarketCrateOutline(WorldRenderContext context, BlockPos pos, BlockState state) {
        Camera camera = context.camera();
        Vec3 camPos = camera.getPosition();
        PoseStack poseStack = context.matrixStack();
        MultiBufferSource consumers = context.consumers();
        if (consumers == null) return;
        VertexConsumer consumer = consumers.getBuffer(RenderType.lines());

        Direction facing = state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? state.getValue(BlockStateProperties.HORIZONTAL_FACING)
                : Direction.NORTH;

        poseStack.pushPose();
        poseStack.translate(
                pos.getX() - camPos.x,
                pos.getY() - camPos.y,
                pos.getZ() - camPos.z
        );

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        float yRot = 0;
        switch (facing) {
            case EAST:  yRot = -90; break;
            case SOUTH: yRot = 180; break;
            case WEST:  yRot = 90; break;
            default:    yRot = 0; break;
        }

        if (yRot != 0) {
            poseStack.mulPose(new Quaternionf(new AxisAngle4f((float) Math.toRadians(yRot), 0.0f, 1.0f, 0.0f)));
        }

        poseStack.translate(-0.5, -0.5, -0.5);

        LevelRenderer.renderVoxelShape(
                poseStack, consumer, CRATE_BASE_OUTLINE,
                0.0, 0.0, 0.0, 0.0f, 0.0f, 0.0f, 0.4f, true
        );

        renderSlantedBasket(poseStack, consumer);

        poseStack.pushPose();

        poseStack.translate(8.0 / 16.0, 15.0 / 16.0, 15.5 / 16.0);
        poseStack.mulPose(new Quaternionf(new AxisAngle4f((float) Math.toRadians(-22.5), 1.0f, 0.0f, 0.0f)));
        poseStack.translate(-8.0 / 16.0, -15.0 / 16.0, -15.5 / 16.0);

        LevelRenderer.renderVoxelShape(
                poseStack, consumer, CRATE_LID_OUTLINE,
                0.0, 0.0, 0.0, 0.0f, 0.0f, 0.0f, 0.4f, true
        );
        LevelRenderer.renderVoxelShape(
                poseStack, consumer, CRATE_LID_INNER_OUTLINE,
                0.0, 0.0, 0.0, 0.0f, 0.0f, 0.0f, 0.4f, true
        );

        poseStack.popPose();
        poseStack.popPose();
        poseStack.popPose();
    }

    private static void renderSlantedBasket(PoseStack poseStack, VertexConsumer consumer) {
        float minX = 1/16f, maxX = 15/16f;
        float minZ = 1/16f, maxZ = 15/16f;
        float yBottom = 8/16f;
        float yFrontTop = 10/16f;
        float yBackTop = 15/16f;

        drawLine(poseStack, consumer, minX, yBottom, minZ, maxX, yBottom, minZ);
        drawLine(poseStack, consumer, maxX, yBottom, minZ, maxX, yBottom, maxZ);
        drawLine(poseStack, consumer, maxX, yBottom, maxZ, minX, yBottom, maxZ);
        drawLine(poseStack, consumer, minX, yBottom, maxZ, minX, yBottom, minZ);

        drawLine(poseStack, consumer, minX, yBottom, minZ, minX, yFrontTop, minZ);
        drawLine(poseStack, consumer, maxX, yBottom, minZ, maxX, yFrontTop, minZ);
        drawLine(poseStack, consumer, maxX, yBottom, maxZ, maxX, yBackTop, maxZ);
        drawLine(poseStack, consumer, minX, yBottom, maxZ, minX, yBackTop, maxZ);

        drawLine(poseStack, consumer, minX, yFrontTop, minZ, maxX, yFrontTop, minZ);
        drawLine(poseStack, consumer, maxX, yBackTop, maxZ, minX, yBackTop, maxZ);
        drawLine(poseStack, consumer, minX, yFrontTop, minZ, minX, yBackTop, maxZ);
        drawLine(poseStack, consumer, maxX, yFrontTop, minZ, maxX, yBackTop, maxZ);
    }

    private static void drawLine(PoseStack poseStack, VertexConsumer consumer, float x1, float y1, float z1, float x2, float y2, float z2) {
        PoseStack.Pose pose = poseStack.last();
        org.joml.Matrix4f matrix4f = pose.pose();

        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len > 0) {
            dx /= len; dy /= len; dz /= len;
        }

        consumer.addVertex(matrix4f, x1, y1, z1).setColor(0.0f, 0.0f, 0.0f, 0.4f).setNormal(pose, dx, dy, dz);
        consumer.addVertex(matrix4f, x2, y2, z2).setColor(0.0f, 0.0f, 0.0f, 0.4f).setNormal(pose, dx, dy, dz);
    }
}
