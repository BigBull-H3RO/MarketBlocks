package de.bigbull.marketblocks.compat.jade;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.feature.singleoffer.block.BaseShopBlock;
import de.bigbull.marketblocks.feature.singleoffer.entity.SingleOfferShopBlockEntity;
import de.bigbull.marketblocks.feature.trader.entity.ShopBuyerEntity;

@WailaPlugin(MarketBlocks.MODID)
public class JadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(ShopBlockComponentProvider.INSTANCE, SingleOfferShopBlockEntity.class);
        registration.registerEntityDataProvider(ShopBuyerComponentProvider.INSTANCE, ShopBuyerEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(ShopBlockComponentProvider.INSTANCE, BaseShopBlock.class);
        registration.registerEntityComponent(ShopBuyerComponentProvider.INSTANCE, ShopBuyerEntity.class);
    }
}
