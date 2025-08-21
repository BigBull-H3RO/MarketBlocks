package de.bigbull.marketblocks.util.custom.screen;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.network.NetworkHandler;
import de.bigbull.marketblocks.network.packets.UpdateOwnersPacket;
import de.bigbull.marketblocks.network.packets.UpdateSettingsPacket;
import de.bigbull.marketblocks.util.custom.block.SideMode;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import de.bigbull.marketblocks.util.custom.menu.SmallShopSettingsMenu;
import de.bigbull.marketblocks.util.custom.screen.gui.GuiConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.*;

public class SmallShopSettingsScreen extends AbstractSmallShopScreen<SmallShopSettingsMenu> {
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/small_shop_settings.png");

    private EditBox nameField;
    private Checkbox emitRedstoneCheckbox;
    private CycleButton<SideMode> leftButton, rightButton, bottomButton, backButton;
    private boolean saved;
    private String originalName;
    private final Map<UUID, Checkbox> ownerCheckboxes = new HashMap<>();

    public SmallShopSettingsScreen(SmallShopSettingsMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = GuiConstants.IMAGE_WIDTH;
        this.imageHeight = GuiConstants.IMAGE_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        SmallShopBlockEntity blockEntity = menu.getBlockEntity();
        boolean isOwner = menu.isOwner();

        if (isOwner) {
            createTabButtons(leftPos + imageWidth + 4, topPos + 8, 2,
                    () -> switchTab(0),
                    () -> switchTab(1),
                    () -> {});

            nameField = addRenderableWidget(new EditBox(font, leftPos + 8, topPos + 20, 120, 20,
                    Component.translatable("gui.marketblocks.shop_name")));
            nameField.setMaxLength(32);
            originalName = blockEntity.getShopName();
            nameField.setValue(originalName);
            nameField.setResponder(s -> saved = false);

            emitRedstoneCheckbox = addRenderableWidget(Checkbox.builder(
                            Component.translatable("gui.marketblocks.emit_redstone"), font)
                    .pos(leftPos + 8, topPos + 50)
                    .selected(blockEntity.isEmitRedstone())
                    .tooltip(Tooltip.create(Component.translatable("gui.marketblocks.emit_redstone.tooltip")))
                    .onValueChange((btn, value) -> saved = false)
                    .build());

            addRenderableWidget(Button.builder(Component.translatable("gui.marketblocks.save"), b -> {
                String name = nameField.getValue();
                boolean emit = emitRedstoneCheckbox.selected();
                blockEntity.setShopNameClient(name);
                blockEntity.setEmitRedstoneClient(emit);
                blockEntity.setLeftMode(menu.getLeft());
                blockEntity.setRightMode(menu.getRight());
                blockEntity.setBottomMode(menu.getBottom());
                blockEntity.setBackMode(menu.getBack());

                List<UUID> selectedOwners = new ArrayList<>();
                blockEntity.getAdditionalOwners().clear();
                ownerCheckboxes.forEach((id, box) -> {
                    if (box.selected()) {
                        selectedOwners.add(id);
                        PlayerInfo info = Minecraft.getInstance().getConnection().getPlayerInfo(id);
                        String playerName = info != null ? info.getProfile().getName() : "";
                        blockEntity.addOwner(id, playerName);
                    }
                });

                NetworkHandler.sendToServer(new UpdateSettingsPacket(blockEntity.getBlockPos(), menu.getLeft(), menu.getRight(), menu.getBottom(), menu.getBack(), name, emit));
                NetworkHandler.sendToServer(new UpdateOwnersPacket(blockEntity.getBlockPos(), selectedOwners));
                saved = true;
            }).bounds(leftPos + imageWidth - 60 - 8, topPos + imageHeight - 20 - 8, 60, 20).build());

            int y = topPos + 80;
            leftButton = addRenderableWidget(createSideButton("left", menu.getLeft(), y, menu::setLeft));
            rightButton = addRenderableWidget(createSideButton("right", menu.getRight(), y + 22, menu::setRight));
            bottomButton = addRenderableWidget(createSideButton("bottom", menu.getBottom(), y + 44, menu::setBottom));
            backButton = addRenderableWidget(createSideButton("back", menu.getBack(), y + 66, menu::setBack));

            int playerY = y + 88;
            ownerCheckboxes.clear();
            if (Minecraft.getInstance().getConnection() != null) {
                Collection<PlayerInfo> players = Minecraft.getInstance().getConnection().getOnlinePlayers();
                Map<UUID, String> current = menu.getAdditionalOwners();
                for (PlayerInfo info : players) {
                    UUID id = info.getProfile().getId();
                    if (id.equals(blockEntity.getOwnerId())) continue;
                    String pname = info.getProfile().getName();
                    Checkbox cb = addRenderableWidget(Checkbox.builder(Component.literal(pname), font)
                            .pos(leftPos + 8, playerY)
                            .selected(current.containsKey(id))
                            .onValueChange((btn, val) -> saved = false)
                            .build());
                    ownerCheckboxes.put(id, cb);
                    playerY += 20;
                }
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        SmallShopBlockEntity blockEntity = menu.getBlockEntity();
        graphics.drawString(font, Component.translatable("gui.marketblocks.settings_title"), 8, 6, 4210752, false);
        renderOwnerInfo(graphics, blockEntity, menu.isOwner(), imageWidth);
        if (!menu.isOwner()) {
            Component info = Component.translatable("gui.marketblocks.settings_owner_only");
            int width = font.width(info);
            graphics.drawString(font, info, (imageWidth - width) / 2, 84, 0x808080, false);
        }
    }

    @Override
    protected boolean isOwner() {
        return menu.isOwner();
    }

    private CycleButton<SideMode> createSideButton(String sideKey, SideMode initial, int y, java.util.function.Consumer<SideMode> setter) {
        return CycleButton.builder(SideMode::getDisplayName)
                .withValues(SideMode.values())
                .withInitialValue(initial)
                .create(leftPos + 8, y, 120, 20,
                        Component.translatable("gui.marketblocks.side." + sideKey),
                        (btn, value) -> {
                            setter.accept(value);
                            saved = false;
                        });
    }

    @Override
    public void onClose() {
        if (!saved) {
            menu.resetModes();
        }
        if (nameField != null && nameField.getValue().trim().isEmpty()) {
            menu.getBlockEntity().setShopNameClient(originalName);
        }
        super.onClose();
    }
}