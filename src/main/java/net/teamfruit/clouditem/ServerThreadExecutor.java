package net.teamfruit.clouditem;

import com.google.common.util.concurrent.AbstractListeningExecutorService;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

public final class ServerThreadExecutor extends AbstractListeningExecutorService {

    public static final ServerThreadExecutor INSTANCE = new ServerThreadExecutor();

    private ServerThreadExecutor() {
    }

    private Deque<Runnable> queue = new ConcurrentLinkedDeque<>();

    @Override
    public void execute(Runnable runnable) {
        queue.offer(runnable);
    }

    public void executeQueuedTaskImmediately() {
        Runnable run;
        while((run = queue.poll()) != null)
            run.run();
    }

    @Override
    public void shutdown() {
    }

    @Override
    public List<Runnable> shutdownNow() {
        return new ArrayList<Runnable>();
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }
}