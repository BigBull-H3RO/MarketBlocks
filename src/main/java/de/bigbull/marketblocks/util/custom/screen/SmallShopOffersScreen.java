package de.bigbull.marketblocks.util.custom.screen;

import com.mojang.datafixers.util.Pair;
import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.data.lang.ModLang;
import de.bigbull.marketblocks.network.NetworkHandler;
import de.bigbull.marketblocks.network.packets.AutoFillPaymentPacket;
import de.bigbull.marketblocks.network.packets.CreateOfferPacket;
import de.bigbull.marketblocks.network.packets.DeleteOfferPacket;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import de.bigbull.marketblocks.util.custom.menu.ShopTab;
import de.bigbull.marketblocks.util.custom.menu.SmallShopOffersMenu;
import de.bigbull.marketblocks.util.custom.screen.gui.GuiConstants;
import de.bigbull.marketblocks.util.custom.screen.gui.IconButton;
import de.bigbull.marketblocks.util.custom.screen.gui.OfferTemplateButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * The screen for the "Offers" tab of the Small Shop.
 * It allows players to view and interact with the shop's trade offer,
 * and allows owners to create or delete the offer.
 */
public class SmallShopOffersScreen extends AbstractSmallShopScreen<SmallShopOffersMenu> {
    private static final ResourceLocation BACKGROUND = MarketBlocks.id("textures/gui/small_shop_offers.png");
    private static final ResourceLocation OUT_OF_STOCK_ICON = MarketBlocks.id("textures/gui/icon/out_of_stock.png");
    private static final ResourceLocation CREATE_ICON = MarketBlocks.id("textures/gui/icon/create.png");
    private static final ResourceLocation DELETE_ICON = MarketBlocks.id("textures/gui/icon/delete.png");

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
            createTabButtons(leftPos + imageWidth + 4, topPos + 8, ShopTab.OFFERS);

            if (!blockEntity.hasOffer()) {
                addRenderableWidget(new IconButton(
                        leftPos + 148, topPos + 17, 20, 20,
                        BUTTON_SPRITES, CREATE_ICON,
                        button -> createOffer(),
                        Component.translatable(ModLang.GUI_CREATE_OFFER),
                        () -> false
                ));
            } else {
                addRenderableWidget(new IconButton(
                        leftPos + 148, topPos + 17, 20, 20,
                        BUTTON_SPRITES, DELETE_ICON,
                        button -> deleteOffer(),
                        Component.translatable(ModLang.GUI_DELETE_OFFER),
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

            if (result.isEmpty()) {
                displayError(ModLang.GUI_ERROR_NO_RESULT_ITEM);
                return;
            }

            if (normalized.getFirst().isEmpty() && normalized.getSecond().isEmpty()) {
                displayError(ModLang.GUI_ERROR_NO_PAYMENT_ITEMS);
                return;
            }

            NetworkHandler.sendToServer(new CreateOfferPacket(
                    menu.getBlockEntity().getBlockPos(),
                    normalized.getFirst(),
                    normalized.getSecond(),
                    result
            ));

            playSound(SoundEvents.EXPERIENCE_ORB_PICKUP);
            // The screen will be re-initialized upon receiving the offer update packet
        } catch (Exception e) {
            MarketBlocks.LOGGER.error("Error creating offer", e);
            playSound(SoundEvents.ITEM_BREAK);
        }
    }

    private void deleteOffer() {
        NetworkHandler.sendToServer(new DeleteOfferPacket(menu.getBlockEntity().getBlockPos()));
    }

    /**
     * Called by a packet handler when the server confirms the offer has been deleted.
     */
    public void onOfferDeleted() {
        if (this.minecraft != null && this.minecraft.player != null) {
            menu.getBlockEntity().setHasOfferClient(false);
            init(); // Re-initialize the screen to update buttons
        }
    }

    /**
     * Called by a packet handler when the server confirms the offer has been created.
     */
    public void onOfferCreated() {
        if (this.minecraft != null && this.minecraft.player != null) {
            menu.getBlockEntity().setHasOfferClient(true);
            init(); // Re-initialize the screen to update buttons
        }
    }

    /**
     * Checks if this screen instance is for the shop at the given position.
     * @param pos The BlockPos to check.
     * @return True if this screen is for the given pos, false otherwise.
     */
    public boolean isFor(BlockPos pos) {
        return this.menu.getBlockEntity().getBlockPos().equals(pos);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
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
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        SmallShopBlockEntity blockEntity = menu.getBlockEntity();

        Component title;
        String name = blockEntity.getShopName();
        if (name != null && !name.isEmpty()) {
            title = Component.literal(name);
        } else {
            title = Component.translatable(ModLang.GUI_SHOP_TITLE);
        }
        graphics.drawString(font, title, 8, 6, 4210752, false);
        renderOwnerInfo(graphics, blockEntity);
        graphics.drawString(font, playerInventoryTitle, 8, GuiConstants.PLAYER_INV_LABEL_Y, 4210752, false);
    }

    private void onOfferClicked() {
        SmallShopBlockEntity blockEntity = menu.getBlockEntity();

        if (blockEntity.hasOffer()) {
            // If an offer exists, clicking the button tries to auto-fill payment
            NetworkHandler.sendToServer(new AutoFillPaymentPacket(blockEntity.getBlockPos()));
        } else if (menu.isOwner()) {
            // If no offer exists and the player is the owner, clear the template slots
            for (int i = 0; i < 3; i++) {
                menu.slots.get(i).set(ItemStack.EMPTY);
            }
        }
    }

    private Pair<ItemStack, ItemStack> normalizePayments(ItemStack p1, ItemStack p2) {
        if (p1.isEmpty() && !p2.isEmpty()) {
            return Pair.of(p2, ItemStack.EMPTY);
        }
        return Pair.of(p1, p2);
    }

    private void displayError(String langKey) {
        if (minecraft != null && minecraft.gui.getChat() != null) {
            minecraft.gui.getChat().addMessage(
                    Component.translatable(langKey).withStyle(ChatFormatting.RED)
            );
        }
        playSound(SoundEvents.ITEM_BREAK);
    }
}