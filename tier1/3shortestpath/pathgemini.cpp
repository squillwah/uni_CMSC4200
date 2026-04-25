#include <iostream>
#include <cstdlib>
#include <ctime>
#include <vector>
#include <queue>
#include <unordered_map>
#include <limits>
#include <algorithm>

#include <graaflib/graph.h>
#include <graaflib/io/dot.h>

#define NUM_NODES 16 
#define EDGE_LIMIT 5 
#define EDGE_DISTANCE_LIMIT 100

// Constant for infinity
const int INF = std::numeric_limits<int>::max();

// Kept as per instructions: Creation and population logic remains the same
std::vector<int> gen_nodes(graaf::undirected_graph<int, int> &g) {
    std::vector<int> nodes; 
    nodes.reserve(NUM_NODES);
    for (int i = 0; i < NUM_NODES; i++) 
        nodes.push_back(g.add_vertex(i));
    
    for (int i = 0; i < NUM_NODES; i++) { 
        for (int e = 0; e < EDGE_LIMIT; e++) {
            int adjacent = i + (rand() % (NUM_NODES)) - i;
            g.add_edge(nodes[i], nodes[adjacent], rand() % EDGE_DISTANCE_LIMIT + 1);
        }
    }
    return nodes;
}

/**
 * Optimized Pathfinding using Dijkstra's Algorithm
 * Time Complexity: O(E log V)
 * Space Complexity: O(V)
 */
std::vector<int> find_shortest_path(graaf::undirected_graph<int, int> &g, int start, int goal) {
    // priority queue: stores {distance, vertex_id}
    using pii = std::pair<int, int>;
    std::priority_queue<pii, std::vector<pii>, std::greater<pii>> pq;

    std::unordered_map<int, int> distances;
    std::unordered_map<int, int> predecessors;
    
    // Initialize distances
    for (int i = 0; i < NUM_NODES; ++i) distances[i] = INF;

    distances[start] = 0;
    pq.push({0, start});

    while (!pq.empty()) {
        int d = pq.top().first;
        int u = pq.top().second;
        pq.pop();

        if (d > distances[u]) continue;
        if (u == goal) break; // Optimization: stop if we reached the goal

        for (const auto& [v, weight] : g.get_neighbors_with_weights(u)) {
            if (distances[u] + weight < distances[v]) {
                distances[v] = distances[u] + weight;
                predecessors[v] = u;
                pq.push({distances[v], v});
            }
        }
    }

    // Reconstruct path
    std::vector<int> path;
    if (distances[goal] == INF) return path; // No path found

    for (int at = goal; at != start; at = predecessors[at]) {
        path.push_back(at);
    }
    path.push_back(start);
    std::reverse(path.begin(), path.end());
    
    return path;
}

int main(void) {
    srand(1247918208);

    int start = rand() % NUM_NODES;
    int goal;
    while ((goal = rand() % NUM_NODES) == start);

    std::cout << "Generating graph ... ";
    graaf::undirected_graph<int, int> graph{};
    gen_nodes(graph);
    graaf::io::to_dot(graph, "./graph.dot");
    std::cout << "done!" << std::endl;
    
    std::cout << "Calculating shortest path (Dijkstra) ... ";
    std::vector<int> shortest = find_shortest_path(graph, start, goal);
    std::cout << "done!" << std::endl;

    if (shortest.empty()) {
        std::cout << "No path exists between " << start << " and " << goal << std::endl;
    } else {
        std::cout << "The shortest path between " << start << " and " << goal << " is:" << std::endl;
        for (size_t i = 0; i < shortest.size(); ++i) {
            std::cout << " | " << shortest[i];
        }
        std::cout << " | " << std::endl;
    }
    
    return 0;
}