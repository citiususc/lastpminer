package source.busqueda.semilla;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import source.busqueda.AbstractMine;
import source.busqueda.GeneradorPatrones;
import source.busqueda.GeneradorPatronesSemilla;
import source.busqueda.IBusquedaConSemilla;
import source.busqueda.IEliminaEventos;
import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.excepciones.AlgoritmoException;
import source.excepciones.FactoryInstantiationException;
import source.excepciones.ModelosBaseNoValidosException;
import source.excepciones.ModelosBaseVaciosException;
import source.excepciones.SemillasNoValidasException;
import source.modelo.AssociationFactory;
import source.modelo.ComparadorAsociaciones;
import source.modelo.IAsociacionSemilla;
import source.modelo.IAsociacionTemporal;
import source.modelo.IMarcasIntervalos;
import source.modelo.Modelo;
import source.modelo.clustering.IClustering;
import source.modelo.semilla.ModeloSemilla;
import source.patron.Patron;

/**
 * Implementación de ASPTminer cuando se utilizan patrones semilla.
 *
 * Cambia la generación de candidatos para basarse en la extensión de los
 * patrones semilla.
 *
 * @author vanesa.graino
 *
 */
public class SemillaConjuncion extends AbstractMine implements IEliminaEventos, IBusquedaConSemilla{
   private static final Logger LOGGER = Logger.getLogger(SemillaConjuncion.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }


   //TODO explicar para qué es cada mapa
   protected Map<String,List<IAsociacionTemporal>> mapa;
   protected Map<String,List<IAsociacionTemporal>> mapaPares; //
   private Map<String,List<IAsociacionTemporal>> nuevoMapa;
   protected boolean soloExtensionesSemilla = false;

   {
      associationClassName = "ModeloMarcasIntervalos";
      patternClassName = "Patron";
   }

   /*
    * Constructors
    */

   public SemillaConjuncion(String executionId, boolean savePatternInstances,
         boolean saveRemovedEvents, IClustering clustering, boolean removePatterns){
      super(executionId, savePatternInstances, saveRemovedEvents, clustering, removePatterns);
   }

   /*
    * Métodos
    */


   // Sacada de MineIntervalMarks
   /**
    * Calcula el soporte de los patrones/modelos semilla y los
    * ModeloEvento con tipos de evento que no están en la semillas y
    * borra los trozos de la secuencia donde no puede encontrarse una
    * extensión de un patrón semilla.
    * @param coleccion
    */
   protected void calcularSoporteSemilla(IColeccion coleccion){
      if(coleccion.isEmpty()){ return; }
      int sSize;
      Evento ev; // Evento leído
      Evento bv; // Evento de principio de ventana
      int sid = 0;
      int borrados=0, restantes=0, eliminados=0;
      ListIterator<Evento> inicioVentana, finalVentana;
      int[] ultimaOcurrencia, intervaloActual = {0,0};
      List<int[]> intervalosActivos;
      int tmp;

      for(ISecuencia secuencia : coleccion){
         if(secuencia.isEmpty()){ continue; }
         inicioVentana = secuencia.listIterator();
         bv = inicioVentana.next(); // Evento de principio de ventana
         sSize = secuencia.size();
         eliminados = 0;
         ISecuencia copia = secuencia.clone();
         finalVentana = copia.listIterator();
         intervalosActivos = new ArrayList<int[]>();
         while(finalVentana.hasNext()){
            ev = finalVentana.next();
            tmp = ev.getInstante();
            List<IAsociacionTemporal> receptores = mapa.get(ev.getTipo());
            boolean hayOcurrencia = false;
            intervaloActual[0] = tmp;
            intervaloActual[1] = tmp;
            if(receptores != null){
               for(IAsociacionTemporal receptor : receptores){
                  // Calcular el intervalo de eventos que incluyen ocurrencias
                  int antes = receptor.getSoporte();
                  receptor.recibeEvento(sid, ev, savePatternInstances);
                  int despues = receptor.getSoporte();
                  if(antes != despues && receptor instanceof IMarcasIntervalos){ // Se encontró alguna ocurrencia
                     IMarcasIntervalos modelo = (IMarcasIntervalos) receptor;
                     hayOcurrencia = true;
                     ultimaOcurrencia = modelo.getUltimaEncontrada();
                     if(intervaloActual[0] > ultimaOcurrencia[0]){
                        intervaloActual[0] = ultimaOcurrencia[0];
                     }
                  }
               }
            }
            int inicioW = ev.getInstante() - windowSize - 1;
            if(hayOcurrencia){
               intervaloActual = insertarIntervalo(intervalosActivos, intervaloActual);
            }
            // Avanzar el comienzo de la ventana, y eliminar aquellos eventos que
            // no están en un intervalo de eventos utilizados
            while(bv.getInstante() <= inicioW){
               // Comprobar si 'bv' pertenece a algún intervalo activo
               boolean estaActivo = estaActivo(intervalosActivos, inicioW, bv.getInstante());

               if(!estaActivo){
                  // No pertenece a ningún intervalo activo, eliminar
                  notificarEventoEliminado(bv, sid, 0);
                  inicioVentana.remove();
                  sSize--;
                  eliminados++;
               }
               bv = inicioVentana.next();
            }

            // Comprobar si algún intervalo activo salió de la ventana
            borrarIntervalosAnteriores(intervalosActivos, bv.getInstante());
         }
         sid++;

         borrados += eliminados;
         restantes += sSize;
      }
      imprimirEliminados(LOGGER, borrados, restantes);

   }

   protected int[] insertarIntervalo(List<int[]> intervalosActivos, int[] intervaloActual){

      //TODO descomentar esto para que no se borren instancias indebidamente
      // Ampliamos el intervalo con el tamaño de la ventana
//      int swap = intervaloActual[1];
//      intervaloActual[1] = intervaloActual[0] + windowSize;
//      intervaloActual[0] = swap - windowSize;

      if(!intervalosActivos.isEmpty() && intervaloActual[0] <= intervalosActivos.get(intervalosActivos.size()-1)[1]){
         int[] ultimo = intervalosActivos.get(intervalosActivos.size()-1);
         // Se solapan
         ultimo[1] = intervaloActual[1];
         if(intervaloActual[0] < ultimo[0]){
            //El nuevo contiene al anterior
            ultimo[0] = intervaloActual[0];

            int[] anterior = intervalosActivos.size()<2 ? null : intervalosActivos.get(intervalosActivos.size()-2);
            // Si extendemos hacia atrás el intervalo puede que se solape con el anterior
            while(anterior != null && ultimo[0]<=anterior[1]){
               anterior[1] = ultimo[1];
               if(anterior[0] > ultimo[0]){
                  anterior[0] = ultimo[0];
               }
               intervalosActivos.remove(intervalosActivos.size()-1);
               ultimo = anterior;
               anterior = intervalosActivos.size()<2 ? null : intervalosActivos.get(intervalosActivos.size()-2);
            }
         }
         return intervaloActual;
      }
      intervalosActivos.add(intervaloActual);
      return new int[2];

   }

   protected boolean estaActivo(List<int[]> intervalosActivos, int inicioW, int instante ){
      boolean estaActivo = false;
      //Mientras haya intervalso y el evento no se anterior al inicio del primer intervalo
      for(int i=0; i<intervalosActivos.size() && intervalosActivos.get(i)[0] <= instante ; i++){
         int[] intervalo = intervalosActivos.get(i);
         if(instante >= intervalo[0] && instante <= intervalo[1]){
            estaActivo = true;
            break;
         }
      }
      return estaActivo;
   }

   protected void borrarIntervalosAnteriores(List<int[]> intervalosActivos, int inicioW){
      Iterator<int[]> iteradorIntervalosActivos = intervalosActivos.iterator();
      //int inicioW = bv.getInstante();
      while(iteradorIntervalosActivos.hasNext()){
         int[] intervalo = iteradorIntervalosActivos.next();
         if(intervalo[1] < inicioW){
            iteradorIntervalosActivos.remove();
         }else{
            //Cuando llegamos a uno que está en la ventana ya no hay que seguir
            break;
         }
      }
   }

   /**
    * Entrada: lista de modelos de esta iteracion, secuencia/registro de entrada
    * Precondición: secuencia está ordenada temporalmente de forma creciente
    * Poscondición: candidatas está actualizada con todas las instancias encontradas
    * Salida: ninguna explícita, lista candidatas actualizada
    * @param candidatas
    * @param coleccion
    */
   protected void calcularSoporte(List<IAsociacionTemporal> candidatas, IColeccion coleccion){
      int sid = 0;
      for(ISecuencia secuencia : coleccion){
         for(Evento ev : secuencia){
            List<IAsociacionTemporal> receptores = mapa.get(ev.getTipo());
            if(receptores != null){
               for(IAsociacionTemporal receptor : receptores){
                  receptor.recibeEvento(sid, ev, savePatternInstances);
               }
            }
         }
         sid++;
      }
   }

   /**
    *
    * @param candidatas
    * @param supmin
    * @param tamActual
    */
   protected void purgarCandidatas(List<IAsociacionTemporal> candidatas, /*List<IAsociacionTemporal> listaSemillas, */int supmin, int tamActual){
      registroT.tiempoPurgar(tamActual-1, true);
      for(int i=candidatas.size()-1; i>=0; i--){
         IAsociacionTemporal modelo = candidatas.get(i);
         if(modelo.necesitaPurga(supmin)){
            candidatas.remove(i);
            //listaSemillas.remove(modelo);
            for(String tipo : modelo.getTipos()){
               mapa.get(tipo).remove(modelo);
            }
         }else{
            patronesFrecuentesNivel[tamActual-1] += modelo.getPatrones().size();
         }
      }
      registroT.tiempoPurgar(tamActual-1, false);
   }

   // Problema: NO COMPRUEBA TIPOS REPETIDOS AL METER LOS NUEVOS MODELOS EN nuevoMapa
   // Precondición: es llamado DESPUÉS de extenderSemilla, ya que las extensiones
   //  evitan que se generen más patrones sobre esas asociaciones temporales
   // Poscondición: actualiza 'mapa' con 'nuevoMapa'

   private List<String> combinar(List<String> tipos, int tamAnt, IAsociacionTemporal mod, List<String> tiposBase){
      List<String> tiposNuevo = new ArrayList<String>(tiposBase);
      String[] tipos2 = mod.getTipos();
      int k,l;
      for(k=0;k<tamAnt;k++){
         String tipo = tipos2[k];
         if(!tiposBase.contains(tipo)){
            int indexN = tipos.indexOf(tipo);
            // Añadir en posición adecuada
            for(l=0;l<tamAnt;l++){
               int index = tipos.indexOf(tiposBase.get(l));
               if(index>indexN){
                  tiposNuevo.add(l,tipo);
                  break;
               }
            }
            if(l==tamAnt){ tiposNuevo.add(tipo); }
            break;
         }
      }
      if(k==tamAnt){
         tiposNuevo.add(tipos2[k]);
      }
      return tiposNuevo;
   }

   /**
    * Tienen en común todos los tipos menos 2
    * @param mod1
    * @param mod2
    * @return
    */
   protected boolean combinables(String[] mod1, String[] mod2){
      int nTipos = mod1.length;
      if(nTipos!=mod2.length){
         return false;
      }

      int i;
      int difs=0;

      String[] tipos1 = mod1;
      for(i=0;i<nTipos;i++){
         if(Arrays.binarySearch(mod2, tipos1[i]) < 0){
            difs++;
            if(difs>1){
               return false;
            }
         }
      }

      return true;
   }

   private int fijarPadres(int tamAnt, GeneradorPatrones genp, List<String> tiposNuevo, IAsociacionTemporal base){
      int k, index=1;
      for(k=tamAnt-1; k>=0; k--){
         List<String> aux = new ArrayList<String>(tiposNuevo);
         aux.remove(k);
         if(aux.equals(base.getTipos())){
            continue;
         }
         List<IAsociacionTemporal> lista = mapa.get(aux.get(0));
         for(IAsociacionTemporal asoc : lista){
            if(Arrays.asList(asoc.getTipos()).equals(aux)){
               genp.setPadre(asoc, index);
               index++;
               break;
            }
         }
      }
      return index;
   }

   /**
    * Comprueba si una nueva asociacion temporal ya había sido incluida.
    * @param nueva - asociacion temporal que se quiere incluir
    * @param creadas - las asociaciones temporales que han sido incluidas
    * @return booleano que indica si existe una asociación/modelo que incluya todos los
    * tipos de eventos de <nueva> en <creadas>
    */
   protected boolean yaIncluida(List<String> nueva, List<IAsociacionTemporal> creadas){
      for(IAsociacionTemporal creada : creadas){
         if(Arrays.asList(creada.getTipos()).equals(nueva)){
            return true;
         }
      }
      return false;
   }
   protected boolean yaIncluida(String[] nueva, List<IAsociacionTemporal> creadas){
      return Collections.binarySearch(creadas, new Modelo(nueva, windowSize, null),
            new ComparadorAsociaciones())>=0;
   }

   protected int binarySearch(IAsociacionTemporal modelo, List<IAsociacionTemporal> creadas){
      return Collections.binarySearch(creadas, modelo,
            new ComparadorAsociaciones());
   }

   // PROBLEMA: NO COMPROBADO PARA TIPOS REPETIDOS
   /** Precondición: Debe ser llamado ANTES de generarCandidatas, crea <nuevoMapa>
    *  si no se hiciese, 'mapa' acabaría incompleto
    *  Salida: cada extensión de una semilla es el resultado de añadirle un tipo de
    *  evento nuevo a la semilla.
    * @throws FactoryInstantiationException
    */
   protected List<IAsociacionTemporal> extenderSemillas(List<IAsociacionTemporal> semillas,
            List<String> tipos) throws FactoryInstantiationException{
      List<IAsociacionTemporal> extensiones = new ArrayList<IAsociacionTemporal>();
      nuevoMapa = construyeMapa(tipos.size(), tipos);
      if(semillas.isEmpty()){
         return extensiones;
      }

      final int tamAnt = semillas.get(0).size();
      final int tam = tamAnt+1;
      int k;
      GeneradorPatronesSemilla genp = new GeneradorPatronesSemilla(tam, this);

      for(IAsociacionTemporal base: semillas){
         List<String> tiposBase = Arrays.asList(base.getTipos());
         for(String tipo: tipos){

            k = Collections.binarySearch(tiposBase, tipo);
            if(k>=0){
               //Si contiene el tipo pasamos al siguiente
               continue;
            }
            List<String> mod = new ArrayList<String>(tiposBase);
            mod.add(-k-1, tipo);

            //Comprobar si esta asociación ya se ha creado antes
            List<IAsociacionTemporal> lista = nuevoMapa.get(tipo);

            String[] modArray = mod.toArray(new String[mod.size()]);
            if(yaIncluida(modArray, lista)){
               continue;
            }

            genp.setPadre(base, 0);
            if(!genp.comprobarSubasociacionesSemilla(tipo, mod, mapaPares)){
               continue;
            }

            List<Patron> patrones = genp.generarPatrones(modArray);
            if(!patrones.isEmpty()){
               IAsociacionTemporal modelo = AssociationFactory.getInstance().getAssociationInstance("Modelo", //associationClassName, //TODO solucion chapuza!!
                     modArray, windowSize, patrones, numHilos);
               notificarModeloGenerado(tam, patrones.size(), modelo, mod, extensiones, nuevoMapa);
            }
         }
      }
      return extensiones;
   }

//   @Deprecated
//   protected void notificarModeloGenerado(int tam, int pSize,
//         IAsociacionTemporal modelo, String[] mod,
//         List<IAsociacionTemporal> candidatas,
//         Map<String,List<IAsociacionTemporal>> nuevoMapa){
//      notificarModeloGenerado(tam, pSize, modelo, Arrays.asList(mod), candidatas,
//            nuevoMapa);
//   }
   protected void notificarModeloGenerado(int tam, int pSize,
         IAsociacionTemporal modelo, String[] mod,
         List<IAsociacionTemporal> candidatas, int puntoInsercion,
         Map<String,List<IAsociacionTemporal>> nuevoMapa){
      notificarModeloGenerado(tam, pSize, modelo, Arrays.asList(mod), candidatas, puntoInsercion,
            nuevoMapa);
   }

//   @Deprecated
//   protected void notificarModeloGenerado(int tam, int pSize,
//         IAsociacionTemporal modelo, Iterable<String> mod,
//         List<IAsociacionTemporal> candidatas,
//         Map<String,List<IAsociacionTemporal>> nuevoMapa){
//      super.notificarModeloGenerado(tam, pSize, modelo, mod, candidatas, nuevoMapa);
//   }
   /**
    * Notifica la creación de un modelo candidato generado.
    * @param tam - Tamaño del modelo
    * @param pSize - Número de patrones
    * @param modelo - El propio modelo
    * @param mod - Tipos de eventos del modelo
    * @param candidatas - Lista de candidatas en la que se inserta el nuevo modelo
    * @param puntoInsercion  - Índice donde se inserta el candidato en la lista de candidatas.
    * @param nuevoMapa - Mapa de candidatas en el que se inserta el nuevo modelo
    */
   protected void notificarModeloGenerado(int tam, int pSize,
         IAsociacionTemporal modelo, Iterable<String> mod,
         List<IAsociacionTemporal> candidatas, int puntoInsercion,
         Map<String,List<IAsociacionTemporal>> nuevoMapa){
      asociacionesNivel[tam-1]++;
      patronesGeneradosNivel[tam-1]+= pSize;

      candidatas.add(modelo);
      for(String tipo : mod){
         nuevoMapa.get(tipo).add(modelo);
      }
   }

   /**
    * Se crean los modelos de tamaño 2 que no han sido creados a partir de
    * las semillas en base a los modelos de tamaño 1 frecuentes.
    * No hay modelos repetidos, es decir, dos modelos diferentes con los
    * mismos tipos de eventos.
    * Los nuevos modelos se añaden a {@code mapa} y a la lista que se devuelve.
    * @param anteriores - Los modelos de tamaño 1 frecuentes.
    * @param tipos - Los tipos de eventos de la colección que son frecuentes.
    * @param semillas - Los modelos que se extrajeron de las semillas.
    * @return Lista con los modelos que han sido generados por el método.
    * @throws FactoryInstantiationException
    */
   protected List<IAsociacionTemporal> generarCandidatasTam2(List<IAsociacionTemporal> anteriores,
         List<String> tipos, List<IAsociacionTemporal> semillas) throws FactoryInstantiationException{
      List<IAsociacionTemporal> candidatas = new ArrayList<IAsociacionTemporal>();
      final int tam = 2, aSize = anteriores.size();
      int i, j;
      IAsociacionTemporal modelo,madre, padre;
      String[] modArray;

      mapa = construyeMapa(tipos.size(), tipos);
      for(i=0; i<aSize-1; i++){
         //madre = (IAsociacionEvento)anteriores.get(i);
         madre = anteriores.get(i);
         for(j=i+1; j<aSize; j++){
            padre = anteriores.get(j);
            modArray = new String[]{ madre.getTipos()[0], padre.getTipos()[0] };
            if(yaIncluida(modArray, semillas)){
               //Se creó con las semillas
               continue;
            }
            modelo = crearModelo(modArray);
            notificarModeloGenerado(tam, 0, modelo, modArray, candidatas, mapa);
         }
      }
      return candidatas;
   }

   protected IAsociacionTemporal crearModelo(String[] modArray) throws FactoryInstantiationException{
      return AssociationFactory.getInstance().getAssociationInstance(associationClassName,
            modArray, windowSize, getClustering(), numHilos);
   }

   /**
    *
    * @param tam
    * @param anteriores
    * @param tipos
    * @return
    * @throws FactoryInstantiationException
    */
   protected void generarCandidatas(final int tam, List<IAsociacionTemporal> anteriores,
            List<String> tipos, List<IAsociacionTemporal> actual) throws FactoryInstantiationException{
      //List<IAsociacionTemporal> nuevos = new ArrayList<IAsociacionTemporal>();
      if(anteriores.isEmpty()) {
         mapa = nuevoMapa;
         return;//return nuevos;
      }
      final int aSize = anteriores.size();
      //final int tamAnt = anteriores.get(0).size();
      //int tam = tamAnt+1;
      int i,j,k;

      GeneradorPatrones genp = new GeneradorPatrones(tam, this);

      for(i=0; i<aSize-1; i++){
         IAsociacionTemporal base = anteriores.get(i);
         List<String> tiposBase = Arrays.asList(base.getTipos());
         for(j=i+1;j<aSize;j++){
            registroT.tiempoAsociaciones(tam-1, true);
            IAsociacionTemporal mod = anteriores.get(j);
            //if(tiposDistintos(base.getTipos(),mod.getTipos()) != 1){ break; }

            //TODO juntar combinales y combinar en un solo método
            if(!combinables(base.getTipos(), mod.getTipos())){ break; }
            List<String> tiposNuevo = combinar(tipos, tam-1, mod, tiposBase);

            List<IAsociacionTemporal> yaCreados = nuevoMapa.get(tiposNuevo.get(0));
            if(yaIncluida(tiposNuevo, yaCreados)){
               continue;
            }
            // Comprobar si las subasociaciones son frecuentes
            genp.setPadre(base,0);

            int index = fijarPadres(tam-1, genp, tiposNuevo, base);


            registroT.tiempoAsociaciones(tam-1, false);

            if(index==1){ continue; }
            if(index==2){
               // No hay suficientes subpatrones para continuar
               // Coger los patrones de tamaño 2 que faltan
               List<String> aux0 = genp.disjointPadreMadre();
               List<IAsociacionTemporal> modelos = mapaPares.get(aux0.get(0));
               for(k=0;k<modelos.size();k++){
                  IAsociacionTemporal modelo = modelos.get(k);
                  if(Arrays.asList(modelo.getTipos()).containsAll(aux0)){
                     genp.setPadre(modelo, 2);
                     index++;
                     break;
                  }
               }
            }
            // TODO añadido para controlar situación en la que no hay suficientes padrse
            if(index<tam){
               //No hay suficientes padres
               continue;
            }
            String[] modArray = tiposNuevo.toArray(new String[tiposNuevo.size()]);
            List<Patron> patrones = genp.generarPatrones(modArray);

            // Crear y añadir la asociación temporal a 'nuevos'
            registroT.tiempoModelo(tam-1, true);
            if(!patrones.isEmpty()){
               //Modelo modelo = new ModeloMarcasIntervalos(tiposNuevo,win,patrones);
               IAsociacionTemporal modelo = AssociationFactory.getInstance().getAssociationInstance(associationClassName,
                     modArray, windowSize, patrones, numHilos);

               //TODO insertar ordenada
               int ptoIns = binarySearch(modelo, actual);
               notificarModeloGenerado(tam, patrones.size(), modelo, modArray, actual, ptoIns, nuevoMapa);
            }
            registroT.tiempoModelo(tam-1, false);
         }
      }
      mapa = nuevoMapa;
      //return nuevos;
   }

   protected List<String> purgarTiposYEventos(IColeccion coleccion, List<IAsociacionTemporal> anteriores,
         List<String> tipos){
      if(anteriores.size() != tipos.size()){
         //TODO ¿hay más estructuras que necesitan una actualización si se borra un tipo de evento?
         List<String> tp = new ArrayList<String>(anteriores.size());
         for(String tipo : tipos){
            if(!anteriores.contains(tipo)){
               //mapa.remove(tipo); // se va a borrar a continuación no es necesario actualizarlo
               mapaPares.remove(tipo); //
            }else{
               tp.add(tipo);
            }
         }
         tipos = tp;
         purgarEventosDeTiposNoFrecuentes(coleccion, tipos);
      }
//      if(anteriores.size() != tipos.size()){
//         tipos = new ArrayList<String>();
//         mapa = new HashMap<String,List<IAsociacionTemporal>>(anteriores.size());
//         for(int i=0;i<anteriores.size();i++){
//            String tp = anteriores.get(i).getTipos()[0];
//            tipos.add(tp);
//            mapa.put(tp, new ArrayList<IAsociacionTemporal>());
//         }
//         purgarEventosDeTiposNoFrecuentes(coleccion, tipos);
//      }
      return tipos;
   }

   /**
    * Procesa la semilla para extraer las distribuciones temporales de pares de eventos
    * del modelo de semilla y las añade a la lista {@code pares} y a {@code mapaPares}.
    * Cuando un ModeloSemilla llega a aquí ya ha pasado por {@link ModeloSemilla#calculaPatrones}
    * por lo que sólo tiene patrones de tamaño 2. Estos patrones están ordenados por
    * sus tipos de eventos y puede haber más de un patrón con los mismos tipos, cuando
    * esto sucede deben meterse en el mismo modelo de tamaño 2.
    * @param semilla - la asociacion temporal. Debe ser semilla
    * @param pares - lista de modelos ya inicializada
    * @throws FactoryInstantiationException
    */
   protected void procesarSemilla(IAsociacionSemilla semilla,
         List<IAsociacionTemporal> pares) throws FactoryInstantiationException{

      List<Patron> patrones = semilla.getPatrones();
      if(patrones.isEmpty()){ return; }


      int[][] distribucion = semilla.getDistribuciones();
      int[] nuevaDist;
      int j, indexDist = 0;
      int pSize = patrones.size();
      IAsociacionTemporal modPar;
      List<Patron> patronesPar = new ArrayList<Patron>();
      List<String> par = Arrays.asList(patrones.get(0).getTipos());
      patronesPar.add(patrones.get(0));
      for(j=1; j<pSize; j++){
         Patron p = patrones.get(j);
         List<String> par2 = Arrays.asList(p.getTipos());
         if(!par.containsAll(par2)){
            // Par de tipos distinto, crear nuevo Modelo
            nuevaDist = distribucion[indexDist].clone();
            indexDist++;
            modPar = crearModelo(new String[]{ par.get(0), par.get(1) }, patronesPar, nuevaDist);
            notificarModeloGenerado(2, patronesPar.size(), modPar, modPar.getTipos(), pares, mapaPares);
            patronesPar = new ArrayList<Patron>();
            par = par2;
         }
         patronesPar.add(p);
      }
      //patsPar.add(patrones.get(j-1)); //TODO hay situaciones en las que repite el patrón semilla
      nuevaDist = distribucion[indexDist].clone();
      modPar = crearModelo(new String[]{ par.get(0), par.get(1) }, patronesPar, nuevaDist);
      notificarModeloGenerado(2, patronesPar.size(), modPar, modPar.getTipos(), pares, mapaPares);
   }

   protected IAsociacionTemporal crearModelo(String[] mod, List<Patron> patrones,
         int[] distribucion) throws FactoryInstantiationException{
      return AssociationFactory.getInstance().getAssociationInstance("Modelo",
          mod, windowSize, patrones, distribucion, getClustering(), numHilos);
   }

   /**
    * Se crean los mapas de asociaciones, tanto mapa como mapaPares, y la lista semNivel.
    * Se crea una asociación temporal para cada tipo de evento que no está en el patrón
    * semilla y se añade a candidatos y a mapa. Se añaden los patrones semilla a candidatos
    * y a mapa.
    * @param tipos
    * @param candidatos
    * @param win
    * @param tiposSemilla
    * @param semillas
    * @param semNivel
    * @param cSize
    * @throws AlgoritmoException
    * @throws FactoryInstantiationException
    */
   protected void inicializaEstructuras(List<String> tipos, List<IAsociacionTemporal> candidatos, int win,
         String[] tiposSemilla, List<ModeloSemilla> semillas, List<List<IAsociacionTemporal>> semNivel,
         int cSize) throws FactoryInstantiationException{
      int tSize = tipos.size();
      iniciarContadores(tSize, cSize);
      windowSize = win;
      // Inicializar las estructuras auxiliares
      // Tipos de evento que no pertenecen a la semilla son los
      //primeros patrones candidatos
      mapa = new HashMap<String,List<IAsociacionTemporal>>(tSize);
      mapaPares = new HashMap<String,List<IAsociacionTemporal>>(tipos.size());

      for(String tipo: tipos){
         semNivel.add(new ArrayList<IAsociacionTemporal>());
         mapa.put(tipo, new ArrayList<IAsociacionTemporal>());
         mapaPares.put(tipo, new ArrayList<IAsociacionTemporal>());
         // Añadir los tipso de evento que no están en la semilla como candidatos
         //if(!tiposSemilla.contains(tipo)){
         if(Arrays.binarySearch(tiposSemilla, tipo)<0){
            //List<String> aux = new ArrayList<String>();
            //aux.add(tipo);
            //Modelo mod = new ModeloSemilla(aux,win, isSavePatternInstances(), getClustering());
//            IAsociacionTemporal mod = AssociationFactory.getInstance().getSeedAssociationInstance(/*"ModeloSemilla",*/
//                  new String[]{tipo}, win, getClustering(), numHilos);
            IAsociacionTemporal mod = AssociationFactory.getInstance().getAssociationInstance("ModeloEvento",
                  tipo, numHilos);
            candidatos.add(mod);
            mapa.get(tipo).add(mod);
         }
      }

      // Añadir las semillas como patrones candidatos
      for(int i=0;i<semillas.size();i++){
         //Modelo semilla = semillas.get(i);
         IAsociacionTemporal semilla = AssociationFactory.getInstance().getSeedAssociationInstance(/*"ModeloSemilla",*/ semillas.get(i), numHilos);
         candidatos.add(semilla);
         for(String tipo: semilla.getTipos()){
            mapa.get(tipo).add(semilla);
         }
      }
   }

   @Override
   public List<IAsociacionTemporal> calcularDistribuciones(List<String> tipos, IColeccion coleccion,
         List<ModeloSemilla> semillas, int supmin, int win) throws SemillasNoValidasException, AlgoritmoException{
      List<List<IAsociacionTemporal>> all =  buscarModelosFrecuentes(tipos, coleccion, semillas, supmin, win, true);
      if (all != null && all.size() > 1) {
         return all.get(1);
      }
      return Collections.emptyList();
   }

   // Precondición: Sólo puede haber 1 semilla
   @Override
   public List<List<IAsociacionTemporal>> buscarModelosFrecuentes(List<String> tipos, IColeccion coleccion,
         List<ModeloSemilla> semillas, int supmin, int win) throws SemillasNoValidasException, AlgoritmoException{
      return buscarModelosFrecuentes(tipos, coleccion, semillas, supmin, win, false);
   }

   protected void procesarSemillas(List<IAsociacionTemporal> candidatas, List<IAsociacionTemporal> pares,
         List<IAsociacionTemporal> anteriores, List<IAsociacionTemporal> semFrecuentes) throws FactoryInstantiationException{
      for(IAsociacionTemporal candidato : candidatas){
         if(candidato.size() == 1){
            anteriores.add(candidato);
         }else{
            // Es patrón semilla
            // Obtener todos los patrones de tamaño 2 derivados del patrón semilla
            // Estos patrones serán extendidos a tamaño 3 mediante la función
            // 'extenderSemillas' y a partir de ahí se procederá con el algoritmo básico.
            procesarSemilla((IAsociacionSemilla)candidato, pares);
            semFrecuentes.add(candidato);
            //semNivel.get(semilla.size()-1).add(semilla);
            //semRestantes++;
         }
      }
   }

   // Precondición: Sólo puede haber 1 semilla
   private List<List<IAsociacionTemporal>> buscarModelosFrecuentes(List<String> tipos, IColeccion coleccion,
         List<ModeloSemilla> semillas, int supmin, int win, boolean hastaNivel2) throws SemillasNoValidasException, AlgoritmoException{
      if(semillas.size() != 1){
         throw new SemillasNoValidasException("Sólo está preparado para tener un patrón semilla");
      }
      try{
         long tiempoAux, inicioTotal, inicioIteracion;
         String[] tiposSemilla;
         List<IAsociacionTemporal> candidatas, anteriores, semFrecuentes;
         List<List<IAsociacionTemporal>> todos = new ArrayList<List<IAsociacionTemporal>>();
         List<List<IAsociacionTemporal>> semNivel = new ArrayList<List<IAsociacionTemporal>>();

         inicioTotal = System.currentTimeMillis();
         inicioIteracion = inicioTotal;

         Runtime runtime = Runtime.getRuntime();

         candidatas = new ArrayList<IAsociacionTemporal>();
         tiposSemilla = semillas.get(0).getTipos();

         long inicioSemilla = System.currentTimeMillis();
         inicializaEstructuras(tipos, candidatas, win, tiposSemilla, semillas, semNivel, coleccion.size());

         // Calcular qué tipos de eventos y qué patrones semilla son frecuentes
         registroT.tiempoSoporte(0, true);
         calcularSoporteSemilla(coleccion);
         tiempoAux = registroT.tiempoSoporte(0, false);
         registrarUsoMemoria(runtime, 1);

         calculaPatrones(candidatas, supmin, 1);

         //System.out.println("SemNivel[0].size: " + semNivel.get(0).size());
         //purgarCandidatas(semNivel.get(0), supmin, 1);
         purgarCandidatas(candidatas, supmin, 1);

         //System.out.println("SemNivel[0].size: " + semNivel.get(0).size());

         // Añadir los tipos de evento del patrón semilla, por compatibilidad

         if(tiposSemilla.length>1){
            for(String tipo : tiposSemilla){
               IAsociacionTemporal mod = AssociationFactory.getInstance().getAssociationInstance("ModeloEvento",
                     tipo, numHilos);
               notificarModeloGenerado(1, 0, mod, Arrays.asList(tipo), candidatas, mapa);
//               //semNivel.get(0).add(mod); //añadido por @vanesa
//               candidatas.add(mod);
//               mapa.get(tipo).add(mod);
            }
         }
         Collections.sort(candidatas);

         // Separar los candidatos frecuentes en semillas frecuentes (semFrecuentes)
         // y tipos de evento frecuentes (anteriores)
         anteriores = new ArrayList<IAsociacionTemporal>();
         semFrecuentes = new ArrayList<IAsociacionTemporal>();
         List<IAsociacionTemporal> pares = new ArrayList<IAsociacionTemporal>();
         procesarSemillas(candidatas, pares, anteriores, semFrecuentes);

         //Se añaden a los resultados los patrones de tamaño 1 tanto los que vienen de la semilla como los que no
         todos.add(anteriores);

         //Se añaden a semNivel los patrones de tamaño 2 que vienen de las semillas
         semNivel.get(1).addAll(pares);

         // Actualizar la lista de tipos para que solo incluya los frecuentes
         registroT.tiempoPurgar(0, true);
         tipos = purgarTiposYEventos(coleccion, anteriores, tipos);
         registroT.tiempoPurgar(0, false);

         //memoriaConPurgaNivel[0] = runtime.totalMemory() - runtime.freeMemory();
         imprimirUsoMemoria(LOGGER, 0);

         tiempoAux = System.currentTimeMillis();
         registroT.tiempoIteracion(0, tiempoAux - inicioIteracion);
         inicioIteracion = tiempoAux;

         // Resto de tamaño 2

         // Obtener pares de tipos de eventos frecuentes (no semilla)
         // y con qué disposiciones temporales son frecuentes (clustering)
         registroT.tiempoCandidatas(1, true);
         candidatas = generarCandidatasTam2(anteriores, tipos, semNivel.get(1)); // no semilla
         registroT.tiempoCandidatas(1, false);

         //memoriaSinPurgaNivel[1] = runtime.totalMemory() - runtime.freeMemory();

         // Comprobar que los nuevos patrones generados fuera de la semilla son frecuentes
         if(!candidatas.isEmpty()){

            registroT.tiempoSoporte(1, true);
            calcularSoporte(candidatas, coleccion);
            registroT.tiempoSoporte(1, false);

            //Calculo patrones
            calculaPatrones(candidatas, supmin, 2);
            //Purga infrecuentes
            //System.out.println("SemNivel[1].size: " + semNivel.get(1).size());
            purgarCandidatas(candidatas, supmin, 2);
            //purgarCandidatas(semNivel.get(1), supmin, 2);
            //System.out.println("SemNivel[1].size: " + semNivel.get(1).size());
         }

         //memoriaConPurgaNivel[1] = runtime.totalMemory() - runtime.freeMemory();
         imprimirUsoMemoria(LOGGER, 1);

         // Añadir los patrones semilla a los resultados
         // Aquí se mezclan los patrones de tamaño 2 que vienen de las semillas  los que vienen de
         todos.add(candidatas);
         //candidatas = todos.get(1);
         int nivelSem = 1;
         if(semNivel.get(1).isEmpty()){
            //Se puede dar si los patrones semilla son de tamaño 1
            //throw new AlgoritmoException("Las semillas no han dado lugar a ningún patrón de tamaño 2 frecuente");
         }else{
            todos.get(1).addAll(semNivel.get(nivelSem));
            Collections.sort(candidatas); //sólo hay que ordenar si se mezclan
         }

         tiempoAux = System.currentTimeMillis();
         LOGGER.log(Level.INFO, "{0}. Tiempo para Inicialización: "
               + (tiempoAux-inicioSemilla), getExecutionId());
         registroT.tiempoIteracion(1, tiempoAux - inicioIteracion);
         inicioIteracion = tiempoAux;


         // Caso general
         // Empieza el procedimiento iterativo. Cada iteración i representa un tamaño i
         // de candidatos. El procedimiento continua mientras queden semillas que introducir
         // O extensiones de semillas que generar.
         // Solo interesan los patrones derivados de la semilla

         int tam = 3;

         if(!hastaNivel2){


            while(!candidatas.isEmpty()){

               candidatas = buscarModelosIteracion(tipos, coleccion, supmin, win, hastaNivel2,
                     candidatas, semNivel.get(nivelSem), runtime, tam);

               if(!candidatas.isEmpty()){
                  todos.add(candidatas);
               }

               tiempoAux = System.currentTimeMillis();
               registroT.tiempoIteracion(tam-1, tiempoAux - inicioIteracion);
               inicioIteracion = tiempoAux;
               //LOGGER.log(Level.INFO, "{0}. Tiempo para iteración " + tam + ": "
               //		+ registroT.getTiemposIteracion()[tam-1], getExecutionId());

               nivelSem++;
               tam++;

               if(tamMaximoPatron != -1 && tam>tamMaximoPatron){
                  break;
               }
            }
         }

         registroT.setTiempoTotal( System.currentTimeMillis() - inicioTotal);

         imprimirNiveles(LOGGER, todos);
         imprimirTiempos(LOGGER);

         return todos;
      }catch(FactoryInstantiationException e){
         LOGGER.log(Level.SEVERE, "Problema instanciando en fábrica", e);
      }
      return Collections.emptyList();
   }

   protected List<IAsociacionTemporal> buscarModelosIteracion(List<String> tipos, IColeccion coleccion,
         int supmin, int win, boolean hastaNivel2, List<IAsociacionTemporal> anterior,
         List<IAsociacionTemporal> semillasNivel, Runtime runtime, int tam) throws FactoryInstantiationException{
      LOGGER.info("Tam: " + tam);

      // Generación de candidatas
      registroT.tiempoCandidatas(tam-1, true);
      List<IAsociacionTemporal> actual = extenderSemillas(semillasNivel, tipos);
      generarCandidatas(tam, anterior, tipos, actual);
      //List<IAsociacionTemporal> restoCandidatos = generarCandidatas(tam, anterior, tipos);
      //actual.addAll(restoCandidatos);

      registroT.tiempoCandidatas(tam-1, false);

      if(actual.isEmpty()){
         return actual;
      }

      // Cálculo de soporte
      registroT.tiempoSoporte(tam-1, true);
      calcularSoporte(actual, coleccion);
      long tiempoAux = registroT.tiempoSoporte(tam-1, false);

      Calendar diferencia = new GregorianCalendar();
      diferencia.setTimeInMillis(tiempoAux);
      imprimirTiempo("Soporte", LOGGER, tam, diferencia);
      registrarUsoMemoria(runtime, tam);

      // Cálculo de patrones
      calculaPatrones(actual, supmin, tam);

      // Purga
      //memoriaSinPurgaNivel[tam-1] = runtime.totalMemory() - runtime.freeMemory();
//      System.out.println("SemNivel[" + (tam-1) + "].size: " + semNivel.get(nivelSemilla+1).size());
      purgarCandidatas(actual, supmin, tam);
//      System.out.println("SemNivel[" + (tam-1) + "].size: " + semNivel.get(nivelSemilla+1).size());
      //memoriaConPurgaNivel[tam-1] = runtime.totalMemory() - runtime.freeMemory();
      imprimirUsoMemoria(LOGGER, tam-1);

      return actual;
   }

   protected void iniciarEstructurasReinicio(List<String> tipos, List<IAsociacionTemporal> modelosBase,
         int win, int cSize) throws FactoryInstantiationException{
      int tSize = tipos.size();
      iniciarContadores(tSize, cSize);
      windowSize = win;
      mapa = new HashMap<String,List<IAsociacionTemporal>>(tSize);
      nuevoMapa = new HashMap<String,List<IAsociacionTemporal>>(tSize);
      mapaPares = new HashMap<String,List<IAsociacionTemporal>>(tSize);

      // Inicializar Mapa
      for(String tipo : tipos){
         mapa.put(tipo, new ArrayList<IAsociacionTemporal>());
         nuevoMapa.put(tipo, new ArrayList<IAsociacionTemporal>());
         mapaPares.put(tipo, new ArrayList<IAsociacionTemporal>());
      }
      // Insertar patrones semilla en Mapa
      for(IAsociacionTemporal modelo : modelosBase){
         if(modelo.size() == 2){ //TODO qué modelos habría que añadir a mapaPares?
            for(String tipo : modelo.getTipos()){
               if(mapaPares.containsKey(tipo)){
                  mapaPares.get(tipo).add(modelo);
               }
            }
         }
         for(String tipo : modelo.getTipos()){
            if(mapa.containsKey(tipo)){
               mapa.get(tipo).add(modelo);
            }
         }
      }
   }

   /**
    *   Busca un conjunto de patrones y asociaciones temporales frecuentes a partir
    * del conjunto de patrones contenidos en 'modelosBase'. El objetivo de este método
    * es proporcionar una forma de reanudar una búsqueda sobre una determinada colección.
    * Por ejemplo, una vez hecha una búsqueda y en base a las distribuciones de
    * frecuencia, se podría optar por modificar el agrupamiento hecho, y querer reanudar
    * la búsqueda desde ese punto, como si el agrupamiento fuese el deseado.
    * Por eso NO se comprobará si los elementos de 'modelosBase' son frecuentes,
    * simplemente se asumirá que sí lo son.
    * @param tipos Tipos de eventos a considerar de la colección.
    * @param coleccion Colección de secuencias de eventos en la que buscar patrones.
    * @param modelosBase Conjunto de asociaciones temporales y patrones base de la búsqueda.
    * @param supmin Umbral de frecuencia mínima para los patrones.
    * @param win Tamaño de ventana a utilizar en la búsqueda.
    * @return Lista de asociaciones temporales frecuentes encontradas a partir de 'modelosBase'.
    * @throws ModelosBaseVaciosException
    */
   @Override
   public List<List<IAsociacionTemporal>> reiniciarBusqueda(List<String> tipos, IColeccion coleccion,
         List<IAsociacionTemporal> modelosBase, int supmin, int win) throws ModelosBaseNoValidosException{
      if(modelosBase==null || modelosBase.isEmpty()){
         throw new ModelosBaseVaciosException();
      }
      try{
         // Inicializar estructuras
         List<List<IAsociacionTemporal>> todos = new ArrayList<List<IAsociacionTemporal>>();
         List<IAsociacionTemporal> actual;
         List<IAsociacionTemporal> listaSemillas = new ArrayList<IAsociacionTemporal>();
         Runtime runtime = Runtime.getRuntime();

         int tam = modelosBase.get(0).size();

         //Para que los patrones de tamaño..
         for(int i=1; i<tam; i++){
            todos.add(new ArrayList<IAsociacionTemporal>());
         }

         actual = new ArrayList<IAsociacionTemporal>();

         iniciarEstructurasReinicio(tipos, modelosBase, win, coleccion.size());

         // Se asume que todos los modelosBase son frecuentes.
         actual = modelosBase;

         tam++;

         while(!actual.isEmpty()){
            todos.add(actual);
            actual = buscarModelosIteracion(tipos, coleccion, supmin, win, false, actual, listaSemillas, runtime, tam);
            tam++;
         }

         imprimirNiveles(LOGGER, todos);
         imprimirTiempos(LOGGER);
         return todos;
      }catch(FactoryInstantiationException e){
         LOGGER.log(Level.SEVERE, "Problema instanciando en fábrica", e);
      }
      return Collections.emptyList();
   }

   protected void setMapa(Map<String,List<IAsociacionTemporal>> mapa){
      this.mapa = mapa;
   }


   protected void setNuevoMapa(Map<String,List<IAsociacionTemporal>> mapa){
      this.nuevoMapa = mapa;
   }

   protected Map<String,List<IAsociacionTemporal>> getNuevoMapa(){
      return nuevoMapa;
   }

   protected void setMapaPares(Map<String,List<IAsociacionTemporal>> mapa){
      this.mapaPares = mapa;
   }

   protected Map<String,List<IAsociacionTemporal>> getMapaPares(){
      return mapaPares;
   }

   @Override
   public Map<String, List<IAsociacionTemporal>> getMapa(Integer tSize){
      if(tSize != null){
         mapa = new HashMap<String, List<IAsociacionTemporal>>(tSize);
      }
      return mapa;
   }

   public Map<String,List<IAsociacionTemporal>> getMapa(){
      return mapa;
   }


   @Override
   public void escribirEstadisticasEstrategia(
         List<List<IAsociacionTemporal>> resultados, Writer fwp,
         boolean shortVersion, int maxIteracion) throws IOException {
      // no hay estadísticas propias de la estrategia
   }

}