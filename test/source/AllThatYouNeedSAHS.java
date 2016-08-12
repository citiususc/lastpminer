package source;

import source.configuracion.ConfigurationParameters;
public class AllThatYouNeedSAHS extends AllThatYouNeed {

   public AllThatYouNeedSAHS(boolean soloUltimaSecuencia) {
      params.setSoloUltimaSecuencia(soloUltimaSecuencia);
      params.setResultPath(params.getResultPath() + "apnea/");
      params.setCollection(ConfigurationParameters.APNEA_DB);
      params.setWindowSize(80);
      /*try {
         coleccion = ApneaReader.parseFiles(params, tipos, episodios, ocurrenciasEpisodios);
         coleccion.sort();
      } catch (MalformedFileException mfe) {
         mfe.printStackTrace();
         System.exit(1);
      }*/
      coleccion = getCollection(params, tipos, episodios, ocurrenciasEpisodios, false);
   }

   public AllThatYouNeedSAHS() {
      this(false);
   }
}
