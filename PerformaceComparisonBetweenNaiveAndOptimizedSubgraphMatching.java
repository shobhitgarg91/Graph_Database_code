import java.io.*;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by shobhitgarg on 3/29/17.
 */

/**
 * This class is used to compare the performance of the naive and optimized implementation in case of protein graph.
 * Initially, we planned to iterate over the complete ground truth files to compare the performance, however, on doing
 * that, we were faced with heating issues in the machine, also it didn't gave any results even when the machine was left
 * isolated for an hour to perform processing.
 */
public class PerformaceComparisonBetweenNaiveAndOptimizedSubgraphMatching {
    HashSet<String> computed;
    HashSet<String> ground;
    HashSet<String> matched = new HashSet<>();
    String[] graphTypes = {"backbones", "human", "ecoli", "mus_musculus", "rattus_norvegicus", "saccharomyces_cerevisiae", "bos_taurus"};
    String[] files = {"Proteins.8.gtr", "Proteins.16.gtr", "Proteins.32.gtr", "Proteins.64.gtr", "Proteins.128.gtr", "Proteins.256.gtr"};
    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();
        PerformaceComparisonBetweenNaiveAndOptimizedSubgraphMatching obj = new PerformaceComparisonBetweenNaiveAndOptimizedSubgraphMatching();
        int count = 8;
        for(String file: obj.files) {
            long t1 = System.currentTimeMillis();
            obj.findSolution(file, count, true);
            long t2 = System.currentTimeMillis();
            System.out.println("File completed: " + file);
            System.out.println("TIME TAKEN FOR OPTIMIZED EXECUTION FOR FILE: " + file + ": " + (t2 - t1)/1000 + " seconds");
        count *= 2;

        }
        long endTime = System.currentTimeMillis();
        System.out.println("\nTIME TAKEN FOR COMPLETE OPTIMIZED EXECUTION: " + (endTime - start)/ 1000 + " seconds\n");
        if(obj.matched.size()>1)
            System.out.println("Not matched found");
      // naive
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        start = System.currentTimeMillis();
        count = 8;
        for(String file: obj.files) {
            long t1 = System.currentTimeMillis();
            obj.findSolution(file, count, false);
            long t2 = System.currentTimeMillis();
            System.out.println("File completed: " + file);
            System.out.println("\nTIME TAKEN FOR NAIVE EXECUTION FOR FILE: " + file + ": " + (t2 - t1)/1000 + " seconds\n");

            count *= 2;
        }
        endTime = System.currentTimeMillis();
        System.out.println("\nTIME TAKEN FOR COMPLETE NAIVE EXECUTION: " + (endTime - start)/ 1000 + " seconds\n");

        if(obj.matched.size()>1)
            System.out.println("Not matched found");

    }

    /**
     * this function is used to process the complete ground truth files.
     * @param fileName      the name of the file to be processed.
     * @param noOfNodes     used to format the output.
     * @throws IOException
     */
    void findCompleteSolution(String fileName, int noOfNodes) throws  IOException {
        fileName = "/Users/shobhitgarg/Downloads/Proteins/Proteins/ground_truth/" + fileName;
        File file = new File(fileName);
        FileReader fileReader = new FileReader(file.getPath());
        BufferedReader br = new BufferedReader(fileReader);
        String line = null;
        while ((line = br.readLine()) != null) {
            if (line.contains("T:")) {

                // particular graph found
                String graphFile = line.split("T:")[1];
                //System.out.println("Graph file: " + graphFile);
                line = br.readLine();
                String queryFile = line.split("P:")[1];
                //System.out.println("Query file: " + queryFile);

                MenuDrivenProfiledSubgraphMatching obj = new MenuDrivenProfiledSubgraphMatching();
                String data[] = {String.valueOf(3), graphFile, queryFile};
                obj.initiateWork(data);
                //System.out.println("Done");
                fillGraph(obj.listOfSolutions, noOfNodes);
                ground = new HashSet<>();
                while(!line.contains("T:"))  {
                    line = br.readLine();
                    ground.add(line);
                }
                boolean wrong = false;
                for(String comput: computed)    {
                    if(!ground.contains(comput))
                        wrong = true;
                }
                if(!wrong) {
                  //  System.out.println("Matched\n");
                    matched.add("matched");
                }
                else {
                   // System.out.println("Not matched\n");
                    matched.add("not matched");
                }

            }
        }
    }

    /**
     * This function is used to find a single instance of each graph in the groundtruth file that is provided.
     * @param fileName      the name of the ground truth file to be processed.
     * @param noOfNodes     used for formatting the output.
     * @param optimized     used to differentiate between optimized and naive processing.
     * @throws FileNotFoundException
     * @throws IOException
     */
    void findSolution(String fileName, int noOfNodes, boolean optimized) throws FileNotFoundException, IOException{
        String fileName1 = fileName;
        fileName = "/Users/shobhitgarg/Downloads/Proteins/Proteins/ground_truth/" + fileName;
        File file = new File(fileName);

        for(String graphType: graphTypes)   {
            boolean found = false;
            FileReader fileReader = new FileReader(file.getPath());
            BufferedReader br = new BufferedReader(fileReader);
            String line = null;
            while ((line = br.readLine()) != null) {
                if(line.contains("T:" + graphType)) {
                    // particular graph found
                    String graphFile = line.split("T:")[1];
                    line = br.readLine();
                    String queryFile = line.split("P:")[1];
                    line = br.readLine();
                    if(Integer.parseInt(line.split(":")[1]) <= 0)
                        continue;
                    found = true;
                    System.out.println("Graph file: " + graphFile);
                    System.out.println("Query file: " + queryFile);
                    String data[] = {String.valueOf(3), graphFile, queryFile};

                    if(optimized) {
                      MenuDrivenProfiledSubgraphMatching obj = new MenuDrivenProfiledSubgraphMatching();
                        obj.initiateWork(data);
                        fillGraph(obj.listOfSolutions, noOfNodes);
                        ground = new HashSet<>();
                        while(!line.contains("T:"))  {
                            line = br.readLine();
                            ground.add(line);
                        }
                        boolean wrong = false;
                        for(String comput: computed)    {
                            if(!ground.contains(comput))
                                wrong = true;
                        }
                        if(!wrong) {
                            System.out.println("Matched\n");
                            matched.add("matched");
                        }
                        else {
                            System.out.println("Not matched\n");
                            matched.add("not matched");
                        }

                        break;
                    }
                    else {
                        MenuDrivenSubgraphMatching obj = new MenuDrivenSubgraphMatching();
                        obj.initiateWork(data);
                        fillGraph(obj.listOfSolutions, noOfNodes);
                        ground = new HashSet<>();
                        while(!line.contains("T:"))  {
                            line = br.readLine();
                            ground.add(line);
                        }
                        boolean wrong = false;
                        for(String comput: computed)    {
                            if(!ground.contains(comput))
                                wrong = true;
                        }
                        if(!wrong) {
                            System.out.println("Matched\n");
                            matched.add("matched");
                        }
                        else {
                            System.out.println("Not matched\n");
                            matched.add("not matched");
                        }

                        break;
                    }


                    //System.out.println("Done");


                    //
                }
            }

            if(!found)
                System.out.println("No matchings of target and query found in ground truth for graph type: " + graphType + " in " + fileName1 + "\n");

            }

    }

    /**
     * Used to store the output of subgraph matching in a hashset such that it could be compared with the output in the ground truth
     * file in constant time.
     * @param listOfSolutions       list of different solutions.
     * @param noOfNodes             used for formatting.
     */
    void fillGraph(HashMap<Integer, HashMap<Integer, Long>> listOfSolutions, int noOfNodes)    {
        computed = new HashSet<>();
        for(int solnNo: listOfSolutions.keySet())   {
            StringBuilder sb = new StringBuilder("S:" + noOfNodes+":");
            HashMap<Integer, Long> soln = listOfSolutions.get(solnNo);
            for (int key: soln.keySet())    {
                sb.append(key+ "," + soln.get(key) + ";");
            }
            sb.setLength(sb.length() - 1);
            computed.add(sb.toString());
        }
    }
}
