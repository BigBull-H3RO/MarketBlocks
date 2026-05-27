package de.bigbull.marketblocks.feature.singleoffer.settings;

import de.bigbull.marketblocks.feature.singleoffer.SideMode;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Settings for the I/O tab: configuration for block sides relative to the shop's facing direction.
 */
public record IoSettings(
        SideMode left,
        SideMode right,
        SideMode bottom,
        SideMode back
) {
    private static final String KEY_LEFT = "Left";
    private static final String KEY_RIGHT = "Right";
    private static final String KEY_BOTTOM = "Bottom";
    private static final String KEY_BACK = "Back";

    public static final IoSettings DEFAULT = new IoSettings(SideMode.DISABLED, SideMode.DISABLED, SideMode.DISABLED, SideMode.DISABLED);

    private static final SideMode[] SIDE_MODE_VALUES = SideMode.values();
    private static final StreamCodec<ByteBuf, SideMode> SIDE_MODE_CODEC = ByteBufCodecs.VAR_INT.map(
            value -> value >= 0 && value < SIDE_MODE_VALUES.length ? SIDE_MODE_VALUES[value] : SideMode.DISABLED,
            SideMode::ordinal
    );

    public static final StreamCodec<ByteBuf, IoSettings> STREAM_CODEC = StreamCodec.of(
            (buf, settings) -> {
                SIDE_MODE_CODEC.encode(buf, settings.left());
                SIDE_MODE_CODEC.encode(buf, settings.right());
                SIDE_MODE_CODEC.encode(buf, settings.bottom());
                SIDE_MODE_CODEC.encode(buf, settings.back());
            },
            buf -> new IoSettings(
                    SIDE_MODE_CODEC.decode(buf),
                    SIDE_MODE_CODEC.decode(buf),
                    SIDE_MODE_CODEC.decode(buf),
                    SIDE_MODE_CODEC.decode(buf)
            )
    );

    public IoSettings {
        left = left == null ? SideMode.DISABLED : left;
        right = right == null ? SideMode.DISABLED : right;
        bottom = bottom == null ? SideMode.DISABLED : bottom;
        back = back == null ? SideMode.DISABLED : back;
    }

    public SideMode getMode(Direction absoluteDir, Direction blockFacing) {
        if (absoluteDir == Direction.DOWN) return bottom;
        if (absoluteDir == blockFacing.getCounterClockWise()) return left;
        if (absoluteDir == blockFacing.getClockWise()) return right;
        if (absoluteDir == blockFacing.getOpposite()) return back;
        return SideMode.DISABLED;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        if (left != SideMode.DISABLED) tag.putString(KEY_LEFT, left.name());
        if (right != SideMode.DISABLED) tag.putString(KEY_RIGHT, right.name());
        if (bottom != SideMode.DISABLED) tag.putString(KEY_BOTTOM, bottom.name());
        if (back != SideMode.DISABLED) tag.putString(KEY_BACK, back.name());
        return tag;
    }

    public static IoSettings load(CompoundTag tag) {
        if (tag == null) return DEFAULT;
        return new IoSettings(
                tag.contains(KEY_LEFT) ? SideMode.valueOf(tag.getString(KEY_LEFT)) : SideMode.DISABLED,
                tag.contains(KEY_RIGHT) ? SideMode.valueOf(tag.getString(KEY_RIGHT)) : SideMode.DISABLED,
                tag.contains(KEY_BOTTOM) ? SideMode.valueOf(tag.getString(KEY_BOTTOM)) : SideMode.DISABLED,
                tag.contains(KEY_BACK) ? SideMode.valueOf(tag.getString(KEY_BACK)) : SideMode.DISABLED
        );
    }

    public IoSettings withMode(Direction absoluteDir, Direction blockFacing, SideMode mode) {
        SideMode newLeft = left;
        SideMode newRight = right;
        SideMode newBottom = bottom;
        SideMode newBack = back;

        if (absoluteDir == Direction.DOWN) newBottom = mode;
        else if (absoluteDir == blockFacing.getCounterClockWise()) newLeft = mode;
        else if (absoluteDir == blockFacing.getClockWise()) newRight = mode;
        else if (absoluteDir == blockFacing.getOpposite()) newBack = mode;

        return new IoSettings(newLeft, newRight, newBottom, newBack);
    }

    /**
     * Mutable draft for the I/O settings tab in the GUI.
     */
    public static final class Draft {
        private SideMode left;
        private SideMode right;
        private SideMode bottom;
        private SideMode back;

        public Draft(IoSettings settings) {
            IoSettings s = settings == null ? DEFAULT : settings;
            this.left = s.left();
            this.right = s.right();
            this.bottom = s.bottom();
            this.back = s.back();
        }

        public SideMode left() { return left; }
        public Draft setLeft(SideMode v) { this.left = v == null ? SideMode.DISABLED : v; return this; }

        public SideMode right() { return right; }
        public Draft setRight(SideMode v) { this.right = v == null ? SideMode.DISABLED : v; return this; }

        public SideMode bottom() { return bottom; }
        public Draft setBottom(SideMode v) { this.bottom = v == null ? SideMode.DISABLED : v; return this; }

        public SideMode back() { return back; }
        public Draft setBack(SideMode v) { this.back = v == null ? SideMode.DISABLED : v; return this; }

        public SideMode getMode(Direction relativeDir) {
            return switch(relativeDir) {
                case WEST -> left; // Representing relative "Left"
                case EAST -> right; // Representing relative "Right"
                case DOWN -> bottom; // Representing relative "Bottom"
                case NORTH -> back; // Representing relative "Back"
                default -> SideMode.DISABLED;
            };
        }
        
        public Draft setMode(Direction relativeDir, SideMode mode) {
            switch(relativeDir) {
                case WEST -> setLeft(mode);
                case EAST -> setRight(mode);
                case DOWN -> setBottom(mode);
                case NORTH -> setBack(mode);
            }
            return this;
        }

        public IoSettings toSettings() {
            return new IoSettings(left, right, bottom, back);
        }
    }
}
