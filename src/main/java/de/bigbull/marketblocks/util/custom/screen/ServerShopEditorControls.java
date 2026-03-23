package de.bigbull.marketblocks.util.custom.screen;

import de.bigbull.marketblocks.network.NetworkHandler;
import de.bigbull.marketblocks.network.packets.serverShop.ServerShopAddOfferPacket;
import de.bigbull.marketblocks.network.packets.serverShop.ServerShopCreatePagePacket;
import de.bigbull.marketblocks.network.packets.serverShop.ServerShopDeleteOfferPacket;
import de.bigbull.marketblocks.network.packets.serverShop.ServerShopDeletePagePacket;
import de.bigbull.marketblocks.network.packets.serverShop.ServerShopMoveOfferPacket;
import de.bigbull.marketblocks.network.packets.serverShop.ServerShopRenamePagePacket;
import de.bigbull.marketblocks.util.custom.screen.gui.IconButton;
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
    private static final int OFFER_LIST_CONTROLS_X_OFFSET = -84;
    private static final int OFFER_LIST_CONTROLS_Y_OFFSET = -98;
    private static final int PREVIEW_ACTION_X_OFFSET = 98;

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
            int controlsX = context.controlsX() + OFFER_LIST_CONTROLS_X_OFFSET;
            int controlsY = context.controlsY() + OFFER_LIST_CONTROLS_Y_OFFSET;

            callbacks.addWidget(new IconButton(controlsX, controlsY, 20, 20, context.buttonSprites(), context.deleteIcon(),
                    ignored -> {
                        NetworkHandler.sendToServer(new ServerShopDeleteOfferPacket(context.selectedOfferId()));
                        callbacks.clearSelectedOffer();
                        callbacks.rebuildUi();
                    },
                    Component.translatable("gui.marketblocks.server_shop.delete_offer"), () -> false));

            callbacks.addWidget(new IconButton(controlsX + 22, controlsY, 20, 20, context.buttonSprites(), context.moveUpIcon(),
                    ignored -> NetworkHandler.sendToServer(new ServerShopMoveOfferPacket(context.selectedOfferId(), page.name(), -1)),
                    Component.translatable("gui.marketblocks.server_shop.move_offer_up"), () -> false));

            callbacks.addWidget(new IconButton(controlsX + 44, controlsY, 20, 20, context.buttonSprites(), context.moveDownIcon(),
                    ignored -> NetworkHandler.sendToServer(new ServerShopMoveOfferPacket(context.selectedOfferId(), page.name(), 1)),
                    Component.translatable("gui.marketblocks.server_shop.move_offer_down"), () -> false));
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

        int clearX = context.previewX() + PREVIEW_ACTION_X_OFFSET;
        int clearY = context.previewY();
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

        callbacks.addWidget(new IconButton(clearX, clearY, 20, 20, context.buttonSprites(), context.clearSelectionIcon(),
                ignored -> {
                    callbacks.clearSelectedOffer();
                    callbacks.updatePreview();
                    callbacks.rebuildUi();
                },
                Component.translatable("gui.marketblocks.server_shop.clear_selection"), () -> false));
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
