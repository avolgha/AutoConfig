package dev.marius.autoconfig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates the given field as property
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Property {
    /**
     * @return Returns the name of the value it will have in the config
     */
    String name();

    /**
     * @return Returns the type of the value it will have in the config
     */
    PropertyType type();
}
