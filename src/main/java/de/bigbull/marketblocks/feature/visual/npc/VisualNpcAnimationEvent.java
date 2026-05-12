package de.bigbull.marketblocks.feature.visual.npc;

/**
 * Tiny wire format for one-shot client animations triggered by the server.
 */
public final class VisualNpcAnimationEvent {
    public static final byte NONE = 0;
    public static final byte SPAWN = 1;
    public static final byte DESPAWN = 2;

    private VisualNpcAnimationEvent() {
    }
}

