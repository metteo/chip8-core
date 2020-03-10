package net.novaware.chip8.core.util;

import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Collection of assertions for method arguments and object state.
 *
 */
public final class AssertUtil {

    private static final String MESSAGE_NOT_NULL = "message must not be null";
    private static final String MESSAGE_SUPPLIER_NOT_NULL = "message supplier must not be null";

    private AssertUtil() {
        //utility class
    }

    public static void assertArgument(final boolean assertion, final String message) {
        requireNonNull(message, MESSAGE_NOT_NULL);

        if (!assertion) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void assertArgument(final boolean assertion, final Supplier<String> message) {
        requireNonNull(message, MESSAGE_SUPPLIER_NOT_NULL);

        assertArgument(assertion, message.get());
    }

    public static void assertState(final boolean assertion, final String message) {
        requireNonNull(message, MESSAGE_NOT_NULL);

        if (!assertion) {
            throw new IllegalStateException(message);
        }
    }

    public static void assertState(final boolean assertion, final Supplier<String> message) {
        requireNonNull(message, MESSAGE_SUPPLIER_NOT_NULL);

        assertState(assertion, message.get());
    }
}
