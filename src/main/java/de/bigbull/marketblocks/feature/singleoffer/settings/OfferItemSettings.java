package de.bigbull.marketblocks.feature.singleoffer.settings;

import de.bigbull.marketblocks.feature.singleoffer.block.CrateLayoutMode;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Settings for the Visuals (Offer Item) tab: item display configuration.
 */
public record OfferItemSettings(
        boolean visible,
        boolean fullbright,
        float scale,
        float speed,
        float heightOffset,
        boolean bobbing,
        int count,
        float rotation,
        CrateLayoutMode layoutMode,
        float spacingXZ,
        float spacingY,
        float chaosRotation,
        boolean dynamicFillLevel) {
    private static final float MIN_SCALE = 0.5f;
    private static final float MAX_SCALE = 1.5f;
    private static final float MIN_SPEED = 0.0f;
    private static final float MAX_SPEED = 1.5f;
    private static final float MIN_HEIGHT = -0.25f;
    private static final float MAX_HEIGHT = 0.25f;
    private static final int MIN_COUNT = 1;
    public static final int MAX_COUNT = 96;
    private static final float MIN_SPACING = -0.5f;
    private static final float MAX_SPACING = 2.0f;
    private static final float MIN_CHAOS_ROTATION = 0.0f;
    private static final float MAX_CHAOS_ROTATION = 1.0f;

    private static final float DEFAULT_SCALE = 0.75f;
    private static final float DEFAULT_SPEED = 2.0f;
    private static final float DEFAULT_HEIGHT = 0.0f;
    private static final int DEFAULT_COUNT = 1;
    private static final float DEFAULT_ROTATION = 0.0f;
    private static final CrateLayoutMode DEFAULT_LAYOUT_MODE = CrateLayoutMode.STACKED;
    private static final float DEFAULT_SPACING_XZ = 0.0f;
    private static final float DEFAULT_SPACING_Y = 0.0f;
    private static final float DEFAULT_CHAOS_ROTATION = 0.1f;
    private static final boolean DEFAULT_DYNAMIC_FILL_LEVEL = false;

    private static final String KEY_VISIBLE = "OfferItemVisible";
    private static final String KEY_FULLBRIGHT = "OfferItemFullbright";
    private static final String KEY_SCALE = "OfferItemScale";
    private static final String KEY_SPEED = "OfferItemSpeed";
    private static final String KEY_HEIGHT = "OfferItemHeight";
    private static final String KEY_BOBBING = "OfferItemBobbing";
    private static final String KEY_COUNT = "OfferItemCount";
    private static final String KEY_ROTATION = "OfferItemRotation";
    private static final String KEY_LAYOUT_MODE = "OfferItemLayoutMode";
    private static final String KEY_SPACING_XZ = "OfferItemSpacingXZ";
    private static final String KEY_SPACING_Y = "OfferItemSpacingY";
    private static final String KEY_CHAOS_ROTATION = "OfferItemChaosRotation";
    private static final String KEY_DYNAMIC_FILL_LEVEL = "DynamicFillLevel";

    public static final OfferItemSettings DEFAULT = new OfferItemSettings(
            true, false, DEFAULT_SCALE, DEFAULT_SPEED, DEFAULT_HEIGHT,
            true, DEFAULT_COUNT, DEFAULT_ROTATION, DEFAULT_LAYOUT_MODE,
            DEFAULT_SPACING_XZ, DEFAULT_SPACING_Y, DEFAULT_CHAOS_ROTATION, DEFAULT_DYNAMIC_FILL_LEVEL);

    public static final StreamCodec<ByteBuf, OfferItemSettings> STREAM_CODEC = StreamCodec.of(
            (buf, s) -> {
                ByteBufCodecs.BOOL.encode(buf, s.visible());
                ByteBufCodecs.BOOL.encode(buf, s.fullbright());
                ByteBufCodecs.FLOAT.encode(buf, s.scale());
                ByteBufCodecs.FLOAT.encode(buf, s.speed());
                ByteBufCodecs.FLOAT.encode(buf, s.heightOffset());
                ByteBufCodecs.BOOL.encode(buf, s.bobbing());
                ByteBufCodecs.INT.encode(buf, s.count());
                ByteBufCodecs.FLOAT.encode(buf, s.rotation());
                ByteBufCodecs.STRING_UTF8.encode(buf, s.layoutMode().serializedName());
                ByteBufCodecs.FLOAT.encode(buf, s.spacingXZ());
                ByteBufCodecs.FLOAT.encode(buf, s.spacingY());
                ByteBufCodecs.FLOAT.encode(buf, s.chaosRotation());
                ByteBufCodecs.BOOL.encode(buf, s.dynamicFillLevel());
            },
            buf -> new OfferItemSettings(
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.FLOAT.decode(buf),
                    ByteBufCodecs.FLOAT.decode(buf),
                    ByteBufCodecs.FLOAT.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.INT.decode(buf),
                    ByteBufCodecs.FLOAT.decode(buf),
                    CrateLayoutMode.fromSerialized(ByteBufCodecs.STRING_UTF8.decode(buf)),
                    ByteBufCodecs.FLOAT.decode(buf),
                    ByteBufCodecs.FLOAT.decode(buf),
                    ByteBufCodecs.FLOAT.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf)));

    public OfferItemSettings {
        scale = clampFinite(scale, MIN_SCALE, MAX_SCALE, DEFAULT_SCALE);
        speed = clampFinite(speed, MIN_SPEED, MAX_SPEED, DEFAULT_SPEED);
        heightOffset = clampFinite(heightOffset, MIN_HEIGHT, MAX_HEIGHT, DEFAULT_HEIGHT);
        count = Math.clamp(count, MIN_COUNT, MAX_COUNT);
        rotation = normalizeDegrees(rotation);
        layoutMode = layoutMode == null ? DEFAULT_LAYOUT_MODE : layoutMode;
        spacingXZ = clampFinite(spacingXZ, MIN_SPACING, MAX_SPACING, DEFAULT_SPACING_XZ);
        spacingY = clampFinite(spacingY, MIN_SPACING, MAX_SPACING, DEFAULT_SPACING_Y);
        chaosRotation = clampFinite(chaosRotation, MIN_CHAOS_ROTATION, MAX_CHAOS_ROTATION, DEFAULT_CHAOS_ROTATION);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(KEY_VISIBLE, visible);
        tag.putBoolean(KEY_FULLBRIGHT, fullbright);
        tag.putFloat(KEY_SCALE, scale);
        tag.putFloat(KEY_SPEED, speed);
        tag.putFloat(KEY_HEIGHT, heightOffset);
        tag.putBoolean(KEY_BOBBING, bobbing);
        tag.putInt(KEY_COUNT, count);
        tag.putFloat(KEY_ROTATION, rotation);
        tag.putString(KEY_LAYOUT_MODE, layoutMode.serializedName());
        tag.putFloat(KEY_SPACING_XZ, spacingXZ);
        tag.putFloat(KEY_SPACING_Y, spacingY);
        tag.putFloat(KEY_CHAOS_ROTATION, chaosRotation);
        tag.putBoolean(KEY_DYNAMIC_FILL_LEVEL, dynamicFillLevel);
        return tag;
    }

    public static OfferItemSettings load(CompoundTag tag) {
        if (tag == null)
            return DEFAULT;
        return new OfferItemSettings(
                !tag.contains(KEY_VISIBLE) || tag.getBoolean(KEY_VISIBLE),
                tag.getBoolean(KEY_FULLBRIGHT),
                tag.contains(KEY_SCALE) ? tag.getFloat(KEY_SCALE) : DEFAULT_SCALE,
                tag.contains(KEY_SPEED) ? tag.getFloat(KEY_SPEED) : DEFAULT_SPEED,
                tag.contains(KEY_HEIGHT) ? tag.getFloat(KEY_HEIGHT) : DEFAULT_HEIGHT,
                !tag.contains(KEY_BOBBING) || tag.getBoolean(KEY_BOBBING),
                tag.contains(KEY_COUNT) ? tag.getInt(KEY_COUNT) : DEFAULT_COUNT,
                tag.contains(KEY_ROTATION) ? tag.getFloat(KEY_ROTATION) : DEFAULT_ROTATION,
                tag.contains(KEY_LAYOUT_MODE) ? CrateLayoutMode.fromSerialized(tag.getString(KEY_LAYOUT_MODE))
                        : DEFAULT_LAYOUT_MODE,
                tag.contains(KEY_SPACING_XZ) ? tag.getFloat(KEY_SPACING_XZ) : DEFAULT_SPACING_XZ,
                tag.contains(KEY_SPACING_Y) ? tag.getFloat(KEY_SPACING_Y) : DEFAULT_SPACING_Y,
                tag.contains(KEY_CHAOS_ROTATION) ? tag.getFloat(KEY_CHAOS_ROTATION) : DEFAULT_CHAOS_ROTATION,
                tag.contains(KEY_DYNAMIC_FILL_LEVEL) && tag.getBoolean(KEY_DYNAMIC_FILL_LEVEL));
    }

    private static float clampFinite(float value, float min, float max, float fallback) {
        if (!Float.isFinite(value))
            return fallback;
        return Math.clamp(value, min, max);
    }

    private static float normalizeDegrees(float value) {
        if (!Float.isFinite(value))
            return DEFAULT_ROTATION;
        float normalized = value % 360.0f;
        return normalized < 0.0f ? normalized + 360.0f : normalized;
    }

    /**
     * Mutable draft for the Offer Item (Visuals) settings tab in the GUI.
     */
    public static final class Draft {
        private boolean visible;
        private boolean fullbright;
        private float scale;
        private float speed;
        private float heightOffset;
        private boolean bobbing;
        private int count;
        private float rotation;
        private CrateLayoutMode layoutMode;
        private float spacingXZ;
        private float spacingY;
        private float chaosRotation;
        private boolean dynamicFillLevel;

        public Draft(OfferItemSettings settings) {
            OfferItemSettings s = settings == null ? DEFAULT : settings;
            this.visible = s.visible();
            this.fullbright = s.fullbright();
            this.scale = s.scale();
            this.speed = s.speed();
            this.heightOffset = s.heightOffset();
            this.bobbing = s.bobbing();
            this.count = s.count();
            this.rotation = s.rotation();
            this.layoutMode = s.layoutMode();
            this.spacingXZ = s.spacingXZ();
            this.spacingY = s.spacingY();
            this.chaosRotation = s.chaosRotation();
            this.dynamicFillLevel = s.dynamicFillLevel();
        }

        public boolean visible() {
            return visible;
        }

        public Draft setVisible(boolean v) {
            this.visible = v;
            return this;
        }

        public boolean fullbright() {
            return fullbright;
        }

        public Draft setFullbright(boolean v) {
            this.fullbright = v;
            return this;
        }

        public float scale() {
            return scale;
        }

        public Draft setScale(float v) {
            this.scale = v;
            return this;
        }

        public float speed() {
            return speed;
        }

        public Draft setSpeed(float v) {
            this.speed = v;
            return this;
        }

        public float heightOffset() {
            return heightOffset;
        }

        public Draft setHeightOffset(float v) {
            this.heightOffset = v;
            return this;
        }

        public boolean bobbing() {
            return bobbing;
        }

        public Draft setBobbing(boolean v) {
            this.bobbing = v;
            return this;
        }

        public int count() {
            return count;
        }

        public Draft setCount(int v) {
            this.count = v;
            return this;
        }

        public float rotation() {
            return rotation;
        }

        public Draft setRotation(float v) {
            this.rotation = v;
            return this;
        }

        public CrateLayoutMode layoutMode() {
            return layoutMode;
        }

        public Draft setLayoutMode(CrateLayoutMode v) {
            this.layoutMode = v == null ? DEFAULT_LAYOUT_MODE : v;
            return this;
        }

        public float spacingXZ() {
            return spacingXZ;
        }

        public Draft setSpacingXZ(float v) {
            this.spacingXZ = v;
            return this;
        }

        public float spacingY() {
            return spacingY;
        }

        public Draft setSpacingY(float v) {
            this.spacingY = v;
            return this;
        }

        public float chaosRotation() {
            return chaosRotation;
        }

        public Draft setChaosRotation(float v) {
            this.chaosRotation = v;
            return this;
        }

        public boolean dynamicFillLevel() {
            return dynamicFillLevel;
        }

        public Draft setDynamicFillLevel(boolean v) {
            this.dynamicFillLevel = v;
            return this;
        }

        public OfferItemSettings toSettings() {
            return new OfferItemSettings(visible, fullbright, scale, speed, heightOffset,
                    bobbing, count, rotation, layoutMode, spacingXZ, spacingY, chaosRotation, dynamicFillLevel);
        }
    }
}
