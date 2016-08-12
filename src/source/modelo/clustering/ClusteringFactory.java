package source.modelo.clustering;


import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public final class ClusteringFactory {
   private static final Logger LOGGER = Logger.getLogger(ClusteringFactory.class.getName());
   private static Map<String, IClustering> registered = new HashMap<String, IClustering>();

   private ClusteringFactory() {
   }

   public static void register(String className, IClustering instance){
      registered.put(className, instance);
   }

   private static Class<?> getClusteringClass(String className){
      Class<?> theClass = null;
      try{
         String clusteringClassName = className;
         theClass = Class.forName(clusteringClassName);
      }catch(ClassNotFoundException e){
         LOGGER.log(Level.SEVERE, "Error instanciando el método de clustering, no se encuentra la clase", e);
      }
      return theClass;
   }

   private static IClustering getClusteringInstance(Class<?> clusteringClass){
      IClustering theInstance = null;
      if(clusteringClass != null){
         try {
            theInstance = (IClustering)clusteringClass.newInstance();
         } catch (InstantiationException e) {
            LOGGER.log(Level.SEVERE, "Error instanciando el método de clustering", e);
         } catch (IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "Error instanciando el método de clustering", e);
         } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, "Error instanciando el método de clustering", e);
         } catch (SecurityException e) {
            LOGGER.log(Level.SEVERE, "Error instanciando el método de clustering", e);
         }
      }
      return theInstance;
   }


   public static IClustering getClustering(String className){
      if(registered.containsKey(className)){
         return registered.get(className);
      }
      IClustering clusteringInstance = getClusteringInstance(getClusteringClass(className));
      if(clusteringInstance != null){
         registered.put(className,clusteringInstance);
      }
      return clusteringInstance;
   }

   /*public static IClustering getClustering(ConfigurationParameters params){
      IClustering theInstance = getClustering(params.clusteringClassName);
      HelperConfiguration.setConfiguration(params, theInstance);
      return theInstance;
   }*/

   /*private static Class<?> clusteringClass;
   private static IClustering clusteringInstance;
   private static String clusteringClassName;

   private ClusteringFactory() {
   }

   private static Class<?> getClusteringClass(String className){
      Class<?> theClass = null;
      try{
         String clusteringClassName = className;
         theClass = Class.forName(clusteringClassName);
      }catch(Exception e){
         e.printStackTrace();
      }
      return theClass;
   }

   private static IClustering getClusteringInstance(){
      IClustering theInstance = null;
      if(clusteringClass != null){
         try {
            theInstance = (IClustering)clusteringClass.newInstance();
         } catch (InstantiationException e) {
            e.printStackTrace();
         } catch (IllegalAccessException e) {
            e.printStackTrace();
         }
      }
      return theInstance;
   }

   public static IClustering getClustering(String className){
      if(!className.equals(clusteringClassName)){
         clusteringClass = getClusteringClass(className);
         clusteringInstance = getClusteringInstance();
         clusteringClassName = className;
      }
      return clusteringInstance;
   }*/

}
