package de.bigbull.marketblocks;

import net.minecraft.world.level.block.entity.SkullBlockEntity;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class PrintMethods {
    public static void main(String[] args) {
        System.out.println("--- SkullBlockEntity Methods ---");
        for (Method m : SkullBlockEntity.class.getDeclaredMethods()) {
            if (Modifier.isPublic(m.getModifiers())) {
                System.out.println(m.getName());
                for (Class<?> p : m.getParameterTypes()) {
                    System.out.println("  Param: " + p.getName());
                }
                System.out.println("  Return: " + m.getReturnType().getName());
            }
        }
    }
}
