package nsu.sd.tool.filter;

import lombok.AllArgsConstructor;
import nsu.sd.tool.JsonKeysReader;

@AllArgsConstructor
public class NOT implements Expression{

    private final Expression expression;

    @Override
    public boolean evaluate(JsonKeysReader keysReader) {
        return !expression.evaluate(keysReader);
    }
}
