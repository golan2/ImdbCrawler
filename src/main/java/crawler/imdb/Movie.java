package crawler.imdb;

import java.util.Map;

public class Movie extends AbsImdbEntity {

    Movie(String id, String title) {
        super(id, title);
    }

    public static Movie fromMap(Map<String, String> map) {
        return new Movie(map.get("imdb_id"), map.get("title"));
    }
}
