package source.modelo.condensacion.episodios;

import java.util.List;
//import java.util.logging.Logger;

import source.evento.Episodio;
import source.evento.Evento;
import source.modelo.condensacion.IModeloTontoEpisodios;
import source.modelo.condensacion.ISuperModelo;
import source.modelo.episodios.ModeloEpisodiosDFE;
import source.patron.Patron;
import source.patron.PatronDictionaryFinalEvent;

/**
 * Versión de {@link ModeloDictionaryTonto} que maneja episodios.
 * @author vanesa.graino
 *
 */
public class ModeloEpisodiosDFETonto extends ModeloEpisodiosDFE implements IModeloTontoEpisodios {
   //private static final Logger LOGGER = Logger.getLogger(ModeloEpisodiosDFETonto.class.getName());

   /**
    * Correspondencia indices del modelo con los indices del SuperModelo
    */
   protected int[] indicesColeccion;
   protected int[] tamColeccion; /* tam para todos los tipos de eventos */

   /**
    *
    */
   private static final long serialVersionUID = -7580264182609253268L;

   public ModeloEpisodiosDFETonto(String[] tipos, List<Episodio> episodios,
         int ventana, ISuperModelo supermodelo) {
      super(tipos, episodios, ventana, null);
      //indices = new int[tipos.size()];
      //fijarEstructuras(supermodelo);
      indicesColeccion = supermodelo.fijarEstructuras(this);
   }

   public ModeloEpisodiosDFETonto(String[] tipos, List<Episodio> episodios,
         int ventana, List<Patron> patrones, ISuperModelo supermodelo) {
      super(tipos, episodios, ventana, patrones, null);
      //indices = new int[tipos.size()];
      //fijarEstructuras(supermodelo);
      indicesColeccion = supermodelo.fijarEstructuras(this);
   }

   @Deprecated
   @Override
   public void actualizaVentana(int sid, Evento evento){
      //se encarga el supermodelo
   }

   @Override
   protected boolean actualizaVentana(int sid, Evento ev, String tipo, int tmp,
         int index, int tSize) {
      return actualizaTamComprueba();
   }

   private boolean actualizaTamComprueba(){
      int[] tam = getTam();
      for(int i=0;i<indicesColeccion.length;i++){
         tam[i] = tamColeccion[indicesColeccion[i]];
         if(tam[i]<=0){ return false; }
      }
      return true;
   }

   protected boolean actualizaTam(){
      int[] tam = getTam();
      for(int i=0;i<indicesColeccion.length;i++){
         tam[i] = tamColeccion[indicesColeccion[i]];
      }
      return true;
   }

   /*@Override
   public void recibeEvento(int sid, Evento ev, boolean savePatternInstances){
      //Aquí no hay que hacer esto, hay que instanciar el modelo correcto con o sin episodios
//      if(episodios.getEventosDeEpisodios()==0){
//         super.recibeEvento(sid, ev, savePatternInstances);
//         return;
//      }
      if(!actualizaTamComprueba()){
         return;
      }

      String[] tiposReordenados = episodios.getTiposReordenados();
      String tipo = ev.getTipo();
      int index = Arrays.asList(tiposReordenados).indexOf(tipo);

      int tSize = tiposReordenados.length;

      int[] tam = getTam(); // Tamaño de las listas circulares, numero de elementos en ellas

      int[] indices = episodios.primerosIndices(index, tam);
      if(indices == null){
         return;
      }

      // Actualizar frecuencias
      int tmp = ev.getInstante();
      List<Patron> patrones = getPatrones();

      int pSize = patrones.size();
      int[] patFrecLocal = new int[pSize];
      int frecuenciaLocal=0;

      int[][] abiertas = getAbiertas(); // Listas circulares, contienen instantes temporales
      int[][] limites = getLimites(); // Límites de las listas circulares, contienen índices a 'abiertas'

      int[] instancia = new int[tSize];
      instancia[index] = tmp;

      do{ // Recorre cada lista
         // Comprobar si pertenece a algun patrón
         episodios.fijarInstancia(tSize, index, tmp, abiertas, limites,
               indices, instancia, ventana);

         if(comprobarPatrones(instancia, patFrecLocal, sid, savePatternInstances)){
            frecuenciaLocal++;
         }
         indices = siguienteCombinacion(tam, indices, index, tipo);

      }while(indices != null);
      addFrecuencias(frecuenciaLocal,patFrecLocal);
   }*/

   /*
    * Este método en la clase padre está deprecated!!!!!
    * (non-Javadoc)
    * @see source.modelo.ModeloEventoFinal#comprobarPatrones(int[], int[], int, java.lang.String, int, boolean)
    */
   /*@Override
   protected boolean comprobarPatrones(int[] instancia, int[] patFrecLocal,
         int sid, boolean savePatternInstances) {
      return super.comprobarPatrones(instancia, patFrecLocal, sid, savePatternInstances);
      int i=0;
      boolean encontrado = false;
      for(Patron patron : patrones){
         if(patron.representa(sid,instancia, savePatternInstances)){
            encontrado = true;
            // TODO No debería llamarse al método del patrón ??
            //((PatronEventoFinal)patron).encontrado();
            patFrecLocal[i]++;
            break;
         }
         i++;
      }
      return encontrado;
   }*/

   //@Override
   /*protected int fijarInstancia(int tSize, int index, int tmp, int[][] abiertas, int[][] limites, int[] indices, int[] instancia){
      return episodios.fijarInstancia(tSize, index, tmp, abiertas, limites, indices, instancia, ventana);
   }*/

   @Override
   public void recibeEvento(int sid, Evento evento, boolean savePatternInstances,
         List<PatronDictionaryFinalEvent> aComprobar, List<Patron> encontrados) {
      actualizaTam();
      super.recibeEvento(sid, evento, savePatternInstances, aComprobar, encontrados);
   }

   @Override
   public void setTamColeccion(int[] tam) {
      this.tamColeccion = tam;
   }

   /*
    * Publico
    * (non-Javadoc)
    * @see source.modelo.Modelo#getLimites()
    */
   @Override
   public int[][] getLimites() {
      return super.getLimites();
   }

   /*
    * Publico
    * (non-Javadoc)
    * @see source.modelo.Modelo#getAbiertas()
    */
   @Override
   public int[][] getAbiertas() {
      return super.getAbiertas();
   }

   @Override
   public int getEventosDeEpisodios() {
      return episodios.getEventosDeEpisodios();
   }

   @Override
   public String[] getTiposReordenados() {
      return episodios.getTiposReordenados();
   }
}
