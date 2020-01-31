package net.novaware.chip8.core.util;

import org.checkerframework.checker.signedness.qual.Unsigned;

//TODO own implementation with tests instead of function calls
public class UnsignedUtil {

    @Unsigned
    public static int uint(@Unsigned short s) {
        return Short.toUnsignedInt(s);
    }

    @Unsigned
    public static int uint(@Unsigned byte b) {
        return Byte.toUnsignedInt(b);
    }

    @Unsigned
    public static short ushort(int i) {
        return (short) i;
    }

    @Unsigned
    public static short ushort(byte b) {
        return (short) uint(b);
    }

    @Unsigned
    public static byte ubyte(int i) {
        return (byte) i;
    }

    @Unsigned
    public static byte ubyte(short s) {
        return (byte) s;
    }
}
