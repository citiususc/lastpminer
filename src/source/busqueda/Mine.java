package source.busqueda;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.excepciones.AlgoritmoException;
import source.excepciones.FactoryInstantiationException;
import source.excepciones.ModelosBaseNoValidosException;
import source.excepciones.ModelosBaseVaciosException;
import source.modelo.AssociationFactory;
import source.modelo.IAsociacionEvento;
import source.modelo.IAsociacionTemporal;
import source.modelo.clustering.IClustering;
import source.patron.Patron;

/**
 *
 * @author vanesa.graino
 *
 */
public class Mine extends AbstractMine implements IBusqueda {

   private static final Logger LOGGER = Logger.getLogger(Mine.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   /**
    * mapa es una hashtable que por cada tipo de evento tiene una lista con las IAsociacionTemporal
    * que tienen a dicho evento
    */
   protected Map<String, List<IAsociacionTemporal>> mapa;

   {
      associationClassName = "Modelo";
      patternClassName = "Patron";
   }

   /*
    * Constructors
    */
   public Mine(String executionId, boolean savePatternInstances, boolean saveRemovedEvents,
         IClustering clustering, boolean removePatterns) {
      super(executionId, savePatternInstances, saveRemovedEvents, clustering, removePatterns);
   }

   // Entrada: lista de modelos de esta iteracion, secuencia/registro de entrada
   // Precondición: secuencia está ordenada temporalmente de forma creciente
   // Poscondición: candidatas está actualizada con todas las instancias encontradas
   // Salida: ninguna explícita, lista candidatas actualizada
   protected void calcularSoporte(List<IAsociacionTemporal> candidatas, IColeccion coleccion){
      int sid = 0;
      for(ISecuencia secuencia : coleccion){
         for(Evento ev : secuencia){
            List<IAsociacionTemporal> receptores = mapa.get(ev.getTipo());
            // Si un tipo de evento no es frecuente es necesaria esta línea
            // si no se borran de la colección
            if(receptores != null){
               for(IAsociacionTemporal receptor : receptores){
                  receptor.recibeEvento(sid, ev, savePatternInstances);
               }
            }
         }
         sid++;
      }
   }

   // Entrada: lista de modelos de esta iteración, soporte mínimo
   // Precondición:
   // Poscondición:
   // Salida: lista de modelos con al menos cobertura mínima
   protected void purgarCandidatas(List<IAsociacionTemporal> candidatas, int supmin, int tamActual){
      registroT.tiempoPurgar(tamActual-1, true);
      for(int i=candidatas.size()-1; i>=0; i--){
         IAsociacionTemporal modelo = candidatas.get(i);
         if(modelo.necesitaPurga(supmin)){
            candidatas.remove(i); // Eliminar de candidatas
            for(String tipo : modelo.getTipos()){
               mapa.get(tipo).remove(modelo);
            }
         }else{
            patronesFrecuentesNivel[tamActual-1] += modelo.getPatrones().size();
         }
      }
      registroT.tiempoPurgar(tamActual-1, false);
   }

   //@Override
   protected void generarCandidatasTam2(List<IAsociacionTemporal> anteriores, List<String> tipos,
         List<IAsociacionTemporal> candidatas, Map<String,List<IAsociacionTemporal>> nuevoMapa) throws FactoryInstantiationException{
      final int tam=2, aSize = anteriores.size();
      int i, j;
      IAsociacionEvento base;
      IAsociacionTemporal modelo;
      String[] modArray;

      for(i=0; i<aSize-1; i++){
         base = (IAsociacionEvento)anteriores.get(i);
         for(j=i+1; j<aSize; j++){ // NO genera tipos repetidos
            String tipo = ((IAsociacionEvento)anteriores.get(j)).getTipoEvento();
            modArray = new String[]{ base.getTipoEvento(), tipo };
            modelo = crearModelo(modArray);
            notificarModeloGenerado(tam, 0, modelo, modArray, candidatas, nuevoMapa);
         }
      }
   }

   protected IAsociacionTemporal crearModelo(String[] modArray) throws FactoryInstantiationException{
      return AssociationFactory.getInstance().getAssociationInstance(associationClassName,
            modArray, windowSize, getClustering(), numHilos);
   }

   //Devuelve el nuevo mapa
   protected Map<String,List<IAsociacionTemporal>> generarCandidatasGeneral(int tam, List<IAsociacionTemporal> anteriores,
         List<String> tipos, List<IAsociacionTemporal> candidatas,
         Map<String,List<IAsociacionTemporal>> mapaFuente, Map<String,List<IAsociacionTemporal>> mapaDestino) throws FactoryInstantiationException{
      final int tSize = tipos.size();
      int i, j, k, lSize;
      IAsociacionTemporal padre1, padre2, modelo;
      String[] modArray;
      List<String> mod;
      GeneradorPatrones genp = new GeneradorPatrones(tam, this);

      // Recorrer listas
      for(i=0;i<tSize;i++){
         List<IAsociacionTemporal> lista = mapaFuente.get(tipos.get(i));
         lSize = lista.size();
         j = 0;
         // Recorre la lista hasta llegar a la posición del primer padre posible
         // de la lista para el tipo i (la primera asociación que comienza con el tipo i-esimo)
         while(j<lSize && lista.get(j).getTipos()[0] != tipos.get(i)){
            j++;
         }

         for(; j<lSize; j++){ // Recorre la lista, elige primer padre
            padre1 = lista.get(j);
            //for(k=j;k<lSize;k++){ // Genera secuencias repitiendo el último tipo
            for(k=j+1; k<lSize; k++){ // Recorre la lista, elige segundo padre
               modArray = Arrays.copyOf(padre1.getTipos(), padre1.getTipos().length + 1);
               mod = Arrays.asList(modArray);
               padre2 = lista.get(k);
               // Comparar todos los tipos menos el último para ambos padres
               if(mod.subList(0, tam-2).containsAll(Arrays.asList(padre2.getTipos()).subList(0, tam-2))){
                  // Solo se diferencian en el último tipo, añadir el último elemento
                  String tipo = padre2.getTipos()[tam-2];
                  modArray[modArray.length-1] = tipo;

                  genp.setPadre(padre1, 0);
                  genp.setPadre(padre2, 1);

                  registroT.tiempoAsociaciones(tam-1, true);
                  boolean valido = genp.comprobarSubasociaciones(tam, mod, mapaFuente);
                  registroT.tiempoAsociaciones(tam-1, false);
                  if(!valido){ continue; }

                  List<Patron> patrones = genp.generarPatrones(modArray);

                  // Construir el modelo si hay patrones candidatos
                  registroT.tiempoModelo(tam-1, true);
                  // Solo creamos y guardamos el modelo si tiene patrones candidatos
                  if(!patrones.isEmpty()){
                     //modelo = AssociationFactory.getInstance().getAssociationInstance(associationClassName, modArray, windowSize,
                     //      patrones, isSavePatternInstances(), numHilos);
                     modelo = crearModelo(modArray, patrones, genp);
                     // Hay: añadir punteros en la tabla hash y a candidatas
                     notificarModeloGenerado(tam, patrones.size(),
                           modelo, modArray, candidatas, mapaDestino);
                  }
                  registroT.tiempoModelo(tam-1, false);
               }else{
                  // Ya no van a coincidir más, pasar a 'siguiente' j
                  break;
               }
            } // for k
         } // for j
      } // for i
      return mapaDestino;
   }

   /**
    * Genera patrones candidatos como la combinación de los demás patrones.
    * Los modelos devueltos no tienen submodelos no frecuentes y no contienen
    * tipos de eventos repetidos.
    * no hay dos modelos que compartan todos los tipos de eventos
    * @param anteriores Lista de modelos frecuentes anteriores
    * @param tipos Tipos existentes en el registro
    * @return Lista de modelos para la actual iteración
    * @throws FactoryInstantiationException
    */
   protected List<IAsociacionTemporal> generarCandidatas(final int tam, List<IAsociacionTemporal> anteriores,
         List<String> tipos) throws FactoryInstantiationException{
      //final int tam = anteriores.get(0).size() + 1;
      List<IAsociacionTemporal> candidatas = new ArrayList<IAsociacionTemporal>();

      if(tam==2){
         mapa = construyeMapa(tipos.size(), tipos);
         generarCandidatasTam2(anteriores, tipos, candidatas, mapa);
         return candidatas;
      }
      final int tSize = tipos.size();
      // Inicializar mapas
      Map<String,List<IAsociacionTemporal>> nuevoMapa = construyeMapa(tSize, tipos);
      mapa = generarCandidatasGeneral(tam, anteriores, tipos, candidatas, mapa, nuevoMapa); // Actualizar mapa global
      return candidatas;
   }

   protected IAsociacionTemporal crearModelo(String[] modArray, List<Patron> patrones,
         GeneradorPatrones genp) throws FactoryInstantiationException{
      return AssociationFactory.getInstance().getAssociationInstance(associationClassName, modArray, windowSize,
            patrones, numHilos);
   }

   protected List<String> purgarTiposYEventos(IColeccion coleccion, List<IAsociacionTemporal> actual,
         List<String> tipos, int tSize){
      List<String> tipoFinales = tipos;
      // Ya sabemos qué tipos de eventos son frecuentes, eliminar los que no lo son
      if(actual.size() != tSize){
         tipoFinales = new ArrayList<String>();
         for(IAsociacionTemporal modelo : actual){
            tipoFinales.add(modelo.getTipos()[0]);
         }
         purgarEventosDeTiposNoFrecuentes(coleccion, tipos);
      }
      return tipoFinales;
   }

   protected void inicializaEstructuras(List<String> tipos, List<IAsociacionTemporal> actual,
         int win, int cSize) throws FactoryInstantiationException{
      int tSize = tipos.size();
      iniciarContadores(tSize, cSize);

      windowSize = win;
      mapa = new HashMap<String,List<IAsociacionTemporal>>(tSize);

      asociacionesNivel[0] = tSize;
      for(String tipo : tipos){
         IAsociacionTemporal modelo = AssociationFactory.getInstance().getAssociationInstance("ModeloEvento", tipo, numHilos);
         List<IAsociacionTemporal> listaModelos = new ArrayList<IAsociacionTemporal>();
         actual.add(modelo);
         listaModelos.add(modelo);
         mapa.put(tipo, listaModelos);
      }
   }

   @Override
   public List<IAsociacionTemporal> calcularDistribuciones(List<String> tipos, IColeccion coleccion, int supmin, int win) {
      List<List<IAsociacionTemporal>> all = buscarModelosFrecuentes(tipos, coleccion, supmin, win, true);
      if (all != null && all.size() > 1) {
         return all.get(1);
      }
      return null;
   }

   @Override
   public List<List<IAsociacionTemporal>> buscarModelosFrecuentes(List<String> tipos, IColeccion coleccion, int supmin, int win) throws AlgoritmoException{
      return buscarModelosFrecuentes(tipos, coleccion, supmin, win, false);
   }

   protected List<List<IAsociacionTemporal>> buscarModelosFrecuentes(List<String> tipos, IColeccion coleccion, int supmin,
         int win, boolean hastaNivel2){
      try{

         int tSize = tipos.size();
         long tiempoAux, inicioTotal, inicioIteracion;
         List<IAsociacionTemporal> actual;
         List<List<IAsociacionTemporal>> todos;

         inicioTotal = System.currentTimeMillis();
         inicioIteracion = inicioTotal;

         todos = new ArrayList<List<IAsociacionTemporal>>();
         if(removePatterns){
            todos.add(null);
         }

         Runtime runtime = Runtime.getRuntime();

         actual = new ArrayList<IAsociacionTemporal>();

         //Se inicia la minería de tamaño 1
         inicializaEstructuras(tipos, actual, win, coleccion.size());
         imprimirInstanciasPatrones(LOGGER, "pre-soporte", 1);

         // Cálculo de soporte
         registroT.tiempoSoporte(0, true);
         calcularSoporte(actual, coleccion);
         registroT.tiempoSoporte(0, false);
         registrarUsoMemoria(runtime, 1);

         //hasta aquí inicialización

         purgarCandidatas(actual, supmin, 1);
         //memoriaSinPurgaNivel[0] = runtime.totalMemory() - runtime.freeMemory();

         // Ya sabemos qué tipos de eventos son frecuentes, eliminar los que no lo son
         registroT.tiempoPurgar(0, true);
         tipos = purgarTiposYEventos(coleccion, actual, tipos, tSize);
         tSize = tipos.size();

         //memoriaConPurgaNivel[0] =  runtime.totalMemory() - runtime.freeMemory();
         imprimirUsoMemoria(LOGGER, 0);
         registroT.tiempoPurgar(0, false);

         imprimirInstanciasPatrones(LOGGER, "pre-purga", 1);

         tiempoAux = System.currentTimeMillis();
         registroT.tiempoIteracion(0, tiempoAux - inicioIteracion);
         inicioIteracion = tiempoAux;

         //Fin tamaño 1

         int tam = 2;
         while(!actual.isEmpty()){

            if(removePatterns){
               todos.set(0, actual);
            }else{
               todos.add(actual);
            }

            llamarGC();
            //LOGGER.info("@@@@ # patrones frecuentes : " + patronesFrecuentesNivel[tam-2]);
            imprimirInstanciasPatrones(LOGGER, "post-purga", tam);

            //TODO la segunda condición no sirve con tipos repetidos
            if ( hastaNivel2 && tam > 2 || tam>tSize
                  || tamMaximoPatron!=-1 && tam>tamMaximoPatron) {
               break;
            }

            actual = buscarModelosIteracion(tipos, coleccion,
                  supmin, win, hastaNivel2, actual, runtime, tSize, tam);

            tiempoAux = System.currentTimeMillis();
            registroT.tiempoIteracion(tam-1, tiempoAux - inicioIteracion);
            inicioIteracion = tiempoAux;

            tam++;
         }

         registroT.setTiempoTotal(System.currentTimeMillis() - inicioTotal);

         imprimirNiveles(LOGGER, todos);
         imprimirTiempos(LOGGER);
         return todos;

      }catch(FactoryInstantiationException e){
         LOGGER.log(Level.SEVERE, "Problema instanciando en fábrica", e);
      }
      return Collections.emptyList();
   }

   protected List<IAsociacionTemporal> buscarModelosIteracion(List<String> tipos, IColeccion coleccion, int supmin,
         int win, boolean hastaNivel2, List<IAsociacionTemporal> anterior, Runtime runtime, int tSize,
         int tam) throws FactoryInstantiationException{
      LOGGER.info("Tam: " + tam);

      // Generación de candidatas
      registroT.tiempoCandidatas(tam-1, true);
      List<IAsociacionTemporal> actual = generarCandidatas(tam, anterior, tipos);
      registroT.tiempoCandidatas(tam-1, false);

      if(actual.isEmpty()){
         return actual;
      }

      //LOGGER.info("@@@@ # patrones generados: " + patronesGeneradosNivel[tam-1]
      //      + ", descartados: " + patronesDescartadosNivel[tam-1]
      //      + ", auxiliares: " + patronesGeneradosConAuxiliaresNivel[tam-1]);

      imprimirInstanciasPatrones(LOGGER, "post-gen", tam);

      llamarGC();
      imprimirInstanciasPatrones(LOGGER, "post-gen (2)", tam);

      // Cálculo de soporte
      registroT.tiempoSoporte(tam-1, true);
      calcularSoporte(actual, coleccion);
      long tiempoAux = registroT.tiempoSoporte(tam-1, false);

      Calendar diferencia = new GregorianCalendar();
      diferencia.setTimeInMillis(tiempoAux);
      imprimirTiempo("Soporte", LOGGER, tam, diferencia);
      registrarUsoMemoria(runtime, tam);

      llamarGC();
      imprimirInstanciasPatrones(LOGGER, "post-soporte", tam);

      // Cálculo de patrones
      calculaPatrones(actual, supmin, tam);

      //memoriaSinPurgaNivel[tam-1] = runtime.totalMemory() - runtime.freeMemory();
      llamarGC();
      imprimirInstanciasPatrones(LOGGER, "post-calcula", tam);

      // Purga
      purgarCandidatas(actual, supmin, tam);

      //memoriaConPurgaNivel[tam-1] = runtime.totalMemory() - runtime.freeMemory();
      imprimirUsoMemoria(LOGGER, tam-1);

      //LOGGER.info("@@@@ # patrones frecuentes : " + patronesFrecuentesNivel[tam-1]);

      return actual;
   }

   @Override
   public Map<String,List<IAsociacionTemporal>> getMapa(Integer tSize){
      if(tSize != null){
         mapa = new HashMap<String,List<IAsociacionTemporal>>(tSize);
      }
      return mapa;
   }

   public Map<String,List<IAsociacionTemporal>> getMapa(){
      return mapa;
   }

   protected void setMapa(Map<String,List<IAsociacionTemporal>> mapa){
      this.mapa = mapa;
   }

   protected void iniciarEstructurasReinicio(List<String> tipos, List<IAsociacionTemporal> modelosBase,
         int win, int cSize) throws FactoryInstantiationException{
      int tSize = tipos.size();
      iniciarContadores(tSize, cSize);
      windowSize = win;
      mapa = new HashMap<String,List<IAsociacionTemporal>>(tSize);

      // Inicializar Mapa
      for(String tipo : tipos){
         mapa.put(tipo, new ArrayList<IAsociacionTemporal>());
      }
      // Insertar modelos base en Mapa
      for(IAsociacionTemporal modelo : modelosBase){
         for(String tipo : modelo.getTipos()){
            if(mapa.containsKey(tipo)){
               mapa.get(tipo).add(modelo);
            }
         }
      }
   }

   //TODO: cuando se reinicia la busqueda los tiempos de las estadísticas sólo
   // son representativos de esa parte de la búsqueda. Debería poder saberse
   // a qué se corresponden estos tiempos.
   /**
    *   Busca un conjunto de patrones y asociaciones temporales frecuentes a partir
    * del conjunto de patrones contenidos en 'modelosBase'. El objetivo de este método
    * es proporcionar una forma de reanudar una búsqueda sobre una determinada colección.
    * Por ejemplo, una vez hecha una búsqueda y en base a las distribuciones de
    * frecuencia, se podría optar por modificar el agrupamiento hecho, y querer reanudar
    * la búsqueda desde ese punto, como si el agrupamiento fuese el deseado.
    * Por eso NO se comprobará si los elementos de 'modelosBase' son frecuentes,
    * simplemente se asumirá que sí lo son.
    * @param tipos Tipos de eventos a considerar de la colección.
    * @param coleccion Colección de secuencias de eventos en la que buscar patrones.
    * @param modelosBase Conjunto de asociaciones temporales y patrones base de la búsqueda.
    * @param supmin Umbral de frecuencia mínima para los patrones.
    * @param win Tamaño de ventana a utilizar en la búsqueda.
    * @return Lista de asociaciones temporales frecuentes encontradas a partir de 'modelosBase'.
    * @throws ModelosBaseVaciosException
    */
   @Override
   public List<List<IAsociacionTemporal>> reiniciarBusqueda(List<String> tipos, IColeccion coleccion,
         List<IAsociacionTemporal> modelosBase, int supmin, int win) throws ModelosBaseNoValidosException{
      if(modelosBase == null || modelosBase.isEmpty()){
         throw new ModelosBaseVaciosException();
      }
      try{
         int tSize = tipos.size();
         Runtime runtime = Runtime.getRuntime();
         // Inicializar estructuras

         List<IAsociacionTemporal> actual;
         int tam = modelosBase.get(0).size();

         // Para que los patrones de tamaño i estén como lo estarían
         // ejecutando buscarModelosFrecuentes
         List<List<IAsociacionTemporal>> todos = new ArrayList<List<IAsociacionTemporal>>();
         if(removePatterns){
            todos.add(null);
         }else{
            for(int i=1; i<tam; i++){
               todos.add(new ArrayList<IAsociacionTemporal>());
            }
         }
         iniciarEstructurasReinicio(tipos, modelosBase, win, coleccion.size());

         actual = modelosBase;
         tam++;
         // Se asume que todos los modelosBase son frecuentes.
         long tiempoAux, inicioIteracion = System.currentTimeMillis();
         while(!actual.isEmpty()){
            if(removePatterns){
               todos.set(0, actual);
            }else{
               todos.add(actual);
            }

            //TODO la segunda condición no sirve con tipos repetidos
            if ( /*hastaNivel2 && tam > 2 ||*/ tam>tSize
                  || tamMaximoPatron!=-1 && tam>tamMaximoPatron) {
               break;
            }

            actual = buscarModelosIteracion(tipos, coleccion, supmin, win, false, actual, runtime, tSize, tam);

            tiempoAux = System.currentTimeMillis();
            registroT.tiempoIteracion(tam-1, tiempoAux - inicioIteracion);
            inicioIteracion = tiempoAux;

            tam++;
         }

         imprimirNiveles(LOGGER, todos);
         imprimirTiempos(LOGGER);
         return todos;
      }catch(FactoryInstantiationException e){
         LOGGER.log(Level.SEVERE, "Problema instanciando en fábrica", e);
      }
      return Collections.emptyList();
   }

   @Override
   public void escribirEstadisticasEstrategia(
         List<List<IAsociacionTemporal>> resultados, Writer fwp,
         boolean shortVersion, int maxIteracion) throws IOException {
      // no hay estadísticas propias de la estrategia
   }

}
