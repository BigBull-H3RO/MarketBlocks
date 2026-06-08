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
        SideMode back,
        IoRedstoneControl redstoneControl,
        boolean allowIo,
        boolean autoIo
) {
    private static final String KEY_LEFT = "Left";
    private static final String KEY_RIGHT = "Right";
    private static final String KEY_BOTTOM = "Bottom";
    private static final String KEY_BACK = "Back";
    private static final String KEY_REDSTONE_CONTROL = "RedstoneControl";
    private static final String KEY_ALLOW_IO = "AllowIo";
    private static final String KEY_AUTO_IO = "AutoIo";

    public static final IoSettings DEFAULT = new IoSettings(
            SideMode.DISABLED, SideMode.DISABLED, SideMode.DISABLED, SideMode.DISABLED,
            IoRedstoneControl.IGNORED, true, false
    );

    private static final SideMode[] SIDE_MODE_VALUES = SideMode.values();
    private static final StreamCodec<ByteBuf, SideMode> SIDE_MODE_CODEC = ByteBufCodecs.VAR_INT.map(
            value -> value >= 0 && value < SIDE_MODE_VALUES.length ? SIDE_MODE_VALUES[value] : SideMode.DISABLED,
            SideMode::ordinal
    );

    private static final IoRedstoneControl[] REDSTONE_CONTROL_VALUES = IoRedstoneControl.values();
    private static final StreamCodec<ByteBuf, IoRedstoneControl> REDSTONE_CONTROL_CODEC = ByteBufCodecs.VAR_INT.map(
            value -> value >= 0 && value < REDSTONE_CONTROL_VALUES.length ? REDSTONE_CONTROL_VALUES[value] : IoRedstoneControl.IGNORED,
            IoRedstoneControl::ordinal
    );

    public static final StreamCodec<ByteBuf, IoSettings> STREAM_CODEC = StreamCodec.of(
            (buf, settings) -> {
                SIDE_MODE_CODEC.encode(buf, settings.left());
                SIDE_MODE_CODEC.encode(buf, settings.right());
                SIDE_MODE_CODEC.encode(buf, settings.bottom());
                SIDE_MODE_CODEC.encode(buf, settings.back());
                REDSTONE_CONTROL_CODEC.encode(buf, settings.redstoneControl());
                ByteBufCodecs.BOOL.encode(buf, settings.allowIo());
                ByteBufCodecs.BOOL.encode(buf, settings.autoIo());
            },
            buf -> new IoSettings(
                    SIDE_MODE_CODEC.decode(buf),
                    SIDE_MODE_CODEC.decode(buf),
                    SIDE_MODE_CODEC.decode(buf),
                    SIDE_MODE_CODEC.decode(buf),
                    REDSTONE_CONTROL_CODEC.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf)
            )
    );

    public IoSettings {
        left = left == null ? SideMode.DISABLED : left;
        right = right == null ? SideMode.DISABLED : right;
        bottom = bottom == null ? SideMode.DISABLED : bottom;
        back = back == null ? SideMode.DISABLED : back;
        redstoneControl = redstoneControl == null ? IoRedstoneControl.IGNORED : redstoneControl;
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
        if (redstoneControl != IoRedstoneControl.IGNORED) tag.putString(KEY_REDSTONE_CONTROL, redstoneControl.name());
        if (!allowIo) tag.putBoolean(KEY_ALLOW_IO, false);
        if (autoIo) tag.putBoolean(KEY_AUTO_IO, true);
        return tag;
    }

    public static IoSettings load(CompoundTag tag) {
        if (tag == null) return DEFAULT;
        return new IoSettings(
                tag.contains(KEY_LEFT) ? SideMode.valueOf(tag.getString(KEY_LEFT)) : SideMode.DISABLED,
                tag.contains(KEY_RIGHT) ? SideMode.valueOf(tag.getString(KEY_RIGHT)) : SideMode.DISABLED,
                tag.contains(KEY_BOTTOM) ? SideMode.valueOf(tag.getString(KEY_BOTTOM)) : SideMode.DISABLED,
                tag.contains(KEY_BACK) ? SideMode.valueOf(tag.getString(KEY_BACK)) : SideMode.DISABLED,
                tag.contains(KEY_REDSTONE_CONTROL) ? IoRedstoneControl.valueOf(tag.getString(KEY_REDSTONE_CONTROL)) : IoRedstoneControl.IGNORED,
                tag.contains(KEY_ALLOW_IO) ? tag.getBoolean(KEY_ALLOW_IO) : true,
                tag.contains(KEY_AUTO_IO) ? tag.getBoolean(KEY_AUTO_IO) : false
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

        return new IoSettings(newLeft, newRight, newBottom, newBack, redstoneControl, allowIo, autoIo);
    }

    /**
     * Mutable draft for the I/O settings tab in the GUI.
     */
    public static final class Draft {
        private SideMode left;
        private SideMode right;
        private SideMode bottom;
        private SideMode back;
        private IoRedstoneControl redstoneControl;
        private boolean allowIo;
        private boolean autoIo;

        public Draft(IoSettings settings) {
            IoSettings s = settings == null ? DEFAULT : settings;
            this.left = s.left();
            this.right = s.right();
            this.bottom = s.bottom();
            this.back = s.back();
            this.redstoneControl = s.redstoneControl();
            this.allowIo = s.allowIo();
            this.autoIo = s.autoIo();
        }

        public SideMode left() { return left; }
        public Draft setLeft(SideMode v) { this.left = v == null ? SideMode.DISABLED : v; return this; }

        public SideMode right() { return right; }
        public Draft setRight(SideMode v) { this.right = v == null ? SideMode.DISABLED : v; return this; }

        public SideMode bottom() { return bottom; }
        public Draft setBottom(SideMode v) { this.bottom = v == null ? SideMode.DISABLED : v; return this; }

        public SideMode back() { return back; }
        public Draft setBack(SideMode v) { this.back = v == null ? SideMode.DISABLED : v; return this; }

        public IoRedstoneControl redstoneControl() { return redstoneControl; }
        public Draft setRedstoneControl(IoRedstoneControl v) { this.redstoneControl = v == null ? IoRedstoneControl.IGNORED : v; return this; }

        public boolean allowIo() { return allowIo; }
        public Draft setAllowIo(boolean v) { this.allowIo = v; return this; }

        public boolean autoIo() { return autoIo; }
        public Draft setAutoIo(boolean v) { this.autoIo = v; return this; }

        public SideMode getMode(Direction relativeDir) {
            return switch(relativeDir) {
                case WEST -> left;
                case EAST -> right;
                case DOWN -> bottom;
                case NORTH -> back;
                default -> SideMode.DISABLED;
            };
        }

        public Draft setMode(Direction relativeDir, SideMode mode) {
            switch(relativeDir) {
                case WEST -> setLeft(mode);
                case EAST -> setRight(mode);
                case DOWN -> setBottom(mode);
                case NORTH -> setBack(mode);
                case SOUTH, UP -> {}
            }
            return this;
        }

        public IoSettings toSettings() {
            return new IoSettings(left, right, bottom, back, redstoneControl, allowIo, autoIo);
        }
    }
}
