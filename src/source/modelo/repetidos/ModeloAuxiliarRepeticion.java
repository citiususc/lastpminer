package source.modelo.repetidos;

import source.modelo.IAsociacionConRepeticion;
import source.modelo.ModeloAsociacion;

/**
 * En tipos se guarda la lista de los tipos de eventos con los repetidos, por ejemplo A,A,B,C,D,D
 * y en índices los índices de los repetidos por ejemplo 1,2,0,0,0,1 esta sería la asociación
 * auxiliar A1,A2,B,C,D,D1.
 *
 * @author vanesa.graino
 *
 */
public class ModeloAuxiliarRepeticion extends ModeloAsociacion implements IAsociacionConRepeticion {
    private static final long serialVersionUID = -2216472859352736162L;

    /*
     * Atributos
     */

    protected int[] indices;

    /*
     * Constructores
     */

    public ModeloAuxiliarRepeticion(String[] tipos, int ventana, Integer frecuencia, int[] indices) {
        super(tipos, ventana, frecuencia);
        this.indices = indices;
    }


    @Override
    public String toStringSinPatrones(){
        StringBuilder sb = new StringBuilder("[");
        int i = 0;
        for(String tipo : tipos){
            sb.append(tipo + (indices[i]>0 ? INFIX + indices[i] : "") + ", ");
            i++;
        }
        sb.replace(sb.length()-2, sb.length(),"]");
        return sb.toString();
    }


    @Override
    public int[] getIndices() {
        return indices;
    }


    @Override
    public int[] getRep() {
        throw new RuntimeException("No tiene sentido llamar a este método para " + ModeloAuxiliarRepeticion.class.getSimpleName());
    }
}
