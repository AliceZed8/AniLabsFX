package com.anilabs.anilabsfx.service;

import com.google.gson.*;
import com.anilabs.anilabsfx.model.*;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;


public class ApiService {
    private static final ApiService instance = new ApiService();

    private final String API_URL = "https://anilabs.ru/api";
    private final Gson gson = new Gson();

    private final HttpClient httpClient = HttpClient.newHttpClient();
    public static final int PAGE_SIZE = 20;
    public static FilterParams filterParams;

    private ApiService() {

    }

    public static ApiService getInstance() {
        return instance;
    }


    public void loadFilterParams() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/anime/filterParams"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
        filterParams = gson.fromJson(jsonObject.get("result"), FilterParams.class);
    }


    // info V2
    public CompletableFuture<Anime> getAnimeAsync(Integer animeId) {
        JsonObject requestBodyJson = new JsonObject();
        requestBodyJson.addProperty("anime_id", animeId);
        String requestBody = gson.toJson(requestBodyJson);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/anime/details"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(body -> {
                    JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
                    return gson.fromJson(jsonObject.get("result"), Anime.class);
                });
    }

    // filter V2
    public CompletableFuture<List<Anime>> filterAsync(FilterRequest filterRequest) {
        String requestBody = gson.toJson(filterRequest);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/anime/filterV2"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(body -> {
                    JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
                    Anime[] animeList = gson.fromJson(jsonObject.get("results"), Anime[].class);
                    return Arrays.asList(animeList);
                });
    }

    public CompletableFuture<List<Anime>> searchAsync(SearchRequest searchRequest) {
        String requestBody = gson.toJson(searchRequest);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/anime/search"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(body -> {
                    JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
                    Anime[] animeList = gson.fromJson(jsonObject.get("results"), Anime[].class);
                    return Arrays.asList(animeList);
                });
    }

    public CompletableFuture<List<Anime>> lastUpdatedAsync(Integer offset, Integer count) {
        JsonObject requestBodyJson = new JsonObject();
        requestBodyJson.addProperty("offset", offset);
        requestBodyJson.addProperty("count", count);
        String requestBody = gson.toJson(requestBodyJson);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/anime/lastUpdated"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(body -> {
                    JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
                    Anime[] animeList = gson.fromJson(jsonObject.get("results"), Anime[].class);
                    return Arrays.asList(animeList);
                });
    }


    public CompletableFuture<Integer> getRandom() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/anime/random"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(body -> {
                    JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
                    JsonObject result = jsonObject.get("result").getAsJsonObject();
                    return gson.fromJson(result.get("id"), Integer.class);
                });
    }




    // api v2
    public CompletableFuture<List<AnimeTranslation>> getAnimeTranslationsAsync(Integer animeId) {

        JsonObject requestBodyJson = new JsonObject();
        requestBodyJson.addProperty("anime_id", animeId);
        String requestBody = gson.toJson(requestBodyJson);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/anime/translations/list"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(body -> {
                    JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
                    AnimeTranslation[] translations = gson.fromJson(jsonObject.get("results"), AnimeTranslation[].class);
                    return Arrays.asList(translations);
                });
    }

    public CompletableFuture<String> getEpisodeCloudPath(Integer animeId, Integer translatorId, Integer episodeNum) {

        JsonObject requestBodyJson = new JsonObject();
        requestBodyJson.addProperty("anime_id", animeId);
        requestBodyJson.addProperty("translator_id", translatorId);
        requestBodyJson.addProperty("episode_num", episodeNum);
        String requestBody = gson.toJson(requestBodyJson);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/anime/translations/episode/getCloudPath"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(body -> {
                    JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
                    return gson.fromJson(jsonObject.get("result"), String.class);
                });
    }

    public CompletableFuture<List<Anime>> getSliderItems() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/anime/sliderList"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(body -> {
                    JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
                    Anime[] animeList = gson.fromJson(jsonObject.get("results"), Anime[].class);
                    return Arrays.asList(animeList);
                });
    }


}
