package crawler.imdb;

import org.jgrapht.EdgeFactory;

public class RelationFactory implements EdgeFactory<String, Relation> {

    @Override
    public Relation createEdge(String sourceVertex, String targetVertex) {
        return new Relation();
    }
}
