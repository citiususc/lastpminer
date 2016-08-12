package source.modelo.arbol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.*;
import source.evento.Episodio;
import source.modelo.IAsociacionTemporal;
import source.modelo.Modelo;
import source.modelo.PatronPrueba;
import source.modelo.episodios.ModeloEpisodios;
import source.restriccion.RIntervalo;


/**
 * Clase con tests para
 *
 * La clase DictionaryUtilsTest tiene métodos (creación de árboles, eliminación de ramas, etc.)
 * que pueden ser útiles para probar esta clase.
 * @author vanesa.graino
 *
 */
public class SupernodoTest {

   //relies on Supernodo.getHijo and Nodo.getHijos
   @Test public void testEliminarNodoEnArbol(){
      List<IAsociacionTemporal> asociaciones = new ArrayList<IAsociacionTemporal>();
      Supernodo arbol = getArbolMemoria(asociaciones);
      String[] tipos = new String[]{"A"};
      arbol.eliminarNodoEnArbol(tipos);
      assertNull(arbol.getHijo("A"));

      asociaciones = new ArrayList<IAsociacionTemporal>();
      arbol = getArbolMemoria(asociaciones);
      tipos = new String[]{"A", "B"};
      arbol.eliminarNodoEnArbol(tipos);
      if(arbol.getHijo("A") == null){
         fail("Nodo nulo por el camino");
      }
      if(arbol.getHijo("A").getHijos() == null){
         fail("Un nodo sin supernodo!!");
      }
      assertNull(arbol.getHijo("A").getHijos().getHijo("B"));
   }

   /**
    * Devuelve un arbol de profundidad 3 con algunos patrones
    * @param asociaciones
    * @return
    */
   public static Supernodo getArbolMemoriaNivel3(List<IAsociacionTemporal> asociaciones){
      Supernodo raiz = getArbolMemoria(asociaciones);
      int win = 80;
      List<Episodio> episodios = new ArrayList<Episodio>();
      IAsociacionTemporal modeloNodoABC = new ModeloEpisodios(new String[]{"A", "B", "C"}, episodios, win, null);
      asociaciones.add(modeloNodoABC);

      Nodo nodoABC = new Nodo(modeloNodoABC);

      Nodo nodoAB = raiz.obtenerNodoEnArbol(Arrays.asList("A","B"));
      Supernodo sn5 = new Supernodo(nodoAB);
      sn5.addNodo(nodoABC, "C");
      nodoAB.setHijos(sn5);

      return raiz;
   }

   /**
    * Devuelve un arbol de profundidad 2 con algunos patrones
    * @param asociaciones - lista en la que se almacenan las <IAsociacionTemporal> del arbol.
    * Tiene que estar inicializada.
    * @return - el <Supernodo> raiz del árbol creado.
    */
   public static Supernodo getArbolMemoria(List<IAsociacionTemporal> asociaciones){
      List<String> tipos =Arrays.asList("A","B","C","D");
      Supernodo raiz = DictionaryUtilsTest.generarArbol(tipos, 2, new ArrayList<Supernodo>());
      for(int i=0;i<tipos.size();i++){
         Nodo padre = raiz.obtenerNodoEnArbol(Arrays.asList(tipos.get(i)));
         asociaciones.add(padre.getModelo());
         for(int j=i+1;j<tipos.size();j++){
            asociaciones.add(padre.getHijos().getHijo(tipos.get(j)).getModelo());
         }
      }

      List<RIntervalo> consts = new ArrayList<RIntervalo>();

      IAsociacionTemporal asocNodoAB = raiz.obtenerNodoEnArbol(Arrays.asList("A","B")).getModelo();
      PatronPrueba p1 = new PatronPrueba(new String[]{"A", "B"}, consts, false);
      p1.setName("P1");
      asocNodoAB.addPatron(p1);
      PatronPrueba p2 = new PatronPrueba(new String[]{"A", "B"}, Arrays.asList(new RIntervalo("A", "B", -10, 20)), false);
      p2.setName("P2");
      asocNodoAB.addPatron(p2);

      IAsociacionTemporal asocNodoAC = raiz.obtenerNodoEnArbol(Arrays.asList("A","C")).getModelo();
      PatronPrueba p3 = new PatronPrueba(new String[]{"A", "C"}, consts, false);
      p3.setName("P3");
      asocNodoAC.addPatron(p3);

      IAsociacionTemporal asocNodoBC = raiz.obtenerNodoEnArbol(Arrays.asList("B","C")).getModelo();
      PatronPrueba p6 = new PatronPrueba(new String[]{"B", "C"}, consts, false);
      p6.setName("P6");
      asocNodoBC.addPatron(p6);

      return raiz;
   }

   /*public static Supernodo getArbolMemoria(List<IAsociacionTemporal> asociaciones){
      int win = 80;
      List<Episodio> episodios = new ArrayList<Episodio>();

      List<RIntervalo> consts = new ArrayList<RIntervalo>();
      Supernodo raiz = new Supernodo();
      PatronPrueba p1 = new PatronPrueba(Arrays.asList("A", "B"), consts, false);
      p1.setName("P1");
      PatronPrueba p2 = new PatronPrueba(Arrays.asList("A", "B"), Arrays.asList(new RIntervalo("A", "B", -10, 20)), false);
      p2.setName("P2");
      PatronPrueba p3 = new PatronPrueba(Arrays.asList("A", "C"), consts, false);
      p3.setName("P3");
      PatronPrueba p6 = new PatronPrueba(Arrays.asList("B", "C"), consts, false);
      p6.setName("P6");

      IAsociacionTemporal modeloNodoA = new ModeloEpisodios(Arrays.asList("A"), episodios, win, false, null),
            modeloNodoB = new ModeloEpisodios(Arrays.asList("B"), episodios, win, false, null),
            modeloNodoC = new ModeloEpisodios(Arrays.asList("C"), episodios, win, false, null),
            modeloNodoD = new ModeloEpisodios(Arrays.asList("D"), episodios, win, false, null),
            modeloNodoAB = new ModeloEpisodios(Arrays.asList("A", "B"), episodios, win, Arrays.asList((Patron) p1, (Patron) p2), false, null),
            modeloNodoAC = new ModeloEpisodios(Arrays.asList("A", "C"), episodios, win, Arrays.asList((Patron) p3), false, null),
            modeloNodoAD = new ModeloEpisodios(Arrays.asList("A", "D"), episodios, win, false, null),
            modeloNodoBC = new ModeloEpisodios(Arrays.asList("B", "C"), episodios, win, Arrays.asList((Patron) p6), false, null),
            modeloNodoBD = new ModeloEpisodios(Arrays.asList("B", "D"), episodios, win, false, null),
            modeloNodoCD = new ModeloEpisodios(Arrays.asList("C", "D"), episodios, win, false, null);

      asociaciones.addAll(Arrays.asList(modeloNodoA, modeloNodoB, modeloNodoC,  modeloNodoD, modeloNodoAB, modeloNodoAC, modeloNodoAD,
            modeloNodoBC, modeloNodoBD, modeloNodoCD));

      Nodo nodoA = new Nodo(modeloNodoA),
            nodoB = new Nodo(modeloNodoB),
            nodoC = new Nodo(modeloNodoC),
            nodoD = new Nodo(modeloNodoD),
            nodoAB = new Nodo(modeloNodoAB),
            nodoAC = new Nodo(modeloNodoAC),
            nodoAD = new Nodo(modeloNodoAD),
            nodoBC = new Nodo(modeloNodoBC),
            nodoBD = new Nodo(modeloNodoBD),
            nodoCD = new Nodo(modeloNodoCD);


      Supernodo sn2 = new Supernodo(nodoA);
      sn2.addNodo(nodoAB, "B");
      sn2.addNodo(nodoAC, "C");
      sn2.addNodo(nodoAD, "D");
      nodoA.setHijos(sn2);

      Supernodo sn3 = new Supernodo(nodoB);
      sn3.addNodo(nodoBC, "C");
      sn3.addNodo(nodoBD, "D");
      nodoB.setHijos(sn3);

      Supernodo sn4 = new Supernodo(nodoC);
      sn4.addNodo(nodoCD, "D");
      nodoC.setHijos(sn4);

      raiz.addNodo(nodoA, "A");
      raiz.addNodo(nodoB, "B");
      raiz.addNodo(nodoC, "C");
      raiz.addNodo(nodoD, "D");

      return raiz;
   }*/

   /**
    * Test de la función de clonado de Supernodo
    */
   @Test public void testClone(){
      List<IAsociacionTemporal> asociaciones = new ArrayList<IAsociacionTemporal>();
      Supernodo raiz = getArbolMemoria(asociaciones);
      Map<String[],IAsociacionTemporal> mapaClones = new HashMap<String[], IAsociacionTemporal>();
      //List<IAsociacionTemporal> clonadas = new ArrayList<IAsociacionTemporal>();
      for(IAsociacionTemporal asoc: asociaciones){
         IAsociacionTemporal clonAsoc = ((Modelo)asoc).clonar();
         mapaClones.put(clonAsoc.getTipos(), clonAsoc);
      }
      Supernodo clon = raiz.clonar(mapaClones, null);
      System.out.println("Raiz: " + raiz);
      DictionaryUtilsTest.printArbol(raiz);
      System.out.println("Clon: " + clon);
      DictionaryUtilsTest.printArbol(clon);
   }


   @Test public void testIterator(){
       List<IAsociacionTemporal> asociaciones = new ArrayList<IAsociacionTemporal>();
       Supernodo raiz = getArbolMemoria(asociaciones);

       int i = 0;
       Iterator<Supernodo> it = raiz.iterator();
       while(it.hasNext()){
           System.out.println("Elemento i=" + (i++) + ": " + it.next().toString());
       }
   }
}


