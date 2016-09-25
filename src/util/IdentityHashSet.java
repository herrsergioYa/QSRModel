/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 *
 * @author HerrSergio
 */
public class IdentityHashSet<E> extends AbstractSet<E> {

    private Set<E> set = Collections.newSetFromMap(new IdentityHashMap<E, Boolean>());

    public IdentityHashSet() {
    }
    
    public IdentityHashSet(Collection<? extends E> c) {
        addAll(c);
    }
    
    @Override
    public boolean removeAll(Collection<?> clctn) {
        return set.removeAll(clctn); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int hashCode() {
        return set.hashCode(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean equals(Object o) {
        return set.equals(o); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Spliterator<E> spliterator() {
        return set.spliterator(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String toString() {
        return set.toString(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void clear() {
        set.clear(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean retainAll(Collection<?> clctn) {
        return set.retainAll(clctn); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean addAll(Collection<? extends E> clctn) {
        return set.addAll(clctn); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean containsAll(Collection<?> clctn) {
        return set.containsAll(clctn); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean remove(Object o) {
        return set.remove(o); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean add(E e) {
        return set.add(e); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> T[] toArray(T[] ts) {
        return set.toArray(ts); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object[] toArray() {
        return set.toArray(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean contains(Object o) {
        return set.contains(o); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isEmpty() {
        return set.isEmpty(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Stream<E> parallelStream() {
        return set.parallelStream(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Stream<E> stream() {
        return set.stream(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean removeIf(Predicate<? super E> prdct) {
        return set.removeIf(prdct); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void forEach(Consumer<? super E> cnsmr) {
        set.forEach(cnsmr); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Iterator<E> iterator() {
        return set.iterator();
    }

    @Override
    public int size() {
        return set.size();
    }

}
