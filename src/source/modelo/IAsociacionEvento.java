package source.modelo;

public interface IAsociacionEvento extends IAsociacionTemporal{

   /**
    * Para obtener directamente el tipo de evento de la asociación
    * de tamaño 1
    * @return El tipo de evento de la asociación, positivo o negativo
    * según corresponda
    */
   String getTipoEvento();

}
