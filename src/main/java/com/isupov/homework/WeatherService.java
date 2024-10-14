package com.isupov.homework;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class WeatherService {

    private static final String KEY = "";

    public static void main(String[] args) throws IOException, InterruptedException {
        String url = "https://api.weather.yandex.ru/v2/forecast" +
                "?lat=55.7522&lon=37.6156" + //Широта и долгота Москвы
                "&limit=3";

        HttpClient httpClient = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .headers("X-Yandex-Weather-Key", KEY)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        String body = response.body();
        System.out.println("Весь запрос: " + body);


        //Темп в данный момент
        JsonObject jsonObject = Json.createReader(new StringReader(body)).readObject();
        int temp = jsonObject.getJsonObject("fact").getInt("temp");
        System.out.println("Температура в данный момент: " + temp + " г.");

        //Средняя темп за 3 дня
        JsonArray forecasts = jsonObject.getJsonArray("forecasts");
        List<Double> listTemps = new ArrayList<>();
        for (int i = 0, size = forecasts.size(); i < size; i++) {
            JsonObject day = forecasts.getJsonObject(i);
            listTemps.add(getDayAvgTemperature(day));
        }
        double averageTemp = listTemps.stream().mapToDouble(e -> e).average().orElseThrow();
        System.out.println("\nСредняя температура в Москвы за 3 дня: " + (int) averageTemp + " г.");
    }


    private static double getDayAvgTemperature(JsonObject day) {
        List<Integer> dayTemp = new ArrayList<>();

        JsonArray hours = day.getJsonArray("hours");
        for (int i = 0, size = hours.size(); i < size; i++) {
            JsonObject hour = hours.getJsonObject(i);
            dayTemp.add(hour.getInt("temp"));
        }

        return dayTemp.stream().mapToInt(e -> e)
                .average().orElseThrow(() -> new RuntimeException("Отсутствует почасовая температура у данных в этом дне!"));
    }
}
