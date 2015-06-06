package com.github.rschmitt.collider;

import org.testng.annotations.Test;

import java.util.Set;

import static com.github.rschmitt.collider.ClojureSet.toClojureSet;
import static com.github.rschmitt.collider.Collider.clojureSet;
import static java.util.Collections.emptySet;
import static java.util.stream.IntStream.range;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ClojureSetTest {
    @Test
    public void transientTest() {
        ClojureSet<String> before = clojureSet("asdf");
        TransientSet<String> strings = before.asTransient();

        strings.add("jkl;");
        ClojureSet<String> after = strings.toPersistent();

        assertEquals(after.size(), 2);
        assertTrue(after.containsAll(before));
        assertTrue(after.contains("jkl;"));
    }

    @Test
    public void collectorTest() {
        ClojureSet<Integer> collect = range(0, 10_000).boxed().collect(toClojureSet());

        assertEquals(collect.size(), 10_000);
        for (int i = 0; i < 10_000; i++) assertTrue(collect.contains(i));
    }

    @Test
    public void addContractTest() {
        ClojureSet<String> before = clojureSet("asdf");
        TransientSet<String> strings = before.asTransient();

        strings.add("jkl;");
        strings.add("jkl;");

        assertEquals(strings.toPersistent().size(), 2);
    }

    @Test
    public void removeContractTest() {
        ClojureSet<String> before = clojureSet("asdf");
        TransientSet<String> strings = before.asTransient();

        strings.remove("asdf");
        strings.remove("asdf");

        ClojureSet<String> persistent = strings.toPersistent();
        Set<Object> emptySet = emptySet();
        persistent.equals(emptySet);
        assertEquals(persistent, emptySet);
        assertEquals(persistent, emptySet);
    }
}
