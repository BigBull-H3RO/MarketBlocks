package de.bigbull.marketblocks.feature.singleoffer.entity;

import de.bigbull.marketblocks.feature.singleoffer.SideMode;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

import java.util.EnumMap;

public class ShopSideManager {
    private final SingleOfferShopBlockEntity blockEntity;
    private final EnumMap<Direction, SideMode> sideModes = new EnumMap<>(Direction.class);

    public ShopSideManager(SingleOfferShopBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
        for (Direction dir : Direction.values()) {
            sideModes.put(dir, SideMode.DISABLED);
        }
    }

    public SideMode getMode(Direction dir) {
        return sideModes.getOrDefault(dir, SideMode.DISABLED);
    }

    public void setMode(Direction dir, SideMode mode, boolean sync) {
        SideMode oldMode = getMode(dir);
        sideModes.put(dir, mode);

        if (blockEntity.getLevel() != null && blockEntity.getLevel().isClientSide) return;

        blockEntity.setChanged();
        blockEntity.invalidateCapabilitiesAndNeighbor(dir);
        if (mode == SideMode.INPUT || mode == SideMode.OUTPUT) {
            blockEntity.lockAdjacentChest(dir);
        } else if (oldMode != SideMode.DISABLED) {
            blockEntity.unlockAdjacentChests();
        }
        if (sync) {
            blockEntity.sync();
            blockEntity.updateNeighborCache();
        }
    }

    public void save(CompoundTag tag) {
        CompoundTag modesTag = new CompoundTag();
        for (Direction dir : Direction.values()) {
            SideMode mode = getMode(dir);
            if (mode != SideMode.DISABLED) {
                modesTag.putString(dir.getName(), mode.name());
            }
        }
        if (!modesTag.isEmpty()) {
            tag.put("SideModes", modesTag);
        }
    }

    public void load(CompoundTag tag) {
        if (tag.contains("SideModes")) {
            CompoundTag modesTag = tag.getCompound("SideModes");
            for (Direction dir : Direction.values()) {
                if (modesTag.contains(dir.getName())) {
                    try {
                        sideModes.put(dir, SideMode.valueOf(modesTag.getString(dir.getName())));
                    } catch (IllegalArgumentException e) {
                        sideModes.put(dir, SideMode.DISABLED);
                    }
                } else {
                    sideModes.put(dir, SideMode.DISABLED);
                }
            }
        } else {
            for (Direction dir : Direction.values()) {
                sideModes.put(dir, SideMode.DISABLED);
            }
        }
    }
}
