package source.busqueda.jerarquia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.excepciones.AlgoritmoException;
import source.excepciones.FactoryInstantiationException;
import source.modelo.AssociationFactory;
import source.modelo.IAsociacionTemporal;
import source.modelo.Modelo;
import source.modelo.arbol.ArbolFactory;
import source.modelo.arbol.Nodo;
import source.modelo.arbol.Supernodo;
import source.modelo.clustering.IClustering;
import source.modelo.jerarquia.ModeloDictionary;
import source.patron.Patron;
import source.patron.PatronDictionaryFinalEvent;

/**
 * Incorpora la generación de asociaciones temporales candidatas en tiempo de cálculo de frecuencia.
 * Es decir, calcula las asociaciones de la siguiente iteración en función de los patrones encontrados.
 * Por tanto, ya no sigue una estrategia apriori.
 *
 *
 * @author vanesa.graino
 *
 */
public class MineDictionaryLazy2 extends MineDictionary {
   private static final Logger LOGGER = Logger.getLogger(MineDictionaryLazy2.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   protected long[] asociacionesDescartadasLazy;
   protected long[] patronesDescartadosLazy;
   protected long[] tiempoLazy;
   protected int minFreq;

   {
      associationClassName = "ModeloDictionary";
      patternClassName = "PatronDictionaryFinalEvent";
   }

   /*
    * Constructores
    */

   public MineDictionaryLazy2(String executionId, boolean savePatternInstances, boolean saveAllAnnotations,
         boolean saveRemovedEvents, IClustering clustering, boolean removePatterns) {
      super(executionId, savePatternInstances, saveAllAnnotations, saveRemovedEvents, clustering, removePatterns);
   }

   /**
    * Este método almacena en las listas coinciden y noCoinciden los tipos de eventos que comparten
    * ambas listas y los que son propios de sólo una, respectivamente.
    * @param mod1 - lista de tipos de eventos (ordenada lexicográficamente)
    * @param mod2 - lista de tipos de eventos (ordenada lexicográficamente)
    * @param coinciden - lista en la que se guardan los tipos de eventos que tienen ambas listas
    * @param noCoinciden - lista en la que se guardan los tipos de eventos que sólo tiene una de las dos listas
    */
   protected void distintosYComunes(String[] mod1, String[] mod2, List<String> coinciden, List<String> noCoinciden){
      int i=0, j=0;
      //List<String> noCoinciden = new ArrayList<String>();
      while(i<mod1.length&&j<mod2.length){
         int comp = mod1[i].compareTo(mod2[j]);
         if(comp == 0){//iguales
            coinciden.add(mod1[i]);
            i++; j++;
         }else if(comp < 0){ //el valor de mod1 es anterior al de mod
            noCoinciden.add(mod1[i]);
            i++;
         }else {
            noCoinciden.add(mod2[j]);
            j++;
         }
      }
      for(;i<mod1.length;i++){
         noCoinciden.add(mod1[i]);
      }
      for(;j<mod2.length;j++){
         noCoinciden.add(mod2[j]);
      }
      //return noCoinciden;
   }

   //Para tomar ventaja de la ordenación lexicográfica
   /**
    * Este método buscan en la lista de listas anotada la lista mod desde el índice indiceInicial.
    * Se aprovecha de que las listas están ordenadas lexicográficamente para parar la búsqueda lo
    * antes posible.
    * @param anotadas - lista de modelos (es decir, de listas de tipos de eventos)
    * @param mod - lista de tipos de eventos
    * @param indiceInicial - índice por el que empezar a buscar
    * @return - el índice en anotadas de mod o -1 si no se ha encontrado.
    */
   protected int indexOfFirstOccurrence(List<String[]> anotadas, List<String> mod, int indiceInicial){
      boolean encontrado = false;
      int index, interno, internoMod, len, modSize = mod.size();

      for(index=indiceInicial; !encontrado && index<anotadas.size(); index++){
         String[] anotacion = anotadas.get(index);
         for(internoMod=0, len=anotacion.length, interno=0; internoMod>-1 && interno<len && internoMod<modSize; interno++){
            int comp = mod.get(internoMod).compareTo(anotacion[interno]);
            if(comp<0){ // no se va a poder encontrar
               internoMod = -1;
               break;
            }
            if(comp == 0){
               internoMod++;
            }
         }
         if(internoMod != -1 && internoMod == modSize){ //tenía todo lo de mod
            encontrado = true;
            return index;
         }
      }
      return encontrado? index : -1;
   }

   /**
    * @param nuevasAsociaciones - las asociaciones calculadas en base a las anotaciones de un evento
    * @throws FactoryInstantiationException
    */
   protected boolean insertarAsociacion(List<String> nuevaAsociacion) throws FactoryInstantiationException{
      if(raizArbol == null){ return false; }

      List<String> padre = nuevaAsociacion.subList(0, nuevaAsociacion.size()-1);
      Nodo nodoPadre = raizArbol.obtenerNodoEnArbol(padre);
      if(nodoPadre == null){
         return false;
      }

      //supernodo en el que vamos
      Supernodo supernodo = nodoPadre.getHijos();
      //Si no existe el supernodo lo creo
      if(supernodo == null){
         supernodo = ArbolFactory.getInstance().getSupernodo(treeClassName, nodoPadre);//new Supernodo(nodoPadre);
      }
      String tipoExtra = nuevaAsociacion.get(nuevaAsociacion.size()-1);
      Nodo nuevo = supernodo.getHijo(tipoExtra);
      if(nuevo == null){ //si no existe el nodo lo creo
         Modelo modelo = new Modelo(nuevaAsociacion.toArray(new String[nuevaAsociacion.size()]), windowSize, null);
         //nuevo = creaNodoFachada(modelo, supernodo);
         //supernodo.addNodoSorted(nuevo, tipoExtra); //insertar en orden
         creaNodoFachada(modelo, supernodo, tipoExtra);
      }
      Modelo m = (Modelo)nuevo.getModelo();
      m.incrementarSoporte();
      //LOGGER.info("La asociacion " + nuevaAsociacion + " tiene soporte " + m.getSoporte());

      return true;
   }

   //Con este método hay una única ordenación por combinación que se prueba
   protected List<String[]> genAsociacionesTemporales(List<String[]> anotadas, String evento, int tam) throws FactoryInstantiationException {
      long inicio = System.currentTimeMillis();
      List<String[]> asociaciones = new ArrayList<String[]>();
      int i,j, len=anotadas.size();
      for(i=0; i<len-tam+1; i++){
         for(j=i+1; j<len-tam+2; j++){
            List<String> distintos = new ArrayList<String>(), iguales = new ArrayList<String>();
            distintosYComunes(anotadas.get(j), anotadas.get(i), iguales, distintos);
            if(distintos.size() == 2){//solo difieren en un elemento

               List<String> comb = new ArrayList<String>(iguales);
               comb.addAll(distintos);
               Collections.sort(comb);

               int encontrados = 0;
               int index = j+1, indexAux;
               List<String> buscable = new ArrayList<String>();
               for(int tapar=tam; tapar>=0; tapar--){
                  if(comb.get(tapar) == evento){ continue; }
                  buscable.clear();
                  buscable.addAll(comb);
                  buscable.remove(tapar);

                  indexAux = indexOfFirstOccurrence(anotadas, buscable, index);
                  if(indexAux == -1){ continue; }
                  index=indexAux;
                  encontrados++;
               }
               //2 ya se han utilizado para crear la combinación así que sabemos que 2 por lo menos hay
               if(encontrados == tam-2){
                  String[] combArray = comb.toArray(new String[comb.size()]);
                  asociaciones.add(combArray);
                  insertarAsociacion(comb);
               }
            }
         }
      }
      tiempoLazy[tam-1] += System.currentTimeMillis() - inicio;
      return asociaciones;
   }

   protected List<String[]> genAsociacionesTemporales(List<Patron> anotaciones, String evento){
      try{
         if(anotaciones.isEmpty()){ return new ArrayList<String[]>(); } //Si no hay ya podemos salir
         int tam = anotaciones.get(0).getTipos().length;
         return genAsociacionesTemporales(listaAnotaciones(anotaciones), evento, tam);
      }catch(FactoryInstantiationException fie){
         LOGGER.log(Level.SEVERE, "Fallando al crear una asociación", fie);
         throw new RuntimeException("Se sale del programa");
      }
   }

   /**
    * Crea una lista de las asociaciones temporales ordenadas y sin duplicados
    * @param anotaciones
    * @return
    */
   protected List<String[]> listaAnotaciones(List<Patron> anotaciones){
      List<String[]> anotadas = new ArrayList<String[]>();
      String[] anterior = anotaciones.get(0).getTipos();

      anotadas.add(anterior);

      boolean added;
      int j;
      // Creamos la lista de asociaciones (sin repetidas)
      for(int i=1, len=anotaciones.size(); i<len; i++){
         String[] actual = anotaciones.get(i).getTipos();
         int comparacion = comparar(anterior, actual);
         if(comparacion == 1){ //actual no esta ordenada respecto a ultima
            added = false;
            j=anotadas.size()-2;
            while(!added){
               int comparacion2 = j<0? 1: comparar(actual,anotadas.get(j));
               if(comparacion2 == 1){
                  anotadas.add(j+1,actual);
                  added = true;
               }else if(comparacion2 == 0){
                  added = true;
               }
               j--;
            }
         }else if(comparacion == -1){ //actual está ordenada
            anterior = actual;
            anotadas.add(anterior);
         }
         //else el elemento está repetido
      }
      return anotadas;
   }

   protected int comparar(String[] primera, String[] segunda) {
      for(int i=0,len=primera.length;i<len;i++){
         int comparison =primera[i].compareTo(segunda[i]);
         if(comparison < 0){
            return -1;//true;
         }else if(comparison > 0 ){
            return 1;//false;
         }
      }
      return 0;//false;
   }

   @Override
   protected void calcularSoporteTam3(IColeccion coleccion){
      // Asociaciones de tamaño 3, aquí aún no hay ningún tipo de señalización
      //sobre qué asociaciones ocurren en cada ventana.
      final int tam = 3;
      int sid = 0;

      Iterator<List<List<Patron>>> itActual = anotaciones.getActual().iterator();
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
               ((ModeloDictionary)receptor).actualizaVentana(sid, evento);
               ((ModeloDictionary)receptor).recibeEvento(sid,evento,savePatternInstances,lista,ventanaActual);
            }
            //calcular asociaciones para la siguiente iteración
            genAsociacionesTemporales(ventanaActual, evento.getTipo());
         }
         sid++;
      }
      LOGGER.fine("Tiempo de lazy: " + tiempoLazy[tam-1] + " millisec.");
   }

   @Override
   protected void calcularSoporteGeneral(IColeccion coleccion, int tam){
      // Caso general. Se tiene calculado qué asociaciones podrían ocurrir en cada
      //posible ventana temporal. Para cada evento leído, se comprueba qué asociaciones
      //de entre las posibles realmente ocurren y, finalmente, se calcula qué asociaciones
      //se podrían encontrar en la siguiente iteración en base a las encontradas.
      Iterator<List<List<Patron>>> itActual = anotaciones.getActual().iterator();
      List<Patron> encontrados = new ArrayList<Patron>();
      List<String> listaTipos = new ArrayList<String>();
      int sid = 0;
      for(ISecuencia secuencia : coleccion){
         Iterator<List<Patron>> itVentanaActual = itActual.next().iterator();
         for(Evento evento : secuencia){
            List<Patron> ventanaActual = itVentanaActual.next();

            // Actualizar ventana de las asociaciones temporales con el tipo de evento leído
            List<IAsociacionTemporal> receptores = mapa.get(evento.getTipo());
            for(IAsociacionTemporal aux : receptores){
               ((ModeloDictionary)aux).actualizaVentana(sid, evento);
            }
            // Calcular las asociaciones temporales a comprobar para el evento actual
            listaTipos = posiblesTiposParaAmpliar(ventanaActual, listaTipos);
            for(Patron aux : ventanaActual){
               PatronDictionaryFinalEvent patron = (PatronDictionaryFinalEvent)aux;
               for(String tipo : listaTipos){
                  List<PatronDictionaryFinalEvent> extensiones = patron.getExtensiones(tipo);
                  if(extensiones!=null && !extensiones.isEmpty()){
                     //Nodo nodo = raizArbol.obtenerNodoEnArbol(extensiones.get(0).getTipos());
                     //ModeloDictionary posible = (ModeloDictionary)nodo.getModelo();
                     ModeloDictionary posible = (ModeloDictionary)extensiones.get(0).getAsociacion();
                     posible.recibeEvento(sid,evento,savePatternInstances,extensiones,encontrados);
                  }
               }
            }

            ventanaActual.clear();
            ventanaActual.addAll(encontrados);
            encontrados.clear();

            // calcular asociaciones para la siguiente iteracion
            genAsociacionesTemporales(ventanaActual, evento.getTipo());
         }
         sid++;
      }
      LOGGER.fine("Tiempo de lazy: " + tiempoLazy[tam-1] + " millisec.");
   }

   @Override
   public List<List<IAsociacionTemporal>> buscarModelosFrecuentes(List<String> tipos, IColeccion coleccion,
         int supmin, int win) throws AlgoritmoException{
      minFreq = supmin;
      // Continuar normalmente
      return super.buscarModelosFrecuentes(tipos, coleccion, supmin, win);
   }

   @Override
   protected void generarCandidatasGeneral(int tam, List<IAsociacionTemporal> candidatas, List<String> tipos,
           List<Supernodo> nuevoNivel) throws FactoryInstantiationException{
      if(tam == 3){
         super.generarCandidatasGeneral(tam, candidatas, tipos, nuevoNivel);
         return;
      }
      int i, j;
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
            for(j=i+1; j<nSize; j++){
               registroT.tiempoAsociaciones(tam-1, false);
               // Construir la asociación temporal
               Nodo madre = nodos.get(j);
               genp.setPadre(madre.getModelo(), 1);
               mod = genp.getModArray();

               boolean valido=true;
               nodoAux = raizArbol.obtenerNodoEnArbol(mod);
               //valido = nodoAux != null && nodoAux.getModelo().getSoporte()>=minFreq;
               if(nodoAux == null || nodoAux.getModelo().getSoporte()<minFreq){
                  valido = false;
                  asociacionesDescartadasLazy[tam-1]++;
                  patronesNoGeneradosNivel[tam-1] += genp.getPatCount()[0]*genp.getPatCount()[1];
               }else{
                  // Comprobar que las subasociaciones temporales son frecuentes
                  valido = genp.comprobarSubasociaciones(raizArbol, mod);
               }
               registroT.tiempoAsociaciones(tam-1, false);

               if(!valido){ continue; }

               // Combinar los patrones
               List<Patron> patrones = genp.generarPatrones(mod);

               // Construir el modelo si hay patrones
               registroT.tiempoModelo(tam-1, true);
               if(!patrones.isEmpty()){
                  // Hay: añadir punteros en la tabla hash y a candidatas
                  modelo = AssociationFactory.getInstance().getAssociationInstance(associationClassName,
                        mod, windowSize, patrones, numHilos);

                  notificarModeloGenerado(tam, patrones.size(), modelo, mod, candidatas, mapa);

                  //setModeloPatrones(patrones, modelo);

                  //En la asociaciones de tamaño 3 hay que crear el nodo, pero en las de mayor tamaño
                  // el nodo ya existe, sólo hay que cambiar el modelo
                  if(tam == 3){
                     //Nodo hijo = creaNodoFachada(modelo,hijos);
                     // Añadir el Nodo al nuevo
                     //hijos.addNodo(hijo, tipoNuevo);
                     creaNodoFachada(modelo, hijos, genp.getTipoNuevo());
                  }else{
                     nodoAux.setModelo(modelo);
                  }
               }// else: No hay patrones candidatos: descartar modelo candidato actual
               registroT.tiempoModelo(tam-1, false);
            } // for j
            if(!hijos.getNodos().isEmpty()){
               nuevoNivel.add(hijos);
            }
         } // for i
      } //for supernodos

      LOGGER.fine("Descartados por lazy: " + asociacionesDescartadasLazy[tam-1]);

   }

   protected boolean comprobarSubasociaciones(int tam, String[] mod, Supernodo raizArbol,
         IAsociacionTemporal[] patBase, int[] patCount){
      boolean valido = true;
      String tipo;
      int index=2;
      List<String> modAux = new ArrayList<String>(Arrays.asList(mod));
      for(int k=tam-3;valido && k>=0;k--){
         tipo = modAux.remove(k);
         Nodo subnodoAux = raizArbol.obtenerNodoEnArbol(modAux);
         if(subnodoAux==null){
            valido=false;
            break;
         }
         patBase[index] = subnodoAux.getModelo();
         patCount[index] = patBase[index].getPatrones().size();
         index++;
         modAux.add(k, tipo);
      }
      return valido;
   }

   @Override
   public void iniciarContadores (int tSize, int cSize) {
      super.iniciarContadores(tSize, cSize);
      asociacionesDescartadasLazy = new long[tSize];
      patronesDescartadosLazy = new long[tSize];
      tiempoLazy = new long[tSize];
   }

}
