package com.clustox.cxlogging;

/**
 * Created by Johar on 8/12/2016.
 */

public class Constants {

    /**
     * Contains the String using for logging.
     */
    public abstract class Log {
        public static final String LOG_FILE_NAME = "Logs.txt";
        public static final String TYPE = "log_type";
        public static final String INFO = "INFO";
        public static final String ERROR = "ERROR";
        public static final String DEBUG = "DEBUG";
        public static final String LOGGLY_TOKEN = "";//TODO: add your own loggly token here
        public static final String LOGGLY_SERVER_URL = "https://logs-01.loggly.com/bulk/" + LOGGLY_TOKEN + "/tag/bulk/";
    }
}
