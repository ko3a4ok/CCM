package com.mesosphere.ccm;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

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
            ((TextView)findViewById(R.id.cluster_user)).setText("Created by: " + o.optString("user"));
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
        final String urlString = CcmJsonArrayRequest.HOST + CcmJsonArrayRequest.MY_CLUSTERS + clusterId + "/";
        try {
            showDialog(DIALOG_LOADING);
            new AsyncTask<Void, Void, Integer>(){

                @Override
                protected Integer doInBackground(Void... voids) {
                    try {
                        URL url = new URL(urlString);
                        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
                        httpCon.setRequestProperty(
                                "Authorization", "Token " + Props.TOKEN);
                        httpCon.setRequestMethod("DELETE");
                        return httpCon.getResponseCode();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Integer code) {
                    System.err.println("RESPONSE CODE: " + code);
                    dismissDialog(DIALOG_LOADING);
                    if (code != null && code < 400) {
                        Toast.makeText(ClusterDetailActivity.this, R.string.delete_successful, Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(ClusterDetailActivity.this, R.string.delete_error, Toast.LENGTH_LONG).show();
                    }
                }
            }.execute();


        } catch (Exception uee) {
            Toast.makeText(this, R.string.delete_error, Toast.LENGTH_LONG).show();
            uee.printStackTrace();
        }

    }

}
