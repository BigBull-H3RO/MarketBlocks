package de.bigbull.marketblocks.feature.visual.npc;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import java.lang.reflect.Method;

public class TestReflection {
    public static void test(MinecraftSessionService service) {
        for (Method m : service.getClass().getMethods()) {
            System.out.println(m.getName() + " " + m.getParameterCount());
        }
    }
}
