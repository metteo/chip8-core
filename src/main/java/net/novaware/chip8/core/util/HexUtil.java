package net.novaware.chip8.core.util;

import static net.novaware.chip8.core.util.UnsignedUtil.uint;

public class HexUtil {
    public static String toHexString(short value) {
        return String.format("0x%04X", uint(value));
    }

    public static String toHexString(byte value) {
        return String.format("0x%02X", uint(value));
    }
}
