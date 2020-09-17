package info.devram.dainikhatabook.ErrorHandlers;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import info.devram.dainikhatabook.Interfaces.FileErrorLoggerListener;

public class LogError implements Runnable {

    //private static final String TAG = "LogError";

    private String fileName;
    private Exception error;
    private Context mContext;
    private FileOutputStream fos;
    private FileErrorLoggerListener mListener;
    private File file;

    public LogError(Exception error, Context context, FileErrorLoggerListener listener) {
        this.fileName = "app-error.json";
        this.error = error;
        this.mContext = context;
        this.mListener = listener;
    }

    private Boolean logError() {

        try {
            file = new File(mContext.getFilesDir(), fileName);

            fos = new FileOutputStream(file);

            if (file.exists()) {

                JSONObject jsonObject = this.appendToFile();

                JSONArray js = this.readFromFile();

                if (js == null) {
                    return false;
                }


                if (jsonObject != null) {
                    js.put(jsonObject);
                }
                fos.write(js.toString().getBytes());

                return true;


            }

            JSONArray jsonArray = new JSONArray();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Error", error.getMessage());

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);

            error.printStackTrace(pw);

            jsonObject.put("Stack", sw.toString());

            jsonArray.put(jsonObject);

            fos.write(jsonArray.toString().getBytes());

            return true;
        } catch (JSONException | IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mContext = null;
        }

    }


    private JSONArray readFromFile() {
        InputStreamReader inputStreamReader = null;
        BufferedReader reader;
        try {
            FileInputStream fis = new FileInputStream(file);

            inputStreamReader =
                    new InputStreamReader(fis, StandardCharsets.UTF_8);

            StringBuilder stringBuilder = new StringBuilder();

            reader = new BufferedReader(inputStreamReader);

            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                stringBuilder.append(line).append("\n");
            }

            return new JSONArray(stringBuilder.toString().toCharArray());
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (JSONException e) {
            e.getMessage();
            return null;
        } finally {
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    e.getMessage();
                }
            }

        }
    }

    private JSONObject appendToFile() {

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Error", error.getMessage());

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);

            error.printStackTrace(pw);

            jsonObject.put("Stack", sw.toString());

            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void run() {


        boolean result = this.logError();

        if (result) {
            mListener.fileStatusListener("File Created");
        } else {
            mListener.fileStatusListener("Error Creating File");
        }



    }
}
