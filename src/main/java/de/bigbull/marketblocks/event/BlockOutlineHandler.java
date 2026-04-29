package de.bigbull.marketblocks.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.init.RegistriesInit;
import de.bigbull.marketblocks.shop.singleoffer.block.TradeStandBlock;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
 * Base shape is always shown. Showcase shape is shown only if the top block exists.
 */
@EventBusSubscriber(modid = MarketBlocks.MODID, value = Dist.CLIENT)
public class BlockOutlineHandler {
    private static final VoxelShape BASE_OUTLINE = Block.box(0, 0, 0, 16, 11, 16);
    private static final VoxelShape SHOWCASE_OUTLINE = Block.box(1, 11, 1, 15, 25, 15);
    private static final VoxelShape FULL_SHOWCASE_OUTLINE = Shapes.or(BASE_OUTLINE, SHOWCASE_OUTLINE);

    private static final VoxelShape CRATE_BASE_OUTLINE = Block.box(0, 0, 0, 16, 8, 16);
    private static final VoxelShape CRATE_LID_OUTLINE = Block.box(0.5, 15, -0.5, 15.5, 17, 15.5); // Adjusted generic box to match 2-pixel lid thickness

    @SubscribeEvent
    public static void onBlockHighlight(RenderHighlightEvent.Block event) {
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

    private static void renderTradeStandOutline(RenderHighlightEvent.Block event, Level level, BlockPos pos, BlockState state) {
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
        VertexConsumer consumer = event.getMultiBufferSource().getBuffer(RenderType.lines());

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

    private static void renderMarketCrateOutline(RenderHighlightEvent.Block event, BlockPos pos, BlockState state) {
        event.setCanceled(true);

        Camera camera = event.getCamera();
        Vec3 camPos = camera.getPosition();
        PoseStack poseStack = event.getPoseStack();
        VertexConsumer consumer = event.getMultiBufferSource().getBuffer(RenderType.lines());

        Direction facing = state.hasProperty(BlockStateProperties.HORIZONTAL_FACING) 
                ? state.getValue(BlockStateProperties.HORIZONTAL_FACING) 
                : Direction.NORTH;

        poseStack.pushPose();
        poseStack.translate(
                pos.getX() - camPos.x,
                pos.getY() - camPos.y,
                pos.getZ() - camPos.z
        );

        // Center on block to apply Y-axis rotation based on facing
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        float yRot = 0;
        switch (facing) {
            case EAST:  yRot = -90; break;
            case SOUTH: yRot = 180; break;
            case WEST:  yRot = 90; break;
            default:    yRot = 0; break; // NORTH
        }

        if (yRot != 0) {
            poseStack.mulPose(new Quaternionf(new AxisAngle4f((float) Math.toRadians(yRot), 0.0f, 1.0f, 0.0f)));
        }

        poseStack.translate(-0.5, -0.5, -0.5);

        // Render base outline
        LevelRenderer.renderVoxelShape(
                poseStack, consumer, CRATE_BASE_OUTLINE,
                0.0, 0.0, 0.0, 0.0f, 0.0f, 0.0f, 0.4f, true
        );

        // Render rotated lid outline
        poseStack.pushPose();

        // Translate to lid rotation origin [8, 15, 15.5] as per JSON
        poseStack.translate(8.0 / 16.0, 15.0 / 16.0, 15.5 / 16.0);
        // Apply -22.5 degrees rotation on X axis
        poseStack.mulPose(new Quaternionf(new AxisAngle4f((float) Math.toRadians(-22.5), 1.0f, 0.0f, 0.0f)));
        // Translate back from origin so the lid box naturally fits
        poseStack.translate(-8.0 / 16.0, -15.0 / 16.0, -15.5 / 16.0);

        LevelRenderer.renderVoxelShape(
                poseStack, consumer, CRATE_LID_OUTLINE,
                0.0, 0.0, 0.0, 0.0f, 0.0f, 0.0f, 0.4f, true
        );

        poseStack.popPose(); // popup lid pose
        poseStack.popPose(); // popup block rotation pose
        poseStack.popPose(); // popup block position pose
    }
}