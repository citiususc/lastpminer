package source.busqueda.semilla;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import source.evento.IColeccion;
import source.excepciones.AlgoritmoException;
import source.excepciones.FactoryInstantiationException;
import source.excepciones.ModelosBaseNoValidosException;
import source.excepciones.PatronSemillaNoFrecuenteException;
import source.excepciones.SemillasNoValidasException;
import source.modelo.AssociationFactory;
import source.modelo.IAsociacionTemporal;
import source.modelo.clustering.IClustering;
import source.modelo.semilla.ModeloSemilla;

public class ManualSearching extends SemillaConjuncion {
   private static final Logger LOGGER = Logger.getLogger(ManualSearching.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   {
      //associationClassName = "ModeloMarcasIntervalos";
      //patternClassName = "Patron";
   }

   /*
    * Constructors
    */

   public ManualSearching(String executionId, boolean savePatternInstances,
         boolean saveRemovedEvents, IClustering clustering, boolean removePatterns){
      super(executionId, savePatternInstances, saveRemovedEvents, clustering, removePatterns);
   }

   /*
    * Métodos
    */

   /** Precondición: Debe ser llamado ANTES de generarCandidatas, crea <nuevoMapa>
    *  si no se hiciese, 'mapa' acabaría incompleto
    *  Salida: cada extensión de una semilla es el resultado de añadirle un tipo de
    *  evento nuevo a la semilla.
    * @throws FactoryInstantiationException
    */
   protected List<IAsociacionTemporal> extenderSemillas(List<IAsociacionTemporal> semillas,
            List<String> tipos) throws FactoryInstantiationException{
      return Collections.emptyList();
   }

   @Override
   protected void generarCandidatas(final int tam, List<IAsociacionTemporal> anteriores,
         List<String> tipos, List<IAsociacionTemporal> actual) throws FactoryInstantiationException{
      //Nada
   }

   @Override
   public List<IAsociacionTemporal> calcularDistribuciones(List<String> tipos, IColeccion coleccion,
         List<ModeloSemilla> semillas, int supmin, int win) throws SemillasNoValidasException, AlgoritmoException{
      List<List<IAsociacionTemporal>> all =  buscarModelosFrecuentes(tipos, coleccion, semillas, supmin, win, true);
      if (all != null && all.size() > 1) {
         return all.get(1);
      }
      return Collections.emptyList();
   }

   // Precondición: Sólo puede haber 1 semilla
   @Override
   public List<List<IAsociacionTemporal>> buscarModelosFrecuentes(List<String> tipos, IColeccion coleccion,
         List<ModeloSemilla> semillas, int supmin, int win) throws SemillasNoValidasException, AlgoritmoException{
      return buscarModelosFrecuentes(tipos, coleccion, semillas, supmin, win, false);
   }



   // Precondición: Sólo puede haber 1 semilla
   private List<List<IAsociacionTemporal>> buscarModelosFrecuentes(List<String> tipos, IColeccion coleccion,
         List<ModeloSemilla> semillas, int supmin, int win, boolean hastaNivel2) throws SemillasNoValidasException, AlgoritmoException{
      if(semillas.size()!=1){
         throw new SemillasNoValidasException("Sólo está preparado para tener un patrón semilla");
      }
      try{
         List<IAsociacionTemporal> candidatas, anteriores, semFrecuentes;
         String[] tiposSemilla;
         //Lista de listas de patrones resultado
         List<List<IAsociacionTemporal>> todos = new ArrayList<List<IAsociacionTemporal>>();
         //
         List<List<IAsociacionTemporal>> semNivel = new ArrayList<List<IAsociacionTemporal>>();

         Runtime runtime = Runtime.getRuntime();
         long toInicial = System.currentTimeMillis();

         candidatas = new ArrayList<IAsociacionTemporal>();
         tiposSemilla = semillas.get(0).getTipos();

         long inicioSemilla = System.currentTimeMillis();
         inicializaEstructuras(tipos, candidatas, win, tiposSemilla, semillas, semNivel, coleccion.size());

         // Calcular qué tipos de eventos y qué patrones semilla son frecuentes
         registroT.tiempoSoporte(0, true);
         calcularSoporteSemilla(coleccion);
         registroT.tiempoSoporte(0, false);
         registrarUsoMemoria(runtime, 1);

         // Se va a reemplazar el patrón semilla por los patrones de tamaño 2 derivados del clustering
         // los tipos del patrón semilla
         // Clonamos los patrones semilla y los guardamos en todos antes de ello
         List<IAsociacionTemporal> listaSemillas = new ArrayList<IAsociacionTemporal>(semillas.size());
         for(ModeloSemilla semilla : semillas){
            listaSemillas.add(semilla.clonar());
         }

         calculaPatrones(candidatas, supmin, 1);
         //memoriaSinPurgaNivel[0] = runtime.totalMemory() - runtime.freeMemory();
         purgarCandidatas(candidatas, supmin, 1);

         // Separar los candidatos frecuentes en semillas frecuentes (semFrecuentes)
         // y tipos de evento frecuentes (anteriores)
         // Las semillas frecuentes dan lugares a patrones de tamaño 2
         // de los que se hace clustering y se guardan en mapaPares
         anteriores = new ArrayList<IAsociacionTemporal>();
         semFrecuentes = new ArrayList<IAsociacionTemporal>();
         List<IAsociacionTemporal> pares = new ArrayList<IAsociacionTemporal>();
         procesarSemillas(candidatas, pares, anteriores, semFrecuentes);

         // Si el patrón semilla no es frecuente se lanza una excepción
         if(semFrecuentes.isEmpty()){
            LOGGER.info("semFrecuentes vacío");
            //return todos;
            throw new PatronSemillaNoFrecuenteException();
         }

         // Añadir los tipos de evento del patrón semilla, por compatibilidad
         for(String tipo : tiposSemilla){
            IAsociacionTemporal mod = AssociationFactory.getInstance().getAssociationInstance("ModeloEvento",
                  tipo, numHilos);
            candidatas.add(mod);
            mapa.get(tipo).add(mod);
            anteriores.add(mod);
         }
         mapa = mapaPares; // En 'generarCandidatasTam2' se terminará de actualizar
         semNivel.get(1).addAll(pares);

         //Se añaden al resultado, primero las semillas frecuentes y después los tipos de eventos frecuentes
         //todos.add(semFrecuentes); //
         todos.add(listaSemillas);
         todos.add(anteriores);

         // Actualizar la lista de tipos para que solo incluya los frecuentes
         tipos = purgarTiposYEventos(coleccion, anteriores, tipos);

         //memoriaConPurgaNivel[0] = runtime.totalMemory() - runtime.freeMemory();
         imprimirUsoMemoria(LOGGER, 0);

         // Obtener pares de tipos de eventos frecuentes (no semilla)
         // y con qué disposiciones temporales son frecuentes (clustering)
         registroT.tiempoCandidatas(1, true);
         candidatas = generarCandidatasTam2(anteriores, tipos, semNivel.get(1)); // no semilla
         registroT.tiempoCandidatas(1, false);

         //memoriaSinPurgaNivel[1] = runtime.totalMemory() - runtime.freeMemory();

         // Comprobar que los nuevos patrones generados para la semilla son frecuentes
         if(!candidatas.isEmpty()){

            registroT.tiempoSoporte(0, true);
            calcularSoporte(candidatas, coleccion);
            registroT.tiempoSoporte(0, false);

            //Calculo patrones
            calculaPatrones(candidatas, supmin, 2);
            //Purga infrecuentes
            purgarCandidatas(candidatas, supmin, 2);
         }

         //memoriaConPurgaNivel[1] = runtime.totalMemory() - runtime.freeMemory();
         imprimirUsoMemoria(LOGGER, 1);

         // Caso general
         // Empieza el procedimiento iterativo. Cada iteración i representa un tamaño i
         // de candidatos. El procedimiento continua mientras queden semillas que introducir
         // O extensiones de semillas que generar.
         // Solo interesan los patrones derivados de la semilla
         if(semNivel.get(1).isEmpty()){
            // Esto NUNCA se debería dar
            //todos.add(new ArrayList<IAsociacionTemporal>());
            throw new AlgoritmoException("Las semillas no han dado lugar a ningún patrón de tamaño 2 frecuente");
         }else{
            todos.add(semNivel.get(1));
         }

         // Añadir los patrones semilla a los resultados
         // Que estaba ya en todos.last ?
         todos.get(todos.size()-1).addAll(candidatas);
         candidatas = todos.get(todos.size()-1);

         long finSemilla = System.currentTimeMillis();
         LOGGER.log(Level.INFO, "{0}. Tiempo para Inicialización: " + (finSemilla-inicioSemilla), getExecutionId());

         long toFinal = System.currentTimeMillis();
         registroT.setTiempoTotal(toFinal - toInicial);

         imprimirNiveles(LOGGER, todos);
         imprimirTiempos(LOGGER);

         return todos;
      }catch(FactoryInstantiationException e){
         LOGGER.log(Level.SEVERE, "Problema instanciando en fábrica", e);
      }
      return Collections.emptyList();
   }

   @Override
   protected List<IAsociacionTemporal> buscarModelosIteracion(List<String> tipos, IColeccion coleccion,
         int supmin, int win, boolean hastaNivel2, List<IAsociacionTemporal> anterior,
         List<IAsociacionTemporal> semillasNivel, Runtime runtime, int tam) throws FactoryInstantiationException{
      return Collections.emptyList();
   }

   @Override
   protected void iniciarEstructurasReinicio(List<String> tipos, List<IAsociacionTemporal> modelosBase,
         int win, int cSize) throws FactoryInstantiationException{
      //Nada
   }

   @Override
   public List<List<IAsociacionTemporal>> reiniciarBusqueda(List<String> tipos,
         IColeccion coleccion, List<IAsociacionTemporal> modelosBase,
         int supmin, int win) throws ModelosBaseNoValidosException{
      throw new RuntimeException("Método no válido para esta clase");
   }



}