package source.busqueda.negacion;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import source.busqueda.GeneradorPatrones;
import source.busqueda.jerarquia.MineArbolSuperModelo;
import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.excepciones.FactoryInstantiationException;
import source.modelo.AssociationFactory;
import source.modelo.IAsociacionEvento;
import source.modelo.IAsociacionTemporal;
import source.modelo.arbol.ArbolFactory;
import source.modelo.arbol.Nodo;
import source.modelo.arbol.Supernodo;
import source.modelo.arbol.SupernodoNegacion;
import source.modelo.clustering.IClustering;
import source.modelo.negacion.HelperModeloNegacion;
import source.modelo.negacion.IAsociacionConNegacion;
import source.modelo.negacion.SuperModeloNegacion;
import source.patron.Patron;

/**
 * Recorre todas las ventanas usa SuperModeloNegacion para ello.
 *
 * La ventana se mueva de forma que entran y salen todos lso eventos de un instante, no
 * evento a evento.
 *
 * Las ocurrencias positivas no deberían variar con respecto a ASTP.
 *
 * @author vanesa.graino
 *
 */
public class NegacionMine extends MineArbolSuperModelo {
   private static final Logger LOGGER = Logger.getLogger(NegacionMine.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   /*
    * Atributos propios
    */

   Map<String,List<IAsociacionConNegacion>> mapaNegados; // mapa con la lista de asociaciones que niegan
   protected long[] patronesSinRestriccionesNivel;
   protected long[] asociacionesNegacionNivel;
   protected long[] patronesConNegacionNivel;

   /*
    * Constructors
    */

   {
      treeClassName = "SupernodoNegacion";
      associationClassName = "Modelo";
      patternClassName = "PatronNegacion";
   }

   public NegacionMine(String executionId,
         boolean savePatternInstances, boolean saveRemovedEvents,
         IClustering clustering, boolean removePatterns) {
      super(executionId, savePatternInstances, saveRemovedEvents, clustering,
            removePatterns);
   }

   /*
    * (non-Javadoc)
    * @see source.busqueda.MineArbol#inicializaEstructuras(java.util.List, java.util.List, int)
    */
   @Override
   protected void inicializaEstructuras(List<String> tipos,
         List<IAsociacionTemporal> actual, int win, int cSize) throws FactoryInstantiationException {
      int tSize = tipos.size();
      iniciarContadores(tSize, cSize);
      windowSize = win;

      mapa = new HashMap<String,List<IAsociacionTemporal>>(tSize);
      //Map<String, List<IAsociacionTemporal>> mapa = new HashMap<String,List<IAsociacionTemporal>>(tSize);
      //mapaGeneradas = new HashMap<String,List<IAsociacionTemporal>>(tSize);
      //candidatasGeneradas = new ArrayList<IAsociacionTemporal>(tSize);

      // Crear el supernodo del árbol
      raizArbol = ArbolFactory.getInstance().getSupernodo(treeClassName);//new Supernodo();
      //raizArbol = new SupernodoNegacion();
      nivelActual = new ArrayList<Supernodo>();
      nivelActual.add(raizArbol);

      List<IAsociacionTemporal> listaNegados = new ArrayList<IAsociacionTemporal>(tSize);
      //final int tam = 1;
      // Modelos evento positivos
      for(String tipo : tipos){
         IAsociacionConNegacion modeloPos = AssociationFactory.getInstance().getAssociationInstance("ModeloEvento", tipo, win, false, numHilos);
         notificarModeloGenerado(modeloPos, tipo, actual, mapa);
         creaNodoFachada(modeloPos, raizArbol, tipo, true);
      }
      // Negados (tienen que ir al final para que se respete el orden)
      // No se añaden a los candidatos, solo se utilizarán para crear modelos de tamaño 2
      for(String tipo : tipos){
         //Modelo evento negado
         IAsociacionConNegacion modeloNeg = AssociationFactory.getInstance().getAssociationInstance(associationClassName, tipo, win, true, numHilos);
         //notificarModeloGenerado(modeloNeg, tipo, listaNegados, mapa);
         listaNegados.add(modeloNeg);
         creaNodoFachada(modeloNeg, raizArbol, tipo, false);
      }
      actual.addAll(listaNegados);

      //setMapa(mapa);
      supermodelo = new SuperModeloNegacion(tipos.toArray(new String[tipos.size()]), win);
      mapaNegados = construyeMapa(tipos.size(), tipos);
   }

   @Deprecated
   @Override
   public Nodo creaNodoFachada(IAsociacionTemporal modelo, Supernodo supernodo, String tipo){
      Nodo n = new Nodo(modelo, supernodo);
      supernodo.addNodo(n, tipo);
      return n;
   }

   @Override
   protected List<String> purgarTiposYEventos(IColeccion coleccion, List<IAsociacionTemporal> actual,
         List<String> tipos, int tSize){
      List<String> tipoFinales = tipos;
      // Ya sabemos qué tipos de eventos son frecuentes, eliminar los que no lo son
      if(actual.size() != tSize){
         tipoFinales = new ArrayList<String>();
         for(IAsociacionTemporal modelo : actual){
            //if(((IAsociacionConNegacion)modelo).partePositiva()){
            if(((IAsociacionConNegacion)modelo).partePositiva()){
               tipoFinales.add(((IAsociacionEvento)modelo).getTipoEvento());
            }
         }
         purgarEventosDeTiposNoFrecuentes(coleccion, tipos);
      }
      return tipoFinales;
   }

   /*
    * Se busca en el árbol con los positivos y los negativos
    * (non-Javadoc)
    * @see source.busqueda.MineArbol#purgarCandidatas(java.util.List, int, int)
    */
   @Override
   protected void purgarCandidatas(List<IAsociacionTemporal> candidatas, int supmin, int tamActual){
      registroT.tiempoPurgar(tamActual-1, true);
      for(int i=candidatas.size()-1;i>=0;i--){
         IAsociacionConNegacion modelo = (IAsociacionConNegacion)candidatas.get(i);
         if(modelo.necesitaPurga(supmin)){
            candidatas.remove(i); // Eliminar de candidatas
            if(raizArbol != null){
               ((SupernodoNegacion)raizArbol).eliminarNodoEnArbol(modelo.getTipos(), modelo.getTiposNegados());
            }
         }else{
            // Se considera como un patrón una asociación temporal con un único tipo de evento positivo
            patronesFrecuentesNivel[tamActual-1] += modelo.getTipos().length>1? modelo.getPatrones().size() : 1;

            if(modelo.parteNegativa()){
               patronesConNegacionNivel[tamActual-1] += modelo.getPatrones().isEmpty() ? 1 : modelo.getPatrones().size();
            }
         }
      }
      registroT.tiempoPurgar(tamActual-1, false);
   }

   @Override
   protected void generarCandidatasTam2(List<IAsociacionTemporal> anteriores, List<String> tipos,
         List<IAsociacionTemporal> candidatas, List<Supernodo> nuevoNivel) throws FactoryInstantiationException{
      //listaNegados.clear();
      List<Nodo> nodos = raizArbol.getListaNodos();
      final int  nSize = nodos.size(), tam=2;
      int i,j;

      List<String[]> comb;
      IAsociacionEvento mod1, mod2;

      mapa = construyeMapa(tipos.size(), tipos);

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

//            if(comb.get(1).length>0){ //hay un negativo
//               patronesConNegacionNivel[tam-1]++;
//            }

            //IAsociacionConNegacion modelo = AssociationFactory.getInstance().getAssociationInstance(associationClassName,
            //      comb.get(0), comb.get(1), windowSize, getClustering(), supermodelo, numHilos);
            IAsociacionConNegacion modelo = crearModelo(comb);

            notificarModeloGenerado(tam, 0, modelo, comb.get(0), comb.get(1), candidatas, mapa, true);

            creaNodoFachada(modelo, hijos, mod2.getTipoEvento(), ((IAsociacionConNegacion)mod2).partePositiva());

            /*if(modelo.hayParteNegativa()){
               listaNegados.add(modelo);
            }*/
         }
         if(!hijos.getNodos().isEmpty()){
            nuevoNivel.add(hijos);
         }
      }
   }

   IAsociacionConNegacion crearModelo(List<String[]> comb) throws FactoryInstantiationException{
      return AssociationFactory.getInstance().getAssociationInstance(associationClassName,
            comb.get(0), comb.get(1), windowSize, getClustering(), supermodelo, numHilos);
   }

   @Override
   protected void iniciarContadores(int tSize, int cSize) {
      // TODO Auto-generated method stub
      super.iniciarContadores(tSize, cSize);
      patronesSinRestriccionesNivel = new long[tSize];
      asociacionesNegacionNivel = new long[tSize];
      patronesConNegacionNivel = new long[tSize];
   }

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

                  notificarModeloGenerado(tam, patrones.size(), modelo, comb.get(0), comb.get(1), candidatas,
                        mapa, true);
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


   public Nodo creaNodoFachada(IAsociacionTemporal modelo, Supernodo supernodo, String tipo, boolean esPositivo) throws FactoryInstantiationException{
      //SupernodoNegacion sn = new SupernodoNegacion();
      SupernodoNegacion sn = (SupernodoNegacion)ArbolFactory.getInstance().getSupernodo(treeClassName);
      Nodo n = new Nodo(modelo, sn, supernodo);
      sn.setPadre(n);
      ((SupernodoNegacion)supernodo).addNodo(n, tipo, esPositivo);
      return n;
   }

   @Override
   public Nodo creaNodoFachada(IAsociacionTemporal modelo) throws FactoryInstantiationException{
      //SupernodoNegacion sn = new SupernodoNegacion();
      SupernodoNegacion sn = (SupernodoNegacion)ArbolFactory.getInstance().getSupernodo(treeClassName);
      Nodo n = new Nodo(modelo, sn, null);
      sn.setPadre(n);
      return n;
   }

   @Deprecated
   @Override
   protected IAsociacionTemporal crearModelo(String[] modArray, List<Patron> patrones, GeneradorPatrones genp) throws FactoryInstantiationException{
      return super.crearModelo(modArray, patrones, genp);
   }

   //@Override
   protected IAsociacionConNegacion crearModelo(String[] positivos, String[] negados,
         List<Patron> patrones, GeneradorPatrones genp) throws FactoryInstantiationException{
      return AssociationFactory.getInstance().getAssociationInstance(associationClassName,
            positivos, negados, windowSize, patrones, supermodelo, numHilos);
   }

   @Override
   @Deprecated
   protected void notificarModeloGenerado(int tam, int pSize,
         IAsociacionTemporal modelo, String[] mod, List<IAsociacionTemporal> candidatas,
         Map<String,List<IAsociacionTemporal>> nuevoMapa){
      super.notificarModeloGenerado(tam, pSize, modelo, mod, candidatas, nuevoMapa);
   }

   /**
    * Para modelos de tamaño 1
    * @param modelo
    * @param tipoEvento
    * @param candidatas
    * @param nuevoMapa
    */
   protected void notificarModeloGenerado(
         IAsociacionConNegacion modelo, String tipoEvento,
         List<IAsociacionTemporal> candidatas,
         Map<String, List<IAsociacionTemporal>> nuevoMapa){
      List<IAsociacionTemporal> maux = new ArrayList<IAsociacionTemporal>();
      candidatas.add(modelo);
      //candidatasGeneradas.add(modelo);
      maux.add(modelo);
      nuevoMapa.put(tipoEvento, maux);
      //mapaGeneradas.put(tipoEvento, maux);
   }



   @Override
   protected void resetMapas(List<String> tipos){
      //mapa.clear();

      // Inicializar mapa
      /*for(String tipo : tipos){
         mapa.put(tipo, new ArrayList<IAsociacionTemporal>());
      }*/
      for(String tipo : tipos){
         mapa.get(tipo).clear();
         mapaNegados.get(tipo).clear();
      }
   }

   @Override
   protected void calcularSoporte(List<IAsociacionTemporal> candidatas, IColeccion coleccion){
      //int tam = candidatas.get(0).size();
      SuperModeloNegacion superModNeg = (SuperModeloNegacion)supermodelo;

      int sid = 0;
      //int totalVentanas = 0;
      for(ISecuencia secuencia : coleccion){
         superModNeg.setSecuencia(secuencia);

         while(superModNeg.nextWindow()){
            //totalVentanas++;
            for(Evento evento: superModNeg.getN()){
               List<IAsociacionTemporal> receptores = mapa.get(evento.getTipo());
               for(IAsociacionTemporal receptor : receptores){
                  receptor.recibeEvento(sid, evento, savePatternInstances);
               }
            }
            for(Evento evento: superModNeg.getD()){
               List<IAsociacionConNegacion> receptores = mapaNegados.get(evento.getTipo());
               for(IAsociacionConNegacion receptor : receptores){
                  //receptor.recibeEvento(sid, evento, savePatternInstances);
                  receptor.saleEventoNegado(sid, evento, savePatternInstances);
               }
            }
         }
         //LOGGER.info("Total ventanas ( al final de la secuencia #" + sid + "): " + totalVentanas);
         sid++;
      }

   }

   //@Override
   protected void notificarModeloGenerado(int tam, int pSize,
         IAsociacionConNegacion modelo, String[] mod, String[] negados,
         List<IAsociacionTemporal> candidatas,
         Map<String, List<IAsociacionTemporal>> nuevoMapa, boolean buscar){
      asociacionesNivel[tam-1]++;
      patronesGeneradosNivel[tam-1] += pSize;

      if(buscar){
         candidatas.add(modelo);
         for(String tipo: mod){
            nuevoMapa.get(tipo).add(modelo);
         }

         for(String tipo: negados){
            mapaNegados.get(tipo).add(modelo); //TODO inicializar mapaNegados
         }
      }
   }

   @Override
   public void escribirEstadisticasEstrategia(
         List<List<IAsociacionTemporal>> resultados, Writer fwp,
         boolean shortVersion, int maxIteracion) throws IOException {
// TODO

      fwp.write(SEPARADOR);

      if(verbose){
         fwp.write("ATENCIÓN! Modo verbose activo\n\n");
      }


      int descNoGenerados = 0, descGenerados = 0, secuenciasGeneradas=0, patronesGenerados=0,
            secuenciasFrecuentes=0, patronesFrecuentes=0,
            patronesSinRestricciones=0, asociacionesNegacion=0, patronesConNegacion=0;

      int rSize = resultados.size();

      //for(int i=0;i<resultados.size();i++){
      for(int i=0; i<=maxIteracion; i++){
         descNoGenerados += patronesNoGeneradosNivel[i];
         descGenerados += patronesDescartadosNivel[i];
         secuenciasGeneradas += asociacionesNivel[i];
         patronesGenerados += patronesGeneradosNivel[i];
         patronesSinRestricciones += patronesSinRestriccionesNivel[i];
         patronesConNegacion += patronesConNegacionNivel[i];
         asociacionesNegacion += asociacionesNegacionNivel[i];
         secuenciasFrecuentes += i<rSize? resultados.get(i).size() : 0;

         //Las asociaciones para todos los niveles
         fwp.write(nivel(i) + "asociaciones generadas: "+ NF.format(asociacionesNivel[i])
               + " vs. asociaciones frecuentes: " + (i<rSize? NF.format(resultados.get(i).size()) : 0) + "\n");
         fwp.write(nivel(i) + "asociaciones con negacion: "+ NF.format(asociacionesNegacionNivel[i]) + "\n");

         //En niveles 2 o superior hay patrones
         if(i>0){
            /*int patronesFrecuentesNivel=0;
            for(int j=0; i<rSize && j<resultados.get(i).size(); j++){
               patronesFrecuentesNivel += resultados.get(i).get(j).getPatrones().size();
            }*/
            patronesFrecuentes += patronesFrecuentesNivel[i];
            if(i>1){
               fwp.write(nivel(i) + "posibles patrones: " + NF.format(patronesPosiblesNivel[i])
                     +" -> patrones generados: " + NF.format(patronesGeneradosNivel[i])
                     + ", patrones resultado: " + NF.format(patronesFrecuentesNivel[i]) + "\n");
               fwp.write(nivel(i) + "patrones con parte negativa: " + NF.format(patronesConNegacionNivel[i]) +
                     ", de los que sólo tienen un evento positivo: " + NF.format(patronesSinRestriccionesNivel[i]) + "\n");
               fwp.write(nivel(i) + "patrones inconsistentes en generación: " + NF.format(patronesDescartadosNivel[i]) + "\n");
               fwp.write(nivel(i) + "patrones descartados sin generar: " + NF.format(patronesNoGeneradosNivel[i]) + "\n");
            }else{
               fwp.write(nivel(i) + "patrones frecuentes: " + NF.format(patronesFrecuentesNivel[i]) + "\n");
            }
         }
         fwp.write("\n");
      }

      fwp.write("\nTotal asociaciones temporales -> generadas: " + NF.format(secuenciasGeneradas)
            + ", frecuentes: " + NF.format(secuenciasFrecuentes) + ", con negacion:"  + NF.format(asociacionesNegacion) + "\n" );
      fwp.write("Total patrones -> generados: " + NF.format(patronesGenerados)
            + ", resultado: " + NF.format(patronesFrecuentes)
            + ",\n\tdescartados en generación: " + NF.format(descGenerados)
            + ", imposibles/descartados sin generar: " + NF.format(descNoGenerados)
            + ",\n\tpatrones con negacion: " + NF.format(patronesConNegacion)
            + ", patrones con un solo tipo positivo: " + NF.format(patronesSinRestricciones) + "\n");

      fwp.write(SEPARADOR);

      fwp.flush();


   }

}
