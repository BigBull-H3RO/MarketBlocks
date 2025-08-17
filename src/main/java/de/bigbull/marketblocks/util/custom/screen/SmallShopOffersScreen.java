package de.bigbull.marketblocks.util.custom.screen;

import com.mojang.datafixers.util.Pair;
import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.network.NetworkHandler;
import de.bigbull.marketblocks.network.packets.AutoFillPaymentPacket;
import de.bigbull.marketblocks.network.packets.CreateOfferPacket;
import de.bigbull.marketblocks.network.packets.DeleteOfferPacket;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import de.bigbull.marketblocks.util.custom.menu.SmallShopOffersMenu;
import de.bigbull.marketblocks.util.custom.screen.gui.GuiConstants;
import de.bigbull.marketblocks.util.custom.screen.gui.IconButton;
import de.bigbull.marketblocks.util.custom.screen.gui.OfferTemplateButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class SmallShopOffersScreen extends AbstractSmallShopScreen<SmallShopOffersMenu> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/small_shop_offers.png");
    private static final ResourceLocation OUT_OF_STOCK_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/out_of_stock.png");

    private static final ResourceLocation CREATE_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/create.png");
    private static final ResourceLocation DELETE_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/delete.png");

    private OfferTemplateButton offerButton;

    public SmallShopOffersScreen(SmallShopOffersMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = GuiConstants.IMAGE_WIDTH;
        this.imageHeight = GuiConstants.IMAGE_HEIGHT;
        this.inventoryLabelY = GuiConstants.PLAYER_INV_LABEL_Y;
    }

    @Override
    protected void init() {
        super.init();

        SmallShopBlockEntity blockEntity = menu.getBlockEntity();
        boolean isOwner = menu.isOwner();

        this.offerButton = addRenderableWidget(new OfferTemplateButton(
                leftPos + 44, topPos + 17,
                button -> onOfferClicked()
        ));

        this.offerButton.active = blockEntity.hasOffer();
        this.offerButton.visible = true;

        if (isOwner) {
            createTabButtons(leftPos + imageWidth + 4, topPos + 8, true, () -> {}, () -> switchTab(false));
        }

        if (isOwner) {
            if (!blockEntity.hasOffer()) {
                addRenderableWidget(new IconButton(
                        leftPos + 148, topPos + 17, 20, 20,
                        BUTTON_SPRITES, CREATE_ICON,
                        button -> createOffer(),
                        Component.translatable("gui.marketblocks.create_offer"),
                        () -> false
                ));
            } else {
                addRenderableWidget(new IconButton(
                        leftPos + 148, topPos + 17, 20, 20,
                        BUTTON_SPRITES, DELETE_ICON,
                        button -> deleteOffer(),
                        Component.translatable("gui.marketblocks.delete_offer"),
                        () -> false
                ));
            }
        }
    }

    private void createOffer() {
        try {
            ItemStack payment1 = menu.slots.get(0).getItem().copy();
            ItemStack payment2 = menu.slots.get(1).getItem().copy();
            ItemStack result = menu.slots.get(2).getItem().copy();

            Pair<ItemStack, ItemStack> normalized = normalizePayments(payment1, payment2);
            payment1 = normalized.getFirst();
            payment2 = normalized.getSecond();

            if (result.isEmpty()) {
                minecraft.gui.getChat().addMessage(
                        Component.translatable("gui.marketblocks.error.no_result_item")
                                .withStyle(ChatFormatting.RED)
                );
                playSound(SoundEvents.ITEM_BREAK);
                return;
            }

            if (payment1.isEmpty() && payment2.isEmpty()) {
                minecraft.gui.getChat().addMessage(
                        Component.translatable("gui.marketblocks.error.no_payment_items")
                                .withStyle(ChatFormatting.RED)
                );
                playSound(SoundEvents.ITEM_BREAK);
                return;
            }

            NetworkHandler.sendToServer(new CreateOfferPacket(
                    menu.getBlockEntity().getBlockPos(),
                    payment1,
                    payment2,
                    result
            ));

            menu.getBlockEntity().setHasOfferClient(true);
            playSound(SoundEvents.EXPERIENCE_ORB_PICKUP);
            init();

        } catch (Exception e) {
            MarketBlocks.LOGGER.error("Error creating offer", e);
            playSound(SoundEvents.ITEM_BREAK);
        }
    }

    private Pair<ItemStack, ItemStack> normalizePayments(ItemStack p1, ItemStack p2) {
        if (p1.isEmpty() && !p2.isEmpty()) {
            return Pair.of(p2, ItemStack.EMPTY);
        }
        return Pair.of(p1, p2);
    }

    private void deleteOffer() {
        NetworkHandler.sendToServer(new DeleteOfferPacket(menu.getBlockEntity().getBlockPos()));
        playSound(SoundEvents.UI_BUTTON_CLICK);
    }

    public void onOfferDeleted() {
        menu.getBlockEntity().setHasOfferClient(false);
        init();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        graphics.blit(BACKGROUND, i, j, 0, 0, imageWidth, imageHeight);

        SmallShopBlockEntity blockEntity = menu.getBlockEntity();

        offerButton.active = blockEntity.hasOffer();

        if (blockEntity.hasOffer()) {
            offerButton.update(
                    blockEntity.getOfferPayment1(),
                    blockEntity.getOfferPayment2(),
                    blockEntity.getOfferResult(),
                    blockEntity.isOfferAvailable()
            );
        } else {
            ItemStack p1 = menu.slots.get(0).getItem();
            ItemStack p2 = menu.slots.get(1).getItem();
            Pair<ItemStack, ItemStack> normalized = normalizePayments(p1, p2);
            offerButton.update(
                    normalized.getFirst(),
                    normalized.getSecond(),
                    menu.slots.get(2).getItem(),
                    true
            );
        }

        if (blockEntity.hasOffer() && !blockEntity.isOfferAvailable()) {
            graphics.blit(OUT_OF_STOCK_ICON, leftPos + 82, topPos + 50, 0, 0, 28, 21, 28, 21);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        SmallShopBlockEntity blockEntity = menu.getBlockEntity();

        Component title = Component.translatable("gui.marketblocks.shop_title");
        graphics.drawString(font, title, 8, 6, 4210752, false);

        renderOwnerInfo(graphics, blockEntity, menu.isOwner(), imageWidth);

        graphics.drawString(font, playerInventoryTitle, 8, GuiConstants.PLAYER_INV_LABEL_Y, 4210752, false);
    }

    protected boolean isOwner() {
        return menu.isOwner();
    }

    private void onOfferClicked() {
        SmallShopBlockEntity blockEntity = menu.getBlockEntity();

        if (blockEntity.hasOffer()) {
            NetworkHandler.sendToServer(new AutoFillPaymentPacket(blockEntity.getBlockPos()));
            playSound(SoundEvents.UI_BUTTON_CLICK);
            return;
        }

        if (menu.isOwner()) {
            for (int i = 0; i < 3; i++) {
                menu.slots.get(i).set(ItemStack.EMPTY);
            }

            playSound(SoundEvents.UI_BUTTON_CLICK);
        }
    }
}