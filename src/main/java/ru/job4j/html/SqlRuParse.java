package ru.job4j.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.Post;
import ru.job4j.grabber.Parse;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.SqlRuDateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 2. Парсинг HTML страницы. [#260358]
 * 2.1.1. Парсинг https://www.sql.ru/forum/job-offers/3 [#285210]
 * 2.4. SqlRuParse [#285213]
 */
public class SqlRuParse implements Parse {
    private final DateTimeParser dateTimeParser;

    public SqlRuParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    public static void main(String[] args) throws IOException {
        SqlRuParse sqlRuParse = new SqlRuParse(new SqlRuDateTimeParser());
        List<Post> posts = sqlRuParse.list("https://www.sql.ru/forum/job-offers");
        System.out.println(posts);
    }

    @Override
    public List<Post> list(String link) throws IOException {
        List<Post> allPosts = new ArrayList<>();
            Document doc = Jsoup.connect(link).get();
            Elements row = doc.select(".postslisttopic");
            for (Element td : row) {
                Element href = td.child(0);
                String linkForPost = href.attr("href");
                Post post = detail(linkForPost);
                allPosts.add(post);
            }
        return allPosts;
    }

    public Post detail(String link) throws IOException {
        Document vacancy = Jsoup.connect(link).get();
        String title = vacancy.select(".messageHeader").get(0).text();
        String description = vacancy.select(".msgBody").get(1).text();
        String dateTimeString = vacancy.select(".msgFooter").get(0).text().split(" \\[")[0];
        LocalDateTime created = dateTimeParser.parse(dateTimeString);
        return new Post(title, link, description, created);
    }
}
