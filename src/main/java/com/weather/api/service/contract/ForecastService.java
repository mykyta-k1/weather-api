package com.weather.api.service.contract;

import com.weather.api.model.Forecast;
import java.util.List;

public interface ForecastService {

  List<Forecast> searchByCityName(String cityName);

}
