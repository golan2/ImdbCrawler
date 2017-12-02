import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class PlayWithJGraphT {


    public static void main(String[] args) {
        final Graph<String, DefaultEdge> g = createHrefGraph();
        System.out.println(g.toString());

        System.out.println(g.containsEdge("yahoo", "amazon"));
        System.out.println(g.containsEdge("amazon", "yahoo"));

    }


    /***
     ** Creates a toy directed graph based on URL objects that represents link
     ** structure.
     **
     ** @return a graph based on URL objects.
     **/
    private static Graph<String, DefaultEdge> createHrefGraph()
    {
        Graph<String, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);




            // add the vertices
            g.addVertex("amazon");
            g.addVertex("yahoo");
            g.addVertex("ebay");

            // add edges to create linking structure
            g.addEdge("yahoo", "amazon");
            g.addEdge("yahoo", "ebay");

        return g;
    }


}
