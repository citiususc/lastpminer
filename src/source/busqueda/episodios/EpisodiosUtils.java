package source.busqueda.episodios;

import java.util.List;

import source.evento.Episodio;
import source.modelo.IAsociacionConEpisodios;
import source.modelo.IAsociacionTemporal;

/**
 * Utilidades comunes a estrategias relacionadas con los episodios.
 * @author vanesa.graino
 *
 */
public final class EpisodiosUtils {

   private EpisodiosUtils(){

   }

   /**
    * Calcula los episodios de una asociación temporal
    * @param eps - lista en la que se almacenarán los episodios de la asociacion.
    * Tiene que estar inicializada.
    * @param listaEpisodios - todos los tipos de episodios de la colección
    * @param mod - lista de tipos de eventos de la asociación
    * @param ambos - si tiene que estar los dos tipos de eventos de un episodio para
    * añadirlo o basta con uno.
    */
   public static void episodiosAsociacionUno(List<Episodio> eps, List<Episodio> listaEpisodios, List<String> mod){
      for(Episodio episodio : listaEpisodios){
         // Se pide que al menos contenga uno de los tipos de evento para iteraciones futuras.
         if(mod.contains(episodio.getTipoInicio()) || mod.contains(episodio.getTipoFin())){
            eps.add(episodio);
         }
      }
   }

   public static void episodiosAsociacionAmbos(List<Episodio> eps, List<Episodio> listaEpisodios, List<String> mod){
      for(Episodio episodio : listaEpisodios){
         // Buscar si algún episodio se aplica a la asociación temporal en curso
         if(mod.contains(episodio.getTipoInicio()) && mod.contains(episodio.getTipoFin())){
            eps.add(episodio);
         }
      }
   }



   /**
    * Calcula los episodios de una asociación temporal
    * @param eps - lista en la que se almacenarán los episodios de la asociacion
    * @param mod - lista de tipos de eventos que compone la asociacion
    */
   public static void episodiosAsociacion(List<Episodio> eps, List<String> mod, IAsociacionTemporal[] asocBase ){
      for(IAsociacionTemporal aux : asocBase){
         IAsociacionConEpisodios fuente = (IAsociacionConEpisodios)aux;
         for(Episodio episodio : fuente.getEpisodios()){
            if(!eps.contains(episodio)){
               eps.add(episodio);
            }
         }
      }
   }

   /**
    * Calcula los episodios de una asociación temporal y devuelve si la asociacion es completa o no.
    * @param eps - lista en la que se almacenarán los episodios de la asociacion
    * @param mod - lista de tipos de eventos que compone la asociacion
    * @param asocBase - un array con las asociaciones base que componen la asociación
    * @return devuelve true si es una asociación completa o false si es parcial
    */
   public static boolean episodiosAsociacionBuscar(List<Episodio> eps, List<String> mod, IAsociacionTemporal[] asocBase ){
      episodiosAsociacion(eps, mod, asocBase);
      for(Episodio episodio : eps){
         if( !mod.contains(episodio.getTipoInicio()) || !mod.contains(episodio.getTipoFin())){
            return false;
         }
      }
      return true;
   }



}
