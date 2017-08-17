import java.io.IOException;
import java.util.HashMap;

/**
 * Created by shobhitgarg on 3/30/17.
 */

/**
 * this class is used to run naive and optimized subgraph matching on the Igraphs.
 */
public class SubgraphMatchingOnIgraphs {
    HashMap<String, Integer> ans = new HashMap<>();
    String files[] = {"human_q10.igraph", "yeast_q10.igraph"};
    public static void main(String[] args) throws IOException {
        SubgraphMatchingOnIgraphs obj = new SubgraphMatchingOnIgraphs();
        for(String file: obj.files)
        obj.compareIgraphExecutions(file);
    }

    /**
     * This function compares the performance of subgraph matching on Igraphs between naive and optimized implementation.
     * It finds an instance where naive performed better than optimized and vice versa.
     * @param fileName      used to determine the type of igraph to be tested upon.
     * @throws IOException
     */
    void compareIgraphExecutions(String fileName) throws IOException {

            for(int i = 0; i<= 999; i++)    {
                MenuDrivenSubgraphMatching obj = new MenuDrivenSubgraphMatching();
                String fileType = null;
                if(fileName.contains("human"))
                    fileType = "1";
                else
                    fileType = "2";
                String []data = {fileType, String.valueOf(i)};
                long t1 = System.currentTimeMillis();
                obj.initiateWork(data);
                long t2 = System.currentTimeMillis();
                long diffNaive = t2 - t1;
                MenuDrivenProfiledSubgraphMatching obj1 = new MenuDrivenProfiledSubgraphMatching();
                t1 = System.currentTimeMillis();
                obj1.initiateWork(data);
                t2 = System.currentTimeMillis();
                long diffopti = t2 - t1;
                if(diffNaive>diffopti)  {
                    if(!ans.containsKey("naive")) {
                        System.out.println("\n\nOptimal execution better than naive for file : " + fileName  + " found for query number: " + i + "   Time taken for Naive: " + diffNaive + "  Time taken for Optimal:"+ diffopti + "\n\n");
                        ans.put("naive", i);
                    }
                }
                else {
                    if(!ans.containsKey("opti")) {
                        System.out.println("\n\nNaive execution better than optimal for file : " + fileName +" found for query number: " + i + "   Time taken for Naive: " + diffNaive + "  Time taken for Optimal:"+ diffopti + "\n\n");
                        ans.put("opti", i);
                    }
                }
            if(ans.size()== 2) {
             ans = new HashMap<>();
                break;
            }

        }



    }
}
