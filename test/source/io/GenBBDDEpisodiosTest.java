package source.io;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import source.AllThatYouNeed;
import source.AllThatYouNeedSinteticas;
import source.configuracion.ConfigSintetica;
import source.configuracion.ExecutionParameters;
import source.evento.ColeccionSimple;
import source.evento.Episodio;
import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.evento.SecuenciaSimple;



public class GenBBDDEpisodiosTest {

   //Comprobar si las bases de datos tienen eventos repetidos
   //Suposicion: las secuencias están ordenadas

   public boolean secuenciaEventosRepetidos(ISecuencia secuencia, int sid){
      secuencia.sort();
      for(int i=0,x=secuencia.size(); i<x-1; i++){
         if(secuencia.get(i).equals(secuencia.get(i+1))){
            System.out.println("Por lo menos el evento  " + secuencia.get(i) + " está repetido en la secuencia #" + sid );
            return true;
         }
      }
      return false;
   }

   public boolean coleccionEventosRepetidos(IColeccion coleccion){
      int sid = 0;
      for(ISecuencia seq: coleccion){
         if(secuenciaEventosRepetidos(seq,sid)){
            return true;
         }
         sid++;
      }
      return false;
   }

   @Test
   public void testEventosRepetidos(){
      //String[] bbdd = { "BD4", "BD5", "BD6", "BD7", "BDR56", "BDR57",
      //		"BDRoE6", "BDRoE9", "BDRoE15"};
      String[] bbdd = {"BDRoE15"};

      for(int i=0, x=bbdd.length; i<x;i++){
         AllThatYouNeedSinteticas capsula = new AllThatYouNeedSinteticas(bbdd[i]);
         if(coleccionEventosRepetidos(capsula.coleccion)){
            System.out.println("La coleccion " + bbdd[i] + " tiene eventos repetidos. ");
         }else{
            System.out.println("La coleccion " + bbdd[i] + " NO tiene eventos repetidos. ");
         }
      }

      AllThatYouNeed capsulaAstp = new AllThatYouNeedSinteticas("BDRoE15");
      capsulaAstp.coleccion = new ColeccionSimple(Arrays.asList((ISecuencia)new SecuenciaSimple(capsulaAstp.coleccion.get(0).subList(0, 100))));
      int index1 = capsulaAstp.coleccion.get(0).indexOf(new Evento("6",24));
      int index2 = capsulaAstp.coleccion.get(0).lastIndexOf(new Evento("6",24));
      System.out.println(index1 + " and " + index2);
   }

   @Test
   public void testNumeroEpisodios(){
      String[] files = new String[]{ "Secuencias.txt", "Secuencias-Sin-Rellenar.txt", "Secuencias-Rellenadas.txt" };
      for(ConfigSintetica cs: ExecutionParameters.BBDD){
         String bbdd = cs.nombre;
         System.out.println("\nBBDD: " + bbdd);
         String folder = ExecutionParameters.PATH_SINTETICAS + bbdd + "/";
         for(String file: files){
            List<String> tipos = new ArrayList<String>();
            List<Episodio> episodios =new ArrayList<Episodio>();
            try {
               BufferedReader dfin = new BufferedReader(new FileReader(folder + file));
               GenBBDDEpisodios.parseFiles(dfin, tipos, episodios);
               System.out.println("\t" + file + ": " + tipos.size() + " tipos.");
            } catch (FileNotFoundException e) {
               //e.printStackTrace();
            }
         }
      }
   }
}
