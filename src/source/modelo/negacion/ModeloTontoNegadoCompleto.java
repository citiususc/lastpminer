package source.modelo.negacion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import source.evento.Evento;
import source.modelo.condensacion.ISuperModelo;
import source.modelo.condensacion.ModeloTonto;
import source.patron.Ocurrencia;
import source.patron.Patron;

public class ModeloTontoNegadoCompleto extends ModeloTonto implements IAsociacionConNegacion {
   private static final Logger LOGGER = Logger.getLogger(ModeloTontoNegadoCompleto.class.getName());
   private static final long serialVersionUID = -5965378424513749836L;

   /*
    * Atributos
    */

   // Estructuras para manejar los eventos negados
   protected final String[] negados;
   protected int[] indicesNegados;
   protected int[] tamNegados;
   protected int ultimoInstante; //ultimo instante en el que se ha procesado algo
   public List<Ocurrencia> ocurrencias;


   /*
    * Constructores
    */

   //Para testing
   public ModeloTontoNegadoCompleto(String[] tipos, String[] negados, int ventana, Integer frecuencia){
      super(tipos, ventana, frecuencia);
      this.negados = negados;
      this.tamNegados = new int[negados.length];
      if(tipos.length==1){
         ocurrencias = new ArrayList<Ocurrencia>();
      }
   }

   public ModeloTontoNegadoCompleto(String[] tipos, String[] negados, int ventana, Integer frecuencia,
         ISuperModelo supermodelo ){
      super(tipos, ventana, frecuencia, supermodelo);
      this.negados = negados;
      this.tamNegados = new int[negados.length];
      this.indicesNegados = supermodelo.obtenerIndices(negados);
      if(tipos.length==1){
         ocurrencias = new ArrayList<Ocurrencia>();
      }
   }

   public ModeloTontoNegadoCompleto(String[] tipos, String[] negados, int ventana, List<Patron> patrones, Integer frecuencia,
         ISuperModelo supermodelo ){
      super(tipos, ventana, patrones, frecuencia, supermodelo);
      this.negados = negados;
      this.tamNegados = new int[negados.length];
      this.indicesNegados = supermodelo.obtenerIndices(negados);
      if(tipos.length==1){
         ocurrencias = new ArrayList<Ocurrencia>();
      }
   }

   /*
    * Otros métodos
    */

   /*
    * (non-Javadoc)
    * @see source.modelo.negacion.IAsociacionConNegacion#getTiposNegados()
    */
   @Override
   public String[] getTiposNegados() {
      return negados;
   }

   @Override
   protected boolean actualizaVentana(int sid, Evento ev, String tipo, int tmp, int index, int tSize){
      return asociacionEnVentana();
   }


   /**
    * Comprueba si la asociación temporal está instanciada en la ventana
    * @return
    */
   protected boolean asociacionEnVentana(){
      int[] tam = getTam();
      //Tipos de eventos negativos
      for(int i=0;i<indicesNegados.length; i++){
         tamNegados[i] = tamColeccion[indicesNegados[i]];
         if(tamNegados[i]>0){
            return false;
         }
      }
      //Tipos de eventos positivos
      for(int i=0; i<indices.length; i++){
         tam[i] = tamColeccion[indices[i]];
         if(tam[i] <= 0){
            return false;
         }
      }
      return true;
   }

   /**
    * Este es el método al que hay que llamar cuando sale de la ventana un evento
    * que este modelo niega. Hay que actualizar
    * @param savePatternInstances
    */
   public void saleEventoNegado(int sid, Evento ev, boolean savePatternInstances){
      if(ultimoInstante == ev.getInstante()){
         //Ya se había recibo la salida de eventos en este instante
         return;
      }
      int tmp = ev.getInstante();
      ultimoInstante = tmp;

      if(!asociacionEnVentana()){
         return;
      }

      if(tipos.length == 1){ //Sólo hay un tipo positivo
         LOGGER.fine("Ocurrencia cuando sale " + ev + "(tam=" + getTam()[0] + ")");
         if(savePatternInstances){
            int[][] limites = getLimites(), abiertas=getAbiertas();
            for(int i=limites[0][0];i!=limites[0][1];i=(i+1)%ventana){
               ocurrencias.add(new Ocurrencia(sid, new int[]{abiertas[0][i]}));
            }
         }
         addFrecuencias(getTam()[0], null);//incrementarSoporte(); //puede ser más de una
         return;
      }

      int[][] abiertas = getAbiertas(), limites = getLimites();
      //Comprobar instancias de patrones
      int tSize = tipos.length;
      int[] indices = new int[tSize];
      int[] instancia = new int[tSize];
      int[] tam = getTam();
      int[] patFrecLocal = new int[getPatrones().size()];
      int frecuenciaLocal = 0;
      do{
         if(fijarInstancia(abiertas, limites, tSize, indices, instancia)){
            if(comprobarPatrones(instancia, patFrecLocal, sid, savePatternInstances)){
               frecuenciaLocal++;
            }
         }
         indices = siguienteCombinacion(tam, indices);
      }while(indices != null);
      addFrecuencias(frecuenciaLocal, patFrecLocal);

   }

   /**
    * Comprueba que no se procesan las instancias desordenadas porque daría lugar
    * contar dos veces una misma instancia.
    * @param abiertas
    * @param limites
    * @param tSize
    * @param indices
    * @param instancia
    * @return
    */
   protected boolean fijarInstancia(int[][] abiertas, int[][] limites, int tSize, int[] indices, int[] instancia){
      instancia[0] = abiertas[0][(limites[0][0]+indices[0])%ventana];
      for(int i=1; i<tSize; i++){
         instancia[i] = abiertas[i][(limites[i][0]+indices[i])%ventana];
         if(instancia[i-1] == instancia[i]){
            return false;
         }
      }
      return false;
   }

   //Cuando se recibe un evento negativo (la salida de un evento de la ventana)
   protected int[] siguienteCombinacion(int[] tam, int[] indices){
      int tSize = tipos.length;
      indices[tSize-1]++;
      for(int i=tSize-1; i>0 ;i--){
         if(indices[i]>=tam[i]){
            indices[i-1]++;
            indices[i]=0;
         }else{
            break; // No hay que propagar más cambios, combinación de eventos válida.
         }
      }
      if(indices[0] >= tam[0]){
         // Si entra aquí es que no hay más combinaciones
         return null;
      }
      return indices;
   }

   @Override
   public void recibeEvento(int sid, Evento ev, boolean savePatternInstances){
      if(!asociacionEnVentana()){
         return;
      }
      //Borrar ocurrencias si se encuentra un tipo negado en el instante en el que termina una
      if(tipos.length == 1){
         LOGGER.fine("Ocurrencia cuando entra " + ev);
         // Podría comprobar que el tipo es el de la asociación, pero presupongo que si
         // entra aquí es porque ha recibido un tipo del modelo
         // Hay que comprobar que no hay negativos en la ventana
         incrementarSoporte();
         if(savePatternInstances){
            ocurrencias.add(new Ocurrencia(sid, new int[]{ev.getInstante()}));
         }
         return;
      }

      // Hay mas de un evento positivo y por tanto patrones: actualizar frecuencias
      int frecuenciaLocal = 0, tSize = tipos.length, tmp = ev.getInstante();
      int[] patFrecLocal = new int[patrones.size()];
      int[] indices = new int[tSize];
      int[] tam = getTam();

      int[][] abiertas = getAbiertas(), limites = getLimites();
      String tipo = ev.getTipo();
      int index = Arrays.binarySearch(tipos, tipo); //tipos.indexOf(tipo);
      int[] instancia = new int[tSize];
      instancia[index] = tmp;

      do{
         if(fijarInstancia(abiertas, limites, indices, instancia, index, tipo, tSize, tmp)){
            if(comprobarPatrones(instancia, patFrecLocal, sid, savePatternInstances)){
               frecuenciaLocal++;
            }
         }
         indices = siguienteCombinacion(tam, indices, index, tipo);
      }while(indices != null);
      addFrecuencias(frecuenciaLocal, patFrecLocal);
   }

   @Deprecated
   @Override
   protected void fijarInstancia(int tSize, int index, int[] indices, int[] instancia){
      super.fijarInstancia(tSize, index, indices, instancia);
   }

   // Cuando se recibe un evento positivo
   protected boolean fijarInstancia(int[][] abiertas, int[][] limites,
      int[] indices, int[] instancia, int index, String tipo, int tSize, int tmp){

      for(int i=0;i<tSize;i++){
         if(i != index){
            instancia[i]=abiertas[i][(limites[i][0]+indices[i])%ventana];
         }
         //if(i>0 && instancia[i-1]==instancia[i]){
         if(i>index && instancia[i] == tmp){
            return false;
         }
      }
      return true;
   }

   /**
    * Controla que no se cree una combinación con un evento posterior
    */
   /*
    * (non-Javadoc)
    * @see source.modelo.ModeloAbstracto#siguienteCombinacion(int[], int[], int, java.lang.String)
    */
   @Override
   protected int[] siguienteCombinacion(int[] tam, int[] indices, int index, String tipo){
      int tSize = tipos.length;
      indices[tSize-1]++;
      for(int i=tSize-1;i>0;i--){
         if(i==index){
            if(indices[i]!=0){
               indices[i]=0;
               indices[i-1]++;
            }
         }else{
            if(indices[i] >= tam[i] ){
               indices[i-1]++;
               indices[i]=0;
            }else{
               break; // No hay que propagar más cambios, combinación de eventos válida.
            }
         }
      }
      if((index==0 && indices[0]>0) || indices[0]>=tam[0]){
         return null;
      }
      return indices;
   }

   @Override
   public String toString(){
      int[] patFrec = getPatFrec();
      StringBuilder aux = new StringBuilder("Modelo: " + toStringSinPatrones() + " - Numero de patrones: " + patrones.size() + "\n");
      for(int i=0;i<patrones.size();i++){
         aux.append( " Fr: "
               + patFrec[i] + " - "
               + patrones.get(i)+"\n");
      }
      return aux.toString();
   }

   @Override
   public boolean partePositiva(){
      return tipos.length>0;
   }

   @Override
   public boolean parteNegativa(){
      return negados.length>0;
   }

   @Override
   public boolean necesitaPurga(int minFreq) {
      if(tipos.length==1){
         return getSoporte() < minFreq;
      }
      return super.necesitaPurga(minFreq);
   }

   @Override
   public int size(){
      return getTipos().length + negados.length;
   }

   public boolean soloNegados(){
      return tipos.length == 0;
   }

   @Override
   public String toStringSinPatrones(){
      StringBuilder sb = new StringBuilder(Arrays.toString(tipos));
      if(negados.length>0){
         sb.deleteCharAt(sb.length()-1);
         if(tipos.length>0){
            sb.append(", ");
         }
         for(String tipo : negados){
            sb.append(PREF_NEG + tipo + ", ");
         }
         sb.deleteCharAt(sb.length()-1).deleteCharAt(sb.length()-1).append("]");
      }
      return sb.toString();
   }

   @Override
   public List<String> getTiposConNegacion(){
      List<String> conNegados = new ArrayList<String>(Arrays.asList(getTipos()));
      for(int i=0; i<negados.length; i++){
         conNegados.add(PREF_NEG + negados[i]);
      }
      return conNegados;
   }

   @Override
   public String getUltimoTipo() {
      return parteNegativa()? negados[negados.length - 1] : tipos[tipos.length -1];
   }

   //@Override
   public int compare(IAsociacionConNegacion o) {
      // TODO Auto-generated method stub
      return 0;
   }
}
