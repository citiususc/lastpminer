package source.busqueda.semilla;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import source.AllThatYouNeed;
import source.AllThatYouNeedSAHS;
import source.CapsulaEjecucion;
import source.Comparacion;
import source.Principal;
import source.PrincipalTestGeneral;
import source.busqueda.Mine;
import source.configuracion.Algorithms;
import source.configuracion.ConfigurationParameters;
import source.configuracion.ExecutionParameters;
import source.configuracion.Modes;
import source.evento.ColeccionSimple;
import source.evento.Episodio;
import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.evento.SecuenciaSimple;
import source.excepciones.AlgoritmoException;
import source.excepciones.FactoryInstantiationException;
import source.excepciones.SemillasNoValidasException;
import source.io.ResultWriter;
import source.modelo.IAsociacionTemporal;
import source.modelo.clustering.ClusteringFactory;
import source.modelo.clustering.IClustering;
import source.modelo.semilla.ModeloSemilla;
import source.patron.Patron;
import source.patron.PatronSemilla;
import source.restriccion.RIntervalo;

public class SemillaConjuncionTest {

   @Test
   public void testGeneral(){
      boolean PASADO = false;
      Collection<Object[]> data = Arrays.asList(new Object[][] {
         {Algorithms.ALG_ASTP, Modes.MODE_SEED, "apnea", 80, PASADO},
      });
      for(Object[] d : data){
         PrincipalTestGeneral test = new PrincipalTestGeneral((Algorithms)d[0], (Modes)d[1], (String)d[2], (Integer)d[3], (Boolean)d[4]);
         test.test();
      }

   }

   /**
    * Este test comprueba que se calcula correctamente la frecuencia de un patrón
    * semilla sencillo en una colección de prueba que se compone de una secuencia
    * (A,22), (B,23), (A,25), (C,27), (B,28), (B,29)
    * @throws AlgoritmoException
    */
   @Test public void calcularFrecuenciaSemillaTest() throws AlgoritmoException{
      ConfigurationParameters params = new ConfigurationParameters();
      params.setWindowSize(6);
      params.setSavePatternInstances(false);
      params.setSaveRemovedEvents(false);
      params.setMinFreq(1);
      IClustering clustering = ClusteringFactory.getClustering(params.getClusteringClassName());

      SemillaConjuncion mine = new SemillaConjuncion(params.getExecutionId(),
            params.isSavePatternInstances(), params.isSaveRemovedEvents(), clustering, !params.isCompleteResult());

      List<String> tiposColeccion = Arrays.asList("A", "B", "C");
      IColeccion coleccion = new ColeccionSimple(new ArrayList<ISecuencia>(1));
      ISecuencia secuencia = new SecuenciaSimple();
      secuencia.add(new Evento("A", 22));
      secuencia.add(new Evento("B", 23));
      secuencia.add(new Evento("A", 25));
      secuencia.add(new Evento("C", 27));
      secuencia.add(new Evento("B", 28));
      secuencia.add(new Evento("B", 29));

      coleccion.add(secuencia);

      //Patrón semilla
      String[] tiposSemilla = new String[]{"A", "B"};
      List<RIntervalo> rests = Arrays.asList(new RIntervalo("A","B",0,3));
      PatronSemilla semilla = new PatronSemilla(tiposSemilla, rests, params.isSavePatternInstances());

      List<Patron> patrones = new ArrayList<Patron>();
      List<ModeloSemilla> semillas = new ArrayList<ModeloSemilla>();
      patrones.add(semilla);
      //ModeloSemilla mod = new ModeloSemilla(tiposSemilla, ventana, patrones, ConfigurationParameters.savePatternInstances, clustering);
      ModeloSemilla mod = new ModeloSemilla(semilla.getTipos(), params.getWindowSize(), patrones, clustering);
      semillas.add(mod);

      try {
         List<List<IAsociacionTemporal>> resultados = mine.buscarModelosFrecuentes(tiposColeccion, coleccion,
               semillas, params.getMinFreq(), params.getWindowSize());
         printResultados(resultados);

      } catch (SemillasNoValidasException e) {
         e.printStackTrace();
      }
      Assert.assertEquals(2, mod.getSoporte());
      //mine.calcularFrecuenciaSemilla(candidatas, coleccion);

   }

   /**
    * Test que comprueba que la semilla puede ser extendida con eventos que aparecen
    * después de sus ocurrencias.
    * Como los eventos que no pertenecen a la semilla son añadidos como patrones semilla
    * de tamaño 1 no se borran de la colección al hacer interval marking en el cálculo
    * de soporte del patrón semilla.
    * @throws AlgoritmoException
    */
   @Ignore @Test public void semillaExtendidaAPosterioriSimpleTest() throws AlgoritmoException{
      ConfigurationParameters params = new ConfigurationParameters();
      params.setWindowSize(6);
      params.setSavePatternInstances(false);
      params.setSaveRemovedEvents(true);
      params.setMinFreq(1);
      IClustering clustering = ClusteringFactory.getClustering(params.getClusteringClassName());

      SemillaConjuncion mine = new SemillaConjuncion(params.getExecutionId(),
            params.isSavePatternInstances(), params.isSaveRemovedEvents(), clustering, !params.isCompleteResult());
   //	mine.setModoIntervalMarkingInterno(false);

      List<String> tiposColeccion = Arrays.asList("A", "B", "C", "D");
      IColeccion coleccion = new ColeccionSimple(new ArrayList<ISecuencia>(1));
      ISecuencia secuencia = new SecuenciaSimple();
      secuencia.add(new Evento("D", 0));
      secuencia.add(new Evento("A", 1));
      secuencia.add(new Evento("B", 2));
      secuencia.add(new Evento("C", 3));
      secuencia.add(new Evento("D", 4));
      secuencia.add(new Evento("A", 50));
      coleccion.add(secuencia);

      //Patrón semilla
      String[] tiposSemilla = new String[]{"A", "B", "C"};
      int win = params.getWindowSize();
      List<RIntervalo> rests = Arrays.asList(new RIntervalo("A","B",-win,+win),
                                             new RIntervalo("A","C",-win,+win));

      PatronSemilla semilla = new PatronSemilla(tiposSemilla, rests, params.isSavePatternInstances());

      List<Patron> patrones = new ArrayList<Patron>();
      List<ModeloSemilla> semillas = new ArrayList<ModeloSemilla>();
      patrones.add(semilla);
      //ModeloSemilla mod = new ModeloSemilla(tiposSemilla, ventana, patrones, ConfigurationParameters.savePatternInstances, clustering);
      ModeloSemilla mod = new ModeloSemilla(semilla.getTipos(), win, patrones, clustering);
      semillas.add(mod);

      try {
         List<List<IAsociacionTemporal>> resultados = mine.buscarModelosFrecuentes(tiposColeccion, coleccion, semillas, params.getMinFreq(), win);
         printResultados(resultados);

      } catch (SemillasNoValidasException e) {
         e.printStackTrace();
         Assert.fail(e.getMessage());
      }
      Assert.assertEquals(6, secuencia.size());
   }

   /**
    * Misma idea que la prueba anterior con una secuencia que añade un evento con
    * mucha separación y que pertenece a los del patrón semilla.
    * @throws AlgoritmoException
    */
   @Ignore @Test public void semillaExtendidaAPosterioriSimple2Test() throws AlgoritmoException{
      ConfigurationParameters params = new ConfigurationParameters();
      params.setWindowSize(6);
      params.setSavePatternInstances(false);
      params.setSaveRemovedEvents(true);
      params.setMinFreq(1);
      IClustering clustering = ClusteringFactory.getClustering(params.getClusteringClassName());

      SemillaConjuncion mine = new SemillaConjuncion(params.getExecutionId(),
            params.isSavePatternInstances(), params.isSaveRemovedEvents(), clustering, !params.isCompleteResult());

      List<String> tiposColeccion = Arrays.asList("A", "B", "C");
      IColeccion coleccion = new ColeccionSimple(new ArrayList<ISecuencia>(1));
      ISecuencia secuencia = new SecuenciaSimple();
      secuencia.add(new Evento("A", 1));
      secuencia.add(new Evento("B", 2));
      secuencia.add(new Evento("C", 3));
      secuencia.add(new Evento("A", 50));
      coleccion.add(secuencia);

      //Patrón semilla
      int win = params.getWindowSize();
      String[] tiposSemilla = new String[]{"A", "B"};
      List<RIntervalo> rests = Arrays.asList(new RIntervalo("A","B",-win,+win));

      PatronSemilla semilla = new PatronSemilla(tiposSemilla, rests, params.isSavePatternInstances());

      List<Patron> patrones = new ArrayList<Patron>();
      List<ModeloSemilla> semillas = new ArrayList<ModeloSemilla>();
      patrones.add(semilla);
      //ModeloSemilla mod = new ModeloSemilla(tiposSemilla, ventana, patrones, ConfigurationParameters.savePatternInstances, clustering);
      ModeloSemilla mod = new ModeloSemilla(semilla.getTipos(), win, patrones, clustering);
      semillas.add(mod);

      try {
         List<List<IAsociacionTemporal>> resultados = mine.buscarModelosFrecuentes(tiposColeccion, coleccion, semillas, params.getMinFreq(), win);
         printResultados(resultados);
      } catch (SemillasNoValidasException e) {
         e.printStackTrace();
         Assert.fail(e.getMessage());
      }

      Assert.assertEquals(4, secuencia.size());
   }

   /**
    * TODO
    * Objetivo comparar los resultados de usar el patrón semilla completo (descrito en la memoria de tesis)
    * y el uso de un patrón semilla igual al anterior sin las restricciones de fD (fin de la bajada de la saturación
    * de oxígeno en sangre).
    * Para que la comparación sea más directa se podrían utilizar los resultados de clustering del uso de la semilla
    * completa para la semilla incompleta de otra forma, los intervalos son diferentes y es muy difícil hacer una
    * comparación.
    * Además, habría que realizar una copia de la colección ya que se pueden borrar eventos en una ejecución.
    * @throws PatronSemillaNoFrecuenteException
    * @throws FactoryInstantiationException
    * @throws ModelosBaseNoValidosException
    */
   //@Ignore("La comparación no es directa")
   @Ignore("") @Test public void semillaExtendidaAPosterioriTest() throws SemillasNoValidasException, FactoryInstantiationException {

      AllThatYouNeed capsula = new AllThatYouNeedSAHS();
      ConfigurationParameters params = capsula.params;
      params.setSavePatternInstances(false);
      params.setSaveRemovedEvents(false);
      params.setMode(Modes.MODE_SEED);
      params.setAlgorithm(Algorithms.ALG_ASTP);
      capsula.mineria();

      AllThatYouNeed capsulaIncompleta = new AllThatYouNeedSAHS();
      capsulaIncompleta.params = params;
      capsulaIncompleta.semilla(capsulaIncompleta.getClustering(), getSemillaIncompleta(params, capsulaIncompleta.tipos, capsulaIncompleta.getClustering()), 0, null);

      List<List<IAsociacionTemporal>> resultA, resultB;
      resultA = capsula.resultados;
      resultB = capsulaIncompleta.resultados;

      params.setResultPath(ExecutionParameters.PROJECT_HOME + "/test/output/");
      ResultWriter.escribirPatrones(params, resultA);
      ResultWriter.escribirPatrones(params, resultB);

      Comparacion comp = Principal.compararResultados(resultA, resultB);
      System.out.println(comp.toString(true));
      //System.out.println(comp.getPatronesSoloA());
      //assertTrue(comp.sonIguales());
   }

   public static void printResultados(List<List<IAsociacionTemporal>> resultados){
      int i=0;
      System.out.println("Resultados. Número de niveles: " + resultados.size());
      for(List<IAsociacionTemporal> nivel: resultados){
         System.out.println("Nivel " + i++);
         for(IAsociacionTemporal asoc: nivel){
            if(i==1){
               System.out.println("\tAsociación: Modelo: " + asoc.getTipos() + ". Frecuencia: " + asoc.getSoporte() );
            }else{
               System.out.println("\tAsociacion: " + asoc);
               for(Patron p: asoc.getPatrones()){
                  System.out.println("\t\tPatron: " + p);
               }
            }
         }
      }
   }

   protected List<ModeloSemilla> getSemillaIncompleta(ConfigurationParameters params, List<String> tipos, IClustering clustering){
      List<ModeloSemilla> semillas = new ArrayList<ModeloSemilla>();
      List<Patron> patrones = new ArrayList<Patron>();

      String[] tiposSemilla = (String[]) tipos.toArray();
      List<RIntervalo> rests = CapsulaEjecucion.getRIntervalosSemilla(params.getWindowSize());
      ListIterator<RIntervalo> it = rests.listIterator();
      while(it.hasNext()){
         RIntervalo r = it.next();
         if(r.getTipoA().equals("fD") || r.getTipoB().equals("fD")){
            it.remove();
         }
      }

      PatronSemilla p = new PatronSemilla(tiposSemilla, rests, params.isSavePatternInstances());
      patrones.add(p);
      ModeloSemilla mod = new ModeloSemilla(p.getTipos(), params.getWindowSize(), patrones, clustering);
      semillas.add(mod);
      return semillas;
   }

   protected boolean cumpleEpisodios(Patron p, List<Episodio> episodios){
      int iInicio, iFin;
      for(Episodio epi: episodios){
         iInicio = Arrays.asList(p.getTipos()).indexOf(epi.getTipoInicio());
         iFin = Arrays.asList(p.getTipos()).indexOf(epi.getTipoFin());
         //contiene los dos tipos de eventos del episodio epi
         if(iInicio != -1 && iFin != -1){
            List<RIntervalo> rests = p.getRestricciones(epi.getTipoInicio(), epi.getTipoFin());
            for(RIntervalo r:rests){
               if(r.getInicio()<0){
                  return false;
               }
            }
         }
      }
      return true;
   }

   @Test
   public void testValidacion() throws SemillasNoValidasException, AlgoritmoException{
      Assert.assertTrue("No se está utilizando la semilla de la tesis, no se puede hacer validación con fichero de referencia", CapsulaEjecucion.SEMILLA_ORIXINAL);
      int win = 80;
      AllThatYouNeed capsula = new AllThatYouNeedSAHS();
      capsula.params.setAlgorithm(Algorithms.ALG_ASTP);
      capsula.params.setMode(Modes.MODE_SEED);
      capsula.params.setWindowSize(win);
      capsula.params.setSaveRemovedEvents(true);
      capsula.mineriaFicheros();

   }

   @Test
   public void testValidacionSinSemilla() throws SemillasNoValidasException, AlgoritmoException{
      int win = 80;
      AllThatYouNeed capsula = new AllThatYouNeedSAHS();
      capsula.params.setAlgorithm(Algorithms.ALG_ASTP);
      capsula.params.setMode(Modes.MODE_SEED);
      capsula.params.setWindowSize(win);


      SemillaConjuncion sem2 = new SemillaConjuncion("asdf", false, true, capsula.getClustering(), false);
      //sem2.setTamMaximoPatron(capsula.params.getTamMaximoPatron());
      //ModeloSemilla semilla = capsula.getModeloSemilla(capsula.getClustering(), false);
      //ModeloSemilla semilla = capsula.getModeloSemilla(capsula.getClustering(), false);
      List<Patron> patrones = new ArrayList<Patron>();

      Patron p = new Patron(new String[]{"fA","fF"}, new ArrayList<RIntervalo>(Arrays.asList(new RIntervalo("fA","fF",-win,win))), false);
      //Patron p = new Patron(new String[]{"fA"}, Collections.<RIntervalo> emptyList(), false);
      patrones.add(p);
      ModeloSemilla semilla = new ModeloSemilla(p.getTipos(), capsula.params.getWindowSize(), patrones, null, capsula.getClustering());

      capsula.resultados = sem2.buscarModelosFrecuentes(capsula.tipos, capsula.coleccion, Arrays.asList(semilla),
            capsula.params.getMinFreq(), win);
      String filePatrones = ResultWriter.escribirPatrones(capsula.params, capsula.resultados);
      ResultWriter.escribirScriptHistogramas(capsula.resultados.get(1), capsula.params.getResultPath() + "/hists", win);
      System.out.println("Histogramas escritos en " +capsula.params.getResultPath() + "/hists" );

      capsula.params.setMode(Modes.MODE_BASIC);
      String ficheroReferencia = CapsulaEjecucion.getFicheroValidacion(capsula.params);
      boolean iguales =  Principal.compararFicheros(ficheroReferencia, filePatrones);
      Assert.assertTrue("Los ficheros no son iguales", iguales);
   }

   @Test
   public void testComparacionDistribuciones() throws SemillasNoValidasException, AlgoritmoException{
      int win = 80;
      AllThatYouNeed capsula = new AllThatYouNeedSAHS();
      capsula.params.setAlgorithm(Algorithms.ALG_ASTP);
      capsula.params.setMode(Modes.MODE_SEED);
      capsula.params.setWindowSize(win);


      SemillaConjuncion2 astp = new SemillaConjuncion2("asdf", false, true, capsula.getClustering(), false);
      //sem2.setTamMaximoPatron(capsula.params.getTamMaximoPatron());
      //ModeloSemilla semilla = capsula.getModeloSemilla(capsula.getClustering(), false);
      //ModeloSemilla semilla = capsula.getModeloSemilla(capsula.getClustering(), false);
      List<Patron> patrones = new ArrayList<Patron>();

      Patron p = new Patron(new String[]{"fA","fF"}, new ArrayList<RIntervalo>(Arrays.asList(new RIntervalo("fA","fF",-win,win))), false);
      //Patron p = new Patron(new String[]{"fA"}, Collections.<RIntervalo> emptyList(), false);
      patrones.add(p);
      ModeloSemilla semilla = new ModeloSemilla(p.getTipos(), capsula.params.getWindowSize(), patrones, null, capsula.getClustering());

      capsula.distribuciones(astp.calcularDistribuciones(capsula.tipos, capsula.coleccion, Arrays.asList(semilla),
            capsula.params.getMinFreq(), win));


      //Original
      semilla = capsula.getModeloSemilla(capsula.getClustering(), false);

      AllThatYouNeed capsula2 = new AllThatYouNeedSAHS();
      capsula2.params = capsula.params;
      capsula2.distribuciones(astp.calcularDistribuciones(capsula.tipos, capsula.coleccion, Arrays.asList(semilla),
            capsula.params.getMinFreq(), win));

      //Comparar distribuciones de las dos

      Principal.compararDistribuciones(capsula.distribuciones(), capsula2.distribuciones());
   }

   /**
    * Se obtienen las distribuciones con una semilla {fA, fF -> [-win,win]} y se comparan las
    * distribuciones sin semilla alguna.
    * @throws SemillasNoValidasException
    * @throws AlgoritmoException
    */
   @Test
   public void testComparacionDistribucionesConBasic() throws SemillasNoValidasException, AlgoritmoException{
      int win = 80;
      AllThatYouNeed capsula = new AllThatYouNeedSAHS();
      capsula.params.setAlgorithm(Algorithms.ALG_ASTP);
      capsula.params.setMode(Modes.MODE_SEED);
      capsula.params.setWindowSize(win);


      SemillaConjuncion astp = new SemillaConjuncion("asdf", false, true, capsula.getClustering(), false);
      //sem2.setTamMaximoPatron(capsula.params.getTamMaximoPatron());
      //ModeloSemilla semilla = capsula.getModeloSemilla(capsula.getClustering(), false);
      //ModeloSemilla semilla = capsula.getModeloSemilla(capsula.getClustering(), false);
      List<Patron> patrones = new ArrayList<Patron>();

      Patron p = new Patron(new String[]{"fA","fF"}, new ArrayList<RIntervalo>(Arrays.asList(new RIntervalo("fA","fF",-win,win))), false);
      //Patron p = new Patron(new String[]{"fA"}, Collections.<RIntervalo> emptyList(), false);
      patrones.add(p);
      ModeloSemilla semilla = new ModeloSemilla(p.getTipos(), capsula.params.getWindowSize(), patrones, null, capsula.getClustering());

      capsula.distribuciones(astp.calcularDistribuciones(capsula.tipos, capsula.coleccion, Arrays.asList(semilla),
            capsula.params.getMinFreq(), win));
      // Escribir distribuciones
      ResultWriter.escribirScriptHistogramas(capsula.distribuciones(), "output/test/histogramas-semilla/semilla/", win);

      // Original
      AllThatYouNeed capsula2 = new AllThatYouNeedSAHS();
      capsula2.params = capsula.params;
      Mine ast2p = new Mine("asdf2", false, false, capsula2.getClustering(), false);
      capsula2.distribuciones(ast2p.calcularDistribuciones(capsula2.tipos, capsula2.coleccion,
            capsula2.params.getMinFreq(), win));

      // Comparar distribuciones de las dos
      ResultWriter.escribirScriptHistogramas(capsula2.distribuciones(), "output/test/histogramas-semilla/basic/", win);

      Principal.compararDistribuciones(capsula.distribuciones(), capsula2.distribuciones());
   }

   @Test
   public void testComparacionPatronesConBasic() throws SemillasNoValidasException, AlgoritmoException{
      int win = 80;
      AllThatYouNeed capsula = new AllThatYouNeedSAHS();
      capsula.params.setAlgorithm(Algorithms.ALG_ASTP);
      capsula.params.setMode(Modes.MODE_SEED);
      capsula.params.setWindowSize(win);


      SemillaConjuncion astp = new SemillaConjuncion("asdf", false, true, capsula.getClustering(), false);
      //sem2.setTamMaximoPatron(capsula.params.getTamMaximoPatron());
      //ModeloSemilla semilla = capsula.getModeloSemilla(capsula.getClustering(), false);
      //ModeloSemilla semilla = capsula.getModeloSemilla(capsula.getClustering(), false);
      List<Patron> patrones = new ArrayList<Patron>();

      Patron p = new Patron(new String[]{"fA","fF"}, new ArrayList<RIntervalo>(Arrays.asList(new RIntervalo("fA","fF",-win,win))), false);
      //Patron p = new Patron(new String[]{"fA"}, Collections.<RIntervalo> emptyList(), false);
      patrones.add(p);
      ModeloSemilla semilla = new ModeloSemilla(p.getTipos(), capsula.params.getWindowSize(), patrones, null, capsula.getClustering());

      capsula.resultados = astp.buscarModelosFrecuentes(capsula.tipos, capsula.coleccion, Arrays.asList(semilla),
            capsula.params.getMinFreq(), win);


      //Original
      AllThatYouNeed capsula2 = new AllThatYouNeedSAHS();
      capsula2.params = capsula.params;
      Mine ast2p= new Mine("asdf2",false,false,capsula.getClustering(), false);
      capsula2.resultados = ast2p.buscarModelosFrecuentes(capsula.tipos, capsula.coleccion,
            capsula.params.getMinFreq(), win);

      //Comparar distribuciones de las dos

      /*Comparacion cmop = */Principal.compararResultados(capsula.resultados, capsula2.resultados);
   }

   @Test
   public void testRendimiento() throws SemillasNoValidasException, AlgoritmoException{
      int win = 80;
      AllThatYouNeed capsula = new AllThatYouNeedSAHS();
      capsula.params.setAlgorithm(Algorithms.ALG_ASTP);
      capsula.params.setMode(Modes.MODE_SEED);
      capsula.params.setWindowSize(win);
      capsula.params.setTamMaximoPatron(2);
      capsula.params.setSaveRemovedEvents(true);
      String resultados1 = capsula.mineria();


      AllThatYouNeed capsula2 = new AllThatYouNeedSAHS();
      capsula2.params = capsula.params;
      SemillaConjuncion2 sem2 = new SemillaConjuncion2("asdf", false, true, capsula.getClustering(), false);
      sem2.setTamMaximoPatron(capsula.params.getTamMaximoPatron());
      ModeloSemilla semilla = capsula2.getModeloSemilla(capsula.getClustering(), false);
      capsula2.resultados = sem2.buscarModelosFrecuentes(capsula2.tipos, capsula2.coleccion, Arrays.asList(semilla),
            capsula.params.getMinFreq(), win);
      capsula2.eventosEliminados = sem2.getEventosEliminados();
      String filePatrones = ResultWriter.escribirPatrones(capsula2.params, capsula2.resultados);

      boolean iguales = Principal.compararFicheros(resultados1, filePatrones);
      System.out.println("Iguales? " + iguales + (iguales? "" : ". Ficheros: \n" + resultados1 + "\n" + filePatrones + "\n" ));



      for(int i = 0; i<capsula.coleccion.size(); i++){
//         System.out.println("Original:\n" + capsula.eventosEliminados.get(i));
//         System.out.println("Mejorado:\n" + capsula2.eventosEliminados.get(i));
         Assert.assertEquals("Diferencia en la secuencia " + i , capsula.eventosEliminados.get(i).size(), capsula2.eventosEliminados.get(i).size());
         Assert.assertEquals("Diferencia en la secuencia " + i , capsula.eventosEliminados.get(i), capsula2.eventosEliminados.get(i));
      }

   }

   private class SemillaConjuncion2 extends SemillaConjuncion {

      public SemillaConjuncion2(String executionId,
            boolean savePatternInstances, boolean saveRemovedEvents,
            IClustering clustering, boolean removePatterns) {
         super(executionId, savePatternInstances, saveRemovedEvents, clustering,
               removePatterns);
      }

      @Override
      protected int[] insertarIntervalo(List<int[]> intervalosActivos, int[] intervaloActual){
         intervalosActivos.add(intervaloActual);
         int[] ultimo = intervaloActual;
         int[] anterior = intervalosActivos.size()<2 ? null : intervalosActivos.get(intervalosActivos.size()-2);
         //Si extendemos hacia atrás el intervalo puede que se solape con el anterior
         while(anterior != null && ultimo[0]<=anterior[1]){
            anterior[1] = ultimo[1];
            if(anterior[0] > ultimo[0]){
               anterior[0] = ultimo[0];
            }
            intervalosActivos.remove(intervalosActivos.size()-1);
            ultimo = anterior;
            anterior = intervalosActivos.size()<2 ?  null : intervalosActivos.get(intervalosActivos.size()-2);
         }
         return new int[2];
      }



   }

   @Test
   public void testInsertarIntervalos(){
      SemillaConjuncion2 sctest = new SemillaConjuncion2("", false, false, null, false);
      List<int[]> intervalosActivos = new ArrayList<int[]>();
      intervalosActivos.add(new int[]{5029, 5098});
      sctest.insertarIntervalo(intervalosActivos, new int[]{5029, 5098});
      Assert.assertEquals("No hay un unico intervalo", 1, intervalosActivos.size());
      Assert.assertArrayEquals(new int[]{5029, 5098}, intervalosActivos.get(0));

      sctest.insertarIntervalo(intervalosActivos, new int[]{5050, 5100});
      Assert.assertEquals("No hay un unico intervalo", 1, intervalosActivos.size());
      Assert.assertArrayEquals(new int[]{5029, 5100}, intervalosActivos.get(0));
   }

   @Test
   public void testBorrarIntervalos(){
      SemillaConjuncion2 sctest = new SemillaConjuncion2("", false, false, null, false);
      List<int[]> intervalosActivos = new ArrayList<int[]>();
      intervalosActivos.add(new int[]{2,4});
      intervalosActivos.add(new int[]{5,8});
      sctest.borrarIntervalosAnteriores(intervalosActivos, 10);
      Assert.assertEquals("No se han borrado todos los intervalos", 0, intervalosActivos.size());

      intervalosActivos.add(new int[]{2,4});
      intervalosActivos.add(new int[]{5,8});
      sctest.borrarIntervalosAnteriores(intervalosActivos, 8);
      Assert.assertEquals("No se ha borrado el primero y respetado el segundo", 1, intervalosActivos.size());
      Assert.assertArrayEquals(new int[]{5,8}, intervalosActivos.get(0));

   }

}
