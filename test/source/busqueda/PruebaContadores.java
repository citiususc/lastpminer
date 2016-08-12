package source.busqueda;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import source.excepciones.FactoryInstantiationException;
import source.modelo.IAsociacionTemporal;
import source.modelo.Modelo;
import source.patron.GeneradorID;
import source.patron.Patron;
import source.patron.PatternFactory;
import source.restriccion.RIntervalo;

public class PruebaContadores  {

//   public PruebaContadores(int tam, AbstractMine mine) {
//      super(tam, mine);
//   }

   // Pattern combination example
   // Miguel's Thesis page 43

   @Test
   public void test() throws FactoryInstantiationException{
      int tam = 4, ventana = 80;
      GeneradorPruebas gen = new GeneradorPruebas(tam);

      List<Patron> patrones = new ArrayList<Patron>();
      patrones.add(new Patron(new String[]{"A","B","C"}, Arrays.asList(new RIntervalo("A","B",12,79),
            new RIntervalo("A","C",-5,2), new RIntervalo("C","B",12,79)), false));
      patrones.add(new Patron(new String[]{"A","B","C"}, Arrays.asList(new RIntervalo("A","B",12,79),
            new RIntervalo("A","C",10,30), new RIntervalo("C","B",12,79)), false));
      IAsociacionTemporal p0 = new Modelo(new String[]{"A","B","C"}, ventana, patrones, 0);
      gen.setPadre(p0, 0);

      patrones = new ArrayList<Patron>();
      patrones.add(new Patron(new String[]{"A","B","D"}, Arrays.asList(new RIntervalo("A","B",33,79),
            new RIntervalo("A","D",8,54), new RIntervalo("D","B",25,71)), false));
      patrones.add(new Patron(new String[]{"A","B","D"}, Arrays.asList(new RIntervalo("A","B",33,79),
            new RIntervalo("A","D",-10,7), new RIntervalo("D","B",25,71)), false));
      IAsociacionTemporal p1 = new Modelo(new String[]{"A","B","D"}, ventana, patrones, 0);
      gen.setPadre(p1, 1);

      patrones = new ArrayList<Patron>();
      patrones.add(new Patron(new String[]{"A","C","D"}, Arrays.asList(new RIntervalo("A","C",-5,2),
            new RIntervalo("A","D",8,79), new RIntervalo("C","D",8,79)), false));
      patrones.add(new Patron(new String[]{"A","C","D"}, Arrays.asList(new RIntervalo("A","C",-5,2),
            new RIntervalo("A","D",8,79), new RIntervalo("C","D",0,7)), false));
      IAsociacionTemporal p2 = new Modelo(new String[]{"A","C","D"}, ventana, patrones, 0);
      gen.setPadre(p2, 2);

      patrones = new ArrayList<Patron>();
      patrones.add(new Patron(new String[]{"B","C","D"}, Arrays.asList(new RIntervalo("C","B",33,79),
            new RIntervalo("C","D",8,54), new RIntervalo("D","B",25,71)), false));
      patrones.add(new Patron(new String[]{"B","C","D"}, Arrays.asList(new RIntervalo("C","B",-20,-10),
            new RIntervalo("C","D",8,54), new RIntervalo("D","B",25,71)), false));
      patrones.add(new Patron(new String[]{"B","C","D"}, Arrays.asList(new RIntervalo("C","B",-79,-29),
            new RIntervalo("C","D",8,54), new RIntervalo("D","B",25,71)), false));
      IAsociacionTemporal p3 = new Modelo(new String[]{"B","C","D"}, ventana, patrones, 0);
      gen.setPadre(p3, 3);

      //Fijar padre, madre
      String[] mod = new String[]{"A","B","C","D"};
      List<Patron> generados = gen.generarPatrones(mod);
      gen.notificarModeloGenerado(tam, generados.size(), null, mod, null, null);

      Assert.assertNotNull(generados);
      Assert.assertTrue("No se han generado patrones", !generados.isEmpty());
      Assert.assertTrue("Se han generado patrone demás", generados.size()==1);

      Patron resultado = new Patron(new String[]{"A","B","C","D"}, Arrays.asList(new RIntervalo("A","C",-5,2),
            new RIntervalo("A","B",33,79), new RIntervalo("A","D",8,54), new RIntervalo("C","B",33,79),
            new RIntervalo("C","D",8,54), new RIntervalo("D","B",25,71)), false);
      Assert.assertEquals(resultado, generados.get(0));

      //Contadores
      System.out.println("patronesPosiblesNivel. " + Arrays.toString(gen.patronesPosiblesNivel));

      System.out.println("patronesDescartadosNivel. " + Arrays.toString(gen.patronesDescartadosNivel));
      System.out.println("patronesNoGeneradosNivel. " + Arrays.toString(gen.patronesNoGeneradosNivel));
      System.out.println("patronesGeneradosNivel. " + Arrays.toString(gen.patronesGeneradosNivel));

      System.out.println("\npatronesGeneradosConAuxiliaresNivel. " + Arrays.toString(gen.patronesGeneradosConAuxiliaresNivel));

      Assert.assertEquals(gen.patronesPosiblesNivel[tam-1], gen.patronesDescartadosNivel[tam-1] + gen.patronesNoGeneradosNivel[tam-1] + gen.patronesGeneradosNivel[tam-1]);

   }

   private class GeneradorPruebas extends GeneradorPatrones {

   long[] patronesPosiblesNivel;
   long[] patronesGeneradosConAuxiliaresNivel;
   long[] patronesDescartadosNivel;
   long[] patronesNoGeneradosNivel;
   long[] patronesGeneradosNivel;
   protected GeneradorID genID = new GeneradorID();

   public GeneradorPruebas(int tam) {
      super(tam, null);
      patronesPosiblesNivel = new long[tam];
      patronesGeneradosConAuxiliaresNivel = new long[tam];
      patronesDescartadosNivel = new long[tam];
      patronesNoGeneradosNivel = new long[tam];
      patronesGeneradosNivel = new long[tam];
   }


   public List<Patron> generarPatrones(String[] mod) throws FactoryInstantiationException {
      List<Patron> patrones = new ArrayList<Patron>();

      int uValido=-1;
      long sumaux=1;
      for(int l=0;l<tam;l++){
         patCache[l] = null;
         patIndex[l] = 0;
         //Cuando hay negación, puede haber modelos sin patrones
         if(patCount[l]>0){ sumaux*=patCount[l]; } //TODO comprobar se esto estropea as contas
         //sumaux*=getPatCount()[l]; //XXX estaba así antes?
      }
      //totalPatrones = sumaux;
      /*mine.*/patronesPosiblesNivel[tam-1]+=sumaux;

      //mine.registroT.tiempoFundir(tam-1, true);
      while(patIndex[0]<patCount[0]){
         uValido = generarPatron(tam, uValido, mod, patrones);
      }
      //mine.registroT.tiempoFundir(tam-1, true);
      return patrones;
   }

   protected int generarPatron(int tam, int uValido, String[] mod,
         List<Patron> patrones) throws FactoryInstantiationException{
      int uValidoOut = uValido;
      if(uValidoOut<=0){
         patCache[0] = PatternFactory.getInstance().getPatternExtension("Patron", mod,
               asocBase[0].getPatron(patIndex[0]), 0);
         /*mine.*/patronesGeneradosConAuxiliaresNivel[tam-1]++;
         uValidoOut = 1;
      }
      boolean act = false;
      int l = uValidoOut;
      //for(int l=uValidoOut;l<tam;l++){
      //for(;l<tam && patCount[l-1]!=0 && patCount[l]!=0;l++){ // Cambiado para que funcione con negación @vanesa
      for(;l<tam && patCount[l-1]!=0;l++){ // Cambiado para que funcione de nuevo @vanesa
         Patron patAux = PatternFactory.getInstance().getPatternClone("Patron",
               patCache[l-1], 0);
         /*mine.*/patronesGeneradosConAuxiliaresNivel[tam-1]++;
         if(patAux.combinar(asocBase[l].getPatron(patIndex[l]),tam-1-l)){
            patCache[l] = patAux;
         }else{
            //No valido: se descarta el patrón
            act = true;
            uValidoOut = notificarPatronDescartado(tam, l, patIndex, patCount);
            break;
         }
      }
      if(!act){
         //uValidoOut = mine.notificarPatronGenerado(tam, patCount, patIndex, patCache, patrones, tam-1);
         uValidoOut = notificarPatronGenerado(tam, patCount, patIndex, patCache, patrones, l-1);// Cambiado para que funcione con negación @vanesa
      }
      return uValidoOut;
   }

   public int notificarPatronDescartado(int tam, int currentIndex, int[] patIndex, int[] patCount){
      patronesDescartadosNivel[tam-1]++;
      patIndex[currentIndex]++;

      if(currentIndex!=tam-1){ // si el índice no era del último modelo que se va a combinar
         int imposibles=1;
         for(int i=currentIndex+1;i<tam;i++){
            if(patCount[i] != 0){
               imposibles *= patCount[i];
            }
         }
         patronesNoGeneradosNivel[tam-1]+=imposibles-1;
      }

      int o;
      for(o=currentIndex;o>=1;o--){
         if(patIndex[o]<patCount[o]){
            break;
         }else{
            patIndex[o]=0;
            patIndex[o-1]++;
         }
      }
      return o;
   }

   public int notificarPatronGenerado(int tam, int[] patCount,
            int[] patIndex, Patron[] patCache, List<Patron> patrones, int indicePatron){
         // Actualizar índices
         patIndex[tam-1]++;
         int l = tam-1;
         for(;l>=1;l--){
            if(patIndex[l]<patCount[l]){
               break;
            }else{
               patIndex[l] = 0;
               patIndex[l-1]++;
            }
         }
         Patron patron = patCache[indicePatron];

         //registroT.tiempoConsistencia(tam-1, true);
         boolean esConsistente = patron.esConsistente(genID);
         //registroT.tiempoConsistencia(tam-1, false);

         if(esConsistente){
            int o=0;
            for(Patron generado : patrones){
               if(generado.equals(patron)){ break; }
               o++;
            }
            if(o==patrones.size()){
               patrones.add(patron);
               //notificarPatronGeneradoConsistente(patron);
            }else{
               // ¿Se da alguna vez?
               //LOGGER.log(Level.WARNING, "Se ha generado un patrón igual a otro generado anteriormente con otros patrones de partida.");
               patronesDescartadosNivel[tam-1]++;
            }
         }else{
            patronesDescartadosNivel[tam-1]++;
         }
         return l;
      }


      protected void notificarModeloGenerado(int tam, int pSize,
            IAsociacionTemporal modelo, String[] mod, List<IAsociacionTemporal> candidatas,
            Map<String,List<IAsociacionTemporal>> nuevoMapa){
   //      asociacionesNivel[tam-1]++;
         patronesGeneradosNivel[tam-1]+= pSize;

   //      candidatas.add(modelo);
   //      for(String tipo : mod){
   //         nuevoMapa.get(tipo).add(modelo);
   //      }
      }
   }
}
