package de.bigbull.marketblocks.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.util.custom.block.SmallShopBlockNeu;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;

/**
 * Custom hover outline for the tall shop.
 * Base shape is always shown. Showcase shape is shown only if the top block exists.
 */
@EventBusSubscriber(modid = MarketBlocks.MODID, value = Dist.CLIENT)
public class BlockOutlineHandler {
    private static final VoxelShape BASE_OUTLINE = Block.box(0, 0, 0, 16, 11, 16);
    private static final VoxelShape SHOWCASE_OUTLINE = Block.box(1, 11, 1, 15, 25, 15);
    private static final VoxelShape FULL_SHOWCASE_OUTLINE = Shapes.or(BASE_OUTLINE, SHOWCASE_OUTLINE);

    @SubscribeEvent
    public static void onBlockHighlight(RenderHighlightEvent.Block event) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        BlockHitResult hit = event.getTarget();
        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);

        BlockPos outlineOrigin;
        if (state.is(RegistriesInit.SMALL_SHOP_BLOCK_NEU.get())) {
            outlineOrigin = pos;
        } else if (state.is(RegistriesInit.SMALL_SHOP_BLOCK_NEU_TOP.get())) {
            outlineOrigin = pos.below();
            if (!level.getBlockState(outlineOrigin).is(RegistriesInit.SMALL_SHOP_BLOCK_NEU.get())) {
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
        boolean hasShowcase = SmallShopBlockNeu.hasShowcase(baseState)
                || level.getBlockState(outlineOrigin.above()).is(RegistriesInit.SMALL_SHOP_BLOCK_NEU_TOP.get());
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
}