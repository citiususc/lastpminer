package source.io.report;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import source.configuracion.ConfigurationParameters;

public class TXTReport {
   private static final Logger LOGGER = Logger.getLogger(TXTReport.class.getName());
   private static final String NOMBRE_BASE = "tiempos";
   private static final String EXTENSION = "txt";

   protected String pathName;
   protected ConfigurationParameters params;

   public TXTReport(String pathName, ConfigurationParameters params) {
      this.pathName = pathName;
      this.params = params;
   }

   public boolean escribirTiempo(int iteracion, long tiempo){
      return escribirTiempo(iteracion==0, tiempo);
   }

   public boolean escribirTiempo(boolean primera, long tiempo){
      File path = new File(pathName);
      if(!path.exists() && (!primera || !path.mkdirs())){
         LOGGER.severe("No existe o no se consigue crear la carpeta <" + pathName + "> para escribir los tiempos");
         return false;
      }
      try {
      File fichero = new File(path, NOMBRE_BASE + "-" + params.getModeString() + "-" + params.getWindowSize() + "-" + params.getAlgorithmString() + "." + EXTENSION );
         if(!fichero.exists() && (!primera|| !fichero.createNewFile())){
            LOGGER.severe("No existe o no se consigue crear la carpeta <" + pathName + "> para escribir los tiempos");
            return false;
         }
         FileWriter writer = new FileWriter(fichero, !primera);
         writer.append(Long.toString(tiempo) + "\n");
         writer.close();
      } catch (IOException e) {
         LOGGER.log(Level.SEVERE, "No se escribe el tiempo", e);
         return false;
      }

      return true;
   }

}
