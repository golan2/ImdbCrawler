package crawler.imdb;


import crawler.imdb.entity.ImdbEntity;
import crawler.imdb.entity.Movie;
import crawler.imdb.entity.Person;

public class ImdbUriFactory {
    private static final String imdbPrefix = "http://www.imdb.com";
    private static final String imdbPersonPrefix = "/name/";

    private static final String imdbApiPrefix = "http://www.theimdbapi.org";
    private static final String imdbApiPersonById = "/api/person?person_id=";
    private static final String imdbApiMovieById = "/api/movie?movie_id=";

    public static String getPersonIdFromImdbUrl(String url) {
        if (url==null) return null;

        final int beginIndex = imdbPrefix.length() + imdbPersonPrefix.length();
        if (beginIndex>url.length()) return null;

        final int endIndex = url.indexOf("/", beginIndex + 1);
        if (endIndex>url.length()) return null;

        return url.substring(beginIndex, endIndex);
    }

    public static String getImdbApiPrefix() {
        return imdbApiPrefix;
    }

    /**
     * Only the uri not the full url.
     * We assume that the HttpClient is initialized with the base url of the imdb api
     * @param imdbEntity the entity we want (Movie, Person, ...)
     * @return the uri suffix to get json of this imdbEntity
     */
    public static String createUri(ImdbEntity imdbEntity) {
        if (imdbEntity instanceof Person) {
            return _create((Person) imdbEntity);
        }
        if (imdbEntity instanceof Movie) {
            return _create((Movie) imdbEntity);
        }
        throw new IllegalArgumentException("Unrecognized class:" + imdbEntity.getClass().getName() );
    }

    private static String _create(Person person) {
        return imdbApiPersonById + person.getId();
    }

    private static String _create(Movie movie) {
        return imdbApiMovieById + movie.getId();
    }


}
