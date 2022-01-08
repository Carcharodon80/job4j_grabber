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
import java.util.Locale;

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

    public static void main(String[] args) {
        SqlRuParse sqlRuParse = new SqlRuParse(new SqlRuDateTimeParser());
        List<Post> posts = sqlRuParse.list("https://www.sql.ru/forum/job-offers");
        System.out.println(posts);
    }

    @Override
    public List<Post> list(String link) {
        List<Post> allPosts = new ArrayList<>();
        Document doc = null;
        for (int i = 1; i <= 5; i++) {
            try {
                doc = Jsoup.connect(link + "/" + i).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            assert doc != null;
            Elements row = doc.select(".postslisttopic");
            for (Element td : row) {
                Element href = td.child(0);
                String title = href.text().toLowerCase(Locale.ROOT);
                if (title.contains("java") && !title.contains("javascript")) {
                    String linkForPost = href.attr("href");
                    Post post = detail(linkForPost);
                    allPosts.add(post);
                }
            }
        }
        return allPosts;
    }

    public Post detail(String link) {
        Document vacancy = null;
        try {
            vacancy = Jsoup.connect(link).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert vacancy != null;
        String title = vacancy.select(".messageHeader").get(0).text();
        String description = vacancy.select(".msgBody").get(1).text();
        String dateTimeString = vacancy.select(".msgFooter").get(0).text().split(" \\[")[0];
        LocalDateTime created = dateTimeParser.parse(dateTimeString);
        return new Post(title, link, description, created);
    }
}
