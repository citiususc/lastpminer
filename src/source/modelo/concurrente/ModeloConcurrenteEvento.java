package source.modelo.concurrente;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import source.evento.Evento;
import source.modelo.IAsociacionEvento;
import source.modelo.IAsociacionTemporal;
import source.patron.GeneradorID;
import source.patron.Patron;
import source.restriccion.RIntervalo;

public class ModeloConcurrenteEvento implements IAsociacionTemporalConcurrente, IAsociacionEvento, Serializable{
   private static final long serialVersionUID = -3454200842856770596L;


   protected String tipoEvento;
   private int frecuencia;
   protected List<ModeloParaleloEventoHilo> modelos;

   public ModeloConcurrenteEvento(String tipoEvento, Integer frecuencia, int numHilos){
      this.tipoEvento = tipoEvento;
      this.frecuencia = frecuencia==null? 0 : frecuencia;
      crearModelosHilos(numHilos);
   }

   @Override
   public String[] getTipos() {
      return new String[]{tipoEvento};//Arrays.asList(tipoEvento);
   }

   @Override
   public void addPatron(Patron patron) {
      throw new UnsupportedOperationException("addPatron no implementado");
   }

   @Override
   public List<Patron> getPatrones() {
      throw new UnsupportedOperationException("getPatrones no implementado");
   }

   @Override
   public Patron getPatron(int index) {
      throw new UnsupportedOperationException("getPatron no implementado");
   }

   @Override
   public int getSoporte() {
      return frecuencia;
   }

   @Override
   public List<RIntervalo> getRestricciones(String desde, String hacia) {
      throw new UnsupportedOperationException("getRestricciones no implementado");
   }

   @Override
   public List<RIntervalo> getRestricciones(String tipo) {
      throw new UnsupportedOperationException("getRestricciones no implementado");
   }

   @Override
   public List<RIntervalo> getRestricciones(boolean filtrar) {
      throw new UnsupportedOperationException("getRestricciones no implementado");
   }

   @Override
   public void recibeEvento(int sid, Evento ev, boolean savePatternInstances) {
      throw new UnsupportedOperationException("Este método no puede llamarse para un modelo concurrente");
   }

   @Override
   public int calculaPatrones(int supmin, String patternClassName, GeneradorID genID, boolean savePatternInstances) {
      throw new UnsupportedOperationException("calculaPatrones no implementado");
   }

   @Override
   public int getVentana() {
      throw new UnsupportedOperationException("getVentana no implementado");
   }

   @Override
   public void recibeEvento(int hilo, int sid, Evento ev, boolean savePatternInstances) {
      modelos.get(hilo).recibeEvento(sid, ev, savePatternInstances);
   }


   protected void crearModelosHilos(int numHilos){
      modelos = new ArrayList<ModeloParaleloEventoHilo>();
      for(int i=0;i<numHilos;i++){
         modelos.add(new ModeloParaleloEventoHilo());
      }
   }


   @Override
   public String toString(){
      return "Modelo: [" + tipoEvento + "] - Numero de patrones: 0\n";
   }

   @Override
   public String toStringSinPatrones(){
      return "[" + tipoEvento + "]";
   }

   @Override
   public int size(){
      return 1;
   }

   /*
    * Clases privadas
    */

   protected class ModeloParaleloEventoHilo {
      protected int frecuenciaHilo;

      public void recibeEvento(int sid, Evento ev, boolean savePatternInstances){
         //if(ev.getTipo() == tipoEvento){
         if(ev.getTipo().equals(tipoEvento)){//TODO falla intern?
            frecuenciaHilo++;
         }
      }

      protected void agregarResultados(){
         //frecuencia y patFrec
         frecuencia+=frecuenciaHilo;
      }
   }


   @Override
   public boolean necesitaPurga(int minFreq) {
      return frecuencia<minFreq;
   }

   @Override
   public String getTipoEvento() {
      return tipoEvento;
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
