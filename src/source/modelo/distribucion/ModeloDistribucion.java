package source.modelo.distribucion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//import java.util.logging.Logger;

import source.evento.Evento;
import source.excepciones.FactoryInstantiationException;
import source.modelo.IAsociacionConDistribucion;
import source.modelo.IAsociacionTemporal;
import source.modelo.IMarcasIntervalos;
import source.modelo.Modelo;
import source.modelo.clustering.IClustering;
import source.patron.GeneradorID;
import source.patron.Patron;
import source.patron.PatternFactory;
import source.restriccion.RIntervalo;

/**
 * Modelo para asociaciones de dos eventos
 * @author vanesa.graino
 *
 */
public class ModeloDistribucion extends Modelo implements IAsociacionConDistribucion, IMarcasIntervalos{
   //private static final Logger LOGGER = Logger.getLogger(ModeloDistribucion.class.getName());
   private static final long serialVersionUID = -8763078146702954768L;


   /*
    * Atributos propios
    */

   private int[] ultimaEncontrada;
   private int[] distribucion;
   protected IClustering clustering;

   /*
    * Constructores
    */

   public ModeloDistribucion(String[] tipos, int ventana,
         Integer frecuencia, IClustering clustering) {
      super(tipos, ventana, frecuencia);
      this.clustering = clustering;
      this.distribucion = iniciarDistribucion(tipos.length, ventana);
      this.ultimaEncontrada = new int[2];
   }

   public ModeloDistribucion(String[] tipos, int ventana,
         List<Patron> patrones,
         Integer frecuencia, IClustering clustering) {
      super(tipos, ventana, patrones, frecuencia);
      this.clustering = clustering;
      //this.distribucion = iniciarDistribucion(tipos.size(), ventana);
      this.ultimaEncontrada = new int[2];
   }

   public ModeloDistribucion(String[] tipos, int ventana, List<Patron> patrones,
         int[] distribucion, IClustering clustering){
      super(tipos, ventana, patrones, null);
      this.patrones = patrones;
      this.clustering = clustering;
      this.distribucion = distribucion;
      this.addFrecuencias(frecuenciaPorDistribucion(distribucion, ventana), null);
      //this.patFrec = new int[patrones.size()];
      int[] patFrec = getPatFrec();
      // Recoger frecuencia de los patrones de 'distribucion'
      frecuenciaPatronesPorDistribucion(patFrec, patrones, distribucion, ventana);
      this.ultimaEncontrada = new int[2];
   }

   /*
    * Otros métodos
    */

   /*
    * Métodos relacionados por distribuciones expuestos publicamente
    */
   //Cuando no hay distribucion
   public static int[] iniciarDistribucion(int tSize, int ventana){
      return new int[2*ventana + 1];
   }

  //Cuando hay distribucion
   public static int frecuenciaPorDistribucion(int[] distribucion, int ventana){
      int frecuencia = 0;
      for(int i=0; i<2*ventana+1; i++){
         frecuencia += distribucion[i];
      }
      return frecuencia;
   }

   //Cuando hay patrones y distribucion
   public static void frecuenciaPatronesPorDistribucion(int[] patFrec, List<Patron> patrones, int[] distribucion, int ventana){
      // Recoger frecuencia de los patrones de 'distribucion'
      int i=0;
      for(Patron patron : patrones){
         for(RIntervalo ri : patron.getRestricciones()){
            for(int j=(int)ri.getInicio()+ventana;j<ri.getFin()+ventana+1;j++){
               patFrec[i]+=distribucion[j];
            }
         }
         i++;
      }
   }

   public static int[] construirPatrones(GeneradorID genID, IAsociacionConDistribucion asoc, String patternClassName,
         boolean savePatternInstances, List<Patron> patronesIniciales) throws FactoryInstantiationException{
      int i=0;
      String[] tipos = asoc.getTiposRestricciones();
      int[] distribucion = asoc.getDistribucion();

      List<RIntervalo> rests = asoc.getClustering().agrupar(distribucion, tipos[0], tipos[1]);

      if(patronesIniciales.isEmpty()){
         for(i=0;i<rests.size();i++){
            List<RIntervalo> aux = new ArrayList<RIntervalo>();
            aux.add(rests.get(i));
            asoc.addPatron(PatternFactory.getInstance().getPattern(genID, patternClassName, tipos, aux, savePatternInstances));
         }
      }else{
         List<RIntervalo> rSemilla = asoc.getRestricciones(tipos[0]);
         List<Patron> truePatterns = new ArrayList<Patron>();
         for(i=0;i<rests.size();i++){
            RIntervalo ri = rests.get(i);
            for(int j=0;j<rSemilla.size();j++){
               RIntervalo rs = rSemilla.get(j);
               int nmin = Math.max(rs.getInicio(),ri.getInicio()),nmax=Math.min(rs.getFin(),ri.getFin());
               if(nmin<=nmax){
                  List<RIntervalo> aux = new ArrayList<RIntervalo>();
                  aux.add(new RIntervalo(rs.getTipoA(),rs.getTipoB(),nmin,nmax));
                  truePatterns.add(PatternFactory.getInstance().getPattern(genID, patternClassName, tipos, aux, savePatternInstances));
               }//else, intersección vacía, no hacer nada
            }
         }
         // Sustituir los patrones semilla por los encontrados en el registro
         // consistentes con las semillas. Las semillas originales "se borran".
         //this.patrones = truePatterns;
         asoc.getPatrones().clear();
         asoc.getPatrones().addAll(truePatterns);

      }
      List<Patron> patrones = asoc.getPatrones();

      // Actualización de la frecuencia de los patrones
      int[] frec = new int[patrones.size()];
      for(i=0;i<patrones.size();i++){
         RIntervalo ri = patrones.get(i).getRestricciones(tipos[0], tipos[1]).get(0);
         for(int j=(int)ri.getInicio()+asoc.getVentana();j<ri.getFin()+asoc.getVentana()+1;j++){
            frec[i]+=distribucion[j];
         }
      }

      return frec;
   }

   /**
    * Atención! Utilizar este método sólo cuando se quiera
    * modificar gran parte de la distribución. Para un único valor,
    * puede que incrementarDistribucion sea más adecuado.
    * @param distr - las distribución con la que se va a incrementar la distribución
    * del modelo <this>.
    */
   protected void addDistribucion(int[] distr){
      int[] distrOriginal = getDistribucion();
      if(distrOriginal == null){ return; }
      for(int i=0, x=distr.length;i<x;i++){
        distrOriginal[i] += distr[i];
      }
   }

   @Override
   public void recibeEvento(int sid, Evento ev, boolean savePatternInstances){
      String tipo = ev.getTipo();
      int index = Arrays.binarySearch(tipos, tipo); //tipos.indexOf(tipo);
      int tmp = ev.getInstante();
      final int tSize = 2; //tipos.length;

      if(!actualizaVentana(sid, ev, tipo, tmp, index, tSize)){
         return;
      }

      // Actualizar frecuencias
      int[] instancia = new int[tSize];
      instancia[index]=tmp;
      int[] indices = new int[tSize];
      buscaOcurrenciasTam2(tipo, index, index, tmp, indices);
   }

   @Override
   public int[] getDistribucion() {
      return distribucion;
   }

   @Override
   public IClustering getClustering() {
      return clustering;
   }

   /**
    * Incrementa en una unidad el valor de la distribución para la
    * coordenada fila x columna.
    * @param fila
    * @param columna
    */
   public void incrementarDistribucion(int indice){
      distribucion[indice]++;
   }

   @Override
   public int calculaPatrones(int supmin, String patternClassName, GeneradorID genID, boolean savePatternInstances) throws FactoryInstantiationException{
      int i=0;
      int[] patFrec = getPatFrec();

      // Evaluación del histograma y agrupamiento
      int[] aux = construirPatrones(genID, this, patternClassName, savePatternInstances, patrones);

      int len = aux.length, borrados = 0;
      for(i=patrones.size()-1;i>=0;i--){
         if(aux[i]<supmin){
            patrones.remove(i);
            borrados++;
         }
      }
      if(patrones.size()==len){// Ninguno era poco frecuente.
         patFrec=aux;
      }else{
         int j=0;
         patFrec = new int[patrones.size()];
         for(i=0,j=0;(i<aux.length)&&(j<patFrec.length);i++){
            if(aux[i]>=supmin){
               patFrec[j]=aux[i];
               j++;
            }
         }
      }
      setPatFrec(patFrec);
      return borrados;
   }


   protected int buscaOcurrenciasTam2(String tipo, int index, int indexReal, int tmp, /*int tSize,*/ int[] indices){
      int i, frecuenciaLocal=0, valor;
      int tMin = tmp;
      final int tSize = 2;
      int[] instancia = new int[tSize];

      int[] tam = getTam();
      int[][] abiertas = getAbiertas();
      int[][] limites=getLimites();
      do{ // Recorre cada lista
         //tMin = fijarInstancia(tSize, index, tmp, indices, instancia);
         tMin = fijarInstancia(tSize, index, tmp, abiertas, limites, indices, instancia);

         // Actualizar las distribuciones de frecuencia
         for(i=0;i<tSize;i++){ // Actualiza las distribuciones de frecuencia
            if(i!=indexReal){
               //instancia[i]=abiertas[i][(limites[i][0]+indices[i])%ventana];
               //valor=abiertas[i][(limites[i][0]+indices[i])%ventana] - tmp;
               valor = (int)(instancia[i] - tmp);
               if(indexReal>i || tipos[indexReal] == tipos[i]) { // Relacion i-> index
                  valor *= -1;
               }
               incrementarDistribucion(valor+ventana);
            }
         }
         frecuenciaLocal++;
         indices = siguienteCombinacion(tam,indices,indexReal,tipo);

      }while(indices != null);
      //TODO se calcula correctamente ultimaEncontrada?
      // Tiene sentido buscar ultimaEncontrada en tamaño 2?? No lo creo
      ultimaEncontrada[0] = tMin;
      ultimaEncontrada[1] = tmp;

      addFrecuencias(frecuenciaLocal,null);
      return tMin;
   }

   /*
    * Se sobrecarga para que
    */
   protected int fijarInstancia(int tSize, int index, int tmp,
         int[][] abiertas, int[][] limites, int[] indices, int[] instancia) {
      int tMin = tmp;
      for(int i=0;i<tSize;i++){
         if(i!=index){
            instancia[i]=abiertas[i][(limites[i][0]+indices[i])%ventana];
            if(tMin>instancia[i]){
               tMin=instancia[i];
            }
         }
      }
      return tMin;
   }

   @Override
   public synchronized void agregar(IAsociacionTemporal asociacion) {
      if(!(asociacion instanceof ModeloDistribucion)){
         return;
      }
      super.agregar(asociacion);
      //Sumar distribuciones
      int[] distr = ((ModeloDistribucion)asociacion).getDistribucion(), distrOriginal = getDistribucion();
      if(distrOriginal == null || distr == null){ return; }
      addDistribucion(distr);
   }

   @Override
   public int[] getUltimaEncontrada() {
      return ultimaEncontrada;
   }

   @Override
   public int size(){
      return 2;
   }

   public String[] getTiposRestricciones(){
       return tipos;
   }

}

