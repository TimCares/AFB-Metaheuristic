// A single agent in the aritificial feeding birds metaheuristic.
public class Bird<T> {
    public T position;
    public double cost;
    public T bestPosition;
    public double bestCost;
    public boolean isBigBird;
    public BirdMove lastMove;
    
    public Bird(
        T position,
        double cost,
        T bestPosition,
        double bestCost,
        boolean isBigBird,
        BirdMove lastMove
    ) {
        this.position = position;
        this.cost = cost;
        this.bestPosition = bestPosition;
        this.bestCost = bestCost;
        this.isBigBird = isBigBird;
        this.lastMove = lastMove;
    }
}
