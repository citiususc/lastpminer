package source.modelo.semilla;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//import java.util.logging.Logger;

import source.evento.Episodio;
import source.evento.Evento;
import source.modelo.IAsociacionConEpisodios;
import source.modelo.clustering.IClustering;
import source.modelo.episodios.EpisodiosWrapper;
import source.patron.Patron;
import source.patron.PatronSemilla;

/*
 * Extensión de 'ModeloSemilla' que tiene por objetivo integrar las mejoras de la utilización de patrones semilla
 * con la definición de episodios de pares de tipos de eventos. Para ello se implementan los mismos métodos que
 * hicieron falta en 'ModeloEpisodios' vía copy-paste, heredando los métodos de ModeloSemilla.
 *
 * El principal añadido se encuentra en la construcción de ocurrencias a partir de los eventos de la ventana,
 * forzando una asociación 1-a-1 entre aquellos eventos que forman parte de episodios (método siguienteCombinación).
 * También se ve afectada la actualización de la ventana (principio del método 'recibeEvento'.
 */
public class ModeloSemillaEpisodios extends ModeloSemilla implements IAsociacionConEpisodios{
   //private static final Logger LOGGER = Logger.getLogger(ModeloSemillaEpisodios.class.getName());
   private static final long serialVersionUID = 2023367769469029795L;

   /*
    * Atributos
    */
   protected EpisodiosWrapper episodios;

   /*
    * Constructores
    */

   public ModeloSemillaEpisodios(String[] tipos, List<Episodio> episodios, int ventana,
         Integer frecuencia, IClustering clustering){
      super(tipos, ventana,frecuencia,clustering);
      this.episodios = new EpisodiosWrapper(episodios,tipos);
   }

   public ModeloSemillaEpisodios(String[] tipos, List<Episodio> episodios, int ventana, List<Patron> patrones,
         Integer frecuencia, IClustering clustering){
      super(tipos, ventana, patrones, frecuencia, clustering);
      this.episodios = new EpisodiosWrapper(episodios,tipos);
   }

   /*
    * Métodos
    */


   /*
    *   Dados unos límites y unos índices, devuelve los índices para una nueva combinación de eventos
    * de la ventana, o 'null' si no hay combinaciones posibles o éstas ya se agotaron.
    *   Redefinido para tratar con el conocimiento de los episodios. El método se divide en dos partes.
    * La primera actúa sobre aquellos eventos que no son episodio (que son los últimos tipos en la lista
    * 'tiposReordenados') y actúa como en la versión tradicional. La segunda parte se centra en los tipos
    * que forman parte de episodio,
    */
   protected int[] siguienteCombinacion(int[] tam, int[] indices, int index, String tipo){
      if(episodios.getEventosDeEpisodios()==0){
         return super.siguienteCombinacion(tam,indices,index,tipo);
      }
      return episodios.siguienteCombinacion(tam, indices, index, tipo);
   }

   public List<Episodio> getEpisodios(){
      return episodios.getEpisodios();
   }

   protected boolean actualizaVentana(int sid, Evento ev, String tipo, int tmp, int index, int tSize){
      int[][] abiertas = getAbiertas(), limites = getLimites();
      int[] tam = getTam(); // Tamaño de las listas circulares, numero de elementos en ellas

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
      if(eventosDeEpisodios==0){
         super.recibeEvento(sid, ev, savePatternInstances);
         return;
      }
      String[] tiposReordenados = episodios.getTiposReordenados();
      String tipo = ev.getTipo();
      int index =  Arrays.asList(tiposReordenados).indexOf(tipo);

      int tmp = ev.getInstante();
      int tSize = tiposReordenados.length;

      if(tSize==1) {
         //LOGGER.severe("Entra en tSize==1 al recibir evento ");
         incrementarSoporte();
         ultimaEncontrada[0] = tmp;
         ultimaEncontrada[1] = tmp;
         return;
      }

      if(!actualizaVentana(sid, ev, tipo, tmp, index, tSize)){
         return;
      }

      int[] tam = getTam(); // Tamaño de las listas circulares, numero de elementos en ellas

      // Actualizar frecuencias
      int[] indices = episodios.primerosIndices(index, tam);
      if(indices == null){
         return;
      }

      int i=0, j=0, frecuenciaLocal=0, valor;

      int[] instancia = new int[tSize];
      instancia[index]=tmp;

      int[][] abiertas = getAbiertas(); // Listas circulares, contienen instantes temporales
      int[][] limites = getLimites(); // Límites de las listas circulares, contienen índices a 'abiertas'
      int[][] distribucion = getDistribuciones();
      int ventana = getVentana();
      List<Patron> patrones = getPatrones();
      int pSize = patrones.size();
      int[] patFrecLocal = new int[pSize];

      ultimaEncontrada[0]=tmp;
      ultimaEncontrada[1]=tmp;

      do{ // Recorre cada lista
         // Comprobar si pertenece a algun patrón
         episodios.fijarInstancia(tSize, index, tmp, abiertas, limites, indices, instancia, ventana);

         if(comprobarPatrones(instancia, patFrecLocal, sid, savePatternInstances)){
            // Actualizar las distribuciones de frecuencia
            int dist=0;
            for(i=0;i<tSize;i++){
               if(ultimaEncontrada[0] > instancia[i]){
                  ultimaEncontrada[0] = instancia[i];
               }
               for(j=i+1;j<tSize;j++){
                  valor = (int)(instancia[j] - instancia[i]);
                  distribucion[dist][valor+ventana]++;
                  dist++;
               }
            }
            frecuenciaLocal++;
         }
         indices = siguienteCombinacion(tam,indices,index,tipo);
      }while(indices != null);
      addFrecuencias(frecuenciaLocal,patFrecLocal);
   }

   @Override
   public boolean sonEpisodiosCompletos() {
      return episodios.isEpisodiosCompletos();
   }

   @Override
   public ModeloSemilla clonar(){
      List<Patron> patronesCopia = new ArrayList<Patron>();
      for(Patron p:getPatrones()){
         patronesCopia.add(((PatronSemilla)p).clonar());
      }
      return new ModeloSemillaEpisodios(getTipos(), episodios.getEpisodios(), getVentana(),
            patronesCopia, getSoporte(), getClustering());
   }


}
