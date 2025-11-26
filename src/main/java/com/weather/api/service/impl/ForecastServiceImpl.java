package com.weather.api.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.weather.api.exception.ResourceNotFoundException;
import com.weather.api.model.City;
import com.weather.api.model.Forecast;
import com.weather.api.model.Weather;
import com.weather.api.model.Wind;
import com.weather.api.service.contract.ForecastService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class ForecastServiceImpl implements ForecastService {

  private final RestClient restClient;
  private final String API_KEY;

  public ForecastServiceImpl(
      RestClient restClient,
      @Value("${api.key}") String API_KEY
  ) {
    this.restClient = restClient;
    this.API_KEY = API_KEY;
  }

  @Override
  public List<Forecast> searchByCityName(String cityName) {
    return buildForecast(fetchData(cityName));
  }

  // TODO: optional add 'metric' and 'imperial' type fetch data
  private JsonNode fetchData(String cityName) {
    return restClient.get()
        .uri("/data/2.5/forecast?q={city}&units=metric&appid={key}", cityName, API_KEY)
        .retrieve()
        .onStatus(
            status -> status.value() == 404, (req, res) -> {
              throw new ResourceNotFoundException("Місто не знайдено: " + cityName);
            }
        )
        .body(JsonNode.class);
  }

  /**
   * Групуємо результат по днях
   *
   * @param forecastJson {@link JsonNode} дані з самого кореня
   * @return список прогнозів
   */
  private List<Forecast> buildForecast(JsonNode forecastJson) {
    JsonNode list = forecastJson.get("list");

    Forecast fr = new Forecast();
    List<Forecast> forecastList = new ArrayList<>();

    Map<Weather, Integer> weathers = new HashMap<>();

    int counter = 0;
    int countPeriodsByDay = 0;
    for (JsonNode node : list) {
      counter++; // Для визначення чи це останній запис
      long timestamp = node.path("dt").asLong();
      LocalDateTime dateTime = convertToLocalDateTime(timestamp);

      if (fr.getDate() == null) {
        fr.setDate(dateTime.toLocalDate());
      }

      // Якщо це інший день
      if (!fr.getDate().equals(dateTime.toLocalDate())) {
        // Логіка підсумовування даних, avg, max, ... — готовий об'єкт
        compilationSummaryForecast(
            fr, forecastJson, forecastList, weathers, countPeriodsByDay);
        // Перезаписуємо змінні для наступних днів
        countPeriodsByDay = 0;
        weathers.clear();
        fr = new Forecast();
      }

      // Перетворює дані Json на поля об'єкта Forecast
      mapJsonToForecast(node, fr, weathers);
      // Підрахунок 3-годинних періодів,
      // необхідних для коректного дільника avg значень прогнозу
      countPeriodsByDay++;

      if (counter == list.size()) {
        compilationSummaryForecast(
            fr, forecastJson, forecastList, weathers, countPeriodsByDay);
      }
    }

    return forecastList;
  }

  /**
   * Проміжний метод для виклику json методів зіставлення полів сутності
   *
   * @param node     конкретний запис в list
   * @param fr       сутність що до якої зіставляються дані
   * @param weathers погодний стан — для зіставлення з цим періодом
   */
  private void mapJsonToForecast(JsonNode node, Forecast fr, Map<Weather, Integer> weathers) {
    fillJsonMain(node, fr);
    try {
      fr.plusVisibility(node.get("visibility").asDouble());
    } catch (Exception e) {
      log.error("Error mapping forecast by visibility value in list node: {}", e);
    }
    fillJsonWind(node, fr.getWind());
    // Збираємо усі потім шукаємо найбільш «частий» тип погоди
    fillJsonWeather(node, weathers);
  }

  /**
   * Виконує «збірку» результату {@link Forecast}, викликаючи методи зіставлення json з сутністю,
   * також виконання певних проміжних операцій по типу ділення на періоди або пошуку максимального
   *
   * @param fr                {@link Forecast} сутність що буде збережена
   * @param forecastJson      {@link JsonNode} дані з самого кореня
   * @param forecastList      список сутностей що будуть результатом повернення із запиту
   * @param weathers          стан погоди — для пошуку найбільш повторювальної за період
   * @param countPeriodsByDay періоди за день (залежать від інтервалу часу)
   */
  private void compilationSummaryForecast(
      Forecast fr, JsonNode forecastJson, List<Forecast> forecastList,
      Map<Weather, Integer> weathers, int countPeriodsByDay
  ) {
    fr.setCity(fillJsonCity(forecastJson)); // Місто — загальні дані
    fr.setWeather(oftenWeather(weathers)); // Додали найбільш «часту» погоду
    divideValues(fr, countPeriodsByDay); // Середнє значення всіх метрик
    forecastList.add(fr);
  }

  /**
   * Заповнення основних даних про метрики погоди
   *
   * @param node     конкретний запис періоду у list
   * @param forecast конкретні метрики що сумуються, потім повинні ділитися на кількість періодів
   */
  private void fillJsonMain(JsonNode node, Forecast forecast) {
    try {
      JsonNode mainJson = node.get("main");
      forecast.plusTemperature(mainJson.path("temp").asDouble());
      forecast.plusFeelsLike(mainJson.path("feels_like").asDouble());
      forecast.plusMinTemperature(mainJson.path("temp_min").asDouble());
      forecast.plusMaxTemperature(mainJson.path("temp_max").asDouble());
      forecast.plusPressure(mainJson.path("pressure").asDouble());
      forecast.plusHumidity(mainJson.path("humidity").asDouble());
    } catch (Exception e) {
      log.error("Error mapping forecast main node: ", e);
    }
  }

  /**
   * Заповнює дані про вітер
   *
   * @param node конкретний запис періоду у list
   * @param wind {@link Wind} вітер
   */
  private void fillJsonWind(JsonNode node, Wind wind) {
    try {
      JsonNode windJson = node.get("wind");
      wind.plusSpeed(windJson.path("speed").asDouble());
      wind.plusGust(windJson.path("gust").asDouble());
      wind.plusDeg(windJson.path("deg").asDouble());
    } catch (Exception e) {
      log.error("Error mapping forecast wind node: ", e);
    }
  }

  /**
   * Заповнює json даними про стан погоди (згідно resp вертається масив з API)
   *
   * @param node     конкретний запис періоду у list
   * @param weathers {@link Weather} стан погоди, з індикатором найбільш повторювальної за день
   */
  private void fillJsonWeather(JsonNode node, Map<Weather, Integer> weathers) {
    JsonNode weatherJson = node.get("weather");
    for (JsonNode item : weatherJson) {
      Weather weather = null;
      try {
        weather = new Weather(
            item.path("main").asText(),
            item.path("description").asText(),
            item.path("icon").asText()
        );
      } catch (Exception e) {
        log.error("Error mapping forecast weather node: ", e);
      }

      if (weathers.containsKey(weather)) {
        weathers.compute(weather, (k, count) -> count != null ? count + 1 : 1);
      } else {
        weathers.put(weather, 1);
      }
    }
  }

  /**
   * Заповнює json даними про місто
   *
   * @param node витягаються дані про місто з основного json
   * @return {@link City} місто — загальні дані
   */
  private City fillJsonCity(JsonNode node) {
    try {
      JsonNode cityJson = node.get("city");
      return new City(
          cityJson.path("name").asText(),
          cityJson.path("country").asText(),
          cityJson.path("population").asLong(),
          convertToLocalDateTime(cityJson.path("timezone").asLong()),
          convertToLocalDateTime(cityJson.path("sunset").asLong()),
          convertToLocalDateTime(cityJson.path("sunrise").asLong())
      );
    } catch (Exception e) {
      log.error("Error mapping forecast city node: ", e);
    }
    return null;
  }

  /**
   * Виконує пошук {@link Weather} що попадався найчастіше за день
   *
   * @param weathers стан погоди
   * @return найбільш частий стан погоди за день
   */
  private Weather oftenWeather(Map<Weather, Integer> weathers) {
    Weather weather = null;
    int max = 0;
    for (Weather w : weathers.keySet()) {
      if (weathers.get(w) > max) {
        weather = w;
        max = weathers.get(w);
      }
    }

    return weather;
  }

  /**
   * Підсумовує значення дня по середньому
   *
   * @param forecast дані за день
   * @param divisor  дільник (3 годинні періоди)
   */
  private void divideValues(Forecast forecast, int divisor) {
    forecast.setTemperature(rounded(forecast.getTemperature() / divisor));
    forecast.setFeelsLike(rounded(forecast.getFeelsLike() / divisor));
    forecast.setMinTemperature(rounded(forecast.getMinTemperature() / divisor));
    forecast.setMaxTemperature(rounded(forecast.getMaxTemperature() / divisor));
    forecast.setPressure(rounded(forecast.getPressure() / divisor));
    forecast.setVisibility(rounded(forecast.getVisibility() / divisor));
    forecast.setHumidity(rounded(forecast.getHumidity() / divisor));

    // Wind avg
    forecast.getWind().setSpeed(rounded(forecast.getWind().getSpeed() / divisor));
    forecast.getWind().setGust(rounded(forecast.getWind().getGust() / divisor));
    forecast.getWind().setDeg(rounded(forecast.getWind().getDeg() / divisor));
  }

  private LocalDateTime convertToLocalDateTime(long timestamp) {
    return Instant.ofEpochSecond(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime();
  }

  private Double rounded(Double value) {
    return BigDecimal
        .valueOf(value)
        .setScale(2, RoundingMode.HALF_UP)
        .doubleValue();
  }
}
