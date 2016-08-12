package source.modelo.concurrente2.semilla;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//import java.util.logging.Logger;


import source.evento.Evento;
import source.excepciones.FactoryInstantiationException;
import source.modelo.IAsociacionSemilla;
import source.modelo.clustering.IClustering;
import source.modelo.concurrente.IMarcasIntervaloConcurrente;
import source.modelo.concurrente2.ModeloConcurrenteDistribucion;
import source.patron.GeneradorID;
import source.patron.Patron;
import source.patron.PatternFactory;
import source.restriccion.RIntervalo;

public class ModeloConcurrenteSemilla extends ModeloConcurrenteDistribucion implements IMarcasIntervaloConcurrente, IAsociacionSemilla {
   //private static final Logger LOGGER = Logger.getLogger(ModeloConcurrenteSemilla.class.getName());
   private static final long serialVersionUID = 2193149216862698424L;

   /*
    * Atributos propios
    */

   private int[][] distribucion;

   /*
    * Constructores
    */

   public ModeloConcurrenteSemilla(String[] tipos, int ventana,
         Integer frecuencia, IClustering clustering, int numHilos, boolean... createHilos){
      super(tipos,ventana, frecuencia, clustering, numHilos,createHilos);
      //this.distribucion = getDistribucion();
   }

   public ModeloConcurrenteSemilla(String[] tipos, int ventana, List<Patron> patrones,
         Integer frecuencia, IClustering clustering, int numHilos, boolean... createHilos){
      super(tipos, ventana, patrones, frecuencia, clustering,numHilos,createHilos);
      distribucion = new int[(tipos.length*(tipos.length-1))/2][2*ventana + 1];
   }

   /*
    * Metodos
    */

   @Override
   public int[][] getDistribuciones(){
      return this.distribucion;
   }

   @Override
   public void incrementarDistribucion(int fila, int columna){
      synchronized (distribucion) {
         distribucion[fila][columna]++;
      }
   }

   @Override
   public int calculaPatrones(int supmin, String patternClassName, GeneradorID genID,
         boolean savePatternInstances) throws FactoryInstantiationException{
      String[] tipos = getTipos();
      int i,j,k,l,tSize=tipos.length;


      List<RIntervalo> restricciones = new ArrayList<RIntervalo>();
      // Clustering de las df's
      int distActual=0;
      for(i=0;i<tSize-1;i++){
         for(j=i+1;j<tSize;j++){
            List<RIntervalo> rests = getClustering().agrupar(getDistribuciones()[distActual], tipos[i], tipos[j]);
            distActual++;
            List<RIntervalo> rSemilla = getRestricciones(tipos[i], tipos[j]);
            // Intersección con las semillas
            if(rSemilla.isEmpty()){
               restricciones.addAll(rests);
               continue;
            }
            // Había semillas, calcular intersección
            List<RIntervalo> trueRests = new ArrayList<RIntervalo>();
            for(k=0;k<rests.size();k++){
               RIntervalo ri = rests.get(k);

               for(l=0;l<rSemilla.size();l++){
                  RIntervalo rs = rSemilla.get(l);
                  if((rs.getInicio()==-Float.NEGATIVE_INFINITY)&&
                        (rs.getFin()==Float.NEGATIVE_INFINITY)){
                     trueRests.add(ri);
                     continue;
                  }
                  int nmin = Math.max(rs.getInicio(), ri.getInicio()), nmax = Math.min(rs.getFin(), ri.getFin());
                  if(nmin<=nmax){
                     trueRests.add(new RIntervalo(rs.getTipoA(),rs.getTipoB(),nmin,nmax));
                  }
                  //else, intersección vacía, no hacer nada
               }
            }
            if(trueRests.isEmpty()){
               // Las semillas no dan lugar a patrones frecuentes, parar proceso
               resetPatrones();
               return 0;
            }else{
               restricciones.addAll(trueRests);
            }
         }
      }

      // Construir patrones de pares de tipos de eventos, y establecer su frecuencia
      int rSize = restricciones.size();
      int[] patFrec = new int[rSize];
      List<Patron> patrones = new ArrayList<Patron>();

      for(i=0;i<rSize;i++){
         RIntervalo ri = restricciones.get(i);
         int indexA = Arrays.binarySearch(tipos, ri.getTipoA());
         int indexB = Arrays.binarySearch(tipos, ri.getTipoB());
         //TODO falla con tamaño 2 si no se hace así
         int indexDist = tSize == 2 ? 0 : (indexA*(indexA-1))/2+(indexB-indexA);
         int[] distr = getDistribuciones()[indexDist];

         String[] tiposPatron = new String[]{ ri.getTipoA(), ri.getTipoB()};
         Patron p = PatternFactory.getInstance().getPattern(genID, patternClassName, tiposPatron,
               new ArrayList<RIntervalo>(restricciones.subList(i, i+1)), savePatternInstances);
         // Calcular frecuencia del patrón
         for(j=(int)ri.getInicio()+ventana;j<ri.getFin()+ventana+1;j++){
            patFrec[i]+=distr[j];
         }
         patrones.add(p);
      }
      setPatrones(patrones,patFrec);

      return 0;
   }

   @Override
   public int[] getUltimaEncontrada(int numHilo) {
      return ((ModeloParaleloSemillaHilo)modelos.get(numHilo)).ultimaEncontrada;
   }

   @Override
   protected void crearModelosHilos(int numHilos){
      int tSize = tipos.length;
      modelos = new ArrayList<ModeloParaleloHilo>();
      for(int i=0;i<numHilos;i++){
         modelos.add(new ModeloParaleloSemillaHilo(tSize));
      }
   }

   /*
    * Clases privadas
    */

   protected class ModeloParaleloSemillaHilo extends ModeloParaleloHilo {

      protected int[] ultimaEncontrada = {0,0};

      public ModeloParaleloSemillaHilo(int tSize) {
         super(tSize);
      }

      @Override
      protected int[] siguienteCombinacionHilo(int[] tam, int[] indices, int index, String tipo){
         int tSize = tipos.length;
         indices[tSize-1]++;
         int i;
         for(i=tSize-1;i>0;i--){
            if(i==index){
               if(indices[i]!=0){
                  indices[i]=0;
                  indices[i-1]++;
               }
            }else{
               if(indices[i] >= tam[i] ){
                  indices[i-1]++;
                  indices[i]=0;
               }else{
                  break; // No hay que propagar más cambios, combinación de eventos válida.
               }
            }
         }
         if((index==0 && indices[0]>0) || indices[0]>=tam[0]){
               return null;
         }
         return indices;
      }

      @Override
      public void recibeEvento(int sid, Evento ev, boolean savePatternInstances){
         String[] tipos = getTipos();
         String tipo = ev.getTipo();
         int index = Arrays.binarySearch(tipos, tipo);

         int tmp = ev.getInstante();
         int tSize = tipos.length;
         ultimaEncontrada[0] = tmp;
         ultimaEncontrada[1] = tmp;

         if(tSize==1) {
            incrementarSoporte();
            return;
         }

         if(!actualizaVentana(sid, ev, tipo, tmp, index, tSize)){
            return;
         }

         int[] tam = getTam();
         //int[] rep = getRep();
         int[][] limites = getLimites();
         int[][] abiertas = getAbiertas();
         List<Patron> patrones = getPatrones();
         int pSize = patrones.size();
         int[] patFrecLocal = new int[pSize];
         int i=0,j=0,valor,frecuenciaLocal = 0;

         // Actualizar frecuencias
         int[] indices = new int[tSize];
         int tMin;
         int[] instancia = new int[tSize];
         instancia[index]=tmp;

         do{
            // Comprobar si pertenece a algun patrón
            tMin = fijarInstancia(tSize, index, tmp, abiertas, limites, indices, instancia);

            if(comprobarPatrones(instancia, patFrecLocal, sid, savePatternInstances)){
               // Actualizar las distribuciones de frecuencia
               int dist=0;
               for(i=0;i<tSize;i++){
                  for(j=i+1;j<tSize;j++){
                     valor = (int)(instancia[j] - instancia[i]);
                     incrementarDistribucion(dist, valor+ventana);
                     dist++;
                  }
               }
               if(tMin<ultimaEncontrada[0]){ ultimaEncontrada[0] = tMin; }
               frecuenciaLocal++; //porque la semilla tiene su propia frecuencia
               frecuenciaHilo++;
            }
            indices = siguienteCombinacionHilo(tam,indices,index,tipo);

         }while(indices != null);
         addFrecuencias(frecuenciaLocal,patFrecLocal);
      }

      protected int fijarInstancia(int tSize, int index, int tmp, int[][] abiertas, int[][] limites,
            int[] indices, int[] instancia){
         int tMin = tmp;
         for(int i=0;i<tSize;i++){
            if(i!=index){
               instancia[i]=abiertas[i][(limites[i][0]+indices[i])%ventana];
               if(tMin>instancia[i]){
                  tMin=instancia[i];
               }
            }
         }
         return tMin;
      }

      // NO PREPARADO PARA TIPOS REPETIDOS (usar rep)
      // Realiza el clustering de las distribuciones de frecuencia
      // Se aprovecha el recorrido de este modelo como recorrido para pares de tipos de eventos
      // Para aquellas restricciones establecidas por el usuario, el clustering se restringe a lo establecido por el usuario
      // Como resultado, se generan de nuevo todos los patrones, con todas las posibles combinaciones, y se eliminan los viejos


   } //Fin de la clase interna


}
