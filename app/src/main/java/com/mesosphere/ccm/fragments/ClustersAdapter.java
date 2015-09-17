package com.mesosphere.ccm.fragments;

import android.content.Context;
import android.widget.SimpleAdapter;

import com.mesosphere.ccm.R;

import java.util.List;
import java.util.Map;

/**
 * Created by ko3a4ok on 17.09.15.
 */
public class ClustersAdapter extends SimpleAdapter {
    static final String[] from = {"cluster_name", "cluster_desc","data", "created_at", "status"};
    private static final int[] to = {R.id.cluster_name, R.id.cluster_desc, R.id.cluster_data, R.id.created_at, R.id.cluster_status};
    public ClustersAdapter(Context context, List<? extends Map<String, ?>> data) {
        super(context, data, R.layout.cluster_item, from, to);
    }
}
