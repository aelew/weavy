package com.aelew.weavy.util;

import org.apache.logging.log4j.LogManager;

@SuppressWarnings("StringConcatenationArgumentToLogCall")
public final class Logger {

    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger();
    private static final String PREFIX = "[weavy] ";

    private Logger() {
        throw new UnsupportedOperationException();
    }

    public static void info(final String message, final Object... params) {
        logger.info(PREFIX + message, params);
    }

    public static void error(final String message, final Object... params) {
        logger.error(PREFIX + message, params);
    }

    public static void error(final String message, final Throwable t) {
        logger.error(PREFIX + message, t);
    }

}
