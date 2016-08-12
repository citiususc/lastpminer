package source.evento;

import java.util.List;
import java.util.ListIterator;


public interface ISecuencia extends Iterable<Evento>{

   void add(Evento ev);
   void sort();
   int size();
   Evento get(int index);
   Evento remove(int index);
   boolean isEmpty();
   /**
    * @return Una copia de la secuencia
    */
   ISecuencia clone();
   ListIterator<Evento> listIterator();
   ListIterator<Evento> listIterator(int index);
   List<Evento> subList(int b, int e);
   boolean contains(Evento ev);
   int indexOf(Evento ev);
   int lastIndexOf(Evento ev);
   Evento set(int index, Evento ev);
   /**
    * Se resta el instante del primer evento de la secuencia en orden cronológico
    * Precondición: la secuencia está ordenada
    */
   void subtractOffsetTime();
   /**
    * @return Instante temporal del último evento de la secuencia o -1 si la secuencia está vacía
    */
   int lastInstant();
}
