package source.modelo.arbol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import source.modelo.IAsociacionTemporal;

/**
 *
 * @author vanesa.graino
 *
 */
public class Supernodo implements Iterable<Supernodo>{
   private static final Logger LOGGER = Logger.getLogger(Supernodo.class.getName());

   // Atributos
   protected Map<String,Nodo> nodos;
   //private final Map<String,Nodo> adoptivos; //usado por MineAhorro
   protected Nodo padre;
   protected final List<Nodo> lista;

   // Constructores
   protected Supernodo(){
      this.nodos = new HashMap<String,Nodo>();
      //this.adoptivos = new HashMap<String,Nodo>();
      this.padre = null;
      this.lista = new ArrayList<Nodo>();
   }

   protected Supernodo(Nodo padre){
      this.nodos = new HashMap<String,Nodo>();
      //this.adoptivos = new HashMap<String,Nodo>();
      this.padre = padre;
      this.lista = new ArrayList<Nodo>();
   }

   /**
    * Recorre el árbol tomando <this> como raiz y busca el nodo que
    * tiene todos los tipos.
    * @param tipos - lista de tipos
    * @return - el nodo con el modelo que tienen tipos o null si no lo hay.
    */
   public Nodo obtenerNodoEnArbol(String[] tipos){
      Nodo resultado = null;
      Supernodo actual = this;
      for(String tipo : tipos){
         if(actual==null){ return null; }
         resultado = actual.getHijo(tipo);
         if(resultado==null){ return null; }
         actual = resultado.getHijos();
      }
      return resultado;
   }
   public Nodo obtenerNodoEnArbol(List<String> tipos){
      Nodo resultado = null;
      Supernodo actual = this;
      for(String tipo : tipos){
         if(actual==null){ return null; }
         resultado = actual.getHijo(tipo);
         if(resultado==null){ return null; }
         actual = resultado.getHijos();
      }
      return resultado;
   }

   /**
    * Cual es el objetivo de borrar esto?
    * @param tipos - tiene que estar ordenado alfabeticamente
    */
   public Nodo eliminarNodoEnArbol(String[] tipos){
      String[] tiposAux = Arrays.copyOf(tipos, tipos.length-1);

      // Eliminar el último tipo, para recuperar el supernodo
      String ultimoTipo = tipos[tipos.length -1];//tiposAux.remove(tiposAux.size()-1);

      Nodo actual = obtenerNodoEnArbol(tiposAux);

      if(actual!=null && actual.getHijos()!=null){
         return actual.getHijos().removeNodo(ultimoTipo);
      }

      return null;
   }

   // Getters y Setters

   /**
    * Devuelve el mapa en el que las claves son los tipos de eventos  con la los nodos de este supernodo por cada
    * tipo de evento que
    * @return
    */
   public Map<String,Nodo> getNodos() {
      return nodos;
   }

   /**
    * Obtiene el nodo de este supernodo que tiene el tipo que se pasa como parámetro
    * @param tipo
    * @return el nodo o null si no hay tal nodo
    */
   public Nodo getHijo(String tipo){
      return nodos.get(tipo);
   }

   /**
    *
    * @param nodos
    */
   public void setNodos(Map<String,Nodo> nodos) {
      this.nodos = nodos;
   }

   /**
    *
    * @param tipo
    */
   protected Nodo removeNodo(String tipo){
      Nodo nodo = nodos.remove(tipo);
      lista.remove(nodo);
      return nodo;
   }

   /**
    *
    * @param nodo
    * @param tipo
    */
   public void addNodo(Nodo nodo, String tipo){
      nodos.put(tipo,nodo);
      lista.add(nodo);
   }

   /**
    * Añade el hijo a las estructuras internas del Supernodo de forma ordenada
    * cuando la estructura lo permite.
    * Sólo está utilizando este método la clase {@link MineDictionaryLazy2}.
    * @param nodo
    * @param tipo
    */
   public void addNodoSorted(Nodo nodo, String tipo){
      nodos.put(tipo,nodo);
      //lista.add(nodo);
      int i=0;
      while(i<lista.size()){
         String ultimo = lista.get(i).getUltimoTipo();
         if(ultimo.compareTo(tipo) > 0){
            lista.add(i,nodo);
         }
         i++;
      }
      lista.add(nodo);
   }

//   private class ComparatorNodo implements Comparator<Nodo> {
//      @Override
//      public int compare(Nodo o1, Nodo o2) {
//         return o1.getUltimoTipo().compareTo(o2.getUltimoTipo());
//      }
//   }

   /**
    *
    * @return
    */
   public Nodo getPadre() {
      return padre;
   }

   /**
    *
    * @param padre
    */
   public void setPadre(Nodo padre) {
      this.padre = padre;
   }

   /**
    *
    * @return
    */
   public List<Nodo> getListaNodos(){
      return lista;
   }

   @Override
   public String toString(){
      StringBuilder aux = new StringBuilder("Supernodo: [ ");
      if(!lista.isEmpty()){
         for(Nodo nodo : lista){
            aux.append(Arrays.toString(nodo.getModelo().getTipos())).append(", ");
         }
         aux.deleteCharAt(aux.length()-1).deleteCharAt(aux.length()-1);
      }
      aux.append("]\n");
      return aux.toString();
   }

   /**
    *
    * @param asociacionesClonadas
    * @param padre
    * @return
    */
   public Supernodo clonar(Map<String[],IAsociacionTemporal> asociacionesClonadas, Nodo padre){
      Supernodo clon;
      if(padre == null){
         clon = new Supernodo();
      }else{
         clon = new Supernodo(padre);
      }
      for(Nodo nodo : lista){
         Nodo nodoClon = nodo.clonar(clon, asociacionesClonadas);
         if(nodoClon == null){
            LOGGER.severe("Algo es nulo!!");
         }
         if(nodoClon.getModelo() == null){
            LOGGER.severe("Algo es nulo2!!");
         }
         String[] tipos = nodoClon.getModelo().getTipos().clone();
         clon.addNodo(nodoClon, tipos[tipos.length-1]);
      }
      return clon;
   }

   private Supernodo nextSupernodeChild(int beginIndex){
       for(int i = beginIndex+1; i<lista.size(); i++){
           if(!lista.get(i).hijos.lista.isEmpty()){
               return lista.get(i).hijos;
           }
       }
       return null;
   }

   private Supernodo nextSupernodeGreatChild(){
       for(int i = 0; i< lista.size(); i++){
           Supernodo sn = lista.get(i).hijos.nextSupernodeChild(0);
           if(sn != null){
               return sn;
           }
       }
       return null;
   }



    @Override
    public Iterator<Supernodo> iterator() {
        Iterator<Supernodo> it = new Iterator<Supernodo>() {


            private Supernodo actual = Supernodo.this;
            private int indiceActual = -1;
            private Supernodo cachedNext = Supernodo.this;

            @Override
            public boolean hasNext() {
                // Primero miramos en
                if(indiceActual != -1){
                    //No es raiz
                    cachedNext = actual.padre.supernodo.nextSupernodeChild(indiceActual);
                    if(cachedNext != null){
                        indiceActual = actual.padre.hijos.lista.indexOf(cachedNext.padre);
                        return true;
                    }
                }else if(cachedNext != null){
                    return true;
                }else{
                    //Miramos entre los hijos del primer nodo de su nivel
                    cachedNext = actual.padre.supernodo.nextSupernodeGreatChild();
                    if(cachedNext != null){
                        indiceActual = 0;
                        return true;
                    }
                }
                return false;
            }

            @Override
            public Supernodo next() {
                actual = cachedNext;
                cachedNext = null;
                return actual;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
        return it;

    }
}
