package source.patron;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import source.evento.Evento;
import source.restriccion.RIntervalo;

/**
 *
 * @author vanesa.graino
 *
 */
public class PatronMarcado extends PatronAnotaciones implements Observer {

   /*
    * Atributos
    */
   protected transient List<int[]> encontradas = new ArrayList<int[]>();


   /*
    * Constructores
    */

   public PatronMarcado(String[] tipos, boolean savePatternInstances) {
      super(tipos, savePatternInstances);
   }

   public PatronMarcado(String[] tipos, List<RIntervalo> restricciones,
         boolean savePatternInstances) {
      super(tipos, restricciones, savePatternInstances);
   }

   public PatronMarcado(String[] tipos, Patron patron) {
      super(tipos, patron);
   }

   public PatronMarcado(Patron patron) {
      super(patron);
   }

   public PatronMarcado(PatronDictionaryFinalEvent patron) {
      super(patron);
   }

   /*
    * Métodos
    */

   public void encontrado(int sid, int tMin, int tmp){
      super.encontrado();
      int[] ultimaEncontrada = new int[3];
      ultimaEncontrada[0] = sid;
      ultimaEncontrada[1] = tMin;
      ultimaEncontrada[2] = tmp;
      encontradas.add(ultimaEncontrada);
      //this.sid = sid;
   }

   public void limpiar(){
      encontradas = null;
   }

   public boolean enEvento(int sid, Evento ev, int ventana){
      if(encontradas.isEmpty()) return false;
      int tmp = ev.getInstante(), inicioVentana = tmp-ventana;
      int[] encontrada = null;
      int i=0;

      //Borrar las que quedan fuera de la ventana
      while(!encontradas.isEmpty()){
         encontrada = encontradas.get(0);
         if(encontrada[0] < sid || (encontrada[0] == sid && encontrada[1] < inicioVentana)){
            encontradas.remove(0);
         }else{
            //Quedarse en la del evento (si hay)
            while(i<encontradas.size() && encontrada[0] == sid && encontrada[2]<tmp){
               encontrada = encontradas.get(i);
               i++;
            }
            break;
         }
      }
      return encontrada[0] == sid && encontrada[2] == tmp;
   }

   public boolean enVentana(int sid, Evento ev, int ventana){

      if(encontradas.isEmpty()) return false;
      int tmp = ev.getInstante(), inicioVentana = tmp-ventana;
      int[] encontrada = null;

      //Borrar las que quedan fuera de la ventana
      while(!encontradas.isEmpty()){
         encontrada = encontradas.get(0);
         // Si es de una secuencia anterior o si el inicio de la ocurrencia sucede
         // antes del inicio de la ventana actual
         if(encontrada[0] < sid || (encontrada[0] == sid && encontrada[1] < inicioVentana)){
            encontradas.remove(0);

         }else{
            break;
         }
      }
      //Hay una ocurrencia dentro de la ventana
      return encontrada[0] == sid && inicioVentana <= encontrada[1] && encontrada[2] <= tmp;
   }

   /*
    * (non-Javadoc)
    * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
    */
   @Override
   public void update(Observable o, Object tamActual) {
      if(((Integer)tamActual)>getTipos().length){
         limpiar();
         o.deleteObserver(this);
      }
   }

}
