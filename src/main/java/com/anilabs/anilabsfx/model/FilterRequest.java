package com.anilabs.anilabsfx.model;

import java.util.List;

public class FilterRequest {
    private Integer count;
    private Integer offset;

    private Integer yearFrom;
    private Integer yearTo;

    private List<Integer> types;
    private List<Integer> statuses;
    private List<Integer> genres;
    private List<Integer> studios;
    private List<Integer> translators;


    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getYearFrom() {
        return yearFrom;
    }

    public void setYearFrom(Integer yearFrom) {
        this.yearFrom = yearFrom;
    }

    public Integer getYearTo() {
        return yearTo;
    }

    public void setYearTo(Integer yearTo) {
        this.yearTo = yearTo;
    }

    public List<Integer> getTypes() {
        return types;
    }

    public void setTypes(List<Integer> types) {
        this.types = types;
    }

    public List<Integer> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<Integer> statuses) {
        this.statuses = statuses;
    }

    public List<Integer> getGenres() {
        return genres;
    }

    public void setGenres(List<Integer> genres) {
        this.genres = genres;
    }

    public List<Integer> getStudios() {
        return studios;
    }

    public void setStudios(List<Integer> studios) {
        this.studios = studios;
    }

    public List<Integer> getTranslators() {
        return translators;
    }

    public void setTranslators(List<Integer> translators) {
        this.translators = translators;
    }
}
