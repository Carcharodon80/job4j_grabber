package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.sql.*;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

/**
 * 1. Quartz [#175122]
 * 1.1. Job c параметрами [#260360]
 */
public class AlertRabbit {

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try {
            properties.load(AlertRabbit.class.getClassLoader().getResourceAsStream("rabbit.properties"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return properties;
    }

    private static Connection initConnection(Properties properties) {
        Connection connection = null;
        try {
            Class.forName(properties.getProperty("jdbc.driver"));
            connection = DriverManager.getConnection(
                    properties.getProperty("jdbc.url"),
                    properties.getProperty("jdbc.username"),
                    properties.getProperty("jdbc.password")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }

    private static void createTable(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            String sql = String.format(
                    "create table if not exists rabbit(%s, %s)",
                    "id serial primary key",
                    "created_time bigint"
            );
            statement.execute(sql);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Properties properties = loadProperties();
        try (Connection connection = initConnection(properties)) {
            createTable(connection);
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap data = new JobDataMap();
            data.put("connection", connection);
            JobDetail job = newJob(Rabbit.class).usingJobData(data).build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(Integer.parseInt(properties.getProperty("rabbit.interval")))
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    public static class Rabbit implements Job {
        public Rabbit() {
            System.out.println(hashCode());
        }

        @Override
        public void execute(JobExecutionContext context) {
            System.out.println("Rabbit runs here...");
            Connection connection = (Connection) context.getJobDetail().getJobDataMap().get("connection");
            try {
                PreparedStatement statement = connection.prepareStatement(
                        "insert into rabbit (created_time) values (?);"
                );
                statement.setLong(1, System.currentTimeMillis());
                statement.execute();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }
}
