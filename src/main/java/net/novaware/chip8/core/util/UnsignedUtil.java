package net.novaware.chip8.core.util;

import org.checkerframework.checker.signedness.qual.Unsigned;

public class UnsignedUtil {

    //TODO: make constants for unsigned byte 0 / short 0 etc

    public static @Unsigned int uint(short s) {
        return s & 0xFFFF;
    }

    public static @Unsigned int uint(byte b) {
        return b & 0xFF;
    }

    public static @Unsigned short ushort(int i) {
        //TODO: maybe log truncation?
        return (short) i;
    }

    public static @Unsigned short ushort(byte b) {
        return (short) (b & 0xFF);
    }

    public static @Unsigned byte ubyte(int i) {
        //TODO: maybe log truncation?
        return (byte) i;
    }

    public static @Unsigned byte ubyte(short s) {
        //TODO: maybe log truncation?
        return (byte) s;
    }
}
