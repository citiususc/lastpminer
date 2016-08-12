package source;

import java.util.List;

import source.configuracion.ConfigurationParameters;
import source.configuracion.ExecutionParameters;
import source.evento.Episodio;
import source.evento.IColeccion;
import source.evento.InstanciaEpisodio;
import source.excepciones.SemillasNoValidasException;
import source.io.ResultWriter;
import source.modelo.IAsociacionTemporal;

/**
 * Clase que encapsula los elementos necesarios para invocar a los algoritmos
 * en cualquiera de sus versiones.
 * @author vanesa.graino
 *
 */
public class AllThatYouNeed extends CapsulaEjecucion{

   public AllThatYouNeed(){
      super();
      this.params = new ConfigurationParameters();
      this.params.setResultPath(ExecutionParameters.PROJECT_HOME + "/test/output/");
   }

   public AllThatYouNeed(IColeccion coleccion, List<String> tipos, List<Episodio> episodios,
         List<InstanciaEpisodio> ocurrenciasEpisodios, String collection){
      this(coleccion, tipos, episodios, ocurrenciasEpisodios);
      this.params.setCollection(collection);
      this.params.setResultPath(this.params.getResultPath() + "test/" + collection + "/");
   }

   public AllThatYouNeed(IColeccion coleccion, List<String> tipos, List<Episodio> episodios,
         List<InstanciaEpisodio> ocurrenciasEpisodios){
      this();
      this.coleccion = coleccion;
      this.tipos = tipos;
      this.episodios = episodios;
      this.ocurrenciasEpisodios = ocurrenciasEpisodios;
   }

   public String mineria(){
      if(coleccion == null){
         throw new NullPointerException("La colección no puede ser nula para llamar al algoritmo.");
      }
      try {
         mineria(0);
         return ResultWriter.escribirPatrones(params, resultados);
      } catch (SemillasNoValidasException e) {
         e.printStackTrace();
      }
      return null;
   }

   public String reiniciarMineria(List<IAsociacionTemporal> modelosBase){
      if(coleccion == null){
         throw new NullPointerException("La colección no puede ser nula para llamar al algoritmo.");
      }
      try {
         reiniciarMineria(0, modelosBase);
         return ResultWriter.escribirPatrones(params, resultados);
      } catch (SemillasNoValidasException e) {
         e.printStackTrace();
      }
      return null;
   }

   public String minarDistribuciones(){
      if(coleccion == null){
         throw new NullPointerException("La colección no puede ser nula para llamar al algoritmo.");
      }
      try {
         distribuciones(false);
         return ResultWriter.escribirScriptHistogramas(params, distribuciones());
      } catch (SemillasNoValidasException e) {
         e.printStackTrace();
      }
      return null;
   }




}
