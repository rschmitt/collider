package com.github.rschmitt.collider;

import org.testng.annotations.Test;

import java.util.Map.Entry;
import java.util.stream.Stream;

import static com.github.rschmitt.collider.ClojureMap.*;
import static com.github.rschmitt.collider.Collider.clojureMap;
import static java.util.function.Function.identity;
import static org.testng.Assert.*;

public class ClojureMapTest {
    @Test
    public void smokeTest() {
        ClojureMap<String, String> emptyMap = clojureMap();

        ClojureMap<String, String> assoc = emptyMap.assoc("key", "value");
        ClojureMap<String, String> dissoc = assoc.dissoc("key");

        assertTrue(assoc.containsKey("key"));
        assertEquals(assoc.size(), 1);
        assertEquals(assoc.get("key"), "value");
        assertEquals(dissoc, emptyMap);
    }

    @Test
    public void convenienceMethods() {
        ClojureMap<String, Integer> collect = Stream.of("two", "three", "four").collect(toClojureMap(identity(), String::length));

        ClojureMap<String, Integer> doubled = collect.mapValues(x -> x * 2);
        assertEquals(doubled.get("two").intValue(), 6);

        ClojureMap<String, Integer> uppercased = collect.mapKeys(String::toUpperCase);
        assertEquals(uppercased.get("THREE").intValue(), 5);
        assertEquals(uppercased.mapKeys(String::toLowerCase), collect);

        ClojureMap<String, Integer> filteredKeys = collect.filterKeys(x -> x.startsWith("t"));
        assertEquals(filteredKeys.size(), 2);
        assertEquals(filteredKeys.get("two").intValue(), 3);
        assertEquals(filteredKeys.get("three").intValue(), 5);
        assertNull(filteredKeys.get("four"));

        ClojureMap<String, Integer> filteredVals = collect.filterValues(v -> v >= 4);
        assertEquals(filteredVals.size(), 2);
        assertNull(filteredVals.get("two"));
        assertEquals(filteredVals.get("three").intValue(), 5);
        assertEquals(filteredVals.get("four").intValue(), 4);

        ClojureMap<String, Integer> excludedKeys = collect.excludeKeys(x -> x.startsWith("t"));
        assertEquals(excludedKeys, clojureMap("four", 4));

        ClojureMap<String, Integer> excludedVals = collect.excludeValues(v -> v >= 4);
        assertEquals(excludedVals, clojureMap("two", 3));
    }

    @Test
    public void factoryMethods() {
        ClojureMap<String, Integer> expected = clojureMap();

        expected = expected.assoc("a", 1);
        assertEquals(clojureMap("a", 1), expected);

        expected = expected.assoc("b", 2);
        assertEquals(clojureMap("a", 1, "b", 2), expected);

        expected = expected.assoc("c", 3);
        assertEquals(clojureMap("a", 1, "b", 2, "c", 3), expected);
    }

    @Test
    public void overwrite() {
        assertEquals(create("a", 1).assoc("a", 3), create("a", 3));
    }

    @Test
    public void strictCollector() {
        ClojureMap<String, Integer> initial = clojureMap("one", 1, "two", 2, "six", 6);

        ClojureMap<Integer, Integer> collect = initial
                .entrySet()
                .stream()
                .collect(toStrictClojureMap(e -> e.getKey().length(), Entry::getValue, Math::max));

        assertEquals(collect, clojureMap(3, 6));
    }

    @Test
    public void merge() throws Exception {
        ClojureMap<String, Integer> actual = clojureMap("a", 1)
                .merge(clojureMap("a", 2, "b", 4),
                        clojureMap("a", 3, "d", 4),
                        clojureMap("b", null, "e", null));
        assertEquals(actual, clojureMap("a", 3, "b", null, "d", 4, "e", null));
    }

    @Test
    public void mergeOptimizations() throws Exception {
        ClojureMap<String, Integer> map = clojureMap("a", 1);
        ClojureMap empty = clojureMap();

        assertSame(map, map.merge());
        assertSame(map, map.merge(empty));
        assertSame(map, map.merge(empty, empty));
        assertSame(map, empty.merge(map));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void destructiveUpdatesFail() {
        ClojureMap<String, Integer> map = clojureMap("a", 0);
        expectThrows(UnsupportedOperationException.class, map::clear);
        expectThrows(UnsupportedOperationException.class, () -> map.put("b", 15));
        expectThrows(UnsupportedOperationException.class, () -> map.putAll(clojureMap()));
        expectThrows(UnsupportedOperationException.class, () -> map.remove("a"));
        expectThrows(UnsupportedOperationException.class, () -> map.remove("a", 0));
        expectThrows(UnsupportedOperationException.class, () -> map.replace("a", 0));
        expectThrows(UnsupportedOperationException.class, () -> map.replace("a", 0, 2));
        expectThrows(UnsupportedOperationException.class, () -> map.putIfAbsent("b", 4));
        expectThrows(UnsupportedOperationException.class, () -> map.compute("a", String::codePointAt));
        expectThrows(UnsupportedOperationException.class, () -> map.computeIfAbsent("b", String::length));
        expectThrows(UnsupportedOperationException.class, () -> map.computeIfPresent("a", String::codePointAt));
        expectThrows(UnsupportedOperationException.class, () -> map.merge("a", 4, (x, y) -> x + y));
        expectThrows(UnsupportedOperationException.class, () -> map.compute("a", String::codePointAt));
        expectThrows(UnsupportedOperationException.class, () -> map.computeIfAbsent("b", String::length));
        expectThrows(UnsupportedOperationException.class, () -> map.computeIfPresent("a", String::codePointAt));
    }
}
