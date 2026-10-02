package de.bigbull.marketblocks.compat.jade;

import de.bigbull.marketblocks.core.config.Config;
import de.bigbull.marketblocks.feature.singleoffer.block.TradeStandTopBlock;
import de.bigbull.marketblocks.feature.singleoffer.entity.SingleOfferShopBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public enum ShopBlockServerDataProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (!Config.ENABLE_JADE_COMPAT.get()) {
            return;
        }
        BlockEntity be = accessor.getBlockEntity();
        if (be == null && accessor.getBlock() instanceof TradeStandTopBlock) {
            be = accessor.getLevel().getBlockEntity(accessor.getPosition().below());
        }
        if (be instanceof SingleOfferShopBlockEntity shop) {
            boolean hasStock = shop.isAdminShopEnabled() || shop.getOfferManager().hasResultItemInInput(true);
            boolean outputFull = !shop.isAdminShopEnabled() && !shop.getInventoryManager().hasOutputSpace(shop.getOfferPayment1(), shop.getOfferPayment2());
            data.putBoolean("HasStock", hasStock);
            data.putBoolean("OutputFull", outputFull);
        }
    }

    @Override
    public Identifier getUid() {
        return ShopBlockComponentProvider.SHOP_INFO;
    }
}
