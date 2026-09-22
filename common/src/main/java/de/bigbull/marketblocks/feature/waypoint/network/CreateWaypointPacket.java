package de.bigbull.marketblocks.feature.waypoint.network;

import de.bigbull.marketblocks.Constants;
import de.bigbull.marketblocks.feature.waypoint.client.WaypointClientHandler;
import de.bigbull.marketblocks.platform.network.PacketContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CreateWaypointPacket(int x, int y, int z, String dim, String name) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<CreateWaypointPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "create_waypoint"));

    public static final StreamCodec<ByteBuf, CreateWaypointPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CreateWaypointPacket::x,
            ByteBufCodecs.VAR_INT, CreateWaypointPacket::y,
            ByteBufCodecs.VAR_INT, CreateWaypointPacket::z,
            ByteBufCodecs.STRING_UTF8, CreateWaypointPacket::dim,
            ByteBufCodecs.STRING_UTF8, CreateWaypointPacket::name,
            CreateWaypointPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CreateWaypointPacket packet, PacketContext context) {
        context.enqueueWork(() -> WaypointClientHandler.handle(packet, context.player()));
    }
}