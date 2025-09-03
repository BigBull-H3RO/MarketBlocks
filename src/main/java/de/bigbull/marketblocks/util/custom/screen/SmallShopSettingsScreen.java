package de.bigbull.marketblocks.util.custom.screen;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.data.lang.ModLang;
import de.bigbull.marketblocks.network.NetworkHandler;
import de.bigbull.marketblocks.network.packets.UpdateOwnersPacket;
import de.bigbull.marketblocks.network.packets.UpdateSettingsPacket;
import de.bigbull.marketblocks.util.custom.block.SmallShopBlock;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import de.bigbull.marketblocks.util.custom.menu.ShopTab;
import de.bigbull.marketblocks.util.custom.menu.SmallShopSettingsMenu;
import de.bigbull.marketblocks.util.custom.screen.gui.GuiConstants;
import de.bigbull.marketblocks.util.custom.screen.gui.SideModeButton;
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
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The screen for the "Settings" tab of the Small Shop.
 * This UI is only accessible to the owner and allows configuration of the shop's name,
 * side I/O modes, redstone behavior, and additional owners.
 */
public class SmallShopSettingsScreen extends AbstractSmallShopScreen<SmallShopSettingsMenu> {
    private static final ResourceLocation BACKGROUND = MarketBlocks.id("textures/gui/small_shop_settings.png");
    private static final int INFO_TEXT_COLOR = 0x808080;
    private static final int MAX_NAME_LENGTH = 32;

    private EditBox nameField;
    private Checkbox emitRedstoneCheckbox;
    private final Map<UUID, Checkbox> ownerCheckboxes = new HashMap<>();

    private boolean hasChanges = false;
    private String originalName;

    public SmallShopSettingsScreen(@NotNull SmallShopSettingsMenu menu, @NotNull Inventory inv, @NotNull Component title) {
        super(menu, inv, title);
        this.imageWidth = GuiConstants.IMAGE_WIDTH;
        this.imageHeight = GuiConstants.IMAGE_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        if (!this.isOwner()) {
            return;
        }
        this.hasChanges = false;
        this.originalName = this.menu.getBlockEntity().getShopName();

        createTabButtons(this.leftPos + this.imageWidth, this.topPos, ShopTab.SETTINGS);
        createNameAndRedstoneWidgets();
        createSideModeButtons();
        createOwnerCheckboxes();
        createSaveButton();
    }

    private void createNameAndRedstoneWidgets() {
        this.nameField = addRenderableWidget(new EditBox(this.font, this.leftPos + 8, this.topPos + 20, 120, 20,
                Component.translatable(ModLang.GUI_SHOP_NAME)));
        this.nameField.setMaxLength(MAX_NAME_LENGTH);
        this.nameField.setValue(this.originalName);
        this.nameField.setResponder(s -> this.hasChanges = true);

        this.emitRedstoneCheckbox = addRenderableWidget(Checkbox.builder(
                        Component.translatable(ModLang.GUI_EMIT_REDSTONE), this.font)
                .pos(this.leftPos + 8, this.topPos + 50)
                .selected(this.menu.getBlockEntity().isEmitRedstone())
                .tooltip(Tooltip.create(Component.translatable(ModLang.GUI_EMIT_REDSTONE_TOOLTIP)))
                .onValueChange((btn, value) -> this.hasChanges = true)
                .build());
    }

    private void createSideModeButtons() {
        final SmallShopBlockEntity blockEntity = this.menu.getBlockEntity();
        final Direction facing = blockEntity.getBlockState().getValue(SmallShopBlock.FACING);
        final int buttonX = this.leftPos + 8;
        int buttonY = this.topPos + 80;

        createSideButton(buttonX, buttonY, facing.getCounterClockWise(), ModLang.GUI_SIDE_LEFT);
        createSideButton(buttonX, buttonY + 22, facing.getClockWise(), ModLang.GUI_SIDE_RIGHT);
        createSideButton(buttonX, buttonY + 44, Direction.DOWN, ModLang.GUI_SIDE_BOTTOM);
        createSideButton(buttonX, buttonY + 66, facing.getOpposite(), ModLang.GUI_SIDE_BACK);
    }

    private void createSideButton(int x, int y, @NotNull Direction dir, @NotNull String langKey) {
        SideModeButton button = new SideModeButton(x, y, 16, 16, this.menu.getMode(dir), newMode -> {
            this.menu.setMode(dir, newMode);
            this.hasChanges = true;
        });
        button.setTooltip(Tooltip.create(Component.translatable(langKey)));
        addRenderableWidget(button);
    }

    private void createOwnerCheckboxes() {
        this.ownerCheckboxes.clear();
        if (this.minecraft == null || this.minecraft.getConnection() == null) return;

        final SmallShopBlockEntity blockEntity = this.menu.getBlockEntity();
        final Map<UUID, String> currentOwners = new HashMap<>(this.menu.getAdditionalOwners());
        int yPos = this.topPos + 80;

        List<PlayerInfo> onlinePlayers = new ArrayList<>(this.minecraft.getConnection().getOnlinePlayers());
        onlinePlayers.sort(Comparator.comparing(p -> p.getProfile().getName().toLowerCase(Locale.ROOT)));

        for (PlayerInfo info : onlinePlayers) {
            final UUID id = info.getProfile().getId();
            if (id.equals(blockEntity.getOwnerId())) continue;

            Checkbox cb = createOwnerCheckbox(yPos, info.getProfile().getName(), currentOwners.containsKey(id));
            this.ownerCheckboxes.put(id, cb);
            currentOwners.remove(id);
            yPos += 20;
        }

        // Add checkboxes for any offline owners
        for (Map.Entry<UUID, String> entry : currentOwners.entrySet()) {
            Checkbox cb = createOwnerCheckbox(yPos, entry.getValue(), true);
            this.ownerCheckboxes.put(entry.getKey(), cb);
            yPos += 20;
        }
    }

    private Checkbox createOwnerCheckbox(int y, @NotNull String name, boolean isSelected) {
        return addRenderableWidget(Checkbox.builder(Component.literal(name), this.font)
                .pos(this.leftPos + 40, y)
                .selected(isSelected)
                .onValueChange((btn, val) -> this.hasChanges = true)
                .build());
    }

    private void createSaveButton() {
        addRenderableWidget(Button.builder(Component.translatable(ModLang.GUI_SAVE), b -> this.saveAndSend())
                .bounds(this.leftPos + this.imageWidth - 68, this.topPos + this.imageHeight - 28, 60, 20)
                .build());
    }

    private void saveAndSend() {
        final SmallShopBlockEntity be = this.menu.getBlockEntity();
        final Direction facing = be.getBlockState().getValue(SmallShopBlock.FACING);

        // Update client-side state for responsiveness
        be.setShopNameClient(this.nameField.getValue());
        be.setEmitRedstoneClient(this.emitRedstoneCheckbox.selected());
        be.setModeClient(facing.getCounterClockWise(), this.menu.getMode(facing.getCounterClockWise()));
        be.setModeClient(facing.getClockWise(), this.menu.getMode(facing.getClockWise()));
        be.setModeClient(Direction.DOWN, this.menu.getMode(Direction.DOWN));
        be.setModeClient(facing.getOpposite(), this.menu.getMode(facing.getOpposite()));

        // Send settings packet
        NetworkHandler.sendToServer(new UpdateSettingsPacket(
                be.getBlockPos(),
                this.menu.getMode(facing.getCounterClockWise()),
                this.menu.getMode(facing.getClockWise()),
                this.menu.getMode(Direction.DOWN),
                this.menu.getMode(facing.getOpposite()),
                this.nameField.getValue(),
                this.emitRedstoneCheckbox.selected()
        ));

        // Send owners packet
        List<UUID> selectedOwnerIds = this.ownerCheckboxes.entrySet().stream()
                .filter(entry -> entry.getValue().selected())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        NetworkHandler.sendToServer(new UpdateOwnersPacket(be.getBlockPos(), selectedOwnerIds));

        this.hasChanges = false;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, Component.translatable(ModLang.GUI_SETTINGS_TITLE), 8, 6, 4210752, false);
        renderOwnerInfo(graphics, this.menu.getBlockEntity());

        if (!isOwner()) {
            Component info = Component.translatable(ModLang.GUI_SETTINGS_OWNER_ONLY);
            graphics.drawCenteredString(this.font, info, this.width / 2, this.height / 2, INFO_TEXT_COLOR);
        } else if (this.ownerCheckboxes.isEmpty()) {
            Component info = Component.translatable(ModLang.GUI_NO_PLAYERS_AVAILABLE);
            graphics.drawCenteredString(this.font, info, this.leftPos + this.imageWidth / 2, this.topPos + 100, INFO_TEXT_COLOR);
        }
    }

    @Override
    public void onClose() {
        // Revert any unsaved changes
        if (this.hasChanges) {
            this.menu.resetModes();
        }
        if (this.nameField != null && this.nameField.getValue().trim().isEmpty()) {
            this.menu.getBlockEntity().setShopNameClient(this.originalName);
        }
        super.onClose();
    }
}