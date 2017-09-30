package com.example.motionlens.motionlens;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.HttpURLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class UploadFilesTask extends AsyncTask<URL, Integer, Boolean> {
    private static final String TAG = "dfManager";
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final MediaType MEDIA_TYPE = MediaType.parse("mdcar");
    ByteBuffer data;

    UploadFilesTask(ByteBuffer data) {
        this.data = data;
    }

    @Override
    protected Boolean doInBackground(URL... urls) {
        boolean success = true;

        for (URL url : urls) {
            try {

							HttpURLConnection con = (HttpURLConnection) url.openConnection();
							con.setRequestMethod("POST");
							con.setDoOutput(true);

							BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(con.getOutputStream(), "UTF-8"));

							//byte array to char array @VGR - https://stackoverflow.com/a/20916888/4654681
							char[] chArr = new char[TOTAL_N_BYTES];
							for (int k = 0; k < TOTAL_N_BYTES; k++) {
									chArr[k] = (char) (data.get(k) & 0xff);
							}

							bw.write(chArr);	
							bw.flush();
							bw.close();

							int responseCode = con.getResponseCode();


            } catch (IOException e) {
                e.printStackTrace();
            }

            return success;
        }
        return null;
    }

    void postRequest(String postUrl,String postBody) throws IOException {

        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(JSON, postBody);

        Request request = new Request.Builder()
                .url(postUrl)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("TAG",response.body().string());
            }
        });
    }
}
