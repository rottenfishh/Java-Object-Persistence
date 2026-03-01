package nsu.sd.metadata;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * метаданные класса хранят сам кдасс объекта, полученный через reflection, имя класса
 * флаг сериалзиации, и все поля объекта
 */
@Getter
@Setter
public class ClassMetadata {
    Class<?> clazz;
    String name;
    boolean serializable;
    Map<String, FieldMetadata> fields;
}
