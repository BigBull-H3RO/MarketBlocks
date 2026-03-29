package de.bigbull.marketblocks.util.custom.screen;

import de.bigbull.marketblocks.network.NetworkHandler;
import de.bigbull.marketblocks.network.packets.serverShop.ServerShopAddOfferPacket;
import de.bigbull.marketblocks.network.packets.serverShop.ServerShopCreatePagePacket;
import de.bigbull.marketblocks.network.packets.serverShop.ServerShopDeleteOfferPacket;
import de.bigbull.marketblocks.network.packets.serverShop.ServerShopDeletePagePacket;
import de.bigbull.marketblocks.network.packets.serverShop.ServerShopMoveOfferPacket;
import de.bigbull.marketblocks.network.packets.serverShop.ServerShopRenamePagePacket;
import de.bigbull.marketblocks.util.custom.screen.gui.IconButton;
import de.bigbull.marketblocks.util.custom.screen.gui.VanillaIconButton;
import de.bigbull.marketblocks.util.custom.servershop.ServerShopOffer;
import de.bigbull.marketblocks.util.custom.servershop.ServerShopPage;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Builds and wires edit-mode controls for {@link ServerShopScreen}.
 */
public final class ServerShopEditorControls {

    private static final int HEADER_X_OFFSET = 4;
    private static final int HEADER_Y_OFFSET = -24;
    private static final int PREVIEW_ACTION_X_OFFSET = 98;
    private static final int PREVIEW_WIDTH = 88;
    private static final int CLEAR_CORNER_BUTTON_SIZE = 8;
    private static final int CLEAR_CORNER_BUTTON_X_OFFSET = 1;
    private static final int CLEAR_CORNER_BUTTON_Y_OFFSET = -1;
    private static final int PREVIEW_MOVE_BUTTON_GAP = 4;
    private static final int PREVIEW_MOVE_BUTTON_Y_OFFSET = 0;
    private static final int MOVE_BUTTON_WIDTH = 20;
    private static final int MOVE_ICON_CONTENT_WIDTH = 9;
    private static final int MOVE_ICON_CONTENT_HEIGHT = 6;
    private static final int MOVE_ICON_CONTENT_U = 4;
    private static final int MOVE_ICON_CONTENT_V = 6;
    private static final int MOVE_DOWN_ICON_Y_OFFSET = 1;

    public void build(Context context, Callbacks callbacks) {
        int headerX = context.leftPos() + HEADER_X_OFFSET;
        int headerY = context.topPos() + HEADER_Y_OFFSET;
        addPageCreateButton(context, callbacks, headerX, headerY);

        if (context.pages().isEmpty()) {
            return;
        }

        ServerShopPage page = context.pages().get(Math.min(context.selectedPage(), context.pages().size() - 1));
        addPageEditButtons(context, callbacks, headerX, headerY, page);
        addOfferListControls(context, callbacks, page);
        addSelectedOfferControls(context, callbacks);
    }

    private void addPageCreateButton(Context context, Callbacks callbacks, int headerX, int headerY) {
        callbacks.addWidget(new IconButton(headerX, headerY, 20, 20, context.buttonSprites(), context.addPageIcon(),
                ignored -> callbacks.openTextInput(Component.translatable("gui.marketblocks.server_shop.add_page"), "",
                        false, name -> NetworkHandler.sendToServer(new ServerShopCreatePagePacket(name))),
                Component.translatable("gui.marketblocks.server_shop.add_page"), () -> false));
    }

    private void addPageEditButtons(Context context, Callbacks callbacks, int headerX, int headerY, ServerShopPage page) {
        callbacks.addWidget(new IconButton(headerX + 24, headerY, 20, 20, context.buttonSprites(), context.renamePageIcon(),
                ignored -> callbacks.openTextInput(Component.translatable("gui.marketblocks.server_shop.rename_page"), page.name(),
                        false, name -> NetworkHandler.sendToServer(new ServerShopRenamePagePacket(page.name(), name))),
                Component.translatable("gui.marketblocks.server_shop.rename_page"), () -> false));

        callbacks.addWidget(new IconButton(headerX + 48, headerY, 20, 20, context.buttonSprites(), context.deletePageIcon(),
                ignored -> NetworkHandler.sendToServer(new ServerShopDeletePagePacket(page.name())),
                Component.translatable("gui.marketblocks.server_shop.delete_page"), () -> false));
    }

    private void addOfferListControls(Context context, Callbacks callbacks, ServerShopPage page) {
        if (context.selectedOfferId() != null) {
            int deleteX = context.previewX() + PREVIEW_ACTION_X_OFFSET;
            int deleteY = context.previewY();

            callbacks.addWidget(new IconButton(deleteX, deleteY, 20, 20, context.buttonSprites(), context.deleteIcon(),
                    ignored -> {
                        NetworkHandler.sendToServer(new ServerShopDeleteOfferPacket(context.selectedOfferId()));
                        callbacks.clearSelectedOffer();
                        callbacks.rebuildUi();
                    },
                    Component.translatable("gui.marketblocks.server_shop.delete_offer"), () -> false));

            int moveButtonHeight = Math.max(1, context.rowHeight() / 2);
            int moveX = context.previewX() - MOVE_BUTTON_WIDTH - PREVIEW_MOVE_BUTTON_GAP;
            int moveY = context.previewY() + PREVIEW_MOVE_BUTTON_Y_OFFSET;

            callbacks.addWidget(new VanillaIconButton(moveX, moveY, MOVE_BUTTON_WIDTH, moveButtonHeight,
                    context.moveUpIcon(), moveButtonHeight,
                    MOVE_ICON_CONTENT_U, MOVE_ICON_CONTENT_V, MOVE_ICON_CONTENT_WIDTH, MOVE_ICON_CONTENT_HEIGHT,
                    ignored -> NetworkHandler.sendToServer(new ServerShopMoveOfferPacket(context.selectedOfferId(), page.name(), -1)),
                    Component.translatable("gui.marketblocks.server_shop.move_offer_up")));

            callbacks.addWidget(new VanillaIconButton(moveX, moveY + moveButtonHeight, MOVE_BUTTON_WIDTH, moveButtonHeight,
                    context.moveDownIcon(), moveButtonHeight,
                    MOVE_ICON_CONTENT_U, MOVE_ICON_CONTENT_V, MOVE_ICON_CONTENT_WIDTH, MOVE_ICON_CONTENT_HEIGHT,
                    MOVE_DOWN_ICON_Y_OFFSET,
                    ignored -> NetworkHandler.sendToServer(new ServerShopMoveOfferPacket(context.selectedOfferId(), page.name(), 1)),
                    Component.translatable("gui.marketblocks.server_shop.move_offer_down")));
        } else {
            int addOfferX = context.previewX() + PREVIEW_ACTION_X_OFFSET;
            int addOfferY = context.previewY();

            callbacks.addWidget(new IconButton(addOfferX, addOfferY, 20, 20, context.buttonSprites(), context.addIcon(),
                    ignored -> NetworkHandler.sendToServer(new ServerShopAddOfferPacket(page.name())),
                    Component.translatable("gui.marketblocks.server_shop.add_offer"), () -> false));
        }
    }


    private void addSelectedOfferControls(Context context, Callbacks callbacks) {
        if (context.selectedOfferId() == null) {
            return;
        }

        int clearX = context.previewX() + PREVIEW_WIDTH - CLEAR_CORNER_BUTTON_SIZE + CLEAR_CORNER_BUTTON_X_OFFSET;
        int clearY = context.previewY() + CLEAR_CORNER_BUTTON_Y_OFFSET;
        ServerShopOffer selectedOffer = callbacks.findOfferOnSelectedPage(context.selectedOfferId());
        int controlsX = callbacks.rightEditorButtonsX();
        int controlsY = callbacks.rightEditorButtonsY();

        if (selectedOffer != null) {
            callbacks.addWidget(new IconButton(controlsX, controlsY, 20, 20, context.buttonSprites(), context.limitsIcon(),
                    ignored -> callbacks.openOfferLimitsEditor(selectedOffer),
                    Component.translatable("gui.marketblocks.server_shop.inline.limits"), () -> false));

            callbacks.addWidget(new IconButton(controlsX, controlsY + context.rightButtonSize() + context.rightButtonGap(), 20, 20,
                    context.buttonSprites(), context.pricingIcon(),
                    ignored -> callbacks.openOfferPricingEditor(selectedOffer),
                    Component.translatable("gui.marketblocks.server_shop.inline.pricing"), () -> false));
        }

        callbacks.addWidget(new VanillaIconButton(clearX, clearY, CLEAR_CORNER_BUTTON_SIZE, CLEAR_CORNER_BUTTON_SIZE,
                context.clearSelectionIcon(), CLEAR_CORNER_BUTTON_SIZE,
                ignored -> {
                    callbacks.clearSelectedOffer();
                    callbacks.updatePreview();
                    callbacks.rebuildUi();
                },
                Component.translatable("gui.marketblocks.server_shop.clear_selection")));
    }

    public interface Callbacks {
        void addWidget(AbstractWidget widget);

        void openTextInput(Component title, String initialValue, boolean allowEmpty, Consumer<String> onConfirm);

        void openOfferLimitsEditor(ServerShopOffer offer);

        void openOfferPricingEditor(ServerShopOffer offer);

        void clearSelectedOffer();

        void updatePreview();

        void rebuildUi();

        ServerShopOffer findOfferOnSelectedPage(UUID offerId);

        int rightEditorButtonsX();

        int rightEditorButtonsY();
    }

    public record Context(
            int leftPos,
            int topPos,
            int selectedPage,
            List<ServerShopPage> pages,
            UUID selectedOfferId,
            int listXOffset,
            int listYOffset,
            int listWidth,
            int rowHeight,
            int scrollOffset,
            int maxVisibleRows,
            int previewXOffset,
            int previewYOffset,
            int controlsXStart,
            int controlsYStart,
            int rightButtonSize,
            int rightButtonGap,
            WidgetSprites buttonSprites,
            ResourceLocation addIcon,
            ResourceLocation deleteIcon,
            ResourceLocation addPageIcon,
            ResourceLocation deletePageIcon,
            ResourceLocation renamePageIcon,
            ResourceLocation clearSelectionIcon,
            ResourceLocation moveUpIcon,
            ResourceLocation moveDownIcon,
            ResourceLocation limitsIcon,
            ResourceLocation pricingIcon
    ) {
        int previewX() {
            return leftPos + previewXOffset;
        }

        int listStartX() {
            return leftPos + listXOffset;
        }

        int listStartY() {
            return topPos + listYOffset;
        }

        int previewY() {
            return topPos + previewYOffset;
        }

        int controlsX() {
            return leftPos + controlsXStart;
        }

        int controlsY() {
            return topPos + controlsYStart;
        }
    }
}
