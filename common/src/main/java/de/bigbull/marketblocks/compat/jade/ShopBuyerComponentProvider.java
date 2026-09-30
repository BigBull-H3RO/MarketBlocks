package de.bigbull.marketblocks.compat.jade;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.ChatFormatting;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import de.bigbull.marketblocks.Constants;
import de.bigbull.marketblocks.core.config.Config;
import de.bigbull.marketblocks.feature.trader.entity.ShopBuyerEntity;
import net.minecraft.nbt.CompoundTag;

public enum ShopBuyerComponentProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {
    INSTANCE;

    public static final Identifier UID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "shop_buyer_info");

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        if (!Config.ENABLE_JADE_COMPAT.get() || !Config.ENABLE_JADE_TRADER_BUDGET.get()) {
            return;
        }
        if (accessor.getEntity() instanceof ShopBuyerEntity) {
            accessor.getServerData().getInt("Budget").ifPresent(budget -> {
                tooltip.add(Component.translatable("marketblocks.jade.trader.budget", budget).withStyle(ChatFormatting.GOLD));
            });
        }
    }

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
        return UID;
    }
}
