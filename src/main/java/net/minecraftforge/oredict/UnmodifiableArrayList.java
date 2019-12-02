package net.minecraftforge.oredict;

import java.util.*;

//Pulled from Collections.UnmodifiableList, as we need to explicitly subclass ArrayList for backward compatibility.
//Delete this class in 1.8 when we loose the ArrayList specific return types.
public class UnmodifiableArrayList<E> extends ArrayList<E> {
 final ArrayList<? extends E> list;

 UnmodifiableArrayList(ArrayList<? extends E> list) {
  super(0);
  this.list = list;
 }

 public ListIterator<E> listIterator() {
  return listIterator(0);
 }

 public boolean equals(Object o) {
  return o == this || list.equals(o);
 }

 public int hashCode() {
  return list.hashCode();
 }

 public E get(int index) {
  return list.get(index);
 }

 public int indexOf(Object o) {
  return list.indexOf(o);
 }

 public int lastIndexOf(Object o) {
  return list.lastIndexOf(o);
 }

 public int size() {
  return list.size();
 }

 public boolean isEmpty() {
  return list.isEmpty();
 }

 public boolean contains(Object o) {
  return list.contains(o);
 }

 public Object[] toArray() {
  return list.toArray();
 }

 public <T> T[] toArray(T[] a) {
  return list.toArray(a);
 }

 public String toString() {
  return list.toString();
 }

 public boolean containsAll(Collection<?> coll) {
  return list.containsAll(coll);
 }

 public E set(int index, E element) {
  throw new UnsupportedOperationException();
 }

 public void add(int index, E element) {
  throw new UnsupportedOperationException();
 }

 public E remove(int index) {
  throw new UnsupportedOperationException();
 }

 public boolean add(E e) {
  throw new UnsupportedOperationException();
 }

 public boolean remove(Object o) {
  throw new UnsupportedOperationException();
 }

 public void clear() {
  throw new UnsupportedOperationException();
 }

 public boolean removeAll(Collection<?> coll) {
  throw new UnsupportedOperationException();
 }

 public boolean retainAll(Collection<?> coll) {
  throw new UnsupportedOperationException();
 }

 public boolean addAll(Collection<? extends E> coll) {
  throw new UnsupportedOperationException();
 }

 public boolean addAll(int index, Collection<? extends E> c) {
  throw new UnsupportedOperationException();
 }

 public ListIterator<E> listIterator(final int index) {
  return new ListIterator<E>() {
   private final ListIterator<? extends E> i = list.listIterator(index);

   public boolean hasNext() {
    return i.hasNext();
   }

   public E next() {
    return i.next();
   }

   public boolean hasPrevious() {
    return i.hasPrevious();
   }

   public E previous() {
    return i.previous();
   }

   public int nextIndex() {
    return i.nextIndex();
   }

   public int previousIndex() {
    return i.previousIndex();
   }

   public void remove() {
    throw new UnsupportedOperationException();
   }

   public void set(E e) {
    throw new UnsupportedOperationException();
   }

   public void add(E e) {
    throw new UnsupportedOperationException();
   }
  };
 }

 public List<E> subList(int fromIndex, int toIndex) {
  return Collections.unmodifiableList(list.subList(fromIndex, toIndex));
 }

 public Iterator<E> iterator() {
  return new Iterator<E>() {
   private final Iterator<? extends E> i = list.iterator();

   public boolean hasNext() {
    return i.hasNext();
   }

   public E next() {
    return i.next();
   }

   public void remove() {
    throw new UnsupportedOperationException();
   }
  };
 }
}