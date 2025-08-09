package de.bigbull.marketblocks.util.custom.entity;

import de.bigbull.marketblocks.util.RegistriesInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.UUID;

public class SmallShopBlockEntity extends BlockEntity {
    // Zwei getrennte Lagerbereiche
    private ItemStackHandler payments = new ItemStackHandler(12); // Erhaltene Zahlungen
    private ItemStackHandler stock = new ItemStackHandler(12);    // Verkaufsware

    // Angebots-Templates
    private ItemStack saleItem = ItemStack.EMPTY;   // Was verkauft wird
    private ItemStack payItemA = ItemStack.EMPTY;  // Erste Bezahlung
    private ItemStack payItemB = ItemStack.EMPTY;  // Zweite Bezahlung (optional)

    // Display-Entities für 3D-Anzeige
    private ItemEntity displayItem;      // Verkaufsitem über dem Block
    private ItemEntity payDisplayItemA; // Bezahlitem A vor dem Block
    private ItemEntity payDisplayItemB; // Bezahlitem B vor dem Block

    private UUID owner;
    private boolean needsDisplayUpdate = false;

    public SmallShopBlockEntity(BlockPos pos, BlockState state) {
        super(RegistriesInit.SMALL_SHOP_BLOCK_ENTITY.get(), pos, state);

        // Setup change listeners für automatische Updates
        setupChangeListeners();
    }

    private void setupChangeListeners() {
        // Listener für Stock-Änderungen
        this.stock = new ItemStackHandler(12) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                if (level != null && !level.isClientSide) {
                    sync();
                }
            }
        };

        // Listener für Payment-Änderungen
        this.payments = new ItemStackHandler(12) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                if (level != null && !level.isClientSide) {
                    sync();
                }
            }
        };
    }

    // Getter/Setter mit verbesserter Synchronisation
    public ItemStackHandler getPayments() {
        return payments;
    }

    public ItemStackHandler getStock() {
        return stock;
    }

    public ItemStack getSaleItem() {
        return saleItem;
    }

    public void setSaleItem(ItemStack stack) {
        this.saleItem = stack.copy();
        this.needsDisplayUpdate = true;
        setChanged();
        sync();
    }

    public ItemStack getPayItemA() {
        return payItemA;
    }

    public void setPayItemA(ItemStack stack) {
        this.payItemA = stack.copy();
        this.needsDisplayUpdate = true;
        setChanged();
        sync();
    }

    public ItemStack getPayItemB() {
        return payItemB;
    }

    public void setPayItemB(ItemStack stack) {
        this.payItemB = stack.copy();
        this.needsDisplayUpdate = true;
        setChanged();
        sync();
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        setChanged();
    }

    // Display-Entity Management
    public void setDisplayItem(ItemEntity item) {
        this.displayItem = item;
    }

    public ItemEntity getDisplayItem() {
        return displayItem;
    }

    public void discardDisplayItem() {
        if (displayItem != null && displayItem.isAlive()) {
            displayItem.discard();
            displayItem = null;
        }
    }

    public void setPayDisplayItemA(ItemEntity item) {
        this.payDisplayItemA = item;
    }

    public void discardPayDisplayItemA() {
        if (payDisplayItemA != null && payDisplayItemA.isAlive()) {
            payDisplayItemA.discard();
            payDisplayItemA = null;
        }
    }

    public void setPayDisplayItemB(ItemEntity item) {
        this.payDisplayItemB = item;
    }

    public void discardPayDisplayItemB() {
        if (payDisplayItemB != null && payDisplayItemB.isAlive()) {
            payDisplayItemB.discard();
            payDisplayItemB = null;
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            // Verzögerte Display-Update nach dem Laden
            level.scheduleTick(getBlockPos(), getBlockState().getBlock(), 5);
        }
    }

    public void tick() {
        if (level == null || level.isClientSide) {
            return;
        }

        if (needsDisplayUpdate) {
            updateDisplayItems();
            needsDisplayUpdate = false;
        }

        // Prüfe Display-Entities auf Validität
        validateDisplayEntities();
    }

    private void validateDisplayEntities() {
        if (displayItem != null && !displayItem.isAlive()) {
            displayItem = null;
            needsDisplayUpdate = true;
        }
        if (payDisplayItemA != null && !payDisplayItemA.isAlive()) {
            payDisplayItemA = null;
            needsDisplayUpdate = true;
        }
        if (payDisplayItemB != null && !payDisplayItemB.isAlive()) {
            payDisplayItemB = null;
            needsDisplayUpdate = true;
        }
    }

    private void updateDisplayItems() {
        if (level == null || level.isClientSide) {
            return;
        }

        // Entferne alte Display-Items
        discardDisplayItem();
        discardPayDisplayItemA();
        discardPayDisplayItemB();

        if (saleItem.isEmpty()) {
            return;
        }

        BlockPos pos = getBlockPos();
        Direction facing = getBlockState().getValue(de.bigbull.marketblocks.util.custom.block.SmallShopBlock.FACING);

        // Hauptitem über dem Block
        ItemEntity sale = createDisplayEntity(saleItem, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5);
        level.addFreshEntity(sale);
        setDisplayItem(sale);

        // Bezahlitems vor dem Block
        double frontX = pos.getX() + 0.5 + facing.getStepX() * 0.7;
        double frontZ = pos.getZ() + 0.5 + facing.getStepZ() * 0.7;

        if (!payItemA.isEmpty()) {
            ItemEntity payA = createDisplayEntity(payItemA, frontX, pos.getY() + 1.0, frontZ);
            level.addFreshEntity(payA);
            setPayDisplayItemA(payA);
        }

        if (!payItemB.isEmpty()) {
            Direction side = facing.getClockWise();
            double sideX = frontX + side.getStepX() * 0.25;
            double sideZ = frontZ + side.getStepZ() * 0.25;
            ItemEntity payB = createDisplayEntity(payItemB, sideX, pos.getY() + 1.0, sideZ);
            level.addFreshEntity(payB);
            setPayDisplayItemB(payB);
        }
    }

    private ItemEntity createDisplayEntity(ItemStack stack, double x, double y, double z) {
        ItemEntity entity = new ItemEntity(level, x, y, z, stack.copy());
        entity.setNoGravity(true);
        entity.setNeverPickUp();
        entity.setUnlimitedLifetime();
        entity.setDeltaMovement(0, 0, 0);
        return entity;
    }

    /**
     * Verbesserte Überprüfung der Handelsfähigkeit
     */
    public boolean canTrade(Container paymentContainer) {
        if (saleItem.isEmpty() || payItemA.isEmpty()) {
            return false;
        }

        if (!hasStock()) {
            return false;
        }

        // Prüfe Bezahlung in den Slots
        ItemStack slotA = paymentContainer.getItem(25);
        ItemStack slotB = paymentContainer.getItem(26);

        if (!ItemStack.isSameItemSameComponents(slotA, payItemA) || slotA.getCount() < payItemA.getCount()) {
            return false;
        }

        if (!payItemB.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(slotB, payItemB) || slotB.getCount() < payItemB.getCount()) {
                return false;
            }
        }

        // Simuliere Einfügen der Bezahlung ins Zahlungs-Lager
        return canStorePayment(payItemA) && (payItemB.isEmpty() || canStorePayment(payItemB));
    }

    private boolean canStorePayment(ItemStack payment) {
        ItemStack simulate = payment.copy();
        for (int i = 0; i < payments.getSlots() && !simulate.isEmpty(); i++) {
            simulate = payments.insertItem(i, simulate, true);
        }
        return simulate.isEmpty();
    }

    /**
     * Verbesserte Handelsausführung
     */
    public boolean performTrade(Player buyer, Container paymentContainer) {
        if (!canTrade(paymentContainer)) {
            return false;
        }

        // Entferne Bezahlung aus Slots
        ItemStack slotA = paymentContainer.getItem(25);
        ItemStack slotB = paymentContainer.getItem(26);

        slotA.shrink(payItemA.getCount());
        if (slotA.isEmpty()) {
            paymentContainer.setItem(25, ItemStack.EMPTY);
        }

        if (!payItemB.isEmpty()) {
            slotB.shrink(payItemB.getCount());
            if (slotB.isEmpty()) {
                paymentContainer.setItem(26, ItemStack.EMPTY);
            }
        }

        // Füge Bezahlung zum Owner-Lager hinzu
        addPaymentToStorage(payItemA.copy());
        if (!payItemB.isEmpty()) {
            addPaymentToStorage(payItemB.copy());
        }

        // Entferne Verkaufsware aus dem Stock
        removeFromStock(saleItem);

        // Gib Verkaufsware an Käufer
        ItemStack result = saleItem.copy();
        if (!buyer.getInventory().add(result)) {
            buyer.drop(result, false);
        }

        setChanged();
        sync();
        return true;
    }

    private void addPaymentToStorage(ItemStack payment) {
        for (int i = 0; i < payments.getSlots(); i++) {
            payment = payments.insertItem(i, payment, false);
            if (payment.isEmpty()) {
                break;
            }
        }
    }

    private void removeFromStock(ItemStack toRemove) {
        int remaining = toRemove.getCount();
        for (int i = 0; i < stock.getSlots() && remaining > 0; i++) {
            ItemStack stack = stock.getStackInSlot(i);
            if (ItemStack.isSameItemSameComponents(stack, toRemove)) {
                int removed = Math.min(stack.getCount(), remaining);
                stack.shrink(removed);
                stock.setStackInSlot(i, stack.isEmpty() ? ItemStack.EMPTY : stack);
                remaining -= removed;
            }
        }
    }

    /**
     * Verbesserte Stock-Überprüfung
     */
    public boolean hasStock() {
        return hasStock(1);
    }

    public boolean hasStock(int requiredAmount) {
        if (saleItem.isEmpty()) {
            return false;
        }

        int totalStock = 0;
        int requiredPerTrade = saleItem.getCount() * requiredAmount;

        for (int i = 0; i < stock.getSlots(); i++) {
            ItemStack stack = stock.getStackInSlot(i);
            if (ItemStack.isSameItemSameComponents(stack, saleItem)) {
                totalStock += stack.getCount();
                if (totalStock >= requiredPerTrade) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Berechnet maximale Anzahl möglicher Trades basierend auf Stock
     */
    public int getMaxTradesFromStock() {
        if (saleItem.isEmpty()) {
            return 0;
        }

        int totalStock = 0;
        for (int i = 0; i < stock.getSlots(); i++) {
            ItemStack stack = stock.getStackInSlot(i);
            if (ItemStack.isSameItemSameComponents(stack, saleItem)) {
                totalStock += stack.getCount();
            }
        }

        return totalStock / saleItem.getCount();
    }

    /**
     * Verbesserte Synchronisation
     */
    private void sync() {
        if (level != null && !level.isClientSide) {
            setChanged();
            BlockState state = getBlockState();
            level.sendBlockUpdated(getBlockPos(), state, state, 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);

        tag.put("Payments", payments.serializeNBT(provider));
        tag.put("Stock", stock.serializeNBT(provider));

        if (!saleItem.isEmpty()) {
            tag.put("SaleItem", saleItem.save(provider));
        }
        if (!payItemA.isEmpty()) {
            tag.put("PayItemA", payItemA.save(provider));
        }
        if (!payItemB.isEmpty()) {
            tag.put("PayItemB", payItemB.save(provider));
        }
        if (owner != null) {
            tag.putUUID("Owner", owner);
        }

        tag.putBoolean("NeedsDisplayUpdate", needsDisplayUpdate);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        payments.deserializeNBT(registries, tag.getCompound("Payments"));
        stock.deserializeNBT(registries, tag.getCompound("Stock"));

        saleItem = tag.contains("SaleItem") ?
                ItemStack.parseOptional(registries, tag.getCompound("SaleItem")) :
                ItemStack.EMPTY;
        payItemA = tag.contains("PayItemA") ?
                ItemStack.parseOptional(registries, tag.getCompound("PayItemA")) :
                ItemStack.EMPTY;
        payItemB = tag.contains("PayItemB") ?
                ItemStack.parseOptional(registries, tag.getCompound("PayItemB")) :
                ItemStack.EMPTY;

        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        needsDisplayUpdate = tag.getBoolean("NeedsDisplayUpdate");
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, provider);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        loadAdditional(tag, provider);
        if (needsDisplayUpdate) {
            updateDisplayItems();
            needsDisplayUpdate = false;
        }
    }

    /**
     * Prüft ob ein bestimmter Spieler Zugriff auf Owner-Funktionen hat
     */
    public boolean hasOwnerAccess(Player player) {
        return owner != null && owner.equals(player.getUUID());
    }

    /**
     * Erweiterte Validierung für Handelsfähigkeit
     */
    public TradeResult validateTrade(ItemStack paymentA, ItemStack paymentB) {
        if (saleItem.isEmpty() || payItemA.isEmpty()) {
            return TradeResult.NO_OFFER;
        }

        if (!hasStock()) {
            return TradeResult.NO_STOCK;
        }

        if (!ItemStack.isSameItemSameComponents(paymentA, payItemA) ||
                paymentA.getCount() < payItemA.getCount()) {
            return TradeResult.INSUFFICIENT_PAYMENT_A;
        }

        if (!payItemB.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(paymentB, payItemB) ||
                    paymentB.getCount() < payItemB.getCount()) {
                return TradeResult.INSUFFICIENT_PAYMENT_B;
            }
        }

        if (!canStorePayment(payItemA) || (!payItemB.isEmpty() && !canStorePayment(payItemB))) {
            return TradeResult.PAYMENT_STORAGE_FULL;
        }

        return TradeResult.SUCCESS;
    }

    /**
     * Berechnet die optimale Anzahl von Trades basierend auf verfügbaren Ressourcen
     */
    public int calculateOptimalTrades(ItemStack availablePayA, ItemStack availablePayB) {
        if (saleItem.isEmpty() || payItemA.isEmpty()) {
            return 0;
        }

        int maxFromPayA = availablePayA.getCount() / payItemA.getCount();
        int maxFromPayB = payItemB.isEmpty() ? Integer.MAX_VALUE :
                availablePayB.getCount() / payItemB.getCount();
        int maxFromStock = getMaxTradesFromStock();

        return Math.min(Math.min(maxFromPayA, maxFromPayB), maxFromStock);
    }

    /**
     * Automatische Inventar-Optimierung für Owner
     */
    public void optimizeInventory() {
        if (saleItem.isEmpty()) {
            return;
        }

        // Konsolidiere gleiche Items im Stock
        consolidateItems(stock, saleItem);

        // Konsolidiere Zahlungen
        if (!payItemA.isEmpty()) {
            consolidateItems(payments, payItemA);
        }
        if (!payItemB.isEmpty()) {
            consolidateItems(payments, payItemB);
        }

        setChanged();
    }

    private void consolidateItems(ItemStackHandler handler, ItemStack targetItem) {
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (ItemStack.isSameItemSameComponents(stack, targetItem) && stack.getCount() < stack.getMaxStackSize()) {

                // Suche nach anderen Stacks zum Zusammenfassen
                for (int j = i + 1; j < handler.getSlots(); j++) {
                    ItemStack other = handler.getStackInSlot(j);
                    if (ItemStack.isSameItemSameComponents(other, targetItem)) {
                        int space = stack.getMaxStackSize() - stack.getCount();
                        int toMove = Math.min(space, other.getCount());

                        if (toMove > 0) {
                            stack.grow(toMove);
                            other.shrink(toMove);
                            handler.setStackInSlot(i, stack);
                            handler.setStackInSlot(j, other.isEmpty() ? ItemStack.EMPTY : other);

                            if (stack.getCount() >= stack.getMaxStackSize()) {
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Debug-Information für Entwicklung
     */
    public String getDebugInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Owner: ").append(owner != null ? owner.toString().substring(0, 8) : "None").append("\n");
        info.append("Sale Item: ").append(saleItem.isEmpty() ? "None" : saleItem.getDisplayName().getString()).append("\n");
        info.append("Pay A: ").append(payItemA.isEmpty() ? "None" : payItemA.getDisplayName().getString()).append("\n");
        info.append("Pay B: ").append(payItemB.isEmpty() ? "None" : payItemB.getDisplayName().getString()).append("\n");
        info.append("Stock Available: ").append(hasStock()).append("\n");
        info.append("Max Trades: ").append(getMaxTradesFromStock()).append("\n");
        return info.toString();
    }

    /**
     * Enum für Trade-Validierungsergebnisse
     */
    public enum TradeResult {
        SUCCESS,
        NO_OFFER,
        NO_STOCK,
        INSUFFICIENT_PAYMENT_A,
        INSUFFICIENT_PAYMENT_B,
        PAYMENT_STORAGE_FULL
    }
}