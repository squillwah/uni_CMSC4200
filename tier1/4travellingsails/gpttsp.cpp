#include <bits/stdc++.h>
using namespace std;

int tsp(vector<vector<int>> &cost, vector<int> &bestPath) {
    int n = cost.size();
    int fullMask = (1 << n) - 1;
    const int INF = INT_MAX / 2;

    vector<vector<int>> dp(1 << n, vector<int>(n, INF));
    vector<vector<int>> parent(1 << n, vector<int>(n, -1));

    dp[1][0] = 0;

    for (int mask = 1; mask <= fullMask; mask++) {
        for (int u = 0; u < n; u++) {
            if (!(mask & (1 << u)) || dp[mask][u] == INF) continue;

            for (int v = 0; v < n; v++) {
                if (mask & (1 << v)) continue;
                if (cost[u][v] == 0) continue;

                int nextMask = mask | (1 << v);
                int newCost = dp[mask][u] + cost[u][v];

                if (newCost < dp[nextMask][v]) {
                    dp[nextMask][v] = newCost;
                    parent[nextMask][v] = u;
                }
            }
        }
    }

    int ans = INF;
    int lastCity = -1;

    for (int i = 1; i < n; i++) {
        if (cost[i][0] && dp[fullMask][i] + cost[i][0] < ans) {
            ans = dp[fullMask][i] + cost[i][0];
            lastCity = i;
        }
    }

    bestPath.clear();

    int mask = fullMask;
    int curr = lastCity;

    while (curr != -1) {
        bestPath.push_back(curr);
        int prev = parent[mask][curr];
        mask ^= (1 << curr);
        curr = prev;
    }

    reverse(bestPath.begin(), bestPath.end());
    bestPath.push_back(0);

    return ans;
}

int main() {
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