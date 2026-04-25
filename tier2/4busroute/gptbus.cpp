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
        {"Garage"}, {"Stop A"}, {"Stop B"}, {"Stop C"}, {"Stop D"},
        {"Stop E"}, {"Stop F"}, {"Stop G"}, {"Stop H"}, {"Stop I"},
        {"Stop J"}, {"Stop K"}, {"School"}
    };

    // adjacency matrix creation unchanged
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

    int start = 0;
    int end = 12;
    int n = stops.size();

    int middleCount = n - 2;
    int fullMask = (1 << middleCount) - 1;
    int INF = numeric_limits<int>::max() / 4;

    vector<vector<int>> dp(1 << middleCount, vector<int>(n, INF));
    vector<vector<int>> parent(1 << middleCount, vector<int>(n, -1));

    dp[0][start] = 0;

    for (int mask = 0; mask <= fullMask; mask++) {
        for (int cur = 0; cur < n; cur++) {
            if (dp[mask][cur] == INF) continue;

            for (int next = 1; next < end; next++) {
                int bit = 1 << (next - 1);

                if ((mask & bit) == 0) {
                    int newMask = mask | bit;
                    int newCost = dp[mask][cur] + cost[cur][next];

                    if (newCost < dp[newMask][next]) {
                        dp[newMask][next] = newCost;
                        parent[newMask][next] = cur;
                    }
                }
            }
        }
    }

    int bestCost = INF;
    int last = -1;

    for (int cur = 1; cur < end; cur++) {
        int finalCost = dp[fullMask][cur] + cost[cur][end];

        if (finalCost < bestCost) {
            bestCost = finalCost;
            last = cur;
        }
    }

    vector<int> route;
    route.push_back(end);

    int mask = fullMask;
    while (last != start && last != -1) {
        route.push_back(last);

        int prev = parent[mask][last];
        mask = mask ^ (1 << (last - 1));
        last = prev;
    }

    route.push_back(start);

    cout << "Optimized Bus Route:\n";

    for (int i = route.size() - 1; i >= 0; i--) {
        cout << stops[route[i]].name;

        if (i > 0) {
            cout << " -> ";
        }
    }

    cout << "\n\nTotal of: " << bestCost << " distance\n";

    return 0;
}