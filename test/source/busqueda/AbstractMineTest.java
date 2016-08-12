package source.busqueda;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import source.modelo.IAsociacionTemporal;


public class AbstractMineTest {
   @Test
   public void testEstadisticas(){
      AbstractMine mine = new Mine("", true, true, null, false);
      RegistroTiempos regTemp = new RegistroTiempos();
      regTemp.acumAsociaciones = 22;
      regTemp.acumCalcula = 23423;
      regTemp.acumCandidatos = 34343;
      regTemp.acumConsistencia = 123;
      regTemp.acumFundir = 256;
      regTemp.acumModelo = 23423;
      regTemp.acumPurgar = 3434;
      regTemp.acumSoporte = 3223333;
      regTemp.tiempoTotal = 322329393;
      mine.registroT = regTemp;
      mine.asociacionesNivel = new long[3];

      mine.patronesNoGeneradosNivel = new long[3];
      mine.maxIteracion = -1;
      //PrintWriter writer = System.console().writer();
      PrintWriter writer = new PrintWriter(System.out);
      try {
         mine.escribirEstadisticas(Collections.<List<IAsociacionTemporal>> emptyList(), writer, false);
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
}
