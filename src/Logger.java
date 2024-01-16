public class Logger {
  static LogLevel logLevel = LogLevel.INFO;

  public static void debug(String msg) {
    if (logLevel == LogLevel.DEBUG) {
      System.out.println("[DEBUG]: " + msg);
    }
  }

  public static void info(String msg) {
    if (logLevel == LogLevel.DEBUG || logLevel == LogLevel.INFO) {
      System.out.println("[INFO]: " + msg);
    }
  }

  public static void logTour(int[] tour, double[][] tsp, LogLevel level) {
      for (int i = 0; i < tour.length; i++) {
        System.out.print(tour[i]);
        if (i != tour.length - 1) {
          System.out.print(" --(" + (tsp[tour[i]][tour[i + 1]]) + ")-> ");
        }
        /*
         * if ((i%20)==0 && i!=0) {
         * System.out.println();
         * }
         */
      }
      System.out.println();
  }
}
