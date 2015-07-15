package com.github.rschmitt.collider;

import org.testng.annotations.Test;

import static com.github.rschmitt.collider.Collider.clojureList;
import static com.github.rschmitt.collider.Collider.transientList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertThrows;

public class TransientListTest {
    @Test
    @SuppressWarnings("unchecked")
    public void reuseFails() throws Exception {
        TransientList transientList = transientList();
        transientList.toPersistent();
        assertThrows(IllegalAccessError.class, () -> transientList.append(new Object()));
    }

    @Test
    public void append() throws Exception {
        TransientList transientList = transientList();

        transientList.append("a");
        transientList.append("a");
        transientList.append("a");

        assertEquals(transientList.toPersistent(), clojureList("a", "a", "a"));
    }

    @Test
    public void readOperations() throws Exception {
        TransientList<Integer> transientList = clojureList(0, 1, 2, 3, 4).asTransient();
        for (int i = 0; i < 5; i++) assertEquals(transientList.get(i).intValue(), i);
        assertThrows(IndexOutOfBoundsException.class, () -> transientList.get(6));
    }

    @Test
    public void setOperations() throws Exception {
        TransientList<Integer> transientList = clojureList(0, 1, 2, 3, 4).asTransient();
        assertThrows(IndexOutOfBoundsException.class, () -> transientList.set(6, 5));

        transientList.append(5);
        transientList.append(6);
        assertEquals(transientList.get(6).intValue(), 6);

        transientList.set(6, 66);
        assertEquals(transientList.get(6).intValue(), 66);
    }

    @Test
    public void size() throws Exception {
        TransientList transientList = transientList();
        assertEquals(transientList.size(), 0);

        transientList.append("a");
        assertEquals(transientList.size(), 1);

        transientList.append("a");
        assertEquals(transientList.size(), 2);

        transientList.append("a");
        assertEquals(transientList.size(), 3);

        transientList.set(2, "b");
        assertEquals(transientList.size(), 3);
    }

    @Test
    public void nulls() throws Exception {
        TransientList transientList = transientList();

        transientList.append(null);
        assertEquals(transientList.size(), 1);

        transientList.append("a");
        assertEquals(transientList.size(), 2);

        transientList.set(1, null);
        assertEquals(transientList.size(), 2);
        assertNull(transientList.get(0));
        assertNull(transientList.get(1));

        assertEquals(transientList.toPersistent(), clojureList(null, null));
    }
}
