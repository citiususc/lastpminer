package source.busqueda.episodios;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import source.busqueda.GeneradorPatrones;
import source.evento.Episodio;
import source.evento.IColeccion;
import source.excepciones.FactoryInstantiationException;
import source.excepciones.ModelosBaseNoValidosException;
import source.excepciones.ModelosBaseVaciosException;
import source.modelo.AssociationFactory;
import source.modelo.IAsociacionConEpisodios;
import source.modelo.IAsociacionTemporal;
import source.modelo.clustering.IClustering;
import source.patron.Patron;

/*
 *   Variante del algoritmo básico que permite trabajar con episodios. Estos episodios se definen identificando
 * un evento de inicio y otro de fin, de forma que una ocurrencia de un evento inicio únicamente se pueda asociar
 * con la siguiente ocurrencia del evento fin que no haya sido ya asociada con un evento inicio, y viceversa.
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
public class MineCompleteEpisodes extends MineEpisodes{
   private static final Logger LOGGER = Logger.getLogger(MineCompleteEpisodes.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   // Mapa 1: Contiene todos los candidatos generados según el procedimiento normal.
   // Mapa 2: Contiene aquellos candidatos cuya frecuencia debe comprobarse realmente (subconjunto del mapa anterior).

   /*
    * Atributos propios
    */

   // mapa(y candidatas) puede estar vacío, lo que supone que esa iteración se omite, pero mientras mapaGeneradas no esté vacío
   // (y por tanto candidatasGeneradas) el proceso continua
   //protected Map<String,List<IAsociacionTemporal>> mapa; // Contiene aquellos candidatos que se buscarán en el cálculo de frecuencia.
   protected Map<String,List<IAsociacionTemporal>> mapaGeneradas; // Contiene todos los candidatos que se generaron en la iteración.
   protected List<IAsociacionTemporal> candidatasGeneradas; // ¿Debe estar a este nivel?

   protected long[] patronesNoFrecuentesNivel;

   {
      //associationClassName = "ModeloEpisodios";
      //patternClassName = "Patron";
   }

   public MineCompleteEpisodes(String executionId, boolean savePatternInstances, boolean saveRemovedEvents,
         IClustering clustering, boolean removePatterns){
      super(executionId, savePatternInstances, saveRemovedEvents, clustering, removePatterns);
   }

   @Override
   protected IAsociacionTemporal crearModelo(String[] modArray)
         throws FactoryInstantiationException {
      // Buscar si algún episodio se aplica a la asociación temporal en curso
      List<Episodio> eps = new ArrayList<Episodio>();
      EpisodiosUtils.episodiosAsociacionUno(eps, listaEpisodios, Arrays.asList(modArray));
      return AssociationFactory.getInstance().getAssociationInstance(associationClassName,
            modArray, eps, windowSize, getClustering(), numHilos);
   }



   /*
    * Redefinición de métodos plantilla y métodos propios
    */

   /*
    * Se sobreescribe el método para añadir también a mapaGeneradas y a candidatasGeneradas.
    * (non-Javadoc)
    * @see source.busqueda.AbstractMine#notificarModeloGenerado(int, int, source.modelo.IAsociacionTemporal, java.lang.String[], java.util.List, java.util.Map)
    */
   @Override
   protected void notificarModeloGenerado(int tam, int pSize,
         IAsociacionTemporal modelo, String[] mod,
         List<IAsociacionTemporal> candidatas,
         Map<String, List<IAsociacionTemporal>> nuevoMapa) {
      notificarModeloGenerado(tam, pSize, modelo, mod, tam==2 || ((IAsociacionConEpisodios)modelo).sonEpisodiosCompletos(),
            candidatas, candidatasGeneradas, mapa, nuevoMapa);
   }

   @Override
   protected IAsociacionTemporal crearModelo(String[] modArray,
         List<Patron> patrones, GeneradorPatrones genp)
         throws FactoryInstantiationException {
      List<Episodio> eps = new ArrayList<Episodio>();
      EpisodiosUtils.episodiosAsociacion(eps, Arrays.asList(modArray), genp.getAsociacionesBase());
      return AssociationFactory.getInstance().getAssociationInstance(associationClassName, modArray, eps,
               windowSize, patrones, numHilos);
   }

   /**
    * Genera patrones candidatos como la combinación de los demás patrones
    * @param anteriores - lista de modelos frecuentes anteriores
    * @param tipos - tipos existentes en el registro
    * Precondicion:
    * Poscondición: los modelos devueltos no contienen tipos de eventos repetidos (futuro: permitirlo)
    * - no hay dos modelos que compartan todos los tipos de eventos
    * - los modelos devueltos contienen la información sobre episodios que les corresponde
    * @return lista de modelos para la actual iteración
    */
   @Override
   protected List<IAsociacionTemporal> generarCandidatas(final int tam, List<IAsociacionTemporal> anteriores,
         List<String> tipos) throws FactoryInstantiationException{

      //int tam = anteriores.get(0).size() + 1;
      List<IAsociacionTemporal> candidatas = new ArrayList<IAsociacionTemporal>();
      candidatasGeneradas = new ArrayList<IAsociacionTemporal>();

      if(tam==2){
         mapa = construyeMapa(tipos.size(), tipos);
         generarCandidatasTam2(anteriores, tipos, candidatas, mapaGeneradas);
         return candidatas;
      }
      // Inicializar mapas
      final int tSize = tipos.size();
      mapa.clear();
      Map<String,List<IAsociacionTemporal>> nuevoMapaGeneradas = new HashMap<String, List<IAsociacionTemporal>>(tSize);
      for(String tipo : tipos){
         mapa.put(tipo, new ArrayList<IAsociacionTemporal>());
         nuevoMapaGeneradas.put(tipo, new ArrayList<IAsociacionTemporal>());
      }
      mapaGeneradas = generarCandidatasGeneral(tam, anteriores, tipos,
            candidatas, mapaGeneradas, nuevoMapaGeneradas);
      return candidatas;
   }


   // Entrada: lista de modelos de esta iteracion, secuencia/registro de entrada
   // Precondición: secuencia está ordenada temporalmente de forma creciente
   // Poscondición: candidatas está actualizada con todas las instancias encontradas
   // Salida: ninguna explícita, lista candidatas actualizada
   @Override
   protected void calcularSoporte(List<IAsociacionTemporal> candidatas, IColeccion coleccion){
      //if(!candidatas.isEmpty() && candidatasGeneradas != null && !candidatasGeneradas.isEmpty()){
      if(!candidatas.isEmpty()){
         // Si no hay candidatos, evitar hacer el cálculo de frecuencia
         super.calcularSoporte(candidatasGeneradas, coleccion);
      }
   }

   // Entrada: lista de modelos de esta iteración, soporte mínimo
   // Precondición:
   // Poscondición: se eliminan de candidatasGeneradas y de mapaGeneradas los mismos candidatos
   // Salida: lista de modelos con al menos cobertura mínima
   @Override
   protected void purgarCandidatas(List<IAsociacionTemporal> candidatas, int supmin, int tamActual){
      registroT.tiempoPurgar(tamActual-1, true);
      for(int i=candidatas.size()-1; i>=0; i--){
         IAsociacionTemporal modelo = candidatas.get(i);
         //if(modelo.getSoporte() < supmin || modelo.size() > 2 && modelo.getPatrones().isEmpty()){
         if(modelo.necesitaPurga(supmin)){
            candidatasGeneradas.remove(modelo); // hay que hacerlo porque se usa para comprobar si
                                                // hay que pasar a la siguiente iteración y añadir al resultado
            // Hay que eliminar los infrecuentes de los mapas ya que se utilizarán
            for(String tipo : modelo.getTipos()){ // Eliminar de la tabla hash
               mapaGeneradas.get(tipo).remove(modelo);
            }
         }else{
            patronesFrecuentesNivel[tamActual-1] += modelo.getPatrones().size();
         }
      }
      registroT.tiempoPurgar(tamActual-1, false);
   }


   @Override
   public List<IAsociacionTemporal> calcularDistribuciones(List<String> tipos, IColeccion coleccion,
         int supmin, int win, List<Episodio> episodios){
      List<List<IAsociacionTemporal>> all = buscarModelosFrecuentes(tipos, coleccion, supmin, win, episodios, true);
      if (all != null && all.size() > 1) {
         return all.get(1);
      }
      return Collections.emptyList();
   }

   @Override
   public List<List<IAsociacionTemporal>> buscarModelosFrecuentes(List<String> tipos, IColeccion coleccion,
         int supmin, int win){
      return buscarModelosFrecuentes(tipos, coleccion, supmin, win, new ArrayList<Episodio>(), false);
   }

   @Override
   public List<List<IAsociacionTemporal>> buscarModelosFrecuentes(List<String> tipos, IColeccion coleccion,
         int supmin, int win, List<Episodio> episodios){
      return buscarModelosFrecuentes(tipos, coleccion, supmin, win, episodios, false);
   }

   @Override
   protected void inicializaEstructuras(List<String> tipos, List<IAsociacionTemporal> actual,
         int win, int cSize) throws FactoryInstantiationException{
      super.inicializaEstructuras(tipos, actual, win, cSize);
      candidatasGeneradas = new ArrayList<IAsociacionTemporal>(actual);
      mapaGeneradas = construyeMapa(tipos.size(), tipos);
   }

   private List<List<IAsociacionTemporal>> buscarModelosFrecuentes(List<String> tipos, IColeccion coleccion,
         int supmin, int win, List<Episodio> episodios, boolean hastaNivel2){
      listaEpisodios = episodios;
      try{
         int tSize = tipos.size();
         long tiempoAux, inicioIteracion, inicioTotal;
         List<IAsociacionTemporal> actual; //,anterior;
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

         //Purga infrecuentes
         purgarCandidatas(actual, supmin, 1);

         // Ya sabemos qué tipos de eventos son frecuentes, eliminar los que no lo son
         registroT.tiempoPurgar(0, true);
         tipos = purgarTiposYEventos(coleccion, actual, tipos, tSize);
         tSize = tipos.size();
         //memoriaConPurgaNivel[0] =  runtime.totalMemory() - runtime.freeMemory();
         imprimirUsoMemoria(LOGGER, 0);
         registroT.tiempoPurgar(0, false);

         tiempoAux = System.currentTimeMillis();
         registroT.tiempoIteracion(0, tiempoAux - inicioIteracion);
         inicioIteracion = tiempoAux;

         //Fin tamaño 1

         int tam = 2;
         while(!candidatasGeneradas.isEmpty()){

            if(removePatterns){
               todos.set(0, candidatasGeneradas);
            }else{
               todos.add(candidatasGeneradas); //todos.add(actual);
            }

            llamarGC();
            imprimirInstanciasPatrones(LOGGER, "post-purga", tam);

            if (tam > tSize || hastaNivel2 && tam > 2
                  || tamMaximoPatron!=-1 && tam>tamMaximoPatron){
               break;
            }

            /*actual = */buscarModelosIteracion(tipos, coleccion, supmin, win, hastaNivel2, candidatasGeneradas, runtime, tSize, tam);

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

   @Override
   public void calculaPatrones(List<IAsociacionTemporal> candidatos, int supmin, int tam)
         throws FactoryInstantiationException {
      registroT.tiempoCalcula(tam-1, true);
      for(IAsociacionTemporal modelo : candidatos){
         patronesNoFrecuentesNivel[tam-1] += modelo.calculaPatrones(supmin, patternClassName, genID, savePatternInstances);
      }
      registroT.tiempoCalcula(tam-1, false);
   }

   @Override
   protected void iniciarContadores(final int tSize, int cSize){
      super.iniciarContadores(tSize, cSize);
      patronesNoFrecuentesNivel = new long[tSize];
   }

   @Override
   protected void iniciarEstructurasReinicio(List<String> tipos, List<IAsociacionTemporal> modelosBase,
         int win, int cSize) throws FactoryInstantiationException{
      windowSize = win;
      int tSize = tipos.size();
      iniciarContadores(tSize, cSize);

      mapa = new HashMap<String,List<IAsociacionTemporal>>(tSize);
      mapaGeneradas = new HashMap<String,List<IAsociacionTemporal>>(tSize);
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


   /*
    * Se asume que los modelos tienen tamaño 2.
    */
   @Override
   public List<List<IAsociacionTemporal>> reiniciarBusqueda(List<String> tipos, IColeccion coleccion,
         List<IAsociacionTemporal> modelosBase, int supmin, int win,
         List<Episodio> episodios) throws ModelosBaseNoValidosException{
      if(modelosBase==null || modelosBase.isEmpty()){
         throw new ModelosBaseVaciosException();
      }
      if(modelosBase.get(0).getTipos().length != 2){
         throw new ModelosBaseNoValidosException("Esta implementación de reiniciarBusqueda solo admite modelos base de tamaño 2");
      }

      try{
         iniciarEstructurasReinicio(tipos, modelosBase, win, coleccion.size());

         // El resto del comportamiento debería ser el mismo
         Runtime runtime = Runtime.getRuntime();
         int tSize = tipos.size();

         // Guardar lista de episodios
         listaEpisodios = new ArrayList<Episodio>();
         listaEpisodios.addAll(episodios);

         //Iniciar lista de listas de resultados
         List<List<IAsociacionTemporal>> todos = new ArrayList<List<IAsociacionTemporal>>();
         todos.add(new ArrayList<IAsociacionTemporal>()); //para las asociaciones de tamaño 1 que no hay

         // Se asume que todos los modelosBase son frecuentes.
         int tam = 3;
         candidatasGeneradas = modelosBase;

         while(!candidatasGeneradas.isEmpty()){
            todos.add(candidatasGeneradas);

            buscarModelosIteracion(tipos, coleccion, supmin, win, false, candidatasGeneradas, runtime, tSize, tam);
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
   public List<List<IAsociacionTemporal>> reiniciarBusqueda(List<String> tipos, IColeccion coleccion,
         List<IAsociacionTemporal> modelosBase, int supmin, int win) throws ModelosBaseNoValidosException{
      return reiniciarBusqueda(tipos, coleccion, modelosBase, supmin, win, new ArrayList<Episodio>(0));
   }

   @Override
   public void escribirEstadisticasEstrategia(List<List<IAsociacionTemporal>> resultados,
         Writer fwp, boolean shortVersion, int maxIteracion) throws IOException {
      super.escribirEstadisticasEstrategia(resultados, fwp, shortVersion, maxIteracion);

      imprimirParcialesYFrecuentes(resultados, fwp, shortVersion);
   }
}
