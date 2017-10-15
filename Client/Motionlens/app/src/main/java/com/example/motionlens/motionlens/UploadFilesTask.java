package com.example.motionlens.motionlens;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.URL;

import java.nio.ByteBuffer;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;


public class UploadFilesTask extends AsyncTask<URL, Integer, Boolean> {
    private static final String TAG = "dfManager";

    ByteBuffer data;

    UploadFilesTask(ByteBuffer data) {
        this.data = data;
    }

    @Override
    protected Boolean doInBackground(URL... urls) {
        boolean success = true;

        for (URL url : urls) {
            try {

                OkHttpClient client = new OkHttpClient();
                data.rewind();

                byte[] bbyte = new byte[data.remaining()];
                data.get(bbyte);

                RequestBody body = RequestBody.create(MediaType.parse("application/octet-stream"), bbyte);
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();

                success = client.newCall(request).execute().isSuccessful();

            } catch (IOException e) {
                e.printStackTrace();
            }

            return success;
        }
        return null;
    }
}
