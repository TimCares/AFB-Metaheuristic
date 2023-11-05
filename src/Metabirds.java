import result_src.AFBResult;

import java.util.ArrayList;
import java.util.Random;

class AFBParams {
    public int n_birds;
    public double smallBirdRatio;
    public double probMoveRandom;
    public double probMoveBest;
    public double probMoveJoin;
    
    public AFBParams(
        int n_birds,
        double smallBirdRatio,
        double probMoveRandom,
        double probMoveBest,
        double probMoveJoin
    ) {
        this.n_birds = n_birds;
        this.smallBirdRatio = smallBirdRatio;
        this.probMoveRandom = probMoveRandom;
        this.probMoveBest = probMoveBest;
        this.probMoveJoin = probMoveJoin;
    }

    public AFBParams(AFBParams other) {
        this.n_birds = other.n_birds;
        this.smallBirdRatio = other.smallBirdRatio;
        this.probMoveRandom = other.probMoveRandom;
        this.probMoveBest = other.probMoveBest;
        this.probMoveJoin = other.probMoveJoin;
    }

    public boolean isInvalid() {
        if (this.probMoveRandom + this.probMoveBest + this.probMoveJoin > 1.0) {
            return true;
        }
        if (this.n_birds < 3) {
            return true;
        }
        return false;
    }
}

public class Metabirds extends AFB<AFBParams> {
    final double stepSize = 0.01;
    final double stepSizeBirdCount = 1.00;
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
                    rand.nextInt(100) + 2,
                    rand.nextDouble(),
                    rand.nextDouble(),
                    rand.nextDouble(),
                    rand.nextDouble()
                ),
                0.0,
                new AFBParams(
                    rand.nextInt(100) + 2,
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
    }

    @Override
    AFBParams clone(AFBParams old) {
        return new AFBParams(old);
    }
    
    @Override
    void walk(int birdIndex) {
        Bird<AFBParams> bird = this.birds.get(birdIndex);
        AFBParams birdParams = new AFBParams(bird.position);
        birdParams.n_birds += Math.round(rand.nextGaussian() * this.stepSizeBirdCount);
        birdParams.n_birds = (int) Math.min(birdParams.n_birds, 3.0);
        birdParams.smallBirdRatio += rand.nextGaussian() * this.stepSize;
        birdParams.smallBirdRatio = Math.min(Math.max(birdParams.smallBirdRatio, 0.0), 1.0);
        birdParams.probMoveRandom += rand.nextGaussian() * this.stepSize;
        birdParams.probMoveRandom = Math.min(Math.max(birdParams.probMoveRandom, 0.0), 1.0);
        birdParams.probMoveBest += rand.nextGaussian() * this.stepSize;
        birdParams.probMoveBest = Math.min(Math.max(birdParams.probMoveBest, 0.0), 1.0);
        birdParams.probMoveJoin += rand.nextGaussian() * this.stepSize;
        birdParams.probMoveJoin = Math.min(Math.max(birdParams.probMoveJoin, 0.0), 1.0);
        if (!birdParams.isInvalid()) {
            bird.position = birdParams;
        } else {
            walk(birdIndex);
        }
    }

    @Override
    void fly(int birdIndex) {
        Bird<AFBParams> bird = this.birds.get(birdIndex);
        AFBParams birdParams = new AFBParams(bird.position);
        birdParams.n_birds = rand.nextInt(100) + 2;
        birdParams.smallBirdRatio = rand.nextDouble();
        birdParams.probMoveRandom = rand.nextDouble();
        birdParams.probMoveBest = rand.nextDouble();
        birdParams.probMoveJoin = rand.nextDouble();
        
        if (!birdParams.isInvalid()) {
            bird.position = birdParams;
        } else {
            fly(birdIndex);
        }
    }

    @Override
    void cost(int birdIndex) {
        Bird<AFBParams> bird = this.birds.get(birdIndex);
        Logger.printLogs = false;
        int max_iters = 100000;
        AFB_TSP solver = new AFB_TSP(
            bird.position.n_birds,
            bird.position.probMoveRandom,
            bird.position.probMoveBest,
            bird.position.probMoveJoin,
            bird.position.smallBirdRatio,
            max_iters,
            this.testTSP,
            this.rand
        );
        
        int repetitions = 4;
        double cost = 0.0;
        for (int i = 0; i < repetitions; i++) {
            solver.init();
            AFBResult<int[]> result = solver.solve();
            cost += result.bestCost / repetitions;
        }
        bird.cost = cost;
        Logger.printLogs = true;
    }
    
}
