package source.utils;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import source.evento.ColeccionSimple;
import source.evento.Episodio;
import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.SecuenciaSimple;

public class CollectionInfoTest {

   @Test
   public void testEstadisticas(){

      IColeccion coleccion = new ColeccionSimple();
      // Seq #1
      coleccion.add(new SecuenciaSimple(Arrays.asList(
            new Evento("A", 10),
            new Evento("B", 10),
            new Evento("A", 15)
      )));
      // Seq #2
      coleccion.add(new SecuenciaSimple(Arrays.asList(
            new Evento("A", 10),
            new Evento("B", 12),
            new Evento("A", 15),
            new Evento("B", 20)
      )));
      CollectionInfoDTO dto = CollectionInfo.calcularEstadisticas(Arrays.asList("A", "B"), Collections.<Episodio> emptyList(), coleccion);

      double delta = 0.00005;

      Assert.assertEquals("Densidad de eventos incorrecta", (double)7/15, dto.densidadEventos, delta);
      Assert.assertEquals("Densidad de transacciones incorrecta", (double)6/15, dto.densidadTransacciones, delta);
      Assert.assertEquals("Distancia media incorrecta", (double)(5+2+3+5)/4, dto.distanciaMediaTransacciones, delta);
      Assert.assertEquals("Tam medio transacciones falla", (double)(2+5)/6 , dto.tamMedioTransaciones, delta);
      Assert.assertEquals("Numero eventos incorrecto", 7, dto.numEvs);
      Assert.assertEquals("Numero de transacciones falla", 6, dto.numTransacciones);

   }
}
