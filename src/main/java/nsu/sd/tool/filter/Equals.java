package nsu.sd.tool.filter;

import lombok.AllArgsConstructor;
import nsu.sd.tool.JsonKeysReader;

@AllArgsConstructor
public class Equals implements Expression{

    private final String key;
    private final Object value;

    @Override
    public boolean evaluate(JsonKeysReader keysReader) {
        if(!keysReader.has(key)) return false;
        return keysReader.get(key).equals(value);
    }
}
