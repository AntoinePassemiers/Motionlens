package com.example.motionlens.motionlens;

import android.content.Context;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;


public class DataflowManager {
    private static final String TAG = "dfManager";
    private Context context;
    public static final int SAMPLE_SIZE = (3 * Float.SIZE + Long.SIZE) / Byte.SIZE;
    public static final int MAX_N_BYTES = 100 * SAMPLE_SIZE;

    private Integer current_ha_id;
    private ByteArrayOutputStream bas;
    private ByteBuffer acc_data;
    private int acc_data_n_samples;
    private ByteBuffer gyr_data;
    private int gyr_data_n_samples;

    public DataflowManager(Context context) {
        this.context = context;
        bas = new ByteArrayOutputStream();
        flushBuffers();
    }

    public void flushBuffers() {
        acc_data = ByteBuffer.allocate(MAX_N_BYTES);
        gyr_data = ByteBuffer.allocate(MAX_N_BYTES);
        acc_data_n_samples = 0;
        gyr_data_n_samples = 0;
    }

    public void startHA(Integer ha_id) {
        current_ha_id = ha_id;
        flushBuffers();
    }

    public void stopHA() {
        packData();
    }

    public void addAccSample(float X, float Y, float Z) {
        acc_data.putFloat(X); acc_data.putFloat(Y); acc_data.putFloat(Z);
        acc_data.putLong(System.currentTimeMillis());
        acc_data_n_samples += SAMPLE_SIZE;
        if (acc_data_n_samples >= MAX_N_BYTES) packData();
    }

    public void addGyrSample(float X, float Y, float Z) {
        gyr_data.putFloat(X); gyr_data.putFloat(Y); gyr_data.putFloat(Z);
        gyr_data.putLong(System.currentTimeMillis());
        gyr_data_n_samples += SAMPLE_SIZE;
        if (gyr_data_n_samples >= MAX_N_BYTES) packData();
    }

    public void packData() {
        try {
            String filename = "tmp";
            FileOutputStream outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(ByteBuffer.allocate(4).putInt(acc_data_n_samples).array());
            outputStream.write(ByteBuffer.allocate(4).putInt(gyr_data_n_samples).array());
            outputStream.write(acc_data.array());
            outputStream.write(gyr_data.array());
            outputStream.close();
            Log.d(TAG, "Saved file " + filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        flushBuffers();
    }
}
