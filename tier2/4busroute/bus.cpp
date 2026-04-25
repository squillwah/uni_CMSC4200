
#include <string>
#include <vector>
#include <limits>
#include <iostream>

using namespace std;

struct stop {
    string name;
};

int findClosest(int cur, vector<vector<int>> cost, vector<bool> visited) {
    int nearest;
    int bestCost = numeric_limits<int>::max();

    for (int i = 0; i < cost.size(); i++) {
        if (!visited[i] && cost[cur][i] < bestCost) {
            bestCost = cost[cur][i];
            nearest = i;
        }
    }
    return nearest;
}

int main() {
    //  stop names, corispond to adj matrix
    vector<stop> stops = {
        {"Garage"},
        {"Stop A"},
        {"Stop B"},
        {"Stop C"},
        {"Stop D"},
        {"Stop E"},
        {"Stop F"},
        {"Stop G"},
        {"Stop H"},
        {"Stop I"},
        {"Stop J"},
        {"Stop K"},
        {"School"}
    };

    //  data sourced from Google
    //  https://developers.google.com/optimization/routing/tsp
    vector<vector<int>> cost = {{0, 2451, 713, 1018, 1631, 1374, 2408, 213, 2571, 875, 1420, 2145, 1972},       //  garage
                                {2451, 0, 1745, 1524, 831, 1240, 959, 2596, 403, 1589, 1374, 357, 579},         //  stop A
                                {713, 1745, 0, 355, 920, 803, 1737, 851, 1858, 262, 940, 1453, 1260},           //  stop B
                                {1018, 1524, 355, 0, 700, 862, 1395, 1123, 1584, 466, 1056, 1280, 987},         //  stop C
                                {1631, 831, 920, 700, 0, 663, 1021, 1769, 949, 796, 879, 586, 371},             //  stop D
                                {1374, 1240, 803, 862, 663, 0, 1681, 1551, 1765, 547, 225, 887, 999},           //  stop E
                                {2408, 959, 1737, 1395, 1021, 1681, 0, 2493, 678, 1724, 1891, 1114, 701},       //  stop F
                                {213, 2596, 851, 1123, 1769, 1551, 2493, 0, 2699, 1038, 1605, 2300, 2099},      //  stop G
                                {2571, 403, 1858, 1584, 949, 1765, 678, 2699, 0, 1744, 1645, 653, 600},         //  stop H
                                {875, 1589, 262, 466, 796, 547, 1724, 1038, 1744, 0, 679, 1272, 1162},          //  stop I
                                {1420, 1374, 940, 1056, 879, 225, 1891, 1605, 1645, 679, 0, 1017, 1200},        //  stop J
                                {2145, 357, 1453, 1280, 586, 887, 1114, 2300, 653, 1272, 1017, 0, 504},         //  stop K
                                {1972, 579, 1260, 987, 371, 999, 701, 2099, 600, 1162, 1200, 504, 0}};          //  school

    //  init paramaters
    int start = 0;              //  garage
    int end = 12;               //  school
    int current = start;
    int totalCost = 0;

    vector<bool> visited(stops.size(), false);
    vector<int> route;

    for (int count = 1; count < stops.size() - 1; count++) {
        int next = findClosest(current, cost, visited);

        if (next == end) {
            visited[end] = true;

            next = findClosest(current, cost, visited);

            visited[end] = false;
        }

        visited[next] = true;
        route.push_back(next);

        totalCost += cost[current][next];
        current = next;
    }

    totalCost += cost[current][end];
    route.push_back(end);

    cout << "Optimized Bus Route:\n";

    for (int i = 0; i < route.size(); i++) {
        cout << stops[route[i]].name;

        if (i < route.size() - 1) {
            cout << " -> ";
        }
    }

    cout << "\n\nTotal of: " << totalCost << " distance\n";


    return 0;
}