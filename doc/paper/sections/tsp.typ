= Traveling Salesman Problem <tsp>

The Traveling Salesman Problem (TSP) is defined as follows:
Given a set of cities and distances between them, find the shortest route that visits each city exactly once and returns to the starting city.
One may be fooled into thinking that this problem is simple, but it is one of the most intensely investigated problems in computational mathematics @cook2011traveling and belongs to the class of NP-complete problems @hoffmann2009tinf.
A brute-force approach would have a running time of $O(n!)$, which becomes infeasible even for small $n$ (brute-forcing a TSP with 20 cities at 4 billion solutions per second would take about 20 years).
// 20! / 4000000000 / 60 / 60 / 24 / 365 = 19.286704149041095890410958904109589041095890410958904109589041095...


