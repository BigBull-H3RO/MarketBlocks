package de.bigbull.marketblocks.core.config.network;

import de.bigbull.marketblocks.Constants;
import de.bigbull.marketblocks.core.config.toml.TomlConfigManager;
import de.bigbull.marketblocks.platform.network.PacketContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * Synchronizes server configuration values to the client upon login or reload.
 * 
 * In-memory only: The received values are applied directly to the active
 * TomlConfigSpec instances in memory, ensuring that client disk files (.toml)
 * are NEVER modified or overwritten.
 */
public record ConfigSyncPacket(Map<String, Map<String, String>> configs) implements CustomPacketPayload {

    public static final Type<ConfigSyncPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "config_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigSyncPacket> CODEC = new StreamCodec<>() {
        @Override
        public ConfigSyncPacket decode(RegistryFriendlyByteBuf buf) {
            int configCount = buf.readVarInt();
            Map<String, Map<String, String>> configs = new HashMap<>(configCount);
            for (int i = 0; i < configCount; i++) {
                String configPath = buf.readUtf();
                int valueCount = buf.readVarInt();
                Map<String, String> values = new HashMap<>(valueCount);
                for (int j = 0; j < valueCount; j++) {
                    String key = buf.readUtf();
                    String val = buf.readUtf();
                    values.put(key, val);
                }
                configs.put(configPath, values);
            }
            return new ConfigSyncPacket(configs);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ConfigSyncPacket packet) {
            buf.writeVarInt(packet.configs().size());
            for (Map.Entry<String, Map<String, String>> entry : packet.configs().entrySet()) {
                buf.writeUtf(entry.getKey());
                Map<String, String> values = entry.getValue();
                buf.writeVarInt(values.size());
                for (Map.Entry<String, String> valEntry : values.entrySet()) {
                    buf.writeUtf(valEntry.getKey());
                    buf.writeUtf(valEntry.getValue());
                }
            }
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ConfigSyncPacket packet, PacketContext context) {
        context.enqueueWork(() -> {
            TomlConfigManager.applySyncedConfigs(packet.configs());
        });
    }
}
