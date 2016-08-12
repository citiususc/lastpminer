package source.busqueda;

import java.io.IOException;
import java.io.Writer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import source.evento.Evento;
import source.evento.EventoEliminado;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.excepciones.FactoryInstantiationException;
import source.modelo.IAsociacionConEpisodios;
import source.modelo.IAsociacionTemporal;
import source.modelo.clustering.IClustering;
import source.patron.GeneradorID;
import source.patron.Patron;

public abstract class AbstractMine implements IEliminaEventos {
   private static final Logger LOGGER = Logger.getLogger(AbstractMine.class.getName());

   protected static boolean verbose = false; //=false
   protected static final String SEPARADOR = "\n------------------------------------------------------------------------------\n";
   protected static final NumberFormat NF = NumberFormat.getInstance(new Locale("es", "ES"));

   private static final String[] MENSAJES_TIEMPOS = new String[]{
      "Tiempo de generación de candidatos", //candidatos
      "Tiempo de cálculo de soporte", //soporte
      "      Tiempo de cálculo de consistencia", //consistencia
      "   Tiempo de cálculo de combinación", //fundir
      "   Tiempo de cálculo de asociación", //asociaciones
      "   Tiempo de construcción de modelo", //modelo
      "Tiempo de cálculo de patrones", //calcula
      "Tiempo de cálculo de purga", //purga
      "TIEMPO TOTAL"
   };


   /*
    * Attributes
    */

   private String executionId;
   protected final boolean savePatternInstances;
   private IClustering clustering;

   protected GeneradorID genID;

   /**
    * Borrar patrones que ya no se utilizan en el proceso
    *
    */
   protected boolean removePatterns = false;

   //Máxima iteración alcanzada en la minería
   int maxIteracion = 0;

   public RegistroTiempoTotal registroT = new RegistroTiempoTotal();
   //public RegistroTiempoTotal registroT = new RegistroTiempos(); //TODO cambiar para tiempos detallados

   //memoria
   protected long[] memoriaNivel;//memoria total ocupada al final de cada iteracion - despues del calculo de frecuencia
   //protected long[] memoriaSinPurgaNivel; //memoria total ocupada al final de cada iteracion - despues del calculo de candidatos
   //protected long[] memoriaConPurgaNivel; //memoria total ocupada al final de cada iteracion - despues de la purga de candidatos

   //estadísticas
   protected long[] asociacionesNivel; //asociaciones temporales generadas en cada nivel
   protected long[] patronesGeneradosNivel; //patrones generados en cada nivel
   protected long[] patronesFrecuentesNivel; //patrones frecuentes (no se incluyen parciales si hay episodios)
   protected long[] patronesGeneradosConAuxiliaresNivel; //patrones puente entre otros patrones
   protected long[] patronesPosiblesNivel; // posibles patrones de cada nivel
   protected long[] patronesDescartadosNivel; //patrones descartados en cada nivel
   protected long[] patronesNoGeneradosNivel; //patrones descartados sin generar en cada nivel

   protected transient int windowSize;
   protected String associationClassName;
   protected String patternClassName;
   protected int numHilos;
   protected int tamMaximoPatron = -1; // -1 es indefinido

   /**
    * Lista de eventos eliminados en cada iteracion.
    * Ejemplo en la posición 0 está la lista de los eventos eliminados en la primera iteración.
    * Cada objeto EventoEliminado tiene el identificador de secuencia (sid) y el propio evento.
    */
   private List<List<EventoEliminado>> eventosEliminados;
   protected boolean saveRemovedEvents;

   public static void setVerbose(boolean newVerbose){
      verbose = newVerbose;
   }

   public static boolean isVerbose(){
      return verbose;
   }

   /*
    * Constructors
    */

   protected AbstractMine(String executionId, boolean savePatternInstances, boolean saveRemovedEvents,
         IClustering clustering, boolean removePatterns){
      this.executionId = executionId;
      this.savePatternInstances = savePatternInstances;
      this.clustering = clustering;
      this.saveRemovedEvents = saveRemovedEvents;
      this.removePatterns = removePatterns;
      this.genID = new GeneradorID();
   }

   /*
    * Methods
    */

   public long[] getPatronesGeneradosConAuxiliaresNivel(){
      return patronesGeneradosConAuxiliaresNivel;
   }

   public long[] getTiemposSoporte(){
      return registroT.getTiemposSoporte();
   }

   public long getTiempoTotal(){
      return registroT.tiempoTotal;
   }

   public long[] getTiempos(){
      return registroT.getTiempos();
   }

   public long[][] getEstadisticas(){
      long[][] estadisticas = new long[5][asociacionesNivel.length];
      estadisticas[0] = asociacionesNivel;
      estadisticas[1] = patronesGeneradosNivel;
      estadisticas[2] = patronesPosiblesNivel;
      estadisticas[3] = patronesDescartadosNivel;
      estadisticas[4] = patronesNoGeneradosNivel;
      return estadisticas;
   }

   public long[][] getUsoMemoria(){
      long[][] memoria = new long[3][asociacionesNivel.length];
      memoria[0] = memoriaNivel;
      //memoria[1] = memoriaSinPurgaNivel;
      //memoria[2] = memoriaConPurgaNivel;
      return memoria;
   }

   public String getExecutionId() {
      return executionId;
   }

   public boolean isSavePatternInstances() {
      return savePatternInstances;
   }

   public IClustering getClustering() {
      return clustering;
   }

   public void setClusteringClassName(IClustering clustering) {
      this.clustering = clustering;
   }

   public abstract Map<String, List<IAsociacionTemporal>> getMapa(Integer tSize);


   @Override
   public List<List<EventoEliminado>> getEventosEliminados() {
      return eventosEliminados;
   }

   @Override
   public boolean isSaveRemovedEvents() {
      return saveRemovedEvents;
   }


   //Ojo al sobreescribir esta operación
   public void calculaPatrones(List<IAsociacionTemporal> candidatos, int supmin, int tam) throws FactoryInstantiationException{
      registroT.tiempoCalcula(0, true);
      for(IAsociacionTemporal candidato : candidatos){
         candidato.calculaPatrones(supmin, patternClassName, genID, savePatternInstances);
      }
      registroT.tiempoCalcula(0, false);
   }



   /**
    * Guarda un evento eliminado si el atributo saveRemovedEvents está activado
    * TODO: no es thread safe
    * @param e Evento que es eliminado
    * @param sid Identificador de la secuencia al que pertenecía el evento
    * @param iteracion Iteración del algoritmo en la que es borrado. La iteración
    * 0 significa que es borrado en cálculo de soporte del patrón semilla. El resto
    * se identifica con el tamaño de patrón buscado en la iteración.
    * @param ee El evento eliminado.
    */
   public void notificarEventoEliminado(Evento e, int sid, int iteracion){
      e.setEliminado(iteracion);
      if(saveRemovedEvents){
         eventosEliminados.get(sid).add(new EventoEliminado(sid, e, iteracion));
      }
   }

   /**
    * Inicializa la lista de eventos borrados por secuencia si es necesario,
    * es decir, si {@code saveRemovedEvents} está activo.
    * @param cSize Número de secuencias en la colección.
    */
   protected void iniciarEstructuraBorrados(int cSize){
      if(saveRemovedEvents){
         eventosEliminados = new ArrayList<List<EventoEliminado>>();
         for(int i=0; i<cSize;i++){
            eventosEliminados.add(new LinkedList<EventoEliminado>());
         }
      }
   }

   protected void iniciarContadores(final int tSize, final int cSize){
      asociacionesNivel = new long[tSize];
      patronesGeneradosNivel = new long[tSize];
      patronesFrecuentesNivel = new long[tSize];
      patronesGeneradosConAuxiliaresNivel = new long[tSize];
      patronesDescartadosNivel = new long[tSize];
      patronesPosiblesNivel = new long[tSize];
      patronesNoGeneradosNivel = new long[tSize];
      memoriaNivel = new long[tSize];
      //memoriaConPurgaNivel = new long[tSize];
      //memoriaSinPurgaNivel = new long[tSize];

      registroT.iniciar(tSize);

      this.genID.resetGenerator();
      iniciarEstructuraBorrados(cSize);
   }

   public int getWindowSize() {
      return windowSize;
   }

   public int getTamMaximoPatron() {
      return tamMaximoPatron;
   }

   public void setTamMaximoPatron(int tamMaximoPatron) {
      this.tamMaximoPatron = tamMaximoPatron;
   }

   public int getNumHilos() {
      return numHilos;
   }

   public void setExecutionId(String executionId){
      this.executionId = executionId;
   }


   /* ------------------------------- MÉTODOS DE MINERÍA ---------------------------*/

   protected <T extends IAsociacionTemporal> Map<String,List<T>> construyeMapa(int tSize, List<String> tipos){
      Map<String,List<T>> nuevoMapa = new HashMap<String,List<T>>(tSize);
      for(int i=0;i<tSize;i++){
         nuevoMapa.put(tipos.get(i),new ArrayList<T>());
      }
      return nuevoMapa;
   }


   /**
    * Este método es llamado por los generadores de patrones cuando se intentan combinar
    * los patrones con los indices actuales de patIndex y se detecta una inconsistencia
    * con el algoritmo de Floyd Warshall. En consecuencia, se actualizan los contadores
    * de patrones descartados y no generados. Finalmente, se incrementan los contadores
    * para pasar a los siguientes índices de patrones que se combinarán.
    *
    * @param tam - iteración actual
    * @param currentIndex - índice del patrón que se estaba combinando y que ha fallado
    * @param patIndex -
    * @param patCount
    * @return
    */
   /*public int notificarPatronDescartado(int tam, int currentIndex, int[] patIndex, int[] patCount){
      patronesDescartadosNivel[tam-1]++;
      patIndex[currentIndex]++;

      if(currentIndex!=tam-1){ // si el índice no era del último modelo que se va a combinar
         int imposibles=1;
         for(int i=currentIndex+1;i<tam;i++){
            if(patCount[i]!=0){
               imposibles *= patCount[i]; // patIndex[o]==0
            }
         }
         patronesNoGeneradosNivel[tam-1]+=imposibles-1;
      }

      int o;
      for(o=currentIndex;o>=1;o--){
         if(patIndex[o]<patCount[o]){
            break;
         }else{
            patIndex[o]=0;
            patIndex[o-1]++;
         }
      }
      return o;
   }*/

   protected void notificarPatronGenerado(int tam, List<Patron> patrones, Patron patron){
      registroT.tiempoConsistencia(tam-1, true);
      boolean esConsistente = patron.esConsistente(genID);
      registroT.tiempoConsistencia(tam-1, false);

      if(esConsistente){
         if(patrones.contains(patron)){
            // ¿Se da alguna vez?
            LOGGER.log(Level.WARNING, "Se ha generado un patrón igual a otro generado anteriormente con otros patrones de partida.");
            patronesDescartadosNivel[tam-1]++;
         }else{
            patrones.add(patron);
            notificarPatronGeneradoConsistente(patron);
         }
      }else{
         patronesDescartadosNivel[tam-1]++;
      }
   }

   protected void notificarPatronGeneradoConsistente(Patron p){
      //No se hace nada?
   }



   /**
    * Este método es utlizado por los métodos generarCandidatasTam2
    * @param tam
    * @param pSize
    * @param modelo
    * @param mod
    * @param candidatas
    * @param nuevoMapa
    */
   protected void notificarModeloGenerado(int tam, int pSize,
         IAsociacionTemporal modelo, Iterable<String> mod,
         List<IAsociacionTemporal> candidatas,
         Map<String,List<IAsociacionTemporal>> nuevoMapa){
      asociacionesNivel[tam-1]++;
      patronesGeneradosNivel[tam-1]+= pSize;

      candidatas.add(modelo);
      for(String tipo : mod){
         nuevoMapa.get(tipo).add(modelo);
      }
   }
   protected void notificarModeloGenerado(int tam, int pSize,
         IAsociacionTemporal modelo, String[] mod,
         List<IAsociacionTemporal> candidatas,
         Map<String,List<IAsociacionTemporal>> nuevoMapa){
      notificarModeloGenerado(tam, pSize, modelo, Arrays.asList(mod), candidatas,
            nuevoMapa);
   }

   /**
    * Este método se utiliza cuando hay episodios, con o sin semilla
    * @param tam
    * @param pSize
    * @param modelo
    * @param mod
    * @param buscar
    * @param candidatas
    * @param candidatasGeneradas
    * @param nuevoMapa
    * @param nuevoMapaGeneradas
    */
   protected void notificarModeloGenerado(int tam, int pSize,
         IAsociacionTemporal modelo, String[] mod, boolean buscar,
         List<IAsociacionTemporal> candidatas,
         List<IAsociacionTemporal> candidatasGeneradas,
         Map<String,List<IAsociacionTemporal>> nuevoMapa){
      notificarModeloGenerado(tam, pSize, modelo, Arrays.asList(mod), buscar,
            candidatas, candidatasGeneradas, nuevoMapa);
   }
   protected void notificarModeloGenerado(int tam, int pSize,
         IAsociacionTemporal modelo, List<String> mod, boolean buscar,
         List<IAsociacionTemporal> candidatas,
         List<IAsociacionTemporal> candidatasGeneradas,
         Map<String,List<IAsociacionTemporal>> nuevoMapa){
      asociacionesNivel[tam-1]++;
      patronesGeneradosNivel[tam-1]+= pSize;

      candidatasGeneradas.add(modelo);

      // Añadir una entrada a la lista de cada tipo de evento
      // que contiene el nuevo candidato
      // Si buscar==true, entonces se mete la asociación temporal en mapa y candidatas
      /*if(buscar){
         candidatas.add(modelo);
         nuevoMapa.get(mod.get(0)).add(modelo);
      }
      if(buscar){
         for(int k=1;k<tam;k++){
            nuevoMapa.get(mod.get(k)).add(modelo);
         }
      }*/

      if(buscar){
         candidatas.add(modelo);
         for(int k=0;k<tam;k++){
            nuevoMapa.get(mod.get(k)).add(modelo);
         }
      }

   }
   /**
    *
    * @param tam
    * @param pSize
    * @param modelo
    * @param mod
    * @param buscar
    * @param candidatas
    * @param candidatasGeneradas
    * @param nuevoMapa
    * @param nuevoMapaGeneradas
    */
   protected void notificarModeloGenerado(int tam, int pSize,
         IAsociacionTemporal modelo, String[] mod, boolean buscar,
         List<IAsociacionTemporal> candidatas,
         List<IAsociacionTemporal> candidatasGeneradas,
         Map<String,List<IAsociacionTemporal>> nuevoMapa,
         Map<String,List<IAsociacionTemporal>> nuevoMapaGeneradas){
      notificarModeloGenerado(tam, pSize, modelo, Arrays.asList(mod), buscar,
            candidatas, candidatasGeneradas, nuevoMapa, nuevoMapaGeneradas);
   }
   /**
    *
    * @param tam - La iteración actual
    * @param pSize - La cantidad de patrones de la nueva asociación candidata
    * @param modelo - El modelo generado
    * @param mod - Los tipos de la nueva asociación temporal
    * @param buscar - Si es una asociación que va a buscarse o no
    * @param candidatas - Se guardan solo las asociaciones que se van a buscar
    * @param candidatasGeneradas - Se guardan todas las asociaciones generadas
    * @param nuevoMapa - Se guardan solo las asociaciones que se van a buscar
    * @param nuevoMapaGeneradas - Se guardan todas las asociaciones generadas
    */
   protected void notificarModeloGenerado(int tam, int pSize,
         IAsociacionTemporal modelo, List<String> mod, boolean buscar,
         List<IAsociacionTemporal> candidatas,
         List<IAsociacionTemporal> candidatasGeneradas,
         Map<String,List<IAsociacionTemporal>> nuevoMapa,
         Map<String,List<IAsociacionTemporal>> nuevoMapaGeneradas){
      asociacionesNivel[tam-1]++;
      patronesGeneradosNivel[tam-1]+= pSize;

      candidatasGeneradas.add(modelo);

      // Añadir una entrada a la lista de cada tipo de evento
      // que contiene el nuevo candidato
      // Si buscar==true, entonces se mete la asociación temporal en mapa y candidatas
      if(buscar){
         candidatas.add(modelo);
         nuevoMapa.get(mod.get(0)).add(modelo);
      }
      nuevoMapaGeneradas.get(mod.get(0)).add(modelo);
      for(int k=1;k<tam;k++){
        if(buscar){ nuevoMapa.get(mod.get(k)).add(modelo); }
        nuevoMapaGeneradas.get(mod.get(k)).add(modelo);
      }
   }


   /**
    *
    * @param coleccion
    * @param tipos - Los tipso tienen que estar ordenados
    */
   protected void purgarEventosDeTiposNoFrecuentes(IColeccion coleccion, List<String> tipos){
      int totales = 0, borrados = 0;
      for(int iSeq=0; iSeq<coleccion.size(); iSeq++){
         ISecuencia secuencia = coleccion.get(iSeq);
         totales += secuencia.size();
         for(int iEvento=0; iEvento<secuencia.size(); iEvento++){
            Evento evento = secuencia.get(iEvento);
            //if(!tipos.contains(evento.getTipo())){
            //Aprovechando que los tipos están ordenados
            if(Collections.binarySearch(tipos, evento.getTipo()) < 0){
               //Eliminar evento
               secuencia.remove(iEvento--);
               borrados++;
               notificarEventoEliminado(evento, iSeq, 1);
            }
         }
      }

      imprimirEliminados(LOGGER, borrados, totales-borrados);
   }


   /* -------------------------------- IMPRESIÓN DE RESULTADOS -----------------------*/


   private String asegurarLongitudConTabs(String cadena, int longitud){
      StringBuilder sb = new StringBuilder(cadena);
      while(sb.length()<longitud){
         sb.append(" ");
      }
      return sb.toString();
   }

   protected String numberFormat(long value){
      return NF.format(value);
   }

   protected String numberFormat(int value){
      return NF.format(value);
   }

   protected String timeFormat(long millis){
      return NF.format(millis) + " ms";
   }

   protected String timeFormat(Calendar diferencia){
      return timeFormat(diferencia, "", "");
   }

   protected String timeFormat(Calendar diferencia, String prepend, String append){
      StringBuffer cadena = new StringBuffer();
      int horas = diferencia.get(Calendar.HOUR_OF_DAY)-1;
      if(horas>0) cadena.append(horas + " hrs. ");
      int minutos = diferencia.get(Calendar.MINUTE);
      if(minutos>0) cadena.append(minutos + " min. ");
      int segundos = diferencia.get(Calendar.SECOND);
      if(segundos>0) cadena.append(segundos + " seg. ");
      int milis = diferencia.get(Calendar.MILLISECOND);
      if(milis>0) cadena.append(milis + " milis. ");

      if(cadena.length()==0){
         return "";
      }
      return prepend + cadena.toString().trim() + append;

      //return (diferencia.get(Calendar.HOUR_OF_DAY)-1) + " hrs. " + diferencia.get(Calendar.MINUTE) + " min. " +
      //      diferencia.get(Calendar.SECOND) + " seg. " + diferencia.get(Calendar.MILLISECOND) + " miliseg.";
   }


   public static String humanReadableByteCount(long bytes, boolean si) {
      int unit = si ? 1000 : 1024;
      if (bytes < unit){
         return bytes + " B";
      }
      int exp = (int) (Math.log(bytes) / Math.log(unit));
      String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
      return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
   }

   protected void imprimirTiempos(final Logger logger){
      Calendar diferencia = new GregorianCalendar();
      if(verbose){
         long[] tiempos = getTiempos();
         //for(int i=0;i<MENSAJES_TIEMPOS.length;i++){
         for(int i : new int[]{0, 4, 3, 2, 5, 1, 6, 7, 8}){
            diferencia.setTimeInMillis(tiempos[i]);
            String mensaje = MENSAJES_TIEMPOS[i];
            logger.log(Level.INFO, "{0}. " + mensaje.trim() + ": " + timeFormat(tiempos[i]) + timeFormat(diferencia, " (", ")") + "\n", executionId);
         }
      }else{
         diferencia.setTimeInMillis(registroT.tiempoTotal);
         logger.log(Level.INFO, "{0}. Tiempo total: " + timeFormat(registroT.tiempoTotal) + timeFormat(diferencia, " (", ")") + "\n", executionId);
      }
   }

   protected void registrarUsoMemoria(Runtime runtime, int tam){
      if(verbose){
         memoriaNivel[tam-1] = runtime.totalMemory() - runtime.freeMemory();
      }
   }

   protected void llamarGC(){
      //TODO descomentar para más estadísticas
      //System.gc();
      //System.runFinalization();
   }

   protected void imprimirInstanciasPatrones(final Logger logger, String mensaje, int tam){
      //TODO descomentar para más estadísticas
      //LOGGER.info("@@@@ # instancias de patrones (tam=" + tam + "," + mensaje + "): " + Patron.noOfInstances);
   }

   protected void imprimirUsoMemoria(final Logger logger, int tam){
      maxIteracion = tam;

      if(verbose){
         logger.log(Level.INFO, "{0}. " + nivel(tam,": ") + "memoria utilizada al terminar la iteracion: "
               + humanReadableByteCount(memoriaNivel[tam],false), executionId);
         //logger.log(Level.INFO, "{0}. " + nivel(tam,": ") + "memoria utilizada al generar candidatos de la siguiente iteración: "
         //      + humanReadableByteCount(memoriaSinPurgaNivel[tam],false), executionId);
         //logger.log(Level.INFO, "{0}. " + nivel(tam,": ") + "memoria utilizada al purgar estos candidatos: "
         //      + humanReadableByteCount(memoriaConPurgaNivel[tam],false), executionId);
      }
   }

   protected void imprimirTiempo(String tipo, final Logger logger, int tam, Calendar diferencia){
      if(verbose){
         logger.log(Level.INFO, "{0}. Tiempo de cálculo de " + tipo + " tamaño " + (tam) +": " + timeFormat(diferencia), executionId);
      }
   }

   protected void imprimirTiempoIteracion(final Logger logger, int tam, Calendar diferencia){
      maxIteracion = tam;
      logger.log(Level.INFO, "{0}. Tiempo de la iteración " +(tam)+": " + timeFormat(diferencia), executionId);
   }

   protected void imprimirNiveles(final Logger logger, List<List<IAsociacionTemporal>> todos){
      logger.log(Level.INFO, "{0}. Niveles: " + todos.size(), executionId);
      for(int i=0;i<todos.size();i++){
         logger.log(Level.INFO, "{0}. " + nivel(i,": ") + todos.get(i).size() + " secuencias", executionId);
         logger.log(Level.INFO, "{0}. Número de asociaciones: " + todos.get(i).size(), executionId);
      }
   }

   protected void imprimirEliminados(final Logger logger, int eliminados, int iSecuencia, int sSize ){
      logger.log(Level.INFO, "{0}. Eliminados " + NF.format(eliminados) + " eventos de la secuencia "
            + iSecuencia +". Quedan " + NF.format(sSize) + " eventos.", executionId);
   }

   protected void imprimirEliminados(final Logger logger, int eliminados, int sSize ){
      logger.log(Level.INFO, "{0}. Eliminados " + NF.format(eliminados) + " eventos de la colección en esta iteracion. "
            + "Quedan " + NF.format(sSize) + " eventos.", executionId);
   }


   /*---------------------------------- En fichero -------------------------*/


   public final void escribirEstadisticas(List<List<IAsociacionTemporal>> resultados,
         Writer fwp, boolean shortVersion) throws IOException{

      GregorianCalendar diferencia = new GregorianCalendar();
      long[][] memoria=getUsoMemoria();
      long[] tiempos = getTiempos();

      if(verbose){
         fwp.write("ATENCIÓN! Modo verbose activo\n\n");
      }

      if(!shortVersion){
         int longitud = MENSAJES_TIEMPOS[2].length()+1; //longitud del mensaje más largo

         //for(int i=0;i<MENSAJES_TIEMPOS.length;i++){
         for(int i : new int[]{0, 4, 3, 2, 5, 1, 6, 7, 8}){
            diferencia.setTimeInMillis(tiempos[i]);
            String mensaje = MENSAJES_TIEMPOS[i];
            fwp.write(asegurarLongitudConTabs(mensaje + ":", longitud) + "\t" + timeFormat(tiempos[i]) + timeFormat(diferencia, " (", ")") + "\n");
         }
      }else{
         diferencia.setTimeInMillis(registroT.tiempoTotal);
         fwp.write("Tiempo total: " + timeFormat(registroT.tiempoTotal) + timeFormat(diferencia, " (", ")") + "\n");
      }

      fwp.write(SEPARADOR);

      fwp.write("\nTiempo de cálculo por iteraciones:\n\n");
      long[] tiemposIteracion = registroT.getTiemposIteracion(),
            tiemposCandidatos= registroT.getTiemposCandidatos(),
            tiemposSoporte = registroT.getTiemposSoporte();

      for(int i=0;i<=maxIteracion;i++){
         fwp.write(nivel(i)
               + " " + timeFormat(tiemposIteracion[i])
               + ", generacion: " + timeFormat(tiemposCandidatos[i])
               + ", soporte: " + timeFormat(tiemposSoporte[i])
               + "\n");
      }

      fwp.write(SEPARADOR);

      int descNoGenerados = 0, descGenerados = 0, secuenciasGeneradas=0, patronesGenerados=0,
            secuenciasFrecuentes=0, patronesFrecuentes=0;

      int rSize = resultados.size();

      //for(int i=0;i<resultados.size();i++){
      for(int i=0; i<=maxIteracion; i++){
         descNoGenerados+=patronesNoGeneradosNivel[i];
         descGenerados+=patronesDescartadosNivel[i];
         secuenciasGeneradas+=asociacionesNivel[i];
         patronesGenerados+=patronesGeneradosNivel[i];
         secuenciasFrecuentes += i<rSize? resultados.get(i).size() : 0;

         //Las asociaciones para todos los niveles
         fwp.write(nivel(i) + "asociaciones generadas: "+ NF.format(asociacionesNivel[i])
               + " vs. asociaciones frecuentes: " + (i<rSize? NF.format(resultados.get(i).size()) : 0) + "\n");

         //En niveles 2 o superior hay patrones
         if(i>0){
            /*int patronesFrecuentesNivel=0;
            for(int j=0; i<rSize && j<resultados.get(i).size(); j++){
               patronesFrecuentesNivel += resultados.get(i).get(j).getPatrones().size();
            }*/
            patronesFrecuentes += patronesFrecuentesNivel[i];
            if(i>1){
               fwp.write(nivel(i) + "posibles patrones: " + NF.format(patronesPosiblesNivel[i])
                     +" -> patrones generados: " + NF.format(patronesGeneradosNivel[i])
                     + ", patrones resultado: " + NF.format(patronesFrecuentesNivel[i]) + "\n");
               fwp.write(nivel(i) + "patrones inconsistentes en generación: " + NF.format(patronesDescartadosNivel[i]) + "\n");
               fwp.write(nivel(i) + "patrones descartados sin generar: " + NF.format(patronesNoGeneradosNivel[i]) + "\n");
            }else{
               fwp.write(nivel(i) + "patrones frecuentes: " + NF.format(patronesFrecuentesNivel[i]) + "\n");
            }
         }
         fwp.write("\n");
      }

      fwp.write("\nTotal asociaciones temporales -> generadas: " + NF.format(secuenciasGeneradas)
            + ", frecuentes: " + NF.format(secuenciasFrecuentes) + "\n" );
      fwp.write("Total patrones -> generados: " + NF.format(patronesGenerados)
            + ", resultado: " + NF.format(patronesFrecuentes)
            + ",\n\tdescartados en generación: " + NF.format(descGenerados)
            + ", imposibles/descartados sin generar: " + NF.format(descNoGenerados) + "\n");

      fwp.write(SEPARADOR);

      fwp.write("\nUso de memoria\n\n");
      for(int i=0;i<=maxIteracion;i++){
         fwp.write(nivel(i) + "Memoria utilizada en el nivel: " + humanReadableByteCount(memoria[0][i],false)+ "\n");
         //fwp.write(nivel(i) + "Memoria utilizada antes de la purga: " + humanReadableByteCount(memoria[1][i],false) + "\n");
         //fwp.write(nivel(i) + "Memoria utilizada después de la purga: " + humanReadableByteCount(memoria[2][i],false) + "\n\n");
      }

      escribirEstadisticasEstrategia(resultados, fwp, shortVersion, maxIteracion);

      fwp.write(SEPARADOR);

      fwp.flush();

   }

   protected void imprimirParcialesYFrecuentes(List<List<IAsociacionTemporal>> resultados,
         Writer fwp, boolean shortVersion) throws IOException{
      int rSize = resultados.size();

      fwp.write(SEPARADOR);
      fwp.write("Patrones parciales y patrones completos por nivel:\n");
      for(int i=2; i<rSize; i++){ //por cada tamaño mayor que
         int patronesFrecuentesNivel = 0, patronesParcialesNivel = 0;
         for(IAsociacionTemporal asoc: resultados.get(i)){
            if(asoc instanceof IAsociacionConEpisodios){
               IAsociacionConEpisodios asocCE = (IAsociacionConEpisodios)asoc;
               if(asocCE.sonEpisodiosCompletos()){
                  patronesFrecuentesNivel += asoc.getPatrones().size();
               }else{
                  patronesParcialesNivel += asoc.getPatrones().size();
               }
            }else{
               patronesFrecuentesNivel += asoc.getPatrones().size();
            }
         }
         fwp.write(nivel(i) + "patrones completos: " + NF.format(patronesFrecuentesNivel)
               + ", parciales: " + NF.format(patronesParcialesNivel) + "\n");
      }
   }

   protected String nivel(int i, String...sep){
      return "Nivel " + (i+1) + (sep==null || sep.length == 0? "-> " : sep[0]);
   }

   /**
    * Este es el método que las versiones del algoritmo tienen que sobreescribir para
    * añadir más estadísticas al fichero.
    * @param resultados
    * @param fwp
    * @param shortVersion
    * @throws IOException
    */
   public abstract void escribirEstadisticasEstrategia(List<List<IAsociacionTemporal>> resultados,
         Writer fwp, boolean shortVersion, int maxIteracion) throws IOException;

   /**
    *
    * @return
    */
   public String getPatternClassName(){
      return patternClassName;
   }
}



