package com.github.rschmitt.collider;

import org.testng.annotations.Test;

import java.util.Set;

import static com.github.rschmitt.collider.Collider.toClojureSet;
import static com.github.rschmitt.collider.Collider.clojureSet;
import static java.util.Collections.emptySet;
import static java.util.stream.IntStream.range;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

public class ClojureSetTest {
    @Test
    public void persistentOps() throws Exception {
        ClojureSet<Integer> initial = clojureSet(23);

        assertEquals(initial.with(23), initial);
        assertEquals(initial.without(23), initial.without(23));
        assertEquals(initial.with(42), clojureSet(23, 42));
        assertEquals(initial.with(42).size(), 2);
    }

    @Test
    public void collector() {
        ClojureSet<Integer> collect = range(0, 10_000).boxed().collect(toClojureSet());

        assertEquals(collect.size(), 10_000);
        for (int i = 0; i < 10_000; i++) assertTrue(collect.contains(i));
    }

    @Test
    public void transients() {
        ClojureSet<String> before = clojureSet("asdf");
        TransientSet<String> strings = before.asTransient();

        strings.add("jkl;");
        ClojureSet<String> after = strings.toPersistent();

        assertEquals(after.size(), 2);
        assertTrue(after.containsAll(before));
        assertTrue(after.contains("jkl;"));
    }

    @Test
    public void transientAdd() {
        ClojureSet<String> before = clojureSet("asdf");
        TransientSet<String> strings = before.asTransient();

        strings.add("jkl;");
        strings.add("jkl;");

        assertEquals(strings.toPersistent().size(), 2);
    }

    @Test
    public void transientRemove() {
        ClojureSet<String> before = clojureSet("asdf");
        TransientSet<String> strings = before.asTransient();

        strings.remove("asdf");
        strings.remove("asdf");

        ClojureSet<String> persistent = strings.toPersistent();
        Set<Object> emptySet = emptySet();
        assertEquals(persistent, emptySet);
    }

    @Test
    public void map() throws Exception {
        ClojureSet<Integer> initial = range(0, 100).boxed().collect(toClojureSet());

        ClojureSet<Integer> mapped = initial.map(x -> x % 3);

        assertEquals(mapped, clojureSet(0, 1, 2));
    }

    @Test
    public void filter() throws Exception {
        ClojureSet<Integer> initial = range(0, 100).boxed().collect(toClojureSet());

        ClojureSet<Integer> filtered = initial.filter(x -> x >= 50);

        assertEquals(filtered, range(50, 100).boxed().collect(toClojureSet()));
    }

    @Test
    public void exclude() throws Exception {
        ClojureSet<Integer> initial = range(0, 100).boxed().collect(toClojureSet());

        ClojureSet<Integer> filtered = initial.exclude(x -> x >= 50);

        assertEquals(filtered, range(0, 50).boxed().collect(toClojureSet()));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void destructiveUpdatesFail() {
        ClojureSet<Integer> set = clojureSet(14);
        expectThrows(UnsupportedOperationException.class, set::clear);
        expectThrows(UnsupportedOperationException.class, () -> set.add(15));
        expectThrows(UnsupportedOperationException.class, () -> set.remove(5));
        expectThrows(UnsupportedOperationException.class, () -> set.remove(new Integer(5)));
        expectThrows(UnsupportedOperationException.class, () -> set.removeAll(clojureSet(4)));
        expectThrows(UnsupportedOperationException.class, () -> set.retainAll(clojureSet()));
    }
}
