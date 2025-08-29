package de.bigbull.marketblocks.util.custom.screen;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.network.NetworkHandler;
import de.bigbull.marketblocks.network.packets.UpdateOwnersPacket;
import de.bigbull.marketblocks.network.packets.UpdateSettingsPacket;
import de.bigbull.marketblocks.util.custom.block.SmallShopBlock;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import de.bigbull.marketblocks.util.custom.menu.ShopTab;
import de.bigbull.marketblocks.util.custom.menu.SmallShopSettingsMenu;
import de.bigbull.marketblocks.util.custom.screen.gui.GuiConstants;
import de.bigbull.marketblocks.util.custom.screen.gui.SideModeButton;
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
import net.minecraft.world.entity.player.Inventory;

import java.util.*;

public class SmallShopSettingsScreen extends AbstractSmallShopScreen<SmallShopSettingsMenu> {
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/small_shop_settings.png");

    private EditBox nameField;
    private Checkbox emitRedstoneCheckbox;
    private SideModeButton leftButton, rightButton, bottomButton, backButton;
    private Direction leftDir, rightDir, bottomDir, backDir;
    private boolean saved;
    private boolean noPlayers;
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
        Direction facing = blockEntity.getBlockState().getValue(SmallShopBlock.FACING);
        leftDir = facing.getCounterClockWise();
        rightDir = facing.getClockWise();
        bottomDir = Direction.DOWN;
        backDir = facing.getOpposite();

        if (isOwner) {
            createTabButtons(leftPos + imageWidth + 4, topPos + 8, ShopTab.SETTINGS,
                    () -> switchTab(ShopTab.OFFERS),
                    () -> switchTab(ShopTab.INVENTORY),
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
                blockEntity.setMode(leftDir, menu.getMode(leftDir));
                blockEntity.setMode(rightDir, menu.getMode(rightDir));
                blockEntity.setMode(bottomDir, menu.getMode(bottomDir));
                blockEntity.setMode(backDir, menu.getMode(backDir));

                List<UUID> selectedOwners = new ArrayList<>();
                blockEntity.getAdditionalOwners().clear();
                Map<UUID, String> storedOwners = menu.getAdditionalOwners();
                ownerCheckboxes.forEach((id, box) -> {
                    if (box.selected()) {
                        selectedOwners.add(id);
                        PlayerInfo info = Minecraft.getInstance().getConnection() != null
                                ? Minecraft.getInstance().getConnection().getPlayerInfo(id)
                                : null;
                        String playerName = info != null ? info.getProfile().getName() : storedOwners.getOrDefault(id, "");
                        blockEntity.addOwner(id, playerName);
                    }
                });

                NetworkHandler.sendToServer(new UpdateSettingsPacket(blockEntity.getBlockPos(), menu.getMode(leftDir), menu.getMode(rightDir), menu.getMode(bottomDir), menu.getMode(backDir), name, emit));
                NetworkHandler.sendToServer(new UpdateOwnersPacket(blockEntity.getBlockPos(), selectedOwners));
                saved = true;
            }).bounds(leftPos + imageWidth - 60 - 8, topPos + imageHeight - 20 - 8, 60, 20).build());

            int y = topPos + 80;
            leftButton = addRenderableWidget(new SideModeButton(leftPos + 8, y, 16, 16, menu.getMode(leftDir), m -> {
                menu.setMode(leftDir, m);
                saved = false;
            }));
            leftButton.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.side.left")));
            leftButton.setMessage(Component.translatable("gui.marketblocks.side.left"));

            rightButton = addRenderableWidget(new SideModeButton(leftPos + 8, y + 22, 16, 16, menu.getMode(rightDir), m -> {
                menu.setMode(rightDir, m);
                saved = false;
            }));
            rightButton.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.side.right")));
            rightButton.setMessage(Component.translatable("gui.marketblocks.side.right"));

            bottomButton = addRenderableWidget(new SideModeButton(leftPos + 8, y + 44, 16, 16, menu.getMode(bottomDir), m -> {
                menu.setMode(bottomDir, m);
                saved = false;
            }));
            bottomButton.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.side.bottom")));
            bottomButton.setMessage(Component.translatable("gui.marketblocks.side.bottom"));

            backButton = addRenderableWidget(new SideModeButton(leftPos + 8, y + 66, 16, 16, menu.getMode(backDir), m -> {
                menu.setMode(backDir, m);
                saved = false;
            }));
            backButton.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.side.back")));
            backButton.setMessage(Component.translatable("gui.marketblocks.side.back"));

            int playerY = y + 88;
            ownerCheckboxes.clear();
            Map<UUID, String> current = new HashMap<>(menu.getAdditionalOwners());
            if (Minecraft.getInstance().getConnection() != null) {
                Collection<PlayerInfo> players = Minecraft.getInstance().getConnection().getOnlinePlayers();
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
        } else if (noPlayers) {
            Component info = Component.translatable("gui.marketblocks.no_players_available");
            int width = font.width(info);
            graphics.drawString(font, info, (imageWidth - width) / 2, 84, 0x808080, false);
        }
    }

    @Override
    protected boolean isOwner() {
        return menu.isOwner();
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