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
import net.minecraft.client.renderer.entity.state.HoldingEntityRenderState;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;

public class ShopBuyerRenderer extends MobRenderer<ShopBuyerEntity, VillagerRenderState, VillagerModel> {
    private static final ResourceLocation CITIZEN_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/wandering_trader.png");
    private static final ResourceLocation WEALTHY_OVERLAY = ResourceLocation.withDefaultNamespace("textures/entity/villager/profession/cartographer.png");
    private static final ResourceLocation NOBLE_OVERLAY = ResourceLocation.withDefaultNamespace("textures/entity/villager/profession/librarian.png");

    public ShopBuyerRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel(context.bakeLayer(ModelLayers.WANDERING_TRADER)), 0.5F);
        this.addLayer(new CustomHeadLayer<>(this, context.getModelSet()));
        this.addLayer(new CrossedArmsItemLayer<>(this));
        this.addLayer(new RenderLayer<VillagerRenderState, VillagerModel>(this) {
            @Override
            public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, VillagerRenderState renderState, float yRot, float xRot) {
                if (renderState.isInvisible) return;
                int level = renderState.villagerData.getLevel();
                ResourceLocation overlay = switch (level) {
                    case 2 -> WEALTHY_OVERLAY;
                    case 3 -> NOBLE_OVERLAY;
                    default -> null;
                };
                if (overlay != null) {
                    renderColoredCutoutModel(this.getParentModel(), overlay, poseStack, bufferSource, packedLight, renderState, -1);
                }
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(VillagerRenderState renderState) {
        // Base texture is always the full Wandering Trader robe for all ranks
        return CITIZEN_TEXTURE;
    }

    @Override
    public VillagerRenderState createRenderState() {
        return new VillagerRenderState();
    }

    @Override
    public void extractRenderState(ShopBuyerEntity entity, VillagerRenderState renderState, float partialTick) {
        super.extractRenderState(entity, renderState, partialTick);
        HoldingEntityRenderState.extractHoldingEntityRenderState(entity, renderState, this.itemModelResolver);
        int rankLevel = switch (entity.getTraderRank()) {
            case CITIZEN -> 1;
            case WEALTHY -> 2;
            case NOBLE -> 3;
        };
        renderState.villagerData = new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, rankLevel);
    }

    @Override
    protected void scale(VillagerRenderState renderState, PoseStack poseStack) {
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
    }
}
