from networkx.classes.function import neighbors
import numpy as np
import pandas as pd
import networkx as nx
import matplotlib.pyplot as plt
import random
import scipy.special
import time
import matplotlib.colors as mcolors
import csv
import collections
from ApproximatedCentralitiesLibrary import *



#########        Create Ranking given CSV
def createRankingFromCSVValues(nameFile, header, n, descending=True):
    # Produces ranking of the first n entries; descending by default
    data = pd.read_csv(nameFile)

    first_column = list(data[header[0]])
    second_column = list(data[header[1]])

    dictionary = {}
    for i in range(0, len(data)):
        dictionary[first_column[i]] = second_column[i]

    ordered_dictionary = {}
    if descending:
        ordered_dictionary = {k: v for k, v in sorted(dictionary.items(), key=lambda item: item[1], reverse=True)}
    else:
        ordered_dictionary = {k: v for k, v in sorted(dictionary.items(), key=lambda item: item[1])}

    return {k: ordered_dictionary[k] for k in list(ordered_dictionary)[:n]}


def printRanking(ranking, header):
    # This function prints the ranking produced with createRankingFromCSVValues()
    print("---------------- RANKING ----------------")
    print("          {}           {}".format(header[0], header[1]))
    i = 1
    for key, value in ranking.items():
        print("Number {}: {}           {}".format(i, key, value))
        i += 1


#########        Saving CSV file given a dictionary
def saveDictionaryCSV(nameFile, dict, header,order):
    if order:
        dict=collections.OrderedDict(sorted(dict.items()))

    print('Writing data...')
    f = open(nameFile, 'w')
    writer = csv.writer(f)
    #--Write the Header to the csv file
    writer.writerow(header)
    #--Write the Data of nodes to the csv file
    for k, v in dict.items(): 
        writer.writerow([k, v])
    #--Close the file
    f.close()

#########        Drawing Functions
def draw(G, pos, measures, measure_name):
    nodes = nx.draw_networkx_nodes(G, pos, node_size=250, cmap=plt.cm.plasma,node_color=list(measures.values()),nodelist=measures.keys())
    nodes.set_norm(mcolors.SymLogNorm(linthresh=0.01, linscale=1, base=10))
    labels = nx.draw_networkx_labels(G, pos)
    edges = nx.draw_networkx_edges(G, pos)
    plt.title(measure_name)
    plt.colorbar(nodes)
    plt.axis('off')
    plt.show()


def drawGraph(G,pos):
    nodes = nx.draw_networkx_nodes(G, pos, node_size=250)
    nodes.set_norm(mcolors.SymLogNorm(linthresh=0.01, linscale=1, base=10))
    labels = nx.draw_networkx_labels(G, pos)
    edges = nx.draw_networkx_edges(G, pos)
    plt.title('Graph')
    plt.axis('off')
    plt.show()


#########        Clustering Coefficient Methods
def LCC(G,v):
    #--Calculate the exact local clustering coefficient of a node v of grapgh G
    num_t=0
    neighbors_v=list(G.neighbors(v))
    for u1 in neighbors_v:
        for u2 in neighbors_v:
            if u1!=u2 and G.has_edge(u1,u2):
                num_t=num_t+1
    
    deg_v=len(neighbors_v)
    if deg_v==1:
        return num_t
    else:
        return num_t/(deg_v*(deg_v-1))

def LCCs(G):
    #--Calculate the exact local clustering coefficient for all the nodes of grapgh G
    lccs={}
    print('Local Clustering Coefficient')
    nodes=list(G.nodes)
    lenght=len(nodes)
    i=0
    for node in nodes:
        print('iteration: ',i,'/',lenght)
        lcc=LCC(G,node)
        lccs[node]=lcc
        i=i+1
    return lccs

def saveTime(filename,line):
    # Open a file with access mode 'a'
    print('saving value')
    file_object = open(filename, 'a')
    file_object.write(line)
    # Close the file
    file_object.close()

def EstimateLCCs(G,k):
    #--Calculate the approximate local clustering coefficient for all the nodes of graph G
    edges=list(G.edges())
    nodes=list(G.nodes)
    Z={}
    print('Initialization')
    for edge in edges:
        Z[frozenset(edge)]=0
        
        
    print('len edges: ',len(edges))
    print('len Z: ',len(Z))
    V=list(range(0, G.number_of_nodes()))
    for i in range(k):
        print('Iteration : ',i,'/',k)
        random.shuffle(V)
        min_v={}
        for node in nodes:
            neighbors=list(G.neighbors(node))
            min_v[node]=min([V[x-1] for x in neighbors]) 
            #print('  Node: ',node,'  min:',min_v[node],'   pi: ',V)

        for edge in edges:
            if min_v[edge[0]]==min_v[edge[1]]:
                Z[frozenset(edge)]=Z[frozenset(edge)]+1

    lccs={}

    print('Creation of lccs Values for all nodes')
    for node in nodes:
        neighbors_v=list(G.neighbors(node))
        num_neigh=len(neighbors_v)

        sum=0
        for u in neighbors_v:
            sum=sum+((Z[frozenset((u,node))]/(Z[frozenset((u,node))]+k))*(num_neigh+len(list(G.neighbors(u)))))
        
        if num_neigh==1:
            lccs[node]=(0.5*sum)
        else:
            lccs[node]=(0.5*sum)/(num_neigh*(num_neigh-1))

    return lccs


def globalClusteringCoefficient(G):
    #--Calculate the exact local clustering coefficient for all the nodes of grapgh G
    num_t=0
    nodes=list(G.nodes)
    for node in nodes:
        neighbors_v=list(G.neighbors(node))
        for u1 in neighbors_v:
            for u2 in neighbors_v:
                if u1!=u2 and G.has_edge(u1,u2):
                    num_t=num_t+1

    return num_t,num_t/(6*scipy.special.binom(len(nodes), 3))



#########        Motifs Methods

def enumerateSubgraphs(G,k):
    #--return all subgraphs of size k in G
    nodes=list(G.nodes)
    v_extension=list()
    subgraphs=list()
    i=0
    for node in nodes:
        start = time.time()
        neighbors_v=list(G.neighbors(node))
        for u in neighbors_v:
            if(u>node):
                v_extension.append(u)
                v_subgraph=list()
                v_subgraph.append(node)
                subgraphs.append(extendSubgraphs(v_subgraph,v_extension,node,k,G))
        print('iteration: ', i, '    --->    time:  ', time.time() - start)
        i = i + 1
    return subgraphs


def extendSubgraphs(v_subgraph,v_extension,v,k,G):
    if (len(v_subgraph)==k):
        return G.subgraph(v_subgraph)
    v_prime_extension=list()
    while (len(v_extension)>0):
        w=v_extension.pop()
        n_excl=list(G.neighbors(w))
        for u in n_excl:
            flag=True
            subgraph_n=list()
            if(u in v_subgraph):
                n_excl.remove(u)
                flag=False
            if(flag):
                for h in v_subgraph:
                    subgraph_n.append(list(G.neighbors(h)))
                if(u in subgraph_n):
                    n_excl.remove(u)
                    flag=False
            if (flag and u>v):
                v_prime_extension.append(u)
        v_subgraph.append(w)
        extendSubgraphs(v_subgraph,v_prime_extension,v,k,G)
    return

def countSubgraphs(subgraphs):
    return len(subgraphs)
