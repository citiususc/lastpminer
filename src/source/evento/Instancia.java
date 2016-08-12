package source.evento;

import java.util.ArrayList;
import java.util.List;

/**
 * Esta clase no se está usando
 * @author vanesa.graino
 *
 */
public class Instancia{
   private final List<String> esperados;
   private final List<Evento> recibidos;
   private int comienzo;

   public Instancia(List<String> tipos){
      esperados = new ArrayList<String>(tipos);
      recibidos = new ArrayList<Evento>();
      comienzo = Integer.MAX_VALUE;
   }

   // Es el modelo el que gestiona la ventana
   // Así puede eliminar las que ya no se pueden cumplir
   // Normalizar la secuencia según va llegando?
   public boolean recibeEvento(Evento ev){

      String tipo = ev.getTipo();
      if (!esperados.contains(tipo)){ return false; }

      int tmp = ev.getInstante();
      if (comienzo > tmp){ comienzo = tmp; }
      recibidos.add(ev);
      esperados.remove(tipo);
      return true;
   }

   public int getComienzo(){
      return comienzo;
   }

   public int getRestantes(){
      return esperados.size();
   }

   public int getInstante(String tipo){
      int i=0;
      for(i=0;i<recibidos.size();i++){
         if(tipo==recibidos.get(i).getTipo()){
            return recibidos.get(i).getInstante();
         }
      }
      return 0;
   }

   public List<Evento> getRecibidos(){
      return recibidos;
   }

   public String toString(){
      return recibidos.toString();
   }
}
