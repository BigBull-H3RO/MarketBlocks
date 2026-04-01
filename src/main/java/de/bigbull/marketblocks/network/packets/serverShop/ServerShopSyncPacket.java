package de.bigbull.marketblocks.network.packets.serverShop;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.menu.ServerShopMenu;
import de.bigbull.marketblocks.shop.server.ServerShopClientState;
import de.bigbull.marketblocks.shop.server.ServerShopOfferViewState;
import de.bigbull.marketblocks.shop.server.ServerShopSerialization;
import net.minecraft.client.Minecraft;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Synchronisiert den vollständigen Datenstand des Server-Shops an den Client.
 */
public record ServerShopSyncPacket(CompoundTag payload, CompoundTag offerViewStates, boolean canEdit, boolean globalEditModeEnabled) implements CustomPacketPayload {
    private static final String ENTRIES_KEY = "entries";
    private static final String OFFER_ID_KEY = "offer_id";
    private static final String STATE_KEY = "state";

    public static final Type<ServerShopSyncPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "server_shop_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerShopSyncPacket> CODEC = new StreamCodec<>() {
        @Override
        public ServerShopSyncPacket decode(RegistryFriendlyByteBuf buf) {
            CompoundTag tag = ByteBufCodecs.TRUSTED_COMPOUND_TAG.decode(buf);
            CompoundTag viewStatesTag = ByteBufCodecs.TRUSTED_COMPOUND_TAG.decode(buf);
            boolean canEdit = buf.readBoolean();
            boolean globalEditModeEnabled = buf.readBoolean();
            return new ServerShopSyncPacket(tag, viewStatesTag, canEdit, globalEditModeEnabled);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ServerShopSyncPacket value) {
            ByteBufCodecs.TRUSTED_COMPOUND_TAG.encode(buf, value.payload() == null ? new CompoundTag() : value.payload());
            ByteBufCodecs.TRUSTED_COMPOUND_TAG.encode(buf, value.offerViewStates() == null ? new CompoundTag() : value.offerViewStates());
            buf.writeBoolean(value.canEdit());
            buf.writeBoolean(value.globalEditModeEnabled());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerShopSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) {
                return;
            }
            ServerShopSerialization.decodeData(packet.payload(), mc.level.registryAccess()).result().ifPresent(data -> {
                ServerShopClientState.apply(data, decodeOfferViewStates(packet.offerViewStates()));
                if (mc.player != null && mc.player.containerMenu instanceof ServerShopMenu menu) {
                    menu.setEditPermissionClient(packet.canEdit(), packet.globalEditModeEnabled());
                    menu.clampSelectedPage(data.pages().size());
                }
            });
        });
    }

    public static CompoundTag encodeOfferViewStates(Map<UUID, ServerShopOfferViewState> states) {
        CompoundTag root = new CompoundTag();
        ListTag list = new ListTag();
        if (states != null) {
            states.forEach((offerId, state) -> {
                if (offerId == null || state == null) {
                    return;
                }
                CompoundTag entry = new CompoundTag();
                entry.put(OFFER_ID_KEY, UUIDUtil.CODEC.encodeStart(NbtOps.INSTANCE, offerId).result().orElseGet(CompoundTag::new));
                entry.put(STATE_KEY, state.toTag());
                list.add(entry);
            });
        }
        root.put(ENTRIES_KEY, list);
        return root;
    }

    public static Map<UUID, ServerShopOfferViewState> decodeOfferViewStates(CompoundTag root) {
        Map<UUID, ServerShopOfferViewState> states = new HashMap<>();
        if (root == null || !root.contains(ENTRIES_KEY, Tag.TAG_LIST)) {
            return states;
        }
        ListTag list = root.getList(ENTRIES_KEY, Tag.TAG_COMPOUND);
        for (Tag tag : list) {
            if (!(tag instanceof CompoundTag entry) || !entry.contains(STATE_KEY, Tag.TAG_COMPOUND)) {
                continue;
            }
            UUIDUtil.CODEC.parse(NbtOps.INSTANCE, entry.get(OFFER_ID_KEY)).result().ifPresent(offerId ->
                    states.put(offerId, ServerShopOfferViewState.fromTag(entry.getCompound(STATE_KEY))));
        }
        return states;
    }
}
