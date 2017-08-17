import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.*;
import java.util.*;

/**
 * Created by shobhitgarg on 3/18/17.
 */

/**
 * This class is used to perform subgraph matching by computing the cheapest search order and using profiling to
 * reduce the search space.
 */
public class MenuDrivenProfiledSubgraphMatching {
    int numSubgraphs = 0;
    HashMap<Integer, Long> nodes = new HashMap<>();
    HashMap<Integer, Long> nodesHuman = new HashMap<>();
    HashMap<Integer, Long> nodesYeast = new HashMap<>();
    HashMap<Integer, HashSet<Long>> searchSpaceProtein = new HashMap<>();
    HashMap<Integer, HashSet<Long>> searchSpaceIgraph = new HashMap<>();
    HashMap<Integer, vertexClass> queryGraphNodes = new HashMap<>();
    HashMap<Integer, HashMap<Integer, Long>> listOfSolutions = new HashMap<>();
    HashMap<Integer, Long> solution = new HashMap<>();
    File graphFile;
    ArrayList<Integer> searchOrderSeq = new ArrayList<>();
    double gamma = 0.5;
    // 1 targetname queryname
    public static void main(String[] args) throws IOException {
        long t1 = System.currentTimeMillis();
        MenuDrivenProfiledSubgraphMatching obj = new MenuDrivenProfiledSubgraphMatching();
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter 1 for human\n2 for yeast\n3 for protein");
        int choice = sc.nextInt();
        if(choice == 1 || choice == 2) {
            // human or yeast

            System.out.println("Enter the query number");
            int queryID = sc.nextInt();
            //int queryID = Integer.parseInt(args[1]);
            File qfile = choice == 1? new File("/Users/shobhitgarg/Downloads/iGraph/human_q10.igraph") : new File("/Users/shobhitgarg/Downloads/iGraph/yeast_q10.igraph");
            File gFile = choice == 1? new File("/Users/shobhitgarg/Documents/GraphDB5/humanIgraph") : new File("/Users/shobhitgarg/Documents/GraphDB5/yeastIgraph");
            obj.graphFile = gFile;
            //obj.insertInGraphDB2(false, gFile);
            obj.readQueryGraph(false, qfile, queryID);
            GraphDatabaseFactory dbFactory = new GraphDatabaseFactory();
            GraphDatabaseService databaseService = dbFactory.newEmbeddedDatabase(obj.graphFile);
            obj.findMatchings(false, databaseService);
            obj.findSearchOrder();
            obj.search(0, databaseService);
            System.out.println("Number of matchings: " + obj.numSubgraphs);
            //obj.printSubgraphs();
            long t2 = System.currentTimeMillis();
            System.out.println("Time taken (Profiled): " + (t2 - t1));
        }
        else   {
            // protein
            System.out.print("\nEnter the file name: ");
            String proteinGraph = sc.next();
            //String proteinGraph = "backbones_1O54.grf";
            System.out.print("\nEnter the query name: ");
            String proteinQuery = sc.next();
            //String proteinQuery = "backbones_1EMA.8.sub.grf";
            File gFile = new File("/Users/shobhitgarg/Documents/GraphDB5/" + proteinGraph);
            File qFile = new File("/Users/shobhitgarg/Downloads/Proteins/Proteins/query/" + proteinQuery);
            obj.graphFile = gFile;
            //obj.insertInGraphDB2(true, gFile);
            obj.readQueryGraph(true, qFile, -1);
            GraphDatabaseFactory dbFactory = new GraphDatabaseFactory();
            GraphDatabaseService databaseService = dbFactory.newEmbeddedDatabase(obj.graphFile);
            obj.findMatchings(true, databaseService);
            obj.findSearchOrder();
            obj.search(0, databaseService);
            System.out.println("Number of matchings: " + obj.numSubgraphs);
            obj.printSubgraphs();
            long t2 = System.currentTimeMillis();
            System.out.println("Time taken (Profiled): " + (t2 - t1));

        }



    }
    /**
     * This function is used for question number 4 and 5. It basically does subgraph matching for the questions.
     * @param data      the arguments that are used to select the type of graph and target and pattern graph.
     * @throws IOException
     */
    void initiateWork(String[] data) throws IOException {
        int choice = Integer.parseInt(data[0]);

        if(choice == 3) {
            String graph = data[1];
            String query = data[2];
            graph = "/Users/shobhitgarg/Documents/GraphDB5/" + graph;
            query = "/Users/shobhitgarg/Downloads/Proteins/Proteins/query/" + query;
            GraphDatabaseFactory dbFactory = new GraphDatabaseFactory();
            GraphDatabaseService databaseService = dbFactory.newEmbeddedDatabase(new File(graph));
            readQueryGraph(true, new File(query), -1);
            findMatchings(true, databaseService);findSearchOrder();
            search(0, databaseService);
            System.out.println("Number of matchings: " + numSubgraphs);
            databaseService.shutdown();
        }
        else if(choice == 2)    {
            String graph = "/Users/shobhitgarg/Documents/GraphDB5/yeastIgraph";
            String query = "/Users/shobhitgarg/Downloads/iGraph/yeast_q10.igraph";
            GraphDatabaseFactory dbFactory = new GraphDatabaseFactory();
            GraphDatabaseService databaseService = dbFactory.newEmbeddedDatabase(new File(graph));
            int queryID = Integer.parseInt(data[1]);
            graphFile = new File(graph);
            readQueryGraph(false, new File(query), queryID);
            findMatchings(false,databaseService ); findSearchOrder();
            search(0, databaseService);
            System.out.println("Number of matchings: " + numSubgraphs);
            databaseService.shutdown();

        }
        else {
            String graph = "/Users/shobhitgarg/Documents/GraphDB5/humanIgraph";
            String query = "/Users/shobhitgarg/Downloads/iGraph/human_q10.igraph";
            GraphDatabaseFactory dbFactory = new GraphDatabaseFactory();
            GraphDatabaseService databaseService = dbFactory.newEmbeddedDatabase(new File(graph));
            int queryID = Integer.parseInt(data[1]);
            graphFile = new File(graph);
            readQueryGraph(false, new File(query), queryID);
            findMatchings(false,databaseService ); findSearchOrder();
            search(0, databaseService);
            System.out.println("Number of matchings: " + numSubgraphs);
            databaseService.shutdown();
        }

    }

    /**
     * This function is used to compute the search order that is the cheapest amongst all others present. It takes into
     * consideration the gamma value by computing the number of edges the node under consideration has  with the
     * nodes present in the solution.
     */
    void findSearchOrder()  {
        boolean filled = false;
        int minIndex = -1, minVal = Integer.MAX_VALUE;
        for(int key: searchSpaceProtein.keySet())   {
            if(searchSpaceProtein.get(key).size()<minVal) {
                minIndex = key;
                minVal = searchSpaceProtein.get(key).size();
            }
        }

        searchOrderSeq.add(minIndex);
        calcOrdering();
    }

    /**
     * This function assists the findSearchOrder() function to calculate the cheapest search order.
     * @return
     */
    boolean calcOrdering() {
        boolean isFull = false;
        if(searchOrderSeq.size() == queryGraphNodes.size())
            return true;
        double minVal = Double.MAX_VALUE;
        int minIndex = -1;
       for(int node: searchOrderSeq) {

               vertexClass vc = queryGraphNodes.get(node);
                    try {
                        for (int key : vc.edges.keySet()) {
                            int countConn = 0;
                            for (int key1 : queryGraphNodes.get(key).edges.keySet()) {
                                if (searchOrderSeq.contains(key1))
                                    countConn++;
                            }
                            double currMinCost = searchSpaceProtein.get(key).size() * Math.pow(gamma, countConn);
                            if (!searchOrderSeq.contains(key) && currMinCost < minVal) {
                                minIndex = key;
                                minVal = currMinCost;
                            }
                        }
                    } catch (Exception e)   {
                        System.out.println("err: " + node);
                        System.exit(1);
                    }
       }
        searchOrderSeq.add(minIndex);
        if(searchOrderSeq.size() == queryGraphNodes.size())
            return true;
        else
            calcOrdering();



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
            sb.append("S:8:");
            for(int key: soln.keySet()) {
                sb.append(key + "," + soln.get(key) + ";");

            }
            sb.setLength(sb.length() - 1);
            sb.append("\n");
        }
        System.out.println("\n" + sb.toString());
    }
    /**
     * This function is used to compute the search space for the graph. takes into consideration the neighborhood profile
     * of the node when considering a target graph node to be included in the search space. The neighborhood profile of
     * the node in the target graph is stored as a property. It calculates the profile of the node in the query graph
     * on the fly.
     * @param isProtein     used to determine the type of graph.
     */
    void findMatchings(boolean isProtein, GraphDatabaseService databaseService)    {

        if(isProtein) {
            for(int nodeQ: queryGraphNodes.keySet())  {
                HashSet<Long> set = new HashSet<>();
                searchSpaceProtein.put(nodeQ, set);
                ResourceIterator<Node> xNodes;
                String x = queryGraphNodes.get(nodeQ).label;
                try(Transaction tx = databaseService.beginTx()) {
                    xNodes = databaseService.findNodes(Label.label(x));


                while (xNodes.hasNext()) {

                    // for each node in the original graph
                    Node node = xNodes.next();
                    String tProfile = (String) node.getProperty("Profile");

                    String [] edges = new String [queryGraphNodes.get(nodeQ).edges.size()];
                    int count = -1;
                    for (int edge : queryGraphNodes.get(nodeQ).edges.keySet()) {
                        edges[++count] = queryGraphNodes.get(edge).label;
                    }
                    Arrays.sort(edges);
                    StringBuilder sb = new StringBuilder();
                    for(String x1: edges)
                        sb.append(x1);
                    String toBeMatched = sb.toString();
                    HashMap<String, Integer> characterCount = new HashMap<>();
                    // finding if the profile exists
                    int count1 = 0;
                    boolean matched = false;
                    for(char x1: tProfile.toCharArray()) {
                        if(count1<toBeMatched.length() - 1) {
                            if(toBeMatched.charAt(count1) == x1)    {
                                count1 ++;
                            }
                        }
                        else {
                            matched = true;
                            break;
                        }
                    }
                    if (matched)
                        searchSpaceProtein.get(nodeQ).add(node.getId());
                    tx.success();
                }

                }

            }
        }
        else    {
// igraph
            for(int nodeQ: queryGraphNodes.keySet())  {
                HashSet<Long> set = new HashSet<>();
                searchSpaceProtein.put(nodeQ, set);
                ResourceIterator<Node> xNodes;
                String [] edges = new String [queryGraphNodes.get(nodeQ).edges.size()];
                int count = -1;
                for (int edge : queryGraphNodes.get(nodeQ).edges.keySet()) {
                    edges[++count] = String.valueOf(queryGraphNodes.get(edge).labels.get(0));
                }

                String x = String.valueOf(queryGraphNodes.get(nodeQ).labels.get(0));
                try(Transaction tx = databaseService.beginTx()) {
                    xNodes = databaseService.findNodes(Label.label(x));


                    while (xNodes.hasNext()) {

                        // for each node in the original graph
                        Node node = xNodes.next();
                        if(edges.length == 0)   {
                            searchSpaceProtein.get(nodeQ).add(node.getId());
                            continue;
                        }
                        String tProfile = null;
                        if(node.hasProperty("Profile"))
                         tProfile = (String) node.getProperty("Profile");
                        else
                            continue;

                        Arrays.sort(edges);
                        StringBuilder sb = new StringBuilder();
                        for(String x1: edges)
                            sb.append(x1 + ",");
                        sb.setLength(sb.length() - 1);
                        // setting up hashmap for targetProfile
                        HashMap<String, Integer> tmap = new HashMap<>();
                        String []tProfiles = tProfile.split(",");
                        for(String x1: tProfiles)   {
                            if(tmap.containsKey(x1))
                                tmap.put(x1, tmap.get(x1) + 1);
                            else
                                tmap.put(x1, 1);
                        }
                        String toBeMatched = sb.toString();
                        // finding if the profile exists
                        int count1 = 0;
                        boolean notMatched = false;
                        for(String x1: toBeMatched.split(","))  {
                            if(!tmap.containsKey(x1) || (tmap.get(x1) == 0))
                                notMatched = true;
                            else
                                tmap.put(x1, tmap.get(x1) - 1);
                        }

                        if (!notMatched)
                            searchSpaceProtein.get(nodeQ).add(node.getId());
                        tx.success();
                    }

                }

            }

        }
    }
    /**
     * This function is used to search for the subgraph in the target graph. It finds the the differnt subgraph matchings
     * recursively.
     * @param count     the node in search order that needs to be considered.
     * @param databaseService       used to access the target graph
     * @return
     */
    boolean search(int count, GraphDatabaseService databaseService)  {
        boolean completed = false;
        int id = searchOrderSeq.get(count);
        // iterating over the search space for a particular node in the query graphk
        for(Long nodeID: searchSpaceProtein.get(id)) {
            boolean alreadyEncountered = false;
            //if()
            for(int id1: solution.keySet()) {
                if(solution.get(id1).equals(nodeID) ) {
                    alreadyEncountered = true;
                    break;
                }

            }
            if(alreadyEncountered || !check(nodeID, id, databaseService))
                continue;


            solution.put(id, nodeID);

            if(solution.size() == searchOrderSeq.size()) {
                listOfSolutions.put(++numSubgraphs, new HashMap<>(solution));

                solution.remove(id);
            }
            if(listOfSolutions.size()>999)
                return true;
            if(count<searchOrderSeq.size() - 1)
                search(count + 1, databaseService);

        }
        if(listOfSolutions.size()>999)
            return true;
        if(solution.size()>0 && count > 0) {
         try {
             solution.remove(searchOrderSeq.get(count - 1));
         }
            catch (Exception e) {
                System.out.println(" exception");
            }

        }
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
                    Node node, otherNode;

                    otherNode = databaseService.getNodeById(solution.get(edge));
                    node = databaseService.getNodeById(nodeID);

                    Iterable<Relationship> relationships = node.getRelationships(Direction.BOTH);

                    Iterator <Relationship>iter = relationships.iterator();
                    while (iter.hasNext())  {
                        Relationship rel = iter.next();
                        Node n = rel.getOtherNode(node);
                        if(n.equals(otherNode))  {
                            relExist = true;
                            break;  }
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
     * This function is used to read the query graph in the memory and store it in the hashmap.
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
                        queryGraphNodes.get(id2).edges.put(id1, label);
                    }

                }
            }



        }
    }


}
