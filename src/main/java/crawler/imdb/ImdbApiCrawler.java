package crawler.imdb;

import crawler.imdb.entity.ImdbEntity;
import crawler.imdb.entity.Person;
import crawler.imdb.handlers.ImdbApiHandlerFactory;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ImdbApiCrawler {

    public static void main(String[] args) throws URISyntaxException, InterruptedException {

        final BlockingQueue<ImdbEntity>      queue          = new ArrayBlockingQueue<>(5000);
        final Graph<ImdbEntity, DefaultEdge> graph          = new DefaultDirectedGraph<>(DefaultEdge.class);
        final Vertx                          vertx          = Vertx.vertx();
        final ImdbApiHandlerFactory          handlerFactory = ImdbApiHandlerFactory.createInstance(queue, graph);

        vertx.exceptionHandler(Throwable::printStackTrace);

        HttpClientOptions options = new HttpClientOptions()
                .setDefaultHost(new URI(ImdbUriFactory.getImdbApiPrefix()).getHost())
                .setDefaultPort(80);
        HttpClient client = vertx.createHttpClient(options);

        queue.offer(new Person("nm4793928", ""));
        while (!queue.isEmpty()) {
            final ImdbEntity imdbEntity = queue.poll();
            final String url = ImdbUriFactory.createUri(imdbEntity);
            final HttpClientRequest request = client.get(url);
            final Handler<Buffer> handler = handlerFactory.getHandler(imdbEntity);
            request.handler(response -> {
                if (200 <= response.statusCode() && response.statusCode() < 300) {
                    response.bodyHandler(handler);
                }
            });
            request.putHeader("content-type", "application/json; charset=utf-8");
            System.out.println("queue.poll ["+imdbEntity.getId()+"] Sending... url=["+url+"] type=["+handler.getClass().getSimpleName()+"]");
            request.end();

            System.out.println("Graph=["+graph.vertexSet().size()+"] Queue=["+queue.size()+"]");

            if (graph.vertexSet().size()>20) break;
            if (queue.isEmpty()) Thread.sleep(5000);
        }

        int finished=30;
        while (finished-->0) {
            System.out.println("Waiting... ["+finished+"]");
            Thread.sleep(1000);
        }

        System.out.println(graph.toString());

        client.close();
        vertx.close();
    }

}
