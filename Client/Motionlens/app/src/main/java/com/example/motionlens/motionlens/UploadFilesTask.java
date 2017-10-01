package com.example.motionlens.motionlens;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.HttpURLConnection;
import java.nio.ByteBuffer;


public class UploadFilesTask extends AsyncTask<URL, Integer, Boolean> {
    private static final String TAG = "dfManager";

    ByteBuffer data;

    UploadFilesTask(ByteBuffer data) {
        this.data = data;
    }

    private void sendGet(URL url) throws Exception{
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        int responseCode = con.getResponseCode();
        System.out.println("get resp code: " + responseCode);

        BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()));

        System.out.println(in.readLine());
    }

    @Override
    protected Boolean doInBackground(URL... urls) {
        boolean success = true;

        for (URL url : urls) {
            try {
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setDoOutput(true);

                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(con.getOutputStream(), "US-ASCII"));

                //byte array to char array @VGR - https://stackoverflow.com/a/20916888/4654681
                char[] chArr = new char[DataflowManager.MAX_N_BYTES];
                for (int k = 0; k < DataflowManager.MAX_N_BYTES; k++) {
                    chArr[k] = (char) data.get(k);
                }

                bw.write(chArr, 0, chArr.length);
                bw.flush();
                bw.close();

                int responseCode = con.getResponseCode();
                System.out.println("Send " + DataflowManager.MAX_N_BYTES + " bytes");
                System.out.println("Response code: " + responseCode);


            } catch (IOException e) {
                e.printStackTrace();
            }

            return success;
        }
        return null;
    }
}
