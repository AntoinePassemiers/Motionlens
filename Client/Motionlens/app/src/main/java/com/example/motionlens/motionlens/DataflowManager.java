package com.example.motionlens.motionlens;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class DataflowManager {
    private static final String SERVER_URL = "http://4c06951c.ngrok.io/file";
    private static final String TAG = "dfManager";
    private Context context;
    public static final int SAMPLE_SIZE = (3 * Float.SIZE + Long.SIZE) / Byte.SIZE;
    public static final int MAX_N_SAMPLES = 100;
    public static final int MAX_N_BYTES_PER_BUFFER = MAX_N_SAMPLES * SAMPLE_SIZE;
    public static final int HEADER_N_BYTES = 4 * 4;
    public static final int MAX_N_BYTES = 2 * MAX_N_BYTES_PER_BUFFER + HEADER_N_BYTES;

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
        // packData();
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
            flushBuffers();
            acc_data.putFloat(2.4f); acc_data.putFloat(63.32f); acc_data.putFloat(234.29f);
            acc_data.putLong(98345);
            acc_data.putFloat(2.4f); acc_data.putFloat(63.32f); acc_data.putFloat(234.29f);
            acc_data.putLong(98345);
            acc_data_n_samples = 2;

            int n_required_bytes = HEADER_N_BYTES + SAMPLE_SIZE * (acc_data_n_samples + gyr_data_n_samples);
            ByteBuffer data = ByteBuffer.allocate(n_required_bytes);
            int activity_id = 45; // TODO
            int user_id = 26; // TODO
            data.put(ByteBuffer.allocate(4).putInt(activity_id).array());
            data.put(ByteBuffer.allocate(4).putInt(user_id).array());
            data.put(ByteBuffer.allocate(4).putInt(acc_data_n_samples).array());
            data.put(ByteBuffer.allocate(4).putInt(gyr_data_n_samples).array());
            data.put(Arrays.copyOfRange(acc_data.array(), 0, SAMPLE_SIZE * acc_data_n_samples));
            data.put(Arrays.copyOfRange(gyr_data.array(), 0, SAMPLE_SIZE * gyr_data_n_samples));

            flushBuffers();

            new UploadFilesTask(data, n_required_bytes).execute(new URL(SERVER_URL));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
