package net.novaware.chip8.core.util;

import org.checkerframework.checker.signedness.qual.Unsigned;

public class UnsignedUtil {

    public static final @Unsigned short USHORT_0 = ushort(0x0000);
    public static final @Unsigned byte UBYTE_0 = ubyte(0x00);

    public static final @Unsigned short USHORT_MAX_VALUE = ushort(0xFFFF);
    public static final @Unsigned byte UBYTE_MAX_VALUE = ubyte(0xFF);

    public static @Unsigned int uint(short s) {
        return s & 0xFFFF;
    }

    public static @Unsigned int uint(byte b) {
        return b & 0xFF;
    }

    public static @Unsigned short ushort(int i) {
        return (short) i;
    }

    public static @Unsigned short ushort(byte b) {
        return (short) (b & 0xFF);
    }

    public static @Unsigned byte ubyte(int i) {
        return (byte) i;
    }

    public static @Unsigned byte ubyte(short s) {
        return (byte) s;
    }
}
