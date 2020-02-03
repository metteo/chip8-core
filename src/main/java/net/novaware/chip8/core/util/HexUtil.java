package net.novaware.chip8.core.util;

public class HexUtil {
    public static String toHexString(short value) {
        return String.format("0x%04X", value);
    }
}
