package net.novaware.chip8.core.cpu.unit;

import net.novaware.chip8.core.cpu.register.Registers;
import net.novaware.chip8.core.memory.Memory;

import static net.novaware.chip8.core.cpu.register.Registers.GC_ERASE;
import static net.novaware.chip8.core.cpu.register.Registers.VF_COLLISION;
import static net.novaware.chip8.core.util.UnsignedUtil.*;

/**
 * GPU
 */
public class GraphicsProcessing {

    public static final int MAX_SPRITE_HEIGHT = 0x10;
    public static final int MAX_HEIGHT = 32; //bits
    public static final int MAX_WIDTH = 64; //bits

    private final Registers registers;

    private final Memory memory;

    private final byte[] spriteBuffer;

    private final byte[] paintBuffer;

    public GraphicsProcessing(Registers registers, Memory memory) {
        this.registers = registers;
        this.memory = memory;

        spriteBuffer = new byte[MAX_SPRITE_HEIGHT];
        paintBuffer = new byte[MAX_SPRITE_HEIGHT];
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

        final short spriteAddress = registers.getIndex().get();
        final int spriteHeight = uint(height);

        fillSpriteBuffer(spriteAddress, spriteBuffer, spriteHeight);
        fillPaintBuffer(xBit, yBit, paintBuffer, height);
        xorBuffers();
        storePaintBuffer();
    }

    /* package */ void fillSpriteBuffer(final short address, final byte[] buffer, final int height) {
        memory.getBytes(address, buffer, height);
    }

    /* package */ void fillPaintBuffer(int xBit, int yBit, final byte[] buffer, final int height) {
        final boolean wrapping = true; //TODO: make configurable
        final boolean clipping = false; //TODO: make configurable

        if (wrapping) {
            yBit %= MAX_HEIGHT;
            xBit %= MAX_WIDTH;
        } else {
            //TODO: handle no wrapping scenario: do not draw anything
        }

        final int graphicSegment = registers.getGraphicSegment().getAsInt();

        for (int row = 0; row < height; ++row) {
            int currentYBit = yBit + row;

            if (!clipping) {
                currentYBit %= MAX_HEIGHT;
            } else {
                //TODO: handle y clipping
            }

            int memorySegmentYByte = graphicSegment + (currentYBit * MAX_WIDTH / 8);
            int memorySegmentXByte1 = xBit / 8;
            int memorySegmentXByte2 = memorySegmentXByte1 + 1;

            if (!clipping) {
                memorySegmentXByte2 %= MAX_WIDTH / 8;
            } else {
                //TODO: handle x clipping
            }

            int byteIndex = xBit % 8;

            byte rowData;
            if (byteIndex == 0) { // byte aligned
                short rowIndex = ushort(memorySegmentYByte + memorySegmentXByte1);
                rowData = memory.getByte(rowIndex);
            } else { // misaligned
                short rowIndex1 = ushort(memorySegmentYByte + memorySegmentXByte1);
                short rowIndex2 = ushort(memorySegmentYByte + memorySegmentXByte2);

                byte rowData1 = memory.getByte(rowIndex1);
                byte rowData2 = memory.getByte(rowIndex2);

                int rowData1Aligned = uint(rowData1) << byteIndex;
                int rowData2Aligned = uint(rowData2) >>> (8 - byteIndex);

                rowData = ubyte(rowData1Aligned | rowData2Aligned);
            }

            buffer[row] = rowData;

        }
    }

    /* package */ void xorBuffers() {

    }


    /* package */ void storePaintBuffer() {

    }
}
