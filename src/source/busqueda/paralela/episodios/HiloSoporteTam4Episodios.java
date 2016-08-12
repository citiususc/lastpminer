package source.busqueda.paralela.episodios;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import source.busqueda.paralela.IBusquedaParalelaSecuenciaAnotaciones;
import source.busqueda.paralela.jerarquia.HiloSoporteTam4;
import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.modelo.IAsociacionConEpisodios;
import source.modelo.IAsociacionDiccionario;
import source.modelo.IAsociacionTemporal;
import source.modelo.arbol.NodoHilos;
import source.modelo.episodios.ModeloEpisodiosDFE;
import source.patron.Patron;
import source.patron.PatronDictionaryFinalEvent;

public class HiloSoporteTam4Episodios extends HiloSoporteTam4 {
   private static final Logger LOGGER = Logger.getLogger(HiloSoporteTam4Episodios.class.getName());

   public HiloSoporteTam4Episodios(IColeccion coleccion, int hilo,
         Map<String, List<IAsociacionTemporal>> mapaHilo,
         List<IAsociacionTemporal> candidatos,
         List<IAsociacionTemporal> candidatosOriginales,
         IBusquedaParalelaSecuenciaAnotaciones mine) {
      super(coleccion, hilo, mapaHilo, candidatos, candidatosOriginales, mine);
   }

   @Override
   protected void calcularSoporte(ISecuencia secuencia, int sid){
      Iterator<List<Patron>> itVentanaActual = mine.getActualIterator(sid);
      List<Patron> encontrados = new ArrayList<Patron>();
      List<String> listaTipos = new ArrayList<String>();

      for(Evento evento : secuencia){
         List<Patron> ventanaActual = itVentanaActual.next();
         List<IAsociacionTemporal> receptores = mapaHilo.get(evento.getTipo());

         for(IAsociacionTemporal receptor : receptores){
            ((IAsociacionDiccionario)receptor).actualizaVentana(sid, evento);
         }

         // Se recorre la lista de la tabla hash
         for(IAsociacionTemporal receptor : receptores){

            //   Procesar únicamente aquellos candidatos que vienen con episodios completos,
            // y vienen por primera vez. (Si hay tipos de eventos que no provienen de episodios
            // en la anterior iteración se pudieron buscar adecuadamente.
            if(((IAsociacionConEpisodios)receptor).sonEpisodiosCompletos()){
               List<Patron> aux = receptor.getPatrones();
               List<PatronDictionaryFinalEvent> lista = new ArrayList<PatronDictionaryFinalEvent>();
               for(Patron patron : aux){
                  lista.add((PatronDictionaryFinalEvent)patron);
               }
               ((IAsociacionDiccionario)receptor).recibeEvento(sid, evento, savePatternInstances, lista, encontrados);
            }
         }


         // Se comprueban las anotaciones que haya de la iteración anterior
         listaTipos = mine.posiblesTiposParaAmpliar(ventanaActual, listaTipos);
         boolean conservar = false;
         for(Patron aux : ventanaActual){
            PatronDictionaryFinalEvent patron = (PatronDictionaryFinalEvent)aux;
            if(patron==null){
               LOGGER.severe("Patron nulo");
               continue;
            }
            for(String tipo : listaTipos){

               List<PatronDictionaryFinalEvent> extensiones = patron.getExtensiones(tipo);
               if(extensiones!=null && !extensiones.isEmpty()){
                  //Hay una copia de cada modelo para cada hilo, los nodos del arco controlan esto
                  NodoHilos nodo = (NodoHilos)mine.getRaizArbol().obtenerNodoEnArbol(extensiones.get(0).getAsociacion().getTipos());
                  ModeloEpisodiosDFE posible = (ModeloEpisodiosDFE)nodo.getModelo(hilo);
                  extensiones = extensionesHilo(extensiones, posible);
                  //si no es episodio completo no se busca
                  if(posible.sonEpisodiosCompletos()){
                     posible.recibeEvento(sid, evento, savePatternInstances, extensiones, encontrados);
                  }else{
                     conservar=true;
                  }
               }
            }
            if(conservar){
               // Añadir la vieja anotación a la lista de anotaciones aceptadas
               encontrados.add(patron);
            }
         }

         ventanaActual.clear();
         ventanaActual.addAll(encontrados); //aquí se guardan las anotaciones de la ventana en actual
         encontrados.clear();

      }
   }

}
