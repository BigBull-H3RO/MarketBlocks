package de.bigbull.marketblocks.platform;

import de.bigbull.marketblocks.platform.services.IPlatformHelper;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.function.Consumer;

public class FabricPlatformHelper implements IPlatformHelper {

    public record OpenMenuData(byte[] bytes) {
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenMenuData> STREAM_CODEC = StreamCodec.of(
                (buf, val) -> buf.writeByteArray(val.bytes),
                buf -> new OpenMenuData(buf.readByteArray())
        );
    }

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public void openMenu(ServerPlayer player, MenuProvider provider, Consumer<RegistryFriendlyByteBuf> extraData) {
        RegistryFriendlyByteBuf temp = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.registryAccess());
        extraData.accept(temp);
        byte[] bytes = new byte[temp.readableBytes()];
        temp.readBytes(bytes);
        OpenMenuData data = new OpenMenuData(bytes);

        player.openMenu(new ExtendedScreenHandlerFactory<OpenMenuData>() {
            @Override
            public OpenMenuData getScreenOpeningData(ServerPlayer p) {
                return data;
            }

            @Override
            public Component getDisplayName() {
                return provider.getDisplayName();
            }

            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player p) {
                return provider.createMenu(syncId, playerInventory, p);
            }
        });
    }

    @Override
    public void invalidateCapabilities(Level level, BlockPos pos) {
        // Fabric transfer API lookups do not use capability caches
    }
}
