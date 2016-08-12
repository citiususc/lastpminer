package source.modelo.concurrente;

public interface IMarcasIntervaloConcurrente extends IAsociacionTemporalConcurrente{

   /**
    *
    * @return int[] de 2 posiciones. El primero es el instante de ocurrencia del evento
    * con valor más bajo que pertenece a una ocurrencia de algún patrón de la asociación,
    * y el segundo el instante de ocurrencia del último evento del patrón.
    */
   int[] getUltimaEncontrada(int numHilo);

   int getSoporte(int numHilo);
}
