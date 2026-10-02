package de.bigbull.marketblocks.compat.jade;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.ChatFormatting;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.JadeUI;
import de.bigbull.marketblocks.Constants;
import de.bigbull.marketblocks.core.config.Config;
import de.bigbull.marketblocks.feature.singleoffer.entity.SingleOfferShopBlockEntity;

import net.minecraft.world.level.block.entity.BlockEntity;
import de.bigbull.marketblocks.feature.singleoffer.block.TradeStandTopBlock;

public enum ShopBlockComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    public static final Identifier SHOP_INFO = Identifier.fromNamespaceAndPath(Constants.MOD_ID,
            "shop_info");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!Config.ENABLE_JADE_COMPAT.get()) {
            return;
        }
        BlockEntity be = accessor.getBlockEntity();
        if (be == null && accessor.getBlock() instanceof TradeStandTopBlock) {
            be = accessor.getLevel().getBlockEntity(accessor.getPosition().below());
        }
        if (be instanceof SingleOfferShopBlockEntity shop) {

            // Status and Owner/Name
            if (shop.getGeneralSettings().isClosed()) {
                tooltip.add(Component.translatable("marketblocks.jade.status.closed").withStyle(ChatFormatting.RED));
            } else if (shop.isAdminShopEnabled()) {
                tooltip.add(Component.translatable("marketblocks.jade.status.admin_shop")
                        .withStyle(ChatFormatting.LIGHT_PURPLE));
            } else {
                String shopName = shop.getShopName();
                String ownerName = shop.getOwnerName();
                if (shopName != null && !shopName.isEmpty()) {
                    tooltip.add(Component.translatable("marketblocks.jade.shop", shopName)
                            .withStyle(ChatFormatting.GREEN));
                }
                if (ownerName != null && !ownerName.isEmpty()) {
                    tooltip.add(Component.translatable("marketblocks.jade.owner", ownerName)
                            .withStyle(ChatFormatting.GREEN));
                }
            }

            // Offer
            if (shop.hasOffer()) {
                ItemStack result = shop.getOfferResult();
                ItemStack p1 = shop.getOfferPayment1();
                ItemStack p2 = shop.getOfferPayment2();

                if (!result.isEmpty() && (!p1.isEmpty() || !p2.isEmpty())) {
                    tooltip.add(Component.translatable("marketblocks.jade.selling").withStyle(ChatFormatting.YELLOW));

                    // Selling Item
                    tooltip.append(JadeUI.item(result, 1f).offset(0, -4));
                    tooltip.append(Component.literal(" "));
                    tooltip.append(result.getHoverName());

                    tooltip.add(Component.translatable("marketblocks.jade.for").withStyle(ChatFormatting.YELLOW));

                    // Payment 1
                    if (!p1.isEmpty()) {
                        tooltip.append(JadeUI.item(p1, 1f).offset(0, -4));
                        tooltip.append(Component.literal(" "));
                    }

                    // Payment 2
                    if (!p2.isEmpty()) {
                        tooltip.append(JadeUI.text(Component.literal("+ ").withStyle(ChatFormatting.GRAY)).offset(0, 1));
                        tooltip.append(JadeUI.item(p2, 1f).offset(0, -4));
                        tooltip.append(Component.literal(" "));
                    }
                }

                // Out of stock warning (if not admin shop)
                if (!shop.isAdminShopEnabled()) {
                    accessor.getServerData().getBoolean("HasStock").ifPresent(hasStock -> {
                        if (!hasStock) {
                            tooltip.add(Component.translatable("marketblocks.jade.out_of_stock")
                                    .withStyle(ChatFormatting.RED));
                        }
                    });
                    accessor.getServerData().getBoolean("OutputFull").ifPresent(outputFull -> {
                        if (outputFull) {
                            tooltip.add(Component.translatable("marketblocks.jade.output_full")
                                    .withStyle(ChatFormatting.RED));
                        }
                    });
                }
            }
        }
    }


    @Override
    public Identifier getUid() {
        return SHOP_INFO;
    }
}
