import java.math.RoundingMode;
import java.text.DecimalFormat;

public class Main {
    public static void main(String[] args) {
        String xmlFilePath = "./data/a280.xml";
        //double[][] tsp = TSPLoader.readTSP("./data/tsp_3.txt");
        //double[][] tsp = createRandomTSP(10);
        double[][] tsp = TSPLoader.generateTSPMatrix(xmlFilePath);

        for (double[] row : tsp) {
            for (double cost : row) {
                System.out.printf("%10f ", cost);
            }
            System.out.println();
        }
        System.out.println();

        System.out.println("Solving...");

        AFB solver = new AFB(20, 0.01, 0.67, 0.07, 0.75, 10000, tsp);

        int repeat = 1;
        double times = 0;
        double distance = 0;
        Object[] res = null;
        for (int i=0; i<repeat; i++) {
            long start = System.currentTimeMillis();
            res = solver.solve();
            times += (System.currentTimeMillis() - start) / 1000F;
            distance += (double) res[1];
        }


        int[] tour = (int[]) res[0];
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