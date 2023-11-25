import java.util.Random;

// AFB for TSP with 3-opt local search for big birds, big birds can only walk.
public class AFB_TSP_TopN_Opt3_NFB extends AFB_TSP_TopN_Opt3_S {
    public AFB_TSP_TopN_Opt3_NFB(
            int n_birds,
            double probMoveRandom,
            double probMoveBest,
            double probMoveJoin,
            double smallBirdRatio,  
            int max_iters,
            double[][] tsp,
            Random rand,
            double joinTop
    ) {
        super(n_birds, probMoveRandom, probMoveBest, probMoveJoin, smallBirdRatio, max_iters, tsp, rand, joinTop);
    }

    @Override
    void fly(int i) {
        if (this.birds.get(i).isBigBird) {
            super.walk(i);
            return;
        }
        super.fly(i);
    }

}
