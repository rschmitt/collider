package com.github.rschmitt.collider;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.github.rschmitt.collider.Collider.clojureSet;
import static com.github.rschmitt.collider.Collider.transientSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TransientSetTest {
    @Test
    @SuppressWarnings("unchecked")
    public void reuseFails() throws Exception {
        TransientSet transientSet = transientSet();
        transientSet.toPersistent();
        assertThrows(IllegalAccessError.class, () -> transientSet.add(new Object()));
    }

    @Test
    public void remove() throws Exception {
        TransientSet transientSet = clojureSet("a", "b", "c").asTransient();
        assertTrue(transientSet.contains("a"));

        transientSet.remove("a");
        assertFalse(transientSet.contains("a"));

        transientSet.remove("b");
        assertFalse(transientSet.contains("a"));

        assertTrue(transientSet.contains("c"));
        assertEquals(transientSet.toPersistent(), clojureSet("c"));
    }

    @Test
    public void idempotentRemove() throws Exception {
        TransientSet transientSet = transientSet();
        transientSet.remove(new Object());
        assertEquals(transientSet.toPersistent(), clojureSet());
    }

    @Test
    public void add() throws Exception {
        TransientSet transientSet = transientSet();
        transientSet.add("a");
        transientSet.add("b");
        transientSet.addAll(clojureSet("b", "c", "d"));
        transientSet.addAll(Arrays.asList("d", "e", "f"));
        assertEquals(transientSet.toPersistent(), clojureSet("a", "b", "c", "d", "e", "f"));
    }

    @Test
    public void nulls() throws Exception {
        TransientSet transientSet = transientSet();
        assertEquals(0, transientSet.size());

        transientSet.add(null);
        assertTrue(transientSet.contains(null));
        assertEquals(1, transientSet.size());

        transientSet.remove(null);
        assertFalse(transientSet.contains(null));
        assertEquals(0, transientSet.size());

        transientSet.add(null);
        assertEquals(transientSet.toPersistent(), clojureSet(null));
    }
}
