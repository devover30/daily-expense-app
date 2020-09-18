package info.devram.dainikhatabook.Controllers;

import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;

import info.devram.dainikhatabook.Interfaces.ResponseAvailableListener;

public class TokenRequest implements Runnable {

    private static final String TAG = "TokenRequest";

    private HashMap<String, String> setupRequest;
    private ResponseAvailableListener mListener;

    public TokenRequest(HashMap<String, String> request, ResponseAvailableListener listener) {
        this.setupRequest = request;
        this.mListener = listener;
    }

    @Override
    public void run() {
        HttpURLConnection connection = null;
        BufferedReader reader;
        Converter converter = new Converter();
        converter.setFromString(true);

        if (setupRequest == null) {
            return;
        }

        try {
            String userCredentials = setupRequest.get("email") + ":" + setupRequest.get("password");
            Log.d(TAG, "run: " + userCredentials);
            byte[] encodedCredentials = Base64.encode(userCredentials.getBytes(), Base64.DEFAULT);
            String basicAuth = "Basic " + new String(encodedCredentials);
            URL url = new URL(setupRequest.get("url"));
            Log.d(TAG, "run: " + url);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", basicAuth);
            connection.setConnectTimeout(8000);
            connection.setDoOutput(true);
            Log.d(TAG, "run: " + connection.getRequestMethod());
            Log.d(TAG, "run: " + connection.getURL());
            Log.d(TAG, "run: " + connection.getResponseCode());

            int response = connection.getResponseCode();
            if (response != 200) {

                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));

                Log.d(TAG, "run: " + response);
                String errorResult = stringBuilder(reader);

                Log.d(TAG, "run: " + errorResult);

                converter.setStringData(errorResult);
                converter.run();

                Log.d(TAG, "run: " + errorResult);
                JSONObject jsonObject = converter.getJsonObject();

                this.mListener.onTokenResponse(jsonObject, response);
                this.sendErrorResponse(errorResult);
                return;
            }
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String okResult = stringBuilder(reader);
            converter.setStringData(okResult);
            converter.run();
            JSONObject jsonObject = converter.getJsonObject();
            this.mListener.onTokenResponse(jsonObject, response);

        } catch (MalformedURLException | SocketTimeoutException | SecurityException e) {
            this.sendErrorResponse(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            this.sendErrorResponse(e.getMessage());
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

    private void sendErrorResponse(String errMessage) {
        this.mListener.onErrorResponse(errMessage, 503);
    }
}
