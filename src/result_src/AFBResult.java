package result_src;

public class AFBResult<T> {
  public T bestPosition;
  public double bestCost;
  public long timeInMs;

  public AFBResult(
    T bestPosition,
    double bestCost,
    long timeInMs
  ) {
      this.bestPosition = bestPosition;
      this.bestCost = bestCost;
      this.timeInMs = timeInMs;
  }
}
