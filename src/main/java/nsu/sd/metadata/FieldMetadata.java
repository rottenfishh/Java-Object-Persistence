package nsu.sd.metadata;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * метаданные поля хранят его имя, объект Field, полученный через reflection
 * тип поля, и флаги сериализации: lazy, ignore(не сериализовать), unwrapped(поле-это вложенный объект)
 */
@Getter
@Setter
public class FieldMetadata {
    String name;
    Field field;
    Type type;
    boolean lazy;
    boolean ignore;
    boolean unwrapped;
}
