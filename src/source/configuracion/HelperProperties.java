package source.configuracion;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;


public class HelperProperties {
   private static final Logger LOGGER = Logger.getLogger(HelperProperties.class.getName());

   private static final String PROP_PROJECT_HOME = "project.home";
   private static final String PROP_PATH_SINTETICAS = "sinteticas.path";
   private static final String PROP_PATH_SINTETICAS_NUEVAS = "sinteticas.nuevas.path";
   private static final String PROP_PATH_RESULTADOS = "resultados.path";
   private static final String PROP_IDS_SINTETICAS = "sinteticas.ids";
   private static final String PROP_MAX_RAM = "max.ram";

   private static final String PROP_CLUSTER_EMAIL = "cluster.email";
   private static final String PROP_CLUSTER_CODE_PATH = "cluster.code.path";
   private static final String PROP_CLUSTER_RESULTS_PATH = "cluster.results.path";
   private static final String PROP_CLUSTER_NODES = "cluster.nodes";
   private static final String PROP_CLUSTER_PPN = "cluster.ppn";
   private static final String PROP_CLUSTER_WALLTIME = "cluster.walltime";
   private static final String PROP_CLUSTER_MAX_RAM = "cluster.max.ram";

   private static final String PROP_SINTETICA_NUEVA = "sinteticas.{0}.esNueva";
   private static final String PROP_SINTETICA_MODO = "sinteticas.{0}.modo";
   private static final String PROP_SINTETICA_VENTANA = "sinteticas.{0}.ventana";
   private static final String PROP_SINTETICA_FICHERO = "sinteticas.{0}.fichero";
   private static final String PROP_SINTETICA_PATH = "sinteticas.{0}.path";


   /*
    * Atributos
    */

   PropertiesConfiguration config = new PropertiesConfiguration();

   /*
    * Constructores
    */

   public HelperProperties(File file){
      LOGGER.info("Absolut path for configuration properties file: " + file.getAbsolutePath());
      try {
         config.load(file);
      } catch (ConfigurationException e) {
         LOGGER.log(Level.SEVERE, "Imposible to initialize HelperProperties", e);
         //System.exit(1);
      }
   }

   public HelperProperties(InputStream input){
      try {
         config.load(input);
      } catch (ConfigurationException e) {
         LOGGER.log(Level.SEVERE, "Imposible to initialize HelperProperties", e);
         //System.exit(1);
      }
   }


   public HelperProperties(){
      try {
         File f = new File("src/config","config.properties");
         if(f.exists()){
            config.load(f);
         }else{
            config.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("config/config.properties"));
         }
      } catch (ConfigurationException e) {
         LOGGER.log(Level.SEVERE, "Unable to initialize HelperProperties", e);
         //System.exit(1);
      }
      //this(new File("resources","config.properties"));
      //this(HelperProperties.class.getResourceAsStream("config/config.properties"));
      //this(Thread.currentThread().getContextClassLoader().getResourceAsStream("config/config.properties"));
   }

   /*
    * Métodos
    */

   public String getMaxRam(){
      if(config.containsKey(PROP_MAX_RAM)){
         return config.getString(PROP_MAX_RAM);
      }
      return "";
   }

   public String getClusterEmail(){
      if(config.containsKey(PROP_CLUSTER_EMAIL)){
         return config.getString(PROP_CLUSTER_EMAIL);
      }
      return "";
   }

   public String getClusterCodePath(){
      if(config.containsKey(PROP_CLUSTER_CODE_PATH)){
         return config.getString(PROP_CLUSTER_CODE_PATH);
      }
      return "";
   }

   public String getClusterResultsPath(){
      if(config.containsKey(PROP_CLUSTER_RESULTS_PATH)){
         return config.getString(PROP_CLUSTER_RESULTS_PATH);
      }
      return "";
   }

   public String getClusterNodes(){
      if(config.containsKey(PROP_CLUSTER_NODES)){
         return config.getString(PROP_CLUSTER_NODES);
      }
      return "";
   }

   public String getClusterPPN(){
      if(config.containsKey(PROP_CLUSTER_PPN)){
         return config.getString(PROP_CLUSTER_PPN);
      }
      return "";
   }

   public String getClusterWalltime(){
      if(config.containsKey(PROP_CLUSTER_WALLTIME)){
         return config.getString(PROP_CLUSTER_WALLTIME);
      }
      return "";
   }

   public String getClusterMaxRam(){
      if(config.containsKey(PROP_CLUSTER_MAX_RAM)){
         return config.getString(PROP_CLUSTER_MAX_RAM);
      }
      return "";
   }

   public String getProjectHome(){
      if(config.containsKey(PROP_PROJECT_HOME)){
         return config.getString(PROP_PROJECT_HOME);
      }
      return "";
   }

   public String getPathSinteticas(){
      if(config.containsKey(PROP_PATH_SINTETICAS)){
         return config.getString(PROP_PATH_SINTETICAS);
      }
      return "";
   }

   public String getPathResultados(){
      if(config.containsKey(PROP_PATH_RESULTADOS)){
         return config.getString(PROP_PATH_RESULTADOS);
      }
      return "";
   }

   public String getPathSinteticasNuevas(){
      if(config.containsKey(PROP_PATH_SINTETICAS_NUEVAS)){
         return config.getString(PROP_PATH_SINTETICAS_NUEVAS);
      }
      return "";
   }

   public ConfigSintetica[] getSinteticas(){
      if(config.containsKey(PROP_IDS_SINTETICAS)){
         String[] nombres = config.getStringArray(PROP_IDS_SINTETICAS);

         List<ConfigSintetica> BBDD = new ArrayList<ConfigSintetica>(nombres.length);
         for(String nombre : nombres){
            ConfigSintetica s = getSintetica(nombre);
            if(s!=null){
               BBDD.add(s);
            }
         }

         return (ConfigSintetica[]) BBDD.toArray(new ConfigSintetica[BBDD.size()]);
      }
      return new ConfigSintetica[]{};
   }

   private ConfigSintetica getSintetica(String nombre){
      LOGGER.finer("Procesando coleccion: " + nombre);

      String strNueva = config.getString(new String(PROP_SINTETICA_NUEVA).replace("{0}", nombre));
      LOGGER.finer("Nueva: " + strNueva);

      String strModo = config.getString(new String(PROP_SINTETICA_MODO).replace("{0}", nombre));
      LOGGER.finer("Modo: " + strModo);

      String[] strsVentana = config.getStringArray(new String(PROP_SINTETICA_VENTANA).replace("{0}", nombre));
      LOGGER.finer("Ventanas: " + Arrays.toString(strsVentana));

      String strFichero = config.getString(new String(PROP_SINTETICA_FICHERO).replace("{0}", nombre), null);
      String strPath = config.getString(new String(PROP_SINTETICA_PATH).replace("{0}", nombre), null);
      LOGGER.finer("Fichero: " + strPath + strFichero);

      try {
         return new ConfigSintetica(nombre, Boolean.parseBoolean(strNueva), Modes.valueOf("MODE_" + strModo),
               getArray(Integer.parseInt(strsVentana[0]), Integer.parseInt(strsVentana[1]),
                     Integer.parseInt(strsVentana[2])), strPath, strFichero);
      } catch (NumberFormatException e) {
         LOGGER.log(Level.WARNING, "Falla para la base de datos " + nombre, e);
      } catch (IllegalArgumentException e) {
         LOGGER.log(Level.WARNING, "Falla para la base de datos " + nombre, e);
      } catch (SecurityException e) {
         LOGGER.log(Level.WARNING, "Falla para la base de datos " + nombre, e);
      }

      return null;
   }


   public static int[] getArray(int inicio, int paso, int fin){
      int number = (fin-inicio)/paso;
      int[] array = new int[number+1];
      for(int i=inicio, j=0; i<=fin; i+=paso, j++){
         array[j]=i;
      }

      return array;
   }

   /*public static final ConfigSintetica[] BBDD = {
      //Antiguas
      new ConfigSintetica("BD4", false, MODE_EPISODE, getArray(20, 20, 120)),
      new ConfigSintetica("BD5", false, MODE_EPISODE, getArray(20, 20, 80)),
      new ConfigSintetica("BD6", false, MODE_EPISODE, getArray(20, 20,  80)),
      new ConfigSintetica("BD7", false, MODE_EPISODE, getArray(20, 20, 80)),

      new ConfigSintetica("BDR56", false, MODE_EPISODE, getArray(10, 10,  80)),
      new ConfigSintetica("BDR57", false, MODE_EPISODE, getArray(10, 10, 80)),

      new ConfigSintetica("BDRoE6", false, MODE_BASIC, getArray(10, 10,  80)),
      new ConfigSintetica("BDRoE9", false, MODE_BASIC, getArray(5, 5, 40)),
      new ConfigSintetica("BDRoE11", false, MODE_BASIC, getArray(5,  5,  20)),
      new ConfigSintetica("BDRoE15", false, MODE_BASIC, getArray(2,  2, 10)),

      new ConfigSintetica("BD4-1", true, MODE_EPISODE, getArray(20, 20, 80)),
      new ConfigSintetica("BD7-8", true, MODE_EPISODE, getArray(20, 20, 80)),
      new ConfigSintetica("BDRoE101-1", true, MODE_BASIC, getArray(5, 5, 40)),
      new ConfigSintetica("BDRoE102-1", true, MODE_BASIC, getArray(5, 5, 40)),

      new ConfigSintetica("BDR4-10", true, MODE_EPISODE, getArray(20,20,80)),
      new ConfigSintetica("BDR4-14", true, MODE_EPISODE, getArray(20,20,80)),
      new ConfigSintetica("BDR5-11", true, MODE_EPISODE, getArray(20,20,80)),
      new ConfigSintetica("BDR6-4", true, MODE_EPISODE, getArray(20,20,80)),
      new ConfigSintetica("BDR7-15", true, MODE_EPISODE, getArray(20,20,80)),
      new ConfigSintetica("BDR7-16", true, MODE_EPISODE, getArray(20,20,80)),

      new ConfigSintetica("BDRoG1-2", true, MODE_EPISODE, getArray(20,20,80)),
      new ConfigSintetica("BDRoG1-7", true, MODE_EPISODE, getArray(20,20,80)),

      new ConfigSintetica("BDRoG2-100", true, MODE_EPISODE, getArray(5,5,40)),

      new ConfigSintetica("BDR56r", false, MODE_EPISODE, getArray(20,20,80)),
      new ConfigSintetica("BDRoE9r", false, MODE_BASIC, getArray(10,10,40)),

      new ConfigSintetica("BDR58-11", true, MODE_EPISODE, getArray(20,20,80)),
      new ConfigSintetica("BDR60-1", true, MODE_EPISODE, getArray(20,20,80)),

      new ConfigSintetica(APNEA_DB, false, MODE_BASIC, getArray(20, 20, 120)) //sahs
   };*/

}
