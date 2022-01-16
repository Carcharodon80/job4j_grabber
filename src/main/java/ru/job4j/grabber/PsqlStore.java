package ru.job4j.grabber;

import ru.job4j.grabber.utils.SqlRuDateTimeParser;
import ru.job4j.html.SqlRuParse;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 5. PsqlStore [#285209]
 */
public class PsqlStore implements Store, AutoCloseable {
    private Connection cnn;

    public PsqlStore(Properties cfg) {
        try {
            Class.forName(cfg.getProperty("jdbc.driver"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        try {
            cnn = DriverManager.getConnection(
                    cfg.getProperty("jdbc.url"),
                    cfg.getProperty("jdbc.username"),
                    cfg.getProperty("jdbc.password")
            );
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public void save(Post post) {
        try {
            PreparedStatement statement = cnn.prepareStatement(
                    "insert into post (name, text, link, created) values (?, ?, ?, ?);",
                    Statement.RETURN_GENERATED_KEYS
            );
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getDescription());
            statement.setString(3, post.getLink());
            statement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            statement.execute();
            try (ResultSet generatedId = statement.getGeneratedKeys()) {
                if (generatedId.next()) {
                    post.setId(generatedId.getInt(1));
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> posts = new ArrayList<>();
        try {
            PreparedStatement statement = cnn.prepareStatement(
                    "select * from post"
            );
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                posts.add(createPost(resultSet));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return posts;
    }

    public void deleteAllAndRestartId() {
        try {
            PreparedStatement statement = cnn.prepareStatement(
                    "delete from post;"
            );
            statement.execute();
            statement = cnn.prepareStatement("alter sequence \"post_id_seq\" restart with 1");
            statement.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public Post findById(int id) {
        Post post = null;
        try {
            PreparedStatement statement = cnn.prepareStatement(
                    "select * from post where id = ?;"
            );
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                post = createPost(resultSet);

            } else {
                System.out.println("Записи с данным id не обнаружено");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return post;
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }

    public static void main(String[] args) {
        PsqlStore psqlStore = new PsqlStore(loadProperties());
        SqlRuParse sqlRuParse = new SqlRuParse(new SqlRuDateTimeParser());
        System.out.println("Размер базы данных post = " + psqlStore.getAll().size() + ".");
        System.out.println();
        List<Post> posts = sqlRuParse.list("https://www.sql.ru/forum/job-offers");
        System.out.println("Добавление постов в таблицу.");
        for (Post post : posts) {
            psqlStore.save(post);
            System.out.println(post);
        }
        System.out.println("Размер базы данных post = " + psqlStore.getAll().size() + ".");
        System.out.println();
        System.out.println("Post с заданным id : " + psqlStore.findById(3));
        System.out.println();
        System.out.println("Удаление всех постов из таблицы и обнуление id.");
        psqlStore.deleteAllAndRestartId();
        System.out.println("Размер базы данных post = " + psqlStore.getAll().size() + ".");

    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try {
            properties.load(PsqlStore.class.getClassLoader().getResourceAsStream("psql.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    private static Post createPost(ResultSet resultSet) {
        Post post = null;
        try {
            post = new Post(
                    resultSet.getInt("id"),
                    resultSet.getString("name"),
                    resultSet.getString("link"),
                    resultSet.getString("text"),
                    resultSet.getTimestamp("created").toLocalDateTime());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return post;
    }

    public Connection getCnn() {
        return cnn;
    }
}
