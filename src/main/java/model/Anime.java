package model;

import java.util.List;
import java.util.Map;

public class Anime {
    private Integer id;
    private String title;
    private String orig_title;
    private String poster_url;
    private String description;

    private Integer year;
    private Integer type_id;
    private Integer status_id;
    private Integer shikimori_id;
    private Integer kinopoisk_id;

    private Integer ep_aired;
    private Integer ep_total;
    private Integer duration;
    private Integer min_age;

    private String status_name;
    private String type_name;

    private String aired_at;
    private String next_ep_at;
    private List<Genre> genres;
    private List<Studio> studios;

    // slider
    private String slider_poster_url;

    // last Updated
    private String translator_name;
    private Integer last_episode_num;
    private String latest_episode_created_at;

    // info
    private List<String> screenshots;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOrigTitle() {
        return orig_title;
    }

    public void setOrigTitle(String orig_title) {
        this.orig_title = orig_title;
    }

    public String getPosterUrl() {
        return poster_url;
    }

    public void setPosterUrl(String poster_url) {
        this.poster_url = poster_url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getType_id() {
        return type_id;
    }

    public void setType_id(Integer type_id) {
        this.type_id = type_id;
    }

    public Integer getStatus_id() {
        return status_id;
    }

    public void setStatus_id(Integer status_id) {
        this.status_id = status_id;
    }

    public Integer getShikimori_id() {
        return shikimori_id;
    }

    public void setShikimori_id(Integer shikimori_id) {
        this.shikimori_id = shikimori_id;
    }

    public Integer getKinopoisk_id() {
        return kinopoisk_id;
    }

    public void setKinopoisk_id(Integer kinopoisk_id) {
        this.kinopoisk_id = kinopoisk_id;
    }

    public Integer getEp_aired() {
        return ep_aired;
    }

    public void setEp_aired(Integer ep_aired) {
        this.ep_aired = ep_aired;
    }

    public Integer getEp_total() {
        return ep_total;
    }

    public void setEp_total(Integer ep_total) {
        this.ep_total = ep_total;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getMin_age() {
        return min_age;
    }

    public void setMin_age(Integer min_age) {
        this.min_age = min_age;
    }

    public String getStatusName() {
        return status_name;
    }

    public void setStatus_name(String status_name) {
        this.status_name = status_name;
    }

    public String getTypeName() {
        return type_name;
    }

    public void setType_name(String type_name) {
        this.type_name = type_name;
    }

    public String getAired_at() {
        return aired_at;
    }

    public void setAired_at(String aired_at) {
        this.aired_at = aired_at;
    }

    public String getNext_ep_at() {
        return next_ep_at;
    }

    public void setNext_ep_at(String next_ep_at) {
        this.next_ep_at = next_ep_at;
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSlider_poster_url() {
        return slider_poster_url;
    }

    public void setSlider_poster_url(String slider_poster_url) {
        this.slider_poster_url = slider_poster_url;
    }

    public String getTranslator_name() {
        return translator_name;
    }

    public void setTranslator_name(String translator_name) {
        this.translator_name = translator_name;
    }

    public Integer getLast_episode_num() {
        return last_episode_num;
    }

    public void setLast_episode_num(Integer last_episode_num) {
        this.last_episode_num = last_episode_num;
    }

    public String getLatest_episode_created_at() {
        return latest_episode_created_at;
    }

    public void setLatest_episode_created_at(String latest_episode_created_at) {
        this.latest_episode_created_at = latest_episode_created_at;
    }

    public List<String> getScreenshots() {
        return screenshots;
    }

    public void setScreenshots(List<String> screenshots) {
        this.screenshots = screenshots;
    }
}

