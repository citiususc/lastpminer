package source.busqueda.paralela.episodios;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import source.busqueda.episodios.MineCEDFE;
import source.busqueda.paralela.IBusquedaParalelaSecuenciaAnotaciones;
import source.busqueda.paralela.jerarquia.HiloSoporteTam4;
import source.evento.Episodio;
import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.modelo.IAsociacionConEpisodios;
import source.modelo.IAsociacionDiccionario;
import source.modelo.IAsociacionTemporal;
import source.modelo.arbol.NodoHilos;
import source.patron.Patron;
import source.patron.PatronDictionaryFinalEvent;

public class HiloSoporteTam5Episodios extends HiloSoporteTam4 {
   private static final Logger LOGGER = Logger.getLogger(HiloSoporteTam5Episodios.class.getName());

   /*
    * Atributos
    */

   protected int tamActual;

   /*
    * Constructores
    */

   public HiloSoporteTam5Episodios(IColeccion coleccion, int hilo,
         Map<String, List<IAsociacionTemporal>> mapaHilo,
         List<IAsociacionTemporal> candidatos,
         List<IAsociacionTemporal> candidatosOriginales,
         IBusquedaParalelaSecuenciaAnotaciones mine, int tamActual) {
      super(coleccion, hilo, mapaHilo, candidatos, candidatosOriginales, mine);
      this.tamActual = tamActual;
   }

   @Override
   protected void calcularSoporte(ISecuencia secuencia, int sid){
      Iterator<List<Patron>> itVentanaActual = mine.getActualIterator(sid);
      List<Patron> encontrados = new ArrayList<Patron>();
      List<String> listaTipos = new ArrayList<String>();

      for(Evento evento : secuencia){
         List<Patron> ventanaActual = itVentanaActual.next();

         // Actualizar ventana de las asociaciones temporales con el tipo de evento leído
         List<IAsociacionTemporal> receptores = mapaHilo.get(evento.getTipo());
         for(IAsociacionTemporal receptor : receptores){
            ((IAsociacionDiccionario)receptor).actualizaVentana(sid, evento);
         }

         listaTipos = mine.posiblesTiposParaAmpliar(ventanaActual, listaTipos);
         for(Patron aux : ventanaActual){
            PatronDictionaryFinalEvent patron = (PatronDictionaryFinalEvent)aux;
            // Comprobar de qué tipo de anotación se trata
            boolean conservar=false;
            // Caso a: anotación hecha en la anterior iteración
            if(patron.getTipos().length == tamActual-1){
               for(String tipo : listaTipos){
                  List<PatronDictionaryFinalEvent> extensiones = patron.getExtensiones(tipo);
                  conservar = buscarExtensionesPatron(extensiones, sid, evento, encontrados, conservar);
               }
            }else if(patron.getTipos().length == tamActual -2){
               // Caso b: anotación hecha hace 2 iteraciones
               // Comprobación por episodios.
               for(Episodio episodio : ((MineCEDFE)mine).getListaEpisodios()){
                  List<PatronDictionaryFinalEvent> extensiones1 = patron.getExtensiones(episodio.getTipoInicio());
                  if(extensiones1!=null && !extensiones1.isEmpty()){
                     for(PatronDictionaryFinalEvent intermedio : extensiones1){
                        List<PatronDictionaryFinalEvent> extensiones = intermedio.getExtensiones(episodio.getTipoFin());
                        conservar = buscarExtensionesPatron(extensiones, sid, evento, encontrados, conservar);
                     }
                  }
               }
            }else{
               LOGGER.severe("Se están guardando anotaciones de iteraciones anteriores a 2");
            }
            if(conservar){
               // Añadir la vieja anotación a la lista de anotaciones aceptadas
               encontrados.add(patron);
            }
         }

         ventanaActual.clear();
         ventanaActual.addAll(encontrados);
         encontrados.clear();
      }
   }

   /**
    * Devuelve si hay que conservar la anotacion original
    * @param extensiones
    * @return si hay que conservar o no
    */
   private boolean buscarExtensionesPatron(List<PatronDictionaryFinalEvent> extensiones, int sid, Evento evento, List<Patron> encontrados, boolean conservarAnterior){
      boolean conservar = conservarAnterior;
      if(extensiones!=null && !extensiones.isEmpty()){
         //Hay una copia del modelo para cada hilo y el nodo los gestiona
         NodoHilos nodo = (NodoHilos)mine.getRaizArbol().obtenerNodoEnArbol(extensiones.get(0).getTipos());
         IAsociacionDiccionario posible = (IAsociacionDiccionario)nodo.getModelo(hilo);
         extensiones = extensionesHilo(extensiones, posible);
         if(((IAsociacionConEpisodios)posible).sonEpisodiosCompletos()){
            posible.recibeEvento(sid,evento, savePatternInstances, extensiones,encontrados);
         }else{
            conservar=true;
         }
      }
      return conservar;
   }
}
