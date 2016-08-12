package source.busqueda.negacion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import source.evento.Evento;
import source.evento.ISecuencia;

/**
 * Clase de prueba de concepto. Después habrá que implementarlo en
 * el super modelo.
 * @author vanesa.graino
 *
 */
public class VentanaNegacion {

      List<Evento> N = new ArrayList<Evento>(); //new,
      List<Evento> D = new ArrayList<Evento>(); //deleted, removed from window
      ISecuencia secuencia;
      int window;
      private int first, last = -1;
      private List<Evento> lastState = Collections.emptyList();


      VentanaNegacion(ISecuencia secuencia, int window){
         this.secuencia = secuencia;
         this.window = window;
      }

      List<Evento> nextState(){
         D.clear();
         N.clear();

         //No hay más eventos en la secuencia
         if(last == secuencia.size()-1){

            if(first > last){
               return null;
            }else if(lastState.isEmpty()){
               first = last+1;
               return null;
            }
            Evento e1 = secuencia.get(first);
            int t1 = e1.getInstante();
            while(first<secuencia.size() && secuencia.get(first).getInstante() == t1){//Borrar eventos en el mismo instante
               D.add(secuencia.get(first++));
            }
            lastState = secuencia.subList(first, last+1);
            if(lastState.isEmpty()){
               first = last+1;
               return null;
            }
            return lastState;
         }


         Evento ek = secuencia.get(last+1);
         int tk = ek.getInstante();

         if(!lastState.isEmpty()){
            //Detectar estado con borrado de eventos
            Evento en = secuencia.get(last);  //Ultimo evento del estado
            Evento e1 = lastState.get(0);
            int t1 = e1.getInstante(), tn=en.getInstante();
            if(t1<tk-window && tn<tk-1){
               D.add(e1);
               first++;
               while(secuencia.get(first).getInstante()==t1){
                  first++;
                  D.add(e1);
               }
               lastState = secuencia.subList(first, last+1);
               return lastState;
            }else if(tn < tk-window){
               lastState = Collections.emptyList();
               first = last;
            }else if(t1<=tk-window){
               while(secuencia.get(first).getInstante() <= tk-window){
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
         return lastState;

      }




}
