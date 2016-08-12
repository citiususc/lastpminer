package source.patron;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import source.evento.Evento;
import source.modelo.IAsociacionTemporal;
import source.restriccion.RIntervalo;

/**
 *
 * @author vanesa.graino
 *
 */
public class PatronDictionaryFinalEvent extends PatronEventoFinal {
   private static final Logger LOGGER = Logger.getLogger(PatronDictionaryFinalEvent.class.getName());

   /*
    * Atributos privados
    */

   /**
    * Se utiliza para después añadir al patrón como hijo de sus padres si es consistente
    */
   protected List<PatronDictionaryFinalEvent> padres;
   private final Map<String,List<PatronDictionaryFinalEvent>> diccionario;
   private Evento ultimoEventoLeido;
   private IAsociacionTemporal modelo;

   /*
    * Constructores
    */
   public PatronDictionaryFinalEvent(String[] tipos, List<RIntervalo> restricciones, boolean savePatternInstances){
      super(tipos,restricciones, savePatternInstances);
      diccionario = new HashMap<String,List<PatronDictionaryFinalEvent>>();
      padres = new ArrayList<PatronDictionaryFinalEvent>();
   }

   protected PatronDictionaryFinalEvent(String[] tipos, boolean savePatternInstances){
      super(tipos, savePatternInstances);
      diccionario = new HashMap<String,List<PatronDictionaryFinalEvent>>();
      padres = new ArrayList<PatronDictionaryFinalEvent>();
   }

   public PatronDictionaryFinalEvent(Patron patron){
      super(patron);
      diccionario = new HashMap<String,List<PatronDictionaryFinalEvent>>();
      if(patron instanceof PatronDictionaryFinalEvent){
         padres = new ArrayList<PatronDictionaryFinalEvent>(((PatronDictionaryFinalEvent)patron).padres);
      }else{
         padres = new ArrayList<PatronDictionaryFinalEvent>();
      }
   }

   public PatronDictionaryFinalEvent(String[] tipos, Patron patron){
      super(tipos, patron);
      diccionario = new HashMap<String,List<PatronDictionaryFinalEvent>>();
      padres = new ArrayList<PatronDictionaryFinalEvent>();
      // Los patrones de tamaño 2 no serán instancia de PatronDictionaryFinalEvent
      if(patron instanceof PatronDictionaryFinalEvent){
         padres.add((PatronDictionaryFinalEvent)patron);
      }
   }

   public PatronDictionaryFinalEvent(PatronDictionaryFinalEvent patron){
      super(patron);
      padres = new ArrayList<PatronDictionaryFinalEvent>(patron.padres);
      //diccionario = new HashMap<String,List<PatronDictionaryFinalEvent>>();
      //diccionario.putAll(patron.diccionario);
      diccionario = patron.diccionario;
   }

   /*
    * Métodos propios
    */

   /**
    *   Se calcula la intersección de las restricciones entre el patrón actual 'this' y
    * el patrón pasado como parámetro, asumiendo la restricción universal cuando uno de
    * los patrones no la especifica.
    * @param patron Patron con el que se va a realizar la combinación.
    * @return true, si la combinación produce un patrón consistente, o false si alguna restricción es el conjunto vacío.
    */
   @Override
   public boolean combinar(Patron patron){
      boolean result = super.combinar(patron);
      if(result && patron instanceof PatronDictionaryFinalEvent){
         padres.add((PatronDictionaryFinalEvent)patron);
      }
      return result;
   }

   // Precond: patron es un posible subpatron de this
   /**
    *   Se calcula la intersección de las restricciones entre el patrón actual 'this' y
    * el patrón pasado como parámetro, asumiendo la restricción universal cuando uno de
    * los patrones no la especifica.
    * @param patron Patron con el que se va a realizar la combinación.
    * @param indiceAusente Índice del (único) tipo de evento que 'patron' no especifica.
    * @return true, si la combinación produce un patrón consistente, o false si alguna restricción es el conjunto vacío.
    */
   @Override
   public boolean combinar(Patron patron, int indiceAusente){
      boolean result = super.combinar(patron, indiceAusente);
      if(result && patron instanceof PatronDictionaryFinalEvent){
         padres.add((PatronDictionaryFinalEvent)patron);
      }
      return result;
   }

   /**
    *   Comprueba, mediante Floyd-Warshall si el patron es consistente. Si lo es, además
    * calcula qué tipos de eventos pueden ocurrir como finalización de una ocurrencia del
    * patrón (por extender la clase PatronEventoFinal). Además, construye la jerarquía de
    * patrones, notificando a los padres que tienen una nueva extensión.
    * @return true, si el patrón es consistente, o false si no lo es.
    */
   @Override
   public boolean esConsistente(GeneradorID genId){
      boolean result = super.esConsistente(genId);
      if(result){
         //for(int i=0, pSize = padres.size();i<pSize;i++){
         for(PatronDictionaryFinalEvent padre : padres){
            padre.addHijo(this);
            //padre.comprobarConsistenciaDiccionario();//TODO esto no hace falta no? @vanesa
         }
      }
      return result;
   }

   /**
    * Se añade el patrón pasado como parámetro como extensión, o hijo, de {@code this}, usando
    * un diccionario que permita localizarlo según el tipo de evento que lo extiende.
    * @param hijo Patrón que extiende al patrón actual.
    */
   protected void addHijo(PatronDictionaryFinalEvent hijo){
      List<String> tipos = new ArrayList<String>(Arrays.asList(hijo.getTipos()));
      tipos.removeAll(Arrays.asList(getTipos()));
      String tipo = tipos.get(0); // Debería haber 1 y sólo 1.
      List<PatronDictionaryFinalEvent> lista = diccionario.get(tipo);
      if(lista==null){
         lista = new ArrayList<PatronDictionaryFinalEvent>();//TODO LinkedList
         lista.add(hijo);
         diccionario.put(tipo, lista);
      }else{
         if(!lista.contains(hijo)){
            lista.add(hijo);
            //insertamos en la lista ordenadamente
            //int insertionPoint = Collections.binarySearch(lista, hijo, hijo);
            //lista.add((insertionPoint > -1) ? insertionPoint : (-insertionPoint) - 1, hijo);
         }//else System.out.println("Intento de hijo repetido!");

      }
   }

   protected boolean comprobarConsistenciaDiccionario(){
      boolean result = true;
      //int index=0;
      //int tSize = getTipos().size();
      // Comprobar
      //for(String tipo : tiposDiccionario){
      for(String tipo : diccionario.keySet()){
         //PatronDictionaryFinalEvent primero = diccionario.get(index).get(0);
         PatronDictionaryFinalEvent primero = diccionario.get(tipo).get(0);
         List<String> tiposPrimero = Arrays.asList(primero.getTipos());
         //for(PatronDictionaryFinalEvent patron : diccionario.get(index)){
         for(PatronDictionaryFinalEvent patron : diccionario.get(tipo)){
            List<String> tiposPatron = Arrays.asList(patron.getTipos());
            if(!tiposPatron.contains(tipo)){
               result=false;
               LOGGER.severe("Diccionario mal construido: una lista contiene patrones sin el tipo asociado a la lista");
               System.exit(1);
            }
            if(!tiposPrimero.containsAll(tiposPatron)){
               result=false;
               LOGGER.severe("Diccionario mal construido: una lista contiene patrones con tipos distintos");
               System.exit(1);
            }
         }
         //index++;
      }
      return result;
   }

   /**
    *   Devuelve una lista de patrones que extienden al patrón actual con un determinado
    * tipo de evento. El objetivo es mantener una cierta jerarquía de patrones, aunque
    * organizada fácilmente según los tipos de eventos que se añaden a los diferentes
    * patrones frecuentes.
    * @param tipo Tipo de evento que los patrones añaden para extender al patrón actual 'this'.
    * @return Lista de patrones que extienden al patrón actual 'this' con el tipo de evento 'tipo' o null
    * si no hay extensiones.
    */
   ///*
   public List<PatronDictionaryFinalEvent> getExtensiones(String tipo){
      return diccionario.get(tipo);
   }

   public List<PatronDictionaryFinalEvent> getPadres(){
      return padres;
   }

   public Evento getUltimoEventoLeido(){
      return ultimoEventoLeido;
   }

   public void setUltimoEventoLeido(Evento evento){
      this.ultimoEventoLeido=evento;
   }

   public IAsociacionTemporal getAsociacion(){
      return modelo;
   }

   public void setAsociacion(IAsociacionTemporal asociacion){
      this.modelo = asociacion;
   }

   @Override
   public PatronDictionaryFinalEvent clonar(){
      return new PatronDictionaryFinalEvent(this);
   }

   public boolean tieneHijos(){
      for(List<PatronDictionaryFinalEvent> lista: diccionario.values()){
         if(!lista.isEmpty()){ return true; }
      }
      return false;
   }



}
