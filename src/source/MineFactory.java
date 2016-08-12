package source;

import source.busqueda.*;
import source.busqueda.concurrente.ConcurrenteMine;
import source.busqueda.concurrente.episodios.ConcurrenteMineCEDFE;
import source.busqueda.concurrente.episodios.ConcurrenteMineCompleteEpisodes;
import source.busqueda.concurrente.jerarquia.ConcurrenteMineDictionary;
import source.busqueda.concurrente.semilla.*;
import source.busqueda.episodios.*;
import source.busqueda.jerarquia.*;
import source.busqueda.negacion.MinePositivos;
import source.busqueda.negacion.NegacionCompletaPruebaNivel2;
import source.busqueda.negacion.NegacionCompletaPruebaNivel3;
import source.busqueda.negacion.NegacionMine;
import source.busqueda.negacion.NegacionMineEpisodios;
import source.busqueda.negacion.NegacionMineEpisodiosCompletos;
import source.busqueda.paralela.ParallelMine;
import source.busqueda.paralela.episodios.ParallelMineCEDFE;
import source.busqueda.paralela.episodios.ParallelMineCompleteEpisodes;
import source.busqueda.paralela.jerarquia.ParallelMineDictionary;
import source.busqueda.paralela.semilla.ParallelSemillaConjuncion;
import source.busqueda.paralela.semilla.ParallelSemillaConjuncionCompleteEpisodes;
import source.busqueda.repetidos.MineRepetidos;
import source.busqueda.semilla.*;
import source.configuracion.ConfigurationParameters;
import source.excepciones.FactoryInstantiationException;
import source.modelo.clustering.IClustering;

public final class MineFactory {

   private MineFactory(){

   }

   public static IBusqueda getBasicInstance(final ConfigurationParameters params,
         final IClustering clustering) throws FactoryInstantiationException{
      IBusqueda mine;
      switch(params.getAlgorithm()){
         //ASTP
         case ALG_ASTP:
            mine = new Mine(params.getExecutionId(), params.isSavePatternInstances(), params.isSaveRemovedEvents(),
                  clustering, !params.isCompleteResult());
            break;
         case ALG_SASTP:
            mine = new MineSuperModelo(params.getExecutionId(), params.isSavePatternInstances(), params.isSaveRemovedEvents(),
                  clustering, !params.isCompleteResult());
            break;

         //TSTP
         case ALG_TSTP:
            mine = new MineArbol(params.getExecutionId(), params.isSavePatternInstances(), params.isSaveRemovedEvents(),
                  clustering, !params.isCompleteResult());
            break;
         //HSTP
         case ALG_HSTP:
            mine = new MineDictionary(params.getExecutionId(), params.isSavePatternInstances(), params.isSaveAllAnnotations(),
                  params.isSaveRemovedEvents(), clustering, !params.isCompleteResult());
            break;
         case ALG_SAV:
            mine = new MineAhorro(params.getExecutionId(), params.isSavePatternInstances(), params.isSaveAllAnnotations(),
                  params.isSaveRemovedEvents(), clustering, !params.isCompleteResult());
            break;
         case ALG_SAV4:
            mine = new MineAhorroV4(params.getExecutionId(), params.isSavePatternInstances(), params.isSaveAllAnnotations(),
                  params.isSaveRemovedEvents(), clustering, !params.isCompleteResult());
            break;
         case ALG_EXP:
            mine = new MineDictionaryExpress(params.getExecutionId(), params.isSavePatternInstances(), params.isSaveAllAnnotations(),
                  params.isSaveRemovedEvents(), clustering, !params.isCompleteResult());
            break;
         case ALG_SAVEXP:
            mine = new MineAhorroExpress(params.getExecutionId(), params.isSavePatternInstances(), params.isSaveAllAnnotations(),
                  params.isSaveRemovedEvents(), clustering, !params.isCompleteResult());
            break;
         case ALG_LESS:
            mine = new MineAhorroExpressLazy(params.getExecutionId(), params.isSavePatternInstances(), params.isSaveAllAnnotations(),
                  params.isSaveRemovedEvents(), clustering, !params.isCompleteResult());
            break;
         case ALG_ANOT:
            mine = new MineAnotaciones(params.getExecutionId(), params.isSavePatternInstances(), params.isSaveAllAnnotations(),
                  params.isSaveRemovedEvents(), clustering, !params.isCompleteResult());
            break;
         case ALG_LAZY:
            mine = new MineDictionaryLazy3(params.getExecutionId(), params.isSavePatternInstances(), params.isSaveAllAnnotations(),
                  params.isSaveRemovedEvents(), clustering, !params.isCompleteResult());
            break;
         case ALG_SUPER:
            mine = new MineDictionarySuperModelo(params.getExecutionId(), params.isSavePatternInstances(), params.isSaveAllAnnotations(),
                  params.isSaveRemovedEvents(), clustering, !params.isCompleteResult());
            break;
         case ALG_SMEXP:
            mine = new MineDictionarySuperModeloExpress(params.getExecutionId(), params.isSavePatternInstances(),
                  params.isSaveAllAnnotations(), params.isSaveRemovedEvents(), clustering, !params.isCompleteResult());
            break;
         case ALG_SMSAVEXP:
            mine = new MineDictionarySuperModeloES(params.getExecutionId(), params.isSavePatternInstances(), params.isSaveAllAnnotations(),
                  params.isSaveRemovedEvents(), clustering, !params.isCompleteResult());
            break;
         case ALG_MARK:
            mine = new MineMarcar(params.getExecutionId(), params.isSavePatternInstances(), params.isSaveAllAnnotations(),
                  params.isSaveRemovedEvents(), clustering, !params.isCompleteResult());
            break;
         case ALG_MARKT:
            mine = new MineDictionarySuperMarcarArbolV2(params.getExecutionId(), params.isSavePatternInstances(),
                  params.isSaveAllAnnotations(), params.isSaveRemovedEvents(), clustering, !params.isCompleteResult());
            break;

         //Estrategias de marcado (astp y hstp)
         case ALG_IM:
            mine = new MineDictionaryIntervalMarking(params.getExecutionId(), params.isSavePatternInstances(),
                  params.isSaveAllAnnotations(), params.isSaveRemovedEvents(), clustering, false, !params.isCompleteResult());
            break;
         case ALG_IM2:
            mine = new MineDictionaryIntervalMarking(params.getExecutionId(), params.isSavePatternInstances(),
                  params.isSaveAllAnnotations(), params.isSaveRemovedEvents(), clustering, true, !params.isCompleteResult());
            break;
         case ALG_WM:
            mine = new MineDictionaryWindowMarking(params.getExecutionId(), params.isSavePatternInstances(),
                  params.isSaveAllAnnotations(), params.isSaveRemovedEvents(), clustering, !params.isCompleteResult());
            break;
         case ALG_OM:
            mine = new MineOccurrenceMarking(params.getExecutionId(), params.isSavePatternInstances(),
                  params.isSaveRemovedEvents(), clustering, !params.isCompleteResult());
            break;
         case ALG_HOM:
            mine = new MineDictionaryOccurrenceMarking(params.getExecutionId(), params.isSavePatternInstances(),
                  params.isSaveAllAnnotations(), params.isSaveRemovedEvents(), clustering, !params.isCompleteResult());
            break;

         //Paralelos (astp y hstp)
         case ALG_PAR:
            mine = new ParallelMine(params.getExecutionId(), params.isSavePatternInstances(),
                  params.isSaveRemovedEvents(), clustering, !params.isCompleteResult(), ConfigurationParameters.NUM_THREADS);
            break;
         case ALG_HPAR:
            mine = new ParallelMineDictionary(params.getExecutionId(), params.isSavePatternInstances(),
                  params.isSaveAllAnnotations(), params.isSaveRemovedEvents(), clustering,
                  !params.isCompleteResult(), ConfigurationParameters.NUM_THREADS);
            break;

         //Concurrentes (astp y hstp)
         case ALG_CON:
            mine = new ConcurrenteMine(params.getExecutionId(), params.isSavePatternInstances(), params.isSaveRemovedEvents(),
                  clustering, !params.isCompleteResult(), ConfigurationParameters.NUM_THREADS);
            break;
         case ALG_HCON:
            mine = new ConcurrenteMineDictionary(params.getExecutionId(), params.isSavePatternInstances(),
                  params.isSaveAllAnnotations(), params.isSaveRemovedEvents(), clustering,
                  !params.isCompleteResult(), ConfigurationParameters.NUM_THREADS);
            break;

         //Negacion
         case ALG_NEG:
            mine = new NegacionMine(params.getExecutionId(), params.isSavePatternInstances(), params.isSaveRemovedEvents(),
                    clustering, !params.isCompleteResult());
            break;
         case ALG_NEG_TEST2:
            mine = new NegacionCompletaPruebaNivel2(params.getExecutionId(), params.isSavePatternInstances(), params.isSaveRemovedEvents(),
                  clustering, !params.isCompleteResult());
            break;
         case ALG_NEG_TEST3:
            mine = new NegacionCompletaPruebaNivel3(params.getExecutionId(), params.isSavePatternInstances(), params.isSaveRemovedEvents(),
                  clustering, !params.isCompleteResult());
            break;
         case ALG_NEG_POS:
            mine = new MinePositivos(params.getExecutionId(), params.isSavePatternInstances(), params.isSaveRemovedEvents(),
                  clustering, !params.isCompleteResult());
            break;

         //Repetición
         case ALG_REP:
            mine = new MineRepetidos(params.getExecutionId(), params.isSavePatternInstances(), params.isSaveRemovedEvents(),
                  clustering, !params.isCompleteResult());
            break;

         default:
            throw new FactoryInstantiationException("Algoritmo desconocido(1): " + params.getAlgorithm());
      }
      ((AbstractMine)mine).setTamMaximoPatron(params.getTamMaximoPatron());
      return mine;

   }

   public static IBusquedaConEpisodios getEpisodeInstance(final ConfigurationParameters params,
         final IClustering clustering) throws FactoryInstantiationException{
      IBusquedaConEpisodios mine;
      switch(params.getAlgorithm()){
         case ALG_ASTP:
            mine = new MineCompleteEpisodes(params.getExecutionId(), params.isSavePatternInstances(), params.isSaveRemovedEvents(),
                  clustering, !params.isCompleteResult());
            break;
         case ALG_ASTPI:
            mine = new MineEpisodes(params.getExecutionId(), params.isSavePatternInstances(), params.isSaveRemovedEvents(),
                  clustering, !params.isCompleteResult());
            break;
         case ALG_TSTP:
            mine = new MineEpisodiosArbol(params.getExecutionId(), params.isSavePatternInstances(), params.isSaveRemovedEvents(),
                  clustering, !params.isCompleteResult());
            break;
         case ALG_HSTP:
            mine = new MineCEDFE(params.getExecutionId(), params.isSavePatternInstances(), params.isSaveAllAnnotations(),
                  params.isSaveRemovedEvents(), clustering, !params.isCompleteResult());
            break;
         case ALG_ANOT:
            mine = new MineAnotacionesEpisodios(params.getExecutionId(), params.isSavePatternInstances(), params.isSaveAllAnnotations(),
                  params.isSaveRemovedEvents(), clustering, !params.isCompleteResult());
            break;
         /*case ALG_IM:
            mine = new MineCEDFEIntervalMarking(params.getExecutionId(), params.isSavePatternInstances(),
                  params.isSaveAllAnnotations(), params.isSaveRemovedEvents(), clustering);
            break;*/
         case ALG_PAR:
            mine = new ParallelMineCompleteEpisodes(params.getExecutionId(), params.isSavePatternInstances(),
                  params.isSaveRemovedEvents(), clustering, !params.isCompleteResult(), ConfigurationParameters.NUM_THREADS);
            break;
         case ALG_HPAR:
            mine = new ParallelMineCEDFE(params.getExecutionId(), params.isSavePatternInstances(), params.isSaveAllAnnotations(),
                  params.isSaveRemovedEvents(), clustering, !params.isCompleteResult(), ConfigurationParameters.NUM_THREADS);
            break;
         case ALG_CON:
            mine = new ConcurrenteMineCompleteEpisodes(params.getExecutionId(), params.isSavePatternInstances(),
                  params.isSaveRemovedEvents(), clustering, !params.isCompleteResult(), ConfigurationParameters.NUM_THREADS);
            break;
         case ALG_HCON:
            mine = new ConcurrenteMineCEDFE(params.getExecutionId(), params.isSavePatternInstances(), params.isSaveAllAnnotations(),
                  params.isSaveRemovedEvents(), clustering, !params.isCompleteResult(), ConfigurationParameters.NUM_THREADS);
            break;
         case ALG_SUPER:
            mine = new MineCEDFESuperModelo(params.getExecutionId(), params.isSavePatternInstances(), params.isSaveAllAnnotations(),
                  params.isSaveRemovedEvents(), clustering, !params.isCompleteResult());
            break;
         case ALG_MARKT:
            mine = new MineCEDFESuperMarcarArbol(params.getExecutionId(), params.isSavePatternInstances(), params.isSaveAllAnnotations(),
                  params.isSaveRemovedEvents(), clustering, !params.isCompleteResult());
            break;
         //Negacion
         case ALG_NEG:
            mine = new NegacionMineEpisodios(params.getExecutionId(), params.isSavePatternInstances(), params.isSaveRemovedEvents(),
                    clustering, !params.isCompleteResult());
            break;
         case ALG_NEGC:
            mine = new NegacionMineEpisodiosCompletos(params.getExecutionId(), params.isSavePatternInstances(), params.isSaveRemovedEvents(),
                    clustering, !params.isCompleteResult());
            break;
         case ALG_LAZY:
            throw new FactoryInstantiationException("Configuración no válida. No hay implementación de lazy con episodios");
         default:
            throw new FactoryInstantiationException("Algoritmo desconocido(2): "+ params.getAlgorithm());
      }
      ((AbstractMine)mine).setTamMaximoPatron(params.getTamMaximoPatron());
      return mine;
   }

   public static IBusquedaConSemilla getSeedInstance(final ConfigurationParameters params,
         final IClustering clustering) throws FactoryInstantiationException{
      IBusquedaConSemilla mine;
      switch(params.getAlgorithm()){
         case ALG_ASTP:
            mine = new SemillaConjuncion(params.getExecutionId(), params.isSavePatternInstances(),
                  params.isSaveRemovedEvents(), clustering, !params.isCompleteResult());
            break;
         case ALG_HSTP:
            /*mine = new SemillaConjuncionCEDFE(params.getExecutionId(),
                  params.isSavePatternInstances(), params.isSaveRemovedEvents(),params.isSaveAllAnnotations(),
                  clustering);*/
            mine = new SemillaConjuncionDictionaryFinalEvent(params.getExecutionId(),
                  params.isSavePatternInstances(), params.isSaveRemovedEvents(), params.isSaveAllAnnotations(), clustering, !params.isCompleteResult());
            break;
         case ALG_PAR:
            mine = new ParallelSemillaConjuncion(params.getExecutionId(),
                  params.isSavePatternInstances(), params.isSaveRemovedEvents(), clustering, !params.isCompleteResult(), ConfigurationParameters.NUM_THREADS);
            break;
         case ALG_IM:
            mine = new SemillaConjuncionIntervalMarking(params.getExecutionId(), params.isSavePatternInstances(),
                  params.isSaveRemovedEvents(), params.isSaveAllAnnotations(), clustering, !params.isCompleteResult());
            break;
         case ALG_CON:
            mine = new ConcurrenteSemillaConjuncion(params.getExecutionId(), params.isSavePatternInstances(),
                  params.isSaveRemovedEvents(), clustering, !params.isCompleteResult(), ConfigurationParameters.NUM_THREADS);
            break;
         case ALG_HCON:
            mine = new ConcurrenteSemillaConjuncionDFE(params.getExecutionId(),
                  params.isSavePatternInstances(), params.isSaveRemovedEvents(), params.isSaveAllAnnotations(),
                  clustering, !params.isCompleteResult(), ConfigurationParameters.NUM_THREADS);
            break;
         case ALG_MAN:
            mine = new ManualSearching(params.getExecutionId(),
                  params.isSavePatternInstances(), params.isSaveRemovedEvents(),
                  clustering, !params.isCompleteResult());
            break;
         case ALG_NEG:
            mine = new NegacionSemilla(params.getExecutionId(),
                  params.isSavePatternInstances(), params.isSaveRemovedEvents(),
                  clustering, !params.isCompleteResult());
            break;
         case ALG_HPAR:
            throw new FactoryInstantiationException("Configuración no válida. No hay implementación de HSTP paralelo con semilla");
         case ALG_LAZY:
            throw new FactoryInstantiationException("Configuración no válida. No hay implementación de lazy con semilla");
         default:
            throw new FactoryInstantiationException("Algoritmo desconocido(3): "+ params.getAlgorithm());
      }
      ((AbstractMine)mine).setTamMaximoPatron(params.getTamMaximoPatron());
      return mine;
   }

   public static IBusquedaConSemillayEpisodios getSeedAndEpisodesInstance(final ConfigurationParameters params,
         final IClustering clustering) throws FactoryInstantiationException{
      IBusquedaConSemillayEpisodios mine;
      switch(params.getAlgorithm()){
         case ALG_ASTP:
            mine = new SemillaConjuncionCompleteEpisodes(params.getExecutionId(),
                  params.isSavePatternInstances(), params.isSaveRemovedEvents(),
                  clustering, !params.isCompleteResult());
            break;
         case ALG_HSTP:
            mine = new SemillaConjuncionCEDFE(params.getExecutionId(),
                  params.isSavePatternInstances(), params.isSaveRemovedEvents(),params.isSaveAllAnnotations(),
                  clustering, !params.isCompleteResult());
            break;
         case ALG_PAR:
            mine = new ParallelSemillaConjuncionCompleteEpisodes(params.getExecutionId(),
                  params.isSavePatternInstances(), params.isSaveRemovedEvents(), clustering,
                  !params.isCompleteResult(), ConfigurationParameters.NUM_THREADS);
            break;
         case ALG_CON:
            mine = new ConcurrenteSemillaConjuncionCE(params.getExecutionId(),
                  params.isSavePatternInstances(), params.isSaveRemovedEvents(),
                  clustering, !params.isCompleteResult(), ConfigurationParameters.NUM_THREADS);
            break;
         case ALG_HCON:
            mine = new ConcurrenteSemillaConjuncionCEDFE(params.getExecutionId(),
                  params.isSavePatternInstances(), params.isSaveRemovedEvents(),params.isSaveAllAnnotations(),
                  clustering, !params.isCompleteResult(), ConfigurationParameters.NUM_THREADS);
            break;
         case ALG_MAN:
            mine = new ManualSearchingEpisodes(params.getExecutionId(),
                  params.isSavePatternInstances(), params.isSaveRemovedEvents(),
                  clustering, !params.isCompleteResult());
            break;
         case ALG_LAZY:
            throw new FactoryInstantiationException("Configuración no válida. No hay implementación de lazy con episodios y semilla");
         case ALG_IM:
         case ALG_IMS:
         case ALG_IM2:
            throw new FactoryInstantiationException("Configuración no válida. No hay implementación de interval marking con episodios y semilla");
         case ALG_HPAR:
            throw new FactoryInstantiationException("Configuración no válida. No hay implementación de HSTP paralelo con episodios y semilla");
         default:
            throw new FactoryInstantiationException("Algoritmo desconocido(4): "+ params.getAlgorithm());
      }
      ((AbstractMine)mine).setTamMaximoPatron(params.getTamMaximoPatron());
      return mine;
   }

}
