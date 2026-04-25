#include <iostream>
#include <cstdlib>
#include <ctime>
#include <vector>

#include <graaflib/graph.h>
#include <graaflib/io/dot.h>

#define NUM_NODES 16 
#define EDGE_LIMIT 5 
#define EDGE_DISTANCE_LIMIT 100

std::vector<int> gen_nodes(graaf::undirected_graph<int, int> &g) {
    std::vector<int> nodes; 
    for (int i = 0; i < NUM_NODES; i++) 
        nodes.push_back(g.add_vertex(i));
    
    int adjacent;
    for (int i = 0; i < NUM_NODES; i++) { 
        for (int e = 0; e < EDGE_LIMIT; e++) {
            adjacent = i + (rand() % (NUM_NODES)) - i;
            g.add_edge(nodes[i], nodes[adjacent], rand() % EDGE_DISTANCE_LIMIT + 1);
        }
    }
    return nodes;
}

std::vector<int> shortest_path(graaf::undirected_graph<int,int> &g, int start, int goal) {
    const int INF = 1000000000;

    std::vector<std::vector<int>> weight(NUM_NODES, std::vector<int>(NUM_NODES, INF));

    for (auto e : g.get_edges()) {
        int a = e.first.first;
        int b = e.first.second;
        int w = e.second;

        if (w < weight[a][b]) {
            weight[a][b] = w;
            weight[b][a] = w;
        }
    }

    std::vector<int> dist(NUM_NODES, INF);
    std::vector<int> prev(NUM_NODES, -1);
    std::vector<bool> visited(NUM_NODES, false);

    dist[start] = 0;

    for (int i = 0; i < NUM_NODES; i++) {
        int current = -1;

        for (int n = 0; n < NUM_NODES; n++) {
            if (!visited[n] && (current == -1 || dist[n] < dist[current])) {
                current = n;
            }
        }

        if (current == -1 || dist[current] == INF) break;
        if (current == goal) break;

        visited[current] = true;

        for (int neighbor = 0; neighbor < NUM_NODES; neighbor++) {
            if (weight[current][neighbor] == INF) continue;

            int new_dist = dist[current] + weight[current][neighbor];

            if (new_dist < dist[neighbor]) {
                dist[neighbor] = new_dist;
                prev[neighbor] = current;
            }
        }
    }

    std::vector<int> path;

    if (dist[goal] == INF) {
        return path;
    }

    for (int at = goal; at != -1; at = prev[at]) {
        path.push_back(at);
    }

    for (int i = 0; i < path.size() / 2; i++) {
        int temp = path[i];
        path[i] = path[path.size() - 1 - i];
        path[path.size() - 1 - i] = temp;
    }

    return path;
}

int main(void) {
    srand(1247918208);

    int start = rand() % NUM_NODES;
    int goal;
    while ((goal = rand() % NUM_NODES) == start);

    std::cout << "Generating graph ... ";
    graaf::undirected_graph<int, int> graph{};
    std::vector<int> nodes = gen_nodes(graph);
    graaf::io::to_dot(graph, "./graph.dot");
    std::cout << "done!" << std::endl;
    
    std::cout << "Calculating shortest path ... ";
    std::vector<int> shortest = shortest_path(graph, start, goal);
    std::cout << "done!" << std::endl;

    std::cout << "The shortest path between " << start << " and " << goal << " is:" << std::endl;

    if (shortest.empty()) {
        std::cout << "No path found." << std::endl;
    } else {
        for (int n : shortest) std::cout << " | " << n;
        std::cout << " | " << std::endl;
    }
    
    return 0;
}