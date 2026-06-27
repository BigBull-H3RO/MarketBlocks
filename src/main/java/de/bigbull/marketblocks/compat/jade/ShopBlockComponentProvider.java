package de.bigbull.marketblocks.compat.jade;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.ChatFormatting;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;
import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.feature.singleoffer.entity.SingleOfferShopBlockEntity;

import net.minecraft.world.phys.Vec2;
import snownee.jade.api.IServerDataProvider;
import net.minecraft.nbt.CompoundTag;

public enum ShopBlockComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    public static final ResourceLocation SHOP_INFO = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID,
            "shop_info");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (accessor.getBlockEntity() instanceof SingleOfferShopBlockEntity shop) {

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
                    IElementHelper elements = IElementHelper.get();

                    tooltip.add(Component.translatable("marketblocks.jade.selling").withStyle(ChatFormatting.YELLOW));

                    // Selling Item
                    tooltip.append(elements.item(result, 1f).translate(new Vec2(0, -4)));
                    tooltip.append(Component.literal(" "));
                    tooltip.append(result.getHoverName());

                    tooltip.add(Component.translatable("marketblocks.jade.for").withStyle(ChatFormatting.YELLOW));

                    // Payment 1
                    if (!p1.isEmpty()) {
                        tooltip.append(elements.item(p1, 1f).translate(new Vec2(0, -4)));
                        tooltip.append(Component.literal(" "));
                    }

                    // Payment 2
                    if (!p2.isEmpty()) {
                        tooltip.append(elements.text(Component.literal("+ ").withStyle(ChatFormatting.GRAY)).translate(new Vec2(0, 1)));
                        tooltip.append(elements.item(p2, 1f).translate(new Vec2(0, -4)));
                        tooltip.append(Component.literal(" "));
                    }
                }

                // Out of stock warning (if not admin shop)
                if (!shop.isAdminShopEnabled()) {
                    if (accessor.getServerData().contains("HasStock")) {
                        boolean hasStock = accessor.getServerData().getBoolean("HasStock");
                        if (!hasStock) {
                            tooltip.add(Component.translatable("marketblocks.jade.out_of_stock")
                                    .withStyle(ChatFormatting.RED));
                        }
                    }
                    if (accessor.getServerData().contains("OutputFull")) {
                        boolean outputFull = accessor.getServerData().getBoolean("OutputFull");
                        if (outputFull) {
                            tooltip.add(Component.translatable("marketblocks.jade.output_full")
                                    .withStyle(ChatFormatting.RED));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof SingleOfferShopBlockEntity shop) {
            boolean hasStock = shop.isAdminShopEnabled() || shop.getOfferManager().hasResultItemInInput(true);
            boolean outputFull = !shop.isAdminShopEnabled() && !shop.getInventoryManager().hasOutputSpace(shop.getOfferPayment1(), shop.getOfferPayment2());
            data.putBoolean("HasStock", hasStock);
            data.putBoolean("OutputFull", outputFull);
        }
    }

    @Override
    public ResourceLocation getUid() {
        return SHOP_INFO;
    }
}
