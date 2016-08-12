package source.configuracion;

import source.modelo.clustering.DensityBasedClustering;
import source.modelo.clustering.IClustering;
import source.modelo.clustering.MountainClustering;

public final class HelperConfiguration {

   private HelperConfiguration(){

   }

   public static void setConfiguration(ConfigurationParameters params, IClustering clustering){
      if(clustering instanceof DensityBasedClustering){
         setConfiguration(params, (DensityBasedClustering)clustering);
      }else if(clustering instanceof MountainClustering){
         setConfiguration(params, (MountainClustering)clustering);
      }

   }


   private static void setConfiguration(ConfigurationParameters params, DensityBasedClustering clustering){
      clustering.setCurrentPercentage(params.currentPercentage);
      clustering.setMaximumPercentage(params.maximumPercentage);
   }

   private static void setConfiguration(ConfigurationParameters params, MountainClustering clustering){
      clustering.setLambda(params.mountainLambda);
   }



}
