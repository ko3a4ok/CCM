package com.mesosphere.ccm;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ko3a4ok on 17.09.15.
 */
public class CcmJsonArrayRequest extends JsonArrayRequest {
    public final static String HOST = "http://ccm.mesosphere.com";
//    private final static String HOST = "http://10.2.2.71:8000";
    public final static String MY_CLUSTERS = "/api/cluster/";
    public final static String ALL_CLUSTERS = "/api/provider_cluster/0/";

    public CcmJsonArrayRequest(String path, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
        super(HOST + path, listener, errorListener);
        System.err.println("REQUEST: "+ HOST + path);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap();
        headers.put("Authorization", "Token " + Props.TOKEN);
        return headers;
    }
}
