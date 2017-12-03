package crawler.imdb.entity;

import io.vertx.core.json.JsonObject;

public class Movie extends AbsImdbEntity {

    Movie(String id, String title) {
        super(id, title);
    }

    public static Movie fromJson(JsonObject j) {
        return new Movie(j.getString("imdb_id"), j.getString("title"));
    }
}
