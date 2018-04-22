package com.idroi.marketsense.Logging;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Created by daniel.hsieh on 2017/1/3.
 */

public class MSLog {

    public static final String LOGGER_NAMESPACE = "com.idroi.marketsense";

    private static final String LOGTAG = "MarketSense";
    private static final Logger LOGGER = Logger.getLogger(LOGGER_NAMESPACE);
    private static final MSLogHandler LOG_HANDLER = new MSLogHandler();

    static {
        LOGGER.setUseParentHandlers(false);
        LOGGER.setLevel(Level.ALL);
        LOG_HANDLER.setLevel(Level.CONFIG);

        LogManager.getLogManager().addLogger(LOGGER);
        addHandler(LOGGER, LOG_HANDLER);
    }

    private MSLog() {}

    public static void v(final String message) {
        MSLog.v(message, null);
    }

    public static void d(final String message) {
        MSLog.d(message, null);
    }

    public static void i(final String message) {
        MSLog.i(message, null);
    }

    public static void w(final String message) {
        MSLog.w(message, null);
    }

    public static void e(final String message) {
        MSLog.e(message, null);
    }

    public static void v(final String message, final Throwable throwable){
        LOGGER.log(Level.FINE, message, throwable);
    }

    public static void d(final String message, final Throwable throwable){
        LOGGER.log(Level.CONFIG, message, throwable);
    }

    public static void i(final String message, final Throwable throwable){
        LOGGER.log(Level.INFO, message, throwable);
    }

    public static void w(final String message, final Throwable throwable){
        LOGGER.log(Level.WARNING, message, throwable);
    }

    public static void e(final String message, final Throwable throwable){
        LOGGER.log(Level.SEVERE, message, throwable);
    }

    public static void printStackTrace(final String message){
        MSLog.printStackTrace(message, null);
    }

    public static void printStackTrace(final String message, final Throwable throwable){
        for(int i = 4; i < Thread.currentThread().getStackTrace().length; i++)
            LOGGER.log(Level.CONFIG, message + ": " + Thread.currentThread().getStackTrace()[i].toString(), throwable);
    }

    public static void setSdkHandlerLevel(@NonNull final Level level) {
        LOG_HANDLER.setLevel(level);
    }

    private static void addHandler(@NonNull final Logger logger,
                                   @NonNull final Handler handler) {
        final Handler[] currentHandlers = logger.getHandlers();
        for (final Handler currentHandler : currentHandlers) {
            if (currentHandler.equals(handler)) {
                return;
            }
        }
        logger.addHandler(handler);
    }

    private static final class MSLogHandler extends Handler {
        private static final Map<Level, Integer> LEVEL_TO_LOG = new HashMap<Level, Integer>(7);

        /*
         * Mapping between Level.* and Log.*:
         * Level.FINEST  => Log.v
         * Level.FINER   => Log.v
         * Level.FINE    => Log.v
         * Level.CONFIG  => Log.d
         * Level.INFO    => Log.i
         * Level.WARNING => Log.w
         * Level.SEVERE  => Log.e
         */
        static {
            LEVEL_TO_LOG.put(Level.FINEST, Log.VERBOSE);
            LEVEL_TO_LOG.put(Level.FINER, Log.VERBOSE);
            LEVEL_TO_LOG.put(Level.FINE, Log.VERBOSE);
            LEVEL_TO_LOG.put(Level.CONFIG, Log.DEBUG);
            LEVEL_TO_LOG.put(Level.INFO, Log.INFO);
            LEVEL_TO_LOG.put(Level.WARNING, Log.WARN);
            LEVEL_TO_LOG.put(Level.SEVERE, Log.ERROR);
        }

        @Override
        @SuppressWarnings({"LogTagMismatch", "WrongConstant"})
        public void publish(final LogRecord logRecord) {
            if (isLoggable(logRecord)) {
                final int priority;
                if (LEVEL_TO_LOG.containsKey(logRecord.getLevel())) {
                    priority = LEVEL_TO_LOG.get(logRecord.getLevel());
                } else {
                    priority = Log.VERBOSE;
                }

                String message = logRecord.getMessage() + "\n";

                final Throwable error = logRecord.getThrown();
                if (error != null) {
                    message += Log.getStackTraceString(error);
                }

                Log.println(priority, LOGTAG, message);
            }
        }

        @Override public void close() {}

        @Override public void flush() {}
    }
}
