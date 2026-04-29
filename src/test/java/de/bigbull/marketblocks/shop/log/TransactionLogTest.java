package de.bigbull.marketblocks.shop.log;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.loading.LoadingModList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionLogTest {

    @BeforeAll
    public static void setup() {
        if (LoadingModList.get() == null) {
            LoadingModList.of(List.of(), List.of(), List.of(), List.of(), Map.of());
        }
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    public void testFifoLimit() {
        // Simuliere die Logik aus ShopTransactionLogSavedData
        int maxEntries = 5;
        ArrayDeque<TransactionLogEntry> deque = new ArrayDeque<>();

        for (int i = 0; i < 10; i++) {
            TransactionLogEntry entry = new TransactionLogEntry(
                    1000L + i,
                    UUID.randomUUID(),
                    "Player" + i,
                    List.of(),
                    List.of(),
                    1,
                    TransactionLogEntry.PurchaseKind.SINGLE
            );
            
            deque.addFirst(entry);
            while (deque.size() > maxEntries) {
                deque.removeLast();
            }
        }

        assertEquals(maxEntries, deque.size());
        assertEquals("Player9", deque.peekFirst().buyerName());
        assertEquals("Player5", deque.peekLast().buyerName());
    }

    @Test
    public void testSmartStacking() {
        UUID buyerId = UUID.randomUUID();
        String buyerName = "TestPlayer";
        
        ItemStack apple = new ItemStack(Items.APPLE, 1);
        ItemStack gold = new ItemStack(Items.GOLD_INGOT, 5);

        TransactionLogEntry entry1 = new TransactionLogEntry(
                1000L,
                buyerId,
                buyerName,
                List.of(gold),
                List.of(apple),
                1,
                TransactionLogEntry.PurchaseKind.SINGLE
        );

        // Gleicher Käufer, gleiche Items, 10 Sekunden später
        TransactionLogEntry entry2 = new TransactionLogEntry(
                1010L,
                buyerId,
                buyerName,
                List.of(gold),
                List.of(apple),
                1,
                TransactionLogEntry.PurchaseKind.SINGLE
        );

        assertTrue(entry1.canMergeWith(entry2), "Entries should be mergeable");

        TransactionLogEntry merged = entry1.mergeWith(entry2);
        assertEquals(1010L, merged.epochSecond());
        assertEquals(1, merged.boughtStacks().get(0).getCount(), "Item count should stay unchanged");
        assertEquals(5, merged.paidStacks().get(0).getCount(), "Item count should stay unchanged");
        assertEquals(2, merged.aggregationCount(), "Aggregation count should be incremented");
    }

    @Test
    public void testNoMergeDifferentBuyers() {
        ItemStack apple = new ItemStack(Items.APPLE, 1);
        
        TransactionLogEntry entry1 = new TransactionLogEntry(1000L, UUID.randomUUID(), "P1", List.of(), List.of(apple), 1, TransactionLogEntry.PurchaseKind.SINGLE);
        TransactionLogEntry entry2 = new TransactionLogEntry(1001L, UUID.randomUUID(), "P2", List.of(), List.of(apple), 1, TransactionLogEntry.PurchaseKind.SINGLE);

        assertFalse(entry1.canMergeWith(entry2), "Different buyers should not merge");
    }

    @Test
    public void testNoMergeAfterTimeLimit() {
        UUID buyerId = UUID.randomUUID();
        ItemStack apple = new ItemStack(Items.APPLE, 1);

        TransactionLogEntry entry1 = new TransactionLogEntry(1000L, buyerId, "P1", List.of(), List.of(apple), 1, TransactionLogEntry.PurchaseKind.SINGLE);
        // 21 seconds later (over the 20 second merge window)
        TransactionLogEntry entry2 = new TransactionLogEntry(1021L, buyerId, "P1", List.of(), List.of(apple), 1, TransactionLogEntry.PurchaseKind.SINGLE);

        assertFalse(entry1.canMergeWith(entry2), "Entries separated by more than 20 seconds should not merge");
    }

    @Test
    public void testNoMergeDifferentPurchaseKind() {
        UUID buyerId = UUID.randomUUID();
        ItemStack apple = new ItemStack(Items.APPLE, 1);

        TransactionLogEntry single = new TransactionLogEntry(1000L, buyerId, "P1", List.of(), List.of(apple), 1, TransactionLogEntry.PurchaseKind.SINGLE);
        TransactionLogEntry shift = new TransactionLogEntry(1005L, buyerId, "P1", List.of(), List.of(apple), 1, TransactionLogEntry.PurchaseKind.SHIFT);

        assertFalse(single.canMergeWith(shift), "Single and shift purchases must not merge");
    }
}
