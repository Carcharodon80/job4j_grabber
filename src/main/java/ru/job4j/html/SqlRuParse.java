package ru.job4j.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * 2. Парсинг HTML страницы. [#260358]
 */
public class SqlRuParse {
    public static void main(String[] args) throws IOException {
        Document doc = Jsoup.connect("https://www.sql.ru/forum/job-offers").get();
        Elements row = doc.select(".postslisttopic");
        for (Element td : row) {
            Element href = td.child(0);
            System.out.println(href.attr("href"));
            System.out.println(href.text());
            System.out.println(td.parent().child(5).text());
        }
    }
}
