package source.modelo.concurrente2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//import java.util.logging.Logger;

import source.evento.Evento;
import source.modelo.concurrente.IMarcasIntervaloConcurrente;
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

   @Override
   public int[] getUltimaEncontrada(int numHilo){
      return ((ModeloParaleloMarcasIntervalosHilo)modelos.get(numHilo)).ultimaOcurrencia;
   }

   @Override
   protected void crearModelosHilos(int numHilos){
      int tSize = tipos.length;
      modelos = new ArrayList<ModeloParaleloHilo>();
      for(int i=0;i<numHilos;i++){
         modelos.add(new ModeloParaleloMarcasIntervalosHilo(tSize));
      }
   }

   protected class ModeloParaleloMarcasIntervalosHilo extends ModeloParaleloHilo {
      protected int[] ultimaOcurrencia = new int[2];

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

         if(!actualizaVentana(sid, ev, tipo, tmp, index, tSize)){
            return;
         }

         ultimaOcurrencia[0] = tmp;
         ultimaOcurrencia[1] = tmp;

         // Actualizar frecuencias
         List<Patron> patrones = getPatrones();
         int tMin, frecuenciaLocal = 0;
         int[] patFrecLocal = new int[patrones.size()];
         int[] tam = getTam(); // Tamaño de las listas circulares, numero de elementos en ellas
         int[] indices = new int[tSize];
         int[] instancia = new int[tSize];
         instancia[index] = tmp;

         do{ // Recorre cada lista
            // Comprobar si pertenece a algun patrón
            tMin = fijarInstancia(tSize, index, tmp, indices, instancia);
            if(comprobarPatrones(instancia, patFrecLocal, sid, savePatternInstances)){
               frecuenciaLocal++;
               if(tMin<ultimaOcurrencia[0]){
                  ultimaOcurrencia[0] = tMin;
               }
            }
            indices = siguienteCombinacionHilo(tam,indices,index,tipo);

         }while(indices != null);
         addFrecuencias(frecuenciaLocal,patFrecLocal);
      }


   } // Fin clase interna

}

