package com.mesosphere.ccm.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;


import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.mesosphere.ccm.CcmJsonArrayRequest;
import com.mesosphere.ccm.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mesosphere.ccm.fragments.ClustersFragment.*;

public class LogsFragment extends ListFragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    List<Map<String, String>> logs;


    // TODO: Rename and change types of parameters
    public static LogsFragment newInstance(String param1, String param2) {
        LogsFragment fragment = new LogsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LogsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logs = new ArrayList();
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        final ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        // TODO: Change Adapter to display your content

        setListAdapter(new ClustersAdapter(getActivity(), logs));
        Volley.newRequestQueue(getActivity()).add(new CcmJsonArrayRequest(CcmJsonArrayRequest.LOGS, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                System.err.println("RESPONSE: " + response);
                int n = response.length();
                for (int i = 0; i < n; i++) {
                    JSONObject o = response.optJSONObject(i);
                    Map<String, String> data = parse(o);
                    logs.add(data);
                }
                ((SimpleAdapter)getListAdapter()).notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.err.println("ERROR: " + error);
                Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_LONG).show();
            }
        }));

    }

    private Map<String, String> parse(JSONObject o) {
        System.err.println("LOG: " + o);
        HashMap<String, String> data = new HashMap<>();
        data.put(ClustersAdapter.from[0], o.optString("cluster_name"));
        data.put(ClustersAdapter.from[1], "User: " + o.optString("user"));
        data.put(ClustersAdapter.from[2], o.toString());
        data.put(ClustersAdapter.from[4], o.optString("cluster_desc"));
        try {
            data.put(ClustersAdapter.from[3], "" + OUTPUT.format(SDF.parse(o.optString("time"))));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
    }

}
