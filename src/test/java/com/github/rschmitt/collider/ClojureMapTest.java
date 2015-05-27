package com.github.rschmitt.collider;

import org.testng.annotations.Test;

import java.util.stream.Stream;

import static com.github.rschmitt.collider.ClojureMap.create;
import static com.github.rschmitt.collider.ClojureMap.toClojureMap;
import static com.github.rschmitt.collider.ClojureMap.toStrictClojureMap;
import static java.util.function.Function.identity;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class ClojureMapTest {
    @Test
    public void smokeTest() {
        ClojureMap<String, String> emptyMap = create();

        ClojureMap<String, String> assoc = emptyMap.assoc("key", "value");

        assertTrue(assoc.containsKey("key"));
        assertEquals(assoc.size(), 1);
        assertEquals(assoc.get("key"), "value");
    }

    @Test
    public void testConvenienceMethods() {
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
    }

    @Test
    public void testFactoryMethods() {
        ClojureMap<String, Integer> expected = create();

        expected = expected.assoc("a", 1);
        assertEquals(create("a", 1), expected);

        expected = expected.assoc("b", 2);
        assertEquals(create("a", 1, "b", 2), expected);

        expected = expected.assoc("c", 3);
        assertEquals(create("a", 1, "b", 2, "c", 3), expected);
    }

    @Test
    public void testOverwrite() {
        assertEquals(create("a", 1).assoc("a", 3), create("a", 3));
    }

    @Test
    public void testStrictCollector() {
        ClojureMap<String, Integer> initial = ClojureMap.create("one", 1, "two", 2, "six", 6);

        ClojureMap<Integer, Integer> collect = initial
                .entrySet()
                .stream()
                .collect(toStrictClojureMap(e -> e.getKey().length(), e -> e.getValue(), Math::max));

        assertEquals(collect, ClojureMap.create(3, 6));
    }
}
