package source.busqueda.semilla;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import source.evento.Episodio;
import source.evento.IColeccion;
import source.excepciones.AlgoritmoException;
import source.excepciones.FactoryInstantiationException;
import source.excepciones.ModelosBaseNoValidosException;
import source.excepciones.ModelosBaseVaciosException;
import source.excepciones.PatronSemillaNoFrecuenteException;
import source.excepciones.SemillasNoValidasException;
import source.modelo.AssociationFactory;
import source.modelo.IAsociacionTemporal;
import source.modelo.clustering.IClustering;
import source.modelo.semilla.ModeloSemilla;

public class ManualSearchingEpisodes extends SemillaConjuncionCompleteEpisodes {
   private static final Logger LOGGER = Logger.getLogger(ManualSearchingEpisodes.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   /*
    * Atributos
    */


//   {
//      associationClassName = "ModeloEpisodios";
//      patternClassName = "Patron";
//   }

   public ManualSearchingEpisodes(String executionId, boolean savePatternInstances,
         boolean saveRemovedEvents, IClustering clustering, boolean removePatterns){
      super(executionId,savePatternInstances,saveRemovedEvents, clustering, removePatterns);
   }

   /*
    * Métodos
    */

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

   // Precondición: Sólo puede haber 1 semilla
   private List<List<IAsociacionTemporal>> buscarModelosFrecuentes(List<String> tipos, IColeccion coleccion,
         List<ModeloSemilla> semillas, int supmin, int win, List<Episodio> episodios, boolean hastaNivel2) throws AlgoritmoException {
      if(semillas.size() != 1){
         throw new SemillasNoValidasException("Solo puede haber una semilla");
      }
      try{
         List<IAsociacionTemporal> candidatas, anteriores, semFrecuentes;
         String[] tiposSemilla;
         List<List<IAsociacionTemporal>> todos = new ArrayList<List<IAsociacionTemporal>>();
         List<List<IAsociacionTemporal>> semNivel = new ArrayList<List<IAsociacionTemporal>>();

         Runtime runtime = Runtime.getRuntime();
         long inicioTotal = System.currentTimeMillis();

         candidatas = new ArrayList<IAsociacionTemporal>();
         tiposSemilla =semillas.get(0).getTipos();

         long inicioSemilla = System.currentTimeMillis();
         inicializaEstructuras(tipos, candidatas, win, tiposSemilla, semillas, semNivel, coleccion.size());

         // Calcular qué tipos de eventos y qué patrones semilla son frecuentes
         registroT.tiempoSoporte(0, true);
         calcularSoporteSemilla(coleccion); //Problema en modelosemillaepisodios
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

         // Añadir los tipos de evento del patrón semilla, por compatibilidad
         for(String tipo : tiposSemilla){
            IAsociacionTemporal mod = AssociationFactory.getInstance().getAssociationInstance("ModeloEvento",
                    tipo, numHilos);
            candidatas.add(mod);
            mapa.get(tipo).add(mod);
         }

         // Separar los candidatos frecuentes en semillas frecuentes (semFrecuentes)
         // y tipos de evento frecuentes (anteriores)
         anteriores = new ArrayList<IAsociacionTemporal>();
         semFrecuentes = new ArrayList<IAsociacionTemporal>();
         List<IAsociacionTemporal> pares = new ArrayList<IAsociacionTemporal>();
         procesarSemillas(candidatas, pares, anteriores, semFrecuentes);

         if(semFrecuentes.isEmpty()){
            LOGGER.info("semFrecuentes vacío");
            //return todos;
            throw new PatronSemillaNoFrecuenteException();
         }

         mapa = mapaPares; // ¿Aquí y así? En 'generarCandidatasTam2' se terminaría de actualizar
         semNivel.get(1).addAll(pares);

         //Se añaden al resultado, primero las semillas frecuentes y después los tipos de eventos frecuentes
         //todos.add(semFrecuentes); //
         todos.add(listaSemillas);
         todos.add(anteriores);

         // Actualizar la lista de tipos para que solo incluya los frecuentes
         tipos = purgarTiposYEventos(coleccion, anteriores, tipos);

         //memoriaConPurgaNivel[0] = runtime.totalMemory() - runtime.freeMemory();
         imprimirUsoMemoria(LOGGER, 0);

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

         if(semNivel.get(1).isEmpty()){
            //todos.add(new ArrayList<IAsociacionTemporal>()); // Esto NUNCA se debería dar
            throw new AlgoritmoException("Las semillas no han dado lugar a ningún patrón de tamaño 2 frecuente");
         }else{

            todos.add(semNivel.get(1));
         }

         // Añadir los patrones semilla a los resultados
         todos.get(todos.size()-1).addAll(candidatas);
         // En este punto todos es una mezcla de modelos de tamaño 1 y tamaño 2
         candidatas = todos.get(todos.size()-1);

         long finSemilla = System.currentTimeMillis();
         LOGGER.log(Level.INFO, "{0}. Tiempo para Inicialización: " + (finSemilla-inicioSemilla), getExecutionId());


         // Localizar semillas
         candidatasGeneradas = candidatas;
         mapaGeneradas = mapaPares;

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
   public List<List<IAsociacionTemporal>> reiniciarBusqueda(List<String> tipos, IColeccion coleccion,
         List<IAsociacionTemporal> modelosBase, int supmin, int win, List<Episodio> episodios) throws ModelosBaseNoValidosException{
      if(modelosBase==null || modelosBase.isEmpty()){
         throw new ModelosBaseVaciosException();
      }
      if(modelosBase.get(0).getTipos().length != 2){
         throw new ModelosBaseNoValidosException("Esta implementación de reiniciarBusqueda solo admite modelos base de tamaño 2");
      }
      LOGGER.severe("Método no implementado");
      return Collections.emptyList();
   }

}