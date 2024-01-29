= Improvements <Improvements>

== Considerations about the number of birds

When we study the effects of how different numbers of birds change the solution of different tours, we learn that fewer birds seem to yield better solutions.
This may seem counterintuitive at first, but there is an intuitive explanation:

The basic algorithm defines each evaluation of the cost of a tour as an iteration.
The total number of iterations is simply divided among the birds.
Therefore, if we increase the number of birds, we decrease the number of iterations _per bird_.
This results in shallower searches and less exploitation of the search space, which can lead to worse results.

One could avoid this by simply increasing the number of iterations, but this inevitably leads to longer running times.
Instead, we focus on improving the bird's behavior so that each bird needs fewer steps to reach a good solution.

== Swarm Behavior <SwarmBehavior>

Currently, when a big bird makes the decision to join another bird, it chooses one at random.
It does not consider how good the bird's position is.
This contradicts the author's original idea that big birds tend to join others who have found a good food source (current solution seems promising) @afb.
Therefore, we propose that a big bird will only join the top-b percent of birds that have the lowest current cost.
This introduces $b$ as an additional hyperparameter that must be balanced between exploration (higher $b$, more birds to join) and exploitation (lower $b$, join more successful birds).

// The underlying assumption is, that the best birds are more likely to be close to the global minimum.
// This is not necessarily true, but we think that it is a good approximation.
// An alternative approach which we did not explore in detail would be to approximate the remaining potential of a birds position.
// TODO: Der nächste Satz könnte Humbug sein.
// This would be somewhat analogous to joining birds which have just found food instead of joining birds which are well fed.

We implement this by storing the indices $i$ of the birds in an ordered integer array $"ord"$.
When we select the bird to join, we draw a random uniform number $j$ between 1 and $b dot n$ ($n$ denotes the number of birds) and get the index of the bird to join from the ordered array ($"ord"[j]$).

The main disadvantage of this approach is that we have to continuously update $"ord"$ so that we only join the current top-b percent at the moment of the move.
We decided to update the list every time all birds have made a move (we call this a _phase_).
This strikes a balance between more accurate results and less computation.
We found that updating the list after every move was too costly.

We tested numerous values for $b$ and decided to build on $b=0.01$ for future improvements, as this strategy gives the best results (@top_b_performance).
For intuitions on why such a low value performs so well, see @Intuitions.

#figure(
  table(
    columns: 7,
    inset: 3pt,
    gutter: (1pt, 0pt),
    stroke: 0.5pt,
    align: horizon,
    [b], [1], [0.25], [0.20], [0.15], [0.05], [*0.01*],
    [Error (in %)], [215], [122], [5.92], [6.14], [6.01], [*5.2*],
    [Time (in s)], [*7.6*], [8.6], [8.7], [8.1], [8.3], [8.1],
  ),
  caption: [
    Comparison of different parameters for our top-join.
    $b = 1$ means all birds are potential candidates for joining.
    This is the behaviour of the original algorithm which is why we use it as a baseline.
    Time is measured as the median runtime in seconds, over all 860 tests.
  ],
) <top_b_performance>

== 3-opt Walk <3_opt_walk>

For the walk move, the original algorithm uses a modified version of the 2-opt heuristic as local search @afb.
In one step, two tour positions are chosen semi-randomly and the tour is reconnected at these opening points (see @diff_2_opt_3_opt).
Instead of using 2-opt, we tested a 3-opt variant, as this often yields the best solutions considering the computational complexity @lin.
At the same time, we decided to remove local similarity estimation from the algorithm, as the authors did not provide any intuition as to why this might be beneficial @afb.

With 3-opt, each bird chooses the tour with the lowest cost from the 7 possible tours (see @diff_2_opt_3_opt).
At which 3 points a tour is opened is determined by a random uniform draw of 3 integers representing the indices of the nodes in the tour.
// Ist jetzt zu spät, aber eigentlich ist das sehr unsauber.
Even though we now have to compute the length of each of the 7 possible tours in just one move, we decided to count this as one iteration.
The results can be seen in @3_opt_performance.

#figure(
  image("../images/3_opt.png", width: 8cm),
  caption: [Comparison of 2-opt (left) and 3-opt (right) for TSP. Reconnections of category 1 are 2-opt variants @3_opt.],
) <diff_2_opt_3_opt>

#figure(
  table(
    columns: 3,
    inset: 3pt,
    gutter: (1pt, 0pt),
    stroke: 0.5pt,
    align: horizon,
    [Configuration], [2-opt], [3-opt],
    [Error (in %)], [5.2], [*4.67*],
    [Time (in s)], [*8.1*], [9.56],
  ),
  caption: [Comparison of 2-opt and 3-opt. Time is measured as the median runtime in seconds over all 860 tests.],
) <3_opt_performance>

== Separating Responsibilities <DelegatingResponsibility>

We do not implement the 3-opt algorithm as an iterative solver by itself, but rather in a swarm algorithm in which agents can perform a single 3-opt iteration at a time.
Using 3-opt instead of 2-opt increases the computational complexity, as seen in @3_opt_performance.
To reduce the impact on runtime, we decided to limit the responsibility of deep exploitation by 3-opt to a subset of birds.

We test the case where only big birds are able to perform a 3-opt walk, while small birds are only able to perform the usual 2-opt walk as specified in the paper. 
This should not only reduce the computational complexity, but also fits well with the assumption that big birds are "superior", since only they can join other birds and thus benefit from them.
This effectively divides the flock into two groups: The small birds are responsible for _exploration_ and the big birds are responsible for _exploitation_.
See @ExploitatorsAndExplorators for details on this separation.

For completeness, we also test the inverse approach: Only small birds can perform 3-opt.
Surprisingly, restricting 3-opt to big birds achieves the same performance as before, while cutting the runtime in half (@three_opt_big_small_performance).
Interestingly, we noticed an increased error rate for small birds, even though the ratio of small birds in our experiments is always $r > 0.5$.
// TODO: What does this mean? Mixing exploration and exploitation is bad?
Based on these results, we decided to use 3-opt only for big birds in our future experiments, as there seems to be no downside to this approach.

/*
\begin{table}[h!]
\centering
\begin{tabular}{ |p{2cm}||p{0.75cm}|p{0.75cm}|p{0.75cm}|p{0.75cm}|  }
 \hline
 Configuration& 2-opt & 3-opt & \textbf{3-opt big birds} & 3-opt small birds\\
 \hline \hline
PercentError & 69 & \textbf{44} & \textbf{44} & 85\\
 \hline
 Time (in s) & \textbf{21} & 85 & 40 & 69\\
 \hline
\end{tabular}
\caption{}
\label{3_opt_big_small_performance}
\end{table}
*/
#figure(
  table(
    columns: 5,
    inset: 3pt,
    gutter: (1pt, 0pt),
    stroke: 0.5pt,
    align: horizon,
    [Configuration], [2-opt], [3-opt], [3-opt big birds], [3-opt #linebreak() small birds],
    [Error (in %)], [69], [*44*], [*44*], [85],
    [Time (in s)], [*21*], [85], [40], [69],
  ),
  caption: [Comparison of 2-opt and 3-opt. Time is measured as the median runtime in seconds, over all 860 tests.],
) <three_opt_big_small_performance>

== Nearest Neighbor Initialization <NearestNeighborInitialization>

So far, we have always initialized the birds with a random tour.
While this allows for a very wide exploration, it also means that the algorithm needs many iterations to reach the first competitive solutions.
To speed up convergence, we decided to use a simple heuristic to generate initial tours.
Initially, we tried to implement the Christofides algorithm, which is the best-known polynomial-time heuristic for solving the TSP and provides a $3/2$-approximation.
However, due to the complexity of the algorithm and the relatively long worst-case runtime of $O(n^3)$ @christofides, we decided to use the nearest neighbor heuristic instead.
This heuristic is very simple, has a worst-case runtime of $O(n^2)$ @nearest_neighbor, and still provides very good solutions.

Adding the heuristic initialization had a negligible impact on the runtime, which makes sense since the initialization is done only once.
However, the results improved dramatically from an average error of $44%$ with random initialization to now $8%$.

== Early Stopping <EarlyStopping>

If we analyze the convergence behavior by plotting the cost of the best solution over the number of iterations
(Figure TODO),
we notice that our improved version of the algorithm converges much faster than the original algorithm,
even without a nearest neighbor initialization. Because it is difficult to estimate how many iterations
are needed for a certain problem, and adapting the number of iterations to the problem at hand would
be cumbersome, we decide to implement an early stopping mechanism.
That way we do not waste computational resources on iterations that do not improve an existing solution,
which will reduce the runtime even further, especially for smaller TSP configurations, while retaining
a similar performance.

We implement this by introducing a new hyperparameter $p$, denoting the number of phases
without an improvement of the best current solution. If it is exceeded, the algorithm will stop.
This requires us to store the best solution over all birds and updating it continuously.
Fittingly, this is already implemented through the top-b join (see @SwarmBehavior),
as we already need to store the best solution over all birds in order to determine which bird
to join to. This is also the reason why we only check if the best solution has improved after each phase,
and not after each iteration, as during a phase this solution is not updated.
A review during a phase therefore does not make sense.

#figure(
  table(
    columns: 3,
    inset: 3pt,
    gutter: (1pt, 0pt),
    stroke: 0.5pt,
    align: horizon,
    [EarlyStopping], [No (default)], [*Yes*],
    [Error (in %)], [*8*], [10],
    [Time (in s)], [42], [*9*],
  ),
  caption: [], // TODO
) <early_stopping_performance>

== Choosing Hyperparameters: Metabirds <Metabirds>

Both the original algorithm and our extensions define various hyperparameters like the number of birds, the ratio of small birds, the top-b join percentage, the probabilities of the basic moves
We have already discussed analytical approaches to choosing some of these values, but these tests were limited and did not capture the possible complex interplay between different hyperparameters.

To remedy this, we chose to apply an optimization algorithm to the hyperparameters of the TSP solver.
The choice of optimization algorithm was simple: we used the same AFB algorithm that the TSP solver itself uses.
We call this algorithm _Metabirds_, as every bird in the hyperparameter optimizer contains itself a swarm of birds solving the TSP.

To be more precise, every metabird represents a position in the hyperparameter space made up of values for the number of birds, the ratio of small birds, the top-b join percentage and the various move probabilities.
For the walk of a metabird, we sample random deltas from a normal distribution and apply them to the hyperparameters.
Flying to a new position is done choosing random values for the hyperparameters.

The problem with these simple implementations is, that they can produce invalid values.
Specifically,the following conditions must be met:

- the sum of the move probabilities cannot exceed one
- the top-b join percentage must be large enough to include at least one bird
- the number of birds cannot be negative.

Since these meta-moves do not contribute a lot to the overall runtime, we decided to solve this by simply sampling new configurations until we find one that is valid.

To evaluate the cost of a metabird, we run create a TSP solver with birds configuration, run it 8 times and average the results.

We ran the metabirds algorithm multiple times with different problems (eil101, d493, dsj1000, fnl4461) to optimize for different TSP sizes.
To make these runs, which took multiple days, feasible, the algorithm was compiled to native code using GraalVM and executed on cloud resources.


/*
// TODO: Results and observations from the metabird runs

#import "@preview/plotst:0.2.0": *

#let histogram_test_2() = {
  let data = (
    (101, 20000000 / 619 / 2),
    (493, 20000000 / 817 / 10),
    (1000, 5000000 / 386 / 5),
    (4461, 500000 / 10 / 10),
    /*(101, 20000000 / 619),
    (493, 20000000 / 817),
    (1000, 5000000 / 386),
    (4461, 500000 / 10),*/
  )
  /*let data = (
    (101, 0.2235857343494696),
    (493, 0.050676743875328945),
    (1000, 0.19409974797046914),
    (4461, 0.3314722966118387),
  )*/

  // Create the axes used for the chart 
  let x_axis = axis(min: 0, max: 4461, step: 1000, location: "bottom")
  let y_axis = axis(min: 0, max: 20000, step: 5000, location: "left", helper_lines: false)

  // Combine the axes and the data and feed it to the plot render function.
  let pl = plot(data: data, axes: (x_axis, y_axis))
  graph_plot(pl, (100%, 25%))
}

#histogram_test_2()
*/

// TODO: One observation might be, that random flying is not completely useless.
// TODO: Metabirds shows how the AFB algorithm can be applied to non discrete problems.
