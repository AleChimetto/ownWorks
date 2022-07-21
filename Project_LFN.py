import networkx as nx
import pandas as pd
import graphLibrary as gl
import math



def manageDrawing(G):
    print("Drawing graph...")
    pos = nx.spring_layout(G, seed=675)
    gl.drawGraph(G, pos)
    print("Done!")


def manageCentralities(G):
    print("Select the type of centrality measure you want to compute [1/2/3/4]: ")
    print("1 <- Closeness Centrality")
    print("2 <- Approximated Closeness Centrality")
    print("3 <- Betweenness Centrality")
    print("4 <- Approximated Betweenness Centrality")
    choice = int(input())

    while choice != 1 and choice != 2 and choice != 3 and choice != 4:
        print("Select the type of centrality measure you want to compute [1/2/3/4]: ")
        print("1 <- Closeness Centrality")
        print("2 <- Approximated Closeness Centrality")
        print("3 <- Betweenness Centrality")
        print("4 <- Approximated Betweenness Centrality")
        choice = int(input())


    title=''
    if choice == 1:
        title="closeness centrality"
        print("Computing closeness centrality...")
        centralities = nx.closeness_centrality(G)
    elif choice == 2:
        print("Choose k (number of iterations); For a good approximation (with epsilon = 0.1) choose k = {}".format(int(math.log10(len(list(G.nodes))) / 0.01)))
        k = int(input())
        title="approximated closeness centrality"
        print("Computing approximated closeness centrality...")
        centralities = gl.approximated_closeness_centrality(G, k)
    elif choice == 3:
        title="betweenness centrality"
        print("Computing betweenness centrality...")
        centralities = nx.betweenness_centrality(G)
    elif choice == 4:
        print("Choose epsilon (additive error from real values):")
        epsilon = float(input())
        title="approximated betweenness centrality"
        print("Computing approximated betweenness centrality...")
        centralities = gl.approximated_betweenness_centrality(G, epsilon)

    print("Done!")
    if graph==2:
        pos = nx.spring_layout(G, seed=675)
        gl.draw(G, pos, centralities, title)


    print("Do you wish to save the results in a .csv file [y/n]?")
    save = input()
    while save != 'y' and save != 'n':
        print("Do you wish to save the results in a .csv file [y/n]?")
        save = input()

    if save == 'y':
        print("Enter the name of the .csv (e.g. foo.csv):")
        path = input()

        if choice == 1:
            gl.saveDictionaryCSV(path, centralities, ['node', 'closeness centrality'], order=True)
        elif choice == 2:
            gl.saveDictionaryCSV(path, centralities, ['node', 'approximated closeness centrality'], order=True)
        elif choice == 3:
            gl.saveDictionaryCSV(path, centralities, ['node', 'betweenness centrality'], order=True)
        elif choice == 4:
            gl.saveDictionaryCSV(path, centralities, ['node', 'approximated betweenness centrality'], order=True)

    print("Do you want to visualize a ranking of the values [y/n]?")
    rank = input()
    while rank != 'y' and rank != 'n':
        print("Do you want to visualize a ranking of the values [y/n]?")
        rank = input()

    if rank == 'y':
        print("Descending order or ascending order (desc/asc)?")
        order = input()
        while order != 'desc' and order != 'asc':
            print("Descending order or ascending order (desc/asc)?")
            order = input()

        if order == 'desc':
            ordered_dictionary = {k: v for k, v in sorted(centralities.items(), key=lambda item: item[1], reverse=True)}
        elif order == 'asc':
            ordered_dictionary = {k: v for k, v in sorted(centralities.items(), key=lambda item: item[1])}

        print("How long should the ranking be? Indicate the number n of nodes to be considered:")
        n = int(input())

        ranking = {k: ordered_dictionary[k] for k in list(ordered_dictionary)[:n]}
        gl.printRanking(ranking, ['node', 'value'])

        print("Do you wish to save the ranking results in a .csv file [y/n]?")
        save_ranking = input()
        while save_ranking != 'y' and save_ranking != 'n':
            print("Do you wish to save the ranking results in a .csv file [y/n]?")
            save_ranking = input()

        if save_ranking == 'y':
            print("Enter the name of the .csv (e.g. foo.csv):")
            path_ranking = input()

            gl.saveDictionaryCSV(path_ranking, ranking, ['node', 'value'], order=False)


def manageClusteringCoefficients(G):
    print("Select the type of clustering coefficient measure you want to compute [1/2/3/4]: ")
    print("1 <- Local Clustering Coefficient for one node")
    print("2 <- Local Clustering Coefficient for all nodes in the graph")
    print("3 <- Approximated Local Clustering Coefficient for all nodes in the graph")
    print("4 <- Global Clustering Coefficient")
    choice = int(input())

    while choice != 1 and choice != 2 and choice != 3 and choice != 4:
        print("Select the type of clustering coefficient measure you want to compute [1/2/3/4]: ")
        print("1 <- Local Clustering Coefficient for one node")
        print("2 <- Local Clustering Coefficient for all nodes in the graph")
        print("3 <- Approximated Local Clustering Coefficient for all nodes in the graph")
        print("4 <- Global Clustering Coefficient")
        choice = int(input())


    title=""
    if choice == 1:
        print("Choose the node for which you want to compute the local clustering coefficient:")
        node = int(input())
        print("Computing local clustering coefficient for node {}...".format(node))
        cc = {}
        cc[node] = gl.LCC(G, node)
        print("The local clustering coefficient for node {} is {}".format(node, cc[node]))
    elif choice == 2:
        title="local clustering coefficient"
        print("Computing local clustering coefficient for all nodes in the graph...")
        cc = gl.LCCs(G)
    elif choice == 3:
        print("Choose the number of iterations k:")
        k = int(input())
        title="approximated local clustering coefficient"
        print("Computing approximated local clustering coefficient for all nodes in the graph...")
        cc = gl.EstimateLCCs(G, k)
    elif choice == 4:
        print("Computing global clustering coefficient...")
        cc = {}
        _, gcc = gl.globalClusteringCoefficient(G)
        cc["G"] = gcc
        print("The global clustering coefficient of G is {}".format(gcc))

    print("Done!")
    if graph==2 and choice!=4 and choice!=1:
        pos = nx.spring_layout(G, seed=675)
        gl.draw(G, pos, cc, title)


    print("Do you wish to save the results in a .csv file [y/n]?")
    save = input()
    while save != 'y' and save != 'n':
        print("Do you wish to save the results in a .csv file [y/n]?")
        save = input()

    if save == 'y':
        print("Enter the name of the .csv (e.g. foo.csv):")
        path = input()

        if choice == 1 or choice == 2:
            gl.saveDictionaryCSV(path, cc, ['node', 'local clustering coefficient'], order=True)
        elif choice == 3:
            gl.saveDictionaryCSV(path, cc, ['node', 'approximated local clustering coefficient'], order=True)
        elif choice == 4:
            gl.saveDictionaryCSV(path, cc, ['graph', 'global clustering coefficient'], order=True)

    if choice == 2 or choice == 3:
        print("Do you want to visualize a ranking of the values [y/n]?")
        rank = input()
        while rank != 'y' and rank != 'n':
            print("Do you want to visualize a ranking of the values [y/n]?")
            rank = input()

        if rank == 'y':
            print("Descending order or ascending order (desc/asc)?")
            order = input()
            while order != 'desc' and order != 'asc':
                print("Descending order or ascending order (desc/asc)?")
                order = input()

            if order == 'desc':
                ordered_dictionary = {k: v for k, v in sorted(cc.items(), key=lambda item: item[1], reverse=True)}
            elif order == 'asc':
                ordered_dictionary = {k: v for k, v in sorted(cc.items(), key=lambda item: item[1])}

            print("How long should the ranking be? Indicate the number n of nodes to be considered:")
            n = int(input())

            ranking = {k: ordered_dictionary[k] for k in list(ordered_dictionary)[:n]}
            gl.printRanking(ranking, ['node', 'value'])

            print("Do you wish to save the ranking results in a .csv file [y/n]?")
            save_ranking = input()
            while save_ranking != 'y' and save_ranking != 'n':
                print("Do you wish to save the ranking results in a .csv file [y/n]?")
                save_ranking = input()

            if save_ranking == 'y':
                print("Enter the name of the .csv (e.g. foo.csv):")
                path_ranking = input()

                gl.saveDictionaryCSV(path_ranking, ranking, ['node', 'value'], order=False)



########       CREATION OF THE GRAPH

print("Select the graph you want to use for testing [1/2]: ")
print("1 <- Twitch Gamers Social Network")
print("2 <- Example graph")
graph = int(input())

while graph != 1 and graph != 2:
    print("Select the graph you want to use for testing [1/2]: ")
    print("1 <- Twitch Gamers Social Network")
    print("2 <- Example graph")
    graph = int(input())

G = nx.Graph()
if graph == 1:
    print("Loading Twitch Gamers Social Network graph...")
    twitchNodes = pd.read_csv('twitch_gamers/large_twitch_features.csv')
    twitchEdges = pd.read_csv('twitch_gamers/large_twitch_edges.csv')
    G = nx.from_pandas_edgelist(twitchEdges, 'numeric_id_1', 'numeric_id_2')
elif graph == 2:
    print("Creating example graph...")
    G.add_nodes_from([0, 1, 2, 3, 4, 5, 6])
    G.add_edges_from([(0, 1), (0, 3), (1, 2), (1, 3), (2, 4), (3, 4), (3, 5), (4, 6), (5, 6)])
print("Done!")


########       MENU'

exit = False

while not exit:
    print('Select what do you want to do [1/2/3]:')
    print("1 <- Draw Graph (not recommended for Twitch Gamers Social Network)")
    print("2 <- Compute Centralities")
    print("3 <- Compute Clustering coefficients")
    choice = int(input())

    while choice != 1 and choice != 2 and choice != 3:
        print('Select what do you want to do [1/2/3]:')
        print("1 <- Draw Graph (not recommended for Twitch Gamers Social Network)")
        print("2 <- Compute Centralities")
        print("3 <- Compute Clustering coefficients")
        choice = int(input())

    if choice == 1:
        manageDrawing(G)
    elif choice == 2:
        manageCentralities(G)
    elif choice == 3:
        manageClusteringCoefficients(G)

    print("Do you want to compute some other measures [y/n]?")
    choice_exit = input()
    while choice_exit != 'y' and choice_exit != 'n':
        print("Do you want to compute some other measures [y/n]?")
        choice_exit = input()

    if choice_exit == 'n':
        exit = True
