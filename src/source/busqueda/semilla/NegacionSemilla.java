package source.busqueda.semilla;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Logger;

import source.busqueda.IBusquedaConSemilla;
import source.busqueda.negacion.NegacionMine;
import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.excepciones.AlgoritmoException;
import source.excepciones.FactoryInstantiationException;
import source.excepciones.SemillasNoValidasException;
import source.modelo.AssociationFactory;
import source.modelo.IAsociacionTemporal;
import source.modelo.IMarcasIntervalos;
import source.modelo.clustering.IClustering;
import source.modelo.semilla.ModeloSemilla;

/**
 * Un único patrón semilla unicamente con tipos positivos
 * @author vanesa.graino
 *
 */
public class NegacionSemilla extends NegacionMine implements IBusquedaConSemilla {
   private static final Logger LOGGER = Logger.getLogger(NegacionSemilla.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }


   List<List<int[]>> intervalosInteresantes;

   {
//      treeClassName = "SupernodoNegacion";
      associationClassName = "ModeloNegacionMarcas";
//      patternClassName = "PatronNegacion";
   }

   public NegacionSemilla(String executionId, boolean savePatternInstances,
         boolean saveRemovedEvents, IClustering clustering,
         boolean removePatterns) {
      super(executionId, savePatternInstances, saveRemovedEvents, clustering,
            removePatterns);
   }

// Sacada de MineIntervalMarks
   /*
    * A diferencia del método en SemillaConjuncion los intervalos no se borran,
    * se mantienen para utilizarlos en el cálculo de frecuencia.
    */
   protected void calcularSoporteSemilla(IColeccion coleccion) throws AlgoritmoException{
      if(coleccion.isEmpty()){
         throw new AlgoritmoException("Coleccion vacía");
      }
      int sSize;
      Evento ev; // Evento leído
      Evento bv; // Evento de principio de ventana
      int sid = 0;
      int borrados=0, restantes=0, eliminados=0;
      ListIterator<Evento> inicioVentana, finalVentana;
      int[] ultimaOcurrencia, intervaloActual = {0,0};
      List<int[]> intervalosSecuencia;
      int tmp;
      int indiceIntervalo; //índice del intervalo

      for(ISecuencia secuencia : coleccion){
         if(secuencia.isEmpty()){ continue; }
         inicioVentana = secuencia.listIterator();
         bv = inicioVentana.next(); // Evento de principio de ventana
         sSize = secuencia.size();
         eliminados = 0;
         ISecuencia copia = secuencia.clone();
         finalVentana = copia.listIterator();
         intervalosSecuencia = intervalosInteresantes.get(sid);//new ArrayList<int[]>();
         indiceIntervalo = 0;

         while(finalVentana.hasNext()){
            ev = finalVentana.next();
            tmp = ev.getInstante();
            List<IAsociacionTemporal> receptores = mapa.get(ev.getTipo());
            boolean hayOcurrencia = false;
            intervaloActual[0] = tmp;
            intervaloActual[1] = tmp;
            if(receptores != null){
               for(IAsociacionTemporal receptor : receptores){
                  // Calcular el intervalo de eventos que incluyen ocurrencias
                  IMarcasIntervalos modelo = (IMarcasIntervalos) receptor;
                  int antes = modelo.getSoporte();
                  modelo.recibeEvento(sid, ev, savePatternInstances);
                  int despues = modelo.getSoporte();
                  if(antes != despues){ // Se encontró alguna ocurrencia
                     hayOcurrencia = true;
                     ultimaOcurrencia = modelo.getUltimaEncontrada();
                     if(intervaloActual[0] > ultimaOcurrencia[0]){
                        intervaloActual[0] = ultimaOcurrencia[0];
                     }
                  }
               }
            }
            int inicioW = ev.getInstante() - windowSize - 1;
            if(hayOcurrencia){
               intervaloActual = insertarIntervalo(intervalosSecuencia, intervaloActual);
            }
            // Avanzar el comienzo de la ventana, y eliminar aquellos eventos que
            // no están en un intervalo de eventos utilizados
            while(bv.getInstante() <= inicioW){
               // Comprobar si 'bv' pertenece a algún intervalo activo
               boolean estaActivo = estaActivo(indiceIntervalo, intervalosSecuencia, inicioW, bv.getInstante());

               if(!estaActivo){
                  // No pertenece a ningún intervalo activo, eliminar
                  notificarEventoEliminado(bv, sid, 0);
                  inicioVentana.remove();
                  sSize--;
                  eliminados++;
               }
               bv = inicioVentana.next();
            }

            // Comprobar si algún intervalo activo salió de la ventana
            indiceIntervalo = actualizarIndiceIntervalo(intervalosSecuencia, bv.getInstante(), indiceIntervalo);
         }
         LOGGER.fine("Cantidad intervalos interesantes de #" + sid + ": " + intervalosSecuencia.size());
         sid++;

         borrados += eliminados;
         restantes += sSize;
      }
      imprimirEliminados(LOGGER, borrados, restantes);

   }

   protected int[] insertarIntervalo(List<int[]> intervalosActivos, int[] intervaloActual){

      // Ampliamos el intervalo con el tamaño de la ventana
      int swap = intervaloActual[1];
      intervaloActual[1] = intervaloActual[0] + windowSize;
      intervaloActual[0] = swap - windowSize;

      if(!intervalosActivos.isEmpty() && intervaloActual[0] <= intervalosActivos.get(intervalosActivos.size()-1)[1]){
         int[] ultimo = intervalosActivos.get(intervalosActivos.size()-1);
         // Se solapan
         ultimo[1] = intervaloActual[1];
         if(intervaloActual[0] < ultimo[0]){
            // El nuevo contiene al anterior
            ultimo[0] = intervaloActual[0];

            int[] anterior = intervalosActivos.size()<2 ? null : intervalosActivos.get(intervalosActivos.size()-2);
            // Si extendemos hacia atrás el intervalo puede que se solape con el anterior
            while(anterior != null && ultimo[0]<=anterior[1]){
               anterior[1] = ultimo[1];
               if(anterior[0] > ultimo[0]){
                  anterior[0] = ultimo[0];
               }
               intervalosActivos.remove(intervalosActivos.size()-1);
               ultimo = anterior;
               anterior = intervalosActivos.size()<2 ?  null : intervalosActivos.get(intervalosActivos.size()-2);
            }
         }
         return intervaloActual;
      }
      intervalosActivos.add(intervaloActual);
      return new int[2];

   }

   /**
    *
    * @param indiceIntervalo - Indice del primer intervalo que solapa con la ventana. Los anteriores no se comprobarán.
    * @param intervalosActivos - Lista de intervalos de la secuencia
    * @param inicioW - Instante temporal en el que comienza la ventana.
    * @param instante - Instante del evento que queremos saber si está activo
    * @return Si el evento está activo o no
    */
   protected boolean estaActivo(int indiceIntervalo, List<int[]> intervalosActivos, int inicioW, int instante ){
      boolean estaActivo = false;
      //Mientras haya intervalso y el evento no se anterior al inicio del primer intervalo
      for(int i=indiceIntervalo; i<intervalosActivos.size() && intervalosActivos.get(i)[0] <= instante ; i++){
         int[] intervalo = intervalosActivos.get(i);
         if(instante >= intervalo[0] && instante <= intervalo[1]){
            estaActivo = true;
            break;
         }
      }
      return estaActivo;
   }

   /**
    * Actualiza el índice del primer intervalo que solapa/está contenido con la ventana
    * teniendo en cuenta el nuevo instante temporal en el que comienza la ventana.
    * @param intervalosActivos - Lista de intervalos de la secuencia
    * @param inicioW - Instante temporal en el que comienza la ventana.
    * @param indiceIntervalo - Indice del primer intervalo que solapa con la ventana. Los anteriores no se comprobarán.
    * @return El indice actualizado del primer intervalo que solapa con la ventana.
    */
   protected int actualizarIndiceIntervalo(List<int[]> intervalosActivos, int inicioW, int indiceIntervalo){
      if(indiceIntervalo>=intervalosActivos.size()){
         return indiceIntervalo;
      }
      Iterator<int[]> iteradorIntervalosActivos = intervalosActivos.listIterator(indiceIntervalo);
      //int inicioW = bv.getInstante();
      while(iteradorIntervalosActivos.hasNext()){
         int[] intervalo = iteradorIntervalosActivos.next();
         if(intervalo[1] < inicioW){
            //iteradorIntervalosActivos.remove();
            indiceIntervalo++;
         }else{
            //Cuando llegamos a uno que está en la ventana ya no hay que seguir
            break;
         }
      }
      return indiceIntervalo;
   }


   /**
    * Se inicializan los intervalos interesantes de cada secuencia {@code s} como {@code<pre>{0,max_t(s)}</pre>}
    * @param coleccion
    */
   private void iniciarIntervalosInteresantes(IColeccion coleccion){
      intervalosInteresantes = new ArrayList<List<int[]>>(coleccion.size());
      //for(ISecuencia s : coleccion){
      for(int i=0; i<coleccion.size(); i++){
         //intervalosInteresantes.add(new ArrayList<int[]>(Arrays.asList(new int[]{0, s.lastInstant()})));
         intervalosInteresantes.add(new ArrayList<int[]>());
      }
   }

   @Override
   public List<List<IAsociacionTemporal>> buscarModelosFrecuentes(
         List<String> tipos, IColeccion coleccion,
         int supmin, int win) throws AlgoritmoException {
      return buscarModelosFrecuentes(tipos, coleccion, Collections.<ModeloSemilla> emptyList(), supmin, win);
   }

   protected void inicializaEstructuras(List<String> tipos, List<IAsociacionTemporal> candidatos, int win,
         String[] tiposSemilla, List<ModeloSemilla> semillas, List<List<IAsociacionTemporal>> semNivel,
         int cSize) throws FactoryInstantiationException{
      int tSize = tipos.size();
      iniciarContadores(tSize, cSize);
      windowSize = win;
      // Inicializar las estructuras auxiliares
      // Tipos de evento que no pertenecen a la semilla son los
      //primeros patrones candidatos
      mapa = new HashMap<String,List<IAsociacionTemporal>>(tSize);
//      mapaPares = new HashMap<String,List<IAsociacionTemporal>>(tipos.size());

      for(String tipo: tipos){
         semNivel.add(new ArrayList<IAsociacionTemporal>());
         mapa.put(tipo, new ArrayList<IAsociacionTemporal>());
//         mapaPares.put(tipo, new ArrayList<IAsociacionTemporal>());
         // Añadir los tipso de evento que no están en la semilla como candidatos
         //if(!tiposSemilla.contains(tipo)){
         if(Arrays.binarySearch(tiposSemilla,tipo)<0){
            //List<String> aux = new ArrayList<String>();
            //aux.add(tipo);
            //Modelo mod = new ModeloSemilla(aux,win, isSavePatternInstances(), getClustering());
            IAsociacionTemporal mod = AssociationFactory.getInstance().getSeedAssociationInstance(/*"ModeloSemilla",*/
                  new String[]{tipo}, win, getClustering(), numHilos);
            //IAsociacionTemporal mod = AssociationFactory.getInstance().getAssociationInstance(associationClassName,
            //      tipo, numHilos);
            candidatos.add(mod);
            mapa.get(tipo).add(mod);
         }
      }

      // Añadir las semillas como patrones candidatos
      for(int i=0;i<semillas.size();i++){
         //Modelo semilla = semillas.get(i);
         IAsociacionTemporal semilla = AssociationFactory.getInstance().getSeedAssociationInstance(/*"ModeloSemilla",*/ semillas.get(i), numHilos);
         candidatos.add(semilla);
         for(String tipo: semilla.getTipos()){
            mapa.get(tipo).add(semilla);
         }
      }
   }

   @Override
   public List<List<IAsociacionTemporal>> buscarModelosFrecuentes(
         List<String> tipos, IColeccion coleccion,
         List<ModeloSemilla> semillas, int supmin, int win)
         throws SemillasNoValidasException, AlgoritmoException {
      iniciarIntervalosInteresantes(coleccion);

      List<IAsociacionTemporal> actual = new ArrayList<IAsociacionTemporal>();
      List<List<IAsociacionTemporal>> semNivel = new ArrayList<List<IAsociacionTemporal>>();

      long inicioSemilla = System.currentTimeMillis();
      //inicializaEstructuras(tipos, actual, win, coleccion.size());
      String[] tiposSemilla = semillas.get(0).getTipos();
      inicializaEstructuras(tipos, actual, win, tiposSemilla, semillas, semNivel, coleccion.size());

      registroT.tiempoSoporte(0, true);
      calcularSoporteSemilla(coleccion);
      registroT.tiempoSoporte(0, false);

      return Collections.emptyList();
   }

   @Override
   public List<IAsociacionTemporal> calcularDistribuciones(List<String> tipos,
         IColeccion coleccion, List<ModeloSemilla> semillas, int supmin, int win)
         throws SemillasNoValidasException, AlgoritmoException {
      // TODO Auto-generated method stub
      return null;
   }




}

