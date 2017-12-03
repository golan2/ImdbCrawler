package crawler.imdb;

import java.util.Map;

public class Person extends AbsImdbEntity {

    private Person(String id, String title) {
        super(id, title);
    }

    public static Person fromMap(Map<String, String> map) {
        return new Person(map.get("person_id"), map.get("title"));
    }
}