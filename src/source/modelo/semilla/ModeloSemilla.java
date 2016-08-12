package source.modelo.semilla;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//import java.util.logging.Logger;




import source.evento.Evento;
import source.excepciones.FactoryInstantiationException;
import source.modelo.IAsociacionSemilla;
import source.modelo.IAsociacionTemporal;
import source.modelo.IMarcasIntervalos;
import source.modelo.Modelo;
import source.modelo.clustering.IClustering;
import source.patron.GeneradorID;
import source.patron.Patron;
import source.patron.PatronSemilla;
import source.patron.PatternFactory;
import source.restriccion.RIntervalo;

/**
 *   Presenta dos cambios importantes con respecto a 'Modelo'.
 *   El primero se encuentra en el procedimiento 'recibeEvento'. Se debe actualizar la distribución de
 * frecuencia independientemente del número de tipos presente. Además, debería mantener alguna estructura
 * que permita cribar las secuencias de entrada, eliminando aquellas subsecuencias en las que no se
 * puede encontrar ninguna ocurrencia del patrón semilla.
 *   El segundo se encuentra en el procedimiento 'calculaPatrones' que, en lugar de purgar aquellos patrones
 * que no cumplen el criterio de frecuencia mínima, realiza un clustering sobre las distribuciones de
 * frecuencia. Para aquellas restricciones que sí han sido definidas por el usuario se calcula la intersección
 * de cada restricción obtenida del clustering con la restricción definida por el usuario, siendo el resultado
 * de cada intersección una restricción para continuar el procedimiento. Si algún par de eventos no tiene
 * ninguna restricción válida, el proceso se termina (se establece la frecuencia del modelo a 0 o valor negativo)
 * @author Miguel
 */

public class ModeloSemilla extends Modelo implements IMarcasIntervalos/*, IAsociacionAgregable*/, IAsociacionSemilla {
   //private static final Logger LOGGER = Logger.getLogger(ModeloSemilla.class.getName());
   private static final long serialVersionUID = -4808348571853911493L;

   //Semilla tiene unos atributos propios

   protected IClustering clustering;

   private int[][] distribuciones;
   private int[][] limites;
   private int[][] abiertas;
   private int[] tam;
   private int[] patFrec;
   private int frecuencia;

   protected int[] ultimaEncontrada = {0,0};

   /*
    * Constructores
    */

   public ModeloSemilla(String[] tipos, int ventana, IClustering clustering){
      this(tipos, ventana, (Integer)null,clustering);
   }

   public ModeloSemilla(String[] tipos, int ventana, Integer frecuencia, IClustering clustering){
      super(tipos,ventana,frecuencia);
      this.clustering = clustering;
      this.distribuciones = iniciarDistribuciones(tipos.length, ventana);
      this.ultimaEncontrada = new int[2];
      this.limites = getLimites();
      this.abiertas = getAbiertas();
      this.tam = getTam();
      this.patFrec = new int[getPatrones().size()];
   }

   public ModeloSemilla(String[] tipos, int ventana, List<Patron> patrones, IClustering clustering){
      this(tipos, ventana, patrones, null, clustering);
   }

   public ModeloSemilla(String[] tipos, int ventana, List<Patron> patrones, Integer frecuencia,
         IClustering clustering){
      super(tipos,ventana,patrones, frecuencia);

      this.clustering = clustering;
      this.distribuciones = iniciarDistribuciones(tipos.length, ventana);
      this.ultimaEncontrada = new int[2];

      this.limites = getLimites();
      this.abiertas = getAbiertas();
      this.tam = getTam();
      this.ventana = getVentana();
      //this.numTipos = getNumTipos();
      this.patFrec = new int[getPatrones().size()];
   }

   /*
    * Metodos
    */

   public static int[][] iniciarDistribuciones(int tSize, int ventana){
     return new int[(tSize*(tSize-1))/2][2*ventana + 1];
  }

   /*
    * TODO por que se sobrescribe?
    * (non-Javadoc)
    * @see source.modelo.ModeloAbstracto#siguienteCombinacion(int[], int[], int, java.lang.String)
    */
   /*@Override
   protected int[] siguienteCombinacion(int[] tam, int[] indices, int index, String tipo){
      int tSize = tipos.length;
      indices[tSize-1]++;
      for(int i=tSize-1;i>0;i--){
         if(i==index){
            if(indices[i]!=0){
               indices[i]=0;
               indices[i-1]++;
            }
            continue; // no puede ser i==index y además estar en un tipo repetido sin ser el i mas bajo
         }
         //if(indices[i]>=(tam[i]-(rep[i]-resta+mod))){
         if(indices[i]>=tam[i]){
            indices[i-1]++;
            indices[i]=0;
         }
      }
      if((index==0 && indices[0]>0) || indices[0]>=tam[0]){
         return null;
      }
      return indices;
   }*/

   /*
    * Sobreescrito para que utilice sus estructuras y no las de modelo
    * (non-Javadoc)
    * @see source.modelo.Modelo#actualizaVentana(source.evento.Evento, java.lang.String, int, int, int)
    */
   //TODO ultimoSid (con cuidado al ser semilla, puede tener un unico evento)
   @Override
   protected boolean actualizaVentana(int sid, Evento ev, String tipo, int tmp, int index, int tSize){
      boolean seguir = true;

      // Actualizar índices fin e inicio para adaptarse a la ventana
      // Eliminar elementos que ya no están en ventana
      for(int j,i=0;i<tSize;i++){
         j = limites[i][0];

         // mientras (hay elementos) y ((hay elementos fuera de la nueva definida) o (el elemento leido ocurre antes => nueva secuencia))
         while(tam[i] > 0 && /*(j!=limites[i][1]) && */ (tmp-ventana >= abiertas[i][j] || tmp < abiertas[i][j])) {
            j = ((j+1) % ventana);
            tam[i]--;
         }
         limites[i][0] = j; // Modificar el indicador de inicio
         if(i!=index && tam[i]<=0 ){
            seguir=false;
         }
      }
      // Añadir el nuevo elemento
      abiertas[index][limites[index][1]] = tmp;
      limites[index][1] = ((limites[index][1]+1)%ventana);
      tam[index]++;
      return seguir;
   }

   @Override
   public void recibeEvento(int sid, Evento ev, boolean savePatternInstances){
      String[] tipos = getTipos();
      String tipo = ev.getTipo();
      int index = Arrays.binarySearch(tipos, tipo); //tipos.indexOf(tipo);

      int tmp = ev.getInstante();
      int tSize = tipos.length;
      if(tSize==1) {
         frecuencia++;
         ultimaEncontrada[0] = tmp;
         ultimaEncontrada[1] = tmp;
         return;
      }

      if(!actualizaVentana(sid, ev, tipo, tmp, index, tSize)){
         return;
      }

      // Actualizar frecuencias
      int[] indices = new int[tSize];
      int[] instancia = new int[tSize];
      instancia[index] = tmp;
      int i=0, j=0, valor;

      ultimaEncontrada[0] = tmp;
      ultimaEncontrada[1] = tmp;

      do{ // Recorre cada lista
         // Comprobar si pertenece a algun patrón
         fijarInstancia(tSize, index, indices, instancia);

         if(comprobarPatrones(instancia, patFrec, sid, savePatternInstances)){
            // Actualizar las distribuciones de frecuencia
            int dist=0;
            for(i=0;i<tSize;i++){ // Actualiza las distribuciones de frecuencia
               if(ultimaEncontrada[0]>instancia[i]){
                  ultimaEncontrada[0]=instancia[i];
               }
               for(j=i+1;j<tSize;j++){
                  valor = (int)(instancia[j] - instancia[i]);
                  distribuciones[dist][valor+ventana]++;
                  dist++;
               }
            }
            frecuencia++;
         }

         indices=siguienteCombinacion(tam,indices,index,tipo);

      }while(indices != null);
      //addFrecuencias(frecuencia,patFrec);
   }

   /*
    * Usa sus propias estructuras
    * (non-Javadoc)
    * @see source.modelo.Modelo#fijarInstancia(int, int, int[], int[])
    */
   @Override
   protected void fijarInstancia(int tSize, int index, int[] indices,
         int[] instancia) {
      for(int i=0;i<tSize;i++){
         if(i!=index){
            instancia[i]=abiertas[i][(limites[i][0]+indices[i])%ventana];
         }
      }
   }

   //Cambia en que no puede hacer el break ya que los patrones semilla no tienen
   // que ser excluyentes entre ellos
   @Override
   protected boolean comprobarPatrones(int[] instancia, int[] patFrecLocal,
         int sid, boolean savePatternInstances) {
      int i, pSize = getPatrones().size();
      boolean encontrado = false;
      for(i=0;i<pSize;i++){
         if(patrones.get(i).representa(sid,instancia,savePatternInstances)){
            encontrado = true;
            patFrecLocal[i]++;
            //break;// TODO break al encontrar una ocurrencia de un patron.
                     // En semilla igual sí debemos procesar todos los patrones
         }
      }
      return encontrado;
   }



   @Override
   public int[][] getDistribuciones(){
      return this.distribuciones;
   }

   @Override
   public void incrementarDistribucion(int indiceDistribucion, int valor){
      this.distribuciones[indiceDistribucion][valor]++;
   }

   @Override
   public IClustering getClustering(){
      return clustering;
   }

   // Realiza el clustering de las distribuciones de frecuencia
   // Se aprovecha el recorrido de este modelo como recorrido para pares de tipos de eventos
   // Para aquellas restricciones establecidas por el usuario, el clustering se restringe a lo establecido por el usuario
   // Como resultado, se generan de nuevo todos los patrones, con todas las posibles combinaciones, y se eliminan los viejos
   @Override
   public int calculaPatrones(int supmin, String patternClassName, GeneradorID genID,
         boolean savePatternInstances) throws FactoryInstantiationException{
      String[] tipos = getTipos();
      int i,j,k,l, tSize = tipos.length;

      super.addFrecuencias(frecuencia, patFrec);

      List<RIntervalo> restricciones = new ArrayList<RIntervalo>();
      // Clustering de las df's (distribuciones de frecuencia)
      int distActual = 0;
      for(i=0; i<tSize-1; i++){
         for(j=i+1; j<tSize; j++){
            List<RIntervalo> rests = getClustering().agrupar(distribuciones[distActual], tipos[i], tipos[j]);
            distActual++;
            List<RIntervalo> rSemilla = getRestricciones(tipos[i], tipos[j]);
            // Intersección con las semillas

            //Si no había, sólo hay que insertar las nuevas restricciones
            if(rSemilla.isEmpty()){
               restricciones.addAll(rests);
               continue;
            }

            // Había semillas, calcular intersección
            List<RIntervalo> trueRests = new ArrayList<RIntervalo>();
            for(k=0;k<rests.size();k++){
               RIntervalo ri = rests.get(k);

               for(l=0;l<rSemilla.size();l++){
                  RIntervalo rs = rSemilla.get(l);
                  if((rs.getInicio() == -Float.NEGATIVE_INFINITY)&&
                        (rs.getFin() == Float.NEGATIVE_INFINITY)){
                     trueRests.add(ri);
                     continue;
                  }
                  int nmin = Math.max(rs.getInicio(), ri.getInicio()), nmax = Math.min(rs.getFin(), ri.getFin());
                  if(nmin <= nmax){
                     trueRests.add(new RIntervalo(rs.getTipoA(), rs.getTipoB(), nmin, nmax));
                  }//else, intersección vacía, no hacer nada
               }
            }
            if(trueRests.isEmpty()){
               // Las semillas no dan lugar a patrones frecuentes, parar proceso
               resetPatrones();
               return 0;
            }else{
               restricciones.addAll(trueRests);
            }
         }
      }

      // Construir patrones de pares de tipos de eventos, y establecer su frecuencia
      int rSize = restricciones.size();
      int[] patFrecLocal = new int[rSize];
      List<Patron> patrones = new ArrayList<Patron>();

      for(i=0; i<rSize; i++){
         RIntervalo ri = restricciones.get(i);
         int indexA = Arrays.binarySearch(tipos, ri.getTipoA());
         int indexB = Arrays.binarySearch(tipos, ri.getTipoB());
         int indexDist = tSize == 2 ? 0 : (indexA*(indexA-1))/2+(indexB-indexA);//TODO falla con tamaño 2 si no se hace así
         int[] distr = distribuciones[indexDist];

         String[] tiposPatron = new String[]{ri.getTipoA(), ri.getTipoB()};
         Patron p = PatternFactory.getInstance().getPattern(genID, patternClassName, tiposPatron,
               new ArrayList<RIntervalo>(restricciones.subList(i, i+1)), savePatternInstances);
         // Calcular frecuencia del patrón
         for(j=(int)ri.getInicio()+ventana; j<ri.getFin()+ventana+1; j++){
            patFrecLocal[i] += distr[j];
         }
         patrones.add(p);
      }
      setPatrones(patrones,patFrecLocal);

      return 0;
/*
// Un fiasco, la explosión combinatoria es intratable.

      // Construccion de los nuevos patrones
      int rSize = restricciones.size();
      int patCount[] = new int[rSize];
      int patIndex[] = new int[rSize];
      Patron[] patCache = new Patron[rSize];
      for(i=0;i<restricciones.size();i++) {patCount[i] = restricciones.get(i).size();}
      int uValido=-1;
      List<Patron> nuevos = new ArrayList<Patron>();
      while(patIndex[0]<patCount[0]){
         //patrones = new ArrayList<Patron>(); // WTF???
         Patron patAux;
         if(uValido<=0){
            List<RIntervalo> ri = new ArrayList<RIntervalo>();
            ri.add(restricciones.get(0).get(patIndex[0]));
            patCache[0] = new Patron(tipos,ri);
            uValido=1;
         }
         // Añadir restricciones
         for(k=uValido;k<rSize;k++){
            patAux = new Patron(patCache[k-1]);
            patAux.addRestriccion(restricciones.get(k).get(patIndex[k]));
            patCache[k] = patAux;
         }
         // Comprobar consistencia del patron
         patAux = patCache[k-1];
         if(patAux.esConsistente()) nuevos.add(patAux);
         // Actualizar patIndex y uValido
         patIndex[k-1]++;
         for(k=rSize-1;(k>0)&&(patIndex[k]>=patCount[k]);k--){
            patIndex[k]=0;
            patIndex[k-1]++;
         }
         uValido=k;

      }
      setPatrones(nuevos);
*/

   }

   @Override
   public int[] getUltimaEncontrada(){
      return ultimaEncontrada;
   }

   @Override
   public int getSoporte(){
      return frecuencia;
   }

   @Override
   protected void addFrecuencias(int frec, int[] patOcs){
      int pSize = getPatrones().size();
      frecuencia+=frec;
      if(patOcs!=null) {
         for(int i=0;i<pSize;i++){
            patFrec[i] += patOcs[i];
         }
      }
   }

   /**
    * Clona este objeto
    */
   @Override
   public ModeloSemilla clonar(){
      List<Patron> patronesCopia = new ArrayList<Patron>();
      for(Patron p:getPatrones()){
         patronesCopia.add(((PatronSemilla)p).clonar());
      }
      return new ModeloSemilla(getTipos(), ventana, patronesCopia, frecuencia, getClustering());
   }

   protected void addDistribuciones(int[][] distribuciones){
      if(distribuciones == null) return;
      for(int i=0;i<distribuciones.length;i++){
         for(int j=0; j<distribuciones[i].length;j++){
            this.distribuciones[i][j] += distribuciones[i][j];
         }
      }
   }

   /**
    * Agrega la frecuencia y las ocurrencias de patron a este modelo
    * Implementa el método que define la interfaz IAsociacionAgregable
    * que se utiliza en ModeloSemillaParalelo
    * @param agregada
    */
   public synchronized void agregar(IAsociacionTemporal asociacion){
      if(!(asociacion instanceof ModeloSemilla)){ return; }
      ModeloSemilla agregada = (ModeloSemilla)asociacion;
      int pSize = getPatrones().size();
      addFrecuencias(agregada.frecuencia, agregada.patFrec);
      for(int i=0;i<pSize;i++){
         Patron p = getPatrones().get(i);
         p.agregar(agregada.getPatrones().get(i));
         //if(p.isSavePatternInstances()){
         //   p.getOcurrencias().addAll(agregada.getPatrones().get(i).getOcurrencias());
         //}
      }
      //Sumar distribuciones
      addDistribuciones(agregada.getDistribuciones());
   }

   @Override
   public boolean necesitaPurga(int minFreq) {
      return frecuencia<minFreq || (tipos.length>1 && patrones.isEmpty());
   }

}