package source.modelo.arbol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import source.busqueda.AbstractMine;
import source.busqueda.IBusquedaArbol;
import source.excepciones.FactoryInstantiationException;
import source.modelo.AssociationFactory;
import source.modelo.IAsociacionTemporal;

/**
 * Clase de métodos útiles para gestionar los árboles que pueden compartirse entre las
 * clases hijas de SemillaConjuncion y las de Mine
 * @author vanesa.graino
 *
 */
public final class DictionaryUtils {
   private DictionaryUtils(){

   }

   public static Supernodo crearArbol(String treeClassName, List<Supernodo> nivelActual,
         AbstractMine mine, List<String> tipos) throws FactoryInstantiationException{
      // Crear el supernodo del árbol
      //Supernodo raizArbol = new Supernodo();
      Supernodo raizArbol = ArbolFactory.getInstance().getSupernodo(treeClassName);
      for(String tipo : tipos){
         IAsociacionTemporal modelo = AssociationFactory.getInstance().getAssociationInstance("ModeloEvento", tipo, mine.getNumHilos());
         //Nodo nodo = ((IBusquedaDiccionario)mine).creaNodoFachada(modelo,raizArbol);
         //raizArbol.addNodo(nodo, tipo);
         ((IBusquedaArbol)mine).creaNodoFachada(modelo,raizArbol, tipo);
      }
      nivelActual.add(raizArbol);

      return raizArbol;
   }

   public static Supernodo crearArbol(String treeClassName, List<Supernodo> nivelActual, List<String> tipos,
         AbstractMine mine, List<IAsociacionTemporal> modelosBase) throws FactoryInstantiationException{
      return crearArbol(treeClassName, nivelActual, tipos, mine.getWindowSize(), mine.isSavePatternInstances(), mine.getNumHilos(),
            (IBusquedaArbol)mine, modelosBase);
   }

   public static Supernodo crearArbol(String treeClassName, List<Supernodo> nivelActual, List<String> tipos,
         AbstractMine mine) throws FactoryInstantiationException{
      return crearArbol(treeClassName, nivelActual, tipos, mine.getNumHilos(), (IBusquedaArbol)mine);
   }

   /**
    * Crea un arbol con modelos de tipo ModeloEvento por cada tipo de evento de la lista tipos.
    * @param treeClassName
    * @param nivelActual
    * @param tipos
    * @param numHilos
    * @param mine
    * @return
    * @throws FactoryInstantiationException
    */
   public static Supernodo crearArbol(String treeClassName, List<Supernodo> nivelActual, List<String> tipos,
         int numHilos, IBusquedaArbol mine) throws FactoryInstantiationException{
      //Supernodo raizArbol = new Supernodo();
      Supernodo raizArbol = ArbolFactory.getInstance().getSupernodo(treeClassName);
      nivelActual.add(raizArbol);
      // Crear primer nivel del árbol
      for(String tipo : tipos){
         //Modelo modelo = new Modelo(aux,win, savePatternInstances, clustering);
         IAsociacionTemporal modelo = AssociationFactory.getInstance().getAssociationInstance("ModeloEvento", tipo, numHilos);
         //Nodo nodo = mine.creaNodoFachada(modelo,raizArbol);
         //raizArbol.addNodo(nodo, tipo);
         mine.creaNodoFachada(modelo, raizArbol, tipo);
      }
      return raizArbol;
   }

   /**
    * Crea la estructura del árbol con los elementos de modelosBase
    * @param raizArbol - un objeto supernodo
    * @param nivelActual - lista de supernodos actuales
    * @param tipos - tipos de la coleccion
    * @param win - ventana
    * @param savePatternInstances - parametro para la creación de modelos
    * @param clustering  - parametro para la creación de modelos
    * @param modelosBase  - parametro para la creación de modelos
    * @throws FactoryInstantiationException
    */
   public static Supernodo crearArbol(String treeClassName, List<Supernodo> nivelActual, List<String> tipos,
         int win, boolean savePatternInstances, int numHilos,
         IBusquedaArbol mine, List<IAsociacionTemporal> modelosBase) throws FactoryInstantiationException{
      //Crear nodos para modelos de tamaño 1
      Supernodo raizArbol = crearArbol(treeClassName, nivelActual, tipos, numHilos, mine);
      // Crear segundo nivel del árbol
      String[] aux;
      List<Supernodo> nuevoNivel = new ArrayList<Supernodo>();
      IAsociacionTemporal anteriorModelo = modelosBase.get(0);
      String[] tiposAnteriorModelo = anteriorModelo.getTipos();
      //Supernodo nuevo = new Supernodo();
      Supernodo nuevo = ArbolFactory.getInstance().getSupernodo(treeClassName);
      Nodo nodo = mine.creaNodoFachada(anteriorModelo);
      nuevo.addNodo(nodo, tiposAnteriorModelo[1]);
      for(IAsociacionTemporal modelo : modelosBase.subList(1,modelosBase.size())){
         aux = modelo.getTipos();
         // Comprobar si comparten todos sus tipos menos el último
         if(aux[0] == tiposAnteriorModelo[0]){
            // Mismo supernodo
            nodo = mine.creaNodoFachada(modelo);
            nuevo.addNodo(nodo, aux[1]);
         }else{
            // Nuevo supernodo
            // Cerrar anterior supernodo
            Nodo padre = raizArbol.getHijo(tiposAnteriorModelo[0]);
            nuevo.setPadre(padre);
            nuevoNivel.add(nuevo);
            padre.setHijos(nuevo);
            // Crear nuevo supernodo
            //nuevo = new Supernodo();
            nuevo = ArbolFactory.getInstance().getSupernodo(treeClassName);
            tiposAnteriorModelo = aux;
            nodo = mine.creaNodoFachada(modelo);
            nuevo.addNodo(nodo, aux[1]);
         }
      }
      // Cerrar el último supernodo
      Nodo padre = raizArbol.getHijo(tiposAnteriorModelo[0]);
      nuevo.setPadre(padre);
      nuevoNivel.add(nuevo);
      padre.setHijos(nuevo);

      //En lugar de hacer esto se limpia la lista nivelActual y se añaden los elementos de nuevoNivel
      //nivelActual = nuevoNivel;
      nivelActual.clear();
      nivelActual.addAll(nuevoNivel);

      //System.out.println("Arbol:\n" + raizArbol + "\n\nArbol completo: ");
      //DictionaryUtilsTest.printArbol(raizArbol);
      return raizArbol;
   }


   /**
    * Comprueba en el árbol de mine que existen todos los nodos que son subasociaciones de mod
    * y actualiza las estructuras patBase y patCount.
    * @param mine
    * @param tam
    * @param asocBase
    * @param patCount
    * @param mod
    * @return
    */
   /*public static boolean comprobarSubasociaciones(Supernodo raizArbol, int tam, IAsociacionTemporal[] asocBase,
         int[] patCount, String[] mod){
      String tipo;
      int index=2;
      List<String> modAux = new ArrayList<String>(Arrays.asList(mod));
      boolean valido=true;
      for(int k = tam-3; k>=0; k--){
         tipo = modAux.remove(k);
         //Nodo aux = mine.getRaizArbol().obtenerNodoEnArbol(modAux);
         Nodo aux = raizArbol.obtenerNodoEnArbol(modAux);
         if(aux==null){
            valido=false;
            break;
         }
         asocBase[index] = aux.getModelo();
         patCount[index] = asocBase[index].getPatrones().size();
         index++;
         modAux.add(k, tipo);
      }
      return valido;
   }*/

   public static boolean comprobarSubasociaciones(Supernodo raizArbol, int tam, IAsociacionTemporal[] asocBase,
         String[] mod){
      String tipo;
      int index=2;
      List<String> modAux = new ArrayList<String>(Arrays.asList(mod));
      boolean valido=true;
      for(int k = tam-3; k>=0; k--){
         tipo = modAux.remove(k);
         //Nodo aux = mine.getRaizArbol().obtenerNodoEnArbol(modAux);
         Nodo aux = raizArbol.obtenerNodoEnArbol(modAux);
         if(aux==null){
            valido=false;
            break;
         }
         asocBase[index] = aux.getModelo();
         index++;
         modAux.add(k, tipo);
      }
      return valido;
   }





}
