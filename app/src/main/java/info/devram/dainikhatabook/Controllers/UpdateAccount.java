package info.devram.dainikhatabook.Controllers;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.Callable;

public class UpdateAccount implements Callable<String>
{
    private static final String TAG = "UpdateAccount";

    private int responseStatus;
    private HashMap<String, String> setupRequest;

    public UpdateAccount(HashMap<String, String> setupRequest) {
        this.setupRequest = setupRequest;
    }

    @Override
    public String call() throws Exception {
        HttpURLConnection connection = null;
        BufferedWriter bufferedWriter;
        BufferedReader reader;

        if (setupRequest == null) {
            return null;
        }

        try {
            URL url = new URL(setupRequest.get("url"));

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + setupRequest.get("token"));
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            bufferedWriter = new BufferedWriter(new
                    OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8));

            bufferedWriter.write(setupRequest.get("data"));
            bufferedWriter.flush();
            bufferedWriter.close();

            int response = connection.getResponseCode();
            if (response != 201) {
                Log.d(TAG, "postRequest: " + response);
                Log.d(TAG, "postRequest: " + connection.getErrorStream());

                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));

                String errorResult = stringBuilder(reader);
                Log.d(TAG, "postRequest: " + errorResult);
                responseStatus = response;
                return errorResult;
            }

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String okResult = stringBuilder(reader);

            responseStatus = response;
            return okResult;

        } catch (MalformedURLException | SecurityException e) {
            responseStatus = 503;
            return e.getMessage();
        } catch (IOException e) {
            e.printStackTrace();
            responseStatus = 503;
            return e.getMessage();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String stringBuilder(BufferedReader reader) {
        StringBuilder result = new StringBuilder();

        try {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                result.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();
    }
}
