package source.io.report;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import source.configuracion.ConfigurationParameters;

public class TXTOneTimeReport {
   private static final Logger LOGGER = Logger.getLogger(TXTOneTimeReport.class.getName());
   private static final String NOMBRE_BASE = "tiempo";
   private static final String EXTENSION = "txt";

   protected String pathName;
   protected ConfigurationParameters params;

   public TXTOneTimeReport(String pathName, ConfigurationParameters params) {
      this.pathName = pathName;
      this.params = params;
   }

   public boolean escribirTiempo(int iteracion, long tiempo){
      File path = new File(pathName);
      if(!path.exists() && !path.mkdirs()){
         LOGGER.severe("No existe o no se consigue crear la carpeta <" + pathName + "> para escribir los tiempos");
         return false;
      }
      try {
      String nombreFichero = NOMBRE_BASE + "-" + params.getModeString() + "-win" + params.getWindowSize() + "-" + params.getAlgorithmString() + "-it" + iteracion + "." + EXTENSION;
      File fichero = new File(path, nombreFichero);
         if(!fichero.exists() && !fichero.createNewFile()){
            LOGGER.severe("No existe o no se consigue crear el fichero <" + nombreFichero + "> para escribir el tiempo de ejecucion");
            return false;
         }
         FileWriter writer = new FileWriter(fichero, false);
         writer.append(Long.toString(tiempo) + "\n");
         writer.close();
      } catch (IOException e) {
         LOGGER.log(Level.SEVERE, "No se escribe el tiempo", e);
         return false;
      }

      return true;
   }


}
