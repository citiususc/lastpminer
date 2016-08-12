package source.busqueda;


public class GeneradorPatronesTest extends GeneradorPatrones {

   public GeneradorPatronesTest(int tam, AbstractMine mine) {
      super(tam, mine);
   }


   public void testDescartados(){
      Mine mine = new Mine("asdf", false, false, null, false);
      GeneradorPatronesTest gen = new GeneradorPatronesTest(4, mine);
      //TODO
   }
}
