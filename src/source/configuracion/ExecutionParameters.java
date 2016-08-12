package source.configuracion;

public final class ExecutionParameters {
   /* Values from properties */
   public static final String PROJECT_HOME;
   public static final String MAX_RAM;

   public static final String CLUSTER_NOTIFICATION_EMAIL;
   public static final String CLUSTER_CODE_PATH;
   public static final String CLUSTER_RESULTS_PATH;
   public static final String CLUSTER_WALLTIME;
   public static final String CLUSTER_MAX_RAM;
   public static final int CLUSTER_NODES;
   public static final int CLUSTER_PPN;

   public static final String PATH_SINTETICAS;
   public static final String PATH_SINTETICAS_NUEVAS;
   public static final String PATH_BASE_RESULTADOS;

   public static final ConfigSintetica[] BBDD;
   static {
      HelperProperties helper = new HelperProperties();
      PROJECT_HOME = helper.getProjectHome();
      PATH_SINTETICAS = helper.getPathSinteticas();
      PATH_SINTETICAS_NUEVAS = helper.getPathSinteticasNuevas();
      PATH_BASE_RESULTADOS = helper.getPathResultados();
      BBDD = helper.getSinteticas();
      MAX_RAM = helper.getMaxRam();

      CLUSTER_NOTIFICATION_EMAIL = helper.getClusterEmail();
      CLUSTER_CODE_PATH = helper.getClusterCodePath();
      CLUSTER_RESULTS_PATH = helper.getClusterResultsPath();
      CLUSTER_NODES = Integer.parseInt(helper.getClusterNodes());
      CLUSTER_PPN = Integer.parseInt(helper.getClusterPPN());
      CLUSTER_WALLTIME = helper.getClusterWalltime();
      CLUSTER_MAX_RAM = helper.getClusterMaxRam();

   }
   /* End of properties part */

   //public static final String REPORT_FILE = PROJECT_HOME + "resources/reports/tempos-de-execucion.ods";
   public static final String REPORTS_PATH = PROJECT_HOME + "resources/reports/";
   public static final String REFERENCIAS_PATH = PROJECT_HOME + "resources/referencias/";


   public static final int indiceBD(String nombre){
      for(int i=0; i<BBDD.length;i++){
         if(BBDD[i].nombre.equals(nombre)) return i;
      }
      return -1;
   }

   private ExecutionParameters() {
      // constructor oculto
   }




}
