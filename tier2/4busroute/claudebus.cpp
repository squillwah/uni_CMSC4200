#include <string>
#include <vector>
#include <limits>
#include <iostream>
using namespace std;

struct stop {
    string name;
};

int main() {
    vector<stop> stops = {
        {"Garage"},
        {"Stop A"}, {"Stop B"}, {"Stop C"}, {"Stop D"},
        {"Stop E"}, {"Stop F"}, {"Stop G"}, {"Stop H"},
        {"Stop I"}, {"Stop J"}, {"Stop K"},
        {"School"}
    };

    vector<vector<int>> cost = {
        {0, 2451, 713, 1018, 1631, 1374, 2408, 213, 2571, 875, 1420, 2145, 1972},
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
        {1972, 579, 1260, 987, 371, 999, 701, 2099, 600, 1162, 1200, 504, 0}
    };

    const int START = 0;
    const int END = 12;
    const int INF = numeric_limits<int>::max() / 2;
    const int n = stops.size();

    // Middle stops are everything except START and END
    // We'll index them 0..m-1 where m = n - 2
    vector<int> mid;
    for (int i = 0; i < n; i++)
        if (i != START && i != END) mid.push_back(i);

    int m = mid.size();                  // number of middle stops (10)
    int fullMask = (1 << m) - 1;         // bitmask with all middle stops set

    // dp[mask][i] = min cost to reach mid[i] having visited exactly the stops in mask
    //               starting from START
    vector<vector<int>> dp(1 << m, vector<int>(m, INF));
    vector<vector<int>> parent(1 << m, vector<int>(m, -1));

    // Base case: travel directly from START to each middle stop
    for (int i = 0; i < m; i++)
        dp[1 << i][i] = cost[START][mid[i]];

    // Fill DP: expand every reachable (mask, last-stop) pair
    for (int mask = 1; mask <= fullMask; mask++) {
        for (int last = 0; last < m; last++) {
            if (!(mask & (1 << last))) continue;   // last not in this mask
            if (dp[mask][last] == INF) continue;

            // Try extending to every unvisited middle stop
            for (int next = 0; next < m; next++) {
                if (mask & (1 << next)) continue;  // already visited

                int newMask = mask | (1 << next);
                int newCost = dp[mask][last] + cost[mid[last]][mid[next]];

                if (newCost < dp[newMask][next]) {
                    dp[newMask][next] = newCost;
                    parent[newMask][next] = last;
                }
            }
        }
    }

    // Find the best last middle stop before END
    int bestCost = INF, bestLast = -1;
    for (int last = 0; last < m; last++) {
        if (dp[fullMask][last] == INF) continue;
        int total = dp[fullMask][last] + cost[mid[last]][END];
        if (total < bestCost) {
            bestCost = total;
            bestLast = last;
        }
    }

    // Reconstruct the path by walking parent pointers backwards
    vector<int> midRoute;
    int mask = fullMask, cur = bestLast;
    while (cur != -1) {
        midRoute.push_back(mid[cur]);
        int prev = parent[mask][cur];
        mask ^= (1 << cur);
        cur = prev;
    }
    reverse(midRoute.begin(), midRoute.end());

    // Print results
    cout << "Optimized Bus Route:\n";
    cout << stops[START].name;
    for (int s : midRoute) cout << " -> " << stops[s].name;
    cout << " -> " << stops[END].name;
    cout << "\n\nTotal of: " << bestCost << " distance\n";

    return 0;
}