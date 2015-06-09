package com.github.rschmitt.collider;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

import clojure.lang.PersistentArrayMap;
import clojure.lang.PersistentHashSet;
import clojure.lang.PersistentVector;

import static com.github.rschmitt.collider.Collider.clojureList;
import static com.github.rschmitt.collider.Collider.clojureMap;
import static com.github.rschmitt.collider.Collider.clojureSet;
import static com.github.rschmitt.collider.Collider.intoClojureList;
import static com.github.rschmitt.collider.Collider.intoClojureMap;
import static com.github.rschmitt.collider.Collider.intoClojureSet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.IntStream.range;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

public class FactoryMethodTest {
    @Test
    public void downcasting() throws Exception {
        Set set = clojureSet();
        List list = clojureList();
        Map map = clojureMap();

        assertSame(intoClojureSet(set), set);
        assertSame(intoClojureList(list), list);
        assertSame(intoClojureMap(map), map);
    }

    @Test
    public void wrapping() throws Exception {
        PersistentHashSet persistentHashSet = PersistentHashSet.create();
        PersistentVector persistentVector = PersistentVector.create();
        PersistentArrayMap persistentArrayMap = PersistentArrayMap.EMPTY;

        ClojureSet clojureSet = intoClojureSet(persistentHashSet);
        ClojureList clojureList = intoClojureList(persistentVector);
        ClojureMap clojureMap = intoClojureMap(persistentArrayMap);

        Field setDelegate = ClojureSet.class.getDeclaredField("delegate");
        Field listDelegate = ClojureList.class.getDeclaredField("delegate");
        Field mapDelegate = ClojureMap.class.getDeclaredField("delegate");

        setDelegate.setAccessible(true);
        listDelegate.setAccessible(true);
        mapDelegate.setAccessible(true);

        assertSame(setDelegate.get(clojureSet), persistentHashSet);
        assertSame(listDelegate.get(clojureList), persistentVector);
        assertSame(mapDelegate.get(clojureMap), persistentArrayMap);
    }

    @Test
    public void copyConstructor() throws Exception {
        List<Integer> list = range(0, 100).boxed().collect(toList());
        Set<Integer> set = range(0, 100).boxed().collect(toSet());
        Map<Integer, String> map = range(0, 100).boxed().collect(toMap(t -> t, String::valueOf));

        assertEquals(intoClojureList(list), list);
        assertEquals(intoClojureSet(set), set);
        assertEquals(intoClojureMap(map), map);
    }
}
