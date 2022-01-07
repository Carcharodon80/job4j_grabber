package ru.job4j.grabber;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;

public class Grabber implements Grab {
    @Override
    public void init(Parse parse, Store store, Scheduler scheduler) throws SchedulerException {

    }
}
