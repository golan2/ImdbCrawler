package crawler.imdb.handlers;

import crawler.imdb.entity.ImdbEntity;
import crawler.imdb.entity.Movie;
import crawler.imdb.entity.Person;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.concurrent.BlockingQueue;
import java.util.stream.Stream;

class ImdbApiMovieBodyHandler implements Handler<Buffer> {

    private final Graph<ImdbEntity, DefaultEdge> graph;
    private final BlockingQueue<ImdbEntity> queue;

    ImdbApiMovieBodyHandler(BlockingQueue<ImdbEntity> queue, Graph<ImdbEntity, DefaultEdge> graph) {
        this.queue = queue;
        this.graph = graph;
    }

    @Override
    public void handle(Buffer buffer) {
        try {
            final JsonObject jsonObject = buffer.toJsonObject();
            final Movie movie = Movie.fromJson(jsonObject);
            System.out.println("Handling movie... ["+movie.getId()+"]");
            graph.addVertex(movie);
            final JsonArray cast = jsonObject.getJsonArray("cast");
            final Stream<Person> actors = cast.stream().map(p -> Person.fromCastJson((JsonObject) p));
            actors.forEach(p -> {
                final boolean added = graph.addVertex(p);
                graph.addEdge(p, movie);
                if (added) {
                    queue.add(p);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
