package com.amberclient.utils.murdererfinder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.List;
import java.util.HashSet;
import java.net.URL;

public class GithubFetcher {
    public static class GithubRelease {
        @Expose public String name;
        @Expose public String tag_name;
    }
    public static class MurderItems {
        @Expose public ArrayList<Integer> items;
    }

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
    private static final Executor executor = Executors.newSingleThreadExecutor();

    /* outdated list
    public static void getMurderItems(Consumer<HashSet<Integer>> consumer) {
        Runnable runnable = () -> {
            String data = fetchRaw("https://raw.githubusercontent.com/thatDudo/Murder-Mystery-Helper/1.18.1/murder_items.json");
            consumer.accept(new HashSet<>(gson.fromJson(data, MurderItems.class).items));
        };
        executor.execute(runnable);
    }*/

    public static String fetchRaw(String url) {
        try {
            URL _url = new URL(url);
            URLConnection connect = _url.openConnection();
            connect.setConnectTimeout(5000);

            BufferedReader br = new BufferedReader(new InputStreamReader(connect.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder html = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null)
                html.append(line);
            return html.toString();
        } catch (IOException exception) {
            return null;
        }
    }
}
