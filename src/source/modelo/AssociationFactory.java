package source.modelo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import source.evento.Episodio;
import source.excepciones.FactoryInstantiationException;
import source.modelo.clustering.IClustering;
import source.modelo.concurrente2.ModeloConcurrenteEvento;
import source.modelo.concurrente2.semilla.ModeloConcurrenteSemillaEpisodios;
import source.modelo.concurrente2.*;
import source.modelo.concurrente2.episodios.*;
import source.modelo.concurrente2.jerarquia.ModeloConcurrenteDFE;
import source.modelo.concurrente2.jerarquia.ModeloConcurrenteDictionary;
import source.modelo.concurrente2.semilla.ModeloConcurrenteSemilla;
import source.modelo.condensacion.ISuperModelo;
import source.modelo.condensacion.ModeloDistribucionTonto;
import source.modelo.condensacion.ModeloTonto;
import source.modelo.condensacion.episodios.ModeloDistribucionEpisodiosTonto;
import source.modelo.condensacion.episodios.ModeloEpisodiosDFETonto;
import source.modelo.condensacion.episodios.ModeloEpisodiosDFETontoMarcarArbol;
import source.modelo.condensacion.jerarquia.*;
import source.modelo.distribucion.ModeloDistribucion;
import source.modelo.episodios.*;
import source.modelo.jerarquia.*;
import source.modelo.negacion.IAsociacionConNegacion;
import source.modelo.negacion.ModeloDistribucionEpisodiosPositivo;
import source.modelo.negacion.ModeloDistribucionPositivo;
//import source.modelo.negacion.ModeloDistribucionTontoPositivo;
import source.modelo.negacion.ModeloEventoNegado;
import source.modelo.negacion.ModeloEventoPositivo;
import source.modelo.negacion.ModeloEventoPositivoMarcas;
import source.modelo.negacion.ModeloTontoNegadoCompleto;
import source.modelo.paralelo.episodios.ModeloEpisodiosDFEParalelo;
import source.modelo.paralelo.jerarquia.ModeloDictionaryParalelo;
import source.modelo.repetidos.ModeloDistribucionRepetido;
import source.modelo.semilla.ModeloSemilla;
import source.modelo.semilla.ModeloSemillaEpisodios;
import source.patron.Patron;

/*
 * Tries to follow the SimpleFactory design pattern. Implements two methods, the first
 * creates instances of IAsociacionTemporal that manage their own window. The second
 * creates instances of IAsociacionTemporal that delegate the window management in an
 * external object, such as 'Ventana' or 'VentanaCache'.
 *
 */
public class AssociationFactory{
   private static final Logger LOGGER = Logger.getLogger(AssociationFactory.class.getName());
   static{
      LOGGER.setLevel(Level.WARNING);
   }

   static class AssociationFactoryHolder{
      private static final AssociationFactory INSTANCE = new AssociationFactory();
   }

   //Singleton
   protected AssociationFactory(){
      //proteger constructor para singleton
   }

   public static AssociationFactory getInstance(){
      return AssociationFactoryHolder.INSTANCE;
   }

   /*
    * Modelos para varios tipos de eventos (3 o más)
    */

   /**
    *   Creates an appropriate instance of type IAsociacionTemporal according to the
    * parameters. The instance provided will manage its own temporal window.
    * @param className Code for the particular IAsociacionTemporal to be instanced.
    * @param types List of event types the instance will contemplate.
    * @param windowSize Maximum window size to be considered by the association and its patterns.
    * @return Appropriate instance of the association, or null if the code was wrong.
    * @throws FactoryInstantiationException
    */
   public IAsociacionTemporal getAssociationInstance(String className,
         String[] types, int windowSize, int threads) throws FactoryInstantiationException{
      LOGGER.info("(3) " + className + " con threads="+threads + ", tipos=" + types);
      if(threads>0){
         return getAssociationInstanceThreads(className, types, windowSize, threads);
      }
      IAsociacionTemporal result = null;
      if(className=="Modelo"){
         result = new Modelo(types,windowSize,null);
      }else if(className=="ModeloEvento"){
         result = new ModeloEvento(types[0],null);
      }else if(className=="ModeloOccurrenceMarking"){
         result = new ModeloOccurrenceMarking(types,windowSize,null);
      }else if(className=="ModeloAsociacion"){
         result = new ModeloAsociacion(types,windowSize,null);
      }else if(className=="ModeloEventoFinal"){
         result = new ModeloEventoFinal(types,windowSize,null);
      }else if(className=="ModeloMarcasIntervalos"){
         result = new ModeloMarcasIntervalos(types,windowSize,null);

      //Jerarquía
      }else if(className=="ModeloDictionaryFinalEvent"){
         result = new ModeloDictionaryFinalEvent(types,windowSize,null);
      }else if(className=="ModeloDictionary"){
         result = new ModeloDictionary(types,windowSize,null);
      }else if(className=="ModeloDictionaryOccurrenceMarking"){
         result = new ModeloDictionaryOccurrenceMarking(types,windowSize,null);
      }else if(className=="ModeloDictionaryParalelo"){
         result = new ModeloDictionaryParalelo(types,windowSize,null);
      }else if(className=="ModeloDictionaryIntervalMarking"){
         result = new ModeloDictionaryIntervalMarking(types,windowSize, (Integer)null);
      }else{
         throw new FactoryInstantiationException("getAssociationInstance (3) cannot handle className :" + className);
      }
      return result;
   }
   private IAsociacionTemporal getAssociationInstanceThreads(String className,
         String[] types, int windowSize, int threads) throws FactoryInstantiationException{
      LOGGER.info("(3) " + className + " con threads="+threads + ", tipos=" + types);
      IAsociacionTemporal result = null;
      if(className=="ModeloEvento"){
         result = new ModeloConcurrenteEvento(types[0],null, threads);
      }else if(className=="Modelo"){
         result = new ModeloConcurrente(types,windowSize,null, threads);
      }else if(className=="ModeloEventoFinal"){
         result = new ModeloConcurrenteEventoFinal(types,windowSize,null, threads);
      //}else if(className=="ModeloMarcasIntervalos"){
      //   result = new ModeloMarcasIntervalos(types,windowSize,savePatternInstances, clustering, hilos);

      //Jerarquía
      }else if(className=="ModeloDictionary"){
         result = new ModeloConcurrenteDictionary(types,windowSize,null, threads);
      }else if(className=="ModeloDictionaryFinalEvent"){
         result = new ModeloConcurrenteDFE(types,windowSize,null, threads);
      //}else if(className=="ModeloDictionaryIntervalMarking"){
      //   result = new ModeloDictionaryIntervalMarking(types,windowSize, savePatternInstances, clustering, hilos);
      }else{
         throw new FactoryInstantiationException("getAssociationInstance (3) cannot handle className :" + className);
      }
      return result;
   }

   /*
    * Con Supermodelo
    */

   public IAsociacionTemporal getAssociationInstance(String className, String[] types, int windowSize,
         ISuperModelo supermodelo, int threads) throws FactoryInstantiationException{
      LOGGER.info("(3.1) " + className + " con threads="+threads + ", tipos=" + types);
      IAsociacionTemporal result = null;
      if(className=="ModeloDictionary"){
         result = new ModeloDictionaryTonto(types,windowSize, supermodelo);
      }else if(className=="ModeloDFETontoMarcaArbol"){
         result = new ModeloDFETontoMarcarArbol(types, windowSize, null, supermodelo);
      }else{
         throw new FactoryInstantiationException("getAssociationInstance (3.1) cannot handle className :" + className);
      }
      return result;
   }

   /*
    * Con episodios
    */

   /**
    *   Creates an appropriate instance of type IAsociacionTemporal according to the
    * parameters. The instance provided will manage its own temporal window.
    * @param className Code for the particular IAsociacionTemporal to be instanced.
    * @param types List of event types the instance will contemplate.
    * @param episodes List of episodes the instance will contemplate.
    * @param windowSize Maximum window size to be considered by the association and its patterns.
    * @return Appropriate instance of the association, or null if the code was wrong.
    * @throws FactoryInstantiationException
    */
   public IAsociacionTemporal getAssociationInstance(String className,
         String[] types, List<Episodio> episodes, int windowSize,
         int threads) throws FactoryInstantiationException{
      LOGGER.info("(4) " + className + " con threads="+threads + ", tipos=" + types);
      if(threads>0){
         return getAssociationInstanceThreads(className, types, episodes, windowSize, threads);
      }
      IAsociacionTemporal result = null;

      if(className=="ModeloEpisodios"){
         result = new ModeloEpisodios(types,episodes,windowSize,null);
      //}else if(className=="ModeloAsociacion"){
      //   result = new ModeloAsociacion(types, windowSize,null);
      }else if(className=="ModeloEpisodiosDFE"){
         result = new ModeloEpisodiosDFE(types,episodes,windowSize,null);
      }else if(className=="ModeloEpisodiosDFEIntervalMarking"){
         result = new ModeloEpisodiosDFEIntervalMarking(types,episodes,windowSize,null);
      }else{
         throw new FactoryInstantiationException("getAssociationInstance (4) cannot handle className :" + className);
      }

      return result;
   }
   private IAsociacionTemporal getAssociationInstanceThreads(String className,
         String[] types, List<Episodio> episodes, int windowSize,
         int threads) throws FactoryInstantiationException{
      LOGGER.info("(4) " + className + " con threads="+threads + ", tipos=" + types);
      IAsociacionTemporal result = null;
      if(className=="ModeloEpisodios"){
         result = new ModeloConcurrenteEpisodios(types,episodes,windowSize, null, threads);
      }else if(className=="ModeloEpisodiosDFE"){
         result = new ModeloConcurrenteEpisodiosDFE(types,episodes,windowSize, null, threads);
      /*}else if(className=="ModeloParaleloEpisodiosDFEIntervalMarking"){
         result = new ModeloEpisodiosDFEIntervalMarking(types,episodes,windowSize,savePatternInstances, clustering, threads);*/
      }else{
         throw new FactoryInstantiationException("getAssociationInstance (4) cannot handle className :" + className);
      }
      return result;
   }

   /*
    * Con patrones
    */

   /**
    *   Creates an appropriate instance of type IAsociacionTemporal according to the
    * parameters. The instance provided will manage its own temporal window.
    * @param className Code for the particular IAsociacionTemporal to be instanced.
    * @param types List of event types the instance will contemplate.
    * @param windowSize Maximum window size to be considered by the association and its patterns.
    * @param patterns List of patterns to be considered by the association. May be null.
    * @return Appropriate instance of the association, or null if the code was wrong.
    * @throws FactoryInstantiationException
    */
   public IAsociacionTemporal getAssociationInstance(String className, String[] types,
         int windowSize, List<Patron> patterns, int threads) throws FactoryInstantiationException{
      LOGGER.info("(1) " + className + " con threads="+threads + ", tipos=" + types);
      if(threads>0){
         return getAssociationInstanceThreads(className, types, windowSize, patterns, threads);
      }
      IAsociacionTemporal result = null;
      className = className.intern();
      if(className=="Modelo" || className == "ModeloParalelo"){
         result = new Modelo(types,windowSize,patterns,null);
      }else if(className=="ModeloOccurrenceMarking"){
         result = new ModeloOccurrenceMarking(types,windowSize,patterns,null);
      }else if(className=="ModeloEventoFinal"){
         result = new ModeloEventoFinal(types,windowSize,patterns,null);
      }else if(className=="ModeloDictionaryFinalEvent"){
         result = new ModeloDictionaryFinalEvent(types,windowSize,patterns,null);
      }else if(className=="ModeloMarcasIntervalos" || className=="ModeloMarcasIntervalosParalelo"){
         result = new ModeloMarcasIntervalos(types,windowSize,patterns,null);

      }else if(className=="ModeloDictionary"){
         result = new ModeloDictionary(types,windowSize,patterns,null);
      }else if(className=="ModeloDictionaryOccurrenceMarking"){
         result = new ModeloDictionaryOccurrenceMarking(types,windowSize,patterns,null);
      }else if(className=="ModeloDictionaryParalelo"){
         result = new ModeloDictionaryParalelo(types,windowSize,patterns,null);
      }else if(className=="ModeloDictionaryIntervalMarking"){
         result = new ModeloDictionaryIntervalMarking(types,windowSize,patterns,null);
      }else if(className=="ModeloDFEMarcadoPatron"){
         result = new ModeloDFEMarcadoPatron(types,windowSize,patterns,null);

      }else if(className=="ModeloEpisodiosDFE"){
         result = new ModeloEpisodiosDFE(types,new ArrayList<Episodio>(),windowSize,patterns,null);

      }else{
         throw new FactoryInstantiationException("getAssociationInstance (1) cannot handle className :" + className + ". Tipos: " + types);
      }

      return result;
   }
   private IAsociacionTemporal getAssociationInstanceThreads(String className, String[] types,
         int windowSize, List<Patron> patterns, int threads) throws FactoryInstantiationException{
      LOGGER.info("(1) " + className + " con threads="+threads + ", tipos=" + types);
      IAsociacionTemporal result = null;
      //className = className.intern();
      if(className=="Modelo"){
         result = new ModeloConcurrente(types,windowSize,patterns,null,threads);
      }else if(className=="ModeloEventoFinal"){
         result = new ModeloConcurrenteEventoFinal(types,windowSize,patterns,null,threads);
      }else if(className=="ModeloDictionary"){
         result = new ModeloConcurrenteDictionary(types,windowSize,patterns,null,threads);
      }else if(className=="ModeloDictionaryFinalEvent"){
         result = new ModeloConcurrenteDFE(types,windowSize,patterns,null,threads);
      }else if(className=="ModeloMarcasIntervalos"){
         result = new ModeloConcurrenteMarcasIntervalos(types,windowSize,patterns,null,threads);
      /*}else if(className=="ModeloDictionaryParalelo"){
         result = new ModeloDictionaryParalelo(types,windowSize,patterns, clustering, hilos);
      }else if(className=="ModeloDictionaryIntervalMarking"){
         result = new ModeloDictionaryIntervalMarking(types,windowSize,patterns, clustering, hilos);*/
      }else{
         throw new FactoryInstantiationException("getAssociationInstance (1) cannot handle className :" + className);
      }

      return result;
   }


   /*
    * Con Supermodelo y patrones
    */

   /**
    * Este método incluye un parámetro supermodelo que define el modelo a instanciar en conjunción con className.
    * @param className
    * @param types
    * @param windowSize
    * @param patterns
    * @param savePatternInstances
    * @param supermodelo
    * @param threads
    * @return
    * @throws FactoryInstantiationException
    */
   public IAsociacionTemporal getAssociationInstance(String className, String[] types,
         int windowSize, List<Patron> patterns, ISuperModelo supermodelo,
         int threads) throws FactoryInstantiationException{
      LOGGER.info("(1.1) " + className + " con threads="+threads + ", tipos=" + types);
      IAsociacionTemporal result = null;
      if(className=="Modelo"){
         result = new ModeloTonto(types,windowSize,patterns, null, supermodelo);
      }else if(className=="ModeloDictionary"){
         result = new ModeloDictionaryTonto(types,windowSize,patterns, supermodelo);
      }else if(className=="ModeloDictionaryFinalEvent"){
         result = new ModeloDFETonto(types,windowSize,patterns, null, supermodelo);
      }else if(className=="ModeloDFETontoMarcarArbol"){
         result = new ModeloDFETontoMarcarArbol(types,windowSize,patterns, null,supermodelo);
      }else{
         throw new FactoryInstantiationException("getAssociationInstance (1.1) cannot handle className :" + className);
      }
      return result;
   }
   public IAsociacionConNegacion getAssociationInstance(String className, String[] positiveTypes, String[] negativeTypes,
         int windowSize, List<Patron> patterns, ISuperModelo supermodelo,
         int threads) throws FactoryInstantiationException{
      LOGGER.info("(1.1.b) " + className + " con threads="+threads
            + ", positivos=" + Arrays.toString(positiveTypes)  + ", negativos=" + Arrays.toString(negativeTypes));
      IAsociacionConNegacion result = null;

      if(className=="Modelo"){
         if(positiveTypes.length==0){
            //result = new ModeloAsociacionNegada(negativeTypes);
            throw new FactoryInstantiationException("getAssociationInstance (1.1) does not instantiates this class anymore :" + className);
         }else{
            result = new ModeloTontoNegadoCompleto(positiveTypes, negativeTypes, windowSize, patterns, null, supermodelo);
         }
      }else{
         throw new FactoryInstantiationException("getAssociationInstance (1.2) cannot handle className :" + className);
      }
      return result;
   }
   /*
    * Con negación y episodios
    */
   //TODO
   public IAsociacionConNegacion getAssociationInstance(String className, String[] positiveTypes, String[] negativeTypes,
         List<Episodio> episodes, int windowSize, List<Patron> patterns, ISuperModelo supermodelo,
         int threads) throws FactoryInstantiationException{
      LOGGER.info("(1.1.b) " + className + " con threads="+threads
            + ", positivos=" + Arrays.toString(positiveTypes)  + ", negativos=" + Arrays.toString(negativeTypes));
      IAsociacionConNegacion result = null;

      if(className=="Modelo"){
         if(positiveTypes.length==0){
            //result = new ModeloAsociacionNegada(negativeTypes);
            throw new FactoryInstantiationException("getAssociationInstance (1.1) does not instantiates this class anymore :" + className);
         }else{
            if(episodes.isEmpty()){
               result = new ModeloTontoNegadoCompleto(positiveTypes, negativeTypes, windowSize, patterns, null, supermodelo);
            }else{

            }
         }
      }else{
         throw new FactoryInstantiationException("getAssociationInstance (1.2) cannot handle className :" + className);
      }
      return result;
   }
   /*
    * Con patrones y episodios
    */

   /**
    *   Creates an appropriate instance of type IAsociacionTemporal according to the
    * parameters. The instance provided will manage its own temporal window.
    * @param className Code for the particular IAsociacionTemporal to be instanced.
    * @param types List of event types the instance will contemplate.
    * @param windowSize Maximum window size to be considered by the association and its patterns.
    * @param patterns List of patterns to be considered by the association. May be null.
    * @param episodes List of episodes to be considered by the association. May be null.
    * @return Appropriate instance of the association, or null if the code was wrong.
    * @throws FactoryInstantiationException
    */
   public IAsociacionTemporal getAssociationInstance(String className, String[] types, List<Episodio> episodes,
         int windowSize, List<Patron> patterns, int threads) throws FactoryInstantiationException{
      LOGGER.info("(2) " + className + " con threads="+threads + ", tipos=" + types);
      className = className.intern();
      if(threads>0){
         return getAssociationInstanceThreads(className, types, windowSize, patterns, episodes, threads);
      }
      IAsociacionTemporal result=null;

      if(className=="ModeloEpisodios" || className == "ModeloEpisodiosParalelo"){
         result = new ModeloEpisodios(types,episodes,windowSize,patterns,null);
      }else if(className=="ModeloEpisodiosDFE"){
         result = new ModeloEpisodiosDFE(types,episodes,windowSize,patterns, null);
      }else if(className=="ModeloEpisodiosDFEIntervalMarking"){
         result = new ModeloEpisodiosDFEIntervalMarking(types,episodes,windowSize,patterns, null);
      }else if(className=="ModeloEpisodiosDFEParalelo"){
         result = new ModeloEpisodiosDFEParalelo(types,episodes,windowSize,patterns, null);
      }else{
         throw new FactoryInstantiationException("getAssociationInstance (2) cannot handle className :" + className);
      }

      return result;
   }
   private IAsociacionTemporal getAssociationInstanceThreads(String className, String[] types,
         int windowSize, List<Patron> patterns, List<Episodio> episodes,
         int threads) throws FactoryInstantiationException{
      LOGGER.info("(2) " + className + " con threads="+threads + ", tipos=" + types);
      IAsociacionTemporal result=null;
      if(className=="ModeloEpisodios"){
         result = new ModeloConcurrenteEpisodios(types,episodes,windowSize,patterns,null, threads);
      }else if(className=="ModeloEpisodiosDFE" || className == "ModeloDictionaryFinalEvent"){
         result = new ModeloConcurrenteEpisodiosDFE(types,episodes,windowSize,patterns, null, threads);
         /*}else if(className=="ModeloEpisodiosDFEIntervalMarking"){
         result = new ModeloParaleloEpisodiosDFEIntervalMarking(types,episodes,windowSize,patterns, savePatternInstances, clustering, hilos);*/
      }else{
         throw new FactoryInstantiationException("getAssociationInstance (2) cannot handle className :" + className);
      }
      return result;
   }

   /*
    * Con patrones, episodios y supermodelo
    */

   public IAsociacionTemporal getAssociationInstance(String className, String[] types, int windowSize,
         List<Patron> patterns, List<Episodio> episodes,
         ISuperModelo supermodelo, int threads) throws FactoryInstantiationException{
      LOGGER.info("(2.1) " + className + " con threads="+threads + ", tipos=" + types);
      IAsociacionTemporal result=null;

      if(className=="ModeloEpisodiosDFE" || className=="ModeloDictionaryFinalEvent"){
         result = new ModeloEpisodiosDFETonto(types,episodes,windowSize,patterns, supermodelo);
      }else if(className=="ModeloDFETontoMarcarArbol"){
         result = new ModeloEpisodiosDFETontoMarcarArbol(types, episodes, windowSize, patterns, supermodelo);
      }else{
         throw new FactoryInstantiationException("getAssociationInstance (2.1) cannot handle className :" + className);
      }

      return result;
   }


   /*
    * Modelos para un único evento
    */

   /**
    * Instanciador cuando hay una único evento
    * @param className
    * @param type
    * @param threads
    * @return
    * @throws FactoryInstantiationException
    */
   public IAsociacionTemporal getAssociationInstance(String className, String type,
         int threads) throws FactoryInstantiationException{
      IAsociacionTemporal result = null;
      if(className=="ModeloEvento"){
         if(threads>0){
            result = new ModeloConcurrenteEvento(type, null, threads);
         }else{
            result = new ModeloEvento(type, null);
         }
      }else if(className=="ModeloNegacionMarcas"){
         if(threads == 0){
            result = new ModeloEventoPositivoMarcas(type, null);
         }else{
            throw new FactoryInstantiationException("getAssociationInstance (2) cannot handle className with threads :" + className);
         }
      }else{
         throw new FactoryInstantiationException("getAssociationInstance (2) cannot handle className :" + className);
      }
      return result;
   }

   /*
    * Para negación
    */

   public IAsociacionConNegacion getAssociationInstance(String className, String type, int ventana, boolean negado, int threads)
         throws FactoryInstantiationException{
      IAsociacionConNegacion result = null;
      if(threads>0){
         //result = new ModeloConcurrenteEvento(type,null,threads);
         throw new FactoryInstantiationException("getAssociationInstance (2) cannot handle className  :" + className + " with threads>0");
      }else{
         if(negado){
            if(className=="Modelo"){
               result = new ModeloEventoNegado(type, null, ventana);
            }else{
                throw new FactoryInstantiationException("getAssociationInstance (2.1) cannot handle className :" + className);
            }
         }else{
            result = new ModeloEventoPositivo(type, null);
         }
      }
      return result;
   }

   /*
    * Modelos de distribución
    */

   public IAsociacionTemporal getAssociationInstance(String className, String[] types, int windowSize,
         IClustering clustering, int threads)
         throws FactoryInstantiationException{
      LOGGER.info("(6) " + className + " con threads="+threads + ", tipos=" + types);
      IAsociacionTemporal result = null;
      //if(className=="Modelo"){
         if(threads>0){
            //result = new ModeloConcurrenteEpisodios(types, eps, windowSize, patterns, distribution, savePatternInstances, clustering, threads);
            result = new ModeloConcurrenteDistribucion(types, windowSize, null, clustering, threads);
         }else{
            //result = new ModeloEpisodios(types, eps, windowSize, patterns, distribution, savePatternInstances, clustering);
            result = new ModeloDistribucion(types, windowSize, null, clustering);
         }
      //}
      return result;
   }

   public IAsociacionConNegacion getAssociationInstance(String className,
         String[] types, String[] negados, int windowSize, IClustering clustering,
         ISuperModelo supermodelo, int threads) throws FactoryInstantiationException{
      LOGGER.info("(5) " + className + " con threads="+threads + ", tipos=" + types);
      IAsociacionConNegacion result = null;
      if(className == "Modelo"){
         switch(types.length){
            case 0: //2 negativos
               //No se debería instanciar
               //result = new ModeloAsociacionNegada(negados);
               break;

            case 1: //1 negativo
               result = new ModeloTontoNegadoCompleto(types, negados, windowSize, 0, supermodelo);
               break;

            case 2: //2 positivos, 0 negativos
               //Funciona con cualquiera de los dos ya que el recibeEvento de ModeloDistribucion llama a actualizaVentana
               result = new ModeloDistribucionPositivo(types, windowSize, 0, clustering);
               //result = new ModeloDistribucionTontoPositivo(types, windowSize, 0, clustering, supermodelo);
               break;
         }
      }else{
         throw new FactoryInstantiationException("getAssociationInstance (123) cannot handle classname: " + className + ", with types: " + types);
      }
      return result;
   }

   //TODO
   public IAsociacionConNegacion getAssociationInstance(String className,
         String[] types, String[] negados, List<Episodio> episodes, int windowSize, IClustering clustering,
         ISuperModelo supermodelo, int threads) throws FactoryInstantiationException{
      IAsociacionConNegacion result = null;
      if(className == "Modelo"){
         switch(types.length){
            case 0: //2 negativos
               //No se debería instanciar
               //result = new ModeloAsociacionNegada(negados);
               break;

            case 1: //1 negativo
               result = new ModeloTontoNegadoCompleto(types, negados, windowSize, 0, supermodelo);
               break;

            case 2: //2 positivos, 0 negativos
               //Funciona con cualquiera de los dos ya que el recibeEvento de ModeloDistribucion llama a actualizaVentana
               result = new ModeloDistribucionEpisodiosPositivo(types, episodes, windowSize, 0, clustering);
               //result = new ModeloDistribucionTontoPositivo(types, windowSize, 0, clustering, supermodelo);
               break;
         }
      }else{
         throw new FactoryInstantiationException("getAssociationInstance (123-epis) cannot handle classname: " + className + ", with types: " + types);
      }
      return result;

   }


   public IAsociacionTemporal getAssociationInstance(String className,
         String[] types, int windowSize, List<Patron> patterns, int[] distribution,
         IClustering clustering, int threads)
               throws FactoryInstantiationException{
      LOGGER.info("(5) " + className + " con threads="+threads + ", tipos=" + types);
      IAsociacionTemporal result = null;
      if(className == "Modelo" || className == "ModeloDictionaryFinalEvent" || className == "ModeloMarcasIntervalos"){
         if(threads>0){
            //result = new ModeloConcurrente(types, windowSize, patterns, distribution, savePatternInstances, clustering, threads);
            result = new ModeloConcurrenteDistribucion(types, windowSize, patterns, distribution, clustering, threads);
         }else{
            //result = new Modelo(types,windowSize, patterns, distribution, savePatternInstances, clustering);
            result = new ModeloDistribucion(types, windowSize, patterns, distribution, clustering);
         }
      }else{
         throw new FactoryInstantiationException("getAssociationInstance (123) cannot handle classname: " + className + ", with types: " + types);
      }
      return result;
   }

   /*
    * Distribución con SuperModelo
    */
   public IAsociacionTemporal getAssociationInstance(String className, String[] types, int windowSize,
         IClustering clustering, ISuperModelo supermodelo, boolean repetidos, int threads)
               throws FactoryInstantiationException{
      LOGGER.info("(6) " + className + " con threads="+threads + ", tipos=" + types);
      IAsociacionTemporal result = null;
      if(repetidos){
          result = new ModeloDistribucionRepetido(types, windowSize, null, clustering);
      }else{
          result = new ModeloDistribucionTonto(types, windowSize, null, clustering, supermodelo);
      }
      return result;
   }

   public IAsociacionTemporal getAssociationInstance(String className, String[] types, int windowSize,
         IClustering clustering, ISuperModelo supermodelo, int threads)
               throws FactoryInstantiationException{
      LOGGER.info("(6) " + className + " con threads="+threads + ", tipos=" + types);
      IAsociacionTemporal result = null;
      result = new ModeloDistribucionTonto(types, windowSize, null, clustering, supermodelo);
      return result;
   }

   /*
    * Distribución con episodios
    */

   public IAsociacionTemporal getAssociationInstance(String className, String[] types, List<Episodio> eps, int windowSize,
         List<Patron> patterns, int[] distribution, IClustering clustering, int threads)
               throws FactoryInstantiationException{
      LOGGER.info("(6) " + className + " con threads="+threads + ", tipos=" + types);
      IAsociacionTemporal result = null;
      className = className.intern();
      if(className == "ModeloEpisodios" || className == "ModeloEpisodiosParalelo" || className == "ModeloEpisodiosDFE"){
         if(threads>0){
            //result = new ModeloConcurrenteEpisodios(types, eps, windowSize, patterns, distribution, savePatternInstances, clustering, threads);
            result = new ModeloConcurrenteDistribucionEpisodios(types, eps, windowSize, patterns, distribution, clustering, threads);
         }else{
            //result = new ModeloEpisodios(types, eps, windowSize, patterns, distribution, savePatternInstances, clustering);
            result = new ModeloDistribucionEpisodios(types, eps, windowSize, patterns, distribution, clustering);
         }
      }else{
         throw new FactoryInstantiationException("getAssociationInstance (123 epis) cannot handle classname: " + className + ", with types: " + types);
      }
      return result;
   }

   public IAsociacionTemporal getAssociationInstance(String className, String[] types, List<Episodio> eps, int windowSize,
         IClustering clustering, int threads)
               throws FactoryInstantiationException{
      LOGGER.info("(6) " + className + " con threads="+threads + ", tipos=" + types);
      IAsociacionTemporal result = null;
      //if(className=="ModeloEpisodios"){
         if(threads>0){
            //result = new ModeloConcurrenteEpisodios(types, eps, windowSize, patterns, distribution, savePatternInstances, clustering, threads);
            result = new ModeloConcurrenteDistribucionEpisodios(types, eps, windowSize, null, clustering, threads);
         }else{
            //result = new ModeloEpisodios(types, eps, windowSize, patterns, distribution, savePatternInstances, clustering);
            result = new ModeloDistribucionEpisodios(types, eps, windowSize, null, clustering);
         }
      //}
      return result;
   }


   /*
    * Distribución con Supermodelo y episodios
    */

   public IAsociacionTemporal getAssociationInstance(String className, String[] types, List<Episodio> eps, int windowSize,
         IClustering clustering, ISuperModelo supermodelo, int threads)
               throws FactoryInstantiationException{
      LOGGER.info("(6) " + className + " con threads="+threads + ", tipos=" + types);
      IAsociacionTemporal result = null;
      result = new ModeloDistribucionEpisodiosTonto(types, eps, windowSize, null, clustering, supermodelo);
      return result;
   }

   /*
    * Modelos con semilla
    */

   public IAsociacionTemporal getSeedAssociationInstance(/*String className,*/ String[] types, int windowSize,
         IClustering clustering, int threads)
         throws FactoryInstantiationException{
      LOGGER.info("ModeloSemilla instanciado. threads=" + threads + ", tipos=" + types);
      IAsociacionTemporal result = null;
      //if("ModeloSemilla".equals(className)){
         if(threads>0){
            result = new ModeloConcurrenteSemilla(types, windowSize, null, clustering, threads);
         }else{
            result = new ModeloSemilla(types, windowSize, clustering);
         }
      //}else{
      //   throw new FactoryInstantiationException("getSeedAssociationInstance(1) cannot handle className :" + className);
      //}
      return result;
   }

   public IAsociacionTemporal getSeedAssociationInstance(/*String className,*/ ModeloSemilla semilla, int threads)
         throws FactoryInstantiationException{
      LOGGER.info("ModeloSemillaEpisodios instanciado. threads=" + threads + ", tipos=" + semilla.getTipos());
      IAsociacionTemporal result = null;
      /*if("ModeloSemilla".equals(className)){
         if(threads>0){
            result = new ModeloConcurrenteSemilla(types, windowSize, savePatternInstances, clustering, threads);
         }else{
            return semilla;
         }
      }else{
         throw new FactoryInstantiationException("getSeedAssociationInstance(2) cannot handle className :" + className);
      }*/
      if(threads>0){
         if(semilla instanceof ModeloSemillaEpisodios){
            result = new ModeloConcurrenteSemillaEpisodios(semilla.getTipos(), ((ModeloSemillaEpisodios) semilla).getEpisodios(),
                  semilla.getVentana(), semilla.getPatrones(),
                  semilla.getSoporte(), semilla.getClustering(), threads);
         }else{
            result = new ModeloConcurrenteSemilla(semilla.getTipos(), semilla.getVentana(), semilla.getPatrones(),
                  semilla.getSoporte(), semilla.getClustering(), threads);
         }
      }else{
         return semilla;
      }
      return result;
   }


}
