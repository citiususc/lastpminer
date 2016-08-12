package source.modelo.episodios;

import java.util.Arrays;
import java.util.List;
//import java.util.logging.Logger;


import source.evento.Episodio;
import source.evento.Evento;
import source.modelo.IAsociacionConEpisodios;
import source.modelo.Modelo;
import source.patron.Patron;

/*
 *   Variante de 'Modelo' que pretende incorporar conocimiento de que ciertos eventos se pueden organizar como
 * episodios. La naturaleza de estos episodios indica que uno de los eventos debe ocurrir antes que el otro, y
 * además se limita la combinación de eventos dentro de la ventana para formar ocurrencias durante la lectura
 * de un evento. En concreto, un evento de finalización solo se puede combinar con un evento de inicio inmedia-
 * tamente anterior, de forma que si A inicia el episodio y B lo termina, y en la ventana tenemos AABB, la pri-
 * mera B se asociará únicamente a la primera A, y la segunda B a la segunda A.
 */
public class ModeloEpisodios extends Modelo implements IAsociacionConEpisodios{
   //private static final Logger LOGGER = Logger.getLogger(ModeloEpisodios.class.getName());
   private static final long serialVersionUID = 254351438848892157L;

   /*
    * Atributos propios
    */
   protected EpisodiosWrapper episodios;

   /*
    * Constructores
    */

   public ModeloEpisodios(String[] tipos, List<Episodio> episodios, int ventana, Integer frecuencia){
      super(tipos, ventana,frecuencia);
      this.episodios = new EpisodiosWrapper(episodios, tipos);
   }

   public ModeloEpisodios(String[] tipos, List<Episodio> episodios, int ventana,
         List<Patron> patrones, Integer frecuencia){
      super(tipos, ventana, patrones, frecuencia);
      this.episodios = new EpisodiosWrapper(episodios, tipos);
   }

   /*
    * Métodos propios
    */

   /*
    *   Dados unos límites y unos índices, devuelve los índices para una nueva combinación de eventos
    * de la ventana, o 'null' si no hay combinaciones posibles o éstas ya se agotaron.
    *   Redefinido para tratar con el conocimiento de los episodios. El método se divide en dos partes.
    * La primera actúa sobre aquellos eventos que no son episodio (que son los últimos tipos en la lista
    * 'tiposReordenados') y actúa como en la versión tradicional. La segunda parte se centra en los tipos
    * que forman parte de episodio,
    */
   @Override
   protected int[] siguienteCombinacion(int[] tam, int[] indices, int index, String tipo){
      if(episodios.getEventosDeEpisodios()==0){
         return super.siguienteCombinacion(tam, indices, index, tipo);
      }
      return episodios.siguienteCombinacion(tam, indices, index, tipo);
   }

   @Override
   protected void actualizaVentana(int sid, Evento evento) {
      //int index = episodios.getTiposReordenados().indexOf(evento.getTipo());
      String tipo = evento.getTipo();
      int index = Arrays.asList(episodios.getTiposReordenados()).indexOf(tipo);
      actualizaVentana(sid, evento, tipo, evento.getInstante(), index, getTipos().length );
   }

   @Override
   protected boolean actualizaVentana(int sid, Evento ev, String tipo, int tmp,
         int index, int tSize) {
      int[][] abiertas = getAbiertas(), limites = getLimites();
      int[] tam = getTam();
      boolean seguir = episodios.actualizarVentana(abiertas, limites, tam, ventana, index, tmp, sid, ultimoSid);
      ultimoSid = sid;
      return seguir;
   }

   /*
    * Redefinición del método para tratar con el conocimiento de episodios.
    *   -) Los eventos se insertan siguiendo el orden de 'tiposReordenados'.
    *   -) Una vez calculados los índices, se construye la instancia usando
    */
   @Override
   public void recibeEvento(int sid, Evento ev, boolean savePatternInstances){
      int eventosDeEpisodios = episodios.getEventosDeEpisodios();
      if(eventosDeEpisodios == 0){
         super.recibeEvento(sid, ev, savePatternInstances);
         return;
      }

      String[] tiposReordenados = episodios.getTiposReordenados();
      String tipo = ev.getTipo();
      int index = Arrays.asList(tiposReordenados).indexOf(tipo);
      int tSize = tiposReordenados.length;
      int tmp = ev.getInstante();

      // Actualizar índices fin e inicio para adaptarse a la ventana
      if(!actualizaVentana(sid, ev, tipo, tmp, index, tSize)){
         return;
      }

      int[] tam = getTam(); // Tamaño de las listas circulares, numero de elementos en ellas
      int[] indices = episodios.primerosIndices(index, tam);
      if(indices == null){
         return;
      }

      // Actualizar frecuencias

      int[] patFrecLocal = new int[getPatrones().size()];
      int[][] abiertas = getAbiertas(), limites = getLimites();
      int frecuenciaLocal=0;

      int[] instancia = new int[tSize];
      instancia[index] = tmp;

      do{ // Recorre cada lista
         // Comprobar si pertenece a algun patrón
         episodios.fijarInstancia(tSize, index, tmp, abiertas, limites, indices, instancia, ventana);

         if(comprobarPatrones(instancia, patFrecLocal, sid, savePatternInstances)){
            frecuenciaLocal++;
         }
         indices = siguienteCombinacion(tam, indices, index, tipo);

      }while(indices != null);
      addFrecuencias(frecuenciaLocal,patFrecLocal);
   }

   @Override
   public List<Episodio> getEpisodios(){
      return episodios.getEpisodios();
   }

   @Override
   public boolean sonEpisodiosCompletos(){
      return episodios.isEpisodiosCompletos();
   }

   /*
    * Métodos para que las subclases puedan acceder a la reordenación de tipos
    */
   protected String[] getTiposReordenados() {
      return episodios.getTiposReordenados();
   }

   protected int[] getEquivalenciasTipos() {
      return episodios.getEquivalenciasTipos();
   }

   protected int getEventosDeEpisodios() {
      return episodios.getEventosDeEpisodios();
   }

}