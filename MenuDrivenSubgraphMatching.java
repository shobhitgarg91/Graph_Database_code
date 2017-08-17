import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.*;
import java.util.*;

/**
 * Created by shobhitgarg on 3/18/17.
 */

/**
 * This class performs naive subgraph matching. takes as input from the user, the target graph name and the pattern graph name.
 * It then loads the database service for the target graph (as it is already stored in Neo4j) and it loads the query
 * graph in the hashmap(memory). It computes a search order dynamically  using DFS and it then creates a search space
 * using the labels (in case of protein) or using the nodes (in case of Igraphs). It then finds the subgraphs that match
 * in the target graph.
 */
public class MenuDrivenSubgraphMatching {
    int numSubgraphs = 0;
    HashMap<Integer, Long> nodes = new HashMap<>();
    HashMap<Integer, Long> nodesHuman = new HashMap<>();
    HashMap<Integer, Long> nodesYeast = new HashMap<>();
    HashMap<String, HashSet<Long>> searchSpaceProtein = new HashMap<>();
    HashMap<Integer, HashSet<Long>> searchSpaceIgraph = new HashMap<>();
    HashMap<Integer, vertexClass> queryGraphNodes = new HashMap<>();
    HashMap<Integer, HashMap<Integer, Long>> listOfSolutions = new HashMap<>();
    HashMap<Integer, Long> solution = new HashMap<>();

    File graphFile;
    ArrayList<Integer> searchOrderSeq = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        long t1 = System.currentTimeMillis();

        MenuDrivenSubgraphMatching obj = new MenuDrivenSubgraphMatching();
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter 1 for human\n2 for yeast\n3 for protein");
        int choice = sc.nextInt();
        if(choice == 1 || choice == 2) {
            // human or yeast
            System.out.println("Enter the query number");
            int queryID = sc.nextInt();
            File qfile = choice == 1? new File("/Users/shobhitgarg/Downloads/iGraph/human_q10.igraph") : new File("/Users/shobhitgarg/Downloads/iGraph/yeast_q10.igraph");
            File gFile = choice == 1? new File("/Users/shobhitgarg/Documents/GraphDB5/humanIgraph") : new File("/Users/shobhitgarg/Downloads/iGraph/yeastIgraph");
            obj.graphFile = gFile;
            obj.readQueryGraph(false, qfile, queryID);
            obj.findSearchOrder();
            obj.findMatchings(false);

        }
        else   {
            // protein
            System.out.print("\nEnter the file name: ");
            String proteinGraph = sc.next();
            System.out.print("\nEnter the query name: ");
            String proteinQuery = sc.next();
            File gFile = new File("/Users/shobhitgarg/Documents/GraphDB5/" + proteinGraph);
            File qFile = new File("/Users/shobhitgarg/Downloads/Proteins/Proteins/query/" + proteinQuery);
            obj.graphFile = gFile;
            obj.readQueryGraph(true, qFile, -1);
            obj.findSearchOrder();
            obj.findMatchings(true);
        }

       // System.out.println("Hello world");
        // finding Matchings

        System.out.println("Number of matchings: " + obj.numSubgraphs);
        obj.printSubgraphs();
        long t2 = System.currentTimeMillis();
        System.out.println("Time taken: " + (t2 - t1));

    }

    /**
     * This function is used for question number 4 and 5. It basically does subgraph matching for the questions.
     * @param data      the arguments that are used to select the type of graph and target and pattern graph.
     * @throws IOException
     */
    void initiateWork(String[] data) throws IOException {

            int choice = Integer.parseInt(data[0]);

            if (choice == 3) {
                String graph = data[1];
                String query = data[2];
                graph = "/Users/shobhitgarg/Documents/GraphDB5/" + graph;
                query = "/Users/shobhitgarg/Downloads/Proteins/Proteins/query/" + query;
                graphFile = new File(graph);
                readQueryGraph(true, new File(query), -1);
                findSearchOrder();
                findMatchings(true);
                findSearchOrder();
                System.out.println("Number of matchings: " + numSubgraphs);
            }
            else if(choice == 2)    {
                String graph = "/Users/shobhitgarg/Documents/GraphDB5/yeastIgraph";
                String query = "/Users/shobhitgarg/Downloads/iGraph/yeast_q10.igraph";
                int queryID = Integer.parseInt(data[1]);
                graphFile = new File(graph);
                readQueryGraph(false, new File(query), queryID);
                findSearchOrder();
                findMatchings(false);
                System.out.println("Number of matchings: " + numSubgraphs);
            }
            else {
                // choice == 1
                String graph = "/Users/shobhitgarg/Documents/GraphDB5/humanIgraph";
                String query = "/Users/shobhitgarg/Downloads/iGraph/human_q10.igraph";
                int queryID = Integer.parseInt(data[1]);
                graphFile = new File(graph);
                readQueryGraph(false, new File(query), queryID);
                findSearchOrder();
                findMatchings(false);
                System.out.println("Number of matchings: " + numSubgraphs);

            }

    }

    /**
     * This function is used to find the search order. It basically uses the DFS technique to compute the
     * search order.
     */

    void findSearchOrder()  {
        boolean filled = false;
        for(int node: queryGraphNodes.keySet()) {

            vertexClass vc = queryGraphNodes.get(node);
            searchOrderSeq.add(node);
            for(int edge: queryGraphNodes.get(node).edges.keySet()) {
                filled = calcOrdering(edge);
                if (filled)
                    break;
            }
            if(searchOrderSeq.size() == queryGraphNodes.size())
                break;

        }

    }

    /**
     * This function assists the function findSearchOrder to calculate the search order.
     * @param node
     * @return
     */
    boolean calcOrdering(int node) {
        boolean isFull = false;
        if(searchOrderSeq.size() == queryGraphNodes.size())
            return true;
        if(!searchOrderSeq.contains(node))  {
            searchOrderSeq.add(node);
            for(int edge: queryGraphNodes.get(node).edges.keySet()) {
                isFull =calcOrdering(edge);
            }
        }

        return isFull;
    }

    /**
     * This function is used to print the solution on the screen
     */
    void printSubgraphs()   {
        StringBuilder sb = new StringBuilder();
        for(int sgKey: listOfSolutions.keySet())    {
            HashMap<Integer, Long> soln = listOfSolutions.get(sgKey);
            // inside a soln
            //sb.append("S:8:");
            for(int key: soln.keySet()) {
                sb.append(key + "," + soln.get(key) + ";");

            }
            sb.setLength(sb.length() - 1);
            sb.append("\n");
        }
        System.out.println("\n" + sb.toString());
    }

    /**
     * This function is used to compute the search space for the graph. It basically computes the search space
     * naively without using profiling
     * @param isProtein
     */
    void findMatchings(boolean isProtein)    {

            GraphDatabaseFactory dbFactory = new GraphDatabaseFactory();
            HashSet<String > searchSpaceList = new HashSet<>();
            GraphDatabaseService databaseService = dbFactory.newEmbeddedDatabase(graphFile);
            for (int id: queryGraphNodes.keySet()) {
                if(isProtein)
                searchSpaceList.add(queryGraphNodes.get(id).label);
                else
                    searchSpaceList.add(String.valueOf(queryGraphNodes.get(id).labels.get(0)));
            }
            for(String x: searchSpaceList)  {
                ResourceIterator<Node> xNodes;
                try(Transaction tx = databaseService.beginTx()) {
                    xNodes = databaseService.findNodes(Label.label(x));
                    tx.success();
                }

                while (xNodes.hasNext()) {
                    Node node = xNodes.next();
                    if (searchSpaceProtein.containsKey(x))
                        searchSpaceProtein.get(x).add(node.getId());
                    else    {
                        HashSet<Long> set = new HashSet<>();
                        set.add(node.getId());
                        searchSpaceProtein.put(x, set);
                    }
                }

            }

            if(isProtein)
            search(0, databaseService, true);
            else
                search(0, databaseService, false);
            databaseService.shutdown();
    }

    /**
     * This function is used to search for the subgraph in the target graph. It finds the the differnt subgraph matchings
     * recursively.
     * @param count     the node in search order that needs to be considered.
     * @param databaseService       used to access the target graph
     * @param isProtein         used to differentiate between the two types of graphs
     * @return
     */
    boolean search(int count, GraphDatabaseService databaseService, boolean isProtein)  {
        boolean completed = false;
        int id = searchOrderSeq.get(count);
        String x = null;
        if(isProtein)
            x = queryGraphNodes.get(id).label;
        else
            x = String.valueOf(queryGraphNodes.get(id).labels.get(0));
        for(Long nodeID: searchSpaceProtein.get(x)) {
            boolean alreadyEncountered = false;
            for(int id1: solution.keySet()) {
                if(solution.get(id1) == nodeID)
                    alreadyEncountered = true;
            }

            if(!check(nodeID, id, databaseService) || alreadyEncountered)
                continue;


            solution.put(id, nodeID);

            if(solution.size() == searchOrderSeq.size()) {
                listOfSolutions.put(++numSubgraphs, new HashMap<>(solution));
                solution.remove(id);
            }
            if(listOfSolutions.size()>999)
                return true;
            if(count<searchOrderSeq.size() - 1)
                search(count + 1, databaseService, isProtein);

        }
        if(listOfSolutions.size()>999)
            return true;
        if(solution.size()>0)
            solution.remove(searchOrderSeq.get(count - 1));
        return completed;
    }

    /**
     * This function is used to determine if the node in the target graph under consideration is suitable to be added to
     * the solution.
     * @param nodeID        the actual Long node ID in the target graph
     * @param qID           the node ID in the query/ pattern graph
     * @param databaseService       used to access the target graph
     * @return
     */
    boolean check(Long nodeID, int qID, GraphDatabaseService databaseService)    {

        vertexClass vc = queryGraphNodes.get(qID);
        try(Transaction tx = databaseService.beginTx()) {
            for(int edge: vc.edges.keySet())    {

                boolean relExist = false;
                if(solution.containsKey(edge))  {
                    // j<i condition confirmed
                    Node edgeNode, otherNode;

                    edgeNode = databaseService.getNodeById(solution.get(edge));
                    otherNode = databaseService.getNodeById(nodeID);

                    Iterable<Relationship> relationships = edgeNode.getRelationships(Direction.BOTH);

                    Iterator <Relationship>iter = relationships.iterator();
                    while (iter.hasNext())  {
                        Relationship rel = iter.next();
                        if(rel.getOtherNode(edgeNode).equals(otherNode))
                            relExist = true;
                    }
                    if(!relExist)
                        return false;
                }

            }
            tx.success();

        }


        return true;
    }

    /**
     * This function is used to read the query graph in the memory and store it in the hashmap
     * @param isProtein         flag used to determine the type of graph
     * @param file              file that the graph is to be read from
     * @param queryID           used in case of igraph.
     * @throws IOException
     */
    void readQueryGraph(boolean isProtein, File file, int queryID) throws IOException {
        if(isProtein) {
            FileReader fileReader = new FileReader(file.getPath());
            BufferedReader br = new BufferedReader(fileReader);
            String line = null;
            int lineread = -1;
            boolean edgesEncountered = false;
            while ((line = br.readLine()) != null) {
                lineread++;
                if (lineread == 0)
                    continue;
                String lineData[] = line.split(" ");
                if (lineData.length == 2) {
                    if (!edgesEncountered) {
                        int id = Integer.parseInt(lineData[0]);
                        String label = lineData[1];
                        vertexClass vc = new vertexClass();
                        vc.id1 = id;
                        vc.label = label;
                        vc.isProtein = true;
                        queryGraphNodes.put(id, vc);

                    } else {
                        int id1 = Integer.parseInt(lineData[0]);
                        int id2 = Integer.parseInt(lineData[1]);
                        queryGraphNodes.get(id1).edges.put(id2, -1);
                    }
                } else {
                    edgesEncountered = true;
                }

            }

        }
        else    {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = null;
            boolean reachedDest = false;
            while ((line = bufferedReader.readLine()) != null) {
                String[] lineData = line.split(" ");
                if (lineData[0].equals("t")) {
                    // new graph
                    if (Integer.parseInt(lineData[2]) == queryID) {
                        reachedDest = true;
                        nodesHuman = new HashMap<>();
                    }
                    else
                        reachedDest = false;

                } else if (lineData[0].equals("v")) {
                    if (reachedDest) {
                        // vertex in the graph
                        int id1 = Integer.parseInt(lineData[1]);
                        HashMap<String, Object> insertData = new HashMap<>();
                        insertData.put("id", id1);
                        //insertData.put("graphID", id);
                        int count = 0;
                        vertexClass vc = new vertexClass();
                        for (int i = 2; i < lineData.length; i++) {
                            vc.labels.add(Integer.parseInt(lineData[i]));

                        }
                        vc.id1 = id1;
                        queryGraphNodes.put(id1, vc);
                    }
                } else if (lineData[0].equals("e")) {
                    // edge in the graph
                    if (reachedDest) {
                        int id1 = Integer.parseInt(lineData[1]);
                        int id2 = Integer.parseInt(lineData[2]);
                        int label = Integer.parseInt(lineData[3]);
                        queryGraphNodes.get(id1).edges.put(id2, label);
                    }

                }
            }
        }
    }


}
