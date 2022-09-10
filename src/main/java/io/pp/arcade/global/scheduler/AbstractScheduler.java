package io.pp.arcade.global.scheduler;

import lombok.Getter;
import lombok.Setter;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

public abstract class AbstractScheduler {
    private ThreadPoolTaskScheduler scheduler;
    @Getter @Setter
    protected String cron;
    @Getter @Setter
    protected Integer interval;

    public abstract Runnable runnable();

    public void renewScheduler() {
        scheduler.shutdown();
        startScheduler();
    }

    @PostConstruct
    public void init() {
        startScheduler();
    }

    @PreDestroy
    public void destroy() {
        scheduler.shutdown();
    }

    private void startScheduler() {
        scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.initialize();
        scheduler.schedule(this.runnable(), new CronTrigger(cron));
    }
}
