package crawler;

import crawler.imdb.ImdbEntity;
import crawler.imdb.Movie;
import crawler.imdb.Person;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PageParser {


    public static void main(String[] args) throws URISyntaxException, InterruptedException {

        final BlockingQueue<String>          queue = new ArrayBlockingQueue<>(5000);
        final Graph<ImdbEntity, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        final Vertx                          vertx = Vertx.vertx();


        HttpClientOptions options = new HttpClientOptions()
                .setDefaultHost("localhost")
                .setDefaultPort(8082);
        HttpClient client = vertx.createHttpClient(options);

        queue.offer("/api/find/person?name=Shia%20LaBeouf");
        while (!queue.isEmpty()) {
            final String uri = queue.poll();
            System.out.println( "URI = " + uri);
            final HttpClientRequest request = client.get(uri);
            request.handler(response -> {
                if (200 <= response.statusCode() && response.statusCode() < 300) {
                    response.bodyHandler(new ImdbApiBodyHandler(graph));
                }
            });
            request.putHeader("content-type", "application/json; charset=utf-8");
            request.end();
        }

        int finished=30;
        while (finished-->0) {
            System.out.println("Waiting... ["+finished+"]");
            Thread.sleep(1000);
        }
        client.close();
        vertx.close();
    }

    private static class ImdbApiBodyHandler implements Handler<Buffer> {

        private final Graph<ImdbEntity, DefaultEdge> graph;

        public ImdbApiBodyHandler(Graph<ImdbEntity, DefaultEdge> graph) {
            this.graph = graph;
        }

        @Override
        public void handle(Buffer buffer) {
//            final LinkedHashMap root = (LinkedHashMap) buffer.toJsonArray().getList().iterator().next();
            final LinkedHashMap root = (LinkedHashMap) buffer.toJsonArray().getList().iterator().next();

            //if it is a person
            final Person person = Person.fromMap(root);
            graph.addVertex(person);
            final LinkedHashMap filmography = (LinkedHashMap) root.get("filmography");
            final List<Map> actor = (List<Map>) filmography.get("actor");
            final List<Map> actress = (List<Map>) filmography.get("actress");
            final ArrayList<Movie> movies = new ArrayList<>();
            if (actor!=null) {
                final Stream<Map> stream = actor.stream();
                final Stream<Movie> movieStream = stream.map(Movie::fromMap);

                final List<Movie> collect = movieStream.collect(Collectors.toList());


                throw new RuntimeException("CONTINUE HERE");
            }
            if (actress!=null) {
                final Stream<Map> stream = actress.stream();
                final Stream<Movie> movieStream = stream.map(Movie::fromMap);
                final List<Movie> collect = movieStream.collect(Collectors.toList());
            }

        }
    }

}
