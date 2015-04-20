package net.cubespace.geSuit.core.storage;

import java.util.Map;

public interface Storable {
    public void save(Map<String, String> values);
    public void load(Map<String, String> values);
}
