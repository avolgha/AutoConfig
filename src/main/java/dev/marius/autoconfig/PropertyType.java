package dev.marius.autoconfig;

import java.util.stream.Stream;

public enum PropertyType {
    STRING(String.class),
    INTEGER(String.class, Integer.class, Double.class, Float.class, Long.class, Short.class),
    DOUBLE(String.class, Integer.class, Double.class, Float.class),
    FLOAT(String.class, Integer.class, Double.class, Float.class),
    LONG(String.class, Integer.class, Long.class),
    SHORT(String.class, Integer.class, Short.class),
    BYTE(String.class, Byte.class),
    BOOLEAN(String.class, Boolean.class),
    REFERENCE(String.class),
    NULL(String.class);

    private final Class<?>[] supportedTypes;

    PropertyType(Class<?>... supportedTypes) {
        this.supportedTypes = supportedTypes;
    }

    /**
     * @return Check if the class type is supported by the property type
     */
    public boolean isSupported(Class<?> type) {
        return Stream.of(this.supportedTypes).anyMatch(current -> current == type);
    }
}
