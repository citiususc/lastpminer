package source.modelo.arbol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import source.modelo.Modelo;
import source.modelo.arbol.Nodo;
import source.modelo.arbol.Supernodo;


/**
 * Métodos auxiliares para testear las funciones relativas al árbol (enumaration tree)
 * de los algoritmos basados en HSTPminer.
 * @author vanesa.graino
 *
 */
public class DictionaryUtilsTest {

   /**
    * Genera un árbol con todos los posibles nodos que son combinación de <tipos> y que tienen
    * como máximo tamaño <maxTam>. En <nivelActual> se almacenan los supernodos del último nivel generado.
    * @param tipos - lista de tipos ordenada
    * @param maxTam - máximo tamaño de las asociaciones temporales de los nodos. No debe ser mayor que tipos.size().
    * @param nivelActual - una lista inicializada y vacía.
    * @return - la raiz del árbol generado.
    */
   public static Supernodo generarArbol(List<String> tipos, int maxTam, List<Supernodo> nivelActual){
      Supernodo raizArbol = new Supernodo();
      List<Supernodo> /*nivelActual = new ArrayList<Supernodo>(),*/ nuevoNivel = new ArrayList<Supernodo>();
      for(String tipo: tipos){
         raizArbol.addNodo(nodoDummy(new String[]{tipo},0), tipo);
      }
      if(maxTam<2) return raizArbol;
      //System.out.println("Nivel: 2 de " + maxTam);
      for(int i=0;i<tipos.size();i++){
         Nodo padre = raizArbol.obtenerNodoEnArbol(Arrays.asList(tipos.get(i)));
         for(int j=i+1;j<tipos.size();j++){
            Nodo n = nodoDummy(new String[]{tipos.get(i), tipos.get(j)},0);
            //System.out.println("Nodo: " + n.getModelo().getTipos());
            padre.addHijo(n, tipos.get(j));
         }
         nivelActual.add(padre.getHijos());
      }
      List<String> mod;
      for(int nivel=3; nivel<=maxTam; nivel++){
         //System.out.println("\nNivel: " +  nivel + " de " + maxTam);
         for(Supernodo supernodo : nivelActual){
            List<Nodo> nodos = supernodo.getListaNodos();
            int nSize = nodos.size();
            for(int iPadre=0;iPadre<(nSize-1);iPadre++){
               Nodo padre = nodos.get(iPadre);
               Supernodo hijos = padre.getHijos();
               //System.out.println("Padre: " + padre.getModelo().getTipos());
               for(int iMadre=iPadre+1;iMadre<nSize;iMadre++){
                  // Construir la asociación temporal
                  Nodo madre = nodos.get(iMadre);
                  //System.out.println("\tMadre: " + madre.getModelo().getTipos());
                  // Todos los nodos del supernodo comparten todos los tipos
                  // de evento salvo el último.
                  String tipoNuevo = madre.getModelo().getTipos()[nivel-2];
                  mod = new ArrayList<String>(Arrays.asList(padre.getModelo().getTipos()));
                  mod.add(tipoNuevo);

                  Nodo n = nodoDummy((String[])mod.toArray(),0);

                  // add to arbol
                  padre.addHijo(n, tipoNuevo);
               }
               if(!hijos.getNodos().isEmpty()){
                  nuevoNivel.add(hijos);
               }
            }
         }
         //nivelActual = nuevoNivel;
         nivelActual.clear();
         nivelActual.addAll(nuevoNivel);
         nuevoNivel.clear();
      }
      return raizArbol;
   }

   /**
    * Crea un nodo con su correspondiente modelo con los tipos <tipos> y el tamaño de ventana <window>.
    * @param tipos - tipos de la asociación temporal del modelo.
    * @param window - ancho de la ventana temporal para la asociación temporal.
    * @return - el nuevo nodo creado.
    */
   public static Nodo nodoDummy(String[] tipos, int window){
      return new Nodo(new Modelo(tipos,window,null));
   }

   /**
    * Borra todos los nodos del árbol <raizArbol> que no son posibles cuando la
    * asociación temporal <asoc> no es frecuente.
    * Si tenemos por ejemplo la asociación [A,F] no solo borrará el subárbol que tiene por
    * raiz al nodo de [A,F] también los nodos que contengan simultáneamente los dos tipos.
    * @param raizArbol - raiz del árbol o del subárbol en la que quieren borrarse los nodos.
    * @param asoc - asociación temporal que quiere borrarse.
    */
   public static void borrarPorAsociacion(Supernodo raizArbol, String[] asoc){
      raizArbol.eliminarNodoEnArbol(asoc);
      //Recorrer arbol borrar nodos que contienen asoc
      borrarRecursivo(raizArbol, asoc);

   }

   /**
    * Función recursiva que busca en el árbol <nd> los nodos que contienen todos los tipos de
    * la asociación temporal <asoc> y borrar los subárboles correspondientes.
    * @param nd - árbol
    * @param asoc - lista de tipos
    */
   private static void borrarRecursivo(Supernodo nd, String[] asoc){
      if(nd == null || nd.getListaNodos().isEmpty()) return;
      List<Nodo> hijos = nd.getListaNodos();
      int numHijos = hijos.size();
      for(int child=0; child<numHijos; child++){
         Nodo n = nd.getListaNodos().get(child);
         List<String> tiposN = Arrays.asList(n.getModelo().getTipos());
         //System.out.println("Explorando nodo: " + tiposN);
         if(tiposN.containsAll(Arrays.asList(asoc))){
            //System.out.println("Contiene " + asoc);
            //raiz.eliminarNodoEnArbol(tiposN);
            n.getSupernodo().eliminarNodoEnArbol((String[])Arrays.asList(tiposN.get(tiposN.size()-1)).toArray());
            hijos.remove(child);
            child--;
            numHijos = hijos.size();
         }else{
            //System.out.println("No contiene " + asoc);
            borrarRecursivo(n.getHijos(),asoc);
         }
      }
   }

   private static final int yOffset = 3, xOffset = 2;

   /**
    * Imprime por pantalla el árbol <raiz>. Para ello recorre el árbol en profundidad.
    * @param raiz - árbol que queremos pintar.
    * @param horizontal - modo horizontal o vertical
    */
   public static void printArbol(Supernodo raiz){
      printArbol(raiz, true, "Root");
   }
   public static void printArbol(Supernodo raiz, boolean horizontal, String rootName){
      List<StringBuffer> niveles = new ArrayList<StringBuffer>();
      if(horizontal){
         niveles.add(new StringBuffer(rootName));
         //Recorrer el arbol en profundidad
         printArbol(raiz, niveles, 1, 0, 0);
         //Imprimir por pantalla
         for(StringBuffer n: niveles){
            System.out.println(n);
         }
      }else{
         System.out.println(rootName);
         for(Nodo n : raiz.getListaNodos()){
            printArbol(n.getHijos(), true, "\n+ Rama: " + Arrays.toString(n.getModelo().getTipos()));
         }
      }
   }

   /**
    * Función recursiva que
    * @param raiz
    * @param niveles
    * @param nivel
    * @param min
    * @param max
    * @return
    */
   private static int printArbol(Supernodo raiz, List<StringBuffer> niveles, int nivel, int min, int max){
      if(raiz == null || raiz.getListaNodos().isEmpty()) return max;
      int numHijos = raiz.getListaNodos().size();
      //Recorrer el arbol en profundidad
      int indice = yOffset*nivel+nivel;

      //es el primer hijo de un padre y no se había alcanzado la profundidad actual
      while(niveles.size()<=indice){
         niveles.add(new StringBuffer());
      }
      // Añadimos la parte que baja de la unión de un nodo con su padre
      fillOrAdd(niveles.get(indice-3), min,' ',false).append("|");
      fillOrAdd(niveles.get(indice-2), min,' ',false).append(numHijos>1? "+" : "|");
      fillOrAdd(niveles.get(indice-1), min,' ',false).append("|");
      fillOrAdd(niveles.get(indice),   min,' ',false);

      StringBuffer sb = niveles.get(indice);
      for(int child=0; child<numHijos; child++){
         //El primer hijo
         if(child>0){
            // unión y espacios en niveles inferiores
            StringBuffer sb2 = niveles.get(indice-yOffset+1);
            fillOrAdd(sb2,max+xOffset, '-',false).append("+");
            fillOrAdd(niveles.get(indice-yOffset+2), max+xOffset,' ', false).append("|");
            fillOrAdd(sb,max+xOffset, ' ',false);
            max = sb2.length();//Math.max(sb2.length(), max);
            min = sb2.length()-1;
         }

         Nodo n = raiz.getListaNodos().get(child);
         String nombre = Arrays.toString(n.getModelo().getTipos()).replace(" ", "");
         max = Math.max(sb.length()+nombre.length(), max);
         fillOrAdd(sb.append(nombre),xOffset, ' ', true);
         max = Math.max(max, printArbol(n.getHijos(), niveles, nivel+1, min, max));
      }
      return max;
   }

   /**
    * Añade tantos caracteres <caracter> a <cadena> como sea necesario.
    * @param cadena - stringbuffer al que se añadirán los caracteres de ser el caso
    * @param tam - si add es true, es el número de caracteres a añadir. En otro caso, se rellena hasta <tam>.
    * @param caracter - el caracter con el que se va a rellenar.
    * @param add - si es <true> se añaden, sino se rellena.
    * @return
    */
   private static StringBuffer fillOrAdd(StringBuffer cadena, int tam, char caracter, boolean add){
      int count = 0;
      while((!add && cadena.length()<tam) || (add && count<tam)){
         cadena.append(caracter);
         count++;
      }
      return cadena;
   }

   /**
    * Test que comprueba que no hay excepciones cuando se crean e imprimen
    * árboles por pantalla.
    * No está validando que los resultados sean los esperados.
    */
   @Test public void testImprimirArbol(){
      List<Supernodo> nivelActual = new ArrayList<Supernodo>();
      Supernodo raiz = generarArbol(Arrays.asList("A","B","C","F"),4,nivelActual);
      printArbol(raiz);

      //nivelActual = new ArrayList<Supernodo>();
      //raiz = generarArbol(Arrays.asList("A","B","F"),5,nivelActual);
      //printArbol(raiz);

      nivelActual = new ArrayList<Supernodo>();
      raiz = generarArbol(Arrays.asList("A","B","E", "F","G"),5,nivelActual);
      printArbol(raiz);


      borrarPorAsociacion(raiz, new String[]{"A","F"});
      printArbol(raiz);
   }
}
