package source.modelo.negacion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import source.evento.Evento;
import source.evento.ISecuencia;
import source.modelo.condensacion.SuperModelo;

/**
 * TODO incorpora VentanaNegacion y las situaciones que en ella se contemplan (salida
 * de eventos sin entrada)
 * No tiene eventos negados.
 * @author vanesa.graino
 *
 */
public class SuperModeloNegacion extends SuperModelo {

   /**
    *
    */
   private static final long serialVersionUID = -2217359500754871738L;

   protected ISecuencia secuencia;
   protected List<Evento> N,D;
   private int first, last = -1 ;
   private List<Evento> lastState = Collections.emptyList();

   public SuperModeloNegacion(String[] tipos, int ventana) {
      super(tipos, ventana);
      N = new ArrayList<Evento>();
      D = new ArrayList<Evento>();
   }

   /**
    * Este modelo no se utiliza alimentándolo con eventos individuales
    * sino con secuencias que se recorren con el método {@link #nextWindow()}.
    */
   @Deprecated
   @Override
   protected boolean actualizaVentana(int sid, Evento ev, String tipo, int tmp,
         int index, int tSize) {
      return super.actualizaVentana(sid, ev, tipo, tmp, index, tSize);
   }

   public void setSecuencia(ISecuencia secuencia){
      this.secuencia = secuencia;
      last = -1;

      /// resetear listas
      int[][] limites = getLimites();
      int[] tam = getTam();
      for(int i=0, tSize=tipos.length; i<tSize; i++){
         tam[i] = 0;
         limites[i][0] = 0;
         limites[i][1] = 0;
      }
   }

   /**
    *
    * @return
    */
   public boolean nextWindow(){
      if(!findNextWindow()){
         return false;
      }
      int tSize = tipos.length;
      int[][] limites = getLimites(), abiertas = getAbiertas();
      int[] tam = getTam();
      //Actualizar abiertas
      if(D.isEmpty()){
         // Tiene que haber nuevos eventos en N
         // todos en el mismo instante (tmp)
         int tmp = N.get(0).getInstante();
         int j,i=0;

         //Sacamos los eventos que ya no están en la ventana
         for(;i<tSize;i++){
            j = limites[i][0];

            // mientras (hay elementos) y (hay elementos fuera de la nueva definida)
            while(tam[i] > 0 && tmp-ventana >= abiertas[i][j]) {
               j = (j+1) % ventana;
               tam[i]--;
            }
            limites[i][0] = j; // Modificar el indicador de inicio
         }
         // Metemos los nuevos eventos en abiertas
         for(Evento ev : N){
            int index = Arrays.binarySearch(tipos, ev.getTipo());
            abiertas[index][limites[index][1]] = tmp;
            limites[index][1] = ((limites[index][1]+1)%ventana);
            tam[index]++;
         }
      }else{
         //Hay borrados

         int tmp = D.get(0).getInstante();
         // Eliminar de la ventana todos los eventos del instante del evento o eventos borrados
         for(int j,i=0;i<tSize;i++){
            j = limites[i][0];
            // mientras (hay elementos) y (hay elementos en el instante de los eventos borrados)
            while(tam[i]>0 && tmp==abiertas[i][j]){
               j = (j+1) % ventana;
               tam[i]--;
            }
            limites[i][0] = j; // Modificar el indicador de inicio
         }
         // Comprobar si hay otros eventos del mismo tipo en la ventana
         // ya que si es así no nos interesa que estos hayan salido
         for(int i=D.size()-1; i>=0; i--){
            Evento ev = D.get(i);
            int index = Arrays.binarySearch(tipos, ev.getTipo());
            if(tam[index]>0){
               D.remove(i);
            }
         }
      }
      return true;
   }

   /**
    * Método que permite recorrer todas las ventanas de una secuencia
    * cuando hay negación.
    * @return True si se ha encontrado una nueva ventana o false si
    * ya se ha recorrido toda la secuencia
    */
   protected boolean findNextWindow(){

      D.clear();
      N.clear();

      //No hay más eventos en la secuencia
      if(last == secuencia.size()-1){

         if(first > last){
            return false;
         }else if(lastState.isEmpty()){
            first = last+1;
            return false;
         }
         Evento e1 = secuencia.get(first);
         int t1 = e1.getInstante();
         while(first<secuencia.size() && secuencia.get(first).getInstante() == t1){//Borrar eventos en el mismo instante
            D.add(secuencia.get(first++));
         }
         lastState = secuencia.subList(first, last+1);
         if(lastState.isEmpty()){
            first = last+1;
            return false;
         }
         return true;
      }


      Evento ek = secuencia.get(last+1);
      int tk = ek.getInstante();

      if(!lastState.isEmpty()){
         //Detectar estado con borrado de eventos
         Evento en = secuencia.get(last);  //Ultimo evento del estado
         Evento e1 = lastState.get(0);
         int t1 = e1.getInstante(), tn=en.getInstante();
         if(t1<tk-ventana && tn<tk-1){
            D.add(e1);
            first++;
            while(secuencia.get(first).getInstante()==t1){
               D.add(secuencia.get(first));
               first++;
            }
            lastState = secuencia.subList(first, last+1);
            return true;
         }else if(tn < tk-ventana){
            lastState = Collections.emptyList();
            first = last;
         }else if(t1<=tk-ventana){
            while(secuencia.get(first).getInstante() <= tk-ventana){
               first++;
            }
         }
      }else{
         first = last+1;
      }

      //Sólo entra el nuevo evento
      N.add(ek);
      //No sale ninguno
      last++;
      while((last+1)<secuencia.size() && secuencia.get(last+1).getInstante() == tk){
         last++;
         N.add(secuencia.get(last));
      }
      lastState = secuencia.subList(first, last+1);
      return true;


   }

   /**
    * Los nuevos eventos que hay en la ventana actual
    * respecto a la ventana anterior.
    * @return
    */
   public List<Evento> getN(){
      return N;
   }

   /**
    * Los eventos que se han borrado de la ventana con
    * respecto de la ventana anterior.
    * @return
    */
   public List<Evento> getD(){
      return D;
   }


   public List<Evento> getLastState(){
      return lastState;
   }
}
