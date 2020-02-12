package net.novaware.chip8.core.cpu.unit;

import net.novaware.chip8.core.cpu.register.Registers;
import net.novaware.chip8.core.memory.Memory;
import net.novaware.chip8.core.util.ViewPort;
import net.novaware.chip8.core.util.ViewPort.Index;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.chip8.core.cpu.register.Registers.*;
import static net.novaware.chip8.core.util.UnsignedUtil.*;
import static net.novaware.chip8.core.util.ViewPort.Bit;

/**
 * GPU
 */
public class GraphicsProcessing {

    private static final Logger LOG = LogManager.getLogger();

    public static final int MAX_SPRITE_HEIGHT = 0x10;

    private final Registers registers;

    private final Memory memory;

    private final ViewPort viewPort = new ViewPort();

    private byte[] spriteBuffer;

    private byte[] paintBuffer;

    private byte[] resultBuffer;

    final boolean wrapping = true; //TODO: make configurable
    final boolean clipping = false; //TODO: make configurable
    final boolean dump = true; //TODO: debugging UI

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

    //TODO: write a combined test
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
        byte gc = xorBuffers(xBit, yBit, spriteHeight);
        storePaintBuffer(xBit, yBit, resultBuffer, spriteHeight);

        registers.getStatus().set((gc == GC_ERASE || gc == GC_MIX) ? 0x1 : 0x0);
        registers.getStatusType().set(VF_COLLISION);

        registers.getGraphicChange().set(gc); // must be last because triggers redraw
    }

    /* package */ void fillSpriteBuffer(final short address, final byte[] buffer, final int height) {
        memory.getBytes(address, buffer, height);

        if (dump) dumpBuffer("sprite", ushort(height), buffer);
    }

    /* package */ void fillPaintBuffer(int xBit, int yBit, final byte[] buffer, final int height) {
        //TODO: get rid of these instantiations from emulator loop, maybe use pooling
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

        if (dump) dumpBuffer("paint " + xBit + ", " + yBit + " ", ushort(height), buffer);
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
    /* package */ byte xorBuffers(int xBit, int yBit, final int height) {
        boolean erasing = false;
        boolean drawing = false;

        for (int y = 0; y < height; ++y) {
            final @Unsigned int spriteRow = uint(spriteBuffer[y]);
            final @Unsigned int paintRow = uint(paintBuffer[y]);

            resultBuffer[y] = ubyte(spriteRow ^ paintRow);

            final @Unsigned int resultRow = uint(resultBuffer[y]);

            // inverse logical consequence ~(p => q) <=> ~(~p | q) <=> p & ~q
            // 1->0 = 1, the rest is 0
            if ((paintRow & ~resultRow) > 0) {
                erasing = true;
            }

            // special ~p & q
            // 0->1 = 1, the rest is 0
            if ((~paintRow & resultRow) > 0) {
                drawing = true;
            }
        }

        if (dump) dumpBuffer("result", ushort(height), resultBuffer);

        return getGraphicChange(erasing, drawing);
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

    //TODO: refactor when covered with more tests
    /* package */ void storePaintBuffer(int xBit, int yBit, final byte[] buffer, final int height) {
        //TODO: get rid of these instantiations from emulator loop, maybe use pooling
        final Bit bit = new Bit(xBit, yBit);
        final Index idx1 = new Index(); // for byte aligned memory access
        final Index idx2 = new Index(); // additional, for misaligned case

        final int graphicSegment = registers.getGraphicSegment().getAsInt();

        for (int row = 0; row < height; ++row) {
            int currentYBit = yBit + row;

            bit.x = xBit;
            bit.y = currentYBit;

            viewPort.toIndex(bit, idx1, true);

            if (idx1.byteBit == 0) { // byte aligned
                memory.setByte(ushort(graphicSegment + idx1.arrayByte), buffer[row]);
            } else { // misaligned
                bit.x += 8;
                viewPort.toIndex(bit, idx2, true);

                short rowIndex1 = ushort(graphicSegment + idx1.arrayByte);
                short rowIndex2 = ushort(graphicSegment + idx2.arrayByte);

                //TODO: maybe store this part of memory on hand when filling so it's available here?
                int rowData1 = uint(memory.getByte(rowIndex1));
                int rowData2 = uint(memory.getByte(rowIndex2));

                int mask1 = (1 << (8 - idx1.byteBit)) - 1 ;
                int mask2 = uint(ubyte(~mask1)); // cut off unneeded 1s on the front

                rowData1 = (rowData1 & mask2) | (uint(buffer[row]) >>> idx1.byteBit);
                rowData2 = (rowData2 & mask1) | (uint(buffer[row]) << (8 - idx1.byteBit));

                memory.setByte(rowIndex1, ubyte(rowData1));
                memory.setByte(rowIndex2, ubyte(rowData2));
            }
        }
    }

    private void dumpBuffer(String title, short height, byte[] buffer) {
        LOG.debug("buffer: " + title);
        for (int i = 0; i < height; i++) { //y
            StringBuilder sb = new StringBuilder();

            for (int j = 7; j >= 0; j--) { //x
                int mask = 0x1 << j;
                int pixel = (uint(buffer[i]) & mask) >>> j;

                sb.append(pixel != 0 ? "█" : "░");
            }
            LOG.debug(sb);
        }
    }
}
