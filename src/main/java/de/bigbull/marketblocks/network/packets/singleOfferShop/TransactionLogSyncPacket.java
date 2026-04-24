package de.bigbull.marketblocks.network.packets.singleOfferShop;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.shop.log.TransactionLogEntry;
import de.bigbull.marketblocks.shop.singleoffer.menu.SingleOfferShopMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Dedicated server->client sync packet for transaction logs.
 * This data is synced only on demand and never via BlockEntity update tags.
 */
public record TransactionLogSyncPacket(BlockPos pos, CompoundTag payload) implements CustomPacketPayload {
    private static final String NBT_ENTRIES = "Entries";
    private static final int MAX_SYNC_ENTRIES = 200;

    public static final Type<TransactionLogSyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "single_offer_transaction_log_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TransactionLogSyncPacket> CODEC = new StreamCodec<>() {
        @Override
        public TransactionLogSyncPacket decode(RegistryFriendlyByteBuf buf) {
            BlockPos pos = BlockPos.STREAM_CODEC.decode(buf);
            CompoundTag payload = ByteBufCodecs.TRUSTED_COMPOUND_TAG.decode(buf);
            return new TransactionLogSyncPacket(pos, payload == null ? new CompoundTag() : payload);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, TransactionLogSyncPacket value) {
            BlockPos.STREAM_CODEC.encode(buf, value.pos());
            ByteBufCodecs.TRUSTED_COMPOUND_TAG.encode(buf, value.payload() == null ? new CompoundTag() : value.payload());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static TransactionLogSyncPacket fromEntries(BlockPos pos, List<TransactionLogEntry> entries, HolderLookup.Provider registries) {
        CompoundTag payload = new CompoundTag();
        ListTag listTag = new ListTag();

        int cappedSize = Math.min(entries == null ? 0 : entries.size(), MAX_SYNC_ENTRIES);
        for (int i = 0; i < cappedSize; i++) {
            TransactionLogEntry entry = entries.get(i);
            if (entry != null) {
                listTag.add(entry.toTag(registries));
            }
        }

        payload.put(NBT_ENTRIES, listTag);
        return new TransactionLogSyncPacket(pos, payload);
    }

    public List<TransactionLogEntry> decodeEntries(HolderLookup.Provider registries) {
        if (payload == null || !payload.contains(NBT_ENTRIES, Tag.TAG_LIST)) {
            return List.of();
        }

        ListTag listTag = payload.getList(NBT_ENTRIES, Tag.TAG_COMPOUND);
        if (listTag.isEmpty()) {
            return List.of();
        }

        List<TransactionLogEntry> entries = new ArrayList<>(Math.min(listTag.size(), MAX_SYNC_ENTRIES));
        for (int i = 0; i < listTag.size() && entries.size() < MAX_SYNC_ENTRIES; i++) {
            CompoundTag entryTag = listTag.getCompound(i);
            entries.add(TransactionLogEntry.fromTag(entryTag, registries));
        }
        return List.copyOf(entries);
    }

    public static void handle(TransactionLogSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() == null || context.player().level() == null) {
                return;
            }

            if (!(context.player().containerMenu instanceof SingleOfferShopMenu menu)) {
                return;
            }

            if (!menu.getBlockEntity().getBlockPos().equals(packet.pos())) {
                return;
            }

            menu.setTransactionLogEntries(packet.decodeEntries(context.player().level().registryAccess()));
        });
    }
}
