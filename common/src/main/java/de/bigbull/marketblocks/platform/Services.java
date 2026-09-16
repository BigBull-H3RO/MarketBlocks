package de.bigbull.marketblocks.platform;

import de.bigbull.marketblocks.Constants;
import de.bigbull.marketblocks.platform.services.IItemTransferHelper;
import de.bigbull.marketblocks.platform.services.INetworkHelper;
import de.bigbull.marketblocks.platform.services.IPlatformHelper;

import java.util.ServiceLoader;

public class Services {

    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);
    public static final INetworkHelper NETWORK = load(INetworkHelper.class);
    public static final IItemTransferHelper ITEM_TRANSFER = load(IItemTransferHelper.class);

    public static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        Constants.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}