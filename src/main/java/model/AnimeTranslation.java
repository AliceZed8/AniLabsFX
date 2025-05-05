package model;
import java.util.List;

public class AnimeTranslation {
    private int translator_id;
    private String translator_name;
    private List<Integer> episodes;

    public int getTranslator_id() {
        return translator_id;
    }

    public void setTranslator_id(int translator_id) {
        this.translator_id = translator_id;
    }

    public String getTranslator_name() {
        return translator_name;
    }

    public void setTranslator_name(String translator_name) {
        this.translator_name = translator_name;
    }

    public List<Integer> getEpisodes() {
        return episodes;
    }

    public void setEpisodes(List<Integer> episodes) {
        this.episodes = episodes;
    }
}
