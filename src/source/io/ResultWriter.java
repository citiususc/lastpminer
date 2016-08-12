package source.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import source.busqueda.AbstractMine;
import source.configuracion.ConfigurationParameters;
import source.modelo.IAsociacionConDistribucion;
import source.modelo.IAsociacionTemporal;

/*
 * Se encarga de generar el fichero de tiempos de ejecución y número de patrones
 * generados en cada iteración del bucle (modo texto plano) - escribirEstadisticas
 *
 * También genera el fichero en el que se guardan los patrones (modo texto plano) -
 * - escribirPatrones
 *
 * Por último, es el encargado de generar los ficheros y scripts usados por gnuplot
 * para construir las gráficas de histogramas - escribirScriptHistogramas.
 * Para lanzar el script de histogramas:
 *
 */
public final class ResultWriter {
   private static final Logger LOGGER = Logger.getLogger(ResultWriter.class.getName());

   private ResultWriter(){

   }

   public static String escribirEstadisticas(List<List<IAsociacionTemporal>> resultados,
         AbstractMine mine, String resultFile, boolean append, boolean shortVersion, Integer iteration){
      FileWriter fwp=null;
      try{
         boolean nuevo = false;
         File f = new File(resultFile);
         if(!f.exists()){
            f.createNewFile();
            nuevo = true;
         }

         fwp = new FileWriter(resultFile,append);
         if(append){
             fwp.write((nuevo ? "" : "\n\n") + "==============================================================================\n\n"
                   + ( iteration != null? "Iteración " + iteration + "\n": ""));
             fwp.write(ConfigurationParameters.FORMATTER.format(new GregorianCalendar().getTime()));
             fwp.write("\n\n==============================================================================\n\n\n");
         }
         mine.escribirEstadisticas(resultados, fwp, shortVersion);
         return resultFile;
      }catch(IOException e){
         LOGGER.log(Level.WARNING, "Error escribiendo el fichero de estadísticas", e);
      }finally{
         if(fwp != null){
            try{
               fwp.close();
            }catch(IOException e){
               LOGGER.log(Level.WARNING, "", e);
            }
         }
      }
      return null;
   }

   public static String escribirEstadisticas(ConfigurationParameters params, List<List<IAsociacionTemporal>> resultados,
         AbstractMine mine, boolean shortVersion, Integer iteration){
      String resultsPath = params.getResultPath();
      File f = new File(resultsPath);
      if(!f.exists()){
         f.mkdirs();
      }
      String base = params.getResultStatisticsFileName();
      return escribirEstadisticas(resultados, mine,  resultsPath + base, params.isAppendResults(), shortVersion, iteration);
   }

   public static String escribirPatrones(List<List<IAsociacionTemporal>> modelos, String resultFile){
      FileWriter fwp = null;
      int i,j;
      List<IAsociacionTemporal> nivel;
      // Elimina el fichero si ya existía
      try{
         //fwp = new FileWriter(ConfigurationParameters.resultPath+ConfigurationParameters.resultPatternsFileName,false);
         fwp = new FileWriter(resultFile, false);
         if(modelos != null){
            for(i=0;i<modelos.size();i++){
               nivel = modelos.get(i);
               if(nivel.isEmpty()){
                  // Puede pasar cuando se reinicia una búsqueda (por ejemplo si los modelos base son de tamaño 2,
                  // no habrá asociaciones de tamaño 1
                  //fwp.write("Tamaño " + i + ": vacío\n"); //si escribimos algo ya no se puede comparar con las referencias
               }else{
                  fwp.write("Tamaño " + nivel.get(0).size() + " - Número de asociaciones frecuentes: " + nivel.size() +"\n");
                  for(j=0;j<nivel.size();j++){
                     fwp.write(nivel.get(j).toStringSinPatrones() + " - frecuencia: " + nivel.get(j).getSoporte()+"\n");
                     fwp.write(nivel.get(j).toString()+"\n");
                  }
               }
            }
         }
         fwp.flush();
         fwp.close();
         return resultFile;
      }catch(IOException e){
         LOGGER.log(Level.WARNING, "Error escribiendo patrones", e);
      }finally{
         if(fwp != null){
            try {
               fwp.close();
            } catch (IOException e){
               LOGGER.log(Level.WARNING, "", e);
            }
         }
      }
      return null;
   }

   /**
    *
    * @param params
    * @param modelos
    * @return el nombre del fichero donde se escriben los patrones
    */
   public static String escribirPatrones(ConfigurationParameters params, List<List<IAsociacionTemporal>> modelos) {
      File f = new File(params.getResultPath());
      if(!f.exists()){
         f.mkdirs();
      }
      // Elimina el fichero si ya existía
      String fileName = params.getResultPath() + params.getResultPatternsFileName();
      return escribirPatrones(modelos, fileName);
   }

   public static String escribirScriptHistogramas(ConfigurationParameters params, List<IAsociacionTemporal> nivel){
      return escribirScriptHistogramas(nivel, params.getHistogramPath() + params.getResultHistogramsFolderName(), params.getWindowSize());
   }

   public static String escribirScriptHistogramas(List<IAsociacionTemporal> nivel, String histogramPath, int ventana){
      File folder = new File(histogramPath);
      if(!folder.exists()){
         folder.mkdirs();
      }

      //int ventana = params.getWindowSize();
      FileWriter fws = null;
      String[] tipos;
      File fileName = new File(folder, "dibuja.sh");//histogramPath+"dibuja.sh";
      try{
         int j;
         int pag=1, mod=10, contador=0;
         fws = new FileWriter(fileName, false);
         fws.write("#!/usr/bin/gnuplot\n");
         fws.write("cd \"" + histogramPath + "\"\n");
         fws.write("set terminal png enhanced size 1024,1280\n");
         fws.write("set output 'histogramas_" + pag + ".png'\n");
         fws.write("set multiplot layout 5,2\n");
         fws.flush();

         //for(i=0;i<nivel.size();i++){
         for(IAsociacionTemporal asoc: nivel){
            tipos = asoc.getTipos();
            if(tipos.length == 2){
               int[] distribucion = ((IAsociacionConDistribucion)asoc).getDistribucion();
               String fichero = "do2_" + tipos[0] + tipos[1] + ".plt";
               String fichero2 = "datos2_" + tipos[0] + tipos[1] + ".plt";

               FileWriter fw = new FileWriter(new File(histogramPath, fichero), false);
               fw.write("set terminal png enhanced size 1024,768\n");
               fw.write("set output 'histogramas_" + tipos[0] + tipos[1] + "'\n");
               //fw.write("set multiplot layout 4,2\n");
               fw.write("set title 'd(" + tipos[0] + "," + tipos[1] + ")'\n");
               //fw.write("plot [-"+ventana+":"+ventana+"] '"+fichero2+"'\n");
               fw.write("plot [-" + ventana + ":" + ventana + "] '" + fichero2 + "' with boxes\n");
               fw.close();

               FileWriter fw2 = new FileWriter(new File(histogramPath, fichero2), false);
               for(j=0; j<2*ventana+1; j++){
                  fw2.write((j-ventana) + " " + distribucion[j]+"\n");
               }
               fw2.close();

               fws.write("set title 'd(" + tipos[0] + "," + tipos[1] + ")'\n");
               fws.write("plot [-" + ventana + ":" + ventana + "] '" + fichero2 + "' with boxes\n");

               contador++;
               if(contador==mod){
                  contador=0;
                  pag++;
                  fws.write("unset multiplot\n");
                  fws.write("set output 'histogramas_" + pag + ".png'\n");
                  fws.write("set multiplot layout 5,2\n");
               }
            }
         }
         fws.write("unset multiplot\n");
         fws.flush();
         fws.close();
         return fileName.getAbsolutePath();
      }catch(IOException e){
         LOGGER.log(Level.WARNING, "Error escribiendo script de histogramas", e);
      }
      return null;
   }

}
