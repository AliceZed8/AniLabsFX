package com.anilabs.anilabsfx.controller;

import com.anilabs.anilabsfx.animation.Animations;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.anilabs.anilabsfx.manager.SceneState;
import com.anilabs.anilabsfx.manager.TabSceneManager;
import com.anilabs.anilabsfx.model.Anime;
import com.anilabs.anilabsfx.model.AnimeTranslation;
import org.freedesktop.gstreamer.elements.AppSink;
import org.freedesktop.gstreamer.elements.PlayBin;
import org.freedesktop.gstreamer.Buffer;
import org.freedesktop.gstreamer.Caps;
import org.freedesktop.gstreamer.FlowReturn;
import org.freedesktop.gstreamer.Sample;
import org.freedesktop.gstreamer.Structure;
import org.freedesktop.gstreamer.event.SeekFlags;
import com.anilabs.anilabsfx.service.ApiService;
import org.freedesktop.gstreamer.*;
import com.anilabs.anilabsfx.service.DbService;
import com.anilabs.anilabsfx.utils.AnimeUtils;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;


public class PlayerController {
    private final ApiService apiService = ApiService.getInstance();
    private final DbService dbService = DbService.getInstance();

    @FXML private StackPane mainPane;
    @FXML private HBox playerTopPanel;
    @FXML private HBox playerCenterPanel;
    @FXML private HBox playerBottomPanel;
    @FXML private HBox centerControlBox;

    @FXML private Label animeTitle;
    @FXML private Label episodeInfo;

    @FXML private ComboBox<String> qualitySelector;
    @FXML private ProgressIndicator playerLoadingIndicator;


    @FXML private HBox playSvg;
    @FXML private HBox pauseSvg;
    @FXML private HBox playSvgMini;
    @FXML private HBox pauseSvgMini;

    @FXML private StackPane volumeButtonPane;

    @FXML private HBox volumeButton;
    @FXML private HBox volumeMuteButton;
    @FXML private Slider volumeSlider;
    @FXML private VBox volumeBox;
    private boolean isMuted = false;


    @FXML private Label currentTimeLabel;
    @FXML private Slider timeSlider;
    @FXML private Label totalTimeLabel;

    private Timeline inactivityTimer;

    private Anime anime;
    private AnimeTranslation translation;
    private Integer episode;


    // GStreamer
    @FXML private ImageView imageView;
    private AtomicReference<Sample> pending;
    private Sample activeSample;
    private Buffer activeBuffer;
    private PlayBin playBin;
    private AppSink sink;




    public void initialize() {
        System.out.println("Player init...");

        // комбо бокс с качествами
        qualitySelector.getItems().addAll("360p", "480p", "720p");
        qualitySelector.setValue("360p");

        qualitySelector.setOnAction(e -> {
            String selectedQuality = qualitySelector.getValue();
            if (selectedQuality != null) {
                long start = (long) timeSlider.getValue();
                loadEpisode(episode, selectedQuality.replace("p", ""), start);
            }
        });


        // листенеры на активность
        mainPane.setOnMouseMoved(e -> resetInactivityTimer());
        mainPane.setOnMouseClicked(e -> resetInactivityTimer());


        // бинд центральных кнопок на индикатор
        centerControlBox.visibleProperty().bind(playerLoadingIndicator.visibleProperty().not());
        pauseSvg.visibleProperty().bind(playSvg.visibleProperty().not());
        pauseSvgMini.visibleProperty().bind(pauseSvg.visibleProperty());
        playSvgMini.visibleProperty().bind(playSvg.visibleProperty());

        // анимации
        Animations.FadeInSlideVertical(playerTopPanel, -50, 0, Animations.DEFAULT_DURATION, Duration.ZERO);
        Animations.FadeInScale(playerCenterPanel, 0.7, 1.0, Animations.DEFAULT_DURATION, Duration.ZERO);
        Animations.FadeInSlideVertical(playerBottomPanel, 50, 0, Animations.DEFAULT_DURATION, Duration.ZERO);



        // настройки для гуи громкости
        volumeMuteButton.visibleProperty().bind(volumeButton.visibleProperty().not());

        PauseTransition hideVolumeBoxDelay = new PauseTransition(Duration.millis(500));
        volumeButtonPane.setOnMouseEntered(e -> {
            hideVolumeBoxDelay.stop();
            if (!volumeBox.isVisible()) {
                volumeBox.setVisible(true);
                Animations.FadeInScale(volumeBox, 0.7, 1.0, Animations.DEFAULT_DURATION, Duration.ZERO);
            }
        });
        volumeButtonPane.setOnMouseExited(e-> {
            hideVolumeBoxDelay.setOnFinished(ee-> {
                if (!volumeBox.isHover()) {
                    Animations.FadeOutScale(volumeBox, 1.0, 0.7, Animations.DEFAULT_DURATION, Duration.ZERO)
                            .setOnFinished(eee -> Platform.runLater(() -> volumeBox.setVisible(false)));
                }
            });
            hideVolumeBoxDelay.playFromStart();
        });

        volumeBox.setOnMouseEntered(e -> {
            hideVolumeBoxDelay.stop();
            if (!volumeBox.isVisible()) {
                volumeBox.setVisible(true);
                Animations.FadeInScale(volumeBox, 0.7, 1.0, Animations.DEFAULT_DURATION, Duration.ZERO);
            }
        });
        volumeBox.setOnMouseExited(e -> {
            hideVolumeBoxDelay.setOnFinished(ee -> {
                if (!volumeButtonPane.isHover()) {
                    Animations.FadeOutScale(volumeBox, 1.0, 0.7, Animations.DEFAULT_DURATION, Duration.ZERO)
                            .setOnFinished(eee -> Platform.runLater(() -> volumeBox.setVisible(false)));
                }
            });
            hideVolumeBoxDelay.playFromStart();
        });


        // при закрытии
        Platform.runLater(() -> {
            Stage stage = (Stage) mainPane.getScene().getWindow();
            stage.setOnCloseRequest(e -> {
                saveWatchProgress();
                stop();
                stage.close();
            });
        });


        Platform.runLater(() -> {
            // Привязка
            imageView.fitWidthProperty().bind(mainPane.getScene().widthProperty());
            imageView.fitHeightProperty().bind(mainPane.getScene().heightProperty());
            imageView.setPreserveRatio(true);
        });



        // Sink Gstreamer
        sink = new AppSink("sink");
        sink.set("emit-signals", true);
        sink.setCaps(Caps.fromString("video/x-raw, format=BGRx"));


        // листенер на фрейм
        sink.connect((AppSink.NEW_SAMPLE) sink -> {
            Sample s = sink.pullSample();
            s = pending.getAndSet(s);
            if (s != null) s.dispose();

            Platform.runLater(this::updateImage);
            return FlowReturn.OK;
        });

        pending = new AtomicReference<>();
    }



    private void updateImage() {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("Not on FX application thread");
        }

        Sample newSample = pending.getAndSet(null);
        if (newSample == null) return;

        Sample oldSample = activeSample;
        Buffer oldBuffer = activeBuffer;

        activeSample = newSample;
        Structure capsStruct = newSample.getCaps().getStructure(0);
        int width = capsStruct.getInteger("width");
        int height = capsStruct.getInteger("height");
        activeBuffer = newSample.getBuffer();

        ByteBuffer mappedBuffer = activeBuffer.map(false);
        if (mappedBuffer == null) return;

        PixelBuffer<ByteBuffer> pixelBuffer = new PixelBuffer<>(width, height, mappedBuffer, PixelFormat.getByteBgraPreInstance());
        WritableImage img = new WritableImage(pixelBuffer);
        imageView.setImage(img);

        if (oldBuffer != null) {
            oldBuffer.unmap();
        }
        if (oldSample != null) {
            oldSample.dispose();
        }

    }


    private void resetInactivityTimer() {

        if (inactivityTimer != null) {
            inactivityTimer.stop();
        }

        if (!playerTopPanel.isVisible()) {
            playerTopPanel.setVisible(true);
            Animations.FadeInSlideVertical(playerTopPanel, -50, 0, Animations.DEFAULT_DURATION, Duration.ZERO);
        }
        if (!playerCenterPanel.isVisible()) {
            playerCenterPanel.setVisible(true);
            Animations.FadeInScale(playerCenterPanel, 0.7, 1.0, Animations.DEFAULT_DURATION, Duration.ZERO);
        }
        if (!playerBottomPanel.isVisible()) {
            playerBottomPanel.setVisible(true);
            Animations.FadeInSlideVertical(playerBottomPanel, 50, 0, Animations.DEFAULT_DURATION, Duration.ZERO);
        }

        inactivityTimer = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
            Animations.FadeOutSlideVertical(playerTopPanel, 0, -50, Animations.DEFAULT_DURATION, Duration.ZERO)
                    .setOnFinished(ee -> playerTopPanel.setVisible(false));
            Animations.FadeOutScale(playerCenterPanel, 1.0, 0.7, Animations.DEFAULT_DURATION, Duration.ZERO)
                    .setOnFinished(ee -> playerCenterPanel.setVisible(false));
            Animations.FadeOutSlideVertical(playerBottomPanel, 0, 50, Animations.DEFAULT_DURATION, Duration.ZERO)
                    .setOnFinished(ee -> playerBottomPanel.setVisible(false));
        }));
        inactivityTimer.setCycleCount(1);
        inactivityTimer.play();
    }


    public void setAnime(Anime anime) {
        this.anime = anime;
        animeTitle.setText(anime.getTitle());
    }

    public void setTranslation(AnimeTranslation translation) {
        this.translation = translation;
    }

    public void setEpisode(Integer episodeNum) {
        this.episode = episodeNum;

    }


    public void load() {
        long startPos = 0;
        Optional<Long> startPosition = dbService.getWatchProgress(anime.getId(), translation.getTranslator_id(), episode);
        if (startPosition.isPresent()) startPos = startPosition.get();

        loadEpisode(episode, qualitySelector.getValue().replace("p", ""), startPos);
    }

    private void loadEpisode(Integer episodeNum, String quality, long startPosition) {
        System.out.println(String.join(" ",
                "Load", anime.getTitle(),
                "Translator", translation.getTranslator_name(),
                "Episode", episodeNum.toString(),
                "Quality", quality));


        AtomicLong savedPosition = new AtomicLong(startPosition);
        stop();

        Platform.runLater(() -> {
            playerLoadingIndicator.setVisible(true);
            episodeInfo.setText(translation.getTranslator_name() + (episodeNum != 0 ? (" · " + episodeNum + " эпизод") : " "));
            timeSlider.setValue(0); // Сбрасываем слайдер
            currentTimeLabel.setText(AnimeUtils.formatTime(0)); // Сбрасываем метку времени
        });

        Platform.runLater(() -> {
            System.out.println("Get episode cloud path");
            apiService.getEpisodeCloudPath(anime.getId(), translation.getTranslator_id(), episodeNum)
                    .thenAccept(cloudPath -> Platform.runLater(() -> {

                        // ссылка на манифест
                        String manifestUrl = cloudPath + (quality + ".mp4") + ":hls:manifest.m3u8";
                        //System.out.println("Manifest url " + manifestUrl);

                        // настройка GStreamer
                        playBin = new PlayBin("play_bin");
                        playBin.set("buffer-duration", 10_000_000_000L);
                        playBin.setURI(URI.create(manifestUrl));
                        playBin.setVideoSink(sink);

                        play();




                        // Слайдер перемотки
                        timeSlider.valueProperty().addListener(e -> {
                            if (timeSlider.isValueChanging()) {
                                resetInactivityTimer();

                                // меняем текст
                                currentTimeLabel.setText(AnimeUtils.formatTime((long) timeSlider.getValue()));
                                long duration = playBin.queryDuration(TimeUnit.NANOSECONDS);
                                if (duration > 0) {
                                    playBin.seekSimple(Format.TIME,
                                            EnumSet.of(SeekFlags.FLUSH),
                                            (long) timeSlider.getValue());
                                }
                            }
                        });

                        // Слайдер для громкости
                        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                            if (volumeSlider.isValueChanging()) {
                                resetInactivityTimer();
                                if (!isMuted) playBin.setVolume(newVal.doubleValue());
                            }
                        });

                        // toogle mute при клике
                        volumeButtonPane.setOnMouseClicked(e -> {
                            isMuted = !isMuted;
                            if (isMuted) {
                                playBin.setVolume(0);
                                volumeButton.setVisible(false);
                            } else {
                                playBin.setVolume(volumeSlider.getValue());
                                volumeButton.setVisible(true);
                            }
                        });


                        // Cинхронизация видео со слайдером
                        Timeline timer = new Timeline(new KeyFrame(Duration.millis(50), e -> {
                            if (!timeSlider.isValueChanging() && (playBin != null)) {
                                long position = playBin.queryPosition(TimeUnit.NANOSECONDS);
                                if (position > 0) {
                                    timeSlider.setValue(position);
                                    currentTimeLabel.setText(AnimeUtils.formatTime(position));
                                }
                            }
                        }));
                        timer.setCycleCount(Animation.INDEFINITE);
                        timer.play();



                        // перемотка дабл кликом
                        mainPane.setOnMouseClicked(event-> {
                            if (event.getClickCount() >= 2) {
                                Platform.runLater(() -> {
                                    double clickX = event.getX();
                                    double width = mainPane.getWidth();

                                    long currentTime = (long) timeSlider.getValue();
                                    long seekOffset = TimeUnit.SECONDS.toNanos(10);
                                    long newTime;

                                    if (clickX < width / 2) {
                                        newTime = Math.max(0, currentTime - seekOffset);
                                    } else {
                                        newTime = Math.min(currentTime + seekOffset, playBin.queryDuration(TimeUnit.NANOSECONDS));
                                    }

                                    playBin.seekSimple(Format.TIME, EnumSet.of(SeekFlags.FLUSH), newTime);
                                });
                            }
                        });


                        new Thread(() -> {
                            // Ждём до 5 секунд, пока состояние не станет PLAYING
                            State currState = playBin.getState(5, TimeUnit.SECONDS);
                            if (currState == State.PLAYING) {
                                // ставим длительность
                                long duration = playBin.queryDuration(TimeUnit.NANOSECONDS);
                                Platform.runLater(() -> {
                                    totalTimeLabel.setText(AnimeUtils.formatTime(duration));
                                    timeSlider.setMax(duration);
                                });

                                // Скрываем индикатор
                                Platform.runLater(() -> Animations.FadeOutScale(playerLoadingIndicator, 1.0, 0.7, Animations.DEFAULT_DURATION, Duration.ZERO)
                                        .setOnFinished(e -> Platform.runLater(() -> {
                                    playerLoadingIndicator.setVisible(false);
                                    Animations.FadeInScale(centerControlBox, 0.7, 1.0, Animations.DEFAULT_DURATION, Duration.ZERO);
                                })));


                                // если указана начальная позиция
                                if (savedPosition.get() != 0) {
                                    playBin.seekSimple(Format.TIME,
                                            EnumSet.of(SeekFlags.FLUSH),
                                            savedPosition.get());
                                    savedPosition.set(0);
                                }
                            }

                        }).start();

                    }))
                    .exceptionally(e -> {
                        System.out.println("Failed to get cloudPath");
                        return null;
                    });
        });
    }

    private void stop() {
        if (playBin != null) {
            playBin.stop();
            playBin.dispose();
            playBin = null;
        }
    }


    public void play() {
        playBin.play();
        playSvg.setVisible(false);
    }

    public void pause() {
        playBin.pause();
        playSvg.setVisible(true);
    }

    public void goBack() {
        saveWatchProgress();
        stop();

        SceneState state = TabSceneManager.get();
        if ((state != null) && (state.getController() == this)) {
            TabSceneManager.goBackAndShowAnimated();
            SceneState newState =  TabSceneManager.get();
            if (newState != null && newState.getController() instanceof EpisodeSelectController episodeSelectController) {
                episodeSelectController.updateEpisodesContainer(anime, translation);
            }
            TabSceneManager.showPanel();
        } else {
            Stage stage = (Stage) mainPane.getScene().getWindow();
            stage.close();
        }
    }



    public void toggleFullScreen() {
        Stage stage = (Stage) mainPane.getScene().getWindow();
        stage.setFullScreen(!stage.isFullScreen());
    }


    public void prevEpisode() {
        saveWatchProgress();

        Integer prevEpisodeNum = episode - 1;
        if (translation.getEpisodes().contains(prevEpisodeNum)) {
            setEpisode(prevEpisodeNum);
            load();
        }
    }

    public void nextEpisode() {
        saveWatchProgress();

        Integer nextEpisodeNum = episode + 1;
        if (translation.getEpisodes().contains(nextEpisodeNum)) {
            setEpisode(nextEpisodeNum);
            load();
        }
    }

    private void saveWatchProgress() {
        dbService.saveWatchProgress(anime.getId(), translation.getTranslator_id(), episode, (long) timeSlider.getValue());

    }

}
