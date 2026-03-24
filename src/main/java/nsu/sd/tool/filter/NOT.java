package nsu.sd.tool.filter;

import lombok.AllArgsConstructor;
import nsu.sd.tool.JsonKeysReader;

import java.util.Set;

@AllArgsConstructor
public class NOT implements Expression{

    private final Expression expression;

    @Override
    public boolean evaluate(JsonKeysReader keysReader) {
        return !expression.evaluate(keysReader);
    }

    @Override
    public Set<String> requiredFields() {
        return expression.requiredFields();
    }
}
