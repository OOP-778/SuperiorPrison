package com.bgsoftware.superiorprison.plugin.test.script.util;

import com.bgsoftware.superiorprison.plugin.test.script.variable.GlobalVariableMap;
import com.oop.datamodule.StorageInitializer;
import com.oop.datamodule.gson.Gson;
import lombok.SneakyThrows;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PasteHelper {
    private static final Pattern ip_pattern = Pattern.compile("(?:[0-9]{1,3}\\.){3}[0-9]{1,3}");

    @SneakyThrows
    public static String paste(GlobalVariableMap variableMap) {
        Gson prettyfiedGson = StorageInitializer.getInstance().getPrettyfiedGson();
        String s = prettyfiedGson.toJson(variableMap);

        byte[] postData = s.getBytes(StandardCharsets.UTF_8);
        int postDataLength = postData.length;

        String requestURL = "https://paste.honeybeedev.com/documents";
        URL url = new URL(requestURL);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("User-Agent", "OOP Paste");
        conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
        conn.setUseCaches(false);

        String response = null;
        DataOutputStream wr;
        try {
            wr = new DataOutputStream(conn.getOutputStream());
            wr.write(postData);
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            response = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (response.contains("\"key\"")) {
            response = response.substring(response.indexOf(":") + 2, response.length() - 2);

            String postURL = "https://paste.honeybeedev.com/";
            response = postURL + response;
        }

        return response;
    }
}
