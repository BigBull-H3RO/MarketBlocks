package de.bigbull.marketblocks.client.event;

import de.bigbull.marketblocks.Constants;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.bigbull.marketblocks.core.init.RegistriesInit;
import de.bigbull.marketblocks.feature.singleoffer.block.TradeStandBlock;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
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
    public static void onBlockHighlight(RenderHighlightEvent.Block event) {
        if (event.isForTranslucentBlocks()) {
            return;
        }

        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        BlockHitResult hit = event.getTarget();
        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);

        if (state.is(RegistriesInit.MARKETCRATE_BLOCK.get())) {
            renderMarketCrateOutline(event, pos, state);
            return;
        }

        if (state.is(RegistriesInit.TRADE_STAND_BLOCK.get()) || state.is(RegistriesInit.TRADE_STAND_BLOCK_TOP.get())) {
            renderTradeStandOutline(event, level, pos, state);
        }
    }

    private static void renderTradeStandOutline(RenderHighlightEvent.Block event, Level level, BlockPos pos,
            BlockState state) {
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

        event.setCanceled(true);

        Camera camera = event.getCamera();
        Vec3 camPos = camera.getPosition();
        PoseStack poseStack = event.getPoseStack();

        BlockState baseState = level.getBlockState(outlineOrigin);
        boolean hasShowcase = TradeStandBlock.hasShowcase(baseState)
                || level.getBlockState(outlineOrigin.above()).is(RegistriesInit.TRADE_STAND_BLOCK_TOP.get());
        VoxelShape outline = hasShowcase ? FULL_SHOWCASE_OUTLINE : BASE_OUTLINE;

        poseStack.pushPose();
        poseStack.translate(
                outlineOrigin.getX() - camPos.x,
                outlineOrigin.getY() - camPos.y,
                outlineOrigin.getZ() - camPos.z);

        boolean highContrast = Minecraft.getInstance().options.highContrastBlockOutline().get();
        if (highContrast) {
            VertexConsumer secondaryConsumer = event.getMultiBufferSource().getBuffer(RenderType.secondaryBlockOutline());
            ShapeRenderer.renderShape(
                    poseStack,
                    secondaryConsumer,
                    outline,
                    0.0,
                    0.0,
                    0.0,
                    -16777216);
        }

        VertexConsumer consumer = event.getMultiBufferSource().getBuffer(RenderType.lines());
        int color = highContrast ? -11010079 : ARGB.color(102, -16777216);

        ShapeRenderer.renderShape(
                poseStack,
                consumer,
                outline,
                0.0,
                0.0,
                0.0,
                color);

        poseStack.popPose();

        if (event.getMultiBufferSource() instanceof MultiBufferSource.BufferSource bufferSource) {
            bufferSource.endLastBatch();
        }
    }

    private static void renderMarketCrateOutline(RenderHighlightEvent.Block event, BlockPos pos, BlockState state) {
        event.setCanceled(true);

        Camera camera = event.getCamera();
        Vec3 camPos = camera.getPosition();
        PoseStack poseStack = event.getPoseStack();

        Direction facing = state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? state.getValue(BlockStateProperties.HORIZONTAL_FACING)
                : Direction.NORTH;

        float yRot = switch (facing) {
            case EAST -> -90.0f;
            case SOUTH -> 180.0f;
            case WEST -> 90.0f;
            default -> 0.0f;
        };

        boolean highContrast = Minecraft.getInstance().options.highContrastBlockOutline().get();
        if (highContrast) {
            VertexConsumer secondaryConsumer = event.getMultiBufferSource().getBuffer(RenderType.secondaryBlockOutline());
            renderMarketCrateShape(poseStack, secondaryConsumer, pos, camPos, yRot, -16777216);
        }

        VertexConsumer consumer = event.getMultiBufferSource().getBuffer(RenderType.lines());
        int color = highContrast ? -11010079 : ARGB.color(102, -16777216);
        renderMarketCrateShape(poseStack, consumer, pos, camPos, yRot, color);

        if (event.getMultiBufferSource() instanceof MultiBufferSource.BufferSource bufferSource) {
            bufferSource.endLastBatch();
        }
    }

    private static void renderMarketCrateShape(PoseStack poseStack, VertexConsumer consumer, BlockPos pos, Vec3 camPos,
            float yRot, int color) {
        poseStack.pushPose();
        poseStack.translate(
                pos.getX() - camPos.x,
                pos.getY() - camPos.y,
                pos.getZ() - camPos.z);

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        if (yRot != 0.0f) {
            poseStack.mulPose(new Quaternionf(new AxisAngle4f((float) Math.toRadians(yRot), 0.0f, 1.0f, 0.0f)));
        }

        poseStack.translate(-0.5, -0.5, -0.5);

        ShapeRenderer.renderShape(
                poseStack, consumer, CRATE_BASE_OUTLINE,
                0.0, 0.0, 0.0, color);

        renderSlantedBasket(poseStack, consumer, color);

        poseStack.pushPose();

        poseStack.translate(8.0 / 16.0, 15.0 / 16.0, 15.5 / 16.0);
        poseStack.mulPose(new Quaternionf(new AxisAngle4f((float) Math.toRadians(-22.5), 1.0f, 0.0f, 0.0f)));
        poseStack.translate(-8.0 / 16.0, -15.0 / 16.0, -15.5 / 16.0);

        ShapeRenderer.renderShape(
                poseStack, consumer, CRATE_LID_OUTLINE,
                0.0, 0.0, 0.0, color);
        ShapeRenderer.renderShape(
                poseStack, consumer, CRATE_LID_INNER_OUTLINE,
                0.0, 0.0, 0.0, color);

        poseStack.popPose();
        poseStack.popPose();
        poseStack.popPose();
    }

    private static void renderSlantedBasket(PoseStack poseStack, VertexConsumer consumer, int color) {
        float minX = 1 / 16f, maxX = 15 / 16f;
        float minZ = 1 / 16f, maxZ = 15 / 16f;
        float yBottom = 8 / 16f;
        float yFrontTop = 10 / 16f;
        float yBackTop = 15 / 16f;

        drawLine(poseStack, consumer, minX, yBottom, minZ, maxX, yBottom, minZ, color);
        drawLine(poseStack, consumer, maxX, yBottom, minZ, maxX, yBottom, maxZ, color);
        drawLine(poseStack, consumer, maxX, yBottom, maxZ, minX, yBottom, maxZ, color);
        drawLine(poseStack, consumer, minX, yBottom, maxZ, minX, yBottom, minZ, color);

        drawLine(poseStack, consumer, minX, yBottom, minZ, minX, yFrontTop, minZ, color);
        drawLine(poseStack, consumer, maxX, yBottom, minZ, maxX, yFrontTop, minZ, color);
        drawLine(poseStack, consumer, maxX, yBottom, maxZ, maxX, yBackTop, maxZ, color);
        drawLine(poseStack, consumer, minX, yBottom, maxZ, minX, yBackTop, maxZ, color);

        drawLine(poseStack, consumer, minX, yFrontTop, minZ, maxX, yFrontTop, minZ, color);
        drawLine(poseStack, consumer, maxX, yBackTop, maxZ, minX, yBackTop, maxZ, color);
        drawLine(poseStack, consumer, minX, yFrontTop, minZ, minX, yBackTop, maxZ, color);
        drawLine(poseStack, consumer, maxX, yFrontTop, minZ, maxX, yBackTop, maxZ, color);
    }

    private static void drawLine(PoseStack poseStack, VertexConsumer consumer, float x1, float y1, float z1, float x2,
            float y2, float z2, int color) {
        PoseStack.Pose pose = poseStack.last();
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

        consumer.addVertex(matrix4f, x1, y1, z1).setColor(color).setNormal(pose, dx, dy, dz);
        consumer.addVertex(matrix4f, x2, y2, z2).setColor(color).setNormal(pose, dx, dy, dz);
    }
}
