package net.novaware.chip8.core.util;

import org.checkerframework.checker.signedness.qual.Unsigned;

public class UnsignedUtil {

    @Unsigned
    public static int uint(short s) {
        return s & 0xFFFF;
    }

    @Unsigned
    public static int uint(byte b) {
        return b & 0xFF;
    }

    @Unsigned
    public static short ushort(int i) {
        return (short) i;
    }

    @Unsigned
    public static short ushort(byte b) {
        return (short) (b & 0xFF);
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
