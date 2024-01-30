#set heading(outlined: false, numbering: none)
#pagebreak()
= Appendix

#figure(
  image("../images/phases.png"),
  caption: [An abstract representation on when the ranking of the birds solutions are updated.
  In each phase, each bird performs one single of the four available moves, which is determined by
  probability. After each phase, the ranking of the birds is then updated to determine
  the best bird(s) for the top-join, and later, for early stopping.],
) <phases>

#figure(
  image("../images/pr1002.png"),
  caption: [A visualization of the optimization behavior for different configuration of the
  AFB algorithm (TSPLIB problem pr1002). "Benchmark" denotes the normal AFB algorithm, without any adjustments.
  Each configuration, from top to bottom, includes respective the improvements made prior.
  For example, "Top-Join" consists of the basic algorithm with the top-join added,
  "Opt3" consists of the basic algorithm, together with the top-join and 3-opt, instead
  of 2-opt.
  Each addition to the algorithm results in a faster convergence of the algorithm,
  while consistenly converging to a lower cost, or shorter tour respectively.
  Moreover, it is observable that the restriction of 3-opt to big birds does not
  appear to change anything in the performance and optimization behavior of the
  algorithm.
  With a nearest neighbor initialization the algorithm gets a significant head start.
  However, the optimization behavior can still be observed, even after this initialization.],
) <pr1002_results>

#figure(
  image("../images/pr2392.png"),
  caption: [A visualization of the optimization behavior for different configuration of the
  AFB algorithm (TSPLIB problem pr2392). We observe same behavior as in @pr1002_results,
  this time for a bigger problem with 2392 cities (instead of 1002). Here a nearest neighbor
  initialization again yields a significant head start. Nearest Neighbor, together with
  Early Stopping, is also visualized on the right hand side to better illustrate
  the optimization behavior when the initial solution is a lot closer the the optimum
  than a random initialization.
  ],
) <pr2392_results>
