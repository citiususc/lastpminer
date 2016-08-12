package source.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public class CollectionInfoDTO {

      protected int numEvs;
      protected int tiempoTotal;
      protected int numTransacciones;
      protected double densidadTransacciones;
      protected double densidadEventos;
      protected double tamMedioTransaciones;
      protected double distanciaMediaTransacciones;
      protected int tiposSize;
      protected int episodiosSize;
      protected int numSecuencias;

      protected double avgDuracionEpisodios;
      protected int maxDuracionEpisodios;

      public CollectionInfoDTO(int numEvs, int tiempoTotal, int numTransacciones, int totalTam,
            int totalDistancias, int totalTransacciones, int tiposSize, int episodiosSize, int numSecuencias,
            double avgDuracionEpisodios, int maxDuracionEpisodios){
         this.numEvs = numEvs;
         this.tiempoTotal = tiempoTotal;
         this.numTransacciones = numTransacciones;
         this.tiposSize = tiposSize;
         this.episodiosSize = episodiosSize;
         this.numSecuencias = numSecuencias;
         this.avgDuracionEpisodios = avgDuracionEpisodios;
         this.maxDuracionEpisodios = maxDuracionEpisodios;
         setDensidadEventos(numEvs, tiempoTotal);
         setDensidadTransacciones(totalTransacciones, tiempoTotal);
         setTamMedioTransacciones(totalTam, numTransacciones);
         setDistanciaMediaTransacciones(totalDistancias, totalTransacciones-numSecuencias);

      }

      public void setDensidadEventos(int numEvs, int tiempoTotal){
         this.densidadEventos = ((double)numEvs)/tiempoTotal;
      }

      public void setDensidadTransacciones(int totalTransacciones, int tiempoTotal){
         this.densidadTransacciones = ((double)numTransacciones)/tiempoTotal;
      }

      public void setTamMedioTransacciones(int totalTam, int numTransacciones){
         this.tamMedioTransaciones = ((double)totalTam)/numTransacciones;
      }

      public void setDistanciaMediaTransacciones(int totalDistancias, int totalTransacciones){
         this.distanciaMediaTransacciones = ((double)totalDistancias)/totalTransacciones;
      }

      public String toString(){
         NumberFormat nf = NumberFormat.getInstance(new Locale("ES"));
         StringBuilder sb = new StringBuilder();
         sb.append("La colección se compone de " + (tiposSize-episodiosSize*2) + " tipos de eventos (sin duración).");
         sb.append("\nCon " + episodiosSize + " tipos de episodios.");
         sb.append("\nConsiste en " + numSecuencias + " secuencias.");
         DecimalFormat df = new DecimalFormat("0.0###", DecimalFormatSymbols.getInstance(new Locale("ES")));
         sb.append("Suma un total de " + nf.format(numEvs) + " eventos. "
               + "\nTiempo total de las secuencias: " + nf.format(tiempoTotal) + " unidades de tiempo."
               + "\nDensidades medias:"
               + "\n\t" + df.format(densidadEventos)  + " eventos/unidad de tiempo."
               + "\n\t" + df.format( densidadTransacciones) + " transacciones/unidad de tiempo."
               + "\nDistancia media entre transacciones real: " + df.format( distanciaMediaTransacciones ) + " unidades de tiempo"
               + "\nTamaño medio de transacciones final: " + df.format( tamMedioTransaciones)
               );
         return sb.toString();
      }
   }
