package de.bigbull.marketblocks.feature.visual.npc;

public enum VisualNpcPlacementResult {
    OK(""),
    NO_STAND_SURFACE("gui.marketblocks.visuals.error.no_surface"),
    BLOCKED_ABOVE("gui.marketblocks.visuals.error.space_blocked");

    private final String translationKey;

    VisualNpcPlacementResult(String translationKey) {
        this.translationKey = translationKey;
    }

    public boolean canSpawn() {
        return this == OK;
    }

    public String translationKey() {
        return translationKey;
    }
}

