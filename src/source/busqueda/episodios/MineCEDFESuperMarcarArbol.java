package source.busqueda.episodios;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import source.busqueda.jerarquia.GeneradorPatronesArbol;
import source.evento.Episodio;
import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.excepciones.FactoryInstantiationException;
import source.modelo.AssociationFactory;
import source.modelo.IAsociacionArbol;
import source.modelo.IAsociacionConEpisodios;
import source.modelo.IAsociacionDiccionario;
import source.modelo.IAsociacionTemporal;
import source.modelo.arbol.Nodo;
import source.modelo.arbol.NodoAntepasados;
import source.modelo.arbol.NodoAntepasadosAnotado;
import source.modelo.arbol.Supernodo;
import source.modelo.arbol.SupernodoAdoptivos;
import source.modelo.arbol.SupernodoAdoptivosAnotados;
import source.modelo.clustering.IClustering;
import source.patron.Patron;
import source.patron.PatronDictionaryFinalEvent;

/**
 * Marcar en el arbol (V1).
 * A diferencia de cuando no hay episodios, en este caso también se combinan los nodos del arbol que son
 * asociaciones parciales, pero tiene que haber por lo menos una marca de la ventana real
 * en el supernodo que se combina.
 *
 * @author vanesa.graino
 *
 */
public class MineCEDFESuperMarcarArbol extends MineCEDFESuperModelo {
   //private static final Logger LOGGER = Logger.getLogger(MineCEDFESuperMarcarArbol.class.getName());
   private static final String TIEMPOS_ESTRATEGIA = "estrategia";

   protected int[] eventosSinAnotaciones; //# de eventos que gracias a la estrategia dejan de tener una lista de anotaciones
   protected long[] anotacionesEvitadas;
   //protected long[] tiemposEstrategia;

   {
      treeClassName = "SupernodoAdoptivosAnotados";
      associationClassName = "ModeloDFETontoMarcarArbol";
      patternClassName = "PatronMarcado";
   }

   public MineCEDFESuperMarcarArbol(String executionId,
         boolean savePatternInstances, boolean saveAllAnnotations,
         boolean saveRemovedEvents, IClustering clustering, boolean removePatterns) {
      super(executionId, savePatternInstances, saveAllAnnotations,
            saveRemovedEvents, clustering, removePatterns);
      registroT.addOtrosTiempos(TIEMPOS_ESTRATEGIA);
   }

   @Override
   protected void iniciarContadores(int tSize, int cSize) {
      super.iniciarContadores(tSize, cSize);
      anotacionesEvitadas = new long[tSize+1];
      //tiemposEstrategia = new long[tSize+1];
      eventosSinAnotaciones = new int[tSize+1];
   }

   // Método de calcularSoporte[Tam3,Tam4,General] copiados de MineCEDFESuperModelo
   // Modificados para que se apliquen las estrategia SAVE

   //@Override
   public void finSecuencia(){
      for(Supernodo ns : nivelActual){
         ((SupernodoAdoptivosAnotados)ns).resetAnotaciones();
      }
   }

   //@Override
   protected void setAnotacionesEvento(List<Patron> encontrados, List<Patron> ventanaActual,
         String evento, int tmp, int tam){

      //TODO cambiar para utilizar los nodos parciales

      //MineDictionarySuperMarcarArbol.setAnotacionesEvento(encontrados, ventanaActual, evento,
      //      tmp, tam, windowSize, nivelActual, anotacionesEvitadas);
      ventanaActual.clear();
      int eSize = encontrados.size();
      if(eSize<tam){
         anotacionesEvitadas[tam-1]+=eSize;
         encontrados.clear();
         return;
      }

      Collections.sort(encontrados);

      //List<Boolean> utiles = new ArrayList<Boolean>(eSize);
      List<Integer> correspondencia = new ArrayList<Integer>(eSize);
      int indice = 0;
      IAsociacionArbol anterior = (IAsociacionArbol)((PatronDictionaryFinalEvent)encontrados.get(0)).getAsociacion();
      List<IAsociacionArbol> allModels = new ArrayList<IAsociacionArbol>(eSize);
      allModels.add(anterior);
      //utiles.add(false);
      correspondencia.add(0);

      // Se marca en el árbol el patrón encontrado para que
      // pueda ser utilizado por la estrategia
      ((IAsociacionArbol)anterior).getNodo().setUtil(false);

      //Lista de supernodos
      List<Supernodo> allSupernodes = new ArrayList<Supernodo>();
      Supernodo snAnterior = anterior.getNodo().getSupernodo();
      allSupernodes.add(snAnterior);

      for(int i=1; i<eSize;i++){
         PatronDictionaryFinalEvent p = (PatronDictionaryFinalEvent)encontrados.get(i);
         if(anterior != p.getAsociacion()){
            indice++;
            anterior = (IAsociacionArbol)p.getAsociacion();
            // Se marca en el árbol el patrón encontrado
            ((IAsociacionArbol)anterior).getNodo().setUtil(false);
            //utiles.add(false);
            allModels.add(anterior);

            //supernodos
            if(snAnterior != anterior.getNodo().getSupernodo()){
               snAnterior = anterior.getNodo().getSupernodo();
               allSupernodes.add(snAnterior);
            }
         }
         correspondencia.add(indice);
      }

      List<NodoAntepasadosAnotado> anotados = new ArrayList<NodoAntepasadosAnotado>();
      //int iSN=nivelActual.indexOf(allModels.get(0).getNodo().getSupernodo());
      //for(; iSN>=0 && iSN<nivelActual.size()-1;iSN++){
      //   SupernodoAdoptivosAnotados ns = (SupernodoAdoptivosAnotados)nivelActual.get(iSN);
      //System.out.println("Total supernodos: " + nivelActual.size() + ", supernodos: " + allSupernodes.size());
      for(Supernodo supernodo : allSupernodes){
         SupernodoAdoptivosAnotados ns = (SupernodoAdoptivosAnotados)supernodo;
         if(ns.nodosMarcadosMasParciales(tmp, windowSize,anotados)){
            combinarNodos(anotados, tmp, windowSize);
         }
      }

      for(int i=0; i<eSize; i++){
         if(!allModels.get(correspondencia.get(i)).getNodo().isUtil()){
            correspondencia.remove(i);
            encontrados.remove(i);
            i--;
            eSize--;
            anotacionesEvitadas[tam-1]++;
         }
      }

      ventanaActual.addAll(encontrados);
      encontrados.clear();
   }

   /**
   *
   * @param anotados
   * @param tmp
   * @param windowSize
   */
  protected static void combinarNodos(List<NodoAntepasadosAnotado> anotados, int tmp, int windowSize){
     for(int i=0,nSize=anotados.size();i<nSize-1;i++){
        NodoAntepasadosAnotado nodo = anotados.get(i);
        for(int j=i+1; j<nSize; j++){
           NodoAntepasadosAnotado nodoExtendido = (NodoAntepasadosAnotado)nodo.getHijos().getHijo(anotados.get(j).getUltimoTipo());
           //TODO Creo que se puede saltar el primero (cambiaría el 0 por un 1)
           if(nodoExtendido != null && nodoExtendido.asegurarAntepasadosConParciales(tmp, windowSize,0)){
              nodoExtendido.padresUtiles();
           }
        }
     }
  }

   /*
    * En tamaño 3 no se utilizan los episodios por lo que no hay problema
    */
   @Override
   protected void calcularSoporteTam3(IColeccion coleccion){
      // Asociaciones de tamaño 3, aquí aún no hay ningún tipo de señalización
      //sobre qué asociaciones *sin* episodios ocurren en cada ventana.
      int sid = 0;
      Iterator<List<List<Patron>>> itActual = anotaciones.getActual().iterator();
      //List<Patron> encontrados = new ArrayList<Patron>();
      for(ISecuencia secuencia : coleccion){
         Iterator<List<Patron>> itVentanaActual = itActual.next().iterator();
         for(Evento evento : secuencia){
            List<Patron> ventanaActual = itVentanaActual.next();
            ventanaActual.clear();
            supermodelo.actualizaVentana(sid,evento);
            // Se recorre la lista de la tabla hash
            List<IAsociacionTemporal> receptores = mapa.get(evento.getTipo());
            for(IAsociacionTemporal receptor : receptores){
               List<Patron> aux = receptor.getPatrones();
               List<PatronDictionaryFinalEvent> lista = new ArrayList<PatronDictionaryFinalEvent>();
               for(Patron patron : aux){
                  lista.add((PatronDictionaryFinalEvent)patron);
               }
               ((IAsociacionDiccionario)receptor).recibeEvento(sid,evento, savePatternInstances,lista,ventanaActual);
            }
            //boolean antes = !encontrados.isEmpty();
            //Guardar anotaciones
            //setAnotacionesEvento(encontrados, ventanaActual, evento.getTipo(), evento.getInstante(), 3);
            //if(antes && ventanaActual.isEmpty()){ eventosSinAnotaciones[3-1]++; }
         }
         sid++;
         finSecuencia();
      }
   }

   @Override
   protected void calcularSoporteTam4(IColeccion coleccion){
      int sid = 0;
      Iterator<List<List<Patron>>> itActual = anotaciones.getActual().iterator();
      List<Patron> encontrados = new ArrayList<Patron>();
      List<String> listaTiposAmpliar = new ArrayList<String>();
      for(ISecuencia secuencia : coleccion){
         Iterator<List<Patron>> itVentanaActual = itActual.next().iterator();
         for(Evento evento : secuencia){
            List<Patron> ventanaActual = itVentanaActual.next();
            supermodelo.actualizaVentana(sid,evento);
            List<IAsociacionTemporal> receptores = mapa.get(evento.getTipo());
            if(receptores != null){
               // Se recorre la lista de la tabla hash
               for(IAsociacionTemporal receptor : receptores){
                  // Procesar únicamente aquellos candidatos que vienen con episodios completos,
                  // y vienen por primera vez. (Si hay tipos de eventos que no provienen de episodios
                  // en la anterior iteración se pudieron buscar adecuadamente.
                  if(((IAsociacionConEpisodios)receptor).sonEpisodiosCompletos()){
                     List<Patron> aux = receptor.getPatrones();
                     List<PatronDictionaryFinalEvent> lista = new ArrayList<PatronDictionaryFinalEvent>();
                     for(Patron patron : aux){
                        lista.add((PatronDictionaryFinalEvent)patron);
                     }
                     ((IAsociacionDiccionario)receptor).recibeEvento(sid,evento, savePatternInstances,lista,encontrados);
                  }
               }
            }
            // Se comprueban las anotaciones que haya de la iteración anterior
            listaTiposAmpliar = posiblesTiposParaAmpliar(ventanaActual, listaTiposAmpliar);
            boolean conservar = false;
            for(Patron aux : ventanaActual){
               PatronDictionaryFinalEvent patron = (PatronDictionaryFinalEvent)aux;
               for(String tipo : listaTiposAmpliar){
                  List<PatronDictionaryFinalEvent> extensiones = patron.getExtensiones(tipo);
                  if(extensiones!=null && !extensiones.isEmpty()){
                     IAsociacionDiccionario posible = (IAsociacionDiccionario)extensiones.get(0).getAsociacion();
                     //if(posible==null){ continue; } // En rara ocasión se puede dar
                     //si no es episodio completo no se busca
                     if(((IAsociacionConEpisodios)posible).sonEpisodiosCompletos()){
                        posible.recibeEvento(sid,evento, savePatternInstances,extensiones,encontrados);
                     }else{
                        conservar=true;
                     }
                  }
               }
               if(conservar){
                  // Añadir la vieja anotación a la lista de anotaciones aceptadas
                  //if(!encontrados.contains(patron)) encontrados.add(patron);
                  encontrados.add(patron);
               }
            }
            ventanaActual.clear();
            ventanaActual.addAll(encontrados); //aquí se guardan las anotaciones de la ventana en actual
            encontrados.clear();
         }
         sid++;
      }
   }

   @Override
   protected void calcularSoporteGeneral(IColeccion coleccion, int tamActual){
      Iterator<List<List<Patron>>> itActual = anotaciones.getActual().iterator();
      List<Patron> encontrados = new ArrayList<Patron>();
      //List<String> listaTiposAmpliar = new ArrayList<String>();
      List<String> listaTiposAmpliarNoEpisodios = new ArrayList<String>();
      List<Episodio> listaEpisodiosAmpliar = new ArrayList<Episodio>();
      int sid = 0;
      for(ISecuencia secuencia : coleccion){
         Iterator<List<Patron>> itVentanaActual = itActual.next().iterator();
         for(Evento evento : secuencia){
            List<Patron> ventanaActual = itVentanaActual.next();
            // Actualizar ventana
            supermodelo.actualizaVentana(sid,evento);
            //listaTiposAmpliar = posiblesTiposParaAmpliar(ventanaActual, listaTiposAmpliar);
            listaTiposAmpliarNoEpisodios = posiblesTiposParaAmpliarNoEpisodios(ventanaActual, listaTiposAmpliarNoEpisodios, evento);
            listaEpisodiosAmpliar = posiblesEpisodiosParaAmpliar(listaEpisodiosAmpliar, evento);
            for(Patron aux : ventanaActual){
               PatronDictionaryFinalEvent patron = (PatronDictionaryFinalEvent)aux;
               // Comprobar de qué tipo de anotación se trata
               // Caso a: anotación hecha en la anterior iteración
               if(patron.getTipos().length == tamActual-1){
                  boolean conservar=false;
                  //for(String tipo : listaTiposAmpliar){
                  //Siempre van a ser asociaciones completas en este primer bucle
                  for(String tipo : listaTiposAmpliarNoEpisodios){
                     List<PatronDictionaryFinalEvent> extensiones = patron.getExtensiones(tipo);
                     if(extensiones!=null && !extensiones.isEmpty()){
                        IAsociacionDiccionario posible = (IAsociacionDiccionario)extensiones.get(0).getAsociacion();
                        //if(posible==null){ continue; }
                        //if(((IAsociacionConEpisodios)posible).sonEpisodiosCompletos()){
                           posible.recibeEvento(sid,evento, savePatternInstances,extensiones,encontrados);
                        //}else{
                        //   conservar=true;
                        //}
                     }
                  }
                  // En esta parte sólo pueden crearse asociaciones incompletas por lo que si existen
                  // extensiones automáticamente
                  for(int i=0; !conservar && i<listaEpisodiosAmpliar.size(); i++){
                     List<PatronDictionaryFinalEvent> extensiones = patron.getExtensiones(listaEpisodiosAmpliar.get(i).getTipoInicio());
                     if(extensiones!=null && !extensiones.isEmpty()){
                        conservar = true;
                     }
                  }
                  if(conservar){
                     // Añadir la vieja anotación a la lista de anotaciones aceptadas
                     encontrados.add(patron);
                  }
               }else{
                  // Caso b: anotación hecha hace 2 iteraciones
                  // Comprobación por episodios.
                  for(Episodio episodio : listaEpisodiosAmpliar){
                     List<PatronDictionaryFinalEvent> extensiones = patron.getExtensiones(episodio.getTipoInicio());
                     if(extensiones!=null && !extensiones.isEmpty()){
                        for(PatronDictionaryFinalEvent intermedio : extensiones){
                           List<PatronDictionaryFinalEvent> ext = intermedio.getExtensiones(episodio.getTipoFin());
                           if(ext!=null && !ext.isEmpty()){
                              IAsociacionDiccionario posible = (IAsociacionDiccionario)ext.get(0).getAsociacion();
                              //if(posible==null){ continue; }
                              //Siempre van a ser completos!
                              //if(((IAsociacionConEpisodios)posible).sonEpisodiosCompletos()){
                                 posible.recibeEvento(sid,evento, savePatternInstances,ext,encontrados);
                              //}
                           }
                        }
                     }
                  }
               }//en caso b
            }
            ventanaActual.clear();
            ventanaActual.addAll(encontrados);
            encontrados.clear();
         }
         sid++;
      }
   }

   /*
    * (non-Javadoc)
    * @see source.busqueda.jerarquia.MineDictionary#creaNodoFachada(source.modelo.IAsociacionTemporal,
    * source.modelo.arbol.Supernodo, java.lang.String)
    */
   @Override
   public Nodo creaNodoFachada(IAsociacionTemporal modelo, Supernodo supernodo, String tipo){
      Nodo n = new NodoAntepasadosAnotado(modelo, supernodo);
      supernodo.addNodo(n, tipo);
      return n;
   }

   /*
    * (non-Javadoc)
    * @see source.busqueda.jerarquia.MineDictionary#creaNodoFachada(source.modelo.IAsociacionTemporal)
    */
   @Override
   public Nodo creaNodoFachada(IAsociacionTemporal modelo){
      return new NodoAntepasadosAnotado(modelo);
   }

   //Copiado de MineCEDFESuperModelo
   protected void generarAsociacionesCandidatasSiguientes(List<String> tipos, int tam, List<Supernodo> nuevoNivel) throws FactoryInstantiationException{
      //LOGGER.info("Asociaciones siguientes a tam " + tam);
      //long inicio = System.currentTimeMillis();
      registroT.tiempo(TIEMPOS_ESTRATEGIA, tam-1, true);
      int i,j,k;
      String[] mod;
      IAsociacionTemporal[] asocBase = new IAsociacionTemporal[tam];
      IAsociacionTemporal modelo;
      Nodo[] nodosBase = new Nodo[tam];
      String[] tiposAdded = new String[tam];

      for(Supernodo supernodo : nuevoNivel){
         List<Nodo> nodos = supernodo.getListaNodos();
         int nSize = nodos.size();
         for(i=0;i<nSize;i++){
            Nodo padre = nodos.get(i);
            nodosBase[0] = padre;
            asocBase[0] = padre.getModelo();
            Supernodo hijos = padre.getHijos();
            for(j=i+1;j<nSize;j++){
               // Construir la asociación temporal
               Nodo madre = nodos.get(j);
               nodosBase[1] = madre;
               asocBase[1] = madre.getModelo();
               String tipoNuevo = asocBase[1].getTipos()[tam-2];
               tiposAdded[0] = tipoNuevo;
               tiposAdded[1] = asocBase[0].getTipos()[tam-2];

               mod = Arrays.copyOf(asocBase[0].getTipos(), asocBase[0].getTipos().length+1);
               mod[mod.length-1] = tipoNuevo;

               boolean valido=true;

               String tipo;
               int index=2;
               List<String> modAux = new ArrayList<String>(Arrays.asList(mod));
               for(k=tam-3;k>=0;k--){
                  tipo = modAux.remove(k);
                  Nodo subnodoAux = raizArbol.obtenerNodoEnArbol(modAux);
                  if(subnodoAux==null){
                     //Si se entra aquí se ha conseguido evitar una combinación que no daba
                     //resultado en la base de datos
                     //LOGGER.info("Se evita crear la asociación " + modAux);
                     //patronesNoGeneradosNivel[tam-1]+=; //TODO no siguen la definición de combinaciones de patrones
                     // frecuentes en la anterior iteración ya que no se ha comprobado todavía si son frecuentes o no
                     valido=false;
                     break;
                  }
                  asocBase[index] = subnodoAux.getModelo();
                  nodosBase[index] = subnodoAux;
                  tiposAdded[index] = tipo;
                  index++;
                  modAux.add(k, tipo);
               }

               if(!valido){ continue; }

               //TODO añadir parte episodios (es necesario?)
               //List<Episodio> eps = new ArrayList<Episodio>();
               //EpisodiosUtils.episodiosAsociacionBuscar(eps, mod, asocBase);
               modelo = AssociationFactory.getInstance().getAssociationInstance("ModeloAsociacion", mod, windowSize, numHilos);

               // Añadir el nuevo nodo a la lista de hijos del padre
               NodoAntepasados hijo = (NodoAntepasados)creaNodoFachada(modelo,hijos, tipoNuevo);
               SupernodoAdoptivos.fijarNodosAdoptivos(nodosBase, tiposAdded, hijo);
            }
         }
      }
      //tiemposEstrategia[tam-1] += System.currentTimeMillis() - inicio;
      registroT.tiempo(TIEMPOS_ESTRATEGIA, tam-1, false);
   }

   @Override
   protected void generarCandidatasGeneral(int tam, List<IAsociacionTemporal> candidatas, List<String> tipos,
           List<Supernodo> nuevoNivel) throws FactoryInstantiationException{
       if(tam==3){
           super.generarCandidatasGeneral(tam, candidatas, tipos, nuevoNivel);
           return;
       }else if(tam == 4){
           super.generarCandidatasGeneral(tam, candidatas, tipos, nuevoNivel);
           generarAsociacionesCandidatasSiguientes(tipos, tam+1, nuevoNivel);
           return;
       }
       // tam>=4

      int i=0,j=0;
      candidatasGeneradas = new ArrayList<IAsociacionTemporal>();
      IAsociacionTemporal modelo;
      String[] mod;
      GeneradorPatronesArbol genp = new GeneradorPatronesArbol(tam, this);

      // Inicializar mapa
      resetMapas(tipos);

      Nodo nodoAux = null;


      // No habría que recorrer simplemente nivelSiguiente y ver que asociaciones
      // tienen todos sus submodelos frecuentes?
      for(Supernodo supernodo : nivelActual){
         List<Nodo> nodos = supernodo.getListaNodos();
         int nSize = nodos.size();
         for(i=0;i<nSize;i++){
            Nodo padre = nodos.get(i);
            genp.setPadre(padre.getModelo(), 0);
            Supernodo hijos = padre.getHijos();
            for(j=i+1;j<nSize;j++){
               registroT.tiempoAsociaciones(tam-1, true);
               // Construir la asociación temporal
               Nodo madre = nodos.get(j);
               genp.setPadre(madre.getModelo(), 1);
               mod = genp.getModArray();

               boolean valido = true;
               nodoAux = raizArbol.obtenerNodoEnArbol(mod);
               //valido = nodoAux != null && nodoAux.getModelo().getSoporte()>=minFreq;
               //Si el nodo no se había creado ya no hay que comprobar si existen las subasociaciones
               if(nodoAux == null){
                  valido = false;
                  //asociacionesDescartadasLazy[tam-1]++;
                  //patronesDescartadosLazy[tam-1] += patCount[0]*patCount[1];
                  patronesNoGeneradosNivel[tam-1] += genp.getPatCount()[0]*genp.getPatCount()[1];
               }else{
                  // Si ya se había creado hay que comprobar que siguen existiendo las subasociaciones
                  // ya que han podido ser purgadas por no ser frecuentes
                  // Comprobar que las subasociaciones temporales son frecuentes
                  valido = genp.comprobarSubasociaciones(raizArbol, mod);
               }

               registroT.tiempoAsociaciones(tam-1, false);
               if(!valido){ continue; }

               // Combinar los patrones
               List<Patron> patrones = genp.generarPatrones(mod);

               // Construir el modelo
               registroT.tiempoModelo(tam-1, true);
               if(!patrones.isEmpty()){
                  // Hay: añadir punteros en la tabla hash y a candidatas

                  List<Episodio> eps = new ArrayList<Episodio>();
                  boolean buscar = EpisodiosUtils.episodiosAsociacionBuscar(eps, Arrays.asList(mod), genp.getAsociacionesBase());

                  if(eps.isEmpty()){
                     modelo = AssociationFactory.getInstance().getAssociationInstance(associationClassName,
                           mod, windowSize, patrones, supermodelo, numHilos);
                  }else{
                     modelo = AssociationFactory.getInstance().getAssociationInstance(associationClassName,
                           mod, windowSize, patrones, eps, supermodelo, numHilos);
                  }

                  notificarModeloGenerado(tam, patrones.size(), modelo, mod, buscar, candidatas,
                        candidatasGeneradas, mapa);

                  nodoAux.setModelo(modelo);

               }// else: No hay patrones candidatos: descartar modelo candidato actual
               registroT.tiempoModelo(tam-1, false);
            } // for j
            if(!hijos.getNodos().isEmpty()){
               nuevoNivel.add(hijos);
            }
         } // for i
      } //for supernodo

      generarAsociacionesCandidatasSiguientes(tipos, tam+1, nuevoNivel);
   }

   @Override
   public void escribirEstadisticasEstrategia(List<List<IAsociacionTemporal>> resultados,
         Writer fwp, boolean shortVersion, int maxIteracion) throws IOException {
      super.escribirEstadisticasEstrategia(resultados, fwp, shortVersion, maxIteracion);

      fwp.write(SEPARADOR);

      fwp.write("\nAnotaciones evitadas por la estrategia:\n");
      for(int i=0;i<anotacionesEvitadas.length;i++){
         fwp.write(nivel(i) + numberFormat(anotacionesEvitadas[i]) + "\n");
      }

      fwp.write("\nTiempos de cálculo de anotaciones útiles:\n");
      long[] tiemposEstrategia = registroT.getTiempos(TIEMPOS_ESTRATEGIA);
      for(int i=0;i<tiemposEstrategia.length;i++){
         fwp.write(nivel(i) + timeFormat(tiemposEstrategia[i]) + "\n");
      }
   }
}
