import java.math.RoundingMode;
import java.text.DecimalFormat;

public class Main {
    public static void main(String[] args) {
        // Pa561 optimal route => 2763
        //String xmlFilePath = "../data/pa561/pa561.xml";
        //String xmlFilePath = "../data/pa561/pa561.xml";
        String xmlFilePath = "../data/eil101/eil101.xml";
        //String xmlFilePath = "../data/rl5934/rl5934.xml";
        //double[][] tsp = TSPLoader.readTSP("../data/tsp_3.txt");
        //double[][] tsp = createRandomTSP(10);
        double[][] tsp = TSPLoader.generateTSPMatrix(xmlFilePath);

        /*
        for (double[] row : tsp) {
            for (double cost : row) {
                System.out.printf("%10f ", cost);
            }
            System.out.println();
        }*/

        System.out.println();

        System.out.println("Solving...");

        AFB<int[]> solver = new AFB_TSP(200, 0.01, 0.67, 0.07, 1.00, 1000, tsp);

        int repeat = 1;
        double times = 0;
        double distance = 0;
        AFBResult<int[]> res = null;
        for (int i=0; i<repeat; i++) {
            long start = System.currentTimeMillis();
            res = solver.solve();
            times += (System.currentTimeMillis() - start) / 1000F;
            distance += res.bestCost;
        }

        int[] tour = res.bestPosition;
        for (int i=0; i<tour.length; i++) {
            System.out.print(tour[i]);
            if (i != tour.length-1) {
                System.out.print(" -> ");
            }
            if ((i%20)==0 && i!=0) {
                System.out.println();
            }
        }
        DecimalFormat df = new DecimalFormat("#.####");
        df.setRoundingMode(RoundingMode.CEILING);
        System.out.println();
        System.out.println("Distance: " + distance/repeat);
        System.out.println("Time: " + df.format(times/repeat) + " seconds");
    }

}
