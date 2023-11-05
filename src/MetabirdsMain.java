import result_src.AFBResult;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Random;

public class MetabirdsMain {
    public static void main(String[] args) {
        System.out.println();
        System.out.println("Starting Metabirds:");

        Random rand = new Random();

        // Distance: 686.5986551818119
        // Time: 379.291 seconds
        // Params: 
        // n_birds: 3 // Interesting, this is the minimum!
        // smallBirdRatio: 0.6979749881176104
        // probMoveRandom: 0.1589684022681154
        // probMoveBest: 0.4624556400235943
        // probMoveJoin: 0.33611898159023834
        Metabirds metaSolver = new Metabirds(
            8,
            0.19,
            0.085,
            0.52,
            0.32521985655768404,
            1024,
            rand
        );
        metaSolver.init();

        AFBResult<AFBParams> res = null;
        long start = System.currentTimeMillis();
        res = metaSolver.solve();
        double time = (System.currentTimeMillis() - start) / 1000F;

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

    }

}

