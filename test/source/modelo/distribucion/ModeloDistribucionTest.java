package source.modelo.distribucion;

import org.junit.Test;

import source.evento.Evento;

public class ModeloDistribucionTest {

	
	@Test
	public void buscaOcurrenciaTam2Test(){
		ModeloDistribucion mod = new ModeloDistribucion(new String[]{"A", "B"}, 10, 0,null);
	
		mod.recibeEvento(0, new Evento("A",2), false);
		mod.recibeEvento(0, new Evento("A",3), false);
		mod.recibeEvento(0, new Evento("B",3), false);
		
		mod.recibeEvento(0, new Evento("A",5), false);
		mod.recibeEvento(0, new Evento("B",7), false);
		
		mod.getDistribucion();
	}
}
