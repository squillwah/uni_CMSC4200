#include <iostream>
#include <cstdlib>
#include <ctime>
#include <vector>

#include <graaflib/graph.h>
#include <graaflib/io/dot.h>

#define NUM_NODES 100
#define EDGE_LIMIT 10
#define EDGE_DISTANCE_LIMIT 100

std::vector<int> gen_nodes(graaf::undirected_graph<int, int> &g) {
    std::vector<int> nodes; 
    for (int i = 0; i < NUM_NODES; i++) { 
        nodes.push_back(g.add_vertex(i));
    }
    for (int i = 0; i < NUM_NODES-EDGE_LIMIT; i++) { 
        for (int e = 0; e < EDGE_LIMIT; e++) {
            g.add_edge(nodes[i], nodes[i+e], i*10);
            //g.add_edge(nodes[i], nodes[(rand() % (NUM_NODES)) - i], rand() % EDGE_DISTANCE_LIMIT + 1);
            //g.add_edge(nodes[i], nodes[i + ((rand() % (NUM_NODES+i)) - i)], rand() % EDGE_DISTANCE_LIMIT + 1);
        }
    }


    return nodes;
}




int main(void) {

    graaf::undirected_graph<int, int> graph{};
    std::vector<int> nodes = gen_nodes(graph);
    graaf::io::to_dot(graph, "./graph.dot");
    
    for (int i = 0; i < NUM_NODES; i++) std::cout << nodes[i] << std::endl;


//    graaf::undirected_graph<int, int> graph{}; // = graaf::undirected_graph<int, int>();
//
//    std::vector<std::vector<int>> paths;
//
//    graaf::undirected_graph<int, int> graph2{}; // = graaf::undirected_graph<int, int>();
//
//
//
//    std::vector<int> verts;
//    verts.push_back(graph.add_vertex(0));
//    for (int i = 1; i < 10; i++) {
//        verts.push_back(graph.add_vertex(i));
//        graph.add_edge(verts[i-1], verts[i], i*10);
//    }
//
//    graaf::io::to_dot(graph2, "./sex.dot");
//
////    std::cout << graph << std::endl;
//
    return 0;
}
