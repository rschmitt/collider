package com.github.rschmitt.collider;

import org.junit.jupiter.api.Test;

import static com.github.rschmitt.collider.Collider.clojureMap;
import static com.github.rschmitt.collider.Collider.transientMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TransientMapTest {
    @Test
    @SuppressWarnings("unchecked")
    public void reuseFails() throws Exception {
        TransientMap transientMap = transientMap();
        transientMap.toPersistent();
        assertThrows(IllegalAccessError.class, () -> transientMap.put(new Object(), new Object()));
    }

    @Test
    public void putOverwrites() throws Exception {
        TransientMap transientMap = transientMap();

        transientMap.put("a", 1);
        assertEquals(transientMap.get("a"), 1);

        transientMap.put("a", 2);
        assertEquals(transientMap.get("a"), 2);

        transientMap.put("b", 3);
        assertEquals(transientMap.get("b"), 3);

        assertEquals(transientMap.toPersistent(), clojureMap("a", 2, "b", 3));
    }

    @Test
    public void remove() throws Exception {
        TransientMap transientMap = transientMap();
        assertEquals(transientMap.size(), 0);

        transientMap.put("a", 1);
        assertEquals(transientMap.size(), 1);

        transientMap.remove("a");
        assertEquals(transientMap.size(), 0);

        transientMap.remove("a");
        assertEquals(transientMap.size(), 0);
    }

    @Test
    public void nullValue() throws Exception {
        TransientMap transientMap = transientMap();

        transientMap.put("key", null);

        assertEquals(transientMap.size(), 1);
        assertNull(transientMap.get("key"));
        assertEquals(transientMap.toPersistent(), clojureMap("key", null));
    }
}
