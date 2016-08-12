package source.scripts;

//import static source.configuracion.ExecutionParameters.BBDD;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import source.configuracion.ConfigSintetica;
import source.configuracion.ExecutionParameters;


/*
 *
 *
 java -cp /home/remoto/vanesa.graino/workspace/hstpminer/target/hstpminer.jar:/home/remoto/vanesa.graino/workspace/hstpminer/lib/*  \
 -Xms512m -Xmx4g -XX:-UseGCOverheadLimit -XX:+UseConcMarkSweepGC source.scripts.ScriptBuilder

 *
 */
public class ScriptBuilder {
   private static final Logger LOGGER = Logger.getLogger(ScriptBuilder.class.getName());
   public enum Estructura {coleccion, algoritmo }


   //private static final String PATH_SINTETICAS = ConfigurationParameters.PATH_SINTETICAS;
   //int[] minFreqs = { 2000, 5000, 10000, 15000, 5000, 4000, 5000, 5000, 5000, /*10000*/ 7000 };
   //private static final int[] MIN_FREQS = {300, 300, 300, 300, 300, 300, 300, 300, 300, 300, 300, 300, 300, 300};
   private static final int MIN_FREQ_SINTETICAS = 300;
   protected static final String MAX_RAM = ExecutionParameters.MAX_RAM;

   /*
    * Atributos
    */

   protected String pathCodigo;
   protected Estructura estructura = Estructura.coleccion;
   protected Integer timeout = null;
   protected boolean apagarAlFinal = true;

   /*
    * Constructores
    */

   protected ScriptBuilder(String pathCodigo){
       setPathCodigo(pathCodigo);
   }

   /*
    * Métodos
    */

   //Nos aseguramos de que termina en /
   public final void setPathCodigo(String pathCodigo){
       if(pathCodigo == null) throw new NullPointerException();

       if(!pathCodigo.trim().isEmpty() && !pathCodigo.endsWith("/")){
           pathCodigo += "/";
       }
       this.pathCodigo = pathCodigo;
   }

   protected String classPath(){
       //String classPath = pathJar
       //      + ":" + pathLibs + "javacsv.jar"
       //      + ":" + pathLibs + "commons-io-2.4.jar"
       //      + ":" + pathLibs + "jOpenDocument-1.3.jar";
      String pathLibs = pathCodigo + "lib/";
      String pathEjecutable = pathCodigo + "target";
      String pathJar = pathEjecutable + "/hstpminer.jar";
      return pathJar + ":" + pathLibs + "*";
   }

   protected String javaOpts(){
       //Otra configuRacion de javaOpts = " -Xms512m -Xmx3g -XX:MaxHeapSize=1g -XX:-UseGCOverheadLimit ";
       //javaOpts =
       //javaOpts += " -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/home/local/vanesa.graino/hstpminer/outofmemory/ ";
       return " -Xms512m -Xmx" + MAX_RAM + " -XX:-UseGCOverheadLimit -XX:+UseConcMarkSweepGC ";
   }

   protected int[] getArray(int inicio, int paso, int fin){
      int number = (fin-inicio)/paso;
      int[] array = new int[number+1];
      for(int i=inicio, j=0; i<=fin; i+=paso, j++){
         array[j]=i;
      }

      return array;
   }

   public void escribirScripts(ConfScriptBuilder c){


      // Ejecución con BBDD sintéticas:
      // java -cp source.Principal algorithm=[astp,hstp] mode=[basic,episode,seed,full] windowSize=[int] minFreq=[int]
      // inputFileName= inputPath=[String] histogramPath=[String] resultPath=[String]

      try {
         File directorio = new File(c.scriptPath);
         if((!directorio.exists() && !directorio.mkdirs()) || !directorio.isDirectory() ){
             LOGGER.warning("No se ha podido crear el directorio para los scripts en <" + directorio.getPath() + ">");
             return;
         }
         LOGGER.info("Se escriben los scripts en : " + directorio.getAbsolutePath());
         if(!c.skip("apnea")){
            File scriptApnea = new File(directorio, c.scriptApneaName);
            if(scriptApnea.exists() || scriptApnea.createNewFile()){
                BufferedWriter fScript2 = new BufferedWriter(new FileWriter(scriptApnea));
                printApnea(c, fScript2);
                LOGGER.info("Script con BD de Apnea generado correctamente!");
            }else{
                LOGGER.warning("No se ha podido escribir el script de ejecución con la base de datos de Apnea");
            }
         }
         File scriptSinteticas = new File(directorio, c.scriptSinteticasName);
         if(scriptSinteticas.exists() || scriptSinteticas.createNewFile()){
             BufferedWriter fScript = new BufferedWriter(new FileWriter(scriptSinteticas));
             printSinteticas(c, fScript);
             LOGGER.info("Script de bases de datos sintéticas generados correctamente!");
         }else{
             LOGGER.warning("No se ha podido escribir el script de ejecución con las bases de datos sintéticas");
         }

      } catch (IOException e) {
         LOGGER.log(Level.WARNING, "Error al escribir el script", e);
      }
   }




   protected void printSinteticas(ConfScriptBuilder c, BufferedWriter fScript) throws IOException{
      LOGGER.info("Synthetic script");
      fScript.write("#!/bin/bash");
      fScript.newLine();


      switch(estructura){
      case coleccion:
         for(int numBD=0; numBD<c.bases.size()-1; numBD++){
            if(c.skip(numBD)){ continue; }
            for(String alg: c.algs){
               printSintetica(c, fScript, alg, numBD);
               fScript.write("#-----------------------------");
               fScript.newLine();
               fScript.newLine();
            }
            fScript.write("#========================");
            fScript.newLine();
            fScript.newLine();
         }
         break;
      case algoritmo:
         for(String alg: c.algs){
            for(int numBD=0; numBD<c.bases.size()-1; numBD++){
               if(c.skip(numBD)){ continue; }
               printSintetica(c, fScript, alg, numBD);
               fScript.write("#-----------------------------");
               fScript.newLine();
               fScript.newLine();
            }
            fScript.write("#========================");
            fScript.newLine();
            fScript.newLine();
         }
      }

      if(apagarAlFinal){
         fScript.write("apagar-equipo");
         fScript.newLine();
      }

      fScript.flush();
      fScript.close();

   }

   protected String commandoEjecucionSintetica(ConfScriptBuilder c, ConfigSintetica bd,
           String alg, int windowSize, Integer iteration, Integer minFreq){
       return (timeout != null? "timeout " + timeout + " ":"")
               + "java -cp " + classPath() + javaOpts() + " source.Principal algorithm=" + alg
               //+ " mode=" + MODOS[numBD]
               + " mode=" + ConfigSintetica.modeToString(bd.modo)
               + " windowSize=" + windowSize
               //+ " minFreq=" + MIN_FREQ_SINTETICAS//MIN_FREQS[numBD]
               + " minFreq=" + ( minFreq == null ? MIN_FREQ_SINTETICAS : minFreq )
               + ( c.inputFileName != null ? " inputFileName=\"" + c.inputFileName + "\"" : "")
               + ( c.inputFileName == null && bd.fichero != null? " inputFileName=\"" + bd.fichero  + "\"": "" )
               + ( bd.path != null ? " inputPath=\"" + bd.path + "\" " : "")
               + " collection=\"" + bd.nombre + "\" iterations=" + c.iterations
               + " resultPath=\"" + c.pathResultados + bd.nombre + "/\""
               + ( c.writeReport? " writeReport=true" : "" )
               + ( c.writeSingleReport? " writeSingleReport=true" : "")
               + ( c.writeMarkingReport? " writeMarkingReport=true" : "")
               + ( c.writeIterationsReport? " writeIterationsReport=true" : "")
               + ( c.writeTxtReport? " writeTxtReport=true" : "")
               + ( c.appendResults? " appendResults=true" : "")
               + ( c.completa ? "" : " completeResult=false")
               + ( iteration == null? "" : " iteration=" + iteration);
   }

   protected void printSintetica(ConfScriptBuilder c, BufferedWriter fScript,
         String alg, int numBD ) throws IOException{
      ConfigSintetica bd = c.bases.get(numBD);
      if(c.onlyLastWindow){
         fScript.write(commandoEjecucionSintetica(c, bd, alg, bd.windows[bd.windows.length-1], null, null) );
         fScript.newLine();
         fScript.newLine();
      }else{
         for(int windowSize : bd.windows){
            if(windowSize==0){ continue; }
            Integer[] freqs = c.minFreq == null? new Integer[]{null} : c.minFreq;
            for(Integer f : freqs){
               fScript.write(commandoEjecucionSintetica(c, bd, alg, windowSize, null, f) );
               fScript.newLine();
               fScript.newLine();
            }

         }
      }

   }

   protected void printApnea(ConfScriptBuilder c, BufferedWriter fScript2) throws IOException{
      //if(c.skip("apnea")) return; //Ya se hace antes

      int[] windowsApnea = getArray(0, 20, 120);

      fScript2.write("#!/bin/bash");
      fScript2.newLine();

      for(String alg: c.algs){
         for(String modo: c.modosApnea){
            for(int windowSize: (c.onlyLastWindow? new int[]{ windowsApnea[windowsApnea.length-1] } : windowsApnea)){
               if(windowSize==0){ continue; }
               fScript2.write((timeout != null? "timeout " + timeout + " ":"")
                     + "java -cp " + classPath() + javaOpts() + " source.Principal algorithm=" + alg
                     + " mode=" + modo + " windowSize=" + windowSize
                     + " collection=\"apnea\" iterations=" + c.iterations
                     + ( c.inputFileName != null ? " inputFileName=\"" + c.inputFileName + "\"" : "")
                     + " resultPath=\"" + c.pathResultados + "apnea/" + "\" "
                     + ( c.writeReport ? " writeReport=true" : "" )
                     + ( c.minFreqApnea == null ? "" : " minFreq=" + c.minFreqApnea )
               );
               fScript2.newLine();
               fScript2.newLine();
            }
         }
      }
      fScript2.flush();
      fScript2.close();
   }


   /*
    * PARTE ESTÁTICA
    */

   /**
    * @param args
    */
   public static void main(final String[] args) {
      ConfScriptBuilder c = ConfScriptBuilder.confColeccionesArticulo(new String[]{"markt", "astp", "tstp", "hstp"});
      c.minFreq = new Integer[]{100, 200, 300, 400};
      //ConfScriptBuilder c = ConfScriptBuilder.configuracion(new String[]{"apnea"}, new String[]{"markt", "astp", "tstp", "hstp"});
      //ConfScriptBuilder c = ConfScriptBuilder.configuracion(new String[]{}, new String[]{"neg"});


//      c.modosApnea = new String[]{"episode"};
//      int replicas = 50;
//      c.inputFileName = "apnea-replicated" + replicas + ".txt";
//      c.minFreqApnea = 30*replicas;

      //c.skips.put("apnea", true);

      ScriptBuilder builder = new ScriptBuilder(ExecutionParameters.PROJECT_HOME);
      //builder.timeout =  30000;
      builder.escribirScripts(c);

      /*List<String> bases = new ArrayList<String>();
      for(int i=0;i<=49;i++){
         bases.add("bbdartigo-3-" + i);
      }

      ConfScriptBuilder c = ConfScriptBuilder.configuracion(bases.toArray(new String[bases.size()]), new String[]{"markt","astp"});
      c.iterations=1;
      c.onlyLastWindow=true;
      c.writeReport=false;
      c.inputFileName = "Secuencias-Sin-Rellenar.txt";


      ScriptBuilder builder = new ScriptBuilder(ExecutionParameters.PROJECT_HOME);
      builder.timeout =  15000;
      builder.escribirScripts(c);*/
   }
}
