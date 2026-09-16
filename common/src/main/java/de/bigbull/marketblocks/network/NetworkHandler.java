package de.bigbull.marketblocks.network;

import de.bigbull.marketblocks.platform.Services;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public class NetworkHandler {
    public static void sendToServer(CustomPacketPayload packet) {
        Services.NETWORK.sendToServer(packet);
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload packet) {
        Services.NETWORK.sendToPlayer(player, packet);
    }

    public static void sendToPlayersTrackingChunk(net.minecraft.server.level.ServerLevel level, net.minecraft.world.level.ChunkPos chunkPos, CustomPacketPayload packet) {
        Services.NETWORK.sendToPlayersTrackingChunk(level, chunkPos, packet);
    }
}