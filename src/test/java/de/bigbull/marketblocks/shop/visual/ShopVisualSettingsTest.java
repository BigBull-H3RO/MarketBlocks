package de.bigbull.marketblocks.shop.visual;

import de.bigbull.marketblocks.feature.visual.npc.ShopVisualSettings;
import de.bigbull.marketblocks.feature.visual.npc.VillagerVisualProfession;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.Bootstrap;
import net.neoforged.fml.loading.LoadingModList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ShopVisualSettingsTest {

    @BeforeAll
    public static void setup() {
        if (LoadingModList.get() == null) {
            LoadingModList.of(List.of(), List.of(), List.of(), List.of(), Map.of());
        }
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void sanitizeNameAndNbtRoundTrip() {
        ShopVisualSettings settings = new ShopVisualSettings(
                true,
                "  Handler!?@#  ",
                VillagerVisualProfession.LIBRARIAN,
                true,
                false,
                true,
                true,
                false,
                1.5f,
                3.0f,
                0.2f,
                false,
                5,
                45.0f,
                true,
                0.4f
        );

        assertEquals("Handler", settings.npcName());

        CompoundTag tag = settings.save();
        ShopVisualSettings loaded = ShopVisualSettings.load(tag);

        assertTrue(loaded.npcEnabled());
        assertEquals("Handler", loaded.npcName());
        assertEquals(VillagerVisualProfession.LIBRARIAN, loaded.profession());
        assertTrue(loaded.purchaseParticlesEnabled());
        assertFalse(loaded.purchaseSoundsEnabled());
        assertTrue(loaded.paymentSlotSoundsEnabled());

        assertTrue(loaded.offerItemVisible());
        assertFalse(loaded.offerItemFullbright());
        assertEquals(1.5f, loaded.offerItemScale());
        assertEquals(3.0f, loaded.offerItemSpeed());
        assertEquals(0.2f, loaded.offerItemHeightOffset());
        assertFalse(loaded.offerItemBobbing());
        assertEquals(5, loaded.offerItemCount());
        assertEquals(45.0f, loaded.offerItemRotation());
        assertTrue(loaded.offerItemChaos());
        assertEquals(0.4f, loaded.offerItemSpread());
    }

    @Test
    void clampsUnsafeVisualValues() {
        ShopVisualSettings settings = new ShopVisualSettings(
                false,
                "",
                VillagerVisualProfession.NONE,
                true,
                true,
                true,
                true,
                false,
                Float.NaN,
                Float.POSITIVE_INFINITY,
                -99.0f,
                true,
                -5,
                -45.0f,
                false,
                -1.0f
        );

        assertEquals(1.0f, settings.offerItemScale());
        assertEquals(2.0f, settings.offerItemSpeed());
        assertEquals(-2.0f, settings.offerItemHeightOffset());
        assertEquals(1, settings.offerItemCount());
        assertEquals(315.0f, settings.offerItemRotation());
        assertEquals(0.0f, settings.offerItemSpread());
    }

    @Test
    void cycleProfessionWrapsAround() {
        assertEquals(VillagerVisualProfession.NONE, VillagerVisualProfession.WEAPONSMITH.next());
    }
}
