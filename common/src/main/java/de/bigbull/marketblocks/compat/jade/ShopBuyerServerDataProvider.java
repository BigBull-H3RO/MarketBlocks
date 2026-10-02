package de.bigbull.marketblocks.compat.jade;

import de.bigbull.marketblocks.core.config.Config;
import de.bigbull.marketblocks.feature.trader.entity.ShopBuyerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IServerDataProvider;

public enum ShopBuyerServerDataProvider implements IServerDataProvider<EntityAccessor> {
    INSTANCE;

    @Override
    public void appendServerData(CompoundTag data, EntityAccessor accessor) {
        if (!Config.ENABLE_JADE_COMPAT.get() || !Config.ENABLE_JADE_TRADER_BUDGET.get()) {
            return;
        }
        if (accessor.getEntity() instanceof ShopBuyerEntity buyer) {
            data.putInt("Budget", buyer.getBudget());
        }
    }

    @Override
    public Identifier getUid() {
        return ShopBuyerComponentProvider.UID;
    }
}
