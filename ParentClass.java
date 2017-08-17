import java.io.IOException;
import java.util.Scanner;

/**
 * Created by shobhitgarg on 3/31/17.
 */
public class ParentClass {
    public static void main(String[] args) throws IOException{
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the path where the new database should be loaded: ");
        String newDBPath = sc.next();
        System.out.println("Enter the folder path where protein and Igraph databases are located in your system (Do not change any folder names! ): ");
        String path = sc.next();
        ETL_LoadGraphsInNeo4j obj1 = new ETL_LoadGraphsInNeo4j();
        obj1.insertInGraphDB(path, newDBPath);

        while (true) {
            System.out.println("Enter the question you want to execute (Possible choices are \n2: naive subgraph matching\n3: optimized subgraph matching\n4: comparison in regards to Protein graph\n5: comparison in regards to Igraph).\nTo exit, enter 6");
            int choice = sc.nextInt();
            if(choice == 6)
                System.exit(1);
            else if(choice == 2)    {
                // execute 2
                System.out.println("\nEnter the target file name: ");
                String target = sc.next();
                System.out.println("\nEnter the query/pattern file name: ");
                String pattern = sc.next();


            }
            else if(choice == 3)    {

            }
            else if(choice == 4)    {

            }
            else if(choice == 5)    {

            }
            else
                System.out.println("Enter the correct choice\n");
        }
    }
}
