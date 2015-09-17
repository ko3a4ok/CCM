package com.mesosphere.ccm;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

import static com.mesosphere.ccm.fragments.ClustersFragment.*;

public class ClusterDetailActivity extends ActionBarActivity {
    public final static int DIALOG_LOADING = 1001;
    private int clusterId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cluster_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        init();
    }

    private void init() {
        try {
            JSONObject o = new JSONObject(getIntent().getStringExtra("data"));
            clusterId = o.optInt("id");
            ((TextView)findViewById(R.id.cluster_name)).setText(o.optString("name"));
            ((TextView)findViewById(R.id.cluster_desc)).setText(o.optString("cluster_desc"));
            ((TextView)findViewById(R.id.cluster_status)).setText(o.optString("status_text"));
            ((TextView)findViewById(R.id.cluster_cost)).setText(String.format("Cluster cost/hour: $%.2f", o.optDouble("cluster_cost")));
            ((TextView)findViewById(R.id.cluster_region)).setText("Region: " + o.optString("region"));
            try {
                ((TextView)findViewById(R.id.cluster_created_at)).setText("Created at: " + OUTPUT.format(SDF.parse(o.optString("created_at"))));
                if (o.optInt("status") == 0) {
                        long left = SDF.parse(o.optString("expired_at")).getTime() - System.currentTimeMillis() + 1000*60*60*3;
                    left /= 60*1000;
                    if (left > 0)
                        ((TextView)findViewById(R.id.cluster_status)).setText(String.format("Expires in %d hours %d minutes", left / 60, left % 60));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cluster_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
        }


        return super.onOptionsItemSelected(item);
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == DIALOG_LOADING) {
            ProgressDialog mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            return mProgressDialog;
        }
        return super.onCreateDialog(id);
    }

    public void onExtendCluster(View v) {

    }

    public void onDeleteCluster(View v) {

    }

}
