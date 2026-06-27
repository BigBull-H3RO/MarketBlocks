package de.bigbull.marketblocks.feature.trader.client;

import com.mojang.blaze3d.vertex.PoseStack;
import de.bigbull.marketblocks.feature.trader.entity.ShopBuyerEntity;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.resources.ResourceLocation;

public class ShopBuyerRenderer extends MobRenderer<ShopBuyerEntity, VillagerModel<ShopBuyerEntity>> {
    // For now we use the vanilla Wandering Trader texture. 
    // It's easy to replace this with a custom ResourceLocation later.
    private static final ResourceLocation WANDERING_TRADER_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/wandering_trader.png");

    public ShopBuyerRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.WANDERING_TRADER)), 0.5F);
        this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getItemInHandRenderer()));
        this.addLayer(new CrossedArmsItemLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(ShopBuyerEntity entity) {
        return WANDERING_TRADER_LOCATION;
    }

    @Override
    protected void scale(ShopBuyerEntity livingEntity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
    }
}
