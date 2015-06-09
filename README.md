[![Build Status](http://img.shields.io/travis/rschmitt/collider.svg)](https://travis-ci.org/rschmitt/collider)
[![License](https://img.shields.io/github/license/rschmitt/collider.svg)](https://creativecommons.org/about/cc0)

# Collider

Collider is a tiny library that provides immutable persistent collections for
Java. It does this by wrapping Clojure's collections in an object-oriented and
type-safe facade.

## Examples

Collider collections can be created by calling one of the factory methods in
the `Collider` class. The resulting collection can be used like any other
unmodifiable Java collection--for instance, `ClojureList<T>` implements the
standard `java.util.List<T>` interface.

```java
List<String> myStrings = clojureList("a", "b", "c");
assertEquals(3, myStrings.size());
assertEquals("a", myStrings.get(0));
```

Collider collections can also be created by transforming another collection.
The technique of persistent modification is used to efficiently create modified
copies while leaving the immutable original untouched.

```java
ClojureMap<String, String> emptyMap = clojureMap();

ClojureMap<String, String> assoc = emptyMap.assoc("key", "value");
assertTrue(assoc.containsKey("key"));
assertEquals(assoc.size(), 1);
assertEquals(assoc.get("key"), "value");

ClojureMap<String, String> dissoc = assoc.dissoc("key");
assertEquals(dissoc, emptyMap);
```

Since Collider specifically targets Java 8, not only can its collections be
used with the Stream API, but they also include some convenience methods to
make common use cases (such as mapping a function over a list) more concise.

```java
ClojureList<Integer> evens = clojureList(0, 2, 4, 6, 8);  // [0, 2, 4, 6, 8]
ClojureList<Integer> odds = evens.map(x -> x + 1);        // [1, 3, 5, 7, 9]
```

Collider also provides collectors that work with the Stream API.

```java
ClojureList<Integer> singles = IntStream.range(0, 100).boxed().collect(toClojureList());
```

Internally, these collectors use Clojure's [transient
collections](http://clojure.org/transients) to efficiently accumulate a result.
Transients are generally used as follows:

1. Obtain a transient version of a collection from a persistent collection in
   O(1) time
2. Mutate the transient collection
3. Turn the transient collection into a persistent collection in O(1) time

Collider makes transients available directly. Continuing the above example:

```java
TransientList<Integer> tr = singles.asTransient();
range(100, 200).forEach(tr::append);
ClojureList<Integer> moreSingles = tr.toPersistent();

assertEquals(moreSingles.size(), 200);
assertEquals(moreSingles, range(0, 200).boxed().collect(toClojureList()));
```
