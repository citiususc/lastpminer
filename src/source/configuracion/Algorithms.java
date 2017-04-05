package source.configuracion;

public enum Algorithms {
   ALG_ASTP,
   ALG_HSTP,
   ALG_LAZY, //
   ALG_IM , //Interval marking
   ALG_PAR, //ASTP paralelo
   ALG_HPAR, //HSTP paralelo
   ALG_CON, //ASTP concurrente
   ALG_HCON, //HSTP concurrente
   ALG_IM2, //solo interval marking de pares
   ALG_WM, // window marking
   ALG_IMS, //interval marking sin clases privadas (+ info en doc de clase)
   ALG_OM, //occurence marking en ASTP
   ALG_EXP,
   ALG_ANOT,
   ALG_SAV,
   ALG_SAV4,
   ALG_HOM, //occurence marking en HSTP
   ALG_SAVEXP,
   ALG_MARK,
   ALG_SUPER,
   ALG_LESS,
   ALG_SMEXP,
   ALG_SMSAVEXP,
   ALG_MARKT, //lastpminer
   ALG_SASTP, //astp con supermodelo
   ALG_TSTP, //tree sin anotaciones
   ALG_MAN, //busqueda manual
   ALG_NEG_POS,
   ALG_NEGC,
   ALG_ASTPI, //episodios incompletos
   ALG_LASTP
}
