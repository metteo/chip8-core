package net.novaware.chip8.core.gpu;

import static java.util.Objects.requireNonNull;

public class ViewPort {

    public static final int DEFAULT_MAX_HEIGHT = 32; //bits
    public static final int DEFAULT_MAX_WIDTH = 64; //bits

    private final int maxHeight;
    private final int maxWidth;

    public static class Bit {
        public int x;
        public int y;

        public Bit() {}

        public Bit(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public static class Index {
        public int arrayByte;
        public int byteBit;
    }

    public ViewPort(int maxWidth, int maxHeight) {
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    public ViewPort() {
        this(DEFAULT_MAX_WIDTH, DEFAULT_MAX_HEIGHT);
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    /**
     * Converts bit based 2d coordinates into 1d array/bit index
     * @param b source
     * @param i destination
     * @param wrapping if true coordinates out of bounds will wrap around
     * @return false if coords are out of bounds and wrapping is disabled
     */
    public boolean toIndex(Bit b, Index i, boolean wrapping) {
        requireNonNull(b, "b is required");
        requireNonNull(i, "i is required");

        if (b.x < 0 || b.y < 0) {
            throw new IllegalArgumentException("both and x & y must be positive");
        }

        int xBit = b.x;
        int yBit = b.y;

        if (wrapping) {
            xBit %= maxWidth;
            yBit %= maxHeight;
        } else if (isOutOfBounds(xBit, yBit)) {
            i.arrayByte = -1;
            i.byteBit = -1;
            return false;
        }

        int arrayByte = yBit * maxWidth / 8 + xBit / 8;
        int byteBit = xBit % 8;

        i.arrayByte = arrayByte;
        i.byteBit = byteBit;

        return true;
    }

    public boolean isOutOfBounds(int xBit, int yBit) {
        return xBit >= maxWidth || yBit >= maxHeight;
    }
}
