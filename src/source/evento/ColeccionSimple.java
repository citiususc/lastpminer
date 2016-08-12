package source.evento;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class ColeccionSimple implements IColeccion {

   /*
    * Atributos
    */

   List<ISecuencia> secuencias;

   /*
    * Constructores
    */

   public ColeccionSimple() {
      this.secuencias = new ArrayList<ISecuencia>();
   }

   public ColeccionSimple(List<ISecuencia> secuencias){
      this.secuencias = secuencias;
   }

   /*
    * Métodos
    */

   @Override
   public void add(ISecuencia secuencia){
      secuencias.add((SecuenciaSimple)secuencia);
   }

   @Override
   public Iterator<ISecuencia> iterator(){
      return secuencias.iterator();
   }

   /**
    * Ordena la colección de forma que las secuencias de mayor tamaño están
    * al principio
    */
   public void sort(){
      Collections.sort(secuencias, new Comparator<ISecuencia>() {
         @Override
         public int compare(ISecuencia o1, ISecuencia o2) {
            return Integer.compare(o2.size(),o1.size());
         }
      });
   }

   @Override
   public int size() {
      return secuencias.size();
   }

   @Override
   public ISecuencia get(int index) {
      return secuencias.get(index);
   }

   @Override
   public boolean isEmpty() {
      return secuencias.isEmpty();
   }


   public IColeccion clone(){
      IColeccion copia = new ColeccionSimple();
      for(ISecuencia secuencia : this){
         copia.add(secuencia.clone());
      }
      return copia;
   }

   @Override
   public void clear() {
      secuencias.clear();
   }

   @Override
   public void remove(int index) {
      secuencias.remove(index);
   }

   @Override
   public String toString(){
      return secuencias.toString();
   }
}
