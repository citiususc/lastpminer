package source.evento;


import java.util.List;
import java.util.ListIterator;
import java.util.logging.Logger;

import org.apache.commons.math3.stat.descriptive.moment.Kurtosis;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Skewness;
import org.apache.commons.math3.stat.descriptive.moment.Variance;


/**
 * Calcula ciertos descriptores de la colección (necesita recorrerla para ello)
 * @author vanesa.graino
 *
 */
public class DescripcionColeccion {
   private static final Logger LOGGER = Logger.getLogger(DescripcionColeccion.class.getName());

   protected int tiempoTotal;
   protected int numEvs;
   protected int numSeqs;

   //Para instantes temporales (los datos son los números de eventos por instante temporal)
   protected Kurtosis kTrans = new Kurtosis();
   protected Mean mTrans = new Mean();
   protected Skewness skTrans = new Skewness();
   protected Variance s2Trans = new Variance();

   //Para la densidad de secuencias
   protected Kurtosis kDen = new Kurtosis();
   protected Mean mDen = new Mean();
   protected Skewness skDen = new Skewness();
   protected Variance s2Den = new Variance();


   public DescripcionColeccion(IColeccion coleccion) {

      if(coleccion == null) return;

      //Informe del contenido del fichero
      numSeqs = coleccion.size();
      numEvs = 0;
      tiempoTotal = 0;

      for (int i = 0; i < coleccion.size(); i++) {
         ISecuencia seq = coleccion.get(i);
         int sSize = seq.size();
         numEvs += sSize;
         double tInicial = (double)seq.get(0).getInstante();
         double tFinal = (double)seq.get(sSize - 1).getInstante()+1;
         tiempoTotal += tFinal - tInicial;
         double densidad = ((double) sSize) / (tFinal - tInicial);
         newSeqDatum(densidad);
         calculateSequenceStats(seq);
         LOGGER.info("Secuencia " + i + " eventos: " + sSize + " - densidad: " + densidad);
      }
   }

   private void calculateSequenceStats(ISecuencia seq){
      if(seq.isEmpty()) return;

      ListIterator<Evento> it = seq.listIterator();
      //int count = 0;
      //Evento last = new Evento(null, 0);
      int count = 1;
      Evento last = it.next();
      Evento evento;
      while(it.hasNext()){
         evento = it.next();
         if(evento.getInstante() == last.getInstante()){
            count++;
         }else{
            newTransDatum(count);
            count = 1;
            for(int i=last.getInstante()+1;i<evento.getInstante();i++){
               newTransDatum(0);
            }
            last = evento;

         }

      }
      newTransDatum(count);
   }

   protected void newTransDatum(double d){
      LOGGER.finer("New datum: " + d);
      kTrans.increment(d);
      mTrans.increment(d);
      skTrans.increment(d);
      s2Trans.increment(d);
   }

   protected void newSeqDatum(double d){
      LOGGER.finer("New datum: " + d);
      kDen.increment(d);
      mDen.increment(d);
      skDen.increment(d);
      s2Den.increment(d);
   }

   /*protected void endCollection(){
      k.evaluate();
      m.evaluate();
      sk.evaluate();
      s2.evaluate();
   }*/

   public void printInfo(){
      LOGGER.info("Consiste en " + numSeqs + " secuencias");
      LOGGER.info("Suma un total de " + numEvs + " eventos. "
            + "\n Densidad media de " + ((double)numEvs)/tiempoTotal);

      LOGGER.info("Estadísticas de instantes temporales:"
            + "\n\tMedia: " + mTrans.getResult()
            + "\n\tVarianza: " + s2Trans.getResult() + " (desviación típica: " + Math.sqrt(s2Trans.getResult()) + ")"
            + "\n\tKurtosis: " + kTrans.getResult()
            + "\n\tSkewness: " + skTrans.getResult());

      LOGGER.info("Estadísticas de densidad de secuencias:"
            + "\n\tMedia: " + mDen.getResult()
            + "\n\tVarianza: " + s2Den.getResult() + " (desviación típica: " + Math.sqrt(s2Den.getResult()) + ")"
            + "\n\tKurtosis: " + kDen.getResult()
            + "\n\tSkewness: " + skDen.getResult());
   }

   public void printInfo(List<String> tipos, List<Episodio> episodios){
      LOGGER.info("La colección se compone de " + tipos.size() + " tipos de eventos: \n\t" + tipos);
      LOGGER.info("Con " + episodios.size() + " tipos de episodios: \n\t" + episodios );
      printInfo();
   }


}
