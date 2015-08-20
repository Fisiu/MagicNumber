package pl.fidano.magicnumber;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String REMOTE_URL = "http://sluchaj.radiooswiecim.pl:8000/status-json.xsl";
    private TextView timestamp;
    private Button roll;

    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initControls();
        updateTimestamp();
        new StatusHandler().execute(REMOTE_URL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    private void initControls() {
        timestamp = (TextView) findViewById(R.id.timestamp);
        roll = (Button) findViewById(R.id.roll);
        roll.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.roll:
                updateTimestamp();
                new StatusHandler().execute(REMOTE_URL);
                break;
            default:
                break;
        }
    }

    private void updateTimestamp() {
        timestamp.setText(sdf.format(new Date()));
    }

    public class StatusHandler extends AsyncTask<String, Void, Integer> {

        private static final int IDLE_LISTENERS = 1; // how many static listeners, ie. icecast or so

        @Override
        protected Integer doInBackground(String... params) {

            int listeners = 0;

            try {
                URL url = new URL(params[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                InputStream in = new BufferedInputStream(conn.getInputStream());
                String json = IOUtils.toString(in, "UTF-8");

                JSONObject stats = new JSONObject(json);
                JSONObject server = stats.getJSONObject("icestats");
                listeners = server.getJSONObject("source").getInt("listeners");

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Error while geting data.", Toast.LENGTH_SHORT).show();
            }
            return listeners - IDLE_LISTENERS;
        }

        @Override
        protected void onPostExecute(final Integer result) {
            final Random rnd = new Random();
            final TextView lucky = (TextView) findViewById(R.id.lucky);
            new CountDownTimer(3000, 70) {
                @Override
                public void onTick(long millisUntilFinished) {
                    lucky.setText(""+rnd.nextInt(100));
                }

                @Override
                public void onFinish() {
                   lucky.setText(result.toString());
                }
            }.start();
        }
    }
}
