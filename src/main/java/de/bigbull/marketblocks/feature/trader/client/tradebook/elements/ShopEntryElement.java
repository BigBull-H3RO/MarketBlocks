package de.bigbull.marketblocks.feature.trader.client.tradebook.elements;

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
        String[] parts = insertion.split("\\|\\|");
        if (parts.length > 1 && parts[1].equals("my_shop")) {
            return 45;
        }
        return 50;
    }

    @Override
    public void render(GuiGraphics graphics, String insertion, int startX, int startY, int mouseX, int mouseY,
            float scale, TradeBookRenderContext context) {
        String[] parts = insertion.split("\\|\\|");
        if (parts.length < 13)
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

        int currentY;

        // Render Owner Name (Centered) for dir_shop
        if (!prefix.equals("my_shop")) {
            currentY = startY + 10; // Abstand zwischen Shopnamen und Spielernamen (hier verändern!)

            String owner = TradeBookLayoutUtils.truncate(ownerFull, 10);
            int ownerWidth = context.getFont().width(owner);
            int ownerX = startX + ((int) (TradeBookLayoutUtils.TEXT_WIDTH / scale) - ownerWidth) / 2;
            graphics.drawString(context.getFont(), owner, ownerX, currentY, 0x555555, false);

            if (ownerFull.length() > 10) {
                int scaledX = (int) (ownerX * scale);
                int scaledY = (int) (currentY * scale);
                int scaledW = (int) (ownerWidth * scale);
                int scaledH = (int) (9 * scale);
                context.addActiveZone(new InteractiveZone(scaledX, scaledY, scaledW, scaledH, () -> {
                    context.setNextHoveredObject("owner_name_" + ownerFull);
                    context.addTooltip(() -> graphics.renderTooltip(context.getFont(), Component.literal(ownerFull),
                            mouseX, mouseY));
                }, null));
            }
            currentY += 12; // Abstand zwischen Spielernamen und Angebot (bleibt konstant)
        } else {
            currentY = startY + 16; // Abstand zwischen Shopnamen und Angebot bei "My Shops" (hier verändern!)
        }

        // Render Head (Left), Offer (Center), Compass (Right)
        if (!offerId.equals("NO_OFFER")) {
            var offer = context.getOffers().get(offerId);
            if (offer != null) {
                if (!prefix.equals("my_shop")) {
                    TradeBookLayoutUtils.renderPlayerHead(graphics, ownerFull, startX + 5, currentY + 3, scale, 10);
                }

                int offerX = startX + 30; // Centered roughly
                TradeBookLayoutUtils.renderInlineOffer(graphics, offer, offerX, currentY + 6, finalStatus, mouseX,
                        mouseY, scale, context);

                if (canTeleport) {
                    renderCompass(graphics, startX, currentY, px, py, pz, dim, scale);
                }
            }
            currentY += 20;
        } else {
            // No offer background
            int offerX = startX + 30;
            int frameX = offerX - 2;
            int frameY = currentY - 4;
            graphics.blit(TradeBookLayoutUtils.OFFER_GUI, frameX, frameY, 1, 3, 94, 26, 96, 32);

            String noOfferStr = Component.translatable("gui.marketblocks.trade_book.active.no_offer").getString();
            int noOfferWidth = context.getFont().width(noOfferStr);
            int noOfferX = frameX + (94 - noOfferWidth) / 2; // Centered inside the frame

            if (!prefix.equals("my_shop")) {
                TradeBookLayoutUtils.renderPlayerHead(graphics, ownerFull, startX + 5, currentY + 3, scale, 10);
            }
            graphics.drawString(context.getFont(), noOfferStr, noOfferX, currentY + 6, 0xAAAAAA, false);

            if (canTeleport) {
                renderCompass(graphics, startX, currentY, px, py, pz, dim, scale);
            }
            currentY += 20;
        }

        // Coordinates
        currentY += 3;
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
                NetworkHandler.sendToServer(new TeleportRequestPacket(dim, px, py, pz));
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
