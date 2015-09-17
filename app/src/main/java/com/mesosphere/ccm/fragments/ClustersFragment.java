package com.mesosphere.ccm.fragments;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.melnykov.fab.FloatingActionButton;
import com.mesosphere.ccm.CcmJsonArrayRequest;
import com.mesosphere.ccm.ClusterDetailActivity;
import com.mesosphere.ccm.CustomSwipeRefreshLayout;
import com.mesosphere.ccm.MainActivity;
import com.mesosphere.ccm.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ClustersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ClustersFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_API = "param_api";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String api;
    private String mParam2;

    private ViewPager pager;
    CustomSwipeRefreshLayout refreshLayout;

    List<Map<String, String>> running;
    List<Map<String, String>> deleted;
    ListClusters runningFragment;
    ListClusters deletedFragment;

    private SimpleAdapter runningAdapter;
    private SimpleAdapter deletedAdapter;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param api Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ClustersFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ClustersFragment newInstance(String api) {
        ClustersFragment fragment = new ClustersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_API, api);
        fragment.setArguments(args);
        return fragment;
    }

    public ClustersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            api = getArguments().getString(ARG_API);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        refreshLayout = (CustomSwipeRefreshLayout) inflater.inflate(R.layout.fragment_clusters, container, false);
        pager = (ViewPager) refreshLayout.findViewById(R.id.pager);
        pager.setAdapter(new ClustersTabAdapter(getChildFragmentManager()));
        final ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        running = new ArrayList();
        deleted = new ArrayList();
        runningAdapter = new ClustersAdapter(getActivity(), running);
        deletedAdapter = new ClustersAdapter(getActivity(), deleted);

        runningFragment = newInstance(0);
        deletedFragment = newInstance(1);
        pager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        // When swiping between pages, select the
                        // corresponding tab.
                        actionBar.setSelectedNavigationItem(position);
                    }
                });
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
//        actionBar.setDisplayShowTitleEnabled(false);

        // Create a tab listener that is called when the user changes tabs.
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                pager.setCurrentItem(tab.getPosition());
            }

            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            }

            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
            }
        };

        actionBar.removeAllTabs();
        for (int i = 0; i < 2; i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(i == 0 ? R.string.running : R.string.deleted)
                            .setTabListener(tabListener));
        }
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(true);
                refresh();
            }
        });
        return refreshLayout;
    }

    private void refresh() {
        Volley.newRequestQueue(getActivity()).add(new CcmJsonArrayRequest(api, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                System.err.println("RESPONSE: " + response);
                running.clear();
                deleted.clear();
                int n = response.length();
                for (int i = 0; i < n; i++){
                    JSONObject o = response.optJSONObject(i);
                    Map<String, String> data = parse(o);

                    if (o.optInt("status") == 5)
                        deleted.add(data);
                    else
                        running.add(data);
                }
                runningAdapter.notifyDataSetChanged();
                deletedAdapter.notifyDataSetChanged();
                refreshLayout.setRefreshing(false);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.err.println("ERROR: " + error);
                Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_LONG).show();
                refreshLayout.setRefreshing(false);
            }
        }));
    }

    public static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    public static SimpleDateFormat OUTPUT = new SimpleDateFormat("HH:mm dd/MM/yyyy");
    private static Map<String, String> parse(JSONObject o) {
        Map<String, String> data = new HashMap();
        data.put(ClustersAdapter.from[0], o.optString("name"));
        data.put(ClustersAdapter.from[1], o.optString("cluster_desc"));
        data.put(ClustersAdapter.from[2], o.toString());
        int status = o.optInt("status");
        try {
            data.put(ClustersAdapter.from[3], "Created at: " + OUTPUT.format(SDF.parse(o.optString("created_at"))));
            String statusText = o.optString("status_text");
            if (status == 0) {
                long left = SDF.parse(o.optString("expired_at")).getTime() - System.currentTimeMillis() +  1000*60*60*3;
                left /= 60*1000;
                if (left > 0)
                statusText = String.format("Expires in %d hours %d minutes", left/60, left%60);
            }
            data.put(ClustersAdapter.from[4], statusText);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }


    private class ClustersTabAdapter extends FragmentPagerAdapter {

        public ClustersTabAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            return i == 0 ? runningFragment : deletedFragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

    }
    private static final String ARG_SECTION_NUMBER = "section_number";
    ListClusters newInstance(int sectionNumber) {
        ListClusters fragment = new ListClusters();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    class ListClusters extends Fragment {
        ListClusters() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            int data = getArguments().getInt(ARG_SECTION_NUMBER);
            final ListView rootView = (ListView) inflater.inflate(R.layout.fragment_main, container, false);
            refreshLayout.setOnChildScrollUpListener(new CustomSwipeRefreshLayout.OnChildScrollUpListener() {
                @Override
                public boolean canChildScrollUp() {
                    return rootView.getFirstVisiblePosition() > 0 ||
                            rootView.getChildAt(0) == null ||
                            rootView.getChildAt(0).getTop() < 0;
                }
            });
            rootView.setAdapter(data == 0 ? runningAdapter : deletedAdapter);
            ((FloatingActionButton)(getActivity()).findViewById(R.id.fab)).attachToListView(rootView);
            rootView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    System.err.println("Click >>> " + rootView.getAdapter().getItem(i));
                    Intent intent = new Intent(getActivity(), ClusterDetailActivity.class);
                    intent.putExtra("data", (String)((Map)rootView.getAdapter().getItem(i)).get("data"));
                    startActivity(intent);
                }
            });
            return rootView;
        }
    }

}
