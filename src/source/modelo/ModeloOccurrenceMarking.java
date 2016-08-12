package source.modelo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import source.evento.Evento;
import source.patron.Patron;

/**
 * Esta es la versión del modelo para la estrategia Occurren Marking (OM) en
 * la que los eventos tienen el atributo <usado> de tipo boolean.
 *
 *
 * @author vanesa.graino
 *
 */
public class ModeloOccurrenceMarking extends Modelo {
   //private static final Logger LOGGER = Logger.getLogger(ModeloOccurrenceMarking.class.getName());
   private static final long serialVersionUID = 1696677801066806913L;

   /**
    * Igual que abiertas sólo que con los objetos de los eventos
    */
   protected Evento[][] eventos;

   /*
    * Constructores
    */

   public ModeloOccurrenceMarking(String[] tipos, int ventana,
         Integer frecuencia) {
      super(tipos, ventana, frecuencia);
      eventos = new Evento[tipos.length][ventana];
   }

   public ModeloOccurrenceMarking(String[] tipos, int ventana,
         List<Patron> patrones, Integer frecuencia) {
      super(tipos, ventana, patrones, frecuencia);
      eventos = new Evento[tipos.length][ventana];
   }


   /*
    * Métodos
    */

   @Override
   public boolean actualizaVentana(int sid, Evento ev, String tipo, int tmp, int index, int tSize){
      boolean seguir = true;

      int[][] abiertas=getAbiertas(); // Listas circulares, contienen instantes temporales
      int[][] limites=getLimites(); // Límites de las listas circulares, contienen índices a 'abiertas'
      int[] tam=getTam(); // Tamaño de las listas circulares, numero de elementos en ellas

      if(ultimoSid != sid){
         ultimoSid = sid;
         for(int i=0;i<tSize;i++){
            tam[i]=0;
            limites[i][0] = 0;
            limites[i][1] = 0;
         }
         abiertas[index][0] = tmp;
         eventos[index][0] = ev;
         limites[index][1] = 1;
         tam[index]=1;

         return false; //Porque no es un modelo de un único evento
      }else{
         // Actualizar índices fin e inicio para adaptarse a la ventana
         // Eliminar elementos que ya no están en ventana
         for(int j,i=0;i<tSize;i++){
            j=limites[i][0];
            while((tam[i]>0)&&((tmp-ventana>=abiertas[i][j])||(tmp<abiertas[i][j]))){
               j=((j+1)%ventana);
               tam[i]--;
            }
            limites[i][0]=j; // Modificar el indicador de inicio
            if(i!=index && tam[i]<=0){
               seguir=false;
            }
         }
         // Añadir el nuevo elemento
         abiertas[index][limites[index][1]] = tmp;
         eventos[index][limites[index][1]] = ev;
         limites[index][1] = ((limites[index][1]+1)%ventana);
         tam[index]++;

         return seguir;
      }
   }

   @Deprecated
   public void recibeEvento(int sid, Evento ev, boolean savePatternInstances){
      throw new RuntimeException("Ñeh");
   }

   /**
    * Se presupone que el tipo de evento de {@code ev} es un tipo de evento
    * que incluye el modelo.
    * @param sid - identificador de la secuencia
    * @param ev - evento
    * @param esUsado - el valor de esUsado en la iteración que va alternándose en cada iteración
    */
   public void recibeEvento(int sid, Evento ev, boolean savePatternInstances, boolean esUsado){
      String tipo = ev.getTipo();
      int index = Arrays.binarySearch(tipos, tipo);//tipos.indexOf(tipo);
      int tSize = tipos.length;
      int tmp = ev.getInstante();

      if(!actualizaVentana(sid, ev, tipo, tmp, index,tSize)){
         return;
      }

      int[][] abiertas = getAbiertas(); // Listas circulares, contienen instantes temporales
      int[][] limites = getLimites(); // Límites de las listas circulares, contienen índices a 'abiertas'
      int[] tam = getTam(); // Tamaño de las listas circulares, numero de elementos en ellas

      List<Patron> patrones = getPatrones();
      int i, frecuenciaLocal=0;
      int[] patFrecLocal = new int[patrones.size()];

      // Actualizar frecuencias
      int[] indices = new int[tSize];
      int[] instancia = new int[tSize];
      instancia[index]=tmp;

      List<Evento> eventosInstancia = new ArrayList<Evento>(tSize);
      do{ // Recorre cada lista
         eventosInstancia.clear();
         // Comprobar si pertenece a algun patrón
         for(i=0;i<tSize;i++){
            if(i==index){
               eventosInstancia.add(ev);
            }else{
               instancia[i] = abiertas[i][(limites[i][0]+indices[i])%ventana];
               eventosInstancia.add(eventos[i][(limites[i][0]+indices[i])%ventana]);
            }
         }

         if(comprobarPatrones(instancia, patFrecLocal, sid, savePatternInstances)){
            frecuenciaLocal++;
            for(Evento eux : eventosInstancia){
               eux.setUsado(esUsado);
            }
         }
         indices = siguienteCombinacion(tam,indices,index,tipo);

      }while(indices != null);
      addFrecuencias(frecuenciaLocal,patFrecLocal);
   }


}
