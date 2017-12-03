package crawler.url;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

class UrlCrawler {


    private static final int URL_CACHE_SIZE = 50000;
    private static final int HTTP_CLIENT_CACHE = 25;

    public static void main(String[] args) throws URISyntaxException, InterruptedException {


        final BlockingQueue<URI>         queue       = new ArrayBlockingQueue<>(500000);
        final Graph<String, DefaultEdge> graph       = new DefaultDirectedGraph<>(DefaultEdge.class);
        final Vertx                      vertx       = Vertx.vertx();
        final Cache                      clientCache = createHttpClientCache();
        final Cache                      urlCache    = createUrlCache       ();


        vertx.exceptionHandler(Throwable::printStackTrace);
        urlCache.put(new net.sf.ehcache.Element("", ""));






        final URI root = new URI("http://www.abc.net.au/news/2017-12-01/heres-what-would-happen-if-the-bitcoin-bubble-burst/9201942");
        queue.add(root);
        graph.addVertex(root.getHost());

        while (!queue.isEmpty()) {
            final URI uri = queue.poll();
            System.out.println("Visit: " + uri);
            if ("http".equals(uri.getScheme())) {
                final HttpClientRequest request = createHttpClientRequest(vertx, clientCache, uri);
                request.handler(response -> {
//                    System.out.println("Response ["+response.statusCode()+"] to [" + uri.toString() + "]");
                    if (200 <= response.statusCode() && response.statusCode() < 300) {
                        response.bodyHandler(new Handler<Buffer>() {
                            @Override
                            public void handle(Buffer buffer) {
//                                System.out.println("Response size ["+buffer.length()+"] to [" + uri.toString() + "]");
                                final Document doc = Jsoup.parse(buffer.toString());
                                final Elements links = doc.select("a[href]");
                                int count = 0;
                                for (Element link : links) {
                                    final String href = link.attr("href");
                                    try {
                                        final URI u = new URI(href);
                                        if ("http".equals(u.getScheme())) {
                                            graph.addVertex(u.getHost());
                                            graph.addEdge(uri.getHost(), u.getHost());
                                            queue.offer(u);
                                            count++;
                                        }
                                    } catch (URISyntaxException ignored) {
                                    }
                                }
                                System.out.println("Finishing ["+count+"]: " + uri);
                            }
                        });
                        response.exceptionHandler(Throwable::printStackTrace);
                    }
                });
                request.exceptionHandler(Throwable::printStackTrace);
                request.end();
//                System.out.println("Request sent [" + uri.toString() + "]");

                if (graph.vertexSet().size()>200) break;
                if (queue.isEmpty()) Thread.sleep(5000);
            }
        }





        int finished=30;
        while (finished-->0) {
            System.out.println("Waiting... ["+finished+"]");
            Thread.sleep(1000);
        }

        System.out.println(graph.toString());

        System.out.println("Closing....");
        vertx.close();

    }

    private static Cache createHttpClientCache() {
        final CacheManager cacheManager = CacheManager.getInstance();
        cacheManager.addCache(new Cache("httpClientCahce", HTTP_CLIENT_CACHE, false, false, Integer.MAX_VALUE, 10));
        return cacheManager.getCache("httpClientCahce");
    }

    private static Cache createUrlCache() {
        final CacheManager cacheManager = CacheManager.getInstance();
        cacheManager.addCache(new Cache("urlCache", URL_CACHE_SIZE, false, false, Integer.MAX_VALUE, Integer.MAX_VALUE));
        return cacheManager.getCache("urlCache");
    }

    private static HttpClientRequest createHttpClientRequest(Vertx vertx, Cache clientCache, URI uri) {
        final String host = uri.getHost();
        net.sf.ehcache.Element element = clientCache.get(host);
        final HttpClient client;
        if (element == null) {
            HttpClientOptions options = new HttpClientOptions()
                    .setDefaultHost(host)
                    .setDefaultPort(getPort(uri));
            client = vertx.createHttpClient(options);
            clientCache.put(new net.sf.ehcache.Element(host, client));
            System.out.println("Client created [" + host + "] [" + client.hashCode() + "]");
        }
        else {
            client = (HttpClient) element.getObjectValue();
        }
        return client.get(uri.getPath());
    }

    private static int getPort(URI uri) {
        if (uri.getPort()>0) return uri.getPort();
        if ("http".equals(uri.getScheme())) return 80;
        if ("https".equals(uri.getScheme())) return 443;
        return -1;
    }


}
