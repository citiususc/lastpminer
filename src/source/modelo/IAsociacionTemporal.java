package source.modelo;

import java.util.List;

import source.evento.Evento;
import source.excepciones.FactoryInstantiationException;
import source.patron.GeneradorID;
//import source.modelo.clustering.IClustering;
import source.patron.Patron;
import source.restriccion.RIntervalo;

public interface IAsociacionTemporal extends Comparable<IAsociacionTemporal>{

   /**
    * Devuelve los tipos de eventos que representa la asociación temporal.
    */
   String[] getTipos();

   /**
    * Añade un patrón a la asociación temporal.
    */
   void addPatron(Patron patron);

   /**
    * Devuelve todos los patrones de la asociación temporal.
    */
   List<Patron> getPatrones();

   /**
    * Devuelve el patrón con la posición 'index' de la asociación temporal.
    */
   Patron getPatron(int index);

   /**
    * Devuelve la frecuencia de la asociación temporal.
    */
   int getSoporte();

   /**
    * Devuelve todas las restricciones 'desde'->'hacia' : [min,max] de todos
    * los patrones de la asociación temporal.
    */
   List<RIntervalo> getRestricciones(String desde,String hacia);

   /**
    * Devuelve todas las restricciones sobre el tipo de evento 'tipo'.
    */
   List<RIntervalo> getRestricciones(String tipo);

   /**
    * Devuelve todas las restricciones de todos los patrones.
    * Si filtrar es true, devuelve sólo las restricciones definidas
    */
   List<RIntervalo> getRestricciones(boolean filtrar);


   /**
    * Método 'observador'. Se le notifica a la asociación temporal que el
    * evento 'ev' ha entrado en la ventana temporal y el identificador de
    * la secuencia a la que pertenece 'sid', para que haga las actualizaciones
    * de frecuencia de los patrones que sean necesarias.
    */
   void recibeEvento(int sid, Evento ev, boolean savePatternInstances);

   /**
    * Elimina aquellos patrones cuya frecuencia es inferior a 'supmin'.
    * Para las asociaciones de tamaño 2, genera los correspondientes patrones
    * en función del resultado del clustering de distribuciones de frecuencia.
    * @return el número de patrones que se han eliminado
    */
   int calculaPatrones(int supmin, String patternClassName, GeneradorID genID,
         boolean savePatternInstances) throws FactoryInstantiationException;


   /**
    * @return Devuelve el tamaño de ventana considerado por la asociación temporal.
    */
   int getVentana();


   /**
    *
    * @return Devuelve true si la asociación debe ser purgada del proceso de minería
    */
   boolean necesitaPurga(int minFreq);


   /**
    *
    * @return Devuelve una cadena con el nombre de la asociación (sin los patrones como toString)
    */
   String toStringSinPatrones();

   /**
    * Tamaño de la asociación temporal
    * @return
    */
   int size();

   /**
    * Obtener el último tipo de la asociación (con prefijo o sufijo si es negativo)
    * @return
    */
   String getUltimoTipo();
}
