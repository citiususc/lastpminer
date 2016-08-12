package source.busqueda.negacion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import source.busqueda.GeneradorPatrones;
import source.busqueda.episodios.EpisodiosUtils;
import source.evento.Episodio;
import source.evento.IColeccion;
import source.excepciones.AlgoritmoException;
import source.excepciones.FactoryInstantiationException;
import source.modelo.AssociationFactory;
import source.modelo.IAsociacionConEpisodios;
import source.modelo.IAsociacionEvento;
import source.modelo.IAsociacionTemporal;
import source.modelo.arbol.Nodo;
import source.modelo.arbol.Supernodo;
import source.modelo.arbol.SupernodoNegacion;
import source.modelo.clustering.IClustering;
import source.modelo.condensacion.episodios.SuperModeloEpisodios;
import source.modelo.negacion.HelperModeloNegacion;
import source.modelo.negacion.IAsociacionConNegacion;
import source.patron.Patron;

/**
 * Se consideran sólo episodios completos. Tanto completamente negados
 * como completamente positivos. Por tanto, no puede haber asociaciones temporales
 * en las que una parte (inicio o fin) está negada y la otra es positiva.
 * Además se consideran como incompletas las asociaciones que contienen un inicio de
 * episodio pero no su fin o viceversa. No se contabiliza la frecuencia de esas
 * asociaciones incompletas, aunque sí se crean para facilitar el proceso de
 * generación de candidatos.
 *
 * Las asociaciones que tienen en su parte positiva el inicio y final de un episodio
 *
 * A diferencia de como se hacía en MineCompleteEpisodes que podíamos borrar de la
 * ventana un fin de episodio cuando su evento de inicio no estaba en ella, con
 * negación estaríamos creando una negación falsa por lo que no podemos hacerlo
 * tan simplemente. Para solucionarlo se manejarán dos supermodelos: uno con episodios
 * y otros sin ellos. Con el supermodelo sin episodios se podrá controlar si se cumplen
 * los eventos negados. Por otra parte con el supermodelo con episodios se podrán seguir
 * las ocurrencias de los tipos de episodios correctamente.
 *
 * Los nuevos modelos con negación y episodios completos tiene que poder "comunicarse"
 * con los dos modelos. Por ejemplo, si tenemos A~B y C~D y el modelo {A,B,-C,-D}
 * necesita ambos supermodelos.
 *
 *
 *
 * @author vanesa.graino
 *
 */
public class NegacionMineEpisodiosCompletos extends NegacionMineEpisodios {

   /*
    * Atributos
    */
   protected SuperModeloEpisodios supermodeloEps;
   protected long[] descartadosEpisodiosNivel;


   /*
    * Constructores
    */

   public NegacionMineEpisodiosCompletos(String executionId,
         boolean savePatternInstances, boolean saveRemovedEvents,
         IClustering clustering, boolean removePatterns) {
      super(executionId, savePatternInstances, saveRemovedEvents, clustering,
            removePatterns);
   }

   /*
    * Métodos
    */

   /*
    * (non-Javadoc)
    * @see source.busqueda.negacion.NegacionMine#iniciarContadores(int, int)
    */
   @Override
   protected void iniciarContadores(int tSize, int cSize) {
      super.iniciarContadores(tSize, cSize);
      descartadosEpisodiosNivel = new long[tSize];
   }

   @Override
   protected void generarCandidatasTam2(List<IAsociacionTemporal> anteriores, List<String> tipos,
         List<IAsociacionTemporal> candidatas, List<Supernodo> nuevoNivel) throws FactoryInstantiationException{

      mapa = construyeMapa(tipos.size(), tipos);

      List<Nodo> nodos = raizArbol.getListaNodos();
      final int nSize = nodos.size(), tam = 2;
      int i, j;

      List<String[]> comb;
      IAsociacionEvento mod1, mod2;

      //Usando el árbol
      for(i=0; i<nSize-1; i++){
         Nodo nodo = nodos.get(i);
         mod1 = (IAsociacionEvento)nodo.getModelo();
         if(!((IAsociacionConNegacion)mod1).partePositiva()){
            //Si el primero es negativo acabamos
            break;
         }
         // Crear supernodo de hijos
         Supernodo hijos = nodo.getHijos();
         for(j=i+1; j<nSize; j++){
            mod2 = (IAsociacionEvento)nodos.get(j).getModelo();

            comb = HelperModeloNegacion.combinablesPrefijo(
                  mod1.getTipoEvento(), ((IAsociacionConNegacion)mod1).partePositiva(),
                  mod2.getTipoEvento(), ((IAsociacionConNegacion)mod2).partePositiva());

            if(comb == null){
               continue;
            }

            //Si hay un evento positivo y uno negativo
            if(comb.get(0).length==1){
               if(!combinablePorEpisodios(comb)){
                  descartadosEpisodiosNivel[tam-1]++;
                  continue;
               }
            }

            IAsociacionConNegacion modelo = crearModelo(comb);
            notificarModeloGenerado(tam, 0, modelo, comb.get(0), comb.get(1), candidatas, mapa, true);
            creaNodoFachada(modelo, hijos, mod2.getTipoEvento(),
                  ((IAsociacionConNegacion)mod2).partePositiva());
         }
         if(!hijos.getNodos().isEmpty()){
            nuevoNivel.add(hijos);
         }
      }
   }

   protected boolean combinablePorEpisodios(List<String[]> comb){
      for(Episodio ep : listaEpisodios){
         if(comb.get(0)[0] == ep.getTipoInicio()) {
            return comb.get(1)[0] != ep.getTipoFin();
         }else if(comb.get(0)[0] == ep.getTipoFin() ){
            return comb.get(1)[0] != ep.getTipoInicio();
         }
      }
      return true;
   }

   /*
    * Cambia en que se calculan los episodios de una asociación temporal
    * (non-Javadoc)
    * @see source.busqueda.negacion.NegacionMine#generarCandidatasGeneral(java.util.List, java.util.List, int, java.util.List)
    */
   @Override
   protected void generarCandidatasGeneral(int tam, List<IAsociacionTemporal> candidatas, List<String> tipos,
         List<Supernodo> nuevoNivel) throws FactoryInstantiationException{
      int i, j;
      IAsociacionConNegacion modelo;

      GeneradorPatronesNegacion genp = new GeneradorPatronesNegacion(tam, this);
      // Inicializar mapa
      resetMapas(tipos);

      for(Supernodo supernodo : nivelActual){
         List<Nodo> nodos = supernodo.getListaNodos();
         int nSize = nodos.size();
         for(i=0; i<nSize; i++){
            Nodo padre = nodos.get(i);
            genp.setPadre(padre.getModelo(), 0);
            Supernodo hijos = padre.getHijos();
            for(j=i+1; j<nSize; j++){
               registroT.tiempoAsociaciones(tam-1, true);
               // Construir la asociación temporal
               Nodo madre = nodos.get(j);
               genp.setPadre(madre.getModelo(), 1);
               //Como se van a comprobar las subasociaciones no es necesario comprobar si son combinables
               List<String[]> comb = HelperModeloNegacion.combinarPrefijo((IAsociacionConNegacion)padre.getModelo(), (IAsociacionConNegacion)madre.getModelo());

               // Comprobar que las subasociaciones temporales son frecuentes
               //boolean valido = genp.comprobarSubasociaciones(raizArbol, modArray);
               boolean valido = genp.comprobarSubasociacionesPrefijo((SupernodoNegacion)raizArbol, comb.get(0), comb.get(1));

               registroT.tiempoAsociaciones(tam-1, false);

               if(!valido){ continue; }

               List<Patron> patrones = Collections.emptyList();
               if(comb.get(0).length>1){ // tiene más de un positivo
                  patrones = genp.generarPatrones(comb.get(0));
//                  if(comb.get(1).length>0){ // tiene algún negativo
//                     patronesConNegacionNivel[tam-1] += patrones.size();
//                  }
               }else{
                  //Las asociaciones con un único evento positivo se contabilizan también como un patrón
                  patronesGeneradosNivel[tam-1]++;
                  patronesPosiblesNivel[tam-1]++;
                  patronesSinRestriccionesNivel[tam-1]++;
//                  patronesConNegacionNivel[tam-1]++; //si sólo tiene un positivo tiene que tener al menos 2 negativos
               }

               // Construir el modelo
               registroT.tiempoModelo(tam-1, true);
               // Sólo si hay patrones o sólo hay un evento positivo
               if( comb.get(0).length<2 || !patrones.isEmpty()){
                  // Hay: añadir punteros en la tabla hash y a candidatas
                  modelo = crearModelo(comb.get(0), comb.get(1), patrones, genp);

                  //setModeloPatrones(patrones, modelo);
                  //TODO estará bien esta condición?
                  // Ejemplo si A~B, y tenemos [A,-C,-D,-E] debería buscarse? No, es incompleto
                  // Ejemplo si A~B, y tenemos [C,D,-A] debería buscarse? No, también es incompleto
                  boolean buscar = ((IAsociacionConEpisodios)modelo).sonEpisodiosCompletos();
                  notificarModeloGenerado(tam, patrones.size(), modelo, comb.get(0), comb.get(1),
                        candidatas, mapa, buscar);
                  // Añadir el Nodo al nuevo
                  //Nodo hijo = creaNodoFachada(modelo,hijos);
                  //hijos.addNodo(hijo, tipoNuevo);
                  creaNodoFachada(modelo, hijos, genp.getTipoNuevo(), genp.tipoNuevoPositivo());

               }// else: No hay patrones candidatos: descartar modelo candidato actual
               registroT.tiempoModelo(tam-1, false);

            } // for j
            if(!hijos.getNodos().isEmpty()){
               nuevoNivel.add(hijos);
            }
         } // fin for i
      } // fin if tam>=3
      //mapa = nuevoMapa; // Actualizar mapa global
      //setMapa(mapa);
   }


   protected IAsociacionConNegacion crearModelo(String[] positivos, String[] negados,
         List<Patron> patrones, GeneradorPatrones genp) throws FactoryInstantiationException{
      List<Episodio> eps = new ArrayList<Episodio>();
      EpisodiosUtils.episodiosAsociacion(eps, Arrays.asList(positivos), genp.getAsociacionesBase());
      if(eps.isEmpty()){
         return AssociationFactory.getInstance().getAssociationInstance(associationClassName, positivos, negados, windowSize,
            patrones, supermodelo, numHilos);
      }
      return AssociationFactory.getInstance().getAssociationInstance(associationClassName, positivos, negados, eps, windowSize,
            patrones, supermodelo, numHilos);
   }


   @Override
   public List<List<IAsociacionTemporal>> buscarModelosFrecuentes(
         List<String> tipos, IColeccion coleccion, int supmin, int win,
         List<Episodio> episodios) throws AlgoritmoException {
      supermodeloEps = new SuperModeloEpisodios(tipos.toArray(new String[tipos.size()]), episodios, win);
      return super.buscarModelosFrecuentes(tipos, coleccion, supmin, win, episodios);
   }
}
