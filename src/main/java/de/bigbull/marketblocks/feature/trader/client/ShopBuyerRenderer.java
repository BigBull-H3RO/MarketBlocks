package de.bigbull.marketblocks.feature.trader.client;

import com.mojang.blaze3d.vertex.PoseStack;
import de.bigbull.marketblocks.feature.trader.entity.ShopBuyerEntity;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public class ShopBuyerRenderer extends MobRenderer<ShopBuyerEntity, VillagerModel<ShopBuyerEntity>> {
    private static final ResourceLocation CITIZEN_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/wandering_trader.png");
    private static final ResourceLocation BASE_VILLAGER_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/villager/villager.png");
    private static final ResourceLocation WEALTHY_OVERLAY = ResourceLocation.withDefaultNamespace("textures/entity/villager/profession/cartographer.png");
    private static final ResourceLocation NOBLE_OVERLAY = ResourceLocation.withDefaultNamespace("textures/entity/villager/profession/librarian.png");

    public ShopBuyerRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.WANDERING_TRADER)), 0.5F);
        this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getItemInHandRenderer()));
        this.addLayer(new CrossedArmsItemLayer<>(this, context.getItemInHandRenderer()));
        this.addLayer(new RenderLayer<ShopBuyerEntity, VillagerModel<ShopBuyerEntity>>(this) {
            @Override
            public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, ShopBuyerEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
                if (entity.isInvisible()) return;
                ResourceLocation overlay = null;
                switch (entity.getTraderRank()) {
                    case WEALTHY:
                        overlay = WEALTHY_OVERLAY;
                        break;
                    case NOBLE:
                        overlay = NOBLE_OVERLAY;
                        break;
                    case CITIZEN:
                    default:
                        break;
                }
                if (overlay != null) {
                    renderColoredCutoutModel(this.getParentModel(), overlay, poseStack, bufferSource, packedLight, entity, -1);
                }
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(ShopBuyerEntity entity) {
        switch (entity.getTraderRank()) {
            case WEALTHY:
            case NOBLE:
                return BASE_VILLAGER_TEXTURE;
            case CITIZEN:
            default:
                return CITIZEN_TEXTURE;
        }
    }

    @Override
    protected void scale(ShopBuyerEntity livingEntity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
    }
}
