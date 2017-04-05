package source;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import static org.junit.Assert.*;
import source.configuracion.Algorithms;
import source.configuracion.ExecutionParameters;
import source.configuracion.Modes;
import source.evento.ColeccionSimple;
import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.evento.SecuenciaSimple;
import source.modelo.IAsociacionTemporal;
import source.patron.Ocurrencia;
import source.patron.Patron;

public class PrincipalTest {
   public final static boolean PASADO = PrincipalTestGeneral.PASADO;
   public final static String TMP_FOLDER = ExecutionParameters.PROJECT_HOME + "/test/output/";

   private boolean contieneTodos(List<Evento> ventana, List<String> tipos){
      int tSize=tipos.size(), numEncontrados = 0;
      boolean encontrados[] = new boolean[tSize];
      for(int i=0, x=ventana.size(); i<x && numEncontrados<tSize; i++){
         String tipo = ventana.get(i).getTipo();
         int index = tipos.indexOf(tipo);
         if(index==-1 || encontrados[index]) continue; //si la asociaicon no tiene al tipo o ya se habia encontrado seguimos
         //Si llega a aquí la asociacion tiene el tipo y no habia sido encontrado
         encontrados[index]=true;
         numEncontrados++;
      }
      return numEncontrados == tSize;
   }

   private int contadorFrecuenciaModeloSecuencia(ISecuencia secuencia, List<String> tipos, int window){
      int frecuencia = 0;
      int inicio=0;
      for(int fin=0, x=secuencia.size(); fin<x; fin++){
         //Eliminar eventos que quedan fuera de la ventana
         while((secuencia.get(fin).getInstante() - secuencia.get(inicio).getInstante()) > window){
            inicio++;
         }

         if(contieneTodos(secuencia.subList(inicio, fin+1), tipos)){
            frecuencia++;
         }
      }
      return frecuencia;
   }
   private int contadorFrecuenciaModelo(AllThatYouNeed capsula, List<String> tipos){
      return contadorFrecuenciaModelo(capsula.coleccion, tipos, capsula.params.getWindowSize());
   }

   private int contadorFrecuenciaModelo(IColeccion coleccion, List<String> tipos, int window){
      int frecuencia = 0;
      for(ISecuencia secuencia : coleccion){
         frecuencia += contadorFrecuenciaModeloSecuencia(secuencia, tipos, window);
      }
      return frecuencia;
   }

   @Test
   public void contadorFrecuenciaBDRoE15(){
      Assume.assumeFalse(PASADO);
      AllThatYouNeedSinteticas capsula = new AllThatYouNeedSinteticas("BDRoE15");
      capsula.params.setWindowSize(4);
      System.out.println(contadorFrecuenciaModelo(capsula, Arrays.asList("2", "5", "6", "3")));
   }

   @Test
   public void testCalculoFrecuenciaModelo(){
      int window = 3;
      List<String> tipos = Arrays.asList("A","B","C");

      ISecuencia secuencia = new SecuenciaSimple();
      secuencia.add(new Evento("A", 3));
      secuencia.add(new Evento("B", 3));
      secuencia.add(new Evento("C", 3));
      secuencia.add(new Evento("A", 7));
      secuencia.add(new Evento("B", 8));
      secuencia.add(new Evento("C", 9));

      assertEquals("La frecuencia de " + tipos + " debería ser 2 (1)", 2, contadorFrecuenciaModeloSecuencia(secuencia, tipos, window));

      secuencia = new SecuenciaSimple();
      secuencia.add(new Evento("A", 3));
      secuencia.add(new Evento("C", 3));
      secuencia.add(new Evento("A", 7));
      secuencia.add(new Evento("B", 8));
      secuencia.add(new Evento("C", 9));

      assertEquals("La frecuencia de " + tipos + " debería ser 1 (2)", 1, contadorFrecuenciaModeloSecuencia(secuencia, tipos, window));

      secuencia = new SecuenciaSimple();
      secuencia.add(new Evento("A", 3));
      secuencia.add(new Evento("C", 3));
      secuencia.add(new Evento("A", 3));
      secuencia.add(new Evento("C", 3));
      secuencia.add(new Evento("A", 7));
      secuencia.add(new Evento("A", 7));
      secuencia.add(new Evento("B", 8));
      secuencia.add(new Evento("B", 8));
      secuencia.add(new Evento("C", 9));
      secuencia.add(new Evento("C", 9));

      assertEquals("La frecuencia de " + tipos + " debería ser 1 (3)", 1, contadorFrecuenciaModeloSecuencia(secuencia, tipos, window));
   }

   @Test
   public void testContieneTodos(){
      List<Evento> secuencia = new ArrayList<Evento>();
      secuencia.add(new Evento("A", 3));
      secuencia.add(new Evento("A", 3));
      secuencia.add(new Evento("C", 3));
      secuencia.add(new Evento("A", 7));
      //secuencia.add(new Evento("B", 8));
      secuencia.add(new Evento("C", 9));
      secuencia.add(new Evento("B", 15));

      assertTrue("La secuencia " + secuencia + " sí tiene A", contieneTodos(secuencia, Arrays.asList("A")));
      assertTrue("La secuencia " + secuencia + " sí tiene A y B", contieneTodos(secuencia, Arrays.asList("A","B")));
      assertTrue("La secuencia " + secuencia + " sí tiene A,B,C", contieneTodos(secuencia, Arrays.asList("A","B","C")));
      assertFalse("La secuencia " + secuencia + " no tiene A,B,D", contieneTodos(secuencia, Arrays.asList("A","B","D")));
   }



   //Comprueba si un patrón tiene ocurrencias repetidas
   public static boolean ocurrenciasRepetidas(Patron p){
      boolean hayRepes = false;
      Assert.assertNotNull("No se han guardado las instancias del patrón", p.getOcurrencias());
      int total = p.getOcurrencias().size();
      Ocurrencia oc1, oc2;
      int repes = 0;
      for(int i=0;i<total-1;i++){
         oc1=p.getOcurrencias().get(i);
         for(int j=i+1;j<total;j++){
            oc2=p.getOcurrencias().get(j);
            if(oc1.equals(oc2)){
               System.out.println("Ocurrencia repetida: " + i + ", " + j + "(" + oc1 + ")");
               hayRepes = true;
               repes++;
               //break; //TODO borrar para que continue
            }
         }
      }
      System.out.println("El patron " + p + " tiene " + repes + " ocurrencias repetidas");
      return hayRepes;
   }

   public static Patron getPatron(List<List<IAsociacionTemporal>> resultados, int tam, int id){
      for(IAsociacionTemporal asoc : resultados.get(tam-1)){
         for(Patron p:asoc.getPatrones()){
            if(p.getID() == id){
               return p;
            }
         }
      }
      return null;
   }

   public static IAsociacionTemporal getAsociacionNivel(List<IAsociacionTemporal> nivel, String[] modelo){
      List<String> modList = Arrays.asList(modelo);
      for(IAsociacionTemporal asoc : nivel){
         if(Arrays.asList(asoc.getTipos()).equals(modList)){
            return asoc;
         }
      }
      return null;
   }

   public static IAsociacionTemporal getAsociacion(List<List<IAsociacionTemporal>> resultados, String[] modelo){
      int tam = modelo.length;
      return getAsociacionNivel(resultados.get(tam-1), modelo);
   }

   public static boolean ocurrenciaFalta(Patron p, IColeccion coleccion){
      return ocurrenciaFalta(p, p.getOcurrencias(), coleccion);
   }

   public static boolean ocurrenciaFalta(Patron p, List<Ocurrencia> ocurrencias, IColeccion coleccion){
      boolean falta = false;
      int faltantes = 0;
      for(Ocurrencia oc : ocurrencias){
         if(!cumpleRestricciones(p, oc)){
            System.out.println("La ocurrencia no cumple las restricciones!!");
         }
         if(ocurrenciaFalta(p.getTipos(), oc, coleccion.get(oc.getSequenceID()))){
            //System.out.println("Falta la ocurrencia: " + oc + " del patron " + p);
            falta = true; faltantes++;
         }
      }
      if(faltantes>0) System.out.println("Faltan " + faltantes + " del patrón " + p);
      else System.out.println("No faltan ocurrencias del patron");
      return falta;
   }

   public static boolean ocurrenciaFalta(String[] tipos, Ocurrencia oc, ISecuencia secuencia){
      for(int i=0;i<tipos.length;i++){
         Evento ev = new Evento(tipos[i], oc.getEventTimes()[i]);
         if(!secuencia.contains(ev)){
            //System.out.println("La ocurrencia " + oc + " no está en la colección");
            return true;
         }
      }
      return false;
   }

   //método que comprueba que una ocurrencia de patrón cumple las restricciones temporales del mismo
   public static boolean cumpleRestricciones(Patron p, Ocurrencia oc){
      /*for(int i=0;i<p.getTipos().size()-1;i++){
         for(int j=i+1;j<p.getTipos().size();j++){
            String tipo1=p.getTipos().get(i), tipo2=p.getTipos().get(j);
            List<RIntervalo> rests = p.getRestricciones(tipo1, tipo2);
            if(){
               return false;
            }
         }
      }
      return true;*/
      //p.setSavePatternInstances(false);
      return p.representa(-1, oc.getEventTimes(), false);
   }

   public static boolean validarResultados(List<List<IAsociacionTemporal>> resultados,IColeccion coleccionOriginal){
      boolean valido = true;
      for(List<IAsociacionTemporal> nivel : resultados){
         for(IAsociacionTemporal mod : nivel){
            if(mod.getTipos().length<4) continue;
            int total = 0;
            for(Patron p: mod.getPatrones()){
               if(ocurrenciasRepetidas(p)){
                  System.out.println("Hay ocurrencias repetidas de " + mod.getTipos());
                  valido = false;
               }
               /*if(PrincipalTest.ocurrenciaFalta(p,coleccionOriginal)){
                  System.out.println("Faltan ocurrencias de " + mod.getTipos());
                  valido = false;
               }*/
               total += p.getOcurrencias().size();
            }
            if(total != mod.getSoporte()){
               System.out.println("Modelo : " + mod.getTipos() + ". Esperada: " + mod.getSoporte() + ", encontrada: " + total);
               }
            }
         }
         return valido;
      }


   @Test
   public void secuenciatojson(){
      HashMap<String,String> alias = new HashMap<String, String>();
      alias.put("fA", "A");
      alias.put("fD", "B");
      alias.put("fF", "C");
      alias.put("fT", "D");
      alias.put("iA", "E");
      alias.put("iD", "F");
      alias.put("iF", "G");
      alias.put("iT", "H");
      AllThatYouNeedSAHS capsula = new AllThatYouNeedSAHS();
      capsula.coleccion.sort();
      int i=0;
      for(ISecuencia seq : capsula.coleccion){
         System.out.println("Seq #" + i++ + ": " + seq.size());
      }
      ISecuencia seq = capsula.coleccion.get(1);
      System.out.println("Ultimo evento: " + seq.get(seq.size()-1));
      int count = 0;
      StringBuffer bf = new StringBuffer();
      for(Evento e:seq){
         if(e.getInstante()>8600) break;
         if(e.getInstante()<8000) continue;
         count++;
         bf.append("{ 'eventType' : '" + alias.get(e.getTipo()) + "', 'instant': " + e.getInstante() + "}," );
         if(count>5){
            count=0;
            bf.append("\n");
         }
      }
      System.out.println("bf: " + bf);
   }

   @Test
   public void testInstancia(){
      AllThatYouNeed capsula = new AllThatYouNeed();

      capsula.coleccion = new ColeccionSimple();
      capsula.coleccion.add(new SecuenciaSimple(Arrays.asList(
            new Evento("B", 1),
            new Evento("E", 1),
            new Evento("H", 1),

            new Evento("B", 2),
            new Evento("G", 2),
            new Evento("H", 2),
            new Evento("B", 3),
            new Evento("B", 4),
            new Evento("F", 4),

            new Evento("C", 5),
            new Evento("D", 5),
            new Evento("F", 5),
            new Evento("A", 6),
            new Evento("C", 6),
            new Evento("D", 6),

            new Evento("E", 7),
            new Evento("H", 8),
            new Evento("B", 9),
            new Evento("G", 9)
      )));
      capsula.tipos = new ArrayList<String>(Arrays.asList("A","B","C","D","E","F","G","H"));
      capsula.params.setAlgorithm(Algorithms.ALG_ASTP);
      capsula.params.setMode(Modes.MODE_BASIC);
      capsula.params.setWindowSize(30);
      capsula.params.setMinFreq(1);
      capsula.params.setTamMaximoPatron(4);
      capsula.mineria();
   }

   @Test
      public void testCompararFicherosFinal(){
         String referencia = "test/output/referencia.txt";
         String candidato = "test/output/candidato.txt";
         boolean iguales = Principal.compararFicherosFinal(referencia, candidato);
         Assert.assertTrue("Los ficheros sí son iguales", iguales);

         candidato = "test/output/candidato2.txt";
         iguales = Principal.compararFicherosFinal(referencia, candidato);
         Assert.assertFalse("Los ficheros son diferentes", iguales);
      }

}
