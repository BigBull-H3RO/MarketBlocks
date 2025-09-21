package de.bigbull.marketblocks.util.custom.servershop;

import com.mojang.serialization.DataResult;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;

import java.util.function.Function;

/**
 * Hilfsmethoden für die Serialisierung der Server-Shop-Daten in Netzwerkpayloads.
 */
public final class ServerShopSerialization {
    private ServerShopSerialization() {
    }

    public static DataResult<CompoundTag> encodeData(ServerShopData data, RegistryAccess access) {
        if (data == null) {
            return DataResult.error(() -> "Daten dürfen nicht null sein");
        }
        if (access == null) {
            return DataResult.error(() -> "RegistryAccess fehlt");
        }
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, access);
        return ServerShopData.CODEC.encodeStart(ops, data).flatMap(asCompound());
    }

    public static DataResult<ServerShopData> decodeData(CompoundTag tag, RegistryAccess access) {
        if (tag == null) {
            return DataResult.error(() -> "CompoundTag fehlt");
        }
        if (access == null) {
            return DataResult.error(() -> "RegistryAccess fehlt");
        }
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, access);
        return ServerShopData.CODEC.parse(ops, tag);
    }

    public static DataResult<CompoundTag> encodeLimit(OfferLimit limit, RegistryAccess access) {
        if (limit == null) {
            return DataResult.error(() -> "Limit darf nicht null sein");
        }
        if (access == null) {
            return DataResult.error(() -> "RegistryAccess fehlt");
        }
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, access);
        return OfferLimit.CODEC.encodeStart(ops, limit).flatMap(asCompound());
    }

    public static DataResult<OfferLimit> decodeLimit(CompoundTag tag, RegistryAccess access) {
        if (tag == null) {
            return DataResult.error(() -> "CompoundTag fehlt");
        }
        if (access == null) {
            return DataResult.error(() -> "RegistryAccess fehlt");
        }
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, access);
        return OfferLimit.CODEC.parse(ops, tag);
    }

    public static DataResult<CompoundTag> encodePricing(DemandPricing pricing, RegistryAccess access) {
        if (pricing == null) {
            return DataResult.error(() -> "Pricing darf nicht null sein");
        }
        if (access == null) {
            return DataResult.error(() -> "RegistryAccess fehlt");
        }
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, access);
        return DemandPricing.CODEC.encodeStart(ops, pricing).flatMap(asCompound());
    }

    public static DataResult<DemandPricing> decodePricing(CompoundTag tag, RegistryAccess access) {
        if (tag == null) {
            return DataResult.error(() -> "CompoundTag fehlt");
        }
        if (access == null) {
            return DataResult.error(() -> "RegistryAccess fehlt");
        }
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, access);
        return DemandPricing.CODEC.parse(ops, tag);
    }

    private static Function<Tag, DataResult<CompoundTag>> asCompound() {
        return tag -> {
            if (tag instanceof CompoundTag compound) {
                return DataResult.success(compound);
            }
            return DataResult.error(() -> "Erwartete CompoundTag, erhielt " + tag.getType().getName());
        };
    }
}