#include <iostream>
#include <cstdlib>
#include <ctime>
#include <vector>

#include <graaflib/graph.h>
#include <graaflib/io/dot.h>

#define NUM_NODES 13
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

std::vector<std::vector<int>> gen_paths(graaf::undirected_graph<int,int> &g, int start, int goal) {
    std::vector<std::vector<int>> pathstacks;
    std::vector<std::pair<int, std::vector<int>>> travstack;  // Node + full path to.
    travstack.push_back({start, {start}});
    while (!travstack.empty()) {
        int node = travstack.back().first;
        std::vector<int> nodepath = travstack.back().second;
        travstack.pop_back();
        if (node == goal) {
            pathstacks.push_back(nodepath);
            continue; // Don't bother going deeper.
        }
        auto neighbors = g.get_neighbors(node);
        for (int n : neighbors) {
            bool visited = false;
            for (int v : nodepath) {
                if (n == v) {
                    visited = true;
                    break;
                }
            } if (visited) continue;
    
            std::vector<int> next_nodepath = nodepath;
            next_nodepath.push_back(n);
            travstack.push_back({n, next_nodepath});
        }
    }
    return pathstacks;
}

int find_cheapest(graaf::undirected_graph<int,int> &g, std::vector<std::vector<int>> paths) {
    int cheapest_index = 0; 
    int cheapest_total = INT_MAX; 
    int p, n, total;
    for (p = 0; p < paths.size(); p++) {
        total = 0;
        for (n = 0; n < paths[p].size()-1; n++) 
            for (auto e : g.get_edges()) 
                if ((e.first.first == n && e.first.second == n+1) || (e.first.second == n && e.first.first == n+1)) 
                    total += e.second;
        if (total < cheapest_total) {
            cheapest_index = p;
            cheapest_total = total;
        }
    }
    return cheapest_index;
}

int main(void) {
    srand(1247918208);

    int start = rand() % NUM_NODES + 1;
    int goal;
    while ((goal = rand() % NUM_NODES + 1) == start);

    std::cout << "Generating graph ... ";
    graaf::undirected_graph<int, int> graph{};
    std::vector<int> nodes = gen_nodes(graph);
    graaf::io::to_dot(graph, "./graph.dot");
    std::cout << "done!" << std::endl;
    
    std::cout << "Finding paths ... ";
    std::vector<std::vector<int>> paths = gen_paths(graph, start, goal);
    std::cout << "done!" << std::endl;

    std::cout << "Calculating shortest path ... ";
    std::vector<int> shortest = paths[find_cheapest(graph, paths)];
    std::cout << "done!" << std::endl;

    std::cout << "The shortest path between " << start << " and " << goal << " is:" << std::endl;
    for (int n : shortest) std::cout << " | " << n;
    std::cout << " | " << std::endl;
    
    return 0;
}
