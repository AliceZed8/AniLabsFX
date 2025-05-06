package utils;

import animation.Animations;
import controller.AnimeDetailController;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import manager.SceneState;
import manager.TabSceneManager;
import model.Anime;
import model.AnimeStatus;
import model.AnimeType;

import javafx.util.Duration;
import service.ApiService;

import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class AnimeUtils {

    static public final Map<String, String> statusMap = Map.of(
            "ongoing", "Онгоинг",
            "released", "Вышел",
            "anons", "Анонс"
    );

    static public final Map<String, String> typeMap = Map.of(
            "tv", "ТВ сериал",
            "ona", "ONA",
            "special", "Спэшл",
            "ova", "OVA",
            "movie", "Фильм",
            "tv_special", "ТВ-Спэшл",
            "pv", "PV",
            "cm", "CM",
            "music", "Музыка"
    );


    private static String plural(long number, String one, String few, String many) {
        number = number % 100;
        if (number >= 11 && number <= 19) return many;
        long i = number % 10;
        if (i == 1) return one;
        if (i >= 2 && i <= 4) return few;
        return many;
    }

    public static String toTimeAgo(String isoDateStr) {
        ZonedDateTime dateTime = ZonedDateTime.parse(isoDateStr);
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());

        java.time.Duration duration = java.time.Duration.between(dateTime, now);
        long seconds = duration.getSeconds();

        if (seconds < 60) {
            return "только что";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            return minutes + " " + plural(minutes, "минута", "минуты", "минут") + " назад";
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            return hours + " " + plural(hours, "час", "часа", "часов") + " назад";
        } else if (seconds < 172800) {
            return "вчера";
        } else if (seconds < 604800) {
            long days = seconds / 86400;
            return days + " " + plural(days, "день", "дня", "дней") + " назад";
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy г.", new Locale("ru"));
            return dateTime.format(formatter);
        }
    }



    public static String getNextEpisodeDay(String nextEpAt) {
        ZonedDateTime zdt = ZonedDateTime.parse(nextEpAt, DateTimeFormatter.ISO_DATE_TIME);
        DayOfWeek dayOfWeek = zdt.getDayOfWeek();
        return "выходит " + getDayName(dayOfWeek);
    }

    private static String getDayName(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "каждый понедельник";
            case TUESDAY -> "каждый вторник";
            case WEDNESDAY -> "каждую среду";
            case THURSDAY -> "каждый четверг";
            case FRIDAY -> "каждую пятницу";
            case SATURDAY -> "каждую субботу";
            case SUNDAY -> "каждое воскресенье";
            default -> "";
        };
    }


    static public String formatTime(long timeInNanos) {
        long timeInSeconds = TimeUnit.NANOSECONDS.toSeconds(timeInNanos);

        int minutes = (int) timeInSeconds / 60;
        int seconds = (int) timeInSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }


    // вспомогательные функции для получения id
    static public int getTypeIdByName(String typename) {
        for (AnimeType type: ApiService.filterParams.getTypes()) {
            if (type.getType_name().equals(typename)) return type.getId();
        }
        return -1;
    }

    static public int getStatusIdByName(String statusname) {
        for (AnimeStatus status: ApiService.filterParams.getStatuses()) {
            if (status.getStatus_name().equals(statusname)) return status.getId();
        }
        return -1;
    }

    static public String cleanText(String text) {
        if (text == null) return "";
        /* Текст ломающий врап :(
            Нико Вакацуки — юная ведьма, которая только-только закончила магическое обучение и нуждается в фамильяре.
            Выбор начинающей колдуньи пал на её друга детства по имени Морихито Отоги, который является о́ни и происходит из древнего рода фамильяров,
            издавна служившего ведьмам.
         */
        return text.replace('\u00A0', ' ')
                .replace("о\u0301", "ó") // кривой симврл
                .trim();
    }

    // тайл
    static public HBox createAnimeTile(Anime anime) {
        HBox tile = new HBox(10);
        tile.getStyleClass().add("anime-tile");
        tile.setPrefWidth(600);


        // Постер
        ImageView poster = new ImageView();
        poster.setPreserveRatio(true);
        poster.setFitWidth(200);
        poster.setCache(true);
        //poster.setSmooth(true);

        Image image = new Image(anime.getPosterUrl(), 200, 0, true, true, true);
        poster.setImage(image);

        image.progressProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() == 1.0) {
                double width = poster.getFitWidth();
                double height = width * image.getHeight() / image.getWidth();

                Rectangle clip = new Rectangle(width, height);
                clip.setArcWidth(16);
                clip.setArcHeight(16);
                poster.setClip(clip);
                Animations.FadeInScale(poster, 0.8, 1.0, Animations.DEFAULT_DURATION, Duration.ZERO);
            }
        });


        // Контент справа
        VBox infoBox = new VBox();
        infoBox.setSpacing(5);
        infoBox.setFillWidth(true);

        // название
        Label title = new Label(anime.getTitle());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        title.setWrapText(true);
        title.setMaxWidth(320);
        title.setMaxHeight(60); // примерно 2 строки
        title.setEllipsisString("...");


        // Доп информация
        String year = anime.getYear().toString();
        String type = AnimeUtils.typeMap.getOrDefault(anime.getTypeName().toLowerCase(), anime.getTypeName());
        String episodesInfo;

        if (anime.getStatusName().equalsIgnoreCase("ongoing")) {
            episodesInfo = anime.getEp_aired().toString() + " из " + (anime.getEp_total() != 0 ? anime.getEp_total().toString() : "?") + " эп.";
        } else episodesInfo = anime.getEp_total().toString() + " эп.";

        String total = String.join(" · ", year, type);
        if (!anime.getTypeName().equalsIgnoreCase("movie")) {
            total += " · " + episodesInfo;
        }

        Label info = new Label(total);
        info.setStyle("-fx-text-fill: #bbbbbb; -fx-font-size: 13px;");



        // описание
        Label description = new Label(anime.getDescription() != null ? cleanText(anime.getDescription()) : "Описание отсутствует.");
        description.setStyle("-fx-text-fill: #888888;");
        description.setWrapText(true);
        description.setMaxWidth(320);

        if (description.getText().length() > 200) {
            description.setText(description.getText().substring(0, 200) + "...");
        }



        infoBox.getChildren().addAll(title, info, description);
        tile.getChildren().addAll(poster, infoBox);

        tile.setOnMouseClicked(e -> {
            SceneState previous = TabSceneManager.get();
            SceneState current = TabSceneManager.create("/fxml/animeDetailScene.fxml", controller -> {
                if (controller instanceof AnimeDetailController detailController) {
                    detailController.setAnimeId(anime.getId());
                }
            });
            TabSceneManager.showCombined(previous.getNode(), current.getNode());

            Animations.FadeInSlideHorizontal(current.getNode(), 500, 0, Duration.millis(300), Duration.ZERO)
                    .setOnFinished(ee -> TabSceneManager.show());

        });
        return tile;
    }

    static public VBox createVTile(Anime anime) {
        VBox item = new VBox(10);
        item.setPrefHeight(300);
        item.getStyleClass().add("anime-v-tile");

        // Постер
        ImageView poster = new ImageView();
        poster.setFitWidth(160);
        poster.setPreserveRatio(true);
        poster.setCache(true);
        poster.setSmooth(true);

        Image image = new Image(anime.getPosterUrl(), true);
        poster.setImage(image);


        // Текст снизу
        Label title = new Label(anime.getTitle());
        title.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
        title.setMaxHeight(40);
        title.setWrapText(true);
        title.setEllipsisString("...");

        VBox titleBox = new VBox(title);
        titleBox.setPrefWidth(150);
        titleBox.setSpacing(5);

        if (anime.getTranslator_name() != null) {
            Label translationInfo = new Label();
            translationInfo.setWrapText(true);
            String translationInfoText= anime.getTranslator_name();
            if (anime.getLast_episode_num() != 0) {
                translationInfoText += " · " + anime.getLast_episode_num() + " эп.";
            }
            translationInfo.setText(translationInfoText);
            translationInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #888888;");

            Label translationInfoTime = new Label(AnimeUtils.toTimeAgo(anime.getLatest_episode_created_at()));
            translationInfoTime.setWrapText(true);
            translationInfoTime.setStyle("-fx-font-size: 12px; -fx-text-fill: #888888;");
            titleBox.getChildren().addAll(translationInfo, translationInfoTime);
        }

        image.progressProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() == 1.0) {
                double width = poster.getFitWidth();
                double height = width * image.getHeight() / image.getWidth();

                Rectangle clip = new Rectangle(width, height);
                clip.setArcWidth(16);
                clip.setArcHeight(16);
                poster.setClip(clip);
                Animations.FadeInScale(poster, 0.8, 1.0, Animations.DEFAULT_DURATION, Duration.ZERO);


                title.setMaxWidth(width);
            }
        });

        item.setOnMouseClicked(e -> {
            SceneState previous = TabSceneManager.get();
            SceneState current = TabSceneManager.create("/fxml/animeDetailScene.fxml", controller -> {
                if (controller instanceof AnimeDetailController detailController) {
                    detailController.setAnimeId(anime.getId());
                }
            });
            TabSceneManager.showCombined(previous.getNode(), current.getNode());

            Animations.FadeInSlideHorizontal(current.getNode(), 500, 0, Duration.millis(300), Duration.ZERO)
                    .setOnFinished(ee -> TabSceneManager.show());

        });

        item.getChildren().addAll(poster, titleBox);
        return item;
    }
}
