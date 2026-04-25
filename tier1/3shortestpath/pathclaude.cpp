#include <iostream>
#include <cstdlib>
#include <ctime>
#include <vector>
#include <unordered_set>
#include <graaflib/graph.h>
#include <graaflib/io/dot.h>

#define NUM_NODES 16
#define EDGE_LIMIT 5
#define EDGE_DISTANCE_LIMIT 100

std::vector<int> gen_nodes(graaf::undirected_graph<int, int> &g) {
    std::vector<int> nodes;
    nodes.reserve(NUM_NODES);
    for (int i = 0; i < NUM_NODES; i++)
        nodes.push_back(g.add_vertex(i));

    int adjacent;
    for (int i = 0; i < NUM_NODES; i++) {
        for (int e = 0; e < EDGE_LIMIT; e++) {
            adjacent = rand() % NUM_NODES; // BUG FIX: was `i + (rand() % NUM_NODES) - i` which always equals rand() % NUM_NODES but obscures intent
            g.add_edge(nodes[i], nodes[adjacent], rand() % EDGE_DISTANCE_LIMIT + 1);
        }
    }
    return nodes;
}

std::vector<std::vector<int>> gen_paths(graaf::undirected_graph<int, int> &g, int start, int goal) {
    std::vector<std::vector<int>> pathstacks;
    // Store node + visited set + path together to avoid O(N) visited checks
    struct Frame {
        int node;
        std::vector<int> path;
        std::unordered_set<int> visited;
    };
    std::vector<Frame> travstack;
    travstack.push_back({start, {start}, {start}});

    while (!travstack.empty()) {
        Frame frame = std::move(travstack.back());
        travstack.pop_back();

        if (frame.node == goal) {
            pathstacks.push_back(std::move(frame.path));
            continue;
        }

        for (int n : g.get_neighbors(frame.node)) {
            if (frame.visited.count(n)) continue;

            Frame next;
            next.node = n;
            next.path = frame.path;           // copy only when branching
            next.path.push_back(n);
            next.visited = frame.visited;      // copy visited set too
            next.visited.insert(n);
            travstack.push_back(std::move(next));
        }
    }
    return pathstacks;
}

// BUG FIX: was comparing path index n against edge vertex IDs — never matched.
// Now correctly looks up edge weight between actual node IDs in the path.
// Also takes paths by const ref instead of by value.
int find_cheapest(graaf::undirected_graph<int, int> &g, const std::vector<std::vector<int>> &paths) {
    int cheapest_index = 0;
    int cheapest_total = INT_MAX;

    for (int p = 0; p < (int)paths.size(); p++) {
        int total = 0;
        const auto &path = paths[p];
        for (int n = 0; n < (int)path.size() - 1; n++) {
            // Direct O(1) edge lookup by vertex ID pair instead of scanning all edges
            auto edge_id = g.get_edge(path[n], path[n + 1]);
            total += g.get_edge_property(edge_id);
        }
        if (total < cheapest_total) {
            cheapest_index = p;
            cheapest_total = total;
        }
    }
    return cheapest_index;
}

int main(void) {
    srand(1247918208);
    // BUG FIX: was rand() % NUM_NODES + 1, giving range 1-16, but node IDs are 0-15
    int start = rand() % NUM_NODES;
    int goal;
    while ((goal = rand() % NUM_NODES) == start);

    std::cout << "Generating graph ... ";
    graaf::undirected_graph<int, int> graph{};
    std::vector<int> nodes = gen_nodes(graph);
    graaf::io::to_dot(graph, "./graph.dot");
    std::cout << "done!" << std::endl;

    std::cout << "Finding paths ... ";
    std::vector<std::vector<int>> paths = gen_paths(graph, start, goal);
    std::cout << "done!" << std::endl;

    if (paths.empty()) {
        std::cout << "No path found between " << start << " and " << goal << std::endl;
        return 1;
    }

    std::cout << "Calculating shortest path ... ";
    const std::vector<int> &shortest = paths[find_cheapest(graph, paths)];
    std::cout << "done!" << std::endl;

    std::cout << "The shortest path between " << start << " and " << goal << " is:" << std::endl;
    for (int n : shortest) std::cout << " | " << n;
    std::cout << " | " << std::endl;

    return 0;
}