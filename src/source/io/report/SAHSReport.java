package source.io.report;

import java.io.FileWriter;
import java.io.IOException;
import java.util.GregorianCalendar;

import source.configuracion.ConfigurationParameters;

public class SAHSReport {

   private String nombreFichero;

   public SAHSReport(String folder, String algorithmName, String algorithmMode, int window) {
      nombreFichero = folder + "/sahs/" + algorithmName + "-" + algorithmMode + "-w=" + window + ".txt";
   }

   public void escribirTiempoTotal(long tiempoTotal, int iteracion, int iteracionesTotales){
      FileWriter writer = null;
      try {
         writer = new FileWriter(nombreFichero,true);
         if(iteracion == 1){
            writer.write(ConfigurationParameters.FORMATTER.format(new GregorianCalendar().getTime()) + "\n");
            writer.write("Ejecuciones: " + iteracionesTotales + "\n\n");
         }
         writer.write(Long.toString(tiempoTotal) + "\n");
         if(iteracion == (iteracionesTotales-1)){
            writer.write("---------------------\n");
         }
         writer.flush();
      } catch (IOException e) {
         e.printStackTrace();
      } finally {
         try{
            if(writer != null) writer.close();
         }catch(Exception e){}
      }
   }

}
