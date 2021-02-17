package com.github.rschmitt.collider;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.rschmitt.collider.Collider.clojureList;
import static com.github.rschmitt.collider.Collider.toClojureList;
import static java.util.stream.IntStream.range;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    public void sublist() throws Exception {
        ClojureList<Integer> singles = range(0, 100).boxed().collect(toClojureList());
        ClojureList<Integer> sublist = singles.subList(0, 50);
        for (int i = 50; i < 100; i++) {
            sublist = sublist.append(i);
        }
        assertEquals(sublist, singles);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void destructiveUpdatesFail() {
        ClojureList<Integer> list = clojureList(14);
        assertThrows(UnsupportedOperationException.class, list::clear);
        assertThrows(UnsupportedOperationException.class, () -> list.add(15));
        assertThrows(UnsupportedOperationException.class, () -> list.add(15, 15));
        assertThrows(UnsupportedOperationException.class, () -> list.set(1, 15));
        assertThrows(UnsupportedOperationException.class, () -> list.remove(5));
        assertThrows(UnsupportedOperationException.class, () -> list.remove(new Integer(5)));
        assertThrows(UnsupportedOperationException.class, () -> list.removeAll(clojureList(14)));
        assertThrows(UnsupportedOperationException.class, () -> list.replaceAll(x -> x * 2));
        assertThrows(UnsupportedOperationException.class, () -> list.retainAll(clojureList(14)));
    }
}
