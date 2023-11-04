import result_src.AFBResult;

import java.util.Arrays;
import java.util.Random;

public class Test {
    public static void testWalkSwap() {
        double[][] tsp = TSPLoader.createRandomTSP(2);
        Random rand = new Random(42);
        AFB_TSP solver = new AFB_TSP_Swap(4, 0.01, 0.67, 0.07, 1.00, 1000, tsp, rand, 0.25);
        solver.init();
        int[] oldPosition = solver.birds.get(0).position.clone();
        solver.walk(0);
        int[] newPosition = solver.birds.get(0).position.clone();
        
        assert oldPosition[0] == newPosition[1];
        assert oldPosition[1] == newPosition[0];
    }

    public static void testIterationCountChangesDistance() {
        //String xmlFilePath = "./data/pa561/pa561.xml";
        String xmlFilePath = "./data/eil101/eil101.xml";
        double[][] tsp = TSPLoader.generateTSPMatrix(xmlFilePath);
        double optimalDistance = 629.0; // eil101
        //double optimalDistance = 2763.0; // pa561
        //double[][] tsp = TSPLoader.readTSP("../data/tsp_3.txt");
        assert tsp != null;

        Random rand = new Random(42);

        int birdCount = 3;
        double smallBirdRatio = 0.6979749881176104;
        double probMoveRandom = 0.1589684022681154;
        double probMoveBest = 0.4624556400235943;
        double probMoveJoin = 0.33611898159023834;

        Logger.printLogs = false;
        int lowIters = 200;
        AFB<int[]> solverLowIter = new AFB_TSP(
            birdCount,
            probMoveRandom,
            probMoveBest,
            probMoveJoin,
            smallBirdRatio,
            lowIters,
            tsp,
            rand,
            0.25
        );
        solverLowIter.init();
        AFBResult<int[]> resLowIter = solverLowIter.solve();

        int highIters = 2000000;
        AFB<int[]> solverHighIter = new AFB_TSP(
            birdCount,
            probMoveRandom,
            probMoveBest,
            probMoveJoin,
            smallBirdRatio,
            highIters,
            tsp,
            rand,
            0.25
        );
        solverHighIter.init();
        AFBResult<int[]> resHighIter = solverHighIter.solve();

        Logger.log("Low iteration best route:");
        Logger.logTour(resLowIter.bestPosition, tsp);
        Logger.log("High iteration best route:");
        Logger.logTour(resHighIter.bestPosition, tsp);
        Logger.printLogs = true;

        Logger.log("Distance " + resLowIter.bestCost + " after " + lowIters + " iterations.");
        Logger.log((resLowIter.bestCost / optimalDistance - 1.0) * 100.0 + " percent longer than optimum (" + optimalDistance + ").");
        Logger.log("Distance " + resHighIter.bestCost + " after " + highIters + " iterations.");
        Logger.log((resHighIter.bestCost / optimalDistance - 1.0) * 100.0 + " percent longer than optimum (" + optimalDistance + ").");

        // Different iteration counts should not find the same route
        assert !Arrays.equals(resLowIter.bestPosition, resHighIter.bestPosition);
        // More iterations should find a shorter route than fewer iterations.
        assert resLowIter.bestCost > resHighIter.bestCost;
        // The route can't be shorter than the optimal route.
        assert resLowIter.bestCost >= optimalDistance;
        assert resHighIter.bestCost >= optimalDistance;
        // The high iteration route should be at most 50% longer than the optimum.
        assert (resHighIter.bestCost / optimalDistance - 1.0) < 0.5;
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
