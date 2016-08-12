package source.modelo;

public interface IAsociacionConRepeticion extends IAsociacionTemporal {
       String INFIX = "_";

       int[] getIndices();
       int[] getRep();
}
