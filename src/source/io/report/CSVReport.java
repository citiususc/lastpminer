package source.io.report;
import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import source.configuracion.ConfigurationParameters;
import source.configuracion.ExecutionParameters;
import source.modelo.IAsociacionTemporal;

public final class CSVReport {
   private static final Logger LOGGER = Logger.getLogger(CSVReport.class.getName());

   private static final char DELIM = ';';

   private static final String[] COLUMNAS = {"algoritmo", "modo", "hora", "numEjecucion", "coleccion",
      "calculoGeneracionCandidatos" , "calculoSoporte", "calculoConsistencia", "calculoCombinacion", "calculoAsociacion",
      "calculoModelo", "calculoTotal", "totalSecuenciasGeneradas", "totalSecuenciasFrecuentes", "totalPatronesFrecuentes",
      "totalPatronesGenerados", "totalPatronesInconsistentes", "totalPatronesRechazados"  };

   private CSVReport(){

   }

   public static List<ReportLine> read(Integer maxEntries, String workflow, File file ) {
      try {
         CsvReader log = new CsvReader(file.getAbsolutePath());
         List<ReportLine> entries = new ArrayList<ReportLine>();
         log.readHeaders();
         int lidas = -1;
         while(log.readRecord() && (maxEntries == null || lidas++ < maxEntries)){
            entries.add(parsearLinea(log));
         }
         log.close();

         return entries;

      } catch (FileNotFoundException fnfe) {
         LOGGER.log(Level.WARNING, "No se encontró el fichero CSV", fnfe);
      } catch (IOException ioe) {
         LOGGER.log(Level.WARNING, "Excepción al escribir las estadísticas en el fichero", ioe);
      }
      return null;
   }

   private static ReportLine parsearLinea(final CsvReader reader){
      ReportLine linea = null;
      //new ReportLine(log.get(CASEID_HEADER),log.get(TASKID_HEADER));
      //TODO procesar línea
      return linea;
   }

   public static void write(ConfigurationParameters params, List<List<IAsociacionTemporal>> resultados, long[] tiempos, long[][]estadisticas){
      String fileName = ExecutionParameters.PROJECT_HOME + params.getReportFileName();
      write(fileName, params, resultados, tiempos, estadisticas);
   }

   public static void write(String fileName,  ConfigurationParameters params, List<List<IAsociacionTemporal>> resultados, long[] tiempos, long[][]estadisticas){
      try {
         write(new File(fileName), Arrays.asList(reportLineFromResult(params,  resultados, tiempos, estadisticas)));
      } catch (IOException e) {
         LOGGER.log(Level.WARNING, "Excepción al escribir las estadísticas en el fichero", e);
      }
   }

   public static synchronized void write(File file, List<ReportLine> lines) throws IOException{
      boolean alreadyExists = file.exists();
      Writer fileWriter = new FileWriter(file,true);
      CsvWriter writer = new CsvWriter(fileWriter, DELIM);

      //Si el fichero no existe, crearlo y escribir las cabeceras

      if (!alreadyExists) {
         for(String columna: COLUMNAS){
            writer.write(columna);
         }
         writer.endRecord();
      }

      // escribir en fichero todas las lineas
      for(ReportLine linea: lines){
         writer.writeRecord(procesarLinea(linea));
      }
      writer.flush();
      writer.close();
   }

   private static String[] procesarLinea(ReportLine linea){
      return linea.toStringArray();
   }

   public static ReportLine reportLineFromResult(ConfigurationParameters params, List<List<IAsociacionTemporal>> resultados, long[] tiempos, long[][]estadisticas){
      ReportLine r = new ReportLine();
      //set fields
      r.setColeccion(params.getCollection());
      r.setAlgoritmo(params.getAlgorithmString());
      r.setModo(params.getModeString());
      r.setHora(new GregorianCalendar());
      r.setCalculos(tiempos[0], tiempos[1], tiempos[2], tiempos[3], tiempos[4], tiempos[5], tiempos[8]);
      //TODO totales
      //r.setTotales(totalSecuenciasGeneradas, totalSecuenciasFrecuentes, totalPatronesFrecuentes, totalPatronesGenerados, totalPatronesInconsistentes, totalPatronesRechazados);

      return r;
   }



}
