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

class ImdbApiPersonBodyHandler implements Handler<Buffer> {

    private final Graph<ImdbEntity, DefaultEdge> graph;
    private final BlockingQueue<ImdbEntity> queue;

    ImdbApiPersonBodyHandler(BlockingQueue<ImdbEntity> queue, Graph<ImdbEntity, DefaultEdge> graph) {
        this.queue = queue;
        this.graph = graph;
    }

    @Override
    public void handle(Buffer buffer) {
        try {
            final JsonObject jsonObject = buffer.toJsonObject();
            final Person person = Person.fromRootJson(jsonObject);

            System.out.println("Handling person... ["+person.getId()+"]");

            graph.addVertex(person);

            final JsonObject filmography = jsonObject.getJsonObject("filmography");
            final JsonArray actorJson = filmography.getJsonArray("actor");
            Stream<Movie> movieStream = Stream.empty();
            if (actorJson!=null) {
                final Stream<Movie> actorStream = actorJson.stream().map(m -> Movie.fromJson((JsonObject) m));
                movieStream = Stream.concat(movieStream, actorStream);
            }
            final JsonArray actressJson = filmography.getJsonArray("actress");
            if (actressJson!=null) {
                final Stream<Movie> actressStream = actressJson.stream().map(m -> Movie.fromJson((JsonObject) m));
                movieStream = Stream.concat(movieStream, actressStream);
            }

            movieStream.forEach(m -> {
                final boolean added = graph.addVertex(m);
                graph.addEdge(person, m);
                if (added) {
                    queue.add(m);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
