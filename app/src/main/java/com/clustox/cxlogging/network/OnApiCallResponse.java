package com.clustox.cxlogging.network;

import org.json.JSONObject;

/**
 * Created by Johar on 8/2/2016.
 */

/**
 * API response interface.
 */
public interface OnApiCallResponse {

    /**
     * used to get response JSON from Server in case of API success
     */
    void onSuccess(JSONObject jsonObject, String url);

    /**
     * used to get response JSON from Server in case of API failure due to some invalid input
     */
    void onFailure(JSONObject jsonObject, String url);

    /**
     * used to get error message if API call gets fail due to server or network error.
     * @param error
     */
    void onError(String error);
}
