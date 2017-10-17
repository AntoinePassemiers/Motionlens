package com.example.motionlens.motionlens;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static boolean USE_WAKE_LOCK = true;
    private static Integer ACCELEROMETER_DELAY = 200000;
    private static Integer GYROSCOPE_DELAY = 200000;

    private boolean recording = false;
    private int current_ha_id = -1;

    private SensorManager sensorManager;
    private Sensor accelerometer, gyroscope;
    private Context context;
    private PowerManager.WakeLock wakeLock;

    private TextView currentX, currentY, currentZ;
    private TextView gyroscopeX, gyroscopeY, gyroscopeZ;
    private Button recordButton;
    private Spinner spinner;

    private HashMap<String, Integer> human_activities;
    private DataflowManager dfManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        context = getApplicationContext();

        human_activities = new HashMap<String, Integer>();
        dfManager = new DataflowManager(context, getDeviceID());

        // https://developer.android.com/training/keyboard-input/style.html
        spinner = (Spinner) findViewById(R.id.activity_with_white_list);
        String[] has = getResources().getStringArray(R.array.human_activities);
        for (String ha : has) {
            current_ha_id = Integer.parseInt(ha.substring(0, 3));
            String ha_name = ha.substring(6);
            assert(human_activities.get(ha_name) == null);
            human_activities.put(ha_name, current_ha_id);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                new ArrayList<String>(human_activities.keySet()));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        currentX = (TextView) findViewById(R.id.currentX);
        currentY = (TextView) findViewById(R.id.currentY);
        currentZ = (TextView) findViewById(R.id.currentZ);
        gyroscopeX = (TextView) findViewById(R.id.gyroscopeX);
        gyroscopeY = (TextView) findViewById(R.id.gyroscopeY);
        gyroscopeZ = (TextView) findViewById(R.id.gyroscopeZ);

        recordButton = (Button) findViewById(R.id.record_button);
        recordButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            recording = !recording;
            if (recording) { // Start recording
                recordButton.setText("Pause");
                Log.d("Selected text", spinner.getSelectedItem().toString().split("\n")[0]);
                String selection = spinner.getSelectedItem().toString();
                current_ha_id = human_activities.get(selection.split("\n")[0]);
                // if (current_ha_id == null) throw new AssertionError("Could not find activity by name");
                dfManager.startHA(current_ha_id);
            }
            else { // Stop recording
                recordButton.setText("Record");
                dfManager.stopHA();
            }
            }
        });

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        }
        else {
            // TODO: raise error
        }
    }

    protected void onResume() {
        super.onResume();
        releaseWakeLock();
        sensorManager.registerListener(this, accelerometer, ACCELEROMETER_DELAY);
        sensorManager.registerListener(this, gyroscope, GYROSCOPE_DELAY);
    }

    protected void onPause() {
        super.onPause();
        releaseWakeLock();
        sensorManager.unregisterListener(this);
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

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == accelerometer) {
            currentX.setText(Float.toString(event.values[0]));
            currentY.setText(Float.toString(event.values[1]));
            currentZ.setText(Float.toString(event.values[2]));
            if (recording) {
                dfManager.addAccSample(event.values[0], event.values[1], event.values[2]);
            }
        }
        else if (event.sensor == gyroscope) {
            gyroscopeX.setText(Float.toString(event.values[0]));
            gyroscopeY.setText(Float.toString(event.values[1]));
            gyroscopeZ.setText(Float.toString(event.values[2]));
            if (recording) {
                dfManager.addGyrSample(event.values[0], event.values[1], event.values[2]);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO
    }

    public void acquireWakeLock() {
        if (USE_WAKE_LOCK) {
            final PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            releaseWakeLock();
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PARTIAL_WAKE_LOCK");
            wakeLock.acquire();
        }
    }

    public void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }
    }

    public Integer getDeviceID() {
        SharedPreferences sp = getSharedPreferences("HAR-prefs", Activity.MODE_PRIVATE);
        Integer device_id = sp.getInt("device_id", -1);
        if(device_id == -1) {
            Random rng = new Random();
            device_id = (int)(rng.nextDouble() * Integer.MAX_VALUE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt("device_id", device_id);
            editor.commit();
        }
        return device_id;
    }
}
