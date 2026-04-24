package de.bigbull.marketblocks.shop.log;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;
import java.util.ArrayDeque;
import java.util.UUID;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionLogTest {

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
                    List.of()
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
}
