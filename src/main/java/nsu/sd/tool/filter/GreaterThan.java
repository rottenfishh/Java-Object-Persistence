package nsu.sd.tool.filter;

import lombok.AllArgsConstructor;
import nsu.sd.tool.JsonKeysReader;

@AllArgsConstructor
public class GreaterThan implements Expression{

    private final String key;
    private final Double num;

    @Override
    public boolean evaluate(JsonKeysReader keysReader) {
        if(keysReader.has(key)) return false;
        Object value = keysReader.get(key);
        if(!(value instanceof Number)) return false;
        return ((Number)value).doubleValue() > num;
    }
}
