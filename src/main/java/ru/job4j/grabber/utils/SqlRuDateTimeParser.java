package ru.job4j.grabber.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.Map;

public class SqlRuDateTimeParser implements DateTimeParser {
    private static final Map<String, String> MONTHS = Map.ofEntries(
            new AbstractMap.SimpleEntry<>("янв", "01"),
            new AbstractMap.SimpleEntry<>("фев", "02"),
            new AbstractMap.SimpleEntry<>("мар", "03"),
            new AbstractMap.SimpleEntry<>("апр", "04"),
            new AbstractMap.SimpleEntry<>("май", "05"),
            new AbstractMap.SimpleEntry<>("июн", "06"),
            new AbstractMap.SimpleEntry<>("июл", "07"),
            new AbstractMap.SimpleEntry<>("авг", "08"),
            new AbstractMap.SimpleEntry<>("сен", "09"),
            new AbstractMap.SimpleEntry<>("окт", "10"),
            new AbstractMap.SimpleEntry<>("ноя", "11"),
            new AbstractMap.SimpleEntry<>("дек", "12")
    );

    @Override
    public LocalDateTime parse(String parse) {
        LocalDateTime result;
        if (parse.contains("сегодня")) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime time = LocalTime.parse(parse.substring(9), formatter);
            result = LocalDateTime.of(LocalDate.now(), time);
        } else if (parse.contains("вчера")) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime time = LocalTime.parse(parse.substring(7), formatter);
            result = LocalDateTime.of(LocalDate.now(), time).minusDays(1);
        } else {
            for (Map.Entry<String, String> entry : MONTHS.entrySet()) {
                if (parse.contains(entry.getKey())) {
                    parse = parse.replace(entry.getKey(), entry.getValue());
                    break;
                }
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MM uu, HH:mm");
            result = LocalDateTime.parse(parse, formatter);
        }
        return result;
    }
}
