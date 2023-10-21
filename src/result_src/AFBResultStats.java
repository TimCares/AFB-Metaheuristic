package result_src;

import java.util.ArrayList;

public class AFBResultStats<T> extends AFBResult<T>{
    public ArrayList<Double> costOverTime;
    public AFBResultStats(
        T bestPosition,
        double bestCost,
        long timeInMs,
        ArrayList<Double> costOverTime
    ) {
        super(bestPosition, bestCost, timeInMs);
        this.costOverTime = costOverTime;
    }
}
