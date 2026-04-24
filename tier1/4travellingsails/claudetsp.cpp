// C++ program to find the shortest possible route 
// that visits every city exactly once and returns to 
// sourced from geeksforgeeks
// https://www.geeksforgeeks.org/dsa/travelling-salesman-problem-implementation-using-backtracking/
// changes to output taken path
#include <bits/stdc++.h>
using namespace std;

// Precomputed minimum outgoing edge per node for lower bound estimation
vector<int> minEdge;

// Lower bound heuristic: current cost + min possible remaining edges
// Uses the minimum outgoing edge from each unvisited node
int lowerBound(int costSoFar, int currPos, int visitedMask, int n) {
    int lb = costSoFar + minEdge[currPos]; // must leave current node
    for (int i = 1; i < n; i++) {
        if (!(visitedMask & (1 << i))) {
            lb += minEdge[i]; // must enter AND leave each unvisited node
        }
    }
    return lb;
}

void totalCost(vector<vector<int>> &cost, int visitedMask,
               int currPos, int n, int count, int costSoFar,
               int &ans, vector<int> &path, vector<int> &bestPath) {

    // Branch-and-bound: prune if lower bound already exceeds best known
    if (lowerBound(costSoFar, currPos, visitedMask, n) >= ans) return;

    // If all nodes are visited and there's an edge to start node
    if (count == n && cost[currPos][0]) {
        int total = costSoFar + cost[currPos][0];
        if (total < ans) {
            ans = total;
            bestPath = path;
            bestPath.push_back(0);
        }
        return;
    }

    // Try visiting each node from current position, sorted by cost (nearest neighbor)
    vector<pair<int,int>> neighbors;
    neighbors.reserve(n);
    for (int i = 1; i < n; i++) {
        if (!(visitedMask & (1 << i)) && cost[currPos][i]) {
            neighbors.push_back({cost[currPos][i], i});
        }
    }
    sort(neighbors.begin(), neighbors.end()); // explore cheapest edges first

    for (auto &[edgeCost, i] : neighbors) {
        path.push_back(i);
        totalCost(cost, visitedMask | (1 << i), i, n, count + 1,
                  costSoFar + edgeCost, ans, path, bestPath);
        path.pop_back();
    }
}

int tsp(vector<vector<int>> &cost, vector<int> &bestPath) {
    int n = cost.size();

    // Precompute minimum outgoing edge for each node (excluding self-loops)
    minEdge.assign(n, INT_MAX);
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            if (i != j && cost[i][j]) {
                minEdge[i] = min(minEdge[i], cost[i][j]);
            }
        }
    }

    // Greedy nearest-neighbor tour for initial upper bound
    int ans = 0;
    {
        vector<bool> used(n, false);
        int cur = 0;
        used[0] = true;
        bestPath.push_back(0);
        for (int step = 1; step < n; step++) {
            int best = -1, bestCost = INT_MAX;
            for (int j = 1; j < n; j++) {
                if (!used[j] && cost[cur][j] && cost[cur][j] < bestCost) {
                    bestCost = cost[cur][j];
                    best = j;
                }
            }
            used[best] = true;
            bestPath.push_back(best);
            ans += bestCost;
            cur = best;
        }
        ans += cost[cur][0];
        bestPath.push_back(0);
    }

    // Now run exact search, seeded with greedy upper bound
    vector<int> path = {0};
    int visitedMask = 1; // node 0 visited
    totalCost(cost, visitedMask, 0, n, 1, 0, ans, path, bestPath);
    return ans;
}

int main() {
    //  data sourced from Google
    //  https://developers.google.com/optimization/routing/tsp
    vector<vector<int>> cost = {{0, 2451, 713, 1018, 1631, 1374, 2408, 213, 2571, 875, 1420, 2145, 1972}, 
                                {2451, 0, 1745, 1524, 831, 1240, 959, 2596, 403, 1589, 1374, 357, 579}, 
                                {713, 1745, 0, 355, 920, 803, 1737, 851, 1858, 262, 940, 1453, 1260}, 
                                {1018, 1524, 355, 0, 700, 862, 1395, 1123, 1584, 466, 1056, 1280, 987},
                                {1631, 831, 920, 700, 0, 663, 1021, 1769, 949, 796, 879, 586, 371},
                                {1374, 1240, 803, 862, 663, 0, 1681, 1551, 1765, 547, 225, 887, 999},
                                {2408, 959, 1737, 1395, 1021, 1681, 0, 2493, 678, 1724, 1891, 1114, 701},
                                {213, 2596, 851, 1123, 1769, 1551, 2493, 0, 2699, 1038, 1605, 2300, 2099},
                                {2571, 403, 1858, 1584, 949, 1765, 678, 2699, 0, 1744, 1645, 653, 600},
                                {875, 1589, 262, 466, 796, 547, 1724, 1038, 1744, 0, 679, 1272, 1162},
                                {1420, 1374, 940, 1056, 879, 225, 1891, 1605, 1645, 679, 0, 1017, 1200},
                                {2145, 357, 1453, 1280, 586, 887, 1114, 2300, 653, 1272, 1017, 0, 504},
                                {1972, 579, 1260, 987, 371, 999, 701, 2099, 600, 1162, 1200, 504, 0}};

    vector<int> bestPath;
    int res = tsp(cost, bestPath);
    cout << res << endl;
    for (int city : bestPath) cout << city << " ";
    cout << endl;
    return 0;
}