package com.github.rschmitt.collider;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.IntStream;

import static com.github.rschmitt.collider.ClojureList.toClojureList;
import static com.github.rschmitt.collider.Collider.clojureList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;

public class ClojureListTest {
    @Test
    public void test() {
        List<String> myStrings = clojureList("a", "b", "c");
        assertEquals(3, myStrings.size());
        assertEquals("a", myStrings.get(0));
    }

    @Test
    public void testAppend() {
        ClojureList<String> strings = clojureList("a", "b", "c");
        strings = strings.append("d");
        assertEquals(4, strings.size());
        assertEquals("d", strings.get(3));
    }

    @Test
    public void testStreams() {
        ClojureList<String> strings = clojureList("a", "b", "c");
        strings = strings.append("d");

        assertEquals(4, strings.stream().count());
    }

    @Test
    public void testCollector() {
        ClojureList<Integer> singles = IntStream.range(0, 100).boxed().collect(toClojureList());
        for (int i = 0; i < 100; i++) assertEquals(singles.get(i).intValue(), i);

        ClojureList<Integer> doubles = singles.map(x -> x * 2);
        assertEquals(doubles.size(), singles.size());
        for (int i = 0; i < 100; i++) assertEquals(doubles.get(i).intValue(), i * 2);
    }

    @Test
    public void destructiveUpdatesFail() {
        ClojureList<Integer> list = clojureList(14);
        assertThrows(list::clear);
        assertThrows(() -> list.add(15));
        assertThrows(() -> list.add(15, 15));
        assertThrows(() -> list.set(1, 15));
        assertThrows(() -> list.remove(5));
        assertThrows(() -> list.remove(new Integer(5)));
        assertThrows(() -> list.removeAll(list));
        assertThrows(() -> list.replaceAll(x -> x * 2));
        assertThrows(() -> list.retainAll(list));
    }
}
