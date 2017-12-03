package crawler.imdb.entity;

class AbsImdbEntity implements ImdbEntity {
    private final String id;
    private final String title;

    AbsImdbEntity(String id, String title) {
        if (id==null) throw new IllegalArgumentException("Null is not allowed for id");
        if (title==null) throw new IllegalArgumentException("Null is not allowed for title");

        this.id = id;
        this.title = title;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbsImdbEntity)) return false;

        AbsImdbEntity that = (AbsImdbEntity) o;

        if (!id.equals(that.id)) return false;
        return title.equals(that.title);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + title.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "{" +
                "\"type\": \"" + this.getClass().getSimpleName() + '\"' +
                ", \"id\": \"" + id + '\"' +
                ", \"title\": \"" + title + '\"' +
                '}';
    }
}
