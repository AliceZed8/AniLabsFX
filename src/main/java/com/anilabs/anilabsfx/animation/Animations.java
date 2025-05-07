package com.anilabs.anilabsfx.animation;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;

import javafx.scene.Node;
import javafx.util.Duration;

public class Animations {
    static public Duration DEFAULT_DURATION = Duration.millis(500);


    static public ScaleTransition Scale(Node node, Double scaleFrom, Double scaleTo, Duration duration, Duration delay) {
        ScaleTransition scale = new ScaleTransition(duration, node);

        scale.setFromX(scaleFrom);
        scale.setFromY(scaleFrom);
        scale.setToX(scaleTo);
        scale.setToY(scaleTo);

        scale.setDelay(delay);
        scale.play();
        return scale;
    }

    static public TranslateTransition TranslateY(Node node, Integer from, Integer to, Duration duration, Duration delay) {
        TranslateTransition translate = new TranslateTransition(duration, node);
        translate.setFromY(from);
        translate.setToY(to);

        translate.setDelay(delay);
        translate.play();
        return translate;
    }
    static public TranslateTransition TranslateX(Node node, Integer from, Integer to, Duration duration, Duration delay) {
        TranslateTransition translate = new TranslateTransition(duration, node);
        translate.setFromX(from);
        translate.setToX(to);

        translate.setDelay(delay);
        translate.play();
        return translate;
    }



    // Fade IN with Scale
    static public ParallelTransition FadeInScale(Node node, Double scaleFrom, Double scaleTo, Duration duration, Duration delay) {
        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(0);
        fade.setToValue(1);

        ScaleTransition scale = new ScaleTransition(duration, node);
        scale.setFromX(scaleFrom);
        scale.setFromY(scaleFrom);
        scale.setToX(scaleTo);
        scale.setToY(scaleTo);

        ParallelTransition parallel = new ParallelTransition(fade, scale);
        parallel.setDelay(delay);
        parallel.play();
        return parallel;
    }


    // Fade Out with scale
    static public ParallelTransition FadeOutScale(Node node, Double scaleFrom, Double scaleTo, Duration duration, Duration delay) {
        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(1);
        fade.setToValue(0);

        ScaleTransition scale = new ScaleTransition(duration, node);
        scale.setFromX(scaleFrom);
        scale.setFromY(scaleFrom);
        scale.setToX(scaleTo);
        scale.setToY(scaleTo);

        ParallelTransition parallel = new ParallelTransition(fade, scale);
        parallel.setDelay(delay);
        parallel.play();
        return parallel;
    }



    // Fade IN with Slide Vertical
    static public ParallelTransition FadeInSlideVertical(Node node, Integer from, Integer to, Duration duration, Duration delay) {
        node.setOpacity(0);
        node.setTranslateY(from);

        TranslateTransition translate = new TranslateTransition(duration, node);
        translate.setFromY(from);
        translate.setToY(to);

        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(0);
        fade.setToValue(1);

        ParallelTransition parallel = new ParallelTransition(translate, fade);
        parallel.setDelay(delay);
        parallel.play();
        return parallel;
    }

    static public ParallelTransition FadeOutSlideVertical(Node node, Integer from, Integer to, Duration duration, Duration delay) {
        node.setOpacity(1);
        node.setTranslateY(from);

        TranslateTransition translate = new TranslateTransition(duration, node);
        translate.setFromY(from);
        translate.setToY(to);

        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(1);
        fade.setToValue(0);

        ParallelTransition parallel = new ParallelTransition(translate, fade);
        parallel.setDelay(delay);
        parallel.play();
        return parallel;
    }


    static public ParallelTransition FadeInSlideHorizontal(Node node, Integer from, Integer to, Duration duration, Duration delay) {
        node.setOpacity(0);
        node.setTranslateX(from);

        TranslateTransition translate = new TranslateTransition(duration, node);
        translate.setFromX(from);
        translate.setToX(to);

        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(0);
        fade.setToValue(1);

        ParallelTransition parallel = new ParallelTransition(translate, fade);
        parallel.setDelay(delay);
        parallel.play();
        return parallel;
    }

    static public ParallelTransition FadeOutSlideHorizontal(Node node, Integer from, Integer to, Duration duration, Duration delay) {
        node.setOpacity(1);
        node.setTranslateX(from);

        TranslateTransition translate = new TranslateTransition(duration, node);
        translate.setFromX(from);
        translate.setToX(to);

        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(1);
        fade.setToValue(0);

        ParallelTransition parallel = new ParallelTransition(translate, fade);
        parallel.setDelay(delay);
        parallel.play();
        return parallel;
    }
}
