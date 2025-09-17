package de.bigbull.marketblocks.util.custom.screen;

import com.mojang.datafixers.util.Pair;
import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.network.NetworkHandler;
import de.bigbull.marketblocks.network.packets.*;
import de.bigbull.marketblocks.util.custom.block.SmallShopBlock;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import de.bigbull.marketblocks.util.custom.menu.ShopTab;
import de.bigbull.marketblocks.util.custom.menu.SmallShopMenu;
import de.bigbull.marketblocks.util.custom.screen.gui.GuiConstants;
import de.bigbull.marketblocks.util.custom.screen.gui.IconButton;
import de.bigbull.marketblocks.util.custom.screen.gui.OfferTemplateButton;
import de.bigbull.marketblocks.util.custom.screen.gui.SideModeButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * Unified screen for the Small Shop. Rebuilds its UI dynamically when the tab
 * changes instead of opening new screens.
 */
public class SmallShopScreen extends AbstractSmallShopScreen<SmallShopMenu> {
    private static final ResourceLocation OFFERS_BG = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/small_shop_offers.png");
    private static final ResourceLocation INVENTORY_BG = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/small_shop_inventory.png");
    private static final ResourceLocation SETTINGS_BG = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/small_shop_settings.png");
    private static final ResourceLocation OUT_OF_STOCK_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/out_of_stock.png");
    private static final ResourceLocation OUTPUT_FULL_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/output_full.png");
    private static final ResourceLocation CREATE_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/create.png");
    private static final ResourceLocation DELETE_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/delete.png");
    private static final ResourceLocation INPUT_OUTPUT_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/input_output.png");

    private record IconRect(int x, int y, int width, int height) { }
    private static final IconRect STATUS_ICON_RECT = new IconRect(82, 50, 28, 21);

    private ShopTab lastTab;

    // Offers widgets
    private OfferTemplateButton offerButton;

    // Settings widgets
    private EditBox nameField;
    private Checkbox emitRedstoneCheckbox;
    private SideModeButton leftButton, rightButton, bottomButton, backButton;
    private Direction leftDir, rightDir, bottomDir, backDir;
    private boolean saved;
    private boolean noPlayers;
    private String originalName;
    private final Map<UUID, Checkbox> ownerCheckboxes = new HashMap<>();

    public SmallShopScreen(SmallShopMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = GuiConstants.IMAGE_WIDTH;
        this.imageHeight = GuiConstants.IMAGE_HEIGHT;
        this.inventoryLabelY = GuiConstants.PLAYER_INV_LABEL_Y;
    }

    @Override
    protected void init() {
        super.init();
        lastTab = menu.getActiveTab();
        if (menu.isOwner()) {
            createTabButtons(leftPos + imageWidth + 4, topPos + 8, lastTab,
                    () -> switchTab(ShopTab.OFFERS),
                    () -> switchTab(ShopTab.INVENTORY),
                    () -> switchTab(ShopTab.SETTINGS));
        }
        buildCurrentTabUI();
    }

    @Override
    public void containerTick() {
        super.containerTick();
        ShopTab current = menu.getActiveTab();
        if (current != lastTab) {
            lastTab = current;
            rebuildUI();
        }
    }

    @Override
    protected void switchTab(ShopTab tab) {
        if (menu.isOwner()) {
            menu.setActiveTabClient(tab);
            lastTab = tab;
            rebuildUI();
        }
        super.switchTab(tab);
    }

    private void rebuildUI() {
        clearWidgets();
        if (menu.isOwner()) {
            createTabButtons(leftPos + imageWidth + 4, topPos + 8, menu.getActiveTab(),
                    () -> switchTab(ShopTab.OFFERS),
                    () -> switchTab(ShopTab.INVENTORY),
                    () -> switchTab(ShopTab.SETTINGS));
        }
        buildCurrentTabUI();
    }

    private void buildCurrentTabUI() {
        switch (menu.getActiveTab()) {
            case OFFERS -> buildOffersUI();
            case INVENTORY -> buildInventoryUI();
            case SETTINGS -> buildSettingsUI();
        }
    }

    // --- Build tab UIs ---
    private void buildOffersUI() {
        SmallShopBlockEntity be = menu.getBlockEntity();
        boolean isOwner = menu.isOwner();
        offerButton = addRenderableWidget(new OfferTemplateButton(leftPos + 44, topPos + 17, b -> onOfferClicked()));
        offerButton.active = be.hasOffer();
        if (isOwner) {
            if (!be.hasOffer()) {
                addRenderableWidget(new IconButton(leftPos + 148, topPos + 17, 20, 20, BUTTON_SPRITES, CREATE_ICON,
                        b -> createOffer(), Component.translatable("gui.marketblocks.create_offer"), () -> false));
            } else {
                addRenderableWidget(new IconButton(leftPos + 148, topPos + 17, 20, 20, BUTTON_SPRITES, DELETE_ICON,
                        b -> deleteOffer(), Component.translatable("gui.marketblocks.delete_offer"), () -> false));
            }
        }
    }

    private void buildInventoryUI() {
        // no special widgets besides tab buttons
    }

    private void buildSettingsUI() {
        SmallShopBlockEntity be = menu.getBlockEntity();
        Direction facing = be.getBlockState().getValue(SmallShopBlock.FACING);
        leftDir = facing.getCounterClockWise();
        rightDir = facing.getClockWise();
        bottomDir = Direction.DOWN;
        backDir = facing.getOpposite();

        nameField = addRenderableWidget(new EditBox(font, leftPos + 8, topPos + 20, 120, 20,
                Component.translatable("gui.marketblocks.shop_name")));
        nameField.setMaxLength(32);
        originalName = be.getShopName();
        nameField.setValue(originalName);
        nameField.setResponder(s -> saved = false);

        emitRedstoneCheckbox = addRenderableWidget(Checkbox.builder(Component.translatable("gui.marketblocks.emit_redstone"), font)
                .pos(leftPos + 8, topPos + 50)
                .selected(be.isEmitRedstone())
                .tooltip(Tooltip.create(Component.translatable("gui.marketblocks.emit_redstone.tooltip")))
                .onValueChange((btn, val) -> saved = false)
                .build());

        addRenderableWidget(Button.builder(Component.translatable("gui.marketblocks.save"), b -> {
            String name = nameField.getValue();
            boolean emit = emitRedstoneCheckbox.selected();
            be.setShopNameClient(name);
            be.setEmitRedstoneClient(emit);
            be.setModeClient(leftDir, menu.getMode(leftDir));
            be.setModeClient(rightDir, menu.getMode(rightDir));
            be.setModeClient(bottomDir, menu.getMode(bottomDir));
            be.setModeClient(backDir, menu.getMode(backDir));
            List<UUID> selectedOwners = new ArrayList<>();
            be.getAdditionalOwners().clear();
            Map<UUID, String> stored = menu.getAdditionalOwners();
            ownerCheckboxes.forEach((id, cb) -> {
                if (cb.selected()) {
                    selectedOwners.add(id);
                    PlayerInfo info = Minecraft.getInstance().getConnection() != null ?
                            Minecraft.getInstance().getConnection().getPlayerInfo(id) : null;
                    String n = info != null ? info.getProfile().getName() : stored.getOrDefault(id, "");
                    be.addOwnerClient(id, n);
                }
            });
            NetworkHandler.sendToServer(new UpdateSettingsPacket(be.getBlockPos(), menu.getMode(leftDir), menu.getMode(rightDir),
                    menu.getMode(bottomDir), menu.getMode(backDir), name, emit));
            NetworkHandler.sendToServer(new UpdateOwnersPacket(be.getBlockPos(), selectedOwners));
            saved = true;
        }).bounds(leftPos + imageWidth - 68, topPos + imageHeight - 28, 60, 20).build());

        int y = topPos + 80;
        leftButton = addRenderableWidget(new SideModeButton(leftPos + 8, y, 16, 16, menu.getMode(leftDir), m -> {
            menu.setMode(leftDir, m); saved = false; }));
        leftButton.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.side.left")));
        leftButton.setMessage(Component.translatable("gui.marketblocks.side.left"));

        rightButton = addRenderableWidget(new SideModeButton(leftPos + 8, y + 22, 16, 16, menu.getMode(rightDir), m -> {
            menu.setMode(rightDir, m); saved = false; }));
        rightButton.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.side.right")));
        rightButton.setMessage(Component.translatable("gui.marketblocks.side.right"));

        bottomButton = addRenderableWidget(new SideModeButton(leftPos + 8, y + 44, 16, 16, menu.getMode(bottomDir), m -> {
            menu.setMode(bottomDir, m); saved = false; }));
        bottomButton.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.side.bottom")));
        bottomButton.setMessage(Component.translatable("gui.marketblocks.side.bottom"));

        backButton = addRenderableWidget(new SideModeButton(leftPos + 8, y + 66, 16, 16, menu.getMode(backDir), m -> {
            menu.setMode(backDir, m); saved = false; }));
        backButton.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.side.back")));
        backButton.setMessage(Component.translatable("gui.marketblocks.side.back"));

        int playerY = y + 88;
        ownerCheckboxes.clear();
        Map<UUID, String> current = new HashMap<>(menu.getAdditionalOwners());
        if (Minecraft.getInstance().getConnection() != null) {
            Collection<PlayerInfo> players = Minecraft.getInstance().getConnection().getOnlinePlayers();
            for (PlayerInfo info : players) {
                UUID id = info.getProfile().getId();
                if (id.equals(be.getOwnerId())) continue;
                String pname = info.getProfile().getName();
                Checkbox cb = addRenderableWidget(Checkbox.builder(Component.literal(pname), font)
                        .pos(leftPos + 8, playerY)
                        .selected(current.containsKey(id))
                        .onValueChange((btn, val) -> saved = false)
                        .build());
                ownerCheckboxes.put(id, cb);
                playerY += 20;
                current.remove(id);
            }
        }
        for (Map.Entry<UUID, String> entry : current.entrySet()) {
            UUID id = entry.getKey();
            String pname = entry.getValue();
            Checkbox cb = addRenderableWidget(Checkbox.builder(Component.literal(pname), font)
                    .pos(leftPos + 8, playerY)
                    .selected(true)
                    .onValueChange((btn, val) -> saved = false)
                    .build());
            ownerCheckboxes.put(id, cb);
            playerY += 20;
        }
        noPlayers = ownerCheckboxes.isEmpty();
    }

    // --- Rendering ---
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);

        if (menu.getActiveTab() == ShopTab.OFFERS) {
            SmallShopBlockEntity be = menu.getBlockEntity();
            if (be.hasOffer() && isHovering(STATUS_ICON_RECT.x(), STATUS_ICON_RECT.y(), STATUS_ICON_RECT.width(), STATUS_ICON_RECT.height(), mouseX, mouseY)) {
                if (!be.hasResultItemInInput(false)) {
                    graphics.renderTooltip(font, Component.translatable("gui.marketblocks.out_of_stock"), mouseX, mouseY);
                } else if (be.isOutputSpaceMissing()) {
                    graphics.renderTooltip(font, Component.translatable("gui.marketblocks.output_full"), mouseX, mouseY);
                } else if (be.isOutputAlmostFull()) {
                    graphics.renderTooltip(font, Component.translatable("gui.marketblocks.output_almost_full"), mouseX, mouseY);
                }
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        switch (menu.getActiveTab()) {
            case OFFERS -> renderOffersBg(graphics);
            case INVENTORY -> renderInventoryBg(graphics);
            case SETTINGS -> renderSettingsBg(graphics);
        }
    }

    private void renderOffersBg(GuiGraphics graphics) {
        graphics.blit(OFFERS_BG, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        SmallShopBlockEntity be = menu.getBlockEntity();
        offerButton.active = be.hasOffer();
        if (be.hasOffer()) {
            offerButton.update(be.getOfferPayment1(), be.getOfferPayment2(), be.getOfferResult(), be.hasResultItemInInput(false));
        } else {
            ItemStack p1 = menu.slots.get(0).getItem();
            ItemStack p2 = menu.slots.get(1).getItem();
            Pair<ItemStack, ItemStack> norm = normalizePayments(p1, p2);
            offerButton.update(norm.getFirst(), norm.getSecond(), menu.slots.get(2).getItem(), true);
        }
        int iconX = leftPos + STATUS_ICON_RECT.x();
        int iconY = topPos + STATUS_ICON_RECT.y();

        boolean outOfStock = be.hasOffer() && !be.hasResultItemInInput(false);
        boolean outputBlocked = be.hasOffer() && be.isOutputSpaceMissing();

        if (outOfStock) {
            graphics.blit(OUT_OF_STOCK_ICON, iconX, iconY, 0, 0, STATUS_ICON_RECT.width(), STATUS_ICON_RECT.height(), STATUS_ICON_RECT.width(), STATUS_ICON_RECT.height());
        } else if (outputBlocked) {
            graphics.blit(OUT_OF_STOCK_ICON, iconX, iconY, 0, 0, STATUS_ICON_RECT.width(), STATUS_ICON_RECT.height(), STATUS_ICON_RECT.width(), STATUS_ICON_RECT.height());
        } else if (be.hasOffer() && be.isOfferAvailable() && be.isOutputAlmostFull()) {
            graphics.blit(OUTPUT_FULL_ICON, iconX, iconY, 0, 0, STATUS_ICON_RECT.width(), STATUS_ICON_RECT.height(), STATUS_ICON_RECT.width(), STATUS_ICON_RECT.height());
        }
    }

    private void renderInventoryBg(GuiGraphics graphics) {
        graphics.blit(INVENTORY_BG, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        graphics.blit(INPUT_OUTPUT_ICON, leftPos + 77, topPos + 33, 0, 0, 22, 22, 22, 22);
    }

    private void renderSettingsBg(GuiGraphics graphics) {
        graphics.blit(SETTINGS_BG, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        switch (menu.getActiveTab()) {
            case OFFERS -> renderOffersLabels(graphics);
            case INVENTORY -> renderInventoryLabels(graphics);
            case SETTINGS -> renderSettingsLabels(graphics);
        }
    }

    private void renderOffersLabels(GuiGraphics graphics) {
        SmallShopBlockEntity be = menu.getBlockEntity();
        Component title;
        String name = be.getShopName();
        if (name != null && !name.isEmpty()) {
            title = Component.literal(name);
        } else {
            title = Component.translatable("gui.marketblocks.shop_title");
        }
        graphics.drawString(font, title, 8, 6, 4210752, false);
        renderOwnerInfo(graphics, be, menu.isOwner(), imageWidth);
        graphics.drawString(font, playerInventoryTitle, 8, GuiConstants.PLAYER_INV_LABEL_Y, 4210752, false);
    }

    private void renderInventoryLabels(GuiGraphics graphics) {
        SmallShopBlockEntity be = menu.getBlockEntity();
        graphics.drawString(font, Component.translatable("gui.marketblocks.inventory_title"), 8, 6, 4210752, false);
        renderOwnerInfo(graphics, be, menu.isOwner(), imageWidth);
        if (!menu.isOwner()) {
            Component info = Component.translatable("gui.marketblocks.inventory_owner_only");
            int w = font.width(info);
            graphics.drawString(font, info, (imageWidth - w) / 2, 84, 0x808080, false);
        }
        graphics.drawString(font, playerInventoryTitle, 8, GuiConstants.PLAYER_INV_LABEL_Y, 4210752, false);
    }

    private void renderSettingsLabels(GuiGraphics graphics) {
        SmallShopBlockEntity be = menu.getBlockEntity();
        graphics.drawString(font, Component.translatable("gui.marketblocks.settings_title"), 8, 6, 4210752, false);
        renderOwnerInfo(graphics, be, menu.isOwner(), imageWidth);
        if (!menu.isOwner()) {
            Component info = Component.translatable("gui.marketblocks.settings_owner_only");
            int w = font.width(info);
            graphics.drawString(font, info, (imageWidth - w) / 2, 84, 0x808080, false);
        } else if (noPlayers) {
            Component info = Component.translatable("gui.marketblocks.no_players_available");
            int w = font.width(info);
            graphics.drawString(font, info, (imageWidth - w) / 2, 84, 0x808080, false);
        }
    }

    // --- Helpers for offers ---
    private void createOffer() {
        try {
            ItemStack p1 = menu.slots.get(0).getItem().copy();
            ItemStack p2 = menu.slots.get(1).getItem().copy();
            ItemStack result = menu.slots.get(2).getItem().copy();
            Pair<ItemStack, ItemStack> norm = normalizePayments(p1, p2);
            p1 = norm.getFirst();
            p2 = norm.getSecond();
            if (result.isEmpty()) {
                minecraft.gui.getChat().addMessage(Component.translatable("gui.marketblocks.error.no_result_item").withStyle(ChatFormatting.RED));
                playSound(SoundEvents.ITEM_BREAK);
                return;
            }
            if (p1.isEmpty() && p2.isEmpty()) {
                minecraft.gui.getChat().addMessage(Component.translatable("gui.marketblocks.error.no_payment_items").withStyle(ChatFormatting.RED));
                playSound(SoundEvents.ITEM_BREAK);
                return;
            }
            SmallShopBlockEntity be = menu.getBlockEntity();
            NetworkHandler.sendToServer(new CreateOfferPacket(be.getBlockPos(), p1, p2, result));
            be.setHasOfferClient(true);
            playSound(SoundEvents.EXPERIENCE_ORB_PICKUP);
            rebuildUI();
        } catch (Exception e) {
            MarketBlocks.LOGGER.error("Error creating offer", e);
            playSound(SoundEvents.ITEM_BREAK);
        }
    }

    private void deleteOffer() {
        SmallShopBlockEntity be = menu.getBlockEntity();
        NetworkHandler.sendToServer(new DeleteOfferPacket(be.getBlockPos()));
        be.setHasOfferClient(false);
        playSound(SoundEvents.UI_BUTTON_CLICK);
        rebuildUI();
    }

    private void onOfferClicked() {
        SmallShopBlockEntity be = menu.getBlockEntity();
        if (be.hasOffer()) {
            NetworkHandler.sendToServer(new AutoFillPaymentPacket(be.getBlockPos()));
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

    private Pair<ItemStack, ItemStack> normalizePayments(ItemStack p1, ItemStack p2) {
        if (p1.isEmpty() && !p2.isEmpty()) {
            return Pair.of(p2, ItemStack.EMPTY);
        }
        return Pair.of(p1, p2);
    }

    @Override
    protected boolean isOwner() {
        return menu.isOwner();
    }

    @Override
    public void onClose() {
        if (!saved && menu.getActiveTab() == ShopTab.SETTINGS) {
            menu.resetModes();
        }
        if (menu.getActiveTab() == ShopTab.SETTINGS && nameField != null && nameField.getValue().trim().isEmpty()) {
            menu.getBlockEntity().setShopNameClient(originalName);
        }
        super.onClose();
    }
}