package com.fatgyft.smartvelov.request;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.fatgyft.smartvelov.R;

public class RequestActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_request, menu);
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

        return super.onOptionsItemSelected(item);
    }

    private class getRouteAsyncTask extends AsyncTask<Void, Void, Void> {

        private ProgressDialog progress;
        private String json;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progress = new ProgressDialog(RequestActivity.this);
                    progress.setMessage("Trying to get json");
                    progress.show();
                }
            });
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (progress.isShowing()) {
                        progress.dismiss();
                    }

                }
            });
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ServiceHandler serviceHandler = new ServiceHandler();
            json = serviceHandler.makeServiceCall("http://private-5543d-smartvelov.apiary-mock.com/route", ServiceHandler.GET);

            return null;
        }
    }
}
