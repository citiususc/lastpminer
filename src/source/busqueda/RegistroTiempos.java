package source.busqueda;

/**
 * Se registran los tiempos comunes entre algoritmos y permite registrar
 * tiempos específicos con un nombre con {@link #addOtrosTiempos(String...)}
 * para cada uno previa a la llamada de {@link #iniciar(int)} utilizando después
 * {@link #tiempo(String, int, boolean)} con el nombre registrado como primer parámetro.
 *
 * Para delimitar un tiempo debe llamarse a {@code tiempo + <nombre_del_tiempo>(int, boolean)}
 * (o {@code tiempo(<nombre_del_tiempo>,int,boolean)} si es específico) donde el entero es la iteración
 * actual y boolean es true en la primera llamada y false en la segunda. La primera llamada resta
 * el tiempo y la segunda lo suma, almacenando el lapso de tiempo transcurrido de esta manera.
 *
 * @author vanesa.graino
 *
 */
public class RegistroTiempos extends RegistroTiempoTotal {


   //tiempos acumulados
   protected long acumCandidatos;
   protected long acumSoporte;
   protected long acumConsistencia;
   protected long acumFundir;
   protected long acumAsociaciones;
   protected long acumModelo;
   protected long acumCalcula;
   protected long acumPurgar;

   public RegistroTiempos() {
      // empty
   }

   @Override
   public long[] getTiempos(){
      long[] tiempos = new long[9];
      tiempos[0]=acumCandidatos;
      tiempos[1]=acumSoporte;
      tiempos[2]=acumConsistencia;
      tiempos[3]=acumFundir;
      tiempos[4]=acumAsociaciones;
      tiempos[5]=acumModelo;
      tiempos[6]=acumCalcula;
      tiempos[7]=acumPurgar;
      tiempos[8]=tiempoTotal;
      return tiempos;
   }

   @Override
   public void tiempo(String name, int iteracion, boolean inicio){
      long t = (inicio? -1 : 1) * System.currentTimeMillis();
      int index = otrosTiemposNombres.indexOf(name);
      long[] tiempos = otrosTiemposValores.get(index);
      tiempos[iteracion] += t;
   }

   @Override
   public void tiempoCandidatas(int iteracion, boolean inicio){
      long t = (inicio? -1 : 1) * System.currentTimeMillis();
      acumCandidatos += t;
      tiemposCandidatos[iteracion] += t;
   }

   @Override
   public long tiempoSoporte(int iteracion, boolean inicio){
      long t = (inicio? -1 : 1) * System.currentTimeMillis();
      acumSoporte += t;
      tiemposSoporte[iteracion] += t;
      return tiemposSoporte[iteracion];
   }

   @Override
   public void tiempoConsistencia(int iteracion, boolean inicio){
      long t = (inicio? -1 : 1) * System.currentTimeMillis();
      acumConsistencia += t;
      tiemposConsistencia[iteracion] += t;
   }

   @Override
   public void tiempoFundir(int iteracion, boolean inicio){
      long t = (inicio? -1 : 1) * System.currentTimeMillis();
      acumFundir += t;
      tiemposFundir[iteracion] += t;
   }

   @Override
   public void tiempoAsociaciones(int iteracion, boolean inicio){
      long t = (inicio? -1 : 1) * System.currentTimeMillis();
      acumAsociaciones += t;
      tiemposAsociaciones[iteracion] += t;
   }

   @Override
   public void tiempoModelo(int iteracion, boolean inicio){
      long t = (inicio? -1 : 1) * System.currentTimeMillis();
      acumModelo += t;
      tiemposModelo[iteracion] += t;
   }

   @Override
   public void tiempoCalcula(int iteracion, boolean inicio){
      long t = (inicio? -1 : 1) * System.currentTimeMillis();
      acumCalcula += t;
      tiemposCalcula[iteracion] += t;
   }

   @Override
   public void tiempoPurgar(int iteracion, boolean inicio){
      long t = (inicio? -1 : 1) * System.currentTimeMillis();
      acumPurgar += t;
      tiemposPurgar[iteracion] += t;
   }

   @Override
   public long tiempoIteracion(int iteracion, long tiempo){
      tiemposIteracion[iteracion] = tiempo;
      return tiemposIteracion[iteracion];
   }




}
