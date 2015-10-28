package net.cubespace.geSuit.profile;

import com.google.gson.Gson;
import net.cubespace.geSuit.Utilities;

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
import java.sql.Timestamp;
import java.util.*;

public class Profile {
    private static final URL PROFILE_URL;
    private static final String NAMEHISTORY_URL = "https://api.mojang.com/user/profiles/";

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

    public static Map<String, Timestamp> getMojangNameHistory(UUID playerUUID) {
        Map<String, Timestamp> result = new HashMap<>();
        String uid = Utilities.getStringFromUUID(playerUUID);
        String urlString = NAMEHISTORY_URL + uid + "/names";
        try {
            URL finalURL = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) finalURL.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json; encoding=UTF-8");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(false);
            Reader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), UTF8));
            NameHistory[] names = gson.get().fromJson(in, NameHistory[].class);
            for (NameHistory name : names) {
                if (name.getName() != null) {
                    result.put(name.getName(), name.getChangedToAt());
                }
            }
        } catch (MalformedURLException ex) {
            throw new IllegalStateException();
        } catch (IOException ex) {
            throw new IllegalStateException();
        }

        return result;
    }

    private class NameHistory {
        private String name = null;
        private Timestamp changedToAt = new Timestamp(0);

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Timestamp getChangedToAt() {
            return changedToAt;
        }

        public void setChangedToAt(String changedToAt) {
            this.changedToAt = Timestamp.valueOf(changedToAt);
        }
    }
}