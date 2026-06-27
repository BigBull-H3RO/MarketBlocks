package de.bigbull.marketblocks.feature.singleoffer.client.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.util.Mth;

import de.bigbull.marketblocks.core.config.Config;
import de.bigbull.marketblocks.feature.singleoffer.settings.AccessMode;
import de.bigbull.marketblocks.feature.singleoffer.settings.AccessSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Encapsulates owner list data, rendering and scrolling for the access settings
 * section.
 */
public class SingleOfferOwnerListPanel {
    public enum ListMode {
        OWNERS("gui.marketblocks.access.edit_owners"),
        ACCESS_LIST("gui.marketblocks.access.edit_access_list");

        private final String key;

        ListMode(String key) {
            this.key = key;
        }

        public Component title() {
            return Component.translatable(key);
        }

        public ListMode next() {
            return this == OWNERS ? ACCESS_LIST : OWNERS;
        }
    }

    private static final int OWNER_VISIBLE_ROWS = 4;
    private static final int OWNER_ROW_HEIGHT = 20;
    private static final int OWNER_PANEL_X_OFFSET = 7;
    private static final int OWNER_PANEL_BORDER = 1;
    private static final int OWNER_PANEL_WIDTH = 162;
    private static final int OWNER_PANEL_HEIGHT = 82;
    private static final int OWNER_SCROLLBAR_X_OFFSET = 156;
    private static final int SCROLLER_WIDTH = 12;
    private static final int SCROLLER_HEIGHT = 15;

    private final Map<UUID, Checkbox> ownerCheckboxes = new HashMap<>();
    private final List<UUID> ownerOrder = new ArrayList<>();
    private final Map<UUID, Boolean> ownerSelected = new HashMap<>();

    private float ownerScrollOffs = 0.0F;
    private boolean ownerScrolling = false;
    private int ownerStartIndex = 0;
    private int ownerListBaseY = 0;
    private boolean noPlayers;

    private SingleOfferShopScreen host;
    private Map<UUID, String> storedNames = Map.of();
    private Runnable onDirty = () -> {
    };
    private ListMode listMode = ListMode.OWNERS;

    public ListMode getListMode() {
        return listMode;
    }

    public void setListMode(ListMode mode) {
        if (this.listMode != mode) {
            this.listMode = mode;
            clearData();
        }
    }

    public Map<UUID, String> getStoredNames() {
        return storedNames;
    }

    private boolean listDisabled = false;

    public boolean isListDisabled() {
        return listDisabled;
    }

    public void prepareAndRender(SingleOfferShopScreen host,
            AccessSettings.Draft accessDraft,
            int listBaseY,
            boolean isPrimaryOwner,
            Runnable onDirty) {
        this.host = host;
        this.ownerListBaseY = listBaseY;
        this.onDirty = onDirty;
        this.ownerScrolling = false;
        this.listDisabled = this.listMode == ListMode.ACCESS_LIST
                && accessDraft.accessMode() == AccessMode.EVERYONE;

        if (!isPrimaryOwner) {
            this.noPlayers = false;
            clearData();
            return;
        }

        if (ownerOrder.isEmpty() && ownerSelected.isEmpty()) {
            populateOwnerData(accessDraft);
        }

        this.storedNames = listMode == ListMode.OWNERS ? accessDraft.additionalOwners() : accessDraft.accessList();
        this.ownerStartIndex = Mth.clamp(ownerStartIndex, 0, getOwnerOffscreenRows());
        renderOwnerWindow();
        this.noPlayers = ownerOrder.isEmpty();
    }

    public void renderBackground(GuiGraphics graphics,
            int leftPos,
            ResourceLocation panelTexture,
            ResourceLocation panelDisabledTexture,
            ResourceLocation scrollerSprite,
            ResourceLocation scrollerDisabledSprite) {
        graphics.blit(
                listDisabled ? panelDisabledTexture : panelTexture,
                leftPos + OWNER_PANEL_X_OFFSET,
                ownerListBaseY - OWNER_PANEL_BORDER,
                0,
                0,
                OWNER_PANEL_WIDTH,
                OWNER_PANEL_HEIGHT,
                OWNER_PANEL_WIDTH,
                OWNER_PANEL_HEIGHT);

        if (isOwnerScrollActive()) {
            int listHeight = OWNER_VISIBLE_ROWS * OWNER_ROW_HEIGHT;
            int barX = leftPos + OWNER_SCROLLBAR_X_OFFSET;
            int barY = ownerListBaseY;
            int barFull = Math.max(0, listHeight - SCROLLER_HEIGHT);
            int knobOffset = (int) (ownerScrollOffs * (float) barFull);
            graphics.blitSprite(scrollerSprite, barX, barY + knobOffset, SCROLLER_WIDTH, SCROLLER_HEIGHT);
        } else {
            int barX = leftPos + OWNER_SCROLLBAR_X_OFFSET;
            int barY = ownerListBaseY;
            graphics.blitSprite(scrollerDisabledSprite, barX, barY, SCROLLER_WIDTH, SCROLLER_HEIGHT);
        }
    }

    public boolean onMouseClicked(double mouseX, double mouseY, int leftPos) {
        if (!isOwnerScrollActive() || noPlayers) {
            return false;
        }

        int listHeight = OWNER_VISIBLE_ROWS * OWNER_ROW_HEIGHT;
        int barX = leftPos + OWNER_SCROLLBAR_X_OFFSET;
        int barY = ownerListBaseY;

        if (mouseX >= barX && mouseX < barX + SCROLLER_WIDTH && mouseY >= barY && mouseY < barY + listHeight) {
            ownerScrolling = true;
            return true;
        }
        return false;
    }

    public boolean onMouseDragged(double mouseY) {
        if (!ownerScrolling || !isOwnerScrollActive() || noPlayers) {
            return false;
        }

        int top = ownerListBaseY;
        int bottom = top + OWNER_VISIBLE_ROWS * OWNER_ROW_HEIGHT;

        ownerScrollOffs = ((float) mouseY - (float) top - (SCROLLER_HEIGHT / 2.0F))
                / ((float) (bottom - top) - (float) SCROLLER_HEIGHT);
        ownerScrollOffs = Mth.clamp(ownerScrollOffs, 0.0F, 1.0F);

        setOwnerScrollFromOffs();
        return true;
    }

    public void onMouseReleased() {
        ownerScrolling = false;
    }

    public boolean onMouseScrolled(double scrollY) {
        if (!isOwnerScrollActive() || noPlayers) {
            return false;
        }

        int offRows = getOwnerOffscreenRows();
        ownerScrollOffs = (float) ((double) ownerScrollOffs - scrollY / (double) offRows);
        ownerScrollOffs = Mth.clamp(ownerScrollOffs, 0.0F, 1.0F);
        setOwnerScrollFromOffs();
        return true;
    }

    public boolean noPlayers() {
        return noPlayers;
    }

    public int listBaseY() {
        return ownerListBaseY;
    }

    public List<UUID> collectSelectedOwners() {
        List<UUID> selected = new ArrayList<>();
        for (Map.Entry<UUID, Boolean> entry : ownerSelected.entrySet()) {
            if (Boolean.TRUE.equals(entry.getValue())) {
                selected.add(entry.getKey());
            }
        }
        return selected;
    }

    public String resolveName(UUID id, Map<UUID, String> stored) {
        var connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            PlayerInfo info = connection.getPlayerInfo(id);
            if (info != null) {
                return info.getProfile().getName();
            }
        }
        return stored.getOrDefault(id, "");
    }

    private void clearData() {
        ownerCheckboxes.clear();
        ownerOrder.clear();
        ownerSelected.clear();
        ownerStartIndex = 0;
        ownerScrollOffs = 0.0F;
    }

    private void populateOwnerData(AccessSettings.Draft accessDraft) {
        ownerOrder.clear();
        ownerSelected.clear();

        Map<UUID, String> current = new HashMap<>(
                listMode == ListMode.OWNERS ? accessDraft.additionalOwners() : accessDraft.accessList());

        if (Minecraft.getInstance().getConnection() != null) {
            Collection<PlayerInfo> players = Minecraft.getInstance().getConnection().getOnlinePlayers();
            for (PlayerInfo info : players) {
                UUID id = info.getProfile().getId();
                if (id.equals(accessDraft.ownerId())) {
                    continue;
                }
                ownerOrder.add(id);
                ownerSelected.put(id, current.containsKey(id));
                current.remove(id);
            }
        }

        for (Map.Entry<UUID, String> entry : current.entrySet()) {
            UUID id = entry.getKey();
            ownerOrder.add(id);
            ownerSelected.put(id, true);
        }

        ownerScrollOffs = 0.0F;
        ownerStartIndex = 0;
    }

    private boolean isOwnerScrollActive() {
        return !listDisabled && ownerOrder.size() > OWNER_VISIBLE_ROWS;
    }

    private int getOwnerOffscreenRows() {
        return Math.max(0, ownerOrder.size() - OWNER_VISIBLE_ROWS);
    }

    private void setOwnerScrollFromOffs() {
        int offRows = getOwnerOffscreenRows();
        ownerStartIndex = (int) ((double) (ownerScrollOffs * (float) offRows) + 0.5);
        renderOwnerWindow();
    }

    private void renderOwnerWindow() {
        if (host == null) {
            return;
        }

        ownerCheckboxes.values().forEach(host::removeSettingsWidget);
        ownerCheckboxes.clear();

        if (listDisabled) {
            return;
        }

        int maxOwners = Config.MAX_CO_OWNERS_PER_SHOP.get();
        boolean limitReached = listMode == ListMode.OWNERS && collectSelectedOwners().size() >= maxOwners;

        int visible = Math.min(OWNER_VISIBLE_ROWS, ownerOrder.size());
        for (int row = 0; row < visible; row++) {
            int idx = ownerStartIndex + row;
            if (idx >= ownerOrder.size()) {
                break;
            }

            UUID id = ownerOrder.get(idx);
            String name = resolveName(id, storedNames);
            boolean selected = ownerSelected.getOrDefault(id, false);

            Component nameComp = Component.literal(name);
            if (limitReached && !selected) {
                nameComp = nameComp.copy().withStyle(ChatFormatting.DARK_GRAY);
            }

            Checkbox cb = host.addSettingsWidget(Checkbox.builder(nameComp, host.settingsFont())
                    .pos(host.settingsLeftPos() + 8, ownerListBaseY + row * OWNER_ROW_HEIGHT)
                    .selected(selected)
                    .onValueChange((btn, value) -> {
                        if (limitReached && !selected && value) {
                            return;
                        }
                        ownerSelected.put(id, value);
                        if (listMode == ListMode.OWNERS) {
                            renderOwnerWindow();
                        }
                        onDirty.run();
                    })
                    .build());

            if (limitReached && !selected) {
                cb.active = false;
            }

            ownerCheckboxes.put(id, cb);
        }
    }
}
