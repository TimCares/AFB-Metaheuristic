
public class Logger {
  static boolean printLogs = true;

  public static void log(String msg) {
    if (Logger.printLogs) System.out.println(msg);
  }

  public static void logTour(int[] tour, double[][] tsp) {
    if (Logger.printLogs) {
      for (int i=0; i<tour.length; i++) {
          System.out.print(tour[i]);
          if (i != tour.length-1) {
              System.out.print(" --(" + (tsp[tour[i]][tour[i + 1]]) + ")-> ");
          }
          /*if ((i%20)==0 && i!=0) {
              System.out.println();
          }*/
      }
      System.out.println();
    }
  }
}
