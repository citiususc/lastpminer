package source.modelo.clustering;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.*;
import source.AllThatYouNeedSinteticas;
import source.configuracion.ConfigurationParameters;
import source.configuracion.HelperConfiguration;
import source.modelo.IAsociacionConDistribucion;
import source.modelo.IAsociacionTemporal;
import source.restriccion.RIntervalo;

public class DensityBasedClusteringTest {
   @Test
   public void test(){
      DensityBasedClustering clust = new DensityBasedClustering();
      HelperConfiguration.setConfiguration(new ConfigurationParameters(), clust);


      int freqs3[] = { 0, 265, 219, 216, 220, 232, 227, 231, 229, 212, 232, 241,
            234, 204, 231, 232, 229, 247, 264, 254, 272, 233, 275, 272, 260, 285,
            272, 250, 281, 345, 321, 331, 300, 344, 299, 301, 301, 300, 308, 306,
            291, 279, 276, 282, 291, 300, 276, 296, 310, 274, 268, 242, 253, 262,
            245, 253, 205, 184, 175, 135, 125, 147, 151, 112, 85, 86, 89, 65, 85,
            102, 95, 98, 125, 152, 152, 240, 416, 479, 528, 1192, 1985, 852, 452,
            485, 394, 299, 261, 245, 148, 150, 130, 132, 126, 104, 97, 106, 105,
            131, 135, 141, 155, 176, 188, 202, 213, 228, 245, 249, 266, 240, 258,
            262, 241, 276, 255, 272, 271, 270, 281, 278, 257, 301, 296, 300, 281,
            303, 297, 324, 304, 320, 287, 265, 273, 274, 275, 294, 276, 271, 264,
            280, 255, 268, 247, 231, 263, 261, 246, 236, 232, 260, 217, 225, 239,
            274, 215, 216, 239, 260, 242, 232, 0};

      int freqs5[] = {0, 218, 194, 207, 172, 202, 213, 185, 205, 198, 197, 227, 227,
            200, 223, 227, 243, 257, 261, 230, 270, 274, 267, 289, 300, 276, 298, 266,
            258, 228, 244, 259, 241, 229, 244, 215, 239, 223, 210, 229, 216, 199, 205,
            207, 202, 202, 226, 221, 216, 199, 247, 215, 201, 203, 215, 187, 195, 192,
            189, 168, 160, 170, 192, 181, 199, 193, 184, 206, 184, 225, 200, 229, 198,
            231, 241, 280, 254, 321, 359, 333, 369, 347, 408, 391, 426, 453, 455, 449,
            439, 369, 316, 269, 223, 190, 168, 120, 110, 81, 75, 43, 32, 31, 57, 90, 72,
            97, 120, 143, 192, 201, 211, 200, 208, 225, 254, 278, 305, 330, 330, 378,
            373, 340, 313, 341, 340, 301, 267, 254, 268, 256, 248, 248, 240, 225, 210,
            255, 220, 223, 224, 225, 207, 215, 209, 200, 203, 218, 208, 208, 197, 200,
            207, 197, 202, 208, 224, 223, 225, 209, 215, 222, 0};

      int freqPrueba[] = {0,22,23,30,31,28,33,31, 40,60,38,35,0};


      List<RIntervalo> intervals = clust.agrupar(freqs3, "a", "b");
      System.out.println("\n" +intervals);

      intervals = clust.agrupar(freqs5, "a", "b");
      System.out.println("\n" +intervals);


      intervals = clust.agrupar(freqPrueba, "a", "b");
      System.out.println("\n" +intervals);
   }


   @Test
   public void clusteringBDRo15Win4(){
      int freqs[][] = {
         {0, 1034, 1728, 2099, 2371, 2033, 2039, 1835, 0},
         {0, 1907, 1955, 2222, 2362, 1913, 2238, 1745, 0},
         {0, 1174, 1175, 1355, 1787, 1214, 1244, 1347, 0},
         {0, 1182, 1143, 1423, 1718, 1237, 1275, 1293, 0},
         {0, 1955, 2019, 2307, 2172, 2154, 1972, 2041, 0},
         {0, 1989, 2131, 2127, 2274, 2296, 1689, 2523, 0},
         {0, 1847, 1873, 2023, 2364, 2054, 325, 7400, 0},
         {0, 1901, 1978, 2342, 2075, 2116, 2054, 1983, 0},
         {0, 1946, 1917, 1857, 2095, 4663, 1886, 1822, 0},
         {0, 2479, 2348, 3670, 1536, 1986, 2079, 2010, 0},
         {0, 1932, 1829, 1998, 2961, 4510, 974, 1927, 0},
         {0, 1060, 4308, 2829, 2383, 1729, 1976, 1997, 0},
         {0, 1170, 1205, 1377, 1718, 1157, 1213, 1257, 0},
         {0, 1162, 1177, 1383, 1742, 1169, 1169, 1327, 0},
         {0, 2411, 3972, 2401, 2018, 2010, 1952, 2003, 0},
         {0, 4283, 2868, 2007, 2183, 1957, 1959, 2163, 0},
         {0, 2081, 1964, 2202, 2201, 2046, 1926, 2047, 0},
         {0, 2442, 3014, 2913, 2322, 1919, 1941, 2090, 0},
         {0, 2035, 1834, 2160, 2118, 2145, 2045, 1898, 0},
         {0, 2049, 1892, 2182, 2284, 1641, 2881, 4326, 0},
         {0, 2024, 1828, 2270, 2089, 2175, 1967, 1900, 0},
         {0, 1126, 1370, 1134, 1698, 1441, 1272, 1213, 0},
         {0, 1194, 1305, 1068, 1805, 1365, 1291, 1244, 0},
         {0, 1917, 1118, 4252, 3873, 1354, 2015, 1975, 0},
         {0, 1959, 372, 7521, 552, 2155, 2023, 1976, 0},
         {0, 1990, 2060, 2278, 2195, 2330, 1960, 2038, 0},
         {0, 1972, 1245, 3510, 3571, 2408, 1660, 2021, 0},
         {0, 2076, 1967, 2186, 2090, 2326, 2102, 1945, 0},
         {0, 1936, 1927, 2283, 2277, 1905, 1953, 2778, 0},
         {0, 2071, 2020, 2117, 2077, 2368, 2097, 1901, 0},
         {0, 726, 749, 777, 1270, 821, 713, 756, 0},
         {0, 1247, 1291, 1664, 1271, 1483, 1190, 1201, 0},
         {0, 1242, 1392, 1466, 1379, 1433, 1143, 1255, 0},
         {0, 1188, 1174, 1286, 1660, 1368, 1195, 1386, 0},
         {0, 1246, 1198, 1533, 1402, 1445, 1248, 1198, 0},
         {0, 1160, 1266, 1308, 1466, 1560, 1256, 1199, 0},
         {0, 1320, 1207, 1402, 1692, 1163, 1230, 1389, 0},
         {0, 1248, 1217, 1434, 1430, 1549, 1265, 1206, 0},
         {0, 1214, 1299, 1627, 1307, 1404, 1139, 1247, 0},
         {0, 1352, 1375, 1474, 1413, 1367, 1155, 1246, 0},
         {0, 1172, 1191, 1199, 1667, 1325, 1232, 1366, 0},
         {0, 1267, 1192, 1576, 1377, 1398, 1179, 1245, 0},
         {0, 1240, 1278, 1241, 1484, 1503, 1297, 1243, 0},
         {0, 1278, 1175, 1417, 1735, 1131, 1255, 1336, 0},
         {0, 1228, 1296, 1411, 1352, 1526, 1336, 1182, 0},
         {0, 2043, 1243, 3664, 4528, 1162, 2030, 2045, 0},
         {0, 2058, 1949, 2372, 2137, 2358, 2084, 2129, 0},
         {0, 1989, 1671, 2373, 3704, 2927, 2013, 1844, 0},
         {0, 2131, 1998, 2295, 1914, 2434, 2099, 1986, 0},
         {0, 1919, 1920, 2309, 2165, 2146, 1757, 2385, 0},
         {0, 2067, 2047, 2324, 1848, 2455, 2130, 1993, 0},
         {0, 2076, 1921, 2124, 2696, 1907, 2324, 2141, 0},
         {0, 2037, 1934, 1421, 3802, 3354, 2357, 1692, 0},
         {0, 2004, 2222, 1995, 2354, 2097, 2208, 2036, 0},
         {0, 1921, 1967, 2046, 2536, 1849, 2047, 2127, 0},
         {0, 2107, 2197, 2072, 2219, 2121, 2250, 2112, 0},
         {0, 2000, 2016, 2312, 2160, 2364, 2018, 2014, 0},
         {0, 1743, 4641, 1948, 1995, 2196, 2004, 1961, 0},
         {0, 1231, 2008, 2294, 2234, 1923, 1950, 1995, 0},
         {0, 2688, 4513, 1057, 2102, 2324, 1890, 1903, 0},
         {0, 2187, 1953, 2320, 1906, 2350, 2057, 2028, 0},
         {0, 1917, 1919, 2281, 2201, 2047, 1978, 2642, 0},
         {0, 2119, 2018, 2309, 1809, 2449, 2102, 2000, 0},
         {0, 2942, 2826, 1659, 2139, 2087, 1930, 2009, 0},
         {0, 1893, 1877, 2819, 4593, 1120, 1981, 1946, 0},
         {0, 2031, 2012, 2099, 2176, 1072, 4285, 2850, 0}
      };
      ConfigurationParameters params = new ConfigurationParameters();
      DensityBasedClustering clust = new DensityBasedClustering();
      HelperConfiguration.setConfiguration(params, clust);

      List<RIntervalo> intervals;
      boolean mal = false;
      for(int[] freq: freqs){
         intervals = clust.agrupar(freq, "a", "b");
         //System.out.println("\n" +intervals);
         //assertFalse("Se solapan los intervalos " + intervals + " en la BBDD " + bbdd[i], solapan(intervals) );
         if(solapan(intervals) ){
            System.out.println("Se solapan los intervalos " + intervals + " de " + Arrays.toString(freq));
            mal = true;
         }
      }
      assertFalse(mal);
   }

   @Test public void agruparTest1(){
      int freq[] = {0, 1034, 1728, 2099, 2371, 2033, 2039, 1835, 0};
      ConfigurationParameters params = new ConfigurationParameters();
      DensityBasedClustering clust = new DensityBasedClustering();
      HelperConfiguration.setConfiguration(params, clust);
      List<RIntervalo> rests = clust.agrupar(freq, "a", "b");
      assertEquals(Arrays.asList(new RIntervalo("a","b",-3,3)),
            rests);
   }

   @Test public void agruparTest2(){
      int freq[] = {0, 2688, 4513, 1057, 2102, 2324, 1890, 1903, 0};
      ConfigurationParameters params = new ConfigurationParameters();
      DensityBasedClustering clust = new DensityBasedClustering();
      HelperConfiguration.setConfiguration(params, clust);
      List<RIntervalo> rests = clust.agrupar(freq, "a", "b");
      assertEquals(Arrays.asList(new RIntervalo("a","b",-3,-2), new RIntervalo("a","b",-1,3)),
            rests);
   }


   @Test public void clusteringSynteticasNoSolapa(){
      //String[] bbdd = { "BD4", "BD5", "BD6", "BD7", "BDR56", "BDR57", "BDRoE6", "BDRoE9", "BDRoE15"};
      //int windows[] = { 120, 80, 80, 80, 80, 80, 80, 40, 20, 10 };

      String[] bbdd = { "BDRoE15"}; int windows[] = { 4 };

      boolean mal = false;

      for(int i=0, x=bbdd.length; i<x;i++){
         AllThatYouNeedSinteticas capsula = new AllThatYouNeedSinteticas(bbdd[i]);
         System.out.println("Simple name: " + DensityBasedClustering.class.getSimpleName());
         capsula.params.setClusteringClassName(DensityBasedClustering.class.getName());
         capsula.params.setWindowSize(windows[i]);
         capsula.minarDistribuciones();

         DensityBasedClustering clust = new DensityBasedClustering();
         HelperConfiguration.setConfiguration(capsula.params, clust);

         List<RIntervalo> intervals;
         for(IAsociacionTemporal asoc: capsula.distribuciones()){
            int[] distr = ((IAsociacionConDistribucion)asoc).getDistribucion();
            System.out.println(Arrays.toString(distr));
            intervals = clust.agrupar(distr, asoc.getTipos()[0], asoc.getTipos()[1]);
            //System.out.println("\n" +intervals);
            //assertFalse("Se solapan los intervalos " + intervals + " en la BBDD " + bbdd[i], solapan(intervals) );
            if(solapan(intervals) ){
               System.out.println("Se solapan los intervalos " + intervals + " en la BBDD " + bbdd[i]);
               mal = true;
            }
         }
      }
      assertFalse(mal);
   }

   private boolean solapan(List<RIntervalo> intervals){
      for(int i=0, x=intervals.size(); i<x-1;i++){
         if(solapan(intervals.get(i), intervals.get(i+1))){
            return true;
         }
      }
      return false;
   }

   private boolean solapan(RIntervalo a, RIntervalo b){
      if(a.getFin()>=b.getInicio() && a.getFin()<=b.getFin()){
         return true;
      }
      if(a.getInicio()>=b.getInicio() && a.getInicio()<=b.getFin()){
         return true;
      }
      if(a.getInicio()<b.getInicio() && a.getFin()>b.getFin()){
         return true;
      }
      return false;
   }
}
