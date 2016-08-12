package source.evento;


public interface IColeccion extends Iterable<ISecuencia>{
   void add(ISecuencia secuencia);
   int size();
   boolean isEmpty();
   ISecuencia get(int index);
   void sort();
   IColeccion clone();
   void clear();
   void remove(int index);
}
