package com.example.motionlens.motionlens;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class DataflowManager {
    private static final String SERVER_URL = "http://renavspainatal.pythonanywhere.com/file";
    private static final String TAG = "dfManager";
    private Context context;

    public static final int INT_SIZE_IN_BYTES = Integer.SIZE / Byte.SIZE;
    public static final int LONG_SIZE_IN_BYTES = Long.SIZE / Byte.SIZE;
    public static final int FLOAT_SIZE_IN_BYTES = Float.SIZE / Byte.SIZE;
    public static final int SAMPLE_SIZE = 3 * FLOAT_SIZE_IN_BYTES + LONG_SIZE_IN_BYTES;
    public static final int MAX_N_SAMPLES = 20000;
    public static final int MAX_N_BYTES_PER_BUFFER = MAX_N_SAMPLES * SAMPLE_SIZE;
    public static final int HEADER_N_BYTES = 4 * LONG_SIZE_IN_BYTES;

    private Integer current_ha_id;
    private Integer device_id;
    private ByteBuffer acc_data;
    private int acc_data_n_samples;
    private ByteBuffer gyr_data;
    private int gyr_data_n_samples;

    public DataflowManager(Context context, Integer device_id) {
        this.context = context;
        this.device_id = device_id;
        flushBuffers();
    }

    public void flushBuffers() {
        acc_data = ByteBuffer.allocate(MAX_N_BYTES_PER_BUFFER);
        gyr_data = ByteBuffer.allocate(MAX_N_BYTES_PER_BUFFER);
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
        acc_data_n_samples += 1;
        if (acc_data_n_samples + gyr_data_n_samples >= MAX_N_SAMPLES) packData();
    }

    public void addGyrSample(float X, float Y, float Z) {
        gyr_data.putFloat(X); gyr_data.putFloat(Y); gyr_data.putFloat(Z);
        gyr_data.putLong(System.currentTimeMillis());
        gyr_data_n_samples += 1;
        if (acc_data_n_samples + gyr_data_n_samples >= MAX_N_SAMPLES) packData();
    }

    public void packData() {
        try {
            int n_required_bytes = HEADER_N_BYTES + SAMPLE_SIZE * (acc_data_n_samples + gyr_data_n_samples);
            ByteBuffer data = ByteBuffer.allocate(n_required_bytes);
            data.put(ByteBuffer.allocate(INT_SIZE_IN_BYTES).putInt(current_ha_id).array());
            data.put(ByteBuffer.allocate(INT_SIZE_IN_BYTES).putInt(device_id).array());
            data.put(ByteBuffer.allocate(INT_SIZE_IN_BYTES).putInt(acc_data_n_samples).array());
            data.put(ByteBuffer.allocate(INT_SIZE_IN_BYTES).putInt(gyr_data_n_samples).array());
            data.put(Arrays.copyOfRange(acc_data.array(), 0, SAMPLE_SIZE * acc_data_n_samples));
            data.put(Arrays.copyOfRange(gyr_data.array(), 0, SAMPLE_SIZE * gyr_data_n_samples));

            flushBuffers();

            new UploadFilesTask(data, n_required_bytes).execute(new URL(SERVER_URL));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setHAID(int ha_id) {
        current_ha_id = ha_id;
    }
}
