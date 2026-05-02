package de.bigbull.marketblocks.feature.marketplace.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;

/**
 * Utility methods for encoding and decoding marketplace data as NBT {@link CompoundTag} payloads.
 */
public final class MarketplaceSerialization {
    private MarketplaceSerialization() {
    }

    /** Encodes the full shop data into a {@link CompoundTag} using the provided registry context. */
    public static DataResult<CompoundTag> encodeData(MarketplaceData data, RegistryAccess access) {
        return encode(data, "Shop data must not be null", MarketplaceData.CODEC, access);
    }

    /** Decodes full shop data from a {@link CompoundTag} using the provided registry context. */
    public static DataResult<MarketplaceData> decodeData(CompoundTag tag, RegistryAccess access) {
        return decode(tag, MarketplaceData.CODEC, access);
    }

    /** Encodes an {@link OfferLimit} into a {@link CompoundTag}. */
    public static DataResult<CompoundTag> encodeLimit(OfferLimit limit, RegistryAccess access) {
        return encode(limit, "Offer limit must not be null", OfferLimit.CODEC, access);
    }

    /** Decodes an {@link OfferLimit} from a {@link CompoundTag}. */
    public static DataResult<OfferLimit> decodeLimit(CompoundTag tag, RegistryAccess access) {
        return decode(tag, OfferLimit.CODEC, access);
    }

    /** Encodes a {@link DemandPricing} configuration into a {@link CompoundTag}. */
    public static DataResult<CompoundTag> encodePricing(DemandPricing pricing, RegistryAccess access) {
        return encode(pricing, "Demand pricing must not be null", DemandPricing.CODEC, access);
    }

    /** Decodes a {@link DemandPricing} configuration from a {@link CompoundTag}. */
    public static DataResult<DemandPricing> decodePricing(CompoundTag tag, RegistryAccess access) {
        return decode(tag, DemandPricing.CODEC, access);
    }





    private static <T> DataResult<CompoundTag> encode(T value, String nullMessage, Codec<T> codec, RegistryAccess access) {
        if (value == null) {
            return DataResult.error(() -> nullMessage);
        }
        if (access == null) {
            return DataResult.error(() -> "RegistryAccess must not be null");
        }
        return codec.encodeStart(RegistryOps.create(NbtOps.INSTANCE, access), value).flatMap(MarketplaceSerialization::requireCompoundTag);
    }

    private static <T> DataResult<T> decode(CompoundTag tag, Codec<T> codec, RegistryAccess access) {
        if (tag == null) {
            return DataResult.error(() -> "CompoundTag must not be null");
        }
        if (access == null) {
            return DataResult.error(() -> "RegistryAccess must not be null");
        }
        return codec.parse(RegistryOps.create(NbtOps.INSTANCE, access), tag);
    }

    private static DataResult<CompoundTag> requireCompoundTag(Tag tag) {
        if (tag instanceof CompoundTag compound) {
            return DataResult.success(compound);
        }
        return DataResult.error(() -> "Expected CompoundTag but got " + tag.getType().getName());
    }
}
