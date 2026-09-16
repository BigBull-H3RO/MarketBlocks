package de.bigbull.marketblocks.feature.marketplace.network;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.compat.journeymap.JourneyMapCompat;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record LinkedBlocksSyncPacket(List<GlobalPos> linkedBlocks) implements CustomPacketPayload {
    public static final Type<LinkedBlocksSyncPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "linked_blocks_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, LinkedBlocksSyncPacket> CODEC = StreamCodec.ofMember(
            LinkedBlocksSyncPacket::write,
            LinkedBlocksSyncPacket::new
    );

    public LinkedBlocksSyncPacket(RegistryFriendlyByteBuf buf) {
        this(readLinkedBlocks(buf));
    }

    private static List<GlobalPos> readLinkedBlocks(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<GlobalPos> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(buf.readGlobalPos());
        }
        return list;
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(linkedBlocks.size());
        for (GlobalPos pos : linkedBlocks) {
            buf.writeGlobalPos(pos);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            JourneyMapCompat.updateMarketplaceMarkers(linkedBlocks);
        });
    }
}
