import java.util.Arrays;
import java.util.Random;

public class Test {
    public static void testWalkSwap() {
        double[][] tsp = TSPLoader.createRandomTSP(2);
        Random rand = new Random(42);
        AFB_TSP solver = new AFB_TSP_Swap(4, 0.01, 0.67, 0.07, 1.00, 1000, tsp, rand);
        solver.init();
        int[] oldPosition = solver.birds.get(0).position.clone();
        solver.walk(0);
        int[] newPosition = solver.birds.get(0).position.clone();
        
        assert oldPosition[0] == newPosition[1];
        assert oldPosition[1] == newPosition[0];
    }

    public static void testIterationCountChangesDistance() {
        String xmlFilePath = "./data/eil101/eil101.xml";
        double[][] tsp = TSPLoader.generateTSPMatrix(xmlFilePath);
        assert tsp != null;
        //double[][] tsp = TSPLoader.readTSP("../data/tsp_3.txt");

        Random rand = new Random(42);

        int birdCount = 2000;
        int lowIters = 100;
        AFB<int[]> solverLowIter = new AFB_TSP_Swap(
            birdCount,
            0.1,
            0.01,
            0.01,
            0.60,
            lowIters,
            tsp,
            rand
        );
        solverLowIter.init();
        AFBResult<int[]> resLowIter = solverLowIter.solve();

        int highIters = 2000;
        AFB<int[]> solverHighIter = new AFB_TSP_Swap(
            birdCount,
            0.1,
            0.01,
            0.01,
            0.60,
            highIters,
            tsp,
            rand
        );
        solverHighIter.init();
        AFBResult<int[]> resHighIter = solverHighIter.solve();

        Logger.log("Low iteration best route:");
        Logger.logTour(resLowIter.bestPosition, tsp);
        Logger.log("High iteration best route:");
        Logger.logTour(resHighIter.bestPosition, tsp);

        Logger.log("Distance " + resLowIter.bestCost + " after " + lowIters + " iterations");
        Logger.log("Distance " + resHighIter.bestCost + " after " + highIters + " iterations");

        // Different iteration counts should not find the same route
        assert !Arrays.equals(resLowIter.bestPosition, resHighIter.bestPosition);
        // More iterations should find a shorter route than fewer iterations.
        assert resLowIter.bestCost > resHighIter.bestCost;
    }

    // Test the function reversInRange
    public static void testReverseInRange() {
        int[] arr = {0, 1, 2, 3, 4, 5};
        int[] expected = {0, 1, 3, 2, 4, 5};
        AFB_TSP.reverseInRange(arr, 2, 4);
        assert Arrays.equals(arr, expected);
    }

    public static void main(String[] args) {
        Logger.printLogs = true;
        
        testReverseInRange();
        testWalkSwap();
        testIterationCountChangesDistance();
    }
}
