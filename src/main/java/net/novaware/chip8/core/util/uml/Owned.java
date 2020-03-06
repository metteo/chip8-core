package net.novaware.chip8.core.util.uml;

import java.lang.annotation.*;

/**
 * Composition
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Owned {

    /**
     * (Optional) The class that owns the field
     *
     * <p> Defaults to the type that stores the association.
     *
     * @return class which owns the target field
     */
    Class<?> by() default void.class;
}
