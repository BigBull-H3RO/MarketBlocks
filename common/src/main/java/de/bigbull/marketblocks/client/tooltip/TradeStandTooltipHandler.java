package de.bigbull.marketblocks.client.tooltip;

import com.mojang.blaze3d.platform.InputConstants;
import de.bigbull.marketblocks.core.init.RegistriesInit;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Handles client-side tooltip display for the Trade Stand item.
 * Shows a compact "[Shift] for info" hint by default, and reveals
 * the full showcase interaction instructions when Shift or Ctrl is held.
 */
public final class TradeStandTooltipHandler {

    private TradeStandTooltipHandler() {}

    public static void appendTooltip(ItemStack stack, List<Component> lines) {
        if (RegistriesInit.TRADE_STAND_ITEM == null || RegistriesInit.TRADE_STAND_ITEM.get() == null) {
            return;
        }
        if (!stack.is(RegistriesInit.TRADE_STAND_ITEM.get())) {
            return;
        }

        if (isExpanded()) {
            lines.add(Component.translatable("tooltip.marketblocks.trade_stand.showcase_info")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            lines.add(Component.translatable("tooltip.marketblocks.hold_shift",
                    Component.literal("[Shift]").withStyle(ChatFormatting.YELLOW))
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    public static boolean isExpanded() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getWindow() == null) {
            return false;
        }
        var window = mc.getWindow();
        return InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT)
                || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT)
                || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_CONTROL)
                || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_CONTROL);
    }
}
