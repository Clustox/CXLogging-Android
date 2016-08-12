package com.clustox.cxlogging.logging;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.android.volley.Request;
import com.clustox.cxlogging.BuildConfig;
import com.clustox.cxlogging.Constants;
import com.clustox.cxlogging.R;
import com.clustox.cxlogging.network.NetworkManager;
import com.clustox.cxlogging.network.OnApiCallResponse;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

/**
 * Created by Johar on 8/2/2016.
 */

/**
 * Deals with logging{@link com.clustox.cxlogging.logging.CXLogger}.
 * This class write Log {@link com.clustox.cxlogging.logging.Event} in a file into its JSON form
 * and the send these logs to server.
 */
public final class CXLogger implements OnApiCallResponse {

    private final static String DELIMITER = "\nCXLogger\n";
    private final static int LOG_LIMIT = 50;
    private final static int ONE_MB_IN_BYTES = 1000000;

    private final static int FILE_LIMIT = 4 * ONE_MB_IN_BYTES;
    /**
     * Shared single instance for app.
     */
    private static CXLogger cxLogger;
    private Context mContext;
    private LogType mLogLevel;
    private Gson mGson;

    private ArrayList<String> mCachedEvents;
    private int mInitialCachedEventSize;

    /**
     * Contains All possible logs types.
     */
    public enum LogType {
        INFO("INFO"),
        DEBUG("DEBUG"),
        ERROR("ERROR"),
        NONE("NONE");

        private String text;

        LogType(String text) {
            this.text = text;
        }
    }

    /**
     * /**
     * This method is used to access the singleton instance.
     *
     * @param context
     * @return
     */
    public synchronized static CXLogger getInstance(Context context) {
        if (cxLogger == null) {
            cxLogger = new CXLogger(context);
        }

        return cxLogger;
    }

    /**
     * private constructor to make this class <b>Singleton</b>
     *
     * @param context
     */
    public CXLogger(Context context) {
        this.mContext = context;
        initializeLogger();
    }

    /**
     * Initialize CXLogger
     */
    private void initializeLogger() {
        Properties properties = new Properties();
        AssetManager assetManager = mContext.getAssets();
        try {
            InputStream inputStream = assetManager.open("app.properties");
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String level;
        if (properties.containsKey(Constants.Log.TYPE)) {
            level = properties.getProperty(Constants.Log.TYPE);
        } else {
            level = Constants.Log.INFO;
        }
        this.mLogLevel = LogType.valueOf(level);
        this.mCachedEvents = new ArrayList<>();
        this.mGson = new GsonBuilder()
                .disableHtmlEscaping()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .serializeNulls()
                .create();
    }

    /**
     * @param logString Message to Log
     * @param logType   Type of given log message.
     */
    public void log(String logString, LogType logType) {
        Event event = new Event(logType.text, logString, mContext);
        switch (this.mLogLevel) {
            case INFO:
                if (logType.equals(LogType.INFO)) {
                    this.logInfo(logString, event);
                } else if (logType.equals(LogType.DEBUG)) {
                    this.logDebug(logString, event);
                } else if (logType.equals(LogType.ERROR)) {
                    this.logError(logString, event);
                }
                break;
            case DEBUG:
                if (logType.equals(LogType.DEBUG)) {
                    this.logDebug(logString, event);
                } else if (logType.equals(LogType.ERROR)) {
                    this.logError(logString, event);
                }
                break;
            case ERROR:
                if (logType.equals(LogType.ERROR)) {
                    this.logError(logString, event);
                }
                break;
            case NONE:
                //do nothing
                break;
            default:
                //do nothing
                break;
        }
    }

    /**
     * Write Logs to file.
     */
    private void writeLogToFile(String logString) {

        try {
            String filename = Constants.Log.LOG_FILE_NAME;
            File root = android.os.Environment.getExternalStorageDirectory();
            File dir = new File(root.getAbsolutePath() + "/" + mContext.getString(R.string.app_name));
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File logFile = new File(dir, filename);
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            removeFileData(logFile);
            FileOutputStream fos = new FileOutputStream(logFile, true);
            fos.write(logString.getBytes());
            fos.write(DELIMITER.getBytes());
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * Reads complete file and return event json(String) array
     */
    private String[] getEventArrayFromFile() {
        try {
            String filename = Constants.Log.LOG_FILE_NAME;
            File root = android.os.Environment.getExternalStorageDirectory();
            File dir = new File(root.getAbsolutePath() + "/" + mContext.getString(R.string.app_name));
            dir.mkdirs();
            File logFile = new File(dir, filename);
            if (!logFile.exists()) {
                return null;
            }
            FileInputStream fis = new FileInputStream(logFile);
            int availableBytes = fis.available();
            byte[] data = new byte[availableBytes];
            fis.read(data);
            fis.close();
            return new String(data).split(DELIMITER);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Log <b>Info</b>
     */
    private void logInfo(String logString, Event event) {
        if (BuildConfig.DEBUG) {
            Log.i(Constants.Log.INFO, logString);
        }
        String eventToLog = this.mGson.toJson(event);
        writeLogToFile(eventToLog);
        sendLogsToServer(eventToLog);
    }

    /**
     * Log <b>Debug</b>
     */
    private void logDebug(String logString, Event event) {
        if (BuildConfig.DEBUG) {
            Log.d(Constants.Log.DEBUG, logString);
        }
        String eventToLog = this.mGson.toJson(event);
        writeLogToFile(eventToLog);
        sendLogsToServer(eventToLog);
    }

    /**
     * Logs <b>Error</b>
     */
    private void logError(String logString, Event event) {
        if (BuildConfig.DEBUG) {
            Log.e(Constants.Log.ERROR, logString);
        }
        String eventToLog = this.mGson.toJson(event);
        writeLogToFile(eventToLog);
        String[] fileLogs = getEventArrayFromFile();
        if (fileLogs != null && fileLogs.length > 0) {
            String[] previousLogs;
            if (fileLogs.length > 50) {
                int startIndex = fileLogs.length - LOG_LIMIT - 1;// "-1" is because error log is already added in file.
                int endIndex = fileLogs.length;
                previousLogs = Arrays.copyOfRange(fileLogs, startIndex, endIndex);
            } else {
                previousLogs = fileLogs;
            }
            this.mCachedEvents = new ArrayList<>(Arrays.asList(previousLogs));
            this.mInitialCachedEventSize = mCachedEvents.size();
        }
        sendLogsToServer(eventToLog);
    }

    /**
     * Check <b>Cached Logs</b> event list size. If list size is greater than or equal to required size then it sends to server.
     */
    private void sendLogsToServer(String eventToLog) {
        if (this.mCachedEvents.size() > 0) {
            this.mCachedEvents.add(eventToLog);
            if (this.mCachedEvents.size() >= this.mInitialCachedEventSize + LOG_LIMIT) {
                NetworkManager.getInstance(this.mContext, this).sendLogsToServer(this.mContext, this.mCachedEvents, Constants.Log.LOGGLY_SERVER_URL, Request.Method.POST);
            }
        }

    }

    private void removeFileData(File file) {
        try {
            long fileSize = file.length();
            if (FILE_LIMIT <= fileSize) {
                String[] fileLogs = getEventArrayFromFile();
                int startIndex = fileLogs.length / 5;// "-1" is because error log is already added in file.
                int endIndex = fileLogs.length;
                String[] remainingContent = Arrays.copyOfRange(fileLogs, startIndex, endIndex);
                FileOutputStream fos = null;
                fos = new FileOutputStream(file);
                fos.write(Arrays.toString(remainingContent).getBytes());
                fos.flush();
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onSuccess(JSONObject jsonObject, String url) {
        this.mCachedEvents.clear();
        this.mInitialCachedEventSize = 0;
    }

    @Override
    public void onFailure(JSONObject jsonObject, String url) {

    }

    @Override
    public void onError(String error) {

    }
}



