package de.bigbull.marketblocks.event;

import de.bigbull.marketblocks.core.init.RegistriesInit;
import de.bigbull.marketblocks.feature.trader.entity.ShopBuyerEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

public class ModEntityEvents {
    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(RegistriesInit.SHOP_BUYER.get(), ShopBuyerEntity.createAttributes().build());
    }
}
