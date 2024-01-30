= Sequential Ordering Problem <sop>

In addition to the TSP, we also applied the AFB algorithm to the Sequential Ordering Problem (SOP).
The SOP is an extension of the TSP which puts additional contstraints on the order in which nodes are visited.
Looking at the cost matrix $C$ of a TSP instance, some edges $C_(i j)$ are valued with $-1$, denoting that a valid tour must include node $j$ before node $i$.

== Naive Approach

The naive approach to extending the TSP solver to solve SOP instances is to simply repeat the existing fly and walk algorithms until the resulting path doesn't violate any of the constraints.
However, as the number of constraints grows, this can lead to significantly longer processing times, especially for the fly move.

// == Reparing Solutions
// TODO: ?

== Generating Valid Paths

A more sophisticated approach is to modify the algorithms, so that they always return a valid path.
Let's take a look at how to do this for the fly move.

Some nodes have dependencies, that is a set of other nodes, which must occur in the path before they do.
To make sure the generated random paths are always valid, we might sequentially draw new random nodes, excluding those that don't yet have their dependencies met.
To implement this, we keep a pool of nodes, which have not yet been drawn, but whose dependencies have been met.
To keep the pool up to date, we must update it after every draw, removing the drawn node and adding the nodes, which had the newly drawn one as their last unmet dependency.

To find the nodes which have no more unmet dependencies, we store the number of unmet dependencies for every node in an array adjacent to the node array.
After every draw, we loop through the relevant row of the cost matrix to find all nodes depending on the one drawn, and decrement their dependency count.
If the count reaches zero, we add the node to the pool.

While this approach works, its runtime is $O(n^2)$ ($n$ draws and $n$ possible dependents per draw), which is not ideal.
