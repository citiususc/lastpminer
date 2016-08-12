package source.busqueda.paralela.jerarquia;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import source.busqueda.paralela.HiloSoporte;
import source.busqueda.paralela.IBusquedaParalelaSecuenciaAnotaciones;
import source.busqueda.paralela.ParallelHelper;
import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.modelo.IAsociacionDiccionario;
import source.modelo.IAsociacionTemporal;
import source.modelo.arbol.NodoHilos;
import source.modelo.episodios.ModeloEpisodiosDFE;
import source.patron.Patron;
import source.patron.PatronDictionaryFinalEvent;


public class HiloSoporteTam4 extends HiloSoporte {
   private static final Logger LOGGER= Logger.getLogger(HiloSoporteTam4.class.getName());

   protected Map<String,List<IAsociacionTemporal>> mapaHilo;
   protected List<IAsociacionTemporal> candidatos;
   protected List<IAsociacionTemporal> candidatosOriginales;
   protected IBusquedaParalelaSecuenciaAnotaciones mine;


   public HiloSoporteTam4(IColeccion coleccion, int hilo,
         Map<String,List<IAsociacionTemporal>> mapaHilo, List<IAsociacionTemporal> candidatos,
         List<IAsociacionTemporal> candidatosOriginales, IBusquedaParalelaSecuenciaAnotaciones mine) {
      super(coleccion, hilo, null, mine.isSavePatternInstances());
      this.mapaHilo = mapaHilo;
      this.candidatos = candidatos;
      this.candidatosOriginales = candidatosOriginales;
      this.mine = mine;
   }

   @Override
   public void run() {
      int sid = mine.getSiguienteSecuencia(coleccion);
      while(sid != -1){
         //System.out.println("Hilo #" + hilo + " con secuencia " + sid);
         calcularSoporte(coleccion.get(sid), sid);
         sid = mine.getSiguienteSecuencia(coleccion);
      }
      ParallelHelper.agregarCandidatos(candidatosOriginales, candidatos);
   }

   protected void calcularSoporte(ISecuencia secuencia, int sid){
      //List<Patron> encontrados = Collections.synchronizedList(new ArrayList<Patron>());
      List<Patron> encontrados = new ArrayList<Patron>();
      List<String> listaTipos = new ArrayList<String>();
      Iterator<List<Patron>> itVentanaActual = mine.getActualIterator(sid);//itActual.next().iterator();
      for(Evento evento : secuencia){
         List<Patron> ventanaActual = itVentanaActual.next();
         //ventanaActual = Collections.synchronizedList(ventanaActual);
         // Actualizar ventana de las asociaciones temporales con el tipo de evento leído
         List<IAsociacionTemporal> receptores = mapaHilo.get(evento.getTipo());
         for(IAsociacionTemporal aux : receptores){
            ((IAsociacionDiccionario)aux).actualizaVentana(sid, evento);
         }
         // Calcular las asociaciones temporales a comprobar para el evento actual
         listaTipos = mine.posiblesTiposParaAmpliar(ventanaActual, listaTipos);
         for(Patron aux : ventanaActual){
            PatronDictionaryFinalEvent patron = (PatronDictionaryFinalEvent)aux;
            for(String tipo : listaTipos){
               List<PatronDictionaryFinalEvent> extensiones = patron.getExtensiones(tipo);
               if(extensiones!=null && !extensiones.isEmpty()){
                  NodoHilos nodo = (NodoHilos)mine.getRaizArbol().obtenerNodoEnArbol(extensiones.get(0).getTipos());
                  //if(nodo == null){
                  //   LOGGER.severe("Nodo no debería ser null");
                  //   continue;
                  //}
                  IAsociacionDiccionario posible = (IAsociacionDiccionario)nodo.getModelo(hilo);
                  if(posible == null){
                     posible = (ModeloEpisodiosDFE)extensiones.get(0).getAsociacion();
                     LOGGER.warning("(Tam4)No debería entrar aquí: " + posible.getTipos() );
                  }
                  extensiones = extensionesHilo(extensiones, posible);
                  posible.recibeEvento(sid, evento, savePatternInstances, extensiones, encontrados);
               }
            }
         }
         ventanaActual.clear();
         ventanaActual.addAll(encontrados);
         encontrados.clear();
      }
   }

   protected List<PatronDictionaryFinalEvent> extensionesHilo(List<PatronDictionaryFinalEvent> extensiones, IAsociacionDiccionario posible){
      List<PatronDictionaryFinalEvent> extensionesHilos = new ArrayList<PatronDictionaryFinalEvent>(extensiones.size());
      int added = 0;
      for(Patron p : posible.getPatrones()){
         PatronDictionaryFinalEvent p1 = (PatronDictionaryFinalEvent)p;
         if(extensiones.contains(p1)){
            extensionesHilos.add(p1);
            added++;
         }
      }
      boolean todos = true;
      for(PatronDictionaryFinalEvent p2 : extensiones){
         if(!extensionesHilos.contains(p2)){
            todos = false;
         }
      }
      if(added < extensiones.size()){//if(!todos && added < extensiones.size()){
         LOGGER.fine("No se han encontrado todas las extensiones: " + added + " de " + extensiones.size()
               + (todos?  ". Pero todas las que hay están" : ""));

      }
      return extensionesHilos;
   }
}
