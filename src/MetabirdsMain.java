import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Random;

public class MetabirdsMain {
    public static void main(String[] args) {
        System.out.println();
        System.out.println("Starting Metabirds:");

        Random rand = new Random();

        // Distance: 2946.7310961181465
        // Time: 68.2301 seconds
        // Params: 
        // smallBirdRatio: 0.32521985655768404
        // probMoveRandom: 0.1994471140145183
        // probMoveBest: 0.08468770839867434
        // probMoveJoin: 0.5209509665351663
        Metabirds metaSolver = new Metabirds(
            200,
            0.1,
            0.07,
            0.07,
            1.00,
            100,
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
        System.out.println("smallBirdRatio: " + res.bestPosition.smallBirdRatio);
        System.out.println("probMoveRandom: " + res.bestPosition.probMoveRandom);
        System.out.println("probMoveBest: " + res.bestPosition.probMoveBest);
        System.out.println("probMoveJoin: " + res.bestPosition.probMoveJoin);

    }

}

