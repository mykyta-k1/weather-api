package com.weather.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Weather {

  private String type;
  private String description;
  private String icon;
}
