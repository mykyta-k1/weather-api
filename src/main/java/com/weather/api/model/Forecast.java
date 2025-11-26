package com.weather.api.model;

import java.time.LocalDate;
import lombok.Data;

@Data
public class Forecast {

  private LocalDate date;
  private Double temperature;
  private Double feelsLike;
  private Double minTemperature;
  private Double maxTemperature;
  private Double pressure;
  private Double visibility;
  private Double humidity;
  private Weather weather;
  private Wind wind;
  private City city;

  public Wind getWind() {
    return this.wind == null ? this.wind = new Wind() : this.wind;
  }

  public void plusTemperature(Double temperature) {
    this.temperature = (this.temperature == null ? 0 : this.temperature + temperature);
  }

  public void plusFeelsLike(Double feelsLike) {
    this.feelsLike = (this.feelsLike == null ? 0 : this.feelsLike + feelsLike);
  }

  public void plusMinTemperature(Double minTemperature) {
    this.minTemperature = (this.minTemperature == null ? 0 : this.minTemperature + minTemperature);
  }

  public void plusMaxTemperature(Double maxTemperature) {
    this.maxTemperature = (this.maxTemperature == null ? 0 : this.maxTemperature + maxTemperature);
  }

  public void plusPressure(Double pressure) {
    this.pressure = (this.pressure == null ? 0 : this.pressure + pressure);
  }

  public void plusVisibility(Double visibility) {
    this.visibility = (this.visibility == null ? 0 : this.visibility + visibility);
  }

  public void plusHumidity(Double humidity) {
    this.humidity = (this.humidity == null ? 0 : this.humidity + humidity);
  }
}
