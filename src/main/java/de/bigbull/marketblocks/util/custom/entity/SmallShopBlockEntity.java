package de.bigbull.marketblocks.util.custom.entity;

import de.bigbull.marketblocks.util.RegistriesInit;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SmallShopBlockEntity extends BlockEntity implements MenuProvider {
    public SmallShopBlockEntity(BlockPos pos, BlockState state) {
        super(RegistriesInit.SMALL_SHOP_BLOCK_ENTITY.get(), pos, state);
    }

    public SmallShopBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public boolean isValidBlockState(BlockState p_353131_) {
        return super.isValidBlockState(p_353131_);
    }

    @Override
    public @Nullable Level getLevel() {
        return super.getLevel();
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
    }

    @Override
    public boolean hasLevel() {
        return super.hasLevel();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
    }

    @Override
    public void saveToItem(ItemStack stack, HolderLookup.Provider registries) {
        super.saveToItem(stack, registries);
    }

    @Override
    public void setChanged() {
        super.setChanged();
    }

    @Override
    public BlockPos getBlockPos() {
        return super.getBlockPos();
    }

    @Override
    public BlockState getBlockState() {
        return super.getBlockState();
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return super.getUpdatePacket();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return super.getUpdateTag(registries);
    }

    @Override
    public boolean isRemoved() {
        return super.isRemoved();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        return super.triggerEvent(id, type);
    }

    @Override
    public void fillCrashReportCategory(CrashReportCategory reportCategory) {
        super.fillCrashReportCategory(reportCategory);
    }

    @Override
    public boolean onlyOpCanSetNbt() {
        return super.onlyOpCanSetNbt();
    }

    @Override
    public BlockEntityType<?> getType() {
        return super.getType();
    }

    @Override
    public CompoundTag getPersistentData() {
        return super.getPersistentData();
    }

    @Override
    public void setBlockState(BlockState blockState) {
        super.setBlockState(blockState);
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        super.removeComponentsFromTag(tag);
    }

    @Override
    public DataComponentMap components() {
        return super.components();
    }

    @Override
    public void setComponents(DataComponentMap components) {
        super.setComponents(components);
    }

    @Override
    public Component getDisplayName() {
        return null;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return null;
    }
}