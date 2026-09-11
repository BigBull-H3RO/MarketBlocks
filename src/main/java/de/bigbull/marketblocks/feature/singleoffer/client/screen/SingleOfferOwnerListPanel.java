package de.bigbull.marketblocks.feature.singleoffer.client.screen;

import com.mojang.authlib.GameProfile;
import de.bigbull.marketblocks.client.gui.CompactCheckbox;
import de.bigbull.marketblocks.core.config.SingleOfferConfig;
import de.bigbull.marketblocks.feature.singleoffer.settings.AccessSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Encapsulates owner list data, rendering and scrolling for the access settings section.
 * Features a dark inset list frame, 8x8 player skin heads, and compact checkboxes.
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
    private static final int OWNER_ROW_HEIGHT = 14;

    private static final int INSET_X_OFFSET = 11;
    private static final int INSET_WIDTH = 154;
    private static final int INSET_HEIGHT = 58;

    private static final int ROW_X_OFFSET = 12;
    private static final int ROW_WIDTH = 143;
    private static final int CHECKBOX_X_OFFSET = 142;

    private static final int SCROLLER_TRACK_X_OFFSET = 157;
    private static final int SCROLLER_TRACK_WIDTH = 6;
    private static final int SCROLLER_TRACK_HEIGHT = 56;
    private static final int SCROLLER_KNOB_HEIGHT = 16;

    private final Map<UUID, CompactCheckbox> ownerCheckboxes = new HashMap<>();
    private final List<UUID> ownerOrder = new ArrayList<>();
    private final Map<UUID, Boolean> ownerSelected = new HashMap<>();

    private float ownerScrollOffs = 0.0F;
    private boolean ownerScrolling = false;
    private int ownerStartIndex = 0;
    private int ownerListBaseY = 0;
    private boolean noPlayers = false;
    private boolean listDisabled = false;

    private SingleOfferShopScreen host;
    private Map<UUID, String> storedNames = Map.of();
    private Runnable onDirty = () -> {};
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
        this.listDisabled = !isPrimaryOwner;

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
            Font font,
            int leftPos,
            int topPos,
            int mouseX,
            int mouseY) {
        int insetX = leftPos + INSET_X_OFFSET;
        int insetY = topPos + 76;

        // Dark inset container background & 1px border
        graphics.fill(insetX, insetY, insetX + INSET_WIDTH, insetY + INSET_HEIGHT, 0xFF1B1B1B);
        graphics.fill(insetX, insetY, insetX + INSET_WIDTH, insetY + 1, 0xFF373737);
        graphics.fill(insetX, insetY + INSET_HEIGHT - 1, insetX + INSET_WIDTH, insetY + INSET_HEIGHT, 0xFF373737);
        graphics.fill(insetX, insetY, insetX + 1, insetY + INSET_HEIGHT, 0xFF373737);
        graphics.fill(insetX + INSET_WIDTH - 1, insetY, insetX + INSET_WIDTH, insetY + INSET_HEIGHT, 0xFF373737);

        if (listDisabled) {
            Component info = Component.translatable("gui.marketblocks.access.primary_owner_only");
            int textW = font.width(info);
            graphics.drawString(font, info, insetX + (INSET_WIDTH - textW) / 2, insetY + (INSET_HEIGHT - font.lineHeight) / 2 + 1, 0x808080, false);
            return;
        }

        if (noPlayers) {
            Component info = Component.translatable("gui.marketblocks.no_players_available");
            int textW = font.width(info);
            graphics.drawString(font, info, insetX + (INSET_WIDTH - textW) / 2, insetY + (INSET_HEIGHT - font.lineHeight) / 2 + 1, 0x808080, false);
            return;
        }

        int maxOwners = SingleOfferConfig.MAX_CO_OWNERS_PER_SHOP.get();
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

            int rx = leftPos + ROW_X_OFFSET;
            int ry = ownerListBaseY + row * OWNER_ROW_HEIGHT;
            int rw = ROW_WIDTH;
            int rh = OWNER_ROW_HEIGHT;

            boolean rowHovered = mouseX >= rx && mouseX < rx + rw && mouseY >= ry && mouseY < ry + rh;
            int rowBg = rowHovered ? 0xFF2A2A2A : ((row % 2 == 1) ? 0xFF242424 : 0xFF1E1E1E);
            graphics.fill(rx, ry, rx + rw, ry + rh, rowBg);

            // 1. Player Head (8x8)
            int headX = rx + 3;
            int headY = ry + 3;
            renderPlayerHead(graphics, id, name, headX, headY);

            // 2. Player Name
            int textX = headX + 8 + 4;
            int textY = ry + 3;
            int maxNameW = 100;
            String displayName = font.width(name) > maxNameW ? font.plainSubstrByWidth(name, maxNameW - font.width("...")) + "..." : name;

            int textColor;
            if (limitReached && !selected) {
                textColor = 0x666666;
            } else if (selected) {
                textColor = 0xFFFFFF;
            } else {
                textColor = 0xCCCCCC;
            }
            graphics.drawString(font, displayName, textX, textY, textColor, false);
        }

        // 3. Scrollbar
        int trackX = leftPos + SCROLLER_TRACK_X_OFFSET;
        int trackY = topPos + 77;
        graphics.fill(trackX, trackY, trackX + SCROLLER_TRACK_WIDTH, trackY + SCROLLER_TRACK_HEIGHT, 0xFF202020);

        if (isOwnerScrollActive()) {
            int barFull = Math.max(0, SCROLLER_TRACK_HEIGHT - SCROLLER_KNOB_HEIGHT);
            int knobY = trackY + (int) (ownerScrollOffs * (float) barFull);
            graphics.fill(trackX, knobY, trackX + SCROLLER_TRACK_WIDTH, knobY + SCROLLER_KNOB_HEIGHT, 0xFF8B8B8B);
            graphics.fill(trackX, knobY, trackX + SCROLLER_TRACK_WIDTH, knobY + 1, 0xFFB0B0B0);
            graphics.fill(trackX, knobY + SCROLLER_KNOB_HEIGHT - 1, trackX + SCROLLER_TRACK_WIDTH, knobY + SCROLLER_KNOB_HEIGHT, 0xFF373737);
        } else {
            graphics.fill(trackX, trackY, trackX + SCROLLER_TRACK_WIDTH, trackY + SCROLLER_KNOB_HEIGHT, 0xFF353535);
        }
    }

    private void renderPlayerHead(GuiGraphics graphics, UUID id, String name, int x, int y) {
        Minecraft client = Minecraft.getInstance();
        GameProfile profile = new GameProfile(id, name);
        ResourceLocation skinTexture = client.getSkinManager().getInsecureSkin(profile).texture();

        // Base head layer (8x8 at u=8, v=8, src 8x8, tex 64x64)
        graphics.blit(skinTexture, x, y, 8, 8, 8.0F, 8.0F, 8, 8, 64, 64);
        // Outer hat layer (8x8 at u=40, v=8, src 8x8, tex 64x64)
        graphics.blit(skinTexture, x, y, 8, 8, 40.0F, 8.0F, 8, 8, 64, 64);
    }

    public boolean onMouseClicked(double mouseX, double mouseY, int leftPos) {
        if (listDisabled || noPlayers) {
            return false;
        }

        int trackX = leftPos + SCROLLER_TRACK_X_OFFSET;
        int trackY = ownerListBaseY;

        if (isOwnerScrollActive() && mouseX >= trackX && mouseX <= trackX + SCROLLER_TRACK_WIDTH
                && mouseY >= trackY && mouseY <= trackY + SCROLLER_TRACK_HEIGHT) {
            ownerScrolling = true;
            int barFull = Math.max(1, SCROLLER_TRACK_HEIGHT - SCROLLER_KNOB_HEIGHT);
            float rel = (float) (mouseY - trackY - SCROLLER_KNOB_HEIGHT / 2.0F) / (float) barFull;
            ownerScrollOffs = Mth.clamp(rel, 0.0F, 1.0F);
            setOwnerScrollFromOffs();
            return true;
        }

        // Clicking anywhere on a player row toggles the selection
        int rx = leftPos + ROW_X_OFFSET;
        int rw = ROW_WIDTH - 14; // area before checkbox
        int visible = Math.min(OWNER_VISIBLE_ROWS, ownerOrder.size());

        for (int row = 0; row < visible; row++) {
            int ry = ownerListBaseY + row * OWNER_ROW_HEIGHT;
            if (mouseX >= rx && mouseX < rx + rw && mouseY >= ry && mouseY < ry + OWNER_ROW_HEIGHT) {
                int idx = ownerStartIndex + row;
                if (idx < ownerOrder.size()) {
                    UUID id = ownerOrder.get(idx);
                    boolean current = ownerSelected.getOrDefault(id, false);
                    int maxOwners = SingleOfferConfig.MAX_CO_OWNERS_PER_SHOP.get();
                    boolean limitReached = listMode == ListMode.OWNERS && collectSelectedOwners().size() >= maxOwners;

                    if (limitReached && !current) {
                        return false;
                    }

                    boolean nextVal = !current;
                    ownerSelected.put(id, nextVal);
                    if (listMode == ListMode.OWNERS) {
                        renderOwnerWindow();
                    } else {
                        CompactCheckbox cb = ownerCheckboxes.get(id);
                        if (cb != null) {
                            cb.setSelected(nextVal);
                        }
                    }
                    onDirty.run();
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    return true;
                }
            }
        }

        return false;
    }

    public boolean onMouseDragged(double mouseY) {
        if (!ownerScrolling || !isOwnerScrollActive()) {
            return false;
        }

        int barFull = Math.max(1, SCROLLER_TRACK_HEIGHT - SCROLLER_KNOB_HEIGHT);
        float rel = (float) (mouseY - ownerListBaseY - SCROLLER_KNOB_HEIGHT / 2.0F) / (float) barFull;
        ownerScrollOffs = Mth.clamp(rel, 0.0F, 1.0F);
        setOwnerScrollFromOffs();
        return true;
    }

    public boolean onMouseReleased() {
        if (ownerScrolling) {
            ownerScrolling = false;
            return true;
        }
        return false;
    }

    public boolean onMouseScrolled(double mouseX, double mouseY, double scrollY, int leftPos) {
        if (!isOwnerScrollActive() || noPlayers || listDisabled) {
            return false;
        }

        int listX = leftPos + INSET_X_OFFSET;
        int listY = ownerListBaseY - 1;
        if (mouseX >= listX && mouseX <= listX + INSET_WIDTH
                && mouseY >= listY && mouseY <= listY + INSET_HEIGHT) {
            int offRows = getOwnerOffscreenRows();
            if (offRows > 0) {
                ownerScrollOffs = Mth.clamp(ownerScrollOffs - (float) (scrollY / (double) offRows), 0.0F, 1.0F);
                setOwnerScrollFromOffs();
                return true;
            }
        }
        return false;
    }

    public void flushToDraft(AccessSettings.Draft accessDraft) {
        if (listDisabled) {
            return;
        }

        Map<UUID, String> updated = new HashMap<>();
        for (Map.Entry<UUID, Boolean> entry : ownerSelected.entrySet()) {
            if (Boolean.TRUE.equals(entry.getValue())) {
                UUID id = entry.getKey();
                updated.put(id, resolveName(id, storedNames));
            }
        }

        if (listMode == ListMode.OWNERS) {
            accessDraft.setAdditionalOwners(updated);
        } else {
            accessDraft.setAccessList(updated);
        }
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

    public void clearData() {
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

        int maxOwners = SingleOfferConfig.MAX_CO_OWNERS_PER_SHOP.get();
        boolean limitReached = listMode == ListMode.OWNERS && collectSelectedOwners().size() >= maxOwners;

        int visible = Math.min(OWNER_VISIBLE_ROWS, ownerOrder.size());
        for (int row = 0; row < visible; row++) {
            int idx = ownerStartIndex + row;
            if (idx >= ownerOrder.size()) {
                break;
            }

            UUID id = ownerOrder.get(idx);
            boolean selected = ownerSelected.getOrDefault(id, false);

            CompactCheckbox cb = host.addSettingsWidget(new CompactCheckbox(
                    host.settingsLeftPos() + CHECKBOX_X_OFFSET,
                    ownerListBaseY + row * OWNER_ROW_HEIGHT + 1,
                    Component.empty(),
                    host.settingsFont(),
                    selected,
                    12,
                    (checkbox, value) -> {
                        if (limitReached && !selected && value) {
                            return;
                        }
                        ownerSelected.put(id, value);
                        if (listMode == ListMode.OWNERS) {
                            renderOwnerWindow();
                        }
                        onDirty.run();
                    }));

            if (limitReached && !selected) {
                cb.active = false;
            }

            ownerCheckboxes.put(id, cb);
        }
    }
}
