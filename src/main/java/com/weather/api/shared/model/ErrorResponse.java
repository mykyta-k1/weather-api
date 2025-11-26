package com.weather.api.shared.model;

import java.time.OffsetDateTime;

/**
 * RFC 7807
 *
 * @param timestamp часова мітка коли сталася помилка
 * @param status    статус помилки, наприклад: 404
 * @param error     короткий опис статусу, наприклад: "Forbidden", "Not Found"
 * @param message   Контекстно-залежне повідомлення про помилку.
 * @param path      URI, на який надійшов запит.
 * @param details   Об'єкт, що містить додаткові деталі, наприклад, помилки валідації.
 */
public record ErrorResponse(
    OffsetDateTime timestamp,
    int status,
    String error,
    String message,
    String path,
    Object details
) {

}
