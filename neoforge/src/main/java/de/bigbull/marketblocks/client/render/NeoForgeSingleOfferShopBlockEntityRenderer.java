package de.bigbull.marketblocks.client.render;

import de.bigbull.marketblocks.feature.singleoffer.client.render.SingleOfferShopBlockEntityRenderer;
import de.bigbull.marketblocks.feature.singleoffer.entity.SingleOfferShopBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

/**
 * NeoForge-specific BlockEntityRenderer extension that provides an expanded
 * render bounding box via IBlockEntityRendererExtension.
 *
 * This prevents NeoForge's frustum culling (ClientHooks.isBlockEntityRendererVisible)
 * from prematurely culling floating shop items or the visual NPC falling from Y=80.
 */
public class NeoForgeSingleOfferShopBlockEntityRenderer extends SingleOfferShopBlockEntityRenderer {

    public NeoForgeSingleOfferShopBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public AABB getRenderBoundingBox(SingleOfferShopBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        // Extends 16 blocks horizontally and up to 128 blocks into the sky
        // to cover the entire drop trajectory of the visual NPC and floating items.
        return new AABB(
                pos.getX() - 16, pos.getY() - 4,
                pos.getZ() - 16,
                pos.getX() + 17, pos.getY() + 128,
                pos.getZ() + 17
        );
    }
}
