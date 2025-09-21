package de.bigbull.marketblocks.network.packets.serverShop;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.menu.ServerShopMenu;
import de.bigbull.marketblocks.util.custom.servershop.ServerShopClientState;
import de.bigbull.marketblocks.util.custom.servershop.ServerShopSerialization;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Synchronisiert den vollständigen Datenstand des Server-Shops an den Client.
 */
public record ServerShopSyncPacket(CompoundTag payload, boolean canEdit) implements CustomPacketPayload {
    public static final Type<ServerShopSyncPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "server_shop_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerShopSyncPacket> CODEC = new StreamCodec<>() {
        @Override
        public ServerShopSyncPacket decode(RegistryFriendlyByteBuf buf) {
            CompoundTag tag = ByteBufCodecs.TRUSTED_COMPOUND_TAG.decode(buf);
            boolean canEdit = buf.readBoolean();
            return new ServerShopSyncPacket(tag, canEdit);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ServerShopSyncPacket value) {
            ByteBufCodecs.TRUSTED_COMPOUND_TAG.encode(buf, value.payload() == null ? new CompoundTag() : value.payload());
            buf.writeBoolean(value.canEdit());
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
                ServerShopClientState.apply(data, packet.canEdit());
                if (mc.player != null && mc.player.containerMenu instanceof ServerShopMenu menu) {
                    menu.setSelectedPageClient(data.selectedPage());
                }
            });
        });
    }
}