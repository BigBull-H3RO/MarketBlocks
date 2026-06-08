package de.bigbull.marketblocks.compat.jade;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.feature.singleoffer.block.BaseShopBlock;

@WailaPlugin(MarketBlocks.MODID)
public class JadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(ShopBlockComponentProvider.INSTANCE, de.bigbull.marketblocks.feature.singleoffer.entity.SingleOfferShopBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(ShopBlockComponentProvider.INSTANCE, BaseShopBlock.class);
    }
}
