package source;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Esta clase es una lista ordenada que no debe tener elementos repetidos
 * @author vanesa.graino
 * Similar a la solución de:
 * http://stackoverflow.com/questions/2661065/a-good-sorted-list-for-java
 */
public class SortedList<T extends Comparable<? super T>> extends ArrayList<T>{

   /**
    *
    */
   private static final long serialVersionUID = -8320702186223553501L;

   public SortedList() {
      super();
   }

   /**
    * Constructs a list containing the elements of the specified collection, in the order they are returned by the collection's iterator.
    * @param c the collection whose elements are to be placed into this list. It is supposed to be already sorted
    */
   public SortedList(Collection<? extends T> c) {
      super(c);
   }

   public SortedList(Collection<? extends T> c, boolean sort) {
      super(c);
      if(sort){
         Collections.sort(this);
      }
   }

   public SortedList(int initialCapacity) {
      super(initialCapacity);
   }

   public boolean contains(T o) {
      return Collections.binarySearch(this, o) > -1;
   }

   public int indexOf(T o) {
      return Collections.binarySearch(this, o);
   }

   @Override
   public boolean add(T e) {
       int insertionPoint = Collections.binarySearch(this, e);
       super.add(insertionPoint > -1 ? insertionPoint : (-insertionPoint) - 1, e);
       return true;
   }

   @Override
   public boolean addAll(Collection<? extends T> c) {
      boolean changed = super.addAll(c);
      Collections.sort(this);
      return changed;
   }


}
