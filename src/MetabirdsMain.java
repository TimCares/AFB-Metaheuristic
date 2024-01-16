import result_src.AFBResult;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Random;

public class MetabirdsMain {
    static final int metabirdCount = 4;
    static final int metabirdIters = 20;

    static int totalMetabirdCostEvals = 0;
    static int currMetabirdCostEvals = 0;

    static long startTime = 0;

    public static void main(String[] args) {
        Logger.info("Starting Metabirds:");
        Logger.info("Metabird count: " + metabirdCount);
        Logger.info("Metabird iterations: " + metabirdIters);
        Logger.info("Each with:");
        Logger.info(Metabirds.metabirdsAverageTries + " tries averaged");
        Logger.info("Running over " + Metabirds.metabirdsNestedIters + " iterations");
        Logger.info("Step size: " + Metabirds.stepSize);
        Logger.info("Bird count step size: " + Metabirds.stepSizeBirdCount);
        Logger.info("\n");
        totalMetabirdCostEvals = metabirdIters * Metabirds.metabirdsAverageTries;
        // Round to the next multiple of metabirdCount * Metabirds.metabirdsAverageTries
        // Because the loop doesn't stop until all metabirds have completed the run.
        int roundingFactor = metabirdCount * Metabirds.metabirdsAverageTries;
        totalMetabirdCostEvals += roundingFactor;
        totalMetabirdCostEvals -= totalMetabirdCostEvals % roundingFactor;

        Logger.info("Total TSP runs: " + totalMetabirdCostEvals);

        Random rand = new Random();
        rand.setSeed(42);

        // Distance: 686.5986551818119
        // Time: 379.291 seconds
        // Params:
        // n_birds: 3 // Interesting, this is the minimum!
        // smallBirdRatio: 0.6979749881176104
        // probMoveRandom: 0.1589684022681154
        // probMoveBest: 0.4624556400235943
        // probMoveJoin: 0.33611898159023834
        Metabirds metaSolver = new Metabirds(
                metabirdCount,
                0.5,
                0.05,
                0.1,
                0.7,
                metabirdIters,
                rand);
        startTime = System.currentTimeMillis();
        Logger.info("Beginning Metabird initialization");
        metaSolver.init();
        Logger.info("Metabird initialization complete");

        AFBResult<AFBParams> res = null;
        res = metaSolver.solve();
        double time = (System.currentTimeMillis() - startTime) / 1000F;

        DecimalFormat df = new DecimalFormat("#.####");
        df.setRoundingMode(RoundingMode.CEILING);
        System.out.println();
        System.out.println("Distance: " + res.bestCost);
        System.out.println("Time: " + df.format(time) + " seconds");
        System.out.println("Params: ");
        System.out.println("n_birds: " + res.bestPosition.n_birds);
        System.out.println("smallBirdRatio: " + res.bestPosition.smallBirdRatio);
        System.out.println("probMoveRandom: " + res.bestPosition.probMoveRandom);
        System.out.println("probMoveBest: " + res.bestPosition.probMoveBest);
        System.out.println("probMoveJoin: " + res.bestPosition.probMoveJoin);
        System.out.println("topJoin: " + res.bestPosition.joinTop);

    }

}
