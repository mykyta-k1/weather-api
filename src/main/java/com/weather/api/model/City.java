package com.weather.api.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class City {

  private String name;
  private String country;
  private Long population;
  private LocalDateTime timezone;
  private LocalDateTime sunset;
  private LocalDateTime sunrise;
}
