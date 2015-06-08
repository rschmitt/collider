package com.github.rschmitt.collider;

import org.testng.annotations.Test;

import java.util.List;

import static com.github.rschmitt.collider.ClojureList.toClojureList;
import static com.github.rschmitt.collider.Collider.clojureList;
import static java.util.stream.IntStream.range;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;

public class ClojureListTest {
    @Test
    public void readOnlyOps() {
        List<String> myStrings = clojureList("a", "b", "c");
        assertEquals(3, myStrings.size());
        assertEquals("a", myStrings.get(0));
    }

    @Test
    public void persistentOps() {
        ClojureList<String> strings = clojureList("a", "b", "c");
        strings = strings.append("d");
        assertEquals(4, strings.size());
        assertEquals("d", strings.get(3));
    }

    @Test
    public void streams() {
        ClojureList<String> strings = clojureList("a", "b", "c");
        strings = strings.append("d");

        assertEquals(4, strings.stream().count());
    }

    @Test
    public void collector() {
        ClojureList<Integer> singles = range(0, 100).boxed().collect(toClojureList());
        for (int i = 0; i < 100; i++) assertEquals(singles.get(i).intValue(), i);
    }

    @Test
    public void map() throws Exception {
        ClojureList<Integer> initial = range(0, 100).boxed().collect(toClojureList());

        ClojureList<Integer> filtered = initial.filter(x -> x >= 50);

        assertEquals(filtered, range(50, 100).boxed().collect(toClojureList()));
    }

    @Test
    public void filter() throws Exception {
        ClojureList<Integer> singles = range(0, 100).boxed().collect(toClojureList());

        ClojureList<Integer> doubles = singles.map(x -> x * 2);

        assertEquals(doubles.size(), singles.size());
        for (int i = 0; i < 100; i++) assertEquals(doubles.get(i).intValue(), i * 2);
    }

    @Test
    public void exclude() throws Exception {
        ClojureList<Integer> initial = range(0, 100).boxed().collect(toClojureList());

        ClojureList<Integer> filtered = initial.exclude(x -> x >= 50);

        assertEquals(filtered, range(0, 50).boxed().collect(toClojureList()));
    }

    @Test
    public void transientOps() throws Exception {
        ClojureList<Integer> singles = range(0, 100).boxed().collect(toClojureList());
        TransientList<Integer> tr = singles.asTransient();

        range(100, 200).forEach(tr::append);
        ClojureList<Integer> moreSingles = tr.toPersistent();

        assertEquals(moreSingles.size(), 200);
        assertEquals(moreSingles, range(0, 200).boxed().collect(toClojureList()));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void destructiveUpdatesFail() {
        ClojureList<Integer> list = clojureList(14);
        expectThrows(UnsupportedOperationException.class, list::clear);
        expectThrows(UnsupportedOperationException.class, () -> list.add(15));
        expectThrows(UnsupportedOperationException.class, () -> list.add(15, 15));
        expectThrows(UnsupportedOperationException.class, () -> list.set(1, 15));
        expectThrows(UnsupportedOperationException.class, () -> list.remove(5));
        expectThrows(UnsupportedOperationException.class, () -> list.remove(new Integer(5)));
        expectThrows(UnsupportedOperationException.class, () -> list.removeAll(clojureList(14)));
        expectThrows(UnsupportedOperationException.class, () -> list.replaceAll(x -> x * 2));
        expectThrows(UnsupportedOperationException.class, () -> list.retainAll(clojureList(14)));
    }
}
