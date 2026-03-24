package nsu.sd.tool;

import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public class JsonKeysReader {

    private final Map<String, Object> jsonMap;

    public Object get(String key) {
        return jsonMap.get(key);
    }

    public boolean has(String key) {
        return jsonMap.containsKey(key);
    }
}
