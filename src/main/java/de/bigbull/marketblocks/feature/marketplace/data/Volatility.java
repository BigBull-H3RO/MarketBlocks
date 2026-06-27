package de.bigbull.marketblocks.feature.marketplace.data;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum Volatility implements StringRepresentable {
    SLOW("slow"),
    NORMAL("normal"),
    FAST("fast");

    public static final Codec<Volatility> CODEC = StringRepresentable.fromEnum(Volatility::values);

    private final String name;

    Volatility(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
