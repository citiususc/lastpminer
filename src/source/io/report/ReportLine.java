package source.io.report;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReportLine {
   private static final Logger LOGGER = Logger.getLogger(ReportLine.class.getName());

   private String algoritmo;
   private String modo;
   private GregorianCalendar hora;
   private Integer numEjecucion;
   private String coleccion;

   private Long calculoGeneracionCandidatos;
   private Long calculoSoporte;
   private Long calculoConsistencia;
   private Long calculoCombinacion;
   private Long calculoAsociacion;
   private Long calculoModelo;
   private Long calculoTotal;

   private Integer totalSecuenciasGeneradas;
   private Integer totalSecuenciasFrecuentes;
   private Integer totalPatronesFrecuentes;
   private Integer totalPatronesGenerados;
   private Integer totalPatronesInconsistentes; //inconsistentes en generacion
   private Integer totalPatronesRechazados; //sin generar

   public ReportLine(){
      //Constructor vacío
   }

   public ReportLine(String algoritmo, String modo, GregorianCalendar hora,
         Integer numEjecucion, String coleccion,
         Long calculoGeneracionCandidatos, Long calculoSoporte,
         Long calculoConsistencia, Long calculoCombinacion,
         Long calculoAsociacion, Long calculoModelo, Long calculoTotal,
         int totalSecuenciasGeneradas, int totalSecuenciasFrecuentes,
         int totalPatronesFrecuentes, int totalPatronesGenerados,
         int totalPatronesInconsistentes, int totalPatronesRechazados) {
      super();
      this.algoritmo = algoritmo;
      this.modo = modo;
      this.hora = hora;
      this.numEjecucion = numEjecucion;
      this.coleccion = coleccion;
      setCalculos(calculoGeneracionCandidatos, calculoSoporte, calculoConsistencia, calculoCombinacion,
            calculoAsociacion, calculoModelo, calculoTotal);
      setTotales(totalSecuenciasGeneradas, totalSecuenciasFrecuentes, totalPatronesFrecuentes,
            totalPatronesGenerados, totalPatronesInconsistentes, totalPatronesRechazados);
   }

   public void setCalculos(Long calculoGeneracionCandidatos, Long calculoSoporte,
         Long calculoConsistencia, Long calculoCombinacion,
         Long calculoAsociacion, Long calculoModelo, Long calculoTotal){
      this.calculoGeneracionCandidatos = calculoGeneracionCandidatos;
      this.calculoSoporte = calculoSoporte;
      this.calculoConsistencia = calculoConsistencia;
      this.calculoCombinacion = calculoCombinacion;
      this.calculoAsociacion = calculoAsociacion;
      this.calculoModelo = calculoModelo;
      this.calculoTotal = calculoTotal;
   }

   public void setTotales(int totalSecuenciasGeneradas, int totalSecuenciasFrecuentes,
         int totalPatronesFrecuentes, int totalPatronesGenerados,
         int totalPatronesInconsistentes, int totalPatronesRechazados){
      this.totalSecuenciasGeneradas = totalSecuenciasGeneradas;
      this.totalSecuenciasFrecuentes = totalSecuenciasFrecuentes;
      this.totalPatronesFrecuentes = totalPatronesFrecuentes;
      this.totalPatronesGenerados = totalPatronesGenerados;
      this.totalPatronesInconsistentes = totalPatronesInconsistentes;
      this.totalPatronesRechazados = totalPatronesRechazados;
   }

   public String getAlgoritmo() {
      return algoritmo;
   }

   public void setAlgoritmo(String algoritmo) {
      this.algoritmo = algoritmo;
   }

   public String getModo() {
      return modo;
   }

   public void setModo(String modo) {
      this.modo = modo;
   }

   public GregorianCalendar getHora() {
      return hora;
   }

   public void setHora(GregorianCalendar hora) {
      this.hora = hora;
   }

   public Integer getNumEjecucion() {
      return numEjecucion;
   }

   public void setNumEjecucion(Integer numEjecucion) {
      this.numEjecucion = numEjecucion;
   }

   public String getColeccion() {
      return coleccion;
   }

   public void setColeccion(String coleccion) {
      this.coleccion = coleccion;
   }

   public Long getCalculoGeneracionCandidatos() {
      return calculoGeneracionCandidatos;
   }

   public void setCalculoGeneracionCandidatos(Long calculoGeneracionCandidatos) {
      this.calculoGeneracionCandidatos = calculoGeneracionCandidatos;
   }

   public Long getCalculoSoporte() {
      return calculoSoporte;
   }

   public void setCalculoSoporte(Long calculoSoporte) {
      this.calculoSoporte = calculoSoporte;
   }

   public Long getCalculoConsistencia() {
      return calculoConsistencia;
   }

   public void setCalculoConsistencia(Long calculoConsistencia) {
      this.calculoConsistencia = calculoConsistencia;
   }

   public Long getCalculoCombinacion() {
      return calculoCombinacion;
   }

   public void setCalculoCombinacion(Long calculoCombinacion) {
      this.calculoCombinacion = calculoCombinacion;
   }

   public Long getCalculoAsociacion() {
      return calculoAsociacion;
   }

   public void setCalculoAsociacion(Long calculoAsociacion) {
      this.calculoAsociacion = calculoAsociacion;
   }

   public Long getCalculoModelo() {
      return calculoModelo;
   }

   public void setCalculoModelo(Long calculoModelo) {
      this.calculoModelo = calculoModelo;
   }

   public Long getCalculoTotal() {
      return calculoTotal;
   }

   public void setCalculoTotal(Long calculoTotal) {
      this.calculoTotal = calculoTotal;
   }

   public int getTotalSecuenciasGeneradas() {
      return totalSecuenciasGeneradas;
   }

   public void setTotalSecuenciasGeneradas(int totalSecuenciasGeneradas) {
      this.totalSecuenciasGeneradas = totalSecuenciasGeneradas;
   }

   public int getTotalSecuenciasFrecuentes() {
      return totalSecuenciasFrecuentes;
   }

   public void setTotalSecuenciasFrecuentes(int totalSecuenciasFrecuentes) {
      this.totalSecuenciasFrecuentes = totalSecuenciasFrecuentes;
   }

   public int getTotalPatronesFrecuentes() {
      return totalPatronesFrecuentes;
   }

   public void setTotalPatronesFrecuentes(int totalPatronesFrecuentes) {
      this.totalPatronesFrecuentes = totalPatronesFrecuentes;
   }

   public int getTotalPatronesGenerados() {
      return totalPatronesGenerados;
   }

   public void setTotalPatronesGenerados(int totalPatronesGenerados) {
      this.totalPatronesGenerados = totalPatronesGenerados;
   }

   public int getTotalPatronesInconsistentes() {
      return totalPatronesInconsistentes;
   }

   public void setTotalPatronesInconsistentes(int totalPatronesInconsistentes) {
      this.totalPatronesInconsistentes = totalPatronesInconsistentes;
   }

   public int getTotalPatronesRechazados() {
      return totalPatronesRechazados;
   }

   public void setTotalPatronesRechazados(int totalPatronesRechazados) {
      this.totalPatronesRechazados = totalPatronesRechazados;
   }

   public String[] toStringArray(){
      Field[] fields = ReportLine.class.getDeclaredFields();
      String[] array = new String[fields.length];
      //for(Field f : fields){
      for(int i=0; i<fields.length; i++){
         Field f = fields[i];
         try {
            Object o = f.get(this);
            if(o == null){
               array[i] = "";
            } else if(o instanceof GregorianCalendar){
               SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRANCE);
               array[i] = formatter.format(((GregorianCalendar)o).getTime());
            }else if(o instanceof String){
               array[i] = (String)o;
            }else{
               array[i] = o.toString();
            }
         } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Excepción al modificar el atributo", e);
         } catch (IllegalAccessException e) {
            LOGGER.log(Level.WARNING, "Excepción al modificar el atributo", e);
         }
      }
      return array;
   }

   public boolean setAttribute(String attribute, String value){
      Field f;
      try {
         f = ReportLine.class.getDeclaredField(attribute);
         if(f != null){
            if(f.getType() == Integer.class){
               f.set(this, Integer.valueOf(value));
            }else if(f.getType() == GregorianCalendar.class){
               SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRANCE);
               GregorianCalendar greg = new GregorianCalendar();
               greg.setTime(formatter.parse(value));
               f.set(this, greg);
            }else{
               f.set(this, value);
            }
         }
      } catch (NoSuchFieldException e) {
         LOGGER.log(Level.WARNING, "Excepción al modificar el atributo", e);
      } catch (SecurityException e) {
         LOGGER.log(Level.WARNING, "Excepción al modificar el atributo", e);
      } catch (IllegalArgumentException e) {
         LOGGER.log(Level.WARNING, "Excepción al modificar el atributo", e);
      } catch (IllegalAccessException e) {
         LOGGER.log(Level.WARNING, "Excepción al modificar el atributo", e);
      } catch (ParseException e) {
         LOGGER.log(Level.WARNING, "Excepción al modificar el atributo", e);
      }

      return false;
   }


}
