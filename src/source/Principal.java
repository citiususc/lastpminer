package source;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.junit.Assert;

import source.configuracion.Algorithms;
import source.configuracion.ConfigSintetica;
import source.configuracion.ConfigurationParameters;
import source.configuracion.ExecutionParameters;
import source.evento.Evento;
import source.evento.EventoEliminado;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.excepciones.SemillasNoValidasException;
import source.modelo.ComparadorTipos;
import source.modelo.IAsociacionConDistribucion;
import source.modelo.IAsociacionTemporal;
import source.modelo.Modelo;
import source.patron.Patron;

/**
 * Clase que permite ejecutar tanto ASTPminer como HSTPminer con los
 * argumentos de ejecución que se explican en la función procesarParametros
 * o sin argumentos utilizando los que aparecen en la clase ConfigurationParameters.
 * @author vanesa.graino
 *
 */
public final class Principal {
   private static final Logger LOGGER = Logger.getLogger(Principal.class.getName());

   private static final boolean ESCRIBIR_HISTOS = true;

   public static final int ITERACIONES_PRUEBAS = 5;

   /*
    * Constructor protegido
    */

   private Principal(){

   }

   /**
    *
    * @param args
    * @param params
    *
    * Args:
    * algorithm=[astp,hstp,im]
    * mode=[basic,episode,seed,full]
    * onlyDistributions=[boolean]
    * savePatternInstances=[boolean]
    * saveRemovedEvents=[boolean]
    * saveAllAnnotations=[boolean]
    * onlyOnePatternOccurrenceEachEvent=[boolean]
    * iterations=[Integer]
    * windowSize=[Integer]
    * minFreq=[Integer]
    * currentPercentage=[double]
    * maximumPercentage=[double]
    * mountainLambda=[double]
    * executionId=[String]
    * clusteringClassName=[String,clusteringClassName]
    * inputFileName=[String-filename] -> vacio para usar las bbdd sintéticas
    * inputPath=[String,path-to-input-file]
    * histogramPath=[String,path]
    * resultPath=[String,path]

    */
   // Ejecución con BBDD sintéticas:
   // main.Principal
   //algorithm=[astp,hstp,im, par, hpar, con, hcon] - default: astp
   //mode=[basic,episode,seed,full] - default: full
   //windowSize=[int]  - default: 80
   //minFreq=[int]  - default: 30
   //inputFileName= - default:
   //inputPath=[String] - default:
   //histogramPath=[String] - default:
   //resultPath=[String]
   //iterations=[int] - default: 1
   //tamMaximoPatron=[-1,int] default: -1
   //savePatternInstances=[boolean] - default: false
   //saveRemovedEvents=[boolean] - default: false
   //saveAllAnnotations=[boolean] - default: false
   //clusteringClassName=[String] - default: "source.modelo.clustering.DensityBasedClustering"

   // Ejemplo con BBDD sintéticas:
   // main.Principal inputFileName= minFreq=300 collection="BD7"

   public static void main(final String[] args) {
      Patron.setPrintID(true);

      ConfigurationParameters params = new ConfigurationParameters();
      params.procesarParametros(args);

      //Por si hay una ConfigSintetica en los parámetros
      ConfigSintetica cs = ConfigSintetica.fromArgs(args, "cs.");

      setLoggerSettings();

      LOGGER.info("Parámetros de la ejecución: \n" + params);

      CapsulaEjecucion capsula = new CapsulaEjecucion(params, cs);

      // Hacer copia de las secuencias originales
      IColeccion copiaColeccion = capsula.coleccion.clone();//copiaColeccion(capsula.coleccion);

      if(params.getIteration()==-1){
         for(int it=0, iterations=params.getIterations(); it<iterations; it++) {
            ejecutarIteracion(it, capsula, copiaColeccion, params);
            System.gc();
            System.runFinalization();
         }
      }else{
         ejecutarIteracion(params.getIteration(), capsula, copiaColeccion, params);
      }
   }

   /**
    *
    * @param it - Empieza en cero
    * @param capsula
    * @param copiaColeccion
    * @param params
    */
   private static void ejecutarIteracion(int it, CapsulaEjecucion capsula,
         IColeccion copiaColeccion, ConfigurationParameters params){
      // Restaurar copia de las secuencias originales (por eliminación de eventos, por ejemplo)
      capsula.coleccion = copiaColeccion.clone();//copiaColeccion(copiaColeccion);
      if(params.getAlgorithm() == Algorithms.ALG_OM){
         for(ISecuencia s : capsula.coleccion){
            for(Evento e: s){
               e.setUsado(false);
            }
         }
      }

      try{
         if (params.isOnlyDistributions()) {
            capsula.distribuciones(ESCRIBIR_HISTOS);
         } else {
            capsula.mineriaFicheros(it);
         }
      }catch(SemillasNoValidasException psnfe){
         LOGGER.log(Level.WARNING, "El patrón semilla no es frecuente", psnfe);
      }
   }

   private static void setLoggerSettings(){
      try{
         FileHandler fh = new FileHandler(ExecutionParameters.PROJECT_HOME + "/output/java-log%g.log",true);
         fh.setLevel(Level.FINEST);

         System.setProperty("java.util.logging.config.file", ExecutionParameters.PROJECT_HOME + "/config/logging.properties");

         SimpleFormatter formatter = new SimpleFormatter();
         fh.setFormatter(formatter);
         LOGGER.info("Loggin to file " + ExecutionParameters.PROJECT_HOME + "/output/java-log%g.log");

         //All loggers to log file
         LogManager mng = LogManager.getLogManager();
         Enumeration<String> logNames = mng.getLoggerNames();
         while(logNames.hasMoreElements()){
            mng.getLogger(logNames.nextElement()).addHandler(fh);
         }
         //Logger.getGlobal().addHandler(fh);
         //Logger.getGlobal().setUseParentHandlers(true);
         //LOGGER.addHandler(fh);

      }catch(IOException ioe){
         LOGGER.log(Level.WARNING, "No se guardan los logs", ioe);
      }
   }



   public static Comparacion compararAsociaciones(IAsociacionTemporal asocA, IAsociacionTemporal asocB, Comparacion comp, int nivel){
      if(asocA.getSoporte() != asocB.getSoporte()){
         LOGGER.info(asocA.getTipos() + " has different support: " + asocA.getSoporte() + "(A), " + asocB.getSoporte() + "(B).");
      }

      List<Patron> soloA = new ArrayList<Patron>();
      List<Patron> soloB = new ArrayList<Patron>();
      List<Patron> comunes = new ArrayList<Patron>();

      List<Integer> incluidosB = new ArrayList<Integer>();
      List<Patron> patronesA = asocA.getPatrones();
      List<Patron> patronesB = asocB.getPatrones();
      for(int indexA=0;indexA<patronesA.size(); indexA++){
         boolean encontrado = false;
         for(int indexB=0; !encontrado && indexB<patronesB.size(); indexB++){
            if(patronesA.get(indexA).equals(patronesB.get(indexB))){
               encontrado = true;
               incluidosB.add(indexB);
               comunes.add(patronesA.get(indexA));
            }
         }
         if(!encontrado){
            soloA.add(patronesA.get(indexA));
         }
      }
      if(incluidosB.size()<patronesB.size()){
         for(int indexB=0;indexB<patronesB.size(); indexB++){
            if(incluidosB.contains(indexB)){
               continue;
            }
            soloB.add(patronesB.get(indexB));
         }
      }
      if(soloA.isEmpty() && soloB.isEmpty()){
         comp.getPatronesIguales().get(nivel).add(asocA);
      }else{
         if(!soloA.isEmpty()){
            IAsociacionTemporal asoc = new Modelo(asocA.getTipos(), asocA.getVentana(), soloA, null/*, asocA.getClustering()*/);
            comp.getPatronesSoloA().get(nivel).add(asoc);
         }
         if(!soloB.isEmpty()){
            IAsociacionTemporal asoc = new Modelo(asocB.getTipos(), asocB.getVentana(), soloB, null/*, asocB.getClustering()*/);
            comp.getPatronesSoloB().get(nivel).add(asoc);
         }
         if(!comunes.isEmpty()){
            IAsociacionTemporal asoc = new Modelo(asocA.getTipos(), asocA.getVentana(), comunes, null/*, asocA.getClustering()*/);
            comp.getPatronesIguales().get(nivel).add(asoc);
         }
      }
      return comp;
   }

   public static void compararDistribuciones(List<IAsociacionTemporal> nivelA, List<IAsociacionTemporal> nivelB){
      //TODO

      ComparadorTipos comparador = new ComparadorTipos();
      List<Integer> incluidosB = new ArrayList<Integer>();
      for(int indexA=0; indexA<nivelA.size(); indexA++){
         boolean encontrado = false;
            for(int indexB=0; indexB<nivelB.size() && !encontrado; indexB++){
               //if(nivelA.get(indexA).getTipos().equals(nivelB.get(indexB).getTipos())){
               IAsociacionConDistribucion distA = (IAsociacionConDistribucion)nivelA.get(indexA);
               IAsociacionConDistribucion distB = (IAsociacionConDistribucion)nivelB.get(indexB);

               if(comparador.compare(distA.getTipos(), distB.getTipos()) == 0){
                  //compararAsociaciones(nivelA.get(indexA), nivelB.get(indexB), comp, nivelComun);
                  Assert.assertArrayEquals("Valores diferentes en la distribucion " + distA.toStringSinPatrones(), distA.getDistribucion(), distB.getDistribucion());
                  incluidosB.add(Integer.valueOf(indexB));
                  encontrado = true;
               }
            }
            if(!encontrado){
               System.out.println("La asociacion " + nivelA.get(indexA).toString() + " no está presente en en B");
               //comp.getPatronesSoloA().get(nivelComun).add(nivelA.get(indexA));
            }
      }
      if(incluidosB.size()<nivelB.size()){
         for(int indexB=0; indexB<nivelB.size();indexB++){
            if(incluidosB.contains(indexB)){
               continue;
            }
            //comp.getPatronesSoloB().get(nivelComun).add(nivelB.get(indexB));
            System.out.println("La asociacion " + nivelB.get(indexB).toString() + " no está presente en en A");
         }
      }
   }

   /**
    *
    * @param resultA
    * @param resultB
    * @return el objecto instancia de Comparacion
    */
   public static Comparacion compararResultados(List<List<IAsociacionTemporal>> resultA, List<List<IAsociacionTemporal>> resultB){
      Comparacion comp = new Comparacion();
      //Por cada nivel de ambos resultados
      int nivelComun=0;
      for(; nivelComun<resultA.size() && nivelComun<resultB.size(); nivelComun++){
         comp.getPatronesIguales().add(new ArrayList<IAsociacionTemporal>());
         comp.getPatronesSoloA().add(new ArrayList<IAsociacionTemporal>());
         comp.getPatronesSoloB().add(new ArrayList<IAsociacionTemporal>());

         List<Integer> incluidosB = new ArrayList<Integer>();
         List<IAsociacionTemporal> nivelA = resultA.get(nivelComun);
         List<IAsociacionTemporal> nivelB = resultB.get(nivelComun);
         for(int indexA=0; indexA<nivelA.size(); indexA++){
            boolean encontrado = false;
               for(int indexB=0; indexB<nivelB.size() && !encontrado; indexB++){
                  //if(nivelA.get(indexA).getTipos().equals(nivelB.get(indexB).getTipos())){
                  if(Arrays.asList(nivelA.get(indexA).getTipos()).equals(Arrays.asList(nivelB.get(indexB).getTipos()))){
                     compararAsociaciones(nivelA.get(indexA), nivelB.get(indexB), comp, nivelComun);
                     incluidosB.add(Integer.valueOf(indexB));
                     encontrado = true;
                  }
               }
               if(!encontrado){
                  comp.getPatronesSoloA().get(nivelComun).add(nivelA.get(indexA));
               }
         }
         if(incluidosB.size()<nivelB.size()){
            for(int indexB=0; indexB<nivelB.size();indexB++){
               if(incluidosB.contains(indexB)){
                  continue;
               }
               comp.getPatronesSoloB().get(nivelComun).add(nivelB.get(indexB));
            }
         }
      }
      if(resultA.size() != resultB.size()){
         List<List<IAsociacionTemporal>> biggerResult = resultA.size()>resultB.size()? resultA : resultB;
         List<List<IAsociacionTemporal>> lista = resultA.size()>resultB.size()? comp.getPatronesSoloA() : comp.getPatronesSoloB();
         for(int j=nivelComun; j<biggerResult.size(); j++){
            lista.add(biggerResult.get(j));
         }
      }
      return comp;
   }

   public static boolean compararEventosBorrados(List<List<EventoEliminado>> eliminadosA, List<List<EventoEliminado>> eliminadosB){
      int i=0, comunes;
      boolean iguales = true;
      for(int x=Math.min(eliminadosA.size(), eliminadosB.size());i<x;i++){
         comunes = 0;
         List<EventoEliminado> eliminadosItA=new ArrayList<EventoEliminado>(eliminadosA.get(i)),
               eliminadosItB = new ArrayList<EventoEliminado>(eliminadosB.get(i));
         for(int j=eliminadosItA.size()-1;j>=0;j--){
            if(eliminadosItB.remove(eliminadosItA.get(j))){
               eliminadosItA.remove(j);
               comunes++;
            }
         }
         LOGGER.info("Iteración #" + i + ". Comunes: " + comunes + ", solo en A: " + eliminadosItA.size() + ", solo en B: " + eliminadosItB.size());
         iguales = iguales && !eliminadosItA.isEmpty() && !eliminadosItB.isEmpty();
      }

      if(i<eliminadosA.size()){
         LOGGER.info("A tiene " + (eliminadosA.size()-i) + " iteraciones más.");
         iguales = false;
      }
      if(i<eliminadosB.size()){
         LOGGER.info("B tiene " + (eliminadosB.size()-i) + " iteraciones más.");
         iguales = false;
      }
      return iguales;
   }

   /**
    * Compara si dos ficheros tienen los mismos contenidos
    * @param referencia
    * @param candidato
    * @return true si los ficheros tienen el mismo contenido y false en caso contrario
    */
   public static boolean compararFicheros(String referencia, String candidato){
      LOGGER.info("Comparando referencia con candidato:\n" + referencia + "\n" + candidato);
      if(referencia==null || !new File(referencia).exists()){
         LOGGER.info("No hay referencia con la que comparar");
         return false;
      }
      try {
         boolean sonIguales = FileUtils.contentEquals(new File(referencia), new File(candidato));
         LOGGER.info(sonIguales? "Son iguales" : "Son diferentes");
         return sonIguales;
      } catch (IOException e) {
         LOGGER.log(Level.WARNING, "Error al leer los ficheros de resultados que se iban a comparar", e);
         throw new RuntimeException(e);
      }
   }

   /**
    * Se comprueba si el fichero referencia termina con el fichero candidato.
    * La referencia puede tener más líneas además de las que tenga candidato.
    * @param referencia
    * @param candidato
    * @return
    */
   public static boolean compararFicherosFinal(String referencia, String candidato){
      LOGGER.info("Comparando referencia con candidato (resultado parcial):\n" + referencia + "\n" + candidato);
      if(referencia==null || !new File(referencia).exists()){
         LOGGER.info("No hay referencia con la que comparar");
         return false;
      }
      ReversedLinesFileReader ref = null, cand = null;
      try {
         ref = new ReversedLinesFileReader(new File(referencia));
         cand = new ReversedLinesFileReader(new File(candidato));
         boolean flagIguales = true;
         String lc, lr;
         lc = cand.readLine();
         lr = ref.readLine();
         while(flagIguales && lc!=null){
            flagIguales = lc.equals(lr);
            if(!flagIguales){
               LOGGER.info(">>" + lc + "\nis different from\n>>" + lr);
            }
            lc = cand.readLine();
            lr = ref.readLine();
         }
         LOGGER.info(flagIguales? "Son iguales" : "Son diferentes");
         return flagIguales;
      } catch (IOException e) {
         LOGGER.log(Level.WARNING, "Error al leer los ficheros de resultados que se iban a comparar", e);
         throw new RuntimeException(e);
      } finally {
         try{
            if(ref != null) ref.close();
         }catch(IOException io){

         }
         try{
            if(cand != null) cand.close();
         }catch(IOException io){

         }
      }
   }

}