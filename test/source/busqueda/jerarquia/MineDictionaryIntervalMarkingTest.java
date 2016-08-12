package source.busqueda.jerarquia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

import source.evento.Evento;

public class MineDictionaryIntervalMarkingTest {
   //TODO parametrizar
   private static long lastTmp = -1;

   @Test public void testIntervalo(){
      List<Evento> eventos = new ArrayList<Evento>();
      Map<Evento, List<long[]>> ocurrencias = new HashMap<Evento, List<long[]>>();

      Evento e = new Evento("A",20); eventos.add(e);
      ocurrencias.put(e, Arrays.asList(new long[]{10,20}));

      e = new Evento("A",40); eventos.add(e);
      ocurrencias.put(e, Arrays.asList(new long[]{30,40}));

      e = new Evento("A",60); eventos.add(e);
      ocurrencias.put(e, Arrays.asList(new long[]{50,60}));

      testGestionIntervalos(eventos, ocurrencias, Arrays.asList(new long[]{10,20},new long[]{30,40},new long[]{50,60}), 80);

      e = new Evento("A",70); eventos.add(e);
      ocurrencias.put(e, Arrays.asList(new long[]{55,70}));

      testGestionIntervalos(eventos, ocurrencias, Arrays.asList(new long[]{10,20},new long[]{30,40},new long[]{50,70}), 80);

      e = new Evento("B",70);eventos.add(e);
      ocurrencias.put(e, Arrays.asList(new long[]{25,70}));

      testGestionIntervalos(eventos, ocurrencias, Arrays.asList(new long[]{10,20},new long[]{25,70}), 80);

      e = new Evento("C",70);eventos.add(e);
      ocurrencias.put(e, Arrays.asList(new long[]{9,70}));
      testGestionIntervalos(eventos, ocurrencias, Arrays.asList(new long[]{9,70}), 80);
   }

   @Test public void testGestionIntervalos(){
      List<Evento> eventos = new ArrayList<Evento>();
      Map<Evento, List<long[]>> ocurrencias = new HashMap<Evento, List<long[]>>();
      intervalosEjemplo(eventos, ocurrencias);
      List<long[]> esperados = Arrays.asList(new long[]{56,89});
      testGestionIntervalos(eventos, ocurrencias, esperados,20);
   }

   @Test public void testGestionIntervalos1(){
      List<Evento> eventos = new ArrayList<Evento>();
      Map<Evento, List<long[]>> ocurrencias = new HashMap<Evento, List<long[]>>();
      intervalosEjemplo1(eventos, ocurrencias);
      List<long[]> esperados = Arrays.asList(new long[]{209,234});
      testGestionIntervalos(eventos, ocurrencias, esperados,20);
   }

   @Test public void testGestionIntervalos2(){
      List<Evento> eventos = new ArrayList<Evento>();
      Map<Evento, List<long[]>> ocurrencias = new HashMap<Evento, List<long[]>>();
      intervalosEjemplo2(eventos, ocurrencias);
      List<long[]> esperados = Arrays.asList(new long[]{209,289});
      testGestionIntervalos(eventos, ocurrencias, esperados, 20);
   }

   public static void testGestionIntervalos(List<Evento> eventos, Map<Evento, List<long[]>> ocurrencias, List<long[]> esperados, int win){
      //List<Evento> eventos = new ArrayList<Evento>();
      //Map<Evento, List<int[]>> ocurrencias = new HashMap<Evento, List<int[]>>();
      //intervalosEjemplo(eventos, ocurrencias);
      List<long[]> intervalosActivos = new ArrayList<long[]>();
      List<Evento> copiaEventos = new ArrayList<Evento>(eventos);
      ListIterator<Evento> it = copiaEventos.listIterator();
      Evento bv = it.next();


      for(Evento e: eventos){
         List<long[]> ocurrenciasE = ocurrencias.get(e);
         if(ocurrenciasE != null){
            for(long[] ocurrencia : ocurrenciasE){
               addIntervalo(e.getInstante(), ocurrencia, intervalosActivos);
            }
         }
         bv = purgarEventosIntervalosNoActivos(e, intervalosActivos, win, bv, it);
      }
      printIntervalos(intervalosActivos);

      //assertEquals(esperados,intervalosActivos);
      assertEquals(esperados.size(), intervalosActivos.size());
      for(int i=0;i<esperados.size();i++){
         assertArrayEquals(esperados.get(i), intervalosActivos.get(i));
      }
   }

   private static void printIntervalos(List<long[]> intervalosActivos){
      StringBuffer sb = new StringBuffer("[");
      for(long[] a : intervalosActivos){
         sb.append(Arrays.toString(a) + ", ");
      }
      sb=sb.deleteCharAt(sb.length()-1).deleteCharAt(sb.length()-1).append("]");
      System.out.println(sb);
   }

   public static void intervalosSAHS(List<Evento> eventos, Map<Evento, List<int[]>> ocurrencias){

   }

   public static void intervalosEjemplo(List<Evento> eventos, Map<Evento, List<long[]>> ocurrencias){
      lastTmp = -1;
      Evento e = new Evento("3",64); eventos.add(e);
      ocurrencias.put(e, Arrays.asList(new long[]{56,64}));

      e = new Evento("16", 65);eventos.add(e);
      ocurrencias.put(e, Arrays.asList(new long[]{56,65}));

      e = new Evento("14", 68);eventos.add(e);
      ocurrencias.put(e, Arrays.asList(new long[]{56,68}));

      e = new Evento("13", 79);eventos.add(e);
      ocurrencias.put(e, Arrays.asList(new long[]{61,79}));

      e = new Evento("17", 86);eventos.add(e);
      ocurrencias.put(e, Arrays.asList(new long[]{68,86}));

      e = new Evento("7", 89);eventos.add(e);
      ocurrencias.put(e, Arrays.asList(new long[]{79,89}));
   }

   public static void intervalosEjemplo1(List<Evento> eventos, Map<Evento, List<long[]>> ocurrencias){
      intervalosEjemplo(eventos, ocurrencias);

      Evento e = new Evento("11",113);eventos.add(e);
      e = new Evento("5",113);eventos.add(e);

      e = new Evento("18", 118);eventos.add(e);
      ocurrencias.put(e, Arrays.asList(new long[]{113,118}));

      e = new Evento("15", 136); eventos.add(e);
      e = new Evento("4", 195); eventos.add(e);
      e = new Evento("8", 209); eventos.add(e);
      e = new Evento("9", 215); eventos.add(e);
      e = new Evento("4", 218); eventos.add(e);
      e = new Evento("16", 219); eventos.add(e);
      ocurrencias.put(e, Arrays.asList(new long[]{209, 219}));

      e = new Evento("14", 221); eventos.add(e);
      ocurrencias.put(e, Arrays.asList(new long[]{209, 221}));

      e = new Evento("3", 221); eventos.add(e);
      ocurrencias.put(e, Arrays.asList(new long[]{209, 221}));

      e = new Evento("13", 228); eventos.add(e);
      ocurrencias.put(e, Arrays.asList(new long[]{209, 228}));

      e = new Evento("8", 233); eventos.add(e);
      e = new Evento("3", 234); eventos.add(e);
      ocurrencias.put(e, Arrays.asList(new long[]{218, 234}));
   }

   public static void intervalosEjemplo2(List<Evento> eventos, Map<Evento, List<long[]>> ocurrencias){
      intervalosEjemplo1(eventos, ocurrencias);

      Evento e = new Evento("16", 235); eventos.add(e);
      ocurrencias.put(e, Arrays.asList(new long[]{218, 235}));

      e = new Evento("17", 240); eventos.add(e);
      ocurrencias.put(e, Arrays.asList(new long[]{221, 240}));

      e = new Evento("7", 240); eventos.add(e);
      ocurrencias.put(e, Arrays.asList(new long[]{221, 240}));

      e = new Evento("14", 241); eventos.add(e);
      ocurrencias.put(e, Arrays.asList(new long[]{233, 241}));

      e = new Evento("9", 241); eventos.add(e);
      ocurrencias.put(e, Arrays.asList(new long[]{233, 241}));

      e = new Evento("4", 250); eventos.add(e);
      ocurrencias.put(e, Arrays.asList(new long[]{240, 250}));

      e = new Evento("13", 253); eventos.add(e);
      ocurrencias.put(e, Arrays.asList(new long[]{234, 253}));

      e = new Evento("7", 255); eventos.add(e);
      ocurrencias.put(e, Arrays.asList(new long[]{241, 255}));

      e = new Evento("5", 259); eventos.add(e);
      ocurrencias.put(e, Arrays.asList(new long[]{240, 259}));

      e = new Evento("7", 265); eventos.add(e);
      ocurrencias.put(e, Arrays.asList(new long[]{250, 265}));

      e = new Evento("8", 265); eventos.add(e);
      ocurrencias.put(e, Arrays.asList(new long[]{250, 265}));

      e = new Evento("11", 268); eventos.add(e);
      ocurrencias.put(e, Arrays.asList(new long[]{250, 268}));

      e = new Evento("3", 274); eventos.add(e);
      ocurrencias.put(e, Arrays.asList(new long[]{259, 274}));

      e = new Evento("9", 274); eventos.add(e);
      ocurrencias.put(e, Arrays.asList(new long[]{259, 274}));

      e = new Evento("18", 275); eventos.add(e);
      ocurrencias.put(e, Arrays.asList(new long[]{259, 275}));

      e = new Evento("15", 278); eventos.add(e);
      ocurrencias.put(e, Arrays.asList(new long[]{259, 278}));

      e = new Evento("16", 279); eventos.add(e);
      ocurrencias.put(e, Arrays.asList(new long[]{265, 279}));

      e = new Evento("14", 283); eventos.add(e);
      ocurrencias.put(e, Arrays.asList(new long[]{265, 283}));

      e = new Evento("18", 287); eventos.add(e);
      ocurrencias.put(e, Arrays.asList(new long[]{268, 287}));

      e = new Evento("5", 288); eventos.add(e);
      ocurrencias.put(e, Arrays.asList(new long[]{274, 288}));

      e = new Evento("11", 289); eventos.add(e);
      ocurrencias.put(e, Arrays.asList(new long[]{274, 289}));

   }

   public static void intervalosEjemplo1(List<long[]> intervalos, List<Integer> tiempos){
      long[][] a ={

            //Nuevo evento: (7,294)
            /*{275, 294},
            //Nuevo evento: (17,295)
            {278, 295},
            //Nuevo evento: (13,296)
            {278, 296},
            //Nuevo evento: (5,313)
            {294, 313},
            //Nuevo evento: (15,314)
            {295, 314},
            //Nuevo evento: (11,319)
            //Nuevo evento: (18,327)
            {313, 327},
            //Nuevo evento: (4,327)
            {313, 327},
            //Nuevo evento: (15,341)
            {327, 341},
            //Nuevo evento: (8,342)
            {327, 342},
            //Nuevo evento: (9,350)
            {341, 350},
            //Nuevo evento: (3,353)
            {341, 353},
            //Nuevo evento: (16,354)
            {341, 354},
            //Nuevo evento: (14,361)
            {342, 361},
            //Nuevo evento: (4,365)
            //Nuevo evento: (13,371)
            {353, 371},
            //Nuevo evento: (7,373)
            {354, 373},
            //Nuevo evento: (17,374)
            {361, 374},
            //Nuevo evento: (8,381)
            {365, 381},
            //Nuevo evento: (9,388)
            //Nuevo evento: (3,390)
            {381, 390},
            //Nuevo evento: (16,397)
            {381, 397},
            //Nuevo evento: (14,398)
            {381, 398},
            //Nuevo evento: (5,400)
            {381, 400},
            //Nuevo evento: (11,406)
            {388, 406},
            //Nuevo evento: (18,407)
            {388, 407},
            //Nuevo evento: (17,412)
            {397, 412},
            //Nuevo evento: (13,414)
            {397, 414},
            //Nuevo evento: (7,414)
            {397, 414},
            //Nuevo evento: (15,423)
            {406, 423},
            //Nuevo evento: (11,428)
            {412, 428},
            //Nuevo evento: (5,440)
            //Nuevo evento: (18,444)
            {428, 444},
            //Nuevo evento: (15,463)
            //Nuevo evento: (4,466)
            //Nuevo evento: (8,474)
            {463, 474},
            //Nuevo evento: (16,481)
            {463, 481},
            //Nuevo evento: (14,486)
            {474, 486},
            //Nuevo evento: (9,489)
            {474, 489},
            //Nuevo evento: (3,491)
            {474, 491},
            //Nuevo evento: (13,498)
            {481, 498},
            //Nuevo evento: (4,500)
            {491, 500},
            //Nuevo evento: (8,508)
            //Nuevo evento: (17,510)
            {491, 510},
            //Nuevo evento: (7,510)
            {491, 510},
            //Nuevo evento: (4,511)
            {498, 511},
            //Nuevo evento: (8,518)
            {500, 518},
            //Nuevo evento: (9,520)
            //Nuevo evento: (14,523)
            {508, 523},
            //Nuevo evento: (3,525)
            {508, 525},
            //Nuevo evento: (16,526)
            {508, 526},
            //Nuevo evento: (14,527)
            {508, 527},
            //Nuevo evento: (16,529)
            {511, 529},
            //Nuevo evento: (11,530)
            {511, 530},
            //Nuevo evento: (5,531)
            {518, 531},
            //Nuevo evento: (9,532)
            {518, 532},
            //Nuevo evento: (13,535)
            {518, 535},
            //Nuevo evento: (3,536)
            {518, 536},
            //Nuevo evento: (13,539)
            {520, 539},
            //Nuevo evento: (7,541)
            {523, 541},
            //Nuevo evento: (18,545)
            {526, 545},
            //Nuevo evento: (17,546)
            {527, 546},
            //Nuevo evento: (7,552)
            {535, 552},
            //Nuevo evento: (17,554)
            {535, 554},
            */
      };
      intervalos.addAll(Arrays.asList(a));

   }

   protected static void addIntervalo(long tmp, long[] intervaloActual, List<long[]> intervalosActivos){

      // 'intervaloActual' contiene el intervalo más grande de eventos incluidos en una ocurrencia
      // Recorrer la lista de intervalos activos

      boolean algunoActualizado=false;
      long[] intervalo;
      int x=intervalosActivos.size(), i=x-1;
      for(;i>=0;i--){
         intervalo = intervalosActivos.get(i);
         //Solapa el inicio del nuevo intervalo con un intervalo existente: situación más habitual
         if((intervaloActual[0]>=intervalo[0]) && (intervaloActual[0]<=intervalo[1])){
            // Actualizar 'intervalo' para que incluya 'intervaloActual'
            intervalo[1]=tmp;
            algunoActualizado=true;
            break;
         }
         if(intervaloActual[0]>intervalo[1]){ break; }
      }
      for(int j=x-1; j>i;j--){ intervalosActivos.remove(j); }
      if(!algunoActualizado){
         // Ningún intervalo fue actualizado, añadir intervalo actual
         intervaloActual[1] = tmp;
         long[] aux = new long[]{intervaloActual[0], tmp};
         intervalosActivos.add(aux);
         intervaloActual = new long[2];
      }

   }

   public static Evento purgarEventosIntervalosNoActivos(Evento evento, List<long[]> intervalosActivos,
         int windowSize, Evento bv, ListIterator<Evento> inicioVentana){
      long tmp = evento.getInstante();
      if(lastTmp==tmp) return bv;
      //if(ultimaIteracion) return 0;
      // Avanzar el comienzo de la ventana, y eliminar aquellos eventos que
      // no están en un intervalo de eventos utilizados
      for(;bv.getInstante()<= tmp - windowSize - 1;){
         // Comprobar si 'bv' pertenece a algún intervalo activo
         boolean estaActivo=false;
         for(long[] intervalo : intervalosActivos){
            long instante = bv.getInstante();
            if((instante>=intervalo[0]) && (instante<=intervalo[1])){
               estaActivo=true;
               break;
            }
         }
         if(!estaActivo){
            System.out.println("Borrando evento : " + bv);
            inicioVentana.remove();
         }
         bv = inicioVentana.next();
      }

      long inicioVentanaTmp = bv.getInstante();
      Iterator<long[]> iteradorIntervalosActivos = intervalosActivos.iterator();
      while(iteradorIntervalosActivos.hasNext()){
         long[] intervalo = iteradorIntervalosActivos.next();
         if(intervalo[1]<inicioVentanaTmp){
            iteradorIntervalosActivos.remove();
         }
         //TODO si los intervalos están ordenados se puede hacer este break
         else if(intervalo[1]>inicioVentanaTmp) break;
      }
      lastTmp = tmp;//TODO el uso de lastTmp es correcto ???
      return bv;
   }
}

