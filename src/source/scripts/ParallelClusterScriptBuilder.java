package source.scripts;


import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import source.configuracion.ConfigSintetica;
import source.configuracion.ExecutionParameters;


/**
 * Cada ejecución es un script separado para enviar como trabajo al cluster
 * tras la generación de la base de datos.
 * @author Vanesa Graíño Pazos
 *
 */
public class ParallelClusterScriptBuilder extends ScriptBuilder {

    private static final Logger LOGGER = Logger.getLogger(ParallelClusterScriptBuilder.class.getName());

    int indice = 1;

    public ParallelClusterScriptBuilder(String pathCodigo){
        super(pathCodigo);
    }


    /**
     *
     * @author Vanesa
     *
     */
    private class SingleClusterScriptBuilder extends AbstractClusterScriptBuilder{
        ConfScriptBuilder c;
        ConfigSintetica bd;
        String alg;
        int[] windowSizes;


        public SingleClusterScriptBuilder(ConfScriptBuilder c, ConfigSintetica bd,
                String alg, int[] windowSizes){
            this.c = c;
            this.bd = bd;
            this.alg = alg;
            this.windowSizes = windowSizes;
            //this.javaOptions = "-Xms512m -Xmx" + ExecutionParameters.CLUSTER_MAX_RAM + " -XX:-UseGCOverheadLimit -XX:+UseConcMarkSweepGC ";
            this.javaOptions = "-Xmx" + ExecutionParameters.CLUSTER_MAX_RAM;
            this.ppn = windowSizes.length;//ExecutionParameters.CLUSTER_PPN;
            this.numNodes = ExecutionParameters.CLUSTER_NODES;
            this.moduleToLoad = "jdk/1.8.0_25";
            this.nodesExtra = new String[]{"intel","xeonl"};
            //this.ficheroOutput ="fichero-output";
            //this.ficheroError = "fichero-error";
        }

        @Override
        protected void escribirContenido(BufferedWriter fScript) throws IOException {
            for(int win : windowSizes){
               fScript.write(commandoEjecucionSintetica(c, bd, alg, win, null, null) + " &");
               fScript.newLine();
            }
            fScript.write("wait");
            fScript.newLine();
        }

    }

    protected String javaOpts(){
        //Otra configuRacion de javaOpts = " -Xms512m -Xmx3g -XX:MaxHeapSize=1g -XX:-UseGCOverheadLimit ";
        //javaOpts =
        //javaOpts += " -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/home/local/vanesa.graino/hstpminer/outofmemory/ ";
        return "";
    }

    @Override
    protected String classPath(){
        //String classPath = pathJar
        //      + ":" + pathLibs + "javacsv.jar"
        //      + ":" + pathLibs + "commons-io-2.4.jar"
        //      + ":" + pathLibs + "jOpenDocument-1.3.jar";
       String pathLibs = pathCodigo + "lib/";
       String pathEjecutable = pathCodigo;
       String pathJar = pathEjecutable + "hstpminer.jar";
       return pathJar + ":" + pathLibs + "*";
    }

    @Override
    protected void printSinteticas(ConfScriptBuilder c, BufferedWriter fScript) throws IOException{
        for(String alg: c.algs){
           for(int numBD=0; numBD<c.bases.size()-1; numBD++){
              if(c.skip(numBD)){
                  LOGGER.info("Se salta la base de datos " + c.bases.get(numBD).nombre);
                  continue;
              }

              indice=0;
              ConfigSintetica bd = c.bases.get(numBD);
              SingleClusterScriptBuilder single = new SingleClusterScriptBuilder(c,
                       bd, alg, bd.windows);
              single.pathGeneracionScripts= c.scriptPath + "/parallel_cluster/" + bd.nombre;
              single.nombreFichero = "script" + indice;
              single.nombreTrabajo= "MINER-" + bd.nombre + "-" + indice;
              single.email = ExecutionParameters.CLUSTER_NOTIFICATION_EMAIL;
              single.pathEjecucion=ExecutionParameters.CLUSTER_CODE_PATH;
              single.escribirScript();
              indice++;

           }
        }

     }

    @Override
    public void escribirScripts(ConfScriptBuilder c){



       try {
          File directorio = new File(c.scriptPath);
          if((!directorio.exists() && !directorio.mkdirs()) || !directorio.isDirectory() ){
              LOGGER.warning("No se ha podido crear el directorio para los scripts en <" + directorio.getPath() + ">");
              return;
          }
          LOGGER.info("Se escriben los scripts en : " + directorio.getAbsolutePath());

          //TODO: apnea
          /*if(!c.skips.containsKey("apnea") || !c.skips.get("apnea")){
             File scriptApnea = new File(directorio, c.scriptApneaName);
             if(scriptApnea.exists() || scriptApnea.createNewFile()){
                 BufferedWriter fScript2 = new BufferedWriter(new FileWriter(scriptApnea));
                 printApnea(c, fScript2);
                 LOGGER.info("Script con BD de Apnea generado correctamente!");
             }else{
                 LOGGER.warning("No se ha podido escribir el script de ejecución con la base de datos de Apnea");
             }
          }*/
          //File scriptSinteticas = new File(directorio, c.scriptSinteticasName);
          //if(scriptSinteticas.exists() || scriptSinteticas.createNewFile()){
              BufferedWriter fScript = null; //new BufferedWriter(new FileWriter(scriptSinteticas));
              printSinteticas(c, fScript);
              LOGGER.info("Script de bases de datos sintéticas generados correctamente!");
          //}else{
          //    LOGGER.warning("No se ha podido escribir el script de ejecución con las bases de datos sintéticas");
          //}

       } catch (IOException e) {
          LOGGER.log(Level.WARNING, "Error al escribir el script", e);
       }
    }


    /**
     * @param args
     */
    public static void main(final String[] args) {
        String[] algoritmos = new String[]{"tstp"};

        ConfScriptBuilder c;
        if(args != null && args.length>0){
            c = ConfScriptBuilder.configuracionIgnorarTodas(algoritmos);
            ConfigSintetica cs = configSinteticaFromArgs(args);
            c.addColeccion(cs);
            c.skips.put(cs.nombre, false);
        }else{
            c = ConfScriptBuilder.confColeccionesArticulo(algoritmos);
        }
        c.pathResultados = ExecutionParameters.CLUSTER_RESULTS_PATH;
        c.writeSingleReport = true;
        c.writeReport = false;
        c.appendResults = true;
       new ParallelClusterScriptBuilder("").escribirScripts(c);
    }

    private static ConfigSintetica configSinteticaFromArgs(String[] args){
       if(args == null){ return null; }

       ConfigSintetica cs = ConfigSintetica.fromArgs(args, null);

       if(cs == null){
          LOGGER.severe("No se ha encontrado la configuración de la colección en los parámetros");
          throw new RuntimeException();
       }

       LOGGER.info("La colección de los argumentos tiene la configuración: " + cs);
       return cs;
    }


}
