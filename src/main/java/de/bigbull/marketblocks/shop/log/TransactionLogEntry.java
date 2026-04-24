package de.bigbull.marketblocks.shop.log;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Immutable entry for one completed transaction.
 *
 * @param epochSecond Unix timestamp in seconds
 * @param buyerUuid   buyer UUID (zero UUID when unknown)
 * @param buyerName   buyer name fallback for offline display
 * @param paidStacks  items paid by the buyer
 * @param boughtStacks items received by the buyer
 */
public record TransactionLogEntry(
        long epochSecond,
        UUID buyerUuid,
        String buyerName,
        List<ItemStack> paidStacks,
        List<ItemStack> boughtStacks
) {
    private static final String NBT_TIME = "Time";
    private static final String NBT_BUYER_UUID = "BuyerUuid";
    private static final String NBT_BUYER_NAME = "BuyerName";
    private static final String NBT_PAID_STACKS = "PaidStacks";
    private static final String NBT_BOUGHT_STACKS = "BoughtStacks";

    private static final UUID UNKNOWN_BUYER_UUID = new UUID(0L, 0L);
    private static final int MAX_NAME_LENGTH = 64;
    private static final int MAX_STACKS_PER_SIDE = 12;

    public static final Codec<TransactionLogEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf("epoch_second").forGetter(TransactionLogEntry::epochSecond),
            UUIDUtil.CODEC.fieldOf("buyer_uuid").forGetter(TransactionLogEntry::buyerUuid),
            Codec.STRING.fieldOf("buyer_name").forGetter(TransactionLogEntry::buyerName),
            ItemStack.OPTIONAL_CODEC.listOf().optionalFieldOf("paid_stacks", List.of()).forGetter(TransactionLogEntry::paidStacks),
            ItemStack.OPTIONAL_CODEC.listOf().optionalFieldOf("bought_stacks", List.of()).forGetter(TransactionLogEntry::boughtStacks)
    ).apply(instance, TransactionLogEntry::new));

    public TransactionLogEntry {
        epochSecond = Math.max(0L, epochSecond);
        buyerUuid = buyerUuid == null ? UNKNOWN_BUYER_UUID : buyerUuid;
        buyerName = sanitizeName(buyerName);
        paidStacks = sanitizeStacks(paidStacks);
        boughtStacks = sanitizeStacks(boughtStacks);
    }

    public static TransactionLogEntry now(UUID buyerUuid, String buyerName, List<ItemStack> paidStacks, List<ItemStack> boughtStacks) {
        return new TransactionLogEntry(Instant.now().getEpochSecond(), buyerUuid, buyerName, paidStacks, boughtStacks);
    }

    public CompoundTag toTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putLong(NBT_TIME, epochSecond);
        tag.putUUID(NBT_BUYER_UUID, buyerUuid);
        tag.putString(NBT_BUYER_NAME, buyerName);
        tag.put(NBT_PAID_STACKS, toStackListTag(paidStacks, registries));
        tag.put(NBT_BOUGHT_STACKS, toStackListTag(boughtStacks, registries));
        return tag;
    }

    public static TransactionLogEntry fromTag(CompoundTag tag, HolderLookup.Provider registries) {
        long time = Math.max(0L, tag.getLong(NBT_TIME));
        UUID uuid = tag.hasUUID(NBT_BUYER_UUID) ? tag.getUUID(NBT_BUYER_UUID) : UNKNOWN_BUYER_UUID;
        String name = tag.getString(NBT_BUYER_NAME);
        List<ItemStack> paid = fromStackListTag(tag.getList(NBT_PAID_STACKS, Tag.TAG_COMPOUND), registries);
        List<ItemStack> bought = fromStackListTag(tag.getList(NBT_BOUGHT_STACKS, Tag.TAG_COMPOUND), registries);
        return new TransactionLogEntry(time, uuid, name, paid, bought);
    }

    /**
     * Creates a copy of the stack and multiplies its count.
     */
    public static ItemStack scaleStack(ItemStack original, int multiplier) {
        if (original == null || original.isEmpty() || multiplier <= 0) {
            return ItemStack.EMPTY;
        }
        long scaledCount = (long) original.getCount() * multiplier;
        if (scaledCount <= 0L) {
            return ItemStack.EMPTY;
        }
        ItemStack copy = original.copy();
        copy.setCount((int) Math.min(Integer.MAX_VALUE, scaledCount));
        return copy;
    }

    private static String sanitizeName(String name) {
        String normalized = name == null ? "" : name.trim();
        if (normalized.length() > MAX_NAME_LENGTH) {
            return normalized.substring(0, MAX_NAME_LENGTH);
        }
        return normalized;
    }

    private static ListTag toStackListTag(List<ItemStack> stacks, HolderLookup.Provider registries) {
        ListTag listTag = new ListTag();
        for (ItemStack stack : sanitizeStacks(stacks)) {
            listTag.add(stack.save(registries));
        }
        return listTag;
    }

    private static List<ItemStack> fromStackListTag(ListTag listTag, HolderLookup.Provider registries) {
        if (listTag == null || listTag.isEmpty()) {
            return List.of();
        }

        List<ItemStack> loaded = new ArrayList<>(Math.min(listTag.size(), MAX_STACKS_PER_SIDE));
        for (int i = 0; i < listTag.size() && loaded.size() < MAX_STACKS_PER_SIDE; i++) {
            CompoundTag stackTag = listTag.getCompound(i);
            ItemStack stack = ItemStack.parseOptional(registries, stackTag);
            if (!stack.isEmpty() && stack.getCount() > 0) {
                loaded.add(stack);
            }
        }
        return loaded.isEmpty() ? List.of() : List.copyOf(loaded);
    }

    private static List<ItemStack> sanitizeStacks(List<ItemStack> stacks) {
        if (stacks == null || stacks.isEmpty()) {
            return List.of();
        }

        List<ItemStack> sanitized = new ArrayList<>(Math.min(stacks.size(), MAX_STACKS_PER_SIDE));
        for (ItemStack stack : stacks) {
            if (sanitized.size() >= MAX_STACKS_PER_SIDE) {
                break;
            }
            if (stack == null || stack.isEmpty() || stack.getCount() <= 0) {
                continue;
            }
            sanitized.add(stack.copy());
        }

        return sanitized.isEmpty() ? List.of() : List.copyOf(sanitized);
    }
}
