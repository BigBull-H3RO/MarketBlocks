package de.bigbull.marketblocks.compat.jade;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ChatFormatting;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.feature.trader.entity.ShopBuyerEntity;
import net.minecraft.nbt.CompoundTag;

public enum ShopBuyerComponentProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {
    INSTANCE;

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "shop_buyer_info");

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        if (accessor.getEntity() instanceof ShopBuyerEntity) {
            if (accessor.getServerData().contains("Budget")) {
                int budget = accessor.getServerData().getInt("Budget");
                tooltip.add(Component.translatable("marketblocks.jade.trader.budget", budget).withStyle(ChatFormatting.GOLD));
            }
        }
    }

    @Override
    public void appendServerData(CompoundTag data, EntityAccessor accessor) {
        if (accessor.getEntity() instanceof ShopBuyerEntity buyer) {
            data.putInt("Budget", buyer.getBudget());
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
