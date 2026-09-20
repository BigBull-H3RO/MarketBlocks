package de.bigbull.marketblocks.core.config.toml;

/**
 * Categorizes configuration specifications to define their network synchronization lifecycle.
 */
public enum TomlConfigType {
    /**
     * Configuration affecting ONLY client-side state (e.g. GUI preferences, audio, visuals).
     * Loaded only on the client side, never synchronized over the network, and never overridden by a server.
     */
    CLIENT,

    /**
     * Server / gameplay configuration defining authoritative rules (e.g. shop limits, mechanics).
     * Authoritative on the server and synchronized to connected clients upon login or reload.
     * On the client, synchronized values are applied strictly in-memory and discarded upon disconnect.
     */
    SERVER
}
