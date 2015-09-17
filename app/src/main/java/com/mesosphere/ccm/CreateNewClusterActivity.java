package com.mesosphere.ccm;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;


public class CreateNewClusterActivity extends ActionBarActivity {
    public final static int DIALOG_LOADING = 1001;

    private SecureRandom random = new SecureRandom();

    public String nextSessionId() {
        return new BigInteger(30, random).toString(32);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_cluster);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((TextView)findViewById(R.id.cluster_name)).setText("MesosphereBot-" + nextSessionId());
        ((TextView)findViewById(R.id.cluster_desc)).setText("Created from android");
        ((Spinner)findViewById(R.id.providers)).setAdapter(ArrayAdapter.createFromResource(this, R.array.providers, android.R.layout.simple_spinner_item));
        ((Spinner)findViewById(R.id.regions)).setAdapter(ArrayAdapter.createFromResource(this, R.array.regions, android.R.layout.simple_spinner_item));
        ((Spinner)findViewById(R.id.duration)).setAdapter(ArrayAdapter.createFromResource(this, R.array.duration, android.R.layout.simple_spinner_item));
        ((Spinner)findViewById(R.id.template)).setAdapter(ArrayAdapter.createFromResource(this, R.array.template, android.R.layout.simple_spinner_item));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_new_cluster, menu);
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

    private long getTime() {
        int pos = ((Spinner)findViewById(R.id.duration)).getSelectedItemPosition();
        if (pos == 0) return 60;
        if (pos == 1) return 120;
        if (pos == 2) return 240;
        if (pos == 3) return 480;
        if (pos == 4) return 960;
        return 60;
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


    public void onCreateCluster(View v) {
        final HttpClient httpClient = new DefaultHttpClient();

        final HttpPost httpPost = new HttpPost(CcmJsonArrayRequest.HOST + CcmJsonArrayRequest.MY_CLUSTERS);
        httpPost.setHeader("Authorization", "Token " + Props.TOKEN);
        List<NameValuePair> nameValuePairList = new ArrayList();
        nameValuePairList.add(new BasicNameValuePair("name", ((TextView)findViewById(R.id.cluster_name)).getText().toString()));
        nameValuePairList.add(new BasicNameValuePair("cluster_desc", ((TextView)findViewById(R.id.cluster_desc)).getText().toString()));
        nameValuePairList.add(new BasicNameValuePair("cloud_provider", "" + ((Spinner) findViewById(R.id.providers)).getSelectedItemPosition()));
        nameValuePairList.add(new BasicNameValuePair("region", "" + ((Spinner) findViewById(R.id.regions)).getSelectedItem()));
        nameValuePairList.add(new BasicNameValuePair("time", "" + getTime()));
        nameValuePairList.add(new BasicNameValuePair("template", "" + ((Spinner) findViewById(R.id.template)).getSelectedItem()));
        nameValuePairList.add(new BasicNameValuePair("public_agents", "1"));
        nameValuePairList.add(new BasicNameValuePair("private_agents", "1"));
        nameValuePairList.add(new BasicNameValuePair("adminlocation", "0.0.0.0/0"));

        try {
            UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(nameValuePairList);
            httpPost.setEntity(urlEncodedFormEntity);
            showDialog(DIALOG_LOADING);
            new AsyncTask<Void, Void, Integer>(){

                @Override
                protected Integer doInBackground(Void... voids) {
                    try {
                        HttpResponse httpResponse = httpClient.execute(httpPost);
                        System.err.println("RESPONSE: " + EntityUtils.toString(httpResponse.getEntity()));
                        return httpResponse.getStatusLine().getStatusCode();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Integer code) {
                    dismissDialog(DIALOG_LOADING);
                    if (code != null && code < 400) {
                        Toast.makeText(CreateNewClusterActivity.this, R.string.create_successful, Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(CreateNewClusterActivity.this, R.string.create_error, Toast.LENGTH_LONG).show();
                    }
                }
            }.execute();


        } catch (Exception uee) {
            Toast.makeText(this, R.string.create_error, Toast.LENGTH_LONG).show();
            uee.printStackTrace();
        }
    }
}
