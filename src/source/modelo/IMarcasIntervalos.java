package source.modelo;

/*
 *   Interfaz que define los métodos que cualquier asociación temporal que permita eliminar eventos
 * de la colección mediante uso de marcas de intervalos debe implementar.
 */
public interface IMarcasIntervalos extends IAsociacionTemporal{

   /**
    *
    * @return int[] de 2 posiciones. El primero es el instante de ocurrencia del evento
    * con valor más bajo que pertenece a una ocurrencia de algún patrón de la asociación,
    * y el segundo el instante de ocurrencia del último evento del patrón.
    */
   int[] getUltimaEncontrada();

}
