package de.bigbull.marketblocks.core.event;

import de.bigbull.marketblocks.core.init.RegistriesInit;
import de.bigbull.marketblocks.feature.trader.entity.ShopBuyerEntity;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

public class ModEntityEvents {

    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(RegistriesInit.SHOP_BUYER.get(), ShopBuyerEntity.createAttributes().build());
    }
}
