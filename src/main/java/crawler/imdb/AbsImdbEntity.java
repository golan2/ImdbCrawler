package crawler.imdb;

class AbsImdbEntity implements ImdbEntity {
    final String name;

    AbsImdbEntity(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
