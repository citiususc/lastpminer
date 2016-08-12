package source.busqueda.jerarquia;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Logger;

import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.modelo.IAsociacionDiccionario;
import source.modelo.IAsociacionTemporal;
import source.modelo.clustering.IClustering;
import source.patron.Patron;
import source.patron.PatronDictionaryFinalEvent;

/**
 * Estrategia basada en HSTP en la que se purgan los patrones de tamaño 4 o mayor que se van a
 * buscar en la ventana que no tienen entre las anotaciones todos sus padres menos uno.
 *
 * @author vanesa.graino
 *
 */
public class MineDictionaryExpressV2 extends MineDictionary {
   private static final String TIEMPOS_PURGA_EXTENSIONES = "purgaExt";
   private static final Logger LOGGER = Logger.getLogger(MineDictionaryExpressV2.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   /*
    * Atributos propios
    */

   protected int[] extensionesEvitadas;
   //protected long[] tiemposPurgaExtensiones;

   //{
      //associationClassName = "ModeloDictionaryExpress";
      //patternClassName = "PatronDictionaryFinalEvent";
   //}

   public MineDictionaryExpressV2(String executionId,
         boolean savePatternInstances, boolean saveAllAnnotations,
         boolean saveRemovedEvents, IClustering clustering, boolean removePatterns) {
      super(executionId, savePatternInstances, saveAllAnnotations, saveRemovedEvents,
            clustering, removePatterns);
      registroT.addOtrosTiempos(TIEMPOS_PURGA_EXTENSIONES);
   }

   @Override
   protected void iniciarContadores(int tSize, int cSize){
      super.iniciarContadores(tSize, cSize);
      extensionesEvitadas = new int[tSize];
      //tiemposPurgaExtensiones = new long[tSize];
   }

   /*
    * Sólo cambian dos cosas (cuando el tamaño es 4 o más). Por cada evento se calcula
    * una lista ordenada con los identificadores de los patrones de las anotaciones del patrón.
    * Ademas, antes de llamar a recibeEvento se llama al método purgeCandidates.
    *
    * (non-Javadoc)
    * @see source.busqueda.jerarquia.MineDictionary#calcularSoporte(java.util.List, java.util.List)
    */
   @Override
   protected void calcularSoporteGeneral(IColeccion coleccion, int tamActual) {
      // Caso general.
      // Se actualizan las ventanas de los posibles receptores.
      //
      // Se tiene calculado qué asociaciones podrían ocurrir en cada
      // posible ventana temporal. Para cada evento leído, se comprueba qué asociaciones
      // de entre las posibles realmente ocurren y, finalmente, se calcula qué asociaciones
      // se podrían encontrar en la siguiente iteración en base a las encontradas.
      Iterator<List<List<Patron>>> itActual = anotaciones.getActual().iterator();
      List<Patron> encontrados = new ArrayList<Patron>();
      List<String> listaTipos = new ArrayList<String>();
      int sid = 0;
      for(ISecuencia secuencia : coleccion){
         Iterator<List<Patron>> itVentanaActual = itActual.next().iterator();
         for(Evento evento : secuencia){
            List<Patron> ventanaActual = itVentanaActual.next();
            List<Integer> patternIDs = new ArrayList<Integer>();
            for(Patron anotacion: ventanaActual){
               patternIDs.add(anotacion.getID());
            }
            Collections.sort(patternIDs); //esto es necesario
            // Actualizar ventana de las asociaciones temporales con el tipo de evento leído
            List<IAsociacionTemporal> receptores = mapa.get(evento.getTipo());
            for(IAsociacionTemporal receptor : receptores){
               ((IAsociacionDiccionario)receptor).actualizaVentana(sid, evento);
            }
            // Calcular las asociaciones temporales a comprobar para el evento actual
            listaTipos = posiblesTiposParaAmpliar(ventanaActual, listaTipos);
            for(Patron aux : ventanaActual){
               PatronDictionaryFinalEvent patron = (PatronDictionaryFinalEvent)aux;
               for(String tipo : listaTipos){
                  List<PatronDictionaryFinalEvent> extensiones = patron.getExtensiones(tipo);
                  if(extensiones!=null && !extensiones.isEmpty()){
                     //Nodo nodo = raizArbol.obtenerNodoEnArbol(extensiones.get(0).getTipos());
                     extensiones = purgeCandidates(extensiones, patternIDs, tamActual);
                     if(extensiones.isEmpty()){ continue;}
                     //IAsociacionDiccionario posible = (IAsociacionDiccionario)nodo.getModelo();
                     IAsociacionDiccionario posible = (IAsociacionDiccionario)extensiones.get(0).getAsociacion();
                     posible.recibeEvento(sid,evento, savePatternInstances,extensiones,encontrados);
                  }
               }
            }
            ventanaActual.clear();
            ventanaActual.addAll(encontrados);
            encontrados.clear();
         }
         sid++;
      }
      LOGGER.info("Extensiones evitadas: " + extensionesEvitadas[tamActual-1]);
   }

   /**
    * Se comprueba que entre las anotaciones de los patrones de aComprobar hay tam-1 padres.
    * En este método el for exterior recorre los ids y el interior los candidatos.
    * @param aComprobar
    * @param patternIDs
    * @return
    */
   protected List<PatronDictionaryFinalEvent> purgeCandidates(List<PatronDictionaryFinalEvent> aComprobar,
         List<Integer> patternIDs, int tam){
      List<PatronDictionaryFinalEvent> aComprobarFinal = new ArrayList<PatronDictionaryFinalEvent>(aComprobar);
      //Ultimo índice en cada padre
      List<Integer> indices = new ArrayList<Integer>(Collections.nCopies(aComprobarFinal.size(), 0));
      List<Integer> faltantes = new ArrayList<Integer>(Collections.nCopies(aComprobarFinal.size(), (Integer)null));

      ListIterator<PatronDictionaryFinalEvent> itAComprobar;
      List<PatronDictionaryFinalEvent> padres;
      Integer id, padreId;
      for(int i=0,x=patternIDs.size();i<x && !aComprobarFinal.isEmpty();i++){
         id = patternIDs.get(i);
         itAComprobar = aComprobarFinal.listIterator();
         int j=0;
         while(itAComprobar.hasNext()){
            padres = itAComprobar.next().getPadres();
            if(indices.get(j) == padres.size()){ continue; }
            padreId = padres.get(indices.get(j)).getID();
            int compare = id.compareTo(padreId);
            // permitimos que falte una anotacion y guardamos constancia de ello
            if(compare>0 && faltantes.get(j) == null){
               faltantes.set(j, padreId);
               //seguimos con el siguiente padre que ya no puede faltar
               indices.set(j, indices.get(j)+1);
               //padreId = padres.get(indices.get(j)).getID();
               compare = id.compareTo(padreId);
            }
            // la anotacion no contiene un segundo padre: se borra el candidato
            if(compare>0){
               indices.remove(j);
               itAComprobar.remove();//aComprobarFinal.remove(j);
               faltantes.remove(j);
               extensionesEvitadas[tam-1]++;
               continue; //j--;
            }else if(compare==0){//aumentar índice
               //la anotacion es el padre actual
               indices.set(j, indices.get(j)+1);
            }
            j++;
         }
      }
      return aComprobarFinal;
   }

   @Override
   public void escribirEstadisticasEstrategia(List<List<IAsociacionTemporal>> resultados,
         Writer fwp, boolean shortVersion, int maxIteracion) throws IOException{
      super.escribirEstadisticasEstrategia(resultados, fwp, shortVersion, maxIteracion);

      fwp.write(SEPARADOR);

      long[] tiemposPurgaExtensiones = registroT.getTiempos(TIEMPOS_PURGA_EXTENSIONES);
      fwp.write("\nTiempos empleados por iteración en purga de extensiones:\n");
      for(int i=0;i<tiemposPurgaExtensiones.length;i++){
         fwp.write(nivel(i) + timeFormat(tiemposPurgaExtensiones[i]) + "\n");
      }

      fwp.write("\n\nExtensiones evitadas en cada iteración:\n");
      for(int i=0;i<extensionesEvitadas.length;i++){
         fwp.write(nivel(i) + numberFormat(extensionesEvitadas[i]) + "\n");
      }
   }

}
