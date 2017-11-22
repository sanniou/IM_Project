package library.san.library_ui.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by hl on 2016/9/27.
 */

public class ExecutorTasks {
    private static ExecutorTasks mTasks;
    private ExecutorService fixedThreadPool;

    private ExecutorTasks() {
        fixedThreadPool = Executors.newFixedThreadPool(8);
    }

    public static ExecutorTasks getInstance() {
        if (mTasks == null) {
            mTasks = new ExecutorTasks();
        }
        return mTasks;
    }

    public boolean isShutdown() {
        return fixedThreadPool.isShutdown();
    }

    public void shutdown() {
        fixedThreadPool.shutdown();
    }

    public void postRunnable(Runnable _run) {
        fixedThreadPool.execute(_run);
    }
}
