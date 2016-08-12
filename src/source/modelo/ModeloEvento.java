package source.modelo;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import source.evento.Evento;
import source.patron.GeneradorID;
import source.patron.Patron;
import source.restriccion.RIntervalo;

/**
 * Modelo para un único tipo de evento
 * @author vanesa.graino
 *
 */
public class ModeloEvento implements IAsociacionEvento, Serializable, IAsociacionDeHilo{
   private static final long serialVersionUID = -834345860952454930L;


   private int hilo = Modelo.getHiloAsignado();

   protected String tipoEvento;
   protected int frecuencia;

   public ModeloEvento(String tipoEvento, Integer frecuencia){
      this.tipoEvento = tipoEvento;
      this.frecuencia = frecuencia==null? 0 : frecuencia;
   }

   @Override
   public String[] getTipos() {
      return new String[]{tipoEvento}; //Arrays.asList(tipoEvento);
   }

   @Override
   public String getTipoEvento(){
      return tipoEvento;
   }

   @Override
   public void addPatron(Patron patron) {
      throw new UnsupportedOperationException("(ModeloEvento) addPatron no implementado");
   }

   @Override
   public List<Patron> getPatrones() {
      //throw new UnsupportedOperationException("(ModeloEvento) getPatrones no implementado");
      return Collections.emptyList();
   }

   @Override
   public Patron getPatron(int index) {
      //throw new UnsupportedOperationException("(ModeloEvento) getPatron no implementado");
      return null;
   }

   @Override
   public int getSoporte() {
      return frecuencia;
   }

   @Override
   public List<RIntervalo> getRestricciones(String desde, String hacia) {
      throw new UnsupportedOperationException("(ModeloEvento) getRestricciones no implementado");
   }

   @Override
   public List<RIntervalo> getRestricciones(String tipo) {
      //throw new UnsupportedOperationException("(ModeloEvento) getRestricciones no implementado");
      return Collections.emptyList();
   }

   @Override
   public List<RIntervalo> getRestricciones(boolean filtrar) {
      //throw new UnsupportedOperationException("(ModeloEvento) getRestricciones no implementado");
      return Collections.emptyList();
   }

   @Override
   public void recibeEvento(int sid, Evento ev, boolean savePatternInstances) {
      //if(ev.getTipo() == tipoEvento){
      //if(ev.getTipo().equals(tipoEvento)){//TODO falla intern?
         frecuencia++;
      //}
   }

   /*
    * (non-Javadoc)
    * @see source.modelo.IAsociacionTemporal#calculaPatrones(int, java.lang.String, source.patron.GeneradorID, boolean)
    */
   @Override
   public int calculaPatrones(int supmin, String patternClassName, GeneradorID genID, boolean savePatternInstances) {
      //throw new UnsupportedOperationException("(ModeloEvento) calculaPatrones no implementado");
      return 0;
   }

   @Override
   public int getVentana() {
      //return ventana;
      throw new UnsupportedOperationException("(ModeloEvento) getVentana no implementado");
   }

   @Override
   public String toString(){
      //return "Modelo: [" + tipoEvento + "]. Soporte: " + frecuencia;
      return "Modelo: [" + tipoEvento + "] - Numero de patrones: 0\n";
   }

   @Override
   public String toStringSinPatrones(){
      return "[" + tipoEvento + "]";
   }

   @Override
   public void setHilo(int hilo) {
      this.hilo = hilo;
   }

   @Override
   public int getHilo() {
      return hilo;
   }

   @Override
   public boolean necesitaPurga(int minFreq) {
      return frecuencia<minFreq;
   }

   @Override
   public int size(){
      return 1;
   }

   @Override
   public String getUltimoTipo() {
      return tipoEvento;
   }

   @Override
   public int compareTo(IAsociacionTemporal o) {
      if(o.size() != 1){
         return -1;
      }
      return tipoEvento.compareTo(o.getTipos()[0]);
   }
}
