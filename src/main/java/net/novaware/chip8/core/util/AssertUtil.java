package net.novaware.chip8.core.util;

public class AssertUtil {

    public static void assertArgument(final boolean condition, final String message) {
        //FIXME: condition should be negated! when fixing, update all usages
        if (condition) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void assertState(final boolean condition, final String message) {
        //FIXME: condition should be negated! when fixing, update all usages
        if (condition) {
            throw new IllegalStateException(message);
        }
    }
}
