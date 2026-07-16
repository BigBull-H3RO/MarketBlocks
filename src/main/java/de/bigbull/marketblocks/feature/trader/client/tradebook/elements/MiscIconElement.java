package de.bigbull.marketblocks.feature.trader.client.tradebook.elements;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.LodestoneTracker;
import de.bigbull.marketblocks.feature.trader.client.tradebook.ITradeBookElement;
import de.bigbull.marketblocks.feature.trader.client.tradebook.TradeBookLayoutUtils;
import de.bigbull.marketblocks.feature.trader.client.tradebook.TradeBookRenderContext;

public class MiscIconElement implements ITradeBookElement {

    @Override
    public boolean isInline() {
        return true;
    }

    @Override
    public boolean canHandle(String insertion) {
        if (insertion == null) return false;
        return insertion.startsWith("HEAD:") ||
               insertion.startsWith("ICON:COMPASS") ||
               insertion.startsWith("TOC_ICON:") ||
               insertion.startsWith("OFFER:");
    }

    @Override
    public int getExtraHeight(String insertion) {
        return 0; // These are inline and don't take extra vertical space on their own line
    }

    @Override
    public void render(GuiGraphics graphics, String insertion, int x, int y, int mouseX, int mouseY, float scale, TradeBookRenderContext context) {
        if (insertion.startsWith("HEAD:")) {
            String name = insertion.substring(5);
            TradeBookLayoutUtils.renderPlayerHead(graphics, name, x, y, scale, 10);
        } else if (insertion.startsWith("ICON:COMPASS")) {
            ItemStack compass = new ItemStack(Items.COMPASS);
            String[] parts = insertion.split(":");
            if (parts.length >= 6) {
                try {
                    int cx = Integer.parseInt(parts[2]);
                    int cy = Integer.parseInt(parts[3]);
                    int cz = Integer.parseInt(parts[4]);
                    String dim = parts[5];
                    if (parts.length > 6) dim += ":" + parts[6];
                    GlobalPos globalPos = GlobalPos.of(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dim)), new BlockPos(cx, cy, cz));
                    compass.set(DataComponents.LODESTONE_TRACKER, new LodestoneTracker(java.util.Optional.of(globalPos), true));
                } catch (Exception ignored) {}
            }
            TradeBookLayoutUtils.renderScaledIcon(graphics, compass, x + 4, y, 0.75f / scale);
        } else if (insertion.startsWith("TOC_ICON:")) {
            String iconId = insertion.substring(9);
            ItemStack iconItem = ItemStack.EMPTY;
            if (iconId.equals("intro")) iconItem = new ItemStack(Items.WRITABLE_BOOK);
            else if (iconId.equals("my_shops")) iconItem = new ItemStack(de.bigbull.marketblocks.core.init.RegistriesInit.TRADE_STAND_BLOCK.get());
            else if (iconId.equals("active_shops")) iconItem = new ItemStack(Items.MAP);
            else if (iconId.equals("shop_stats")) iconItem = new ItemStack(Items.GOLD_INGOT);
            else if (iconId.equals("market_stats")) iconItem = new ItemStack(Items.EMERALD);
            else if (iconId.equals("trends")) iconItem = new ItemStack(Items.COMPARATOR);

            if (!iconItem.isEmpty()) {
                TradeBookLayoutUtils.renderScaledIcon(graphics, iconItem, x - 1, y, 0.5f / scale);
            }
        } else if (insertion.startsWith("OFFER:")) {
            String[] parts = insertion.substring(6).split(":");
            String offerId = parts[0];
            String status = parts.length > 1 ? parts[1] : "OK";
            var offer = context.getOffers().get(offerId);
            if (offer != null) {
                TradeBookLayoutUtils.renderInlineOffer(graphics, offer, x + 7, y + 2, status, mouseX, mouseY, scale, context);
            }
        }
    }
}
