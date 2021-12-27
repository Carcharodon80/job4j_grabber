package ru.job4j.grabber.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 2.1. Преобразование даты [#289476 #244788]
 */
public class SqlRuDateTimeParser implements DateTimeParser {
    private static final Map<String, String> MONTHS = Map.ofEntries(
            Map.entry("янв", "01"),
            Map.entry("фев", "02"),
            Map.entry("мар", "03"),
            Map.entry("апр", "04"),
            Map.entry("май", "05"),
            Map.entry("июн", "06"),
            Map.entry("июл", "07"),
            Map.entry("авг", "08"),
            Map.entry("сен", "09"),
            Map.entry("окт", "10"),
            Map.entry("ноя", "11"),
            Map.entry("дек", "12")
    );
    private static final DateTimeFormatter FORMATTER_TIME = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FORMATTER_DATE = DateTimeFormatter.ofPattern("d MM uu");

    @Override
    public LocalDateTime parse(String parse) {
        LocalDateTime result;
        String[] dateAndTime = parse.split(", ");
        String time = dateAndTime[1];
        LocalTime localTime = LocalTime.parse(time, FORMATTER_TIME);
        String date = dateAndTime[0];
        if ("сегодня".equals(date)) {
            result = LocalDateTime.of(LocalDate.now(), localTime);
        } else if ("вчера".equals(date)) {
            result = LocalDateTime.of(LocalDate.now(), localTime).minusDays(1);
        } else {
            String month = date.split(" ")[1];
            date = date.replace(month, MONTHS.get(month));
            LocalDate localDate = LocalDate.parse(date, FORMATTER_DATE);
            result = LocalDateTime.of(localDate, localTime);
        }
        return result;
    }
}
