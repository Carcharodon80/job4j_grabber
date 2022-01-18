package ru.job4j.grabber;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import ru.job4j.grabber.utils.SqlRuDateTimeParser;
import ru.job4j.html.SqlRuParse;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * 6. Grabber. [#289477]
 */
public class Grabber implements Grab {
    private final Properties cfg = new Properties();

    public Store store() {
        return new PsqlStore(cfg);
    }

    public Scheduler scheduler() {
        Scheduler scheduler = null;
        try {
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return scheduler;
    }

    public void cfg() {
        try (InputStream in = new FileInputStream("app.properties")) {
            cfg.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init(Parse parse, Store store, Scheduler scheduler) throws SchedulerException {
        JobDataMap data = new JobDataMap();
        data.put("store", store);
        data.put("parse", parse);
        JobDetail job = newJob(GrabJob.class)
                .usingJobData(data)
                .build();
        SimpleScheduleBuilder times = simpleSchedule()
                .withIntervalInSeconds(Integer.parseInt(cfg.getProperty("time")))
                .repeatForever();
        Trigger trigger = newTrigger()
                .startNow()
                .withSchedule(times)
                .build();
        scheduler.scheduleJob(job, trigger);
    }

    public static class GrabJob implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            JobDataMap map = context.getJobDetail().getJobDataMap();
            Store store = (Store) map.get("store");
            Parse parse = (Parse) map.get("parse");
            List<Post> posts = parse.list("https://www.sql.ru/forum/job-offers");
            for (Post post : posts) {
                store.save(post);
            }
        }
    }

    public static void main(String[] args) {
        Grabber grab = new Grabber();
        grab.cfg();
        Store store = grab.store();
        Scheduler scheduler = grab.scheduler();
        Parse parse = new SqlRuParse(new SqlRuDateTimeParser());
        try {
            grab.init(parse, store, scheduler);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}

