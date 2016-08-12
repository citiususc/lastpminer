package source;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import source.busqueda.AbstractMine;
import source.busqueda.IBusqueda;
import source.busqueda.IBusquedaAnotaciones;
import source.busqueda.IBusquedaConEpisodios;
import source.busqueda.IBusquedaConSemilla;
import source.busqueda.IBusquedaConSemillayEpisodios;
import source.busqueda.IBusquedaArbol;
import source.configuracion.ConfigSintetica;
import source.configuracion.ConfigurationParameters;
import source.configuracion.HelperConfiguration;
import source.configuracion.ExecutionParameters;
import source.configuracion.Modes;
import source.evento.DescripcionColeccion;
import source.evento.Episodio;
import source.evento.EventoEliminado;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.evento.InstanciaEpisodio;
import source.excepciones.AlgoritmoException;
import source.excepciones.FactoryInstantiationException;
import source.excepciones.SemillasNoValidasException;
import source.io.ApneaReader;
import source.io.GenBBDDEpisodios;
import source.io.GenericEpisodesCSVReader;
import source.io.MalformedFileException;
import source.io.ResultWriter;
import source.io.report.TXTOneTimeReport;
import source.io.report.TXTReport;
import source.modelo.IAsociacionTemporal;
import source.modelo.clustering.ClusteringFactory;
import source.modelo.clustering.IClustering;
import source.modelo.semilla.ModeloSemilla;
import source.modelo.semilla.ModeloSemillaEpisodios;
import source.patron.Patron;
import source.patron.PatronSemilla;
import source.restriccion.RIntervalo;

public class CapsulaEjecucion {
   private static final Logger LOGGER = Logger.getLogger(CapsulaEjecucion.class.getName());
   public static final boolean SEMILLA_ORIXINAL = true;

   public IColeccion coleccion;
   public ConfigurationParameters params;
   public List<String> tipos;
   public List<Episodio> episodios;
   public List<InstanciaEpisodio> ocurrenciasEpisodios;
   public List<List<EventoEliminado>> eventosEliminados;
   public List<List<List<List<Patron>>>> anotaciones;
   public List<List<IAsociacionTemporal>> resultados;
   //public List<IAsociacionTemporal> distribuciones;
   public AbstractMine mine;
   public String fileEstadisticas;
   public String filePatrones;
   public ConfigSintetica configSintetica;
   //public IClustering clustering;

   protected CapsulaEjecucion(){
      this.tipos = new ArrayList<String>();
      this.episodios = new ArrayList<Episodio>();
      this.ocurrenciasEpisodios = new ArrayList<InstanciaEpisodio>();
      //this.eventosEliminados = new ArrayList<List<EventoEliminado>>();
      this.anotaciones = new ArrayList<List<List<List<Patron>>>>();
      //this.clustering = ClusteringFactory.getClustering(params.getClusteringClassName());
      //HelperConfiguration.setConfiguration(params, clustering);
   }

   public CapsulaEjecucion(ConfigurationParameters params, ConfigSintetica cs){
      this();
      this.params = params;
      this.configSintetica = cs;
      coleccion = getCollection(true);
   }

   public IClustering getClustering(){
      IClustering clustering = ClusteringFactory.getClustering(params.getClusteringClassName());
      HelperConfiguration.setConfiguration(params, clustering);
      return clustering;
   }

   public void mineria(int iteracion) throws SemillasNoValidasException {
      IClustering clustering = getClustering();
      mine = null;
      switch(params.getMode()){
         case MODE_BASIC:
            LOGGER.info("Minería. Sin episodios ni patron semilla");
            resultados = basico(clustering, iteracion, null);
            break;
         case MODE_EPISODE:
            LOGGER.info("Minería. Con episodios, sin patron semilla");
            resultados = episodios(clustering, iteracion, null);
            break;
         case MODE_SEED:
            LOGGER.info("Minería. Sin episodios, CON patron semilla");
            resultados = semilla(clustering, iteracion, null);
            break;
         case MODE_FULL:
            LOGGER.info("Minería. Con episodios y con patron semilla");
            resultados = semillaYEpisodios(clustering, iteracion, null);
            break;
         default:
            throw new RuntimeException("Opción no válida");
      }
   }

   public void reiniciarMineria(int iteracion, List<IAsociacionTemporal> modelosBase) throws SemillasNoValidasException {
      IClustering clustering = getClustering();
      mine = null;
      switch(params.getMode()){
         case MODE_BASIC:
            LOGGER.info("Minería. Sin episodios ni patron semilla");
            resultados = basico(clustering, iteracion, modelosBase);
            break;
         case MODE_EPISODE:
            LOGGER.info("Minería. Con episodios, sin patron semilla");
            resultados = episodios(clustering, iteracion, modelosBase);
            break;
         case MODE_SEED:
            LOGGER.info("Minería. Sin episodios, CON patron semilla");
            resultados = semilla(clustering, iteracion, modelosBase);
            break;
         case MODE_FULL:
            LOGGER.info("Minería. Con episodios y con patron semilla");
            resultados = semillaYEpisodios(clustering, iteracion, modelosBase);
            break;
         default:
            throw new RuntimeException("Opción no válida");
      }
   }

   public void escribirEstadisticas(int iteracion){
      fileEstadisticas = ResultWriter.escribirEstadisticas(params, resultados, mine, false, iteracion);
      if(resultados.size()>1 && !resultados.get(1).isEmpty()){
         ResultWriter.escribirScriptHistogramas(params, resultados.get(1));
      }
   }

   /**
    *
    * @param iteracion - valor entre 0 y {@link ConfigurationParameters}.iterations-1
    * @throws SemillasNoValidasException
    */
   public Boolean mineriaFicheros() throws SemillasNoValidasException {
      return mineriaFicheros(0);
   }
   public Boolean mineriaFicheros(int iteracion) throws SemillasNoValidasException {
      resetResultadosCapsula();
      Patron.setPrintID(false);
      mineria(iteracion);
      if (mine != null && !resultados.isEmpty()) {
         fileEstadisticas = ResultWriter.escribirEstadisticas(params, resultados, mine, false, iteracion);
         writeReports(iteracion);
         if(iteracion == 0){
            filePatrones = ResultWriter.escribirPatrones(params, resultados);
            if(params.isValidate()){
               String ficheroVal = getFicheroValidacion(params);
               if(ficheroVal != null && new File(ficheroVal).exists()){
                  LOGGER.info("VALIDACIÓN: se procede a validar los resultados");
                  boolean valido = params.isCompleteResult() ? Principal.compararFicheros(ficheroVal, filePatrones) : Principal.compararFicherosFinal(ficheroVal, filePatrones);
                  LOGGER.info(valido? "Los resultados son VÁLIDOS! Yippie ka yei!" : "Los resultados NO son válidos!! C'est la cata!");
                  if(!valido){
                     //Si no es un resulado correcto se renombra el fichero
                     String ficheroPatronesIncorrecto = filePatrones.substring(0, filePatrones.length()-4) + "--incorrecto.txt";
                     new File(filePatrones).renameTo(new File(ficheroPatronesIncorrecto));
                     LOGGER.info("VALIDACION: " + ficheroPatronesIncorrecto);
                     // Salir con system exit -1;
                     Runtime.getRuntime().halt(-1);
                  }else{
                     String ficheroPatronesCorrecto = filePatrones.substring(0, filePatrones.length()-4) + "--validado.txt";
                     new File(filePatrones).renameTo(new File(ficheroPatronesCorrecto));
                  }
                  return valido;
               }else{
                  LOGGER.info("VALIDACIÓN: no se pueden validar los resultados porque no hay una referencia disponible.");
                  LOGGER.fine("ficheroVal " + (ficheroVal == null ? "es nulo" : "no existe"));
               }
            }
         }

      }
      return null;
   }

   public List<IAsociacionTemporal> distribuciones(boolean writeToFile) throws SemillasNoValidasException {
      resetResultadosCapsula();
      IClustering clustering = getClustering();
      List<IAsociacionTemporal> distribuciones;
      switch(params.getMode()){
         case MODE_BASIC:
            LOGGER.info("Distribuciones. Sin episodios ni patron semilla");
            distribuciones = distribucionesBasica(clustering);
            break;
         case MODE_EPISODE:
            LOGGER.info("Distribuciones. Con episodios, sin patron semilla");
            distribuciones = distribucionesConEpisodios(clustering);
            break;
         case MODE_SEED:
            LOGGER.info("Distribuciones. Sin episodios, CON patron semilla");
            distribuciones = distribucionesConSemilla(clustering);
            break;
         case MODE_FULL:
            LOGGER.info("Distribuciones. Con episodios y con patron semilla");
            distribuciones = distribucionesConSemillaYEpisodios(clustering);
            break;
         default:
            throw new RuntimeException("Subopcion no valida");
      }
      resultados = new ArrayList<List<IAsociacionTemporal>>();
      resultados.add(new ArrayList<IAsociacionTemporal>());
      resultados.add(distribuciones);
      if(writeToFile){ ResultWriter.escribirScriptHistogramas(params, distribuciones); }
      return distribuciones;
   }

   public List<IAsociacionTemporal> distribucionesBasica(IClustering clustering) {
      try{
         IBusqueda a = MineFactory.getBasicInstance(params, clustering);
         return a.calcularDistribuciones(tipos, coleccion, params.getMinFreq(), params.getWindowSize());
      }catch(FactoryInstantiationException e){
         LOGGER.log(Level.SEVERE, "Fallo al calcular distribuciones sin episodios ni semilla.", e);
      }catch(AlgoritmoException e){
         LOGGER.log(Level.SEVERE, "Exception genérica del algoritmo.", e);
      }
      return Collections.emptyList();
   }

   public List<IAsociacionTemporal> distribucionesConEpisodios(IClustering clustering) {
      try{
         IBusquedaConEpisodios a = MineFactory.getEpisodeInstance(params, clustering);
         return a.calcularDistribuciones(tipos, coleccion, params.getMinFreq(), params.getWindowSize(), episodios);
      }catch(FactoryInstantiationException e){
         LOGGER.log(Level.SEVERE, "Fallo al calcular distribuciones con semilla.", e);
      }catch(AlgoritmoException e){
         LOGGER.log(Level.SEVERE, "Exception genérica del algoritmo.", e);
      }
      return Collections.emptyList();
   }

   public List<IAsociacionTemporal> distribucionesConSemilla(IClustering clustering) throws SemillasNoValidasException {
      return distribucionesConSemilla(clustering, Arrays.asList(getModeloSemilla(clustering, false)));
   }

   public List<IAsociacionTemporal> distribucionesConSemilla(IClustering clustering, List<ModeloSemilla> semillas) throws SemillasNoValidasException {
      try{
         IBusquedaConSemilla a = MineFactory.getSeedInstance(params, clustering);
         return a.calcularDistribuciones(tipos, coleccion, semillas, params.getMinFreq(), params.getWindowSize());
      }catch(FactoryInstantiationException e){
         LOGGER.log(Level.SEVERE, "Fallo al instanciar para calcular distribuciones con semilla", e);
      }catch(AlgoritmoException e){
         LOGGER.log(Level.SEVERE, "Exception genérica del algoritmo.", e);
      }
      return Collections.emptyList();
   }

   public List<IAsociacionTemporal> distribucionesConSemillaYEpisodios(IClustering clustering) throws SemillasNoValidasException {
      return distribucionesConSemillaYEpisodios(clustering, Arrays.asList(getModeloSemilla(clustering, true)));
   }

   public List<IAsociacionTemporal> distribucionesConSemillaYEpisodios(IClustering clustering, List<ModeloSemilla> semillas) throws SemillasNoValidasException {
      try{
         IBusquedaConSemillayEpisodios a = MineFactory.getSeedAndEpisodesInstance(params, clustering);
         mine = (AbstractMine)a;
         return a.calcularDistribuciones(tipos, coleccion, semillas, params.getMinFreq(), params.getWindowSize(), episodios);
      }catch(FactoryInstantiationException e){
         LOGGER.log(Level.SEVERE, "Fallo al instanciar para calcular distribuciones con semilla y episodios", e);
      }catch(AlgoritmoException e){
         LOGGER.log(Level.SEVERE, "Exception genérica del algoritmo.", e);
      }
      return Collections.emptyList();
   }

   /**
    * Este procedimiento se encarga de escribir los informes correspondientes.
    * @param iteracion - valor entre 0 y params.iterations-1
    */
   private void writeReports(int iteracion){
      if(params.isWriteTxtReport()){
         new TXTReport(ExecutionParameters.REPORTS_PATH, params).escribirTiempo(iteracion==0, mine.getTiempoTotal());
         new TXTOneTimeReport(ExecutionParameters.REPORTS_PATH, params).escribirTiempo(iteracion, mine.getTiempoTotal());
      }

      //Los informes a seguir solo funcionan con 5 iteraciones, en caso, contrario, salir
      if(params.getIterations() != Principal.ITERACIONES_PRUEBAS){
         LOGGER.info("No se escriben los informes porque el número de iteraciones no es " + Principal.ITERACIONES_PRUEBAS);
         return;
      }
   }

   public List<List<IAsociacionTemporal>> basico(IClustering clustering, int iteracion, List<IAsociacionTemporal> modelosBase) {
      try{
         // Algoritmo básico
         IBusqueda a = MineFactory.getBasicInstance(params, clustering);
         mine = (AbstractMine)a;
         List<List<IAsociacionTemporal>> resultados;
         if(modelosBase == null){
            resultados = a.buscarModelosFrecuentes(tipos, coleccion, params.getMinFreq(), params.getWindowSize());
         }else{
            resultados = a.reiniciarBusqueda(tipos, coleccion, modelosBase, params.getMinFreq(), params.getWindowSize());
         }
         eventosEliminados = ((AbstractMine)a).getEventosEliminados();
         if(anotaciones != null && a instanceof IBusquedaAnotaciones){
            anotaciones.addAll(((IBusquedaAnotaciones)a).getAnotaciones().getTodasAnotaciones());
         }
         return resultados;
      }catch(FactoryInstantiationException e){
         LOGGER.log(Level.SEVERE, "Fallo al instanciar minería sin semilla ni episodios.", e);
      }catch(AlgoritmoException e){
         LOGGER.log(Level.SEVERE, "Exception genérica del algoritmo.", e);
      }
      return Collections.emptyList();
   }

   public List<List<IAsociacionTemporal>> episodios(IClustering clustering, int iteracion, List<IAsociacionTemporal> modelosBase) {
      try{
         IBusquedaConEpisodios a = MineFactory.getEpisodeInstance(params, clustering);
         mine = (AbstractMine)a;
         List<List<IAsociacionTemporal>> resultados;
         if(modelosBase == null){
            resultados = ((IBusquedaConEpisodios) a).buscarModelosFrecuentes(tipos, coleccion, params.getMinFreq(), params.getWindowSize(), episodios);
         }else{
            resultados = ((IBusquedaConEpisodios) a).reiniciarBusqueda(tipos, coleccion, modelosBase, params.getMinFreq(), params.getWindowSize(), episodios);
         }
         eventosEliminados = ((AbstractMine)a).getEventosEliminados();
         if(anotaciones != null && a instanceof IBusquedaAnotaciones){
            anotaciones.addAll(((IBusquedaAnotaciones)a).getAnotaciones().getTodasAnotaciones());
         }

         return resultados;
      }catch(FactoryInstantiationException e){
         LOGGER.log(Level.SEVERE, "Fallo al instanciar minería con episodios.", e);
      }catch(AlgoritmoException e){
         LOGGER.log(Level.SEVERE, "Exception genérica del algoritmo.", e);
      }
      return Collections.emptyList();
   }

   public List<List<IAsociacionTemporal>> semilla(IClustering clustering, int iteracion,
         List<IAsociacionTemporal> modelosBase) throws SemillasNoValidasException {
      return semilla(clustering, Arrays.asList(getModeloSemilla(clustering, false)), iteracion, modelosBase);
   }

   public List<List<IAsociacionTemporal>> semilla(IClustering clustering, List<ModeloSemilla> semillas, int iteracion,
         List<IAsociacionTemporal> modelosBase) throws SemillasNoValidasException {
      try{
         IBusquedaConSemilla a = MineFactory.getSeedInstance(params, clustering);
         mine = (AbstractMine)a;

         List<List<IAsociacionTemporal>> resultados;
         if(modelosBase == null){
            resultados = a.buscarModelosFrecuentes(tipos, coleccion, semillas, params.getMinFreq(), params.getWindowSize());//*/
         }else{
            resultados = a.reiniciarBusqueda(tipos, coleccion, modelosBase, params.getMinFreq(), params.getWindowSize());//*/
         }
         eventosEliminados = ((AbstractMine)a).getEventosEliminados();
         if(anotaciones != null && a instanceof IBusquedaArbol){
            anotaciones.addAll(((IBusquedaAnotaciones)a).getAnotaciones().getTodasAnotaciones());
         }
         return resultados;
      }catch(FactoryInstantiationException e){
         LOGGER.log(Level.SEVERE, "Fallo al instanciar minería con semilla.", e);
      }catch(AlgoritmoException e){
         LOGGER.log(Level.SEVERE, "Exception genérica del algoritmo.", e);
      }
      return Collections.emptyList();
   }

   public List<List<IAsociacionTemporal>> semillaYEpisodios(IClustering clustering, int iteracion,
         List<IAsociacionTemporal> modelosBase) throws SemillasNoValidasException {
   // Patrón de apnea y episodios (SemillasConjuncionCompleteEpisodes)
      return semillaYEpisodios(clustering, Arrays.asList(getModeloSemilla(clustering, true)), iteracion, modelosBase);
   }

   public List<List<IAsociacionTemporal>> semillaYEpisodios(IClustering clustering, List<ModeloSemilla> semillas,
         int iteracion, List<IAsociacionTemporal> modelosBase) throws SemillasNoValidasException {
      try{
         IBusquedaConSemillayEpisodios a = MineFactory.getSeedAndEpisodesInstance(params, clustering);
         mine = (AbstractMine)a;

         List<List<IAsociacionTemporal>> resultados;
         if(modelosBase == null){
            resultados = a.buscarModelosFrecuentes(tipos, coleccion,
                  semillas, params.getMinFreq(), params.getWindowSize(), episodios);
         }else{
            resultados = a.reiniciarBusqueda(tipos, coleccion, modelosBase,
                  params.getMinFreq(), params.getWindowSize(), episodios);
         }

         eventosEliminados = ((AbstractMine)a).getEventosEliminados();
         if(anotaciones != null && a instanceof IBusquedaArbol){
            anotaciones.addAll(((IBusquedaAnotaciones)a).getAnotaciones().getTodasAnotaciones());
         }
         return resultados;
      }catch(FactoryInstantiationException e){
         LOGGER.log(Level.SEVERE, "Fallo al instanciar minería con semilla y episodios.", e);
      }catch(AlgoritmoException e){
         LOGGER.log(Level.SEVERE, "Exception genérica del algoritmo.", e);
      }
      return Collections.emptyList();
   }

   public ModeloSemilla getModeloSemilla(IClustering clustering, boolean conEpisodios){
      List<Patron> patrones = new ArrayList<Patron>();

      Patron p = getPatronSemilla();
      patrones.add(p);
      if(conEpisodios)
         return new ModeloSemillaEpisodios(p.getTipos(), episodios, params.getWindowSize(),
               patrones, null, clustering);
      return new ModeloSemilla(p.getTipos(), params.getWindowSize(), patrones, null, clustering);
   }

   public PatronSemilla getPatronSemilla(){
      if("BD4".equals(params.getCollection())){
         LOGGER.info("Seed pattern for BD4");
         return getPatronSemillaDB4(params, tipos);
      }

      //String[] tiposSemilla = tipos.toArray(new String[tipos.size()]).clone();
      List<RIntervalo> rests = getRIntervalosSemilla(params.getWindowSize());
      List<String> tiposList = new ArrayList<String>();
      for(RIntervalo r : rests){
         if(!tiposList.contains(r.getTipoA())){
            tiposList.add(r.getTipoA());
         }
         if(!tiposList.contains(r.getTipoB())){
            tiposList.add(r.getTipoB());
         }
      }
      String[] tiposSemilla = tiposList.toArray(new String[tiposList.size()]);
      Arrays.sort(tiposSemilla);
      return new PatronSemilla(tiposSemilla, rests, params.isSavePatternInstances());
   }

   private final IColeccion getCollection(boolean printInfo){
      return getCollection(params, tipos, episodios, ocurrenciasEpisodios, printInfo);
   }

   public static PatronSemilla getPatronSemillaDB4(ConfigurationParameters params, List<String> tipos){
      /*
      List<RIntervalo> rests = new ArrayList<RIntervalo>(Arrays.asList(
            new RIntervalo("7", "9", -1, 20),
            new RIntervalo("9", "b1", -32, -19),
            new RIntervalo("b1", "6", 51, 56),
            new RIntervalo("6", "5", -39, -11),
            new RIntervalo("5", "b4", -31, 10),
            new RIntervalo("b4", "8", -39, -23),
            new RIntervalo("b1", "f1", 9, 9),
            new RIntervalo("b4", "f4", 7, 7)
      ));
      List<String> tiposSemilla = new ArrayList<String>(Arrays.asList("5", "6", "7", "8", "9", "b1", "b4", "f1", "f4"));
      //List<String> tiposSemilla = new ArrayList<String>(Arrays.asList("b1", "b4", "f1", "f4"));
      //List<String> tiposSemilla = new ArrayList<String>(Arrays.asList("7","9"));

      */
      List<RIntervalo> rests = new ArrayList<RIntervalo>(Arrays.asList(
            new RIntervalo("f4", "9", -0,19),
            new RIntervalo("f4", "f1", 9,19),
            new RIntervalo("f4", "5", 16,19),
            new RIntervalo("f4", "b2", 10,19),
            new RIntervalo("f4", "6", 2,19),
            new RIntervalo("f4", "f3", 1,19)/*,
            //new RIntervalo("f4", "7", 7,19),
            new RIntervalo("9", "f1", -10,19),
            new RIntervalo("9", "5", -3,16),
            new RIntervalo("9", "b2", -9,19),
            new RIntervalo("9", "6", -17,19),
            new RIntervalo("9", "f3", -18,19),
            //new RIntervalo("9", "7", -12,7),
            new RIntervalo("f1", "5", -3,7),
            new RIntervalo("f1", "b2", -9,10),
            new RIntervalo("f1", "6", -17,10),
            new RIntervalo("f1", "f3", -18,10),
            //new RIntervalo("f1", "7", -12,10),
            new RIntervalo("5", "b2", -6,3),
            new RIntervalo("5", "6", -14,3),
            new RIntervalo("5", "f3", -15,3),
            //new RIntervalo("5", "7", -9,3),
            new RIntervalo("b2", "6", -17,9),
            new RIntervalo("b2", "f3", -18,9),
            //new RIntervalo("b2", "7", -12,9),
            new RIntervalo("6", "f3", -7,4)
            //new RIntervalo("6", "7", -12,6),
            //new RIntervalo("f3", "7", -12,13)*/
      ));
      //List<String> tiposSemilla = new ArrayList<String>(Arrays.asList("5", "6", "7", "8", "9", "b1", "b4", "f1", "f4"));
      String[] tiposSemilla = new String[]{"f4", "9", "f1", "5", "b2", "6", "f3"/*, "7"*/};
      Arrays.sort(tiposSemilla);
      return new PatronSemilla(tiposSemilla, rests, params.isSavePatternInstances());
   }

   //Mismo patrón que en la memoria de tesis
   public static List<RIntervalo> getRIntervalosSemilla(int ventana) {
      if(!SEMILLA_ORIXINAL) LOGGER.warning("El patrón semilla no es el de la tesis");

      List<RIntervalo> rests = new ArrayList<RIntervalo>();
      RIntervalo ri;
      // Restricciones de inicio - fin de episodio
      ri = new RIntervalo("iA", "fA", 1, ventana);
      rests.add(ri);
      ri = new RIntervalo("iD", "fD", 1, ventana);
      rests.add(ri);
      ri = new RIntervalo("iF", "fF", 1, ventana);
      rests.add(ri);
      if(SEMILLA_ORIXINAL)       ri = new RIntervalo("iT", "fT", 1, ventana);
      if(SEMILLA_ORIXINAL)       rests.add(ri);

      // Otras restricciones
      ri = new RIntervalo("iA", "iF", -5, 5);
      rests.add(ri);
      if(SEMILLA_ORIXINAL)       ri = new RIntervalo("iF", "iT", -5, 5);
      if(SEMILLA_ORIXINAL)       rests.add(ri);
      ri = new RIntervalo("fA", "fF", -5, 5);
      rests.add(ri);
      if(SEMILLA_ORIXINAL)       ri = new RIntervalo("fF", "fT", -5, 5);
      if(SEMILLA_ORIXINAL)       rests.add(ri);
      ri = new RIntervalo("iD", "iF", -ventana, 0);
      rests.add(ri);

      // Restricciones comentadas
       //ri = new RIntervalo("iA","iT",-5,5);
      //rests.add(ri);
      //ri = new RIntervalo("fA","fT",-5,5);
      //rests.add(ri);
      //ri = new RIntervalo("iA","iD",0,ventana);
      //rests.add(ri);
      //ri = new RIntervalo("iF","iD",0,ventana);
      //rests.add(ri);
      return rests;
   }

   public void resetResultadosCapsula(){
      resultados = null;
      anotaciones = null;

      System.gc();
      System.runFinalization();
   }

   public List<IAsociacionTemporal> distribuciones(){
      if(resultados != null && resultados.size()>1){
         return resultados.get(1);
      }
      return null;
   }
   public void distribuciones(List<IAsociacionTemporal> distribuciones){
      resultados = new ArrayList<List<IAsociacionTemporal>>();
      resultados.add(new ArrayList<IAsociacionTemporal>());
      resultados.add(distribuciones);
   }


   /**
    * Devuelve el nombre del fichero con el que comparar los resultados de una
    * ejecución con la configuración contenida en <params>.
    * No comprueba que el fichero exista.
    * La estructura del nombre del fichero es la siguiente:
    * {@code <collection>-<modo>[-neg]-<window>.txt }
    * @param params
    * @return La ruta absoluta del fichero con el que comparar o null si no puede
    * construirse con la configuración de params (si falta el nombre de la colección).
    */
   public static String getFicheroValidacion(ConfigurationParameters params){
      String collection = params.getCollection();
      if(collection == null || collection.isEmpty()){
         return null;
      }
      String base = ExecutionParameters.REFERENCIAS_PATH;
      return base + collection + "-" + params.getModeString()
            + (params.esNegativo()? "-neg" : "") + "-" + params.getWindowSize()
            + "-cp-" + params.getCurrentPercentage() + "-mp-" + params.getMaximumPercentage()
            + "-mf-" + params.getMinFreq() + ".txt";
   }

   public static IColeccion getCollection(ConfigurationParameters params, List<String> tipos,
         List<Episodio> episodios, List<InstanciaEpisodio> ocurrenciasEpisodios, boolean printInfo){
      IColeccion coleccion = null;

      try {
         if(params.esApnea()){
            coleccion = ApneaReader.parseFiles(params, tipos, episodios, ocurrenciasEpisodios);
            if(params.isSoloUltimaSecuencia()){
               ISecuencia ultimaSecuencia = coleccion.get(coleccion.size()-1);
               coleccion.clear();
               coleccion.add(ultimaSecuencia);
               LOGGER.info("Sólo se utilizará la última secuencia de la colección de SAHS.");
            }else{
               coleccion.sort();
            }
         }else if(params.getInputFileName() != null && params.getInputFileName().endsWith(".csv")){
            coleccion = GenericEpisodesCSVReader.parseFiles(tipos, episodios, ocurrenciasEpisodios,
                  params.getInputPath(ExecutionParameters.PROJECT_HOME, ExecutionParameters.PATH_SINTETICAS_NUEVAS),
                  params.getInputFileName(), params.getMode() == Modes.MODE_EPISODE);
         }else{
            if("".equals(params.getInputFileName())){
               coleccion = GenBBDDEpisodios.parseFiles(tipos, episodios, ocurrenciasEpisodios,
                     params.getInputPath(ExecutionParameters.PROJECT_HOME, ExecutionParameters.PATH_SINTETICAS),
                     params.getInputFileName());
            }else{
               coleccion = GenBBDDEpisodios.parseFiles(tipos, episodios, ocurrenciasEpisodios,
                     params.getInputPath(ExecutionParameters.PROJECT_HOME, ExecutionParameters.PATH_SINTETICAS),
                     params.getInputFileName());
            }
         }
      } catch (MalformedFileException mfe) {
         LOGGER.log(Level.SEVERE, "El fichero de la colección no es correcto. Se termina el programa.", mfe);
         System.exit(1);
      }

      if(printInfo){
         new DescripcionColeccion(coleccion).printInfo(tipos, episodios);
      }


      return coleccion;
   }
}
