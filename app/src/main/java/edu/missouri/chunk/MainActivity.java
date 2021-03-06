package edu.missouri.chunk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.util.Date;

import static android.os.BatteryManager.BATTERY_PLUGGED_AC;
import static android.os.BatteryManager.BATTERY_PLUGGED_USB;
import static android.os.BatteryManager.BATTERY_PLUGGED_WIRELESS;
import static android.os.BatteryManager.EXTRA_LEVEL;

public class MainActivity extends Activity {
    private class BatteryReceiver extends BroadcastReceiver {
        public static final int TRIGGER = 90;

        @Override
        public void onReceive(Context context, Intent intent) {
            int batteryLevel = intent.getIntExtra(EXTRA_LEVEL, 0);
            int charging     = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);

            boolean isChargingAC       = charging == BATTERY_PLUGGED_AC;
            boolean isChargingUsb      = charging == BATTERY_PLUGGED_USB;
            boolean isChargingWireless = charging == BATTERY_PLUGGED_WIRELESS;

            boolean isCharging = isChargingAC || isChargingUsb || isChargingWireless;

            batteryTextView.setText(String.format("Battery: %d", batteryLevel));

            if (batteryLevel == TRIGGER) {
                if (isCharging) {
                    startButton.setText("Please unplug your charger!");
                } else {
                    unregisterReceiver(this);

                    // Collect the user input
                    int chunkSizeKB     = Integer.parseInt(chunksSpinner.getSelectedItem().toString());
                    int intervalSeconds = Integer.parseInt(interValSpinner.getSelectedItem().toString());

                    URI uri;

                    try {
                        uri = new URI(urlEditText.getText().toString());

                        performingSync = true;

                        // Initialize and start the data transmitter.
                        Log.d(TAG, "Initializing and starting data transmitter");
                        dataTransmitter.setFreq(intervalSeconds);
                        dataTransmitter.setSize(chunkSizeKB);
                        dataTransmitter.setUri(uri);
                        dataTransmitter.start();

                        startButton.setText(getString(R.string.running));

                        final String startTime = DATE_TIME_FORMAT.format(dataTransmitter.getStart());

                        startTextView.setText(String.format("Start: %s", startTime));
                        endTextView.setText("End:");

                        AsyncTask<Void, Void, Boolean> waitTask = new AsyncTask<Void, Void, Boolean>() {
                            @Override
                            protected Boolean doInBackground(Void... voids) {
                                while (MainActivity.performingSync) {
                                    try {
                                        Thread.sleep(500, 0);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }

                                return true;
                            }

                            @Override
                            protected void onPostExecute(Boolean aBoolean) {
                                super.onPostExecute(aBoolean);

                                progressBar.setVisibility(View.INVISIBLE);
                                endTextView.setText(String.format("End: %s", DATE_TIME_FORMAT.format(new Date())));

                                Context ctx = getApplicationContext();

                                MediaPlayer mediaPlayer = MediaPlayer.create(ctx, R.raw.sound_file_1);
                                mediaPlayer.start();

                                urlEditText.setEnabled(true);
                                chunksSpinner.setEnabled(true);
                                interValSpinner.setEnabled(true);

                                startButton.setEnabled(true);
                                startButton.setText(R.string.start);

                                MainActivity.counter = 0;
                            }
                        };

                        waitTask.execute();
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private class OnStartButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            final boolean areFieldsSet =
                    chunksSpinner.getSelectedItem().toString().length()   > 0 &&
                    interValSpinner.getSelectedItem().toString().length() > 0 &&
                    urlEditText.getText().toString().length()             > 0;

            // If all the fields are filled out when start is pressed,
            if (areFieldsSet) {

                registerReceiver(new BatteryReceiver(), new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

                startButton.setEnabled(false);
                urlEditText.setEnabled(false);
                chunksSpinner.setEnabled(false);
                interValSpinner.setEnabled(false);
                startButton.setEnabled(false);

                progressBar.setVisibility(View.VISIBLE);

                startButton.setText("Waiting for battery...");

            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder
                .setTitle("Error")
                .setMessage("Fields must not be empty.")
                .setNeutralButton(
                        "Ok",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        }
                );

                AlertDialog dialog = builder.create();
                dialog.show();
            }

            Log.d(TAG, "After waitTask.execute");
        }
    }

    private static final String TAG = "MainActivity";
    private final DateFormat DATE_TIME_FORMAT = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG);
    static boolean performingSync;

    private DataTransmitter dataTransmitter;

    private EditText    urlEditText;
    private Spinner     chunksSpinner;
    private Spinner     interValSpinner;
    private Button      startButton;
    private ProgressBar progressBar;
    private TextView    startTextView;
    private TextView    endTextView;
    private TextView    batteryTextView;

    static int counter;

    private static final String[] CHUNKS = new String[]{
            "50",
            "500",
            "5000"
    };

    private static final String[] INTERVALS = new String[]{
            "60",
            "330",
            "600"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        urlEditText     = (EditText)    findViewById(R.id.urlEdit);
        chunksSpinner   = (Spinner)     findViewById(R.id.chunkSizeSpinner);
        interValSpinner = (Spinner)     findViewById(R.id.intervalSpinner);
        progressBar     = (ProgressBar) findViewById(R.id.progressBar);
        startButton     = (Button)      findViewById(R.id.startButton);
        startTextView   = (TextView)    findViewById(R.id.startTextView);
        endTextView     = (TextView)    findViewById(R.id.endTextView);
        batteryTextView = (TextView)    findViewById(R.id.batteryTextView);

        counter = 0;

        progressBar.setVisibility(View.INVISIBLE);

        ArrayAdapter<String> chunk    = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, CHUNKS);
        ArrayAdapter<String> interval = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, INTERVALS);

        chunksSpinner.setAdapter(chunk);
        interValSpinner.setAdapter(interval);

        // On click listener
        startButton.setOnClickListener(new OnStartButtonClickListener());
        dataTransmitter = new DataTransmitter(this);
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
}