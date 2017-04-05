package source.configuracion;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;


/*
 * Esta clase contiene los diferentes parámetros de configuración para los algoritmos
 * Número de iteraciones/veces a ejecutar el algoritmo
 * Tamaño de ventana
 * Frecuencia mínima
 * Paths y nombres de ficheros de entrada y de resultado
 * Porcentajes para los algoritmos de clustering
 */
public class ConfigurationParameters {
   private static final Logger LOGGER = Logger.getLogger(ConfigurationParameters.class.getName());

   public static final String APNEA_DB = "apnea";
   public static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,S",Locale.FRANCE);

   public static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();

   /*
    * Non-static part
    */

   protected Algorithms algorithm = Algorithms.ALG_ASTP;
   protected Modes mode = Modes.MODE_BASIC;
   protected boolean onlyDistributions;
   protected boolean validate = true;
   protected boolean writeReport;
   protected boolean writeSingleReport;
   protected boolean writeMarkingReport;
   protected boolean writeIterationsReport;
   protected boolean writeCSVReport;
   protected boolean writeApneaReport;
   protected boolean writeTxtReport;
   protected Date initTime = new GregorianCalendar().getTime();

   //protected boolean appendTimeStamp; //Incluir un timestamp al final del nombre de los ficheros de patrones y estadísticas
   protected boolean appendResults; //Si cada estadística se incluye en un fichero aparte o se añade al final de un fichero

   protected String executionId = "proba1";
   protected int iterations = 1;
   /**
    * Empieza en 0
    */
   protected int iteration = -1;
   protected int windowSize = 80; //60-600(60);1200-3600(600);7200-28800(3600)
   protected int minFreq = 30;
   protected int tamMaximoPatron = -1;

   protected double currentPercentage = 0.25;
   protected double maximumPercentage = 0.45;
   protected double mountainLambda = 0.9;

   protected String clusteringClassName = "source.modelo.clustering.DensityBasedClustering";
   //protected String clusteringClassName = "source.modelo.clustering.MountainClustering";
   //protected String clusteringClassName = "source.modelo.clustering.DominantPointClustering";

   protected boolean completeResult = true;
   protected boolean soloUltimaSecuencia;


   protected String inputFileName = "";//"resources/apnea4-166.txt-corrected";
   protected String inputPath = "";//PROJECT_HOME;

   protected String reportFileName = "report.csv";

   protected String resultPath = "";
   protected String histogramPath = "";

   protected String collection = "apnea"; //APNEA_DB;

   protected boolean savePatternInstances;
   protected boolean saveRemovedEvents;
   protected boolean saveAllAnnotations;
   protected boolean onlyOnePatternOccurrenceEachEvent;

   private String resultStatisticsFileName = "";
   private String resultPatternsFileName = "";
   private String resultHistogramsFolderName = "";

   /*
    * Constructores
    */
   public ConfigurationParameters(){
      //constructor vacío
   }

   public ConfigurationParameters(ConfigSintetica sintetica){
      this.mode = sintetica.modo;
      this.collection = sintetica.nombre;
   }

   /*
    * Methods
    */


   public boolean isCompleteResult() {
      return completeResult;
   }


   public void setCompleteResult(boolean completeResult) {
      this.completeResult = completeResult;
   }


   public String getHistogramPath(){
      return "".equals(histogramPath) ? getResultPath() : histogramPath;
   }

   public String getResultPath(){
      if("".equals(resultPath)){
         return inputPath + "/output/"
               + windowSize + "-"
               + getAlgorithmString() + "-"
               + getModeString() + "/";
      }
      return resultPath;
   }

   public String getResultHistogramsFolderName(){
      if("".equals(resultHistogramsFolderName)){
         return "hists--" + getExecutionIdentifier();
      }
      return resultHistogramsFolderName;
   }

   public String getResultPatternsFileName(){
      if("".equals(resultPatternsFileName)){
         return "patrones-regs4-166--" + getExecutionIdentifier() + ".txt";
      }
      return resultPatternsFileName;
   }

   public String getResultStatisticsFileName(){
      if("".equals(resultStatisticsFileName)){
         return "regs4-166--" + getExecutionIdentifier() + ".txt";
      }
      return resultStatisticsFileName;
   }


   public String getExecutionIdentifier(boolean appendTimeStamp){
      return "WIN-" + windowSize
               + "--ALG-" + getAlgorithmString()
               + "--MODE-" + getModeString()
               + "--FREQ-" + minFreq
               + "--CP-" + currentPercentage
               + "--MP-" + maximumPercentage
               + (tamMaximoPatron==-1? "" : "--MAXP-" + tamMaximoPatron)
               + (completeResult? "" : "--INC")
               + (appendTimeStamp?  "-" + FORMATTER.format(initTime) : "");
   }

   public String getExecutionIdentifier(){
      boolean appendTimeStamp = !appendResults; //podría convertirse en algo más complejo
      return getExecutionIdentifier(appendTimeStamp);
   }

   public String getAlgorithmString(){
      for(Algorithms alg : Algorithms.values()){
         if(algorithm == alg){
            return alg.name().substring(4);
         }
      }
      throw new RuntimeException("El algoritmo " + algorithm + " no se ha especificado");
   }

   public String getModeString(){
      return ConfigSintetica.modeToString(mode);
   }



   @Override
   public String toString() {
      StringBuilder buffer = new StringBuilder(50);
      buffer.append(super.toString())
         .append("\n\talgorithm=" + getAlgorithmString() + "\n\tmode=" + getModeString());
      Field[] fields = ConfigurationParameters.class.getDeclaredFields();

      int searchMods = 0x0 | Modifier.FINAL | Modifier.STATIC;

      for(Field f: fields){

         if((f.getModifiers() & searchMods) != searchMods ){
            if(f.getName().equals("algorithm") || f.getName().equals("mode") ) continue;
            f.setAccessible(true);
            try {
               buffer.append("\n\t" + f.getName()  + "=" + f.get(this));
            } catch ( IllegalArgumentException e) {
               LOGGER.log(Level.WARNING, "Error procesando los parámetro de configuración", e);
            } catch ( IllegalAccessException e) {
               LOGGER.log(Level.WARNING, "Error procesando los parámetro de configuración", e);
            }
         }
      }

      //Número hilos
      buffer.append("\n\tNUM_THREADS=" + NUM_THREADS );

      return buffer.toString();
   }

   public void procesarParametros(String[] args){
      if(args == null){ return; }
      for(String arg: args){
         String fieldName = arg.substring(0, arg.indexOf('='));
         LOGGER.finer("Nombre del atributo: " + fieldName);

         if(fieldName.equalsIgnoreCase("algorithm")){
            String algorithmIn = arg.substring("algorithm=".length());
            LOGGER.fine("Algorithm: " + algorithmIn);
            this.algorithm = Algorithms.valueOf("ALG_" + algorithmIn.toUpperCase(Locale.FRANCE));

         } else if(fieldName.equalsIgnoreCase("mode")){
            String modeIn = arg.substring("mode=".length());
            LOGGER.fine("Mode: " + modeIn);
            this.mode = Modes.valueOf("MODE_" + modeIn.toUpperCase(Locale.FRANCE));
         } else {

            try {

               Field field = ConfigurationParameters.class.getDeclaredField(fieldName);
               field.setAccessible(true);
               String valorString = arg.substring(fieldName.length() + 1);

               LOGGER.finer("Type: " + field.getType());
               Class<?> fieldClass = field.getType();

               if(fieldClass == String.class){
                  LOGGER.finer("Valor: " + valorString);
                  field.set(this, valorString);
               }else if(fieldClass == int.class || fieldClass == Integer.class){
                  Integer valor = Integer.valueOf(valorString);
                  LOGGER.finer("Valor: " + valor);
                  field.set(this, valor);
               }else if(fieldClass == double.class || fieldClass == Double.class){
                  Double valor = Double.valueOf(valorString);
                  LOGGER.finer("Valor: " + valor);
                  field.set(this, valor);
               }else if(fieldClass == boolean.class || fieldClass == Boolean.class){
                  Boolean valor = Boolean.valueOf(arg.substring(fieldName.length() + 1));
                  LOGGER.finer("Valor: " + valor);
                  field.set(this, valor);
               }
            } catch ( NoSuchFieldException e) {
               LOGGER.log(Level.WARNING, "Nombre de atributo no válido: " + fieldName, e);
            } catch ( SecurityException  e) {
               LOGGER.log(Level.WARNING, "Error procesando los parámetro de configuración", e);
            } catch ( IllegalAccessException e) {
               LOGGER.log(Level.WARNING, "Error procesando los parámetro de configuración", e);
            }
         }
      }
      //LOGGER.info("Params: " + this);
   }

   public Algorithms getAlgorithm() {
      return algorithm;
   }

   public Modes getMode() {
      return mode;
   }

   public boolean isOnlyDistributions() {
      return onlyDistributions;
   }

   public boolean isValidate() {
      return validate;
   }

   public boolean isWriteTxtReport() {
      return writeTxtReport;
   }

   public boolean isAppendResults(){
       return appendResults;
   }

   public String getExecutionId() {
      return executionId;
   }

   public int getIteration(){
      return iteration;
   }

   public int getIterations() {
      return iterations;
   }

   public int getWindowSize() {
      return windowSize;
   }

   public int getMinFreq() {
      return minFreq;
   }

   public int getTamMaximoPatron() {
      return tamMaximoPatron;
   }

   public double getCurrentPercentage() {
      return currentPercentage;
   }

   public double getMaximumPercentage() {
      return maximumPercentage;
   }

   public double getMountainLambda() {
      return mountainLambda;
   }

   public String getClusteringClassName() {
      return clusteringClassName;
   }

   public boolean isSoloUltimaSecuencia() {
      return soloUltimaSecuencia;
   }

   public String getInputFileName() {
      if(inputFileName.isEmpty()){
         if(esApnea()){
            return "apnea4-166.txt-corrected";
         }
         return "Secuencias.txt";
      }
      return inputFileName;
   }

   public String getInputPath(String projectHome, String pathSinteticas) {
      if(inputPath.isEmpty()){
         if(esApnea()){
            return projectHome + "resources/";
         }
         return pathSinteticas + "/" + collection + "/";
      }
      return inputPath;
   }

   public String getReportFileName() {
      return reportFileName;
   }

   /**
    *
    * @return - Nombre de la colección
    */
   public String getCollection() {
      return collection;
   }

   public boolean isSavePatternInstances() {
      return savePatternInstances;
   }

   public boolean isSaveRemovedEvents() {
      return saveRemovedEvents;
   }

   public boolean isSaveAllAnnotations() {
      return saveAllAnnotations;
   }

   public boolean isOnlyOnePatternOccurrenceEachEvent() {
      return onlyOnePatternOccurrenceEachEvent;
   }

   public void setResultPath(String resultPath) {
      this.resultPath = resultPath;
   }

   public void setHistogramPath(String histogramPath) {
      this.histogramPath = histogramPath;
   }

   public void setCollection(String collection) {
      this.collection = collection;
   }

   public void setAlgorithm(Algorithms algorithm) {
      this.algorithm = algorithm;
   }

   public void setMode(Modes mode) {
      this.mode = mode;
   }

   public void setExecutionId(String executionId) {
      this.executionId = executionId;
   }

   public void setIteration(int iteration){
      this.iteration = iteration;
   }

   public void setIterations(int iterations) {
      this.iterations = iterations;
   }

   public void setMaximumPercentage(double maximumPercentage) {
      this.maximumPercentage = maximumPercentage;
   }

   public void setClusteringClassName(String clusteringClassName) {
      this.clusteringClassName = clusteringClassName;
   }

   public void setInputFileName(String inputFileName) {
      this.inputFileName = inputFileName;
   }

   public void setInputPath(String inputPath) {
      this.inputPath = inputPath;
   }

   public void setOnlyDistributions(boolean onlyDistributions) {
      this.onlyDistributions = onlyDistributions;
   }

   public void setValidate(boolean validate) {
      this.validate = validate;
   }

   public void setWriteApneaReport(boolean writeApneaReport){
      this.writeApneaReport = writeApneaReport;
   }

   public void setWriteReport(boolean writeReport) {
      this.writeReport = writeReport;
   }

   public void setWriteTxtReport(boolean writeTxtReport) {
      this.writeTxtReport = writeTxtReport;
   }

   public void setWriteSingleReport(boolean writeSingleReport){
      this.writeSingleReport = writeSingleReport;
   }

   public void setWriteMarkingReport(boolean writeMarkingReport) {
      this.writeMarkingReport = writeMarkingReport;
   }

   public void setWriteIterationsReport(boolean writeIterationsReport) {
      this.writeIterationsReport = writeIterationsReport;
   }

   public void setWriteCSVReport(boolean writeCSVReport) {
      this.writeCSVReport = writeCSVReport;
   }

   public void setAppendResults(boolean appendResults){
       this.appendResults = appendResults;
   }

   public void setWindowSize(int windowSize) {
      this.windowSize = windowSize;
   }

   public void setMinFreq(int minFreq) {
      this.minFreq = minFreq;
   }

   public void setTamMaximoPatron(int tamMaximoPatron) {
      this.tamMaximoPatron = tamMaximoPatron;
   }

   public void setCurrentPercentage(double currentPercentage) {
      this.currentPercentage = currentPercentage;
   }

   public void setMountainLambda(double mountainLambda) {
      this.mountainLambda = mountainLambda;
   }

   public void setSoloUltimaSecuencia(boolean soloUltimaSecuencia) {
      this.soloUltimaSecuencia = soloUltimaSecuencia;
   }

   public void setReportFileName(String reportFileName) {
      this.reportFileName = reportFileName;
   }

   public void setSavePatternInstances(boolean savePatternInstances) {
      this.savePatternInstances = savePatternInstances;
   }

   public void setSaveRemovedEvents(boolean saveRemovedEvents) {
      this.saveRemovedEvents = saveRemovedEvents;
   }

   public void setSaveAllAnnotations(boolean saveAllAnnotations) {
      this.saveAllAnnotations = saveAllAnnotations;
   }

   public void setOnlyOnePatternOccurrenceEachEvent(
         boolean onlyOnePatternOccurrenceEachEvent) {
      this.onlyOnePatternOccurrenceEachEvent = onlyOnePatternOccurrenceEachEvent;
   }

   public void setResultStatisticsFileName(String resultStatisticsFileName) {
      this.resultStatisticsFileName = resultStatisticsFileName;
   }

   public void setResultPatternsFileName(String resultPatternsFileName) {
      this.resultPatternsFileName = resultPatternsFileName;
   }

   public boolean esApnea(){
      return collection.equalsIgnoreCase(APNEA_DB);
      //return !"".equals(params.getInputFileName())
      //      && !"Secuencias.txt".equals(params.getInputFileName())
      //      && !"Secuencias-Sin-Rellenar.txt".equals(params.getInputFileName());
   }

   public ConfigurationParameters clonar(){
      ConfigurationParameters params = new ConfigurationParameters();
      params.algorithm = this.algorithm;
      params.clusteringClassName = this.clusteringClassName;
      params.collection = this.collection;
      params.currentPercentage = this.currentPercentage;
      params.executionId= this.executionId;
      params.histogramPath = this.histogramPath;
      params.inputFileName = this.inputFileName;
      params.inputPath = this.inputPath;
      params.iterations = this.iterations;
      params.iteration = this.iteration;
      params.maximumPercentage = this.maximumPercentage;
      params.minFreq = this.minFreq;
      params.mode = this.mode;
      params.mountainLambda = this.mountainLambda;
      params.onlyDistributions = this.onlyDistributions;
      params.onlyOnePatternOccurrenceEachEvent = this.onlyOnePatternOccurrenceEachEvent;
      params.reportFileName = this.reportFileName;
      params.resultPath = this.resultPath;
      params.resultPatternsFileName = this.resultPatternsFileName;
      params.resultStatisticsFileName = this.resultStatisticsFileName;
      params.saveAllAnnotations = this.saveAllAnnotations;
      params.savePatternInstances = this.savePatternInstances;
      params.saveRemovedEvents = this.saveRemovedEvents;
      params.soloUltimaSecuencia = this.soloUltimaSecuencia;
      params.tamMaximoPatron = this.tamMaximoPatron;
      params.validate = this.validate;
      params.windowSize = this.windowSize;
      params.writeCSVReport = this.writeCSVReport;
      params.writeIterationsReport = this.writeIterationsReport;
      params.writeMarkingReport = this.writeMarkingReport;
      params.writeReport = this.writeReport;
      params.writeApneaReport = this.writeApneaReport;
      params.writeSingleReport = this.writeSingleReport;
      params.writeTxtReport = this.writeTxtReport;
      params.completeResult = this.completeResult;
      return params;
   }

}