package model;

import java.util.List;

public class FilterParams {
    private List<AnimeType> types;
    private List<AnimeStatus> statuses;
    private List<Genre> genres;
    private List<Studio> studios;
    private List<Translator> translators;



    public List<AnimeType> getTypes() {
        return types;
    }

    public void setTypes(List<AnimeType> types) {
        this.types = types;
    }

    public List<AnimeStatus> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<AnimeStatus> statuses) {
        this.statuses = statuses;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }

    public List<Studio> getStudios() {
        return studios;
    }

    public void setStudios(List<Studio> studios) {
        this.studios = studios;
    }

    public List<Translator> getTranslators() {
        return translators;
    }

    public void setTranslators(List<Translator> translators) {
        this.translators = translators;
    }
}
