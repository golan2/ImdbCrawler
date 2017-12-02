package crawler;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
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

public class PageParser {


    public static void main(String[] args) throws URISyntaxException, InterruptedException {

        final BlockingQueue<String>      queue  = new ArrayBlockingQueue<>(5000);
        final Graph<String, DefaultEdge> g      = new DefaultDirectedGraph<>(DefaultEdge.class);
        final Vertx                      vertx  = Vertx.vertx();


        HttpClientOptions options = new HttpClientOptions()
                .setDefaultHost("localhost")
                .setDefaultPort(8082);
        HttpClient client = vertx.createHttpClient(options);

        queue.offer("/api/find/person?name=Shia%20LaBeouf");
        while (!queue.isEmpty()) {
            final String uri = queue.poll();
            System.out.println( "URI = " + uri);
            final HttpClientRequest request = client.get(uri, response -> {
                System.out.println(response.statusCode());
                if (200 <= response.statusCode() && response.statusCode() < 300) {
                    response.bodyHandler(buffer -> {
                        final LinkedHashMap root = (LinkedHashMap) buffer.toJsonArray().getList().iterator().next();

                        final String title = (String) root.get("title");
                        final String id = (String) root.get("person_id");
                        final Person person = new Person(id, title);
                        final LinkedHashMap filmography =  (LinkedHashMap) root.get("filmography");
                        final List<Map> actor = (List<Map>) filmography.get("actor");
                        final List<Movie> actorim = actor.stream().map(Movie::fromMap).collect(Collectors.toList());
                        

                    });
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
//        System.out.println(queue.stream().collect(Collectors.joining("\n")));
//        System.out.println(
//                uris.values().stream().map(v -> "["+v.count.get()+"] " + v.url).collect(Collectors.joining("\n"))
//        );
        client.close();
        vertx.close();
    }

//    private static class PersonParser {
//        private static
//    }

    private static class Person {
        private final String fullName;
        private final String id;
        private final List<Movie> actorList = new ArrayList<>();

        private Person(String id, String fullName) {
            this.fullName = fullName;
            this.id = id;
        }
    }


    private static class Movie {
        private String url;

        private static Movie fromMap(Map<String, String> map) {
            final Movie result = new Movie();
            result.url = map.get("url");
            return result;
        }
    }
}
