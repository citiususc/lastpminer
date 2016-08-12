package source.busqueda.semilla;

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

import source.busqueda.GeneradorPatrones;
import source.busqueda.IBusquedaConSemillayEpisodios;
import source.busqueda.episodios.EpisodiosUtils;
import source.evento.Episodio;
import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.excepciones.AlgoritmoException;
import source.excepciones.FactoryInstantiationException;
import source.excepciones.ModelosBaseNoValidosException;
import source.excepciones.ModelosBaseVaciosException;
import source.excepciones.SemillasNoValidasException;
import source.modelo.AssociationFactory;
import source.modelo.IAsociacionConEpisodios;
import source.modelo.IAsociacionEvento;
import source.modelo.IAsociacionTemporal;
import source.modelo.clustering.IClustering;
import source.modelo.semilla.ModeloSemilla;
import source.patron.Patron;

/*
 *   Variante del algoritmo de búsqueda usando patrón semilla básico que permite trabajar con episodios.
 * Estos episodios se definen identificando un evento de inicio y otro de fin, de forma que una ocurrencia de un
 * evento inicio únicamente se pueda asociar con la siguiente ocurrencia del evento fin que no haya sido ya
 * asociada con un evento inicio, y viceversa.
 * Por ejemplo, si A y B son inicio y fin de episodio, y la ventana contiene AABB, el primer evento A únicamente
 * puede asociarse con el primer evento B, y el segundo evento A únicamente puede asociarse con el segundo evento
 * A, y viceversa.
 *
 *   Estos episodios se suministran por parámetro al algoritmo de búsqueda y se tendrán en cuenta a partir de la
 * búsqueda de patrones frecuentes de tamaño 2, donde se introducirán en las asociaciones temporales. En las
 * iteraciones posteriores estos episodios se heredan en cada asociación temporal a partir de las asociaciones
 * temporales que extiende. Además, aquellas asociaciones temporales que contengan únicamente un tipo de evento
 * de cualquier episodio no serán objeto de búsqueda, pero tampoco se eliminan del proceso de búsqueda, para así
 * poder generar patrones y asociaciones temporales posteriores.
 */
public class SemillaConjuncionCompleteEpisodes extends SemillaConjuncion implements IBusquedaConSemillayEpisodios{
   private static final Logger LOGGER = Logger.getLogger(SemillaConjuncionCompleteEpisodes.class.getName());
//   static {
//      LOGGER.setLevel(Level.ALL);
//   }

   public static Logger getLogger(){
      return LOGGER;
   }

   /*
    * Atributos
    */
   // mapa(y candidatas) puede estar vacío, lo que supone que esa iteración se omite, pero mientras mapaGeneradas no esté vacío
   // (y por tanto candidatasGeneradas) el proceso continua
   protected Map<String,List<IAsociacionTemporal>> mapaGeneradas; // Contiene todos los candidatos que se generaron en la iteración.
   protected List<IAsociacionTemporal> candidatasGeneradas; // ¿Debe estar a este nivel?
   protected List<Episodio> listaEpisodios;

   protected long[] patronesNoFrecuentesNivel;

   {
      associationClassName = "ModeloEpisodios";
      patternClassName = "Patron";
   }

   public SemillaConjuncionCompleteEpisodes(String executionId, boolean savePatternInstances,
         boolean saveRemovedEvents, IClustering clustering, boolean removePatterns){
      super(executionId,savePatternInstances,saveRemovedEvents, clustering, removePatterns);
   }

   /*
    * Métodos
    */

   /**
    * Genera patrones candidatos como la combinación de los demás patrones
    * Entrada: lista de modelos frecuentes anteriores, tipos existentes en el registro
    * Precondicion:
    * Poscondición: los modelos devueltos no contienen tipos de eventos repetidos (futuro: permitirlo)
    * no hay dos modelos que compartan todos los tipos de eventos
    * los modelos devueltos contienen la información sobre episodios que les corresponde
    * @return lista de modelos para la actual iteración
    */
   @Override
   protected void generarCandidatas(final int tam, List<IAsociacionTemporal> anteriores,
         List<String> tipos, List<IAsociacionTemporal> actual) throws FactoryInstantiationException{
      final int tSize = tipos.size(); //aSize = anteriores.size();
      //final int tam = anteriores.get(0).size() + 1;
      int i, j, k;
      //List<IAsociacionTemporal> candidatas = new ArrayList<IAsociacionTemporal>();
      candidatasGeneradas = new ArrayList<IAsociacionTemporal>();

      int lSize = 0;
      IAsociacionTemporal padre1, padre2, modelo;
      List<String> mod;
      String[] modArray;
      GeneradorPatrones genp = new GeneradorPatrones(tam, this);

      // Inicializar mapas
      Map<String,List<IAsociacionTemporal>> nuevoMapaGeneradas = new HashMap<String,List<IAsociacionTemporal>>(tSize);
      mapa.clear();
      for(String tipo : tipos){ //i=0;i<tSize;i++){
         //nuevoMapa.put(tipo,new ArrayList<IAsociacionTemporal>());
         mapa.put(tipo,new ArrayList<IAsociacionTemporal>());
         nuevoMapaGeneradas.put(tipo, new ArrayList<IAsociacionTemporal>());
      }

      for(i=0; i<tSize; i++){ // Lista recorrida
         List<IAsociacionTemporal> lista = mapaGeneradas.get(tipos.get(i));
         lSize = lista.size();
         j=0;
         // Recorre la lista hasta llegar a la posición del primer padre posible
         // de la lista para el tipo i
         while((j<lSize) && (lista.get(j).getTipos()[0] != tipos.get(i))){
            j++;
         }

         for(; j<lSize; j++){ // Recorre la lista, elige primer padre
            padre1 = lista.get(j);
            //for(k=j;k<lSize;k++){ // Genera secuencias repitiendo el último tipo
            for(k=j+1; k<lSize; k++){ // Recorre la lista, elige segundo padre
               modArray = Arrays.copyOf(padre1.getTipos(), padre1.getTipos().length + 1);
               //mod = new ArrayList<String>(Arrays.asList(padre1.getTipos()));
               mod = Arrays.asList(modArray);
               padre2 = lista.get(k);
               // Comparar todos los tipos menos el último para ambos padres
               if(mod.subList(0,tam-2).containsAll(Arrays.asList(padre2.getTipos()).subList(0,tam-2))){
                  // Solo se diferencian en el último tipo, añadir el último elemento
                  String tipo = padre2.getTipos()[tam-2];
                  modArray[modArray.length-1] = tipo;
                  genp.setPadre(padre1, 0);
                  genp.setPadre(padre2, 1);

                  registroT.tiempoAsociaciones(tam-1, true);
                  boolean valido = genp.comprobarSubasociaciones(tam, mod, mapaGeneradas);
                  registroT.tiempoAsociaciones(tam-1, false);

                  if(!valido){ continue; }

                  List<Patron> patrones = genp.generarPatrones(modArray);

                  // ¿Hay patrones candidatos?
                  registroT.tiempoModelo(tam-1, true);
                  if(!patrones.isEmpty()){
                     modelo = crearModelo(modArray, patrones, genp);
                     // Hay: añadir punteros en la tabla hash y a candidatas
                     // TODO insertar en orden o ordenar al final?
                     notificarModeloGenerado(tam, patrones.size(), modelo, modArray,
                           ((IAsociacionConEpisodios)modelo).sonEpisodiosCompletos(), actual,
                           candidatasGeneradas, mapa, nuevoMapaGeneradas);
                  }
                  registroT.tiempoModelo(tam-1, false);
               }else{
                  // Ya no van a coincidir más, pasar a 'siguiente' j
                  break;
               }
            } // for k
         } // for j
      } // for i
      LOGGER.log(Level.FINE, "Total generadas: " + actual.size() + "; generadas que se buscarán: " + candidatasGeneradas);
      mapaGeneradas = nuevoMapaGeneradas;
      //return candidatas;
   }

   protected IAsociacionTemporal crearModelo(String[] modArray, List<Patron> patrones, GeneradorPatrones genp) throws FactoryInstantiationException{
      List<Episodio> eps = new ArrayList<Episodio>();
      EpisodiosUtils.episodiosAsociacion(eps, Arrays.asList(modArray), genp.getAsociacionesBase());

      return AssociationFactory.getInstance().getAssociationInstance(associationClassName, modArray, eps, windowSize,
            patrones, numHilos);
   }

   // Poscondición: actualiza el valor de mapaPares
   // Meter en 'mapaPares' las asociaciones de 'semillas'?
   protected List<IAsociacionTemporal> generarCandidatasTam2(List<IAsociacionTemporal> anteriores,
            List<String> tipos, List<IAsociacionTemporal> semillas) throws FactoryInstantiationException{
      List<IAsociacionTemporal> candidatas = new ArrayList<IAsociacionTemporal>();
      final int tam=2, tSize = tipos.size(), aSize = anteriores.size();
      int i, j;
      IAsociacionEvento madre, padre;
      IAsociacionTemporal modelo;
      String[] modArray;

      //TODO esto non é reset mapas?
      mapa = new HashMap<String,List<IAsociacionTemporal>>(tSize);
      mapaGeneradas = new HashMap<String,List<IAsociacionTemporal>>(tSize);
      for(String tipo : tipos){
         mapa.put(tipo,new ArrayList<IAsociacionTemporal>());
         mapaGeneradas.put(tipo,new ArrayList<IAsociacionTemporal>());
      }

      for(i=0; i<aSize-1; i++){
         madre = (IAsociacionEvento)anteriores.get(i);
         for(j=i+1; j<aSize; j++){
            padre = (IAsociacionEvento)anteriores.get(j);
            modArray = new String[]{ madre.getTipoEvento(), padre.getTipoEvento() };
            if(yaIncluida(Arrays.asList(modArray), semillas)){
               //Se creó con las semillas
               continue;
            }
            modelo = crearModelo(modArray);
            notificarModeloGenerado(tam, 0, modelo, modArray, true, candidatas, candidatasGeneradas, mapa, mapaGeneradas);
         }
      }
      return candidatas;
   }

   @Override
   protected void notificarModeloGenerado(int tam, int pSize,
         IAsociacionTemporal modelo, String[] mod,
         List<IAsociacionTemporal> candidatas,
         Map<String, List<IAsociacionTemporal>> nuevoMapa) {
      notificarModeloGenerado(tam, pSize, modelo, mod, true, candidatas, candidatasGeneradas, mapa, nuevoMapa);
   }

   @Override
   protected IAsociacionTemporal crearModelo(String[] modArray)
         throws FactoryInstantiationException {
      // Buscar si algún episodio se aplica a la asociación temporal en curso
      List<Episodio> eps = new ArrayList<Episodio>();
      EpisodiosUtils.episodiosAsociacionUno(eps, listaEpisodios, Arrays.asList(modArray));
      return AssociationFactory.getInstance().getAssociationInstance("ModeloEpisodios", modArray, eps,
            windowSize, getClustering(), numHilos);
   }

   // Entrada: lista de modelos de esta iteración, soporte mínimo
   // Precondición:
   // Poscondición: se eliminan de candidatasGeneradas y de mapaGeneradas los mismos candidatos
   // Salida: lista de modelos con al menos cobertura mínima
   @Override
   protected void purgarCandidatas(List<IAsociacionTemporal> candidatas, int supmin, int tamActual){
      registroT.tiempoPurgar(tamActual-1, true);
      for(int i=candidatas.size()-1;i>=0;i--){
         IAsociacionTemporal modelo = candidatas.get(i);
         if(modelo.necesitaPurga(supmin)){
            candidatasGeneradas.remove(modelo);
            for(String tipo : modelo.getTipos()){ // Eliminar de la tabla hash
               mapaGeneradas.get(tipo).remove(modelo);
            }
         }else{
            patronesFrecuentesNivel[tamActual-1] += modelo.getPatrones().size();
         }
      }
      registroT.tiempoPurgar(tamActual-1, false);
   }

   // Entrada: lista de modelos de esta iteracion, secuencia/registro de entrada
   // Precondición: secuencia está ordenada temporalmente de forma creciente
   // Poscondición: candidatas está actualizada con todas las instancias encontradas
   // Salida: ninguna explícita, lista candidatas actualizada
   //NOTA: ESTO ES UNA COPIA DE MineCompleteEpisodes, NO SE HACE NADA DE SEMILLACONJUNCION (y realmente debería ser el mismo método).
   @Override
   protected void calcularSoporte(List<IAsociacionTemporal> candidatas, IColeccion coleccion){
      if(!candidatas.isEmpty()){
         calcularSoporte(candidatas, coleccion, false);
      }
   }

   protected void calcularSoporte(List<IAsociacionTemporal> candidatas, IColeccion coleccion, boolean esSemilla){
      if(esSemilla || !candidatasGeneradas.isEmpty()){
      // Si no hay candidatos, evitar hacer el cálculo de frecuencia
         int sid=0;
         for(ISecuencia secuencia : coleccion){
            for(Evento ev : secuencia){
               List<IAsociacionTemporal> receptores = mapa.get(ev.getTipo());
               for(IAsociacionTemporal receptor : receptores){
                  receptor.recibeEvento(sid,ev, savePatternInstances);
               }
            }
            sid++;
         }
      }
   }

   @Override
   public List<List<IAsociacionTemporal>> buscarModelosFrecuentes(List<String> tipos, IColeccion coleccion,
         List<ModeloSemilla> semillas, int supmin, int win) throws AlgoritmoException {
      return buscarModelosFrecuentes(tipos,coleccion,semillas,supmin,win,new ArrayList<Episodio>());
   }

   @Override
   public List<IAsociacionTemporal> calcularDistribuciones(List<String> tipos, IColeccion coleccion,
         List<ModeloSemilla> semillas, int supmin, int win, List<Episodio> episodios) throws AlgoritmoException{
      List<List<IAsociacionTemporal>> all = buscarModelosFrecuentes(tipos, coleccion,  semillas,  supmin,  win, episodios, false);
      if (all != null && all.size() > 1) {
         return all.get(1);
      }
      return Collections.emptyList();
   }

   @Override
   public List<List<IAsociacionTemporal>> buscarModelosFrecuentes(List<String> tipos, IColeccion coleccion,
         List<ModeloSemilla> semillas, int supmin, int win, List<Episodio> episodios) throws AlgoritmoException {
      return buscarModelosFrecuentes(tipos, coleccion,  semillas,  supmin,  win, episodios, false);
   }

   @Override
   protected void inicializaEstructuras(List<String> tipos, List<IAsociacionTemporal> candidatos,  int win, String[] tiposSemilla,
         List<ModeloSemilla> semillas, List<List<IAsociacionTemporal>> semNivel,
         int cSize) throws FactoryInstantiationException{
      //Usando el método heredado
      super.inicializaEstructuras(tipos, candidatos, win, tiposSemilla, semillas, semNivel, cSize);
      mapaGeneradas = new HashMap<String,List<IAsociacionTemporal>>(tipos.size());
      candidatasGeneradas = new ArrayList<IAsociacionTemporal>();
      for(String tipo : tipos){
         mapaGeneradas.put(tipo, new ArrayList<IAsociacionTemporal>());
      }
      for(IAsociacionTemporal mod: candidatos){
         //mapaGeneradas y candidatasGeneradas no tienen los modelos de las semillas
         if(mod.size() == 1){
            mapaGeneradas.get(mod.getTipos()[0]).add(mod);
            candidatasGeneradas.add(mod);
         }
      }
   }

   @Override
   protected IAsociacionTemporal crearModelo(String[] mod,
         List<Patron> patrones, int[] distribucion)
         throws FactoryInstantiationException {
   // Introducir información de episodios
    List<Episodio> eps = new ArrayList<Episodio>();
    EpisodiosUtils.episodiosAsociacionUno(eps, listaEpisodios, Arrays.asList(mod));
//    //EpisodiosUtils.episodiosAsociacionAmbos(eps, episodios, par);//No da el mismo resultado en asociaciones de tamaño 3
      return AssociationFactory.getInstance().getAssociationInstance(associationClassName,
          mod, eps, windowSize, patrones,  distribucion, getClustering(), numHilos);
   }

   @Override
   public void calculaPatrones(List<IAsociacionTemporal> candidatos, int supmin,
         int tam) throws FactoryInstantiationException{
      registroT.tiempoCalcula(tam-1, true);
      for(IAsociacionTemporal candidato : candidatos){
         patronesNoFrecuentesNivel[tam-1] += candidato.calculaPatrones(supmin, patternClassName, genID, savePatternInstances);
      }
      registroT.tiempoCalcula(tam-1, false);
   }

   // Precondición: Sólo puede haber 1 semilla
   private List<List<IAsociacionTemporal>> buscarModelosFrecuentes(List<String> tipos, IColeccion coleccion,
         List<ModeloSemilla> semillas, int supmin, int win, List<Episodio> episodios, boolean hastaNivel2) throws SemillasNoValidasException {
      listaEpisodios = episodios;
      if(semillas.size() != 1){
         throw new SemillasNoValidasException("Solo puede haber una semilla");
      }
      try{
         long tiempoAux, inicioTotal, inicioIteracion;
         List<IAsociacionTemporal> candidatas, anteriores, semFrecuentes;
         String[] tiposSemilla;
         List<List<IAsociacionTemporal>> todos = new ArrayList<List<IAsociacionTemporal>>();
         List<List<IAsociacionTemporal>> semNivel = new ArrayList<List<IAsociacionTemporal>>();

         inicioTotal = System.currentTimeMillis();
         inicioIteracion = inicioTotal;

         Runtime runtime = Runtime.getRuntime();

         candidatas = new ArrayList<IAsociacionTemporal>();
         tiposSemilla = semillas.get(0).getTipos();

         long inicioSemilla = System.currentTimeMillis();
         inicializaEstructuras(tipos, candidatas, win, tiposSemilla, semillas, semNivel, coleccion.size());

         // Calcular qué tipos de eventos y qué patrones semilla son frecuentes
         registroT.tiempoSoporte(0, true);
         calcularSoporteSemilla(coleccion); //Problema en modelosemillaepisodios
         registroT.tiempoSoporte(0, false);
         registrarUsoMemoria(runtime, 1);

         calculaPatrones(candidatas, supmin, 1);
         //memoriaSinPurgaNivel[0] = runtime.totalMemory() - runtime.freeMemory();

         purgarCandidatas(candidatas, supmin, 1);

         // Añadir los tipos de evento del patrón semilla, por compatibilidad
         for(String tipo : tiposSemilla){
            // TODO modelo evento?
            IAsociacionTemporal mod = AssociationFactory.getInstance().getAssociationInstance("ModeloEvento",
                    tipo, numHilos);
            //candidatas.add(mod);
            //mapa.get(tipo).add(mod);
            notificarModeloGenerado(1, 0, mod, Arrays.asList(tipo), candidatas, mapa);
         }

         // Separar los candidatos frecuentes en semillas frecuentes (semFrecuentes)
         // y tipos de evento frecuentes (anteriores)
         anteriores = new ArrayList<IAsociacionTemporal>();
         semFrecuentes = new ArrayList<IAsociacionTemporal>();
         List<IAsociacionTemporal> pares = new ArrayList<IAsociacionTemporal>();
         procesarSemillas(candidatas, pares, anteriores, semFrecuentes);

         todos.add(anteriores);
//         if(semFrecuentes.isEmpty()){
//            LOGGER.info("semFrecuentes vacío");
//            //return todos;
//            throw new PatronSemillaNoFrecuenteException();
//         }

         semNivel.get(1).addAll(pares);

         // Actualizar la lista de tipos para que solo incluya los frecuentes
         registroT.tiempoPurgar(0, true);
         tipos = purgarTiposYEventos(coleccion, anteriores, tipos);
         registroT.tiempoPurgar(0, false);

         //memoriaConPurgaNivel[0] = runtime.totalMemory() - runtime.freeMemory();
         imprimirUsoMemoria(LOGGER, 0);

         tiempoAux = System.currentTimeMillis();
         registroT.tiempoIteracion(0, tiempoAux - inicioIteracion);
         inicioIteracion = tiempoAux;

         int tam = 2;
         // Obtener pares de tipos de eventos frecuentes (no semilla)
         // y con qué disposiciones temporales son frecuentes (clustering)
         // Se crean las candidatas de tamaño 2 que no son semilla
         registroT.tiempoCandidatas(tam-1, true);
         candidatas = generarCandidatasTam2(anteriores, tipos, semNivel.get(1)); // no semilla
         registroT.tiempoCandidatas(tam-1, false);
         //memoriaSinPurgaNivel[tam-1] = runtime.totalMemory() - runtime.freeMemory();

         // Comprobar que los nuevos patrones generados para la semilla son frecuentes
         if(!candidatas.isEmpty()){
            registroT.tiempoSoporte(tam-1, true);
            calcularSoporte(candidatas, coleccion);
            registroT.tiempoSoporte(tam-1, false);
            calculaPatrones(candidatas, supmin, tam);
            purgarCandidatas(candidatas, supmin, tam);
         }

         //memoriaConPurgaNivel[tam-1] = runtime.totalMemory() - runtime.freeMemory();
         imprimirUsoMemoria(LOGGER, tam-1);

         // Caso general
         // Empieza el procedimiento iterativo. Cada iteración i representa un tamaño i
         // de candidatos. El procedimiento continua mientras queden semillas que introducir
         // O extensiones de semillas que generar.
         // Solo interesan los patrones derivados de la semilla
         todos.add(candidatas);
         int nivelSem = 1;
         if(semNivel.get(1).isEmpty()){
            //Se puede dar si los patrones semilla son de tamaño 1
            //throw new AlgoritmoException("Las semillas no han dado lugar a ningún patrón de tamaño 2 frecuente");
         }else{
            todos.get(1).addAll(semNivel.get(nivelSem));
            Collections.sort(candidatas); //sólo hay que ordenar si se mezclan
         }

         tiempoAux = System.currentTimeMillis();
         LOGGER.log(Level.INFO, "{0}. Tiempo para Inicialización: "
               + (tiempoAux-inicioSemilla), getExecutionId());
         registroT.tiempoIteracion(1, tiempoAux - inicioIteracion);
         inicioIteracion = tiempoAux;

         if(!hastaNivel2){

            candidatasGeneradas = candidatas;
            mapaGeneradas = mapaPares;

            tam = 3;
            while(!candidatasGeneradas.isEmpty()){
               anteriores = candidatasGeneradas;

               candidatas = buscarModelosIteracion(tipos, coleccion, supmin, win, hastaNivel2,
                     candidatasGeneradas, semNivel.get(nivelSem), runtime, tam);

               if(!candidatasGeneradas.isEmpty()){
                  todos.add(candidatasGeneradas);
               }

               tiempoAux = System.currentTimeMillis();
               registroT.tiempoIteracion(tam-1, tiempoAux - inicioIteracion);
               inicioIteracion = tiempoAux;
               //LOGGER.log(Level.INFO, "{0}. Tiempo para iteración " + tam + ": "
               //      + registroT.getTiemposIteracion()[tam-1], getExecutionId());

               nivelSem++;
               tam++;

               if(tamMaximoPatron != -1 && tam > tamMaximoPatron){
                  break;
               }
            }
         }

         long toFinal = System.currentTimeMillis();
         registroT.setTiempoTotal(toFinal - inicioTotal);

         imprimirNiveles(LOGGER, todos);
         imprimirTiempos(LOGGER);

         return todos;
      }catch(FactoryInstantiationException e){
         LOGGER.log(Level.SEVERE, "Problema instanciando en fábrica", e);
      }
      return Collections.emptyList();
   }

   /*
    * Se asume que los modelos tienen tamaño 2.
    */

   @Override
   protected void iniciarContadores(final int tSize, int cSize){
      super.iniciarContadores(tSize, cSize);
      patronesNoFrecuentesNivel = new long[tSize];
   }

   @Override
   protected void iniciarEstructurasReinicio(List<String> tipos, List<IAsociacionTemporal> modelosBase,
         int win, int cSize){
      //super.iniciarEstructurasReinicio(tipos, modelosBase, win, cSize); //en este caso no es necesario
      int tSize = tipos.size();
      iniciarContadores(tSize, cSize);
      windowSize = win;
      mapa = new HashMap<String, List<IAsociacionTemporal>>(tSize);
      mapaGeneradas = new HashMap<String, List<IAsociacionTemporal>>(tSize);
      candidatasGeneradas = new ArrayList<IAsociacionTemporal>(tSize);

      // Preparar 'mapaGeneradas'
      for(String tipo : tipos){
         mapaGeneradas.put(tipo, new ArrayList<IAsociacionTemporal>());
      }
      // Añadir cada asociación temporal a sus listas correspondientes.
      for(IAsociacionTemporal modelo : modelosBase){
         for(String tipo : modelo.getTipos()){
            if(mapaGeneradas.containsKey(tipo)){
               mapaGeneradas.get(tipo).add(modelo);
            }
         }
      }
   }

   @Override
   public List<List<IAsociacionTemporal>> reiniciarBusqueda(List<String> tipos, IColeccion coleccion,
         List<IAsociacionTemporal> modelosBase, int supmin, int win, List<Episodio> episodios) throws ModelosBaseNoValidosException{
      if(modelosBase==null || modelosBase.isEmpty()){
         throw new ModelosBaseVaciosException();
      }
      if(modelosBase.get(0).getTipos().length != 2){
         throw new ModelosBaseNoValidosException("Esta implementación de reiniciarBusqueda solo admite modelos base de tamaño 2");
      }

      try{
         iniciarEstructurasReinicio(tipos, modelosBase, win, coleccion.size());

         List<List<IAsociacionTemporal>> result = new ArrayList<List<IAsociacionTemporal>>();
         result.add(new ArrayList<IAsociacionTemporal>()); //para las asociaciones de tamaño 1 que no hay

         // No es igual al método general porque no diferencia los candidatos que son
         // semilla de los que no lo son
         // TODO igual deberían diferenciarse semillas y otros candidatos

         // Se asume que todos los modelosBase son frecuentes.
         // Esta parte es exactamente igual a la de SemillaConjuncionCompleteEpisodes
         long tiempo;
         Calendar diferencia = new GregorianCalendar();

         candidatasGeneradas = modelosBase;
         List<IAsociacionTemporal> candidatas;
         int tam = 3;

         while(!candidatasGeneradas.isEmpty()){
            result.add(candidatasGeneradas);

            registroT.tiempoCandidatas(tam-1, true);
            candidatas = new ArrayList<IAsociacionTemporal>();
            generarCandidatas(tam, candidatasGeneradas, tipos, candidatas);
            registroT.tiempoCandidatas(tam-1, false);

            registroT.tiempoSoporte(tam-1, true);
            calcularSoporte(candidatas, coleccion);
            tiempo = registroT.tiempoSoporte(tam-1, false);
            diferencia = new GregorianCalendar();
            diferencia.setTimeInMillis(tiempo);
            imprimirTiempo("Soporte", LOGGER, tam, diferencia);

            calculaPatrones(candidatas, supmin, tam);

            purgarCandidatas(candidatas, supmin, tam);

            tam++;
         }
         imprimirNiveles(LOGGER, result);
         imprimirTiempos(LOGGER);

         return result;
      }catch(FactoryInstantiationException e){
         LOGGER.log(Level.SEVERE, "Problema instanciando en fábrica", e);
      }
      return Collections.emptyList();
   }

   @Override
   public List<Episodio> getListaEpisodios(){
      return listaEpisodios;
   }

   @Override
   public void escribirEstadisticasEstrategia(List<List<IAsociacionTemporal>> resultados,
         Writer fwp, boolean shortVersion, int maxIteracion) throws IOException {
      super.escribirEstadisticasEstrategia(resultados, fwp, shortVersion, maxIteracion);

      imprimirParcialesYFrecuentes(resultados, fwp, shortVersion);
   }

}