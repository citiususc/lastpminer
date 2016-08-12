package source.busqueda.episodios;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import source.modelo.IAsociacionTemporal;
import source.modelo.clustering.IClustering;
import source.patron.Patron;
import source.patron.PatronAnotaciones;

public class MineAnotacionesEpisodios extends MineCEDFE{
   private static final Logger LOGGER = Logger.getLogger(MineAnotacionesEpisodios.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   /*
    * Atributos
    */

   int[] patronesIteracion;
   int[] patronesMasEspecificosIteracion;

   {
      patternClassName = "PatronAnotaciones";
   }

   /*
    * Constructores
    */

   public MineAnotacionesEpisodios(String executionId, boolean savePatternInstances,
         boolean saveAllAnnotations, boolean saveRemovedEvents, IClustering clustering, boolean removePatterns){
      super(executionId, savePatternInstances, saveAllAnnotations, saveRemovedEvents, clustering, removePatterns);
   }

   @Override
   protected void iniciarContadores(int tSize, int cSize) {
      super.iniciarContadores(tSize, cSize);
      patronesIteracion = new int[tSize];
      patronesMasEspecificosIteracion = new int[tSize];
   }

   @Override
   protected void notificarModeloGenerado(int tam, int pSize,
         IAsociacionTemporal modelo, String[] mod, boolean buscar,
         List<IAsociacionTemporal> candidatas,
         List<IAsociacionTemporal> candidatasGeneradas,
         Map<String,List<IAsociacionTemporal>> nuevoMapa){
      super.notificarModeloGenerado(tam, pSize, modelo, mod, buscar, candidatas,
            candidatasGeneradas, nuevoMapa);

      for(Patron p:modelo.getPatrones()){
         if(((PatronAnotaciones)p).esMasEspecifico()){
            patronesMasEspecificosIteracion[tam-1]++;
         }
         patronesIteracion[tam-1]++;
      }
   }

   @Override
   public void escribirEstadisticasEstrategia(
         List<List<IAsociacionTemporal>> resultados, Writer fwp,
         boolean shortVersion, int maxIteracion) throws IOException {
      super.escribirEstadisticasEstrategia(resultados, fwp, shortVersion, maxIteracion);
      fwp.write(SEPARADOR);

      fwp.write("\nPatrones generados (patrones más específicos):\n");
      for(int i=2;i<patronesIteracion.length;i++){
         fwp.write(nivel(i) + numberFormat(patronesIteracion[i])
               + "(" + numberFormat(patronesMasEspecificosIteracion[i]) + ")\n");
      }
   }


}
