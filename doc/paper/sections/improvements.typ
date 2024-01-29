= Improvements <Improvements>

== Considerations about the number of birds <CANOF>

When we study the effects of how different numbers of birds change the solution of different tours, we learn that fewer birds seem to yield better solutions.
This may seem counterintuitive at first, but there is an intuitive explanation:

The basic algorithm defines each evaluation of the length of a tour (represented by an agent) as an iteration.
Although the number of iterations are not evenly distributed between the birds, as the algorithm
only evaluates a tour upon the moves "fly" and "walk", and whether a bird executes those is determined randomly @afb,
on average, all birds will use an equal amount of iterations.
Therefore, if we increase the number of birds while maintaining the iterations, we simultaneously decrease the number of iterations _per bird_.
Since a decreased number of iterations per birds means each bird has less overall moves to find a good solution,
there will be a less pronounced exploitation of the search space, which in turn leads to worse results.

One could avoid this by simply increasing the number of iterations, but this inevitably leads to longer running times.
Instead, _we_ focus on improving the birds behavior, so that each bird needs fewer steps to reach a good solution.

== Swarm Behavior <SwarmBehavior>

Currently, if one bird decides to join another, it does so by picking one randomly.
This means joining any bird without considering how good the position of that bird might be. This contradicts the original idea of the authors that big birds tend to join others that have found a good food source (current solution seems promising) @afb.
Therefore, we propose that a big will only be able to join the top-b percent of birds that have the lowest current cost. If one chooses the right ratio, we assume that it will automatically nudge the swarm in the direction of the global minimum.

We implement this by storing the indices $i$ of the birds in an ordered integer array $"ord"$ and introducing a new hyperparameter $b$, denoting which of the top-b percent to join. When we select the bird to join to, we draw a random uniform number $j$ between 1 and $b dot n$ ($n$ denotes the number of birds), and get the index of the bird to join from the ordered array ($"ord"[j]$).
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
We decide to update the list after each bird has performed one move, which we call a "phase" (the algorithm
goes through the phases until the iterations are used up).
This strikes a balance between more accurate results and less computation, as updating the list after every move is too costly.

We tested numerous values for $b$ and decided to build on $b=0.01$ for future improvements, as this strategy gives the best results (@top_b_performance).
For intuitions on why such a low value performs so well, see @Intuitions.

#figure(
  table(
    columns: 8,
    inset: 3pt,
    gutter: (1pt, 0pt),
    stroke: 0.5pt,
    align: horizon,
    [b], [1], [0.25], [0.20], [0.15], [0.05], [0.01], [*only best*],
    [Error (in %)], [497], [323], [279], [246], [150], [79], [*69*],
    [Time (in s)], [*19.7*], [21.8], [21.3], [21.3], [21.7], [21.4], [20.8],
  ),
  caption: [
    Comparison of different parameters for our top-join.
    $b = 1$ means all birds are potential candidates for joining.
    This is the behaviour of the original algorithm, which is why we use it as a baseline.
    It is not surprising that the baseline algorithms performs faster than our modified version,
    as we need additional time for sorting. Notice however that the computation time
    will not change for b-values in the interval $(0, 1)$, as we always need to sort all birds based on
    their performance.
  ],
) <top_b_performance>

The improvements reflect our intuitions that good food sources (i.e. solutions) are
more worthwhile to exploit that others. Interestingly, if during one phase
all birds, wanting to join another, are forced to join the same bird (the best one of the
current phase, which does not change as we do not update the ranking within a phase),
we get the lowest error out of all configurations. We analyze why this setting performs so
well in @Intuitions.

== 3-opt Walk <3_opt_walk>

For the walk move, the original algorithm uses a modified version of the 2-opt heuristic as local search @afb.
Instead of using 2-opt, we tested a 3-opt variant, as this often yields the best solutions considering the computational complexity @lin.
At the same time, we decide to remove the estimation of local similarity from the algorithm, which was used together with 2-opt to perform a local
search. We do this, because the authors did not provide any intuitions on why this might be beneficial @afb.


With 3-opt, each bird creates 7 slight variations of its current tour, and chooses the tour with the lowest cost (see @diff_2_opt_3_opt).
At which 3 points a tour is opened is determined by a random uniform draw of 3 integers representing the indices of the nodes in the tour.

Even though we now have to compute the lengths of 7 possible tours in just one move, we decided to still count this as just one iteration (the evaluation of each tour
normally costs one iteration (compare @CANOF)).
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
    [Error (in %)], [69], [*44*],
    [Time (in s)], [*21*], [85],
  ),
  caption: [Comparison of 2-opt and 3-opt. Note that the 2-opt configuration describes
  our previous configuration of the algorithm, which already includes the top-join of @SwarmBehavior.
  As noted in @Methodology, each experiment build on the most recent best performing one.
  Therefore, the configuration 3-opt denotes a top-join to the best bird of a phase, together
  with 3-opt for local search.
  ],
) <3_opt_performance>

We acknowledge that this adjustment to the algorithm increases the runtime by a margin, but
but due to the enormous improvement we decide to commit to this change and focus on improving
the runtime behavior in the following sections.

== Separating Responsibilities <DelegatingResponsibility>

Apart form the obvious increase in complexity that results for the 3-opt algorithm itself,
the main reason why the runtime quadruples is that we do not implement the 3-opt algorithm
as an iterative solver by itself, but rather in a swarm algorithm, in which agents can perform a single 3-opt iteration at a time.
To now reduce this effect, we decided to limit the responsibility of deep exploitation by 3-opt to a subset of birds.

We test the case where only big birds are able to perform a 3-opt walk, while small birds are only able to perform the usual 2-opt walk as specified in the paper. 
This should not only reduce the computational complexity, but also fits well with the assumption that big birds are "superior", since only they can join other birds and thus benefit from them.
This effectively divides the flock into two groups: The small birds responsible for _exploration_ and the big birds responsible for _exploitation_.
See @ExploitatorsAndExplorators for details on this separation.
For completeness, we also test the inverse approach: Only small birds can perform 3-opt.

Surprisingly, restricting 3-opt to big birds achieves the same performance as before, while cutting the runtime in half (@three_opt_big_small_performance).
We also noticed an increased error rate for small birds, even though the ratio of small birds in our experiments is always greater than 50% ($r > 0.5$),
meaning that the algorithm performs worse if we have more birds that perform 3-opt, given that those are small birds.
This suggests that which type of bird can perform 3-opt is more important than the number of birds that can perform it.

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
    [Configuration], [2-opt], [3-opt], [*3-opt big birds*], [3-opt #linebreak() small birds],
    [Error (in %)], [69], [*44*], [*44*], [85],
    [Time (in s)], [*21*], [85], [40], [69],
  ),
  caption: [Because be usually configure more birds to be small than large ($r > 0.5$),
  we have a higher reduction in runtime if only big birds can perform 3-opt, compared to the
  inverse variant. Under this circumstance it is surprising that we achieve a better performance
  if big birds can perform 3-opt. Again, we compare this configuration with our best result
  up to this point (3-opt together with top-join), and provide 2-opt paired with top-join as
  an additional reference.],
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

#figure(
  table(
    columns: 3,
    inset: 3pt,
    gutter: (1pt, 0pt),
    stroke: 0.5pt,
    align: horizon,
    [Initialization], [Random], [*Nearest Neighbor*],
    [Error (in %)], [44], [*8*], 
    [Time (in s)], [*40*], [42], 
  ),
  caption: [We compare this approach to the best result from @three_opt_big_small_performance,
  which still uses the random initialization from the base algorithm. Adding nearest neighbor
  initialization reduces the error over all 50 tests by 36%. The increase in runtime is rather
  low, and therefore negligible, as the initialization is only done once.
  ]
) <nn_performance>

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
Fittingly, this is already implemented through the top-join (see @SwarmBehavior), as we already need to store the best solution over all birds in order to determine which bird
to join to. This is also the reason why we only check if the best solution has improved after each phase,
and not after each iteration, as the ranking of the birds is not updated within a phase.
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
  caption: [Adding early stopping to our algorithm has the desired effect:
  We can reduce the runtime to a time less than half of the basic algorithm (compare @top_b_performance),
  while increasing to error by a merely 2% compared to @NearestNeighborInitialization.
  Overall, we are able to reduce the error for our benchmark problems (see @Methodology)
  by *487%*.],
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
