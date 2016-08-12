package source.busqueda;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Sólo registra el tiempo total ignorando todos los registros parciales
 * (como el de generación, soporte, etc).
 * @author vanesa.graino
 *
 */
public class RegistroTiempoTotal {


   protected List<String> otrosTiemposNombres;
   protected List<long[]> otrosTiemposValores;

   protected long[] tiemposCandidatos;
   protected long[] tiemposSoporte;
   protected long[] tiemposConsistencia;
   protected long[] tiemposFundir;
   protected long[] tiemposAsociaciones;
   protected long[] tiemposModelo;
   protected long[] tiemposCalcula;
   protected long[] tiemposPurgar;
   protected long[] tiemposIteracion; //tiempo total consumido por cada iteración


   protected long tiempoTotal;

   public RegistroTiempoTotal() {
      // Vacío
   }

   /**
    * Permite añadir otros tiempos.
    * Después de llamar a este método hay que llamar a {@link #iniciar(int)}.
    * @param otrosTiempos El nombre o nombres con que se registran los otros tiempos.
    */
   public void addOtrosTiempos(String... otrosTiempos){
      if(otrosTiemposNombres == null){ //No había otros
         otrosTiemposNombres = new ArrayList<String>(otrosTiempos.length);
         otrosTiemposValores = new ArrayList<long[]>(otrosTiempos.length);
      }
      otrosTiemposNombres.addAll(Arrays.asList(otrosTiempos));
      ((ArrayList<long[]>)otrosTiemposValores).ensureCapacity(otrosTiemposNombres.size());
   }

   public long[] getTiempos(){
      long[] tiempos = new long[9];
      tiempos[0]=-1;
      tiempos[1]=-1;
      tiempos[2]=-1;
      tiempos[3]=-1;
      tiempos[4]=-1;
      tiempos[5]=-1;
      tiempos[6]=-1;
      tiempos[7]=-1;
      tiempos[8]=tiempoTotal;
      return tiempos;
   }

   public void iniciar(int tSize){
      tiemposCandidatos = new long[tSize];
      tiemposSoporte = new long[tSize];
      tiemposConsistencia = new long[tSize];
      tiemposFundir = new long[tSize];
      tiemposAsociaciones = new long[tSize];
      tiemposModelo = new long[tSize];
      tiemposCalcula = new long[tSize];
      tiemposPurgar = new long[tSize];
      tiemposIteracion = new long[tSize];

      if(otrosTiemposNombres != null){
         otrosTiemposValores.clear();
         for(int i=0; i<otrosTiemposNombres.size(); i++){
            otrosTiemposValores.add(new long[tSize]);
         }
         //otrosTiemposValores.addAll(Collections.nCopies(otrosTiemposNombres.size(), new Long[tSize]));
      }
   }

   /**
    * Permite obtener el array de tiempos para un tiempo específico registrado por nombre
    * con {@link #addOtrosTiempos(String...)}.
    * @param name
    * @return
    */
   public long[] getTiempos(String name){
      return otrosTiemposValores.get(otrosTiemposNombres.indexOf(name));
   }

   public void tiempo(String name, int iteracion, boolean inicio){

   }

   public void tiempoCandidatas(int iteracion, boolean inicio){
      //Empty
   }

   public void tiempoConsistencia(int iteracion, boolean inicio){
      //Empty
   }

   public void tiempoFundir(int iteracion, boolean inicio){
      //Empty
   }

   public void tiempoAsociaciones(int iteracion, boolean inicio){
      //Empty
   }

   public long tiempoSoporte(int iteracion, boolean inicio){
      return -1;
   }

   public void tiempoModelo(int iteracion, boolean inicio){
      //Empty
   }

   public void tiempoCalcula(int iteracion, boolean inicio){
      //Empty
   }

   public void tiempoPurgar(int iteracion, boolean inicio){
      //Empt
   }

   public long tiempoIteracion(int iteracion, long tiempo){
      return -1;
   }

   /*
    * Getters and setters
    */

   public long[] getTiemposCandidatos() {
      return tiemposCandidatos;
   }

   public long[] getTiemposSoporte() {
      return tiemposSoporte;
   }

   public long[] getTiemposConsistencia() {
      return tiemposConsistencia;
   }

   public long[] getTiemposFundir() {
      return tiemposFundir;
   }

   public long[] getTiemposAsociaciones() {
      return tiemposAsociaciones;
   }

   public long[] getTiemposModelo() {
      return tiemposModelo;
   }

   public long[] getTiemposCalcula() {
      return tiemposCalcula;
   }

   public long[] getTiemposPurgar() {
      return tiemposPurgar;
   }

   public long[] getTiemposIteracion() {
      return tiemposIteracion;
   }

   public void setTiempoTotal(long tiempoTotal){
      this.tiempoTotal = tiempoTotal;
   }


}
