package source.modelo;

import java.util.Arrays;
import java.util.List;
//import java.util.logging.Logger;

import source.evento.Evento;
import source.patron.Patron;

public class ModeloMarcasIntervalos extends Modelo implements IMarcasIntervalos{
   //private static final Logger LOGGER = Logger.getLogger(ModeloMarcasIntervalos.class.getName());
   private static final long serialVersionUID = -2568244372486077179L;

   /*
    * Atributos
    */

   /**
    * Intervalo de la ultima ocurrencia de un patrón del modelo encontrada
    */
   protected int[] ultimaEncontrada = new int[2];

   /*
    * Constructores
    */

   public ModeloMarcasIntervalos(String[] tipos, int ventana, Integer frecuencia){
      super(tipos,ventana, frecuencia);
   }

   public ModeloMarcasIntervalos(String[] tipos, int ventana, List<Patron> patrones,
         Integer frecuencia){
      super(tipos,ventana,patrones, frecuencia);
   }

   /*
    * Métodos
    */
   @Override
   public int[] getUltimaEncontrada(){
      return ultimaEncontrada;
   }

   /*
    * Solo cambia que se guardan los instantes inicial y final de
    * la ultima ocurrencia encontrada en <ultimaEncontrada>
    * (non-Javadoc)
    * @see source.modelo.Modelo#recibeEvento(int, source.evento.Evento, boolean)
    */
   @Override
   public void recibeEvento(int sid, Evento ev, boolean savePatternInstances){
      String tipo = ev.getTipo();
      int index = Arrays.binarySearch(tipos, tipo); //tipos.indexOf(tipo);
      int tSize = tipos.length;
      int tmp = ev.getInstante();

      if(!actualizaVentana(sid, ev, tipo, tmp, index, tSize)){
         return;
      }

      // Actualizar frecuencias
      int[] tam = getTam(); // Tamaño de las listas circulares, numero de elementos en ellas
      int[] instancia = new int[tSize];
      int[] indices = new int[tSize];
      int[] patFrecLocal = new int[patrones.size()];
      int tMin, frecuenciaLocal = 0;

      instancia[index] = tmp;
      ultimaEncontrada[0] = tmp;
      ultimaEncontrada[1] = tmp;

      do{ // Recorre cada lista
         // Comprobar si pertenece a algun patrón
         tMin = fijarInstancia(tSize, index, tmp, indices, instancia);

         if(comprobarPatrones(instancia, patFrecLocal, sid, savePatternInstances)){
            frecuenciaLocal++;
            if(tMin < ultimaEncontrada[0]){
               ultimaEncontrada[0] = tMin;
            }
         }
         indices = siguienteCombinacion(tam,indices,index,tipo);

      }while(indices != null);
      addFrecuencias(frecuenciaLocal,patFrecLocal);
   }


}
