package crawler.imdb.entity;

import crawler.imdb.ImdbUriFactory;
import io.vertx.core.json.JsonObject;

public class Person extends AbsImdbEntity {

    public Person(String id, String title) {
        super(id, title);
    }

    /**
     * When parsing a person json which is a result of: http://www.theimdbapi.org/api/person?person_id=
     * The person is the root object of the json we parse.
     */
    public static Person fromRootJson(JsonObject j) {
        return new Person(j.getString("person_id"), j.getString("title"));
    }

    /**
     * When parsing the "cast" json where the movie is the root object (http://www.theimdbapi.org/api/movie?movie_id=)
     */
    public static Person fromCastJson(JsonObject j) {
        try {
            final String imdbUrl = j.getString("link");
            final String id = ImdbUriFactory.getPersonIdFromImdbUrl(imdbUrl);
            return new Person(id, j.getString("name"));
        } catch (Exception e) {
            return new UnparssedPerson(j.toString());
        }
    }

    /**
     * Store the original json on the id field
     */
    private static class UnparssedPerson extends Person {
        UnparssedPerson(String json) {
            super(json, "UnparssedPerson");
        }

    }
}