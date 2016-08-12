package source.modelo.negacion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import source.evento.Evento;
import source.excepciones.FactoryInstantiationException;
import source.modelo.IAsociacionTemporal;
import source.patron.GeneradorID;
import source.patron.Patron;
import source.restriccion.RIntervalo;

/**
 *
 *
 * Modelo para una asociación negada, sin tipos positivos.
 * No tiene patrones ni se busca, ni tiene matriz de restricciones, sólo
 * la lista de tipos de eventos que niega. Este tipo de asociación se usa
 * en la versión que maneja negación para facilitar la creación de modelos.
 * @author vanesa.graino
 *
 */
public class ModeloAsociacionNegada implements IAsociacionConNegacion {

   public final String[] negados;

   public ModeloAsociacionNegada(String[] negados) {
      this.negados = negados;
   }

   @Override
   public String[] getTipos() {
      return new String[0];
   }

   @Override
   public void addPatron(Patron patron) {
      // No procede
   }

   @Override
   public List<Patron> getPatrones() {
      return Collections.emptyList();
   }

   @Override
   public Patron getPatron(int index) {
      return null;
   }

   @Override
   public int getSoporte() {
      return 0;
   }

   @Override
   public List<RIntervalo> getRestricciones(String desde, String hacia) {
      return Collections.emptyList();
   }

   @Override
   public List<RIntervalo> getRestricciones(String tipo) {
      return Collections.emptyList();
   }

   @Override
   public List<RIntervalo> getRestricciones(boolean filtrar) {
      return Collections.emptyList();
   }

   @Override
   public void recibeEvento(int sid, Evento ev, boolean savePatternInstances) {
      //No procede
   }

   @Override
   public void saleEventoNegado(int sid, Evento ev, boolean savePatternInstances) {
      // No se hace nada
   }

   @Override
   public int calculaPatrones(int supmin, String patternClassName,
         GeneradorID genID, boolean savePatternInstances)
         throws FactoryInstantiationException {
      //No procede
      return 0;
   }

   @Override
   public int getVentana() {
      //No procede
      return 0;
   }



   @Override
   public String toString(){
      return "Modelo: " + toStringSinPatrones()  + " - Numero de patrones: 0\n";
   }

   @Override
   public String[] getTiposNegados() {
      return negados;
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
   public boolean necesitaPurga(int minFreq) {
      return false;
   }

   @Override
   public int size() {
      return negados.length;
   }

   @Override
   public String toStringSinPatrones() {
      //return "[" + PREF_NEG + String.join(", " + PREF_NEG, negados) + "]";
      return "[" + PREF_NEG + StringUtils.join(negados, ", " + PREF_NEG) + "]";
   }

   @Override
   public List<String> getTiposConNegacion(){
      //TODO habrá una forma mejor?
      List<String> lista = new ArrayList<String>(negados.length);
      for(String tipo : negados){
         lista.add(PREF_NEG + tipo);
      }
      return lista;
   }

   @Override
   public String getUltimoTipo() {
      return negados[negados.length-1];
   }

   @Override
   public int compareTo(IAsociacionTemporal o) {
      // TODO Auto-generated method stub
      return 0;
   }

}
