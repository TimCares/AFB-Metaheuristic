= Improvements <Improvements>

== Considerations about number of birds

If we study the effects of how different number of birds change the solution of different tours, we learn that less birds seem to yield better solutions. At first this may seem counterintuitive, but there is an intuitive explanation to this:

The basic algorithm defines one iteration as the calculation for the cost of one tour. Now, let a phase of the algorithm denote each bird performing one move. So, in each phase each bird performs one move.
If we now use more birds the number of phases the algorithm will run through will decrease, as in each phase the length of more tours will be calculated, which will use up more iterations. Therefore, the more birds we have, the more iterations we will use up per phase, which means the algorithm will run through less phases until it stops. 
Because in one phase each bird can perform one move, fewer phases mean each bird can perform fewer moves. 
Less moves in turn leads to a less pronounced search of possible solutions,
which will make the algorithm worse.
This is why the fewer birds we have, the better the results will be.
Examples can be seen in Figure ... (fig about relationship n birds x cost).

One could avoid this by simply increasing the number of iterations,
which balances the relationship between number of birds used and the results obtained.
However, this inevitable leads to longer running times.

Consequently, we focus on improving the bird behavior,
so that each bird needs fewer overall steps to achieve a good solution.

== Swarm Behavior <SwarmBehavior>

Currently, if one big bird made the decision to join another bird, he picks one randomly.
This means joining any bird without considering how good the position of that bird might be. This contradicts the original idea of the authors that big birds tend to join others that have found a good food source (current solution seems promising) @afb.
Therefore, we propose that a big will only be able to join the top-b percent of birds that have the lowest current cost. If one chooses the right ratio, we assume that it will automatically nudge the swarm in the direction of the global minimum.

We implement this by storing the indices $i$ of the birds in an ordered integer array $"ord"$ and introducing a new hyperparameter $b$, denoting which of the top-b percent to join. When we select the bird to join to, we draw a random uniform number $j$ between 1 and $b dot n$ ($n$ denotes the number of birds) and get the index of the bird to join from the ordered array ($"ord"[j]$).

The main disadvantage this approach has is that at we need to continuously update $"ord"$ so that we only join the actual top-b percent at the moment of the move.
We decide to update the list after each bird has performed one move (after each phase).
This will still increase the computational complexity, but we think that this is better than updating the list after each move, as this would be too costly.

We test numerous values for $b$ and decide to build on $b=0.01$ for future improvement, as this strategy yields the best results (@top_b_performance).
For intuitions on why such a low value performs this good, please refer to @Intuitions.

#figure(
  table(
    columns: 7,
    inset: 3pt,
    gutter: (1pt, 0pt),
    stroke: 0.5pt,
    align: horizon,
    [Top-b], [1], [0.25], [0.20], [0.15], [0.05], [*0.01*],
    [PercentError], [215], [122], [5.92], [6.14], [6.01], [*5.2*],
    [Time (in s)], [*7.6*], [8.6], [8.7], [8.1], [8.3], [8.1],
  ),
  caption: [Comparison of different parameters for our top-join. 1 means 100%, so a bird randomly joins another bird. This is our benchmark and the default behaviour of the algorithm. Time is measured as the median runtime in seconds, over all 860 tests.],
) <top_b_performance>

== 3-opt Walk <3_opt_walk>

For the walk move the algorithm uses a modified version of the 2-opt heuristic as a local search @afb. Instead of using 2-opt, we test a 3-opt variant, as this often yields the best solutions under consideration of computational complexity @lin. At the same time we decide to remove the estimation of local similarity from the algorithm, as the authors did not provide any intuitions why this may be beneficial @afb.

With 3-opt, each bird selects the tour with the lowest cost out of the 7 different tours possible.
At which 3 points a tour is opened is determined by a random uniform draw of 3 integers,
denoting the nodes of the tour. Because we now need to calculate the length of each of the possible 7 tours
in just one move, we decide it will still only cost us one iteration.
The results can be seen in @3_opt_performance.

#figure(
  image("../images/3_opt.png", width: 8cm),
  caption: [Comparison of 2-opt (left) and 3-opt (right) for TSP. Reconnections of category 1 are 2-opt variants @3_opt.],
)

#figure(
  table(
    columns: 3,
    inset: 3pt,
    gutter: (1pt, 0pt),
    stroke: 0.5pt,
    align: horizon,
    [Configuration], [2-opt], [3-opt],
    [PercentError], [5.2], [*4.67*],
    [Time (in s)], [*8.1*], [9.56],
  ),
  caption: [Comparison of 2-opt and 3-opt. Time is measured as the median runtime in seconds, over all 860 tests.],
) <3_opt_performance>

== Delegating Responsibility <DelegatingResponsibility>

Because we are not implementing the 3-opt algorithm as an iterative solver by itself,
but rather in a swarm algorithm, in which each bird (or agent respectively) can perform this action,
the computational complexity will rise by a margin, as seen in @3_opt_performance.
In order to now compensate for this increase in complexity, we decide to test if we can delegate
the responsibility of performing 3-opt to only a subset of the birds. This sounds promising, as
we can reduce the computational complexity while still being able to profit from the increased exploration of 3-opt.

We test the case where only big birds are able to perform a 3-opt walk, while smaller birds
are only capable of the usual 2-opt walk as specified in the paper. 
This should not only reduce the computational complexity, but it also pairs well with the 
assumption that big birds are "superior", as only they can join other birds and therefore profit from them.

For the sake of completeness we also test the inverse approach: Only small birds can perform 3-opt.
Surprisingly, for big birds we observed that we can achieve the same performance as before, while
cutting runtime by half (@three_opt_big_small_performance). Interestingly, for small birds
we noticed an increased error rate, even though the small bird ratio is always $r > 0.5$ in our experiments.
Based on the results, we decided to use 3-opt only for big birds in our future experiments, as there
seems to be no downside to this approach.

#figure(
  table(
    columns: 5,
    inset: 3pt,
    gutter: (1pt, 0pt),
    stroke: 0.5pt,
    align: horizon,
    [Configuration], [2-opt], [3-opt], [3-opt #linebreak() small birds], [3-opt big birds],
    [PercentError], [5.2], [4.67], [55%], [*55%*],
    [Time (in s)], [*8.1*], [9.56], [55], [55],
  ),
  caption: [Comparison of 2-opt and 3-opt. Time is measured as the median runtime in seconds, over all 860 tests.],
) <three_opt_big_small_performance>

== Nearest-Neighbor Initialization <NearestNeighborInitialization>

== Early Stopping <EarlyStopping>

If we analyze the convergence behavior by plotting the cost of the best solution over the number of iterations
(Figure TODO),
we notice that our improved version of the algorithm converges much faster than the original algorithm,
even without a Nearest-Neighbor initialization. Because it is difficult to estimate how many iterations
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
    [PercentError], [*8*], [10],
    [Time (in s)], [42], [*9*],
  ),
  caption: [], // TODO
) <early_stopping_performance>

== Metabirds <Metabirds>

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

// TODO: Results and observations from the metabird runs

/*
#import "@preview/plotst:0.2.0": *

#let histogram_test_2() = {
  /*let data = (
    (101, 30000000 / 619),
    (493, 20000000 / 817),
    (1000, 5000000 / 386),
    (4461, 500000 / 10),
  )*/
  let data = (
    (101, 0.2235857343494696),
    (493, 0.050676743875328945),
    (1000, 0.19409974797046914),
    (4461, 0.3314722966118387),
  )

  // Create the axes used for the chart 
  let x_axis = axis(min: 0, max: 4461, step: 1000, location: "bottom")
  let y_axis = axis(min: 0, max: max(0.1, 0.3), location: "left", helper_lines: false)

  // Combine the axes and the data and feed it to the plot render function.
  let pl = plot(data: data, axes: (x_axis, y_axis))
  graph_plot(pl, (100%, 25%))
}

#histogram_test_2()
*/
