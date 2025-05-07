module com.anilabs.anilabsfx {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.base;
    requires java.net.http;
    requires java.security.sasl;
    requires jdk.crypto.cryptoki;

    requires com.google.gson;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires org.slf4j;
    requires org.freedesktop.gstreamer;

    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome6;

    exports com.anilabs.anilabsfx;
    exports com.anilabs.anilabsfx.animation;
    exports com.anilabs.anilabsfx.controller;
    exports com.anilabs.anilabsfx.elements;
    exports com.anilabs.anilabsfx.manager;
    exports com.anilabs.anilabsfx.model;
    exports com.anilabs.anilabsfx.service;
    exports com.anilabs.anilabsfx.utils;

    opens com.anilabs.anilabsfx.controller to javafx.fxml;
    opens com.anilabs.anilabsfx.elements to javafx.fxml;
    opens com.anilabs.anilabsfx.model to com.google.gson;
}