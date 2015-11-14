package jp.codic.plugins.intellij.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Debouncer {
    private ScheduledExecutorService scheduledExecutorService ;
    private long delay  = 0;

    private ScheduledFuture future;

    public Debouncer(long delay) {
        this.delay = delay;
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    public void push(Runnable runnable) {
        if (future != null) {
            future.cancel(false);
            future = null;
        }
        future = scheduledExecutorService.schedule(runnable, delay, TimeUnit.MILLISECONDS);
    }

    public void showdown() {
        scheduledExecutorService.shutdownNow();
    }
}
