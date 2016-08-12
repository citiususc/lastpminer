package source.modelo;

import source.modelo.arbol.NodoAntepasadosAnotado;

public interface IAsociacionArbol extends IAsociacionTemporal{
   NodoAntepasadosAnotado getNodo();
   void setNodo(NodoAntepasadosAnotado nodo);
}
