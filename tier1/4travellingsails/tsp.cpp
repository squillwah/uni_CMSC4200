// C++ program to find the shortest possible route 
// that visits every city exactly once and returns to 
// sourced from geeksforgeeks
// https://www.geeksforgeeks.org/dsa/travelling-salesman-problem-implementation-using-backtracking/
// changes to output taken path

#include <bits/stdc++.h>
using namespace std;

void totalCost(vector<vector<int>> &cost, vector<bool> &visited,
               int currPos, int n, int count, int costSoFar,
               int &ans, vector<int> &path, vector<int> &bestPath) {

    // If all nodes are visited and there's an edge to
    // start node
    if (count == n && cost[currPos][0]) {

        // Update the minimum cost
        if (costSoFar + cost[currPos][0] < ans) {
            ans = costSoFar + cost[currPos][0];
            bestPath = path;
            bestPath.push_back(0);
        }
        return;
    }

    // Try visiting each node from current position
    for (int i = 0; i < n; i++) {
        if (!visited[i] && cost[currPos][i]) {

            // If node not visited and has an edge
            // Mark as visited
            visited[i] = true;
            path.push_back(i);
            totalCost(cost, visited, i, n, count + 1, 
                      costSoFar + cost[currPos][i], ans, path, bestPath);
            path.pop_back();
            visited[i] = false;
        }
    }
}

int tsp(vector<vector<int>> &cost, vector<int> &bestPath) {
    int n = cost.size();
    vector<bool> visited(n, false);
    visited[0] = true;
    int ans = INT_MAX;
    vector<int> path = {0};
    totalCost(cost, visited, 0, n, 1, 0, ans, path, bestPath);
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
    
    //  data for testing
    /*vector<vector<int>> cost = {{0, 10, 15, 20}, 
                                {10, 0, 35, 25}, 
                                {15, 35, 0, 30}, 
                                {20, 25, 30, 0}};
    */

    vector<int> bestPath;
    int res = tsp(cost, bestPath);

    cout << res << endl;
    for (int city : bestPath) cout << city << " ";
    cout << endl;

    return 0;
}