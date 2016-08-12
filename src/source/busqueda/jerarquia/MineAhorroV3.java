package source.busqueda.jerarquia;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.excepciones.FactoryInstantiationException;
import source.modelo.AssociationFactory;
import source.modelo.IAsociacionDiccionario;
import source.modelo.IAsociacionTemporal;
import source.modelo.arbol.Nodo;
import source.modelo.arbol.NodoAntepasados;
import source.modelo.arbol.Supernodo;
import source.modelo.arbol.SupernodoAdoptivos;
import source.modelo.clustering.IClustering;
import source.patron.Patron;
import source.patron.PatronDictionaryFinalEvent;

/**
 * Estrategia de ahorro con el árbol normal, no el extendido como utilizan {@link MineAhorro}
 * y {@link MineAhorroV1}
 * TODO cambiar getAnotacionesEventoUtiles para que no utilice el arbol extendido deberia haber
 * una versión en el repositorio
 * @author vanesa.graino
 *
 */
public class MineAhorroV3 extends MineDictionary{
   private static final String TIEMPOS_ESTRATEGIA = "estrategia";
   private static final Logger LOGGER = Logger.getLogger(MineAhorroV3.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   /**
    * Número de anotaciones que se han eliminado por no ser útiles, en cada iteración
    */
   private int[] eventosSinAnotaciones; //# de eventos que gracias a la estrategia dejan de tener una lista de anotaciones
   protected long[] anotacionesEvitadas;
   //protected long[] tiemposEstrategia;

   /*
    * Constructores
    */

   public MineAhorroV3(String executionId, boolean savePatternInstances,
         boolean saveAllAnnotations, boolean saveRemovedEvents,
         IClustering clustering, boolean removePatterns) {
      super(executionId, savePatternInstances, saveAllAnnotations, saveRemovedEvents,
            clustering, removePatterns);
      registroT.addOtrosTiempos(TIEMPOS_ESTRATEGIA);
   }

   /*
    * Métodos
    */

   @Override
   protected void iniciarContadores(int tSize, int cSize) {
      super.iniciarContadores(tSize, cSize);
      anotacionesEvitadas = new long[tSize+1];
      //tiemposEstrategia = new long[tSize+1];
      eventosSinAnotaciones = new int[tSize+1];
   }

   @Override
   protected void calcularSoporteTam3(IColeccion coleccion){
      // Asociaciones de tamaño 3, aquí aún no hay ningún tipo de señalización
      //sobre qué asociaciones ocurren en cada ventana.
      int sid = 0;
      Iterator<List<List<Patron>>> itActual = anotaciones.getActual().iterator();
      List<Patron> encontrados = new ArrayList<Patron>();
      for(ISecuencia secuencia : coleccion){
         Iterator<List<Patron>> itVentanaActual = itActual.next().iterator();
         for(Evento evento : secuencia){
            List<Patron> ventanaActual = itVentanaActual.next();
            ventanaActual.clear();
            // Se recorre la lista de la tabla hash
            List<IAsociacionTemporal> receptores = mapa.get(evento.getTipo());
            for(IAsociacionTemporal receptor : receptores){
               List<Patron> aux = receptor.getPatrones();
               List<PatronDictionaryFinalEvent> lista = new ArrayList<PatronDictionaryFinalEvent>();
               for(Patron patron : aux){
                  lista.add((PatronDictionaryFinalEvent)patron);
               }
               ((IAsociacionDiccionario)receptor).actualizaVentana(sid, evento);
               ((IAsociacionDiccionario)receptor).recibeEvento(sid,evento, savePatternInstances,lista,encontrados);
            }
            boolean antes = !encontrados.isEmpty();
            setAnotacionesEvento(encontrados, ventanaActual, evento.getTipo(), 3);
            if(antes && ventanaActual.isEmpty()){ eventosSinAnotaciones[3-1]++; }
         }
         sid++;
      }
   }

   @Override
   protected void calcularSoporteGeneral(IColeccion coleccion, int tamActual){
      // Caso general. Se tiene calculado qué asociaciones podrían ocurrir en cada
      //posible ventana temporal. Para cada evento leído, se comprueba qué asociaciones
      //de entre las posibles realmente ocurren y, finalmente, se calcula qué asociaciones
      //se podrían encontrar en la siguiente iteración en base a las encontradas.
      //long tiempoV=0, tiempoA=0;
      Iterator<List<List<Patron>>> itActual = anotaciones.getActual().iterator();
      List<Patron> encontrados = new ArrayList<Patron>();
      List<String> listaTipos = new ArrayList<String>();
      int sid = 0;
      for(ISecuencia secuencia : coleccion){
         Iterator<List<Patron>> itVentanaActual = itActual.next().iterator();
         for(Evento evento : secuencia){
            List<Patron> ventanaActual = itVentanaActual.next();
            //long inicio = System.currentTimeMillis();
            registroT.tiempo(TIEMPOS_VENTANA, tamActual-1, true);
            // Actualizar ventana de las asociaciones temporales con el tipo de evento leído
            List<IAsociacionTemporal> receptores = mapa.get(evento.getTipo());
            for(IAsociacionTemporal receptor : receptores){
               ((IAsociacionDiccionario)receptor).actualizaVentana(sid, evento);
            }
            //tiempoV += System.currentTimeMillis() - inicio;
            registroT.tiempo(TIEMPOS_VENTANA, tamActual-1, false);
            //inicio = System.currentTimeMillis();
            registroT.tiempo(TIEMPOS_ANOTACIONES, tamActual-1, true);
            // Calcular las asociaciones temporales a comprobar para el evento actual
            listaTipos = posiblesTiposParaAmpliar(ventanaActual, listaTipos);
            for(Patron aux : ventanaActual){
               PatronDictionaryFinalEvent patron = (PatronDictionaryFinalEvent)aux;
               for(String tipo : listaTipos){
                  List<PatronDictionaryFinalEvent> extensiones = patron.getExtensiones(tipo);
                  if(extensiones!=null && !extensiones.isEmpty()){
                     //Nodo nodo = raizArbol.obtenerNodoEnArbol(extensiones.get(0).getTipos());
                     //IAsociacionDiccionario posible = (IAsociacionDiccionario)nodo.getModelo();
                     IAsociacionDiccionario posible = (IAsociacionDiccionario)extensiones.get(0).getAsociacion();
                     posible.recibeEvento(sid, evento, savePatternInstances, extensiones, encontrados);
                  }
               }
            }
            //tiempoA += System.currentTimeMillis() - inicio;
            registroT.tiempo(TIEMPOS_ANOTACIONES, tamActual-1, false);

            boolean antes = !encontrados.isEmpty();
            setAnotacionesEvento(encontrados, ventanaActual, evento.getTipo(), tamActual);
            if(antes && ventanaActual.isEmpty()){
               eventosSinAnotaciones[tamActual-1]++;
            }
         }
         sid++;
      }
      //tiemposAnotaciones[tamActual-1] = tiempoA;
      //tiemposVentana[tamActual-1] = tiempoV;
   }

   protected List<Boolean> getAnotacionesEventoUtiles(List<String[]> allModels, List<Boolean> utiles,
         List<String> listaEventos, String evento, int tam){
      for(int i=0; i<allModels.size(); i++){
         //Si ya habiamos comprobado que la asociacion es util continuamos
         if(utiles.get(i)){ continue; }
         // Debido a esta línea cuando estamos analizando las subasociaciones de un candidato
         // no podemos empezar en i+1
         String[] m = allModels.get(i);

         Nodo nodo = raizArbol.obtenerNodoEnArbol(m);
         SupernodoAdoptivos hijos = (SupernodoAdoptivos)nodo.getHijos();

         List<String> mExt = new ArrayList<String>(Arrays.asList(m));
         List<Integer> indices = new ArrayList<Integer>();
         for(int j=0;j<listaEventos.size();j++){
            indices.clear();
            String e = listaEventos.get(j);
            int insertionPoint = Collections.binarySearch(mExt, e);
            //La asociacion ya tiene este evento, pasamos
            if(insertionPoint > -1){ continue; }
            insertionPoint = -insertionPoint - 1;
            mExt.add(insertionPoint, e); //se inserta ordenadamente

            //Si existe el hijo/hijo adoptivo en el nodo
            if(hijos.getHijo(e, insertionPoint == mExt.size()-1) != null ){
               for(int k=tam;k>=0;k--){
                  if(k == insertionPoint){ continue; }
                  String borrado = mExt.remove(k);
                  // Si el borrado es del evento anotado, no nos sirve
                  if(borrado.equals(evento)){
                     mExt.add(k,borrado);
                     continue;
                  }
                  //Buscamos la anotacion en las anotaciones
                  for(int l=0; l<allModels.size();l++){
                     if(i==l){ continue; }
                     if(Arrays.asList(allModels.get(l)).equals(mExt)){
                        indices.add(l);
                        break;
                     }
                  }

                  //Volvemos a meter el evento
                  mExt.add(k,borrado);
               }
               if(indices.size() >= tam-1){
                  utiles.set(i,true);
                  for(int iUtil : indices){ utiles.set(iUtil, true); }
                  break;
               }
            }//Fin de busqueda de subasociaciones para mExt

            mExt.remove(insertionPoint);
         }//Fin de extender a m

      }//Fin de recorrer las asociaciones

      return utiles;
   }

   /**
    * Fija las anotaciones para un evento purgando aquellas que no son suficientes para extender
    * y por lo tanto no serán útiles en un futuro.
    * Para procesar las anotaciones llama al método {@link #getAnotacionesEventoUtiles}.
    * @param encontrados - los patrones encontrados en el evento.
    * @param ventanaActual - la lista de anotaciones del evento en la que se guardarán las nuevas anotaciones.
    * @param evento - el tipo de evento del evento anotado.
    * @param tam - el tamaño de las asociaciones temporales actuales.
    */
   protected void setAnotacionesEvento(List<Patron> encontrados, List<Patron> ventanaActual, String evento, int tam){
      ventanaActual.clear();
      int eSize = encontrados.size();
      if(eSize<tam){
         anotacionesEvitadas[tam-1]+=eSize;
         encontrados.clear();
         return;
      }

      //long inicio = System.currentTimeMillis();
      registroT.tiempo(TIEMPOS_ESTRATEGIA, tam-1, true);

      Set<String> allTypes = new TreeSet<String>();
      List<String[]> allModels = new ArrayList<String[]>(eSize);
      List<Boolean> utiles = new ArrayList<Boolean>(eSize);
      Collections.sort(encontrados);

      //Para no tener asociaciones repetidas
      List<Integer> correspondencia = new ArrayList<Integer>(eSize);
      int indice = 0;
      String[] anterior = encontrados.get(0).getTipos();
      allTypes.addAll(Arrays.asList(anterior));
      allModels.add(anterior);
      utiles.add(false);
      correspondencia.add(0);

      //for(Patron e : encontrados){
      for(int i=1; i<eSize;i++){
         String[] ts = encontrados.get(i).getTipos();
         if(!ts.equals(anterior)){
            indice++;
            allTypes.addAll(Arrays.asList(ts));
            allModels.add(ts);
            utiles.add(false);
            anterior = ts;
         }
         correspondencia.add(indice);
      }

      //Si el número de asociaciones es menor que las necesarias para extender acabamos
      if(allModels.size() < tam){
         //tiemposEstrategia[tam-1] += System.currentTimeMillis() - inicio;
         registroT.tiempo(TIEMPOS_ESTRATEGIA, tam-1, false);
         anotacionesEvitadas[tam-1]+=eSize;
         encontrados.clear();
         return;
      }

      List<String> allTypes2 = new ArrayList<String>(allTypes);
      allTypes2.remove(evento);

      utiles = getAnotacionesEventoUtiles(allModels, utiles, allTypes2, evento, tam);

      for(int i=0; i<eSize; i++){
         if(!utiles.get(correspondencia.get(i))){
            correspondencia.remove(i);
            encontrados.remove(i);
            i--;
            eSize--;
            anotacionesEvitadas[tam-1]++;
         }
      }
      //tiemposEstrategia[tam-1] += System.currentTimeMillis() - inicio;
      registroT.tiempo(TIEMPOS_ESTRATEGIA, tam-1, false);

      ventanaActual.addAll(encontrados);
      encontrados.clear();
   }

   protected void generarAsociacionesCandidatasSiguientes(List<String> tipos, int tam, List<Supernodo> nuevoNivel) throws FactoryInstantiationException{
      LOGGER.info("Asociaciones siguientes a tam " + tam);
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

               //mod = new ArrayList<String>(asocBase[0].getTipos());
               //mod.add(mod.size(),tipoNuevo);
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

               modelo = AssociationFactory.getInstance().getAssociationInstance("ModeloAsociacion", mod, windowSize, numHilos);


               // Añadir el nuevo nodo a la lista de hijos del padre
               NodoAntepasados hijo = (NodoAntepasados)creaNodoFachada(modelo,hijos, tipoNuevo);
               SupernodoAdoptivos.fijarNodosAdoptivos(nodosBase, tiposAdded, hijo);
            }
         }
      }

      //mapaFuturas = nuevoMapa;
      //nivelSiguiente = nuevoNivel;
      //tiemposEstrategia[tam-1] += System.currentTimeMillis() - inicio;
      registroT.tiempo(TIEMPOS_ESTRATEGIA, tam-1, false);
   }

   // Este método es casi igual al de MineDictionaryLazy sólo que no utiliza el umbral de frecuencia al
   // crear los patrones de la iteración actual para purgar asociaciones temporales.
   @Override
   protected void generarCandidatasGeneral(int tam, List<IAsociacionTemporal> candidatas, List<String> tipos,
           List<Supernodo> nuevoNivel) throws FactoryInstantiationException{
      if(tam == 3){
         super.generarCandidatasGeneral(tam, candidatas, tipos, nuevoNivel);
         generarAsociacionesCandidatasSiguientes(tipos, tam+1, nuevoNivel);
         return;
      }
      //tam>=3
      int i=0,j=0;
      IAsociacionTemporal modelo;
      String[] mod;
      GeneradorPatronesArbol genp = new GeneradorPatronesArbol(tam, this);

      // Inicializar mapa
      resetMapas(tipos);

      Nodo nodoAux = null;

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

               boolean valido=true;
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
                  modelo = AssociationFactory.getInstance().getAssociationInstance(associationClassName, mod, windowSize, patrones, numHilos);

                  notificarModeloGenerado(tam, patrones.size(), modelo, mod, candidatas, mapa);
                  //El nodo siempre existe en asociaciones de tamaño > 3
                  nodoAux.setModelo(modelo);

               }// else: No hay patrones candidatos: descartar modelo candidato actual
               registroT.tiempoModelo(tam-1, false);
            } // for j
            if(!hijos.getNodos().isEmpty()){
               nuevoNivel.add(hijos);
            }
         } // for i
      } //for supernodos
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

      long[] tiemposEstrategia = registroT.getTiempos(TIEMPOS_ESTRATEGIA);
      fwp.write("\nTiempos de cálculo de anotaciones útiles:\n");
      for(int i=0;i<tiemposEstrategia.length;i++){
         fwp.write(nivel(i) + timeFormat(tiemposEstrategia[i]) + "\n");
      }
      fwp.write("\n");
   }

}
