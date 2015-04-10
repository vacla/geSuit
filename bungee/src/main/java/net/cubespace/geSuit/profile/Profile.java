package net.cubespace.geSuit.profile;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Profile {
    private static final URL PROFILE_URL;
    static {
        try {
            PROFILE_URL = new URL("https://api.mojang.com/profiles/minecraft");
        } catch (MalformedURLException ex) {
            throw new IllegalStateException();
        }
    }
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final ThreadLocal<Gson> gson = new ThreadLocal<Gson>() {

        @Override
        protected Gson initialValue() {
            return new Gson();
        }
        
    };
    private String id;
    private String name;


    public UUID getUUID() {
        BigInteger uuid = new BigInteger(id, 16);
        long lsb = uuid.longValue();
        long msb = uuid.shiftRight(64).longValue();
        return new UUID(msb, lsb);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static Map<String,UUID> getOnlineUUIDs(List<String> players) throws IOException {
        int offset = 0;
        Map<String,UUID> result = new HashMap<>();
        while (offset < players.size()) {
            List<String> batch = players.subList(offset, Math.min(offset + 100, players.size()));
            offset += batch.size();
            HttpURLConnection connection = (HttpURLConnection) PROFILE_URL.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type","application/json; encoding=UTF-8");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            OutputStream out = connection.getOutputStream();
            out.write(gson.get().toJson(batch).getBytes(UTF8));
            out.close();
            Reader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), UTF8));
            Profile[] profiles = gson.get().fromJson(in, Profile[].class);
            for (Profile profile : profiles) {
                result.put(profile.getName(), profile.getUUID());
            }
        }
        return result;
    }
}