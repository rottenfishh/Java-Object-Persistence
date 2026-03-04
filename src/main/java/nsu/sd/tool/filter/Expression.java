package nsu.sd.tool.filter;

import nsu.sd.tool.JsonKeysReader;

public interface Expression {
    boolean evaluate(JsonKeysReader keysReader);
}
