package nsu.sd.tool.filter;

import lombok.AllArgsConstructor;
import nsu.sd.tool.JsonKeysReader;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
public class OR implements Expression{

    private final Expression left;
    private final Expression right;

    @Override
    public boolean evaluate(JsonKeysReader keysReader) {
        return left.evaluate(keysReader) || right.evaluate(keysReader);
    }

    @Override
    public Set<String> requiredFields() {
        Set<String> fields = new HashSet<>();

        fields.addAll(left.requiredFields());
        fields.addAll(right.requiredFields());

        return fields;
    }
}
