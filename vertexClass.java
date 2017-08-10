package assignment4;

import org.neo4j.cypher.internal.frontend.v2_3.ast.In;
import org.neo4j.cypher.internal.frontend.v2_3.ast.functions.Str;
import org.neo4j.graphdb.Label;
import org.neo4j.register.Register;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by shobhitgarg on 3/23/17.
 */

/**
 * This class represents a node in the query graph. It is used to store the query graph in memory.
 */
public class vertexClass {
    int id1;
    boolean isProtein = false;
    String label;
    ArrayList<Integer> labels = new ArrayList<>();
    HashMap<Integer, Integer> edges = new HashMap<>();


}
