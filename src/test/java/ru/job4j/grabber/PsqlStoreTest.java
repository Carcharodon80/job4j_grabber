package ru.job4j.grabber;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Properties;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PsqlStoreTest {
    private static PsqlStore psqlStore;

    @BeforeClass
    public static void initConnection() {
        try (InputStream in = PsqlStoreTest.class.getClassLoader().getResourceAsStream("test.properties")) {
            Properties config = new Properties();
            config.load(in);
            psqlStore = new PsqlStore(config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void closeConnection() {
        try {
            psqlStore.getCnn().close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @After
    public void wipeTable() {
        try (PreparedStatement statement = psqlStore.getCnn().prepareStatement("delete from post")) {
            statement.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Test
    public void whenSaveAndGetPostSame() {
        Post post = new Post("title", "link", "description",
                LocalDateTime.of(2021, 12, 24, 14, 55));
        Post post2 = new Post("title2", "link2", "description2",
                LocalDateTime.of(2021, 12, 24, 14, 55));
        psqlStore.save(post);
        psqlStore.save(post2);
        assertThat(post, is(psqlStore.findById(post.getId())));
        assertThat(post2, is(psqlStore.findById(post2.getId())));
    }
}