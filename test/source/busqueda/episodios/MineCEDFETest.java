package source.busqueda.episodios;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import static source.PrincipalTestGeneral.PASADO;
import source.PrincipalTestGeneral;
import source.configuracion.Algorithms;
import source.configuracion.Modes;
import source.evento.Episodio;
import source.excepciones.FactoryInstantiationException;
import source.modelo.IAsociacionTemporal;
import source.modelo.PatronPrueba;
import source.modelo.arbol.Supernodo;
import source.modelo.arbol.SupernodoTest;

public class MineCEDFETest extends MineCEDFE {

   public MineCEDFETest() {
      super("", false, false, false, null,false);
      patternClassName = "PatronPrueba";
   }

   public void setRaizArbol(Supernodo raizArbol){
      this.raizArbol = raizArbol;
   }

   public void setNivelActual(List<Supernodo> nivelActual){
      this.nivelActual = nivelActual;
   }

   @Test
   public void testGeneral(){
      //boolean PASADO = false;
      Collection<Object[]> data =Arrays.asList(new Object[][] {
         {Algorithms.ALG_HSTP, Modes.MODE_EPISODE, "apnea", 80, PASADO},
         {Algorithms.ALG_HSTP, Modes.MODE_EPISODE, "BD4", 20, PASADO},
         {Algorithms.ALG_HSTP, Modes.MODE_EPISODE, "BD5", 20, PASADO},
         {Algorithms.ALG_HSTP, Modes.MODE_EPISODE, "BD6", 20, PASADO},
         {Algorithms.ALG_HSTP, Modes.MODE_EPISODE, "BD7", 20, PASADO},
         {Algorithms.ALG_HSTP, Modes.MODE_EPISODE, "BDR56", 20, PASADO},
         {Algorithms.ALG_HSTP, Modes.MODE_EPISODE, "BDR57", 20, PASADO}
      });
      for(Object[] d : data){
         PrincipalTestGeneral test = new PrincipalTestGeneral((Algorithms)d[0], (Modes)d[1], (String)d[2], (Integer)d[3], (Boolean)d[4]);
         test.test();
      }

   }

   /**
    * Comprueba que el método generarCandidatas genera la cantidad esperada
    * de patrones descartados y de candidatos.
    * Para esto utiliza una clase de PatronPrueba que permite indicar qué patrones son
    * incompatibles.
    * @throws FactoryInstantiationException
    */
   @Test public void testGeneracionCandidatas() throws FactoryInstantiationException{
      //TODO manera de utilizar AssociationFactoryTest y PatternFactoryTest en lugar de los originales
      //associationFactory = AssociationFactoryTest.getInstance();
      //patternFactory = PatternFactoryTest.getInstance();


      PatronPrueba.incompatibles.add(Arrays.asList("P2", "P3"));

      List<String> tipos = new ArrayList<String>(Arrays.asList("A", "B", "C", "D"));
      int win = 80, tam = 3;
      List<Episodio> episodios = new ArrayList<Episodio>();


      List<IAsociacionTemporal> asociaciones = new ArrayList<IAsociacionTemporal>();
      //raiz
      Supernodo raiz = SupernodoTest.getArbolMemoria(asociaciones);

      //anteriores
      List<IAsociacionTemporal> anteriores = new ArrayList<IAsociacionTemporal>();
      anteriores.addAll(Arrays.asList(
            raiz.obtenerNodoEnArbol(Arrays.asList("A","B")).getModelo(), //modelo de AB
            raiz.obtenerNodoEnArbol(Arrays.asList("A","C")).getModelo(), //modelo de AC
            raiz.obtenerNodoEnArbol(Arrays.asList("A","D")).getModelo(), //etc
            raiz.obtenerNodoEnArbol(Arrays.asList("B","C")).getModelo(),
            raiz.obtenerNodoEnArbol(Arrays.asList("B","D")).getModelo(),
            raiz.obtenerNodoEnArbol(Arrays.asList("C","D")).getModelo()));
      //nivelActual
      nivelActual = new ArrayList<Supernodo>(Arrays.asList(
            raiz.getListaNodos().get(0).getHijos(), //AB, AC, AD (2)
            raiz.getListaNodos().get(1).getHijos(), //BC, BD (3)
            raiz.getListaNodos().get(2).getHijos())); // CD (4)

      setNivelActual(nivelActual);

      //raizArbol
      setRaizArbol(raiz);
      //variables de estadísticas
      iniciarContadores(3,0);
      windowSize = win;
      listaEpisodios = episodios;

      candidatasGeneradas = anteriores;
      mapa = new HashMap<String, List<IAsociacionTemporal>>();
      List<IAsociacionTemporal> candidatas = generarCandidatas(tam, candidatasGeneradas, tipos);
      System.out.println("Se han generado " + candidatas.size() + " candidatos");
      for(IAsociacionTemporal candidata: candidatas){
         System.out.println("Candidato: " + candidata);
      }
      Assert.assertEquals(1, candidatas.size());
      Assert.assertEquals(1, patronesGeneradosNivel[tam-1]);

      Assert.assertEquals(2, patronesPosiblesNivel[tam-1]);
   }
}
