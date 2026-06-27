package de.bigbull.marketblocks.feature.singleoffer.network;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.core.config.Config;
import de.bigbull.marketblocks.core.init.RegistriesInit;
import de.bigbull.marketblocks.feature.singleoffer.block.BaseShopBlock;
import de.bigbull.marketblocks.feature.singleoffer.entity.SingleOfferShopBlockEntity;
import de.bigbull.marketblocks.feature.singleoffer.settings.AccessSettings;
import de.bigbull.marketblocks.feature.singleoffer.settings.GeneralSettings;
import de.bigbull.marketblocks.feature.singleoffer.settings.IoSettings;
import de.bigbull.marketblocks.feature.singleoffer.settings.OfferItemSettings;
import de.bigbull.marketblocks.feature.singleoffer.settings.VillagerSettings;
import de.bigbull.marketblocks.feature.singleoffer.settings.NotificationSettings;
import de.bigbull.marketblocks.feature.singleoffer.settings.IoRedstoneControl;
import de.bigbull.marketblocks.feature.visual.npc.ShopVisualPlacementValidator;
import de.bigbull.marketblocks.feature.visual.npc.VisualNpcPlacementResult;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * A packet sent from the client to the server to save all shop settings.
 * Validates the settings and triggers relevant advancements on success.
 */
public record UpdateSettingsPacket(
        BlockPos pos,
        IoSettings ioSettings,
        GeneralSettings generalSettings,
        VillagerSettings villagerSettings,
        OfferItemSettings offerItemSettings,
        AccessSettings accessSettings,
        NotificationSettings notificationSettings) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdateSettingsPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "update_side_config"));

    public static final StreamCodec<ByteBuf, UpdateSettingsPacket> CODEC = StreamCodec.of(
            (buf, packet) -> {
                BlockPos.STREAM_CODEC.encode(buf, packet.pos());
                IoSettings.STREAM_CODEC.encode(buf, packet.ioSettings());
                GeneralSettings.STREAM_CODEC.encode(buf, packet.generalSettings());
                VillagerSettings.STREAM_CODEC.encode(buf, packet.villagerSettings());
                OfferItemSettings.STREAM_CODEC.encode(buf, packet.offerItemSettings());
                AccessSettings.STREAM_CODEC.encode(buf, packet.accessSettings());
                NotificationSettings.STREAM_CODEC.encode(buf, packet.notificationSettings());
            },
            buf -> {
                BlockPos pos = BlockPos.STREAM_CODEC.decode(buf);
                IoSettings io = IoSettings.STREAM_CODEC.decode(buf);
                GeneralSettings general = GeneralSettings.STREAM_CODEC.decode(buf);
                VillagerSettings villager = VillagerSettings.STREAM_CODEC.decode(buf);
                OfferItemSettings offerItem = OfferItemSettings.STREAM_CODEC.decode(buf);
                AccessSettings access = AccessSettings.STREAM_CODEC.decode(buf);
                NotificationSettings notifications = NotificationSettings.STREAM_CODEC.decode(buf);
                return new UpdateSettingsPacket(pos, io, general, villager, offerItem, access, notifications);
            });

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UpdateSettingsPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            Level level = player.level();
            if (player.containerMenu instanceof de.bigbull.marketblocks.feature.singleoffer.menu.SingleOfferShopMenu menu
                    && menu.getBlockEntity() == level.getBlockEntity(packet.pos())
                    && menu.stillValid(player)
                    && menu.getBlockEntity() instanceof SingleOfferShopBlockEntity blockEntity
                    && blockEntity.isOwner(player)) {

                Direction facing = blockEntity.getBlockState().getValue(BaseShopBlock.FACING);
                VillagerSettings villager = packet.villagerSettings();

                if (villager.npcEnabled()) {
                    boolean isValid = ShopVisualPlacementValidator.validate(level, packet.pos(), facing)
                            .result() == VisualNpcPlacementResult.OK;
                    if (!isValid) {
                        villager = villager.withNpcEnabled(false);
                    }
                }

                AccessSettings incomingAccess = packet.accessSettings();
                AccessSettings existingAccess = blockEntity.getSettingsManager().getAccessSettings();
                boolean isPrimaryOwner = blockEntity.getAccessManager().isPrimaryOwner(player);
                boolean isAdminMode = Config.MARKETBLOCKS_ADMIN_MODE_ENABLED.get() && player.hasPermissions(2);

                boolean newAdminShopEnabled = existingAccess.adminShopEnabled();
                if (isAdminMode) {
                    newAdminShopEnabled = incomingAccess.adminShopEnabled();
                }

                UUID newOwnerId = existingAccess.ownerId();
                String newOwnerName = existingAccess.ownerName();

                Map<UUID, String> newAdditionalOwners = existingAccess.additionalOwners();
                de.bigbull.marketblocks.feature.singleoffer.settings.AccessMode newAccessMode = existingAccess.accessMode();
                Map<UUID, String> newAccessList = existingAccess.accessList();

                if (isPrimaryOwner) {
                    newAdditionalOwners = incomingAccess.additionalOwners();
                    newAccessMode = incomingAccess.accessMode();
                    newAccessList = incomingAccess.accessList();

                    int maxOwners = Config.MAX_CO_OWNERS_PER_SHOP.get();
                    if (newAdditionalOwners.size() > maxOwners) {
                        Map<UUID, String> truncated = new HashMap<>();
                        int count = 0;
                        for (var entry : newAdditionalOwners.entrySet()) {
                            if (count >= maxOwners)
                                break;
                            truncated.put(entry.getKey(), entry.getValue());
                            count++;
                        }
                        newAdditionalOwners = truncated;
                    }
                }

                blockEntity.updateSettingsBatch(
                        packet.ioSettings(),
                        packet.generalSettings(),
                        villager,
                        packet.offerItemSettings(),
                        new AccessSettings(newAdminShopEnabled, newOwnerId, newOwnerName, newAdditionalOwners, newAccessMode, newAccessList),
                        packet.notificationSettings());

                if (villager.npcEnabled()) {
                    RegistriesInit.SHOP_NPC_TRIGGER.get().trigger(player);
                }

                if (!newAdditionalOwners.isEmpty()) {
                    RegistriesInit.SHOP_CO_OWNER_TRIGGER.get().trigger(player);
                }

                if (villager.npcEnabled() && (!villager.npcName().isEmpty() || villager.usePlayerSkin())) {
                    RegistriesInit.SHOP_NPC_CUSTOMIZE_TRIGGER.get().trigger(player);
                }

                if (packet.generalSettings().emitRedstone() || packet.ioSettings()
                        .redstoneControl() != IoRedstoneControl.IGNORED) {
                    RegistriesInit.SHOP_REDSTONE_TRIGGER.get().trigger(player);
                }

                if (packet.ioSettings().autoIo()) {
                    RegistriesInit.SHOP_AUTO_IO_TRIGGER.get().trigger(player);
                }
            }
        });
    }
}

