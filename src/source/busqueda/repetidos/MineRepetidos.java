package source.busqueda.repetidos;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import source.busqueda.jerarquia.GeneradorPatronesArbol;
import source.busqueda.jerarquia.MineArbolSuperModelo;
import source.excepciones.FactoryInstantiationException;
import source.modelo.AssociationFactory;
import source.modelo.IAsociacionConRepeticion;
import source.modelo.IAsociacionEvento;
import source.modelo.IAsociacionTemporal;
import source.modelo.arbol.Nodo;
import source.modelo.arbol.Supernodo;
import source.modelo.clustering.IClustering;
import source.modelo.repetidos.ModeloAuxiliarRepeticion;
import source.modelo.repetidos.ModeloDistribucionRepetido;
import source.patron.Patron;

public class MineRepetidos extends MineArbolSuperModelo {
   private static final String TIEMPOS_NODOS_AUX= "nodosAux";
    private static final Logger LOGGER = Logger.getLogger(MineRepetidos.class.getName());
    public static Logger getLogger(){
       return LOGGER;
    }

    //protected long[] tiemposNodosAuxiliares;

    public MineRepetidos(String executionId, boolean savePatternInstances, boolean saveRemovedEvents,
            IClustering clustering, boolean removePatterns) {
        super(executionId, savePatternInstances, saveRemovedEvents, clustering, removePatterns);
        registroT.addOtrosTiempos(TIEMPOS_NODOS_AUX);
    }

    /*@Override
    protected void iniciarContadores(int tSize, int cSize) {
        super.iniciarContadores(tSize, cSize);
        this.tiemposNodosAuxiliares = new long[tSize];
    }*/

    // COMBINACIÓN DE EVENTOS

    // Ideas: un combinador de nodos que decida qué hacer dependiendo
    // del tipo de nodo: los nodos del árbol tienen un tipo de nodo
    // o son diferentes subclases que implementan alguna interfaz de combinación
    // Problema que sucede si se combinan nodos de diferentes tipos, cual manda? el padre
    // es decir el primer que se encuentra para combinar)?
    // Por ejemplo:
    // + si el nodo tiene eventos repetidos se tiene que comportar diferente
    //   a si no tiene evnetos repetidos
    //   Ejemplo: A0A1B para combinar con A0A1B no tiene que existir A1BC
    // + si solo tiene eventos repetidos:
    //   Ejemplo: A1A2 se extiende a A1A2A3 si comprobar más subnodos
    // + Si no tiene eventos repetidos se comporta igual que se comportaba

    // Crear nodos falsos tipo A2B o realmente buscar estos eventos, desde qué tamaño?
    // Por ejemplo: si los voy a buscar debería crear A2B, A3B, A4B, etc
    // Creación retroactiva de nodos para facilitar la construcción? Es necesario?
    // Crear estos pseudonodos sin padres o construir toda la jerarquía?


    // CALCULO DE FRECUENCIA

    // Utilizando un supermodelo
    // Una nueva implementación de ModeloEventoFinal para manejar tipos repetidos
    //


    // MODELOS CON REPETICION

    // variable rep[tam] que indica repetición de cada tipo
    // getString con repeticion
    // sin repetición A,B,C,D
    // con rep = [2,1,1,1] -> A,A1,B,C,D
    // Considerar directamente A,A1,B,C,D como tipos
    // para poder tener restricciones entre A y A1
    // tener un booleano de repetidos: false, true, false, false, false
    // de quien es repetido: null, A, null, null, null
    // tienes repetidos existe i tal que repetido[i] != null

    // Repetido auxiliar o no


    // Combinacion
    // Crear A;B;C;D; ...
    // Crear AA1, AB, AC, AD; BB1, BC, BD; CC1, CD; DD1

    // Llamo a calcularPatrones y purgaCandidatas
    // Y creo nodos auxiliares
    // A; *A1; B; *B1; C; *C1; D; *D1; -> Los hijos de A1,B1, no estarán en la lista de nodos actual
    // AA1, AB, *AB1, AC, *AC1, AD, *AD1; *A1B, *A1B1, *A1C, *A1C1, *A1D, *A1D1; BB1, BC, BD; *B1C, *B1D ...
    // Ej.: AB1 es repetido auxiliar no es padre en la combinación, en su lugar los es AB
    //

    // En la siguiente combino AA1A2, AA1+AB = AA1B, AA1+AC =AA1C ,etc


    protected void generarCandidatasTam2(List<IAsociacionTemporal> anteriores, List<String> tipos,
             List<IAsociacionTemporal> candidatas, List<Supernodo> nuevoNivel) throws FactoryInstantiationException{
       List<Nodo> nodos = raizArbol.getListaNodos();
       final int nSize = nodos.size(), tam=2;
       int i,j;
       String[] modArray;

       mapa = construyeMapa(tipos.size(), tipos);
       for(i=0;i<nSize;i++){
          Nodo nodo = nodos.get(i);
          IAsociacionEvento padre = (IAsociacionEvento)nodo.getModelo();

          // Crear supernodo de hijos
          Supernodo hijos = nodo.getHijos();
          //for(j=i+1;j<nSize;j++){
          for(j=i;j<nSize;j++){ //Repite eventos
             IAsociacionEvento madre = (IAsociacionEvento)nodos.get(j).getModelo();
             //mod = new ArrayList<String>(padre.getTipos());
             //mod.add(tipo);


             //
             if(j==i){ //Tipo evento repetido
                 modArray = new String[]{ padre.getTipoEvento()};
             }else{
                 modArray = new String[]{ padre.getTipoEvento(), madre.getTipoEvento() };
             }
             //modArray = Arrays.copyOf(padre.getTipos(), padre.getTipos().length+1);
             //modArray[modArray.length-1] = tipo;

             IAsociacionTemporal modelo = crearModelo(modArray);
             //IAsociacionTemporal modelo = AssociationFactory.getInstance().getAssociationInstance(associationClassName,
             //      mod, windowSize, isSavePatternInstances(), getClustering(), numHilos);

             notificarModeloGenerado(tam, 0, modelo, modArray, candidatas, mapa);

             // Crear nodo hijo y agregar al padre
             //Nodo hijo = creaNodoFachada(modelo,hijos);
             //hijos.addNodo(hijo, tipo);
             creaNodoFachada(modelo, hijos, madre.getTipoEvento());

          }
          if(!hijos.getNodos().isEmpty()){
             nuevoNivel.add(hijos);
          }
       }
    }

    @Override
    protected IAsociacionTemporal crearModelo(String[] modArray) throws FactoryInstantiationException{
       return AssociationFactory.getInstance().getAssociationInstance(associationClassName,
             modArray, windowSize, getClustering(), supermodelo, modArray.length==1, numHilos);
    }


    @Override
    protected void generarCandidatasGeneral(int tam, List<IAsociacionTemporal> candidatas, List<String> tipos,
            List<Supernodo> nuevoNivel) throws FactoryInstantiationException{
      int i, j;
      IAsociacionTemporal modelo;
      String[] modArray;

      GeneradorPatronesArbol genp = new GeneradorPatronesArbol(tam, this);

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
               modArray = genp.getModArray();

               // Comprobar que las subasociaciones temporales son frecuentes
               //boolean valido= DictionaryUtils.comprobarSubasociaciones(raizArbol, tam, asocBase, patCount, modArray);
               boolean valido = genp.comprobarSubasociaciones(raizArbol, modArray);

               registroT.tiempoAsociaciones(tam-1, false);

               if(!valido){ continue; }

               List<Patron> patrones = genp.generarPatrones(modArray);

               // Construir el modelo
               registroT.tiempoModelo(tam-1, true);
               if(!patrones.isEmpty()){
                  // Hay: añadir punteros en la tabla hash y a candidatas
                  modelo = crearModelo(modArray, patrones, genp);

                  //setModeloPatrones(patrones, modelo);

                  notificarModeloGenerado(tam, patrones.size(), modelo, modArray, candidatas, mapa);
                  // Añadir el Nodo al nuevo
                  //Nodo hijo = creaNodoFachada(modelo,hijos);
                  //hijos.addNodo(hijo, tipoNuevo);
                  creaNodoFachada(modelo, hijos, genp.getTipoNuevo());

               }// else: No hay patrones candidatos: descartar modelo candidato actual
               registroT.tiempoModelo(tam-1, false);

            } // for j
            if(!hijos.getNodos().isEmpty()){
               nuevoNivel.add(hijos);
            }
         } // fin for i
      }
  }


    @Override
    protected void purgarCandidatas(List<IAsociacionTemporal> candidatas, int supmin, int tamActual){

        super.purgarCandidatas(candidatas, supmin, tamActual);

        // Crear nodos auxiliares para facilitar la construcción con repetidos
        // TODO

        //long tInicial = System.currentTimeMillis();
        registroT.tiempo(TIEMPOS_NODOS_AUX, tamActual-1, true);
        int i,j;
        for(Supernodo supernodo : nivelActual){
            List<Nodo> nodos = supernodo.getListaNodos();
            int nSize = nodos.size();
            for(i=0;i<nSize;i++){
               Nodo padre = nodos.get(i);

               if(!(padre.getModelo() instanceof IAsociacionConRepeticion)){ continue; }

               IAsociacionConRepeticion padreMod = (IAsociacionConRepeticion)padre.getModelo();


               //Si es repetición pura: creamos el tamaño siguiente. Ejemplo: si es AA1, creamos AA1A2
               if(padreMod instanceof ModeloDistribucionRepetido){

                   // Modelo tamaño 1
                   String tipo = padreMod.getTipos()[0];
                   int indice = padreMod.getRep()[0];
                   IAsociacionConRepeticion modelo = new ModeloAuxiliarRepeticion(new String[]{ tipo }, windowSize, null, new int[]{ indice }); //TODO
                   Nodo nodo = creaNodoFachada(modelo, raizArbol, tipo, indice);

                   Iterator<Supernodo> it = raizArbol.iterator();
                   List<Nodo> superNodoActual;

                   // Recorrido en anchura creando nodos hasta llegar a <nodo>
                   while(it.hasNext()){
                       superNodoActual = it.next().getListaNodos();
                       for(Nodo nodoActual : superNodoActual){
                           if(nodo != nodoActual){
                               break;
                           }

                       }
                   }
               }else{
                   // Si es otro tipo de nodo
                   //Supernodo hijos = padre.getHijos();
                   for(j=i+1;j<nSize;j++){

                   }
               }
            }
        }

        //this.tiemposNodosAuxiliares[tamActual-1] = System.currentTimeMillis() - tInicial;
        registroT.tiempo(TIEMPOS_NODOS_AUX, tamActual-1, false);
    }

    //@Override
    public Nodo creaNodoFachada(IAsociacionTemporal modelo, Supernodo supernodo, String tipo, int indice){
       Nodo n = new Nodo(modelo, supernodo);
       supernodo.addNodo(n, tipo);
       return n;
    }




}
