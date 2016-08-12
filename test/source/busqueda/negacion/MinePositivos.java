package source.busqueda.negacion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import source.excepciones.FactoryInstantiationException;
import source.modelo.AssociationFactory;
import source.modelo.IAsociacionTemporal;
import source.modelo.arbol.ArbolFactory;
import source.modelo.arbol.Supernodo;
import source.modelo.clustering.IClustering;
import source.modelo.negacion.IAsociacionConNegacion;
import source.modelo.negacion.SuperModeloNegacion;

/**
 * Sólo mina los positivos pero recorrienda la colección como lo hace
 * negación
 * @author vanesa.graino
 *
 */
public class MinePositivos extends NegacionMine {

   public MinePositivos(String executionId, boolean savePatternInstances,
         boolean saveRemovedEvents, IClustering clustering,
         boolean removePatterns) {
      super(executionId, savePatternInstances, saveRemovedEvents, clustering,
            removePatterns);
   }

   @Override
   protected void inicializaEstructuras(List<String> tipos,
         List<IAsociacionTemporal> actual, int win, int cSize) throws FactoryInstantiationException {
      int tSize = tipos.size();
      iniciarContadores(tSize, cSize);
      windowSize = win;

      Map<String, List<IAsociacionTemporal>> mapa = new HashMap<String,List<IAsociacionTemporal>>(tSize);
      //mapaGeneradas = new HashMap<String,List<IAsociacionTemporal>>(tSize);
      //candidatasGeneradas = new ArrayList<IAsociacionTemporal>(tSize);

      // Crear el supernodo del árbol
      raizArbol = ArbolFactory.getInstance().getSupernodo(treeClassName);//new Supernodo();
      //raizArbol = new SupernodoNegacion();
      nivelActual = new ArrayList<Supernodo>();
      nivelActual.add(raizArbol);

      //List<IAsociacionTemporal> listaNegados = new ArrayList<IAsociacionTemporal>(tSize);
      //final int tam = 1;
      // Modelos evento positivos
      for(String tipo : tipos){
         IAsociacionConNegacion modeloPos = AssociationFactory.getInstance().getAssociationInstance("ModeloEvento", tipo, win, false, numHilos);
         notificarModeloGenerado(modeloPos, tipo, actual, mapa);
         creaNodoFachada(modeloPos, raizArbol, tipo, true);
      }
      //Negados (tienen que ir al final para que se respete el orden)
      /*for(String tipo : tipos){
         //Modelo evento negado
         IAsociacionConNegacion modeloNeg = AssociationFactory.getInstance().getAssociationInstance(associationClassName, tipo, win, true, numHilos);
         //notificarModeloGenerado(modeloNeg, tipo, listaNegados, mapa);
         listaNegados.add(modeloNeg);
         creaNodoFachada(modeloNeg, raizArbol, tipo, false);
      }
      actual.addAll(listaNegados);*/

      setMapa(mapa);
      supermodelo = new SuperModeloNegacion(tipos.toArray(new String[tipos.size()]), win);
      mapaNegados = construyeMapa(tipos.size(), tipos);
   }

}
