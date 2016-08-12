package source.modelo.negacion;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import source.evento.Evento;
import source.modelo.IAsociacionDeHilo;
import source.modelo.IAsociacionTemporal;
import source.modelo.ModeloEvento;
import source.restriccion.RIntervalo;

/**
 * Un modelo para una asociación temporal de tamaño 1 cuyo único evento está negado.
 * @author nessa
 *
 */
public class ModeloEventoNegado extends ModeloEvento implements IAsociacionConNegacion, Serializable, IAsociacionDeHilo{

   /*
    * Parte estática
    */
   //private static final Logger LOGGER = Logger.getLogger(ModeloEventoNegado.class.getName());
   private static final long serialVersionUID = -6245278070922538036L;

   protected int ventana;
   // Último instante en el que se ha encontrado el evento negado
   protected int ultimoNegadoEncontrado = -1;
   protected int ultimoInstante = -1; /* instante de la última ocurrencia */

   /*
    * Constructores
    */

   public ModeloEventoNegado(String tipoEvento, Integer frecuencia, int ventana){
      super(tipoEvento, frecuencia);
      this.ventana = ventana;
   }

   /*
    * Otros métodos
    */

   @Override
   public void saleEventoNegado(int sid, Evento ev, boolean savePatternInstances) {
      // No se hace nada
   }

   @Override
   public void recibeEvento(int sid, Evento evento, boolean savePatternInstances) {
      // Si se ha había encontrado el evento negado pero ya no está en la ventana, actualizar
      int tmp = evento.getInstante();
      if(ultimoNegadoEncontrado > -1 && tmp-ventana>=ultimoNegadoEncontrado){
         ultimoNegadoEncontrado = -1;
      }

      String tipo = evento.getTipo();
      //if(tipo == this.tipoEvento){
      //TODO falla internalización de strings, java8????
      if(this.tipoEvento.equals(tipo)){ //Si entra el evento negado
         ultimoNegadoEncontrado = tmp;

         // Si habíamos sumado ocurrencias en ese instante hay que restarlas
         // porque acabamos de encontrar el tipo de evento negado
         if(ultimoInstante == tmp){
            this.frecuencia --;
            ultimoInstante = -1;
         }
         return;
      }

      // Es un evento distinto al evento negado
      // Dos opciones: hay un evento negado en la ventana o no
      if(ultimoNegadoEncontrado == -1 && ultimoInstante != tmp){
         //No hay evento negado
         this.frecuencia++;
         this.ultimoInstante = tmp;
      }
   }

   @Override
   public int getVentana() {
      return ventana;
   }

   @Override
   public String toString(){
      return "Modelo: " + toStringSinPatrones() + ". Soporte: " + frecuencia + "\n";
   }



   @Override
   public String[] getTipos() {
      return new String[]{}; //Arrays.asList(tipoEvento);
   }

   @Override
   public String[] getTiposNegados() {
      return new String[]{ tipoEvento };
   }

   @Override
   public List<RIntervalo> getRestricciones(boolean filtrar) {
      return Collections.emptyList();
   }

   @Override
   public boolean partePositiva() {
      return false;
   }

   @Override
   public boolean parteNegativa(){
      return true;
   }

   @Override
   public String toStringSinPatrones(){
      return "[" + PREF_NEG + tipoEvento + "]";
   }

   @Override
   public List<String> getTiposConNegacion() {
      return Arrays.asList(PREF_NEG + tipoEvento);
   }


   @Override
   public int compareTo(IAsociacionTemporal o) {
      // TODO Auto-generated method stub
      return 0;
   }

   //@Override
   public int compareTo(IAsociacionConNegacion o) {
      // TODO Auto-generated method stub
      return 0;
   }

}
