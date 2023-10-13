import java.util.ArrayList;
import java.util.Random;

class AFBParams {
    public double smallBirdRatio;
    public double probMoveRandom;
    public double probMoveBest;
    public double probMoveJoin;
    
    public AFBParams(
        double smallBirdRatio,
        double probMoveRandom,
        double probMoveBest,
        double probMoveJoin
    ) {
        this.smallBirdRatio = smallBirdRatio;
        this.probMoveRandom = probMoveRandom;
        this.probMoveBest = probMoveBest;
        this.probMoveJoin = probMoveJoin;
    }

    public AFBParams(AFBParams other) {
        this.smallBirdRatio = other.smallBirdRatio;
        this.probMoveRandom = other.probMoveRandom;
        this.probMoveBest = other.probMoveBest;
        this.probMoveJoin = other.probMoveJoin;
    }

    public boolean isInvalid() {
        return (this.probMoveRandom + this.probMoveBest + this.probMoveJoin > 1.0);
    }
}

public class Metabirds extends AFB<AFBParams> {
    final double stepSize = 0.05;
    double[][] testTSP;
    
    public Metabirds(
        int n_birds,
        double probMoveRandom,
        double probMoveBest,
        double probMoveJoin,
        double smallBirdRatio,
        int max_iters,
        Random rand
    ) {
        super(n_birds, probMoveRandom, probMoveBest, probMoveJoin, smallBirdRatio, max_iters, rand);
    }
    
    @Override
    void init() {
        String xmlFilePath = "./data/eil101/eil101.xml";
        this.testTSP = TSPLoader.generateTSPMatrix(xmlFilePath);

        this.birds = new ArrayList<Bird<AFBParams>>(this.n_birds);

        for (int birdIndex=0; birdIndex<this.n_birds; birdIndex++) {
            Bird<AFBParams> newBird = new Bird<AFBParams>(
                new AFBParams(
                    rand.nextDouble(),
                    rand.nextDouble(),
                    rand.nextDouble(),
                    rand.nextDouble()
                ),
                0.0,
                new AFBParams(
                    rand.nextDouble(),
                    rand.nextDouble(),
                    rand.nextDouble(),
                    rand.nextDouble()
                ),
                0.0,
                false,
                BirdMove.FlyRandom
            );
            this.birds.add(newBird);
            fly(birdIndex);
            newBird.lastMove = BirdMove.FlyRandom;
            cost(birdIndex);
            newBird.bestPosition = clone(newBird.position);
            newBird.bestCost = newBird.cost;
            newBird.isBigBird = rand.nextDouble() > this.smallBirdRatio;
        }
        this.curr_iters = 0;
    }

    @Override
    AFBParams clone(AFBParams old) {
        return new AFBParams(old);
    }
    
    @Override
    void walk(int birdIndex) {
        Bird<AFBParams> bird = this.birds.get(birdIndex);
        AFBParams birdParams = bird.position;
        birdParams.smallBirdRatio += rand.nextGaussian() * this.stepSize;
        birdParams.smallBirdRatio = Math.min(Math.max(birdParams.smallBirdRatio, 0.0), 1.0);
        birdParams.probMoveRandom += rand.nextGaussian() * this.stepSize;
        birdParams.probMoveRandom = Math.min(Math.max(birdParams.probMoveRandom, 0.0), 1.0);
        birdParams.probMoveBest += rand.nextGaussian() * this.stepSize;
        birdParams.probMoveBest = Math.min(Math.max(birdParams.probMoveBest, 0.0), 1.0);
        birdParams.probMoveJoin += rand.nextGaussian() * this.stepSize;
        birdParams.probMoveJoin = Math.min(Math.max(birdParams.probMoveJoin, 0.0), 1.0);
        
        if (birdParams.isInvalid()) {
            walk(birdIndex);
        }
    }

    @Override
    void fly(int birdIndex) {
        Bird<AFBParams> bird = this.birds.get(birdIndex);
        AFBParams birdParams = bird.position;
        birdParams.smallBirdRatio = rand.nextDouble();
        birdParams.probMoveRandom = rand.nextDouble();
        birdParams.probMoveBest = rand.nextDouble();
        birdParams.probMoveJoin = rand.nextDouble();
        
        if (birdParams.isInvalid()) {
            fly(birdIndex);
        }
    }

    @Override
    void cost(int birdIndex) {
        Logger.log("[DEBUG]: Meta-Iteration: " + this.curr_iters);
        Bird<AFBParams> bird = this.birds.get(birdIndex);
        Logger.printLogs = false;
        int max_iters = 500;
        AFB_TSP solver = new AFB_TSP(
            this.n_birds,
            bird.position.probMoveRandom,
            bird.position.probMoveBest,
            bird.position.probMoveJoin,
            bird.position.smallBirdRatio,
            max_iters,
            this.testTSP,
            this.rand
        );
        
        int repetitions = 32;
        double cost = 0.0;
        for (int i = 0; i < repetitions; i++) {
            solver.init();
            AFBResult<int[]> result = solver.solve();
            cost += result.bestCost / repetitions;
        }
        bird.cost = cost;
        this.curr_iters++;
        Logger.printLogs = true;
    }
    
}
