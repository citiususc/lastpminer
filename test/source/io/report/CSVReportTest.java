package source.io.report;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.GregorianCalendar;

import org.junit.Test;

import source.configuracion.ExecutionParameters;
import static org.junit.Assert.*;


public class CSVReportTest {
   @Test
   public void test(){
      String inputPath = ExecutionParameters.PROJECT_HOME;
      String reportPath = inputPath + "/output/";
      String reportFileName = "report.csv";
      ReportLine r = new ReportLine("astp", "basic", new GregorianCalendar(), 10, "apnea",
            new Long(34), new Long(33), new Long(32), new Long(31), new Long(30),
            new Long(29), new Long(28), 27, 26, 25, 24, 23, 21);

      try {
         CSVReport.write(new File(reportPath + reportFileName), Arrays.asList( r));
      } catch (IOException e) {
         e.printStackTrace();
         fail(e.getMessage());
      }
   }
}
