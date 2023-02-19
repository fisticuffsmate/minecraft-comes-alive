package net.mca.entity.ai.rest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.mca.MCA;
import net.mca.entity.VillagerEntityMCA;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.JsonHelper;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Classifier {
    public static final String URL = "http://snoweagle.tk/";

    private static final Map<String, List<String>> COMMANDS = new HashMap<>();

    static {
        COMMANDS.put("follow", List.of("follow"));
        COMMANDS.put("stay", List.of("stay"));
        COMMANDS.put("move", List.of("move freely"));
        COMMANDS.put("armor", List.of("armor", "equip"));
        COMMANDS.put("ridehorse", List.of("ride", "horse"));
        COMMANDS.put("sethome", List.of("set home"));
        COMMANDS.put("gohome", List.of("go home"));
        COMMANDS.put("setworkplace", List.of("set workplace", "work"));
        COMMANDS.put("trade", List.of("trade", "offer"));
        COMMANDS.put("inventory", List.of("inventory"));
        COMMANDS.put("gift", List.of("gift"));
        COMMANDS.put("stopworking", List.of("stop work"));
        COMMANDS.put("harvesting", List.of("harvest", "farm"));
        COMMANDS.put("chopping", List.of("chop", "wood"));
        COMMANDS.put("hunting", List.of("hunt"));
        COMMANDS.put("fishing", List.of("fish"));
    }

    public static Map<String, Float> request(String encodedURL) {
        try {
            // receive
            HttpURLConnection con = (HttpURLConnection)(new URL(encodedURL)).openConnection();
            con.setRequestProperty("Accept-Charset", StandardCharsets.UTF_8.toString());
            InputStream response = con.getInputStream();
            String body = IOUtils.toString(response, StandardCharsets.UTF_8);

            // parse json
            JsonObject map = JsonParser.parseString(body).getAsJsonObject();

            if (map.has("result")) {
                JsonObject json = map.get("result").getAsJsonObject();

                Map<String, Float> resultMap = new HashMap<>();
                for (String key : json.keySet()) {
                    resultMap.put(key, JsonHelper.getFloat(json, key));
                }

                return resultMap;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void answer(ServerPlayerEntity player, VillagerEntityMCA villager, String msg) {
        try {
            // build http request
            Map<String, String> params = new HashMap<>();
            params.put("prompt", msg);
            params.put("classes", COMMANDS.values().stream().flatMap(Collection::stream).collect(Collectors.joining(",")));

            // encode and create url
            String encodedURL = params.keySet().stream()
                    .map(key -> key + "=" + URLEncoder.encode(params.get(key), StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&", URL + "classify?", ""));

            // encode and create url
            Map<String, Float> resultMap = request(encodedURL);

            if (resultMap != null) {
                float best = 0.75f;
                String bestCommand = null;
                for (Map.Entry<String, List<String>> entry : COMMANDS.entrySet()) {
                    for (String s : entry.getValue()) {
                        float v = resultMap.get(s);
                        if (v > best) {
                            best = v;
                            bestCommand = entry.getKey();
                        }
                    }
                }

                if (bestCommand != null) {
                    villager.getInteractions().handle(player, bestCommand);
                }
            }
        } catch (Exception e) {
            MCA.LOGGER.error(e);
        }
    }
}
