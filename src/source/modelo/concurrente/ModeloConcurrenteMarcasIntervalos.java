package source.modelo.concurrente;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//import java.util.logging.Logger;


import source.evento.Evento;
import source.patron.Patron;

public class ModeloConcurrenteMarcasIntervalos extends ModeloConcurrente implements IMarcasIntervaloConcurrente{
   //private static final Logger LOGGER = Logger.getLogger(ModeloConcurrenteMarcasIntervalos.class.getName());
   private static final long serialVersionUID = 4433756220071530464L;

   /*
    * Constructores
    */

   public ModeloConcurrenteMarcasIntervalos(String[] tipos, int ventana,
         Integer frecuencia, int numHilos){
      super(tipos,ventana, frecuencia, numHilos);
   }

   public ModeloConcurrenteMarcasIntervalos(String[] tipos, int ventana, List<Patron> patrones,
         Integer frecuencia, int numHilos){
      super(tipos,ventana,patrones, frecuencia, numHilos);
   }

   public ModeloConcurrenteMarcasIntervalos(String[] tipos, int ventana, List<Patron> patrones,
         int numHilos){
      this(tipos,ventana,patrones, null, numHilos);
   }

   public ModeloConcurrenteMarcasIntervalos(String[] tipos, int ventana, int numHilos){
      this(tipos,ventana, (Integer)null, numHilos);
   }

   /*
    * Otros métodos
    */

   @Override
   public int[] getUltimaEncontrada(int numHilo){
      return ((ModeloParaleloMarcasIntervalosHilo)modelos.get(numHilo)).ultimaEncontrada;
   }

   @Override
   protected void crearModelosHilos(int numHilos){
      int tSize = tipos.length;
      modelos = new ArrayList<ModeloParaleloHilo>();
      for(int i=0;i<numHilos;i++){
         modelos.add(new ModeloParaleloMarcasIntervalosHilo(tSize));
      }
   }

   /*
    * Clases privadas
    */

   protected class ModeloParaleloMarcasIntervalosHilo extends ModeloParaleloHilo {
      protected int[] ultimaEncontrada = new int[2];

      public ModeloParaleloMarcasIntervalosHilo(int tSize) {
         super(tSize);
      }

      @Override
      public void recibeEvento(int sid, Evento ev, boolean savePatternInstances){
         String tipo = ev.getTipo();
         String[] tipos = getTipos();
         int index = Arrays.binarySearch(tipos, tipo); //tipos.indexOf(tipo);

         int tmp = ev.getInstante();
         int tSize = tipos.length;

         ultimaEncontrada[0]=tmp;
         ultimaEncontrada[1]=tmp;

         if(!actualizaVentana(sid, tipo, tmp, index, tSize)){
            return;
         }

         int[][] abiertas = getAbiertas(); // Listas circulares, contienen instantes temporales
         int[][] limites = getLimites(); // Límites de las listas circulares, contienen índices a 'abiertas'
         int[] tam = getTam(); // Tamaño de las listas circulares, numero de elementos en ellas
         //int[] rep = getRep(); // Numero de tipos iguales al indice actual

         int[] indices = new int[tSize];

         // Actualizar frecuencias
         int tMin;
         int[] instancia = new int[tSize];
         instancia[index]=tmp;


         do{ // Recorre cada lista
            // Comprobar si pertenece a algun patrón
            tMin = fijarInstancia(tSize, index, tmp, abiertas, limites, indices, instancia);

            if(comprobarPatrones(instancia, sid, savePatternInstances)){
               frecuenciaHilo++;
               if(tMin<ultimaEncontrada[0]){
                  ultimaEncontrada[0] = tMin;
               }
            }
            indices = siguienteCombinacionHilo(tam,indices,index,tipo);

         }while(indices != null);

         //addFrecuencias(frecuenciaLocal,patFrecLocal);//se usan las variables del hilo
      }

      /**
       * Sobrecarga del método {@code fijarInstancia} para que busque el instante mínimo de la misma.
       * @param tSize
       * @param index
       * @param tmp
       * @param abiertas
       * @param limites
       * @param indices
       * @param instancia
       * @return
       */
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

      /*
       * Sin cambios
       * (non-Javadoc)
       * @see source.modelo.concurrente.ModeloConcurrente.ModeloParaleloHilo#comprobarPatrones(int[], int, java.lang.String, int, boolean)
       */
      /*@Override
      protected boolean comprobarPatrones(int[] instancia, int sid,
            boolean savePatternInstances) {
         int i=0;
         boolean encontrado = false;
         for(Patron patron : patrones){
            if(patron.representa(sid, instancia, savePatternInstances)) {
               encontrado = true;
               patFrecHilo[i]++;
               break;
            }
            i++;
         }
         return encontrado;
      }*/

   } // Fin de clase interna




}
