package de.bigbull.marketblocks.shop.visual;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShopVisualSettingsTest {

    @Test
    void sanitizeNameAndNbtRoundTrip() {
        ShopVisualSettings settings = new ShopVisualSettings(
                true,
                "  Handler!?@#  ",
                VillagerVisualProfession.LIBRARIAN,
                true,
                false
        );

        assertEquals("Handler", settings.npcName());

        CompoundTag tag = settings.save();
        ShopVisualSettings loaded = ShopVisualSettings.load(tag);

        assertTrue(loaded.npcEnabled());
        assertEquals("Handler", loaded.npcName());
        assertEquals(VillagerVisualProfession.LIBRARIAN, loaded.profession());
        assertTrue(loaded.purchaseParticlesEnabled());
        assertFalse(loaded.purchaseSoundsEnabled());
    }

    @Test
    void cycleProfessionWrapsAround() {
        assertEquals(VillagerVisualProfession.NONE, VillagerVisualProfession.WEAPONSMITH.next());
    }
}


