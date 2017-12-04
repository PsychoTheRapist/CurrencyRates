package com.github.psychotherapist.currencyrates.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public final class NetworkUtils {
    public static String getJsonFromUrl(String urlString) throws IOException {
        URL ratesUrl = new URL(urlString);
        StringBuilder jsonStringBuilder = new StringBuilder();

        HttpURLConnection conn = (HttpURLConnection) ratesUrl.openConnection();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while((line = br.readLine()) != null) {
                jsonStringBuilder.append(line);
            }
        }
        finally {
            conn.disconnect();
        }

        return  jsonStringBuilder.toString();
    }
}