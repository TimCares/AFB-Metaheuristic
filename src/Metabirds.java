import result_src.AFBResult;
import java.util.stream.IntStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.hsh.parser.Dataset;
import com.hsh.parser.Parser;

class AFBParams {
    public int n_birds;
    public double smallBirdRatio;
    public double probMoveRandom;
    public double probMoveBest;
    public double probMoveJoin;
    public double joinTop;

    public AFBParams(
            int n_birds,
            double smallBirdRatio,
            double probMoveRandom,
            double probMoveBest,
            double probMoveJoin,
            double joinTop) {
        this.n_birds = n_birds;
        this.smallBirdRatio = smallBirdRatio;
        this.probMoveRandom = probMoveRandom;
        this.probMoveBest = probMoveBest;
        this.probMoveJoin = probMoveJoin;
        this.joinTop = joinTop;
    }

    public AFBParams(AFBParams other) {
        this.n_birds = other.n_birds;
        this.smallBirdRatio = other.smallBirdRatio;
        this.probMoveRandom = other.probMoveRandom;
        this.probMoveBest = other.probMoveBest;
        this.probMoveJoin = other.probMoveJoin;
        this.joinTop = other.joinTop;
    }

    public boolean isInvalid() {
        if (this.probMoveRandom + this.probMoveBest + this.probMoveJoin > 1.0) {
            Logger.debug("Prob sum invalid");
            return true;
        }
        if (this.n_birds < 10) {
            Logger.debug("Bird count invalid");
            return true;
        }
        if (((int) (this.joinTop * this.n_birds)) < 1) {
            Logger.debug("Top join invalid: " + this.joinTop);
            return true;
        }
        return false;
    }
}

public class Metabirds extends AFB<AFBParams> {
    static final double stepSize = 0.03;
    static final double stepSizeBirdCount = 2.00;

    static final int metabirdsNestedIters = 30_000_000;
    // How many runs are averaged for a single metabirds cost calculation
    static final int metabirdsAverageTries = 8;
    static final String problemPath = "./data/tsp/eil101.tsp";

    static final int birdCountLimit = 1000;

    double[][] testTSP;

    public Metabirds(
            int n_birds,
            double probMoveRandom,
            double probMoveBest,
            double probMoveJoin,
            double smallBirdRatio,
            int max_iters,
            Random rand) {
        super(n_birds, probMoveRandom, probMoveBest, probMoveJoin, smallBirdRatio, max_iters, rand);
    }

    @Override
    protected void init() {
        String filePath = problemPath;
        Dataset dataset = null;
        try {
            dataset = Parser.read(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.testTSP = TSPLoader.generateTSPFromNodes(dataset.getNodes());
        this.birds = new ArrayList<Bird<AFBParams>>(this.n_birds);

        for (int birdIndex = 0; birdIndex < this.n_birds; birdIndex++) {
            Bird<AFBParams> newBird = new Bird<AFBParams>(
                    new AFBParams(
                            rand.nextInt(birdCountLimit) + 2,
                            rand.nextDouble(),
                            rand.nextDouble(),
                            rand.nextDouble(),
                            rand.nextDouble(),
                            rand.nextDouble()),
                    0.0,
                    new AFBParams(
                            rand.nextInt(birdCountLimit) + 2,
                            rand.nextDouble(),
                            rand.nextDouble(),
                            rand.nextDouble(),
                            rand.nextDouble(),
                            rand.nextDouble()),
                    0.0,
                    false,
                    BirdMove.FlyRandom);
            this.birds.add(newBird);
            fly(birdIndex);
            newBird.lastMove = BirdMove.FlyRandom;
            cost(birdIndex);
            newBird.bestPosition = clone(newBird.position);
            newBird.bestCost = newBird.cost;
            newBird.isBigBird = rand.nextDouble() > this.smallBirdRatio;
        }
    }

    @Override
    AFBParams clone(AFBParams old) {
        return new AFBParams(old);
    }

    @Override
    void walk(int birdIndex) {
        Bird<AFBParams> bird = this.birds.get(birdIndex);
        AFBParams birdParams;

        int invalidCount = 0;
        do {
            Logger.debug("Invalid count: " + invalidCount);
            birdParams = new AFBParams(bird.position);
            birdParams.n_birds += Math.round(rand.nextGaussian() * this.stepSizeBirdCount);
            birdParams.n_birds = (int) Math.min(birdParams.n_birds, 10.0);
            birdParams.smallBirdRatio += rand.nextGaussian() * this.stepSize;
            birdParams.smallBirdRatio = Math.min(Math.max(birdParams.smallBirdRatio, 0.0), 1.0);
            birdParams.probMoveRandom += rand.nextGaussian() * this.stepSize;
            birdParams.probMoveRandom = Math.min(Math.max(birdParams.probMoveRandom, 0.0), 1.0);
            birdParams.probMoveBest += rand.nextGaussian() * this.stepSize;
            birdParams.probMoveBest = Math.min(Math.max(birdParams.probMoveBest, 0.0), 1.0);
            birdParams.probMoveJoin += rand.nextGaussian() * this.stepSize;
            birdParams.probMoveJoin = Math.min(Math.max(birdParams.probMoveJoin, 0.0), 1.0);
            birdParams.joinTop += rand.nextGaussian() * 1.0 * this.stepSize;
            birdParams.joinTop = Math.min(Math.max(birdParams.probMoveJoin, 1.0 / (double) birdParams.n_birds),
                    1.0);
            invalidCount++;
        } while (birdParams.isInvalid());

        bird.position = birdParams;
    }

    @Override
    void fly(int birdIndex) {
        Bird<AFBParams> bird = this.birds.get(birdIndex);
        AFBParams birdParams;

        int invalidCount = 0;
        do {
            Logger.debug("Invalid count: " + invalidCount);
            birdParams = new AFBParams(bird.position);
            birdParams.n_birds = rand.nextInt(birdCountLimit) + 2;
            birdParams.smallBirdRatio = rand.nextDouble();
            birdParams.probMoveRandom = rand.nextDouble();
            birdParams.probMoveBest = rand.nextDouble();
            birdParams.probMoveJoin = rand.nextDouble();
            birdParams.joinTop = rand.nextDouble();
            invalidCount++;
        } while (birdParams.isInvalid());

        bird.position = birdParams;
    }

    String formatTime(double secondsRemaining) {
      return String.format("%d h %d min, %d sec",
                        TimeUnit.SECONDS.toHours((long) secondsRemaining),
                        TimeUnit.SECONDS.toMinutes((long) secondsRemaining) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours((long) secondsRemaining)),
                        TimeUnit.SECONDS.toSeconds((long) secondsRemaining) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes((long) secondsRemaining)));
    }

    @Override
    void cost(int birdIndex) {
        Bird<AFBParams> bird = this.birds.get(birdIndex);
        int max_iters = metabirdsNestedIters;


        int repetitions = metabirdsAverageTries;
        // Use a parallel stream to run the nested solver in parallel.
        double cost = IntStream.range(0, repetitions).parallel().mapToDouble(i -> {
           AFB_TSP solver = new AFB_TSP_TopN_Opt3_S_NN(
                  bird.position.n_birds,
                  bird.position.probMoveRandom,
                  bird.position.probMoveBest,
                  bird.position.probMoveJoin,
                  bird.position.smallBirdRatio,
                  max_iters,
                  this.testTSP,
                  this.rand,
                  bird.position.joinTop
            );

            long nestedStartTime = System.currentTimeMillis();
            solver.init();
            Logger.debug("Beginning nested solve.");
            AFBResult<int[]> result = solver.solve();
            double nestedRuntime = (System.currentTimeMillis() - nestedStartTime) / 1000F;
            Logger.debug("Nested solve done");

            MetabirdsMain.currMetabirdCostEvals++;
            long timeSoFar = System.currentTimeMillis() - MetabirdsMain.startTime;
            long msPerEval = timeSoFar / MetabirdsMain.currMetabirdCostEvals;
            long evalsRemaining = MetabirdsMain.totalMetabirdCostEvals - MetabirdsMain.currMetabirdCostEvals;
            double secondsRemaining = msPerEval * evalsRemaining / 1000F;
            Logger.info(MetabirdsMain.currMetabirdCostEvals + " / " + MetabirdsMain.totalMetabirdCostEvals + " took " + formatTime(nestedRuntime) + " time remaining: " + formatTime(secondsRemaining) + " found tour with length " + result.bestCost);

            return result.bestCost / (double) repetitions;
        }).sum();
        bird.cost = cost;
        Logger.debug("Meta cost done");
    }

}
