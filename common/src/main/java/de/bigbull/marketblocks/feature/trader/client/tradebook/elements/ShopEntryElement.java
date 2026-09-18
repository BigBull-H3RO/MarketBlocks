package de.bigbull.marketblocks.feature.trader.client.tradebook.elements;

import java.util.UUID;

import de.bigbull.marketblocks.feature.trader.network.TeleportRequestPacket;
import de.bigbull.marketblocks.network.NetworkHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.component.DataComponents;
import de.bigbull.marketblocks.feature.trader.client.tradebook.ITradeBookElement;
import de.bigbull.marketblocks.feature.trader.client.tradebook.InteractiveZone;
import de.bigbull.marketblocks.feature.trader.client.tradebook.TradeBookLayoutUtils;
import de.bigbull.marketblocks.feature.trader.client.tradebook.TradeBookRenderContext;

public class ShopEntryElement implements ITradeBookElement {

    @Override
    public boolean canHandle(String insertion) {
        return insertion != null && insertion.startsWith("SHOP_ENTRY||");
    }

    @Override
    public int getExtraHeight(String insertion) {
        return 45;
    }

    @Override
    public void render(GuiGraphics graphics, String insertion, int startX, int startY, int mouseX, int mouseY,
            float scale, TradeBookRenderContext context) {
        String[] parts = insertion.split("\\|\\|");
        if (parts.length < 14)
            return;

        String prefix = parts[1];
        String shopNameFull = parts[2];
        String ownerFull = parts[3];
        String compactSales = parts[4];
        boolean isClosed = Boolean.parseBoolean(parts[5]);
        String offerId = parts[6];
        String finalStatus = parts[7];
        int px = Integer.parseInt(parts[8]);
        int py = Integer.parseInt(parts[9]);
        int pz = Integer.parseInt(parts[10]);
        String dim = parts[11];
        boolean canTeleport = Boolean.parseBoolean(parts[12]);
        String shopId = parts[13];
        UUID ownerUuid = null;
        if (parts.length > 14 && !parts[14].isEmpty()) {
            try {
                ownerUuid = UUID.fromString(parts[14]);
            } catch (Exception ignored) {}
        }

        // Render Icon / Status
        String statusIcon = isClosed ? "§c✖" : "§a✔";
        graphics.drawString(context.getFont(), statusIcon, startX, startY, 0, false);

        // Render Shop Name
        String shopName = TradeBookLayoutUtils.truncate(shopNameFull, 18);
        graphics.drawString(context.getFont(), shopName, startX + 12, startY, 0xFFAA00, false);

        if (shopNameFull.length() > 18) {
            int nameWidth = context.getFont().width(shopName);
            int scaledX = (int) (startX * scale);
            int scaledY = (int) (startY * scale);
            int scaledW = (int) (nameWidth * scale);
            int scaledH = (int) (9 * scale);
            context.addActiveZone(new InteractiveZone(scaledX + (int) (12 * scale), scaledY, scaledW, scaledH, () -> {
                context.setNextHoveredObject("shop_name_" + shopNameFull);
                context.addTooltip(() -> graphics.renderTooltip(context.getFont(), Component.literal(shopNameFull),
                        mouseX, mouseY));
            }, null));
        }

        // Render Sales
        String salesText = "Sales: " + compactSales;
        TradeBookLayoutUtils.drawRightAlignedString(graphics, context, salesText, startX, startY,
                (int) (TradeBookLayoutUtils.TEXT_WIDTH / scale), 0x555555, false);

        int currentY = startY + 18;

        // Render Head (Left) with 1px border and tooltip for player shops
        if (!prefix.equals("my_shop")) {
            TradeBookLayoutUtils.renderPlayerHead(graphics, ownerUuid, ownerFull, startX + 5, currentY + 3, scale, 10, true);

            int headScreenX = (int) ((startX + 5) * scale);
            int headScreenY = (int) ((currentY + 2) * scale);
            context.addActiveZone(new InteractiveZone(headScreenX - 1, headScreenY - 1, 12, 12, () -> {
                context.setNextHoveredObject("owner_head_" + ownerFull + "_" + shopId);
                context.addTooltip(() -> graphics.renderTooltip(context.getFont(),
                        Component.translatable("gui.marketblocks.owner", ownerFull), mouseX, mouseY));
            }, null));
        }

        // Render Offer (Center), Compass (Right)
        if (!offerId.equals("NO_OFFER")) {
            var offer = context.getOffers().get(offerId);
            if (offer != null) {
                int offerX = startX + 30; // Centered roughly
                TradeBookLayoutUtils.renderInlineOffer(graphics, offer, offerX, currentY + 6, finalStatus, mouseX,
                        mouseY, scale, context);

                if (canTeleport) {
                    renderCompass(graphics, startX, currentY, px, py, pz, dim, scale);
                }
            }
            currentY += 21;
        } else {
            // No offer background
            int offerX = startX + 30;
            int frameX = offerX - 3;
            int frameY = currentY - 5;
            graphics.blit(TradeBookLayoutUtils.OFFER_GUI, frameX, frameY, 0, 2, 96, 28, 96, 32);

            String noOfferStr = Component.translatable("gui.marketblocks.trade_book.active.no_offer").getString();
            int noOfferWidth = context.getFont().width(noOfferStr);
            int noOfferX = frameX + (96 - noOfferWidth) / 2; // Centered inside the frame

            graphics.drawString(context.getFont(), noOfferStr, noOfferX, currentY + 6, 0xAAAAAA, false);

            if (canTeleport) {
                renderCompass(graphics, startX, currentY, px, py, pz, dim, scale);
            }
            currentY += 21;
        }

        // Coordinates
        currentY += 4;
        String coords = px + ", " + py + ", " + pz;
        int coordsWidth = context.getFont().width(coords);
        int coordsX = startX + ((int) (TradeBookLayoutUtils.TEXT_WIDTH / scale) - coordsWidth) / 2;

        if (canTeleport) {
            graphics.drawString(context.getFont(), coords, coordsX, currentY, 0x5555FF, false);

            int scaledX = (int) (coordsX * scale);
            int scaledY = (int) (currentY * scale);
            int scaledW = (int) (coordsWidth * scale);
            int scaledH = (int) (9 * scale);

            context.addActiveZone(new InteractiveZone(scaledX, scaledY, scaledW, scaledH, () -> {
                context.setNextHoveredObject("tp_" + px + "_" + py + "_" + pz);
                context.addTooltip(() -> graphics.renderTooltip(context.getFont(),
                        Component.translatable("gui.marketblocks.trade_book.active.hover_tp"), mouseX, mouseY));
            }, () -> {
                NetworkHandler.sendToServer(new TeleportRequestPacket(shopId));
            }));
        } else {
            graphics.drawString(context.getFont(), coords, coordsX, currentY, 0x555555, false);
        }

        if (currentY < 170) {
            int lineY = currentY + 9;
            graphics.fill(startX, lineY, startX + (int) (TradeBookLayoutUtils.TEXT_WIDTH / scale), lineY + 1,
                    0x33000000);
        }
    }

    private void renderCompass(GuiGraphics graphics, int startX, int currentY, int px, int py, int pz, String dim,
            float scale) {
        ItemStack compass = new ItemStack(Items.COMPASS);
        try {
            GlobalPos globalPos = GlobalPos.of(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dim)),
                    new BlockPos(px, py, pz));
            compass.set(DataComponents.LODESTONE_TRACKER, new LodestoneTracker(java.util.Optional.of(globalPos), true));
        } catch (Exception ignored) {
        }

        int compassX = startX + (int) (TradeBookLayoutUtils.TEXT_WIDTH / scale) - 25;
        TradeBookLayoutUtils.renderScaledIcon(graphics, compass, compassX, currentY + 2, 0.75f / scale);
    }
}
