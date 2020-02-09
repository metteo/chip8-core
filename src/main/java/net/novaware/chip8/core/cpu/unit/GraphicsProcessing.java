package net.novaware.chip8.core.cpu.unit;

import net.novaware.chip8.core.cpu.register.Registers;
import net.novaware.chip8.core.memory.Memory;
import net.novaware.chip8.core.util.ViewPort;
import net.novaware.chip8.core.util.ViewPort.Index;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.chip8.core.cpu.register.Registers.*;
import static net.novaware.chip8.core.util.UnsignedUtil.*;
import static net.novaware.chip8.core.util.ViewPort.Bit;

/**
 * GPU
 */
public class GraphicsProcessing {

    public static final int MAX_SPRITE_HEIGHT = 0x10;

    private final Registers registers;

    private final Memory memory;

    private final ViewPort viewPort = new ViewPort();

    private byte[] spriteBuffer;

    private byte[] paintBuffer;

    private byte[] resultBuffer;

    final boolean wrapping = true; //TODO: make configurable
    final boolean clipping = false; //TODO: make configurable

    public GraphicsProcessing(Registers registers, Memory memory) {
        this.registers = registers;
        this.memory = memory;

        spriteBuffer = new byte[MAX_SPRITE_HEIGHT];
        paintBuffer = new byte[MAX_SPRITE_HEIGHT];
        resultBuffer = new byte[MAX_SPRITE_HEIGHT];
    }

    public void clearScreen() {
        final int gs = registers.getGraphicSegment().getAsInt();

        for(int i = 0; i < 32 * 64 / 8; ++i) {
            memory.setByte(ushort(gs + i), ubyte(0));
        }

        registers.getStatus().set(0x1);
        registers.getStatusType().set(VF_COLLISION);

        registers.getGraphicChange().set(GC_ERASE);
    }

    public void drawSprite(short x, short y, short height) {
        final int xBit = registers.getVariable(x).getAsInt();
        final int yBit = registers.getVariable(y).getAsInt();

        if (viewPort.isOutOfBounds(xBit, yBit) && !wrapping) {
            //TODO: handle no wrapping scenario: do not read from memory
            return;
        }

        final short spriteAddress = registers.getIndex().get();
        final int spriteHeight = uint(height);

        fillSpriteBuffer(spriteAddress, spriteBuffer, spriteHeight);
        fillPaintBuffer(xBit, yBit, paintBuffer, spriteHeight);
        xorBuffers(xBit, yBit, spriteHeight);
        storePaintBuffer(xBit, yBit, resultBuffer, spriteHeight);
    }

    /* package */ void fillSpriteBuffer(final short address, final byte[] buffer, final int height) {
        memory.getBytes(address, buffer, height);
    }

    /* package */ void fillPaintBuffer(int xBit, int yBit, final byte[] buffer, final int height) {
        final Bit bit = new Bit(xBit, yBit);
        final Index idx1 = new Index(); // for byte aligned memory access
        final Index idx2 = new Index(); // additional, for misaligned case

        final int graphicSegment = registers.getGraphicSegment().getAsInt();

        for (int row = 0; row < height; ++row) {
            int currentYBit = yBit + row;

            bit.x = xBit;
            bit.y = currentYBit;

            viewPort.toIndex(bit, idx1, true);

            byte rowData;
            if (idx1.byteBit == 0) { // byte aligned
                rowData = getRowData(graphicSegment, idx1);
            } else { // misaligned
                bit.x += 8;
                viewPort.toIndex(bit, idx2, true);
                rowData = getRowData(graphicSegment, idx1, idx2);
            }

            buffer[row] = rowData;
        }
    }

    private byte getRowData(int graphicSegment, Index idx1) {
        short rowIndex = ushort(graphicSegment + idx1.arrayByte);
        return memory.getByte(rowIndex);
    }

    private byte getRowData(int graphicSegment, Index idx1, Index idx2) {
        short rowIndex1 = ushort(graphicSegment + idx1.arrayByte);
        short rowIndex2 = ushort(graphicSegment + idx2.arrayByte);

        byte rowData1 = memory.getByte(rowIndex1);
        byte rowData2 = memory.getByte(rowIndex2);

        int rowData1Aligned = uint(rowData1) << idx1.byteBit;
        int rowData2Aligned = uint(rowData2) >>> (8 - idx1.byteBit);

        return ubyte(rowData1Aligned | rowData2Aligned);
    }

    //TODO: handle clipping in xor method
    /* package */ void xorBuffers(int xBit, int yBit, final int height) {
        boolean erasing = false;
        boolean drawing = false;

        for (int y = 0; y < height; ++y) {
            final @Unsigned int spriteRow = uint(spriteBuffer[y]);
            final @Unsigned int paintRow = uint(paintBuffer[y]);

            resultBuffer[y] = ubyte(spriteRow ^ paintRow);

            final @Unsigned int resultRow = uint(resultBuffer[y]);

            // inverse logical consequence ~(p => q) <=> ~(~p v q) <=> p ^ ~q
            if ((paintRow & ~resultRow) > 0) {
                erasing = true;
            }

            // special ~p & q
            if ((~paintRow & resultRow) > 0) {
                drawing = true;
            }
        }

        registers.getStatus().set(erasing ? 0x1 : 0x0);
        registers.getStatusType().set(VF_COLLISION);

        registers.getGraphicChange().set(getGraphicChange(erasing, drawing));
    }

    private byte getGraphicChange(boolean erasing, boolean drawing) {
        byte gc;
        if (drawing && erasing) {
            gc = GC_MIX;
        } else if (drawing) {
            gc = GC_DRAW;
        } else if (erasing) {
            gc = GC_ERASE;
        } else {
            gc = GC_NOOP;
        }
        return gc;
    }


    /* package */ void storePaintBuffer(int xBit, int yBit, final byte[] buffer, final int height) {

    }
}
