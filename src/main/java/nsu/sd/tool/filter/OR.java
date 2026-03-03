package nsu.sd.tool.filter;

import lombok.AllArgsConstructor;
import nsu.sd.tool.JsonKeysReader;

@AllArgsConstructor
public class OR implements Expression{

    private final Expression left;
    private final Expression right;

    @Override
    public boolean evaluate(JsonKeysReader keysReader) {
        return left.evaluate(keysReader) || right.evaluate(keysReader);
    }
}
