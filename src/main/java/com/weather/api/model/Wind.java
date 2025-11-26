package com.weather.api.model;

import lombok.Data;

@Data
public class Wind {

  private Double speed;
  private Double deg;
  private Double gust;

  public void plusSpeed(Double speed) {
    this.speed = (this.speed == null ? 0.0 : this.speed + speed);
  }

  public void plusDeg(Double deg) {
    this.deg = (this.deg == null ? 0.0 : this.deg + deg);
  }

  public void plusGust(Double gust) {
    this.gust = (this.gust == null ? 0.0 : this.gust + gust);
  }
}
