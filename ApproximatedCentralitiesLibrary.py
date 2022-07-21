import networkx as nx
import random as rd
from numpy.random import choice
import math
from sys import maxsize
from collections import deque


def _add_edge(adj, src, dest):
    # Adds edge between src and dest for adjacency list representation
    adj[src].append(dest)
    adj[dest].append(src)


def _find_paths(paths, path, parent, n, u):
    # Base Case
    if u == -1:
        paths.append(path.copy())
        return

    # Loop for all the parents
    # of the given vertex
    for par in parent[u]:
        # Insert the current
        # vertex in path
        path.append(u)

        # Recursive call for its parent
        _find_paths(paths, path, parent, n, par)

        # Remove the current vertex
        path.pop()


def _bfs(adj, parent, n, start):
    # dist will contain shortest distance
    # from start to every other vertex
    dist = [maxsize for _ in range(n)]
    q = deque()

    # Insert source vertex in queue and make
    # its parent -1 and distance 0
    q.append(start)
    parent[start] = [-1]
    dist[start] = 0

    # Until Queue is empty
    while q:
        u = q[0]
        q.popleft()
        for v in adj[u]:
            if dist[v] > dist[u] + 1:
                # A shorter distance is found
                # So erase all the previous parents
                # and insert new parent u in parent[v]
                dist[v] = dist[u] + 1
                q.append(v)
                parent[v].clear()
                parent[v].append(u)

            elif dist[v] == dist[u] + 1:

                # Another candidate parent for
                # shortest path found
                parent[v].append(u)


def _shortest_paths(adj, n, start, end):
    paths = []
    path = []
    parent = [[] for _ in range(n)]

    # Function call to bfs
    _bfs(adj, parent, n, start)

    # Function call to find_paths
    _find_paths(paths, path, parent, n, end)

    for v in paths:
        v.reverse()

    return paths


def _compute_vertex_diameter_approximation(G):
    # Sample one random node
    v = rd.sample(G.nodes, 1)

    # Compute all the shortest path lengths from the sampled node to every other
    # node and store them in a ordered dictionary
    paths = nx.single_source_shortest_path_length(G, v[0])

    # Since now we have a ordered dictionary with all the lengths of each shortest path,
    # to compute the approximation we just need to select the last two entries and sum them
    return list(paths.values())[-1] + list(paths.values())[-2]


def _build_adjacency_list(G, n):
    # Build adjacency list representation for the graph
    adj = [[] for _ in range(n)]
    for (u, v) in G.edges:
        _add_edge(adj, u, v)
    return adj


def _define_probability_distribution(adj, n, u, t):
    # Pu_t will contain the subset of neighbors of t that are predecessors of t
    # along the shortest paths from u to t
    Pu_t = []

    # Each of the nodes in Pu_t will be sampled with a probability defined
    # in probability_distribution
    probability_distribution = []

    # Compute the shortest paths between u and t
    paths_u_t = _shortest_paths(adj, n, u, t)

    # Compute the number of shortest paths between u and t
    sigma_u_t = len(paths_u_t)

    # For each shortest path
    for j in range(0, sigma_u_t):

        # Consider the predecessor of t in the shortest path of index j between u and t
        predecessor = paths_u_t[j][-2]

        if j == 0:
            # Add predecessor to Pu_t and update sigma_u_predecessor
            Pu_t.append(predecessor)
            sigma_u_predecessor = 1
        else:
            if predecessor == Pu_t[-1]:
                # This implies that there are more than one shortest paths from
                # u to t that contain the given predecessor; therefore, let's
                # update sigma_u_predecessor
                sigma_u_predecessor += 1
            else:
                # We have a new predecessor; now, sigma_u_predecessor contains the total number 
                # of shortest paths from u to t that contain the old predecessor; so, we can 
                # define its probability of being chosen for the random path sampling as the 
                # number of shortest paths between u and the given predecessor divided by the 
                # number of shortest paths between u and t; moreover, we can add the new predecessor
                # to Pu_t and set to 1 the number of shortest paths between u and the new predecessor
                sampling_probability = sigma_u_predecessor / sigma_u_t
                probability_distribution.append(sampling_probability)
                Pu_t.append(predecessor)
                sigma_u_predecessor = 1

        if j == sigma_u_t - 1:
            # This is needed in case sigma_u_t = 1; in this case
            # we are forced to choose the given predecessor
            # when sampling the random shortest path
            sampling_probability = sigma_u_predecessor / sigma_u_t
            probability_distribution.append(sampling_probability)

    return Pu_t, probability_distribution


def approximated_betweenness_centrality(G, epsilon, delta=0.1, c=0.5):
    """
    Approximated betweenness centrality implementation using the Riondato-Karnaropoulos algorithm (2016).

    :param G: weighted/unweighted graph
    :param epsilon: desired accuracy
    :param delta: used to select the desired confidence (which is expressed by 1-delta) for the accuracy; we set
                  delta=0.1 as in the paper
    :param c: constant (we set c=0.5 as in Löffler and Phillips, 2009)
    :return: dictionary containing the approximated betweenness centralities for each node
    """

    # Initialize n with the number of nodes in the graph
    n = len(list(G.nodes))

    # Compute the vertex diameter approximation
    vertex_diameter_approximation = _compute_vertex_diameter_approximation(G)

    # Compute the number of iterations for the algorithm
    r = int((c / epsilon ** 2) * (math.floor(math.log2(vertex_diameter_approximation - 2)) + math.log(1 / delta)))

    # Initialize with 0 the dictionary containing the approximated betweenness
    # centralities for each node
    approximated_centralities = {}
    for node in list(G.nodes):
        approximated_centralities[node] = 0

    # Build adjacency list representation for the graph in order to compute shortest
    # paths between pair of nodes
    adj = _build_adjacency_list(G, n)

    # Start main loop for r iterations
    for i in range(0, r):

        # Randomly sample a pair of nodes u and v
        u, v = rd.sample(G.nodes, 2)

        # Compute all the shortest paths between u and v
        paths = _shortest_paths(adj, n, u, v)

        # Check if there are at least one path
        if len(paths) > 0:

            # Start the procedure to randomly sample a shortest path
            # by setting as starting node the destination node
            t = v

            # Loop until we reach the source node
            while t != u:

                # Define the candidates for the next node of the random sampled shortest
                # path and the probability of choosing them
                Pu_t, probability_distribution = _define_probability_distribution(adj, n, u, t)

                # Sample z from Pu_t according to the probability distribution defined above;
                # this node is the next node for our sampled shortest path
                z = int(choice(Pu_t, 1, p=probability_distribution))

                # Update betweenness centralities estimations
                if z != u:
                    approximated_centralities[z] += 1 / r

                # Update t
                t = z

    return approximated_centralities


def approximated_closeness_centrality(G, k):
    """
    Approximated closeness centrality implementation using the Eppstein-Wang algorithm.

    :param G: weighted/unweighted graph
    :param k: number of iterations
    :return: dictionary containing the approximated closeness centralities for each node
    """

    # Initialize variables
    n = len(list(G.nodes))
    sum_nodes = {}
    approximated_centralities = {}

    # Initialize with 0 the dictionaries containing the sum of distances and
    # the approximated closeness centralities for each node
    for node in list(G.nodes):
        sum_nodes[node] = 0
        approximated_centralities[node] = 0

    for i in range(0, k):

        # Sample random vertex from G
        random_node = rd.sample(G.nodes, 1)[0]

        # Solve the SSSP problem for the random node we just sampled
        shortest_paths = nx.single_source_shortest_path(G, random_node)

        # Update the sum of distances for all the nodes
        for node, path in shortest_paths.items():
            sum_nodes[node] += len(path) - 1

    # Compute the approximated closeness centralities
    for node in list(G.nodes):
        approximated_centralities[node] = 1 / ((n * sum_nodes[node]) / (k * (n - 1)))

    return approximated_centralities
