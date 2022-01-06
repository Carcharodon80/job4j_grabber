package ru.job4j.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.Post;
import ru.job4j.grabber.utils.SqlRuDateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * 2. Парсинг HTML страницы. [#260358]
 * 2.1.1. Парсинг https://www.sql.ru/forum/job-offers/3 [#285210]
 */
public class SqlRuParse {
    public static void main(String[] args) throws IOException {
        for (int i = 1; i <= 5; i++) {
            Document doc = Jsoup.connect("https://www.sql.ru/forum/job-offers/" + i).get();
            Elements row = doc.select(".postslisttopic");
            for (Element td : row) {
                Element href = td.child(0);
                String link = href.attr("href");
                Post post = detail(link);
            }
        }
    }

    public static Post detail(String link) throws IOException {
        Document vacancy = Jsoup.connect(link).get();
        String title = vacancy.select(".messageHeader").get(0).text();
        String description = vacancy.select(".msgBody").get(1).text();
        SqlRuDateTimeParser dateTimeParser = new SqlRuDateTimeParser();
        String dateTimeString = vacancy.select(".msgFooter").get(0).text().split(" \\[")[0];
        LocalDateTime created = dateTimeParser.parse(dateTimeString);
        return new Post(title, link, description, created);
    }
}
