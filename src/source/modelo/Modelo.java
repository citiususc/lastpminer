package source.modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//import java.util.logging.Logger;


import source.configuracion.ConfigurationParameters;
import source.evento.Evento;
import source.patron.Patron;

/**
 * Clase base para las asociaciones temporales de las versiones en serie y la versión paralela (concurrente
 * utiliza un conjunto diferente de modelos).
 *
 * No utilizar esta clase para modelos con un único tipo de evento, para ellos utilizar {@link ModeloEvento} o alguna
 * implementacion de {@link IAsociacionEvento}.
 *
 *
 * TODO cómo se están utilizando abiertas y limites
 *
 * @author vanesa.graino
 *
 */
public class Modelo extends ModeloAbstracto implements IAsociacionTemporal, IAsociacionDeHilo, Serializable{
   /*
    * Parte estática
    */
   //private static final Logger LOGGER = Logger.getLogger(Modelo.class.getName());
   private static final long serialVersionUID = -3490529231233155049L;

   private static int ultimoHilo = ConfigurationParameters.NUM_THREADS;

   /* Parte dinámica */

   private int hilo = getHiloAsignado();

   /**
    * Matriz de tamaño tSize x ventana. Son listas circulares que contienen instantes temporales.
    */
   private int[][] abiertas;
   /**
    * Matrix de tSize x 2 con límite inferior y superior para cada tipo de evento de la asociacion temporal
    * estos límites son índices de las listas circulares, es decir, de abiertas
    */
   private int[][] limites;
   /**
    *  Array de tSize elementos, cada uno contiene el tamaño de las listas circulares
    */
   private int[] tam;

   /**
    * Sid de la última secuencia de la que se ha recibido evento
    */
   protected int ultimoSid = -1;

   /*
    * Static methods
    */

   protected static int getHiloAsignado(){
      ultimoHilo = (ultimoHilo+1) % ConfigurationParameters.NUM_THREADS;
      return ultimoHilo;
   }

   /*
    * Constructores
    */

   public Modelo(String[] tipos, int ventana, Integer frecuencia ){
      super(tipos,ventana,frecuencia);
      int tSize = tipos.length;
      this.abiertas = new int[tSize][ventana]; // Hará falta más tamaño? @miguel
      this.tam = new int[tSize];
      this.limites = new int[tSize][2];
   }

   public Modelo(String[] tipos, int ventana, List<Patron> patrones, Integer frecuencia){
      super(tipos,ventana,patrones,frecuencia);
      int tSize = tipos.length;
      this.abiertas = new int[tSize][ventana]; // Hará falta más tamaño? @miguel
      this.tam = new int[tSize];
      this.limites = new int[tSize][2];
   }

   /*
    * Getters and setters
    */

   protected int[][] getLimites(){
      return limites;
   }

   protected int[][] getAbiertas(){
      return abiertas;
   }

   protected int[] getTam(){
      return tam;
   }

   public int getHilo(){
      return hilo;
   }

   public void setHilo(int hilo) {
      this.hilo = hilo;
   }

   /*
    * Otros métodos
    */

   /**
    * Incorpora el evento <evento> a las estructuras de control de ventana
    * y elimina los eventos que estaban en la ventana y dejan de estarlo.
    * Presupone que el tipo de evento es parte de los tipos de evento del modelo.
    * @param sid
    * @param evento
    */
   protected void actualizaVentana(int sid, Evento evento){
      String tipo = evento.getTipo();
      int index = Arrays.binarySearch(tipos, tipo); //tipos.indexOf(tipo);
      int tSize = tipos.length;
      actualizaVentana(sid, evento, tipo, evento.getInstante(), index, tSize);
   }

   /**
    * Este método es igual a {@link #actualizaVentana(sid,evento)} pero ademas de actualizar la ventana
    * comprueba que hay los eventos necesarios para encontrar una instancia de
    * patrón
    * @param tipo - tipo del evento
    * @param tmp - instante del evento
    * @param index - indice del tipo del evento en la lista de tipos de la asociacion
    * @param tSize - tamaño de la lista de tipos
    * @return true si hay suficientes eventos o false en caso contrario
    */
   protected boolean actualizaVentana( int sid, Evento ev, String tipo, int tmp, int index, int tSize){
      boolean seguir = true;

      // Actualizar índices fin e inicio para adaptarse a la ventana
      // Eliminar elementos que ya no están en ventana
      if(ultimoSid != sid){
         ultimoSid = sid;
         for(int i=0;i<tSize;i++){
            tam[i]=0;
            limites[i][0] = 0;
            limites[i][1] = 0;
         }
         abiertas[index][0] = tmp;
         limites[index][1] = 1;
         tam[index]=1;
         return false; //Porque no es un modelo de un único evento
      }

      //Seguimos en la misma secuencia
      for(int j,i=0;i<tSize;i++){
         j = limites[i][0];

         // mientras (hay elementos) y ((hay elementos fuera de la nueva definida) o (el elemento leido ocurre antes => nueva secuencia))
         while(tam[i] > 0 && /*j!=limites[i][1] && */ tmp-ventana >= abiertas[i][j]) {
            j = ((j+1) % ventana);
            tam[i]--;
         }
         limites[i][0] = j; // Modificar el indicador de inicio
         if(i!=index && tam[i]<=0 ){
            seguir=false;
         }
      }
      // Añadir el nuevo elemento
      abiertas[index][limites[index][1]] = tmp;
      limites[index][1] = ((limites[index][1]+1)%ventana);
      tam[index]++;
      return seguir;
   }

   /**
    * Versión con repetidos
    * TODO utilizar ultimoSid
    */
   protected boolean actualizaVentana(int sid, Evento ev, String tipo, int tmp, int index, int tSize, int[] rep){
      boolean seguir = true;
      // Actualizar índices fin e inicio para adaptarse a la ventana
      // Eliminar elementos que ya no están en ventana
      for(int j,i=0; i<tSize; i++){
         j = limites[i][0];
         // mientras (hay elementos) y ((hay elementos fuera de la nueva definida) o (el elemento leido ocurre antes => nueva secuencia))
         while(tam[i] > 0 && /*j!=limites[i][1] && */ (tmp-ventana >= abiertas[i][j] || tmp < abiertas[i][j])) {
            j = ((j+1) % ventana);
            tam[i]--;
         }
         limites[i][0] = j; // Modificar el indicador de inicio
         if(i!=index && tam[i]<=rep[i] ){
            seguir=false;
         }
      }
      // Añadir el nuevo elemento
      abiertas[index][limites[index][1]] = tmp;
      limites[index][1] = ((limites[index][1]+1)%ventana);
      tam[index]++;
      return seguir && tam[index]>=rep[index];
   }

   /*
    * (non-Javadoc)
    * @see source.modelo.IAsociacionTemporal#recibeEvento(int, source.evento.Evento, boolean)
    */
   @Override
   public void recibeEvento(int sid, Evento ev, boolean savePatternInstances){
      String tipo = ev.getTipo();
      int index = Arrays.binarySearch(tipos, tipo); //tipos.indexOf(tipo);

      //tamaño de la asociacion temporal
      int tSize = tipos.length;

      //instante del evento
      int tmp = ev.getInstante();

      if(!actualizaVentana(sid, ev, tipo, tmp, index, tSize)){
         return;
      }

      int frecuenciaLocal=0;
      int[] patFrecLocal = new int[patrones.size()];
      int[] indices = new int[tSize];

      // Actualizar frecuencias
      //int actualizadas;
      int[] instancia = new int[tSize];
      instancia[index]=tmp;
      fijarInstancia(tSize, index, indices, instancia);

      do{ // Recorre cada lista
         if(comprobarPatrones(instancia, patFrecLocal, sid, savePatternInstances)){
            frecuenciaLocal++;
         }
         //indices = siguienteCombinacion(tam, indices, index, tipo);
      }while(siguienteInstancia(tam, indices, instancia, index, tipo, tSize));
      // Actualizar la frecuencia
      addFrecuencias(frecuenciaLocal,patFrecLocal);
   }

   protected boolean siguienteInstancia(int[] tam, int[] indices, int[] instancia, int index, String tipo, int tSize){
      indices = siguienteCombinacion(tam, indices, index, tipo);
      if(indices != null){
         for(int i=0;i<tSize;i++){
            if(i!=index){
               instancia[i]=abiertas[i][(limites[i][0]+indices[i])%ventana];
            }
         }
         return true;
      }
      return false;
   }

   protected void fijarInstancia(int tSize, int index, int[] indices, int[] instancia){
      for(int i=0;i<tSize;i++){
         if(i!=index){
            instancia[i]=abiertas[i][(limites[i][0]+indices[i])%ventana];
         }
      }
   }

   protected int fijarInstancia(int tSize, int index, int tmp, int[] indices, int[] instancia){
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

   /*protected int fijarInstancia(int tSize, int index, int tmp, int[][] abiertas, int[][] limites,
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
   }*/

   protected boolean comprobarPatrones(int[] instancia, int[] patFrecLocal, int sid,
          boolean savePatternInstances){
      boolean encontrado = false;
      int i=0;
      for(Patron patron : patrones){
         if(patron.representa(sid, instancia, savePatternInstances)){
            //actualizadas++;
            encontrado = true;
            patFrecLocal[i]++;
            patronEncontrado(patron, i);
            // Los patrones de una asociación temporal no pueden compartir
            // instancias (al menos una restricción temporal es disjunta),
            // por lo tanto podemos cortar aquí las demás comprobaciones
            break;
         }
         i++;
      }
      return encontrado;
   }

   public void patronEncontrado(Patron patron, int pIndex){
      //Para que futuras implementaciones lo puedan modificar (por ejemplo en negación)
   }

   public synchronized void agregar(IAsociacionTemporal asociacion) {
      if(!(asociacion instanceof Modelo)){ return; }
      Modelo agregada = (Modelo)asociacion;
      int pSize = patrones.size();
      addFrecuencias(agregada.getSoporte(), agregada.getPatFrec());
      for(int i=0;i<pSize;i++){
         Patron p = patrones.get(i);
         p.agregar(agregada.getPatrones().get(i));
      }
   }

   /**
    * Clona este objeto
    */
   //@Override
   public Modelo clonar() {
      List<Patron> patronesCopia = new ArrayList<Patron>();
      for(Patron p:getPatrones()){
         patronesCopia.add(((Patron)p).clonar());
      }
      return new Modelo(getTipos(), ventana, patronesCopia, getSoporte());
   }

   /*
    * (non-Javadoc)
    * @see source.modelo.IAsociacionTemporal#toModeloString()
    */
   @Override
   public String toStringSinPatrones(){
      return Arrays.toString(tipos);
   }



}