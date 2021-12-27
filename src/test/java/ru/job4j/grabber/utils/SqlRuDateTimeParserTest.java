package ru.job4j.grabber.utils;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class SqlRuDateTimeParserTest {
    @Test
    public void ifToday() {
        String date = "сегодня, 17:53";
        LocalDate dateNow = LocalDate.now();
        LocalTime time = LocalTime.of(17, 53);
        LocalDateTime expected = LocalDateTime.of(dateNow, time);
        assertThat(new SqlRuDateTimeParser().parse(date), is(expected));
    }

    @Test
    public void ifYesterday() {
        String date = "вчера, 23:54";
        LocalDate dateNow = LocalDate.now().minusDays(1);
        LocalTime time = LocalTime.of(23, 54);
        LocalDateTime expected = LocalDateTime.of(dateNow, time);
        assertThat(new SqlRuDateTimeParser().parse(date), is(expected));
    }

    @Test
    public void ifDateAndTime() {
        String date = "8 ноя 21, 10:36";
        LocalDateTime expected = LocalDateTime.of(2021, 11, 8, 10, 36);
        assertThat(new SqlRuDateTimeParser().parse(date), is(expected));
    }
}