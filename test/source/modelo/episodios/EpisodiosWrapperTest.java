package source.modelo.episodios;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import source.evento.Episodio;
import source.evento.Evento;


public class EpisodiosWrapperTest {
   @Test
   public void testSiguientesIndices(){
      System.out.println("\n============================\nCon solapamiento\n============================\n");
      int ventana = 5;
      List<Evento> secuencia = new ArrayList<Evento>();
      secuencia.add(new Evento("A",11));
      secuencia.add(new Evento("A",12));
      secuencia.add(new Evento("B",13));
      secuencia.add(new Evento("B",14));
      secuencia.add(new Evento("C",15));
      secuencia.add(new Evento("C",16));

      List<int[]> instanciasEsperadas = new ArrayList<int[]>(Arrays.asList(
            new int[]{11,13,15}, new int[]{12,14,15},
            new int[]{12,14,16}
      ));

      analizarSecuencia(secuencia, instanciasEsperadas, ventana);
   }

   @Test
   public void testSiguientesIndicesEpisodioLargo(){
      System.out.println("\n============================\nCon solapamiento\n============================\n");
      int ventana = 5;
      List<Evento> secuencia = new ArrayList<Evento>();
      secuencia.add(new Evento("A",1));
      secuencia.add(new Evento("A",12));
      secuencia.add(new Evento("B",13));
      secuencia.add(new Evento("B",14));
      secuencia.add(new Evento("C",15));
      secuencia.add(new Evento("C",16));

      List<int[]> instanciasEsperadas = new ArrayList<int[]>(Arrays.asList(
            new int[]{12,14,15}, new int[]{12,14,16}
      ));

      analizarSecuencia(secuencia, instanciasEsperadas, ventana);
   }

   @Test
   public void testSiguientesIndicesSinSolapamento(){
      System.out.println("\n============================\nSin solapamiento\n============================\n");
      int ventana = 5;
      List<Evento> secuencia = new ArrayList<Evento>();
      secuencia.add(new Evento("A",1));
      secuencia.add(new Evento("B",12));
      secuencia.add(new Evento("A",13));
      secuencia.add(new Evento("B",14));
      secuencia.add(new Evento("C",15));
      secuencia.add(new Evento("C",16));

      List<int[]> instanciasEsperadas = new ArrayList<int[]>(Arrays.asList(
            new int[]{13,14,15}, new int[]{13,14,16}
      ));

      analizarSecuencia(secuencia, instanciasEsperadas, ventana);

   }


   public List<int[]> analizarSecuencia(List<Evento> secuencia, List<int[]> instanciasEsperadas, int ventana){

      List<Episodio> episodios = new ArrayList<Episodio>(Arrays.asList(new Episodio("A","B")));
      String[] tipos = new String[]{"A","B","C"};
      int tSize = tipos.length;

      int[][] abiertas = new int[tSize][ventana];
      int[] tam = new int[tSize];
      int[][] limites = new int[tSize][2];
      int[] instancia;

      EpisodiosWrapper wrapper = new EpisodiosWrapper(episodios, tipos);
      List<int[]> instancias = new ArrayList<int[]>();

      for(Evento ev : secuencia){
         System.out.println("\nEvento: " + ev);
         String tipo = ev.getTipo();
         int tmp = ev.getInstante();
         int index = Arrays.asList(wrapper.getTiposReordenados()).indexOf(tipo);
         if(wrapper.actualizarVentana(abiertas, limites, tam, ventana, index, tmp, 0, 0)){

            int[] indices = wrapper.primerosIndices(index, tam);
            if(indices == null){
               System.out.println("nulos");
            }
            while(indices!=null){
               instancia = new int[tSize];
               System.out.println(Arrays.toString(indices));
               wrapper.fijarInstancia(tSize, index, tmp, abiertas, limites, indices, instancia, ventana);
               System.out.println("Instancia: " + Arrays.toString(instancia));
               instancias.add(instancia);

               indices = wrapper.siguienteCombinacion(tam, indices, index, tipo);
            }
         }
      }

      //Comparar con instancias esperadas
      Assert.assertEquals("Diferente número de instancias al esperado", instanciasEsperadas.size(), instancias.size());

      for(int i=0;i<instanciasEsperadas.size();i++){
         Assert.assertArrayEquals("Falla la instancia con indice " + i, instanciasEsperadas.get(i), instancias.get(i));
      }

      return instancias;
   }

   private class EpisodiosWrapperOriginal extends EpisodiosWrapper {
      public EpisodiosWrapperOriginal(List<Episodio> episodios, String[] tipos){
         super(episodios, tipos);
      }
      @Override
      protected final void organizarTipos(String[] tipos){
         eventosDeEpisodios = 0;
         int tSize = tipos.length;

         //repReordenados = new int[tSize];
         equivalenciasTipos = new int[tSize];
         if(episodios == null || episodios.isEmpty()){
            //tiposReordenados.addAll(Arrays.asList(tipos));
            tiposReordenados = tipos.clone();
            for(int i=0;i<tSize;i++){
               equivalenciasTipos[i]=i;
            }
            return;
         }
         List<String> tiposReordenados = new ArrayList<String>(tSize);
         int insertados=0;
         // Insertar los tipos contenidos en el listado de episodios
         for(Episodio episodio : episodios){
            // Comprobar si contiene ambos tipos de eventos
            //if(!tipos.contains(episodio.getTipoInicio()) || !tipos.contains(episodio.getTipoFin())){
            if(Arrays.binarySearch(tipos, episodio.getTipoInicio())<0
                  || Arrays.binarySearch(tipos, episodio.getTipoFin()) < 0){
               // Ignorar el episodio, puesto que no contiene ambos tipos
               episodiosCompletos = false;
               continue;
            }
            eventosDeEpisodios+=2;
            String tipo = episodio.getTipoInicio();
            int index = Arrays.binarySearch(tipos, tipo); //tipos.indexOf(tipo);
            equivalenciasTipos[insertados]=index;
            //repReordenados[insertados]=rep[index];
            tiposReordenados.add(tipo);
            insertados++;
            tipo = episodio.getTipoFin();
            index = Arrays.binarySearch(tipos, tipo); //tipos.indexOf(tipo);
            equivalenciasTipos[insertados]=index;
            //repReordenados[insertados]=rep[index];
            //tiposReordenados.set(insertados,tipo);
            tiposReordenados.add(tipo);
            insertados++;
         }
         // Insertar el resto de tipos
         int index=0;
         for(String tipo : tipos){
            if(!tiposReordenados.contains(tipo)){
               equivalenciasTipos[insertados]=index;
               //repReordenados[insertados]=rep[index];
               //tiposReordenados.set(insertados,tipo);
               tiposReordenados.add(tipo);
               insertados++;
            }
            index++;
         }
         this.tiposReordenados = tiposReordenados.toArray(new String[tiposReordenados.size()]);
      }
   }

   private class EpisodiosWrapperCopia extends EpisodiosWrapper{

      public EpisodiosWrapperCopia(List<Episodio> episodios, String[] tipos) {
         super(episodios, tipos);
      }

      protected final void organizarTipos(String[] tipos){
         eventosDeEpisodios = 0;
         int tSize = tipos.length;

         //repReordenados = new int[tSize];
         equivalenciasTipos = new int[tSize];
         if(episodios == null || episodios.isEmpty()){
            //tiposReordenados.addAll(Arrays.asList(tipos));
            tiposReordenados = tipos.clone();
            for(int i=0;i<tSize;i++){
               equivalenciasTipos[i]=i;
            }
            return;
         }

         tiposReordenados = new String[tSize];
         int insertados = 0, indiceOrd = 0;
         // Insertar los tipos contenidos en el listado de episodios
         for(Episodio episodio : episodios){
            // Comprobar si contiene ambos tipos de eventos
            //if(!tipos.contains(episodio.getTipoInicio()) || !tipos.contains(episodio.getTipoFin())){
            String tipoInicio = episodio.getTipoInicio();
            int indexInicio = Arrays.binarySearch(tipos, tipoInicio); //tipos.indexOf(tipo);
            if(indexInicio < 0){
               // Ignorar el episodio, puesto que no contiene ambos tipos
               episodiosCompletos = false;
               continue;
            }
            String tipoFin = episodio.getTipoFin();
            int indexFin = Arrays.binarySearch(tipos, tipoFin); //tipos.indexOf(tipo);
            if(indexFin < 0){
               // Ignorar el episodio, puesto que no contiene ambos tipos
               episodiosCompletos = false;
               continue;
            }

            eventosDeEpisodios += 2;
            equivalenciasTipos[insertados] = indexInicio;
            //tiposReordenados.add(tipo);
            tiposReordenados[indiceOrd++] = tipoInicio;
            insertados++;
            equivalenciasTipos[insertados] = indexFin;
            //tiposReordenados.add(tipoFin);
            tiposReordenados[indiceOrd++] = tipoFin;
            insertados++;

         }

         // Insertar el resto de tipos
         // Con esta estructura auxiliar se hace una búsqueda con menos cantidad de elementos
         // que buscando en tiposReordenados
         List<String> copiaInsertados = new ArrayList<String>(Arrays.asList(tiposReordenados).subList(0,indiceOrd));
         int index = 0;
         for(String tipo : tipos){
            if(!copiaInsertados.remove(tipo)){
               // Si se borra es que estaba insertado, si no se borra no lo estaba
               equivalenciasTipos[insertados++] = index;
               //insertados++;
               //tiposReordenados.add(tipo);
               tiposReordenados[indiceOrd++] = tipo;
            }
            index++;
         }
         //this.tiposReordenados = tiposReordenados.toArray(new String[tiposReordenados.size()]);
      }
   }

   private class EpisodiosWrapperRestantes extends EpisodiosWrapper{

      public EpisodiosWrapperRestantes(List<Episodio> episodios, String[] tipos) {
         super(episodios, tipos);
      }

      protected final void organizarTipos(String[] tipos){
         eventosDeEpisodios = 0;
         int tSize = tipos.length;

         //repReordenados = new int[tSize];
         equivalenciasTipos = new int[tSize];
         if(episodios == null || episodios.isEmpty()){
            //tiposReordenados.addAll(Arrays.asList(tipos));
            tiposReordenados = tipos.clone();
            for(int i=0;i<tSize;i++){
               equivalenciasTipos[i]=i;
            }
            return;
         }

         List<String> restantes = new ArrayList<String>(Arrays.asList(tipos));
         tiposReordenados = new String[tSize];
         int insertados = 0, indiceOrd = 0;
         // Insertar los tipos contenidos en el listado de episodios
         for(Episodio episodio : episodios){
            // Comprobar si contiene ambos tipos de eventos
            //if(!tipos.contains(episodio.getTipoInicio()) || !tipos.contains(episodio.getTipoFin())){
            String tipoInicio = episodio.getTipoInicio();
            int indexInicio = Arrays.binarySearch(tipos, tipoInicio); //tipos.indexOf(tipo);
            if(indexInicio < 0){
               // Ignorar el episodio, puesto que no contiene ambos tipos
               episodiosCompletos = false;
               continue;
            }
            String tipoFin = episodio.getTipoFin();
            int indexFin = Arrays.binarySearch(tipos, tipoFin); //tipos.indexOf(tipo);
            if(indexFin < 0){
               // Ignorar el episodio, puesto que no contiene ambos tipos
               episodiosCompletos = false;
               continue;
            }

            eventosDeEpisodios += 2;
            equivalenciasTipos[insertados] = indexInicio;
            //tiposReordenados.add(tipo);
            tiposReordenados[indiceOrd++] = tipoInicio;
            insertados++;
            equivalenciasTipos[insertados] = indexFin;
            //tiposReordenados.add(tipoFin);
            tiposReordenados[indiceOrd++] = tipoFin;
            insertados++;

            //Borrar de restantes
            restantes.set(restantes.indexOf(tipoInicio), null);
            restantes.set(restantes.indexOf(tipoFin), null);
         }

         // Insertar el resto de tipos
         for(int index = 0; index<tSize; index++){
            if(restantes.get(index) != null){
               equivalenciasTipos[insertados++] = index;
               tiposReordenados[indiceOrd++] = restantes.get(index);
            }
         }
      }
   }

   @Ignore
   @Test
   public void pruebaEstres(){
      final int NUM = 100, REP = 50000, TAM = 16, EPS = 2;
      final List<String> tipos = new ArrayList<String>(Arrays.asList("A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q"));
      Random rdn = new Random();

      List<String[]> listaModelos = new ArrayList<String[]>(NUM);
      List<List<Episodio>> listaEpisodios = new ArrayList<List<Episodio>>(NUM);

      System.out.println("" + (2 % TAM));
      System.out.println("" + (10 % TAM));
      System.out.println("" + (12 % TAM));
      //Generar aleatoriamente
      for(int i = 0; i < NUM; i++){
         List<String> modelo = new ArrayList<String>(tipos);
         while(modelo.size()>TAM){
            modelo.remove(rdn.nextInt(modelo.size()));
         }
         listaModelos.add((String[])modelo.toArray(new String[TAM]));
         //System.out.println("Modelo: " + modelo);


         List<Episodio> eps = new ArrayList<Episodio>();
         // generar <EPS> episodios
         for(int e=0; e<EPS; e++){
            eps.add(new Episodio(modelo.remove(rdn.nextInt(modelo.size())), modelo.remove(rdn.nextInt(modelo.size()))));
         }
         listaEpisodios.add(eps);
      }

      //Construir modelos y medir tiempos

      //
      long inicio = System.currentTimeMillis();
      for(int j=0; j<REP; j++){

         for(int i = 0; i < NUM; i++){
            new EpisodiosWrapperCopia(listaEpisodios.get(i), listaModelos.get(i));
         }


      }
      long tiempo = System.currentTimeMillis() - inicio;
      System.out.println("Tiempo copia: " + tiempo);
      //

      inicio = System.currentTimeMillis();
      for(int j=0; j<REP; j++){

         for(int i = 0; i < NUM; i++){
            new EpisodiosWrapperRestantes(listaEpisodios.get(i), listaModelos.get(i));
         }

      }
      tiempo = System.currentTimeMillis() - inicio;
      System.out.println("Tiempo restantes: " + tiempo);

      //
      inicio = System.currentTimeMillis();
      for(int j=0; j<REP; j++){
         for(int i = 0; i < NUM; i++){
            new EpisodiosWrapperOriginal(listaEpisodios.get(i), listaModelos.get(i));
         }

      }
      tiempo = System.currentTimeMillis() - inicio;
      System.out.println("Tiempo original: " + tiempo);

   }
}
