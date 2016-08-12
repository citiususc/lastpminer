package source.busqueda.negacion;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.excepciones.FactoryInstantiationException;
import source.modelo.IAsociacionTemporal;
import source.modelo.arbol.Nodo;
import source.modelo.arbol.Supernodo;
import source.modelo.arbol.SupernodoNegacion;
import source.modelo.clustering.IClustering;
import source.modelo.negacion.HelperModeloNegacion;
import source.modelo.negacion.IAsociacionConNegacion;
import source.modelo.negacion.ModeloEventoPositivo;
import source.modelo.negacion.ModeloTontoNegadoCompleto;
import source.modelo.negacion.SuperModeloNegacion;
import source.patron.Ocurrencia;
import source.patron.Patron;

/**
 *
 * @author vanesa.graino
 *
 */
public class NegacionCompletaPruebaNivel3 extends NegacionMine {
   private static final Logger LOGGER = Logger.getLogger(NegacionCompletaPruebaNivel3.class.getName());

   public NegacionCompletaPruebaNivel3(String executionId,
         boolean savePatternInstances, boolean saveRemovedEvents,
         IClustering clustering, boolean removePatterns) {
      super(executionId, savePatternInstances, saveRemovedEvents, clustering,
            removePatterns);
   }


   /*@Override
   protected void generarCandidatasGeneral(int tam, List<IAsociacionTemporal> candidatas, List<String> tipos,
         List<Supernodo> nuevoNivel) throws FactoryInstantiationException{

      IAsociacionConNegacion modelo;

      GeneradorPatronesNegacion genp = new GeneradorPatronesNegacion(tam, this);
      // Inicializar mapa
      resetMapas(tipos);

      if(tam>3){
         return;
      }
      Nodo padre = ((SupernodoNegacion)raizArbol).obtenerNodoEnArbol(new String[]{"fA"}, new String[]{"fD"});
      genp.setPadre(padre.getModelo(), 0);
      Supernodo hijos = padre.getHijos();

      Nodo madre = ((SupernodoNegacion)raizArbol).obtenerNodoEnArbol(new String[]{"fA"}, new String[]{"fF"});
      genp.setPadre(madre.getModelo(), 1);


      List<String[]> comb = HelperModeloNegacion.combinarPrefijo((IAsociacionConNegacion)padre.getModelo(), (IAsociacionConNegacion)madre.getModelo());

      // Comprobar que las subasociaciones temporales son frecuentes
      //boolean valido= DictionaryUtils.comprobarSubasociaciones(raizArbol, tam, asocBase, patCount, modArray);
      //boolean valido = genp.comprobarSubasociaciones(raizArbol, modArray);
      boolean valido = genp.comprobarSubasociacionesPrefijo((SupernodoNegacion)raizArbol, comb.get(0), comb.get(1));

      registroT.tiempoAsociaciones(tam-1, false);

      if(!valido){
         LOGGER.severe("No es valido");
         return;
      }

      List<Patron> patrones = Collections.emptyList();
      if(comb.get(0).length>1){
         patrones = genp.generarPatrones(comb.get(0));
      }

      // Construir el modelo
      registroT.tiempoModelo(tam-1, true);
      if( comb.get(0).length<2 || !patrones.isEmpty()){
         // Hay: añadir punteros en la tabla hash y a candidatas
         modelo = crearModelo(comb.get(0), comb.get(1), patrones);

         //setModeloPatrones(patrones, modelo);

         notificarModeloGenerado(tam, patrones.size(), modelo, comb.get(0), comb.get(1), candidatas, nuevoMapa);
         // Añadir el Nodo al nuevo
         //Nodo hijo = creaNodoFachada(modelo,hijos);
         //hijos.addNodo(hijo, tipoNuevo);
         creaNodoFachada(modelo, hijos, genp.getTipoNuevo(), genp.tipoNuevoPositivo());

      }// else: No hay patrones candidatos: descartar modelo candidato actual
      registroT.tiempoModelo(tam-1, false);


      ModeloEventoPositivo modelo2 = new ModeloEventoPositivo("fD", 0);
      notificarModeloGenerado(tam, 0, modelo2, modelo2.getTipos(), modelo2.getTiposNegados(), candidatas, nuevoMapa);

      if(!hijos.getNodos().isEmpty()){
         nuevoNivel.add(hijos);
      }

   }*/


   @Override
   protected void calcularSoporte(List<IAsociacionTemporal> candidatas, IColeccion coleccion){
      int tam = candidatas.get(0).size();
      SuperModeloNegacion superModNeg = (SuperModeloNegacion)supermodelo;
      if(tam>3){
         return;
      }
      int sid = 0;
      //int totalVentanas = 0;
      for(ISecuencia secuencia : coleccion){
         superModNeg.setSecuencia(secuencia);

         while(superModNeg.nextWindow()){
            //totalVentanas++;
            for(Evento evento: superModNeg.getN()){
               List<IAsociacionTemporal> receptores = mapa.get(evento.getTipo());
               for(IAsociacionTemporal receptor : receptores){
                  receptor.recibeEvento(sid, evento, savePatternInstances);
               }
            }
            for(Evento evento: superModNeg.getD()){
               List<IAsociacionConNegacion> receptores = mapaNegados.get(evento.getTipo());
               for(IAsociacionConNegacion receptor : receptores){
                  //receptor.recibeEvento(sid, evento, savePatternInstances);
                  receptor.saleEventoNegado(sid, evento, savePatternInstances);
               }
            }
         }
         //LOGGER.info("Total ventanas ( al final de la secuencia #" + sid + "): " + totalVentanas);
         sid++;
      }

      if(tam==3){
         IAsociacionTemporal modelo = ((SupernodoNegacion)raizArbol).obtenerNodoEnArbol(new String[]{"fA"}, new String[]{"fD", "fF"}).getModelo();
         IAsociacionTemporal padre = ((SupernodoNegacion)raizArbol).obtenerNodoEnArbol(new String[]{"fA"}, new String[]{"fD"}).getModelo();

         System.out.println("Soporte de " + modelo.toStringSinPatrones() + ": " + modelo.getSoporte()
               + "(" + ((ModeloTontoNegadoCompleto)modelo).ocurrencias.size() + ")"
               + ", soporte de " + padre.toStringSinPatrones() + ": " + padre.getSoporte());

         // Pattern instances

         // TODO buscar repetidos en [fd,-fa]
         List<Ocurrencia> ocs = ((ModeloTontoNegadoCompleto)modelo).ocurrencias;
         for(int i=0;i<ocs.size()-1;i++){
            if(Arrays.equals(ocs.get(i).getEventTimes(), ocs.get(i+1).getEventTimes())){
               System.out.println("Ocurrencia repetida: " + ocs.get(i));
            }
         }

         // TODO comparar ocurrencias de

      }

   }
}
