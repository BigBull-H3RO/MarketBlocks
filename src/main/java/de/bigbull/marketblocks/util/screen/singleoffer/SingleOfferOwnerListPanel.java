package de.bigbull.marketblocks.util.screen.singleoffer;

import de.bigbull.marketblocks.shop.singleoffer.block.entity.SingleOfferShopBlockEntity;
import de.bigbull.marketblocks.shop.singleoffer.menu.SingleOfferShopMenu;
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
 * Encapsulates owner list data, rendering and scrolling for the access settings section.
 */
public class SingleOfferOwnerListPanel {
    private static final int OWNER_VISIBLE_ROWS = 2;
    private static final int OWNER_ROW_HEIGHT = 20;
    private static final int OWNER_PANEL_X_OFFSET = 7;
    private static final int OWNER_PANEL_BORDER = 1;
    private static final int OWNER_PANEL_WIDTH = 137;
    private static final int OWNER_PANEL_HEIGHT = 42;
    private static final int OWNER_SCROLLBAR_X_OFFSET = 131;
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
    private Runnable onDirty = () -> {};

    public void prepareAndRender(SingleOfferShopScreen host,
                                 SingleOfferShopMenu menu,
                                 SingleOfferShopBlockEntity be,
                                 int listBaseY,
                                 boolean isPrimaryOwner,
                                 Runnable onDirty) {
        this.host = host;
        this.ownerListBaseY = listBaseY;
        this.onDirty = onDirty;
        this.ownerScrolling = false;

        if (!isPrimaryOwner) {
            this.noPlayers = false;
            clearData();
            return;
        }

        if (ownerOrder.isEmpty() && ownerSelected.isEmpty()) {
            populateOwnerData(menu, be);
        }

        this.storedNames = menu.getAdditionalOwners();
        this.ownerStartIndex = net.minecraft.util.Mth.clamp(ownerStartIndex, 0, getOwnerOffscreenRows());
        renderOwnerWindow();
        this.noPlayers = ownerOrder.isEmpty();
    }

    public void renderBackground(GuiGraphics graphics,
                                 int leftPos,
                                 ResourceLocation panelTexture,
                                 ResourceLocation scrollerSprite,
                                 ResourceLocation scrollerDisabledSprite) {
        graphics.blit(
                panelTexture,
                leftPos + OWNER_PANEL_X_OFFSET,
                ownerListBaseY - OWNER_PANEL_BORDER,
                0,
                0,
                OWNER_PANEL_WIDTH,
                OWNER_PANEL_HEIGHT,
                OWNER_PANEL_WIDTH,
                OWNER_PANEL_HEIGHT
        );

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
        ownerScrollOffs = net.minecraft.util.Mth.clamp(ownerScrollOffs, 0.0F, 1.0F);

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
        float step = (float) scrollY / (float) Math.max(1, offRows);
        ownerScrollOffs = net.minecraft.util.Mth.clamp(ownerScrollOffs - step, 0.0F, 1.0F);
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

    private void populateOwnerData(SingleOfferShopMenu menu, SingleOfferShopBlockEntity be) {
        ownerOrder.clear();
        ownerSelected.clear();

        Map<UUID, String> current = new HashMap<>(menu.getAdditionalOwners());

        if (Minecraft.getInstance().getConnection() != null) {
            Collection<PlayerInfo> players = Minecraft.getInstance().getConnection().getOnlinePlayers();
            for (PlayerInfo info : players) {
                UUID id = info.getProfile().getId();
                if (id.equals(be.getOwnerId())) {
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
        return ownerOrder.size() > OWNER_VISIBLE_ROWS;
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

        int visible = Math.min(OWNER_VISIBLE_ROWS, ownerOrder.size());
        for (int row = 0; row < visible; row++) {
            int idx = ownerStartIndex + row;
            if (idx >= ownerOrder.size()) {
                break;
            }

            UUID id = ownerOrder.get(idx);
            String name = resolveName(id, storedNames);
            boolean selected = ownerSelected.getOrDefault(id, false);

            Checkbox cb = host.addSettingsWidget(Checkbox.builder(Component.literal(name), host.settingsFont())
                    .pos(host.settingsLeftPos() + 8, ownerListBaseY + row * OWNER_ROW_HEIGHT)
                    .selected(selected)
                    .onValueChange((btn, value) -> {
                        ownerSelected.put(id, value);
                        onDirty.run();
                    })
                    .build());

            ownerCheckboxes.put(id, cb);
        }
    }
}



