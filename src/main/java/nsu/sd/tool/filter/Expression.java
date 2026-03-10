package nsu.sd.tool.filter;

import nsu.sd.tool.JsonKeysReader;
import java.util.Set;

public interface Expression {
    boolean evaluate(JsonKeysReader keysReader);

    Set<String> requiredFields();
}
