#include <iostream>
#include <cstdlib>
#include <ctime>
#include <vector>

#include <graaflib/graph.h>
#include <graaflib/io/dot.h>

#define NUM_NODES 10
#define EDGE_LIMIT 5 
#define EDGE_DISTANCE_LIMIT 100

std::vector<int> gen_nodes(graaf::undirected_graph<int, int> &g) {
    std::vector<int> nodes; 
    for (int i = 0; i < NUM_NODES; i++) { 
        nodes.push_back(g.add_vertex(i));
    }
    
    int adjacent;
    for (int i = 0; i < NUM_NODES-EDGE_LIMIT; i++) { 
        std::cout << "Connecting node: " << nodes[i] << std::endl;
        for (int e = 0; e < EDGE_LIMIT; e++) {
            adjacent = i + (rand() % (NUM_NODES)) - i;
            std::cout << "| node: " << nodes[adjacent] << std::endl;
            g.add_edge(nodes[i], nodes[adjacent], rand() % EDGE_DISTANCE_LIMIT + 1);
        }
    }

    return nodes;
}

std::vector<int> path_builder(graaf::undirected_graph<int,int> &graph, std::vector<int> &visited, int start, int goal) {
    visited.push_back(start);
    std::vector<int> result(start);

    bool fresh;
    for (const auto& edge : graph.get_neighbors(start)) {
        //std::cout << edge << " " << std::endl;
        fresh = true;
        for (const auto& visitededge : visited) {
            if (edge == visitededge) {
                fresh = false;
                break;
            }
        }
        if (fresh || edge == goal) {
            result.push_back(edge);
            break;
        } 
    }   

    return result;
}


    

//push_back
//pop_back
//back()
//empty()


//std::vector<std:vector<int>> path_builder(graaf::undirected_graph<int, int> &g,
//std::vector<int> shortest_path(graaf::undirected_graph<int, int> &g, int start, int goal) {
//    std::vector<int> visited;
//    std::vector<std::vector<int>> paths;
//    for (const auto& edge : graph.get_edges(start)) {
//        for (const auto& subedge : graph.get_edges(edge)) {
//            paths.push_back(std::vector<int>(start))
//}


int main(void) {
    graaf::undirected_graph<int, int> graph{};
    std::vector<int> nodes = gen_nodes(graph);
    graaf::io::to_dot(graph, "./graph.dot");

    std::vector<int> v = {};
    std::vector<int> p = path_builder(graph, v, nodes[0], nodes[4]);
    
    /*std::vector<std::vector<int>> pathstacks; 
    
    std::vector<int> visistack;
    std::vector<int> travstack;
    std::vector<int> pathstack;
    
    int node;
    int start = nodes[0];
    int goal = nodes[4];
    travstack.push_back(start);
    while(!travstack.empty()) {
        node = travstack.back();
        //if (node == goal) pathstacks.push_back(travstack);
        std::cout << "Node: " << node << std::endl;
        
        travstack.pop_back();
        pathstack.push_back(node);
        visistack.push_back(node);
    
        bool visited;
        bool emptyorallvisited = true;
        auto neighbors = graph.get_neighbors(node);
        for (int n : neighbors) {
            visited = false;
            for (int v : visistack) {
                if (n == v) {
                    visited = true;
                    break;
                }
            } if (visited) continue;
            
            emptyorallvisited = false;
            travstack.push_back(n);
            std::cout << "| " << n << std::endl;
        }
        if (emptyorallvisited) {
            pathstacks.push_back(pathstack);
            pathstack.pop_back();
        }
    }*/


    int start = nodes[0];
    int goal = nodes[4];
    
    std::vector<std::vector<int>> pathstacks;
    std::vector<std::pair<int, std::vector<int>>> travstack;  // Node + full path to.
    travstack.push_back({start, {start}});
    
    while (!travstack.empty()) {
        int node = travstack.back().first;
        std::vector<int> nodepath = travstack.back().second;
        //auto [node, path] = travstack.back();
        travstack.pop_back();
        std::cout << "Node: " << node << std::endl;
    
        if (node == goal) {
            pathstacks.push_back(nodepath);
            continue; // Don't bother going deeper.
        }
    
        auto neighbors = graph.get_neighbors(node);
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
            
            std::cout << "| " << n << std::endl;
        }
    }
   


    for (auto p : pathstacks) {
        for (int n : p)
            std::cout << " | " << n;
        std::cout << std::endl;
    }


    //for (int i = 0; i < p.size(); i++) 
    //    std::cout << i << " " << std::endl;
    
    
    
    
    return 0;
}
