package net.minecraftforge.oredict;

import org.cliffc.high_scale_lib.NonBlockingHashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class SpecialArrayList<T> extends ArrayList<T> {
 private final NonBlockingHashSet<T> internalSet;
 private final Collection
  <T> target;

 public SpecialArrayList(final Collection<T> target) {
  super(target);
  this.internalSet = new NonBlockingHashSet<>();
  this.target = target;
 }

 @Override
 public boolean remove(final Object stack) {
  return internalSet.remove(stack) && target.remove(stack);
 }

 @Override
 public T remove(final int i) {
  T is = null;
  if (i < size()) {
   is = get(i);
   if (remove(is) && target.remove(is)) {
    return is;
   }
  }
  return is;
 }

 @Override
 public boolean removeAll(final Collection<?> c) {
  return internalSet.removeAll(c) && target.removeAll(c);
 }

 @Override
 public void clear() {
  target.removeAll(this);
 }

 @Override
 public boolean retainAll(final Collection<?> c) {
  return internalSet.retainAll(c) && target.retainAll(c);
 }

 @Override
 public boolean add(final T stack) {
  return internalSet.add(stack) && target.add(stack);
 }

 @Override
 public void add(final int i, final T stack) {
  //TODO Could cause problems
  throw new UnsupportedOperationException();
//  internalSet.add(i, stack);
//  target.add(stack);
 }

 @Override
 public boolean addAll(final Collection<? extends T> c) {
  return internalSet.addAll(c) && target.addAll(c);
 }

 @Override
 public boolean addAll(final int i, final Collection<? extends T> c) {
  //TODO Could cause problems
  throw new UnsupportedOperationException();
//  return internalSet.addAll(i, c) && target.addAll(c);
 }

 @Override
 public boolean contains(final Object o) {
  //If this list does not 'o' then query it from 'target' just in case
  if (target.contains(o) && !internalSet.contains(o)) {
   internalSet.add((T)o);
  }
  return internalSet.contains(o);
 }

 @Override
 public Object clone() {
  return new ArrayList<>(this);
 }

 @Override
 public List<T> subList(final int i1, final int i2) {
  //TODO Could cause problems
  throw new UnsupportedOperationException();
//  return new SpecialArrayList(internalSet.subList(i1, i2));
 }

 @Override
 public boolean removeIf(final Predicate<? super T> p) {
  return internalSet.removeIf(p) && target.removeIf(p);
 }

 @Override
 public void replaceAll(final UnaryOperator<T> u) {
//    return internalSet.replaceAll(u) && target.replaceAll();
  //TODO may cause problems
  throw new UnsupportedOperationException();
 }
}
