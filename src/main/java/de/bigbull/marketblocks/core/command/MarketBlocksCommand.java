package de.bigbull.marketblocks.core.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.core.data.ShopDirectorySavedData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.List;

@EventBusSubscriber(modid = MarketBlocks.MODID)
public class MarketBlocksCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("marketblocks")
                .then(Commands.literal("list")
                        .executes(MarketBlocksCommand::executeList)
                )
        );
    }

    private static int executeList(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ShopDirectorySavedData data = ShopDirectorySavedData.get(source.getLevel());
        List<ShopDirectorySavedData.ShopEntry> shops = data.getShops();

        if (shops.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No shops found on this server."), false);
            return 1;
        }

        source.sendSuccess(() -> Component.literal("--- MarketBlocks Shop List ---"), false);
        for (ShopDirectorySavedData.ShopEntry shop : shops) {
            String shopName = shop.shopName();
            if (shopName == null || shopName.isEmpty()) shopName = "Unnamed Shop";

            String owner = shop.ownerName() != null ? shop.ownerName() : "Unknown";
            String status = shop.isClosed() ? "§c[Closed]" : "§a[Open]";
            GlobalPos pos = shop.pos();

            MutableComponent text = Component.literal(String.format("%s %s §rby %s", status, shopName, owner));

            // If sender is a player with OP, add teleport click event
            if (source.getEntity() instanceof ServerPlayer player && player.hasPermissions(2)) {
                text.append(Component.literal(" §7[TP]").withStyle(style ->
                        style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                        String.format("/execute in %s run tp @s %d %d %d", pos.dimension().location(), pos.pos().getX(), pos.pos().getY(), pos.pos().getZ())))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to teleport")))
                ));
            } else {
                text.append(Component.literal(String.format(" §7(%d, %d, %d)", pos.pos().getX(), pos.pos().getY(), pos.pos().getZ())));
            }

            source.sendSuccess(() -> text, false);
        }

        return 1;
    }
}
