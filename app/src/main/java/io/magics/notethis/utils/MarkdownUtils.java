package io.magics.notethis.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MarkdownUtils {

    private static final String TAG = "MarkdownUtils";
    private static final String SPLIT_STRING = "<sup></sup>";

    public static List<String> loadHelpFile(Context context) {
        InputStream stream = null;

        try {
            stream = context.getAssets().open("help.md");
        } catch (IOException e) {
            Log.d(TAG, "loadHelp: ", e);
        }

        return readStream(stream);
    }

    private static List<String> readStream(InputStream stream) {
        List<String> tempList = new ArrayList<>();
        List<String> retList = new ArrayList<>();

        if (stream != null){
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuilder stringBuilder = new StringBuilder();

                String line;

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line)
                            .append("\n");
                }

                if (stringBuilder.indexOf(SPLIT_STRING) != -1) {
                    String[] paragraphArray = stringBuilder.toString().split(SPLIT_STRING);
                    tempList.addAll(Arrays.asList(paragraphArray));
                }

                for (int i = 0; i < tempList.size(); i++) {
                    String str = tempList.get(i);
                    retList.add(str.replaceAll(SPLIT_STRING, "\n"));
                }

            } catch (IOException e) {
                Log.d(TAG, "readStream: ", e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.d(TAG, "readStream: ", e);
                    }
                }
            }
        }
        return retList;
    }

    private MarkdownUtils() {}

}
