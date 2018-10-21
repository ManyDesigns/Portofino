package com.manydesigns.elements.blobs;

import java.util.Enumeration;
import java.util.Iterator;

/** Little helper class to create an enumeration as per the interface. */
public class IteratorEnumeration implements Enumeration<String> {
    Iterator<String> iterator;

    /** Constructs an enumeration that consumes from the underlying iterator. */
    IteratorEnumeration(Iterator<String> iterator) { this.iterator = iterator; }

    /** Returns true if more elements can be consumed, false otherwise. */
    public boolean hasMoreElements() { return this.iterator.hasNext(); }

    /** Gets the next element out of the iterator. */
    public String nextElement() { return this.iterator.next(); }
}
