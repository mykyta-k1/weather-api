package com.weather.api.controller;

import com.weather.api.model.Forecast;
import com.weather.api.service.contract.ForecastService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/forecasts")
@RequiredArgsConstructor
public class ForecastController {

  private final ForecastService forecastService;

  @GetMapping("/{cityName}")
  public ResponseEntity<List<Forecast>> searchByCityName(@PathVariable String cityName) {
    return ResponseEntity.ok(forecastService.searchByCityName(cityName));
  }

}
