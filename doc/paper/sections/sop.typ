// TODO: Das hier sind erstmal nur Notizen. Wie das in die Struktur passt, müssen wir noch überlegen.

= Sequential Ordering Problem

The Sequential Ordering Problem (SOP) is an extension of the Traveling Salesman Problem (TSP) which puts additional contstraints on the order in which nodes are visited.
Looking at the cost matrix $C$ of a TSP instance, some edges $C_(i j)$ are valued with $-1$, denoting that a valid tour must include node $j$ before node $i$.

== Naive Approach

The naive approach to extending the TSP solver to solve SOP instances is to simply repeat the existing fly and walk algorithms until the resulting path doesn't violate any of the constraints.
However, as the number of constraints grows, this can lead to significantly longer processing times, especially for the fly move.
A more sophisticated approach is to modify the algorithms, so that they always return a valid path.
Let's take a look at how to do this for the fly.

Some nodes have dependencies, that is a set of other nodes, which must occur in the path before they do.
To make sure the generated random paths are always valid, we might sequentially draw new random nodes, excluding those that don't yet have their dependencies met.
To keep the pool from which new nodes are drawn up to date, we must update it after every draw, adding the nodes which had the newly drawn one as their last unmet dependency.
To make this efficient, fetching the dependents of a node must be cheap.


% Nicht vollkommen neue Pfade generieren, sondern invalide Pfade reparieren.

% Algorithmen so anpassen, dass ausschließlich valide Pfade entstehen
