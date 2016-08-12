package com.clustox.cxlogging.network;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.clustox.cxlogging.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Johar on 8/2/2016.
 */

/**
 * The responsibility of this class is to use handle network class and return their response using {@link com.clustox.cxlogging.network.OnApiCallResponse}.
 */
public final class NetworkManager {


    private static NetworkManager sInstance;
    // Instantiate of Blazt API request.
    private RequestQueue mRequestQueue;
    private static Context sContext;

    private final int MY_SOCKET_TIMEOUT_MS = 20 * 1000;
    /**
     * Used to return response of API.
     */
    private OnApiCallResponse mApiResponse;

    private NetworkManager(Context context) {
        NetworkManager.sContext = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized NetworkManager getInstance(Context context, OnApiCallResponse apiResponse) {
        if (sInstance == null) {
            sInstance = new NetworkManager(context);
        }
        sInstance.mApiResponse = apiResponse;
        return sInstance;
    }

    /**
     * Getter of mRequestQueue
     *
     * @return {@link com.android.volley.RequestQueue}
     */
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(sContext.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }


    /**
     * Cancels all requests in this queue with the given tag. Tag must be non-null
     * and equality is by identity.
     *
     * @param tag
     */
    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }



    /**
     * This method finds the error for which network call fails.
     */
    private String findErrorType(Context context, VolleyError volleyError) {
        String errorString = context.getString(R.string.server_not_responding_message);
        if (volleyError instanceof NoConnectionError) {
            errorString = context.getString(R.string.no_internet_connection_message);
        } else if (volleyError instanceof TimeoutError) {
            errorString = context.getString(R.string.connection_time_out_message);
        } else if (volleyError instanceof AuthFailureError) {
            errorString = context.getString(R.string.auth_failure_message);
        } else if (volleyError instanceof ServerError) {
            errorString = context.getString(R.string.server_not_responding_message);
        } else if (volleyError instanceof NetworkError) {
            errorString = context.getString(R.string.network_error_message);
        } else if (volleyError instanceof ParseError) {
            errorString = context.getString(R.string.data_parsing_error);
        }
        return errorString;
    }


    /**
     * This method is used to send PUT/POST/DELETE/GET API call with some input JSON.
     */
    public void sendLogsToServer(final Context context, ArrayList<String> logsEvent, final String url, int requestType) {
        JSONObject jsonObject = new JSONObject();
        JSONArray eventsJSONArray = new JSONArray();
        try {
            for (String eventString : logsEvent) {
                JSONObject eventJSON = new JSONObject(eventString);
                eventsJSONArray.put(eventJSON);
            }
            jsonObject.putOpt("Events", eventsJSONArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonRequest = new JsonObjectRequest(requestType, url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        String response = jsonObject.optString("response");
                        if ("ok".equalsIgnoreCase(response)) {
                            mApiResponse.onSuccess(jsonObject, url);
                        } else {
                            mApiResponse.onFailure(jsonObject, url);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        mApiResponse.onError(findErrorType(context, volleyError));
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json");
                params.put("Accept", "application/json");
                params.put("Accept-Encoding", "utf-8");
                return params;
            }
        };
        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        NetworkManager.getInstance(context, mApiResponse).addToRequestQueue(jsonRequest);
    }

}
