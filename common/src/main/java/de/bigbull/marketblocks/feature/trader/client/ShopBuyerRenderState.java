package de.bigbull.marketblocks.feature.trader.client;

import de.bigbull.marketblocks.feature.trader.entity.ShopBuyerEntity;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;

public class ShopBuyerRenderState extends VillagerRenderState {
    public ShopBuyerEntity.TraderRank rank = ShopBuyerEntity.TraderRank.CITIZEN;
}
