package de.legend.legendperms.database;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by YannicK S. on 28.05.2023
 */
public abstract class AsyncDatabaseUpdate {

    private static final ExecutorService EXECUTOR_POOL = Executors.newCachedThreadPool();

    /**
     * Führt eine gegebene Runnable-Aufgabe asynchron aus.
     *
     * @param runnable Die auszuführende Runnable-Aufgabe.
     */
    public void executeAsync(final Runnable runnable) {
        EXECUTOR_POOL.execute(runnable);
    }

}
