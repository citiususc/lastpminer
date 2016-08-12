package source.modelo.concurrente;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import source.evento.Evento;
import source.patron.GeneradorID;
import source.patron.Patron;
import source.restriccion.RIntervalo;

public class PatronConcurrenteDFE extends PatronConcurrenteEventoFinal{
   private static final Logger LOGGER = Logger.getLogger(PatronConcurrenteDFE.class.getName());

   /*
    * Atributos privados
    */

   protected List<PatronConcurrenteDFE> padres;
   private final Map<String,List<PatronConcurrenteDFE>> diccionario;
   private List<Evento> ultimoEventoLeido;
   private IAsociacionTemporalConcurrente modelo;

   /*
    * Constructores
    */

   public PatronConcurrenteDFE(String[] tipos, List<RIntervalo> restricciones, boolean savePatternInstances, int numHilos){
      super(tipos,restricciones, savePatternInstances);
      diccionario = new HashMap<String,List<PatronConcurrenteDFE>>();
      padres = new ArrayList<PatronConcurrenteDFE>();
      preparaUltimosEventos(numHilos);
   }

   protected PatronConcurrenteDFE(String[] tipos, boolean savePatternInstances, int numHilos){
      super(tipos, savePatternInstances);
      diccionario = new HashMap<String,List<PatronConcurrenteDFE>>();
      padres = new ArrayList<PatronConcurrenteDFE>();
      preparaUltimosEventos(numHilos);
   }

   public PatronConcurrenteDFE(Patron patron, int numHilos){
      super(patron);
      diccionario = new HashMap<String,List<PatronConcurrenteDFE>>();
      preparaUltimosEventos(numHilos);
      if(patron instanceof PatronConcurrenteDFE){
         padres = new ArrayList<PatronConcurrenteDFE>(((PatronConcurrenteDFE)patron).padres);
      }else{
         padres = new ArrayList<PatronConcurrenteDFE>();
      }
   }

   public PatronConcurrenteDFE(String[] tipos, Patron patron, int numHilos){
      super(tipos, patron);
      diccionario = new HashMap<String,List<PatronConcurrenteDFE>>();
      padres = new ArrayList<PatronConcurrenteDFE>();
      preparaUltimosEventos(numHilos);
      // Los patrones de tamaño 2 no serán instancia de PatronDictionaryFinalEvent
      if(patron instanceof PatronConcurrenteDFE){
         padres.add((PatronConcurrenteDFE)patron);
      }
   }

   public PatronConcurrenteDFE(PatronConcurrenteDFE patron, int numHilos){
      super(patron);
      padres = new ArrayList<PatronConcurrenteDFE>(patron.padres);
      //diccionario = new HashMap<String,List<PatronDictionaryFinalEvent>>();
      //diccionario.putAll(patron.diccionario);
      diccionario = patron.diccionario;
      preparaUltimosEventos(numHilos);
   }

   /*
    * Métodos propios
    */

   private final void preparaUltimosEventos(int numHilos){
      //ultimoEventoLeido = new ArrayList<Evento>(numHilos);
      ultimoEventoLeido = new ArrayList<Evento>(Collections.nCopies(numHilos, (Evento)null));
   }

   /**
    *   Se calcula la intersección de las restricciones entre el patrón actual 'this' y
    * el patrón pasado como parámetro, asumiendo la restricción universal cuando uno de
    * los patrones no la especifica.
    * @param patron Patron con el que se va a realizar la combinación.
    * @return true, si la combinación produce un patrón consistente, o false si alguna restricción es el conjunto vacío.
    */
   public boolean combinar(Patron patron){
      boolean result = super.combinar(patron);
      if(result && (patron instanceof PatronConcurrenteDFE)){
         padres.add((PatronConcurrenteDFE)patron);
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
   public boolean combinar(Patron patron, int indiceAusente){
      boolean result = super.combinar(patron, indiceAusente);
      if(result && (patron instanceof PatronConcurrenteDFE)){
         padres.add((PatronConcurrenteDFE)patron);
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
   public boolean esConsistente(GeneradorID genID){
      boolean result = super.esConsistente(genID);
      if(result){
         for(int i=0, pSize = padres.size();i<pSize;i++){
            padres.get(i).addHijo(this);
            padres.get(i).comprobarConsistenciaDiccionario();
         }
      }
      return result;
   }

   /**
    *   Se añade el patrón pasado como parámetro como extensión, o hijo, de 'this', usando
    * un diccionario que permita localizarlo según el tipo de evento que lo extiende.
    * @param hijo Patrón que extiende al patrón actual.
    */
   protected void addHijo(PatronConcurrenteDFE hijo){
      List<String> tipos = new ArrayList<String>(Arrays.asList(hijo.getTipos()));
      tipos.removeAll(Arrays.asList(getTipos()));
      String tipo = tipos.get(0); // Debería haber 1 y sólo 1.
      List<PatronConcurrenteDFE> lista = diccionario.get(tipo);
      if(lista==null){
         lista = new ArrayList<PatronConcurrenteDFE>();
         lista.add(hijo);
         diccionario.put(tipo, lista);
      }else{
         if(!lista.contains(hijo)){
            lista.add(hijo);
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
         PatronConcurrenteDFE primero = diccionario.get(tipo).get(0);
         List<String> tiposPrimero = Arrays.asList(primero.getTipos());
         //for(PatronDictionaryFinalEvent patron : diccionario.get(index)){
         for(PatronConcurrenteDFE patron : diccionario.get(tipo)){
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


   /*
    * Entrada: tipo de evento.
    * Salida: Lista de patrones que extienden a 'this' con el tipo 'tipo'.
    * Poscondición: Si no hay extensiones de 'this' con 'tipo', devuelve null.
    */
   /**
    *   Devuelve una lista de patrones que extienden al patrón actual con un determinado
    * tipo de evento. El objetivo es mantener una cierta jerarquía de patrones, aunque
    * organizada fácilmente según los tipos de eventos que se añaden a los diferentes
    * patrones frecuentes.
    * @param tipo Tipo de evento que los patrones añaden para extender al patrón actual 'this'.
    * @return Lista de patrones que extienden al patrón actual 'this' con el tipo de evento 'tipo'.
    */
   ///*
   public List<PatronConcurrenteDFE> getExtensiones(String tipo){
      return diccionario.get(tipo);
   }

   public List<PatronConcurrenteDFE> getPadres(){
      return padres;
   }

   public Evento getUltimoEventoLeido(int numHilo){
      return ultimoEventoLeido.get(numHilo);
   }

   public void setUltimoEventoLeido(int numHilo, Evento evento){
      //this.ultimoEventoLeido=evento;
      ultimoEventoLeido.set(numHilo, evento);
   }

   public List<Evento> getUltimosEventosLeidos(){
      return ultimoEventoLeido;
   }

   public IAsociacionTemporalConcurrente getAsociacion(){
      return modelo;
   }

   public void setAsociacion(IAsociacionTemporalConcurrente asociacion){
      this.modelo = asociacion;
   }

}


