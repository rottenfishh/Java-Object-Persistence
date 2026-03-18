package exception;

import java.io.IOException;

public class TypeNotSupportedException extends IOException {
    public TypeNotSupportedException(String message) {
        super(message);
    }
}