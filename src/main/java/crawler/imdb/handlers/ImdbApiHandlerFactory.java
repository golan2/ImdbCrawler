package crawler.imdb.handlers;

import crawler.imdb.entity.ImdbEntity;
import crawler.imdb.entity.Movie;
import crawler.imdb.entity.Person;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class ImdbApiHandlerFactory {

    private final BlockingQueue<ImdbEntity>      queue;
    private final Graph<ImdbEntity, DefaultEdge> graph;
    private final Map<String, Handler<Buffer>>   handlersMap;

    private ImdbApiHandlerFactory(BlockingQueue<ImdbEntity> queue, Graph<ImdbEntity, DefaultEdge> graph) {
        this.queue = queue;
        this.graph = graph;
        this.handlersMap = new HashMap<>();
    }

    public static ImdbApiHandlerFactory createInstance(BlockingQueue<ImdbEntity> queue, Graph<ImdbEntity, DefaultEdge> graph) {
        return new ImdbApiHandlerFactory(queue, graph);
    }


    public Handler<Buffer> getHandler(ImdbEntity imdbEntity) {
        final String className = imdbEntity.getClass().getSimpleName();
        Handler<Buffer> handler = handlersMap.get(className);
        if (handler!=null) return handler;


        //todo: consider using synchronize here or accept the fact that we will maybe createUri several handlers but only one can be in the map so over time...should be ok(?)
        if (imdbEntity instanceof Person) {
            handler = new ImdbApiPersonBodyHandler(queue, graph);
        }
        else if (imdbEntity instanceof Movie) {
            handler = new ImdbApiMovieBodyHandler(queue, graph);
        }
        else {
            throw new IllegalArgumentException("Unknown type: " + className);
        }
        handlersMap.put(className, handler);
        return handler;
    }


    /**
     * @deprecated Use the non static way instead of creating new handlers every timee
     * @see #createInstance(BlockingQueue, Graph)
     * @see #getHandler(ImdbEntity)
     */
    public static Handler<Buffer> createHandler(BlockingQueue<ImdbEntity> queue, Graph<ImdbEntity, DefaultEdge> graph, ImdbEntity imdbEntity) {
        final Handler<Buffer> handler;
        if (imdbEntity instanceof Person) {
            handler = new ImdbApiPersonBodyHandler(queue, graph);
        }
        else if (imdbEntity instanceof Movie) {
            handler = new ImdbApiMovieBodyHandler(queue, graph);
        }
        else {
            throw new IllegalArgumentException("Unknown type: " + imdbEntity.getClass().getName());
        }
        return handler;
    }
}
