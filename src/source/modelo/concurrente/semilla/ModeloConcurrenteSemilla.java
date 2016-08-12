package source.modelo.concurrente.semilla;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import source.evento.Evento;
import source.excepciones.FactoryInstantiationException;
import source.modelo.IAsociacionSemilla;
import source.modelo.clustering.IClustering;
import source.modelo.concurrente.IMarcasIntervaloConcurrente;
import source.modelo.concurrente.ModeloConcurrente;
import source.modelo.semilla.ModeloSemilla;
import source.patron.GeneradorID;
import source.patron.Patron;
import source.patron.PatternFactory;
import source.restriccion.RIntervalo;

public class ModeloConcurrenteSemilla extends ModeloConcurrente implements IMarcasIntervaloConcurrente, IAsociacionSemilla {
   private static final Logger LOGGER = Logger.getLogger(ModeloConcurrenteSemilla.class.getName());
   private static final long serialVersionUID = 2193149216862698424L;

   protected IClustering clustering;
   protected int[][] distribuciones;

   private int frecuencia;


   /*
    * Constructores
    */

   public ModeloConcurrenteSemilla(String[] tipos, int ventana,
         Integer frecuencia, IClustering clustering, int numHilos, boolean... createHilos){
      super(tipos, ventana, frecuencia, numHilos, createHilos);
      this.clustering = clustering;
   }

   public ModeloConcurrenteSemilla(String[] tipos, int ventana, List<Patron> patrones,
         Integer frecuencia, IClustering clustering,
         int numHilos, boolean... createHilos){
      super(tipos, ventana, patrones, frecuencia, numHilos, createHilos);
      this.clustering = clustering;
   }

   /*
    * Metodos
    */

   @Override
   public IClustering getClustering(){
      return clustering;
   }

   @Override
   public int[][] getDistribuciones(){
      return distribuciones;
   }

   @Override
   public void incrementarDistribucion(int indexDistribucion, int valor){
      throw new UnsupportedOperationException("Este método no debe llamarse para esta clase");
   }

   @Override
   public int calculaPatrones(int supmin, String patternClassName, GeneradorID genID,
         boolean savePatternInstances) throws FactoryInstantiationException{
      String[] tipos = getTipos();
      int i,j,k,l, tSize = tipos.length;
      super.addFrecuencias(frecuencia,null);

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
                  }//else, intersección vacía, no hacer nada
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
      int[] patFrecLocal = new int[rSize];
      List<Patron> patrones = new ArrayList<Patron>();


      for(i=0;i<rSize;i++){
         RIntervalo ri = restricciones.get(i);
         int indexA = Arrays.binarySearch(tipos, ri.getTipoA());
         int indexB = Arrays.binarySearch(tipos, ri.getTipoB());
         int indexDist = tSize == 2? 0 : (indexA*(indexA-1))/2+(indexB-indexA);//TODO falla con tamaño 2 si no se hace así
         int[] distr = getDistribuciones()[indexDist];
//         List<String> tiposPatron = new ArrayList<String>();
//         tiposPatron.add(ri.getTipoA());
//         tiposPatron.add(ri.getTipoB());
         String[] tiposPatron = new String[]{ri.getTipoA(), ri.getTipoB()};
         //Patron p = new Patron(aux, restricciones.subList(i, i+1),getSavePatternInstances()); // CUIDADO: ¿puede alterar la lista original?
         Patron p = PatternFactory.getInstance().getPattern(genID, patternClassName, tiposPatron, restricciones.subList(i, i+1), savePatternInstances);
         // Calcular frecuencia del patrón
         for(j=(int)ri.getInicio()+ventana;j<ri.getFin()+ventana+1;j++){
            patFrecLocal[i]+=distr[j];
         }
         patrones.add(p);
      }
      setPatrones(patrones,patFrecLocal);
      return 0;
   }

   @Override
   public int[] getUltimaEncontrada(int numHilo) {
      return ((ModeloParaleloSemillaHilo)modelos.get(numHilo)).ultimaEncontrada;
   }

   @Override
   public int getSoporte(){
      return frecuencia;
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

     protected int[][] distribucionesHilo;
      protected int[] ultimaEncontrada = {0,0};

      public ModeloParaleloSemillaHilo(int tSize) {
         super(tSize);
         this.distribucionesHilo = ModeloSemilla.iniciarDistribuciones(tSize, ventana);
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

      public void recibeEvento(int sid, Evento ev, boolean savePatternInstances){
         String[] tipos = getTipos();


         String tipo = ev.getTipo();
         int index = Arrays.binarySearch(tipos, tipo);
         if(index<0){
            LOGGER.severe("Entra en index<0 al recibir evento");
            return;
         }

         int tmp = ev.getInstante();
         int tSize = tipos.length;
         if(tSize==1) {
            //LOGGER.severe("Entra en tSize==1 al recibir evento");
            frecuencia++;//incrementarSoporte();
            ultimaEncontrada[0] = tmp;
            ultimaEncontrada[1] = tmp;
            return;
         }


         if(!actualizaVentana(sid, tipo, tmp, index, tSize)){
            return;
         }

         int[] tam = getTam();
         int i=0,j=0,valor;
         int[] indices = new int[tSize];

         // Actualizar frecuencias

         int[] instancia = new int[tSize];
         instancia[index]=tmp;

         ultimaEncontrada[0]=tmp;
         ultimaEncontrada[1]=tmp;

         do{ // Recorre cada lista
            // Comprobar si pertenece a algun patrón
            fijarInstancia(instancia, index, indices, tSize, tmp);
            if(comprobarPatrones(instancia, sid, savePatternInstances)){
               // Actualizar las distribuciones de frecuencia
               int dist=0;
               for(i=0;i<tSize;i++){ // Actualiza las distribuciones de frecuencia
                  if(ultimaEncontrada[0]>instancia[i]){
                     ultimaEncontrada[0]=instancia[i];
                  }
                  for(j=i+1;j<tSize;j++){
                     valor = (int)(instancia[j] - instancia[i]);
                     distribucionesHilo[dist][valor+ventana]++;
                     //incrementarDistribucion(dist, valor+ventana);
                     dist++;
                  }
               }
               frecuenciaHilo++;
            }
            indices = siguienteCombinacionHilo(tam,indices,index,tipo);

         }while(indices != null);
         //addFrecuencias(frecuenciaLocal,patFrecLocal);
      }

      // NO PREPARADO PARA TIPOS REPETIDOS (usar rep)
      // Realiza el clustering de las distribuciones de frecuencia
      // Se aprovecha el recorrido de este modelo como recorrido para pares de tipos de eventos
      // Para aquellas restricciones establecidas por el usuario, el clustering se restringe a lo establecido por el usuario
      // Como resultado, se generan de nuevo todos los patrones, con todas las posibles combinaciones, y se eliminan los viejos


   }


}
