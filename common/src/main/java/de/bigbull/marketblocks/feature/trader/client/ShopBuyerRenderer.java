package de.bigbull.marketblocks.feature.trader.client;

import com.mojang.blaze3d.vertex.PoseStack;
import de.bigbull.marketblocks.feature.trader.entity.ShopBuyerEntity;
import net.minecraft.client.model.npc.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HoldingEntityRenderState;
import net.minecraft.resources.Identifier;

import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerType;

public class ShopBuyerRenderer extends MobRenderer<ShopBuyerEntity, VillagerRenderState, VillagerModel> {
    private static final Identifier CITIZEN_TEXTURE = Identifier.withDefaultNamespace("textures/entity/wandering_trader/wandering_trader.png");
    private static final Identifier WEALTHY_OVERLAY = Identifier.withDefaultNamespace("textures/entity/villager/profession/cartographer.png");
    private static final Identifier NOBLE_OVERLAY = Identifier.withDefaultNamespace("textures/entity/villager/profession/librarian.png");

    public ShopBuyerRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel(context.bakeLayer(ModelLayers.WANDERING_TRADER)), 0.5F);
        this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getPlayerSkinRenderCache()));
        this.addLayer(new CrossedArmsItemLayer<>(this));
        this.addLayer(new RenderLayer<VillagerRenderState, VillagerModel>(this) {
            @Override
            public void submit(PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, VillagerRenderState renderState, float yRot, float xRot) {
                if (renderState.isInvisible) return;
                int level = renderState.villagerData.level();
                Identifier overlay = switch (level) {
                    case 2 -> WEALTHY_OVERLAY;
                    case 3 -> NOBLE_OVERLAY;
                    default -> null;
                };
                if (overlay != null) {
                    renderColoredCutoutModel(this.getParentModel(), overlay, poseStack, nodeCollector, packedLight, renderState, -1, 0);
                }
            }
        });
    }

    @Override
    public Identifier getTextureLocation(VillagerRenderState renderState) {
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
        renderState.villagerData = new VillagerData(
                BuiltInRegistries.VILLAGER_TYPE.getOrThrow(VillagerType.PLAINS),
                BuiltInRegistries.VILLAGER_PROFESSION.getOrThrow(VillagerProfession.NONE),
                rankLevel);
    }

    @Override
    protected void scale(VillagerRenderState renderState, PoseStack poseStack) {
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
    }
}
