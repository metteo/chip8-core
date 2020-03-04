package net.novaware.chip8.core.util.di;

import javax.inject.Scope;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Identifies a type that the injector only instantiates once per {@link net.novaware.chip8.core.Board}. Not inherited.
 *
 * @see javax.inject.Scope @Scope
 * @see javax.inject.Singleton @Singleton
 */
@Scope
@Documented
@Retention(RUNTIME)
public @interface BoardScope {
}
