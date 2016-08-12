package source;

import source.configuracion.ConfigurationParameters;
import source.configuracion.ExecutionParameters;

public class AllThatYouNeedSinteticas extends AllThatYouNeed {

   /**
    * Sólo necesita tener fijado el nombre de la base de datos sintética
    * @param params
    */
   public AllThatYouNeedSinteticas(ConfigurationParameters params){
      this.params = params;
      setBd();
      coleccion = getCollection(params, tipos, episodios, ocurrenciasEpisodios, true);
   }

   public AllThatYouNeedSinteticas(String bd){
      this.params.setCollection(bd);
      setBd();
      coleccion = getCollection(params, tipos, episodios, ocurrenciasEpisodios, true);
   }

   private final void setBd(){
      this.params.setInputPath(ExecutionParameters.PATH_SINTETICAS + this.params.getCollection());
      this.params.setInputFileName("");
      this.params.setResultPath( ExecutionParameters.PROJECT_HOME + "/test/output/" + this.params.getCollection() + "/");
      this.params.setMinFreq(300);
   }
}
