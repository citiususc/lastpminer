package source.modelo.repetidos;

import org.junit.Assert;
import org.junit.Test;

import source.modelo.IAsociacionConRepeticion;

public class ModeloAuxiliarRepeticionTest {

    @Test
    public void testToStringSinPatrones(){
        String infix = IAsociacionConRepeticion.INFIX;
        ModeloAuxiliarRepeticion mod = new  ModeloAuxiliarRepeticion(new String[]{"A","A","B","C","D"}, 60, null, new int[]{1,2,0,0,2});
        Assert.assertEquals("[A"+infix+"1, A"+infix+"2, B, C, D"+infix+"2]", mod.toStringSinPatrones());
    }
}
